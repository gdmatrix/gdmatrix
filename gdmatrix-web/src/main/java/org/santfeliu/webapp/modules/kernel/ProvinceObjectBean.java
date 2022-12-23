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
package org.santfeliu.webapp.modules.kernel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.KernelManagerPort;
import org.matrix.kernel.Province;
import org.matrix.kernel.ProvinceFilter;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.webapp.FinderBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;

/**
 *
 * @author blanquepa
 */
@Named
@ManualScoped
public class ProvinceObjectBean extends TerritoryObjectBean
{
  private Province province = new Province();
  private List<SelectItem> provinceSelectItems;
  
  @Inject
  ProvinceFinderBean provinceFinderBean;
  
  @Inject
  CountryObjectBean countryObjectBean;
  
  @Inject
  CityObjectBean cityObjectBean;
  
  @Inject
  ProvinceTypeBean provinceTypeBean;

  public Province getProvince()
  {
    return province;
  }

  public void setProvince(Province province)
  {
    this.province = province;
  }
  
  @Override
  public FinderBean getFinderBean()
  {
    return provinceFinderBean;
  }

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.PROVINCE_TYPE;
  }

  @Override
  public String show()
  {
    return "/pages/kernel/province.xhtml";
  }
  
  @Override
  public Province getObject()
  {
    return isNew() ? null : province;
  } 
  
  @Override
  public ProvinceTypeBean getTypeBean()
  {
    return provinceTypeBean;
  }   
  
  @Override
  public String getDescription()
  {
    return isNew() ? "" : getDescription(province.getProvinceId());
  }  
  
  public String getDescription(String provinceId)
  {
    return getTypeBean().getDescription(provinceId);
  }   
  
  @Override
  public void createObject()
  {
    province = new Province();
  }
  
  @Override
  public void loadObject() throws Exception
  {
    if (!NEW_OBJECT_ID.equals(objectId))
    {
      KernelManagerPort port = KernelModuleBean.getPort(false);
      province = port.loadProvince(objectId);
      if (!province.getCountryId().equals(countryObjectBean.getObjectId()))
      {
        countryObjectBean.setObjectId(province.getCountryId());
        countryObjectBean.loadObject();
        loadProvinceSelectItems();
      }
      editing = false;
    }
    else 
    {
      createObject();
    }       
  }  
  
  @Override
  public void storeObject() throws Exception
  {
    KernelModuleBean.getPort(false).storeProvince(province);
    editing = false; 
    provinceSelectItems = null;
  }  
  
  public List<SelectItem> getProvinceSelectItems()
  {
    if (provinceSelectItems == null)
    {
      try
      {
        loadProvinceSelectItems();
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    
    return provinceSelectItems;
  }  
  
  public void loadProvinceSelectItems() throws Exception
  {
    provinceSelectItems = new ArrayList<>();    
    ProvinceFilter filter = new ProvinceFilter();
    String countryId = countryObjectBean.getCountry().getCountryId();

    if (!StringUtils.isBlank(countryId))
    {
      filter.setCountryId(countryId);
      List<Province> provinces = 
        KernelModuleBean.getPort(false).findProvinces(filter);

      for (Province p : provinces)
      {
        SelectItem item = 
          new SelectItem(p.getProvinceId(), p.getName());
        provinceSelectItems.add(item);
      }     
    }
  }
  
  public void onProvinceChange()
  {
    try
    {
      loadObject();
      cityObjectBean.loadCitySelectItems();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }      
  
  @Override
  public Serializable saveState()
  {
    return province;
  }

  @Override
  public void restoreState(Serializable state)
  {
    this.province = (Province)province;
  }    
}
