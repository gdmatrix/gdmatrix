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
import java.util.Collections;
import java.util.List;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.kernel.AddressFilter;
import org.matrix.kernel.AddressView;
import org.santfeliu.util.BigList;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
/**
 *
 * @author blanquepa
 */
@Named
@ViewScoped
public class AddressFinderBean extends FinderBean
{
  private String smartFilter;
  private AddressFilter filter = new AddressFilter();
  private List<AddressView> rows;
  private int firstRow;
  private boolean outdated;

  @Inject
  NavigatorBean navigatorBean;

  @Inject
  AddressObjectBean addressObjectBean;

  @Inject
  AddressTypeBean addressTypeBean;

  public List<AddressView> getRows()
  {
    return rows;
  }

  public void setRows(List<AddressView> rows)
  {
    this.rows = rows;
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }

  public String getSmartFilter()
  {
    return smartFilter;
  }

  public void setSmartFilter(String smartFilter)
  {
    this.smartFilter = smartFilter;
  }

  public AddressFilter getFilter()
  {
    return filter;
  }

  public void setFilter(AddressFilter filter)
  {
    this.filter = filter;
  }

  public List<String> getFilterAddressId()
  {
    return this.filter.getAddressIdList();
  }

  public void setFilterAddressId(List<String> addressIds)
  {
    this.filter.getAddressIdList().clear();
    if (addressIds != null && !addressIds.isEmpty())
      this.filter.getAddressIdList().addAll(addressIds);
  }

  @Override
  public String getObjectId(int position)
  {
    return rows == null ? NEW_OBJECT_ID : rows.get(position).getAddressId();
  }

  @Override
  public int getObjectCount()
  {
    return rows == null ? 0 : rows.size();
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return addressObjectBean;
  }

  @Override
  public void smartFind()
  {
    setFinding(true);
    setFilterTabSelector(0);
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
    filter = addressTypeBean.queryToFilter(smartFilter, baseTypeId);
    doFind(true);
    firstRow = 0;
  }

  @Override
  public void find()
  {
    setFinding(true);
    setFilterTabSelector(1);
    smartFilter = addressTypeBean.filterToQuery(filter);
    doFind(true);
    firstRow = 0;
  }

  public void outdate()
  {
    this.outdated = true;
  }

  public void update()
  {
    if (outdated)
    {
      doFind(false);
    }
  }

  public void clear()
  {
    filter = new AddressFilter();
    smartFilter = null;
    rows = null;
    setFinding(false);
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ isFinding(), getFilterTabSelector(), filter, firstRow, 
      getObjectPosition(), rows, outdated };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[]) state;
      setFinding((Boolean)stateArray[0]);
      setFilterTabSelector((Integer) stateArray[1]);
      filter = (AddressFilter) stateArray[2];
      firstRow = (Integer) stateArray[3];
      setObjectPosition((Integer) stateArray[4]);
      rows = (List<AddressView>) stateArray[5];
      outdated = (Boolean) stateArray[6];
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private void doFind(boolean autoLoad)
  {
    try
    {
      if (!isFinding())
      {
        rows = Collections.EMPTY_LIST;
      }
      else
      {
        rows = new BigList()
        {
          @Override
          public int getElementCount()
          {
            try
            {
              String streetName = filter.getStreetName();
              filter.setStreetName(setWildcards(streetName));
              int count = KernelModuleBean.getPort(false).countAddresses(filter);
              resetWildcards(filter);
              return count;
            }
            catch (Exception ex)
            {
              error(ex);
            }
            return 0;
          }

          @Override
          public List getElements(int firstResult, int maxResults)
          {
            try
            {
              String streetName = filter.getStreetName();
              filter.setStreetName(setWildcards(streetName));        
              filter.setFirstResult(firstResult);
              filter.setMaxResults(maxResults);
              List results = 
                KernelModuleBean.getPort(false).findAddressViews(filter);
              resetWildcards(filter);
              return results;
              
            }
            catch (Exception ex)
            {
              error(ex);
            }
            return null;
          }
        };

        outdated = false;

        if (autoLoad)
        {
          if (rows.size() == 1)
          {
            AddressView addressView = (AddressView) rows.get(0);
            navigatorBean.view(addressView.getAddressId());
            addressObjectBean.setSearchTabSelector(1);
          }
          else
          {
            addressObjectBean.setSearchTabSelector(0);
          }
        }
      }
    }
    catch(Exception ex)
    {
      error(ex);
    }
  }
  
  private String setWildcards(String text)
  {
    if (text != null)
      text = text.trim();    
    if (text != null && !text.startsWith("\"") && !text.endsWith("\""))
      text = "%" + text.replaceAll("^%|%$", "") + "%" ;
    else if (text != null && text.startsWith("\"") && text.endsWith("\""))
      text = text.replaceAll("^\"|\"$", "");
    return text;
  } 
  
  private void resetWildcards(AddressFilter filter)
  {
    String streetName = filter.getStreetName();
    if (streetName != null && !streetName.startsWith("\"") 
      && !streetName.endsWith("\""))
    {
      streetName = streetName.replaceAll("^%+|%+$", "");
      filter.setStreetName(streetName);
    }
  }  

}
