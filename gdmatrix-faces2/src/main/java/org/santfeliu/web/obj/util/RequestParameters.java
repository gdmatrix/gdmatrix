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
package org.santfeliu.web.obj.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author blanquepa
 */
  public class RequestParameters implements Serializable
  {
    private Map<String, Item> map = new HashMap();

    public void add(Item parameter)
    {
      if (parameter != null)
        map.put(parameter.getName(), parameter);
    }
    
    public void add(String name, String value, String queryString)
    {
      Item item = new Item(name, value, queryString);
      add(item);
    }
    
    public String getParameterValue(String name)
    {
      Item parameter = map.get(name);
      if (parameter != null)
        return parameter.getValue();
      return null;
    }
    
    public List<Item> getList()
    {
      ArrayList<Item> list = new ArrayList();
      list.addAll(map.values());
      return list;
    }
    
    public class Item implements Serializable
    {
      private String name;
      private String value;
      private boolean inURL;

      public Item(String name, String value, boolean inURL)
      {
        this.name = name;
        this.value = value;
        this.inURL = inURL;
      }

      public Item(String name, String value, String queryString)
      {
        this.name = name;
        this.value = value;
        this.inURL = queryString != null && 
          (queryString.contains("?" + name + "=") || 
           queryString.contains("&" + name + "="));      
      }

      public String getName()
      {
        return name;
      }

      public void setName(String name)
      {
        this.name = name;
      }

      public String getValue()
      {
        return value;
      }

      public void setValue(String value)
      {
        this.value = value;
      }

      public boolean isInURL()
      {
        return inURL;
      }

      public void setInURL(boolean inURL)
      {
        this.inURL = inURL;
      }

    }     
     
  }
