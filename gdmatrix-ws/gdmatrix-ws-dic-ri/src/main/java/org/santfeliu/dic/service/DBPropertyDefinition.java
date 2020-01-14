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
package org.santfeliu.dic.service;

import org.matrix.dic.PropertyDefinition;
import org.matrix.dic.PropertyType;
import org.santfeliu.jpa.JPAUtils;

/**
 *
 * @author realor
 */
public class DBPropertyDefinition extends PropertyDefinition
{
  private String typeId;
  private String typeValue;
  private String hiddenValue;
  private String readOnlyValue;
  private String defaultValue;

  public DBPropertyDefinition()
  {
  }

  public String getTypeId()
  {
    return typeId;
  }

  public void setTypeId(String typeId)
  {
    this.typeId = typeId;
  }

  public String getTypeValue()
  {
    return typeValue;
  }

  public void setTypeValue(String typeValue)
  {
    this.typeValue = typeValue;
  }

  public String getHiddenValue()
  {
    return hiddenValue;
  }

  public void setHiddenValue(String hiddenValue)
  {
    this.hiddenValue = hiddenValue;
  }

  public String getDefaultValue()
  {
    if (this.getValue().size() > 0)
      return this.getValue().get(0);
    else
      return null;
  }

  public void setDefaultValue(String defaultValue)
  {
    this.getValue().clear();
    if (defaultValue != null)
      this.getValue().add(defaultValue);
  }

  public String getReadOnlyValue()
  {
    return readOnlyValue;
  }

  public void setReadOnlyValue(String readOnlyValue)
  {
    this.readOnlyValue = readOnlyValue;
  }

  public DBPropertyDefinition(PropertyDefinition propertyDefinition)
  {
    copyFrom(propertyDefinition);
  }

  public DBPropertyDefinitionPK getPrimaryKey()
  {
    DBPropertyDefinitionPK pk = new DBPropertyDefinitionPK();
    pk.setTypeId(typeId);
    pk.setName(name);
    return pk;
  }

  public void copyTo(PropertyDefinition propertyDefinition)
  {
    JPAUtils.copy(this, propertyDefinition);
    if ("T".equals(typeValue))
      propertyDefinition.setType(PropertyType.TEXT);
    else if ("N".equals(typeValue))
      propertyDefinition.setType(PropertyType.NUMERIC);
    else if ("B".equals(typeValue))
      propertyDefinition.setType(PropertyType.BOOLEAN);
    else if ("D".equals(typeValue))
      propertyDefinition.setType(PropertyType.DATE);
    else if ("S".equals(typeValue))
      propertyDefinition.setType(PropertyType.STRUCT);
    propertyDefinition.setHidden("T".equals(hiddenValue));
    propertyDefinition.setReadOnly("T".equals(readOnlyValue));
    propertyDefinition.getValue().clear();
    if (getValue().size() > 0)
      propertyDefinition.getValue().addAll(getValue());
  }

  public void copyFrom(PropertyDefinition propertyDefinition)
  {
    JPAUtils.copy(propertyDefinition, this);
    PropertyType propertyType = propertyDefinition.getType();
    if (propertyType == null) propertyType = PropertyType.TEXT;
    switch (propertyType)
    {
      case TEXT: typeValue = "T"; break;
      case NUMERIC: typeValue = "N"; break;
      case BOOLEAN: typeValue = "B"; break;
      case DATE: typeValue = "D"; break;
      case STRUCT: typeValue = "S"; break;
    }
    hiddenValue = propertyDefinition.isHidden() ? "T" : "F";
    readOnlyValue = propertyDefinition.isReadOnly() ? "T" : "F";
    this.getValue().clear();
    if (propertyDefinition.getValue().size() > 0)
      this.getValue().addAll(propertyDefinition.getValue());
  }
}
