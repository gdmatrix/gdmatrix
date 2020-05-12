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

import java.util.List;

import javax.faces.model.SelectItem;
import org.matrix.agenda.AgendaConstants;
import org.matrix.agenda.Event;

import org.matrix.cases.CaseEvent;
import org.matrix.cases.CaseEventFilter;
import org.matrix.cases.CaseEventView;
import org.matrix.cases.CaseManagerPort;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.PropertyDefinition;
import org.santfeliu.cases.web.CaseBean;
import org.santfeliu.cases.web.CaseConfigBean;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.web.obj.DynamicTypifiedPageBean;

/**
 *
 * @author unknown
 */
public class EventCasesBean extends DynamicTypifiedPageBean
{
  public static final String ROOT_TYPE_ID_PROPERTY = "_caseRootTypeId";
  public static final String GROUPBY_PROPERTY = "_casesGroupBy";
  public static final String GROUP_SELECTION_MODE_PROPERTY = "_casesGroupSelectionMode";    
  public static final String HELP_PROPERTY = "_casesHelp";
  
  private CaseEvent editingCase;
  private List<CaseEventView> rows;

  public EventCasesBean()
  {
    super(DictionaryConstants.CASE_EVENT_TYPE, AgendaConstants.AGENDA_ADMIN_ROLE);

    EventMainBean eventMainBean = (EventMainBean)getBean("eventMainBean");
    Event event = eventMainBean.getEvent();
    Type eventType = TypeCache.getInstance().getType(event.getEventTypeId());
    if (eventType != null)
    {
      PropertyDefinition pd1 =
        eventType.getPropertyDefinition(ROOT_TYPE_ID_PROPERTY);
      if (pd1 != null && pd1.getValue() != null && pd1.getValue().size() > 0)
        setRootTypeId(pd1.getValue().get((0)));
      
      PropertyDefinition pd2 =
        eventType.getPropertyDefinition(GROUP_SELECTION_MODE_PROPERTY);
      if (pd2 != null && pd2.getValue() != null && pd2.getValue().size() > 0)
        groupSelectionMode = pd2.getValue().get(0);

      PropertyDefinition pd3 =
        eventType.getPropertyDefinition(GROUPBY_PROPERTY);
      if (pd3 != null && pd3.getValue() != null && pd3.getValue().size() > 0)
        groupBy = pd3.getValue().get(0);      
    }
    load();
  }

  public CaseEvent getEditingCase()
  {
    return editingCase;
  }

  public void setEditingCase(CaseEvent editingCase)
  {
    this.editingCase = editingCase;
  }
  
  public void setRows(List<CaseEventView> rows)
  {
    this.rows = rows;
  }

  public String show()
  {
    return "event_cases";
  }

  public String store()
  {
    if (editingCase != null)
    {
      storeCase();
    }
    else
    {
      load();
    }
    return show();
  }
  
  public String showCase()
  {
    return getControllerBean().showObject("Case",
     (String)getValue("#{row.caseObject.caseId}"));
  }
  
  public String searchCase()
  {
    return getControllerBean().searchObject("Case",
      "#{eventCasesBean.editingCase.caseId}");
  }
  
  public String removeCase()
  {
    try
    {
      CaseEventView row = (CaseEventView)getRequestMap().get("row");
      preRemove();
      CaseManagerPort port = CaseConfigBean.getPort();
      port.removeCaseEvent(row.getCaseEventId());
      postRemove();
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String storeCase()
  {
    try
    {
      String eventId = getObjectId();
      editingCase.setEventId(eventId);
      
      org.matrix.dic.Type type = getCurrentType();
      if (type != null)
        editingCase.setCaseEventTypeId(type.getTypeId());
      
      editingCase.getProperty().clear();
      List properties = getFormDataAsProperties();
      if (properties != null)
        editingCase.getProperty().addAll(properties);
            
      preStore();
      CaseManagerPort port = CaseConfigBean.getPort();
      port.storeCaseEvent(editingCase);
      
      postStore();
      
      editingCase = null;
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String createCase()
  {
    editingCase = new CaseEvent();
    getData().clear();
    setCurrentTypeId(null);
    return null;
  }
  
  public String editCase()
  {
    try
    {
      CaseEventView row = (CaseEventView)getExternalContext().
        getRequestMap().get("row");   
      String caseEventId = row.getCaseEventId();
      if (caseEventId != null)
      {
        editingCase = CaseConfigBean.getPort().loadCaseEvent(caseEventId);
        setCurrentTypeId(editingCase.getCaseEventTypeId());
        setFormDataFromProperties(editingCase.getProperty());
      }
      else
      {
        editingCase = new CaseEvent();
        getData().clear();        
        setCurrentTypeId(null);
      }        
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
  }
 
  public String cancelCase()
  {
    editingCase = null;
    return null;
  }
  
  public List<SelectItem> getCaseSelectItems()
  {
    CaseBean caseBean = (CaseBean)getBean("caseBean");
    return caseBean.getSelectItems(editingCase.getCaseId());
  }
  
  protected void load()
  {
    try
    {
      editingCase = null;            
      if (!isNew())
      {
        CaseEventFilter filter = new CaseEventFilter();
        filter.setEventId(getObjectId());
        rows = CaseConfigBean.getPort().findCaseEventViews(filter);
        setGroups(rows, getGroupExtractor());
        //TODO Order: parametrize using GroupablePageBean
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }
  
  public String getRowTypeLabel()
  {
    CaseEventView caseEventView = 
      (CaseEventView)getExternalContext().getRequestMap().get("row");
    if (caseEventView.getCaseEventId() != null)
    {
      String typeId = caseEventView.getCaseEventTypeId();
      TypeCache typeCache = TypeCache.getInstance();
      try
      {
        Type type = typeCache.getType(typeId);
        if (type != null) return type.getDescription();
      }
      catch (Exception ex)
      {
        warn(ex.getMessage());
      }
    }
    return null;
  }
  
  public String getCasesHelp()
  {
    EventMainBean eventMainBean = (EventMainBean)getBean("eventMainBean");
    Event event = eventMainBean.getEvent();
    if (event != null)
    {      
      String typeId = event.getEventTypeId();    
      if (typeId != null)
      {
        Type eventType = TypeCache.getInstance().getType(typeId);
        if (eventType != null)
        {
          PropertyDefinition pd =
            eventType.getPropertyDefinition(HELP_PROPERTY);
          if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
            return pd.getValue().get(0);
        }        
      }
    }
    return null;
  }
  
  @Override
  public Object getSelectedRow()
  {
    return getRequestMap().get("row");    
  }
  
  @Override
  protected String getRowTypeId(Object row)
  {
    CaseEventView cevRow = (CaseEventView)row;
    return cevRow.getCaseEventTypeId();
  }
}
