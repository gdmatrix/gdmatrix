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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.faces.model.SelectItem;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.PropertyDefinition;
import org.matrix.pf.web.WebBacking;
import org.matrix.web.WebUtils;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.web.TypeBean;

/**
 *
 * @author blanquepa
 */
public class TypedHelper extends WebBacking
  implements Serializable
{
  public static final String TYPEID_SEPARATOR = ";";
  
  private static final String TYPE_BEAN = "typeBean";

  protected final Typed backing;

  public TypedHelper(Typed backing)
  {
    this.backing = backing;
  }

  public String getTypeId()
  {
    String objectTypeId = backing.getTypeId();
    return (objectTypeId != null && !objectTypeId.contains(TYPEID_SEPARATOR)) ?
      objectTypeId : backing.getRootTypeId();
  }

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
    return isPropertyHidden(getTypeId(), propName);
  }
  

  /**
   * @param propName: render property name.
   * @return  true if property is defined in CMS node and hasn't 'false' value
   * or if is defined in type as PropertyDefinitions with value not 'false'.
   */
  @Override
  public boolean render(String propName)
  {
    propName = "render" + StringUtils.capitalize(propName);
    String value = getFirstPropertyDefinitionValue(propName);
    boolean defValue = (value == null || !value.equalsIgnoreCase("false"));
    return super.render(propName, defValue);
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
    return getFirstPropertyDefinitionValue(name);
  }
  
  /**
   * Get property from current node or dictionary definition.
   * @param name
   * @return List of values.
   */
  @Override
  public List<String> getMultivaluedProperty(String name)
  {
    List<String> values = super.getMultivaluedProperty(name);
    if (values == null || values.isEmpty())
    {
      values = getPropertyDefinitionValue(name);
    }
    return values;
  }  

  // Private methods  
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
      return pd.getValue();
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

  private boolean isPropertyHidden(String typeId, String propName)
  {
    PropertyDefinition pd = getPropertyDefinition(typeId, propName);
    if (pd != null)
    {
      return pd.isHidden();
    }
    else
    {
      return false;
    }
  }

  private List<SelectItem> getAllTypeItems(String... actions)
  {
    try
    {
      TypeBean typeBean = WebUtils.getBacking(TYPE_BEAN);
      if (!backing.getRootTypeId().equals(backing.getTypeId()))
      {
        return typeBean.getAllSelectItems(getTypeId(),
          backing.getAdminRole(), actions, true);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return Collections.emptyList();
  }

}
