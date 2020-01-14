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
package org.santfeliu.kernel.service;

import org.matrix.kernel.Street;



/**
 *
 * @author unknown
 */
public class DBStreet extends DBEntityBase
{
  private String countryId;
  private String provinceId;
  private String cityId;
  private String streetId;
  private String streetTypeId; // references DBStreetType
  private String particle;
  private String name;
  private String description;
  private String valdata;
  private String baixasw;

  private DBCity city;
 
  public void setCountryId(String countryId)
  {
    this.countryId = countryId;
  }

  public String getCountryId()
  {
    return countryId;
  }

  public void setProvinceId(String provinceId)
  {
    this.provinceId = provinceId;
  }

  public String getProvinceId()
  {
    return provinceId;
  }

  public void setCityId(String cityId)
  {
    this.cityId = cityId;
  }

  public String getCityId()
  {
    return cityId;
  }

  public void setStreetId(String streetId)
  {
    this.streetId = streetId;
  }

  public String getStreetId()
  {
    return streetId;
  }

  public void setStreetTypeId(String streetTypeId)
  {
    this.streetTypeId = streetTypeId;
  }

  public String getStreetTypeId()
  {
    return streetTypeId;
  }

  public void setParticle(String particle)
  {
    this.particle = particle;
  }

  public String getParticle()
  {
    return particle;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getDescription()
  {
    return description;
  }

  public void setValdata(String valdata)
  {
    this.valdata = valdata;
  }

  public String getValdata()
  {
    return valdata;
  }

  public void setBaixasw(String baixasw)
  {
    this.baixasw = baixasw;
  }

  public String getBaixasw()
  {
    return baixasw;
  }

  /* relationships */
  public void setCity(DBCity city)
  {
    this.city = city;
  }

  public DBCity getCity()
  {
    return city;
  }

  public void copyFrom(Street street)
  {
    String ids[] = street.getStreetId().split(KernelManager.PK_SEPARATOR);
    this.countryId = ids[0];
    this.provinceId = ids[1];
    this.cityId = ids[2];
    this.streetId = ids[3];
    this.name = street.getName();
    this.particle = street.getParticle();
    this.streetTypeId = street.getStreetTypeId();
  }
  
  public void copyTo(Street street)
  {
    street.setCityId(countryId + KernelManager.PK_SEPARATOR + 
      provinceId + KernelManager.PK_SEPARATOR + cityId);
    street.setStreetId(countryId + KernelManager.PK_SEPARATOR + 
      provinceId + KernelManager.PK_SEPARATOR + 
      cityId + KernelManager.PK_SEPARATOR + streetId);
    street.setName(name);
    street.setStreetTypeId(streetTypeId);
    street.setParticle(particle);
  }
}
