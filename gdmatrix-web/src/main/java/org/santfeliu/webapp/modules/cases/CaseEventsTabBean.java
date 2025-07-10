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
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.cases.CaseEvent;
import org.matrix.cases.CaseEventFilter;
import org.matrix.cases.CaseEventView;
import org.matrix.dic.DictionaryConstants;
import org.primefaces.PrimeFaces;
import org.santfeliu.webapp.DataTableRowExportable;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.helpers.GroupableRowsHelper;
import org.santfeliu.webapp.helpers.RowsExportHelper;
import org.santfeliu.webapp.helpers.RowsFilterHelper;
import org.santfeliu.webapp.helpers.TablePropertyHelper;
import org.santfeliu.webapp.modules.agenda.EventTypeBean;
import org.santfeliu.webapp.modules.dic.TypeTypeBean;
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
import org.santfeliu.webapp.util.DataTableRow;
import org.santfeliu.webapp.util.DataTableRowComparator;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author lopezrj-sf
 */
@Named
@RequestScoped
public class CaseEventsTabBean extends TabBean  
  implements DataTableRowExportable
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
    RowsFilterHelper rowsFilterHelper = RowsFilterHelper.create(null, prev -> 
      new RowsFilterHelper<CaseEventsDataTableRow>(prev)
    {
      @Override
      public ObjectBean getObjectBean() 
      {
        return CaseEventsTabBean.this.getObjectBean();
      }
      
      @Override
      public List<CaseEventsDataTableRow> getRows()
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
      public void resetFirstRow()
      {
        firstRow = 0;
      }

      @Override
      public List<TableProperty> getColumns() 
      {
        return CaseEventsTabBean.this.getColumns();
      }

      @Override
      public Item getFixedColumnValue(CaseEventsDataTableRow row, 
        String columnName) 
      {
        return null; //No fixed columns
      }

      @Override
      public String getRowTypeId(CaseEventsDataTableRow row) 
      {
        return row.getTypeId();               
      }
    });

    RowsFilterHelper rowsFilterHelper2 = 
      RowsFilterHelper.create(rowsFilterHelper, prev -> 
        new RowsFilterHelper<CaseEventsDataTableRow>(prev)
    {
      @Override
      public ObjectBean getObjectBean() 
      {
        return CaseEventsTabBean.this.getObjectBean();
      }
      
      @Override
      public List<CaseEventsDataTableRow> getRows()
      {
        return prev.getFilteredRows();
      }

      @Override
      public boolean isGroupedViewEnabled()
      {
        return CaseEventsTabBean.this.getGroupableRowsHelper().
          isGroupedViewEnabled();
      }

      @Override
      public void resetFirstRow()
      {
        firstRow = 0;
      }

      @Override
      public List<TableProperty> getColumns() 
      {
        return CaseEventsTabBean.this.getColumns();        
      }

      @Override
      public Item getFixedColumnValue(CaseEventsDataTableRow row, 
        String columnName) 
      {
        return null; //No fixed columns        
      }

      @Override
      public String getRowTypeId(CaseEventsDataTableRow row) 
      {
        return row.getTypeId();               
      }      
    });
    
    public RowsFilterHelper getRowsFilterHelper()
    {
      return rowsFilterHelper;
    }
    
    public RowsFilterHelper getRowsFilterHelper2()
    {
      return rowsFilterHelper2;
    }
    
    public RowsFilterHelper getActiveRowsFilterHelper()
    {
      if (rowsFilterHelper2.isRendered())
      {
        return rowsFilterHelper2;
      }
      else
      {
        return rowsFilterHelper;
      }
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
    groupableRowsHelper = new GroupableRowsHelper()
    {
      @Override
      public ObjectBean getObjectBean()
      {
        return CaseEventsTabBean.this.getObjectBean();
      }

      @Override
      public List<TableProperty> getColumns()
      {
        return CaseEventsTabBean.this.getColumns();
      }

      @Override
      public void sortRows()
      {
        if (getOrderBy() != null)
        {
          Collections.sort(getCurrentTabInstance().rows,
            new DataTableRowComparator(getColumns(), getOrderBy()));
        }
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

  @Override
  public List<TableProperty> getTableProperties()
  {
    EditTab activeEditTab = caseObjectBean.getActiveEditTab();
    if (activeEditTab != null)
      return activeEditTab.getTableProperties();
    else
      return Collections.EMPTY_LIST;
  }

  @Override
  public List<TableProperty> getColumns()
  {
    return TablePropertyHelper.getColumnTableProperties(getTableProperties());
  }

  @Override
  public List<? extends DataTableRow> getExportableRows() 
  {
    return getCurrentTabInstance().getActiveRowsFilterHelper().
      getFilteredRows();    
  }  

  @Override
  public int getRowExportLimit()
  {
    return RowsExportHelper.getActiveEditTabRowExportLimit(caseObjectBean);
  }
  
  @Override
  public boolean isExportable()
  {
    return RowsExportHelper.isActiveEditTabExportable(caseObjectBean);
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
        executeTabAction(PRE_TAB_EDIT_ACTION, row);
        editing = CasesModuleBean.getPort(false).loadCaseEvent(row.getRowId());
        executeTabAction(POST_TAB_EDIT_ACTION, editing);
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
  public void edit(String rowId)
  {
    List<? extends DataTableRow> rows = getRows();
    for (DataTableRow row : rows)
    {       
      if (row.getRowId().equals(rowId))
      {
        edit(row);
        PrimeFaces.current().executeScript("PF('caseEventsDialog').show()");
        return;
      }
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
        CaseEventFilter filter = new CaseEventFilter();
        String typeId = getTabBaseTypeId();
        EditTab tab = caseObjectBean.getActiveEditTab();
        if (tab.isShowAllTypes())
          typeId = DictionaryConstants.CASE_EVENT_TYPE;
        filter.setCaseEventTypeId(typeId);

        filter.setCaseId(caseObjectBean.getObjectId());
        filter.setExcludeMetadata(false);
        List<CaseEventView> events =
          CasesModuleBean.getPort(false).findCaseEventViews(filter);
        List<CaseEventsDataTableRow> auxList = toDataTableRows(events);
        if (getOrderBy() != null)
        {
          Collections.sort(auxList,
            new DataTableRowComparator(getColumns(), getOrderBy()));
        }
        setRows(auxList);
        getCurrentTabInstance().rowsFilterHelper.refresh();
        getCurrentTabInstance().rowsFilterHelper2.refresh();
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
      getCurrentTabInstance().rowsFilterHelper.refresh();
      getCurrentTabInstance().rowsFilterHelper2.refresh();
      tabInstance.firstRow = 0;
    }
  }

  public void create()
  {
    executeTabAction(PRE_TAB_EDIT_ACTION, null);
    editing = new CaseEvent();
    editing.setCaseEventTypeId(getCreationTypeId());
    formSelector = null;
    executeTabAction(POST_TAB_EDIT_ACTION, editing);
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
        editing = (CaseEvent) executeTabAction(PRE_TAB_STORE_ACTION, editing);
        editing = CasesModuleBean.getPort(false).storeCaseEvent(editing);
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

  public void remove(DataTableRow row)
  {
    if (row != null)
    {
      try
      {
        row = (DataTableRow)executeTabAction(PRE_TAB_REMOVE_ACTION, row);
        CasesModuleBean.getPort(false).removeCaseEvent(row.getRowId());
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
      dataTableRow.setValues(this, row, getTableProperties());
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
