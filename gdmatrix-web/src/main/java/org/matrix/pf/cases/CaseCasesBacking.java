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
package org.matrix.pf.cases;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.matrix.cases.Case;
import org.matrix.cases.CaseCase;
import org.matrix.cases.CaseCaseFilter;
import org.matrix.cases.CaseCaseView;
import org.matrix.cases.CaseFilter;
import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;
import org.matrix.dic.DictionaryConstants;
import org.matrix.pf.script.ScriptBacking;
import org.matrix.pf.web.ControllerBacking;
import org.matrix.pf.web.PageBacking;
import org.matrix.pf.web.helper.ResultListHelper;
import org.matrix.pf.web.helper.ResultListPage;
import org.matrix.pf.web.helper.TabHelper;
import org.matrix.pf.web.helper.TypedHelper;
import org.matrix.pf.web.helper.TypedTabPage;
import org.matrix.web.WebUtils;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.Visibility;
import org.santfeliu.cases.web.CaseConfigBean;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.bean.CMSProperty;

/**
 *
 * @author blanquepa
 */
@Named
public class CaseCasesBacking extends PageBacking
  implements TypedTabPage, ResultListPage
{
  private static final String CASE_BACKING = "caseBacking";
  private static final String OUTCOME = "pf_case_cases";
  
  private static final String BLANK_PAGE = "/pf/common/obj/blank.xhtml";


  @CMSProperty
  public static final String SCRIPT_NAME = "scriptName";

  private CaseBacking caseBacking;

  //Helpers
  private TypedHelper typedHelper;
  private ResultListHelper<CaseCaseView> resultListHelper;
  private TabHelper tabHelper;

  private CaseCase editing;
  private SelectItem caseSelectItem;

  public CaseCasesBacking()
  {
  }

  @PostConstruct
  public void init()
  {
    caseBacking = WebUtils.getBacking(CASE_BACKING);
    typedHelper = new TypedHelper(this);
    resultListHelper = new ResultListHelper(this);
    tabHelper = new TabHelper(this);
    populate();
  }

  public CaseCase getEditing()
  {
    return editing;
  }

  public void setEditing(CaseCase editing)
  {
    this.editing = editing;
  }
  
  public Date getStartDate()
  {
    if (editing != null && editing.getStartDate() != null)
      return TextUtils.parseInternalDate(editing.getStartDate());
    else
      return null;
  }
  
  public Date getEndDate()
  {
    if (editing != null && editing.getEndDate() != null)
      return TextUtils.parseInternalDate(editing.getEndDate());
    else
      return null;
  }  
    
  public void setStartDate(Date date)
  {
    if (date != null && editing != null)
      editing.setStartDate(TextUtils.formatDate(date, "yyyyMMdd"));
  }
  
  public void setEndDate(Date date)
  {
    if (date != null && editing != null)
      editing.setEndDate(TextUtils.formatDate(date, "yyyyMMdd"));
  }    

  public boolean isNew()
  {
    return isNew(editing);
  }

  @Override
  public String getPageObjectId()
  {
    if (editing != null)
    {
      return editing.getCaseCaseId();
    }
    else
    {
      return null;
    }
  }

  public String getPageObjectDescription()
  {
    if (editing != null)
    {
      return getDescription(caseBacking, editing.getCaseId());
    }
    return null;
  }

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.CASE_CASE_TYPE;
  }

  @Override
  public CaseBacking getObjectBacking()
  {
    return caseBacking;
  }

  @Override
  public String getTypeId()
  {
    return caseBacking.getTabTypeId();
  }

  @Override
  public ResultListHelper<CaseCaseView> getResultListHelper()
  {
    return resultListHelper;
  }

  @Override
  public TypedHelper getTypedHelper()
  {
    return typedHelper;
  }

  public TabHelper getTabHelper()
  {
    return tabHelper;
  }

  public List<CaseCaseView> getRows()
  {
    return resultListHelper.getRows();
  }
  
  public String getRowStartDate()
  {
    String date = "";
    CaseCaseView row = WebUtils.evaluateExpression("#{row}");
    if (row != null)
    {
      date = row.getStartDate();
      date = TextUtils.formatDate(
        TextUtils.parseInternalDate(date), "dd/MM/yyyy");
    }
    return date;
  }

  public String getRowEndDate()
  {
    String date = "";
    CaseCaseView row = WebUtils.evaluateExpression("#{row}");
    if (row != null)
    {
      date = row.getEndDate();
      date = TextUtils.formatDate(
        TextUtils.parseInternalDate(date), "dd/MM/yyyy");
    }
    return date;
  }    
  
  public boolean isRenderXhtmlForm()
  {
    return getProperty(SCRIPT_NAME) != null;
  }
  
  public String getXhtmlFormUrl()
  {
    String scriptName = getProperty(SCRIPT_NAME);   
    if (scriptName != null)
    {
      ScriptBacking scriptBacking = WebUtils.getBacking("scriptBacking");
      return scriptBacking.getXhtmlFormUrl(scriptName);
    }
    else
      return BLANK_PAGE;
  }
  
  public String show(CaseCaseView row)
  {
    String typeId;
    String caseId;
    
    if (isReverseRelation(row))
    {
      typeId = row.getMainCase().getCaseTypeId();
      caseId = row.getMainCase().getCaseId();
    }
    else
    {
      typeId = row.getRelCase().getCaseTypeId();
      caseId = row.getRelCase().getCaseId(); 
    }
 
    return ControllerBacking.getCurrentInstance().show(typeId, caseId);
  }  

  @Override
  public String show(String pageObjectId)
  {
    editCase(pageObjectId);
    showDialog();
    return isEditing(pageObjectId) ? OUTCOME : show();
  }

  @Override
  public String show()
  {
    populate();
    return OUTCOME;
  }

  public String editCase(CaseCaseView row)
  {
    String caseCaseId = null;
    if (row != null)
      caseCaseId = row.getCaseCaseId();

    return editCase(caseCaseId);
  } 
  
  public String createCase()
  {
    editing = new CaseCase();
    return null;
  }  
  
  public String removeCase(CaseCaseView row)
  {
    try
    {
      if (row == null)
        throw new Exception("CASE_MUST_BE_SELECTED");
      
      String rowCaseCaseId = row.getCaseCaseId();
      
      if (editing != null && rowCaseCaseId.equals(editing.getCaseCaseId()))
        editing = null;
      
      CaseManagerPort port = CaseConfigBean.getPort();
      port.removeCaseCase(rowCaseCaseId);
      
      return show();
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
      if (editing == null)
        return null;
      
      //Case must be selected
      if (editing.getCaseId() == null || editing.getCaseId().isEmpty())
        throw new Exception("CASE_MUST_BE_SELECTED"); 
                            
      String caseId = caseBacking.getObjectId();
      editing.setCaseId(caseId);
      
      if (editing.getCaseCaseTypeId() == null)
        editing.setCaseCaseTypeId(typedHelper.getTypeId());
                  
      CaseManagerPort port = CaseConfigBean.getPort();
      port.storeCaseCase(editing);
       
      cancel();
      return show();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
    

  @Override
  public List<CaseCaseView> getResults(int firstResult, int maxResults)
  {
    try
    {
      String typeId = getTypeId();
      String[] params = typeId.split(TypedHelper.TYPEID_SEPARATOR);
      if (params != null && params.length == 2)
      {
        String cpTypeId1 = params[0];
        String cpTypeId2 = params[1];
        return getResultsByPersons(cpTypeId1, cpTypeId2, maxResults);
      }
      else
      {
        return getResultsByDefault(firstResult, maxResults);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  @Override
  public String store()
  {
    return storeCase();
  }

  @Override
  public void load()
  {
    resultListHelper.search();
  }

  @Override
  public void create()
  {
    editing = new CaseCase();
  }

  @Override
  public String cancel()
  {
    editing = null;
    return null;
  }

  @Override
  public void reset()
  {
    cancel();
    resultListHelper.reset();
  }
  
  public void onRowToggle(ToggleEvent event)
  {
    Visibility visibility = event.getVisibility();
    if (Visibility.VISIBLE.equals(visibility))
    {
      CaseCaseView row = (CaseCaseView) event.getData();
      if (row != null)
      {
        try
        {
          Case relCase = 
            CaseConfigBean.getPort().loadCase(row.getRelCase().getCaseId());
          row.setRelCase(relCase);
        }
        catch (Exception ex)
        {
          error(ex);
        }
      }
    }
  }
  
  //Case selection
  public void setSelectedCase(String caseId)
  {
    editing.setRelCaseId(caseId);
    if (caseSelectItem == null || 
      !caseId.equals(caseSelectItem.getValue()))
    {    
      String description = caseBacking.getDescription(caseId);
      caseSelectItem = new SelectItem(caseId, description);       
    }    
    showDialog();
  }  
  
  public SelectItem getCaseSelectItem()
  {
    return caseSelectItem;
  }
  
  public void setCaseSelectItem(SelectItem item)
  {
    caseSelectItem = item;
    editing.setRelCaseId((String) item.getValue());
  }
  
  public void onCaseSelect(SelectEvent<SelectItem> event) 
  {
    SelectItem item = event.getObject();
    String caseId = (String) item.getValue();
    editing.setCaseId(caseId);
  }  
  
  public List<SelectItem> completeCase(String query)
  {
    List<SelectItem> results = new ArrayList<>();
    try
    {
      results = completeCase(query, editing.getCaseId());
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return results;
  } 

  //Related via CasePerson or others
  public boolean isIndirectRelation()
  {
    String typeId = getTypeId();
    return typeId != null && typeId.contains(TypedHelper.TYPEID_SEPARATOR);
  }
  
  //Relataion created with revCase as main case.
  public boolean isReverseRelation(CaseCaseView caseCase)
  {
    return caseCase != null 
      && caseBacking.getObjectId().equals(caseCase.getRelCase().getCaseId())
      && !caseBacking.getObjectId().equals(caseCase.getMainCase().getCaseId());
  } 
  

  
  private List<SelectItem> completeCase(String query, String caseId) 
    throws Exception
  {
    ArrayList<SelectItem> items = new ArrayList<>();
    
    //Add current item
    if (!isNew(editing))
    {
      String description = "";
      if (caseId != null)
        description = caseBacking.getDescription(caseId);
      items.add(new SelectItem(caseId, description));
    }
        
    //Query search
    if (query != null && query.length() >= 3)
    {
      CaseFilter filter = new CaseFilter();
      filter.setTitle("%" + query + "%");
      filter.setMaxResults(10);
      List<Case> cases = 
        CaseConfigBean.getPort().findCases(filter);
      if (cases != null)
      {       
        for (Case cas : cases)
        {
          String description = caseBacking.getDescription(cas);
          SelectItem item = new SelectItem(cas.getCaseId(), description);
          items.add(item);
        }
      }
    }
    else
    {
      //Add favorites
      items.addAll(caseBacking.getFavorites()); 
    }
    
    return items;
  }  
      
  private boolean isNew(CaseCase caseCase)
  {
    return (caseCase != null && caseCase.getCaseCaseId() == null);
  }

  private String editCase(String caseCaseId)
  {
    try
    {
      if (caseCaseId != null && !isEditing(caseCaseId))
      {
        editing = CaseConfigBean.getPort().loadCaseCase(caseCaseId);   
        loadCaseSelectItem();
      }
      else if (caseCaseId == null)
      {
        editing = new CaseCase();
      }
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
  } 
  
  private void loadCaseSelectItem()
  {
    if (editing != null)
    {      
      if (editing.getRelCaseId() != null)
      {
        String description = 
          caseBacking.getDescription(editing.getRelCaseId());
        caseSelectItem = 
          new SelectItem(editing.getRelCaseId(), description);
      }
    }
  }    

  private List<CaseCaseView> getResultsByDefault(int firstResult, int maxResults)
    throws Exception
  {
    CaseCaseFilter filter = new CaseCaseFilter();
    filter.setCaseId(caseBacking.getObjectId());
    filter.setCaseCaseTypeId(getTypeId());
    filter.setFirstResult(firstResult);
    filter.setMaxResults(maxResults);
    List<CaseCaseView> results = 
      CaseConfigBean.getPort().findCaseCaseViews(filter);
      
    //Reverse cases
    if (maxResults == 0 || results.size() < maxResults)
    {
      filter = new CaseCaseFilter();
      filter.setRelCaseId(caseBacking.getObjectId());
      filter.setCaseCaseTypeId(getTypeId());        
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);        
      List<CaseCaseView> revResults = 
        CaseConfigBean.getPort().findCaseCaseViews(filter);
    
      if (revResults != null && !revResults.isEmpty())
      {
        if (maxResults == 0)
          maxResults = results.size() + revResults.size();
        int revCount = revResults.size();
        
        for (int i = 0; i < revCount && results.size() <= maxResults; i++)
        {
          results.add(revResults.get(i));
        }
      }
    }

    return results;
  }

  private List<CaseCaseView> getResultsByPersons(String typeId1, String typeId2,
    int maxResults) throws Exception
  {
    CasePersonFilter filter = new CasePersonFilter();
    filter.setCaseId(caseBacking.getObjectId());
    filter.setCasePersonTypeId(typeId1);
    filter.setMaxResults(maxResults);
    List<CasePersonView> casePersons
      = CaseConfigBean.getPort().findCasePersonViews(filter);
    
    CaseMainBacking caseMainBacking = WebUtils.getBacking("caseMainBacking");
    Case mainCase = caseMainBacking.getCase();    
    CaseMatcher matcher = new CaseMatcher(mainCase, casePersons);
    
    for (String personId : matcher.getPersonIds())
    {
      filter = new CasePersonFilter();
      filter.setPersonId(personId);
      filter.setCasePersonTypeId(typeId2);
      filter.setMaxResults(maxResults);
      casePersons = CaseConfigBean.getPort().findCasePersonViews(filter);
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
      String key = casePerson.getPersonView().getPersonId() + ";" +
        casePerson.getStartDate() + ";" + casePerson.getEndDate();
      CaseCaseView item = results.get(key);
      if (item == null && isWithinRange(casePerson))
      {
        CaseCaseView caseCaseView = new CaseCaseView();
        caseCaseView.setCaseCaseTypeId(getConfigTypeId());
        caseCaseView.setMainCase(mainCase);
        caseCaseView.setRelCase(casePerson.getCaseObject());
        //TODO: Merge fechas
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
          return true;
      }
      return false;
    }
    
    private void putToMap(CasePersonView casePerson)
    {
      String personId = casePerson.getPersonView().getPersonId();
      Period period = new Period(casePerson);
      List<Period> list = map.get(personId);
      if (list == null)
        list = new ArrayList<>();
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
        this.startDate = TextUtils.parseInternalDate(startDate);
      if (endDate != null)
        this.endDate = TextUtils.parseInternalDate(endDate);
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
