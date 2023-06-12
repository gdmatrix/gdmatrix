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
import org.primefaces.PrimeFaces;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.util.TextUtils;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.modules.kernel.PersonTypeBean;
import org.santfeliu.webapp.setup.Column;
import org.santfeliu.webapp.util.DataTableRow;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author blanquepa
 */
@Named
@ViewScoped
public class CaseCasesTabBean extends TabBean
{
  private static final String TYPEID_PROPERTY = "typeId";
  
  private static final String TYPEID1_PROPERTY = "typeId1";
  private static final String TYPEID2_PROPERTY = "typeId2";
  Map<String, TabInstance> tabInstances = new HashMap<>();
  private final TabInstance EMPTY_TAB_INSTANCE = new TabInstance();

  public class TabInstance
  {
    String objectId = NEW_OBJECT_ID;
    List<CaseCasesDataTableRow> rows;
    int firstRow = 0;
    boolean groupedView = true;
    boolean relatedByPerson = false;
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
  
  public boolean isRelatedByPerson()
  {
    return getCurrentTabInstance().relatedByPerson;
  }  

  public List<Column> getColumns()
  {
    EditTab activeEditTab = caseObjectBean.getActiveEditTab();
    if (activeEditTab != null)
      return activeEditTab.getColumns();
    else
      return Collections.EMPTY_LIST;
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
    return isGroupedViewEnabled() && getCurrentTabInstance().groupedView;
  }

  public void setGroupedView(boolean groupedView)
  {
    getCurrentTabInstance().groupedView = groupedView;
  }

  public boolean isGroupedViewEnabled()
  {
    return caseObjectBean.getActiveEditTab().getProperties()
      .getBoolean("groupedViewEnabled");
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
    if (baseTypeId != null)
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
  
  public String getTabBaseTypeId()
  { 
    String typeId = null;
    EditTab editTab = caseObjectBean.getActiveEditTab();
    if (editTab != null)
      typeId = editTab.getProperties().getString(TYPEID_PROPERTY);

    return typeId;
  }  
  
  @Override
  public void load() throws Exception
  {
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        EditTab editTab = caseObjectBean.getActiveEditTab();
        if (editTab != null)
        {
          String typeId1 = 
            editTab.getProperties().getString(TYPEID1_PROPERTY);
          String typeId2 = 
            editTab.getProperties().getString(TYPEID2_PROPERTY);
            
          if (typeId1 != null || typeId2 != null)
          {
            TabInstance tabInstance = getCurrentTabInstance();
            tabInstance.rows = getResultsByPersons(typeId1, typeId2);
            tabInstance.relatedByPerson = true;
          }
          else
          {
            String typeId = editTab.getProperties().getString(TYPEID_PROPERTY);            
            getCurrentTabInstance().rows = getResultsByDefault(typeId);
          }
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
      formSelector = null;  
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
      info("STORE_OBJECT");
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

  private List<CaseCasesDataTableRow> getResultsByPersons(String cpTypeId1,
    String cpTypeId2) throws Exception
  {
    CasePersonFilter filter = new CasePersonFilter();
    filter.setCaseId(getObjectId());
    filter.setCasePersonTypeId(cpTypeId1);
    List<CasePersonView> casePersons
      = CasesModuleBean.getPort(false).findCasePersonViews(filter);

    Case mainCase = caseObjectBean.getCase();
    CaseMatcher matcher = new CaseMatcher(mainCase, casePersons);

    for (String personId : matcher.getPersonIds())
    {
      filter = new CasePersonFilter();
      filter.setPersonId(personId);
      filter.setCasePersonTypeId(cpTypeId2);
      casePersons = CasesModuleBean.getPort(false).findCasePersonViews(filter);
      matcher.compare(casePersons);
    }

    return toDataTableRows(matcher.getResults());
  }
  
  private List<CaseCasesDataTableRow> toDataTableRows(List<CaseCaseView>
    caseCaseViews) throws Exception
  {
    List<CaseCasesDataTableRow> convertedRows = new ArrayList<>();
    for (CaseCaseView row : caseCaseViews)
    {
      CaseCasesDataTableRow dataTableRow = new CaseCasesDataTableRow(row);
      dataTableRow.setValues(this, row, getColumns());
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

  public class CaseCasesDataTableRow extends DataTableRow
  {
    private boolean reverseRelation;
    private String caseId; 
    private String caseTypeId;    
    private String title;
    private String personId; //If is related by person

    public CaseCasesDataTableRow(CaseCaseView row)
    {
      super(row.getCaseCaseId(), row.getCaseCaseTypeId());
      
      Case mainCase = row.getMainCase();
      Case relCase = row.getRelCase();

      String objectId = getObjectId();
      reverseRelation = objectId.equals(relCase.getCaseId()) 
        && !objectId.equals(mainCase.getCaseId());
      
      caseId = reverseRelation ? mainCase.getCaseId() : relCase.getCaseId();
      caseTypeId = reverseRelation ? mainCase.getCaseTypeId() : 
        relCase.getCaseTypeId();
      title = reverseRelation ? mainCase.getTitle() : relCase.getTitle();
      personId = 
        DictionaryUtils.getPropertyValue(row.getProperty(), "personId");
    }

    public boolean isReverseRelation()
    {
      return reverseRelation;
    }

    public String getCaseTypeId()
    {
      return caseTypeId;
    }

    public void setCaseTypeId(String caseTypeId)
    {
      this.caseTypeId = caseTypeId;
    }

    public String getTitle()
    {
      return title;
    }

    public void setTitle(String title)
    {
      this.title = title;
    }

    public String getCaseId()
    {
      return caseId;
    }

    public void setCaseId(String caseId)
    {
      this.caseId = caseId;
    }

    public String getPersonId()
    {
      return personId;
    }

    public String getPerson()
    {
      PersonTypeBean personTypeBean = WebUtils.getBean("personTypeBean");
      return personTypeBean.getDescription(personId);
    }
    
    @Override
    protected Object getDefaultValue(String columnName)
    {
      if (columnName != null)
      {
        switch (columnName)
        {
          case "caseId":
            return getCaseId();
          case "caseTitle":      
            return getTitle();
          case "caseTypeId":
            return getCaseTypeId();
          case "person":
            return getPerson();
          default:
            break;
        }    
      }
      return super.getDefaultValue(columnName);
    }
  }

  private class CaseMatcher
  {
    private Case mainCase;
    private final Map<String, List<Period>> personsPeriods = new HashMap<>();
    private TreeMap<String, CaseCaseView> results = new TreeMap<>();

    public CaseMatcher(Case mainCase, List<CasePersonView> casePersons)
    {
      this.mainCase = mainCase;
      for (CasePersonView casePerson : casePersons)
      {
        storePeriods(casePerson);
      }
    }

    public Set<String> getPersonIds()
    {
      return personsPeriods.keySet();
    }

    public List<CaseCaseView> getResults()
    {
      List<CaseCaseView> list = new ArrayList<>();
      list.addAll(results.values());
      return list;
    }
    
    public void compare(List<CasePersonView> casePersons)
    {
      for (CasePersonView casePerson : casePersons)
      {
        addMatch(casePerson);
      }
    }    

    private void addMatch(CasePersonView casePerson)
    {
      String personId = casePerson.getPersonView().getPersonId();
      String casePersonId = casePerson.getCasePersonId();
      CaseCaseView item = results.get(casePersonId);
      Period p = isWithinRange(casePerson);
      if (item == null && p != null)
      {
        CaseCaseView caseCaseView = new CaseCaseView();
        Case relCase = casePerson.getCaseObject();
        caseCaseView.setCaseCaseTypeId(relCase.getCaseTypeId());
        caseCaseView.setMainCase(mainCase);
        caseCaseView.setRelCase(relCase);
        caseCaseView.setStartDate(p.getFormattedStartDate());
        caseCaseView.setEndDate(p.getFormattedEndDate());
        DictionaryUtils.setProperty(caseCaseView, "personId", personId);
        results.put(casePersonId, caseCaseView);
      }
    }

    private Period isWithinRange(CasePersonView casePerson)
    {
      String personId = casePerson.getPersonView().getPersonId();
      List<Period> list = personsPeriods.get(personId);
      if (list != null && !list.isEmpty())
      {
        for (Period period : list)
        {
          Period p = period.isWithinRange(casePerson);
          if (p != null)
          {
            return p;
          }
        }
      }
      return null;
    }

    private void storePeriods(CasePersonView casePerson)
    {
      String personId = casePerson.getPersonView().getPersonId();
      Period period = new Period(casePerson);
      List<Period> list = personsPeriods.get(personId);
      if (list == null)
      {
        list = new ArrayList<>();
      }
      list.add(period);
      personsPeriods.put(personId, list);
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
      
      public String getFormattedStartDate()
      {
        return TextUtils.formatDate(startDate, "yyyyMMdd");
      }
      
      public String getFormattedEndDate()
      {
        return TextUtils.formatDate(endDate, "yyyyMMdd");
      }      

      public Period isWithinRange(CasePersonView casePerson)
      {
        Date cpStart = TextUtils.parseInternalDate(casePerson.getStartDate());
        Date cpEnd = TextUtils.parseInternalDate(casePerson.getEndDate());
        if ((startDate == null || cpEnd == null || startDate.getTime() <= cpEnd.getTime())
          && (endDate == null || cpStart == null || endDate.getTime() >= cpStart.getTime()))
        {
          return getMergedPeriod(casePerson.getStartDate(), casePerson.getEndDate());
        }
        else return null;
      }
      
      private Period getMergedPeriod(String startDate, String endDate)
      {        
        Period result = new Period(startDate, endDate);
        
        if (this.startDate != null)
        {
          if (result.getStartDate() == null ||
            result.getStartDate().getTime() <= this.startDate.getTime())
          {
            result.setStartDate(this.startDate);
          }
        }
        
        if (this.endDate != null)
        {
          if (result.getEndDate() == null || 
            result.getEndDate().getTime() > this.endDate.getTime())
          {
            result.setEndDate(this.endDate);
          }
        }
        
        return result;        
      }
    }    
  }
}