package org.santfeliu.misc.presence.web;

import org.santfeliu.misc.presence.PresenceEntry;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.matrix.cases.Case;
import org.matrix.cases.CaseFilter;
import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CaseManagerService;
import org.matrix.dic.Property;
import org.matrix.kernel.KernelManagerPort;
import org.matrix.kernel.KernelManagerService;
import org.matrix.kernel.Person;
import org.matrix.security.SecurityManagerPort;
import org.matrix.security.SecurityManagerService;
import org.matrix.security.User;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.misc.presence.PresenceDB;
import org.santfeliu.misc.presence.PresenceEntryType;
import org.santfeliu.misc.presence.WorkerProfile;
import org.santfeliu.security.util.LDAPConnector;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;

/**
 *
 * @author realor
 */
@CMSManagedBean
public final class PresenceBean extends WebBean implements Savable
{
  private static final Logger logger = Logger.getLogger("PresenceBean");
  public static final String IDCODE_PARAM = "idcode";

  @CMSProperty(mandatory=true)
  public static final String IDCODE_PROPERTY = "idCodeProperty";
  @CMSProperty
  public static final String WORKER_CASETYPEID_PROPERTY = "workerCaseTypeId";
  @CMSProperty
  public static final String WORK_HOURS_PROPERTY = "workHoursProperty";
  @CMSProperty
  public static final String WORK_MINUTES_PROPERTY = "workMinutesProperty";
  @CMSProperty
  public static final String BONUS_MINUTES_PROPERTY = "bonusMinutesProperty";
  @CMSProperty
  public static final String BONUS_START_DATE_PROPERTY = "bonusStartDate";
  @CMSProperty
  public static final String ENTRY_TYPES_PROPERTY = "entryTypes";
  @CMSProperty
  public static final String LDAP_USERID_PROPERTY = "ldapUserIdProperty";
  @CMSProperty(mandatory=true)
  public static final String DATA_SOURCE_PROPERTY = "dataSource";
  @CMSProperty
  public static final String VALID_IP_ADDRESS_PROPERTY = "validIpAddress";
  @CMSProperty
  public static final String MAX_WORKING_PERIOD_PROPERTY = "maxWorkingPeriod";
  @CMSProperty
  public static final String CASES_ENDPOINT_NAME_PROPERTY = "casesEndpointName";

  private String idCode;
  private Date currentDate;
  private boolean editingTime;
  private String time;
  private String reason;
  private PresenceEntry lastEntry;
  private PresenceEntry editingEntry;
  private List<PresenceEntryType> entryTypes;
  private int workedTimeInDay;
  private int workedTimeInWeek;
  private int workedTimeInYear;
  private int bonusTimeInDay;
  private int bonusTimeInWeek;
  private int bonusTimeInYear;
  private List<PresenceEntry> entries;
  private transient PresenceDB db;

  public PresenceBean()
  {
    initDate();
  }

  public String getSessionTrack()
  {
    return "sessionTrack";
  }

  public void setSessionTrack(String track)
  {
    WorkerProfile workerProfile = getWorkerProfile();
    if (workerProfile == null)
    {
      String outcome = sessionLost();
      FacesContext context = getFacesContext();
      Application application = context.getApplication();
      application.getNavigationHandler().handleNavigation(
        context, null, outcome);
      context.renderResponse();
    }
  }

  public WorkerProfile getWorkerProfile()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    WorkerProfile workerProfile =
      (WorkerProfile)userSessionBean.getAttribute("workerProfile");
    return workerProfile;
  }

  public void setWorkerProfile(WorkerProfile workerProfile)
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    userSessionBean.setAttribute("workerProfile", workerProfile);
  }

  public List<PresenceEntry> getEntries()
  {
    return entries;
  }

  public boolean isToday()
  {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
    String d1 = df.format(currentDate);
    String d2 = df.format(new Date());
    return d1.equals(d2);
  }

  public Date getCurrentDate()
  {
    return currentDate;
  }

  public String getIdCode()
  {
    return idCode;
  }

  public void setIdCode(String idCode)
  {
    this.idCode = idCode;
  }

  public boolean isEditingTime()
  {
    return editingTime;
  }

  public void setEditingTime(boolean editTime)
  {
    this.editingTime = editTime;
  }

  public String getTime()
  {
    return time;
  }

  public void setTime(String time)
  {
    this.time = time;
  }

  public String getReason()
  {
    return reason;
  }

  public void setReason(String reason)
  {
    this.reason = reason;
  }

  public List<PresenceEntryType> getEntryTypes()
  {
    if (entryTypes == null)
    {
      entryTypes = new ArrayList<PresenceEntryType>();
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      MenuItemCursor cursor = userSessionBean.getSelectedMenuItem();
      List<String> list = cursor.getMultiValuedProperty("entryTypes");
      for (String elem : list)
      {
        PresenceEntryType type = PresenceEntryType.parse(elem);
        if (type != null) entryTypes.add(type);
      }
    }
    return entryTypes;
  }

  public PresenceEntry getEditingEntry()
  {
    return editingEntry;
  }

  public List<SelectItem> getEditingEntryTypes()
  {
    ArrayList<SelectItem> selectItems = new ArrayList<SelectItem>();
    PresenceEntryType editingEntryType = editingEntry.getEntryType(entryTypes);
    for (PresenceEntryType entryType : getEntryTypes())
    {
      if (entryType.isChangeableTo(editingEntryType))
      {
        SelectItem item = new SelectItem();
        item.setValue(entryType.getType());
        item.setLabel(entryType.getLabel());
        item.setDescription(entryType.getLabel());
        selectItems.add(item);
      }
    }
    return selectItems;
  }

  public boolean isEditingEntryInternal()
  {
    return isInternalAddress(editingEntry.getIpAddress());
  }

  public String getEditingEntryLabel()
  {
    if (editingEntry == null) return null;
    PresenceEntryType entryType = editingEntry.getEntryType(getEntryTypes());
    return entryType == null ? editingEntry.getType() : entryType.getLabel();
  }

  public String getEditingEntryBonusTimeFormatted()
  {
    return formatDuration(editingEntry.getBonusTime());
  }

  public String getEntryLabel()
  {
    PresenceEntry entry = (PresenceEntry)getValue("#{entry}");
    PresenceEntryType entryType = entry.getEntryType(getEntryTypes());
    return entryType == null ? entry.getType() : entryType.getLabel();
  }

  public boolean isWorkEntry()
  {
    PresenceEntry entry = (PresenceEntry)getValue("#{entry}");
    PresenceEntryType entryType = entry.getEntryType(getEntryTypes());
    return entryType == null ? false : entryType.isWork();
  }

  public boolean isEntryTypeEnabled()
  {
    // entryType disable check
    PresenceEntryType entryType = (PresenceEntryType)getValue("#{entryType}");
    if (entryType == null) return false;
    if (entryType.isDisabled(entries)) return false;

    // check previous entry
    if (lastEntry == null) return entryType.isWork();
    if (!entryType.isPreviousTypeValid(lastEntry.getType())) return false;

    return true;
  }

  public int getEntryDuration()
  {
    PresenceEntry entry = (PresenceEntry)getValue("#{entry}");
    return entry.getDuration(new Date());
  }

  public int getWorkedTimeInDay()
  {
    return workedTimeInDay;
  }

  public int getWorkedTimeInWeek()
  {
    return workedTimeInWeek;
  }

  public int getWorkedTimeInYear()
  {
    return workedTimeInYear;
  }

  public int getBonusTimeInDay()
  {
    return bonusTimeInDay;
  }

  public int getBonusTimeInWeek()
  {
    return bonusTimeInWeek;
  }

  public int getBonusTimeInYear()
  {
    return bonusTimeInYear;
  }

  public String getWorkedTimeInDayFormatted()
  {
    return formatDuration(workedTimeInDay);
  }

  public String getWorkedTimeInWeekFormatted()
  {
    return formatDuration(workedTimeInWeek);
  }

  public String getWorkedTimeInYearFormatted()
  {
    return formatDuration(workedTimeInYear);
  }

  public String getBonusTimeInDayFormatted()
  {
    return formatDuration(bonusTimeInDay);
  }

  public String getBonusTimeInWeekFormatted()
  {
    return formatDuration(bonusTimeInWeek);
  }

  public String getBonusTimeInYearFormatted()
  {
    return formatDuration(bonusTimeInYear);
  }

  public String getTotalTimeInDayFormatted()
  {
    return formatDuration(workedTimeInDay + bonusTimeInDay);
  }

  public String getTotalTimeInWeekFormatted()
  {
    return formatDuration(workedTimeInWeek + bonusTimeInWeek);
  }

  public String getTotalTimeInYearFormatted()
  {
    return formatDuration(workedTimeInYear + bonusTimeInYear);
  }

  public String getEntryDurationFormatted()
  {
    return formatDuration(getEntryDuration());
  }

  public String getWorkerWorkingTimeFormatted()
  {
    return formatDuration(getWorkerProfile().getWorkingTime());
  }

  public String getWorkerBonusTimeFormatted()
  {
    return formatDuration(getWorkerProfile().getBonusTime());
  }

  /****** actions ******/

  @CMSAction
  public String show()
  {
    idCode =
     (String)getExternalContext().getRequestParameterMap().get(IDCODE_PARAM);
    if (idCode != null) return identify();

    if (getWorkerProfile() == null)
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      if (userSessionBean.isAnonymousUser()) return "presence_id";
      try
      {
        loadWorkerProfile(userSessionBean.getUserId());
      }
      catch (Exception ex)
      {
        error(ex);
        return "presence_id";
      }
    }
    try
    {
      initDate();
      findEntries();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    editingTime = false;
    return "presence";
  }

  public String identify()
  {
    try
    {
      setWorkerProfile(null);
      initDate();

      if (idCode == null) return "presence_id";

      String userId = findIdCodeInLDAP(idCode);
      loadWorkerProfile(userId);

      findEntries();
      return "presence";
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "presence_id";
  }

  public String goToday()
  {
    WorkerProfile workerProfile = getWorkerProfile();
    if (workerProfile == null) return sessionLost();

    logger.log(Level.INFO, "personId:{0}", workerProfile.getPersonId());
    try
    {
      currentDate = new Date();
      addDate(0);
      editingTime = false;
      findEntries();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String nextDay()
  {
    WorkerProfile workerProfile = getWorkerProfile();
    if (workerProfile == null) return sessionLost();

    logger.log(Level.INFO, "personId:{0}", workerProfile.getPersonId());
    addDate(24);
    editingTime = !isToday();
    try
    {
      findEntries();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String previousDay()
  {
    WorkerProfile workerProfile = getWorkerProfile();
    if (workerProfile == null) return sessionLost();

    logger.log(Level.INFO, "personId:{0}", workerProfile.getPersonId());
    addDate(-24);
    editingTime = !isToday();
    try
    {
      findEntries();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String mark()
  {
    try
    {
      WorkerProfile workerProfile = getWorkerProfile();
      if (workerProfile == null) return sessionLost();

      String personId = workerProfile.getPersonId();

      Date now = new Date();
      Date date;
      if (editingTime)
      {
        if (StringUtils.isBlank(time))
        {
          error("ENTER_TIME");
          return null;
        }
        String sdate = TextUtils.formatDate(currentDate, "yyyyMMdd");
        String pattern;
        int elems = time.split(":").length;
        if (elems == 1) pattern = "yyyyMMddHH";
        else if (elems == 2) pattern = "yyyyMMddHH:mm";
        else pattern = "yyyyMMddHH:mm:ss";
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        df.setLenient(false);
        try
        {
          date = df.parse(sdate + time);
        }
        catch (ParseException ex)
        {
          error("INVALID_HOUR_FORMAT");
          return null;
        }
        if (date.after(now))
        {
          error("FUTURE_TIME_NOT_ALLOWED");
          return null;
        }
      }
      else date = now;

      String dateTime = TextUtils.formatDate(date, "yyyyMMddHHmmss");
      PresenceEntryType entryType = (PresenceEntryType)getValue("#{entryType}");
      PresenceEntry newEntry = createEntry(personId, dateTime, entryType);
      try
      {
        if (editingTime)
        {
          getDB().insertEntry(newEntry);
        }
        else
        {
          getDB().addEntry(newEntry);
        }
        reason = null;
        time = null;
        currentDate = date; // go to entry date
        addDate(0);
        editingTime = !isToday();
      }
      finally
      {
        findEntries();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "presence";
  }

  public String editEntry()
  {
    WorkerProfile workerProfile = getWorkerProfile();
    if (workerProfile == null) return "presence_id";

    logger.log(Level.INFO, "personId:{0}", workerProfile.getPersonId());

    editingEntry = (PresenceEntry)getValue("#{entry}");
    if (editingEntry != null) return "presence_entry";

    return "presence";
  }

  public String editLastEntry()
  {
    WorkerProfile workerProfile = getWorkerProfile();
    if (workerProfile == null) return "presence_id";

    logger.log(Level.INFO, "personId:{0}", workerProfile.getPersonId());

    if (lastEntry != null)
    {
      editingEntry = lastEntry;
      return "presence_entry";
    }
    return "presence";
  }

  public String back()
  {
    WorkerProfile workerProfile = getWorkerProfile();
    if (workerProfile == null) return sessionLost();

    try
    {
      editingEntry = null;

      String personId = workerProfile.getPersonId();

      logger.log(Level.INFO, "personId:{0}", personId);

      findEntries();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "presence";
  }

  public String modifyEntry()
  {
    WorkerProfile workerProfile = getWorkerProfile();
    if (workerProfile == null) return sessionLost();

    try
    {
      String personId = workerProfile.getPersonId();

      logger.log(Level.INFO, "personId:{0}", personId);
      try
      {
        getDB().updateEntry(editingEntry);
        editingEntry = null;
      }
      finally
      {
        findEntries();
      }
      return "presence";
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String removeEntry()
  {
    WorkerProfile workerProfile = getWorkerProfile();
    if (workerProfile == null) return sessionLost();

    try
    {
      String personId = workerProfile.getPersonId();

      logger.log(Level.INFO, "personId:{0}", personId);
      try
      {
        if (editingEntry != null)
        {
          getDB().removeEntry(editingEntry);
          editingEntry = null;
        }
      }
      finally
      {
        findEntries();
      }
      return "presence";
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String editTime()
  {
    WorkerProfile workerProfile = getWorkerProfile();
    if (workerProfile == null) return sessionLost();

    String personId = workerProfile.getPersonId();

    logger.log(Level.INFO, "personId:{0}", personId);

    editingTime = true;
    time = "";
    reason = "";
    return null;
  }

  public String cancel()
  {
    editingTime = false;
    return null;
  }

  public String close()
  {
    WorkerProfile workerProfile = getWorkerProfile();
    if (workerProfile == null) return sessionLost();

    String personId = workerProfile.getPersonId();
    logger.log(Level.INFO, "personId:{0}", personId);
    reset();
    return "presence_id";
  }

  public String showWorkerProfile()
  {
    return "presence_worker";
  }

  /**** private methods ****/

  private String sessionLost()
  {
    logger.log(Level.INFO, "session lost");
    warn("PRESENCE_SESSION_LOST");
    reset();
    return "presence_id";
  }

  private void reset()
  {
    setWorkerProfile(null);
    entries = null;
    entryTypes = null;
    time = null;
    reason = null;
    editingTime = false;
    lastEntry = null;
    workedTimeInDay = 0;
    workedTimeInWeek = 0;
    bonusTimeInDay = 0;
    bonusTimeInWeek = 0;
  }

  private PresenceDB getDB() throws Exception
  {
    if (db == null)
      db = new PresenceDB(getDSN(), getEntryTypes(), getWorkerProfile());
    return db;
  }

  private PresenceEntry createEntry(String personId, String dateTime,
    PresenceEntryType entryType)
  {
    String ipAddress = getIpAddress();
    PresenceEntry entry = new PresenceEntry();
    entry.setPersonId(personId);
    entry.setDateTime(dateTime);
    entry.setType(entryType.getType());
    entry.setManipulated(editingTime || !isInternalAddress(ipAddress));
    entry.setReason(reason);
    String nowDateTime = TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");
    entry.setCreationDateTime(nowDateTime);
    entry.setIpAddress(ipAddress);
    return entry;
  }

  private String formatDuration(int duration)
  {
    if (duration == 0) return "0";
    long hours = duration / (60 * 60);
    long minutes = (duration % (60 * 60)) / 60;
    long seconds = duration % 60;
    StringBuilder buffer = new StringBuilder();
    buffer.append(hours).append("h ");
    if (minutes != 0 || seconds != 0) buffer.append(minutes).append("m ");
    if (seconds != 0) buffer.append(seconds).append("s ");
    return buffer.toString();
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

  private String getWorkerCaseTypeId()
  {
    return getProperty(WORKER_CASETYPEID_PROPERTY);
  }

  private String getWorkHoursProperty()
  {
    return getProperty(WORK_HOURS_PROPERTY);
  }

  private String getWorkMinutesProperty()
  {
    return getProperty(WORK_MINUTES_PROPERTY);
  }

  private String getBonusMinutesProperty()
  {
    return getProperty(BONUS_MINUTES_PROPERTY);
  }

  private String getBonusStartDate()
  {
    return getProperty(BONUS_START_DATE_PROPERTY);
  }

  private String getCasesEndpointName()
  {
    return getProperty(CASES_ENDPOINT_NAME_PROPERTY);
  }

  private int getMaxWorkingPeriod()
  {
    String value = getProperty(MAX_WORKING_PERIOD_PROPERTY);
    int maxWorkingPeriod = 14; // hours
    try
    {
      maxWorkingPeriod = Integer.parseInt(value);
    }
    catch (NumberFormatException ex)
    {
    }
    return maxWorkingPeriod;
  }

  private SecurityManagerPort getSecurityPort()
  {
    String userId = MatrixConfig.getProperty("adminCredentials.userId");
    String password = MatrixConfig.getProperty("adminCredentials.password");

    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint = wsDirectory.getEndpoint(SecurityManagerService.class);
    return endpoint.getPort(SecurityManagerPort.class, userId, password);
  }

  private KernelManagerPort getKernelPort()
  {
    String userId = MatrixConfig.getProperty("adminCredentials.userId");
    String password = MatrixConfig.getProperty("adminCredentials.password");

    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint = wsDirectory.getEndpoint(KernelManagerService.class);
    return endpoint.getPort(KernelManagerPort.class, userId, password);
  }

  private CaseManagerPort getCasesPort()
  {
    String userId = MatrixConfig.getProperty("adminCredentials.userId");
    String password = MatrixConfig.getProperty("adminCredentials.password");

    WSDirectory wsDirectory = WSDirectory.getInstance();
    String casesEndpointName = getCasesEndpointName();
    WSEndpoint endpoint = (casesEndpointName == null) ?
      wsDirectory.getEndpoint(CaseManagerService.class) :
      wsDirectory.getEndpoint(casesEndpointName);

    return endpoint.getPort(CaseManagerPort.class, userId, password);
  }

  private void initDate()
  {
    currentDate = new Date();
    addDate(0);
  }

  private void addDate(int hours)
  {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(currentDate);
    calendar.set(Calendar.HOUR_OF_DAY, 12);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.add(Calendar.HOUR_OF_DAY, hours);
    currentDate = calendar.getTime();
  }

  private boolean isInternalAddress(String ipAddress)
  {
    String validIpAddress = getProperty(VALID_IP_ADDRESS_PROPERTY);
    if (validIpAddress == null) return true;
    return ipAddress.matches(validIpAddress);
  }

  private String getIpAddress()
  {
    HttpServletRequest request =
      (HttpServletRequest)getFacesContext().getExternalContext().getRequest();
    return request.getRemoteAddr();
  }

  private void findEntries() throws Exception
  {
    WorkerProfile workerProfile = getWorkerProfile();
    if (workerProfile == null) return;

    String personId = workerProfile.getPersonId();

    entries = getDB().findEntries(currentDate, personId);

    // update lastEntry
    lastEntry = null;

    int entryCount = entries.size();
    if (entryCount > 0)
    {
      PresenceEntry entry = entries.get(entryCount - 1);
      if (entry.getDuration() == 0) lastEntry = entry;
    }
    if (lastEntry == null)
    {
      lastEntry = getDB().findLastEntry(personId);
    }

    updateTimes(personId);

    checkStillWorking();
  }

  private String getDSN() throws Exception
  {
    String dsn = getProperty(DATA_SOURCE_PROPERTY);

    if (dsn == null) throw new Exception("UNDEFINED_DATASOURCE");
    return dsn;
  }

  private void updateTimes(String personId) throws Exception
  {
    workedTimeInDay = 0;
    bonusTimeInDay = 0;
    for (PresenceEntry entry : entries)
    {
      workedTimeInDay += entry.getWorkedTime();
      bonusTimeInDay += entry.getBonusTime();
    }
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    Calendar cal = Calendar.getInstance();
    int times[];

    cal.setTime(currentDate);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    cal.set(Calendar.DAY_OF_MONTH, 15);
    cal.set(Calendar.MONTH, Calendar.JANUARY);
    if (cal.getTime().compareTo(currentDate) > 0)
    {
      cal.add(Calendar.YEAR, -1);
    }
    String yearStartDateTime = df.format(cal.getTime());
    cal.add(Calendar.YEAR, 1);
    String yearEndDateTime = df.format(cal.getTime());

    times =
      getDB().getWorkedAndBonusTime(personId, yearStartDateTime, yearEndDateTime);
    workedTimeInYear = times[0];
    bonusTimeInYear = times[1];

    cal.setTime(currentDate);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
    String weekStartDateTime = df.format(cal.getTime());
    cal.add(Calendar.DAY_OF_MONTH, 7);
    String weekEndDateTime = df.format(cal.getTime());

    times =
      getDB().getWorkedAndBonusTime(personId, weekStartDateTime, weekEndDateTime);
    workedTimeInWeek = times[0];
    bonusTimeInWeek = times[1];

    // add current interval times
    if (lastEntry != null)
    {
      PresenceEntryType entryType = lastEntry.getEntryType(getEntryTypes());
      if (entryType != null && entryType.isWork())
      {
        Date nowDate = new Date();
        Date lastDate = lastEntry.getDate();

        int duration = (int)((nowDate.getTime() - lastDate.getTime()) / 1000L);
        int lastWorkedTime = entryType.getWorkedTime(duration);

        String sLastDate = TextUtils.formatDate(lastDate, "yyyyMMdd");
        String sCurrentDate = TextUtils.formatDate(currentDate, "yyyyMMdd");
        String sNowDate = TextUtils.formatDate(nowDate, "yyyyMMdd");
        String nowDateTime = TextUtils.formatDate(nowDate, "yyyyMMddHHmmss");

        if (sLastDate.equals(sCurrentDate) && sNowDate.equals(sCurrentDate))
        {
          workedTimeInDay += lastWorkedTime;
        }

        if (nowDateTime.compareTo(weekStartDateTime) > 0 &&
            nowDateTime.compareTo(weekEndDateTime) < 0)
        {
          workedTimeInWeek += lastWorkedTime;
        }

        if (nowDateTime.compareTo(yearStartDateTime) > 0 &&
            nowDateTime.compareTo(yearEndDateTime) < 0)
        {
          workedTimeInYear += lastWorkedTime;
        }
      }
    }
  }

  private void checkStillWorking()
  {
    if (lastEntry != null)
    {
      Date now = new Date();
      Date last = lastEntry.getDate();
      int maxWorkingPeriod = getMaxWorkingPeriod(); // hours
      long periodInMillis = maxWorkingPeriod * 3600000;
      if (now.getTime() - last.getTime() > periodInMillis)
      {
        String lastEntryDate = lastEntry.getDateTime().substring(0, 8);
        String todayDate = TextUtils.formatDate(now, "yyyyMMdd");
        if (lastEntryDate.compareTo(todayDate) < 0) // previous day
        {
          PresenceEntryType entryType = lastEntry.getEntryType(getEntryTypes());
          if (entryType.isWork()) // still working
          {
            warn("STILL_WORKING");
          }
        }
      }
    }
  }

  private void loadWorkerProfile(String userId) throws Exception
  {
    WorkerProfile workerProfile = new WorkerProfile();
    workerProfile.setUserId(userId);

    SecurityManagerPort securityPort = getSecurityPort();
    User user = securityPort.loadUser(userId);
    String personId = user.getPersonId();
    workerProfile.setPersonId(personId);

    // read worker case
    String workerCaseTypeId = getWorkerCaseTypeId();
    if (workerCaseTypeId != null)
    {
      String today = TextUtils.formatDate(new Date(), "yyyyMMdd");
      CaseManagerPort casesPort = getCasesPort();
      CaseFilter caseFilter = new CaseFilter();
      caseFilter.setCaseTypeId(workerCaseTypeId);
      caseFilter.setPersonId(personId);
      List<Case> caseList = casesPort.findCases(caseFilter);
      boolean found = false;
      Iterator<Case> iter = caseList.iterator();
      Case _case = null;
      while (!found && iter.hasNext())
      {
        _case = iter.next();
        String startDate = _case.getStartDate();
        String endDate = _case.getEndDate();
        if ((startDate == null || startDate.compareTo(today) <= 0) &&
            (endDate == null || endDate.compareTo(today) >= 0))
        {
          found = true;
          workerProfile.setDisplayName(_case.getTitle());
        }
      }
      if (found)
      {
        String caseId = _case.getCaseId();
        _case = casesPort.loadCase(caseId);

        String workHoursProperty = getWorkHoursProperty();
        String workMinutesProperty = getWorkMinutesProperty();
        String bonusMinutesProperty = getBonusMinutesProperty();

        int workingTime = 0;
        int bonusTime = 0;
        for (Property property : _case.getProperty())
        {
          String propertyName = property.getName();
          if (propertyName.equals(workHoursProperty)) // hours
          {
            String value = property.getValue().get(0);
            workingTime += 3600 * parseDouble(value);
          }
          else if (propertyName.equals(workMinutesProperty)) // minutes
          {
            String value = property.getValue().get(0);
            workingTime += 60 * parseDouble(value);
          }
          else if (propertyName.equals(bonusMinutesProperty)) // minutes
          {
            String value = property.getValue().get(0);
            bonusTime += 60 * parseDouble(value);
          }
        }
        workerProfile.setCaseId(caseId);
        workerProfile.setWorkingTime(workingTime);
        workerProfile.setBonusTime(bonusTime);
        workerProfile.setBonusStartDate(getBonusStartDate());
      }
      else warn("WORKER_CASE_NOT_FOUND");
    }

    if (workerProfile.getDisplayName() == null)
    {
      KernelManagerPort kernelPort = getKernelPort();
      Person person = kernelPort.loadPerson(personId);
      StringBuilder buffer = new StringBuilder();
      if (person.getName() != null)
      {
        buffer.append(person.getName());
      }
      if (person.getFirstParticle() != null)
      {
        buffer.append(" ");
        buffer.append(person.getFirstParticle());
      }
      if (person.getFirstSurname() != null)
      {
        buffer.append(" ");
        buffer.append(person.getFirstSurname());
      }
      if (person.getSecondParticle() != null)
      {
        buffer.append(" ");
        buffer.append(person.getSecondParticle());
      }
      if (person.getSecondSurname() != null)
      {
        buffer.append(" ");
        buffer.append(person.getSecondSurname());
      }
      workerProfile.setDisplayName(buffer.toString());
    }

    setWorkerProfile(workerProfile);

    System.out.println(workerProfile);
  }

  private double parseDouble(String value)
  {
    try
    {
      return Double.parseDouble(value);
    }
    catch (NumberFormatException ex)
    {
      return 0;
    }
  }
}


