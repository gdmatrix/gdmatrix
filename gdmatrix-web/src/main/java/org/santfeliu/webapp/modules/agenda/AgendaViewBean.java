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

import com.google.gson.Gson;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.agenda.EventFilter;
import org.matrix.agenda.EventPlaceView;
import org.matrix.agenda.EventThemeView;
import org.matrix.agenda.EventView;
import org.matrix.agenda.SecurityMode;
import org.santfeliu.agenda.Place;
import org.santfeliu.cases.AddressDescriptionCache;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.webapp.helpers.BasicSearchHelper;
import org.santfeliu.webapp.modules.kernel.RoomTypeBean;

/**
 *
 * @author blanquepa
 */
@Named
@RequestScoped
public class AgendaViewBean extends WebBean implements Serializable
{
  public static final String THEMES_PROPERTY = "Event.themes";
  public static final String TYPES_PROPERTY =  "Event.types";
  public static final String ROOMS_PROPERTY = "Event.rooms";
  public static final String SORT_EVENT_ROOM = "sortEventRoom";  
  
  private static final String OUTCOME = "/pages/agenda/agenda_view.xhtml";

  private EventFilter eventFilter;

  private final boolean groupedByMonth = true;
  
  @Inject
  ThemeTypeBean themeTypeBean;
  @Inject
  RoomTypeBean roomTypeBean;  
  
  private BasicSearchHelper<EventView> basicSearchHelper;

  @PostConstruct
  public void init()
  {
    setConfigurationFilter();
    basicSearchHelper = new BasicSearchHelper() 
    {
      @Override
      public int countResults()
      {
        try
        {
          return AgendaModuleBean.getClient().countEventsFromCache(eventFilter);
        }
        catch (Exception ex)
        {
          error(ex);
          return 0;
        }
      }

      @Override
      public List getResults(int firstResult, int maxResults)
      {
        List<EventRow> results = null;
        try
        {
          eventFilter.setFirstResult(firstResult);
          eventFilter.setMaxResults(maxResults);
          eventFilter.setIncludeMetadata(true);
          List<EventView> events = AgendaModuleBean.getClient().
            findEventViewsFromCache(eventFilter);
          results = events.stream().map(event -> new EventRow(event)).toList(); //TODO:
        }
        catch (Exception ex)
        {
          error(ex);
        }

        return results;
      }
    };
  }

  public String getFilterName()
  {
    return eventFilter.getContent();
  }

  public void setFilterName(String filterName)
  {
    eventFilter.setContent(filterName);
  }

  public Date getFilterStartDate()
  {
    if (eventFilter.getStartDateTime() != null)
      return TextUtils.parseInternalDate(eventFilter.getStartDateTime());
    else
      return null;
  }

  public void setFilterStartDate(Date filterStartDate)
  {
    if (filterStartDate != null)
      eventFilter.setStartDateTime(
        TextUtils.formatDate(filterStartDate, "yyyyMMddHHmmss"));
    else
      eventFilter.setStartDateTime(null);    
  }

  public String getSelectedTheme()
  {
    if (eventFilter.getThemeId().size() > 1)
      return "";
    else
      return eventFilter.getThemeId().get(0);
  }

  public void setSelectedTheme(String selectedTheme)
  {
    if (!StringUtils.isBlank(selectedTheme))
    {
      eventFilter.getThemeId().clear();
      eventFilter.getThemeId().add(selectedTheme);
    }
    else
    {
      eventFilter.getThemeId().clear();
      eventFilter.getThemeId().addAll(getThemeIds());
    }
  }
  
  public String getSelectedLocation()
  {
    return eventFilter.getRoomId();
  }

  public void setSelectedLocation(String filterLocation)
  {
    eventFilter.setRoomId(filterLocation);
  }

  @Override
  public Map getProperties()
  {
    return super.getProperties();
  }

  public String getContent()
  {
    return OUTCOME;
  }

  @CMSAction
  public String show()
  {
    basicSearchHelper.search();
    String template = UserSessionBean.getCurrentInstance().getTemplate();
    return "/templates/" + template + "/template.xhtml";
  }

  public int getPageSize()
  {
    return basicSearchHelper.getPageSize();
  }

  public List<EventRow> getRows()
  {
    System.out.println("--> getRows() solicitado por el XHTML");
    return basicSearchHelper.getRows();
  }

  public void clearFilters()
  {
    setConfigurationFilter(); // Reset base filter
    search();
  }
  
  public List<String> getThemeIds()
  {
    MenuItemCursor selectedMenuItem
      = UserSessionBean.getCurrentInstance().getSelectedMenuItem();    
    return selectedMenuItem.getMultiValuedProperty(THEMES_PROPERTY);
  }
  
  public List<String> getTypeIds()
  {
    MenuItemCursor selectedMenuItem
      = UserSessionBean.getCurrentInstance().getSelectedMenuItem();    
    return selectedMenuItem.getMultiValuedProperty(TYPES_PROPERTY);
  }  
  
  public List<String> getRoomIds()
  {
    MenuItemCursor selectedMenuItem
      = UserSessionBean.getCurrentInstance().getSelectedMenuItem();    
    return selectedMenuItem.getMultiValuedProperty(ROOMS_PROPERTY);
  }    
  
  public List<SelectItem> getThemes()
  {
    return themeTypeBean.getSelectItems(getThemeIds());
  }
  
  public List<SelectItem> getLocations()
  {
    return createRoomSelectItems(eventFilter.getRoomId());
  }
  
  public void search()
  {
    basicSearchHelper.search();
  }

  private void setConfigurationFilter()
  {
    eventFilter = new EventFilter();
    eventFilter.setStartDateTime(TextUtils.formatDate(
      new Date(), "yyyyMMddHHmmss"));
    eventFilter.getThemeId().clear();
    eventFilter.getThemeId().addAll(getThemeIds());
    eventFilter.getEventTypeId().clear();
    eventFilter.getEventTypeId().addAll(getTypeIds());
    eventFilter.setSecurityMode(SecurityMode.FILTERED);
  }
  
  public class EventRow
  {

    private final String eventId;
    private final String eventTypeId;
    private final String eventTypeName;
    private final String summary;
    private final String startDateTime;
    private final String endDateTime;
    private final LocalDate startLocalDate;
    private final LocalDate endLocalDate;
    private String location = "";
    private String themes = "";

    public EventRow(EventView eventView)
    {
      this.eventId = eventView.getEventId();
      this.summary = eventView.getSummary();
      this.eventTypeId = eventView.getEventTypeId();
      this.eventTypeName = eventView.getEventTypeName();
      this.startDateTime = eventView.getStartDateTime();
      this.endDateTime = eventView.getEndDateTime();
      DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
      startLocalDate = LocalDate.parse(startDateTime, format);
      endLocalDate = LocalDate.parse(endDateTime, format);
      if (!eventView.getPlaces().isEmpty())
      {
        for (EventPlaceView eventPlaceView : eventView.getPlaces())
        {
          Place place = new Place(eventPlaceView);
          location = place.getDescription();
        }
      }
      if (!eventView.getThemes().isEmpty())
      {
        for (EventThemeView theme : eventView.getThemes())
        {
          if (theme.getThemeId() != null)
          {
            themes = themes + ", " + theme.getThemeId();
          }
        }
      }
    }

    public String getEventId()
    {
      return eventId;
    }

    public String getEventTypeId()
    {
      return eventTypeId;
    }

    public String getEventTypeName()
    {
      return eventTypeName;
    }

    public String getSummary()
    {
      return summary;
    }

    public String getStartDateTime()
    {
      return startDateTime;
    }

    public String getEndDateTime()
    {
      return endDateTime;
    }

    public LocalDate getStartLocalDate()
    {
      return startLocalDate;
    }

    public LocalDate getEndLocalDate()
    {
      return endLocalDate;
    }

    public String getLocation()
    {
      return location;
    }

    public String getThemes()
    {
      return themes;
    }

    public String getJsonParameters()
    {
      Map<String, String> params = new HashMap();
      params.put("eventid", eventId);
      return new Gson().toJson(params);
    }

  }

  //Group by month functions
  public boolean isGroupedByMonth()
  {
    String isGroupedProp = getProperty("groupByMonth");
    if (isGroupedProp != null)
    {
      return Boolean.parseBoolean(isGroupedProp);
    }
    return groupedByMonth;
  }

  public boolean isFirstOfMonth(int index)
  {
    List<?> currentRows = getRows();
    if (currentRows == null || index < 0 || index >= currentRows.size())
    {
      return false;
    }

    if (index == 0)
    {
      return true;
    }

    EventRow current = (EventRow) currentRows.get(index);
    EventRow previous = (EventRow) currentRows.get(index - 1);

    if (current.getStartLocalDate() == null || previous.getStartLocalDate() == null)
    {
      return false;
    }

    return !current.getStartLocalDate().getMonth().equals(previous.getStartLocalDate().getMonth())
      || current.getStartLocalDate().getYear() != previous.getStartLocalDate().getYear();
  }
  
  private List<SelectItem> createRoomSelectItems(String roomId)
  {
    List<SelectItem> result;
  
    List roomIdList = getRoomIds();
    if (roomIdList.isEmpty())
      result = roomTypeBean.getSelectItems();
    else
      result = roomTypeBean.getSelectItems(roomIdList);

    if (!result.isEmpty())
    {
      for (SelectItem item : result)
      {
        String id = (String)item.getValue();
        String label = item.getLabel();
        if (label.equals(roomTypeBean.getRootTypeId() + " " + id)) 
        { 
          //is not a room, is probably an address
          if (!StringUtils.isBlank(id))
          {
            String description = null;
            if (id.contains("\"") && id.endsWith("\""))
            {
              int idx = id.indexOf("\"");
              int lastIdx = id.lastIndexOf("\"");
              description = id.substring(idx + 1, lastIdx);
            }
            else
            {
              description =
                AddressDescriptionCache.getInstance().getAddressDescription(id);
            }

            if (description != null)
            {
              if (description.equals("NOT_FOUND"))
              {
                ResourceBundle bundle = ResourceBundle.getBundle(
                  "org.santfeliu.web.resources.MessageBundle", getLocale());
                description = bundle.getString(description) + " (" + id + ")";
              }
              item.setLabel(description);
              item.setDescription(description);
            }
          }
        }
      }
    }

    String sort = getProperty(SORT_EVENT_ROOM);
    if (sort != null && !"false".equals(sort))
      result = FacesUtils.sortSelectItems(result);

    return result;
  }  
   
}
