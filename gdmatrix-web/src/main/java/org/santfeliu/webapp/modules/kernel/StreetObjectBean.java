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
import org.matrix.kernel.Street;
import org.matrix.kernel.StreetFilter;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;

/**
 *
 * @author blanquepa
 */
@Named
@ManualScoped
public class StreetObjectBean extends TerritoryObjectBean
{
  public static final String DEFAULT_CITY_NAME = "defaultCityName";

  private Street street = new Street();
  private String streetId;
  private List<SelectItem> streetSelectItems;
  
  @Inject
  StreetFinderBean streetFinderBean;
  
  @Inject
  private CityObjectBean cityObjectBean;
  
  @Inject
  StreetTypeBean streetTypeBean;  
  
  public StreetObjectBean()
  {  
  }

  public Street getStreet()
  {
    return street;
  }

  public void setStreet(Street street)
  {
    this.street = street;
  }

  public String getStreetId()
  {
    return streetId;
  }

  public void setStreetId(String streetId)
  {
    this.streetId = streetId;
  }
  
  @Override
  public FinderBean getFinderBean()
  {
    return streetFinderBean;
  }

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.STREET_TYPE;
  }

  @Override
  public String show()
  {
    return "/pages/kernel/street.xhtml";
  }

  @Override
  public Street getObject()
  {
    return isNew() ? null : street;
  }    
  
  @Override
  public StreetTypeBean getTypeBean()
  {
    return streetTypeBean;
  }   
  
  @Override
  public String getDescription()
  {
    return isNew() ? "" : getDescription(street.getStreetId());
  }  
  
  public String getDescription(String streetId)
  {
    return getTypeBean().getDescription(streetId);
  }   
  
  @Override
  public void createObject()
  {
    street = new Street();
    setObjectId(NavigatorBean.NEW_OBJECT_ID);
    street.setCityId(cityObjectBean.getCity().getCityId());
  } 
  
  @Override
  public void loadObject() throws Exception
  {
    if (objectId != null && !isNew())
    {
      KernelManagerPort port = KernelModuleBean.getPort(false);
      street = port.loadStreet(objectId);
      if (!street.getCityId().equals(cityObjectBean.getObjectId()))
      {
        cityObjectBean.setObjectId(street.getCityId());
        cityObjectBean.loadObject();
        loadStreetSelectItems();        
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
    if (!street.getCityId().equals(cityObjectBean.getObjectId()))
      street.setCityId(cityObjectBean.getObjectId());     
    street = KernelModuleBean.getPort(false).storeStreet(street);
    setObjectId(street.getStreetId());
    editing = false; 
    streetSelectItems = null;
    streetFinderBean.outdate();    
  }     
  
  public List<SelectItem> getStreetSelectItems()
  {
    if (streetSelectItems == null)
    {
      try
      {
        loadStreetSelectItems();
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    
    return streetSelectItems;
  }  
  
  public void loadStreetSelectItems() throws Exception
  {
    streetSelectItems = new ArrayList<>();    
    StreetFilter filter = new StreetFilter();
    String cityId = cityObjectBean.getObjectId();
    if (!StringUtils.isBlank(cityId))
    {
      filter.setCityId(cityId);
    
      List<Street> streets = 
        KernelModuleBean.getPort(false).findStreets(filter);

      for (Street s : streets)
      {
        SelectItem item = 
          new SelectItem(s.getStreetId(), s.getStreetTypeId() + " " + s.getName());
        streetSelectItems.add(item);
      }     
    }
  }
  
  public void onStreetChange()
  {
    try
    {
      loadObject();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }      
  
  @Override
  public Serializable saveState()
  {
    return street;
  }

  @Override
  public void restoreState(Serializable state)
  {
    this.street = (Street)street;
  } 



 
  
}
