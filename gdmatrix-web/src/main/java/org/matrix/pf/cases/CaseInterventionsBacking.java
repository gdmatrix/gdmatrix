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
import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;
import org.matrix.cases.Intervention;
import org.matrix.cases.InterventionFilter;
import org.matrix.cases.InterventionView;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Property;
import org.matrix.pf.script.ScriptFormHelper;
import org.matrix.pf.script.ScriptBacking;
import org.matrix.pf.web.ControllerBacking;
import org.matrix.pf.web.PageBacking;
import org.matrix.pf.web.helper.ResultListHelper;
import org.matrix.pf.web.helper.ResultListPage;
import org.matrix.pf.web.helper.TabHelper;
import org.matrix.pf.web.helper.TypedHelper;
import org.matrix.pf.web.helper.TypedTabPage;
import org.matrix.web.Describable;
import org.matrix.web.WebUtils;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.Visibility;
import org.santfeliu.cases.web.CaseConfigBean;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.bean.CMSProperty;
import org.matrix.pf.script.ScriptFormPage;

/**
 *
 * @author blanquepa
 */
@Named
public class CaseInterventionsBacking extends PageBacking
  implements TypedTabPage, ResultListPage, Describable, ScriptFormPage
{
  private static final String CASE_BACKING = "caseBacking";
  private static final String OUTCOME = "pf_case_interventions";
  
  private static final String BLANK_PAGE = "/pf/common/obj/blank.xhtml";


  @CMSProperty
  public static final String SCRIPT_NAME = "rowScriptName";

  private CaseBacking caseBacking;

  //Helpers
  private TypedHelper typedHelper;
  private ResultListHelper<InterventionView> resultListHelper;
  private TabHelper tabHelper;
  private ScriptFormHelper scriptFormHelper;

  private Intervention editing;
  private SelectItem interventionSelectItem;
  
  public CaseInterventionsBacking()
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
    scriptFormHelper = new ScriptFormHelper(this);       
  }

  public Intervention getEditing()
  {
    return editing;
  }

  public void setEditing(Intervention editing)
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
      return editing.getIntId();
    }
    else
    {
      return null;
    }
  }

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.INTERVENTION_TYPE;
  }

  @Override
  public CaseBacking getObjectBacking()
  {
    return caseBacking;
  }

  @Override
  public String getTypeId()
  {
    return caseBacking.getPageTypeId();
  }

  @Override
  public ResultListHelper<InterventionView> getResultListHelper()
  {
    return resultListHelper;
  }

  @Override
  public TypedHelper getTypedHelper()
  {
    return typedHelper;
  }

  @Override
  public TabHelper getTabHelper()
  {
    return tabHelper;
  }

  public List<InterventionView> getRows()
  {
    return resultListHelper.getRows();
  }
  
  public String getRowStartDate()
  {
    String date = "";
    InterventionView row = WebUtils.evaluateExpression("#{row}");
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
    InterventionView row = WebUtils.evaluateExpression("#{row}");
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
  
  public String show(InterventionView row)
  {
    String typeId = row.getIntTypeId();
    String caseId = row.getCaseId();
    
    return ControllerBacking.getCurrentInstance().show(typeId, caseId);
  }  

  @Override
  public String show(String pageObjectId)
  {
    editIntervention(pageObjectId);
    showDialog();
    return isEditing(pageObjectId) ? OUTCOME : show();
  }

  @Override
  public String show()
  {
    populate();
    return OUTCOME;
  }

  public String editIntervention(InterventionView row)
  {
    String intId = null;
    if (row != null)
      intId = row.getIntId();

    return editIntervention(intId);
  } 
  
  public String createIntervention()
  {
    editing = new Intervention();
    return null;
  }  
  
  public String removeIntervention(InterventionView row)
  {
    try
    {
      if (row == null)
        throw new Exception("INTERVENTION_MUST_BE_SELECTED");
      
      String rowIntId = row.getIntId();
      
      if (editing != null && rowIntId.equals(editing.getIntId()))
        editing = null;
      
      CaseManagerPort port = CaseConfigBean.getPort();
      port.removeIntervention(rowIntId);
      
      return show();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }  

  public String storeIntervention()
  {
    try
    {
      if (editing == null)
        return null;
                                  
      String caseId = caseBacking.getObjectId();
      editing.setCaseId(caseId);
      
      scriptFormHelper.mergeProperties();
  
      CaseManagerPort port = CaseConfigBean.getPort();
      port.storeIntervention(editing);
       
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
  public List<InterventionView> getResults(int firstResult, int maxResults)
  {
    try
    {
      String typeId = getTypeId();
      String[] params = typeId.split(TypedHelper.TYPEID_SEPARATOR);
      if (params != null && params.length == 2)
      {
        String cpTypeId = params[0];
        String intTypeId = params[1];
        return getResultsByPersons(cpTypeId, intTypeId, maxResults);
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
    return storeIntervention();
  }

  @Override
  public void load()
  {
    resultListHelper.search();
  }

  @Override
  public void create()
  {
    editing = new Intervention();
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
      InterventionView row = (InterventionView) event.getData();
      if (row != null)
      {
        try
        {
          Intervention intervention = 
            CaseConfigBean.getPort().loadIntervention(row.getIntId());
          row.getProperty().clear();
          row.getProperty().addAll(intervention.getProperty());
        }
        catch (Exception ex)
        {
          error(ex);
        }
      }
    }
  }
  
  //Case selection
  public void setSelectedIntervention(String intId)
  {
    editing.setIntId(intId);
    if (interventionSelectItem == null || 
      !intId.equals(interventionSelectItem.getValue()))
    {    
      String description = caseBacking.getDescription(intId);
      interventionSelectItem = new SelectItem(intId, description);       
    }    
    showDialog();
  }  
  
  public SelectItem getInterventionSelectItem()
  {
    return interventionSelectItem;
  }
  
  public void setInterventionSelectItem(SelectItem item)
  {
    interventionSelectItem = item;
    editing.setIntId((String) item.getValue());
  }
  
  public void onInterventionSelect(SelectEvent<SelectItem> event) 
  {
    SelectItem item = event.getObject();
    String interventionId = (String) item.getValue();
    editing.setCaseId(interventionId);
  }  
  
  //Related via CasePerson or others
  public boolean isIndirectRelation()
  {
    String typeId = getTypeId();
    return typeId != null && typeId.contains(TypedHelper.TYPEID_SEPARATOR);
  }
           
  private boolean isNew(Intervention intervention)
  {
    return (intervention != null && intervention.getIntId() == null);
  }

  private String editIntervention(String intId)
  {
    try
    {
      if (intId != null && !isEditing(intId))
      {
        editing = CaseConfigBean.getPort().loadIntervention(intId);   
        loadInterventionSelectItem();
      }
      else if (intId == null)
      {
        editing = new Intervention();
      }
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
  } 
  
  private void loadInterventionSelectItem()
  {
    if (editing != null)
    {      
      if (editing.getIntId() != null)
      {
        String description = getDescription(this, editing.getIntId());
        interventionSelectItem = 
          new SelectItem(editing.getIntId(), description);
      }
    }
  }    

  private List<InterventionView> getResultsByDefault(int firstResult, 
    int maxResults) throws Exception
  {
    InterventionFilter filter = new InterventionFilter();
    filter.setCaseId(caseBacking.getObjectId());
    filter.setIntTypeId(getTypeId());
    filter.setFirstResult(firstResult);
    filter.setMaxResults(maxResults);
    List<InterventionView> results = 
      CaseConfigBean.getPort().findInterventionViews(filter);
      
    return results;
  }

  private List<InterventionView> getResultsByPersons(String cpTypeId, 
          String intTypeId, int maxResults) throws Exception
  {
    CasePersonFilter filter = new CasePersonFilter();
    filter.setCaseId(caseBacking.getObjectId());
    filter.setCasePersonTypeId(cpTypeId);
    filter.setMaxResults(maxResults);
    List<CasePersonView> casePersons
      = CaseConfigBean.getPort().findCasePersonViews(filter);
    
    CaseMainBacking caseMainBacking = WebUtils.getBacking("caseMainBacking");
    Case mainCase = caseMainBacking.getCase();    
    CaseMatcher matcher = new CaseMatcher(mainCase, casePersons);
    
    for (String personId : matcher.getPersonIds())
    {
      InterventionFilter filter2 = new InterventionFilter();
      filter2.setPersonId(personId);
      filter2.setIntTypeId(intTypeId);
      filter2.setMaxResults(maxResults);
      List<InterventionView> interventionList = 
        CaseConfigBean.getPort().findInterventionViews(filter2);
      matcher.addIfMatch(interventionList);
    }

    return matcher.getResults();
  }

  @Override
  public String getObjectTypeId()
  {
    return editing != null ? editing.getIntTypeId() : getTypeId();
  }

  @Override
  public String getObjectId()
  {
    return editing != null ? editing.getIntId() : null;
  }

  @Override 
  public String getDescription()
  {
    if (editing != null)
    {
      Type type = TypeCache.getInstance().getType(editing.getIntTypeId());
      return type.getDescription();
    }
    else
      return "";
  }

  @Override
  public ScriptFormHelper getScriptFormHelper()
  {
    return scriptFormHelper;
  }

  @Override
  public List<Property> getProperties()
  {
    return editing.getProperty();
  }
        
  private class CaseMatcher
  {
    private Case mainCase;
    private final Map<String, List<Period>> map = new HashMap<>();
    
    private TreeMap<String, InterventionView> results = new TreeMap<>();
    
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
    
    public void addIfMatch(List<InterventionView> interventions)
    {
      for (InterventionView intervention : interventions)
      {
        addIfMatch(intervention);
      }      
    }
    
    public List<InterventionView> getResults()
    {
      List<InterventionView> list = new ArrayList<>();
      list.addAll(results.values());
      return list;
    }    
    
    private void addIfMatch(InterventionView intervention)
    {
      String key = intervention.getPersonView().getPersonId() + ";" +
        intervention.getStartDate() + ";" + intervention.getEndDate();
      InterventionView item = results.get(key);
      if (item == null && isWithinRange(intervention))
      {
        InterventionView interventionView = new InterventionView();
        interventionView.setIntTypeId(intervention.getIntTypeId());
        interventionView.setCaseId(mainCase.getCaseId());
        interventionView.setIntId(intervention.getIntId());
        interventionView.setPersonView(intervention.getPersonView());
        //TODO: Merge fechas
        interventionView.setStartDate(intervention.getStartDate());
        interventionView.setEndDate(intervention.getEndDate());
        results.put(key, interventionView);
      }
    }
    
    private boolean isWithinRange(InterventionView intervention)
    {
      String personId = intervention.getPersonView().getPersonId();
      List<Period> list = map.get(personId);
      for (Period period : list)
      {
        if (period.isWithinRange(intervention))
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

    public boolean isWithinRange(InterventionView intervention)
    {
      Date cpStart = TextUtils.parseInternalDate(intervention.getStartDate());
      Date cpEnd = TextUtils.parseInternalDate(intervention.getEndDate());
      return (startDate == null || cpEnd == null || startDate.getTime() <= cpEnd.getTime()) 
        && (endDate == null || cpStart == null || endDate.getTime() >= cpStart.getTime());
    }    
  }
  
  
    


}
