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

import org.matrix.kernel.City;
import org.matrix.kernel.Province;
import org.matrix.kernel.Street;

import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.web.obj.ObjectBean;


/**
 *
 * @author unknown
 */
public class StreetBean extends ObjectBean
{
  public StreetBean()
  {
  }

  public String getObjectTypeId()
  {
    return "Street";
  }
  
  public String getDescription()
  {
    CountryToStreetBean bean = 
      (CountryToStreetBean)getBean("countryToStreetBean");
    return getDescription(bean.getStreet(), bean.getCity(), bean.getProvince());
  }
  
  public String getDescription(String objectId)
  {
    if (ControllerBean.NEW_OBJECT_ID.equals(objectId)) return "";
    try
    {
      Street street = KernelConfigBean.getPort().loadStreet(objectId);
      City city = KernelConfigBean.getPort().loadCity(street.getCityId());
      Province province = KernelConfigBean.getPort().loadProvince(
        city.getProvinceId());
      return getDescription(street, city, province);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return objectId;
  }
  
  private String getDescription(Street street, City city, Province province)
  {
    StringBuffer buffer = new StringBuffer();
    if (street != null) 
      buffer.append(street.getStreetTypeId() + " " + street.getName());
    if (city != null)
      buffer.append(" - " + city.getName());
    if (province != null)
      buffer.append(" (" + province.getName() + ")");
    return buffer.toString();
  }
}
