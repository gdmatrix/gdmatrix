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
package org.santfeliu.misc.query.web;

import java.util.ArrayList;
import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.dic.EnumTypeItem;
import org.matrix.dic.EnumTypeItemFilter;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.misc.query.Query;
import org.santfeliu.misc.query.Query.Parameter;

/**
 *
 * @author realor
 */
public class QueryEditorBean extends FacesBean implements Savable
{
  private Integer scroll;
  private Query.Parameter editingParameter;
  private Query.Predicate editingPredicate;
  private Query.Output editingOutput;
  private transient List<SelectItem> formatSelectItems;
  private transient List<SelectItem> queryScopeSelectItems;
  private transient List<SelectItem> queryObjectSelectItems;
  private transient List<SelectItem> queryTypeSelectItems;
  
  public QueryEditorBean()
  {    
  }
  
  public final Query getQuery()
  {
    return (Query)getValue("#{queryBean.query}");
  }  

  public Integer getScroll()
  {
    return scroll;
  }

  public void setScroll(Integer scroll)
  {
    this.scroll = scroll;
  }

  public Query.Parameter getEditingParameter()
  {
    return editingParameter;
  }

  public Query.Predicate getEditingPredicate()
  {
    return editingPredicate;
  }

  public Query.Output getEditingOutput()
  {
    return editingOutput;
  }
  
  public void editParameter()
  {
    editingParameter = (Query.Parameter)getValue("#{parameter}");
  }
  
  public void addParameter()
  {
    editingParameter = getQuery().addParameter();
    editingParameter.setName("NEW");
    editingParameter.setDescription("New");
    editingParameter.setFormat(Parameter.TEXT);
  }
  
  public void removeParameter()
  {
    getQuery().removeParameter(editingParameter);
    editingParameter = null;
  }
  
  public void editPredicate()
  {
    editingPredicate = (Query.Predicate)getValue("#{predicate}");
  }

  public void addPredicate()
  {
    editingPredicate = getQuery().addPredicate();
    editingPredicate.setName("NEW");
  }

  public void movePredicateUp()
  {
    getQuery().movePredicate(editingPredicate, -1);
  }

  public void movePredicateDown()
  {
    getQuery().movePredicate(editingPredicate, +1);
  }
  
  public void removePredicate()
  {
    getQuery().removePredicate(editingPredicate);
    editingPredicate = null;
  }
    
  public void editOutput()
  {
    editingOutput = (Query.Output)getValue("#{output}");
  }
  
  public void addOutput()
  {
    editingOutput = getQuery().addOutput();
    editingOutput.setName("NEW");
  }

  public void moveOutputUp()
  {
    getQuery().moveOutput(editingOutput, -1);
  }

  public void moveOutputDown()
  {
    getQuery().moveOutput(editingOutput, +1);
  }
  
  public void removeOutput()
  {
    getQuery().removeOutput(editingOutput);
    editingOutput = null;
  }
  
  public List<SelectItem> getFormatSelectItems()
  {
    if (formatSelectItems == null)
    {
      formatSelectItems = new ArrayList<SelectItem>();
      formatSelectItems.add(new SelectItem(Parameter.TEXT, "TEXT"));
      formatSelectItems.add(new SelectItem(Parameter.NUMBER, "NUMBER"));
      formatSelectItems.add(new SelectItem(Parameter.DATE, "DATE"));
    }
    return formatSelectItems;
  }

  public List<SelectItem> getQueryScopeSelectItems()
  {
    if (queryScopeSelectItems == null)
    {
      queryScopeSelectItems = getEnumTypeSelectItems(QueryBean.QUERY_SCOPE_TYPEID);
    }
    return queryScopeSelectItems;
  }

  public List<SelectItem> getQueryTypeSelectItems()
  {
    if (queryTypeSelectItems == null)
    {
      queryTypeSelectItems = getEnumTypeSelectItems(QueryBean.QUERY_TYPE_TYPEID);
    }
    return queryTypeSelectItems;
  }

  public List<SelectItem> getQueryObjectSelectItems()
  {
    if (queryObjectSelectItems == null)
    {
      queryObjectSelectItems = getEnumTypeSelectItems(QueryBean.QUERY_OBJECT_TYPEID);
    }
    return queryObjectSelectItems;
  }
  
  private List<SelectItem> getEnumTypeSelectItems(String enumTypeId)
  {
    List<SelectItem> selectItems = new ArrayList<SelectItem>(); 
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
  
  public String copyQuery()
  {
    QueryBean queryBean = (QueryBean)getBean("queryBean");
    queryBean.copyQuery();
    return "query_editor";
  }
  
  public String reloadQuery()
  {
    try
    {
      QueryBean queryBean = (QueryBean)getBean("queryBean");
      queryBean.reloadQuery();
      info("QUERY_RELOAD");
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "query_editor";
  }
  
  public String saveQuery()
  {
    try
    {
      QueryBean queryBean = (QueryBean)getBean("queryBean");
      queryBean.saveQuery();
      info("QUERY_SAVED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "query_editor";
  }

  public String removeQuery()
  {
    try
    {
      QueryBean queryBean = (QueryBean)getBean("queryBean");
      queryBean.removeQuery();
      info("QUERY_REMOVED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "query_editor";
  }
}
