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
package org.santfeliu.doc.swing.action;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.matrix.security.AccessControl;
import org.matrix.doc.DocumentConstants;
import org.matrix.doc.Document;
import org.matrix.doc.Content;
import org.matrix.dic.Property;
import org.matrix.doc.RelatedDocument;
import org.matrix.doc.State;
import org.santfeliu.doc.swing.DocumentSimplePanel;
import org.santfeliu.util.Utilities;

/**
 *
 * @author unknown
 */
public class SaveDocumentAction extends AbstractAction
{
  private DocumentSimplePanel documentPanel;
  
  public SaveDocumentAction(DocumentSimplePanel documentPanel)
  {
    this.documentPanel = documentPanel;
    this.putValue(Action.NAME, documentPanel.getLocalizedText("save"));
  }

  public void actionPerformed(ActionEvent e)
  {
    try
    {
      documentPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      DataHandler dataHandler = null;
      String contentId = null;
      String urlString = null;

      Content content = new Content();

      Map map = documentPanel.getFormValues();      
      String title = (String)map.get(DocumentConstants.TITLE);
      map.remove(DocumentConstants.TITLE);
      
      String fileRef = documentPanel.getFileRef();
      if (fileRef.length() == 0)
      {
        content = null;
      }
      else if ((fileRef.length() > 0) && (!Utilities.isUUID(fileRef)))
      {
        File file = new File(fileRef);
        if (file.exists())
        {
          FileDataSource dataSource = new FileDataSource(file);
          dataHandler = new DataHandler(dataSource);
        }
        else
        {
          URL url = new URL(fileRef);
          urlString = url.toString();
        }
      }
      else //UUID
      {
        contentId = fileRef;
      }
      map.remove(DocumentConstants.CONTENTID);
      
      String docId = documentPanel.getDocId();
      String language = documentPanel.getDocLanguage();
      String state = documentPanel.getDocState();
      String creationDate = documentPanel.getDocCreationDate();
      if (creationDate != null && creationDate.length() > 0)
      {
        creationDate = new SimpleDateFormat("yyyyMMdd").format(
          new SimpleDateFormat("dd/MM/yyyy").parse(creationDate));
      }
      
      Document document = new Document();
      document.setIncremental(false);
      if (contentId == null)
      {
        if (dataHandler != null)
        {
          content.setData(dataHandler);
          if (dataHandler.getContentType().
            equalsIgnoreCase("application/octet-stream"))
          {
            content.setContentType(null);
          }
          else
          {
            content.setContentType(dataHandler.getContentType());
          }
        }
        else if (urlString != null)
        {
          content.setUrl(urlString);
        }
      }
      else
      {
        content.setContentId(contentId);
      }
      document.setContent(content);
      document.setTitle(title);
      document.setLanguage(language);
      document.setState(State.fromValue(state));
      document.setCreationDate(creationDate);
      
      List<RelatedDocument> relatedDocList = 
        (List<RelatedDocument>)map.get(DocumentConstants.RELATED_DOC_LIST);
      map.remove(DocumentConstants.RELATED_DOC_LIST);
      document.getRelatedDocument().clear();
      document.getRelatedDocument().addAll(relatedDocList);

      List<AccessControl> accessControlList =
        (List<AccessControl>)map.get(DocumentConstants.ACL_LIST);
      map.remove(DocumentConstants.ACL_LIST);
      document.getAccessControl().clear();
      document.getAccessControl().addAll(accessControlList);

      putProperties(document, map);
      
      document.setDocTypeId((String)documentPanel.getDocTypeIdComboBox().
        getSelectedItem());

      if (documentPanel.existsDocument())
      {
        // updateDocument
        document.setDocId(docId);
        if (documentPanel.isDocCreateNewVersion())
        {
          document.setVersion(-1);
        }
        else
        {
          String selectedVersion = 
            (String)documentPanel.getVersionComboBox().getSelectedItem();
          document.setVersion(Integer.parseInt(selectedVersion));          
        }
        documentPanel.getClient().storeDocument(document);
      }
      else
      {
        // createDocument
        document = documentPanel.getClient().storeDocument(document);
        docId = document.getDocId();
      }
      if (documentPanel.isDocCreateNewVersion())
      {
        documentPanel.loadDocument(docId, DocumentConstants.LAST_VERSION);
      }
      else
      {
        documentPanel.loadDocument(docId, 
          Integer.parseInt(documentPanel.getDocVersion()));
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      documentPanel.showError(ex);      
    }
    finally
    {
      documentPanel.setCursor(
        Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));      
    }
  }
  
  private void putProperties(Document document, Map map)
  {
    Map<String, Map<Integer, String>> userPropertiesMap = 
      new HashMap<String, Map<Integer, String>>();
    for (Object key : map.keySet())
    {
      String strKey = (String)key;
      String name = strKey;
      Integer index = 0;
      int lastIndex = strKey.lastIndexOf(":");
      if (lastIndex != -1)
      {
        name = strKey.substring(0, lastIndex);
        index = Integer.valueOf(strKey.substring(lastIndex + 1));
      }        
      if (!userPropertiesMap.containsKey(name))
      {
        userPropertiesMap.put(name, new HashMap<Integer, String>());
      }
      String value = (String)map.get(key);
      if (value != null && value.length() > 0)
      {
        userPropertiesMap.get(name).put(index, value);
      }
    }
    for (String name : userPropertiesMap.keySet())
    {
      Map<Integer, String> propertyMap = userPropertiesMap.get(name);
      int firstIndex = Integer.MAX_VALUE;
      List<String> values = new ArrayList<String>();
      for (Integer index : propertyMap.keySet())
      {
        if (index < firstIndex)
        {
          values.add(0, propertyMap.get(index));
          firstIndex = index;
        }
        else
        {
          values.add(propertyMap.get(index));
        }
      }
      Property p = new Property();
      p.setName(name);
      p.getValue().addAll(values);
      document.getProperty().add(p);
    }    
  }  
}
