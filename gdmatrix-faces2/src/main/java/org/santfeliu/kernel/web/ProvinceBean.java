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
package org.santfeliu.kernel.web;

import org.matrix.kernel.Country;
import org.matrix.kernel.Province;

import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.web.obj.ObjectBean;


/**
 *
 * @author unknown
 */
public class ProvinceBean extends ObjectBean
{
  public ProvinceBean()
  {
  }

  public String getObjectTypeId()
  {
    return "Province";
  }
  
  public String getDescription()
  {
    CountryToStreetBean bean = 
      (CountryToStreetBean)getBean("countryToStreetBean");
    return getDescription(bean.getProvince(), bean.getCountry());
  }
  
  public String getDescription(String objectId)
  {
    if (ControllerBean.NEW_OBJECT_ID.equals(objectId)) return "";
    try
    {
      Province province = KernelConfigBean.getPort().loadProvince(objectId);
      Country country = KernelConfigBean.getPort().loadCountry(
        province.getCountryId());
      return getDescription(province, country);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return objectId;    
  }
  
  private String getDescription(Province province, Country country)
  {
    StringBuffer buffer = new StringBuffer();
    if (province != null) buffer.append(province.getName());
    if (country != null) buffer.append(" (" + country.getName() + ")");
    return buffer.toString();
  }
}
