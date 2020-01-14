package org.santfeliu.presence.web;

import java.util.Map;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import org.matrix.presence.PresenceConstants;
import org.matrix.presence.PresenceManagerPort;
import org.matrix.presence.Worker;
import org.matrix.security.SecurityConstants;
import org.matrix.security.SecurityManagerPort;
import org.matrix.security.User;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.security.util.LDAPConnector;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSProperty;
import org.santfeliu.web.obj.PageBean;

/**
 *
 * @author realor
 */
public class PresenceMainBean extends PageBean
{
  public static final String IDCODE_PARAM = "idcode";

  @CMSProperty(mandatory=true)
  public static final String IDCODE_PROPERTY = "idCodeProperty";
  @CMSProperty
  public static final String LDAP_USERID_PROPERTY = "ldapUserIdProperty";
  @CMSProperty
  public static final String VALID_IP_ADDRESS_PROPERTY = "validIpAddress";
  @CMSProperty
  public static final String PROCESS_NODEID_PROPERTY = "processNodeId";
  @CMSProperty
  public static final String PROCESS_WORKFLOW_PROPERTY = "processWorkflowName";
  
  private static final String NO_PERSONID = "0";
  
  private String idCode;
  private String view;
  private String loggedPersonId;

  public String getIdCode()
  {
    return idCode;
  }

  public void setIdCode(String idCode)
  {
    this.idCode = idCode;
  }

  public String getSessionTrack()
  {
    return "sessionTrack";
  }

  public void setSessionTrack(String track)
  {
    checkSessionLost();
  }
  
  public boolean isPresenceAdministrator()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return userSessionBean.isUserInRole(PresenceConstants.PRESENCE_ADMIN_ROLE);
  }

  public boolean isInAdvancedMode()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return !userSessionBean.isAnonymousUser() && 
      !userSessionBean.isAutoLoginUser();
  }
  
  public boolean isShowingLoggedWorker()
  {
    return !isInAdvancedMode() || 
      getWorker().getPersonId().equals(getLoggedPersonId());
  }

  public boolean isEditionEnabled()
  {
    return isShowingLoggedWorker() || isPresenceAdministrator();
  }
  
  public String getLoggedPersonId()
  {
    if (loggedPersonId == null)
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      String userId = userSessionBean.getUserId();
      try
      {
        User user = PresenceConfigBean.getSecurityPort().loadUser(userId);
        loggedPersonId = user.getPersonId();
      }
      catch (Exception ex)
      {
        loggedPersonId = NO_PERSONID;
      }
    }
    return loggedPersonId;
  }
  
  public String getView()
  {
    return view;
  }

  public void setView(String view)
  {
    this.view = view;
  }
  
  @CMSAction
  public String show()
  {
    idCode = 
     (String)getExternalContext().getRequestParameterMap().get(IDCODE_PARAM);
    return identify();
  }
  
  public String showLoggedWorker()
  {
    Worker loggedWorker = 
      PresenceConfigBean.getInstance().getWorker(loggedPersonId);
    return showWorkerWeek(loggedWorker);
  }
  
  public String showWorkerWeekView()
  {
    return showView("worker_week");
  }
  
  public String showWorkerCalendarView()
  {
    return showView("worker_calendar");
  }

  public String showGroupalView()
  {
    return showView("groupal_view");
  }    
  
  public String showAbsencesView()
  {
    return showView("absences");
  }

  public String showEntriesView()
  {
    return showView("entries");
  }
  
  public String showStatsView()
  {
    return showView("presence_stats");
  }
  
  public String showCountersView()
  {
    return showView("absence_counters");
  }
  
  public String showAdminView()
  {
    return showView("presence_admin");
  }  
  
  public String showView(String newView)
  {
    return showView(newView, null);
  }

  public String showView(String newView, String date)
  {
    if (checkSessionLost()) return null;
    
    this.view = newView;
    if ("worker_calendar".equals(view))
    {
      WorkerCalendarBean workerCalendarBean = 
        (WorkerCalendarBean)getBean("workerCalendarBean");
      return workerCalendarBean.update();
    }
    else if ("groupal_view".equals(view))
    {
      GroupalViewBean groupalViewBean = 
        (GroupalViewBean)getBean("groupalViewBean");
      if (date == null) return groupalViewBean.show();
      else return groupalViewBean.show(date);
    }
    else if ("absences".equals(view))
    {
      AbsencesBean absencesBean = (AbsencesBean)getBean("absencesBean");
      return absencesBean.search();
    }
    else if ("entries".equals(view))
    {
      EntriesBean entriesBean = (EntriesBean)getBean("entriesBean");
      return entriesBean.search();
    }
    else if ("presence_stats".equals(view))
    {
      PresenceStatsBean presenceStatsBean = 
        (PresenceStatsBean)getBean("presenceStatsBean");
      return presenceStatsBean.search();
    }
    else if ("absence_counters".equals(view))
    {
      AbsenceCountersBean absenceCountersBean = 
        (AbsenceCountersBean)getBean("absenceCountersBean");
      return absenceCountersBean.update();
    }
    else if ("presence_admin".equals(view))
    {
      PresenceAdminBean presenceAdminBean = 
        (PresenceAdminBean)getBean("presenceAdminBean");
      return presenceAdminBean.show();
    }
    else // worker_week
    {
      view = "worker_week";
      WorkerWeekBean workerWeekBean = (WorkerWeekBean)getBean("workerWeekBean");
      if (date == null)
      {
        return workerWeekBean.show();
      }
      else
      {
        return workerWeekBean.goDate(date);
      }
    }
  }
  
  public String identify()
  {
    try
    {
      if (idCode == null) // identify user from UserSessionBean
      {
        UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
        if (userSessionBean.isAnonymousUser())
        {
          // first access or logout
          return "presence_blank";
        }
        else
        {
          String userId = userSessionBean.getUserId();
          loadWorker(userId);
          return enterProgram();
        }
      }
      else
      {
        String userId = findIdCodeInLDAP(idCode);
        loadWorker(userId);
        return enterProgram();
      }
    }
    catch (Exception ex)
    {
      // login failure, show error and retry
      error(ex);
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      try
      {
        userSessionBean.login(SecurityConstants.ANONYMOUS, null);
      }
      catch (Exception ex2)
      {
      }
      return "worker_id";
    }
  }
  
  public Worker getWorker()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    Worker worker = (Worker)userSessionBean.getAttribute("worker");
    if (worker == null)
    {
      worker = new Worker();
      worker.setFullName("");
      worker.setPersonId(NO_PERSONID);
      userSessionBean.setAttribute("worker", worker);
    }
    return worker;
  }
  
  public void setWorker(Worker worker)
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    userSessionBean.setAttribute("worker", worker);
  }
  
  public String showWorkerWeek(Worker otherWorker)
  {
    // change worker in presenceMainBean
    setWorker(otherWorker);
    PresenceConfigBean.clearBean("workerWeekBean");
    PresenceConfigBean.clearBean("workerCalendarBean");
    PresenceConfigBean.clearBean("presenceStatsBean");
    PresenceConfigBean.clearBean("groupalViewBean");
    PresenceConfigBean.clearBean("absencesBean");
    
    view = "worker_week";
    WorkerWeekBean workerWeekBean = (WorkerWeekBean)getBean("workerWeekBean");
    return workerWeekBean.show();
  }
  
  public String enterProgram()
  {
    Map<String, String> parameters = 
      getExternalContext().getRequestParameterMap();
    String newView = parameters.get("view");
    String date = (String)parameters.get("date");
    return showView(newView, date);
  }

  private boolean checkSessionLost()
  {
    if (getWorker().getPersonId().equals(NO_PERSONID))
    {
      FacesContext context = getFacesContext();
      Application application = context.getApplication();
      application.getNavigationHandler().handleNavigation(
        context, null, "presence_blank");
      context.renderResponse();
      return true;
    }
    return false;
  }
  
  private String findIdCodeInLDAP(String idCode) throws Exception
  {
    String userId = null;

    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor cursor = userSessionBean.getSelectedMenuItem();

    LDAPConnector conn = new LDAPConnector();
    String ldapUrl = MatrixConfig.getProperty(
      "org.santfeliu.security.service.SecurityManager.ldap.url");
    String ldapDomain = MatrixConfig.getProperty(
      "org.santfeliu.security.service.SecurityManager.ldap.domain");
    String ldapBase = MatrixConfig.getProperty(
      "org.santfeliu.security.service.SecurityManager.ldap.base");
    String ldapUserId = MatrixConfig.getProperty(
      "org.santfeliu.security.service.SecurityManager.ldap.adminUserId");
    String ldapPassword = MatrixConfig.getProperty(
      "org.santfeliu.security.service.SecurityManager.ldap.adminPassword");

    conn.setLdapUrl(ldapUrl);
    conn.setDomain(ldapDomain);
    conn.setSearchBase(ldapBase);
    conn.setUserId(ldapUserId);
    conn.setPassword(ldapPassword);

    String dn = conn.find(cursor.getProperty(IDCODE_PROPERTY), idCode);
    if (dn != null)
    {
      String userIdProperty = cursor.getProperty(LDAP_USERID_PROPERTY);
      if (userIdProperty == null) userIdProperty = "sAMAccountName";
      userId = (String)conn.getAttribute(dn, userIdProperty);
    }
    if (userId == null) throw new Exception("INVALID_IDCODE");
    return userId;
  }
  
  private void loadWorker(String userId) throws Exception
  {
    SecurityManagerPort securityPort = PresenceConfigBean.getSecurityPort();
    User user = securityPort.loadUser(userId);
    String personId = user.getPersonId();
    PresenceManagerPort port = PresenceConfigBean.getPresencePort();
    Worker worker = port.loadWorker(personId);
    setWorker(worker);
  }  
}
