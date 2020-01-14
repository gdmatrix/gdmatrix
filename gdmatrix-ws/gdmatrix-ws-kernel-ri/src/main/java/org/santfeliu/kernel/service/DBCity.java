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

import org.matrix.kernel.City;


/**
 *
 * @author unknown
 */
public class DBCity extends DBEntityBase
{
  private String countryId;
  private String provinceId;
  private String cityId;
  private String name;
  private String description;
  private String munidc;
  private int munivnum;
  private int illavnum;
  private int tramvnum;
  private String comcod;
  private String valdata;
  private String baixasw;
  private String municps;
  
  private DBProvince province;
  
  public DBCity()
  {
  }

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

  public void setMunidc(String munidc)
  {
    this.munidc = munidc;
  }

  public String getMunidc()
  {
    return munidc;
  }

  public void setMunivnum(int munivnum)
  {
    this.munivnum = munivnum;
  }

  public int getMunivnum()
  {
    return munivnum;
  }

  public void setIllavnum(int illavnum)
  {
    this.illavnum = illavnum;
  }

  public int getIllavnum()
  {
    return illavnum;
  }

  public void setTramvnum(int tramvnum)
  {
    this.tramvnum = tramvnum;
  }

  public int getTramvnum()
  {
    return tramvnum;
  }

  public void setComcod(String comcod)
  {
    this.comcod = comcod;
  }

  public String getComcod()
  {
    return comcod;
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

  public void setMunicps(String municps)
  {
    this.municps = municps;
  }

  public String getMunicps()
  {
    return municps;
  }

  /* relationships */
  public void setProvince(DBProvince province)
  {
    this.province = province;
  }

  public DBProvince getProvince()
  {
    return province;
  }

  public void copyFrom(City city)
  {
    String ids[] = city.getCityId().split(KernelManager.PK_SEPARATOR);
    this.countryId = ids[0];
    this.provinceId = ids[1];
    this.cityId = ids[2];
    this.name = city.getName();
  }
  
  public void copyTo(City city)
  {
    city.setProvinceId(countryId + KernelManager.PK_SEPARATOR + provinceId);
    city.setCityId(countryId + KernelManager.PK_SEPARATOR + 
      provinceId + KernelManager.PK_SEPARATOR + cityId);
    city.setName(name);
  }
}
