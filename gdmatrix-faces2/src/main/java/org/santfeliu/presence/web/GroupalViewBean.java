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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.faces.application.FacesMessage;
import org.apache.commons.lang.StringUtils;
import org.matrix.presence.Absence;
import org.matrix.presence.AbsenceFilter;
import org.matrix.presence.DayType;
import org.matrix.presence.Holiday;
import org.matrix.presence.HolidayFilter;
import org.matrix.presence.PresenceManagerPort;
import org.matrix.presence.Worker;
import org.matrix.presence.WorkerFilter;
import org.matrix.presence.WorkerSchedule;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.presence.util.Utils;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author realor
 */
public class GroupalViewBean extends FacesBean implements Savable
{
  private List<String> personIdList = new ArrayList<String>();
  private List<WorkerSchedule> workerSchedule;
  private boolean[] selection;
  private List<Day> days;
  private String startDate;
  private String endDate;
  private String periodStartDate;
  private String periodEndDate;
  private String workerToAdd;
  private String teamToAdd;
  private String weekTypeId;
  private String dayTypeId;
  private Map<String, List<Absence>> absenceMap;
  private HashSet<String> workerPersonIdSet;
  private double scroll;

  public GroupalViewBean()
  {
    PresenceMainBean presenceMainBean =
      (PresenceMainBean)getBean("presenceMainBean");
    Worker worker = presenceMainBean.getWorker();
    String personId = worker.getPersonId();
    try
    {
      personIdList = getPort().getWorkerGroup(personId);
    }
    catch (Exception ex)
    {
    }
    
    if (personIdList.isEmpty())
    {
      personIdList.add(personId);
      try
      {
        addValidatedWorkers(personId, false);
        addTeamMates(personId);
      }
      catch (Exception ex)
      {
      }
    }
    selection = new boolean[personIdList.size()];
    goToday();
  }

  public String getWeekTypeId()
  {
    return weekTypeId;
  }

  public void setWeekTypeId(String weekTypeId)
  {
    this.weekTypeId = weekTypeId;
  }

  public String getDayTypeId()
  {
    return dayTypeId;
  }

  public void setDayTypeId(String dayTypeId)
  {
    this.dayTypeId = dayTypeId;
  }

  public double getScroll()
  {
    return scroll;
  }

  public void setScroll(double scroll)
  {
    this.scroll = scroll;
  }
  
  public Absence getAbsenceDay()
  {
    WorkerSchedule schedule = (WorkerSchedule)getValue("#{schedule}");
    String personId = schedule.getPersonId();
    List<Absence> absences = getAbsenceMap().get(personId);
    if (absences == null) return null;
    int index = (Integer)getValue("#{index}");
    Day day = getDays().get(index);
    boolean found = false;
    int i = 0;
    Absence absence = null;
    while (i < absences.size() && !found)
    {
      absence = absences.get(i);
      String absenceStartDate = absence.getStartDateTime().substring(0, 8);
      String absenceEndDate = absence.getEndDateTime().substring(0, 8);
      if (absenceStartDate.compareTo(day.date) <= 0 &&
        absenceEndDate.compareTo(day.date) >= 0) found = true;
      i++;
    }
    setValue("#{absenceDay}", absence);
    return found ? absence : null;
  }

  public String getAbsenceDayInfo()
  {
    Absence absence = (Absence)getValue("#{absenceDay}");
    if (absence == null) return null;
    PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
    return presenceConfigBean.getAbsenceLabel(absence, false);
  }

  public String getAbsenceDayStatus()
  {
    Absence absence = (Absence)getValue("#{absenceDay}");
    if (absence == null) return "";
    return absence.getStatus();
  }

  public String getAbsenceDayStatusLabel()
  {
    Absence absence = (Absence)getValue("#{absenceDay}");
    if (absence == null) return null;
    return PresenceConfigBean.getInstance().
      getAbsenceStatusLabel(absence.getStatus());
  }
  
  public Map<String, List<Absence>> getAbsenceMap()
  {
    if (absenceMap == null)
    {
      try
      {
        absenceMap = new HashMap<String, List<Absence>>();
        PresenceManagerPort port = getPort();
        AbsenceFilter filter = new AbsenceFilter();
        filter.setStartDateTime(startDate + "000000");
        filter.setEndDateTime(endDate + "235959");
        filter.getPersonId().addAll(personIdList);
        List<Absence> absences = port.findAbsences(filter);
        for (Absence absence : absences)
        {
          String personId = absence.getPersonId();
          List<Absence> list = absenceMap.get(personId);
          if (list == null)
          {
            list = new ArrayList<Absence>();
            absenceMap.put(personId, list);
          }
          list.add(absence);
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return absenceMap;
  }

  public Worker getWorker()
  {
    String personId = (String)getValue("#{schedule.personId}");
    PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
    return presenceConfigBean.getWorker(personId);
  }

  public String getDayTypeCode()
  {
    String dayTypeId = (String)getValue("#{dayTypeId}");
    PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
    DayType dayType = presenceConfigBean.getDayType(dayTypeId);
    return dayType.getCode() == null ?
      dayType.getDayTypeId() : dayType.getCode();
  }

  public String getDayTypeLabel()
  {
    String dayTypeId = (String)getValue("#{dayTypeId}");
    PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
    DayType dayType = presenceConfigBean.getDayType(dayTypeId);
    return presenceConfigBean.getDayTypeLabel(dayType, false);
  }

  public String getDayTypeColor()
  {
    String dayTypeId = (String)getValue("#{dayTypeId}");
    PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
    DayType dayType = presenceConfigBean.getDayType(dayTypeId);
    return dayType.getColor() == null ?
      "transparent" : "#" + dayType.getColor();
  }

  public String getPeriodStartDate()
  {
    return periodStartDate;
  }

  public String getPeriodStartDateFormatted()
  {
    return TextUtils.formatDate(
      TextUtils.parseInternalDate(periodStartDate), "dd/MM/yyyy");
  }

  public void setPeriodStartDate(String periodStartDate)
  {
    this.periodStartDate = periodStartDate;
  }

  public String getPeriodEndDate()
  {
    return periodEndDate;
  }

  public String getPeriodEndDateFormatted()
  {
    return TextUtils.formatDate(
      TextUtils.parseInternalDate(periodEndDate), "dd/MM/yyyy");
  }

  public void setPeriodEndDate(String periodEndDate)
  {
    this.periodEndDate = periodEndDate;
  }

  public void cancelPeriod()
  {
    this.periodStartDate = null;
    this.periodEndDate = null;
  }

  public boolean isDaySelected()
  {
    if (periodStartDate == null) return false;
    int index = (Integer)getValue("#{index}");
    Day day = getDays().get(index);

    return periodStartDate.compareTo(day.date) <= 0 &&
      (periodEndDate == null || periodEndDate.compareTo(day.date) >= 0);
  }

  public String getWorkerToAdd()
  {
    return workerToAdd;
  }

  public void setWorkerToAdd(String workerToAdd)
  {
    this.workerToAdd = workerToAdd;
  }

  public String getTeamToAdd()
  {
    return teamToAdd;
  }

  public void setTeamToAdd(String teamToAdd)
  {
    this.teamToAdd = teamToAdd;
  }
  
  public boolean[] getSelection()
  {
    return selection;
  }

  public boolean isToday()
  {
    String date = (String)getValue("#{day.date}");
    return date.equals(TextUtils.formatDate(new Date(), "yyyyMMdd"));
  }

  public String getMonthYear()
  {
    return TextUtils.formatDate(TextUtils.parseInternalDate(startDate), "MMMM yyyy", getLocale()) +
      " - " + TextUtils.formatDate(TextUtils.parseInternalDate(endDate), "MMMM yyyy", getLocale());
  }

  public List<Day> getDays()
  {
    if (days == null)
    {
      days = new ArrayList<Day>();
      Calendar calendar = Calendar.getInstance();
      calendar.setFirstDayOfWeek(Calendar.MONDAY);
      calendar.setTime(TextUtils.parseInternalDate(startDate));
      Date ed = TextUtils.parseInternalDate(endDate);
      Date d = calendar.getTime();
      while (d.compareTo(ed) <= 0)
      {
        String date = TextUtils.formatDate(d, "yyyyMMdd");
        Day day = new Day();
        day.date = date;
        day.dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        day.label = calendar.getDisplayName(
          Calendar.DAY_OF_WEEK, Calendar.SHORT, getLocale());
        day.label = day.label.substring(0, 2).toUpperCase() + " " +
          day.getDayOfMonth();
        days.add(day);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        d = calendar.getTime();
      }

      List<Holiday> holidays = null;
      try
      {
        PresenceManagerPort port = getPort();
        HolidayFilter filter = new HolidayFilter();
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        holidays = port.findHolidays(filter);

        for (Holiday holiday : holidays)
        {
          for (Day day : days)
          {
            if (day.date.compareTo(holiday.getStartDate()) >= 0 &&
              day.date.compareTo(holiday.getEndDate()) <= 0)
            {
              day.holiday = holiday;
            }
          }
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return days;
  }

  public List<WorkerSchedule> getWorkerSchedule()
  {
    if (workerSchedule == null && !personIdList.isEmpty())
    {
      try
      {
        PresenceManagerPort port = getPort();
        workerSchedule = port.getSchedule(personIdList, startDate, endDate);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return workerSchedule;
  }

  public boolean isWorkerVisible()
  {
    PresenceMainBean presenceMainBean = 
      (PresenceMainBean)getBean("presenceMainBean");
    
    if (presenceMainBean.isPresenceAdministrator()) return true;
    if (!presenceMainBean.isInAdvancedMode()) return false;

    int workerIndex = (Integer)getValue("#{workerIndex}");
    String workerPersonId = personIdList.get(workerIndex);
    return getWorkerPersonIdSet().contains(workerPersonId);
  }

  public Collection<String> getWorkerPersonIdSet()
  {
    if (workerPersonIdSet == null)
    {
      try
      {
        workerPersonIdSet = new HashSet<String>();

        PresenceMainBean presenceMainBean = 
          (PresenceMainBean)getBean("presenceMainBean");
        
        String bossPersonId = presenceMainBean.getLoggedPersonId();

        PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
        workerPersonIdSet = new HashSet<String>();
        workerPersonIdSet.add(bossPersonId);
        presenceConfigBean.loadValidatedWorkers(
          bossPersonId, true, workerPersonIdSet);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return workerPersonIdSet;
  }

  public String show()
  {
    workerSchedule = null;
    absenceMap = null;
    days = null;
    return "groupal_view";
  }

  public String show(String date)
  {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(TextUtils.parseInternalDate(date));
    calendar.setFirstDayOfWeek(Calendar.MONDAY);
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
    startDate = TextUtils.formatDate(calendar.getTime(), "yyyyMMdd");
    endDate = Utils.addDate(startDate, 27);
    workerSchedule = null;
    absenceMap = null;
    days = null;
    return "groupal_view";
  }

  public String showWorkerWeek()
  {
    PresenceMainBean presenceMainBean =
      (PresenceMainBean)getBean("presenceMainBean");
    int workerIndex = (Integer)getValue("#{workerIndex}");
    String personId = personIdList.get(workerIndex);
    Worker worker = PresenceConfigBean.getInstance().getWorker(personId);
    return presenceMainBean.showWorkerWeek(worker);
  }

  public void goToday()
  {
    Calendar calendar = Calendar.getInstance();
    calendar.setFirstDayOfWeek(Calendar.MONDAY);
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
    startDate = TextUtils.formatDate(calendar.getTime(), "yyyyMMdd");
    endDate = Utils.addDate(startDate, 27);
    workerSchedule = null;
    absenceMap = null;
    days = null;
  }

  public void previousYear()
  {
    startDate = Utils.addDate(startDate, -364);
    endDate = Utils.addDate(endDate, -364);
    workerSchedule = null;
    absenceMap = null;
    days = null;
  }

  public void previousMonth()
  {
    startDate = Utils.addDate(startDate, -28);
    endDate = Utils.addDate(endDate, -28);
    workerSchedule = null;
    absenceMap = null;
    days = null;
  }

  public void previousWeek()
  {
    startDate = Utils.addDate(startDate, -7);
    endDate = Utils.addDate(endDate, -7);
    workerSchedule = null;
    absenceMap = null;
    days = null;
  }

  public void nextYear()
  {
    startDate = Utils.addDate(startDate, 364);
    endDate = Utils.addDate(endDate, 364);
    workerSchedule = null;
    absenceMap = null;
    days = null;
  }

  public void nextMonth()
  {
    startDate = Utils.addDate(startDate, 28);
    endDate = Utils.addDate(endDate, 28);
    workerSchedule = null;
    absenceMap = null;
    days = null;
  }

  public void nextWeek()
  {
    startDate = Utils.addDate(startDate, 7);
    endDate = Utils.addDate(endDate, 7);
    workerSchedule = null;
    absenceMap = null;
    days = null;
  }

  public void saveView()
  {
    try
    {
      PresenceMainBean presenceMainBean =
        (PresenceMainBean)getBean("presenceMainBean");
      Worker worker = presenceMainBean.getWorker();
      String personId = worker.getPersonId();
      int count = getPort().setWorkerGroup(personId, personIdList);
      message("org.santfeliu.presence.web.resources.PresenceBundle",
        "viewSaved", new Object[]{count}, FacesMessage.SEVERITY_INFO);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public void addWorker()
  {
    try
    {
      PresenceManagerPort port = getPort();
      WorkerFilter filter = new WorkerFilter();
      filter.setFullName(workerToAdd);
      filter.setTeam(teamToAdd);
      List<Worker> workers = port.findWorkers(filter);
      for (Worker worker : workers)
      {
        String personId = worker.getPersonId();
        if (personIdList.indexOf(personId) == -1)
        {
          personIdList.add(personId);
        }
      }
      selection = new boolean[personIdList.size()];
      workerToAdd = null;
      teamToAdd = null;
      absenceMap = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    workerSchedule = null;
  }

  public void removeSelectedWorkers()
  {
    for (int i = 0; i < selection.length; i++)
    {
      if (selection[i])
      {
        personIdList.set(i, null);
      }
    }
    personIdList.removeAll(Collections.singleton(null));
    selection = new boolean[personIdList.size()];
    workerSchedule = null;
    absenceMap = null;
  }

  public void addValidatedWorkers()
  {
    addValidatedWorkers(false);
  }

  public void addAllValidatedWorkers()
  {
    addValidatedWorkers(true);
  }

  public void addValidatedWorkers(boolean recursive)
  {
    try
    {
      for (int i = 0; i < selection.length; i++)
      {
        if (selection[i])
        {
          PresenceConfigBean presenceConfigBean =
            PresenceConfigBean.getInstance();
          String personId = personIdList.get(i);
          presenceConfigBean.loadValidatedWorkers(personId, recursive,
            personIdList);
        }
      }
      workerSchedule = null;
      selection = new boolean[personIdList.size()];
      absenceMap = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void addTeamMates()
  {
    try
    {
      for (int i = 0; i < selection.length; i++)
      {
        if (selection[i])
        {
          addTeamMates(personIdList.get(i));
        }
      }
      workerSchedule = null;
      selection = new boolean[personIdList.size()];
      absenceMap = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void captureDate()
  {
    Day day = (Day)getValue("#{day}");
    String date = day.getDate();
    if (StringUtils.isBlank(periodStartDate))
    {
      periodStartDate = date;
    }
    else if (StringUtils.isBlank(periodEndDate))
    {
      if (date.compareTo(periodStartDate) > 0)
      {
        periodEndDate = date;
      }
      else
      {
        periodStartDate = date;
      }
    }
    else
    {
      periodStartDate = date;
      periodEndDate = null;
    }
  }

  public void captureDateForWorker()
  {
    for (int i = 0; i < personIdList.size(); i++)
    {
      selection[i] = false;
    }
    int workerIndex = (Integer)getValue("#{workerIndex}");
    selection[workerIndex] = true;
    int index = (Integer)getValue("#{index}");
    String date = getDays().get(index).getDate();
    if (StringUtils.isBlank(periodStartDate))
    {
      periodStartDate = date;
    }
    else if (StringUtils.isBlank(periodEndDate))
    {
      if (date.compareTo(periodStartDate) > 0)
      {
        periodEndDate = date;
      }
      else
      {
        periodStartDate = date;
      }
    }
    else
    {
      periodStartDate = date;
      periodEndDate = null;
    }
  }

  public void assignWeekType()
  {
    try
    {
      PresenceManagerPort port = getPort();
      int count = 0;
      for (int i = 0; i < personIdList.size(); i++)
      {
        if (selection[i])
        {
          String personId = personIdList.get(i);
          port.setWorkerWeekType(personId, periodStartDate, periodEndDate, weekTypeId);
          count++;
        }
      }
      message("org.santfeliu.presence.web.resources.PresenceBundle",
        "weekTypeAssigned", new Object[]{count}, FacesMessage.SEVERITY_INFO);

      workerSchedule = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void assignDayType()
  {
    try
    {
      PresenceManagerPort port = getPort();
      int count = 0;
      for (int i = 0; i < personIdList.size(); i++)
      {
        if (selection[i])
        {
          String personId = personIdList.get(i);
          port.setWorkerDayType(personId, periodStartDate, dayTypeId);
          count++;
        }
      }
      workerSchedule = null;
      message("org.santfeliu.presence.web.resources.PresenceBundle",
        "dayTypeAssigned", new Object[]{count}, FacesMessage.SEVERITY_INFO);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private void addValidatedWorkers(String personId, boolean recursive)
    throws Exception
  {
    PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
    presenceConfigBean.loadValidatedWorkers(personId, recursive,
      personIdList);
  }

  private void addTeamMates(String personId) throws Exception
  {
    PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
    Worker worker = presenceConfigBean.getWorker(personId);
    if (worker != null)
    {
      String validatorPersonId = worker.getValidatorPersonId();
      if (validatorPersonId != null)
      {
        if (personIdList.indexOf(validatorPersonId) == -1)
        {
          personIdList.add(validatorPersonId);
        }
        presenceConfigBean.loadValidatedWorkers(validatorPersonId, false,
          personIdList);
      }
    }
  }

  public String getYear()
  {
    return startDate.substring(0, 4);
  }

  public void createAbsenceCounters()
  {
    try
    {
      PresenceManagerPort port = getPort();
      int created = 0;
      for (int i = 0; i < personIdList.size(); i++)
      {
        if (selection[i])
        {
          String personId = personIdList.get(i);
          created += port.createAbsenceCounters(personId, getYear(), false);
        }
      }
      message("org.santfeliu.presence.web.resources.PresenceBundle",
        "createdCounters", new Object[]{created}, FacesMessage.SEVERITY_INFO);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void copyAbsenceCounters()
  {
    try
    {
      List<String> selectedPersonIds = new ArrayList<String>();
      for (int i = 0; i < personIdList.size(); i++)
      {
        if (selection[i])
        {
          selectedPersonIds.add(personIdList.get(i));
        }
      }
      PresenceManagerPort port = getPort();
      PresenceMainBean presenceMainBean =
        (PresenceMainBean)getBean("presenceMainBean");
      Worker worker = presenceMainBean.getWorker();
      int copied = port.copyAbsenceCounters(worker.getPersonId(),
        selectedPersonIds, getYear());
      message("org.santfeliu.presence.web.resources.PresenceBundle",
        "copiedCounters", new Object[]{copied}, FacesMessage.SEVERITY_INFO);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public class Day implements Serializable
  {
    private String date;
    private int dayOfWeek;
    private Holiday holiday;
    private String label;

    public int getDayOfMonth()
    {
      return Integer.parseInt(date.substring(6, 8));
    }

    public int getDayOfWeek()
    {
      return dayOfWeek;
    }

    public String getLabel()
    {
      return label;
    }

    public String getDate()
    {
      return date;
    }

    public Holiday getHoliday()
    {
      return holiday;
    }

    public String getStyle()
    {
      return holiday == null ? null : "background-color:#" + holiday.getColor();
    }

    public boolean isSelected()
    {
      if (periodStartDate == null) return false;
      if (date.equals(periodStartDate)) return true;
      if (periodEndDate != null &&
          date.compareTo(periodStartDate) >= 0 &&
          date.compareTo(periodEndDate) <= 0)
        return true;
      return false;
    }
  }

  private PresenceManagerPort getPort() throws Exception
  {
    return PresenceConfigBean.getPresencePort();
  }
}
