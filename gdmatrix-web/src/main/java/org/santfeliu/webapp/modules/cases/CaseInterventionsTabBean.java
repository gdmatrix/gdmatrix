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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;
import org.matrix.cases.Intervention;
import org.matrix.cases.InterventionFilter;
import org.matrix.cases.InterventionView;
import org.matrix.dic.DictionaryConstants;
import org.primefaces.PrimeFaces;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.helpers.GroupableRowsHelper;
import org.santfeliu.webapp.helpers.RowsFilterHelper;
import org.santfeliu.webapp.helpers.TablePropertyHelper;
import static org.santfeliu.webapp.setup.Action.POST_TAB_EDIT_ACTION;
import static org.santfeliu.webapp.setup.Action.POST_TAB_LOAD_ACTION;
import static org.santfeliu.webapp.setup.Action.POST_TAB_REMOVE_ACTION;
import static org.santfeliu.webapp.setup.Action.POST_TAB_STORE_ACTION;
import static org.santfeliu.webapp.setup.Action.PRE_TAB_EDIT_ACTION;
import static org.santfeliu.webapp.setup.Action.PRE_TAB_LOAD_ACTION;
import static org.santfeliu.webapp.setup.Action.PRE_TAB_REMOVE_ACTION;
import static org.santfeliu.webapp.setup.Action.PRE_TAB_STORE_ACTION;
import org.santfeliu.webapp.setup.TableProperty;
import org.santfeliu.webapp.util.DataTableRow;
import org.santfeliu.webapp.util.DataTableRow.Value;
import org.santfeliu.webapp.util.DataTableRowComparator;
import org.santfeliu.webapp.util.DateTimeRowStyleClassGenerator;
import org.santfeliu.webapp.util.RowStyleClassGenerator;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author blanquepa
 */
@Named
@RequestScoped
public class CaseInterventionsTabBean extends TabBean
{
  private static final String CASE_PERSON_TYPE_ID = "casePersonTypeId";
  
  @Inject
  CaseTypeBean caseTypeBean;

  Map<String, TabInstance> tabInstances = new HashMap<>();
  private final TabInstance EMPTY_TAB_INSTANCE = new TabInstance();
  private GroupableRowsHelper groupableRowsHelper;

  public class TabInstance
  {
    String objectId = NEW_OBJECT_ID;
    List<CaseInterventionsDataTableRow> rows;
    int firstRow = 0;
    RowsFilterHelper rowsFilterHelper =
      new RowsFilterHelper<CaseInterventionsDataTableRow>()
    {
      @Override
      public ObjectBean getObjectBean() 
      {
        return CaseInterventionsTabBean.this.getObjectBean();
      }
      
      @Override
      public List<CaseInterventionsDataTableRow> getRows()
      {
        return rows;
      }

      @Override
      public boolean isGroupedViewEnabled()
      {
        return CaseInterventionsTabBean.this.getGroupableRowsHelper().
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
        return CaseInterventionsTabBean.this.getColumns();        
      }

      @Override
      public String getFixedColumnValue(CaseInterventionsDataTableRow row, 
        String columnName) 
      {
        return null; //No fixed columns        
      }

      @Override
      public String getRowTypeId(CaseInterventionsDataTableRow row) 
      {
        return row.getTypeId();               
      }
    };

    public RowsFilterHelper getRowsFilterHelper()
    {
      return rowsFilterHelper;
    }
  }

  private Intervention editing;
  private String formSelector;
  private List<CasePersonView> casePersonViews;  

  @Inject
  CaseObjectBean caseObjectBean;

  @PostConstruct
  public void init()
  {
    groupableRowsHelper = new GroupableRowsHelper()
    {
      @Override
      public ObjectBean getObjectBean()
      {
        return CaseInterventionsTabBean.this.getObjectBean();
      }

      @Override
      public List<TableProperty> getColumns()
      {
        return CaseInterventionsTabBean.this.getColumns();
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
        return "intTypeId";
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

  @Override
  public ObjectBean getObjectBean()
  {
    return caseObjectBean;
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

  public Intervention getEditing()
  {
    return editing;
  }

  public void setEditing(Intervention editing)
  {
    this.editing = editing;
  }

  public String getFormSelector()
  {
    return formSelector;
  }

  public void setFormSelector(String formSelector)
  {
    this.formSelector = formSelector;
  }

  public String getPersonId()
  {
    return editing != null ? editing.getPersonId() : null;
  }

  public void setPersonId(String personId)
  {
    if (editing == null)
      editing = new Intervention();

    editing.setPersonId(personId);
  }

  public List<CaseInterventionsDataTableRow> getRows()
  {
    return getCurrentTabInstance().rows;
  }

  public void setRows(List<CaseInterventionsDataTableRow> rows)
  {
    this.getCurrentTabInstance().rows = rows;
  }

  public List<String> getOrderBy()
  {
    EditTab activeEditTab = caseObjectBean.getActiveEditTab();
    if (activeEditTab != null)
      return activeEditTab.getOrderBy();
    else
      return Collections.EMPTY_LIST;
  }

  public List<TableProperty> getTableProperties()
  {
    EditTab activeEditTab = caseObjectBean.getActiveEditTab();
    if (activeEditTab != null)
      return activeEditTab.getTableProperties();
    else
      return Collections.EMPTY_LIST;
  }

  public List<TableProperty> getColumns()
  {
    return TablePropertyHelper.getColumnTableProperties(getTableProperties());
  }

  public int getFirstRow()
  {
    return getCurrentTabInstance().firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    getCurrentTabInstance().firstRow = firstRow;
  }

  public String getStartDateTime()
  {
    if (editing == null)
      return null;

    String startDate = editing.getStartDate() != null ?
      editing.getStartDate() : "";
    String startTime = editing.getStartTime() != null ?
      editing.getStartTime() : "";

    return startDate + startTime;
  }

  public String getEndDateTime()
  {
    if (editing == null)
      return null;

    String endDate = editing.getEndDate() != null ?
      editing.getEndDate() : "";
    String endTime = editing.getEndTime() != null ?
      editing.getEndTime() : "";

    return endDate + endTime;
  }

  public void setStartDateTime(String dateTime)
  {
    String date = null;
    String time = null;

    if (dateTime != null)
    {
      date = dateTime.substring(0, 8);
      time = dateTime.length() > 8 ? dateTime.substring(8) : null;
    }

    if (editing != null)
    {
      editing.setStartDate(date);
      editing.setStartTime(time);
    }
  }

  public void setEndDateTime(String dateTime)
  {
    String date = null;
    String time = null;

    if (dateTime != null)
    {
      date = dateTime.substring(0, 8);
      time = dateTime.length() > 8 ? dateTime.substring(8) : null;
    }

    if (editing != null)
    {
      editing.setEndDate(date);
      editing.setEndTime(time);
    }
  }

  public void setIntTypeId(String intTypeId)
  {
    if (editing != null)
      editing.setIntTypeId(intTypeId);
  }

  public String getIntTypeId()
  {
    return editing == null ? NEW_OBJECT_ID : editing.getIntTypeId();
  }
  
  public void onPersonClear()
  {
    editing.setPersonId(null);
  }

  public void create()
  {
    executeTabAction(PRE_TAB_EDIT_ACTION, null);
    editing = new Intervention();
    editing.setIntTypeId(getCreationTypeId());
    setDefaultPerson(editing);    
    formSelector = null;
    executeTabAction(POST_TAB_EDIT_ACTION, editing);
  }

  @Override
  public void load() throws Exception
  {
    executeTabAction(PRE_TAB_LOAD_ACTION, null);
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        InterventionFilter filter = new InterventionFilter();
        filter.setCaseId(getObjectId());

        String typeId = getTabBaseTypeId();
        EditTab tab = caseObjectBean.getActiveEditTab();
        if (tab.isShowAllTypes())
          typeId = DictionaryConstants.INTERVENTION_TYPE;
        if (typeId != null)
          filter.setIntTypeId(typeId);

        List<InterventionView> interventions =
          CasesModuleBean.getPort(false).findInterventionViews(filter);

        List<CaseInterventionsDataTableRow> auxList =
          toDataTableRows(interventions);
        if (getOrderBy() != null)
        {
          Collections.sort(auxList,
            new DataTableRowComparator(getColumns(), getOrderBy()));
        }
        setRows(auxList);
        getCurrentTabInstance().rowsFilterHelper.load();
        if (isInactiveHidden())
        {
          ResourceBundle bundle = ResourceBundle.getBundle(
            "org.santfeliu.cases.web.resources.CaseBundle", getLocale());        
          warn(bundle.getString("caseInterventions_inactiveHiddenWarning"));
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
      getCurrentTabInstance().rowsFilterHelper.load();
      tabInstance.firstRow = 0;
    }
  }

  public void edit(DataTableRow row)
  {
    String intId = null;
    if (row != null)
      intId = row.getRowId();

    try
    {
      if (intId != null)
      {
        casePersonViews = null;
        executeTabAction(PRE_TAB_EDIT_ACTION, row);
        editing = CasesModuleBean.getPort(false).loadIntervention(intId);
        executeTabAction(POST_TAB_EDIT_ACTION, editing);
      }
      else
      {
        create();
      }
      formSelector = null;
    }
    catch (Exception ex)
    {
      error(ex);
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
        PrimeFaces.current().executeScript(
          "PF('caseInterventionsDialog').show()");
        return;
      }
    }
  }

  @Override
  public void store() throws Exception
  {
    try
    {
      String caseId = getObjectId();
      editing.setCaseId(caseId);
      if (editing.getStartDate() == null)
      {
        Date now = new Date();
        editing.setStartDate(TextUtils.formatDate(now, "yyyyMMdd"));
        editing.setStartTime(TextUtils.formatDate(now, "HHmmss"));
      }
      editing = (Intervention) executeTabAction(PRE_TAB_STORE_ACTION, editing);
      editing = CasesModuleBean.getPort(false).storeIntervention(editing);
      executeTabAction(POST_TAB_STORE_ACTION, editing);
      refreshHiddenTabInstances();
      load();
      editing = null;
      growl("STORE_OBJECT");
    }
    catch(Exception ex)
    {
      error(ex);
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

  public void remove(DataTableRow row)
  {
    try
    {
      if (row != null)
      {
        row = (DataTableRow) executeTabAction(PRE_TAB_REMOVE_ACTION, row);
        String intId = row.getRowId();
        CasesModuleBean.getPort(false).removeIntervention(intId);
        executeTabAction(POST_TAB_REMOVE_ACTION, row);
        refreshHiddenTabInstances();
        load();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
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
      editing = (Intervention)stateArray[0];
      formSelector = (String)stateArray[1];

      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public boolean isInactiveHidden()
  {
    Boolean hideInactive = (Boolean)UserSessionBean.getCurrentInstance()
      .getAttribute("hideInactiveInterventions");
    if (hideInactive == null)
      return false;
    else
      return hideInactive;
  }

  public void setInactiveHidden(boolean hideInactive)
  {
    try
    {
      UserSessionBean.getCurrentInstance().getAttributes()
        .put("hideInactiveInterventions", hideInactive); 
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public void switchInactive()
  {
    setInactiveHidden(!isInactiveHidden());
  }  
  
  public List<CasePersonView> getCasePersonViews()
  {
    if (casePersonViews != null)
      return casePersonViews;
    
    casePersonViews = new ArrayList<>();   
    
    String casePersonTypeId = null;
    EditTab tab = caseObjectBean.getActiveEditTab();
    if (tab != null)
      casePersonTypeId = tab.getProperties().getString(CASE_PERSON_TYPE_ID);
    if (casePersonTypeId == null)
      return casePersonViews;
 
    try
    {     
      CasePersonFilter filter = new CasePersonFilter(); 
      if (caseObjectBean.getObjectId() != null)
      {
        filter.setCaseId(caseObjectBean.getObjectId());
        filter.setCasePersonTypeId(casePersonTypeId);
        casePersonViews =
          CasesModuleBean.getPort(false).findCasePersonViews(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    
    return casePersonViews;
  }
  
  public boolean isSinglePerson()
  {
    return getCasePersonViews().size() == 1;
  }
  
  private void setDefaultPerson(Intervention editing)
  {
    casePersonViews = null;
    if (getCasePersonViews() != null && isSinglePerson())
    {
      String personId = getCasePersonViews().get(0).getPersonView().getPersonId();
      editing.setPersonId(personId);
    }
  }   

  private List<CaseInterventionsDataTableRow> toDataTableRows(
    List<InterventionView> interventions) throws Exception
  {     
    List<CaseInterventionsDataTableRow> convertedRows = new ArrayList<>();
    for (InterventionView row : interventions)
    {
      if (!isInactiveHidden() || row.getEndDate() == null || 
        row.getEndDate().compareTo(TextUtils.formatDate(new Date(), "yyyyMMdd")) > 0)
      {
        CaseInterventionsDataTableRow dataTableRow =
          new CaseInterventionsDataTableRow(row);
        dataTableRow.setValues(this, row, getTableProperties());
        dataTableRow.setStyleClass(getRowStyleClass(row));
        convertedRows.add(dataTableRow);
      }
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

  private RowStyleClassGenerator getRowStyleClassGenerator()
  {
    return new DateTimeRowStyleClassGenerator("startDate,startTime",
      "endDate,endTime", null);
  }

  private String getRowStyleClass(Object row)
  {
    RowStyleClassGenerator styleClassGenerator =
      getRowStyleClassGenerator();
    return styleClassGenerator.getStyleClass(row);
  }

  public class CaseInterventionsDataTableRow extends DataTableRow
  {
    private String startDateTime;
    private String endDateTime;
    private String personId;
    private String personName;
    private String personIdent;

    public CaseInterventionsDataTableRow(InterventionView row)
    {
      super(row.getIntId(), row.getIntTypeId());
      if (row.getStartDate() != null)
      {
        startDateTime = row.getStartDate();
        if (row.getStartTime() != null) startDateTime += row.getStartTime();
      }
      if (row.getEndDate() != null)
      {
        endDateTime = row.getEndDate();
        if (row.getEndTime() != null) endDateTime += row.getEndTime();
      }
      if (row.getPersonView() != null)
      {
        personId = row.getPersonView().getPersonId();
        personName = row.getPersonView().getFullName();
        personIdent = row.getPersonView().getNif() != null ?
          row.getPersonView().getNif() :
          row.getPersonView().getPassport();
      }
    }

    public String getStartDateTime()
    {
      return startDateTime;
    }

    public void setStartDateTime(String startDateTime)
    {
      this.startDateTime = startDateTime;
    }

    public String getEndDateTime()
    {
      return endDateTime;
    }

    public void setEndDateTime(String endDateTime)
    {
      this.endDateTime = endDateTime;
    }

    public String getPersonId()
    {
      return personId;
    }

    public void setPersonId(String personId)
    {
      this.personId = personId;
    }

    public String getPersonName()
    {
      return personName;
    }

    public void setPersonName(String personName)
    {
      this.personName = personName;
    }

    public String getPersonIdent()
    {
      return personIdent;
    }

    public void setPersonIdent(String personIdent)
    {
      this.personIdent = personIdent;
    }

    @Override
    protected Value getDefaultValue(String columnName)
    {
      if (columnName != null)
      {
        switch (columnName)
        {
          case "startDateTime":
            return new DateValue(getStartDateTime());
          case "endDateTime":
            return new DateValue(getEndDateTime());
          case "personId":
            return new NumericValue(getPersonId());
          case "personName":
            return new DefaultValue(getPersonName());
          case "personIdent":
            return new DefaultValue(getPersonIdent());
          default:
            break;
        }
      }
      return super.getDefaultValue(columnName);
    }
  }

}
