package org.santfeliu.presence.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.faces.model.SelectItem;
import org.apache.commons.lang.StringUtils;
import org.matrix.presence.PresenceEntry;
import org.matrix.presence.PresenceEntryType;
import org.matrix.presence.PresenceManagerPort;
import org.matrix.presence.WorkerStatistics;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.WebBean;

/**
 *
 * @author realor
 */
public class PresenceStatsBean extends WebBean implements Savable
{
  private WorkerStatistics workerStatistics;
  private List<PresenceEntryTypeStatistics> entryTypeStatistics;
  private String startDate;
  private String endDate;
  private transient List<SelectItem> periodSelectItems;
  private String period;
  private int selectedTabIndex;

  public PresenceStatsBean()
  {
    period = TextUtils.formatDate(new Date(), "yyyyMM");
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

  public int getSelectedTabIndex()
  {
    return selectedTabIndex;
  }

  public void setSelectedTabIndex(int selectedTabIndex)
  {
    this.selectedTabIndex = selectedTabIndex;
  }
 
  public WorkerStatistics getWorkerStatistics()
  {
    return workerStatistics;
  }

  public List<PresenceEntryTypeStatistics> getEntryTypeStatistics()
  {
    selectedTabIndex = 1;
    return entryTypeStatistics;
  }
  
  public String getPeriod()
  {
    return period;
  }

  public void setPeriod(String period)
  {
    this.period = period;
  }

  public boolean isDatesEnabled()
  {
    return StringUtils.isBlank(period);
  }

  public boolean isCurrentPeriod()
  {
    String today = TextUtils.formatDate(new Date(), "yyyyMMdd");
    return startDate.compareTo(today) <= 0 && today.compareTo(endDate) <= 0;
  }
  
  public String show()
  {
    return "presence_stats";
  }
  
  public String getCurrentDifferentialMessage()
  {
    return getProperty("currentDifferentialMessage");
  }

  public String getPreviousDifferentialMessage()
  {
    return getProperty("previousDifferentialMessage");
  }
  
  public String getFaceMessage()
  {
    selectedTabIndex = 2;
    return getProperty("faceMessage");
  }  
  
  public String getFaceScript()
  {
    StringBuilder builder = new StringBuilder();
    builder.append("<script type=\"text/javascript\" src=\"");
    builder.append(getContextPath());
    builder.append("/common/presence/js/face.js");
    builder.append("\"></script>");
    builder.append("<script type=\"text/javascript\">paintFace('face_panel',");
    builder.append(workerStatistics.getComplianceDegree());
    builder.append(",");
    builder.append(workerStatistics.getPunctualityDegree());
    builder.append(",");
    builder.append(workerStatistics.getVeracityDegree());
    builder.append(",");
    builder.append(workerStatistics.getPresenceDegree());
    builder.append(");</script>");
    return builder.toString();
  }
  
  public String getGaugeScript()
  {
    selectedTabIndex = 0;
    StringBuilder builder = new StringBuilder();
    builder.append("<script type=\"text/javascript\" src=\"");
    builder.append(getContextPath());
    builder.append("/plugins/gauge/gauge.min.js");
    builder.append("\"></script>");
    builder.append("<script type=\"text/javascript\" src=\"");
    builder.append(getContextPath());
    builder.append("/common/presence/js/gauge.js");
    builder.append("\"></script>");
    builder.append("<script type=\"text/javascript\">paintGauges(");
    builder.append(workerStatistics.getComplianceDegree());
    builder.append(",");
    builder.append(workerStatistics.getPunctualityDegree());
    builder.append(",");
    builder.append(workerStatistics.getVeracityDegree());
    builder.append(",");
    builder.append(workerStatistics.getPresenceDegree());
    builder.append(");</script>");
    return builder.toString();
  }
    
  public List<SelectItem> getPeriodSelectItems()
  {
    if (periodSelectItems == null)
    {
      periodSelectItems = new ArrayList<SelectItem>();
      Calendar calendar = Calendar.getInstance();
      
      for  (int i = 0; i < 12; i++)
      {
        Date date = calendar.getTime();
        SelectItem selectItem = new SelectItem();
        selectItem.setValue(TextUtils.formatDate(date, "yyyyMM"));
        selectItem.setLabel(TextUtils.formatDate(date, "LLLL yyyy", getLocale()));
        periodSelectItems.add(selectItem);
        calendar.add(Calendar.MONTH, -1);
      }
    }
    return periodSelectItems;
  }
  
  public String search()
  {
    try
    {
      initDates();
      PresenceManagerPort port = getPort();
      PresenceMainBean presenceMainBean =
        (PresenceMainBean)getBean("presenceMainBean");
      String personId = presenceMainBean.getWorker().getPersonId();
      workerStatistics = port.getWorkerStatistics(personId,
        startDate + "000000", endDate + "235959");
      PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
      HashMap<String, PresenceEntryTypeStatistics> cache = 
        new HashMap<String, PresenceEntryTypeStatistics>();
      entryTypeStatistics = new ArrayList<PresenceEntryTypeStatistics>();
      for (PresenceEntry entry : workerStatistics.getPresenceEntry())
      {
        String entryTypeId = entry.getEntryTypeId();
        PresenceEntryTypeStatistics entryTypeStats = cache.get(entryTypeId);
        if (entryTypeStats == null)
        {
          entryTypeStats = new PresenceEntryTypeStatistics();
          PresenceEntryType presenceEntryType = 
            presenceConfigBean.getPresenceEntryType(entryTypeId);
          entryTypeStats.setLabel(presenceEntryType.getLabel());
          entryTypeStats.setPosition(presenceEntryType.getPosition());
          entryTypeStatistics.add(entryTypeStats);
          cache.put(entryTypeId, entryTypeStats);
        }
        if (entry.getCreationDateTime() != null)
        {
          int count = entryTypeStats.getCount() + 1;
          entryTypeStats.setCount(count);
        }
        int duration = entryTypeStats.getDuration() + entry.getDuration();
        entryTypeStats.setDuration(duration);
      }
      int workedDays = workerStatistics.getWorkedDays();
      for (PresenceEntryTypeStatistics entryTypeStats : entryTypeStatistics)
      {
        int count = entryTypeStats.getCount();
        int duration = entryTypeStats.getDuration();
        if (workedDays > 0)
        {
          entryTypeStats.setEntriesPerDay((double)count / workedDays);
        }
        if (count > 0)
        {
          entryTypeStats.setDurationPerEntry(duration / count);
        }
      }
      Collections.sort(entryTypeStatistics, new Comparator()
      {
        public int compare(Object o1, Object o2)
        {
          PresenceEntryTypeStatistics s1 = (PresenceEntryTypeStatistics)o1; 
          PresenceEntryTypeStatistics s2 = (PresenceEntryTypeStatistics)o2;
          return s1.position - s2.position;
        }
      });
      workerStatistics.getPresenceEntry().clear();
      if (isCurrentPeriod())
      {
        selectedTabIndex = 0;
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return show();
  }

  private PresenceManagerPort getPort() throws Exception
  {
    return PresenceConfigBean.getPresencePort();
  }

  private void initDates()
  {
    Calendar calendar = Calendar.getInstance();

    if (!StringUtils.isBlank(period))
    {
      startDate = period + "01";
      Date date = TextUtils.parseInternalDate(startDate);
      calendar.setTime(date);
      calendar.add(Calendar.MONTH, 1);
      calendar.set(Calendar.DAY_OF_MONTH, 1);
      calendar.add(Calendar.DAY_OF_MONTH, -1);
      date = calendar.getTime();
      endDate = TextUtils.formatDate(date, "yyyyMMdd");
    }
    else 
    {
      if (startDate == null)
      {
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        startDate = TextUtils.formatDate(calendar.getTime(), "yyyyMMdd");
      }
      if (endDate == null)
      {
        endDate = TextUtils.formatDate(new Date(), "yyyyMMdd");
      }
    }
  }
  
  public class PresenceEntryTypeStatistics implements Serializable
  {
    private String label;
    private int count;
    private int duration;
    private double entriesPerDay;
    private int durationPerEntry;
    private int position;

    public String getLabel()
    {
      return label;
    }

    public void setLabel(String label)
    {
      this.label = label;
    }

    public int getCount()
    {
      return count;
    }

    public void setCount(int count)
    {
      this.count = count;
    }

    public int getDuration()
    {
      return duration;
    }

    public void setDuration(int duration)
    {
      this.duration = duration;
    }

    public double getEntriesPerDay()
    {
      return entriesPerDay;
    }

    public void setEntriesPerDay(double entriesByDay)
    {
      this.entriesPerDay = entriesByDay;
    }

    public double getDurationPerEntry()
    {
      return durationPerEntry;
    }

    public void setDurationPerEntry(int durationByEntry)
    {
      this.durationPerEntry = durationByEntry;
    }
    
    public int getPosition()
    {
      return position;
    }

    public void setPosition(int position)
    {
      this.position = position;
    }
  }
}
