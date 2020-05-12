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

import java.util.List;
import org.matrix.dic.EnumType;
import org.matrix.dic.EnumTypeFilter;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.obj.BasicSearchBean;

/**
 *
 * @author unknown
 */
@CMSManagedBean
public class EnumTypeSearchBean extends BasicSearchBean
{
  private String enumTypeIdInput;
  private EnumTypeFilter filter;
  
  public EnumTypeSearchBean()
  {
    filter = new EnumTypeFilter();
  }

  public String getEnumTypeIdInput()
  {
    return enumTypeIdInput;
  }

  public void setEnumTypeIdInput(String enumTypeIdInput)
  {
    this.enumTypeIdInput = enumTypeIdInput;
  }

  public EnumTypeFilter getFilter()
  {
    return filter;
  }

  public void setFilter(EnumTypeFilter filter)
  {
    this.filter = filter;
  }

  public int countResults()
  {
    try
    {
      setFilterEnumTypeId();
      return DictionaryConfigBean.getPort().countEnumTypes(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return 0;
  }

  public List getResults(int firstResult, int maxResults)
  {
    try
    {
      setFilterEnumTypeId();
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      return DictionaryConfigBean.getPort().findEnumTypes(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  @CMSAction  
  public String show()
  {
    return "enum_type_search";
  }

  public String selectEnumType()
  {
    EnumType row = (EnumType)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    String enumTypeId = row.getEnumTypeId();
    return getControllerBean().select(enumTypeId);
  }

  public String showEnumType()
  {
    return getControllerBean().showObject("EnumType",
      (String)getValue("#{row.enumTypeId}"));
  }

  private void setFilterEnumTypeId()
  {
    filter.getEnumTypeId().clear();
    if (enumTypeIdInput != null)
    {
      for (String enumTypeId : enumTypeIdInput.split(";"))
      {
        if (!enumTypeId.isEmpty()) filter.getEnumTypeId().add(enumTypeId);
      }
    }
  }
  
}
