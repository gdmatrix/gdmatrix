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
import org.primefaces.PrimeFaces;
import org.santfeliu.util.TextUtils;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;

/**
 *
 * @author blanquepa
 */
@Named
@ViewScoped
public class CaseCasesTabBean extends TabBean
{
  private static final String TYPEID_SEPARATOR = ";";
  
  private List<CaseCaseView> rows;
  private CaseCase editing;
  private int firstRow;
  private boolean groupedView = true;

  @Inject
  CaseObjectBean caseObjectBean;

  public List<CaseCaseView> getRows()
  {
    return rows;
  }

  public void setRows(List<CaseCaseView> rows)
  {
    this.rows = rows;
  }

  public CaseCase getEditing()
  {
    return editing;
  }

  public void setEditing(CaseCase editing)
  {
    this.editing = editing;
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }

  public boolean isGroupedView()
  {
    return groupedView;
  }

  public void setGroupedView(boolean groupedView)
  {
    this.groupedView = groupedView;
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
    editing.setCaseId(objectId);
    String baseTypeId = getTabBaseTypeId();
    if (baseTypeId != null && !baseTypeId.contains(TYPEID_SEPARATOR))
      editing.setCaseCaseTypeId(baseTypeId);
  }
  
  public void switchView()
  {
    groupedView = !groupedView;
  }
  
  //TODO: get property from JSON  
  private String getTabBaseTypeId()
  {
    String typeId;
        
    String tabPrefix = String.valueOf(caseObjectBean.getDetailSelector());
    typeId = getProperty("tabs::" + tabPrefix + "::typeId");
    if (typeId == null)
      typeId = DictionaryConstants.CASE_CASE_TYPE; 
    
    return typeId;
  }
  

  @Override
  public void load() throws Exception
  {
    if (!NEW_OBJECT_ID.equals(objectId))
    {
      try
      {
        String typeId = getTabBaseTypeId();
        
        String[] params = typeId.split(TYPEID_SEPARATOR);
        if (params != null && params.length == 2)
        {
          String cpTypeId1 = params[0];
          String cpTypeId2 = params[1];
          rows = getResultsByPersons(cpTypeId1, cpTypeId2);
        }
        else
        {
          rows = getResultsByDefault(typeId);
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else
    {
      rows = Collections.emptyList();
    }
  }
  
  public boolean isReverseRelation(CaseCaseView caseCase)
  {
    return caseCase != null 
      && objectId.equals(caseCase.getRelCase().getCaseId())
      && !objectId.equals(caseCase.getMainCase().getCaseId());
  }     

  public void edit(CaseCaseView caseCaseView)
  {
    String caseCaseId = null;
    if (caseCaseView != null)
    {
      caseCaseId = caseCaseView.getCaseCaseId();
    }

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
    if (editing != null)
    {
      if (editing.getRelCaseId() == null || editing.getRelCaseId().isEmpty())
        throw new Exception("CASE_MUST_BE_SELECTED"); 
      
      if (!editing.getCaseId().equals(objectId))
        throw new Exception("CAN_NOT_STORE_REVERSE_RELATION");
      
      CasesModuleBean.getPort(false).storeCaseCase(editing);
      load();
      editing = null;
    }
  }
  
  public void cancel()
  {
    info("CANCEL_OBJECT");
    editing = null;
  }
  
  public void remove(CaseCaseView row)
  {
    try
    {
      if (row != null)
      {
        String caseCaseId = row.getCaseCaseId();
        CasesModuleBean.getPort(false).removeCaseCase(caseCaseId);
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
    return new Object[]{ editing, groupedView };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      editing = (CaseCase)stateArray[0];
      groupedView = (boolean)stateArray[1];

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
      current.executeScript("PF('caseCasesDialog" + 
        caseObjectBean.getDetailSelector() + "').show();");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }  

  private List<CaseCaseView> getResultsByDefault(String caseCaseTypeId)
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

    return results;
  }

  private List<CaseCaseView> getResultsByPersons(String typeId1, String typeId2)
    throws Exception
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

    return matcher.getResults();
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
