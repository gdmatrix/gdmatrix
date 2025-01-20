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
import java.util.List;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.TypeBean;

/**
 *
 * @author blanquepa
 * @param <F> filter
 * @param <V> view.
 */
public abstract class TerritoryFinderBean<F, V extends Serializable>
  extends FinderBean
{
  protected String smartFilter;
  protected F filter;
  protected List<V> rows;
  protected int firstRow;
  protected boolean outdated;

  public String getSmartFilter()
  {
    return smartFilter;
  }

  public void setSmartFilter(String smartFilter)
  {
    this.smartFilter = smartFilter;
  }

  @Override
  public F getFilter()
  {
    return filter;
  }

  public void setFilter(F filter)
  {
    this.filter = filter;
  }

  @Override
  public List<V> getRows()
  {
    return rows;
  }

  public void setRows(List<V> rows)
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

  protected abstract F createFilter();

  @Override
  public void clear()
  {
    super.clear();
    createFilter();
    smartFilter = null;
    rows = null;
    setFinding(false);
  }

  protected abstract void doFind(boolean autoLoad);

  protected abstract TypeBean getTypeBean();

  @Override
  public Serializable saveState()
  {
    return new Object[]{ isFinding(), getFilter(), firstRow, 
      getObjectPosition(), pageSize };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      setFinding((Boolean)stateArray[0]);
      setFilter((F) stateArray[1]);
      smartFilter = getTypeBean().filterToQuery(filter);

      doFind(false);

      firstRow = (Integer)stateArray[2];
      setObjectPosition((Integer)stateArray[3]);
      pageSize = (Integer)stateArray[4];      
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

}
