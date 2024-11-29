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
import org.matrix.cases.CaseEvent;
import org.matrix.cases.CaseEventFilter;
import org.matrix.cases.CaseEventView;
import org.matrix.dic.DictionaryConstants;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.helpers.GroupableRowsHelper;
import org.santfeliu.webapp.helpers.TablePropertyHelper;
import org.santfeliu.webapp.modules.cases.CaseTypeBean;
import org.santfeliu.webapp.modules.cases.CasesModuleBean;
import org.santfeliu.webapp.modules.dic.TypeTypeBean;
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
public class EventCasesTabBean extends TabBean
{
  private final TabInstance EMPTY_TAB_INSTANCE = new TabInstance();

  private CaseEvent editing;
  Map<String, TabInstance> tabInstances = new HashMap();
  private String formSelector;
  private GroupableRowsHelper groupableRowsHelper;

  public class TabInstance
  {
    String objectId = NEW_OBJECT_ID;
    List<CaseEventsDataTableRow> rows;
    int firstRow = 0;
  }

  @Inject
  EventObjectBean eventObjectBean;

  @Inject
  CaseTypeBean caseTypeBean;

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
        return EventCasesTabBean.this.getObjectBean();
      }

      @Override
      public List<TableProperty> getColumns()
      {
        return EventCasesTabBean.this.getColumns();
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

  public List<String> getOrderBy()
  {
    EditTab activeEditTab = eventObjectBean.getActiveEditTab();
    if (activeEditTab != null)
      return activeEditTab.getOrderBy();
    else
      return Collections.EMPTY_LIST;
  }

  public List<TableProperty> getTableProperties()
  {
    EditTab activeEditTab = eventObjectBean.getActiveEditTab();
    if (activeEditTab != null)
      return activeEditTab.getTableProperties();
    else
      return Collections.EMPTY_LIST;
  }

  public List<TableProperty> getColumns()
  {
    return TablePropertyHelper.getColumnTableProperties(getTableProperties());
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

  public String getCaseDescription()
  {
    if (editing != null && !isNew(editing))
    {
      return caseTypeBean.getDescription(editing.getCaseId());
    }
    return "";
  }

  public void edit(DataTableRow row)
  {
    if (row != null)
    {
      try
      {
        executeTabAction("preTabEdit", row);
        editing = CasesModuleBean.getPort(false).
          loadCaseEvent(row.getRowId());
        executeTabAction("postTabEdit", editing);
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
        filter.setEventId(eventObjectBean.getObjectId());
        EditTab tab = eventObjectBean.getActiveEditTab();
        if (tab.isShowAllTypes())
          typeId = DictionaryConstants.CASE_EVENT_TYPE;
        filter.setCaseEventTypeId(typeId);
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
        executeTabAction("postTabLoad", null);
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
    executeTabAction("preTabEdit", null);
    editing = new CaseEvent();
    editing.setCaseEventTypeId(getCreationTypeId());
    formSelector = null;
    executeTabAction("postTabEdit", editing);
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
        editing = (CaseEvent)executeTabAction("preTabStore", editing);
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
    private String caseId;
    private String caseTypeId;
    private String caseTitle;

    public CaseEventsDataTableRow(CaseEventView row)
    {
      super(row.getCaseEventId(), row.getCaseEventTypeId());
      if (row.getCaseObject() != null)
      {
        caseId = row.getCaseObject().getCaseId();
        caseTypeId = row.getCaseObject().getCaseTypeId();
        caseTitle = row.getCaseObject().getTitle();
      }
    }

    public String getCaseId()
    {
      return caseId;
    }

    public void setCaseId(String caseId)
    {
      this.caseId = caseId;
    }

    public String getCaseTypeId()
    {
      return caseTypeId;
    }

    public void setCaseTypeId(String caseTypeId)
    {
      this.caseTypeId = caseTypeId;
    }

    public String getCaseTitle()
    {
      return caseTitle;
    }

    public void setCaseTitle(String caseTitle)
    {
      this.caseTitle = caseTitle;
    }

    @Override
    protected Value getDefaultValue(String columnName)
    {
      if (columnName != null)
      {
        switch (columnName)
        {
          case "caseId":
            return new DefaultValue(getCaseId());
          case "caseTypeId":
            return new TypeValue(getCaseTypeId());
          case "caseTitle":
            return new DefaultValue(getCaseTitle());
          default:
            break;
        }
      }
      return super.getDefaultValue(columnName);
    }
  }

}
