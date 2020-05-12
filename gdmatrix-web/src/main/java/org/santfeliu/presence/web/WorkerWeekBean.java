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
package org.santfeliu.presence.web;

import java.util.Collections;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import javax.servlet.ServletRequest;
import org.apache.commons.lang.StringUtils;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentConstants;
import org.matrix.doc.DocumentManagerPort;
import org.matrix.presence.Absence;
import org.matrix.presence.AbsenceCounterFilter;
import org.matrix.presence.AbsenceCounterView;
import org.matrix.presence.AbsenceCounting;
import org.matrix.presence.AbsenceFilter;
import org.matrix.presence.AbsenceType;
import org.matrix.presence.AbsenceTypeFilter;
import org.matrix.presence.DayType;
import org.matrix.presence.Holiday;
import org.matrix.presence.HolidayFilter;
import static org.matrix.presence.PresenceConstants.*;
import org.matrix.presence.PresenceEntry;
import org.matrix.presence.PresenceEntryType;
import org.matrix.presence.PresenceEntryTypeFilter;
import org.matrix.presence.PresenceManagerPort;
import org.matrix.presence.ScheduleEntry;
import org.matrix.presence.ScheduleFault;
import org.matrix.presence.WorkReduction;
import org.matrix.presence.Worker;
import org.matrix.presence.WorkerSchedule;
import org.matrix.presence.WorkerStatistics;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.presence.util.Utils;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.WebBean;

/**
 *
 * @author realor
 */
public class WorkerWeekBean extends WebBean implements Savable
{
  private String startDate;
  private String endDate;
  private String insertDate;
  private String insertTime;
  private boolean clockVisible = true;
  private String mode = "entry";
  private int pixelsPerDay = 72;
  private int pixelsPerHour = 16;
  private List<PresenceEntryType> entryTypes;
  private WorkerStatistics workerStatistics;
  private List<Holiday> holidays;
  private List<String> weekDates;
  private List<Absence> absences;
  private List<AbsenceCounterView> absenceCounters;
  private PresenceEntry editingEntry = new PresenceEntry();
  private Absence editingAbsence = new Absence();
  private boolean entriesVisible = true;
  private boolean absencesVisible = true;
  private boolean scheduleVisible = true;
  private String absenceStartDate;
  private String absenceEndDate;
  private String absenceStartTime;
  private String absenceEndTime;
  private double scroll;
  private int zoomLevel = 1;
  private static int[] zoomLevels = new int[]{16, 40, 100};
  private transient List<SelectItem> absenceTypeSelectItems;

  public WorkerWeekBean()
  {
  }

  public int getPixelsPerDay()
  {
    return pixelsPerDay;
  }

  public int getPixelsPerHour()
  {
    return pixelsPerHour;
  }

  public String getStartDate()
  {
    return startDate;
  }

  public void setStartDate(String startDate)
  {
    this.startDate = startDate;
  }

  public String getEndDate()
  {
    return endDate;
  }

  public void setEndDate(String endDate)
  {
    this.endDate = endDate;
  }

  public String getInsertDate()
  {
    return insertDate;
  }

  public void setInsertDate(String insertDate)
  {
    this.insertDate = insertDate;
  }

  public String getInsertTime()
  {
    return insertTime;
  }

  public void setInsertTime(String insertTime)
  {
    this.insertTime = insertTime;
  }

  public String getAbsenceStartDate()
  {
    return absenceStartDate;
  }

  public void setAbsenceStartDate(String absenceStartDate)
  {
    this.absenceStartDate = absenceStartDate;
  }

  public String getAbsenceEndDate()
  {
    return absenceEndDate;
  }

  public void setAbsenceEndDate(String absenceEndDate)
  {
    this.absenceEndDate = absenceEndDate;
  }

  public String getAbsenceStartTime()
  {
    return absenceStartTime;
  }

  public void setAbsenceStartTime(String absenceStartTime)
  {
    this.absenceStartTime = absenceStartTime;
  }

  public String getAbsenceEndTime()
  {
    return absenceEndTime;
  }

  public void setAbsenceEndTime(String absenceEndTime)
  {
    this.absenceEndTime = absenceEndTime;
  }

  public double getScroll()
  {
    return scroll;
  }

  public void setScroll(double scroll)
  {
    this.scroll = scroll;
  }

  public boolean isClockVisible()
  {
    return clockVisible;
  }

  public void setClockVisible(boolean clockVisible)
  {
    this.clockVisible = clockVisible;
  }

  public Worker getWorker()
  {
    PresenceMainBean presenceMainBean =
      (PresenceMainBean)getBean("presenceMainBean");
    return presenceMainBean.getWorker();
  }

  public String getAbsencesYear()
  {
    if (!getAbsenceCounters().isEmpty())
    {
      return getAbsenceCounters().get(0).getAbsenceCounter().getYear();
    }
    return null;
  }

  public String getCounterCounting()
  {
    AbsenceCounting counting = (AbsenceCounting)getValue("#{counter.absenceType.counting}");
    return PresenceConfigBean.getInstance().getAbsenceCountingLabel(counting);
  }
  
  public List<AbsenceCounterView> getAbsenceCounters()
  {
    try
    {
      if (absenceCounters == null)
      {
        AbsenceCounterFilter filter = new AbsenceCounterFilter();
        String personId = getWorker().getPersonId();
        filter.setDate(startDate);
        filter.setPersonId(personId);
        filter.setCounterVisible(Boolean.TRUE);
        PresenceManagerPort port = getPort();
        absenceCounters = port.findAbsenceCounterViews(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return absenceCounters;
  }

  public String getInsertDateFormatted()
  {
    return TextUtils.formatDate(TextUtils.parseInternalDate(insertDate),
      "EEEE, d MMMM yyyy", getLocale());
  }

  public Absence getEditingAbsence()
  {
    return editingAbsence;
  }

  public void setEditingAbsence(Absence editingAbsence)
  {
    this.editingAbsence = editingAbsence;
    String startDateTime = editingAbsence.getStartDateTime();
    String endDateTime = editingAbsence.getEndDateTime();
    if (StringUtils.isBlank(startDateTime))
    {
      absenceStartDate = null;
      absenceStartTime = null;
    }
    else
    {
      absenceStartDate = startDateTime.substring(0, 8);
      absenceStartTime = startDateTime.substring(8);
    }
    if (StringUtils.isBlank(endDateTime))
    {
      absenceEndDate = null;
      absenceEndTime = null;
    }
    else
    {
      absenceEndDate = endDateTime.substring(0, 8);
      absenceEndTime = endDateTime.substring(8);
    }
  }

  public boolean isAbsenceIntegerCounting()
  {
    String absenceTypeId = editingAbsence.getAbsenceTypeId();
    if (!StringUtils.isBlank(absenceTypeId))
    {
      AbsenceType absenceType =
        PresenceConfigBean.getInstance().getAbsenceType(absenceTypeId);
      return AbsenceCounting.DAYS.equals(absenceType.getCounting());
    }
    return false;
  }

  public boolean isAbsenceAutorizable()
  {
    String absenceTypeId = editingAbsence.getAbsenceTypeId();
    if (!StringUtils.isBlank(absenceTypeId))
    {
      AbsenceType absenceType =
        PresenceConfigBean.getInstance().getAbsenceType(absenceTypeId);
      return absenceType.isAuthorizable();
    }
    return false;
  }  

  public boolean isAbsenceJustificable()
  {
    String absenceTypeId = editingAbsence.getAbsenceTypeId();
    if (!StringUtils.isBlank(absenceTypeId))
    {
      AbsenceType absenceType =
        PresenceConfigBean.getInstance().getAbsenceType(absenceTypeId);
      return absenceType.isJustificable();
    }
    return false;
  }
  
  public String getAbsenceDescription()
  {
    String absenceTypeId = editingAbsence.getAbsenceTypeId();
    if (!StringUtils.isBlank(absenceTypeId))
    {
      AbsenceType absenceType =
        PresenceConfigBean.getInstance().getAbsenceType(absenceTypeId);
      return absenceType.getDescription();
    }
    return null;
  }

  public boolean isEntriesVisible()
  {
    return entriesVisible;
  }

  public boolean isAbsencesVisible()
  {
    return absencesVisible;
  }

  public boolean isScheduleVisible()
  {
    return scheduleVisible;
  }

  public PresenceEntry getEditingEntry()
  {
    return editingEntry;
  }

  public void setEditingEntry(PresenceEntry editingEntry)
  {
    this.editingEntry = editingEntry;
  }

  public int getEditingEntryDuration()
  {
    return Utils.getDuration(editingEntry.getStartDateTime(),
      editingEntry.getEndDateTime());
  }

  public boolean isCurrentWeek()
  {
    String now = TextUtils.formatDate(new Date(), "yyyyMMdd");
    return startDate.compareTo(now) <= 0 &&
      now.compareTo(endDate) <= 0;
  }

  public boolean isSaveAbsenceEnabled()
  {
    PresenceMainBean presenceMainBean =
      (PresenceMainBean)getBean("presenceMainBean");
    if (presenceMainBean.isPresenceAdministrator()) return true;
    String status = editingAbsence.getStatus();
    return status == null || PENDENT_STATUS.equals(status);
  }

  public boolean isDeleteAbsenceEnabled()
  {
    if (editingAbsence.getAbsenceId() == null) return false;

    PresenceMainBean presenceMainBean =
      (PresenceMainBean)getBean("presenceMainBean");
    if (presenceMainBean.isPresenceAdministrator()) return true;
    String status = editingAbsence.getStatus();
    return PENDENT_STATUS.equals(status) ||
           CANCELLED_STATUS.equals(status) ||
           DENIED_STATUS.equals(status);
  }

  public boolean isEntryAddressValid()
  {
    PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
    return presenceConfigBean.isValidIpAddress(editingEntry.getIpAddress());
  }

  public Holiday getHoliday()
  {
    String weekDate = (String)getValue("#{weekDate}");
    Iterator<Holiday> iter = getHolidays().iterator();
    boolean found = false;
    Holiday holiday = null;
    while (iter.hasNext() && !found)
    {
      holiday = iter.next();
      found = (holiday.getStartDate().compareTo(weekDate) <= 0 &&
        weekDate.compareTo(holiday.getEndDate()) <= 0);
    }
    return found ? holiday : null;
  }

  public String getDayTypeLabel()
  {
    ScheduleEntry entry = (ScheduleEntry)getValue("#{entry}");
    PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
    DayType dayType = presenceConfigBean.getDayType(entry.getDayTypeId());
    return presenceConfigBean.getDayTypeLabel(dayType, false);
  }

  public String getReductionLabel()
  {
    String reductionId = (String)getValue("#{reductionId}");
    PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
    WorkReduction workReduction =
      presenceConfigBean.getWorkReduction(reductionId);
    return workReduction == null ? null : workReduction.getDescription();
  }

  public int getReductionTime()
  {
    String reductionId = (String)getValue("#{reductionId}");
    PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
    WorkReduction workReduction =
      presenceConfigBean.getWorkReduction(reductionId);
    if (workReduction == null) return 0;
    int initialDayDuration = (Integer)getValue("#{entry.initialDayDuration}");
    int reductionTime = 
      (int)Math.round(0.01 * initialDayDuration * workReduction.getFactor());
    return reductionTime;
  }

  public String getHolidayDescription()
  {
    Holiday holiday = getHoliday();
    return holiday == null ? null : holiday.getDescription();
  }

  public String getHolidayColor()
  {
    Holiday holiday = getHoliday();
    if (holiday != null)
    {
      String color = holiday.getColor();
      if (color != null)
      {
        if (!color.startsWith("#"))
        {
          color = "#" + color;
        }
        return "background-color:" + color;
      }
    }
    return "";
  }

  public List<Holiday> getHolidays()
  {
    try
    {
      if (holidays == null)
      {
        HolidayFilter holidayFilter = new HolidayFilter();
        holidayFilter.setStartDate(startDate);
        holidayFilter.setEndDate(endDate);
        holidays = getPort().findHolidays(holidayFilter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return holidays;
  }

  public WorkerStatistics getWorkerStatistics()
  {
    try
    {
      if (workerStatistics == null)
      {
        PresenceManagerPort port = getPort();
        workerStatistics = port.getWorkerStatistics(getWorker().getPersonId(),
          startDate + "000000", endDate + "235959");
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return workerStatistics;
  }

  public List<PresenceEntry> getDatePresenceEntries()
  {
    List<PresenceEntry> presenceEntries = 
      getWorkerStatistics().getPresenceEntry();
    List<PresenceEntry> datePresenceEntries = new ArrayList<PresenceEntry>();
    String date = (String)getValue("#{weekDate}");
    for (PresenceEntry entry : presenceEntries)
    {
      if (entry.getStartDateTime().substring(0, 8).equals(date))
      {
        datePresenceEntries.add(entry);
      }
    }
    return datePresenceEntries;
  }
  
  public int getDayWorkedTime()
  {
    int workedTime = 0;
    List<PresenceEntry> presenceEntries = 
      getWorkerStatistics().getPresenceEntry();
    String date = (String)getValue("#{weekDate}");
    for (PresenceEntry entry : presenceEntries)
    {
      String startDateTime = entry.getStartDateTime();
      String endDateTime = entry.getEndDateTime();
      if (startDateTime.substring(0, 8).equals(date))
      {
        if (endDateTime == null)
        {
          String nowDateTime = 
            TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");
          PresenceConfigBean presenceConfigBean = 
            PresenceConfigBean.getInstance();
          PresenceEntryType presenceEntryType = 
            presenceConfigBean.getPresenceEntryType(entry.getEntryTypeId());
          int maxWorkedTime = presenceEntryType.getMaxWorkedTime();
          if (maxWorkedTime > 0)
          {
            int duration = Utils.getDuration(startDateTime, nowDateTime);
            workedTime += Math.min(duration, maxWorkedTime);
          }
        }
        else
        {
          workedTime += entry.getWorkedTime();
        }
      }
    }
    return workedTime;
  }
  
  public List<PresenceEntryType> getPresenceEntryTypes()
  {
    try
    {
      if (entryTypes == null)
      {
        PresenceManagerPort port = getPort();
        PresenceEntryTypeFilter filter = new PresenceEntryTypeFilter();
        filter.setEnabled(true);
        filter.setTeam(getWorker().getTeam());
        entryTypes = port.findPresenceEntryTypes(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return entryTypes;
  }

  public List<SelectItem> getPresenceEntryTypeSelectItems()
  {
    List<SelectItem> selectItems = new ArrayList<SelectItem>();
    for (PresenceEntryType entryType : getPresenceEntryTypes())
    {
      SelectItem selectItem = new SelectItem();
      selectItem.setValue(entryType.getEntryTypeId());
      selectItem.setLabel(entryType.getLabel());
      selectItems.add(selectItem);
    }
    return selectItems;
  }  
  
  public List<String> getWeekDates()
  {
    if (weekDates == null)
    {
      weekDates = new ArrayList<String>();
      Calendar calendar = Calendar.getInstance();
      Date date = TextUtils.parseInternalDate(startDate);
      calendar.setTime(date);
      for (int i = 0; i < 7; i++)
      {
        date = calendar.getTime();
        weekDates.add(TextUtils.formatDate(date, "yyyyMMdd"));
        calendar.add(Calendar.DAY_OF_YEAR, 1);
      }
    }
    return weekDates;
  }

  public List<Absence> getAbsences()
  {
    try
    {
      if (absences == null)
      {
        PresenceManagerPort port = getPort();
        AbsenceFilter filter = new AbsenceFilter();
        filter.getPersonId().add(getWorker().getPersonId());
        filter.setStartDateTime(startDate + "000000");
        filter.setEndDateTime(endDate + "235959");
        filter.setSplitByDay(true);
        absences = port.findAbsences(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return absences;
  }

  public List<SelectItem> getAbsenceTypeSelectItems()
  {
    List<SelectItem> selectItems = new ArrayList<SelectItem>();
    try
    {
      List<AbsenceType> absenceTypes;
      if (absenceTypeSelectItems == null)
      {
        PresenceManagerPort port = getPort();
        AbsenceTypeFilter filter = new AbsenceTypeFilter();
        absenceTypes = port.findAbsenceTypes(filter);
        for (AbsenceType absenceType : absenceTypes)
        {
          if (absenceType.isEnabled() || 
            absenceType.getAbsenceTypeId().equals(editingAbsence.getAbsenceTypeId()))
          {
            SelectItem selectItem = new SelectItem();
            selectItem.setLabel(absenceType.getLabel());
            selectItem.setValue(absenceType.getAbsenceTypeId());
            selectItems.add(selectItem);
          }
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return selectItems;
  }
  
  public int getWeekNumber()
  {
    Date date = TextUtils.parseInternalDate(startDate);
    Calendar calendar = Calendar.getInstance();
    calendar.setMinimalDaysInFirstWeek(1);
    calendar.setTime(date);
    return calendar.get(Calendar.WEEK_OF_YEAR);
  }

  public String getMonthAndYear()
  {
    StringBuilder buffer = new StringBuilder();
    Date date = TextUtils.parseInternalDate(startDate);
    buffer.append(TextUtils.formatDate(date, "d MMMM yyyy", getLocale()));

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.DAY_OF_WEEK, 6);
    date = calendar.getTime();
    buffer.append(" - ");
    buffer.append(TextUtils.formatDate(date, "d MMMM yyyy", getLocale()));
    return buffer.toString();
  }

  public String getWeekDateFormatted()
  {
    String weekDate = (String)getValue("#{weekDate}");
    Date date = TextUtils.parseInternalDate(weekDate);
    return TextUtils.formatDate(date, "EEEE d", getLocale());
  }

  public int getWeekDateLeft()
  {
    String weekDate = (String)getValue("#{weekDate}");
    return getLeft(weekDate);
  }

  public String getPresenceEntryType()
  {
    PresenceEntry presenceEntry = (PresenceEntry)getValue("#{entry}");
    if (presenceEntry.getCreationDateTime() == null) return null;
    return PresenceConfigBean.getInstance().
      getPresenceEntryType(presenceEntry.getEntryTypeId()).getLabel();
  }
  
  public String getPresenceEntryTime()
  {
    PresenceEntry presenceEntry = (PresenceEntry)getValue("#{entry}");
    if (presenceEntry.getCreationDateTime() == null) return null;
    String startDateTime = presenceEntry.getStartDateTime();
    return startDateTime.substring(8, 10) + ":" +
      startDateTime.substring(10, 12) + ":" + startDateTime.substring(12);
  }

  public int getPresenceEntryLeft()
  {
    PresenceEntry entry = (PresenceEntry)getValue("#{entry}");
    return getLeft(entry.getStartDateTime());
  }

  public int getPresenceEntryTop()
  {
    PresenceEntry entry = (PresenceEntry)getValue("#{entry}");
    String time = entry.getStartDateTime().substring(8);
    return getTop(time);
  }

  public int getPresenceEntryHeight()
  {
    PresenceEntry entry = (PresenceEntry)getValue("#{entry}");
    String time1 = entry.getStartDateTime().substring(8);
    String endDateTime = entry.getEndDateTime();
    if (endDateTime == null)
      endDateTime = TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");
    String time2 = endDateTime.substring(8);
    return getHeight(time1, time2);
  }

  public String getPresenceEntryStyle()
  {
    PresenceEntry entry = (PresenceEntry)getValue("#{entry}");
    String entryTypeId = entry.getEntryTypeId();
    PresenceEntryType presenceEntryType =
      PresenceConfigBean.getInstance().getPresenceEntryType(entryTypeId);
    String color = presenceEntryType.getColor();
    String style;
    if (StringUtils.isBlank(color)) // no color entry
    {
      int height = Math.min(14, getPresenceEntryHeight());
      style = "background:transparent;border-color:transparent;height:" + 
        height + "px"; 
    }
    else
    {
      int height = getPresenceEntryHeight();
      style = "background-color:#" + color + ";height:" + height + "px";
    }
    return style;
  }

  public String getPresenceEntryTimeStyleClass()
  {
    PresenceEntry entry = (PresenceEntry)getValue("#{entry}");
    return getPresenceEntryStyleClass(entry);
  }

  public String getEditingEntryStyleClass()
  {
    return getPresenceEntryStyleClass(editingEntry);
  }

  public String getPresenceEntryStyleClass(PresenceEntry entry)
  {
    String styleClass;
    if (entry.isManipulated())
    {
      styleClass = "manipulated";
    }
    else if (entry.getCompensationTime() > 0)
    {
      styleClass = "compensated";
    }
    else
    {
      styleClass = "normal";
    }
    return styleClass;
  }

  public int getScheduleFaultLeft()
  {
    ScheduleFault fault = (ScheduleFault)getValue("#{fault}");
    return getLeft(fault.getDateTime());
  }

  public int getScheduleFaultTop()
  {
    ScheduleFault fault = (ScheduleFault)getValue("#{fault}");
    String time = fault.getDateTime().substring(8);
    return getTop(time);
  }

  public int getScheduleEntryLeft()
  {
    ScheduleEntry entry = (ScheduleEntry)getValue("#{entry}");
    return getLeft(entry.getStartDateTime());
  }

  public int getScheduleEntryTop()
  {
    ScheduleEntry entry = (ScheduleEntry)getValue("#{entry}");
    String time = entry.getStartDateTime().substring(8);
    return getTop(time);
  }

  public int getScheduleEntryHeight()
  {
    ScheduleEntry entry = (ScheduleEntry)getValue("#{entry}");
    String time1 = entry.getStartDateTime().substring(8);
    String endDateTime = entry.getEndDateTime();
    if (endDateTime == null)
      endDateTime = TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");
    String time2 = endDateTime.substring(8);
    return getHeight(time1, time2);
  }

  public int getAbsenceLeft()
  {
    Absence absence = (Absence)getValue("#{absence}");
    return getLeft(absence.getStartDateTime());
  }

  public int getAbsenceTop()
  {
    Absence absence = (Absence)getValue("#{absence}");
    return getTop(absence.getStartDateTime().substring(8));
  }

  public int getAbsenceHeight()
  {
    Absence absence = (Absence)getValue("#{absence}");
    return getHeight(
      absence.getStartDateTime().substring(8),
      absence.getEndDateTime().substring(8));
  }

  public int getNowLeft()
  {
    return getLeft(TextUtils.formatDate(new Date(), "yyyyMMdd"));
  }

  public int getNowTop()
  {
    try
    {
      String now = TextUtils.formatDate(new Date(), "HHmm");
      int top = (int)(pixelsPerHour *
        (Double.parseDouble(now.substring(0, 2)) +
        Double.parseDouble(now.substring(2, 4)) / 60.0));
      return top;
    }
    catch (Exception ex)
    {
      return 0;
    }
  }

  public int getLeft(String date)
  {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(TextUtils.parseInternalDate(date));
    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
    int pos = 0;
    switch (dayOfWeek)
    {
      case Calendar.MONDAY:
        pos = 0;
        break;
      case Calendar.TUESDAY:
        pos = 1;
        break;
      case Calendar.WEDNESDAY:
        pos = 2;
        break;
      case Calendar.THURSDAY:
        pos = 3;
        break;
      case Calendar.FRIDAY:
        pos = 4;
        break;
      case Calendar.SATURDAY:
        pos = 5;
        break;
      case Calendar.SUNDAY:
        pos = 6;
        break;
    }
    return pos * pixelsPerDay;
  }

  public int getTop(String inTime)
  {
    try
    {
      return (int)(pixelsPerHour *
        (Double.parseDouble(inTime.substring(0, 2)) +
        Double.parseDouble(inTime.substring(2, 4)) / 60.0));
    }
    catch (Exception ex)
    {
      return 0;
    }
  }

  public int getHeight(String inTime, String outTime)
  {
    try
    {
      if (outTime.equals("000000")) outTime = "240000";
      int pos1 = getTop(inTime);
      int pos2 = getTop(outTime);
      return pos2 - pos1;
    }
    catch (Exception ex)
    {
      return 0;
    }
  }

  public String getTopHeight(String inTime, String outTime)
  {
    return "top:" + getTop(inTime) + "px;height:" +
      getHeight(inTime, outTime) + "px";
  }

  public String getEntryTitle()
  {
    PresenceEntry entry = (PresenceEntry)getValue("#{entry}");
    String entryTypeId = entry.getEntryTypeId();
    PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
    try
    {
      PresenceEntryType entryType = 
        presenceConfigBean.getPresenceEntryType(entryTypeId);
      String title = entryType.getLabel();
      if (entry.getReason() != null)
      {
        title += "\n" + entry.getReason();
      }
      return title;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public boolean isEditingEntryTypeEnabled()
  {
    PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
    PresenceEntryType presenceEntryType =
      presenceConfigBean.getPresenceEntryType(editingEntry.getEntryTypeId());
    return presenceEntryType.isEnabled();
  }

  public String getEditingEntryTypeLabel()
  {
    PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
    PresenceEntryType presenceEntryType =
      presenceConfigBean.getPresenceEntryType(editingEntry.getEntryTypeId());
    return presenceEntryType.getLabel();
  }

  public String getAccessKey()
  {
    PresenceEntryType entryType =
      (PresenceEntryType)getValue("#{presenceEntryType}");
    int position = entryType.getPosition();
    return position == 0 ? null : String.valueOf(position);
  }
  
  public boolean isLastEntryDurationRendered()
  {
    List<PresenceEntry> entries = getWorkerStatistics().getPresenceEntry();
    if (!entries.isEmpty())
    {
      PresenceEntry lastEntry = entries.get(entries.size() - 1);
      return lastEntry.getCreationDateTime() != null && 
        lastEntry.getEndDateTime() == null && 
        lastEntry.getStartDateTime().substring(0, 8).equals(insertDate);
    }
    return false;
  }
    
  public int getLastEntryDuration()
  {
    int duration = 0;
    List<PresenceEntry> entries = getWorkerStatistics().getPresenceEntry();
    if (!entries.isEmpty())
    {
      PresenceEntry lastEntry = entries.get(entries.size() - 1);
      if (lastEntry.getEndDateTime() == null)
      {
        String nowDateTime = TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");
        duration = Utils.getDuration(lastEntry.getStartDateTime(), nowDateTime);
      }
    }
    return duration;
  }

  public boolean isWorkedTimeRendered()
  {
    return startDate.compareTo(insertDate) <= 0 && 
      endDate.compareTo(insertDate) >= 0;
  }
    
  public int getWorkedTime()
  {
    String nowDateTime = TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");
    String date = insertDate;    
    int workedTime = 0;
    List<PresenceEntry> entries = getWorkerStatistics().getPresenceEntry();
    for (PresenceEntry entry : entries)
    {
      String startDateTime = entry.getStartDateTime();
      String endDateTime = entry.getEndDateTime();
      if (startDateTime.substring(0, 8).equals(date))
      {
        if (endDateTime == null) // last entry
        {
          PresenceConfigBean presenceConfigBean = 
            PresenceConfigBean.getInstance();
          PresenceEntryType presenceEntryType = 
            presenceConfigBean.getPresenceEntryType(entry.getEntryTypeId());
          int maxWorkedTime = presenceEntryType.getMaxWorkedTime();
          if (maxWorkedTime > 0)
          {
            int duration = Utils.getDuration(startDateTime, nowDateTime);
            workedTime += Math.min(duration, maxWorkedTime);
          }
        }
        else
        {
          workedTime += entry.getWorkedTime();
        }
      }
    }
    return workedTime;
  }
  
  public String nextWeek()
  {
    addDates(Calendar.DAY_OF_YEAR, +7);
    workerStatistics = null;
    absenceCounters = null;
    absences = null;
    weekDates = null;
    holidays = null;
    return "worker_week";
  }

  public String previousWeek()
  {
    addDates(Calendar.DAY_OF_YEAR, -7);
    workerStatistics = null;
    absenceCounters = null;
    absences = null;
    weekDates = null;
    holidays = null;
    return "worker_week";
  }

  public String nextMonth()
  {
    addDates(Calendar.MONTH, +1);
    workerStatistics = null;
    absenceCounters = null;
    absences = null;
    weekDates = null;
    holidays = null;
    return "worker_week";
  }

  public String previousMonth()
  {
    addDates(Calendar.MONTH, -1);
    workerStatistics = null;
    absenceCounters = null;
    absences = null;
    weekDates = null;
    holidays = null;
    return "worker_week";
  }

  public String show()
  {
    entryTypes = null;
    absenceCounters = null;
    setEditingAbsence(new Absence());
    mode = "entry";
    return goToday();
  }

  public String show(PresenceEntry entry)
  {
    String outcome = goDate(entry.getStartDateTime());
    editingEntry = entry;
    entryTypes = null;
    absenceCounters = null;
    mode = "entry";
    insertDate = entry.getStartDateTime().substring(0, 8);
    insertTime = entry.getStartDateTime().substring(8);
    clockVisible = false;
    editingAbsence = new Absence();
    return outcome;
  }
  
  public String show(Absence absence)
  {
    setEditingAbsence(absence);
    entryTypes = null;
    absenceCounters = null;
    editingEntry = new PresenceEntry();
    mode = "absence";
    return goDate(absence.getStartDateTime());
  }

  public String goToday()
  {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
    Date date = calendar.getTime();
    startDate = TextUtils.formatDate(date, "yyyyMMdd");
    addDates(Calendar.DAY_OF_MONTH, 0);
    insertDate = TextUtils.formatDate(new Date(), "yyyyMMdd");
    clockVisible = true;
    workerStatistics = null;
    absenceCounters = null;
    absences = null;
    weekDates = null;
    holidays = null;
    editingEntry = new PresenceEntry();
    return "worker_week";
  }

  public String goDate(String dateString)
  {
    Calendar calendar = Calendar.getInstance();
    Date date = TextUtils.parseInternalDate(dateString);
    calendar.setTime(date);
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
    date = calendar.getTime();
    startDate = TextUtils.formatDate(date, "yyyyMMdd");
    addDates(Calendar.DAY_OF_MONTH, 0);
    insertDate = TextUtils.formatDate(new Date(), "yyyyMMdd");
    clockVisible = true;
    workerStatistics = null;
    absenceCounters = null;
    absences = null;
    weekDates = null;
    holidays = null;
    return "worker_week";
  }

  public void changeInsertDate()
  {
    insertDate = (String)getValue("#{weekDate}");
    clockVisible = false;
    insertTime = null;
    editingEntry = new PresenceEntry();
    setEditingAbsence(new Absence());
    mode = "entry";
  }

  public String getMode()
  {
    return mode;
  }

  public void entryMode()
  {
    mode = "entry";
    setEditingAbsence(new Absence());
    goToday();
  }

  public void absenceMode()
  {
    mode = "absence";
  }

  public void statsMode()
  {
    mode = "stats";
    workerStatistics = null;
  }

  public String storeEntry()
  {
    try
    {
      Date date;
      if (clockVisible)
      {
        date = new Date();
      }
      else
      {
        try
        {
          SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
          df.setLenient(false);
          date = df.parse(insertDate + insertTime);
        }
        catch (ParseException ex)
        {
          error("INVALID_HOUR_FORMAT");
          return null;
        }
      }
      PresenceManagerPort port = getPort();
      String nowDateTime = TextUtils.formatDate(date, "yyyyMMddHHmmss");
      editingEntry.setStartDateTime(nowDateTime);
      ServletRequest request =
        (ServletRequest)getExternalContext().getRequest();
      String ipAddress = request.getRemoteAddr();
      editingEntry.setIpAddress(ipAddress);
      editingEntry.setManipulated(!clockVisible);
      editingEntry.setPersonId(getWorker().getPersonId());
      PresenceEntryType presenceEntryType =
        (PresenceEntryType)getValue("#{presenceEntryType}");
      if (presenceEntryType != null)
      {
        editingEntry.setEntryTypeId(presenceEntryType.getEntryTypeId());
      }
      port.storePresenceEntry(editingEntry);
      
      // info message
      PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
      PresenceEntryType entryType =
        presenceConfigBean.getPresenceEntryType(editingEntry.getEntryTypeId());
      message("org.santfeliu.presence.web.resources.PresenceBundle",
        "entrySaved", new Object[]{entryType.getLabel()},
         FacesMessage.SEVERITY_INFO);
      
      insertTime = null;     
      editingEntry = new PresenceEntry();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    workerStatistics = null; // refresh entries and force view redraw
    return "worker_week";
  }

  public String editEntry()
  {
    try
    {
      mode = "entry";
      PresenceManagerPort port = getPort();
      editingEntry = (PresenceEntry)getValue("#{entry}");
      editingEntry = port.loadPresenceEntry(editingEntry.getEntryId());
      String startDateTime = editingEntry.getStartDateTime();
      insertDate = startDateTime.substring(0, 8);
      insertTime = startDateTime.substring(8);
      clockVisible = false;
      setEditingAbsence(new Absence());
      return null;
    }
    catch (Exception ex)
    {
      // entry was removed, refresh page
      editingEntry = new PresenceEntry();
      clockVisible = true;
      insertTime = null;
      workerStatistics = null;
      error(ex);
      return "worker_week";
    }
  }

  public void removeEntry()
  {
    try
    {      
      PresenceManagerPort port = getPort();
      port.removePresenceEntry(editingEntry.getEntryId());
      message("org.santfeliu.presence.web.resources.PresenceBundle",
        "entryRemoved", null, FacesMessage.SEVERITY_INFO);
      workerStatistics = null;
      editingEntry = new PresenceEntry();
      insertTime = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void editEntryTime()
  {
    clockVisible = false;
    insertTime = null;
    editingEntry = new PresenceEntry();
    editingEntry.setManipulated(true);
  }

  public void editAbsence()
  {
    try
    {
      mode = "absence";
      PresenceManagerPort port = getPort();
      editingAbsence = (Absence)getValue("#{absence}");
      /* reload absence */
      setEditingAbsence(port.loadAbsence(editingAbsence.getAbsenceId()));
      editingEntry = new PresenceEntry();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void storeAbsence()
  {
    try
    {
      if (StringUtils.isBlank(editingAbsence.getAbsenceTypeId()))
      {
        message("org.santfeliu.presence.web.resources.PresenceBundle",
          "absenceTypeIsMandatory", null, FacesMessage.SEVERITY_ERROR);
        return;
      }
      
      if (StringUtils.isBlank(absenceStartDate))
      {
        message("org.santfeliu.presence.web.resources.PresenceBundle",
          "startDateIsMandatory", null, FacesMessage.SEVERITY_ERROR);
        return;
      }

      if (StringUtils.isBlank(absenceEndDate))
      {
        absenceEndDate = absenceStartDate;
      }
      String absenceTypeId = editingAbsence.getAbsenceTypeId();
      PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
      AbsenceType absenceType = presenceConfigBean.getAbsenceType(absenceTypeId);
      if (absenceType.getCounting().equals(AbsenceCounting.DAYS))
      {
        absenceStartTime = null;
        absenceEndTime = null;
      }

      PresenceManagerPort port = getPort();
      String personId = getWorker().getPersonId();

      if (StringUtils.isBlank(absenceStartTime))
      {
        List<WorkerSchedule> schedule = port.getSchedule(
          Collections.singletonList(personId),
          absenceStartDate, absenceStartDate);
        String dayTypeId = schedule.get(0).getDayTypeId().get(0);
        if (dayTypeId != null)
        {
          DayType dayType = presenceConfigBean.getDayType(dayTypeId);
          absenceStartTime = dayType.getInTime1();
        }
        else
        {
          absenceStartTime = "000000";
        }
      }

      if (StringUtils.isBlank(absenceEndTime))
      {
        List<WorkerSchedule> schedule = port.getSchedule(
          Collections.singletonList(personId),
          absenceEndDate, absenceEndDate);
        String dayTypeId = schedule.get(0).getDayTypeId().get(0);
        if (dayTypeId != null)
        {
          DayType dayType = port.loadDayType(dayTypeId);
          absenceEndTime = dayType.getOutTime2() == null ?
            dayType.getOutTime1() : dayType.getOutTime2();
        }
        else
        {
          absenceEndTime = "235959";
        }
      }

      if (absenceStartDate.equals(absenceEndDate) &&
        absenceStartTime.compareTo(absenceEndTime) > 0)
      {
        absenceEndDate = Utils.addDate(absenceStartDate, 1);
      }

      editingAbsence.setStartDateTime(absenceStartDate + absenceStartTime);
      editingAbsence.setEndDateTime(absenceEndDate + absenceEndTime);

      editingAbsence.setPersonId(personId);
      setEditingAbsence(port.storeAbsence(editingAbsence));
      absences = null;
      workerStatistics = null;
      absenceCounters = null;
      goDate(editingAbsence.getStartDateTime());
      PresenceConfigBean.clearBean("workerCalendarBean");
      message("org.santfeliu.presence.web.resources.PresenceBundle",
        "absenceSaved", null, FacesMessage.SEVERITY_INFO);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void removeAbsence()
  {
    try
    {
      String absenceId = editingAbsence.getAbsenceId();
      if (absenceId != null)
      {
        PresenceManagerPort port = getPort();
        port.removeAbsence(absenceId);
        setEditingAbsence(new Absence());
        absences = null;
        absenceCounters = null;
        workerStatistics = null;
        PresenceConfigBean.clearBean("workerCalendarBean");
        message("org.santfeliu.presence.web.resources.PresenceBundle",
          "absenceRemoved", null, FacesMessage.SEVERITY_INFO);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void newAbsence()
  {
    setEditingAbsence(new Absence());
    absences = null;
  }

  public String getAbsenceDocumentUrl()
  {
    try
    {
      DocumentManagerPort port = PresenceConfigBean.getDocumentPort();
      Document document =
        port.loadDocument(editingAbsence.getAbsenceDocId(),
          DocumentConstants.LAST_VERSION, ContentInfo.METADATA);
      String url = "/documents/" + document.getContent().getContentId();
      String xslTemplate =
        DictionaryUtils.getPropertyValue(document.getProperty(), "XSL");
      if (xslTemplate != null)
      {
        url += "?transform-to=text/html&XSL=" + xslTemplate;
      }
      return url;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String getJustificantDocumentUrl()
  {
    try
    {
      DocumentManagerPort port = PresenceConfigBean.getDocumentPort();
      Document document =
        port.loadDocument(editingAbsence.getJustificantDocId(),
          DocumentConstants.LAST_VERSION, ContentInfo.ID);
      return "/documents/" + document.getContent().getContentId();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public boolean isToday()
  {
    String weekDate = (String)getValue("#{weekDate}");
    String today = TextUtils.formatDate(new Date(), "yyyyMMdd");
    return today.equals(weekDate);
  }

  public void setZoomLevel(int zoomLevel)
  {
    if (zoomLevel >= 0 && zoomLevel <= zoomLevels.length)
    {
      this.zoomLevel =  zoomLevel;
      if (zoomLevel == 0)
      {
        pixelsPerHour = zoomLevels[0];        
      }
      else
      {
        double ratio = scroll / pixelsPerHour;
        pixelsPerHour = zoomLevels[zoomLevel - 1];
        scroll = ratio * pixelsPerHour;
      }
    }
  }

  public int getZoomLevel()
  {
    return zoomLevel;
  }

  public void zoom0()
  {
    setZoomLevel(0);
  }
  
  public void zoom1()
  {
    setZoomLevel(1);
  }

  public void zoom2()
  {
    setZoomLevel(2);
  }

  public void zoom3()
  {
    setZoomLevel(3);
  }

  public void toogleAbsencesVisibility()
  {
    absencesVisible = !absencesVisible;
  }

  public void toogleEntriesVisibility()
  {
    entriesVisible = !entriesVisible;
  }

  public void toogleScheduleVisibility()
  {
    scheduleVisible = !scheduleVisible;
  }

  public void processAbsence()
  {
    PresenceConfigBean.getInstance().processAbsence(editingAbsence);
  }

  private void addDates(int field, int number)
  {
    Calendar calendar = Calendar.getInstance();
    Date date = TextUtils.parseInternalDate(startDate);
    calendar.setTime(date);
    calendar.add(field, number);
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
    date = calendar.getTime();
    startDate = TextUtils.formatDate(date, "yyyyMMdd");
    calendar.add(Calendar.DAY_OF_MONTH, 6);
    date = calendar.getTime();
    endDate = TextUtils.formatDate(date, "yyyyMMdd");
  }
  
  private PresenceManagerPort getPort() throws Exception
  {
    return PresenceConfigBean.getPresencePort();
  }
}
