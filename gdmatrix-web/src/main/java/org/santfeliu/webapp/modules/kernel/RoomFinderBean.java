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
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.kernel.RoomFilter;
import org.matrix.kernel.RoomView;
import org.santfeliu.dic.web.TypeBean;
import org.santfeliu.util.BigList;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author lopezrj-sf
 */
@Named
@RequestScoped
public class RoomFinderBean extends FinderBean
{
  private String smartFilter;
  private RoomFilter filter = new RoomFilter();
  private List<RoomView> rows;
  private int firstRow;
  private boolean outdated;

  @Inject
  NavigatorBean navigatorBean;

  @Inject
  RoomTypeBean roomTypeBean;

  @Inject
  RoomObjectBean roomObjectBean;

  @PostConstruct
  public void init()
  {
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return roomObjectBean;
  }

  public String getSmartFilter()
  {
    return smartFilter;
  }

  public void setSmartFilter(String smartFilter)
  {
    this.smartFilter = smartFilter;
  }

  public RoomFilter getFilter()
  {
    return filter;
  }

  public void setFilter(RoomFilter filter)
  {
    this.filter = filter;
  }

  @Override
  public List<RoomView> getRows()
  {
    return rows;
  }

  public void setRows(List<RoomView> rows)
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
  public String getObjectId(int position)
  {
    return rows == null ? NEW_OBJECT_ID : rows.get(position).getRoomId();
  }

  @Override
  public int getObjectCount()
  {
    return rows == null ? 0 : rows.size();
  }

  public List<String> getRoomId()
  {
    return filter.getRoomIdList();
  }

  public void setRoomId(List<String> roomIds)
  {
    filter.getRoomIdList().clear();
    if (roomIds != null && !roomIds.isEmpty())
      filter.getRoomIdList().addAll(roomIds);
  }

  @Override
  public void smartFind()
  {
    setFinding(true);
    setFilterTabSelector(0);
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
    filter = roomTypeBean.queryToFilter(smartFilter, baseTypeId);
    doFind(true);
    firstRow = 0;
  }

  @Override
  public void find()
  {
    setFinding(true);
    setFilterTabSelector(1);
    smartFilter = roomTypeBean.filterToQuery(filter);
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
    filter = new RoomFilter();
    smartFilter = null;
    rows = null;
    setFinding(false);
  }

  public String getRoomTypeDescription(RoomView roomView)
  {
    TypeBean typeBean = WebUtils.getBean("typeBean");
    return ((roomView == null || roomView.getRoomTypeId() == null) ? "" :
      typeBean.getDescription(roomView.getRoomTypeId()));
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ isFinding(), getFilterTabSelector(), filter,
      firstRow, getObjectPosition(), getPageSize() };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      setFinding((Boolean)stateArray[0]);
      setFilterTabSelector((Integer)stateArray[1]);
      filter = (RoomFilter)stateArray[2];

      doFind(false);

      firstRow = (Integer)stateArray[3];
      setObjectPosition((Integer)stateArray[4]);
      setPageSize((Integer)stateArray[5]);      
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private void doFind(boolean autoLoad)
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
            return KernelModuleBean.getPort(false).countRooms(filter);
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
            return KernelModuleBean.getPort(false).findRoomViews(filter);
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
          navigatorBean.view(rows.get(0).getRoomId());
          roomObjectBean.setSearchTabSelector(roomObjectBean.getEditModeSelector());
        }
        else
        {
          roomObjectBean.setSearchTabSelector(0);
        }
      }
    }
  }

}
