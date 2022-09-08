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
package org.matrix.pf.web;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;


/**
 *
 * @author blanquepa
 * @param <T>
 */
public class SelectEntityList<T> implements Serializable
{
  private String idFieldName; 
  private List<SelectEntity<T>> entities;
  
  public SelectEntityList(String idFieldName)
  {
    this(new ArrayList<>(), idFieldName);
  }
    
  public SelectEntityList(List<T> list, String idFieldName)
  {
    this.idFieldName = idFieldName;
    this.entities = new ArrayList<>();

    for (T item : list)
    {
      //if is a SelectEntity list then transform entities to original. 
      if (item instanceof SelectEntity)
        item = (T)((SelectEntity)item).getOriginal();
      
      if (item != null)
      {
        Class itemClass = item.getClass();
        try
        {
          Method idGetter = 
            itemClass.getMethod("get" + StringUtils.capitalize(idFieldName));
          Object id = idGetter.invoke(item);
          entities.add(new SelectEntity(String.valueOf(id), item));
        }
        catch (IllegalAccessException | IllegalArgumentException |
          SecurityException | NoSuchMethodException | 
          InvocationTargetException ex)
        {
          //Continue with next item
        }
      }
    }    
  }
  
  public List<SelectEntity<T>> getEntities()
  {
    return entities;
  }
  
  public List<T> getOriginals()
  {
    List<T> result = new ArrayList<>();
    for (SelectEntity<T> entity : entities)
    {
      result.add(entity.getOriginal());
    }
    return result;
  }
  
  public SelectEntityList<T> findEntities(List<String> ids)
  {
    List<T> result = new ArrayList<>();
    for (String id : ids)
    {
      SelectEntity<T> entity = findEntityById(id);
      result.add(entity.getOriginal());        
    } 
    return new SelectEntityList<>(result, idFieldName);
  }  
  
  public SelectEntity findEntityById(String id)
  {
    boolean found = false;
    SelectEntity entity = null;
    Iterator<SelectEntity<T>> iter = entities.iterator();
    while (!found && iter.hasNext())
    {
      entity = iter.next();
      if (entity.getId().equals(id)) found = true;
    }
    return found ? entity : null;
  }    
  
  public List<String> getIds()
  {
    List<String> idList = new ArrayList<>();
    for (SelectEntity entity : entities)
    {
      String id = entity.getId();
      if (!idList.contains(id))
      {
        idList.add(id);
      }
    }   
    return idList;
  }
  
  public boolean contains(SelectEntity<T> entity)
  {
    return entities.contains(entity);
  }
  
  public boolean add(String id, T item)
  {
    return add(new SelectEntity(id, item));
  }
  
  public boolean add(SelectEntity<T> entity)
  {
    if (entities == null)
      entities = new ArrayList<>();
    return entities.add(entity);
  }
    
  public void clear()
  {
    entities.clear();
  }
      
}
