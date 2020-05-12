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
package org.santfeliu.dic.web;

import java.util.List;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.DictionaryManagerPort;
import org.matrix.dic.Type;
import org.matrix.dic.TypeFilter;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.obj.BasicSearchBean;

/**
 *
 * @author realor
 */
@CMSManagedBean
public class TypeSearchBean extends BasicSearchBean
{
  private TypeFilter filter = new TypeFilter();
  private String rootTypeId;

  public TypeFilter getFilter()
  {
    return filter;
  }

  public void setFilter(TypeFilter filter)
  {
    this.filter = filter;
  }

  public String getRootTypeId()
  {
    return rootTypeId;
  }

  public void setRootTypeId(String rootTypeId)
  {
    this.rootTypeId = rootTypeId;
  }

  @Override
  public int countResults()
  {
    try
    {
      String typePath = (rootTypeId != null && rootTypeId.trim().length() > 0) ?
       DictionaryConstants.TYPE_PATH_SEPARATOR + rootTypeId +
       DictionaryConstants.TYPE_PATH_SEPARATOR + "%" : null;
      filter.setTypePath(typePath);
      DictionaryManagerPort port = DictionaryConfigBean.getPort();
      return port.countTypes(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return 0;
  }

  @Override
  public List getResults(int firstResult, int maxResults)
  {
    try
    {
      String typePath = (rootTypeId != null && rootTypeId.trim().length() > 0) ?
       DictionaryConstants.TYPE_PATH_SEPARATOR + rootTypeId +
       DictionaryConstants.TYPE_PATH_SEPARATOR + "%" : null;
      filter.setTypePath(typePath);
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      DictionaryManagerPort port = DictionaryConfigBean.getPort();
      return port.findTypes(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  @Override
  @CMSAction
  public String show()
  {
    return "type_search";
  }

  public boolean isLeafType()
  {
    Type row = (Type)getValue("#{row}");
    TypeCache cache = TypeCache.getInstance();
    org.santfeliu.dic.Type type = cache.getType(row.getTypeId());
    if (type != null) return type.isLeaf();
    return false;
  }

  public String findDerivedTypes()
  {
    Type row = (Type)getValue("#{row}");
    String typeId = row.getTypeId();
    filter.setSuperTypeId(typeId);
    filter.setTypeId(null);
    filter.setDescription(null);
    return search();
  }

  public String showInTree()
  {
    Type row = (Type)getValue("#{row}");
    String typeId = row.getTypeId();
    TypeBean typeBean = (TypeBean)getBean("typeBean");
    typeBean.setObjectId(typeId);
    TypeTreeBean typeTreeBean = (TypeTreeBean)getBean("typeTreeBean");
    return typeTreeBean.expandType(typeId);
  }

  public String findDerivedTypesFromPath()
  {
    String typeId = (String)getValue("#{typeId}");
    filter.setSuperTypeId(typeId);
    filter.setTypeId(null);
    filter.setDescription(null);
    return search();
  }

  public boolean isNavigationEnabled()
  {
    String superTypeId = filter.getSuperTypeId();
    return (superTypeId != null && superTypeId.trim().length() > 0 &&
      !superTypeId.equals("-"));
  }

  public List<String> getTypePathList()
  {
    TypeCache cache = TypeCache.getInstance();
    org.santfeliu.dic.Type superType = cache.getType(filter.getSuperTypeId());
    if (superType != null) return superType.getTypePathList();
    return null;
  }

  public String showType()
  {
    return getControllerBean().showObject("Type",
      (String)getValue("#{row.typeId}"));
  }

  public String selectType()
  {
    Type row = (Type)getValue("#{row}");
    String typeId = row.getTypeId();
    return getControllerBean().select(typeId);
  }
}
