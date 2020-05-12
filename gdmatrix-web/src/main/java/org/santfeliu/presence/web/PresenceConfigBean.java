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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.faces.application.Application;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import org.matrix.doc.DocumentManagerPort;
import org.matrix.doc.DocumentManagerService;
import org.matrix.kernel.KernelManagerPort;
import org.matrix.kernel.KernelManagerService;
import org.matrix.presence.Absence;
import org.matrix.presence.AbsenceCounting;
import org.matrix.presence.AbsenceType;
import org.matrix.presence.AbsenceTypeFilter;
import org.matrix.presence.DayType;
import org.matrix.presence.DayTypeFilter;
import org.matrix.presence.PresenceConstants;
import static org.matrix.presence.PresenceConstants.*;
import org.matrix.presence.PresenceEntryType;
import org.matrix.presence.PresenceEntryTypeFilter;
import org.matrix.presence.PresenceManagerPort;
import org.matrix.presence.PresenceManagerService;
import org.matrix.presence.PresenceParameter;
import org.matrix.presence.WeekType;
import org.matrix.presence.WeekTypeFilter;
import org.matrix.presence.WorkReduction;
import org.matrix.presence.Worker;
import org.matrix.presence.WorkerFilter;
import org.matrix.security.SecurityManagerPort;
import org.matrix.security.SecurityManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.matrix.workflow.WorkflowManagerPort;
import org.matrix.workflow.WorkflowManagerService;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;

/**
 *
 * @author realor
 */
public class PresenceConfigBean extends WebBean
{
  private final String processWorkflowName;
  private final String processNodeId;
  
  private final static Map<String, DayType> dayTypeMap =
    new HashMap<String, DayType>();
  private final static Map<String, Worker> workerMap =
    new HashMap<String, Worker>();
  private final static Map<String, PresenceEntryType> presenceEntryTypeMap =
    new HashMap<String, PresenceEntryType>();
  private final static Map<String, WorkReduction> workReductionMap =
    new HashMap<String, WorkReduction>();
  private final static Map<String, AbsenceType> absenceTypeMap =
    new HashMap<String, AbsenceType>();
  private final static Map<String, String> parameterMap = 
    new HashMap<String, String>();
  private static long lastRefresh;
    
  private static final String[] absenceStatus = new String[]
  {
    PENDENT_STATUS, 
    IN_PROCESS_STATUS, 
    APPROVED_STATUS, 
    DENIED_STATUS, 
    CONSOLIDATED_STATUS, 
    CANCELLED_STATUS
  };

  // data refreshed once per request
  private String[] daysOfWeek;
  private List<SelectItem> weekTypeSelectItems;
  private List<SelectItem> dayTypeSelectItems;
  private List<SelectItem> presenceEntryTypeSelectItems;
  private List<SelectItem> enabledPresenceEntryTypeSelectItems;
  private List<SelectItem> absenceStatusSelectItems;

  public PresenceConfigBean()
  {
    processNodeId = getProperty(PresenceMainBean.PROCESS_NODEID_PROPERTY);    
    processWorkflowName = getProperty(PresenceMainBean.PROCESS_WORKFLOW_PROPERTY);
  }
  
  public static PresenceConfigBean getInstance()
  {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    Application application = facesContext.getApplication();
    return (PresenceConfigBean)application.getVariableResolver().
      resolveVariable(facesContext, "presenceConfigBean");
  }

  public Map<String, Worker> getWorkerMap()
  {
    return workerMap;
  }

  public Map<String, DayType> getDayTypeMap()
  {
    return dayTypeMap;
  }

  public Map<String, PresenceEntryType> getPresenceEntryTypeMap()
  {
    return presenceEntryTypeMap;
  }

  public Map<String, AbsenceType> getAbsenceTypeMap()
  {
    return absenceTypeMap;
  }
  
  public String getPresenceEntryTypeLabel(PresenceEntryType presenceEntryType,
    boolean withCode)
  {
    StringBuilder buffer = new StringBuilder();
    if (withCode)
    {
      if (presenceEntryType.getCode() == null)
      {
        buffer.append(presenceEntryType.getEntryTypeId());
      }
      else
      {
        buffer.append(presenceEntryType.getCode());
      }
      buffer.append(" ");
    }
    buffer.append(presenceEntryType.getLabel());
    return buffer.toString();
  }

  public String getDayTypeLabel(DayType dayType, boolean withCode)
  {
    StringBuilder buffer = new StringBuilder();
    if (withCode)
    {
      if (dayType.getCode() == null)
      {
        buffer.append(dayType.getDayTypeId());
      }
      else
      {
        buffer.append(dayType.getCode());
      }
      buffer.append(" ");
    }
    buffer.append(dayType.getLabel());
    buffer.append(" (");
    buffer.append(Integer.parseInt(dayType.getInTime1().substring(0, 2)));
    buffer.append(":");
    buffer.append(dayType.getInTime1().substring(2, 4));
    buffer.append("h - ");
    buffer.append(Integer.parseInt(dayType.getOutTime1().substring(0, 2)));
    buffer.append(":");
    buffer.append(dayType.getOutTime1().substring(2, 4));
    buffer.append("h");
    if (dayType.getInTime2() != null)
    {
      buffer.append(", ");
      buffer.append(Integer.parseInt(dayType.getInTime2().substring(0, 2)));
      buffer.append(":");
      buffer.append(dayType.getInTime2().substring(2, 4));
      buffer.append("h - ");
      buffer.append(Integer.parseInt(dayType.getOutTime2().substring(0, 2)));
      buffer.append(":");
      buffer.append(dayType.getOutTime2().substring(2, 4));
      buffer.append("h");
    }
    buffer.append(")");
    return buffer.toString();
  }

  public String getAbsenceLabel(Absence absence, boolean full)
  {
    StringBuilder buffer = new StringBuilder();
    if (full)
    {
      String absenceTypeId = absence.getAbsenceTypeId();
      AbsenceType absenceType = getAbsenceType(absenceTypeId);
      buffer.append(absenceType.getLabel());
      buffer.append(" ");
    }
    String startDateTime = absence.getStartDateTime();
    String endDateTime = absence.getEndDateTime();
    if (startDateTime.substring(0, 8).equals(endDateTime.substring(0, 8)))
    {
      buffer.append(TextUtils.formatInternalDate(
        absence.getStartDateTime(), "H:mm"));
      buffer.append("h - ");
      buffer.append(TextUtils.formatInternalDate(
        absence.getEndDateTime(), "H:mm"));
      buffer.append("h");
    }
    else
    {
      buffer.append(TextUtils.formatInternalDate(
        absence.getStartDateTime(), "d/MM/yyyy H:mm"));
      buffer.append("h - ");
      buffer.append(TextUtils.formatInternalDate(
        absence.getEndDateTime(), "d/MM/yyyy H:mm"));
      buffer.append("h");
    }
    return buffer.toString();
  }

  public Worker getWorker(String personId)
  {
    updateCaches();

    Worker worker = workerMap.get(personId);
    if (worker == null)
    {
      try
      {
        PresenceManagerPort port = getPresencePort();
        worker = port.loadWorker(personId);
        workerMap.put(personId, worker);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return worker;
  }

  public DayType getDayType(String dayTypeId)
  {
    updateCaches();

    DayType dayType = dayTypeMap.get(dayTypeId);
    if (dayType == null)
    {
      try
      {
        PresenceManagerPort port = getPresencePort();
        dayType = port.loadDayType(dayTypeId);
        dayTypeMap.put(dayTypeId, dayType);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return dayType;
  }

  public PresenceEntryType getPresenceEntryType(String entryTypeId)
  {
    updateCaches();

    PresenceEntryType presenceEntryType = presenceEntryTypeMap.get(entryTypeId);
    if (presenceEntryType == null)
    {
      try
      {
        PresenceManagerPort port = getPresencePort();
        presenceEntryType = port.loadPresenceEntryType(entryTypeId);
        presenceEntryTypeMap.put(entryTypeId, presenceEntryType);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return presenceEntryType;
  }

  public AbsenceType getAbsenceType(String absenceTypeId)
  {
    updateCaches();

    AbsenceType absenceType = absenceTypeMap.get(absenceTypeId);
    if (absenceType == null)
    {
      try
      {
        PresenceManagerPort port = getPresencePort();
        absenceType = port.loadAbsenceType(absenceTypeId);
        absenceTypeMap.put(absenceTypeId, absenceType);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return absenceType;
  }
  
  public List<SelectItem> getWeekTypeSelectItems()
  {
    if (weekTypeSelectItems == null)
    {
      try
      {
        weekTypeSelectItems = new ArrayList<SelectItem>();
        PresenceManagerPort port = getPresencePort();
        WeekTypeFilter filter = new WeekTypeFilter();
        List<WeekType> weekTypes = port.findWeekTypes(filter);
        for (WeekType weekType : weekTypes)
        {
          SelectItem selectItem = new SelectItem();
          selectItem.setValue(weekType.getWeekTypeId());
          selectItem.setLabel(weekType.getLabel());
          weekTypeSelectItems.add(selectItem);
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return weekTypeSelectItems;
  }

  public List<SelectItem> getDayTypeSelectItems()
  {
    if (dayTypeSelectItems == null)
    {
      try
      {
        dayTypeSelectItems = new ArrayList<SelectItem>();
        PresenceManagerPort port = getPresencePort();
        DayTypeFilter filter = new DayTypeFilter();
        List<DayType> dayTypes = port.findDayTypes(filter);
        for (DayType dayType : dayTypes)
        {
          SelectItem selectItem = new SelectItem();
          selectItem.setValue(dayType.getDayTypeId());
          String label = getDayTypeLabel(dayType, true);
          selectItem.setLabel(label);
          dayTypeSelectItems.add(selectItem);
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return dayTypeSelectItems;
  }

  public List<SelectItem> getPresenceEntryTypeSelectItems()
  {
    return getPresenceEntryTypeSelectItems(true);
  }

  public List<SelectItem> getPresenceEntryTypeSelectItems(boolean withCode)
  {
    if (presenceEntryTypeSelectItems == null)
    {
      try
      {
        presenceEntryTypeSelectItems = new ArrayList<SelectItem>();
        PresenceManagerPort port = getPresencePort();
        PresenceEntryTypeFilter filter = new PresenceEntryTypeFilter();
        filter.setTeam("%");
        List<PresenceEntryType> presenceEntryTypes = port.findPresenceEntryTypes(filter);
        for (PresenceEntryType presenceEntryType : presenceEntryTypes)
        {
          SelectItem selectItem = new SelectItem();
          selectItem.setValue(presenceEntryType.getEntryTypeId());
          selectItem.setLabel(getPresenceEntryTypeLabel(presenceEntryType, withCode));
          presenceEntryTypeSelectItems.add(selectItem);
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return presenceEntryTypeSelectItems;
  }

  public List<SelectItem> getEnabledPresenceEntryTypeSelectItems()
  {
    if (enabledPresenceEntryTypeSelectItems == null)
    {
      try
      {
        enabledPresenceEntryTypeSelectItems = new ArrayList<SelectItem>();
        PresenceManagerPort port = getPresencePort();
        PresenceEntryTypeFilter filter = new PresenceEntryTypeFilter();
        filter.setEnabled(Boolean.TRUE);
        filter.setTeam("%");
        List<PresenceEntryType> presenceEntryTypes = port.findPresenceEntryTypes(filter);
        for (PresenceEntryType presenceEntryType : presenceEntryTypes)
        {
          SelectItem selectItem = new SelectItem();
          selectItem.setValue(presenceEntryType.getEntryTypeId());
          selectItem.setLabel(presenceEntryType.getLabel());
          enabledPresenceEntryTypeSelectItems.add(selectItem);
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return enabledPresenceEntryTypeSelectItems;
  }
    
  public List<SelectItem> getAbsenceTypeSelectItems()
  {
    List<SelectItem> selectItems = new ArrayList<SelectItem>();
    try
    {
      List<AbsenceType> absenceTypes;
      PresenceManagerPort port = getPresencePort();
      AbsenceTypeFilter filter = new AbsenceTypeFilter();
      absenceTypes = port.findAbsenceTypes(filter);
      for (AbsenceType absenceType : absenceTypes)
      {
        SelectItem selectItem = new SelectItem();
        selectItem.setLabel(absenceType.getLabel());
        selectItem.setValue(absenceType.getAbsenceTypeId());
        selectItems.add(selectItem);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return selectItems;
  }

  public String[] getDaysOfWeek()
  {
    if (daysOfWeek == null)
    {
      daysOfWeek = new String[7];
      Calendar calendar = Calendar.getInstance(getLocale());
      calendar.setFirstDayOfWeek(Calendar.MONDAY);
      calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
      for (int i = 0; i < 7; i++)
      {
        String displayName = calendar.getDisplayName(
          Calendar.DAY_OF_WEEK, Calendar.SHORT, getLocale());
        displayName = displayName.substring(0, 2).toUpperCase();
        daysOfWeek[i] = displayName;
        calendar.add(Calendar.DAY_OF_WEEK, 1);
      }
    }
    return daysOfWeek;
  }
  
  public List<SelectItem> getAbsenceStatusSelectItems()
  {
    if (absenceStatusSelectItems == null)
    {
      ResourceBundle bundle = ResourceBundle.getBundle(
        "org.santfeliu.presence.web.resources.PresenceBundle", getLocale());
      
      absenceStatusSelectItems = new ArrayList<SelectItem>();
      for (String status : absenceStatus)
      {
        SelectItem selectItem = new SelectItem();
        selectItem.setValue(status);
        selectItem.setLabel(bundle.getString("status_" + status));
        absenceStatusSelectItems.add(selectItem); 
      }
    }
    return absenceStatusSelectItems;
  }

  public String getAbsenceStatusLabel(String status)
  {
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.presence.web.resources.PresenceBundle", getLocale());   
    return bundle.getString("status_" + status);
  }

  public String getAbsenceCountingLabel(AbsenceCounting counting)
  {
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.presence.web.resources.PresenceBundle", getLocale());
    return bundle.getString("org.matrix.presence.AbsenceCounting." + counting.value());
  }
  
  public WorkReduction getWorkReduction(String reductionId)
  {
    WorkReduction workReduction = workReductionMap.get(reductionId);
    if (workReduction == null)
    {
      try
      {
        workReduction = getPresencePort().loadWorkReduction(reductionId);
        workReductionMap.put(reductionId, workReduction);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return workReduction;
  }
  
  public String getParameterValue(String parameterId)
  {
    String value = parameterMap.get(parameterId);
    if (value == null)
    {
      try
      {
        PresenceParameter parameter = 
          getPresencePort().loadParameter(parameterId);
        value = parameter.getValue();
        parameterMap.put(parameterId, value);
      }
      catch (Exception ex)
      {        
        // ignore
      }
    }
    return value;
  }
  
  public boolean isValidIpAddress(String ipAddress)
  {
    if (ipAddress == null) return true;

    String pattern = 
      getParameterValue(PresenceConstants.VALID_IP_ADDRESS_PARAM);
    if (pattern == null) return true;
    return ipAddress.matches(pattern);
  }
  
  public void processAbsence(Absence absence)
  {
    try
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      boolean anonymous = userSessionBean.isAnonymousUser();
      String action = anonymous ? "/login.faces" : "/go.faces";
      String url;
      PresenceManagerPort port = getPresencePort();
      // reload absence
      Absence editingAbsence = port.loadAbsence(absence.getAbsenceId());
      String instanceId = editingAbsence.getInstanceId();
      if (instanceId == null)
      {
        // create new workflow instance
        url = action + "?xmid=" + processNodeId + "&workflow=" + 
          processWorkflowName + "&absenceId=" + editingAbsence.getAbsenceId();
      }
      else
      {
        // show existing workflow instance
        url = action + "?xmid=" + processNodeId + "&instanceid=" + instanceId;
      }
      System.out.println(">>>> url=" + url);
      getExternalContext().redirect(url);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public void loadValidatedWorkers(String personId, boolean recursive, 
    Collection<String> personIdList) throws Exception
  {
    WorkerFilter filter = new WorkerFilter();
    filter.setValidatorPersonId(personId);
    PresenceManagerPort port = getPresencePort();
    List<Worker> workers = port.findWorkers(filter);
    for (Worker worker : workers)
    {
      String subPersonId = worker.getPersonId();
      if (!personIdList.contains(subPersonId))
      {
        personIdList.add(subPersonId);
        workerMap.put(subPersonId, worker);
      }
      if (recursive)
      {
        loadValidatedWorkers(subPersonId, recursive, personIdList);
      }
    }
  }
  
  private synchronized void updateCaches()
  {
    long now = System.currentTimeMillis();
    if (now - lastRefresh > 60000) // one minute
    {
      dayTypeMap.clear();
      workerMap.clear();
      presenceEntryTypeMap.clear();
      absenceTypeMap.clear();
      workReductionMap.clear();
      parameterMap.clear();
      lastRefresh = now;
    }
  }
  
  public static PresenceManagerPort getPresencePort() throws Exception
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint =
      wsDirectory.getEndpoint(PresenceManagerService.class);
    return endpoint.getPort(PresenceManagerPort.class,
      UserSessionBean.getCurrentInstance().getUserId(),
      UserSessionBean.getCurrentInstance().getPassword());
  }

  public static WorkflowManagerPort getWorkflowPort() throws Exception
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint =
      wsDirectory.getEndpoint(WorkflowManagerService.class);
    return endpoint.getPort(WorkflowManagerPort.class,
      UserSessionBean.getCurrentInstance().getUserId(),
      UserSessionBean.getCurrentInstance().getPassword());
  }

  public static KernelManagerPort getKernelPort() throws Exception
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint =
      wsDirectory.getEndpoint(KernelManagerService.class);
    return endpoint.getPort(KernelManagerPort.class,
      UserSessionBean.getCurrentInstance().getUserId(),
      UserSessionBean.getCurrentInstance().getPassword());
  }

  public static DocumentManagerPort getDocumentPort() throws Exception
  {
    String userId = MatrixConfig.getProperty("adminCredentials.userId");
    String password = MatrixConfig.getProperty("adminCredentials.password");

    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint =
      wsDirectory.getEndpoint(DocumentManagerService.class);
    return endpoint.getPort(DocumentManagerPort.class, userId, password);
  }
  
  public static SecurityManagerPort getSecurityPort()
  {
    String userId = MatrixConfig.getProperty("adminCredentials.userId");
    String password = MatrixConfig.getProperty("adminCredentials.password");

    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint = wsDirectory.getEndpoint(SecurityManagerService.class);
    return endpoint.getPort(SecurityManagerPort.class, userId, password);
  }

  public static void clearBean(String beanName)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    ExternalContext extContext = context.getExternalContext();
    Map requestMap = extContext.getRequestMap();
    requestMap.remove(beanName);
  }
}
