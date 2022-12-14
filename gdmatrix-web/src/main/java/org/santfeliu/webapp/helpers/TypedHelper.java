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
package org.santfeliu.webapp.helpers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.faces.model.SelectItem;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.PropertyDefinition;
import org.matrix.web.WebUtils;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.web.TypeBean;
import org.santfeliu.webapp.BaseBean;

/**
 *
 * @author blanquepa
 */
public abstract class TypedHelper extends BaseBean
  implements Serializable
{
  public static final String TYPEID_SEPARATOR = ";";
  
  private static final String TYPE_BEAN = "typeBean";

  protected final BaseBean baseBean;

  public TypedHelper(BaseBean baseBean)
  {
    this.baseBean = baseBean;
  }
  
  public abstract String getTypeId();
  
  public abstract String getAdminRole();
  
  public List<Type> getAllTypes()
  {
    TypeCache tc = TypeCache.getInstance();
    List<Type> types = new ArrayList<>();
    List<SelectItem> items = getAllTypeItems();
    for (SelectItem i : items)
    {
      types.add(tc.getType(String.valueOf(i.getValue())));
    }
    return types;
  }

  public List<SelectItem> getAllTypeItems()
  {
    return getAllTypeItems(DictionaryConstants.READ_ACTION,
      DictionaryConstants.CREATE_ACTION, DictionaryConstants.WRITE_ACTION);
  }

  public boolean isPropertyHidden(String propName)
  {
    PropertyDefinition pd = getPropertyDefinition(getTypeId(), propName);     
    return isPropertyHidden(pd);
  }
    
  public boolean isPropertyHidden(PropertyDefinition pd)
  {
    if (pd == null)
      return true;
    
    String propName = pd.getName();
    propName = "render" + StringUtils.capitalize(propName);
    
    String value = getNodeProperty(propName);
    if (value != null)
      return !Boolean.parseBoolean(value);

    return pd.isHidden(); 
  }
  
  public String getPropertyLabel(String propName, String altName)
  {
    PropertyDefinition pd
      = getPropertyDefinition(getTypeId(), propName);

    return pd != null ? pd.getDescription() : altName;
  }

  /**
   * Get property from dictionary definition.
   * @param name
   * @return Property value.
   */
  @Override
  public String getProperty(String name)
  {
    String value = getNodeProperty(name);
    
    if (value == null)
      value = getFirstPropertyDefinitionValue(name);
    
    return value;
  }
  
  /**
   * Get multi-valued property from dictionary definition.
   * @param name
   * @return List of values.
   */
  public List<String> getMultivaluedProperty(String name)
  {
    List<String> values = getMultivaluedNodeProperty(name);
    
    if (values == null || values.isEmpty())
      values = getPropertyDefinitionValue(name);

    return values;
  }  

  // Private methods 
  
  private String getNodeProperty(String propName)
  {
    String value;
//    TODO
//    if (baseBean instanceof TabBean)
//      value = ((TabPage)backing).getTabHelper().getProperty(propName);
//    else
      value = WebUtils.getMenuItemProperty(propName); 
    
    return value;
  }
  
  private List<String> getMultivaluedNodeProperty(String propName)
  {
    List<String> values;
//    TODO:
//    if (baseBean instanceof TabBean)
//      values = ((TabPage)backing).getTabHelper()
//        .getMultivaluedProperty(propName);
//    else
      values = WebUtils.getMultivaluedMenuItemProperty(propName);
    return values;
  }
   
  private String getFirstPropertyDefinitionValue(String propName)
  {
    List<String> values = getPropertyDefinitionValue(propName);
    return !values.isEmpty() ? values.get(0) : null;
  }  
  
  private List<String> getPropertyDefinitionValue(String propName)
  {
    PropertyDefinition pd
      = getPropertyDefinition(getTypeId(), propName);
    if (pd != null)
    {
      List<String> values = pd.getValue();
      if (values != null && values.size() == 1)
      {
        String value = values.get(0);
        if (value != null && value.contains(";"))
        {
          String[] parts = value.split(";");
          return Arrays.asList(parts);
        }
      }
      
      return values;
    }
    else
    {
      return Collections.emptyList();
    }
  }

  private PropertyDefinition getPropertyDefinition(String typeId,
    String propName)
  {
    Type type = TypeCache.getInstance().getType(typeId);
    if (type != null)
    {
      List<PropertyDefinition> pds = type.getPropertyDefinition();
      for (PropertyDefinition pd : pds)
      {
        if (pd.getName().equals(propName))
        {
          return pd;
        }
      }
      String superTypeId = type.getSuperTypeId();
      if (superTypeId != null)
      {
        return getPropertyDefinition(superTypeId, propName);
      }
    }
    return null;
  }

  private List<SelectItem> getAllTypeItems(String... actions)
  {
    try
    {
      String typeId = getTypeId();
      TypeBean typeBean = WebUtils.getBacking(TYPE_BEAN);
      String adminRole = getAdminRole();
      return typeBean.getAllSelectItems(typeId, adminRole, actions, true);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return Collections.emptyList();
  }


}
