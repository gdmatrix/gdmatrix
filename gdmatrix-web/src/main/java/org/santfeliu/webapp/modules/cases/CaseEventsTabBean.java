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
package org.santfeliu.webapp.modules.cases;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.cases.CaseEvent;
import org.matrix.cases.CaseEventFilter;
import org.matrix.cases.CaseEventView;
import org.matrix.dic.Property;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.helpers.GroupableRowsHelper;
import org.santfeliu.webapp.helpers.TypeSelectHelper;
import org.santfeliu.webapp.modules.agenda.EventTypeBean;
import org.santfeliu.webapp.modules.dic.TypeTypeBean;
import org.santfeliu.webapp.setup.Column;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.util.DataTableRow;
import org.santfeliu.webapp.util.DataTableRowComparator;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author lopezrj-sf
 */
@Named
@ViewScoped
public class CaseEventsTabBean extends TabBean
{
  private CaseEvent editing;
  Map<String, TabInstance> tabInstances = new HashMap();
  private String formSelector;
  private final TabInstance EMPTY_TAB_INSTANCE = new TabInstance();  
  private GroupableRowsHelper groupableRowsHelper; 

  public class TabInstance
  {
    String objectId = NEW_OBJECT_ID;
    List<CaseEventsDataTableRow> rows;
    int firstRow = 0;
    TypeSelectHelper typeSelectHelper = new TypeSelectHelper()
    {
      @Override
      public List<? extends DataTableRow> getRows()
      {
        return rows;
      }

      @Override
      public boolean isGroupedViewEnabled()
      {
        return CaseEventsTabBean.this.getGroupableRowsHelper().
          isGroupedViewEnabled();
      }

      @Override
      public String getTabBaseTypeId()
      {
        return CaseEventsTabBean.this.getTabBaseTypeId();        
      }

      @Override
      public void resetFirstRow()
      {
        firstRow = 0;
      }      
    };
    
    public TypeSelectHelper getTypeSelectHelper()
    {
      return typeSelectHelper;
    }    
  }

  @Inject
  CaseObjectBean caseObjectBean;

  @Inject
  EventTypeBean eventTypeBean;

  @Inject
  TypeTypeBean typeTypeBean;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
    groupableRowsHelper = new GroupableRowsHelper()
    {
      @Override
      public ObjectBean getObjectBean()
      {
        return CaseEventsTabBean.this.getObjectBean();
      }

      @Override
      public List<Column> getColumns()
      {
        return CaseEventsTabBean.this.getColumns();
      }

      @Override
      public void sortRows()
      {
        Collections.sort(getCurrentTabInstance().rows, 
          new DataTableRowComparator(getColumns(), getOrderBy()));             
      }

      @Override
      public String getRowTypeColumnName()
      {
        return "caseEventTypeId";
      }

      @Override
      public String getFixedColumnValue(Object row, String columnName)
      {
        return null; //No fixed columns
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
    EditTab tab = caseObjectBean.getActiveEditTab();
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
    else
      return EMPTY_TAB_INSTANCE;
  }

  public Map<String, TabInstance> getTabInstances()
  {
    return tabInstances;
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
    return caseObjectBean;
  }

  public List<String> getOrderBy()
  {
    EditTab activeEditTab = caseObjectBean.getActiveEditTab();
    if (activeEditTab != null)
      return activeEditTab.getOrderBy();
    else
      return Collections.EMPTY_LIST;
  }

  public List<Column> getColumns()
  {
    EditTab activeEditTab = caseObjectBean.getActiveEditTab();
    if (activeEditTab != null)
      return activeEditTab.getColumns();
    else
      return Collections.EMPTY_LIST;
  }
  
  public List<Column> getCustomColumns()
  {
    EditTab activeEditTab = caseObjectBean.getActiveEditTab();
    if (activeEditTab != null)
      return activeEditTab.getCustomColumns();
    else
      return Collections.EMPTY_LIST;
  }  
  
  public CaseEvent getEditing()
  {
    return editing;
  }

  public void setEditing(CaseEvent editing)
  {
    this.editing = editing;
  }

  public void setCaseEventTypeId(String caseEventTypeId)
  {
    if (editing != null)
      editing.setCaseEventTypeId(caseEventTypeId);
  }

  public String getCaseEventTypeId()
  {
    return editing == null ? NEW_OBJECT_ID : editing.getCaseEventTypeId();
  }

  public List<CaseEventsDataTableRow> getRows()
  {
    return getCurrentTabInstance().rows;
  }

  public void setRows(List<CaseEventsDataTableRow> rows)
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

  public String getFormSelector()
  {
    return formSelector;
  }

  public void setFormSelector(String formSelector)
  {
    this.formSelector = formSelector;
  }

  public String getEventDescription()
  {
    if (editing != null && !isNew(editing))
    {
      return eventTypeBean.getDescription(editing.getEventId());
    }
    return "";
  }

  public void edit(DataTableRow row)
  {
    if (row != null)
    {
      try
      {      
        editing = CasesModuleBean.getPort(false).loadCaseEvent(row.getRowId());
      }
      catch (Exception ex)
      {
        error(ex);
      }
      formSelector = null;        
    }
    else
    {
      create();
    }
  }

  @Override
  public void load() throws Exception
  {
    executeTabAction("preTabLoad", null);    
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        String typeId = getTabBaseTypeId();
        CaseEventFilter filter = new CaseEventFilter();
        filter.setCaseId(caseObjectBean.getObjectId());
        filter.setCaseEventTypeId(typeId);
        filter.setExcludeMetadata(false);
        List<CaseEventView> events = 
          CasesModuleBean.getPort(false).findCaseEventViews(filter);
        List<CaseEventsDataTableRow> auxList = toDataTableRows(events);
        Collections.sort(auxList, 
          new DataTableRowComparator(getColumns(), getOrderBy()));
        setRows(auxList);
        getCurrentTabInstance().typeSelectHelper.load();        
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
      getCurrentTabInstance().typeSelectHelper.load();      
      tabInstance.firstRow = 0;
    }
    executeTabAction("postTabLoad", null);     
  }

  public void create()
  {
    editing = new CaseEvent();
    editing.setCaseEventTypeId(getCreationTypeId());
    formSelector = null;    
  }

  @Override
  public void store()
  {
    try
    {
      if (editing != null)
      {
        String caseId = caseObjectBean.getObjectId();
        editing.setCaseId(caseId);
        editing = (CaseEvent) executeTabAction("preTabStore", editing);        
        editing = CasesModuleBean.getPort(false).storeCaseEvent(editing);
        executeTabAction("postTabStore", editing);        
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
  
  public void remove(DataTableRow row)
  {
    if (row != null)
    {
      try
      {
        row = (DataTableRow)executeTabAction("preTabRemove", row);
        CasesModuleBean.getPort(false).removeCaseEvent(row.getRowId());
        executeTabAction("postTabRemove", row);
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
  
  public void addRowCustomProperty(DataTableRow row, String name, String value)
  {
    Property auxProperty = new Property();
    auxProperty.setName(name);
    auxProperty.getValue().add(value);
    row.getCustomProperties().add(auxProperty);
  }  
  
  @Override
  public void clear()
  {
    tabInstances.clear();
  }  

  @Override
  public Serializable saveState()
  {
    return new Object[]{ editing, formSelector };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      editing = (CaseEvent)stateArray[0];
      formSelector = (String)stateArray[1];
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private boolean isNew(CaseEvent attendant)
  {
    return (attendant != null && attendant.getCaseEventId() == null);
  }

  private List<CaseEventsDataTableRow> toDataTableRows(
    List<CaseEventView> events) throws Exception
  {
    List<CaseEventsDataTableRow> convertedRows = new ArrayList();
    for (CaseEventView row : events)
    {
      CaseEventsDataTableRow dataTableRow = new CaseEventsDataTableRow(row);
      dataTableRow.setValues(this, row, getColumns());
      dataTableRow.setCustomValues(this, row, getCustomColumns());
      convertedRows.add(dataTableRow);
    }
    return convertedRows;
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
  
  public class CaseEventsDataTableRow extends DataTableRow
  {
    private String eventId;
    private String eventTypeId;
    private String eventTitle;
    private String eventIniDate;
    private String eventEndDate;    

    public CaseEventsDataTableRow(CaseEventView row)
    {
      super(row.getCaseEventId(), row.getCaseEventTypeId());
      if (row.getEvent() != null)
      {
        eventId = row.getEvent().getEventId();
        eventTypeId = row.getEvent().getEventTypeId();
        eventTitle = row.getEvent().getSummary();
        eventIniDate = row.getEvent().getStartDateTime();
        eventEndDate = row.getEvent().getEndDateTime();      
      }
    }

    public String getEventId()
    {
      return eventId;
    }

    public void setEventId(String eventId)
    {
      this.eventId = eventId;
    }

    public String getEventTypeId()
    {
      return eventTypeId;
    }

    public void setEventTypeId(String eventTypeId)
    {
      this.eventTypeId = eventTypeId;
    }

    public String getEventTitle()
    {
      return eventTitle;
    }

    public void setEventTitle(String eventTitle)
    {
      this.eventTitle = eventTitle;
    }

    public String getEventIniDate()
    {
      return eventIniDate;
    }

    public void setEventIniDate(String eventIniDate)
    {
      this.eventIniDate = eventIniDate;
    }

    public String getEventEndDate()
    {
      return eventEndDate;
    }

    public void setEventEndDate(String eventEndDate)
    {
      this.eventEndDate = eventEndDate;
    }

    @Override
    protected Value getDefaultValue(String columnName)
    {
      if (columnName != null)
      {
        switch (columnName)
        {
          case "eventId":
            return new NumericValue(getEventId());
          case "eventTypeId":
            return new TypeValue(getEventTypeId());
          case "eventTitle":
            return new DefaultValue(getEventTitle());
          case "eventIniDate":
            return new DateValue(getEventIniDate());
          case "eventEndDate":
            return new DateValue(getEventEndDate());
          default:
            break;
        }
      }
      return super.getDefaultValue(columnName);
    }
  }  

}
