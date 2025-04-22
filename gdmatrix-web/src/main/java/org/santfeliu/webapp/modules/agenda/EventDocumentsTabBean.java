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
package org.santfeliu.webapp.modules.agenda;

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
import org.matrix.agenda.Event;
import org.matrix.agenda.EventDocument;
import org.matrix.agenda.EventDocumentFilter;
import org.matrix.agenda.EventDocumentView;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.PropertyDefinition;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.security.AccessControl;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.faces.matrixclient.model.DefaultMatrixClientModel;
import static org.santfeliu.faces.matrixclient.model.DocMatrixClientModels.DOCTYPES_PARAMETER;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.helpers.GroupableRowsHelper;
import org.santfeliu.webapp.modules.dic.TypeTypeBean;
import org.santfeliu.webapp.modules.doc.DocModuleBean;
import org.santfeliu.webapp.modules.doc.DocumentTypeBean;
import static org.santfeliu.webapp.setup.Action.POST_TAB_EDIT_ACTION;
import static org.santfeliu.webapp.setup.Action.POST_TAB_LOAD_ACTION;
import static org.santfeliu.webapp.setup.Action.POST_TAB_REMOVE_ACTION;
import static org.santfeliu.webapp.setup.Action.POST_TAB_STORE_ACTION;
import static org.santfeliu.webapp.setup.Action.PRE_TAB_EDIT_ACTION;
import static org.santfeliu.webapp.setup.Action.PRE_TAB_LOAD_ACTION;
import static org.santfeliu.webapp.setup.Action.PRE_TAB_REMOVE_ACTION;
import static org.santfeliu.webapp.setup.Action.PRE_TAB_STORE_ACTION;
import org.santfeliu.webapp.setup.TableProperty;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author lopezrj-sf
 */
@Named
@RequestScoped
public class EventDocumentsTabBean extends TabBean
{
  public static final String SPREAD_ROLES_PROPERTY = "_documentsSpreadRoles";
  
  private final TabInstance EMPTY_TAB_INSTANCE = new TabInstance();

  private EventDocument editing;
  Map<String, TabInstance> tabInstances = new HashMap<>();
  private GroupableRowsHelper groupableRowsHelper;
  private DefaultMatrixClientModel clientModel;  

  public class TabInstance
  {
    String objectId = NEW_OBJECT_ID;
    List<EventDocumentView> rows;
    int firstRow = 0;
  }

  @Inject
  EventObjectBean eventObjectBean;

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
        return EventDocumentsTabBean.this.getObjectBean();
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
        return "eventDocTypeId";
      }

      @Override
      public String getFixedColumnValue(Object row, String columnName)
      {
        EventDocumentView eventDocumentView = (EventDocumentView)row;
        if ("documentId".equals(columnName))
        {
          return eventDocumentView.getDocument().getDocId();
        }
        else if ("documentTitle".equals(columnName))
        {
          return eventDocumentView.getDocument().getTitle();
        }
        else if ("eventDocTypeId".equals(columnName))
        {
          return typeTypeBean.getDescription(
            eventDocumentView.getEventDocTypeId());
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
    EditTab tab = eventObjectBean.getActiveEditTab();
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
    return eventObjectBean;
  }
  public EventDocument getEditing()
  {
    return editing;
  }

  public void setEditing(EventDocument editing)
  {
    this.editing = editing;
  }

  public void setEventDocTypeId(String eventDocTypeId)
  {
    if (editing != null)
      editing.setEventDocTypeId(eventDocTypeId);
  }

  public String getEventDocTypeId()
  {
    return editing == null ? NEW_OBJECT_ID : editing.getEventDocTypeId();
  }

  public List<EventDocumentView> getRows()
  {
    return getCurrentTabInstance().rows;
  }

  public void setRows(List<EventDocumentView> rows)
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
      return documentTypeBean.getDescription(editing.getDocId());
    }
    return "";
  }

  public void edit(EventDocumentView row)
  {
    if (row != null)
    {
      try
      {
        executeTabAction(PRE_TAB_EDIT_ACTION, row);
        editing = AgendaModuleBean.getClient(false).
          loadEventDocument(row.getEventDocId());
        executeTabAction(POST_TAB_EDIT_ACTION, editing);
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
    executeTabAction(PRE_TAB_LOAD_ACTION, null);
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        EventDocumentFilter filter = new EventDocumentFilter();
        filter.setEventId(eventObjectBean.getObjectId());
        List<EventDocumentView> auxList = AgendaModuleBean.getClient(false).
          findEventDocumentViewsFromCache(filter);
        String typeId = getTabBaseTypeId();
        EditTab tab = eventObjectBean.getActiveEditTab();
        if (typeId == null || tab.isShowAllTypes())
        {
          getCurrentTabInstance().rows = auxList;
        }
        else
        {
          List<EventDocumentView> result = new ArrayList();
          for (EventDocumentView item : auxList)
          {
            Type eventDocType =
              TypeCache.getInstance().getType(item.getEventDocTypeId());
            if (eventDocType.isDerivedFrom(typeId))
            {
              result.add(item);
            }
          }
          getCurrentTabInstance().rows = result;
        }
        executeTabAction(POST_TAB_LOAD_ACTION, null);
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
    executeTabAction(PRE_TAB_EDIT_ACTION, null);
    editing = new EventDocument();
    editing.setEventDocTypeId(getCreationTypeId());
    executeTabAction(POST_TAB_EDIT_ACTION, editing);
  }

  @Override
  public void store()
  {
    try
    {
      if (editing != null)
      {
        String eventId = eventObjectBean.getObjectId();
        editing.setEventId(eventId);
        editing = (EventDocument)executeTabAction(PRE_TAB_STORE_ACTION, editing);
        AgendaModuleBean.getClient(false).storeEventDocument(editing);
        executeTabAction(POST_TAB_STORE_ACTION, editing);
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

  public void remove(EventDocumentView row)
  {
    if (row != null)
    {
      try
      {
        row = (EventDocumentView)executeTabAction(PRE_TAB_REMOVE_ACTION, row);
        AgendaModuleBean.getClient(false).removeEventDocument(
          row.getEventDocId());
        executeTabAction(POST_TAB_REMOVE_ACTION, row);
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
      {
        spreadDocumentRoles(docId);
        EventDocument eventDocument = new EventDocument();
        eventDocument.setDocId(docId);
        eventDocument.setEventId(getObjectId());
        String eventDocTypeId = getCreationTypeId();
        if (eventDocTypeId == null) 
          eventDocTypeId = DictionaryConstants.EVENT_DOCUMENT_TYPE;
        eventDocument.setEventDocTypeId(eventDocTypeId);
        eventDocument = 
          (EventDocument)executeTabAction(PRE_TAB_STORE_ACTION, eventDocument);
        AgendaModuleBean.getClient(false).storeEventDocument(eventDocument);
        executeTabAction(POST_TAB_STORE_ACTION, editing);
        
        load();
      }
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
      editing = (EventDocument)stateArray[0];
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  private boolean isNew(EventDocument eventDocument)
  {
    return (eventDocument != null && eventDocument.getEventDocId() == null);
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
  
  private void spreadDocumentRoles(String docId) throws Exception
  {
    Event event = eventObjectBean.getEvent();
    String eventTypeId = event.getEventTypeId();
    if (eventTypeId != null)
    {
      Type eventType = TypeCache.getInstance().getType(eventTypeId);
      String spreadRoles = getSpreadRoles(eventType);
      if (spreadRoles != null)
      {
        Document document = 
          DocModuleBean.getPort(true).loadDocument(docId, 0, ContentInfo.ID);
        if (document != null)
        {
          boolean update = false;
          if ("true".equalsIgnoreCase(spreadRoles)) //Spread Case roles
          {
            List<AccessControl> accessControlList = new ArrayList();
            accessControlList.addAll(eventType.getAccessControl());
            accessControlList.addAll(event.getAccessControl());
            for (AccessControl ac : accessControlList)
            {
              if (!containsAC(document.getAccessControl(), ac))
              {
                update = true;
                document.getAccessControl().add(ac);
              }
            }
          }
          else
          {
            String[] actions = {DictionaryConstants.READ_ACTION, 
              DictionaryConstants.WRITE_ACTION, 
              DictionaryConstants.DELETE_ACTION};
            for (String action : actions)
            {
              AccessControl ac = new AccessControl();
              ac.setAction(action);
              ac.setRoleId(spreadRoles);
              if (!containsAC(document.getAccessControl(), ac))
              {
                document.getAccessControl().add(ac);
                update = true;
              }
            }            
          }
          if (update)
          {
            try
            {
              DocModuleBean.getPort(true).storeDocument(document);
              info("DOCUMENT_SECURITY_UPDATED");
            }
            catch (Exception ex) 
            {
            }
          }
          else
          {
            warn("DOCUMENT_SECURITY_NOT_UPDATED");
          }
        }
      }
    }
  }

  private String getSpreadRoles(Type caseType)
  {
    if (caseType != null)
    {
      PropertyDefinition pd =
        caseType.getPropertyDefinition(SPREAD_ROLES_PROPERTY);
      if (pd != null && pd.getValue() != null && !pd.getValue().isEmpty())
      {
        String value = pd.getValue().get(0);
        if ("false".equals(value))
          return null;
        else 
          return value;
      }
    }
    return null;
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

}
