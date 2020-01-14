package org.santfeliu.agenda.web.view;

import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.matrix.agenda.EventFilter;
import org.matrix.agenda.EventPlaceView;
import org.matrix.agenda.EventView;
import org.santfeliu.agenda.Place;
import org.santfeliu.agenda.web.AgendaConfigBean;
import org.santfeliu.agenda.web.EventSearchBean;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.obj.BasicSearchBean;
/**
 *
 * @author blanquepa
 */
public class LinkedEventViewBean extends BasicSearchBean
  implements EventSearchView
{
  private EventSearchBean eventSearchBean;
  private EventFilter eventFilter;
  private String currentTypeId;


  public LinkedEventViewBean()
  {
    eventFilter = new EventFilter();
    eventSearchBean = (EventSearchBean)getBean("eventSearchBean");
    eventSearchBean.setEventViewBean(this);
  }

  public String getCurrentTypeId()
  {
    if (eventFilter.getEventTypeId() != null &&
        eventFilter.getEventTypeId().size() == 1)
    {
      return eventFilter.getEventTypeId().get(0);
    }
    else
      return currentTypeId;
  }

  public void setCurrentTypeId(String currentTypeId)
  {
    this.currentTypeId = currentTypeId;
    eventFilter.getEventTypeId().clear();
    eventFilter.getEventTypeId().add(currentTypeId);
  }

  public int countResults()
  {
    try
    {
      return AgendaConfigBean.getPort().countEventsFromCache(eventFilter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return 0;
  }

  public List getResults(int firstResult, int maxResults)
  {
    try
    {
      eventFilter.setFirstResult(firstResult);
      eventFilter.setMaxResults(maxResults);
      eventFilter.setReducedInfo(Boolean.FALSE);
      List results = AgendaConfigBean.getPort().findEventViewsFromCache(eventFilter);

      return results;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String search(EventFilter eventFilter)
  {
    this.eventFilter = copyEventFilter(eventFilter);
    return search();
  }

  public String showEvent()
  {
    return getControllerBean().showObject("Event",
      (String)getValue("#{row.eventId}"));
  }

  public Date getStartDateTime()
  {
    EventView eventView = (EventView) getValue("#{row}");
    if (eventView != null)
    {
      String startDateTime = eventView.getStartDateTime();
      try
      {
        return TextUtils.parseInternalDate(startDateTime);
      }
      catch (Exception e)
      {
        return null;
      }
    }
    else
      return null;
  }

  public Date getStartDate()
  {
    EventView eventView = (EventView) getValue("#{row}");
    if (eventView != null)
    {
      String startDateTime = eventView.getStartDateTime();
      try
      {
        if (startDateTime != null)
          startDateTime = startDateTime.substring(0, 8);
        return TextUtils.parseInternalDate(startDateTime);
      }
      catch (Exception e)
      {
        return null;
      }
    }
    else
      return null;
  }

  public Date getEndDateTime()
  {
    EventView eventView = (EventView)getValue("#{row}");
    if (eventView != null)
    {
      String endDateTime = eventView.getEndDateTime();
      try
      {
        return TextUtils.parseInternalDate(endDateTime);
      }
      catch (Exception e)
      {
        return null;
      }
    }
    else
      return null;
  }

  public Place getPlace()
  {
    Place place = null;
    EventView eventView = (EventView)getValue("#{row}");
    if (eventView != null)
    {
      List<EventPlaceView> placeViews = eventView.getPlaces();
      if (placeViews != null && !placeViews.isEmpty())
      {
        for (EventPlaceView placeView : placeViews)
        {
          place = new Place(placeView);
          if (place.isRoom() && place.getPlaceId().equals(eventFilter.getRoomId()))
            return place;
        }
      }
    }
    
    return place;
  }

  public String getDateTimeParameters()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(getBaseURL());
    EventView eventView = (EventView)getValue("#{row}");
    if (eventView != null)
    {
      sb.append("&startdatetime=");
      sb.append(eventView.getStartDateTime().substring(0, 8));
      sb.append(getEventFilterParameters("datetime"));
    }
    return sb.toString();
  }

  public String getTypeIdParameters()
  {
    EventView eventView = (EventView)getValue("#{row}");
    StringBuilder sb = new StringBuilder();
    sb.append(getBaseURL());
    if (eventView != null)
    {
      sb.append("&eventtypeid=");
      sb.append(eventView.getEventTypeId());
      sb.append(getEventFilterParameters("eventtypeid"));
    }
    return sb.toString();
  }

  public String getRoomParameters()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(getBaseURL());
    sb.append("&roomid=");
    Place place = getPlace();
    if (place != null)
      sb.append(getPlace().getPlaceId());
    sb.append(getEventFilterParameters("roomid"));
    return sb.toString();
  }

  public String getBaseURL()
  {
    return "go.faces?xmid=" + getSelectedMenuItem().getMid();
  }

  public String getEventFilterParameters(String main)
  {
    StringBuilder sb = new StringBuilder();
    if (!StringUtils.isBlank(eventFilter.getStartDateTime()) && !"datetime".equals(main))
      sb.append("&startdatetime=").append(eventFilter.getStartDateTime().substring(0, 8));
    if (!StringUtils.isBlank(eventFilter.getEndDateTime()) && !"datetime".equals(main))
      sb.append("&enddatetime=").append(eventFilter.getEndDateTime().substring(0, 8));
    if (!StringUtils.isBlank(getCurrentTypeId()) && !"eventtypeid".equals(main))
      sb.append("&eventtypeid=").append(getCurrentTypeId());
    if (!StringUtils.isBlank(eventFilter.getRoomId()) && !"roomid".equals(main))
      sb.append("&roomid=").append(eventFilter.getRoomId());
    if (!StringUtils.isBlank(eventFilter.getContent()))
      sb.append("&content=").append(eventFilter.getContent());
    if (!StringUtils.isBlank(eventFilter.getPersonId()))
      sb.append("&personid=").append(eventFilter.getPersonId());
    if (!StringUtils.isBlank(eventSearchBean.getThemeId()))
      sb.append("&themeid=").append(eventSearchBean.getThemeId());
    sb.append("&oc=");
    sb.append(eventSearchBean.getOnlyCurrentDate());

    return sb.toString();
  }

  private EventFilter copyEventFilter(EventFilter eventFilter)
  {
    EventFilter copy = new EventFilter();
    copy.setContent(eventFilter.getContent());
    copy.setEndChangeDateTime(eventFilter.getEndChangeDateTime());
    copy.setEndDateTime(eventFilter.getEndDateTime());
    copy.getEventId().addAll(eventFilter.getEventId());
    copy.setPersonId(eventFilter.getPersonId());
    copy.setRoomId(eventFilter.getRoomId());
    copy.setSecurityMode(eventFilter.getSecurityMode());
    copy.setStartChangeDateTime(eventFilter.getStartChangeDateTime());
    copy.setStartDateTime(eventFilter.getStartDateTime());
    copy.getOrderBy().addAll(eventFilter.getOrderBy());
    copy.getEventTypeId().addAll(eventFilter.getEventTypeId());
    copy.getThemeId().addAll(eventFilter.getThemeId());
    copy.getProperty().addAll(eventFilter.getProperty());
    copy.setDateComparator(eventFilter.getDateComparator());
    return copy;
  }

  @Override
  public String show()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
