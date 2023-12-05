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
package org.santfeliu.webapp.modules.policy;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.policy.Policy;
import org.matrix.policy.PolicyFilter;
import org.santfeliu.util.BigList;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.setup.Column;

/**
 *
 * @author blanquepa
 */
@Named
@ViewScoped
public class PolicyFinderBean extends FinderBean
{
  private String smartFilter;
  private PolicyFilter filter = new PolicyFilter();
  private List<Policy> rows;
  private int firstRow;
  private boolean outdated;
  private String formSelector;

  @Inject
  NavigatorBean navigatorBean;

  @Inject
  PolicyTypeBean policyTypeBean;

  @Inject
  PolicyObjectBean policyObjectBean;

  @Override
  public PolicyObjectBean getObjectBean()
  {
    return policyObjectBean;
  }

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
  }

  public String getSmartFilter()
  {
    return smartFilter;
  }

  public void setSmartFilter(String smartFilter)
  {
    this.smartFilter = smartFilter;
  }

  public PolicyFilter getFilter()
  {
    return filter;
  }

  public void setFilter(PolicyFilter filter)
  {
    this.filter = filter;
  }

  public String getFormSelector()
  {
    return formSelector;
  }
  
  public void setFormSelector(String formSelector)
  {
    this.formSelector = formSelector;
  }

  public List<Column> getColumns()
  {
    try
    {
      if (objectSetup == null)
        loadObjectSetup();

      return objectSetup.getSearchTabs().get(0).getColumns();
    }
    catch (Exception ex)
    {
      return Collections.emptyList();
    }
  }

  @Override
  public String getObjectId(int position)
  {
    return rows == null ? NEW_OBJECT_ID : rows.get(position).getPolicyId();
  }

  @Override
  public int getObjectCount()
  {
    return rows == null ? 0 : rows.size();
  }

  public List<Policy> getRows()
  {
    return rows;
  }

  public void setRows(List<Policy> rows)
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

  @Override
  public void smartFind()
  {
    setFinding(true);
    setFilterTabSelector(0);
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
    filter = policyTypeBean.queryToFilter(smartFilter, baseTypeId);
    doFind(true);
    firstRow = 0;
  }

  @Override
  public void find()
  {
    setFinding(true);
    setFilterTabSelector(1);
    smartFilter = policyTypeBean.filterToQuery(filter);
    doFind(true);
    firstRow = 0;
  }

  public void outdate()
  {
    outdated = true;
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
    filter = new PolicyFilter();
    smartFilter = null;
    rows = null;
    setFinding(false);
    formSelector = null;
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ isFinding(), getFilterTabSelector(),
      filter, firstRow, getObjectPosition(), formSelector, rows, outdated };
  }

  @Override
  public void restoreState(Serializable state)
  {
    Object[] stateArray = (Object[])state;
    setFinding((Boolean)stateArray[0]);
    setFilterTabSelector((Integer)stateArray[1]);
    filter = (PolicyFilter)stateArray[2];
    firstRow = (Integer)stateArray[3];
    smartFilter = policyTypeBean.filterToQuery(filter);
    formSelector = (String)stateArray[5];
    rows = (List<Policy>)stateArray[6];
    outdated = (Boolean)stateArray[7];
    setObjectPosition((Integer)stateArray[4]);
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
        rows = new BigList(20, 10)
        {
          @Override
          public int getElementCount()
          {
            try
            {
              return PolicyModuleBean.getPort(false).countPolicies(filter);
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
            try
            {
              filter.setFirstResult(firstResult);
              filter.setMaxResults(maxResults);
              return PolicyModuleBean.getPort(false).findPolicies(filter);
            }
            catch (Exception ex)
            {
              error(ex);
              return null;
            }
          }
        };

        outdated = false;

        if (autoLoad)
        {
          if (rows.size() == 1)
          {
            navigatorBean.view(rows.get(0).getPolicyId());
            policyObjectBean.setSearchTabSelector(
              policyObjectBean.getEditModeSelector());
          }
          else
          {
            policyObjectBean.setSearchTabSelector(0);
          }
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

}
