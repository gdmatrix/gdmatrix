package org.santfeliu.presence.web;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.matrix.presence.Absence;
import org.matrix.presence.AbsenceFilter;
import org.matrix.presence.AbsenceView;
import org.matrix.presence.PresenceManagerPort;
import org.matrix.presence.Worker;
import org.matrix.presence.WorkerFilter;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.presence.util.Utils;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.WebBean;

/**
 *
 * @author realor
 */
public class AbsencesBean extends WebBean implements Savable
{
  private List<AbsenceView> absences;
  private final AbsenceFilter filter = new AbsenceFilter();
  private String workerName;
  private double totalDays;
  private int totalRequestedTime;
  private int totalConsolidatedTime;
  private String view = "status";
  private String sortColumn;

  public AbsencesBean()
  {
    String nowDate = TextUtils.formatDate(new Date(), "yyyyMMdd");
    filter.setStartDateTime(Utils.addDate(nowDate, -31));    
  }
  
  public String show()
  {
    return "absences";
  }

  public AbsenceFilter getFilter()
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

  public double getTotalDays()
  {
    return totalDays;
  }

  public int getTotalRequestedTime()
  {
    return totalRequestedTime;
  }

  public int getTotalConsolidatedTime()
  {
    return totalConsolidatedTime;
  }

  public String getSortColumn()
  {
    return sortColumn;
  }

  public void setSortColumn(String sortColumn)
  {
    this.sortColumn = sortColumn;
  }

  public String getView()
  {
    return view;
  }

  public void statusView()
  {
    view = "status";
  }

  public void timesView()
  {
    view = "times";
  }

  public List<AbsenceView> getAbsences()
  {
    return absences;
  }

  public String getAbsenceStatusTitle()
  {
    AbsenceView absenceView = (AbsenceView)getValue("#{absenceView}");
    PresenceConfigBean presenceConfigBean = PresenceConfigBean.getInstance();
    return presenceConfigBean.getAbsenceStatusLabel(absenceView.getAbsence().getStatus());
  }

  public int getAbsenceCount()
  {
    return absences == null ? 0 : absences.size();
  }

  public String search()
  {
    try
    {
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
      else // show only own absences
      {
        personIdSet.add(personId);
      }

      boolean doFind = false;
      if (!StringUtils.isBlank(filter.getAbsenceId()))
      {
        filter.setAbsenceTypeId(null);
        filter.setStatus(null);
        filter.setStartDateTime(null);
        filter.setEndDateTime(null);
        if (!presenceAdministrator)
        {
          filter.getPersonId().addAll(personIdSet);
        }
        doFind = true;
      }
      else if (StringUtils.isBlank(workerName))
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
        absences = getPort().findAbsenceViews(filter);
      }
      else
      {
        absences = Collections.EMPTY_LIST;
      }

      totalDays = 0;
      totalRequestedTime = 0;
      totalConsolidatedTime = 0;
      for (AbsenceView absenceView : absences)
      {
        Absence absence = absenceView.getAbsence();
        totalDays += absence.getRequestedDays();
        totalRequestedTime += absence.getRequestedTime();
        totalConsolidatedTime += absence.getConsolidatedTime();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }    
    return "absences";
  }

  public String showAbsence()
  {
    AbsenceView absenceView = (AbsenceView)getValue("#{absenceView}");
    PresenceMainBean presenceMainBean =
      (PresenceMainBean)getBean("presenceMainBean");
    Worker absenceWorker = absenceView.getWorker();
    String outcome =  presenceMainBean.showWorkerWeek(absenceWorker);
    WorkerWeekBean workerWeekBean = (WorkerWeekBean)getBean("workerWeekBean");
    workerWeekBean.show(absenceView.getAbsence());
    return outcome;
  }

  public boolean isProcessAbsenceEnabled()
  {
    PresenceMainBean presenceMainBean = 
      (PresenceMainBean)getBean("presenceMainBean");

    String personId = presenceMainBean.getWorker().getPersonId();
    Absence absence = (Absence)getValue("#{absenceView.absence}");
    if (absence != null)
    {
      if (absence.getPersonId().equals(personId)) return true;
    
      if (absence.getInstanceId() != null) return true;
    }
    return false;
  }
  
  public void processAbsence()
  {
    AbsenceView absenceView = (AbsenceView)getValue("#{absenceView}");
    PresenceConfigBean.getInstance().processAbsence(absenceView.getAbsence());
  }

  private PresenceManagerPort getPort() throws Exception
  {
    return PresenceConfigBean.getPresencePort();
  }
}
