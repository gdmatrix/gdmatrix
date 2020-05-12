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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import org.apache.commons.io.IOUtils;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.RelatedDocument;
import org.matrix.doc.RelationType;
import org.matrix.doc.State;
import org.santfeliu.doc.client.CachedDocumentManagerClient;
import org.santfeliu.doc.util.HtmlFixer;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TemporaryDataSource;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 */
public class DocumentEditor implements Serializable
{
  private Document document;
  private String documentData;
  private Map configProperties = new HashMap();

  public DocumentEditor(String docId) throws Exception
  {
    document = getClient().loadDocument(docId, 0, ContentInfo.ALL);
    if (document == null)
      throw new Exception("DOCUMENT_NOT_FOUND");
  }

  public Map getConfigProperties()
  {
    return configProperties;
  }

  public void setConfigProperties(Map configProperties)
  {
    this.configProperties = configProperties;
  }

  public String getDocumentData()
  {
    return documentData;
  }

  public void setDocumentData(String documentData)
  {
    this.documentData = documentData;
  }

  public void setDocument(Document document)
  {
    this.document = document;
  }

  public Document getDocument()
  {
    return document;
  }

  public boolean isLockUser()
  {
    return isLockUser(document.getLockUserId());
  }

  public void editDocument(boolean editTranslation)
    throws Exception
  {
    try
    {
      if (document == null)
        throw new Exception("CANNOT_EDIT_NULL_DOCUMENT");
      else if (document.getContent() != null)
      {
        String viewLanguage = FacesUtils.getViewLanguage();
        //Translation
        if (editTranslation)
        {
          if (!viewLanguage.equals(document.getLanguage()))
          {
            document = getTranslation(document, viewLanguage);
          }
        }

        if (lockDocument())
        {
          configProperties.put("DefaultLanguage", viewLanguage);

          //Getting content data
          DataHandler dataHandler = document.getContent().getData();
          this.documentData = readDocument(dataHandler.getDataSource());
          configProperties.put("EditorAreaCSS", getEditorAreaCSS());
          //CSS list
          configProperties.put("ContentsCSS", getCSSList());
        }
      }
    }
    finally
    {
      if (document != null && document.getContent() != null)
        document.getContent().setData(null);
    }
  }

  public void storeDocument() throws Exception
  {
    storeDocument(false);
  }

  public void storeDocument(boolean keepLocking) throws Exception
  {
    try
    {
      if (document == null)
        throw new Exception("CANNOT_STORE_NULL_DOCUMENT");
      else
      {
        if (!keepLocking)
          unlockDocument();
        TemporaryDataSource dataSource =
          
          writeDocument(documentData, document.getContent().getContentType());
        DataHandler dataHandler = new DataHandler(dataSource);
        document.getContent().setContentId(null);
        document.getContent().setData(dataHandler);
        document.setIncremental(true);
        document.setState(State.COMPLETE);
        getClient().storeDocument(document);
      }
    }
    finally
    {
      if (document != null && document.getContent() != null)
        document.getContent().setData(null);
    }
  }

  /*
   * return if current user is the lockUser
   */
  public boolean lockDocument() throws Exception
  {
    if (document == null)
      throw new Exception("CANNOT_LOCK_NULL_DOCUMENT");

    String lockuser = document.getLockUserId();
    int version = document.getVersion();
    if (lockuser == null || isLockUser(lockuser))
    {
      if (lockuser == null)
      {
        getClient().lockDocument(document.getDocId(), version);
        document.setLockUserId(UserSessionBean.getCurrentInstance().getUsername());
      }
      return true;
    }
    else if (lockuser != null)
      throw new DocumentLockedByUser(lockuser);
    
    return false;
  }

  /*
   * return if document has been unlocked
   */
  public boolean unlockDocument() throws Exception
  {
    if (document == null)
      throw new Exception("CANNOT_UNLOCK_NULL_DOCUMENT");

    getClient().unlockDocument(document.getDocId(), document.getVersion());
    document.setLockUserId(null);

    return true;
  }
  
  private List<String> getCSSList()
  {
    List list = new ArrayList();
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();    

    list.add("/templates/" + userSessionBean.getTemplate() + "/css/template.css");
    list.add("/themes/" + userSessionBean.getTheme() + "/theme.css");
    for (String nodeCSS : userSessionBean.getNodeCSS())
    {
      list.add(nodeCSS);
    }
    return list;
  }

  private String getEditorAreaCSS()
  {
    String buffer = "";
    List cssList = getCSSList();
    for (int i = 0; i < cssList.size(); i++)
    {
      buffer += (i > 0 ? "," : "") + cssList.get(i);
    }
    return buffer;
  }
  
  private CachedDocumentManagerClient getClient()
    throws Exception
  {
    CachedDocumentManagerClient client = new CachedDocumentManagerClient(
      UserSessionBean.getCurrentInstance().getUsername(),
      UserSessionBean.getCurrentInstance().getPassword());

    return client;
  }

  private String readDocument(DataSource dataSource) throws IOException
  {
    String result = null;
    InputStream is = dataSource.getInputStream();
    try
    {
      StringBuffer sb = new StringBuffer();
      int numRead;
      byte[] bytes = new byte[1024];
      while ((numRead = is.read(bytes)) != -1)
      {
        sb.append(new String(bytes).toCharArray(), 0, numRead);
      }
      result = sb.toString();
    }
    finally
    {
      is.close();
    }
    return result;
  }

  private TemporaryDataSource writeDocument(String documentData,
    String contentType)
    throws Exception
  {
    InputStream in = IOUtils.toInputStream(documentData);
    File file = File.createTempFile("htmleditor",".tmp");
    FileOutputStream out = new FileOutputStream(file);
    
    //Correcció dels atributs aria-label als enllaços
    String scriptName = MatrixConfig.getProperty("htmlFixer.script");
    String userId = MatrixConfig.getProperty("adminCredentials.userId");
    String password = MatrixConfig.getProperty("adminCredentials.password");
    HtmlFixer htmlFixer = new HtmlFixer(scriptName, userId, password);
    htmlFixer.fixCode(in, out); 

    in.close();
    out.close();

    TemporaryDataSource dataSource = new TemporaryDataSource(file, contentType);

    return dataSource;
  }

  private Document getTranslation(Document document, String language)
    throws Exception
  {
    List<RelatedDocument> relDocs = document.getRelatedDocument();
    for (RelatedDocument relDoc : relDocs)
    {
      RelationType relType = relDoc.getRelationType();
      String relName = relDoc.getName();
      if (RelationType.TRANSLATION.equals(relType) && language.equals(relName))
      {
        return getClient().loadDocument(relDoc.getDocId(), 0, ContentInfo.ALL);
      }
    }

    return document;
  }

  private boolean isLockUser(String lockuser)
  {
    return (lockuser != null &&
      lockuser.trim().equals(UserSessionBean.getCurrentInstance().getUsername().trim()));
  }
  
  public class DocumentLockedByUser extends Exception
  {
    private String userId;
    
    private DocumentLockedByUser(String lockuser)
    {
      super("DOCUMENT_LOCKED_BY_ANOTHER_USER");
      this.userId = lockuser;
    }

    public String getUserId()
    {
      return userId;
    }
  }

}
