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
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.kernel.PersonFilter;
import org.matrix.kernel.PersonView;
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
@RequestScoped
public class PersonFinderBean extends FinderBean
{
  private String smartFilter;
  private PersonFilter filter = new PersonFilter();
  private List<PersonView> rows;
  private int firstRow;
  private boolean outdated;

  @Inject
  NavigatorBean navigatorBean;

  @Inject
  PersonTypeBean personTypeBean;

  @Inject
  PersonObjectBean personObjectBean;

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }

  @Override
  public List<PersonView> getRows()
  {
    return rows;
  }

  public void setRows(List<PersonView> rows)
  {
    this.rows = rows;
  }

  public String getSmartFilter()
  {
    return smartFilter;
  }

  public void setSmartFilter(String smartFilter)
  {
    this.smartFilter = smartFilter;
  }

  public PersonFilter getFilter()
  {
    return filter;
  }

  public void setFilter(PersonFilter filter)
  {
    this.filter = filter;
  }

  public List<String> getFilterPersonId()
  {
    return this.filter.getPersonId();
  }

  public void setFilterPersonId(List<String> personIds)
  {
    this.filter.getPersonId().clear();
    if (personIds != null && !personIds.isEmpty())
    {
      this.filter.getPersonId().addAll(personIds);
    }
  }

  @Override
  public String getObjectId(int position)
  {
    return rows == null ? NEW_OBJECT_ID : rows.get(position).getPersonId();
  }

  @Override
  public int getObjectCount()
  {
    return rows == null ? 0 : rows.size();
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return personObjectBean;
  }

   @Override
  public void smartFind()
  {
    setFinding(true);
    setFilterTabSelector(0);
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
    filter = personTypeBean.queryToFilter(smartFilter, baseTypeId);
    doFind(true);
    firstRow = 0;
  }

  @Override
  public void find()
  {
    setFinding(true);
    setFilterTabSelector(1);
    smartFilter = personTypeBean.filterToQuery(filter);
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

  @Override
  public void clear()
  {
    super.clear();
    filter = new PersonFilter();
    smartFilter = null;
    rows = null;
    setFinding(false);
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]
    {
      isFinding(), getFilterTabSelector(), filter, firstRow, 
        getObjectPosition(), getPageSize()
    };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[]) state;
      setFinding((Boolean)stateArray[0]);
      setFilterTabSelector((Integer) stateArray[1]);
      filter = (PersonFilter) stateArray[2];

      doFind(false);

      firstRow = (Integer) stateArray[3];
      setObjectPosition((Integer) stateArray[4]);
      setPageSize((Integer)stateArray[5]);      
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
        rows = new BigList(2 * getPageSize() + 1, getPageSize())
        {
          @Override
          public int getElementCount()
          {
            try
            {
              return KernelModuleBean.getPort(false).countPersons(filter);
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
              filter.setFirstResult(firstResult);
              filter.setMaxResults(maxResults);
              return KernelModuleBean.getPort(false).findPersonViews(filter);
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
            PersonView personView = (PersonView) rows.get(0);
            navigatorBean.view(personView.getPersonId());
            personObjectBean.setSearchTabSelector(1);
          }
          else
          {
            personObjectBean.setSearchTabSelector(0);
          }
        }
      }
    }
    catch(Exception ex)
    {
      error(ex);
    }
  }

}
