package org.santfeliu.presence.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import javax.faces.model.SelectItem;
import org.apache.commons.lang.StringUtils;
import org.matrix.presence.PresenceEntry;
import org.matrix.presence.PresenceEntryFilter;
import org.matrix.presence.PresenceEntryType;
import org.matrix.presence.PresenceManagerPort;
import org.matrix.presence.Worker;
import org.matrix.presence.WorkerFilter;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.WebBean;

/**
 *
 * @author realor
 */
public class EntriesBean extends WebBean implements Savable
{
  private List<PresenceEntryView> entries;
  private final PresenceEntryFilter filter = new PresenceEntryFilter();
  private String workerName;
  private String sortColumn;
  private String startDate;
  private String endDate;
  private boolean manipulated;
  private long totalTime;
  private long totalWorkedTime;

  public EntriesBean()
  {
    startDate = TextUtils.formatDate(new Date(), "yyyyMMdd");
  }
  
  public String show()
  {
    return "entries";
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
  
  public PresenceEntryFilter getFilter()
  {
    return filter;
  }
  
  public String getWorkerName()
  {
    return workerName;
  }

  public void setWorkerName(String workerName)
  {
    this.workerName = workerName;
  }

  public boolean isManipulated() 
  {
    return manipulated;
  }

  public void setManipulated(boolean manipulated) 
  {
    this.manipulated = manipulated;
  }

  public String getSortColumn()
  {
    return sortColumn;
  }

  public void setSortColumn(String sortColumn)
  {
    this.sortColumn = sortColumn;
  }

  public List<PresenceEntryView> getEntries()
  {
    return entries;
  }

  public int getPresenceEntryCount()
  {
    return entries == null ? 0 : entries.size();
  }
  
  public List<SelectItem> getPresenceEntryTypeSelectItems()
  {
    return PresenceConfigBean.getInstance().getPresenceEntryTypeSelectItems(false);
  }

  public long getTotalTime()
  {
    return totalTime;
  }

  public long getTotalWorkedTime()
  {
    return totalWorkedTime;
  }
  
  public String search()
  {
    try
    {
      totalTime = 0;
      totalWorkedTime = 0;
      PresenceMainBean presenceMainBean =
        (PresenceMainBean)getBean("presenceMainBean");
      String personId = presenceMainBean.getWorker().getPersonId();
      boolean presenceAdministrator =
        presenceMainBean.isPresenceAdministrator();

      filter.getPersonId().clear();
      HashSet<String> personIdSet = new HashSet<String>();
      if (presenceMainBean.isInAdvancedMode()) // filter by all descendants
      {
        PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
        presenceConfigBean.loadValidatedWorkers(personId, true, personIdSet);
        personIdSet.add(personId);        
      }
      else // show only own entries
      {
        personIdSet.add(personId);
      }

      boolean doFind = false;
      if (StringUtils.isBlank(workerName))
      {
        if (!presenceAdministrator)
        {
          filter.getPersonId().addAll(personIdSet);
        }
        doFind = true;
      }
      else
      {
        PresenceManagerPort port = getPort();
        WorkerFilter workerFilter = new WorkerFilter();
        workerFilter.setFullName(workerName);
        List<Worker> workers = port.findWorkers(workerFilter);
        for (Worker worker : workers)
        {
          if (presenceAdministrator ||
            personIdSet.contains(worker.getPersonId()))
          {
            filter.getPersonId().add(worker.getPersonId());
            doFind = true;
          }
        }
      }

      if (doFind)
      {
        filter.setMaxResults(1000);
        if (!StringUtils.isBlank(startDate))
        {
          filter.setStartDateTime(startDate + "000000");
        }
        else 
        {
          filter.setStartDateTime(null);
        }
        if (!StringUtils.isBlank(endDate))
        {
          filter.setEndDateTime(endDate + "235959");
        }
        else
        {
          filter.setEndDateTime(null);
        }
        if (StringUtils.isBlank(filter.getReason()))
        {
          filter.setReason(null);
        }
        if (manipulated)
        {
          filter.setManipulated(true);
        }
        else
        {
          filter.setManipulated(null);
        }
        List<PresenceEntry> presenceEntries = 
          getPort().findPresenceEntries(filter);
        entries = new ArrayList<PresenceEntryView>();
        PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
        for (PresenceEntry presenceEntry : presenceEntries)
        {
          PresenceEntryView entry = new PresenceEntryView(presenceEntry);
          entries.add(entry);

          // update counters
          PresenceEntryType entryType = 
            presenceConfigBean.getPresenceEntryType(presenceEntry.getEntryTypeId());
          totalTime += entry.getDuration(); 
          int workedTime = Math.min(entry.getDuration(), entryType.getMaxWorkedTime());
          totalWorkedTime += workedTime;
        }
        presenceEntries.clear();
      }
      else
      {
        entries = Collections.EMPTY_LIST;
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return show();
  }
  
  public String showPresenceEntry()
  {
    try
    {
      PresenceEntryView entry = (PresenceEntryView)getValue("#{entry}");
      PresenceMainBean presenceMainBean =
        (PresenceMainBean)getBean("presenceMainBean");
      String personId = entry.getPersonId();
      Worker worker = PresenceConfigBean.getInstance().getWorker(personId);
      String outcome =  presenceMainBean.showWorkerWeek(worker);
      PresenceEntry presenceEntry = 
        getPort().loadPresenceEntry(entry.getEntryId());
      WorkerWeekBean workerWeekBean = (WorkerWeekBean)getBean("workerWeekBean");
      workerWeekBean.show(presenceEntry);
      return outcome;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  private PresenceManagerPort getPort() throws Exception
  {
    return PresenceConfigBean.getPresencePort();
  }
  
  public class PresenceEntryView implements Serializable
  {
    private String entryId;
    private String personId;
    private String workerName;
    private String entryTypeLabel;
    private String startDateTime;
    private String endDateTime;
    private int duration;
    private String reason;
    private boolean manipulated;
    private String color;

    public PresenceEntryView()
    {
    }

    public PresenceEntryView(PresenceEntry entry)
    {
      PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
      String entryTypeId = entry.getEntryTypeId();
      PresenceEntryType entryType = 
        presenceConfigBean.getPresenceEntryType(entryTypeId);
      entryTypeLabel = 
        presenceConfigBean.getPresenceEntryTypeLabel(entryType, false);
      color = entryType.getColor();
      try
      {
        Worker worker = presenceConfigBean.getWorker(entry.getPersonId());
        workerName = worker.getFullName();
      }
      catch (Exception ex)
      {
        workerName = entry.getPersonId();
      }
      entryId = entry.getEntryId();
      personId = entry.getPersonId();
      startDateTime = entry.getStartDateTime();
      endDateTime = entry.getEndDateTime();
      if (endDateTime == null)
      {
        Date now = new Date();
        Date date = TextUtils.parseInternalDate(startDateTime);
        duration = (int)((now.getTime() - date.getTime()) / 1000);
      }
      else
      {
        duration = entry.getDuration();
      }
      reason = entry.getReason();
      manipulated = entry.isManipulated();
    }

    public String getEntryId()
    {
      return entryId;
    }

    public void setEntryId(String entryId)
    {
      this.entryId = entryId;
    }

    public String getPersonId()
    {
      return personId;
    }

    public void setPersonId(String personId)
    {
      this.personId = personId;
    }

    public String getColor()
    {
      return color;
    }

    public void setColor(String color)
    {
      this.color = color;
    }
    
    public String getWorkerName() 
    {
      return workerName;
    }

    public void setWorkerName(String workerName) 
    {
      this.workerName = workerName;
    }

    public String getEntryTypeLabel() 
    {
      return entryTypeLabel;
    }

    public void setEntryTypeLabel(String entryTypeLabel) 
    {
      this.entryTypeLabel = entryTypeLabel;
    }

    public String getStartDateTime()
    {
      return startDateTime;
    }

    public void setStartDateTime(String startDateTime)
    {
      this.startDateTime = startDateTime;
    }

    public String getEndDateTime()
    {
      return endDateTime;
    }

    public void setEndDateTime(String endDateTime)
    {
      this.endDateTime = endDateTime;
    }
    
    public int getDuration() 
    {
      return duration;
    }

    public void setDuration(int duration) 
    {
      this.duration = duration;
    }

    public String getReason() 
    {
      return reason;
    }

    public void setReason(String reason) 
    {
      this.reason = reason;
    }

    public boolean isManipulated() 
    {
      return manipulated;
    }

    public void setManipulated(boolean manipulated) 
    {
      this.manipulated = manipulated;
    }    
  }
}
