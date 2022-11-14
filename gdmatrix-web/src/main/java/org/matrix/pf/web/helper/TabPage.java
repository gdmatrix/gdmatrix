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

import java.util.List;
import org.matrix.pf.web.ObjectBacking;

/**
 *
 * @author blanquepa
 */
public interface TabPage extends Page
{

  
  /**
   * Responsible of retrieve page data and other data related actions like 
   * preload/postload actions. By default invokes only load().
   */
  public default void populate()
  {
    ObjectBacking objectBacking = getObjectBacking();
    if (objectBacking != null && !objectBacking.isNew() 
      || objectBacking == null)
    {
      load();
    }
  }
  
  /**
   * Action invoked to create an instance of the object wrapped in the page.
   */
  public void create();
  
  /**
   * Responsible to retrieve the data from a source. 
   * 
   */
  public void load();    
  
  /**
   * Action invoked for the page to be shown.
   * @return Outcome 
   */
  public String show();
  
  /**
   * Action invoked when save button is pressed
   * @return 
   */
  public String store(); 
  
  /**
   * Action invoked when cancel button is pressed
   * @return 
   */  
  public String cancel();
  
  /**
   * Action invoked to reset completely tab data
   */
  public void reset();
  
  public TabHelper getTabHelper();
  
  @Override
  public default String getProperty(String name)
  {
    return getTabHelper().getProperty(name);
  }  
  
  @Override
  public default List<String> getMultivaluedProperty(String name)
  {
    return getTabHelper().getMultivaluedProperty(name);
  }    
}
