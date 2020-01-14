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
package org.santfeliu.agenda.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;
import org.matrix.agenda.AgendaConstants;
import org.matrix.agenda.Event;
import org.matrix.agenda.EventDocument;
import org.matrix.agenda.EventDocumentFilter;
import org.matrix.agenda.EventDocumentView;

import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.PropertyDefinition;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.security.AccessControl;
import org.santfeliu.agenda.client.AgendaManagerClient;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.doc.web.DocumentBean;
import org.santfeliu.doc.web.DocumentConfigBean;
import org.santfeliu.doc.web.DocumentUrlBuilder;
import org.santfeliu.faces.matrixclient.model.DocMatrixClientModels;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.obj.TypifiedPageBean;


/**
 *
 * @author unknown
 */
public class EventDocumentsBean extends TypifiedPageBean
{
  public static final String ROOT_TYPE_ID_PROPERTY = "_documentRootTypeId";
  public static final String GROUPBY_PROPERTY = "_documentsGroupBy";
  public static final String SPREAD_ROLES_PROPERTY = "_documentsSpreadRoles";

  private EventDocument editingDocument;

  private String command;
  private String docId;
  
  private DocMatrixClientModels models;
  private Map userDocTypes;

  public EventDocumentsBean()
  {
    super(DictionaryConstants.EVENT_DOCUMENT_TYPE, AgendaConstants.AGENDA_ADMIN_ROLE);

    EventMainBean eventMainBean = (EventMainBean)getBean("eventMainBean");
    Event event = eventMainBean.getEvent();
    Type eventType = TypeCache.getInstance().getType(event.getEventTypeId());
    if (eventType != null)
    {
      PropertyDefinition pd =
        eventType.getPropertyDefinition(ROOT_TYPE_ID_PROPERTY);
      if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
        setRootTypeId(pd.getValue().get((0)));

      PropertyDefinition gpd =
        eventType.getPropertyDefinition(GROUPBY_PROPERTY);
      if (gpd != null && gpd.getValue() != null && gpd.getValue().size() > 0)
        groupBy = gpd.getValue().get(0);
    }
    
    DocumentConfigBean configBean =
      (DocumentConfigBean)getBean("documentConfigBean");
    userDocTypes = configBean.getDocTypes();    
    models = new DocMatrixClientModels(userDocTypes);

    load();
  }
  
  //Accessors
  public void setCommand(String command)
  {
    this.command = command;
  }

  public String getCommand()
  {
    return command;
  }
  
  public void setDocId(String docId)
  {
    this.docId = docId;
  }

  public String getDocId()
  {
    return docId;
  }
  
  public EventDocument getEditingDocument()
  {
    return editingDocument;
  }

  public void setEditingDocument(EventDocument editingDocument)
  {
    this.editingDocument = editingDocument;
  }

  public DocMatrixClientModels getModels() {
    return models;
  }

  public void setModels(DocMatrixClientModels models) {
    this.models = models;
  }

  //Object actions
  public String show()
  {
    return "event_documents";
  }

  private void load()
  {
    try
    {
      if (!isNew())
      {
        EventDocumentFilter filter = new EventDocumentFilter();
        filter.setEventId(getObjectId());

        List<EventDocumentView> rows =
          AgendaConfigBean.getPort().findEventDocumentViewsFromCache(filter);

        setGroups(rows, getGroupExtractor());
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  @Override
  public String store()
  {
    if (editingDocument != null)
    {
      storeEventDocument();
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
    {
      try
      {
        spreadDocumentRoles(docId);
        EventDocument eventDocument = new EventDocument();
        eventDocument.setEventId(getObjectId());
        eventDocument.setDocId(docId);
        eventDocument.setEventDocTypeId(getRootTypeId());
        AgendaConfigBean.getPort().storeEventDocument(eventDocument);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    load();
    return null;
  }
  
  //Page actions
  public String createEventDocument()
  {
    editingDocument = new EventDocument();
    return null;
  }

  public String editEventDocument()
  {
    try
    {
      EventDocumentView row = (EventDocumentView)getExternalContext().
        getRequestMap().get("row");
      String eventDocId = row.getEventDocId();
      if (eventDocId != null)
        editingDocument =
          AgendaConfigBean.getPort().loadEventDocumentFromCache(eventDocId);
      else
        editingDocument = new EventDocument();
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
  }  
  
  public String removeEventDocument()
  {
    try
    {
      EventDocumentView row = (EventDocumentView)getExternalContext().
        getRequestMap().get("row");
      preRemove();
      AgendaConfigBean.getPort().removeEventDocument(row.getEventDocId());
      postRemove();
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public String storeEventDocument()
  {
    try
    {
      EventDocument eventDocument = new EventDocument();

      String objectId = editingDocument.getDocId();
      String[] split = DocumentConfigBean.fromObjectId(objectId);
      String documentId = split[0];
      if (documentId == null || documentId.isEmpty())
      {
        throw new Exception("DOCUMENT_MUST_BE_SELECTED");
      }
      spreadDocumentRoles(documentId);
      eventDocument.setDocId(documentId);
      eventDocument.setEventDocId(editingDocument.getEventDocId());
      eventDocument.setEventId(getObjectId());
      eventDocument.setComments(editingDocument.getComments());
      String eventDocTypeId = editingDocument.getEventDocTypeId();
      if (eventDocTypeId == null)
        eventDocTypeId = getRootTypeId();
      eventDocument.setEventDocTypeId(eventDocTypeId);
      
      preStore();
      AgendaConfigBean.getPort().storeEventDocument(eventDocument);
      postStore();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    finally
    {
      editingDocument = null;
      load();
    }
    return null;
  }  
  
  public String cancelEventDocument()
  {
    editingDocument = null;
    return null;
  }
  
  public String documentSent()
  {
    editingDocument = null;
    
    try
    {
      String docId = (String)models.getSendModel().parseResult();
      if (docId != null)
      {
        spreadDocumentRoles(docId);
        EventDocument eventDocument = new EventDocument();
        eventDocument.setDocId(docId);
        eventDocument.setEventId(getObjectId());
        eventDocument.setEventDocTypeId(getRootTypeId());
        preStore();
        AgendaConfigBean.getPort().storeEventDocument(eventDocument);
        postStore();
        
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
  
  public String documentUpdated()
  {
    try
    {
      models.getUpdateModel().parseResult();
      AgendaManagerClient.getCache().clear();
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

  public String showDocument()
  {
    EventDocumentView row = (EventDocumentView)getValue("#{row}");
    return getControllerBean().showObject(DictionaryConstants.DOCUMENT_TYPE,
      DocumentConfigBean.toObjectId(
        row.getDocument().getDocId(), row.getDocument().getVersion()));
  }

  public String createDocument()
  {
    return getControllerBean().createObject(DictionaryConstants.DOCUMENT_TYPE,
      "#{eventDocumentsBean.editingDocument.docId}");
  }

  public String searchDocument()
  {
    return getControllerBean().searchObject(DictionaryConstants.DOCUMENT_TYPE,
      "#{eventDocumentsBean.editingDocument.docId}");
  }

  //Auxiliar getters
  public List<SelectItem> getDocumentSelectItems()
  {
    DocumentBean documentBean = (DocumentBean)getBean("document2Bean");
    return documentBean.getSelectItems(editingDocument.getDocId());
  }

  public String getMimeType() 
  {
    EventDocumentView row = (EventDocumentView)getExternalContext().
      getRequestMap().get("row");
    Content content =
      (row.getDocument() != null ? row.getDocument().getContent() : null);
    if (content != null)
      return DocumentBean.getContentTypeIcon(content.getContentType());
    else
      return DocumentBean.getContentTypeIcon(null);
  }  
  
  public String getLanguage()
  {
    EventDocumentView row = (EventDocumentView)getExternalContext().
      getRequestMap().get("row");
    
    String language = row.getEventDocId() != null && row.getDocument() != null
      ? row.getDocument().getLanguage() : "";

    return DocumentUtils.extendLanguage(language);
  }
  
  public String getDocumentUrl()
  { 
    EventDocumentView row = (EventDocumentView)getValue("#{row}");
    if (row != null)
    {
      Document document = row.getDocument();
      return DocumentUrlBuilder.getDocumentUrl(document);
    }
    return "";
  }

  public String getDocumentCreationDate()
  {
    EventDocumentView row = (EventDocumentView)getValue("#{row}");

    Document document = row.getDocument();
    if (document != null)
    {
      String creationDate = row.getDocument().getCreationDate();
      if (creationDate != null)
        return TextUtils.formatDate(
          TextUtils.parseInternalDate(creationDate), "dd/MM/yyyy");
    }

    return null;
  }

  public Date getCreationDateTime()
  {
    if (editingDocument != null && editingDocument.getCreationDateTime() != null)
      return TextUtils.parseInternalDate(editingDocument.getCreationDateTime());
    else
      return null;
  }

  public Date getChangeDateTime()
  {
    if (editingDocument != null && editingDocument.getChangeDateTime() != null)
      return TextUtils.parseInternalDate(editingDocument.getChangeDateTime());
    else
      return null;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }
  
  @Override
  public Object getSelectedRow()
  {
    return getRequestMap().get("row");    
  }
    
  @Override
  protected String getRowTypeId(Object row)
  {
    EventDocumentView edvRow = (EventDocumentView)row;
    return edvRow.getEventDocTypeId();
  }  

  private void spreadDocumentRoles(String docId) throws Exception
  {
    EventMainBean eventMainBean = (EventMainBean)getBean("eventMainBean");
    String eventTypeId = eventMainBean.getEvent().getEventTypeId();
    if (eventTypeId != null)
    {
      Type eventType = TypeCache.getInstance().getType(eventTypeId);
      String spreadRoles = getSpreadRoles(eventType);
      if (spreadRoles != null)
      {
        Document document = DocumentConfigBean.getClient().loadDocument(docId);
        if (document != null)
        {
          boolean update = false;
          if ("true".equalsIgnoreCase(spreadRoles)) //Spread Case roles
          {
            List<AccessControl> accessControlList = new ArrayList();
            accessControlList.addAll(eventType.getAccessControl());
            accessControlList.addAll(
              eventMainBean.getEvent().getAccessControl());
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
              DocumentConfigBean.getClient().storeDocument(document);
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
      if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
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
