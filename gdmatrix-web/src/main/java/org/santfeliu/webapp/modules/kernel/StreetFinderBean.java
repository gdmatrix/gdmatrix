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
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.kernel.Street;
import org.matrix.kernel.StreetFilter;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.util.BigList;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TypeBean;
import org.santfeliu.webapp.modules.kernel.StreetFinderBean.StreetView;

/**
 *
 * @author blanquepa
 */
@Named
@ManualScoped
public class StreetFinderBean 
  extends TerritoryFinderBean<StreetFilter, StreetView>
{  
  @Inject
  NavigatorBean navigatorBean;
  
  @Inject
  StreetObjectBean streetObjectBean;
  
  @Inject
  StreetTypeBean streetTypeBean;
  
  @Inject
  CityTypeBean cityTypeBean;
  
  public StreetFinderBean()
  {
    filter = new StreetFilter();
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return streetObjectBean;
  }  
  
  @Override
  public String getObjectId(int position)
  {
    return rows == null ? NEW_OBJECT_ID : rows.get(position).getStreetId();
  }

  @Override
  public int getObjectCount()
  {
    return rows == null ? 0 : rows.size();
  }    

  @Override
  protected StreetFilter createFilter()
  {
    return new StreetFilter();
  }  

  @Override
  protected TypeBean getTypeBean()
  {
    return streetTypeBean;
  }
  
  @Override
  public void smartFind()
  {
    findMode = 1;
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
    filter = streetTypeBean.queryToFilter(smartFilter, baseTypeId);
    doFind(true);
    firstRow = 0;
  }

  @Override
  public void find()
  {
    findMode = 2;
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
    filter.setStreetTypeId(baseTypeId);
    smartFilter = streetTypeBean.filterToQuery(filter);
    doFind(true);
    firstRow = 0;
  }  
    
  @Override
  protected void doFind(boolean autoLoad)
  {
    try
    {
      if (findMode == 0)
      {
        rows = Collections.EMPTY_LIST;
      }
      else
      {
        if (findMode == 1)
        {
          setTabIndex(0);
        }
        else
        {
          setTabIndex(1);
        }

        rows = new BigList(20, 10)
        {
          @Override
          public int getElementCount()
          {
            try
            {
              return KernelModuleBean.getPort(false).countStreets(filter);
            }
            catch (Exception ex)
            {
              error(ex);
              return 0;
            }
          }

          @Override
          public List getElements(int firstResult, int maxResults)
          {
            List<StreetView> results = new ArrayList();            
            try
            {
              filter.setFirstResult(firstResult);
              filter.setMaxResults(maxResults);
              
              List<Street> streets =
                KernelModuleBean.getPort(false).findStreets(filter);
              for (Street street : streets)        
              {
                StreetView view = new StreetView(street);
                results.add(view);
              }
            }
            catch (Exception ex)
            {
              error(ex);
              return null;
            }
            return results;
          }
        };

        outdated = false;

        if (autoLoad)
        {
          if (rows.size() == 1)
          {
            navigatorBean.view(rows.get(0).getStreetId());
            streetObjectBean.setSearchTabIndex(
              streetObjectBean.getEditionTabIndex());
          }
          else
          {
            streetObjectBean.setSearchTabIndex(0);
          }
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public class StreetView implements Serializable
  {
    private String streetId;
    private String name;
    private String city;    
    
    public StreetView(Street street)
    {
      this.streetId = street.getStreetId();      
      this.name = street.getStreetTypeId() + " " + street.getName();  
      this.city = cityTypeBean.getDescription(street.getCityId());
    }

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public String getStreetId()
    {
      return streetId;
    }

    public void setStreetId(String streetId)
    {
      this.streetId = streetId;
    }

    public String getCity()
    {
      return city;
    }

    public void setCity(String city)
    {
      this.city = city;
    }
  }
  
}
