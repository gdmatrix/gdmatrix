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
package org.santfeliu.news.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.model.SelectItem;
import org.matrix.dic.DictionaryConstants;
import org.santfeliu.dic.Type;
import org.matrix.security.AccessControl;
import org.matrix.doc.Document;
import org.matrix.news.NewDocument;
import org.matrix.news.NewSection;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.doc.web.DocumentBean;
import org.santfeliu.doc.web.DocumentConfigBean;
import org.santfeliu.faces.matrixclient.model.DocMatrixClientModels;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.news.client.NewsManagerClient;
import org.santfeliu.util.MimeTypeMap;
import org.santfeliu.web.ApplicationBean;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.TypifiedPageBean;

/**
 *
 * @author unknown
 */
public class NewDocumentsBean extends TypifiedPageBean
{
/*
  private static final String FLAGS_PATH_URL = 
    "/common/translation/images/flags/";
*/
  private static final String DOC_SERVLET_URL = "/documents/";
  
  private List<NewDocument> rows;
  private NewDocument editingDocument;
  private String command;
  private String uploadedDocId;
  private Map documentProperties = new HashMap();
  
  private DocMatrixClientModels models;
  private Map userDocTypes;  

  public NewDocumentsBean()
  {
    super(DictionaryConstants.NEW_DOCUMENT_TYPE, "WEBMASTER");
    DocumentConfigBean configBean =
      (DocumentConfigBean)getBean("documentConfigBean");
    userDocTypes = configBean.getDocTypes();
    models = new DocMatrixClientModels(userDocTypes);

    load();
  }

  public void setRows(List<NewDocument> rows)
  {
    this.rows = rows;
  }

  public List<NewDocument> getRows()
  {
    return rows;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  public void setEditingDocument(NewDocument editingDocument)
  {
    this.editingDocument = editingDocument;
  }

  public NewDocument getEditingDocument()
  {
    return editingDocument;
  }

  public String getCommand()
  {
    return command;
  }

  public void setCommand(String command)
  {
    this.command = command;
  }

  public void setUploadedDocId(String uploadedDocId)
  {
    this.uploadedDocId = uploadedDocId;
  }

  public String getUploadedDocId()
  {
    return uploadedDocId;
  }

  public void setDocumentProperties(Map documentProperties)
  {
    this.documentProperties = documentProperties;
  }

  public Map getDocumentProperties()
  {
    return documentProperties;
  }

  public DocMatrixClientModels getModels() 
  {
    return models;
  }

  public void setModels(DocMatrixClientModels models) 
  {
    this.models = models;
  }  

  //Auxiliar getters
/*
  public String getLanguageFlag()
  { 
    NewDocument row = (NewDocument)getExternalContext().
      getRequestMap().get("row");
    String language = row.getLanguage();   
    return DocumentUtils.getLanguageFlag(FLAGS_PATH_URL, language);
  }
  
  public String getLanguage()
  {
    NewDocument row = (NewDocument)getExternalContext().
      getRequestMap().get("row");
    String language = row.getLanguage();
    return DocumentUtils.extendLanguage(language);
  }
*/   
  public String getDocumentUrl()
  { 
    String url = "";
    NewDocument row = (NewDocument)getExternalContext().
      getRequestMap().get("row");
    if (row != null)  
    {
      String title = row.getTitle();
      String mimeType = row.getMimeType();
      String extension = MimeTypeMap.getMimeTypeMap().getExtension(mimeType);
      String filename = DocumentUtils.getFilename(title) + "." + extension;
      url = getContextPath() + DOC_SERVLET_URL + row.getContentId() + "/" +
        filename;
    }
    return url;
  }    

  //Object actions
  public String show()
  {
    return "new_documents";
  }
  
  private void load()
  {
    try
    {
      if (!isNew())
      {
        rows = NewsConfigBean.getPort().findNewDocumentsFromCache(
          getObjectId(), null);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public String store()
  {
    if (editingDocument != null)
    {
      storeNewDocument();
    }
    else
    {
      load();
    }
    return show();
  }
  
  public String refresh()
  {
    editingDocument = null;
    if ("sendFile".equals(command))
      return storeSentDocument(uploadedDocId);
    
    return null;
  }
  
  public String documentSent()
  {
    editingDocument = null;
    
    try
    {
      String docId = (String)models.getSendModel().parseResult();
      if (docId != null)
        return storeSentDocument(docId);
    }
    catch (Exception ex)
    {
      if (!"NO_FILE".equals(ex.getMessage()))
        error(ex);
    }

    return null;
  }
  
  public String documentUpdated()
  {
    try
    {
      models.getUpdateModel().parseResult();
      NewsManagerClient.getCache().clear();
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }

    return null;
  }  
  
  public String documentEdited()
  {
    try
    {
      models.getEditModel().parseResult();
    }
    catch (Exception ex)
    {
      error(ex);
    }

    return null;
  }  

  //Page actions
  public String createNewDocument()
  {
    editingDocument = new NewDocument();
    return null;
  }

  public String editNewDocument()
  {
    editingDocument = (NewDocument)getExternalContext().
      getRequestMap().get("row");    
    return null;
  }
   
  public String removeNewDocument()
  {
    try
    {
      NewDocument row = (NewDocument)getExternalContext().
        getRequestMap().get("row");
      NewsConfigBean.getPort().removeNewDocument(row.getNewDocumentId());
      editingDocument = null;
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }   
  
  public String storeNewDocument()
  {
    try
    {
      if (editingDocument.getDocumentId() == null ||
        editingDocument.getDocumentId().length() == 0)
      {
        throw new Exception("DOCUMENT_MUST_BE_SELECTED");
      }
      String[] docIdArray = editingDocument.getDocumentId().split("-");
      if (isAddedDocumentInList(docIdArray[0]))
      {
        throw new Exception("DOCUMENT_ALREADY_IN_LIST");
      }
      editingDocument.setDocumentId(docIdArray[0]);
      updateDocumentRoles(editingDocument.getDocumentId());
      info("DOCUMENT_SECURITY_UPDATED");
    }
    catch (Exception ex)
    {
      if ("ACTION_DENIED".equals(ex.getMessage()))
      {
        warn("DOCUMENT_SECURITY_NOT_UPDATED");
      }
      else
      {
        error(ex);
        return null;
      }
    }
    try
    {
      NewDocument newDocument = new NewDocument();
      newDocument.setNewDocumentId(editingDocument.getNewDocumentId());
      newDocument.setNewId(getObjectId());
      newDocument.setDocumentId(editingDocument.getDocumentId());
      //newDocument.setDocRole(editingDocument.getDocRole());
      newDocument.setNewDocTypeId(editingDocument.getNewDocTypeId());
      NewsConfigBean.getPort().storeNewDocument(newDocument);
      editingDocument = null;
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }    
  
  public String cancelNewDocument()
  {
    editingDocument = null;
    return null;
  }
  
  public String showDocument()
  {
    try
    {
      NewDocument row = (NewDocument)getExternalContext().
        getRequestMap().get("row");
      String docId = row.getDocumentId();
      Document document = DocumentConfigBean.getClient().loadDocument(docId, 0);
      if (document != null)
      {
        return getControllerBean().showObject(DictionaryConstants.DOCUMENT_TYPE,
          DocumentConfigBean.toObjectId(docId, document.getVersion()));
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String searchDocument()
  {
    return getControllerBean().searchObject(DictionaryConstants.DOCUMENT_TYPE,
      "#{newDocumentsBean.editingDocument.documentId}");
  }

  public List<SelectItem> getNewDocumentSelectItems()
  {
    DocumentBean documentBean = (DocumentBean)getBean("document2Bean");
    return documentBean.getSelectItems(editingDocument.getDocumentId());
  }

  public String showNewDocType()
  {
    return getControllerBean().showObject("Type",
      editingDocument.getNewDocTypeId());
  }

  public boolean isRenderShowNewDocTypeButton()
  {
    return editingDocument.getNewDocTypeId() != null &&
      editingDocument.getNewDocTypeId().trim().length() > 0;
  }

  public String getRowTypeLabel()
  {
    try
    {
      NewDocument row = (NewDocument)getExternalContext().
        getRequestMap().get("row");
      String typeId = row.getNewDocTypeId();
      TypeCache typeCache = TypeCache.getInstance();
      Type type = typeCache.getType(typeId);
      if (type != null)
        return type.formatTypePath(false, true, false, getRootTypeId());
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "";
  }

  private void updateDocumentRoles(String docId) throws Exception
  {
    Document document = DocumentConfigBean.getClient().loadDocument(docId);
    if (document != null)
    {
      String workspaceId =
        UserSessionBean.getCurrentInstance().getWorkspaceId();
      MenuModel menuModel = ApplicationBean.getCurrentInstance().
        createMenuModel(workspaceId);
      List<String> readRoleList = new ArrayList<String>();
      List<String> writeRoleList = new ArrayList<String>();
      List<NewSection> newSectionList = 
        NewsConfigBean.getPort().findNewSectionsFromCache(getObjectId());
      int i = 0;
      boolean readNotProtected = false;
      boolean writeNotProtected = false;
      while (i < newSectionList.size()
        && (!readNotProtected || !writeNotProtected))
      {
        NewSection ns = newSectionList.get(i++);
        MenuItemCursor nodeMic = menuModel.getMenuItemByMid(ns.getSectionId());
        if (!readNotProtected)
        {
          if (nodeMic.getViewRoles().isEmpty())
          {
            readNotProtected = true;
          }
          else
          {
            readRoleList.addAll(nodeMic.getViewRoles());          
          }          
        }
        if (!writeNotProtected)
        {
          if (nodeMic.getEditRoles().isEmpty())
          {
            writeNotProtected = true;
          }
          else
          {
            writeRoleList.addAll(nodeMic.getEditRoles());
          }
        }
      }
      if (readNotProtected)
      {
        //document.getReadRole().clear();
        AccessControl ac = new AccessControl();
        ac.setAction(DictionaryConstants.READ_ACTION);
        ac.setRoleId("EVERYONE");
        if (!containsAC(document.getAccessControl(), ac))
        {
          document.getAccessControl().add(ac);
        }
      }
      else
      {
        for (String readRole : readRoleList)
        {
          AccessControl ac = new AccessControl();
          ac.setAction(DictionaryConstants.READ_ACTION);
          ac.setRoleId(readRole);
          if (!containsAC(document.getAccessControl(), ac))
          {
            document.getAccessControl().add(ac);
          }
//          if (!document.getReadRole().contains(readRole))
//          {
//            document.getReadRole().add(readRole);
//          }
        }
      }
      if (writeNotProtected)
      {
//        document.getWriteRole().clear();
        AccessControl ac = new AccessControl();
        ac.setAction(DictionaryConstants.WRITE_ACTION);
        ac.setRoleId("EVERYONE");
        if (!containsAC(document.getAccessControl(), ac))
        {
          document.getAccessControl().add(ac);
        }
      }
      else
      {
        for (String writeRole : writeRoleList)
        {
          AccessControl ac = new AccessControl();
          ac.setAction(DictionaryConstants.WRITE_ACTION);
          ac.setRoleId(writeRole);
          if (!containsAC(document.getAccessControl(), ac))
          {
            document.getAccessControl().add(ac);
          }
//          if (!document.getWriteRole().contains(writeRole))
//          {
//            document.getWriteRole().add(writeRole);
//          }
        }
      }
      DocumentConfigBean.getClient().storeDocument(document);
    }    
  }

  private boolean containsAC(List<AccessControl> acl, AccessControl ac)
  {
    for (AccessControl item : acl)
    {
      if (ac.getAction().equals(item.getAction()) &&
          ac.getRoleId().equals(item.getRoleId()))
        return true;
    }
   return false;
  }

  private boolean isAddedDocumentInList(String docId)
  {
    if (editingDocument.getNewDocumentId() == null) //is a new document
    {
      if (rows != null)
      {
        for (NewDocument newDocument : rows)
        {
          String rowDocId = newDocument.getDocumentId();
          if (newDocument.getNewDocumentId() != null &&
            rowDocId != null && rowDocId.equals(docId))
          {
            return true;
          }
        }
      }
    }    
    return false;
  }
  
  private String storeSentDocument(String docId)
  {
    try
    {
      updateDocumentRoles(docId);
      info("DOCUMENT_SECURITY_UPDATED");
    }
    catch (Exception ex)
    {
      if ("ACTION_DENIED".equals(ex.getMessage()))
      {
        warn("DOCUMENT_SECURITY_NOT_UPDATED");
      }
      else
      {
        error(ex);
        return null;
      }
    }
    try
    {
      NewDocument newDocument = new NewDocument();
      newDocument.setNewId(getObjectId());
      newDocument.setDocumentId(docId);
      //newDocument.setDocRole(EXTENDED_INFO_ROLE);
      newDocument.setNewDocTypeId(NewsConfigBean.EXTENDED_INFO_TYPE);
      NewsConfigBean.getPort().storeNewDocument(newDocument);
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
}
