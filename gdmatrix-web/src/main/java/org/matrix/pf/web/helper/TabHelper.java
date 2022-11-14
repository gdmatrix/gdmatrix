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
package org.matrix.pf.web.helper;

import java.io.Serializable;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.matrix.pf.web.ObjectBacking;
import org.matrix.pf.web.Tab;
import org.matrix.pf.web.WebBacking;

/**
 *
 * @author blanquepa
 */
public class TabHelper extends WebBacking
  implements Serializable
{
  private static final String SEPARATOR = "::";
  private static final String PREFIX = "tab";
  
  protected final TabPage backing;  
  
  public TabHelper(TabPage backing)
  {
    this.backing = backing;
  }
  
  public boolean isPropertyHidden(String propName)
  {
    String value = getProperty("render" + StringUtils.capitalize(propName));
    if (value != null)
      return value.equalsIgnoreCase("false");
    else if (backing instanceof TypedTabPage)
    {
      TypedHelper typedHelper = ((TypedTabPage) backing).getTypedHelper();
      return typedHelper.isPropertyHidden(propName);
    }
    else
      return false;
  }
  
  public String getProperty(String name)
  { 
    String result = null;
    int tabIndex = getCurrentTabIndex();
    if (tabIndex >= 0)
    {
      String propName = PREFIX + String.valueOf(tabIndex) + SEPARATOR + name;
    
      String value = getMenuItemProperty(propName);
      if (value != null)
        result = value;
    }  
    
    return result;
  }
  
  public List<String> getMultivaluedProperty(String name)
  { 
    List<String> result = null;
    int tabIndex = getCurrentTabIndex();
    if (tabIndex >= 0)
    {
      String propName = PREFIX + String.valueOf(tabIndex) + SEPARATOR + name;
    
      List<String> value = getMultivaluedMenuItemProperty(propName);
      if (value != null)
        result = value;
    }
    
    return result;
  }  
  
  public int getCurrentTabIndex()
  {
    int index = -1;
    ObjectBacking objectBacking = backing.getObjectBacking();
    if (objectBacking != null)
    {
      Tab currentTab = objectBacking.getCurrentTab();
      if (currentTab != null)
        index = currentTab.getIndex();
    }
    return index;
  }
}
