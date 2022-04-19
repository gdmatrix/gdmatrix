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
package org.matrix.web;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import org.santfeliu.faces.menu.model.MenuItemCursor;

/**
 *
 * @author blanquepa
 */
public class WebUtils
{ 
  public static final String OBJECT_TYPEID_PROPERTY = "objectTypeId";  
  public static final String TOPWEB_PROPERTY = "topweb";

  public static <T> T getInstance(Class<?> clazz)
  {
    String backingName = clazz.getAnnotation(Named.class).value();
    return getBacking(backingName);
  }
  
  public static <T> T getBacking(String name)
  {
    return (T) evaluateExpression("#{" + name + "}");      
  }
  
  public static <T> T getBacking(MenuItemCursor mic)
  {
    return getBackingFromAction(mic.getAction());
  }
  
  public static <T> T getBackingFromAction(String actionExpression)
  {
    String backingName = getBackingName(actionExpression);
    if (backingName != null)
      return getBacking(backingName);      
    else
      return null;
  } 
  
  private static String getBackingName(String action)
  {
    if (action != null)
    {
      Pattern pattern = Pattern.compile("#\\{(\\w*)\\..*\\}");
      Matcher matcher = pattern.matcher(action);
      if (matcher.find())
      {
        return matcher.group(1);
      }
    } 
    return null;     
  }  
  
  public static <T> T evaluateExpression(String expr)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Application application = context.getApplication();
    return (T) application
      .evaluateExpressionGet(context, expr, Object.class);  
  }  
       
  public static MenuItemCursor getTopWebMenuItem(MenuItemCursor menuItem)
  {
    MenuItemCursor auxMenuItem = menuItem.getClone();
    
    while (!auxMenuItem.isRoot() && 
      !(auxMenuItem.getDirectProperty(TOPWEB_PROPERTY)!= null &&
      auxMenuItem.getDirectProperty(TOPWEB_PROPERTY).equals("true")))
    {
      auxMenuItem.moveParent();
    }   
    
    return auxMenuItem;
  }    

}
