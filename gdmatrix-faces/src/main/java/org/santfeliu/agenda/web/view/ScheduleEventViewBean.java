package org.santfeliu.agenda.web.view;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import org.apache.myfaces.custom.schedule.ScheduleMouseEvent;
import org.apache.myfaces.custom.schedule.model.ScheduleDay;
import org.apache.myfaces.custom.schedule.model.ScheduleEntry;
import org.apache.myfaces.custom.schedule.model.ScheduleModel;
import org.apache.myfaces.custom.schedule.model.SimpleScheduleModel;
import org.matrix.agenda.EventFilter;
import org.matrix.agenda.EventView;
import org.matrix.dic.DictionaryConstants;
import org.santfeliu.agenda.web.AgendaConfigBean;
import org.santfeliu.agenda.web.EventMainBean;
import org.santfeliu.agenda.web.EventSearchBean;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author blanquepa
 */
public class ScheduleEventViewBean extends EventViewBean
  implements ScheduleEventSearchView, Savable
{
  private EventSearchBean eventSearchBean;
  private ScheduleModel model;
  private static final ScheduleEntryRenderer entryRenderer =
    new ScheduleEntryRenderer();

  public ScheduleEventViewBean()
  {
    eventSearchBean = (EventSearchBean)getBean("eventSearchBean");
    eventSearchBean.setEventViewBean(this);
    model = new SimpleScheduleModel();
    model.setSelectedDate(new Date());
    setScheduleMode(null);
    model.refresh();
  }

  public ScheduleModel getModel()
  {
    return model;
  }

  public void setModel(ScheduleModel model)
  {
    this.model = model;
  }

  public ScheduleEntryRenderer getEntryRenderer()
  {
    return entryRenderer;
  }

  public String getMode()
  {
    return String.valueOf(model.getMode());
  }

  public void setMode(String mode)
  {
    model.setMode(Integer.parseInt(mode));
  }

  public String setMonthMode()
  {
    return setScheduleMode(ScheduleModel.MONTH);
  }

  public String setDayMode()
  {
    return setScheduleMode(ScheduleModel.DAY);
  }

  public String setWeekMode()
  {
    return setScheduleMode(ScheduleModel.WEEK);
  }

  public String setWorkweekMode()
  {
    return setScheduleMode(ScheduleModel.WORKWEEK);
  }

  public String previous()
  {
    Calendar referenceDate = new GregorianCalendar();
    referenceDate.setTime(model.getSelectedDate());
    switch (model.getMode())
    {
      case ScheduleModel.WORKWEEK:
      case ScheduleModel.WEEK:
          referenceDate.add(Calendar.DATE, -7);
          break;
      case ScheduleModel.MONTH:
          referenceDate.add(Calendar.MONTH, -1);
          break;
      case ScheduleModel.DAY:
          referenceDate.add(Calendar.DATE, -1);
          break;
    }
    eventSearchBean.setSelectedDate(referenceDate.getTime());
    return eventSearchBean.search();
  }

  public String next()
  {
    Calendar referenceDate = new GregorianCalendar();
    referenceDate.setTime(model.getSelectedDate());
    switch (model.getMode())
    {
      case ScheduleModel.WORKWEEK:
      case ScheduleModel.WEEK:
          referenceDate.add(Calendar.DATE, 7);
          break;
      case ScheduleModel.MONTH:
          referenceDate.add(Calendar.MONTH, 1);
          break;
      case ScheduleModel.DAY:
          referenceDate.add(Calendar.DATE, 1);
          break;
    }
    eventSearchBean.setSelectedDate(referenceDate.getTime());
    return eventSearchBean.search();
  }

  public String today()
  {
    Date today = new Date();
    eventSearchBean.setSelectedDate(today);
    return eventSearchBean.search();
  }

  public String search(EventFilter eventFilter)
  {
    try
    {
      Iterator it = ((SimpleScheduleModel)model).iterator();
      while (it.hasNext())
      {
        ScheduleDay scheduleDay = (ScheduleDay)it.next();
        Iterator it2 = scheduleDay.iterator();
        while (it2.hasNext())
        {
          ScheduleEntry entry = (ScheduleEntry)it2.next();
          model.removeEntry(entry);
        }
      }

      Date startDate = model.getSelectedDate();
      Date endDate = model.getSelectedDate();
      Calendar referenceDate = new GregorianCalendar();
      referenceDate.setTime(model.getSelectedDate());
      referenceDate.setFirstDayOfWeek(Calendar.MONDAY);

      if (eventSearchBean.getScheduleMode() != null)
        setScheduleMode(eventSearchBean.getScheduleMode());
      switch (model.getMode())
      {
        case ScheduleModel.WORKWEEK:
          referenceDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
          startDate = referenceDate.getTime();
          referenceDate.add(Calendar.DATE, 5);
          endDate = referenceDate.getTime();
          break;
        case ScheduleModel.WEEK:
          referenceDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
          startDate = referenceDate.getTime();
          referenceDate.add(Calendar.DATE, 7);
          endDate = referenceDate.getTime();
          break;
        case ScheduleModel.MONTH:
          referenceDate.set(Calendar.DAY_OF_MONTH, 1);
          referenceDate.add(Calendar.DATE, -7); // a week before
          startDate = referenceDate.getTime();
          referenceDate.add(Calendar.DATE, 38); // a week after
          endDate = referenceDate.getTime();
          break;
      }

      String startDateTime =
        TextUtils.formatDate(startDate, "yyyyMMdd") + "000000";
      String endDateTime =
        TextUtils.formatDate(endDate, "yyyyMMdd") + "235959";
      eventFilter.setStartDateTime(startDateTime);
      eventFilter.setEndDateTime(endDateTime);
      eventFilter.setReducedInfo(Boolean.TRUE);

      List<EventView> events =
        AgendaConfigBean.getPort().findEventViewsFromCache(eventFilter);
      for (EventView event : events)
      {
        EventViewScheduleEntry entry = new EventViewScheduleEntry();
        entry.setId(event.getEventId());

        startDate = TextUtils.parseInternalDate(event.getStartDateTime());
        entry.setStartTime(startDate);
        endDate = TextUtils.parseInternalDate(event.getEndDateTime());
        entry.setEndTime(endDate);
        entry.setTitle(event.getSummary());
        entry.setDescription(event.getDescription());
        entry.setEditable(event.isEditable());
        model.addEntry(entry);
        Date currentDate = new Date();
        if (currentDate.after(startDate) && currentDate.before(endDate))
          model.setSelectedEntry(entry);
      }
      model.refresh();
    }
    catch (Exception ex)
    {
      error(ex);
    }    
    return null;
  }

  public String getHeaderDate()
  {
    Date date = model.getSelectedDate();
    String pattern;
    if (model.getMode() == ScheduleModel.DAY)
    {
      pattern = "EEEE, dd MMMM yyyy";
    }
    else if (model.getMode() == ScheduleModel.MONTH)
    {
      pattern = "LLLL yyyy";
    }
    else
    {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      calendar.setFirstDayOfWeek(Calendar.MONDAY);
      int weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH);
      String weekOfMonthLabel = (String)getValue("#{agendaBundle.weekOfMonth}");
      pattern = "LLLL yyyy, '" + weekOfMonthLabel + " " + weekOfMonth + "'";
    }
    Locale locale = getFacesContext().getViewRoot().getLocale();
    return TextUtils.formatDate(date, pattern, locale);
  }

  public String getSelectedDayStyle()
  {
    Date date = model.getSelectedDate();
    String dateString = TextUtils.formatDate(date, "yyyyMMdd");
    return "<style type=\"text/css\">a#mainform\\3Aschedule_header_" +
     dateString + "{font-weight:bold}</style>";
  }

  public boolean isEditable()
  {
    boolean editable = false;
    try
    {
      EventViewScheduleEntry entry =
        (EventViewScheduleEntry)model.getSelectedEntry();
      boolean isEntryEditable = entry != null ? entry.isEditable() : true;
      editable = (isEntryEditable || eventSearchBean.isShowEventAllowed());
    }
    catch (Exception ex)
    {
    }
    return editable;
  }

  public String selectEvent()
  {
    ScheduleEntry entry = model.getSelectedEntry();
    if (entry != null)
    {
      String eventId = entry.getId();
      String selectMode = getEventSearchBean().getScheduleSelectMode();
      if ("edit".equalsIgnoreCase(selectMode))
      {
        // show event edit screen
        return eventSearchBean.showEvent(eventId);
      }
      else if ("select".equalsIgnoreCase(selectMode))
      {
        // only select event
      }
      else
      {
        // show event detail screen, non editable view (default value)
        return eventSearchBean.showDetail(eventId);
      }
    }
    return null;
  }

  public String showEvent()
  {
    ScheduleEntry entry = model.getSelectedEntry();
    if (entry != null)
    {
      String eventId = entry.getId();
      return eventSearchBean.showDetail(eventId);
    }
    return null;
  }

  public String editEvent()
  {
    ScheduleEntry entry = model.getSelectedEntry();
    if (entry != null)
    {
      String eventId = entry.getId();
      return eventSearchBean.showEvent(eventId);
    }
    return null;
  }

  public void mouseListener(ScheduleMouseEvent event)
  {
    int eventType = event.getEventType();
    if (eventType == ScheduleMouseEvent.SCHEDULE_HEADER_CLICKED)
    {
      showDay(event.getClickedDate());
    }
    else if (eventType == ScheduleMouseEvent.SCHEDULE_BODY_CLICKED)
    {
      if (isEditable())
      {
        String outcome = eventSearchBean.getControllerBean().createObject(
          DictionaryConstants.EVENT_TYPE);
        EventMainBean eventMainBean = (EventMainBean)getBean("eventMainBean");
        Date date = roundDate(event.getClickedDate());
        String startDateTime = TextUtils.formatDate(date, "yyyyMMddHHmmss");
        eventMainBean.getEvent().setStartDateTime(startDateTime);

        FacesContext facesContext = getFacesContext();
        NavigationHandler navigationHandler =
          facesContext.getApplication().getNavigationHandler();
        navigationHandler.handleNavigation(facesContext, null, outcome);
        facesContext.renderResponse();
      }
      else
      {
        showDay(event.getClickedDate());
      }
    }
  }

  public Date getSelectedDate()
  {
    return model.getSelectedDate();
  }

  public void setSelectedDate(Date date)
  {
    model.setSelectedDate(date);
  }

  public boolean isRenderScheduleModeSelection()
  {
    return (eventSearchBean.getScheduleMode() == null);
  }

  // private methods

  private void showDay(Date date)
  {
    eventSearchBean.setSelectedDate(date);
    model.setSelectedDate(date);
    model.setMode(ScheduleModel.DAY);
  }

  private String setScheduleMode(int mode)
  {
    ScheduleEntry selectedEntry = model.getSelectedEntry();
    if (selectedEntry != null)
    {
      Date date = selectedEntry.getStartTime();
      model.setSelectedDate(date);
      eventSearchBean.setSelectedDate(date);
    }
    model.setMode(mode);
    return eventSearchBean.search();
  }

  private void setScheduleMode(String scheduleMode)
  {
    if ("DAY".equalsIgnoreCase(scheduleMode))
      model.setMode(ScheduleModel.DAY);
    else if ("WEEK".equalsIgnoreCase(scheduleMode))
      model.setMode(ScheduleModel.WEEK);
    else if ("WORKWEEK".equalsIgnoreCase(scheduleMode))
      model.setMode(ScheduleModel.WORKWEEK);
    else
      model.setMode(ScheduleModel.MONTH);
  }

  private Date roundDate(Date date)
  {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    int minutes = calendar.get(Calendar.MINUTE);
    minutes = (minutes / 30) * 30;
    calendar.set(Calendar.MINUTE, minutes);
    return calendar.getTime();
  }
}
