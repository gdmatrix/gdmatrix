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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.DictionaryManagerPort;
import org.matrix.dic.DictionaryManagerService;
import org.matrix.dic.EnumTypeItem;
import org.matrix.dic.EnumTypeItemFilter;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.dic.Type;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author realor
 */
public class DictionaryConfigBean implements Serializable
{
  public static DictionaryManagerPort getPort() throws Exception
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint =
      wsDirectory.getEndpoint(DictionaryManagerService.class);
    return endpoint.getPort(DictionaryManagerPort.class,
      UserSessionBean.getCurrentInstance().getUserId(),
      UserSessionBean.getCurrentInstance().getPassword());
  }

  public static List<SelectItem> getDerivedTypesSelectItems(String typeId)
  {
    ArrayList<SelectItem> items = new ArrayList();
    org.santfeliu.dic.TypeCache typeCache =
      org.santfeliu.dic.TypeCache.getInstance();
    List<String> derivedTypeIds = typeCache.getDerivedTypeIds(typeId);
    if (derivedTypeIds != null)
    {
      for (String derivedTypeId : derivedTypeIds)
      {
        Type derivedType = typeCache.getType(derivedTypeId);
        if (derivedType.canPerformAction("Read", UserSessionBean.getCurrentInstance().getRoles()))
        {
          SelectItem item = new SelectItem();
          item.setLabel(derivedType.getDescription());
          item.setValue(derivedTypeId);
          items.add(item);
        }
      }
    }

    return items;
  }

  public static List<SelectItem> getActionSelectItems(String typeId)
  {
    ArrayList<SelectItem> items = new ArrayList();
    org.santfeliu.dic.TypeCache typeCache =
      org.santfeliu.dic.TypeCache.getInstance();
    
    FacesContext context = FacesContext.getCurrentInstance();
    Locale locale = context.getViewRoot().getLocale();
    String bundleName = context.getApplication().getMessageBundle();
    ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);

    // standard actions
    for (String action : DictionaryConstants.standardActions)
    {
      SelectItem item = new SelectItem();
      item.setValue(action);
      item.setLabel(getLocalizedAction(action, bundle));
      items.add(item);
    }
    // specific actions
    for (String action : typeCache.getType(typeId).getActions())
    {
      SelectItem item = new SelectItem();
      item.setValue(action);
      item.setLabel(getLocalizedAction(action, bundle));
      items.add(item);
    }
    return items;
  }
  
  public static List<SelectItem> getEnumTypeSelectItems(String enumTypeId)
  {
    List<SelectItem> result = new ArrayList();
    EnumTypeItemFilter filter = new EnumTypeItemFilter();
    filter.setEnumTypeId(enumTypeId);
    try
    {  
      List<EnumTypeItem> enumItems = getPort().findEnumTypeItems(filter);
      for (EnumTypeItem enumItem : enumItems)
      {
        SelectItem selectItem = 
          new SelectItem(enumItem.getValue(), enumItem.getLabel(), 
            enumItem.getDescription());
        result.add(selectItem);
      }
    } 
    catch (Exception ex) 
    {
      return Collections.EMPTY_LIST;
    }
    return result;
  }   

  public static String getLocalizedAction(String action)
  {
    if (action == null) return null;
    FacesContext context = FacesContext.getCurrentInstance();
    Locale locale = context.getViewRoot().getLocale();
    String bundleName = context.getApplication().getMessageBundle();
    ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);
    return getLocalizedAction(action, bundle);
  }

  private static String getLocalizedAction(String action, ResourceBundle bundle)
  {
    String value;
    try
    {
      value = bundle.getString(action);
    }
    catch (MissingResourceException ex)
    {
      value = action;
    }
    return value;
  }
}
