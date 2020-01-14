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

import java.util.List;
import javax.faces.model.SelectItem;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.Property;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.OrderByProperty;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.dic.web.DictionaryConfigBean;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.faces.beansaver.Savable;

/**
 *
 * @author realor
 */
public class QuerySearchBean extends FacesBean implements Savable
{
  private String filterByName;
  private String filterByTitle;  
  private String filterByScope;
  private String filterByType;
  private String filterByObject;
  private transient List<SelectItem> queryScopeSelectItems;
  private transient List<SelectItem> queryObjectSelectItems;
  private transient List<SelectItem> queryTypeSelectItems;  
  private List<Document> documents;

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

  public String getFilterByScope() 
  {
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
      queryScopeSelectItems = 
        DictionaryConfigBean.getEnumTypeSelectItems(QueryBean.QUERY_SCOPE_TYPEID);
    }
    return queryScopeSelectItems;
  }

  public List<SelectItem> getQueryTypeSelectItems()
  {
    if (queryTypeSelectItems == null)
    {
      queryTypeSelectItems = 
        DictionaryConfigBean.getEnumTypeSelectItems(QueryBean.QUERY_TYPE_TYPEID);
    }
    return queryTypeSelectItems;
  }

  public List<SelectItem> getQueryObjectSelectItems()
  {
    if (queryObjectSelectItems == null)
    {
      queryObjectSelectItems = 
        DictionaryConfigBean.getEnumTypeSelectItems(QueryBean.QUERY_OBJECT_TYPEID);
    }
    return queryObjectSelectItems;
  }
  
  public void search()
  {
    DocumentFilter filter = new DocumentFilter();
    filter.setFirstResult(0);
    filter.setDocTypeId(QueryBean.QUERY_TYPEID);
    if (!StringUtils.isBlank(filterByName))
    {
      Property property = new Property();
      property.setName(QueryBean.QUERY_NAME_PROPERTY);
      property.getValue().add("%" + filterByName + "%");
      filter.getProperty().add(property);
    }
    filter.getOutputProperty().add(QueryBean.QUERY_NAME_PROPERTY);
    filter.getOutputProperty().add(QueryBean.QUERY_DESCRIPTION_PROPERTY);
    filter.getOutputProperty().add(QueryBean.QUERY_UPDATE_PROPERTY);
    OrderByProperty orderProperty = new OrderByProperty();
    orderProperty.setName(QueryBean.QUERY_NAME_PROPERTY);
    filter.getOrderByProperty().add(orderProperty);

    if (!StringUtils.isBlank(filterByTitle))
    {
      filter.setTitle("%" + filterByTitle + "%");
    }
    
    if (!StringUtils.isBlank(filterByScope))
    {
      Property property = new Property();
      property.setName(QueryBean.QUERY_SCOPE_PROPERTY);
      property.getValue().add(filterByScope);
      filter.getProperty().add(property);
    }

    if (!StringUtils.isBlank(filterByObject))
    {
      Property property = new Property();
      property.setName(QueryBean.QUERY_OBJECT_PROPERTY);
      property.getValue().add(filterByObject);
      filter.getProperty().add(property);
    }

    if (!StringUtils.isBlank(filterByType))
    {
      Property property = new Property();
      property.setName(QueryBean.QUERY_TYPE_PROPERTY);
      property.getValue().add(filterByType);
      filter.getProperty().add(property);
    }
    documents = QueryBean.getDocumentManagerClient().findDocuments(filter);
  }
  
  public List<Document> getDocuments()
  {
    return documents;
  }

  public void clearDocuments()
  {
    documents = null;
  }
  
  public String getQueryName()
  {
    Document document = (Document)getValue("#{document}");
    Property property = DictionaryUtils.getProperty(document, 
      QueryBean.QUERY_NAME_PROPERTY);
    return property == null ? null : property.getValue().get(0);
  }

  public String getQueryDescription()
  {
    Document document = (Document)getValue("#{document}");
    Property property = DictionaryUtils.getProperty(document, 
      QueryBean.QUERY_DESCRIPTION_PROPERTY);
    return property == null ? null : property.getValue().get(0);
  }
  
  public boolean isQueryUpdate()
  {
    Document document = (Document)getValue("#{document}");
    Property property = DictionaryUtils.getProperty(document, 
      QueryBean.QUERY_UPDATE_PROPERTY);
    return property == null ? false : Boolean.valueOf(property.getValue().get(0));
  }
  
  public String show()
  {
    if (documents == null)
    {
      search();
    }
    return "query_search";
  }

  public String showQuery()
  {
    try
    {
      String queryName = getQueryName();
      QueryBean queryBean = (QueryBean)getBean("queryBean");
      queryBean.loadQuery(queryName);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "query_instance";
  }
}
