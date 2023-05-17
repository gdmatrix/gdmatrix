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
  private int selectedTab;

  public QueryEditorBean()
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

  public void moveParameterUp()
  {
    getQuery().moveParameter(editingParameter, -1);
  }

  public void moveParameterDown()
  {
    getQuery().moveParameter(editingParameter, +1);
  }

  public void removeParameter()
  {
    getQuery().removeParameter(editingParameter);
    editingParameter = null;
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
