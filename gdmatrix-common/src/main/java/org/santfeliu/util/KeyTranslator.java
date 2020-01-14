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
package org.santfeliu.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author unknown
 */
public class KeyTranslator
{
  private HashMap translationTable;

  public KeyTranslator()
  {
    translationTable = new HashMap();
  }
  
  public void setKeyTranslation(String key, String newKey)
  { 
    translationTable.put(key, newKey);
  }
  
  public void removeKeyTranslation(String key)
  {
    translationTable.remove(key);
  }
  
  public void clear()
  {
    translationTable.clear();
  }
  
  public Map translate(Map map)
  {
    Set set = map.keySet();
    Object keys[] = set.toArray();
    for (int i = 0; i < keys.length; i++)
    {
      Object key = keys[i];
      Object newKey = translationTable.get(key);
      if (newKey != null)
      {
        Object value = map.remove(key);
        map.put(newKey, value);
      }
    }
    return map;
  }
  
  public Table translate(Table table)
  {
    for (int c = 0 ; c < table.getColumnCount(); c++)
    {
      String columnName = table.getColumnName(c);
      Object newKey = translationTable.get(columnName);
      if (newKey != null)
      {
        String newColumnName = String.valueOf(newKey);
        table.setColumnName(c, newColumnName);
      }
    }
    return table;
  }
  
  public KeyTranslator getInverseTranslator()
  {
    KeyTranslator tr = new KeyTranslator();
    Set set = translationTable.keySet();
    Object keys[] = set.toArray();
    for (int i = 0; i < keys.length; i++)
    {
      Object key = keys[i];
      Object newKey = translationTable.get(key);
      if (newKey != null)
      {
        tr.translationTable.put(newKey, key);
      }
    }
    return tr;
  }
  
  public static void main(String args[])
  {
    try
    {
      KeyTranslator tr = new KeyTranslator();
      tr.setKeyTranslation("username", "usrcod");
      tr.setKeyTranslation("password", "usrpass");
      Map map = new HashMap();
      map.put("username", "realor");
      map.put("password", "cccdsff");
      System.out.println(map);    
      tr.translate(map);
      System.out.println(map);
      Table table = new Table("username", "password");
      table.addRow("realor", "dsdfsdfsd");
      table.addRow("leongc", "ssvbvfffd");
      System.out.println(table);
      tr.translate(table);
      System.out.println(table);   
      tr.getInverseTranslator().translate(table);
      System.out.println(table);   
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
