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
import java.util.Set;
import java.util.TreeMap;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.cases.Case;
import org.matrix.cases.CaseCase;
import org.matrix.cases.CaseCaseFilter;
import org.matrix.cases.CaseCaseView;
import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Property;
import org.primefaces.PrimeFaces;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.util.TextUtils;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.setup.Column;
import org.santfeliu.webapp.setup.ObjectSetup;
import org.santfeliu.webapp.util.DataTableRow;

/**
 *
 * @author blanquepa
 */
@Named
@ViewScoped
public class CaseCasesTabBean extends TabBean
{
  private static final String TYPEID_SEPARATOR = ";";

  Map<String, TabInstance> tabInstances = new HashMap<>();

  public class TabInstance
  {
    String objectId = NEW_OBJECT_ID;
    String typeId = getTabBaseTypeId();
    List<CaseCasesDataTableRow> rows;
    int firstRow = 0;
    boolean groupedView = isGroupedViewEnabled();
  }

  private CaseCase editing;
  private String formSelector;

  @Inject
  CaseObjectBean caseObjectBean;

  @Inject
  CaseTypeBean caseTypeBean;
  
  public TabInstance getCurrentTabInstance()
  {
    EditTab tab = caseObjectBean.getActiveEditTab();
    TabInstance tabInstance = tabInstances.get(tab.getSubviewId());
    if (tabInstance == null)
    {
      tabInstance = new TabInstance();
      tabInstances.put(tab.getSubviewId(), tabInstance);
    }
    return tabInstance;
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

  public List<CaseCasesDataTableRow> getRows()
  {
    return getCurrentTabInstance().rows;
  }

  public void setRows(List<CaseCasesDataTableRow> rows)
  {
    getCurrentTabInstance().rows = rows;
  }

  public List<Column> getColumns()
  {
    EditTab activeEditTab = caseObjectBean.getActiveEditTab();
    List<Column> columns = activeEditTab.getColumns();
    if (columns == null || columns.isEmpty())
    {
      //Get default objectSetup columns configuration
      ObjectSetup defaultSetup = caseTypeBean.getObjectSetup();
      EditTab defaultEditTab =
        defaultSetup.findEditTabByViewId(activeEditTab.getViewId());
      columns = defaultEditTab.getColumns();
    }
    return columns;
  }

  public CaseCase getEditing()
  {
    return editing;
  }

  public void setEditing(CaseCase editing)
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

  public int getFirstRow()
  {
    return getCurrentTabInstance().firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    getCurrentTabInstance().firstRow = firstRow;
  }

  public boolean isGroupedView()
  {
    return getCurrentTabInstance().groupedView;
  }

  public void setGroupedView(boolean groupedView)
  {
    getCurrentTabInstance().groupedView = groupedView;
  }

  public boolean isGroupedViewEnabled()
  {
    return Boolean.parseBoolean(caseObjectBean.getActiveEditTab().
      getProperties().getString("groupedViewEnabled"));
  }

  public void setCaseCaseTypeId(String caseCaseTypeId)
  {
    if (editing != null)
      editing.setCaseCaseTypeId(caseCaseTypeId);

    showDialog();
  }

  public String getCaseCaseTypeId()
  {
    return editing == null ? NEW_OBJECT_ID : editing.getCaseCaseTypeId();
  }

  public void setRelCaseId(String caseId)
  {
    if (editing != null)
      editing.setRelCaseId(caseId);

    showDialog();
  }

  public String getRelCaseId()
  {
    return editing == null ? NEW_OBJECT_ID : editing.getRelCaseId();
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return caseObjectBean;
  }

  public void create()
  {
    editing = new CaseCase();
    editing.setCaseId(getObjectId());
    String baseTypeId = getTabBaseTypeId();
    if (baseTypeId != null && !baseTypeId.contains(TYPEID_SEPARATOR))
      editing.setCaseCaseTypeId(baseTypeId);
  }

  public void switchView()
  {
    getCurrentTabInstance().groupedView = !getCurrentTabInstance().groupedView;
  }

  public String getCaseDescription()
  {
    if (editing != null && !isNew(editing))
    {
      if (editing.getCaseId().equals(getObjectId())) //direct
      {
        return caseTypeBean.getDescription(editing.getRelCaseId());
      }
      else //reverse
      {
        return caseTypeBean.getDescription(editing.getCaseId());
      }
    }
    return "";
  }

  //TODO: get property from JSON
  public String getTabBaseTypeId()
  {
    String typeId;

    String tabPrefix = String.valueOf(caseObjectBean.getEditTabSelector());
    typeId = getProperty("tabs::" + tabPrefix + "::typeId");
    if (typeId == null)
    {
      EditTab editTab = caseObjectBean.getActiveEditTab();
      typeId = editTab.getProperties().getString("typeId");
      if (typeId == null)
      {
        typeId = DictionaryConstants.CASE_CASE_TYPE;
      }
    }
    return typeId;
  }

  @Override
  public void load() throws Exception
  {
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        String typeId = getCurrentTabInstance().typeId;

        String[] params = typeId.split(TYPEID_SEPARATOR);
        if (params != null && params.length == 2)
        {
          String cpTypeId1 = params[0];
          String cpTypeId2 = params[1];
          getCurrentTabInstance().rows =
            getResultsByPersons(cpTypeId1, cpTypeId2);
        }
        else
        {
          getCurrentTabInstance().rows = getResultsByDefault(typeId);
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

  public void edit(DataTableRow row)
  {
    String caseCaseId = null;
    if (row != null)
      caseCaseId = row.getRowId();

    try
    {
      if (caseCaseId != null)
      {
        editing = CasesModuleBean.getPort(false).loadCaseCase(caseCaseId);
      }
      else
      {
        create();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  @Override
  public void store() throws Exception
  {
    String objectId = getObjectId();
    if (editing != null)
    {
      if (editing.getRelCaseId() == null || editing.getRelCaseId().isEmpty())
        throw new Exception("CASE_MUST_BE_SELECTED");

      if (!editing.getCaseId().equals(objectId))
        throw new Exception("CAN_NOT_STORE_REVERSE_RELATION");

      CasesModuleBean.getPort(false).storeCaseCase(editing);
      refreshHiddenTabInstances();
      load();
      editing = null;
    }
  }

  public void cancel()
  {
    editing = null;
  }

  public void remove(DataTableRow row)
  {
    try
    {
      if (row != null)
      {
        String caseCaseId = row.getRowId();
        CasesModuleBean.getPort(false).removeCaseCase(caseCaseId);
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
      editing = (CaseCase)stateArray[0];
      formSelector = (String)stateArray[1];

      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private void showDialog()
  {
    try
    {
      PrimeFaces current = PrimeFaces.current();
      current.executeScript("PF('caseCasesDialog').show();");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private boolean isNew(CaseCase caseCase)
  {
    return (caseCase != null && caseCase.getCaseCaseId() == null);
  }

  private List<CaseCasesDataTableRow> getResultsByDefault(String caseCaseTypeId)
    throws Exception
  {
    CaseCaseFilter filter = new CaseCaseFilter();
    filter.setCaseId(getObjectId());
    filter.setCaseCaseTypeId(caseCaseTypeId);
    List<CaseCaseView> results
      = CasesModuleBean.getPort(false).findCaseCaseViews(filter);

    //Reverse cases
    filter = new CaseCaseFilter();
    filter.setRelCaseId(getObjectId());
    filter.setCaseCaseTypeId(caseCaseTypeId);
    List<CaseCaseView> revResults
      = CasesModuleBean.getPort(false).findCaseCaseViews(filter);

    if (revResults != null && !revResults.isEmpty())
    {
      int maxResults = results.size() + revResults.size();
      int revCount = revResults.size();

      for (int i = 0; i < revCount && results.size() <= maxResults; i++)
      {
        results.add(revResults.get(i));
      }
    }

    return toDataTableRows(results);
  }

  private List<CaseCasesDataTableRow> getResultsByPersons(String typeId1,
    String typeId2) throws Exception
  {
    CasePersonFilter filter = new CasePersonFilter();
    filter.setCaseId(getObjectId());
    filter.setCasePersonTypeId(typeId1);
    List<CasePersonView> casePersons
      = CasesModuleBean.getPort(false).findCasePersonViews(filter);

    Case mainCase = caseObjectBean.getCase();
    CaseMatcher matcher = new CaseMatcher(mainCase, casePersons);

    for (String personId : matcher.getPersonIds())
    {
      filter = new CasePersonFilter();
      filter.setPersonId(personId);
      filter.setCasePersonTypeId(typeId2);
      casePersons = CasesModuleBean.getPort(false).findCasePersonViews(filter);
      matcher.addIfMatch(casePersons);
    }

    return toDataTableRows(matcher.getResults());
  }

  private List<CaseCasesDataTableRow> toDataTableRows(List<CaseCaseView>
    caseCaseViews) throws Exception
  {
    List<CaseCasesDataTableRow> convertedRows = new ArrayList<>();
    for (CaseCaseView row : caseCaseViews)
    {
      CaseCasesDataTableRow dataTableRow =
        new CaseCasesDataTableRow(row.getCaseCaseId(), row.getCaseCaseTypeId(),
        row.getMainCase(), row.getRelCase());
      dataTableRow.setValues(row, getColumns());
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

  //TODO: Replace by expressions in Columns
  public class CaseCasesDataTableRow extends DataTableRow
  {
    private boolean reverseRelation;
    private String mainTypeId;
    private String mainCaseId;
    private String mainTitle;
    private String relTypeId;
    private String relCaseId;
    private String relTitle;

    public CaseCasesDataTableRow(String rowId, String typeId,
      Case mainCase, Case relCase)
    {
      super(rowId, typeId);
      mainCaseId = mainCase.getCaseId();
      mainTypeId = mainCase.getCaseTypeId();
      mainTitle = mainCase.getTitle();
      relCaseId = relCase.getCaseId();
      relTypeId = relCase.getCaseTypeId();
      relTitle = relCase.getTitle();

      String objectId = getObjectId();
      reverseRelation =
        objectId.equals(relCaseId) && !objectId.equals(mainCaseId);
    }

    public boolean isReverseRelation()
    {
      return reverseRelation;
    }

    public String getMainTypeId()
    {
      return mainTypeId;
    }

    public String getMainCaseId()
    {
      return mainCaseId;
    }

    public String getMainTitle()
    {
      return mainTitle;
    }

    public void setMainTitle(String mainTitle)
    {
      this.mainTitle = mainTitle;
    }

    public String getRelTypeId()
    {
      return relTypeId;
    }

    public String getRelCaseId()
    {
      return relCaseId;
    }

    public String getRelTitle()
    {
      return relTitle;
    }

    public void setRelTitle(String relTitle)
    {
      this.relTitle = relTitle;
    }
    
    public String getCaseId()
    {
      return isReverseRelation() ? getMainCaseId() : getRelCaseId();
    }
    
    public String getCaseTitle()
    {
      String caseTitle = 
        isReverseRelation() ? getMainTitle() : getRelTitle();
      return caseTitle;
    }
    
    @Override
    public void setValues(Object row, List<Column> columns) 
      throws Exception
    {
      values = new Object[columns.size()];
      for (int i = 0; i < columns.size(); i++)
      {
        Column column = columns.get(i);
        if ("caseId".equals(column.getName()))
          values[i] = getCaseId();
        else if ("caseTitle".equals(column.getName()))
          values[i] = getCaseTitle();
        else
        {
          Property property = DictionaryUtils.getProperty(row, column.getName());        
          if (property != null)
          {
            String columnName = column.getName();
            List<String> value = property.getValue();
            values[i] = formatValue(typeId, columnName, value);            
          }
        }
      } 
    }      

  }

  private class CaseMatcher
  {

    private Case mainCase;
    private final Map<String, List<Period>> map = new HashMap<>();

    private TreeMap<String, CaseCaseView> results = new TreeMap<>();

    public CaseMatcher(Case mainCase, List<CasePersonView> casePersons)
    {
      this.mainCase = mainCase;
      for (CasePersonView casePerson : casePersons)
      {
        putToMap(casePerson);
      }
    }

    public Set<String> getPersonIds()
    {
      return map.keySet();
    }

    public void addIfMatch(List<CasePersonView> casePersons)
    {
      for (CasePersonView casePerson : casePersons)
      {
        addIfMatch(casePerson);
      }
    }

    public List<CaseCaseView> getResults()
    {
      List<CaseCaseView> list = new ArrayList<>();
      list.addAll(results.values());
      return list;
    }

    private void addIfMatch(CasePersonView casePerson)
    {
      String key = casePerson.getPersonView().getPersonId() + ";"
        + casePerson.getStartDate() + ";" + casePerson.getEndDate();
      CaseCaseView item = results.get(key);
      if (item == null && isWithinRange(casePerson))
      {
        CaseCaseView caseCaseView = new CaseCaseView();
        Case relCase = casePerson.getCaseObject();
        caseCaseView.setCaseCaseTypeId(relCase.getCaseTypeId());
        caseCaseView.setMainCase(mainCase);
        caseCaseView.setRelCase(relCase);
        //TODO: Merge dates
        caseCaseView.setStartDate(casePerson.getStartDate());
        caseCaseView.setEndDate(casePerson.getEndDate());
        results.put(key, caseCaseView);
      }
    }

    private boolean isWithinRange(CasePersonView casePerson)
    {
      String personId = casePerson.getPersonView().getPersonId();
      List<Period> list = map.get(personId);
      for (Period period : list)
      {
        if (period.isWithinRange(casePerson))
        {
          return true;
        }
      }
      return false;
    }

    private void putToMap(CasePersonView casePerson)
    {
      String personId = casePerson.getPersonView().getPersonId();
      Period period = new Period(casePerson);
      List<Period> list = map.get(personId);
      if (list == null)
      {
        list = new ArrayList<>();
      }
      list.add(period);
      map.put(personId, list);
    }
  }

  private class Period
  {

    private Date startDate;
    private Date endDate;

    public Period(String startDate, String endDate)
    {
      if (startDate != null)
      {
        this.startDate = TextUtils.parseInternalDate(startDate);
      }
      if (endDate != null)
      {
        this.endDate = TextUtils.parseInternalDate(endDate);
      }
    }

    public Period(CasePersonView casePerson)
    {
      this(casePerson.getStartDate(), casePerson.getEndDate());
    }

    public Date getStartDate()
    {
      return startDate;
    }

    public void setStartDate(Date startDate)
    {
      this.startDate = startDate;
    }

    public Date getEndDate()
    {
      return endDate;
    }

    public void setEndDate(Date endDate)
    {
      this.endDate = endDate;
    }

    public boolean isWithinRange(CasePersonView casePerson)
    {
      Date cpStart = TextUtils.parseInternalDate(casePerson.getStartDate());
      Date cpEnd = TextUtils.parseInternalDate(casePerson.getEndDate());
      return (startDate == null || cpEnd == null || startDate.getTime() <= cpEnd.getTime())
        && (endDate == null || cpStart == null || endDate.getTime() >= cpStart.getTime());
    }
  }
}
