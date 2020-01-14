package org.santfeliu.agenda.web.view;

import java.util.Date;
import java.util.List;
import org.matrix.agenda.EventFilter;
import org.matrix.agenda.EventView;
import org.santfeliu.agenda.web.AgendaConfigBean;
import org.santfeliu.agenda.web.EventSearchBean;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.obj.BasicSearchBean;

/**
 *
 * @author blanquepa
 */
public class TableEventViewBean extends BasicSearchBean implements EventSearchView
{
  private EventSearchBean eventSearchBean;
  private EventFilter eventFilter; //copy to process in getResults method (after search)

  public TableEventViewBean()
  {
    eventSearchBean = (EventSearchBean)getBean("eventSearchBean");
    eventSearchBean.setEventViewBean(this);
  }

  public EventSearchBean getEventSearchBean()
  {
    return eventSearchBean;
  }

  public void setEventSearchBean(EventSearchBean eventSearchBean)
  {
    this.eventSearchBean = eventSearchBean;
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
      eventFilter.setReducedInfo(Boolean.TRUE);
      List results = AgendaConfigBean.getPort().findEventViewsFromCache(eventFilter);

      return results;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  @Override
  public String show()
  {
    throw new UnsupportedOperationException("Not supported yet.");
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
    EventView row = (EventView) getValue("#{row}");
    if (row != null)
    {
      String startDateTime = row.getStartDateTime();
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
    EventView row = (EventView) getValue("#{row}");
    if (row != null)
    {
      String startDateTime = row.getStartDateTime();
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
    EventView row = (EventView) getValue("#{row}");
    if (row != null)
    {
      String endDateTime = row.getEndDateTime();
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

}
