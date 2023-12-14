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
package org.santfeliu.webapp.modules.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.EnumTypeItem;
import org.matrix.dic.EnumTypeItemFilter;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.misc.query.Query;
import org.santfeliu.misc.query.Query.Output;
import org.santfeliu.misc.query.Query.Parameter;
import org.santfeliu.misc.query.Query.Predicate;
import org.santfeliu.web.WebBean;

/**
 *
 * @author lopezrj-sf
 */
@Named
@RequestScoped
public class QueryEditBean extends WebBean implements Serializable
{
  private Integer scroll;
  private Parameter editingParameter;
  private Predicate editingPredicate;
  private Output editingOutput;
  private List<SelectItem> formatSelectItems;
  private List<SelectItem> queryScopeSelectItems;
  private List<SelectItem> queryObjectSelectItems;
  private List<SelectItem> queryTypeSelectItems;  
  private int selectedTab;
  
  //DataTable variables
  private List<Parameter> parameterList = null;
  private List<Predicate> predicateList = null;
  private List<Output> outputList = null;
  private Parameter selectedParameter;
  private Predicate selectedPredicate;
  private Output selectedOutput;
  
  @Inject
  QueryMainBean queryMainBean;  
  
  @Inject
  QueryListBean queryListBean;   
  
  public QueryEditBean()
  {
  }

  public int getSelectedTab()
  {
    return selectedTab;
  }

  public void setSelectedTab(int selectedTab)
  {
    this.selectedTab = selectedTab;
  }

  public Query getQuery()
  {
    return queryMainBean.getQuery();
  }

  public Integer getScroll()
  {
    return scroll;
  }

  public void setScroll(Integer scroll)
  {
    this.scroll = scroll;
  }

  public Parameter getEditingParameter()
  {
    return editingParameter;
  }

  public Predicate getEditingPredicate()
  {
    return editingPredicate;
  }
  
  public Output getEditingOutput()
  {
    return editingOutput;
  }  
  
  public void editParameter(Parameter parameter)
  {
    editingParameter = parameter;
  }
  
  public void addParameter()
  {
    int idx = getMaxNewParameter() + 1;
    editingParameter = getQuery().addParameter();
    editingParameter.setName("NEW" + idx);
    editingParameter.setDescription("New");
    editingParameter.setFormat(Parameter.TEXT);
    parameterList = null;
    selectedParameter = editingParameter;    
  }

  public void moveParameterUp()
  {
    getQuery().moveParameter(editingParameter, -1);
    parameterList = null;
  }

  public void moveParameterDown()
  {
    getQuery().moveParameter(editingParameter, +1);
    parameterList = null;
  }

  public void removeParameter()
  {
    getQuery().removeParameter(editingParameter);
    editingParameter = null;
    parameterList = null;
    selectedParameter = null;
  }
  
  public void sortParameters()
  {
    getQuery().sortParameters();
    parameterList = null;    
  }

  public boolean isRenderMoveParameterUpButton()
  {
    return isRenderMoveElementUpButton(getQuery().getParameters(),
      editingParameter);
  }

  public boolean isRenderMoveParameterDownButton()
  {
    return isRenderMoveElementDownButton(getQuery().getParameters(),
      editingParameter);
  }

  public boolean isRenderRemoveParameterButton()
  {
    return isRenderRemoveElementButton(editingParameter);
  }

  public void editPredicate(Predicate predicate)
  {
    editingPredicate = predicate;
  }

  public void addPredicate()
  {
    int idx = getMaxNewPredicate() + 1;    
    editingPredicate = getQuery().addPredicate();
    editingPredicate.setName("NEW" + idx);
    predicateList = null;
    selectedPredicate = editingPredicate;    
  }

  public void movePredicateUp()
  {
    getQuery().movePredicate(editingPredicate, -1);
    predicateList = null;
  }

  public void movePredicateDown()
  {
    getQuery().movePredicate(editingPredicate, +1);
    predicateList = null;
  }

  public void removePredicate()
  {
    getQuery().removePredicate(editingPredicate);
    editingPredicate = null;
    predicateList = null;
    selectedPredicate = null;    
  }

  public void sortPredicates()
  {
    getQuery().sortPredicates();
    predicateList = null;
  }  
  
  public boolean isRenderMovePredicateUpButton()
  {
    return isRenderMoveElementUpButton(getQuery().getPredicates(),
      editingPredicate);
  }

  public boolean isRenderMovePredicateDownButton()
  {
    return isRenderMoveElementDownButton(getQuery().getPredicates(),
      editingPredicate);
  }

  public boolean isRenderRemovePredicateButton()
  {
    return isRenderRemoveElementButton(editingPredicate);
  }

  public void editOutput(Output output)
  {
    editingOutput = output;
  }

  public void addOutput()
  {
    int idx = getMaxNewOutput() + 1;
    editingOutput = getQuery().addOutput();
    editingOutput.setName("NEW" + idx);
    outputList = null;
    selectedOutput = editingOutput;
  }

  public void moveOutputUp()
  {
    getQuery().moveOutput(editingOutput, -1);
    outputList = null;    
  }

  public void moveOutputDown()
  {
    getQuery().moveOutput(editingOutput, +1);
    outputList = null;    
  }

  public void removeOutput()
  {
    getQuery().removeOutput(editingOutput);
    editingOutput = null;
    outputList = null;
    selectedOutput = null;
  }

  public void sortOutputs()
  {
    getQuery().sortOutputs();
    outputList = null;
  }    
  
  public boolean isRenderMoveOutputUpButton()
  {
    return isRenderMoveElementUpButton(getQuery().getOutputs(),
      editingOutput);
  }

  public boolean isRenderMoveOutputDownButton()
  {
    return isRenderMoveElementDownButton(getQuery().getOutputs(),
      editingOutput);
  }

  public boolean isRenderRemoveOutputButton()
  {
    return isRenderRemoveElementButton(editingOutput);
  }

  public void setFormatSelectItems(List<SelectItem> formatSelectItems)
  {
    this.formatSelectItems = formatSelectItems;
  }

  public List<SelectItem> getFormatSelectItems()
  {
    if (formatSelectItems == null)
    {
      formatSelectItems = new ArrayList();
      formatSelectItems.add(new SelectItem(Parameter.TEXT, "TEXT"));
      formatSelectItems.add(new SelectItem(Parameter.NUMBER, "NUMBER"));
      formatSelectItems.add(new SelectItem(Parameter.DATE, "DATE"));
    }
    return formatSelectItems;
  }

  public void setQueryScopeSelectItems(List<SelectItem> queryScopeSelectItems)
  {
    this.queryScopeSelectItems = queryScopeSelectItems;
  }

  public List<SelectItem> getQueryScopeSelectItems()
  {
    if (queryScopeSelectItems == null)
    {
      queryScopeSelectItems = getEnumTypeSelectItems(
        QueryMainBean.QUERY_SCOPE_TYPEID);
    }
    return queryScopeSelectItems;
  }

  public void setQueryTypeSelectItems(List<SelectItem> queryTypeSelectItems)
  {
    this.queryTypeSelectItems = queryTypeSelectItems;
  }

  public List<SelectItem> getQueryTypeSelectItems()
  {
    if (queryTypeSelectItems == null)
    {
      queryTypeSelectItems = getEnumTypeSelectItems(
        QueryMainBean.QUERY_TYPE_TYPEID);
    }
    return queryTypeSelectItems;
  }

  public void setQueryObjectSelectItems(List<SelectItem> queryObjectSelectItems)
  {
    this.queryObjectSelectItems = queryObjectSelectItems;
  }

  public List<SelectItem> getQueryObjectSelectItems()
  {
    if (queryObjectSelectItems == null)
    {
      queryObjectSelectItems = getEnumTypeSelectItems(
        QueryMainBean.QUERY_OBJECT_TYPEID);
    }
    return queryObjectSelectItems;
  }

  public void viewQuery()
  {
    queryMainBean.setView("query_view");
  }  
  
  public void copyQuery()
  {
    queryMainBean.copyQuery();
    info("QUERY_COPIED");    
  }

  public void reloadQuery()
  {
    try
    {
      reset();
      queryMainBean.reloadQuery();
      info("QUERY_RELOAD");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void saveQuery()
  {
    try
    {
      queryMainBean.saveQuery();
      info("QUERY_SAVED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public void saveNewQuery()
  {
    try
    {
      queryMainBean.saveQuery();
      if (getQuery().getBase() != null)
      {
        reset();
        queryMainBean.reloadQuery();
      }
      info("QUERY_SAVED");
    }
    catch (Exception ex)
    {
      error(ex);
    }    
  }
  
  public void removeQuery()
  {
    try
    {
      reset();
      queryMainBean.removeQuery();
      info("QUERY_REMOVED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  private List<SelectItem> getEnumTypeSelectItems(String enumTypeId)
  {
    List<SelectItem> selectItems = new ArrayList();
    TypeCache typeCache = TypeCache.getInstance();
    EnumTypeItemFilter filter = new EnumTypeItemFilter();
    filter.setEnumTypeId(enumTypeId);
    List<EnumTypeItem> items =
      typeCache.getPort().findEnumTypeItems(filter);
    for (EnumTypeItem item : items)
    {
      SelectItem selectItem = new SelectItem();
      selectItem.setLabel(item.getLabel());
      selectItem.setValue(item.getValue());
      selectItems.add(selectItem);
    }
    return selectItems;
  }

  private boolean isRenderMoveElementUpButton(List elementList, Object element)
  {
    return (element != null && elementList.indexOf(element) > 0);
  }

  private boolean isRenderMoveElementDownButton(List elementList,
    Object element)
  {
    return (element != null &&
      (elementList.indexOf(element) < elementList.size() - 1));
  }

  private boolean isRenderRemoveElementButton(Object element)
  {
    return (element != null);
  }  

  private void reset()
  {
    editingParameter = null;
    editingPredicate = null;
    editingOutput = null;      
    selectedTab = 0;            
    parameterList = null;
    predicateList = null;
    outputList = null;
    selectedParameter = null;
    selectedPredicate = null;
    selectedOutput = null;
  }  
  
  /* PARAMETERS */

  public List<Parameter> getParameterList()
  {
    if (parameterList == null)
    {
      parameterList = new ArrayList();
      parameterList.addAll(getQuery().getParameters());
    }      
    return parameterList;
  }

  public void setParameterList(List<Parameter> parameterList)
  {
    this.parameterList = parameterList;
  }
    
  public void onParameterSelect() 
  {
    editParameter(selectedParameter);
  }  
  
  public void onParameterReorder() 
  {
    syncParameters();
  }  

  public Parameter getSelectedParameter()
  {
    return selectedParameter;
  }

  public void setSelectedParameter(Parameter selectedParameter)
  {
    this.selectedParameter = selectedParameter;
  }
  
  private void syncParameters()
  {
    StringBuilder criteria = new StringBuilder();    
    for (int i = 0; i < parameterList.size(); i++)
    {      
      if (i > 0) criteria.append(",");
      criteria.append(parameterList.get(i).getName());
    }
    getQuery().sortParameters(criteria.toString());
    parameterList = null;    
  }
  
  private int getMaxNewParameter()
  {
    int max = 0;
    List<Parameter> list = getQuery().getParameters();    
    for (Parameter parameter : list)
    {
      if (parameter.getName().startsWith("NEW"))
      {
        try
        {
          Integer idx = Integer.parseInt(parameter.getName().substring(3));
          if (idx > max) max = idx;
        }
        catch (NumberFormatException ex)
        {
          //Ignore
        }        
      }
    }
    return max;
  }  
  
  /* PREDICATES */

  public List<Predicate> getPredicateList()
  {
    if (predicateList == null)
    {
      predicateList = new ArrayList();
      predicateList.addAll(getQuery().getPredicates());
    }
    return predicateList;
  }

  public void setPredicateList(List<Predicate> predicateList)
  {
    this.predicateList = predicateList;
  }
    
  public void onPredicateSelect() 
  {
    editPredicate(selectedPredicate);
  } 

  public void onPredicateReorder() 
  {
    syncPredicates();    
  }  

  public Predicate getSelectedPredicate()
  {
    return selectedPredicate;
  }

  public void setSelectedPredicate(Predicate selectedPredicate)
  {
    this.selectedPredicate = selectedPredicate;
  }
  
  private void syncPredicates()
  {
    StringBuilder criteria = new StringBuilder();    
    for (int i = 0; i < predicateList.size(); i++)
    {      
      if (i > 0) criteria.append(",");
      criteria.append(predicateList.get(i).getName());
    }
    getQuery().sortPredicates(criteria.toString());
    predicateList = null;    
  }
  
  private int getMaxNewPredicate()
  {
    int max = 0;
    List<Predicate> list = getQuery().getPredicates();    
    for (Predicate predicate : list)
    {
      if (predicate.getName().startsWith("NEW"))
      {
        try
        {
          Integer idx = Integer.parseInt(predicate.getName().substring(3));
          if (idx > max) max = idx;
        }
        catch (NumberFormatException ex)
        {
          //Ignore
        }        
      }
    }
    return max;
  }  

  /* OUTPUTS */

  public List<Output> getOutputList()
  {
    if (outputList == null)
    {
      outputList = new ArrayList();
      outputList.addAll(getQuery().getOutputs());
    }
    return outputList;
  }

  public void setOutputList(List<Output> outputList)
  {
    this.outputList = outputList;
  }
    
  public void onOutputSelect() 
  {
    editOutput(selectedOutput);
  } 

  public void onOutputReorder() 
  {
    syncOutputs();    
  }  

  public Output getSelectedOutput()
  {
    return selectedOutput;
  }

  public void setSelectedOutput(Output selectedOutput)
  {
    this.selectedOutput = selectedOutput;
  }
  
  private void syncOutputs()
  {
    StringBuilder criteria = new StringBuilder();    
    for (int i = 0; i < outputList.size(); i++)
    {      
      if (i > 0) criteria.append(",");
      criteria.append(outputList.get(i).getName());
    }
    getQuery().sortOutputs(criteria.toString());
    outputList = null;    
  }
  
  private int getMaxNewOutput()
  {
    int max = 0;
    List<Output> list = getQuery().getOutputs();    
    for (Output output : list)
    {
      if (output.getName().startsWith("NEW"))
      {
        try
        {
          Integer idx = Integer.parseInt(output.getName().substring(3));
          if (idx > max) max = idx;
        }
        catch (NumberFormatException ex)
        {
          //Ignore
        }        
      }
    }
    return max;
  }  

}
