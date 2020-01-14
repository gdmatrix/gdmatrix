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
package org.santfeliu.cases.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.model.SelectItem;
import org.apache.commons.lang.StringUtils;
import org.matrix.cases.Case;
import org.matrix.cases.Intervention;
import org.matrix.cases.InterventionFilter;
import org.matrix.cases.InterventionView;
import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;
import org.matrix.cases.InterventionProblem;
import org.matrix.cases.InterventionProblemFilter;
import org.matrix.cases.InterventionProblemView;
import org.matrix.cases.ProblemFilter;
import org.matrix.cases.ProblemView;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.PropertyDefinition;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.kernel.web.PersonBean;
import org.santfeliu.util.TextUtils;
import org.santfeliu.dic.util.ObjectDumper;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.DynamicTypifiedPageBean;
import org.santfeliu.web.obj.ExternalEditable;
import org.santfeliu.ws.WSExceptionFactory;

/**
 *
 * @author unknown
 */
public class CaseInterventionsBean extends DynamicTypifiedPageBean implements ExternalEditable
{
  //Dic Case properties
  public static final String ALL_TYPES_VISIBLE_PROPERTY = "_interventionsAllTypesVisible";
  public static final String ROW_TYPE_ID_PROPERTY = "_interventionsRowTypeId";
  public static final String ROOT_TYPE_ID_PROPERTY = "_interventionRootTypeId";
  public static final String GROUPBY_PROPERTY = "_interventionsGroupBy";
  public static final String GROUP_SELECTION_MODE_PROPERTY = "_interventionsGroupSelectionMode";  
  public static final String ORDERBY_PROPERTY = "_interventionsOrderBy";
  public static final String INTERVENTION_PAGE_SIZE_PROPERTY = "_interventionPageSize";
  public static final String RENDER_PROBLEMS_PROPERTY = "_interventionsRenderProblems";
  public static final String RENDER_PERSON_PROPERTY = "_interventionsRenderPerson";
  public static final String PERSON_TYPE_FILTER = "_interventionsPersonTypeFilter";   
  public static final String SHOW_PROPERTIES_PROPERTY = "_interventionProperties";
  public static final String SHOW_INACTIVE_BUTTON_PROPERTY = "_interventionsShowInactiveButton";
  
  //Dic Intervention properties
  public static final String AUTO_START_DATE_PROPERTY = "_interventionAutoStartDate";
  public static final String RENDER_END_DATE_PROPERTY = "_renderEndDate";
  public static final String IMMEDIATE_CLOSING_PROPERTY = "_immediateClosing";
  public static final String VIEW_DATE_FORMAT = "_viewDateFormat";
  
  public static final String PROBLEM_TAB_MID = "problemTabMid";
  public static final int PAGE_SIZE = 100;
  public static final int DEFAULT_MAX_SIZE = 4000;
  
  private Intervention editingIntervention;
  private List<InterventionProblemView> solvedProblems;
  private List<InterventionView> rows;
  private String startHour;
  private String endHour;

  private String startDateTime;
  private String endDateTime;
  
  private int pageSize;
  
  private String selectedProblem;  
  private List<SelectItem> selectableProblemSelectItems;
  private boolean renderProblems;
  private boolean renderPerson;
  private boolean renderInactiveButton;

  private String personTypeFilter;
  private Map<String,List<InterventionProblemView>> problemsMap;
  
  private String lastInterventionMid = null;
  private int objectPageScroll;  

  public CaseInterventionsBean()
  {
    super(DictionaryConstants.INTERVENTION_TYPE, "CASE_ADMIN", false);
  }

  //Accessors
  public String getEndDateTime()
  {
    return endDateTime;
  }

  public void setEndDateTime(String endDateTime)
  {
    this.endDateTime = endDateTime;
  }

  public String getStartDateTime()
  {
    return startDateTime;
  }

  public void setStartDateTime(String startDateTime)
  {
    this.startDateTime = startDateTime;
  }

  public void setEditingIntervention(Intervention editingIntervention)
  {
    this.editingIntervention = editingIntervention;
  }

  public Intervention getEditingIntervention()
  {
    return editingIntervention;
  }

  public void setStartHour(String startHour)
  {
    this.startHour = startHour;
  }

  public String getStartHour()
  {
    return startHour;
  }

  public void setEndHour(String endHour)
  {
    this.endHour = endHour;
  }

  public String getEndHour()
  {
    return endHour;
  }  

  public int getObjectPageScroll()
  {    
    return objectPageScroll;
  }

  public void setObjectPageScroll(int objectPageScroll)
  {
    this.objectPageScroll = objectPageScroll;
  }
  
  public List<InterventionView> getAllRows()
  {
    return rows;
  }

  public Map<String, List<InterventionProblemView>> getProblemsMap() {
    return problemsMap;
  }

  public void setProblemsMap(Map<String, List<InterventionProblemView>> problemsMap) {
    this.problemsMap = problemsMap;
  }
  
  public List<Map<String,String>> getProblems()
  {
    List<Map<String,String>> result = 
      new ArrayList<Map<String,String>>();
    
    InterventionView iView = (InterventionView) getValue("#{row}");
    if (iView != null && problemsMap != null)
    {
      List<InterventionProblemView> views = problemsMap.get(iView.getIntId());
      if (views != null)
      {
        for (InterventionProblemView view : views)
        {
          HashMap map = new HashMap();    
          String intId = view.getProblem().getProbId();
          map.put("probId", intId);
          String typeId = view.getProblem().getProbTypeId();
          Type type = TypeCache.getInstance().getType(typeId);
          map.put("type", type.getDescription());
          map.put("comments", view.getProblem().getComments());
          result.add(map);
        }
      }
    }    
    return result;
  }  

  public boolean isInactiveHidden()
  {
    Boolean hideInactive = 
      (Boolean)UserSessionBean.getCurrentInstance().getAttribute("hideInactiveInterventions");
    if (hideInactive == null)
      return false;
    else
      return hideInactive;
  }

  public void setInactiveHidden(boolean hideInactive)
  {
    UserSessionBean.getCurrentInstance().getAttributes().put("hideInactiveInterventions", hideInactive);
  }
  
  public String switchInactive()
  {
    setInactiveHidden(!isInactiveHidden());
    lastInterventionMid = null;
    return show();
  }
  
  //Object actions
  public String show()
  {
    try
    {
      preShow();
      objectPageScroll = 0;
      String mid = UserSessionBean.getCurrentInstance().getSelectedMid();
      if (!mid.equals(lastInterventionMid))
      {
        load();
      }
      lastInterventionMid = mid;  
      postShow();
    }
    catch(Exception ex)
    {
      error(ex);
    }
    finally
    {
      return "case_interventions";
    }
  }
  
  @Override
  public String store()
  {
    if (editingIntervention != null)
    {
      storeIntervention();
    }
    else
    {
      load();
    }
    return show();
  }
  
  //Page actions
  public String createIntervention()
  {
    try
    {
      editingIntervention = new Intervention();
      getData().clear();
      startDateTime = null;
      endDateTime = null;
      startHour = null;
      endHour = null;
      if (renderProblems)
        loadIntProblems(null);
      setCurrentTypeId(null);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public String editObject(String intId)
  {
    return editIntervention(intId);
  }
  
  public String editIntervention(String intId)
  {
    try
    {
      if (intId != null)
      {
        editingIntervention = 
          CaseConfigBean.getPort().loadIntervention(intId);
        setCurrentTypeId(editingIntervention.getIntTypeId());
        setFormDataFromProperties(editingIntervention.getProperty());
        startDateTime =
          concatDateTime(editingIntervention.getStartDate(),
            editingIntervention.getStartTime());
        endDateTime =
          concatDateTime(editingIntervention.getEndDate(),
            editingIntervention.getEndTime());
        if (renderProblems)
          loadIntProblems(intId);
      }
      else
      {
        editingIntervention = new Intervention();        
        setCurrentTypeId(null);
        startDateTime = null;
        endDateTime = null;
      }
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String editIntervention()
  {
    InterventionView row = (InterventionView)getExternalContext().
      getRequestMap().get("row");   
    return editIntervention(row != null ? row.getIntId() : null);
  }  
  
  public String storeIntervention()
  {
    try
    {
      objectPageScroll = 0;      
      preStore();
      
      String caseId = getObjectId();
      editingIntervention.setCaseId(caseId);
      org.matrix.dic.Type type = getCurrentType();
      if (type != null)
        editingIntervention.setIntTypeId(type.getTypeId());
      editingIntervention.getProperty().clear();
      List properties = getFormDataAsProperties();
      if (properties != null)
        editingIntervention.getProperty().addAll(properties);
      String intTypeId = editingIntervention.getIntTypeId();
      editingIntervention.setStartDate(getStartDate(startDateTime, intTypeId));
      editingIntervention.setStartTime(getStartTime(startDateTime, intTypeId));
      editingIntervention.setEndDate(getEndDate(endDateTime, intTypeId));
      editingIntervention.setEndTime(getEndTime(endDateTime, intTypeId));
      List<SelectItem> casePersonsSelectItems = getCasePersonsSelectItems();
      if (!isRenderPerson() && isRenderCasePersons() && casePersonsSelectItems.size() == 2)
      {
        SelectItem item = casePersonsSelectItems.get(1); //First item is blank
        editingIntervention.setPersonId((String)item.getValue());
      }

      CaseManagerPort port = CaseConfigBean.getPort();
      editingIntervention = port.storeIntervention(editingIntervention);
      
      if (!StringUtils.isBlank(selectedProblem)) addProblem();
      getViewPropertiesMap().remove(editingIntervention.getIntId());      
      
      postStore();
      
      editingIntervention = null;
      load();
    }
    catch (Exception ex)
    {
      error(ex);
      List<String> details = WSExceptionFactory.getDetails(ex);
      if (details.size() > 0) error(details);
    }
    return null;
  }    
  
  public String removeIntervention()
  {
    try
    {
      InterventionView row = (InterventionView)getRequestMap().get("row");
      preRemove();
      CaseManagerPort port = CaseConfigBean.getPort();
      port.removeIntervention(row.getIntId());
      getViewPropertiesMap().remove(row.getIntId());
      postRemove();
      editingIntervention = null; 
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public String cancelIntervention()
  {
    objectPageScroll = 0;    
    editingIntervention = null;
    return null;
  }
  
  @Override
  public String showGroup()
  {    
    editingIntervention = null;
    return super.showGroup();
  }  
    
  public String showProblem()
  {
    String probTabMid = getProperty(PROBLEM_TAB_MID);
    String outcome = getControllerBean().show(probTabMid, getObjectId());
    
    Map<String,String> intProbView = 
      (Map<String,String>)getValue("#{intProbView}");
    String probId = intProbView.get("probId");
    
    CaseProblemsBean caseProblemsBean = 
      (CaseProblemsBean)getBean("caseProblemsBean");
    caseProblemsBean.editProblem(probId);
    
    return outcome;
  }   
  
  public String searchPerson()
  {
    return getControllerBean().searchObject("Person",
      "#{caseInterventionsBean.editingIntervention.personId}");
  }

  public String showPerson()
  {
    return getControllerBean().showObject("Person",
      editingIntervention.getPersonId());
  }

  public boolean isRenderShowPersonButton()
  {
    return editingIntervention.getPersonId() != null &&
      editingIntervention.getPersonId().trim().length() > 0;
  }

  public List<SelectItem> getPersonSelectItems()
  {
    if (!isRenderCasePersons())
    {
      PersonBean personBean = (PersonBean)getBean("personBean");
      return personBean.getSelectItems(editingIntervention.getPersonId());
    }
    else
      return getCasePersonsSelectItems();
  }

  public boolean isRenderProblems()
  {
    return renderProblems;
  }
  
  public boolean isRenderPerson()
  {
    return renderPerson;
  }
  
  @Override
  public int getPageSize()
  {
    if (pageSize > 0)
      return pageSize;
    return super.getPageSize();
  }

  protected void load()
  {
    try
    {
      editingIntervention = null;      
      //Set rootTypeId
      CaseMainBean caseMainBean = (CaseMainBean)getBean("caseMainBean");
      Case cas = caseMainBean.getCase();
      Type caseType = TypeCache.getInstance().getType(cas.getCaseTypeId());
      if (caseType != null)
        loadPropertyDefinitions(caseType);

      if (!isNew())
      {
        preLoad();
        InterventionFilter filter = new InterventionFilter();
        filter.setCaseId(getObjectId());
        filter.setIntTypeId(rowTypeId);
        List<InterventionView> auxRows = 
          CaseConfigBean.getPort().findInterventionViews(filter);
        if (isRenderInactiveButton() && isInactiveHidden())
        {
          if (rows == null) 
            rows = new ArrayList();
          else
            rows.clear();
          for (InterventionView row : auxRows)
          {
            if (row.getEndDate() == null || 
              row.getEndDate().compareTo(TextUtils.formatDate(new Date(), "yyyyMMdd")) > 0) 
            {
              rows.add(row);
            }
          }
        }
        else
          rows = auxRows;
        if (isRenderProblems()) 
          setProblemsMap();
        loadViewPropertiesMap(rows);
        postLoad();
        setGroups(rows, getGroupExtractor());        
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  private void loadPropertyDefinitions(Type caseType)
  {
    if (caseType != null)
    {
      rootTypeId = getIndexedDicProperty(caseType, ROOT_TYPE_ID_PROPERTY, rootTypeId);    
      
      rowTypeId = getIndexedDicProperty(caseType, ROW_TYPE_ID_PROPERTY, null);    

      String allTypesVisibleString = getIndexedDicProperty(caseType, ALL_TYPES_VISIBLE_PROPERTY, null);
      if (allTypesVisibleString != null)
      {
        allTypesVisible = Boolean.parseBoolean(allTypesVisibleString);
      }
      
      groupBy = getIndexedDicProperty(caseType, GROUPBY_PROPERTY, null);
      
      groupSelectionMode = getIndexedDicProperty(caseType, GROUP_SELECTION_MODE_PROPERTY, null);
      
      String orderByString = getIndexedDicProperty(caseType, ORDERBY_PROPERTY, null);
      if (orderByString != null)
      {
        String[] array = orderByString.split(",");
        if (array != null)
          orderBy = Arrays.asList(array);
      }      
      
      String value = getIndexedDicProperty(caseType, INTERVENTION_PAGE_SIZE_PROPERTY, "0");
      pageSize = Integer.valueOf(value);
    
      value = getIndexedDicProperty(caseType, RENDER_PROBLEMS_PROPERTY, "false");
      renderProblems = "true".equals(value);
      
      value = getIndexedDicProperty(caseType, RENDER_PERSON_PROPERTY, "true");
      renderPerson = "true".equals(value);
      
      value = getIndexedDicProperty(caseType, SHOW_INACTIVE_BUTTON_PROPERTY, "false");
      renderInactiveButton = "true".equals(value);
         
      personTypeFilter = getIndexedDicProperty(caseType, PERSON_TYPE_FILTER, null);

      PropertyDefinition pd = 
        caseType.getPropertyDefinition(SHOW_PROPERTIES_PROPERTY);
      if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
        defaultViewDumper = new ObjectDumper(pd.getValue().get(0));
      else
        defaultViewDumper = new ObjectDumper(new ArrayList());      
    }
  }

  public String getViewStartDateTime()
  {
    String dateTime = "";
    String dateFormat = "dd/MM/yyyy";
    InterventionView row = (InterventionView)getValue("#{row}");
    if (row != null)
    {
      String typeId = row.getIntTypeId();    
      if (typeId != null)
      {
        Type type = TypeCache.getInstance().getType(typeId);
        if (type != null)
        {
          PropertyDefinition pd = type.getPropertyDefinition(VIEW_DATE_FORMAT);        
          if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
            dateFormat = (String)pd.getValue().get(0);
        }
      }      
      dateTime = row.getStartDate() + (row.getStartTime() != null ? row.getStartTime() : "");
      dateTime = TextUtils.formatDate(
        TextUtils.parseInternalDate(dateTime), dateFormat);
    }

    return dateTime;
  }

  public String getViewEndDateTime()
  {
    String dateTime = "";
    String dateFormat = "dd/MM/yyyy";
    InterventionView row = (InterventionView)getValue("#{row}");
    if (row != null)
    {
      String typeId = row.getIntTypeId();    
      if (typeId != null)
      {
        Type type = TypeCache.getInstance().getType(typeId);
        if (type != null)
        {
          PropertyDefinition pd = type.getPropertyDefinition(VIEW_DATE_FORMAT);        
          if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
            dateFormat = (String)pd.getValue().get(0);
        }
      }       
      dateTime = row.getEndDate() + (row.getEndTime() != null ? row.getEndTime() : "");
      dateTime = TextUtils.formatDate(
        TextUtils.parseInternalDate(dateTime), dateFormat);
    }

    return dateTime;
  }
  
  public boolean isRenderEndDate()
  {
    InterventionView row = (InterventionView)getValue("#{row}");
    String typeId = row.getIntTypeId();    
    if (typeId != null)
    {
      Type type = TypeCache.getInstance().getType(typeId);
      if (type != null)
      {
        PropertyDefinition pd = type.getPropertyDefinition(RENDER_END_DATE_PROPERTY);        
        if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
        {          
          return ("true".equals(pd.getValue().get(0)));
        }
      }
    }
    return true;
  }

  public String getTypeDescription()
  {
    InterventionView row = (InterventionView)getValue("#{row}");
    String typeId = row.getIntTypeId();
    return getTypeDescription(typeId);
  }
  
  public String getIntProbTypeDescription()
  {
    String description = "";
    InterventionProblemView row = (InterventionProblemView)getValue("#{intProb}");
    String typeId = row.getProblem().getProbTypeId();
    
    if (typeId != null)
    {
      Type type = TypeCache.getInstance().getType(typeId);
      description = type.getDescription();
    }
    else
      description = typeId;
    return description;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  public String showType()
  {
    return getControllerBean().showObject("Type", getCurrentTypeId());
  }

  public boolean isRenderShowTypeButton()
  {
    return getCurrentTypeId() != null && getCurrentTypeId().trim().length() > 0;
  }
  
  public boolean isRenderInactiveButton()
  {
    return renderInactiveButton;
  }
   
  public String addProblem()
  {
    try
    {
      if (selectedProblem != null)
      {
        InterventionProblem intProblem = new InterventionProblem();
        intProblem.setIntId(editingIntervention.getIntId());
        intProblem.setProbId(selectedProblem);
        CaseConfigBean.getPort().storeInterventionProblem(intProblem);
        loadIntProblems(editingIntervention.getIntId());
        selectedProblem = null;
      }
    } 
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public String removeProblem()
  {
    try
    {
      InterventionProblemView intProbView = (InterventionProblemView)getValue("#{intProb}");
      if (intProbView != null)
      {
        String intProbId = intProbView.getIntProbId();
        CaseConfigBean.getPort().removeInterventionProblem(intProbId);
        loadIntProblems(editingIntervention.getIntId());
      }
    } 
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }  
  
  public List<SelectItem> getSelectableProblemSelectItems()
  {
    return selectableProblemSelectItems;
  }

  public void setSelectableProblemSelectItems(List<SelectItem> selectableProblemSelectItems)
  {
    this.selectableProblemSelectItems = selectableProblemSelectItems;
  }

  public void setSolvedProblems(List<InterventionProblemView> solvedProblems)
  {
    this.solvedProblems = solvedProblems;
  }
  
  public List<InterventionProblemView> getSolvedProblems()
  {
    return solvedProblems;
  }

  public String getSelectedProblem()
  {
    return selectedProblem;
  }

  public void setSelectedProblem(String selectedProblem)
  {
    this.selectedProblem = selectedProblem;
  }  
  
  public boolean isRenderCasePersons()
  {
    return (personTypeFilter != null);
  }
  
  public boolean isImmediateClosing()
  {
    return isImmediateClosing(getCurrentTypeId());
  }
  
  @Override
  public boolean isModified()
  {
    return editingIntervention != null;
  }
  
  //Private methods
  
  private boolean isInterventionAutoStartDate(String intTypeId)
  {
    if (intTypeId != null)
    {
      Type intType = TypeCache.getInstance().getType(intTypeId);
      if (intType != null)
      {
        PropertyDefinition pd = 
          intType.getPropertyDefinition(AUTO_START_DATE_PROPERTY);
        if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
          return Boolean.valueOf(pd.getValue().get(0));
      }      
    }
    return true; //true by default
  }
  
  private boolean isImmediateClosing(String intTypeId)
  {
    if (intTypeId != null)
    {
      Type intType = TypeCache.getInstance().getType(intTypeId);
      if (intType != null)
      {
        PropertyDefinition pd = 
          intType.getPropertyDefinition(IMMEDIATE_CLOSING_PROPERTY);
        if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
          return Boolean.valueOf(pd.getValue().get(0));
      }      
    }
    return false; //false by default
  }

  private List<SelectItem> getCasePersonsSelectItems()
  {
    List<SelectItem> selectItems = new ArrayList();
    selectItems.add(new SelectItem("", " ")); //Add blank entry
    
    CaseManagerPort port;
    try
    {
      CasePersonFilter filter = new CasePersonFilter();
      filter.setCaseId(getObjectId());
      port = CaseConfigBean.getPort();
      List<CasePersonView> casePersonViewList = port.findCasePersonViews(filter);
      for (CasePersonView casePersonView : casePersonViewList)
      {
        String cpTypeId = casePersonView.getCasePersonTypeId();
        Type type = TypeCache.getInstance().getType(cpTypeId);
        if (type != null && 
          (personTypeFilter == null || type.isDerivedFrom(personTypeFilter)))
        {
          String personId = casePersonView.getPersonView().getPersonId();
          String personName = casePersonView.getPersonView().getFullName();
          if (cpTypeId != null && !cpTypeId.equals(getRootTypeId()))
          {
            personName = personName + " (" + type.getDescription() + ")";
          }
          selectItems.add(new SelectItem(personId, personName));
        }
      }
    }
    catch (Exception e)
    {
      error(e);
    }
    
    return selectItems;
  }  
  
  private void loadIntProblems(String intId) throws Exception
  {
    selectedProblem = null;
    
    solvedProblems = new ArrayList();
    if (intId != null)
    {
      InterventionProblemFilter filter = new InterventionProblemFilter();
      filter.setIntId(intId);
      solvedProblems = 
        CaseConfigBean.getPort().findInterventionProblemViews(filter);
    }
    
    Map probIds = new HashMap();
    for (InterventionProblemView ipView : solvedProblems)
    {
      probIds.put(ipView.getProblem().getProbId(), ipView);
    }

    String caseId = getObjectBean().getObjectId();    
    ProblemFilter pFilter = new ProblemFilter();
    pFilter.setCaseId(caseId);

    List<ProblemView> problemViews = 
      CaseConfigBean.getPort().findProblemViews(pFilter);
    selectableProblemSelectItems = new ArrayList();
    for (ProblemView problemView : problemViews)
    {
      if (!probIds.containsKey(problemView.getProbId())) //Only items not selected yet
      {
        String probTypeId = problemView.getProbTypeId();
        String probType = probTypeId;
        if (probTypeId != null)
        {
          Type type = TypeCache.getInstance().getType(probTypeId);
          probType = problemView.getProbId() + " - " + type.getDescription();
        }
        selectableProblemSelectItems.add(
          new SelectItem(problemView.getProbId(), probType));
      }
    }    
  }  

  private String getStartDate(String dateTime, String intTypeId)
  {
    String date = null;
    if (dateTime == null)
    {
      if (isInterventionAutoStartDate(intTypeId))
        date = new SimpleDateFormat("yyyyMMdd").format(new Date());
    }
    else
      date = getDate(dateTime);

    return date;
  }

  private String getStartTime(String dateTime, String intTypeId)
  {
    String time = null;
    if (dateTime == null)
    {
      if (isInterventionAutoStartDate(intTypeId))
        time = new SimpleDateFormat("HHmmss").format(new Date());
    }
    else
      time = getTime(dateTime);

    return time;
  }
  
  private String getEndDate(String dateTime, String intTypeId)
  {
    String date = null;
    if (dateTime == null)
    {
      if (isImmediateClosing(intTypeId))
        date = getStartDate(getStartDateTime(), intTypeId);
    }
    else
      date = getDate(dateTime);
    return date;
  }  

  private String getEndTime(String dateTime, String intTypeId)
  {
    String time = null;
    if (dateTime == null)
    {
      if (isInterventionAutoStartDate(intTypeId))
        time = getStartTime(getStartDateTime(), intTypeId);
    }
    else
      time = getTime(dateTime);

    return time;
  }

  private String getDate(String dateTime)
  {
    String date = null;

    if (dateTime != null && dateTime.length() == 14)
      date = dateTime.substring(0, 8);

    return date;
  }

  private String getTime(String dateTime)
  {
    String time = null;

    if (dateTime != null && dateTime.length() == 14)
      time = dateTime.substring(8);

    return time;
  }

  private String concatDateTime(String date, String time)
  {
    String dateTime = null;

    if (date != null && time != null)
      dateTime = date + time;
    else if (date != null && time == null)
      dateTime = date + "000000";

    return dateTime;
  }
  
  private String getTypeDescription(String typeId)
  {
    String description = "";
    TypeCache typeCache = TypeCache.getInstance();
    try
    {
      Type type = typeCache.getType(typeId);
      if (type != null)
        description = type.formatTypePath(false, true, false, getRootTypeId());
      else
        description = typeId;
    }
    catch (Exception ex)
    {
      warn(ex.getMessage());
    }

    return description;
  }    

  private void setProblemsMap()
  {
    try
    {    
      problemsMap = new HashMap<String,List<InterventionProblemView>>();
      for (InterventionView caseInterventionView : rows)
      {
        InterventionProblemFilter filter = new InterventionProblemFilter();
        filter.setIntId(caseInterventionView.getIntId());
        List<InterventionProblemView> intProbView =
          CaseConfigBean.getPort().findInterventionProblemViews(filter);
        problemsMap.put(caseInterventionView.getIntId(), intProbView);
      }
    } 
    catch (Exception ex)
    {
      warn(ex.getMessage());
    }
  }
    
  @Override
  public Object getSelectedRow()
  {
    return getRequestMap().get("row");    
  }
  
  @Override
  protected String getRowId(Object row)
  {
    InterventionView intRow = (InterventionView)row;
    return intRow.getIntId();    
  }

  @Override
  protected String getRowTypeId(Object row)
  {
    InterventionView intRow = (InterventionView)row;
    return intRow.getIntTypeId();
  }

  @Override
  protected String getShowPropertiesPropertyName(Object row)
  {
    return SHOW_PROPERTIES_PROPERTY;
  }
}
