/*
 * GDMatrix
 *  
 * Copyright (C) 2020, Ajuntament de Sant Feliu de Llobregat
 *  
 * This program is licensed and may be used, modified and redistributed under 
 * the terms of the European Public License (EUPL), either version 1.1 or (at 
 * your option) any later version as soon as they are approved by the European 
 * Commission.
 *  
 * Alternatively, you may redistribute and/or modify this program under the 
 * terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation; either  version 3 of the License, or (at your option) 
 * any later version. 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *    
 * See the licenses for the specific language governing permissions, limitations 
 * and more details.
 *    
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along 
 * with this program; if not, you may find them at: 
 *    
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/ 
 * and 
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.santfeliu.doc.web;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.activation.DataHandler;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.ws.WebServiceException;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentConstants;
import org.matrix.security.AccessControl;
import org.santfeliu.doc.client.CachedDocumentManagerClient;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.util.FileDataSource;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author unknown
 */
public class DocumentWriter
{
  public DocumentWriter(String tempFilePath)
  {
  }

  public void processRequest(HttpServletRequest request,
    HttpServletResponse response)
  {
    try
    {
      ObjectInputStream is = new ObjectInputStream(request.getInputStream());
      try
      {
        ObjectOutputStream os = new ObjectOutputStream(response.getOutputStream());
        try
        {
          Credentials credentials;
          HttpSession session = request.getSession();
          UserSessionBean userSessionBean = 
            (UserSessionBean)session.getAttribute("userSessionBean");
          if (userSessionBean != null)
          {
            credentials = userSessionBean.getCredentials();
          }
          else
          {
            credentials = new Credentials();
          }
          CachedDocumentManagerClient client =
            new CachedDocumentManagerClient(credentials.getUserId(),
              credentials.getPassword());

          String operation = (String)is.readObject();
          System.out.println("operation: " + operation);

          if ("loadDocument".equals(operation))
          {
            String docId = (String)is.readObject();
            System.out.println("loadDocument " + docId);
            Document document = 
              client.loadDocument(docId, 0, ContentInfo.METADATA);
            Map properties = new HashMap();
            properties.put("docId", document.getDocId());
            properties.put("version", document.getVersion());
            properties.put("docTypeId", document.getDocTypeId());
            properties.put("title", document.getTitle());
            properties.put("language", document.getLanguage());
            properties.put("contentId", document.getContent().getContentId());
            properties.put("contentType", document.getContent().getContentType());
            properties.put("lockUserId", document.getLockUserId());
            os.writeObject(properties);
            os.flush();
          }
          else if ("storeDocument".equals(operation))
          {
            Map properties = (Map)is.readObject();
            System.out.println("storeDocument " + properties);
            String contentId = (String)properties.remove("contentId");
            String contentType = (String)properties.remove("contentType");
            if (contentId != null)
            {
              Document document = new Document();
              document.setIncremental(true);

              File file = getFileFromContentId(contentId);
              DataHandler dataHandler = new DataHandler(
                new FileDataSource(file));
              Content content = new Content();
              content.setData(dataHandler);
              content.setContentType(contentType);
              document.setContent(content);

              properties = toMultivaluedPropertiesMap(properties);
              setSecurity(document, properties);
              DocumentUtils.setProperties(document, properties);

              document = client.storeDocument(document);
              file.delete();
              properties.clear();
              properties.put("docId", document.getDocId());
              properties.put("version", document.getVersion());
              properties.put("docTypeId", document.getDocTypeId());
              properties.put("title", document.getTitle());
              properties.put("language", document.getLanguage());
              properties.put("contentId", document.getContent().getContentId());
              os.writeObject(properties);
              os.flush();
            }
            else throw new Exception("No contentId");
          }
          else if ("lockDocument".equals(operation))
          {
            String docId = (String)is.readObject();
            System.out.println("lockDocument " + docId);
            client.lockDocument(docId, 0);
            os.writeObject(Boolean.TRUE);
            os.flush();
          }
          else if ("unlockDocument".equals(operation))
          {
            String docId = (String)is.readObject();
            System.out.println("unlockDocument " + docId);
            client.unlockDocument(docId, 0);
            os.writeObject(Boolean.TRUE);
            os.flush();
          }
          else throw new Exception("Invalid operation");
        }
        catch (WebServiceException e)
        {
          e.printStackTrace();
          os.writeObject(new Exception(e.getMessage()));
          os.flush();
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
          os.writeObject(ex);
          os.flush();
        }
        finally
        {
          os.close();
        }
      }
      finally
      {
        is.close();
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  void processUpload(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException
  {
    String uri = request.getRequestURI();
    int index = uri.lastIndexOf("/");
    String contentId = uri.substring(index + 1);
    File file = getFileFromContentId(contentId);
    InputStream is = request.getInputStream();
    try
    {
      BufferedOutputStream bos =
      new BufferedOutputStream(new FileOutputStream(file, true), 4096);
      try
      {
        int b = is.read();
        while (b != -1)
        {
          bos.write(b);
          b = is.read();
        }
      }
      finally
      {
        bos.close();
      }
    }
    finally
    {
      is.close();
    }
  }

  private File getFileFromContentId(String contentId)
  {
    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    File file = new File(tmpDir, contentId + ".tmp");
    return file;
  }
  
  private Map<String,Object> toMultivaluedPropertiesMap(Map<String, Object> properties)
  {
    Map<String, Object> result = new HashMap();

    Set<Entry<String, Object>> entrySet = properties.entrySet();
    for (Entry entry : entrySet)
    {
      String key = (String)entry.getKey();
      if (key.contains(":"))
        key = key.substring(0, key.indexOf(":"));
      
      Object value = entry.getValue();
      if (value instanceof List)
        result.put(key, value);
      else
      {
        List list = (List)result.get(key);
        if (list == null)
          list = new ArrayList();
        list.add(entry.getValue());
        result.put(key, list);
      }
    }

    return result;
  }

  private void setSecurity(Document document,
    Map<String, Object> properties)
  {
    List<String> roles =
      (List<String>)properties.get(DocumentConstants.READ_ROLE);
    if (roles != null)
    {
      for (String rol : roles)
      {
        AccessControl acl = new AccessControl();
        acl.setRoleId(rol);
        acl.setAction("Read");
        document.getAccessControl().add(acl);
      }
      properties.remove(DocumentConstants.READ_ROLE);
    }

    roles = (List<String>)properties.get(DocumentConstants.WRITE_ROLE);
    if (roles != null)
    {
      for (String rol : roles)
      {
        AccessControl acl = new AccessControl();
        acl.setRoleId(rol);
        acl.setAction("Write");
        document.getAccessControl().add(acl);
      }
      properties.remove(DocumentConstants.WRITE_ROLE);
    }

    roles = (List<String>)properties.get(DocumentConstants.DELETE_ROLE);
    if (roles != null)
    {
      for (String rol : roles)
      {
        AccessControl acl = new AccessControl();
        acl.setRoleId(rol);
        acl.setAction("Delete");
        document.getAccessControl().add(acl);
      }
      properties.remove(DocumentConstants.DELETE_ROLE);
    }
  }

  private void setProperties(Document document,
    Map<String, Object> properties) throws Exception
  {
    Map<String, Object> resultMap = toMultivaluedPropertiesMap(properties);
    DocumentUtils.setProperties(document, resultMap);
  }
}
