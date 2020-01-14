package org.santfeliu.agenda.web.view;

import java.util.Date;
import java.util.List;
import org.matrix.agenda.EventFilter;
import org.matrix.agenda.EventView;
import org.santfeliu.agenda.web.AgendaConfigBean;
import org.santfeliu.agenda.web.EventSearchBean;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.obj.GroupablePageBean;

/**
 *
 * @author blanquepa
 */
public class GroupedEventViewBean extends GroupablePageBean
  implements EventSearchView, Savable
{
  private EventSearchBean eventSearchBean;

  public GroupedEventViewBean()
  {
    eventSearchBean = (EventSearchBean)getBean("eventSearchBean");
    eventSearchBean.setEventViewBean(this);
    groupBy = "startDateTime";
  }

  public EventSearchBean getEventSearchBean()
  {
    return eventSearchBean;
  }

  public void setEventSearchBean(EventSearchBean eventSearchBean)
  {
    this.eventSearchBean = eventSearchBean;
  }

  public Date getGroupDate()
  {
    String dateTime = (String)getValue("#{group.description}");
    return TextUtils.parseInternalDate(dateTime);
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

  public String getTypeDescription()
  {
    EventView row = (EventView)getValue("#{row}");
    if (row != null)
    {
      String rowEventTypeId = row.getEventTypeId();

      TypeCache typeCache = TypeCache.getInstance();
      Type type = typeCache.getType(rowEventTypeId);

      if (type != null)
        return type.getDescription();
      else
        return rowEventTypeId;
    }
    else
      return "";
  }

  @Override
  public String show()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public String search(EventFilter eventFilter)
  {
    try
    {
      eventFilter.setMaxResults(1000);
      eventFilter.setReducedInfo(Boolean.TRUE);
      List rows = 
        AgendaConfigBean.getPort().findEventViewsFromCache(eventFilter);
      setGroups(rows, getGroupExtractor());
    }
    catch (Exception ex)
    {
      error(ex);
    }

    return null;
  }

  public String showEvent()
  {
    return getControllerBean().showObject("Event",
      (String)getValue("#{row.eventId}"));
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  @Override
  public GroupExtractor getGroupExtractor()
  {
    if (groupBy == null)
      return null;
    else
      return new StartDateGroupExtractor();
  }

  protected class StartDateGroupExtractor extends GroupExtractor
  {
    @Override
    public Group getGroup(Object view)
    {
      if (view instanceof EventView)
      {
        EventView row = (EventView)view;
        if (row != null)
        {
          String startDate = row.getStartDateTime();
          if (startDate != null)
            startDate = startDate.substring(0, 8);
          return new Group(startDate, startDate);
        }
        else return NULL_GROUP;
      }
      else
        return NULL_GROUP;
    }
  }
}
