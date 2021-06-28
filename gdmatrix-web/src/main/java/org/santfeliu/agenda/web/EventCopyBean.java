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
package org.santfeliu.agenda.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import javax.faces.model.SelectItem;
import org.apache.commons.lang.StringUtils;
import org.matrix.agenda.AgendaManagerPort;
import org.matrix.agenda.Attendant;
import org.matrix.agenda.AttendantFilter;
import org.matrix.agenda.Event;
import org.matrix.agenda.EventDocument;
import org.matrix.agenda.EventDocumentFilter;
import org.matrix.agenda.EventDocumentView;
import org.matrix.agenda.EventFilter;
import org.matrix.agenda.EventPlace;
import org.matrix.agenda.EventPlaceFilter;
import org.matrix.agenda.EventTheme;
import org.matrix.agenda.EventThemeFilter;
import org.matrix.agenda.EventThemeView;
import org.matrix.agenda.SecurityMode;
import org.matrix.cases.CaseEvent;
import org.matrix.cases.CaseEventFilter;
import org.matrix.cases.CaseEventView;
import org.matrix.cases.CaseManagerPort;
import org.matrix.dic.Property;
import org.santfeliu.cases.web.CaseConfigBean;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.PojoUtils;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.obj.PageBean;

/**
 *
 * @author blanquepa
 */
public class EventCopyBean extends PageBean implements Serializable
{
  private int firstRowIndex = 0;

  //frequencyMode
  public static final int DAYLY = 0;
  public static final int WEEKLY = 1;
  public static final int MONTHLY = 2;
  public static final int YEARLY = 3;

  private Event event;
  private List<EventPlace> places;
  private List<Attendant> attendants;
  private List<EventThemeView> themes;
  private List<EventDocumentView> documents;
  private List<CaseEventView> cases;
  private String masterEventId;

  private List<EventRow> rows;
  private EventRow editingRow;
  private EventRow editedRow;

  private int selectedIndex;
  private int frequencyMode;

  //Dayly
  public static final String BY_FREQUENCY_VALUE = "0";
  private String daylyFrequencyMode = BY_FREQUENCY_VALUE;
  private int daylyFrequencyValue = 1;

  //Weekly
  private int weeklyFrequencyValue;
  private List<String> daysOfWeek;

  //Monthly
  public static final String BY_SELECTED_DAY = "0";
  private String monthlyFrequencyMode = BY_SELECTED_DAY;
  private int monthlyFrequencyValue0;
  private int monthlyFrequencyValue1;

  //Monthly & Yearly
  private int dayOfMonth;
  private String weekPosition;
  private String dayOfWeekType;

  //Yearly
  private String yearlyFrequencyMode = BY_SELECTED_DAY;
  private String month0;
  private String month1;

  //Range
  public static final String BY_MAX_DATE = "0";
  public static final String BY_MAX_ITERATIONS = "1";
  public static final int DEFAULT_MAX_RECURRENCES = 100;
  public static final String MAX_RECURRENCES_PROPERTY = "agenda.maxRecurrences";
  
  private String rangeStartDateTime;
  private int numberOfIterations;
  private String endRangeMode = BY_MAX_DATE;
  private String rangeEndDateTime;
  private boolean checkAttendantsAvailability;
  private int maxRecurrences;

  //Hours pattern
  private HourPattern hourPattern;
  private String duration;
  private String durationSelector;
  private String gap;
  private String gapSelector;
  private static final String MINUTES_SELECTOR = "m";
  private static final String HOURS_SELECTOR = "h";
  private static final String DAYS_SELECTOR = "d";

  //Set related data copy
  private boolean copyAttendants = true;
  private boolean copyPlaces = true;
  private boolean copyThemes = true;
  private boolean copyDocuments = true;
  private boolean copyCases = true;

  public EventCopyBean()
  {
    init();
  }
  
  private void init()
  {
    maxRecurrences = DEFAULT_MAX_RECURRENCES;
    String maxRecurrencesValue = 
      MatrixConfig.getProperty(MAX_RECURRENCES_PROPERTY);
    if (maxRecurrencesValue != null)
    {
      try
      {
        maxRecurrences = Integer.valueOf(maxRecurrencesValue);
      }
      catch (NumberFormatException ex)
      {
        ResourceBundle bundle = ResourceBundle.getBundle(
          "org.santfeliu.agenda.web.resources.AgendaBundle", getLocale()); 
        String[] params = {maxRecurrencesValue};
        warn(bundle.getString("eventCopy_invalidRecurrencesCount"), params);
      }
    }    
  }

  public void setEvent(Event event)
  {
//    this.event = event;
    this.event = new Event();
    PojoUtils.copy(event, this.event);
    if (event != null)
    {
      this.rangeStartDateTime = event.getStartDateTime();
      this.rangeEndDateTime = event.getEndDateTime();
      long time =
        getTimeBetweenDates(event.getStartDateTime(), event.getEndDateTime());
      this.duration = String.valueOf(time);
      this.durationSelector = MINUTES_SELECTOR;
      this.gap = "0";
      this.gapSelector = MINUTES_SELECTOR;
      this.hourPattern =
        new HourPattern(event.getStartDateTime(), event.getEndDateTime(), time);
      setEventProperties(event);
    }
  }

  public Event getEvent()
  {
    return event;
  }

  public List<EventPlace> getPlaces()
  {
    return places;
  }

  public String getMasterEventId()
  {
    return masterEventId;
  }

  public void setMasterEventId(String masterEventId)
  {
    this.masterEventId = masterEventId;
  }

  public EventRow getEditingRow()
  {
    return editingRow;
  }

  public void setEditingRow(EventRow editingRow)
  {
    this.editingRow = editingRow;
  }

  public int getSelectedIndex()
  {
    return selectedIndex;
  }

  public void setSelectedIndex(int selectedIndex)
  {
    this.selectedIndex = selectedIndex;
  }

  public List<EventRow> getRows()
  {
    return rows;
  }

  public void setRows(List<EventRow> rows)
  {
    this.rows = rows;
  }

  public int getFrequencyMode()
  {
    return frequencyMode;
  }

  public int getDayOfMonth()
  {
    return dayOfMonth;
  }

  public void setDayOfMonth(int dayOfMonth)
  {
    this.dayOfMonth = dayOfMonth;
  }

  public String getDayOfWeekType()
  {
    return dayOfWeekType;
  }

  public void setDayOfWeekType(String dayOfWeekType)
  {
    this.dayOfWeekType = dayOfWeekType;
  }

  public String getDaylyFrequencyMode()
  {
    return daylyFrequencyMode;
  }

  public void setDaylyFrequencyMode(String daylyFrequencyMode)
  {
    this.daylyFrequencyMode = daylyFrequencyMode;
  }

  public int getDaylyFrequencyValue()
  {
    return daylyFrequencyValue;
  }

  public void setDaylyFrequencyValue(int daylyFrequencyValue)
  {
    this.daylyFrequencyValue = daylyFrequencyValue;
  }

  public List<String> getDaysOfWeek()
  {
    return daysOfWeek;
  }

  public void setDaysOfWeek(List<String> daysOfWeek)
  {
    this.daysOfWeek = daysOfWeek;
  }

  public String getMonthlyFrequencyMode()
  {
    return monthlyFrequencyMode;
  }

  public void setMonthlyFrequencyMode(String monthlyFrequencyMode)
  {
    this.monthlyFrequencyMode = monthlyFrequencyMode;
  }

  public int getMonthlyFrequencyValue0()
  {
    return monthlyFrequencyValue0;
  }

  public void setMonthlyFrequencyValue0(int monthlyFrequencyValue)
  {
    this.monthlyFrequencyValue0 = monthlyFrequencyValue;
  }

  public int getMonthlyFrequencyValue1()
  {
    return monthlyFrequencyValue1;
  }

  public void setMonthlyFrequencyValue1(int monthlyFrequencyValue)
  {
    this.monthlyFrequencyValue1 = monthlyFrequencyValue;
  }

  public String getWeekPosition()
  {
    return weekPosition;
  }

  public void setWeekPosition(String weekPosition)
  {
    this.weekPosition = weekPosition;
  }

  public int getWeeklyFrequencyValue()
  {
    return weeklyFrequencyValue;
  }

  public void setWeeklyFrequencyValue(int weeklyFrequencyValue)
  {
    this.weeklyFrequencyValue = weeklyFrequencyValue;
  }

  public String getYearlyFrequencyMode()
  {
    return yearlyFrequencyMode;
  }

  public void setYearlyFrequencyMode(String yearlyFrequencyMode)
  {
    this.yearlyFrequencyMode = yearlyFrequencyMode;
  }

  public void setFrequencyMode(int frequencyMode)
  {
    this.frequencyMode = frequencyMode;
  }

  public int getNumberOfIterations()
  {
    return numberOfIterations;
  }

  public void setNumberOfIterations(int numberOfIterations)
  {
    this.numberOfIterations = numberOfIterations;
  }

  public String getEndRangeMode()
  {
    return endRangeMode;
  }

  public void setEndRangeMode(String endRangeMode)
  {
    this.endRangeMode = endRangeMode;
  }

  public String getRangeEndDateTime()
  {
    return rangeEndDateTime;
  }

  public Date getRangeStartDate()
  {
    if (rangeStartDateTime != null)
      return TextUtils.parseInternalDate(rangeStartDateTime);
    else
      return null;
  }

  public void setRangeEndDateTime(String rangeEndDateTime)
  {
    this.rangeEndDateTime = rangeEndDateTime;
  }

  public String getRangeStartDateTime()
  {
    return rangeStartDateTime;
  }

  public void setRangeStartDateTime(String rangeStartDateTime)
  {
    this.rangeStartDateTime = rangeStartDateTime;
  }

  public String getDuration()
  {
    long d = hourPattern.getDuration();
    if (durationSelector.equals(HOURS_SELECTOR))
      d = TimeUnit.HOURS.convert(d, TimeUnit.MINUTES);
    else if (durationSelector.equals(DAYS_SELECTOR))
      d = TimeUnit.DAYS.convert(d, TimeUnit.MINUTES);
    
    return String.valueOf(d);
  }

  public void setDuration(String duration)
  {
    this.duration = duration;
  }

  public String getGap()
  {
    long g = hourPattern.getGap();
    if (gapSelector.equals(HOURS_SELECTOR))
      g = TimeUnit.HOURS.convert(g, TimeUnit.MINUTES);
    else if (durationSelector.equals(DAYS_SELECTOR))
      g = TimeUnit.DAYS.convert(g, TimeUnit.MINUTES);

    return String.valueOf(g);
  }

  public void setGap(String gap)
  {
    this.gap = gap;
  }

  public String getDurationSelector()
  {
    return durationSelector;
  }

  public void setDurationSelector(String durationSelector)
  {
    this.durationSelector = durationSelector;
  }

  public String getGapSelector()
  {
    return gapSelector;
  }

  public void setGapSelector(String gapSelector)
  {
    this.gapSelector = gapSelector;
  }

  public String getStartHour()
  {
    return hourPattern.getFormattedStartHour();
  }

  public void setStartHour(String startHour)
  {
    this.hourPattern.setFormattedStartHour(startHour);
  }

  public String getEndHour()
  {
    return hourPattern.getFormattedEndHour();
  }

  public void setEndHour(String endHour)
  {
    this.hourPattern.setFormattedEndHour(endHour);
  }

  public List<Attendant> getAttendants()
  {
    return attendants;
  }

  public void setAttendants(List<Attendant> attendants)
  {
    this.attendants = attendants;
  }

  public boolean isCheckAttendantsAvailability()
  {
    return checkAttendantsAvailability;
  }

  public void setCheckAttendantsAvailability(boolean checkAttendantsAvailability)
  {
    this.checkAttendantsAvailability = checkAttendantsAvailability;
  }

  public boolean isCopyAttendants()
  {
    return copyAttendants;
  }

  public void setCopyAttendants(boolean copyAttendants)
  {
    this.copyAttendants = copyAttendants;
  }

  public boolean isCopyPlaces()
  {
    return copyPlaces;
  }

  public void setCopyPlaces(boolean copyPlaces)
  {
    this.copyPlaces = copyPlaces;
  }

  public boolean isCopyThemes()
  {
    return copyThemes;
  }

  public void setCopyThemes(boolean copyThemes)
  {
    this.copyThemes = copyThemes;
  }

  public boolean isCopyDocuments()
  {
    return copyDocuments;
  }

  public void setCopyDocuments(boolean copyDocuments)
  {
    this.copyDocuments = copyDocuments;
  }
  
  public boolean isCopyCases()
  {
    return this.copyCases;
  }
          
  public void setCopyEvents(boolean copyCases)
  {
    this.copyCases = copyCases;
  }

  public List<SelectItem> getMonthsSelectItems()
  {
    List<SelectItem> result = new ArrayList<SelectItem>();
    for (int i = Calendar.JANUARY; i <= Calendar.DECEMBER; i++)
    {
      Locale locale = getFacesContext().getViewRoot().getLocale();
      Calendar c = Calendar.getInstance();
      c.set(Calendar.MONTH, i);
      SelectItem item = 
        new SelectItem(String.valueOf(i),
          TextUtils.getStandaloneMonthName(c, Calendar.MONTH, locale));
      result.add(item);
    }
    return result;
  }

  public String getMonth0()
  {
    return month0;
  }

  public void setMonth0(String month)
  {
    this.month0 = month;
  }

  public String getMonth1()
  {
    return month1;
  }

  public void setMonth1(String month)
  {
    this.month1 = month;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  public int getFirstRowIndex()
  {
    return firstRowIndex;
  }

  public void setFirstRowIndex(int firstRowIndex)
  {
    this.firstRowIndex = firstRowIndex;
  }

  public int getMaxRecurrencesCount()
  {
    return maxRecurrences;
  }
  
  //GUI actions
  @Override
  public String show()
  {
    buildPreview();
    return null;
  }
  
  public String reset()
  {
    resetFilter();
    resetPreview();

    return null;
  }

  public String copy()
  {
    try
    {
      if (rows != null)
      {
        copyRecurrences(true);
        resetPreview();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }

    return null;
  }

  public String editEvent()
  {
    editingRow = (EventRow)getValue("#{row}");
    Event e = new Event();
    PojoUtils.copy(editingRow.getEvent(), e);
    editedRow = new EventRow(e);

    return null;
  }

  public String removeEvent()
  {
    EventRow row = (EventRow)getValue("#{row}");
    rows.remove(row);

    return null;
  }

  public String storeEvent()
  {
    //set room & attendants availability
    int index = rows.indexOf(editingRow);
    rows.get(index).checkRoomsAvailability(editingRow.getEvent());
    if (checkAttendantsAvailability)
      rows.get(index).checkAttendantsAvailability(editingRow.getEvent());
    editingRow = null;
    return null;
  }

  public String cancelEvent()
  {
    int index = rows.indexOf(editingRow);
    rows.set(index, editedRow);
    editingRow = null;
    return null;
  }

  //Other actions
  /**
   * Creates a copy of current Event.
   *
   * @param timeOffset offset in minutes from event startDate.
   * @param duration in minutes of the event to be created.
   * @return The created event
   * @throws java.lang.Exception
   */
  public EventRow duplicate(long timeOffset, long duration) throws Exception
  {
    EventRow copy = null;

    if (timeOffset != 0)
    {
      Date date = TextUtils.parseInternalDate(event.getStartDateTime());

      date.setTime(date.getTime() +
        TimeUnit.MILLISECONDS.convert(timeOffset, TimeUnit.MINUTES));
      setRangeStartDateTime(TextUtils.formatDate(date, "yyyyMMddHHmmss"));
      setStartHour(TextUtils.formatDate(date, "HH:mm"));

      date.setTime(date.getTime() +
        TimeUnit.MILLISECONDS.convert(duration, TimeUnit.MINUTES));
      setRangeEndDateTime(TextUtils.formatDate(date, "yyyyMMddHHmmss"));
      setEndHour(TextUtils.formatDate(date, "HH:mm"));
    }
    if (duration > 0)
    {
      setDurationSelector(MINUTES_SELECTOR);
      setDuration(String.valueOf(duration));
    }

    //set parameters
    setFrequencyMode(EventCopyBean.DAYLY);
    setDaylyFrequencyMode(EventCopyBean.BY_FREQUENCY_VALUE);
    setDaylyFrequencyValue(1);
    setEndRangeMode(EventCopyBean.BY_MAX_ITERATIONS);
    setNumberOfIterations(1);

    //prebuild & create copy
    buildPreview();
//    copyRecurrences(addMasterEventId);

    //get result
    if (rows != null && !rows.isEmpty())
      copy = rows.get(0);

    return copy;
  }

  /**
   *
   * @param startDateTime
   * @param endDateTime
   * @return time between two dates in minutes
   */
  public static long getTimeBetweenDates(String startDateTime, String endDateTime)
  {
    if (startDateTime != null && endDateTime != null)
    {
      Date sDate = TextUtils.parseInternalDate(startDateTime);
      Date eDate = TextUtils.parseInternalDate(endDateTime);
      long millis = eDate.getTime() - sDate.getTime();
      long d = TimeUnit.MINUTES.convert(millis, TimeUnit.MILLISECONDS);
      return (d > 0 ? d : 1440);
    }
    else
      return 1440;
  }
  
  private void resetFilter()
  {
    setDaylyFrequencyValue(1);
    setDaylyFrequencyMode(BY_FREQUENCY_VALUE);
    setNumberOfIterations(0);
    setEndRangeMode(BY_MAX_DATE);
  }

  private void resetPreview()
  {
    if (rows != null)
      rows = null;
    firstRowIndex = 0;
  }

  public void copyRecurrences(boolean addMasterEventId) throws Exception
  {
    if (rows != null)
    {
      for (EventRow row : rows)
      {
        Event storeEvent = row.getEvent();
        if (storeEvent != null)
        {
          AgendaManagerPort port = AgendaConfigBean.getPort();

          if (addMasterEventId)
          {
            //Add masterEventId property
            Property p = new Property();
            p.setName("masterEventId");
            if (masterEventId == null)
              p.getValue().add(event.getEventId());
            else
              p.getValue().add(masterEventId);
            storeEvent.getProperty().add(p);
          }
          storeEvent = port.storeEvent(storeEvent);
          row.setEvent(storeEvent);
          //Attendants
          if (copyAttendants && attendants != null)
          {
            for (Attendant srcAtt : attendants)
            {
              Attendant att = new Attendant();
              att.setAttendantTypeId(srcAtt.getAttendantTypeId());
              att.setComments(srcAtt.getComments());
              att.setEventId(storeEvent.getEventId());
              att.setHidden(srcAtt.isHidden());
              att.setAttended(srcAtt.getAttended());
              att.setPersonId(srcAtt.getPersonId());
              port.storeAttendant(att);
            }
          }
          //Places
          if (copyPlaces && places != null)
          {
            for (EventPlace srcEP : places)
            {
              EventPlace eventPlace = new EventPlace();
              eventPlace.setAddressId(srcEP.getAddressId());
              eventPlace.setComments(srcEP.getComments());
              eventPlace.setEventId(storeEvent.getEventId());
              eventPlace.setEventPlaceTypeId(srcEP.getEventPlaceTypeId());
              eventPlace.setRoomId(srcEP.getRoomId());
              try
              {
                port.storeEventPlace(eventPlace);
              }
              catch (Exception ex)
              {
                if (ex.getMessage().contains("ROOM_UNAVAILABLE"))
                {
                  warn("RECURRENCE_ROOMS_IGNORED");
                }
                else
                  throw ex;
              }
            }
          }
          //Themes
          if (copyThemes && themes != null)
          {
            for (EventThemeView srcET : themes)
            {
              EventTheme eventTheme = new EventTheme();
              eventTheme.setEventId(storeEvent.getEventId());
              eventTheme.setThemeId(srcET.getThemeId());
              eventTheme.setComments(srcET.getDescription());
              port.storeEventTheme(eventTheme);
            }
          }
          //Documents
          if (copyDocuments && documents != null)
          {
            for (EventDocumentView srcED : documents)
            {
              EventDocument eventDocument = new EventDocument();
              eventDocument.setEventId(storeEvent.getEventId());
              eventDocument.setDocId(srcED.getDocument().getDocId());
              eventDocument.setEventDocTypeId(srcED.getEventDocTypeId());
              port.storeEventDocument(eventDocument);
            }
          }
          
          //Cases
          if (copyCases && cases != null)
          {
            for (CaseEventView srcEC : cases)
            {
              CaseEvent caseEvent = new CaseEvent();
              caseEvent.setEventId(storeEvent.getEventId());
              caseEvent.setCaseId(srcEC.getCaseObject().getCaseId());
              caseEvent.setCaseEventTypeId(srcEC.getCaseEventTypeId());
              caseEvent.getProperty().addAll(srcEC.getProperty());
              CaseManagerPort cport = CaseConfigBean.getPort();
              cport.storeCaseEvent(caseEvent);
            }
          }
        }
      }
    }
  }

  private void buildPreview()
  {
    if (endRangeMode != null && rangeStartDateTime != null)
    {
      //Set duration
      if (!StringUtils.isBlank(duration) && !StringUtils.isBlank(durationSelector))
        hourPattern.setDuration(timeToLong(duration + durationSelector));
      //Set gap
      if (!StringUtils.isBlank(gap) && !StringUtils.isBlank(gapSelector))
        hourPattern.setGap(timeToLong(gap + gapSelector));

      PreviewListBuilder previewListBuilder = null;
      switch (frequencyMode)
      {
        case DAYLY:
          previewListBuilder = new DaylyPreviewListBuilder();
          break;
        case WEEKLY:
          previewListBuilder = new WeeklyPreviewListBuilder();
          break;
        case MONTHLY:
          previewListBuilder = new MonthlyPreviewListBuilder();
          break;
        case YEARLY:
          previewListBuilder = new YearlyPreviewListBuilder();
          break;
      }

      Date rangeStartDate = TextUtils.parseInternalDate(rangeStartDateTime);
      if (endRangeMode.equals(BY_MAX_ITERATIONS))
      {
        rows = previewListBuilder.getEvents(rangeStartDate, numberOfIterations);
      }
      else if (endRangeMode.equals(BY_MAX_DATE) && rangeEndDateTime != null)
      {
        rangeEndDateTime = rangeEndDateTime.substring(0, 8) + "235959";
        Date rangeEndDate = TextUtils.parseInternalDate(rangeEndDateTime);
        rows = previewListBuilder.getEvents(rangeStartDate, rangeEndDate);
      }
      firstRowIndex = 0;
    }
  }

  private void setEventProperties(Event event)
  {
    try
    {
      this.event.getProperty().clear();
      for (Property property : event.getProperty())
      {
        if (!"exchangeId".equals(property.getName()))
          this.event.getProperty().add(property);
      }
      
      AttendantFilter attFilter = new AttendantFilter();
      attFilter.setEventId(event.getEventId());
      this.attendants = AgendaConfigBean.getPort().findAttendantsFromCache(attFilter);

      EventPlaceFilter epFilter = new EventPlaceFilter();
      epFilter.setEventId(event.getEventId());
      this.places = AgendaConfigBean.getPort().findEventPlacesFromCache(epFilter);

      EventThemeFilter etFilter = new EventThemeFilter();
      etFilter.setEventId(event.getEventId());
      this.themes = AgendaConfigBean.getPort().findEventThemeViewsFromCache(etFilter);

      EventDocumentFilter edFilter = new EventDocumentFilter();
      edFilter.setEventId(event.getEventId());
      this.documents = AgendaConfigBean.getPort().findEventDocumentViewsFromCache(edFilter);
      
      CaseEventFilter ceFilter = new CaseEventFilter();
      ceFilter.setEventId(event.getEventId());
      this.cases = CaseConfigBean.getPort().findCaseEventViews(ceFilter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private long timeToLong(String duration)
  {
    long result = 0;
    if (!StringUtils.isBlank(duration))
    {
      if (duration.contains(DAYS_SELECTOR))
      {
        String[] parts = duration.split(DAYS_SELECTOR);
        result = TimeUnit.MINUTES.convert(Long.parseLong(parts[0]), TimeUnit.DAYS);
        if (parts.length > 1)
          duration = parts[1];
        else
          duration = "0";
      }

      if (duration.contains(HOURS_SELECTOR))
      {
        String[] parts = duration.split(HOURS_SELECTOR);
        result = result + TimeUnit.MINUTES.convert(Long.parseLong(parts[0]), TimeUnit.HOURS);
        if (parts.length > 1)
          duration = parts[1];
        else
          duration = "0";
      }

      if (duration.contains(MINUTES_SELECTOR))
      {
        String[] parts = duration.split(MINUTES_SELECTOR);
        result = result + Long.parseLong(parts[0]);
      }
      else
        result = result + Long.parseLong(duration);
    }

    return result;
  }

  public class EventRow implements Serializable
  {
    private Event event;
    private List<Event> overlappingEvents = new ArrayList();
    private List<String> overlappingAttendants = new ArrayList();

    public EventRow(Event event)
    {
      this.event = event;
    }
    
    public Event getEvent()
    {
      return event;
    }

    public void setEvent(Event event)
    {
      this.event = event;
    }

    public boolean isRoomAvailable()
    {
      return overlappingEvents.isEmpty();
    }

    public boolean isAttendantAvailable()
    {
      return overlappingAttendants.isEmpty();
    }

    public List<Event> getOverlappingEvents()
    {
      return overlappingEvents;
    }

    public void setOverlappingEvents(List<Event> overlappingEvents)
    {
      this.overlappingEvents = overlappingEvents;
    }

    public List<String> getOverlappingAttendants()
    {
      return overlappingAttendants;
    }

    public void setOverlappingAttendants(List<String> overlappingAttendants)
    {
      this.overlappingAttendants = overlappingAttendants;
    }

    public boolean checkRoomsAvailability(Event event)
    {
      overlappingEvents = getOverlappingEvents(event);
      return isRoomAvailable();
    }

    public boolean checkRoomsAvailability()
    {
      return checkRoomsAvailability(this.event);
    }

    public boolean checkAttendantsAvailability(Event event)
    {
      overlappingAttendants = getOverlappingAttendants(event);
      return isAttendantAvailable();
    }

    public boolean checkAttendantsAvailability()
    {
      return checkAttendantsAvailability(this.event);
    }

    public Date getStartDateTime()
    {
      String startDateTime = event.getStartDateTime();
      try
      {
        return TextUtils.parseInternalDate(startDateTime);
      }
      catch (Exception e)
      {
        return null;
      }
    }

    public Date getEndDateTime()
    {
      String endDateTime = event.getEndDateTime();
      try
      {
        return TextUtils.parseInternalDate(endDateTime);
      }
      catch (Exception e)
      {
        return null;
      }
    }

    public Date getOverlappingEventStartDateTime()
    {
      Event oe = (Event)getValue("#{oe}");
      if (oe != null)
      {
        String startDateTime = oe.getStartDateTime();
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

    public Date getOverlappingEventEndDateTime()
    {
      Event oe = (Event)getValue("#{oe}");
      if (oe != null)
      {
        String endDateTime = oe.getEndDateTime();
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

    private List<Event> getOverlappingEvents(Event event)
    {
      overlappingEvents.clear();
      for (EventPlace place : places)
      {
        try
        {
          EventFilter filter = new EventFilter();
          filter.setRoomId(place.getRoomId());
          filter.setStartDateTime(event.getStartDateTime());
          filter.setEndDateTime(event.getEndDateTime());
          filter.setSecurityMode(SecurityMode.HIDDEN);
          filter.setDateComparator("R");
          filter.setMaxResults(10);
          List<Event> events = AgendaConfigBean.getPort().findEventsFromCache(filter);
          if (events != null && !events.isEmpty())
          {
            for (Event e : events)
            {
              if (!e.getStartDateTime().equals(event.getEndDateTime()) &&
                  !e.getEndDateTime().equals(event.getStartDateTime()))
              {
                overlappingEvents.add(e);
              }
            }
          }
        }
        catch (Exception ex)
        {
          error(ex);
        }
      }
      return overlappingEvents;
    }

    private List<String> getOverlappingAttendants(Event event)
    {
      overlappingAttendants.clear();
      for (Attendant attendant : attendants)
      {
        try
        {
          EventFilter filter = new EventFilter();
          filter.setPersonId(attendant.getPersonId());
          filter.setStartDateTime(event.getStartDateTime());
          filter.setEndDateTime(event.getEndDateTime());
          filter.setSecurityMode(SecurityMode.HIDDEN);
          filter.setDateComparator("R");
          filter.setMaxResults(10);
          List<Event> events = AgendaConfigBean.getPort().findEventsFromCache(filter);
          if (events != null && !events.isEmpty())
          {
            for (Event e : events)
            {
              if (!e.getStartDateTime().equals(event.getEndDateTime()) &&
                  !e.getEndDateTime().equals(event.getStartDateTime()))
              {
                String personId = attendant.getPersonId();
                if (!overlappingAttendants.contains(personId))
                  overlappingAttendants.add(personId);
              }
            }
          }
        }
        catch (Exception ex)
        {
          error(ex);
        }
      }
      return overlappingAttendants;
    }

  }

  private class HourPattern implements Serializable
  {
    String startHour; //HHmm
    String endHour; //HHmm
    long duration; //in minutes
    long gap; //in minutes

    public HourPattern(String startDateTime, String endDateTime, long duration)
    {
      if (startDateTime != null && endDateTime != null)
      {
        this.startHour = startDateTime.substring(8, 12);
        this.endHour = endDateTime.substring(8, 12);
      }
      else
      {
        this.startHour = "0000";
        this.endHour = "0030";
      }
      this.duration = duration;
      this.gap = 0;
    }

    public void setDuration(long duration)
    {
      this.duration = duration;
      long time = getTimeBetweenHours(this.startHour, this.endHour);
      if (duration > time)
      {
        Date sDate = TextUtils.parseUserDate(this.startHour, "HHmm");
        long t = sDate.getTime() + TimeUnit.MILLISECONDS.convert(duration, TimeUnit.MINUTES);
        Date eDate = new Date(t);
        this.endHour = TextUtils.formatDate(eDate, "HHmm");
        this.gap = 0;
      }
    }

    public void setGap(long gap)
    {
      this.gap = gap;
      long time = getTimeBetweenHours(this.startHour, this.endHour);
      if (duration + gap > time && duration < time)
        this.gap = (time - duration);
      else if (duration + gap > time)
        this.gap = 0;
    }

    public long getDuration()
    {
      if (duration > 0)
        return duration;
      else
        return 1;
    }

    public long getDurationInMillis()
    {
      return TimeUnit.MILLISECONDS.convert(getDuration(), TimeUnit.MINUTES);
    }

    public long getGap()
    {
      return gap;
    }

    public String getFormattedEndHour()
    {
      if (endHour != null)
        return endHour.substring(0, 2) + ":" + endHour.substring(2, 4);
      else
        return endHour;
    }

    public void setFormattedEndHour(String endHour)
    {
      this.endHour = formatTime(endHour);
    }

    public String getFormattedStartHour()
    {
      if (startHour != null)
        return startHour.substring(0, 2) + ":" + startHour.substring(2, 4);
      else
        return startHour;
    }

    public void setFormattedStartHour(String startHour)
    {
      this.startHour = formatTime(startHour);
    }
    


    public boolean includeChangeOfDay()
    {
      if (startHour != null && endHour != null)
      {
        return TextUtils.parseUserDate(endHour, "HHmm").
          before(TextUtils.parseUserDate(startHour, "HHmm"));
      }
      else
        return false;
    }

    public boolean isLongerThanOneDay()
    {
      return duration > 1440;
    }

    public long getTimeBetweenHours()
    {
      return getTimeBetweenHours(startHour, endHour);
    }

    private long getTimeBetweenHours(String startHour, String endHour)
    {
      String shour = startHour.substring(0, 2);
      String sminutes = startHour.substring(2, 4);
      long start =
        TimeUnit.MINUTES.convert(Long.parseLong(shour), TimeUnit.HOURS);
      start = start + Long.parseLong(sminutes);

      String ehour = endHour.substring(0, 2);
      String eminutes = endHour.substring(2, 4);
      long end =
        TimeUnit.MINUTES.convert(Long.parseLong(ehour), TimeUnit.HOURS);
      end = end + Long.parseLong(eminutes);

      if (end > start)
        return (end - start);
      else 
        return (1440 - start) + end;
    }
    
    private String formatTime(String time)
    {
      String result = "0000";
      
      if (time != null && time.contains(":"))
      {
        String[] parts = time.split(":");
        String hour = StringUtils.leftPad(parts[0], 2, '0');
        String minutes = StringUtils.leftPad(parts[1], 2, '0');
        result = hour + minutes;
      }
      else if (time != null && !time.contains(":") && time.length() <= 2)
      {
        String hour = StringUtils.leftPad(time, 2, '0');
        result = hour + "00";
      }
      
      return result;
    }    
  }

  private abstract class PreviewListBuilder
  {
    public List<EventRow> getEvents(Date rangeStartDate, Date rangeEndDate)
    {
      List<EventRow> events = new ArrayList();
      Calendar cal = Calendar.getInstance();
      cal.setTime(rangeStartDate);
      boolean loop = true;
      while (loop && cal.getTime().before(rangeEndDate))
      {
        loop = addEvents(events, cal);
        loop = loop && events.size() < maxRecurrences;
      }
      if (events.size() >= maxRecurrences)
      {
        ResourceBundle bundle = ResourceBundle.getBundle(
          "org.santfeliu.agenda.web.resources.AgendaBundle", getLocale()); 
        Object[] params = {maxRecurrences};
        warn(bundle.getString("eventCopy_limitedRecurrencesCount"), params);
      }      
      for (EventRow row : events)
      {
        row.checkRoomsAvailability();
        if (checkAttendantsAvailability)
          row.checkAttendantsAvailability();
      }

      return events;
    }

    public List<EventRow> getEvents(Date rangeStartDate, int numberOfIterations)
    {
      List<EventRow> events = new ArrayList();
      Calendar cal = Calendar.getInstance();
      cal.setTime(rangeStartDate);
      boolean loop = true;
      if (numberOfIterations > maxRecurrences)
      {
        numberOfIterations = maxRecurrences;
        ResourceBundle bundle = ResourceBundle.getBundle(
          "org.santfeliu.agenda.web.resources.AgendaBundle", getLocale()); 
        Object[] params = {maxRecurrences};
        warn(bundle.getString("eventCopy_limitedRecurrencesCount"), params);        
      }
      while (loop && events.size() < numberOfIterations)
      {
        loop = addEvents(events, cal);
      }
      if (numberOfIterations > 0 && numberOfIterations < events.size())
        events = new ArrayList(events.subList(0, numberOfIterations));
      for (EventRow row : events)
      {
        row.checkRoomsAvailability();
        if (checkAttendantsAvailability)
          row.checkAttendantsAvailability();
      }

      return events;
    }

    /**
     *
     * @param date
     * @param duration in minutes
     * @return
     */
    protected Event copyEvent(Date date, HourPattern hourPattern)
    {
      Event e = new Event();
      e.setEventTypeId(event.getEventTypeId());
      e.setSummary(event.getSummary());
      e.setComments(event.getComments());
      e.setDescription(event.getDescription());
      e.setDetail(event.getDetail());
      e.setOnlyAttendants(event.isOnlyAttendants());
      long duration = hourPattern.getDuration();

      if (duration == 0) //if no duration then is calculated from source event
      {
        Date startDate = TextUtils.parseInternalDate(event.getStartDateTime());
        Date endDate = TextUtils.parseInternalDate(event.getEndDateTime());
        long millis = endDate.getTime() - startDate.getTime();
        duration = TimeUnit.MINUTES.convert(millis, TimeUnit.MILLISECONDS);
      }

      String startDateTime = TextUtils.formatDate(date, "yyyyMMddHHmmss");
      e.setStartDateTime(startDateTime);
      long endMillis = TextUtils.parseInternalDate(startDateTime).getTime() + duration * 60000;
      e.setEndDateTime(TextUtils.formatDate(new Date(endMillis), "yyyyMMddHHmmss"));

      copyEventProperties(event, e);

      return e;
    }

    protected void copyEventProperties(Event srcEvent, Event dstEvent)
    {
      List<Property> properties = srcEvent.getProperty();
      if (properties !=  null && !properties.isEmpty())
      {
        dstEvent.getProperty().clear();
        for (Property p : properties)
        {
          if (!p.getName().equals("masterEventId"))
            dstEvent.getProperty().add(p);
        }
      }
    }

    protected boolean addEvent(List<EventRow> events, Date date,
      HourPattern hourPattern)
    {
      if (!StringUtils.isBlank(hourPattern.getFormattedStartHour())
        && !StringUtils.isBlank(hourPattern.getFormattedEndHour()))
      {
        //Set start hour
        Calendar scal = Calendar.getInstance();
        scal.setTime(date);
        String[] sStartHour = hourPattern.getFormattedStartHour().split(":");
        scal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(sStartHour[0]));
        scal.set(Calendar.MINUTE, Integer.valueOf(sStartHour[1]));

        //Set end hour
        Calendar ecal = Calendar.getInstance();
        if (hourPattern.isLongerThanOneDay())
          ecal.setTimeInMillis(scal.getTimeInMillis() +
            hourPattern.getDurationInMillis());
        else
          ecal.setTimeInMillis(scal.getTimeInMillis() +
            TimeUnit.MILLISECONDS.convert(hourPattern.getTimeBetweenHours(), 
              TimeUnit.MINUTES));

        boolean inRange =
          scal.getTimeInMillis() + hourPattern.getDurationInMillis() <= ecal.getTimeInMillis();
        if (!inRange)
        {
          error("OUT_OF_RANGE");
          return false;
        }

        Calendar maxDate = Calendar.getInstance();
        maxDate.set(Calendar.YEAR, 3000);

        while (inRange && scal.before(maxDate))
        {
          EventRow e = new EventRow(copyEvent(scal.getTime(), hourPattern));
          events.add(e);
          scal.add(Calendar.MINUTE, (int) hourPattern.getDuration());
          scal.add(Calendar.MINUTE, (int) hourPattern.getGap());
          inRange = (scal.getTimeInMillis() +
            hourPattern.getDurationInMillis() <= ecal.getTimeInMillis());
        }
      }
      else
      {
        Event event = copyEvent(date, hourPattern);
        EventRow e = new EventRow(event);
        events.add(e);
      }

      return true;
    }
    
    public abstract boolean addEvents(List<EventRow> events, Calendar cal);
  }

  private class DaylyPreviewListBuilder extends PreviewListBuilder
  {
    public boolean addEvents(List<EventRow> events, Calendar cal)
    {
      boolean result = true;
      if (daylyFrequencyMode.equals(BY_FREQUENCY_VALUE))
      {
        if (daylyFrequencyValue == 0)
          return false;

        if (!getRangeStartDate().after(cal.getTime()))
        {
          result = addEvent(events, cal.getTime(), hourPattern);
        }
        cal.add(Calendar.DATE, daylyFrequencyValue);
      }
      else
      {
        if (!getRangeStartDate().after(cal.getTime()))
        {
          result = addEvent(events, cal.getTime(), hourPattern);
        }
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.FRIDAY)
          cal.add(Calendar.DATE, 3);
        else if (dayOfWeek == Calendar.SATURDAY)
          cal.add(Calendar.DATE, 2);
        else
          cal.add(Calendar.DATE, 1);
      }
      return result;
    }
  }

  private class WeeklyPreviewListBuilder extends PreviewListBuilder
  {
    @Override
    public boolean addEvents(List<EventRow> events, Calendar cal)
    {
      boolean result = true;
      if (weeklyFrequencyValue == 0 || daysOfWeek.isEmpty())
        return false;

      int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
      if (!getRangeStartDate().after(cal.getTime()) &&
        daysOfWeek.contains(String.valueOf(dayOfWeek)))
      {
        result = addEvent(events, cal.getTime(), hourPattern);
      }
      
      if (dayOfWeek == Calendar.SUNDAY)
      {
        int offset = ((weeklyFrequencyValue - 1) * 7) + 1;
        cal.add(Calendar.DATE, offset);
      }
      else
        cal.add(Calendar.DATE, 1);

      return result;
    }
  }

  private class MonthlyPreviewListBuilder extends PreviewListBuilder
  {
    @Override
    public boolean addEvents(List<EventRow> events, Calendar cal)
    {
      boolean result = true;
      if (monthlyFrequencyMode.equals(BY_SELECTED_DAY))
      {
        if (monthlyFrequencyValue0 == 0 || dayOfMonth == 0)
          return false;

        if (dayOfMonth == cal.get(Calendar.DAY_OF_MONTH))
        {
          result = addEvent(events, cal.getTime(), hourPattern);
        }

        cal.add(Calendar.MONTH, monthlyFrequencyValue0);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
      }
      else
      {
        if (weekPosition == null || dayOfWeekType == null)
          return false;

        cal.set(Calendar.DAY_OF_MONTH, 1);

        int month = cal.get(Calendar.MONTH);
        int day = 1;
        int position = Integer.valueOf(weekPosition);
        int counter = position;

        while (counter > 0 && month == cal.get(Calendar.MONTH))
        {
          if (dayOfWeekType.equals("8")) //Day
          {
            day = cal.get(Calendar.DAY_OF_MONTH);
            if (position != 5)
              counter--;
          }
          else if (dayOfWeekType.equals("9")) //Working day
          {
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY)
            {
              day = cal.get(Calendar.DAY_OF_MONTH);
              if (position != 5)
                counter--;
            }
          }
          else if (dayOfWeekType.equals("10")) //Weekend day
          {
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY)
            {
              day = cal.get(Calendar.DAY_OF_MONTH);
              if (position != 5)
                counter--;
            }
          }
          else
          {
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Integer.valueOf(dayOfWeekType).intValue())
            {
              day = cal.get(Calendar.DAY_OF_MONTH);
              if (position != 5)
                counter--;
            }
          }

          cal.add(Calendar.DATE, 1);
        }

        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        if (!getRangeStartDate().after(cal.getTime()))
        {
          result = addEvent(events, cal.getTime(), hourPattern);
        }

        //Next
        cal.add(Calendar.MONTH, monthlyFrequencyValue1);
      }


      return result;
    }
  }

  private class YearlyPreviewListBuilder extends PreviewListBuilder
  {
    @Override
    public boolean addEvents(List<EventRow> events, Calendar cal)
    {
      boolean result = true;
      if (yearlyFrequencyMode.equals(BY_SELECTED_DAY))
      {
        if (month0 == null || dayOfMonth == 0)
          return false;

        int month = Integer.valueOf(month0).intValue();
        if (dayOfMonth == cal.get(Calendar.DAY_OF_MONTH) &&
            cal.get(Calendar.MONTH) == month &&
            !getRangeStartDate().after(cal.getTime()))
        {
          result = addEvent(events, cal.getTime(), hourPattern);
          cal.add(Calendar.YEAR, 1);
        }
        else if (getRangeStartDate().after(cal.getTime()))
          cal.add(Calendar.YEAR, 1);

        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        cal.set(Calendar.MONTH, month);
      }
      else
      {
        if (weekPosition == null || dayOfWeekType == null || month1 == null)
          return false;


        cal.set(Calendar.DAY_OF_MONTH, 1);

        int month = Integer.parseInt(month1);
        int day = 1;
        int position = Integer.valueOf(weekPosition);
        int counter = position;

        while (counter > 0 && month == cal.get(Calendar.MONTH))
        {
          if (dayOfWeekType.equals("8")) //Day
          {
            day = cal.get(Calendar.DAY_OF_MONTH);
            if (position != 5)
              counter--;
          }
          else if (dayOfWeekType.equals("9")) //Working day
          {
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY)
            {
              day = cal.get(Calendar.DAY_OF_MONTH);
              if (position != 5)
                counter--;
            }
          }
          else if (dayOfWeekType.equals("10")) //Weekend day
          {
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY)
            {
              day = cal.get(Calendar.DAY_OF_MONTH);
              if (position != 5)
                counter--;
            }
          }
          else
          {
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Integer.valueOf(dayOfWeekType).intValue())
            {
              day = cal.get(Calendar.DAY_OF_MONTH);
              if (position != 5)
                counter--;
            }
          }

          cal.add(Calendar.DATE, 1);
        }

        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        if (!getRangeStartDate().after(cal.getTime()))
        {
          result = addEvent(events, cal.getTime(), hourPattern);
        }
        cal.add(Calendar.YEAR, 1);
          
      }

      return result;
    }
  }


}
