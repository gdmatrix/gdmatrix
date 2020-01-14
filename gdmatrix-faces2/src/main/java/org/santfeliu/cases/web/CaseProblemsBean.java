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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;
import org.matrix.cases.Case;

import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;
import org.matrix.cases.InterventionProblemFilter;
import org.matrix.cases.InterventionProblemView;
import org.matrix.cases.Problem;
import org.matrix.cases.ProblemFilter;
import org.matrix.cases.ProblemView;

import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.DictionaryManagerPort;
import org.matrix.dic.DictionaryManagerService;
import org.matrix.dic.EnumTypeItem;
import org.matrix.dic.EnumTypeItemFilter;
import org.matrix.dic.PropertyDefinition;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import static org.santfeliu.cases.web.InterventionSearchBean.INTERVENTION_TAB_MID;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.web.obj.TypifiedPageBean;


/**
 *
 * @author unknown
 */
public class CaseProblemsBean extends TypifiedPageBean
{
  public static final String ROOT_TYPE_ID_PROPERTY = "_problemRootTypeId";
  public static final String REASON_ENUM_TYPE_PROPERTY = "_reasonProblemEnumType";
  public static final String RENDER_PRIORITY_PROPERTY = "_renderProblemPriority";
  public static final String RENDER_PERSON_PROPERTY = "_renderProblemPerson";
  public static final String PERSON_TYPE_FILTER = "_problemsPersonTypeFilter";  
  public static final String GROUPBY_PROPERTY = "_problemsGroupBy";
  public static final String GROUP_SELECTION_MODE_PROPERTY = "_problemsGroupSelectionMode";


  private List<ProblemView> rows;
  private Problem editingProblem;
  private List<SelectItem> reasonSelectItems;
  private boolean renderPriority;
  private boolean renderPerson;
  private String personTypeFilter;
  private Map<String,List<InterventionProblemView>> interventionsMap;
  
  public CaseProblemsBean()
  {
    super(DictionaryConstants.PROBLEM_TYPE, "CASE_ADMIN");

    CaseMainBean caseMainBean = (CaseMainBean)getBean("caseMainBean");
    Case cas = caseMainBean.getCase();
    Type caseType = TypeCache.getInstance().getType(cas.getCaseTypeId());
    if (caseType != null)
    {
      //Root type
      PropertyDefinition pd =
        caseType.getPropertyDefinition(ROOT_TYPE_ID_PROPERTY);
      if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
        setRootTypeId(pd.getValue().get(0));
      
      //Reason
      PropertyDefinition pd2 =
        caseType.getPropertyDefinition(REASON_ENUM_TYPE_PROPERTY);
      if (pd2 != null && pd2.getEnumTypeId() != null)
      {
        reasonSelectItems = new ArrayList();        
        String enumTypeId = pd2.getEnumTypeId();
        WSDirectory dir = WSDirectory.getInstance();
        WSEndpoint endpoint = dir.getEndpoint(DictionaryManagerService.class);
        DictionaryManagerPort port = endpoint.getPort(DictionaryManagerPort.class);
        EnumTypeItemFilter filter = new EnumTypeItemFilter();
        filter.setEnumTypeId(enumTypeId);
        List<EnumTypeItem> types = port.findEnumTypeItems(filter);
        for (EnumTypeItem type : types)
        {
          SelectItem item = new SelectItem(type.getValue(), type.getLabel());
          reasonSelectItems.add(item);
        }
      }
      
      //Priority
      PropertyDefinition pd3 = 
        caseType.getPropertyDefinition(RENDER_PRIORITY_PROPERTY);
      renderPriority = pd3 != null && !pd3.getValue().isEmpty() 
        && pd3.getValue().get(0).equalsIgnoreCase("true");
      
      //Person
      PropertyDefinition pd4 = 
        caseType.getPropertyDefinition(RENDER_PERSON_PROPERTY);
      renderPerson = pd4 != null && !pd4.getValue().isEmpty() 
        && pd4.getValue().get(0).equalsIgnoreCase("true");   
      
      PropertyDefinition pd5 =
        caseType.getPropertyDefinition(GROUP_SELECTION_MODE_PROPERTY);
      if (pd5 != null && pd5.getValue() != null && pd5.getValue().size() > 0)
        groupSelectionMode = pd5.getValue().get(0);

      PropertyDefinition pd6 =
        caseType.getPropertyDefinition(GROUPBY_PROPERTY);
      if (pd6 != null && pd6.getValue() != null && pd6.getValue().size() > 0)
        groupBy = pd6.getValue().get(0);
      
      PropertyDefinition pd7 =
        caseType.getPropertyDefinition(PERSON_TYPE_FILTER);
      if (pd7 != null && pd7.getValue() != null && pd7.getValue().size() > 0)
        personTypeFilter = pd7.getValue().get(0);      
    }
    load();
  }
  
  //Object Actions
  public String show()
  {
    return "case_problems";
  }
  
  private void load()
  {
    try
    {
      if (!isNew())
      {
        ProblemFilter filter = new ProblemFilter();
        filter.setCaseId(getObjectId());
        rows = CaseConfigBean.getPort().findProblemViews(filter);
        
        setRowsTypeLabels();
        setInterventionsMap();
        setGroups(rows, getGroupExtractor());         
      }
    }
    catch (Exception ex)
    {
      error(ex);
      ex.printStackTrace();
    }
  }     
  
  public String store()
  {
    if (editingProblem != null)
    {
      storeProblem();
    }
    else
    {
      load();
    }
    return show();
  }
  
  //Page Actions
  public String createProblem()
  {
    editingProblem = new Problem();
    return null;
  }

  public String editProblem()
  {
    ProblemView row = (ProblemView)getExternalContext().
      getRequestMap().get("row");   
    String probId = row.getProbId();
    return editProblem(probId);
  }
  
  public String editProblem(String problemId)
  {
    try
    {
      editingProblem =
        CaseConfigBean.getPort().loadProblem(problemId);
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;    
  }

  public String storeProblem()
  {
    try
    {
      String caseId = getObjectId();
      editingProblem.setCaseId(caseId);
      
      CaseManagerPort port = CaseConfigBean.getPort();
      port.storeProblem(editingProblem);
      editingProblem = null;
      load();
    }
    catch (Exception ex)
    {
      error(ex.getMessage());
    }
    return null;
  }    
  
  public String removeProblem()
  {
    try
    {
      ProblemView row = (ProblemView)getRequestMap().get("row");
      CaseManagerPort port = CaseConfigBean.getPort();
      port.removeProblem(row.getProbId());
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public String cancelProblem()
  {
    editingProblem = null;
    return null;
  }  
  
  public String searchPerson()
  {
    return getControllerBean().searchObject("Person",
      "#{caseProblemsBean.editingProblem.personId}");
  }

  public String showRowPerson()
  {
    ProblemView row = (ProblemView)getRequestMap().get("row");
    return getControllerBean().showObject("Person",
      row.getPersonView().getPersonId());
  }

  public String showEditPerson()
  {
    return getControllerBean().showObject("Person",
      editingProblem.getPersonId());
  }

  public boolean isRenderShowEditPersonButton()
  {
    return editingProblem.getPersonId() != null &&
      editingProblem.getPersonId().trim().length() > 0;
  }  
  
  //Accessors
  public String getGroupBy()
  {
    return groupBy;
  }

  public void setEditingProblem(Problem editingProblem)
  {
    this.editingProblem = editingProblem;
  }

  public Problem getEditingProblem()
  {
    return editingProblem;
  }
  
  //Select Items
  public List<SelectItem> getCasePersonsSelectItems()
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
        if ((personTypeFilter == null || cpTypeId.equals(personTypeFilter)))
        {
          String personId = casePersonView.getPersonView().getPersonId();
          String personName = casePersonView.getPersonView().getFullName();
          if (cpTypeId != null && !cpTypeId.equals(getRootTypeId()))
          {
            Type type = TypeCache.getInstance().getType(cpTypeId);
            if (type != null)
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
  
  public List<SelectItem> getReasonSelectItems()
  {
    return reasonSelectItems;    
  }
  
  public boolean isRenderReason()
  {
    return reasonSelectItems != null;
  }
  
  public boolean isRenderPriority()
  {
    return renderPriority;
  }
  
  public boolean isRenderPerson()
  {
    return renderPerson;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  public String showRowType()
  {
    return getControllerBean().showObject("Type", getRowTypeId());
  }

  public String showEditType()
  {
    return getControllerBean().showObject("Type",
      getEditingProblem().getProbTypeId());
  }

  public boolean isRenderShowEditTypeButton()
  {
    return getEditingProblem().getProbTypeId() != null &&
      getEditingProblem().getProbTypeId().trim().length() > 0;
  }

  public Map<String, List<InterventionProblemView>> getInterventionsMap()
  {
    return interventionsMap;
  }

  public void setInterventionsMap(Map<String, List<InterventionProblemView>> interventionsMap)
  {
    this.interventionsMap = interventionsMap;
  }
  
  public List<Map<String,String>> getInterventions()
  {
    List<Map<String,String>> result = 
      new ArrayList<Map<String,String>>();
    
    ProblemView pView = (ProblemView) getValue("#{row}");
    if (pView != null && interventionsMap != null)
    {
      List<InterventionProblemView> views = interventionsMap.get(pView.getProbId());
      if (views != null)
      {
        for (InterventionProblemView view : views)
        {
          HashMap map = new HashMap();    
          String intId = view.getIntervention().getIntId();
          map.put("intId", intId);
          String typeId = view.getIntervention().getIntTypeId();
          Type type = TypeCache.getInstance().getType(typeId);
          map.put("type", type.getDescription());
          map.put("comments", view.getIntervention().getComments());
          result.add(map);
        }
      }
    }    
    return result;
  }

  private String getRowTypeId()
  {
    try
    {
      ProblemView row = (ProblemView)getExternalContext().getRequestMap().get("row");
      String probId = row.getProbId();
      if (probId != null)
      {
        return CaseConfigBean.getPort().loadProblem(probId).getProbTypeId();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  private void setRowsTypeLabels()
  {
    for (ProblemView caseProblemView : rows)
    {
      String typeId = caseProblemView.getProbTypeId();
      TypeCache typeCache = TypeCache.getInstance();
      try
      {
        Type type = typeCache.getType(typeId);
        if (type != null)
          caseProblemView.setProbTypeId(type.formatTypePath(
            false, true, false, getRootTypeId()));
      }
      catch (Exception ex)
      {
        warn(ex.getMessage());
      }
    }
  }
  
  public String showIntervention()
  {
    String intTabMid = getProperty(INTERVENTION_TAB_MID);
    String outcome = getControllerBean().show(intTabMid, getObjectId());
    
    Map<String,String> intProbView = 
      (Map<String,String>)getValue("#{intProbView}");
    String intId = intProbView.get("intId");
    
    CaseInterventionsBean caseInterventionsBean = 
      (CaseInterventionsBean)getBean("caseInterventionsBean");
    caseInterventionsBean.editIntervention(intId);
    
    return outcome;
  }  
  
  private void setInterventionsMap()
  {
    try
    {    
      interventionsMap = new HashMap<String,List<InterventionProblemView>>();
      for (ProblemView caseProblemView : rows)
      {
        InterventionProblemFilter filter = new InterventionProblemFilter();
        filter.setProbId(caseProblemView.getProbId());
        List<InterventionProblemView> intProbView =
          CaseConfigBean.getPort().findInterventionProblemViews(filter);
        interventionsMap.put(caseProblemView.getProbId(), intProbView);
      }
    } 
    catch (Exception ex)
    {
      warn(ex.getMessage());
    }
  }  
}
