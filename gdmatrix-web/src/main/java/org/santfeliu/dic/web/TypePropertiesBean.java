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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryManagerPort;
import org.matrix.dic.EnumType;
import org.matrix.dic.PropertyDefinition;
import org.santfeliu.dic.Type;

/**
 *
 * @author realor
 */
public class TypePropertiesBean extends PropertiesPageBean
{  
  private Map<String, List<PropertyDefinition>> propertyDefinitionsMap;
  private boolean renderInheritedProperties = true;

  @Override
  public String show()
  {
    return "type_properties";
  }

  public List<Type> getSuperTypes()
  {
    propertyDefinitionsMap = new HashMap();

    TypeMainBean typeMainBean = (TypeMainBean)getBean("typeMainBean");
    List<Type> superTypes = typeMainBean.getSuperTypes();
    for (Type superType : superTypes)
    {
      List<PropertyDefinition> pdfs = superType.getPropertyDefinition();
      propertyDefinitionsMap.put(superType.getTypeId(), pdfs);
    }

    return superTypes;
  }

  public boolean isRenderSuperTypes()
  {
    return !getSuperTypes().isEmpty();
  }

  public List<PropertyDefinition> getSuperTypePropertyDefinitions()
  {
    List<PropertyDefinition> result = new ArrayList();
    Type type = (Type)getExternalContext().getRequestMap().get("type");
    if (propertyDefinitionsMap != null)
      result = propertyDefinitionsMap.get(type.getTypeId());

    return result;
  }

  public boolean isInheritedPropertiesRendered()
  {
    return renderInheritedProperties;
  }

  public void setInheritedPropertiesRendered(boolean renderInheritedProperties)
  {
    this.renderInheritedProperties = renderInheritedProperties;
  }

  public String changeRenderInheritedProperties()
  {
    this.renderInheritedProperties = !this.renderInheritedProperties;
    return "type_properties";
  }

  public String getEnumTypeId()
  {
    PropertyDefinition propertyDefinition =
      editingPropertyDefinitionItem.getPropertyDefinition();
    return propertyDefinition.getEnumTypeId();
  }

  public void setEnumTypeId(String enumTypeId)
  {
    PropertyDefinition propertyDefinition =
      editingPropertyDefinitionItem.getPropertyDefinition();
    propertyDefinition.setEnumTypeId(enumTypeId);
    if (!StringUtils.isBlank(enumTypeId))
    {
      try
      {
        DictionaryManagerPort port = DictionaryConfigBean.getPort();
        EnumType enumType = port.loadEnumType(enumTypeId);
        propertyDefinition.setType(enumType.getItemType());
      }
      catch (Exception ex)
      {
      }
    }
  }

  public String getTypeIcon()
  {
    String icon = null;
    Object rowObject = getValue("#{row}");
    if (rowObject != null)
    {
      PropertyDefinition row = null;
      if (rowObject instanceof PropertyDefinition)
      {
        row = (PropertyDefinition)rowObject;
      }
      else if (rowObject instanceof PropertyDefinitionItem)
      {
        row = ((PropertyDefinitionItem)rowObject).getPropertyDefinition();
      }
      if (row != null)
      {
        if (row.getType() != null)
        {
          switch (row.getType())
          {
            case TEXT: icon = "text_type"; break;
            case NUMERIC: icon = "numeric_type"; break;
            case BOOLEAN: icon = "boolean_type"; break;
            case DATE: icon = "date_type"; break;
          }
        }
      }
    }
    return icon;
  }

  public boolean isOverrided()
  {
    boolean overrided = false;
    PropertyDefinition superPd = (PropertyDefinition)getValue("#{row}");
    String propertyName = superPd.getName();

    int i = 0;
    while (i < getRows().size() && !overrided)
    {
      overrided = propertyName.equals(
        getRows().get(i).getPropertyDefinition().getName());
      i++;
    }
    return overrided;
  }

  public String overrideProperty()
  {
    PropertyDefinition superPd = (PropertyDefinition)getValue("#{row}");
    addPropertyDefinition();
    PropertyDefinition newPd =
      editingPropertyDefinitionItem.getPropertyDefinition();
    newPd.setName(superPd.getName());
    newPd.setDescription(superPd.getDescription());
    newPd.setType(superPd.getType());
    newPd.setMinOccurs(superPd.getMinOccurs());
    newPd.setMaxOccurs(superPd.getMaxOccurs());
    newPd.setSize(superPd.getSize());
    newPd.getValue().addAll(superPd.getValue());
    newPd.setReadOnly(superPd.isReadOnly());
    newPd.setHidden(superPd.isHidden());
    return null;
  }

  public String showType()
  {
    Type type = (Type)getValue("#{type}");
    return getControllerBean().showObject("Type", type.getTypeId());
  }

  public String searchEnumType()
  {
    return getControllerBean().searchObject("EnumType",
      "#{typePropertiesBean.enumTypeId}");
  }

  public String showEnumType()
  {
    return getControllerBean().showObject("EnumType", getEnumTypeId());
  }

  @Override
  protected List<PropertyDefinition> getMainPropertyDefinitionList()
  {
    TypeMainBean typeMainBean = (TypeMainBean)getBean("typeMainBean");
    return typeMainBean.getType().getPropertyDefinition();
  }
}
