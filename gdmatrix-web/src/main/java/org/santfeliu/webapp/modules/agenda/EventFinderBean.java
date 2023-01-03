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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.agenda.Event;
import org.matrix.agenda.EventFilter;
import org.matrix.agenda.Theme;
import org.matrix.agenda.ThemeFilter;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.Person;
import org.matrix.kernel.Room;
import org.primefaces.event.SelectEvent;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.util.TextUtils;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.helpers.ReferenceHelper;

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
  private boolean isSmartFind;
  
  private String searchEventTypeId;

  ReferenceHelper<Person> personReferenceHelper; 
  ReferenceHelper<Room> roomReferenceHelper; 
  
  @Inject
  NavigatorBean navigatorBean;

  @Inject
  EventObjectBean eventObjectBean;
  
  @Inject
  EventTypeBean eventTypeBean;  
  
  public EventFinderBean()
  {
    personReferenceHelper = 
      new PersonReferenceHelper(DictionaryConstants.PERSON_TYPE);
    roomReferenceHelper = 
      new RoomReferenceHelper(DictionaryConstants.ROOM_TYPE);      
  }
  
  @Override
  public ObjectBean getObjectBean()
  {
    return eventObjectBean;
  }

  public ReferenceHelper<Person> getPersonReferenceHelper() 
  {
    return personReferenceHelper;
  }  
  
  public ReferenceHelper<Room> getRoomReferenceHelper() 
  {
    return roomReferenceHelper;
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
  
  public String getThemeId() 
  {
    if (!filter.getThemeId().isEmpty())
      return filter.getThemeId().get(0);
    else
      return null;
  }

  public void setThemeId(String themeId) 
  {
    filter.getThemeId().clear();
    if (!StringUtils.isBlank(themeId))
    {
      filter.getThemeId().add(themeId);
    }
  }

  public List<SelectItem> getThemeSelectItems()
  {
    List<SelectItem> themeSelectItems = new ArrayList();    
    try
    {
      themeSelectItems.add(new SelectItem(NavigatorBean.NEW_OBJECT_ID, " "));
      List<Theme> themes = AgendaModuleBean.getClient(false).
        findThemesFromCache(new ThemeFilter());
      for (Theme theme : themes)
      {
        SelectItem item = new SelectItem(theme.getThemeId(), 
          theme.getDescription());
        themeSelectItems.add(item);
      }
      return FacesUtils.sortSelectItems(themeSelectItems);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return themeSelectItems;
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
    isSmartFind = true;
    doFind(true);
  }

  public void smartClear()
  {
    smartFilter = null;
    rows = null;
  }

  @Override
  public void find()
  {
    isSmartFind = false;
    doFind(true);
  }

  public void clear()
  {
    filter = new EventFilter();
    setFromDate(new Date());
    filter.setDateComparator("1");    
    searchEventTypeId = null;
    rows = null;
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ isSmartFind, filter, firstRow, searchEventTypeId, 
      getObjectPosition() };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      isSmartFind = (Boolean)stateArray[0];
      filter = (EventFilter)stateArray[1];

      doFind(false);

      firstRow = (Integer)stateArray[2];
      searchEventTypeId = (String)stateArray[3];
      setObjectPosition((Integer)stateArray[4]);      
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
      firstRow = 0;
      String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
      
      if (isSmartFind)
      {
        filter = eventTypeBean.queryToFilter(smartFilter, baseTypeId);
        setFromDate(new Date());
        filter.setDateComparator("1");
        searchEventTypeId = null;
        setTabIndex(0);
      }
      else
      {
        smartFilter = eventTypeBean.filterToQuery(filter);
        filter.getEventTypeId().clear();
        if (!StringUtils.isBlank(searchEventTypeId))
        {
          filter.getEventTypeId().add(searchEventTypeId);
        }
        if (filter.getStartDateTime() == null)
        {
          setFromDate(new Date());
          filter.setDateComparator("1");
        }
        setTabIndex(1);
      }
      rows = AgendaModuleBean.getClient(false).findEventsFromCache(filter);      

      if (autoLoad)
      {
        if (rows.size() == 1)
        {
          navigatorBean.view(rows.get(0).getEventId());
          eventObjectBean.setSearchTabIndex(1);
        }
        else
        {
          eventObjectBean.setSearchTabIndex(0);
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  private class PersonReferenceHelper extends ReferenceHelper<Person>
  {
    public PersonReferenceHelper(String typeId)
    {
      super(typeId);
    }

    @Override
    public String getId(Person person)
    {
      return person.getPersonId();
    }

    @Override
    public String getSelectedId()
    {
      return filter != null ? filter.getPersonId() : "";
    }

    @Override
    public void setSelectedId(String value)
    {
      if (filter != null)
        filter.setPersonId(value);
    }
  }
  
  private class RoomReferenceHelper extends ReferenceHelper<Room>
  {
    public RoomReferenceHelper(String typeId)
    {
      super(typeId);
    }

    @Override
    public String getId(Room room)
    {
      return room.getRoomId();
    }

    @Override
    public String getSelectedId()
    {
      return filter != null ? filter.getRoomId() : "";
    }

    @Override
    public void setSelectedId(String value)
    {
      if (filter != null)
      filter.setRoomId(value);
    }
  }  
  
  
}
