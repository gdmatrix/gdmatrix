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
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.KernelManagerPort;
import org.matrix.kernel.Province;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;

/**
 *
 * @author blanquepa
 */
@Named
@ViewScoped
public class ProvinceObjectBean extends TerritoryObjectBean
{  
  @Inject
  ProvinceFinderBean provinceFinderBean;
   
  @Inject
  ProvinceTypeBean provinceTypeBean;
  
  @Inject
  NavigatorBean navigatorBean;
  
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
    setObjectId(NavigatorBean.NEW_OBJECT_ID);
    if (country != null)
      province.setCountryId(country.getCountryId());
  }
  
  @Override
  public void loadObject() throws Exception
  {
    if (objectId != null && !isNew())
    {
      KernelManagerPort port = KernelModuleBean.getPort(false);
      loadProvince(port, objectId);
    }
    else 
    {
      createObject();
    }       
  }  
  
  @Override
  public void storeObject() throws Exception
  {
    String countryId = country.getCountryId();
    if (!countryId.equals(province.getCountryId()))
      province.setCountryId(countryId);    
    
    province = KernelModuleBean.getPort(false).storeProvince(province);
    setObjectId(province.getProvinceId());
    provinceFinderBean.outdate();
  }  
  
  @Override
  public void removeObject() throws Exception
  {
    KernelModuleBean.getPort(false).removeProvince(objectId);
    provinceFinderBean.doFind(false);    
    navigatorBean.view("");
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
