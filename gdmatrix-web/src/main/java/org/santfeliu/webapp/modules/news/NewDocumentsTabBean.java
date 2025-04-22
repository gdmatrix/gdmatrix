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
package org.santfeliu.webapp.modules.news;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.news.NewDocument;
import org.matrix.news.NewSection;
import org.matrix.security.AccessControl;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.doc.web.DocumentConfigBean;
import org.santfeliu.faces.matrixclient.model.DefaultMatrixClientModel;
import static org.santfeliu.faces.matrixclient.model.DocMatrixClientModels.DOCTYPES_PARAMETER;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.news.web.NewsConfigBean;
import org.santfeliu.web.ApplicationBean;
import org.santfeliu.web.UserSessionBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.helpers.GroupableRowsHelper;
import org.santfeliu.webapp.modules.dic.TypeTypeBean;
import org.santfeliu.webapp.modules.doc.DocModuleBean;
import org.santfeliu.webapp.modules.doc.DocumentTypeBean;
import org.santfeliu.webapp.setup.TableProperty;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author blanquepa
 */
@Named
@RequestScoped
public class NewDocumentsTabBean extends TabBean
{
  private final TabInstance EMPTY_TAB_INSTANCE = new TabInstance();

  private NewDocument editing;
  Map<String, TabInstance> tabInstances = new HashMap<>();
  private GroupableRowsHelper groupableRowsHelper;
  private DefaultMatrixClientModel clientModel;

  public class TabInstance
  {
    String objectId = NEW_OBJECT_ID;
    List<NewDocument> rows;
    int firstRow = 0;
  }

  @Inject
  NewObjectBean newObjectBean;

  @Inject
  DocumentTypeBean documentTypeBean;

  @Inject
  TypeTypeBean typeTypeBean;

  @PostConstruct
  public void init()
  {
    groupableRowsHelper = new GroupableRowsHelper()
    {
      @Override
      public ObjectBean getObjectBean()
      {
        return NewDocumentsTabBean.this.getObjectBean();
      }

      @Override
      public List<TableProperty> getColumns()
      {
        return Collections.EMPTY_LIST;
      }

      @Override
      public void sortRows()
      {
      }

      @Override
      public String getRowTypeColumnName()
      {
        return "newDocTypeId";
      }

      @Override
      public String getFixedColumnValue(Object row, String columnName)
      {
        NewDocument newDocument = (NewDocument)row;
        if ("documentId".equals(columnName))
        {
          return newDocument.getDocumentId();
        }
        else if ("documentTitle".equals(columnName))
        {
          return newDocument.getTitle();
        }
        else if ("newDocTypeId".equals(columnName))
        {
          return typeTypeBean.getDescription(newDocument.getNewDocTypeId());
        }
        else
        {
          return null;
        }
      }
    };
  }

  public GroupableRowsHelper getGroupableRowsHelper()
  {
    return groupableRowsHelper;
  }

  public void setGroupableRowsHelper(GroupableRowsHelper groupableRowsHelper)
  {
    this.groupableRowsHelper = groupableRowsHelper;
  }

  public TabInstance getCurrentTabInstance()
  {
    EditTab tab = newObjectBean.getActiveEditTab();
    if (WebUtils.getBeanName(this).equals(tab.getBeanName()))
    {
      TabInstance tabInstance = tabInstances.get(tab.getSubviewId());
      if (tabInstance == null)
      {
        tabInstance = new TabInstance();
        tabInstances.put(tab.getSubviewId(), tabInstance);
      }
      return tabInstance;
    }
    else return EMPTY_TAB_INSTANCE;
  }

  @Override
  public String getObjectId()
  {
    return getCurrentTabInstance().objectId;
  }

  @Override
  public void setObjectId(String objectId)
  {
    getCurrentTabInstance().objectId = objectId;
  }

  @Override
  public boolean isNew()
  {
    return NEW_OBJECT_ID.equals(getCurrentTabInstance().objectId);
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return newObjectBean;
  }
  public NewDocument getEditing()
  {
    return editing;
  }

  public void setEditing(NewDocument editing)
  {
    this.editing = editing;
  }

  public void setNewDocTypeId(String newDocTypeId)
  {
    if (editing != null)
      editing.setNewDocTypeId(newDocTypeId);
  }

  public String getNewDocTypeId()
  {
    return editing == null ? NEW_OBJECT_ID : editing.getNewDocTypeId();
  }

  public List<NewDocument> getRows()
  {
    return getCurrentTabInstance().rows;
  }

  public void setRows(List<NewDocument> rows)
  {
    getCurrentTabInstance().rows = rows;
  }

  public int getFirstRow()
  {
    return getCurrentTabInstance().firstRow;
  }

  public void setFirstRow(int firstRow)
  {
     getCurrentTabInstance().firstRow = firstRow;
  }

  public String getDocumentDescription()
  {
    if (editing != null && !isNew(editing))
    {
      return documentTypeBean.getDescription(editing.getDocumentId());
    }
    return "";
  }

  public void edit(NewDocument row)
  {
    if (row != null)
    {
      try
      {
        editing = row;
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else
    {
      create();
    }
  }

  @Override
  public void load() throws Exception
  {
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        List<NewDocument> auxList = NewsModuleBean.getPort(false)
          .findNewDocuments(newObjectBean.getObjectId(), null);
        String typeId = getTabBaseTypeId();
        if (typeId == null)
        {
          getCurrentTabInstance().rows = auxList;
        }
        else
        {
          List<NewDocument> result = new ArrayList();
          for (NewDocument item : auxList)
          {
            Type newDocType =
              TypeCache.getInstance().getType(item.getNewDocTypeId());
            if (newDocType.isDerivedFrom(typeId))
            {
              result.add(item);
            }
          }
          getCurrentTabInstance().rows = result;
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else
    {
      TabInstance tabInstance = getCurrentTabInstance();
      tabInstance.objectId = NEW_OBJECT_ID;
      tabInstance.rows = Collections.EMPTY_LIST;
      tabInstance.firstRow = 0;
    }
  }

  public void create()
  {
    editing = new NewDocument();
    editing.setNewDocTypeId(getCreationTypeId());
  }

  @Override
  public void store()
  {
    try
    {
      if (editing != null)
      {
        String newId = newObjectBean.getObjectId();
        editing.setNewId(newId);
        NewsModuleBean.getPort(false).storeNewDocument(editing);
        refreshHiddenTabInstances();
        load();
        editing = null;
        growl("STORE_OBJECT");
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void remove(NewDocument row)
  {
    if (row != null)
    {
      try
      {
        NewsModuleBean.getPort(false).removeNewDocument(
          row.getNewDocumentId());
        refreshHiddenTabInstances();
        load();
        growl("REMOVE_OBJECT");
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
  }

  public void cancel()
  {
    editing = null;
  }

  @Override
  public boolean isDialogVisible()
  {
    return (editing != null);
  }

  @Override
  public void clear()
  {
    tabInstances.clear();
  }
     
  public DefaultMatrixClientModel getClientModel()
  {
    if (clientModel == null)
    {
      clientModel = new DefaultMatrixClientModel();
    }
    return clientModel;
  }

  public void setClientModel(DefaultMatrixClientModel clientModel)
  {
    this.clientModel = clientModel;
  }
  
  public DefaultMatrixClientModel getSendClientModel()
  {
    clientModel = getClientModel();
    Map creationDocTypes = 
      DocModuleBean.getUserDocTypes(DictionaryConstants.CREATE_ACTION);
    clientModel.putParameter(DOCTYPES_PARAMETER, creationDocTypes);
    return clientModel;
  }  

  public void documentEdited()
  {
    try
    {
      clientModel.parseResult();
      this.load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public String documentSent()
  {   
    editing = null;
    
    try
    {
      String docId = (String) clientModel.parseResult();
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

  @Override
  public Serializable saveState()
  {
    return new Object[]{ editing };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      editing = (NewDocument)stateArray[0];
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
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
      newDocument.setNewDocTypeId(NewsConfigBean.EXTENDED_INFO_TYPE);
      NewsModuleBean.getPort(false).storeNewDocument(newDocument);
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }  
  
  private void updateDocumentRoles(String docId) throws Exception
  {
    Document document =
      DocModuleBean.getPort(false).loadDocument(docId, 0, ContentInfo.ID);
    if (document != null)
    {
      String workspaceId =
        UserSessionBean.getCurrentInstance().getWorkspaceId();
      MenuModel menuModel = ApplicationBean.getCurrentInstance().
        createMenuModel(workspaceId);
      List<String> readRoleList = new ArrayList<>();
      List<String> writeRoleList = new ArrayList<>();
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
        }
      }
      if (writeNotProtected)
      {
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

  private boolean isNew(NewDocument newDocument)
  {
    return (newDocument != null && newDocument.getNewDocumentId() == null);
  }

  private void refreshHiddenTabInstances()
  {
    for (TabInstance tabInstance : tabInstances.values())
    {
      if (tabInstance != getCurrentTabInstance())
      {
        tabInstance.objectId = NEW_OBJECT_ID;
      }
    }
  }

}
