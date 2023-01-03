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
package org.santfeliu.webapp.modules.dic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Type;
import org.matrix.dic.TypeFilter;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.webapp.TypeBean;


/**
 *
 * @author blanquepa
 */
@Named
@ApplicationScoped
public class TypeTypeBean extends TypeBean<Type, TypeFilter>
{    
  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.TYPE_TYPE;
  }

  @Override
  public String describe(Type type)
  {
    return type.getDescription();
  }

  @Override
  public Type loadObject(String objectId)
  {
    return TypeCache.getInstance().getType(objectId);
  }

  @Override
  public TypeFilter queryToFilter(String query, String typeId)
  {
    if (query == null) query = "";
    
    TypeFilter filter = new TypeFilter();
    
    if (query.contains(":")) 
    {    
      query = query.substring(query.indexOf(":") + 1, query.length());
      if (!query.endsWith("%")) query += "%";       
      filter.setTypePath("%/" + query + "/%");
    }
    else
    {
      if (!query.startsWith("%")) query = "%" + query;
      if (!query.endsWith("%")) query += "%"; 
      filter.setDescription(query);      
    }

    return filter;
  }

  @Override
  public String filterToQuery(TypeFilter filter)
  {
    String query = "";

    if (filter.getTypePath() != null)
    {
      query = filter.getTypePath();
      query = query.substring(2, query.length() - 3);
    }
    else if (filter.getDescription() != null)
    {
      query = filter.getDescription();
      query = query.substring(0, query.length() - 1);
    }
    return query;
  }

  @Override
  public List<Type> find(TypeFilter filter)
  {
    List<Type> types = new ArrayList();    
    try
    {
      if (filter.getTypeId() != null)
        types.add(TypeCache.getInstance().getType(filter.getTypeId()));
      else if (filter.getSuperTypeId() != null)
        types.addAll(getFromTypeCache(
          filter.getSuperTypeId(), filter.getMaxResults()));
      else
        types.addAll(DicModuleBean.getPort(true).findTypes(filter));
    }
    catch (Exception ex)
    {
      return Collections.EMPTY_LIST;
    }
    return types;
  }

  private List<Type> getFromTypeCache(String superTypeId, int maxResults)
  {
    List<Type> results = new ArrayList();
    org.santfeliu.dic.Type type = TypeCache.getInstance().getType(superTypeId);
    if (type != null)
    {
      if (type.isInstantiable())
        results.add(type);
      
      List<org.santfeliu.dic.Type> derived = type.getDerivedTypes();
      if (maxResults == 0)
        maxResults = derived.size();

      for (int i = 0; i < maxResults; i++)
      {
        Type child = derived.get(i);
        if (child.isInstantiable())
          results.add(derived.get(i));
      }
    }  
    return results;
  }
  
  public static void main(String[] args)
  {
    TypeTypeBean bean = new TypeTypeBean();
    String query = "sf:Familia"; 
    List<Type> list = bean.find(query);
    for (Type item : list)
    {
      System.out.println(item.getDescription());
    }
  }
      
}
