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
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.faces.model.SelectItem;
import org.apache.commons.lang.StringUtils;
import org.matrix.kernel.KernelManagerPort;
import org.matrix.kernel.PersonFilter;
import org.matrix.kernel.PersonView;
import org.matrix.presence.AbsenceCounting;
import org.matrix.presence.AbsenceType;
import org.matrix.presence.AbsenceTypeFilter;
import org.matrix.presence.DayType;
import org.matrix.presence.DayTypeFilter;
import org.matrix.presence.Holiday;
import org.matrix.presence.HolidayFilter;
import org.matrix.presence.PresenceEntryType;
import org.matrix.presence.PresenceEntryTypeFilter;
import org.matrix.presence.PresenceManagerPort;
import org.matrix.presence.PresenceParameter;
import org.matrix.presence.WeekType;
import org.matrix.presence.WeekTypeFilter;
import org.matrix.presence.WorkReduction;
import org.matrix.presence.WorkReductionFilter;
import org.matrix.presence.Worker;
import org.matrix.presence.WorkerFilter;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.presence.util.Utils;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.WebBean;

/**
 *
 * @author realor
 */
public class PresenceAdminBean extends WebBean implements Savable
{
  private List<PresenceEntryType> entryTypes;
  private List<AbsenceType> absenceTypes;
  private List<WeekType> weekTypes;
  private List<DayType> dayTypes;
  private List<Holiday> holidays;
  private List<WorkReduction> workReductions;
  private List<Worker> workers = new ArrayList<Worker>();
  private List<PresenceParameter> parameters;
  private int workerPage;
  private int workerCount = -1;
  
  private PresenceEntryType editingEntryType;
  private AbsenceType editingAbsenceType;
  private DayType editingDayType;
  private WeekType editingWeekType;
  private Holiday editingHoliday;
  private WorkReduction editingWorkReduction;
  private Worker editingWorker;
  private PresenceParameter editingParameter;

  private int holidayYear;
  private int workReductionYear;

  private final WorkerFilter workerFilter = new WorkerFilter();
  private final PersonFilter personFilter = new PersonFilter();
  private transient List<SelectItem> workerSelectItems;
  private List<PersonView> personList;
  private static final int WORKERS_PAGE_SIZE = 100;

  public PresenceAdminBean()
  {
    currentHolidayYear();
    currentWorkReductionYear();
  }

  public String show()
  {
    return "presence_admin";
  }

  /* PresenceEntryType */

  public List<PresenceEntryType> getEntryTypes()
  {
    if (entryTypes == null)
    {
      try
      {
        PresenceEntryTypeFilter filter = new PresenceEntryTypeFilter();
        filter.setTeam("%");
        entryTypes = getPort().findPresenceEntryTypes(filter);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return entryTypes;
  }

  public PresenceEntryType getEditingEntryType()
  {
    return editingEntryType;
  }

  public void newEntryType()
  {
    editingEntryType = new PresenceEntryType();
    entryTypes.add(editingEntryType);
  }

  public void storeEntryType()
  {
    try
    {
      getPort().storePresenceEntryType(editingEntryType);
      PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
      presenceConfigBean.getPresenceEntryTypeMap().remove(
        editingEntryType.getEntryTypeId());
      editingEntryType = null;
      entryTypes = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void removeEntryType()
  {
    try
    {
      PresenceEntryType entryType = (PresenceEntryType)getValue("#{entryType}");
      getPort().removePresenceEntryType(entryType.getEntryTypeId());
      PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
      presenceConfigBean.getPresenceEntryTypeMap().remove(
        entryType.getEntryTypeId());
      editingEntryType = null;
      entryTypes = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void cancelEntryType()
  {
    editingEntryType = null;
    entryTypes = null;
  }

  public void editEntryType()
  {
    editingEntryType = (PresenceEntryType)getValue("#{entryType}");
  }

  /* AbsenceType */

  public SelectItem[] getAbsenceCountingSelectItems()
  {
    ResourceBundle bundle = ResourceBundle.getBundle(
     "org.santfeliu.presence.web.resources.PresenceBundle", getLocale());    
    return FacesUtils.getEnumSelectItems(AbsenceCounting.class, bundle);
  }
  
  public String getAbsenceCountingLabel()
  {
    AbsenceCounting counting = (AbsenceCounting)getValue("#{absenceType.counting}");
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.presence.web.resources.PresenceBundle", getLocale());
    return bundle.getString("org.matrix.presence.AbsenceCounting." + counting.value());
  }
  
  public List<AbsenceType> getAbsenceTypes()
  {
    if (absenceTypes == null)
    {
      try
      {
        AbsenceTypeFilter filter = new AbsenceTypeFilter();
        absenceTypes = getPort().findAbsenceTypes(filter);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return absenceTypes;
  }

  public AbsenceType getEditingAbsenceType()
  {
    return editingAbsenceType;
  }

  public void newAbsenceType()
  {
    editingAbsenceType = new AbsenceType();
    absenceTypes.add(editingAbsenceType);
  }

  public void storeAbsenceType()
  {
    try
    {
      getPort().storeAbsenceType(editingAbsenceType);
      editingAbsenceType = null;
      absenceTypes = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void removeAbsenceType()
  {
    try
    {
      AbsenceType absenceType = (AbsenceType)getValue("absenceType");
      getPort().removeAbsenceType(absenceType.getAbsenceTypeId());
      editingAbsenceType = null;
      absenceTypes = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void cancelAbsenceType()
  {
    editingAbsenceType = null;
    absenceTypes = null;
  }

  public void editAbsenceType()
  {
    editingAbsenceType = (AbsenceType)getValue("#{absenceType}");
  }

  /* DayWeek */

  public List<DayType> getDayTypes()
  {
    if (dayTypes == null)
    {
      try
      {
        DayTypeFilter filter = new DayTypeFilter();
        dayTypes = getPort().findDayTypes(filter);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return dayTypes;
  }

  public DayType getEditingDayType()
  {
    return editingDayType;
  }

  public void newDayType()
  {
    editingDayType = new DayType();
    dayTypes.add(editingDayType);
  }

  public void storeDayType()
  {
    try
    {
      getPort().storeDayType(editingDayType);
      PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
      presenceConfigBean.getDayTypeMap().remove(editingDayType.getDayTypeId());
      editingDayType = null;
      dayTypes = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void removeDayType()
  {
    try
    {
      DayType dayType = (DayType)getValue("#{dayType}");
      getPort().removeDayType(dayType.getDayTypeId());
      PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
      presenceConfigBean.getDayTypeMap().remove(editingDayType.getDayTypeId());
      editingDayType = null;
      dayTypes = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void cancelDayType()
  {
    editingDayType = null;
    dayTypes = null;
  }

  public void editDayType()
  {
    editingDayType = (DayType)getValue("#{dayType}");
  }

  /* WorkerWeek */

  public List<WeekType> getWeekTypes()
  {
    if (weekTypes == null)
    {
      try
      {
        WeekTypeFilter filter = new WeekTypeFilter();
        weekTypes = getPort().findWeekTypes(filter);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return weekTypes;
  }

  public WeekType getEditingWeekType()
  {
    return editingWeekType;
  }

  public void newWeekType()
  {
    editingWeekType = new WeekType();
    weekTypes.add(editingWeekType);
  }

  public void storeWeekType()
  {
    try
    {
      getPort().storeWeekType(editingWeekType);
      editingWeekType = null;
      weekTypes = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void removeWeekType()
  {
    try
    {
      WeekType weekType = (WeekType)getValue("#{weekType}");
      getPort().removeWeekType(weekType.getWeekTypeId());
      editingWeekType = null;
      weekTypes = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void cancelWeekType()
  {
    editingWeekType = null;
    weekTypes = null;
  }

  public void editWeekType()
  {
    editingWeekType = (WeekType)getValue("#{weekType}");
  }

  /* Holiday */

  public int getHolidayYear()
  {
    return holidayYear;
  }

  public List<Holiday> getHolidays()
  {
    if (holidays == null)
    {
      try
      {
        HolidayFilter filter = new HolidayFilter();
        filter.setStartDate(String.valueOf(holidayYear) + "0101");
        filter.setEndDate(String.valueOf(holidayYear) + "1231");
        holidays = getPort().findHolidays(filter);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return holidays;
  }

  public Holiday getEditingHoliday()
  {
    return editingHoliday;
  }

  public void newHoliday()
  {
    editingHoliday = new Holiday();
    holidays.add(editingHoliday);
  }

  public void storeHoliday()
  {
    try
    {
      getPort().storeHoliday(editingHoliday);
      editingHoliday = null;
      holidays = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void removeHoliday()
  {
    try
    {
      Holiday holiday = (Holiday)getValue("#{holiday}");
      getPort().removeHoliday(holiday.getHolidayId());
      editingHoliday = null;
      holidays = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void copyHoliday()
  {
    try
    {
      Holiday holiday = (Holiday)getValue("#{holiday}");
      String startDate = holiday.getStartDate();
      String endDate = holiday.getEndDate();
      Calendar calendar = Calendar.getInstance();
      Date date;
      int year;

      date = TextUtils.parseInternalDate(startDate);
      calendar.setTime(date);
      year = calendar.get(Calendar.YEAR);
      calendar.set(Calendar.YEAR, year + 1);
      startDate = TextUtils.formatDate(calendar.getTime(), "yyyyMMdd");

      date = TextUtils.parseInternalDate(endDate);
      calendar.setTime(date);
      year = calendar.get(Calendar.YEAR);
      calendar.set(Calendar.YEAR, year + 1);
      endDate = TextUtils.formatDate(calendar.getTime(), "yyyyMMdd");

      PresenceManagerPort port = getPort();
      HolidayFilter filter = new HolidayFilter();
      filter.setStartDate(startDate);
      filter.setEndDate(endDate);
      if (port.countHolidays(filter) == 0) // not overlapped
      {
        Holiday holidayClone = new Holiday();
        holidayClone.setStartDate(startDate);
        holidayClone.setEndDate(endDate);
        holidayClone.setDescription(holiday.getDescription());
        holidayClone.setColor(holiday.getColor());
        holidayClone.setOptional(holiday.isOptional());
        port.storeHoliday(holidayClone);
        info("HOLIDAY_COPIED");
      }
      editingHoliday = null;
      holidays = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void cancelHoliday()
  {
    editingHoliday = null;
    holidays = null;
  }

  public void editHoliday()
  {
    editingHoliday = (Holiday)getValue("#{holiday}");
  }

  public void currentHolidayYear()
  {
    Calendar calendar = Calendar.getInstance();
    holidayYear = calendar.get(Calendar.YEAR);
    holidays = null;
  }

  public void previousHolidayYear()
  {
    holidayYear--;
    holidays = null;
  }

  public void nextHolidayYear()
  {
    holidayYear++;
    holidays = null;
  }

  /* WorkReduction */

  public int getWorkReductionYear()
  {
    return workReductionYear;
  }

  public List<WorkReduction> getWorkReductions()
  {
    if (workReductions == null)
    {
      try
      {
        WorkReductionFilter filter = new WorkReductionFilter();
        filter.setStartDate(String.valueOf(workReductionYear) + "0101");
        filter.setEndDate(String.valueOf(workReductionYear) + "1231");
        workReductions = getPort().findWorkReductions(filter);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return workReductions;
  }

  public WorkReduction getEditingWorkReduction()
  {
    return editingWorkReduction;
  }

  public void newWorkReduction()
  {
    editingWorkReduction = new WorkReduction();
    workReductions.add(editingWorkReduction);
  }

  public void storeWorkReduction()
  {
    try
    {
      getPort().storeWorkReduction(editingWorkReduction);
      editingWorkReduction = null;
      workReductions = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void removeWorkReduction()
  {
    try
    {
      WorkReduction workReduction = (WorkReduction)getValue("#{workReduction}");
      getPort().removeWorkReduction(workReduction.getReductionId());
      editingWorkReduction = null;
      workReductions = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void cancelWorkReduction()
  {
    editingWorkReduction = null;
    workReductions = null;
  }

  public void editWorkReduction()
  {
    editingWorkReduction = (WorkReduction)getValue("#{workReduction}");
  }

  public void currentWorkReductionYear()
  {
    Calendar calendar = Calendar.getInstance();
    workReductionYear = calendar.get(Calendar.YEAR);
    workReductions = null;
  }

  public void previousWorkReductionYear()
  {
    workReductionYear--;
    workReductions = null;
  }

  public void nextWorkReductionYear()
  {
    workReductionYear++;
    workReductions = null;
  }

  /* Worker */

  public WorkerFilter getWorkerFilter()
  {
    return workerFilter;
  }

  public List<SelectItem> getWorkerSelectItems()
  {
    if (workerSelectItems == null)
    {
      try
      {
        workerSelectItems = new ArrayList<SelectItem>();
        WorkerFilter filter = new WorkerFilter();
        List<Worker> itemWorkers = getPort().findWorkers(filter);
        for (Worker worker : itemWorkers)
        {
          SelectItem selectItem = new SelectItem();
          selectItem.setLabel(worker.getFullName());
          selectItem.setValue(worker.getPersonId());
          workerSelectItems.add(selectItem);
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return workerSelectItems;
  }

  public List<Worker> getWorkers()
  {
    return workers;
  }

  public int getWorkerPage()
  {
    return workerPage + 1;
  }
  
  public int getWorkerFirst()
  {
    return workerPage * WORKERS_PAGE_SIZE + 1;
  }

  public int getWorkerLast()
  {
    return workerPage * WORKERS_PAGE_SIZE + workers.size();    
  }
  
  public int getWorkerCount()
  {
    return workerCount;
  }
  
  public void previousWorkerPage()
  {
    if (workerPage > 0)
    {
      workerPage--;
      findWorkers(workerPage);
    }
  }

  public void nextWorkerPage()
  {
    if (getWorkerLast() < workerCount)
    {
      workerPage++;
      findWorkers(workerPage);
    }
  }
  
  public void findWorkers()
  {
    try
    {
      PresenceManagerPort port = getPort();
      workerFilter.setFirstResult(0);
      workerFilter.setMaxResults(0);
      workerCount = port.countWorkers(workerFilter);
      workerPage = 0;
      if (workerCount > 0)
      {
        findWorkers(workerPage);
      }
      else
      {
        workers = new ArrayList<Worker>();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void findWorkers(int page)
  {
    try
    {
      editingWorker = null;
      PresenceManagerPort port = getPort();
      workerFilter.setFirstResult(page * WORKERS_PAGE_SIZE);
      workerFilter.setMaxResults(WORKERS_PAGE_SIZE);
      workers = port.findWorkers(workerFilter);
      PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
      for (Worker worker : workers)
      {
        presenceConfigBean.getWorkerMap().put(worker.getPersonId(), worker);
      }
      String validatorPersonId = workerFilter.getValidatorPersonId();
      if (validatorPersonId != null)
      {
        Worker validator = presenceConfigBean.getWorker(validatorPersonId);
        if (validator != null)
        {
          workers.add(0, validator);
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void findWorkerDescendants()
  {
    Worker worker = (Worker)getValue("#{worker}");
    workerFilter.setValidatorPersonId(worker.getPersonId());
    workerFilter.setFullName(null);
    findWorkers();
  }

  public void findValidatorDescendants()
  {
    PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
    Worker worker = (Worker)getValue("#{worker}");
    Worker validator = presenceConfigBean.getWorker(worker.getValidatorPersonId());
    if (validator != null)
    {
      workerFilter.setValidatorPersonId(validator.getPersonId());
      workerFilter.setFullName(null);
      findWorkers();
    }
  }

  public void clearWorkers()
  {
    workerPage = -1;
    workerFilter.setFullName(null);
    workerFilter.setValidatorPersonId(null);
    workers = new ArrayList<Worker>();
  }

  public String getFilterValidatorFullName()
  {
    PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
    String personId = workerFilter.getValidatorPersonId();
    return personId == null ? null :
      presenceConfigBean.getWorker(personId).getFullName();
  }

  public String getValidatorFullName()
  {
    PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
    Worker worker = (Worker)getValue("#{worker}");
    String personId = worker.getValidatorPersonId();
    return StringUtils.isBlank(personId) ? null :
      presenceConfigBean.getWorker(personId).getFullName();
  }

  public Worker getEditingWorker()
  {
    return editingWorker;
  }

  public void newWorker()
  {
    editingWorker = new Worker();
    personList = null;
    personFilter.setFullName(null);
  }

  public void storeWorker()
  {
    try
    {
      Worker worker = getPort().storeWorker(editingWorker);
      copyWorker(worker, editingWorker);
      PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
      presenceConfigBean.getWorkerMap().remove(worker.getPersonId());
      editingWorker = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void removeWorker()
  {
    try
    {
      Worker worker = (Worker)getValue("#{worker}");
      getPort().removeWorker(worker.getPersonId());
      int index = workers.indexOf(worker);
      if (index != -1)
      {
        workers.remove(index);
      }
      PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
      presenceConfigBean.getWorkerMap().remove(worker.getPersonId());
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void cancelWorker()
  {
    try
    {
      Worker worker = (Worker)getValue("#{worker}");
      try
      {
        Worker prevWorker = getPort().loadWorker(worker.getPersonId());
        copyWorker(prevWorker, worker);
      }
      catch (Exception ex) // NOT FOUND
      {
        int index = workers.indexOf(worker);
        if (index != -1)
        {
          workers.remove(index);
        }
      }
      editingWorker = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public String showWorkerWeek()
  {
    PresenceMainBean presenceMainBean = 
      (PresenceMainBean)getBean("presenceMainBean");
    Worker selectedWorker = (Worker)getValue("#{worker}");
    return presenceMainBean.showWorkerWeek(selectedWorker);
  }
  
  public void editWorker()
  {
    editingWorker = (Worker)getValue("#{worker}");
  }

  public PersonFilter getPersonFilter()
  {
    return personFilter;
  }

  public List<PersonView> getPersonList()
  {
    return personList;
  }

  /* PresenceParameter */
  
  public PresenceParameter getEditingParameter()
  {
    return editingParameter;
  }
  
  public List<PresenceParameter> getParameters()
  {
    if (parameters == null)
    {
      try
      {
        parameters = getPort().findParameters();
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return parameters;
  }
  
  public void storeParameter()
  {
    try
    {
      getPort().storeParameter(editingParameter);
      editingParameter = null;
      parameters = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void removeParameter()
  {
    try
    {
      PresenceParameter parameter = (PresenceParameter)getValue("#{parameter}");
      getPort().removeParameter(parameter.getParameterId());
      editingParameter = null;
      parameters = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void newParameter()
  {
    editingParameter = new PresenceParameter();
    parameters.add(editingParameter);    
  }
  
  public void cancelParameter()
  {
    editingParameter = null;
    parameters = null;
  }

  public void editParameter()
  {
    editingParameter = (PresenceParameter)getValue("#{parameter}");
  }
  
  public List<String> getDayTypeCodeList()
  {
    return dayTypeCodeList;
  }

  public List<String> getDayTypeLabelList()
  {
    return dayTypeLabelList;
  }

  public List<String> getDayTypeColorList()
  {
    return dayTypeColorList;
  }

  public String getAbsenceEntryTypeLabel()
  {
    AbsenceType absenceType = (AbsenceType)getValue("#{absenceType}");
    String presenceEntryTypeId = absenceType.getEntryTypeId();
    if (presenceEntryTypeId == null) return null;
    PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
    PresenceEntryType presenceEntryType =
      presenceConfigBean.getPresenceEntryType(presenceEntryTypeId);
    return presenceConfigBean.getPresenceEntryTypeLabel(presenceEntryType, false);
  }

  public String getAbsenceEntryTypeCode()
  {
    AbsenceType absenceType = (AbsenceType)getValue("#{absenceType}");
    String presenceEntryTypeId = absenceType.getEntryTypeId();
    if (presenceEntryTypeId == null) return null;
    PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
    PresenceEntryType presenceEntryType =
      presenceConfigBean.getPresenceEntryType(presenceEntryTypeId);
    return presenceEntryType.getCode() == null ?
      presenceEntryType.getEntryTypeId() :
      presenceEntryType.getCode();
  }

  public String getAbsenceEntryTypeColor()
  {
    AbsenceType absenceType = (AbsenceType)getValue("#{absenceType}");
    String presenceEntryTypeId = absenceType.getEntryTypeId();
    if (presenceEntryTypeId != null)
    {
      PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
      PresenceEntryType presenceEntryType =
        presenceConfigBean.getPresenceEntryType(presenceEntryTypeId);
      if (presenceEntryType.getColor() != null)
      {
        return "#" + presenceEntryType.getColor();
      }
    }
    return "transparent";
  }

  public void findPersons()
  {
    try
    {
      KernelManagerPort port = PresenceConfigBean.getKernelPort();
      personFilter.setMaxResults(10);
      personList = port.findPersonViews(personFilter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void cancelFindPersons()
  {
    personList = null;
    editingWorker = null;
  }

  public void selectPerson()
  {
    try
    {
      PersonView personView = (PersonView)getValue("#{person}");
      try
      {
        Worker worker = getPort().loadWorker(personView.getPersonId());
        addWorkerIfNotFound(worker);
        editingWorker = null;
      }
      catch (Exception ex) // not found
      {
        editingWorker.setPersonId(personView.getPersonId());
        editingWorker.setFullName(personView.getFullName());
        addWorkerIfNotFound(editingWorker);
      }
    }
    catch (Exception ex2)
    {
      error(ex2);
    }
  }

  public String getScripts()
  {
    StringBuilder buffer = new StringBuilder();
    addScriptFile("/plugins/color/jscolor.js", buffer);
    return buffer.toString();
  }

  public int getWeekTime()
  {
    int weekTime = 0;
    WeekType weekType = (WeekType)getValue("#{weekType}");
    for (int i = 0; i < 7; i++)
    {
      DayType dayType = getWeekDayType(weekType, i);
      if (dayType != null)
      {
        weekTime +=
          Utils.getTimesDuration(dayType.getInTime1(), dayType.getOutTime1());
        if (dayType.getInTime2() != null)
        {
          weekTime +=
            Utils.getTimesDuration(dayType.getInTime2(), dayType.getOutTime2());
        }
      }
    }
    return weekTime;
  }

  private void addScriptFile(String path, StringBuilder buffer)
  {
    String contextPath = getContextPath();
    buffer.append("<script src=\"").append(contextPath);
    buffer.append(path);
    buffer.append("\" type=\"text/javascript\">\n</script>\n");
  }

  private void copyWorker(Worker src, Worker dest)
  {
    dest.setPersonId(src.getPersonId());
    dest.setFullName(src.getFullName());
    dest.setValidatorPersonId(src.getValidatorPersonId());
  }

  private void addWorkerIfNotFound(Worker worker)
  {
    Iterator<Worker> iter = workers.iterator();
    boolean found = false;
    while (iter.hasNext() && !found)
    {
      Worker next = iter.next();
      found = next.getPersonId().equals(worker.getPersonId());
    }
    if (!found)
    {
      workers.add(worker);
    }
  }

  private DayType getWeekDayType(int index)
  {
    WeekType weekType = (WeekType)getValue("#{weekType}");
    return getWeekDayType(weekType, index);
  }

  private DayType getWeekDayType(WeekType weekType, int index)
  {
    String dayTypeId = null;
    switch (index)
    {
      case 0: dayTypeId = weekType.getMondayTypeId(); break;
      case 1: dayTypeId = weekType.getTuesdayTypeId(); break;
      case 2: dayTypeId = weekType.getWednesdayTypeId(); break;
      case 3: dayTypeId = weekType.getThursdayTypeId(); break;
      case 4: dayTypeId = weekType.getFridayTypeId(); break;
      case 5: dayTypeId = weekType.getSaturdayTypeId(); break;
      case 6: dayTypeId = weekType.getSundayTypeId(); break;
    }
    if (dayTypeId == null) return null;
    PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
    return (DayType)presenceConfigBean.getDayType(dayTypeId);
  }

  public abstract class DayTypeList extends AbstractList<String>
    implements Serializable
  {
    @Override
    public int size()
    {
      return 7;
    }
  }

  private final List<String> dayTypeCodeList = new DayTypeList()
  {
    @Override
    public String get(int index)
    {
      DayType dayType = getWeekDayType(index);
      if (dayType == null) return null;
      if (dayType.getCode() == null) return dayType.getDayTypeId();
      return dayType.getCode();
    }
  };

  private final List<String> dayTypeLabelList = new DayTypeList()
  {
    @Override
    public String get(int index)
    {
      DayType dayType = getWeekDayType(index);
      if (dayType == null) return null;
      return PresenceConfigBean.getInstance().getDayTypeLabel(dayType, false);
    }
  };

  private final List<String> dayTypeColorList = new DayTypeList()
  {
    @Override
    public String get(int index)
    {
      DayType dayType = getWeekDayType(index);
      if (dayType != null && dayType.getColor() != null)
      {
        return "#" + dayType.getColor();
      }
      return "transparent";
    }
  };

  private PresenceManagerPort getPort() throws Exception
  {
    return PresenceConfigBean.getPresencePort();
  }
}
