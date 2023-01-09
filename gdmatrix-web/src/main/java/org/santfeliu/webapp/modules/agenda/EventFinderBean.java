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
package org.santfeliu.webapp.modules.agenda;


import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.agenda.Event;
import org.matrix.agenda.EventFilter;
import org.primefaces.event.SelectEvent;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.util.BigList;
import org.santfeliu.util.TextUtils;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;

/**
 *
 * @author lopezrj-sf
 */
@Named
@ManualScoped
public class EventFinderBean extends FinderBean
{
  private String smartFilter;
  private EventFilter filter = new EventFilter();
  private List<Event> rows;
  private int firstRow;
  private boolean finding;
  private boolean outdated;

  private String searchEventTypeId;
  private String searchEventThemeId;

  @Inject
  NavigatorBean navigatorBean;

  @Inject
  EventObjectBean eventObjectBean;

  @Inject
  EventTypeBean eventTypeBean;

  @PostConstruct
  public void init()
  {
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return eventObjectBean;
  }

  public String getSmartFilter()
  {
    return smartFilter;
  }

  public void setSmartFilter(String smartFilter)
  {
    this.smartFilter = smartFilter;
  }

  public EventFilter getFilter()
  {
    return filter;
  }

  public void setFilter(EventFilter filter)
  {
    this.filter = filter;
  }

  public List<Event> getRows()
  {
    return rows;
  }

  public void setRows(List<Event> rows)
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

  public String getSearchEventTypeId()
  {
    return searchEventTypeId;
  }

  public void setSearchEventTypeId(String searchEventTypeId)
  {
    this.searchEventTypeId = searchEventTypeId;
  }

  public String getSearchEventThemeId()
  {
    return searchEventThemeId;
  }

  public void setSearchEventThemeId(String searchEventThemeId)
  {
    this.searchEventThemeId = searchEventThemeId;
  }

  @Override
  public String getObjectId(int position)
  {
    return rows == null ? NEW_OBJECT_ID : rows.get(position).getEventId();
  }

  @Override
  public int getObjectCount()
  {
    return rows == null ? 0 : rows.size();
  }

  public Date getFromDate()
  {
    if (filter.getStartDateTime() != null)
      return TextUtils.parseInternalDate(filter.getStartDateTime());
    else
      return null;
  }

  public void setFromDate(Date date)
  {
    if (date != null)
      filter.setStartDateTime(TextUtils.formatDate(date, "yyyyMMddHHmmss"));
    else
      filter.setStartDateTime(null);
  }

  public Date getToDate()
  {
    if (filter.getEndDateTime() != null)
      return TextUtils.parseInternalDate(filter.getEndDateTime());
    else
      return null;
  }

  public void setToDate(Date date)
  {
    if (date != null)
      filter.setEndDateTime(TextUtils.formatDate(date, "yyyyMMddHHmmss"));
    else
      filter.setEndDateTime(null);
  }

  public List<String> getEventId()
  {
    return filter.getEventId();
  }

  public void setEventId(List<String> eventIds)
  {
    filter.getEventId().clear();
    if (eventIds != null && !eventIds.isEmpty())
      filter.getEventId().addAll(eventIds);
  }

  //TODO Move to superclass
  public String getLanguage()
  {
    return getLocale().getLanguage();
  }

  public void onPersonSelect(SelectEvent<SelectItem> event)
  {
    SelectItem item = event.getObject();
    String personId = (String)item.getValue();
    filter.setPersonId(personId);
  }

  public void onPersonClear()
  {
    filter.setPersonId(null);
  }

  public void onRoomSelect(SelectEvent<SelectItem> event)
  {
    SelectItem item = event.getObject();
    String roomId = (String)item.getValue();
    filter.setRoomId(roomId);
  }

  public void onRoomClear()
  {
    filter.setRoomId(null);
  }

  @Override
  public void smartFind()
  {
    finding = true;
    setTabIndex(0);
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
    filter = eventTypeBean.queryToFilter(smartFilter, baseTypeId);
    setFromDate(new Date());
    filter.setDateComparator("1");
    searchEventTypeId = null;
    doFind(true);
    firstRow = 0;
  }

  @Override
  public void find()
  {
    finding = true;
    setTabIndex(1);
    smartFilter = eventTypeBean.filterToQuery(filter);
    filter.getEventTypeId().clear();
    if (!StringUtils.isBlank(searchEventTypeId))
    {
      filter.getEventTypeId().clear();
      filter.getEventTypeId().add(searchEventTypeId);
    }
    if (filter.getStartDateTime() == null)
    {
      setFromDate(new Date());
      filter.setDateComparator("1");
    }
    if (!StringUtils.isBlank(searchEventThemeId))
    {
      filter.getThemeId().clear();
      filter.getThemeId().add(searchEventThemeId);
    }

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
    filter = new EventFilter();
    smartFilter = null;
    setFromDate(new Date());
    filter.setDateComparator("1");
    searchEventTypeId = null;
    searchEventThemeId = null;
    rows = null;
    finding = false;
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ finding, getTabIndex(), filter, firstRow,
      searchEventTypeId, searchEventThemeId, getObjectPosition() };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      finding = (Boolean)stateArray[0];
      setTabIndex((Integer)stateArray[1]);
      filter = (EventFilter)stateArray[2];

      doFind(false);

      firstRow = (Integer)stateArray[3];
      searchEventTypeId = (String)stateArray[4];
      searchEventThemeId = (String)stateArray[5];
      setObjectPosition((Integer)stateArray[6]);
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
      if (!finding)
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
              return AgendaModuleBean.getClient(false).
                countEventsFromCache(filter);
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
              return AgendaModuleBean.getClient(false).
                findEventsFromCache(filter);
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
            navigatorBean.view(rows.get(0).getEventId());
            eventObjectBean.setSearchTabIndex(eventObjectBean.
              getEditionTabIndex());
          }
          else
          {
            eventObjectBean.setSearchTabIndex(0);
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
