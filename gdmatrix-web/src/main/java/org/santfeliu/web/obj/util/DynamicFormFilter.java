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

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.Property;
import org.santfeliu.dic.util.DictionaryUtils;

/**
 *
 * @author blanquepa
 */
public abstract class DynamicFormFilter extends FormFilter
{
  public void setFormProperties(List<Property> properties)
  {
    //Properties get from dynamic form
    if (properties != null)
    {
      for (Property property : properties)
      {
        DictionaryUtils.setProperty(getObjectFilter(), property.getName(), property.getValue());
      }
    }

    //Properties get from default form
    String name1 = getPropertyName1();
    String name2 = getPropertyName2();
    String value1 = getPropertyValue1();
    String value2 = getPropertyValue2();
    if (!StringUtils.isBlank(name1) && !StringUtils.isBlank(name2)
      && name1.equals(name2))
    {
      List values =
        Arrays.asList(new String[]{value1, value2});
      DictionaryUtils.setProperty(getObjectFilter(), name1, values);
    }
    else
    {
      if (!StringUtils.isBlank(name1) && !StringUtils.isBlank(value1))
        DictionaryUtils.setProperty(getObjectFilter(), name1, value1);
      if (!StringUtils.isBlank(name2) && !StringUtils.isBlank(value2))
        DictionaryUtils.setProperty(getObjectFilter(), name2, value2);
    }
  }
  
  /**
   * This method copies and transforms input properties to inner object filter
   * @param formProperties
   */  
  public abstract void setInputProperties(List<Property> formProperties);  

  protected abstract String getPropertyName1();

  protected abstract String getPropertyName2();

  protected abstract String getPropertyValue1();

  protected abstract String getPropertyValue2();
  
  protected abstract List<Property> getProperty();
}
