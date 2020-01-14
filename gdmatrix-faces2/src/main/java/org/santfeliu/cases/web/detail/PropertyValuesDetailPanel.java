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
package org.santfeliu.cases.web.detail;

import java.util.ArrayList;
import java.util.List;
import org.matrix.cases.Case;
import org.matrix.dic.EnumTypeItem;
import org.matrix.dic.EnumTypeItemFilter;
import org.matrix.dic.Property;
import org.matrix.dic.PropertyDefinition;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.dic.web.DictionaryConfigBean;
import org.santfeliu.web.obj.DetailBean;

/**
 *
 * @author blanquepa
 */
public class PropertyValuesDetailPanel extends TabulatedDetailPanel
{
  public static final String MAIN_PROPERTY_NAME = "mainPropertyName";
  public static final String INTERSECT_ENUMTYPEID = "intersectEnumTypeId";

  private List<EnumTypeItem> mainEnumTypeItems;
  private List<EnumTypeItem> intersectEnumTypeItems;

  private List<String> values;

  @Override
  public void loadData(DetailBean detailBean)
  {
    values = new ArrayList();
    CaseDetailBean caseDetailBean = (CaseDetailBean) detailBean;
    Case cas = caseDetailBean.getCase();
    if (cas != null)
    {
      String mainPropertyName = getProperty(MAIN_PROPERTY_NAME);
      Property mainProperty = DictionaryUtils.getProperty(cas, mainPropertyName);
      if (mainProperty != null && !mainProperty.getValue().isEmpty())
      {
        String caseTypeId = cas.getCaseTypeId();
        if (caseTypeId != null)
        {
          Type type = TypeCache.getInstance().getType(caseTypeId);
          if (type != null)
          {
            PropertyDefinition pd = type.getPropertyDefinition(mainPropertyName);
            String enumTypeId = pd.getEnumTypeId();
            mainEnumTypeItems = getEnumTypeItems(enumTypeId);

            intersectEnumTypeItems =
              getEnumTypeItems(getProperty(INTERSECT_ENUMTYPEID));

            for (String value : mainProperty.getValue())
            {
              if (enumTypeId != null)
              {
                EnumTypeItem enumType = getEnumTypeItemByValue(mainEnumTypeItems, value);
                if (enumType != null)
                  value = enumType.getLabel();
              }
              values.add(value);
            }
          }
        }
      }
    }
  }

  public List<String> getValues()
  {
    return values;
  }

  public void setValues(List<String> values)
  {
    this.values = values;
  }

  @Override
  public boolean isRenderContent()
  {
    return values != null && !values.isEmpty();
  }

  @Override
  public String getType()
  {
    return "property_values";
  }

  public boolean isIntersectedValue()
  {
    String row = (String)getValue("#{row}");
    if (row != null)
    {
      EnumTypeItem item = getEnumTypeItemByLabel(intersectEnumTypeItems, row);
      if (item != null)
        return true;
    }
    return false;
  }

  private List<EnumTypeItem> getEnumTypeItems(String enumTypeId)
  {
    List<EnumTypeItem> result = new ArrayList();
    if (enumTypeId != null)
    {
      EnumTypeItemFilter filter = new EnumTypeItemFilter();
      filter.setEnumTypeId(enumTypeId);
      try
      {
        result = DictionaryConfigBean.getPort().findEnumTypeItems(filter);
      }
      catch (Exception ex)
      {
      }
    }
    return result;
  }

  private EnumTypeItem getEnumTypeItemByValue(List<EnumTypeItem>items, String name)
  {
    if (name != null && items != null)
    {
      for (EnumTypeItem item : items)
      {
        if (item.getValue().equals(name))
          return item;
      }
    }

    return null;
  }

  private EnumTypeItem getEnumTypeItemByLabel(List<EnumTypeItem>items, String label)
  {
    if (label != null && items != null)
    {
      for (EnumTypeItem item : items)
      {
        if (item.getLabel().equals(label))
          return item;
      }
    }

    return null;
  }

}
