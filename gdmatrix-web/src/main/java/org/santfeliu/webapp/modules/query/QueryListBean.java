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
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.Property;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.OrderByProperty;
import org.santfeliu.web.WebBean;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.webapp.modules.dic.DicModuleBean;

/**
 *
 * @author lopezrj-sf
 */
@Named
@RequestScoped
public class QueryListBean extends WebBean implements Serializable
{  
  private String filterByName;
  private String filterByTitle;  
  private String filterByBase;  
  private String filterByScope;
  private String filterByType;
  private String filterByObject;
  private transient List<SelectItem> queryScopeSelectItems;
  private transient List<SelectItem> queryObjectSelectItems;
  private transient List<SelectItem> queryTypeSelectItems;  
  private List<Document> documents;
  private int firstRow;  

  @Inject
  QueryMainBean queryMainBean;  
  
  public QueryListBean()
  {
  }
  
  public String getFilterByName()
  {
    return filterByName;
  }

  public void setFilterByName(String filterByName)
  {
    this.filterByName = filterByName;
  }

  public String getFilterByTitle()
  {
    return filterByTitle;
  }

  public void setFilterByTitle(String filterByTitle)
  {
    this.filterByTitle = filterByTitle;
  }

  public String getFilterByBase() 
  {
    return filterByBase;
  }

  public void setFilterByBase(String filterByBase) 
  {
    this.filterByBase = filterByBase;
  }

  public String getFilterByScope() 
  {
    if (filterByScope == null || !isScopeVisible(filterByScope))
    {
      List<String> queryScopeList = 
        getSelectedMenuItem().getMultiValuedProperty("queryScope");
      if (!queryScopeList.isEmpty())
      {
        filterByScope = queryScopeList.get(0);
      }
    }
    return filterByScope;
  }

  public void setFilterByScope(String filterByScope) 
  {
    this.filterByScope = filterByScope;
  }

  public String getFilterByType() {
    return filterByType;
  }

  public void setFilterByType(String filterByType) 
  {
    this.filterByType = filterByType;
  }

  public String getFilterByObject() 
  {
    return filterByObject;
  }

  public void setFilterByObject(String filterByObject) 
  {
    this.filterByObject = filterByObject;
  }
  
  public List<SelectItem> getQueryScopeSelectItems()
  {
    if (queryScopeSelectItems == null)
    {
      queryScopeSelectItems = new ArrayList();  
      List<SelectItem> allQueryScopeSelectItems = 
        DicModuleBean.getEnumTypeSelectItems(QueryMainBean.QUERY_SCOPE_TYPEID);
      for (SelectItem item : allQueryScopeSelectItems)
      {
        if (isScopeVisible((String)item.getValue()))
        {
          queryScopeSelectItems.add(item);
        }
      }
    }
    return queryScopeSelectItems;
  }
  
  public boolean isRenderBlankScope()
  {
    List<String> queryScopeList = 
      getSelectedMenuItem().getMultiValuedProperty("queryScope");
    return queryScopeList.isEmpty();
  }
  
  public List<SelectItem> getQueryTypeSelectItems()
  {
    if (queryTypeSelectItems == null)
    {
      queryTypeSelectItems = DicModuleBean.getEnumTypeSelectItems(
        QueryMainBean.QUERY_TYPE_TYPEID);
    }
    return queryTypeSelectItems;
  }

  public List<SelectItem> getQueryObjectSelectItems()
  {
    if (queryObjectSelectItems == null)
    {
      queryObjectSelectItems = DicModuleBean.getEnumTypeSelectItems(
        QueryMainBean.QUERY_OBJECT_TYPEID);
    }
    return queryObjectSelectItems;
  }
  
  // **** actions ****

  public void search()
  {
    doFind();
    queryMainBean.setView("query_list");
  }
  
  public List<Document> getDocuments()
  {
    if (documents == null)
    {
      doFind();
    }
    return documents;
  }

  public void clearDocuments()
  {
    documents = null;
  }  

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }
    
  public String getQueryName()
  {
    Document document = (Document)getValue("#{document}");
    Property property = DictionaryUtils.getProperty(document, 
      QueryMainBean.QUERY_NAME_PROPERTY);
    return property == null ? null : property.getValue().get(0);
  }

  public String getQueryDescription()
  {
    Document document = (Document)getValue("#{document}");
    Property property = DictionaryUtils.getProperty(document, 
      QueryMainBean.QUERY_DESCRIPTION_PROPERTY);
    return property == null ? null : property.getValue().get(0);
  }
  
  public boolean isQueryUpdate()
  {
    Document document = (Document)getValue("#{document}");
    Property property = DictionaryUtils.getProperty(document, 
      QueryMainBean.QUERY_UPDATE_PROPERTY);
    return property == null ? 
      false : 
      Boolean.valueOf(property.getValue().get(0));
  }
  
  public void show()
  {
    if (documents == null)
    {
      search();
    }
    queryMainBean.setView("query_list");    
  }

  public void showQuery()
  {    
    try
    {
      String queryName = getQueryName();
      queryMainBean.setCreateNewVersion(true);
      queryMainBean.loadQuery(queryName);
      queryMainBean.setView("query_view");    
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  private boolean isScopeVisible(String scope)
  {
    List<String> queryScopeList = 
      getSelectedMenuItem().getMultiValuedProperty("queryScope");
    return queryScopeList.isEmpty() || queryScopeList.contains(scope);
  }

  private void doFind()
  {
    DocumentFilter filter = new DocumentFilter();
    filter.setFirstResult(0);
    filter.setDocTypeId(QueryMainBean.QUERY_TYPEID);
    if (!StringUtils.isBlank(filterByName))
    {
      Property property = new Property();
      property.setName(QueryMainBean.QUERY_NAME_PROPERTY);
      property.getValue().add("%" + filterByName + "%");
      filter.getProperty().add(property);
    }
    filter.getOutputProperty().add(QueryMainBean.QUERY_NAME_PROPERTY);
    filter.getOutputProperty().add(QueryMainBean.QUERY_DESCRIPTION_PROPERTY);
    filter.getOutputProperty().add(QueryMainBean.QUERY_UPDATE_PROPERTY);
    OrderByProperty orderProperty = new OrderByProperty();
    orderProperty.setName(QueryMainBean.QUERY_NAME_PROPERTY);
    filter.getOrderByProperty().add(orderProperty);

    if (!StringUtils.isBlank(filterByTitle))
    {
      filter.setTitle("%" + filterByTitle + "%");
    }
    
    if (!StringUtils.isBlank(getFilterByScope()))
    {
      Property property = new Property();
      property.setName(QueryMainBean.QUERY_SCOPE_PROPERTY);
      property.getValue().add(getFilterByScope());
      filter.getProperty().add(property);
    }

    if (!StringUtils.isBlank(filterByObject))
    {
      Property property = new Property();
      property.setName(QueryMainBean.QUERY_OBJECT_PROPERTY);
      property.getValue().add(filterByObject);
      filter.getProperty().add(property);
    }

    if (!StringUtils.isBlank(filterByType))
    {
      Property property = new Property();
      property.setName(QueryMainBean.QUERY_TYPE_PROPERTY);
      property.getValue().add(filterByType);
      filter.getProperty().add(property);
    }

    if (!StringUtils.isBlank(filterByBase))
    {
      Property property = new Property();
      property.setName(QueryMainBean.QUERY_BASE_PROPERTY);
      property.getValue().add(filterByBase);
      filter.getProperty().add(property);
    }

    firstRow = 0;
    documents = QueryMainBean.getDocumentManagerClient().findDocuments(filter);    
  }  
  
}
