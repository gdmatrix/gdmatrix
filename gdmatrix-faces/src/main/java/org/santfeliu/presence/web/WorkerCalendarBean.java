package org.santfeliu.presence.web;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.matrix.presence.Absence;
import org.matrix.presence.AbsenceCounterFilter;
import org.matrix.presence.AbsenceCounterView;
import org.matrix.presence.AbsenceCounting;
import org.matrix.presence.AbsenceFilter;
import org.matrix.presence.Holiday;
import org.matrix.presence.HolidayFilter;
import org.matrix.presence.PresenceManagerPort;
import org.matrix.presence.Worker;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.web.WebBean;
import org.santfeliu.presence.util.Utils;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author realor
 */
public class WorkerCalendarBean extends WebBean implements Savable
{
  private int year;
  private List<Month> months;
  private List<AbsenceCounterView> absenceCounters;
  private int absenceCount;
  private int absenceRequestedTime;
  private int absenceConsolidatedTime;
  private transient List<Absence> absences;

  public WorkerCalendarBean()
  {
    currentYear();
  }

  public int getYear()
  {
    return year;
  }

  public int getAbsenceCount()
  {
    loadAbsences();
    return absenceCount;
  }

  public int getAbsenceRequestedTime()
  {
    loadAbsences();
    return absenceRequestedTime;
  }

  public int getAbsenceConsolidatedTime()
  {
    loadAbsences();
    return absenceConsolidatedTime;
  }
  
  public boolean isToday()
  {
    String today = TextUtils.formatDate(new Date(), "yyyyMMdd");
    Day day = (Day)getValue("#{day}");
    return day.getDate().equals(today);
  }

  public Worker getWorker()
  {
    PresenceMainBean presenceMainBean = 
      (PresenceMainBean)getBean("presenceMainBean");
    return presenceMainBean.getWorker();
  }
  
  public List<Month> getMonths()
  {
    if (months == null)
    {
      List<Holiday> holidays = null;
      try
      {
        PresenceManagerPort port = PresenceConfigBean.getPresencePort();
        HolidayFilter filter = new HolidayFilter();
        filter.setStartDate(year + "0101");
        filter.setEndDate(year + "1231");
        holidays = port.findHolidays(filter);
      }
      catch (Exception ex)
      {
        error(ex);
      }
      loadAbsences();
      months = new ArrayList<Month>();
      Calendar calendar = Calendar.getInstance();
      calendar.setFirstDayOfWeek(Calendar.MONDAY);
      calendar.setMinimalDaysInFirstWeek(1);
      SimpleDateFormat dfDate = new SimpleDateFormat("yyyyMMdd");

      for (int monthNum = Calendar.JANUARY; monthNum <= Calendar.DECEMBER; monthNum++)
      {
        Month month = new Month();
        month.monthNumber = monthNum;
        months.add(month);

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthNum);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.getTime(); // BUG in calendar!
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.set(Calendar.HOUR_OF_DAY, 8);

        Week week = new Week();
        week.weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
        int lastWeekOfYear = week.weekOfYear;
        month.weeks.add(week);

        // last days of previous month
        while (calendar.get(Calendar.MONTH) != monthNum)
        {
          int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
          int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
          Day day = new Day();
          day.dayOfMonth = dayOfMonth;
          day.dayOfWeek = dayOfWeek;
          day.date = dfDate.format(calendar.getTime());
          day.visible = false;
          week.days.add(day);
          calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        // days of current month
        while (calendar.get(Calendar.MONTH) == monthNum)
        {
          int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
          int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
          int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

          if (weekOfYear != lastWeekOfYear)
          {
            week = new Week();
            week.weekOfYear =
              weekOfYear > lastWeekOfYear ? weekOfYear : lastWeekOfYear + 1;
            lastWeekOfYear = weekOfYear;
            month.weeks.add(week);
          }
          Day day = new Day();
          day.dayOfMonth = dayOfMonth;
          day.dayOfWeek = dayOfWeek;
          day.date = dfDate.format(calendar.getTime());
          day.visible = true;
          linkDay(day, holidays, absences);
          week.days.add(day);
          calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        // first days of next month
        Date date = calendar.getTime();
        int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
        while (weekOfYear == lastWeekOfYear)
        {
          int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
          int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
          Day day = new Day();
          day.dayOfMonth = dayOfMonth;
          day.dayOfWeek = dayOfWeek;
          day.date = dfDate.format(calendar.getTime());
          day.visible = false;
          week.days.add(day);
          calendar.add(Calendar.DAY_OF_YEAR, 1);
          weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
        }
        calendar.setTime(date);
      }
    }
    return months;
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
        filter.setYear(String.valueOf(year));
        filter.setPersonId(personId);
        filter.setCounterVisible(Boolean.TRUE);
        PresenceManagerPort port = PresenceConfigBean.getPresencePort();
        absenceCounters = port.findAbsenceCounterViews(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return absenceCounters;
  }

  public void previousYear()
  {
    year--;
    months = null;
    absenceCounters = null;
  }

  public void currentYear()
  {
    year = Calendar.getInstance().get(Calendar.YEAR);
    months = null;
    absenceCounters = null;
  }

  public void nextYear()
  {
    year++;
    months = null;
    absenceCounters = null;
  }

  public String update()
  {
    months = null;
    absenceCounters = null;
    return show();
  }
  
  public void linkDay(Day day,
    List<Holiday> holidays, List<Absence> absences)
  {
    String date = day.getDate();
    if (holidays != null)
    {
      boolean found = false;
      Iterator<Holiday> iter = holidays.iterator();
      while (!found && iter.hasNext())
      {
        Holiday holiday = iter.next();
        if (Utils.compareDates(date, holiday.getStartDate()) >=0 &&
          Utils.compareDates(date, holiday.getEndDate()) <= 0)
        {
          day.holiday = holiday;
          found = true;
        }
      }
    }
    if (absences != null)
    {
      boolean found = false;
      Iterator<Absence> iter = absences.iterator();
      while (!found && iter.hasNext())
      {
        Absence absence = iter.next();
        if (Utils.compareDates(date, absence.getStartDateTime().substring(0, 8)) >=0 &&
          Utils.compareDates(date, absence.getEndDateTime().substring(0, 8)) <= 0)
        {
          day.absence = absence;
          found = true;
        }
      }
    }
  }

  public String show()
  {
    return "worker_calendar";
  }

  private void loadAbsences()
  {
    if (absences == null)
    {
      try
      {
        PresenceManagerPort port = PresenceConfigBean.getPresencePort();
        AbsenceFilter filter = new AbsenceFilter();
        filter.setStartDateTime(year + "0101000000");
        filter.setEndDateTime(year + "1231235959");
        filter.setSplitByDay(false);
        filter.getPersonId().add(getWorker().getPersonId());
        absences = port.findAbsences(filter);
        updateCounters(absences);
      }
      catch (Exception ex)
      {
        error(ex);
      }    
    }
  }
  
  private void updateCounters(List<Absence> absences)
  {
    absenceCount = absences.size();
    absenceRequestedTime = 0;
    absenceConsolidatedTime = 0;
    for (Absence absence : absences)
    {
      absenceRequestedTime += absence.getRequestedTime();
      absenceConsolidatedTime += absence.getConsolidatedTime();
    }
  }

  public class Month implements Serializable
  {
    private int monthNumber;
    private List<Week> weeks = new ArrayList<Week>();

    public String getName()
    {
      Locale locale = getFacesContext().getViewRoot().getLocale();
      Calendar calendar = Calendar.getInstance();
      calendar.set(Calendar.MONTH, monthNumber);
      calendar.set(Calendar.DAY_OF_MONTH, 1);
      calendar.set(Calendar.HOUR_OF_DAY, 12);
      return TextUtils.getStandaloneMonthName(calendar, Calendar.MONTH, locale);
    }

    public int getMonthNumber()
    {
      return monthNumber;
    }

    public List<Week> getWeeks()
    {
      return weeks;
    }
  }

  public class Week implements Serializable
  {
    private int weekOfYear;
    private List<Day> days = new ArrayList<Day>();

    public int getWeekOfYear()
    {
      return weekOfYear;
    }

    public List<Day> getDays()
    {
      return days;
    }
  }

  public class Day implements Serializable
  {
    private int dayOfMonth;
    private int dayOfWeek;
    private String description;
    private String date;
    private boolean visible;
    private Holiday holiday;
    private Absence absence;

    public int getDayOfMonth()
    {
      return dayOfMonth;
    }

    public int getDayOfWeek()
    {
      return dayOfWeek;
    }

    public String getDate()
    {
      return date;
    }

    public String getDescription()
    {
      return description;
    }

    public Holiday getHoliday()
    {
      return holiday;
    }

    public Absence getAbsence()
    {
      return absence;
    }

    public String getStyle()
    {
      if (absence == null && holiday == null) return null;
      StringBuilder style = new StringBuilder();
      if (absence != null)
      {
        style.append("border:1px solid red;background-image:url(images/absence.png);");
      }
      if (holiday != null)
      {
        if (holiday.getColor() == null)
          style.append("background-color: #60FF60;");
        else
          style.append("background-color:#").
            append(holiday.getColor()).append(";");
      }
      return style.toString();
    }

    public String getTitle()
    {
      if (absence != null)
      {
        PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
        return presenceConfigBean.getAbsenceLabel(absence, true);
      }
      return holiday == null ? null : holiday.getDescription();
    }

    public String showWeek()
    {
      PresenceMainBean presenceMainBean = 
        (PresenceMainBean)getBean("presenceMainBean");
      return presenceMainBean.showView("worker_week", date);
    }

    public boolean isVisible()
    {
      return visible;
    }

    public void setVisible(boolean visible)
    {
      this.visible = visible;
    }
  }
}
