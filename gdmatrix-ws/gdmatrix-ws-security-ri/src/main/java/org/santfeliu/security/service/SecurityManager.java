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
package org.santfeliu.security.service;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import org.apache.commons.lang.StringUtils;
import org.matrix.agenda.AgendaConstants;
import org.matrix.cases.CaseConstants;
import org.matrix.classif.ClassificationConstants;
import org.matrix.cms.CMSConstants;
import org.matrix.dic.DictionaryConstants;
import org.santfeliu.security.SecurityProvider;
import org.santfeliu.security.UserCache;
import org.santfeliu.security.encoder.DigestEncoder;
import org.santfeliu.security.util.LDAPConnector;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;
import org.santfeliu.ws.WSExceptionFactory;
import org.matrix.security.*;
import org.santfeliu.security.util.StringCipher;
import org.santfeliu.ws.WSProperties;
import org.santfeliu.ws.annotations.Disposer;
import org.santfeliu.ws.annotations.Initializer;
import org.santfeliu.ws.annotations.MultiInstance;
import org.santfeliu.ws.annotations.State;
import static org.matrix.dic.DictionaryConstants.ROLE_TYPE;
import org.matrix.dic.Property;
import org.matrix.doc.DocumentConstants;
import org.matrix.forum.ForumConstants;
import org.matrix.job.JobConstants;
import org.matrix.kernel.KernelConstants;
import org.matrix.policy.PolicyConstants;
import org.matrix.presence.PresenceConstants;
import org.matrix.sql.SQLConstants;
import org.matrix.workflow.WorkflowConstants;
import org.santfeliu.security.util.Credentials;


/**
 *
 * @author realor
 */
@WebService(endpointInterface="org.matrix.security.SecurityManagerPort")
@HandlerChain(file="handlers.xml")
@MultiInstance
public class SecurityManager implements SecurityManagerPort
{
  private static final Logger LOGGER = Logger.getLogger("Security");

  private static final int USER_PROPERTY_NAME_MAX_SIZE = 100;
  private static final int USER_PROPERTY_VALUES_MAX_ITEMS = 99999;
  private static final int USER_PROPERTY_VALUE_MAX_SIZE = 100;

  // internal constants
  public static final String PK_SEPARATOR = ";";
  public static final int ROLE_DESCRIPTION_MAX_SIZE = 400;
  public static final int ROLE_ID_MAX_SIZE = 20;
  public static final int ROLE_NAME_MAX_SIZE = 100;
  public static final int USER_DISPLAY_NAME_MAX_SIZE = 60;
  public static final int USER_ID_MAX_SIZE = 20;

  // MatrixConfig properties

  /* digest encoder className */
  public static final String DIGEST_ENCODER = "digestEncoder";
  /* digest encode parameters */
  public static final String DIGEST_PARAMETERS = "digestParameters";
  /* master password to log with any userId */
  public static final String MASTER_PASSWORD = "masterPassword";
  /* certificate user roles (list separated by comma) */
  public static final String CERT_USER_ROLES = "certUserRoles";
  /* validate certificate against a validation service (true|false) */
  public static final String VALIDATE_CERTIFICATE = "validateCertificate";
  /* minimum user length */
  public static final String MIN_USER_LENGTH = "userLength";
  /* minimum password length */
  public static final String MIN_PASSWORD_LENGTH = "passwordLength";
  /* admin userId */
  public static final String ADMIN_USERID = "adminCredentials.userId";
  /* admin password */
  public static final String ADMIN_PASSWORD = "adminCredentials.password";
  /* LDAP user validation enable (true|false) */
  public static final String LDAP_ENABLED = "ldap.enabled";
  /* LDAP server list (separated by comma) */
  public static final String LDAP_URL = "ldap.url";
  /* LDAP domain */
  public static final String LDAP_DOMAIN = "ldap.domain";
  /* LDAP search base */
  public static final String LDAP_BASE = "ldap.base";
  /* LDAP admin userId */
  public static final String LDAP_ADMIN_USERID = "ldap.adminUserId";
  /* LDAP admin password */
  public static final String LDAP_ADMIN_PASSWORD = "ldap.adminPassword";
  /* default personId for a user */
  public static final String DEFAULT_PERSONID = "defaultPersonId";
  /* Lock user control mode: all|noldap|off */
  private static final String USER_LOCK_CONTROL_MODE = "userLockControlMode";
  /* Max failed login attempts before block */  
  private static final String MAX_FAILED_LOGIN_ATTEMPTS = 
    "maxFailedLoginAttempts";
  /* Margin time to unlock a locked user */  
  private static final String AUTO_UNLOCK_MARGIN_TIME = "autoUnlockMarginTime";
  /* Min login attempts to log an intrusion attempt */  
  private static final String MIN_INTRUSION_ATTEMPTS = "minIntrusionAttempts";  

  @Resource
  WebServiceContext wsContext;

  @PersistenceContext(unitName="security_ri")
  EntityManager entityManager;

  @State
  Configuration config;

  public class Configuration
  {
    String masterPassword;
    Set<String> certUserRoles;
    boolean validateCertificate;
    DigestEncoder digestEncoder;
    String digestParameters;
    int minUserLength;
    int minPasswordLength;
    String defaultPersonId;
    String userLockControlMode;
    int maxFailedLoginAttempts;
    int autoUnlockMarginTime;
    int minIntrusionAttempts;

    Configuration(String endpointName)
    {
      try
      {
        WSProperties props =
          new WSProperties(endpointName, SecurityManager.class);
        String digestEncodeClass = props.getString(DIGEST_ENCODER,
          "org.santfeliu.security.encoder.MatrixDigestEncoder");
        digestEncoder =
          (DigestEncoder)Class.forName(digestEncodeClass).newInstance();
        digestParameters = props.getString(DIGEST_PARAMETERS);
        masterPassword = props.getString(MASTER_PASSWORD, "changeme");
        validateCertificate = props.getBoolean(VALIDATE_CERTIFICATE, false);
        String certUserRolesString = props.getString(CERT_USER_ROLES);
        String roles[] = certUserRolesString.split(",");
        certUserRoles = new HashSet<>();
        for (String role : roles)
          certUserRoles.add(role.trim());
        minUserLength = props.getInteger(MIN_USER_LENGTH, 8);
        minPasswordLength = props.getInteger(MIN_PASSWORD_LENGTH, 4);
        defaultPersonId = props.getString("defaultPersonId", "0");
        userLockControlMode = props.getString(USER_LOCK_CONTROL_MODE, "off");
        maxFailedLoginAttempts = props.getInteger(MAX_FAILED_LOGIN_ATTEMPTS, 5);
        autoUnlockMarginTime = props.getInteger(AUTO_UNLOCK_MARGIN_TIME, 600);
        minIntrusionAttempts = props.getInteger(MIN_INTRUSION_ATTEMPTS, 10);    
      }
      catch (Exception ex)
      {
        LOGGER.log(Level.WARNING, "Configuration error: {0}", ex.toString());
      }
    }
  };

  @Initializer
  public void initialize(String endpointName)
  {
    config = new Configuration(endpointName);

    String adminId = MatrixConfig.getProperty(ADMIN_USERID);
    createUser(adminId);
    createUserInRole(adminId, SecurityConstants.EVERYONE_ROLE, "Everyone");
    createUserInRole(adminId, SecurityConstants.SECURITY_ADMIN_ROLE,
      "Security administrator");
    createUserInRole(adminId, DictionaryConstants.DIC_ADMIN_ROLE,
      "Dictionary administrator");
    createUserInRole(adminId, KernelConstants.KERNEL_ADMIN_ROLE,
      "Kernel administrator");
    createUserInRole(adminId, DocumentConstants.DOC_ADMIN_ROLE,
      "Document administrator");
    createUserInRole(adminId, CaseConstants.CASE_ADMIN_ROLE,
      "Case administrator");
    createUserInRole(adminId, ClassificationConstants.CLASSIF_ADMIN_ROLE,
      "Classification administrator");
    createUserInRole(adminId, AgendaConstants.AGENDA_ADMIN_ROLE,
      "Agenda administrator");
    createUserInRole(adminId, JobConstants.JOB_ADMIN_ROLE,
      "Job administrator");
    createUserInRole(adminId, ForumConstants.FORUM_ADMIN_ROLE,
      "Forum administrator");
    createUserInRole(adminId, WorkflowConstants.WORKFLOW_ADMIN_ROLE,
      "Workflow administrator");
    createUserInRole(adminId, SQLConstants.SQL_ADMIN_ROLE,
      "SQL administrator");
    createUserInRole(adminId, PresenceConstants.PRESENCE_ADMIN_ROLE,
      "Presence administrator");
    createUserInRole(adminId, CMSConstants.CMS_ADMIN_ROLE,
      "CMS administrator");
    createUserInRole(adminId, PolicyConstants.POLICY_ADMIN_ROLE,
      "Policy administrator");
  }

  private void createUser(String userId)
  {
    Query query = entityManager.createNamedQuery("selectUser");
    query.setParameter("userId", userId);
    if (query.getResultList().isEmpty())
    {
      DBUser user = new DBUser();
      user.setUserId("admin");
      user.setDisplayName("Administrator");
      user.setPersonId(config.defaultPersonId);
      entityManager.persist(user);
      entityManager.flush();
    }
  }

  private void createUserInRole(String userId, String roleId, String roleDesc)
  {
    DBRole role = entityManager.find(DBRole.class, roleId);
    if (role == null)
    {
      role = new DBRole();
      role.setRoleId(roleId);
      role.setName(roleDesc);
      entityManager.persist(role);
      entityManager.flush();
    }

    Query query = entityManager.createNamedQuery("selectUserInRole");
    query.setParameter("userId", userId);
    query.setParameter("roleId", roleId);
    if (query.getResultList().isEmpty())
    {
      DBUserInRole userInRole = new DBUserInRole();
      userInRole.setUserId(userId);
      userInRole.setRoleId(roleId);
      entityManager.persist(userInRole);
      entityManager.flush();
    }
  }

  @Disposer
  public void dispose(String endpointName)
  {
  }

  @Override
  public SecurityMetaData getSecurityMetaData()
  {
    try
    {
      LOGGER.log(Level.INFO, "getSecurityMetaData");
      if (!isMatrixAdmin() && !isUserAdmin()) 
        throw new Exception("ACTION_DENIED");

      SecurityMetaData metaData = new SecurityMetaData();
      metaData.setRoleDescriptionMaxSize(ROLE_DESCRIPTION_MAX_SIZE);
      metaData.setRoleIdMaxSize(ROLE_ID_MAX_SIZE);
      metaData.setRoleNameMaxSize(ROLE_NAME_MAX_SIZE);
      metaData.setUserDisplayNameMaxSize(USER_DISPLAY_NAME_MAX_SIZE);
      metaData.setUserIdMaxSize(USER_ID_MAX_SIZE);
      metaData.setUserLockControlMode(config.userLockControlMode);
      metaData.setMaxFailedLoginAttempts(config.maxFailedLoginAttempts);
      metaData.setAutoUnlockMarginTime(config.autoUnlockMarginTime);
      metaData.setMinIntrusionAttempts(config.minIntrusionAttempts);
      return metaData;
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "getSecurityMetaData failed");
      throw WSExceptionFactory.create(ex);
    }
  }

  @Override
  public List<User> findUsers(UserFilter filter)
  {
    try
    {
      LOGGER.log(Level.INFO, "findUsers");
      if (!isMatrixAdmin() && !isUserAdmin()) 
        throw new Exception("ACTION_DENIED");

      List<DBUser> dbUsers = new ArrayList();
      int userCount = filter.getUserId() != null ? filter.getUserId().size() : 0;
      Query query;
      if (userCount > 1)
        query = entityManager.createNamedQuery("findUsersMultipleId");
      else
        query = entityManager.createNamedQuery("findUsersSingleId");
      setUserFilterParameters(query, filter, userCount);
      List<Object[]> dbList = query.getResultList();
      for (Object[] row : dbList)
      {
        DBUser dbUser = (DBUser)row[0];
        if (row[1] != null)
        {
          try
          {
            dbUser.setFailedLoginAttempts(Integer.valueOf((String)row[1]));
          }
          catch (NumberFormatException ex)
          {
            dbUser.setFailedLoginAttempts(0);
          }                    
        }
        if (row[2] != null)
        {          
          dbUser.setLastSuccessLoginDateTime((String)row[2]);
        }
        if (row[3] != null)
        {
          dbUser.setLastFailedLoginDateTime((String)row[3]);
        }
        if (row[4] != null)
        {
          dbUser.setLastIntrusionDateTime((String)row[4]);
        }
        dbUsers.add(dbUser);        
      }

      List<User> users = new ArrayList<>();
      for (DBUser dbUser : dbUsers)
      {
        User user = new User();
        dbUser.copyTo(user);
        users.add(user);
      }
      return users;
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "findUsers failed");
      throw WSExceptionFactory.create(ex);
    }
  }

  @Override
  public int countUsers(UserFilter filter)
  {
    try
    {
      LOGGER.log(Level.INFO, "countUsers");
      if (!isMatrixAdmin() && !isUserAdmin()) 
        throw new Exception("ACTION_DENIED");

      int userCount = filter.getUserId() != null ? filter.getUserId().size() : 0;
      Query query;
      if (userCount > 1)
        query = entityManager.createNamedQuery("countUsersMultipleId");
      else
        query = entityManager.createNamedQuery("countUsersSingleId");
      setUserFilterParameters(query, filter, userCount);
      query.setFirstResult(0);
      query.setMaxResults(1);
      Number count = (Number)query.getSingleResult();
      return count.intValue();
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "countUsers failed");
      throw WSExceptionFactory.create(ex);
    }
  }

  @Override
  public User loadUser(String userId)
  {
    try
    {
      LOGGER.log(Level.INFO, "loadUser userId:{0}", userId);
      if (!isMatrixAdmin() && !isUserAdmin()) 
        throw new Exception("ACTION_DENIED");

      // checkRoles
      DBUser dbUser = selectUser(userId);
      if (dbUser == null)
        throw new WebServiceException("security:USER_NOT_FOUND");

      User user = new User();
      dbUser.copyTo(user);
      return user;
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "loadUser failed");
      throw WSExceptionFactory.create(ex);
    }
  }

  @Override
  public User storeUser(User user)
  {
    try
    {
      LOGGER.log(Level.INFO, "storeUser userId:{0}", user.getUserId());
      if (!isUserAdmin()) throw new Exception("ACTION_DENIED");
      validateUser(user);

      // check user data
      String userId = user.getUserId().trim();
      String password = user.getPassword();
      if (!StringUtils.isBlank(password))
      {
        // password change
        String adminUserId = MatrixConfig.getProperty(ADMIN_USERID);        
        String autoLoginUserId = 
          MatrixConfig.getProperty("org.santfeliu.web.autoLogin.userId");      

        if (userId.equals(adminUserId) || userId.equals(autoLoginUserId) || 
          isUserInLDAP(userId))
        {
          throw new Exception("security:CAN_NOT_CHANGE_PASSWORD");
        }
        
        password = password.trim();
        checkPasswordFormat(password);
      }
      String personId = user.getPersonId();
      if (StringUtils.isBlank(personId))
      {
        personId = config.defaultPersonId;
      }

      SimpleDateFormat ddf = new SimpleDateFormat("yyyyMMdd");
      SimpleDateFormat tdf = new SimpleDateFormat("HHmmss");
      Date now = new Date();
      String changeDate = ddf.format(now);
      String changeTime = tdf.format(now);
      String changeUserId = UserCache.getUser(wsContext).getUserId();

      DBUser dbUser = selectUser(userId);
      if (dbUser == null) // new user
      {
        dbUser = new DBUser(user);
        dbUser.setPassword(calcHash(password));
        dbUser.setPersonId(personId);
        dbUser.setCreationUserId(changeUserId);
        dbUser.setStddgr(changeDate);
        dbUser.setStdhgr(changeTime);
        dbUser.setChangeUserId(changeUserId);
        dbUser.setStddmod(changeDate);
        dbUser.setStdhmod(changeTime);
        entityManager.persist(dbUser);
      }
      else // change user
      {
        dbUser.copyFrom(user);
        dbUser.setPersonId(personId);
        if (password != null)
        {
          dbUser.setPassword(calcHash(password));
        }
        dbUser.setChangeUserId(changeUserId);
        dbUser.setStddmod(changeDate);
        dbUser.setStdhmod(changeTime);
        updateUser(dbUser);
      }
      if (checkUserLockControlEnabled(userId))
      {
        int failedLoginAttempts = (dbUser.getFailedLoginAttempts() == null ? 0 :
          dbUser.getFailedLoginAttempts());
        storeFailedLoginAttempts(dbUser.getUserId(), failedLoginAttempts);
        dbUser.setFailedLoginAttempts(failedLoginAttempts);
      }
      dbUser.copyTo(user);
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "storeUser", ex);
      throw WSExceptionFactory.create(ex);
    }
    return user;
  }

  @Override
  public boolean removeUser(String userId)
  {
    boolean result = false;
    try
    {
      LOGGER.log(Level.INFO, "removeUser userId:{0}", userId);
      if (!isUserAdmin()) throw new Exception("ACTION_DENIED");

      Query query = entityManager.createNamedQuery("removeUserInRole");
      query.setParameter("userId", userId);
      query.setParameter("roleId", null);
      query.executeUpdate();

      query = entityManager.createNamedQuery("removeUserProperty");
      query.setParameter("userId", userId.trim());
      query.setParameter("name", null);
      query.setParameter("index", null);
      query.executeUpdate();

      query = entityManager.createNamedQuery("removeUser");
      query.setParameter("userId", userId);
      query.executeUpdate();

      result = true;
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "removeUser", ex);
      throw WSExceptionFactory.create(ex);
    }
    return result;
  }

  @Override
  public User login(String userId, String password)
  {
    User user;
    try
    {
      LOGGER.log(Level.INFO, "login userId:{0}", userId);
      if (StringUtils.isBlank(userId))
      {
        user = null;
      }
      else if (userId.equals(SecurityConstants.ANONYMOUS))
      {
        // anonymous user
        user = new User();
        user.setUserId(SecurityConstants.ANONYMOUS);
        user.setDisplayName(SecurityConstants.ANONYMOUS);
      }
      else if (userId.startsWith(SecurityConstants.TEMP_USER_PREFIX))
      {
        // temporary user
        user = new User();
        user.setUserId(userId);
        user.setDisplayName(userId);
      }
      else
      {
        Date now = new java.util.Date();        
        DBUser dbUser = selectUser(userId);
        if (dbUser != null)
        {
          // check user locked
          if (dbUser.getLockedValue() == 1)
          {
            throw new Exception("security:LOCKED_USER");
          }

          try
          {
            boolean userLockControlEnabled = 
              checkUserLockControlEnabled(userId);
            boolean userLocked = false;
            
            if (userLockControlEnabled)
            {
              unlockUserIfNeeded(dbUser, now);
              userLocked = isUserLocked(dbUser);
            }
            
            // persistent user
            if (isMasterPassword(userId, password) ||
                isValidPassword(userId, password, dbUser.getPassword()))
            {
              if (!userLocked)
              {
                updateLastSuccessLoginDateTime(dbUser, now);
              }
              if (userLockControlEnabled)
              {
                checkUserLock(dbUser);
                resetUserLock(dbUser);              
              }
              user = new User();
              dbUser.copyTo(user);
              user.setPassword(password);
              if (userId.startsWith(SecurityConstants.AUTH_USER_PREFIX))
              {
                // registered certificate user: #NUMBER
                loadIdentificationInfo(user, userId);
              }
            }
            else //wrong password
            {
              if (!userLocked)
              {
                updateLastFailedLoginDateTime(dbUser, now);
              }              
              if (userLockControlEnabled)
              {
                incrementFailedLoginAttempts(dbUser, now);
                checkUserLock(dbUser);
              }
              throw new Exception("security:INVALID_IDENTIFICATION");
            }
          }
          catch (Exception ex)
          {
            //commit before exception
            txCommit();
            throw ex;
          }
        }
        else if (userId.startsWith(SecurityConstants.AUTH_USER_PREFIX))
        {          
          // unregistered certificate user: #NUMBER
          user = new User();
          user.setUserId(userId.trim());          
          if (isMasterPassword(userId, password) ||
              getAuthUserPassword(userId).equals(password))
          {
            user.setPassword(password);
            loadIdentificationInfo(user, userId);
            loadLockUserProperties(user);
            updateLastSuccessLoginDateTime(user, now);
          }
          else 
          {
            updateLastFailedLoginDateTime(user, now);
            //commit before exception
            txCommit();
            throw new Exception("security:INVALID_IDENTIFICATION");
          }
        }
        else throw new Exception("security:INVALID_IDENTIFICATION");
      }
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.WARNING, "login", ex);
      throw WSExceptionFactory.create(ex);
    }
    return user;
  }

  @Override
  public User loginCertificate(byte[] certData)
  {
    User user = null;
    try
    {
      LOGGER.log(Level.INFO, "loginCertificate");
      
      Date now = new java.util.Date();      
      String userId = null;
      String displayName = null;
      String givenName = null;
      String surname = null;
      String NIF = null;
      String CIF = null;
      String organizationName = null;
      String email = null;
      boolean representant = false;

      if (config.validateCertificate) // use Certificate validation service
      {
        Map attributes = new HashMap();

        SecurityProvider provider = SecurityUtils.getSecurityProvider();
        boolean valid = provider.validateCertificate(certData, attributes);

        if (!valid) throw new Exception("INVALID_CERTIFICATE");

        NIF = (String)attributes.get(SecurityProvider.NIF);
        CIF = (String)attributes.get(SecurityProvider.CIF);

        String commonName =
          (String)attributes.get(SecurityProvider.COMMON_NAME);
        if (CIF != null)
        {
          String repCIF = SecurityUtils.getRepresentantCIF(commonName);
          representant = repCIF != null && CIF.equalsIgnoreCase(repCIF);
        }

        if (NIF != null)
        {
          userId = SecurityConstants.AUTH_USER_PREFIX + NIF;
        }
        else if (CIF != null)
        {
          userId = SecurityConstants.AUTH_USER_PREFIX + CIF;
        }
        // TODO: accept others
        else throw new Exception("INVALID_CERTIFICATE");

        givenName = (String)attributes.get(SecurityProvider.GIVEN_NAME);
        surname = (String)attributes.get(SecurityProvider.SURNAME);
        email = (String)attributes.get(SecurityProvider.EMAIL);
        organizationName = (String)attributes.get(
          SecurityProvider.ORGANIZATION_NAME);
      }
      else // accept any Certificate
      {
        CertificateFactory certFactory = CertificateFactory.getInstance("X509");
        X509Certificate certificate = (X509Certificate)
          certFactory.generateCertificate(new ByteArrayInputStream(certData));

        Map attributes = SecurityUtils.getCertificateAttributes(certificate);

        NIF = (String)attributes.get("SERIALNUMBER");
        if (NIF == null) throw new Exception("INVALID_CERTIFICATE");

        givenName = (String)attributes.get("GIVENNAME");
        surname = (String)attributes.get("SURNAME");
        email = (String)attributes.get("SAN-1");
        organizationName = (String)attributes.get("O");

        userId = SecurityConstants.AUTH_USER_PREFIX + NIF;
      }
      displayName = givenName;
      if (surname != null)
      {
        int index = surname.indexOf("-");
        if (index != -1) surname = surname.substring(0, index);
        displayName += " " + surname;
      }

      user = new User();
      user.setUserId(userId.trim());
      user.setPassword(getAuthUserPassword(userId));
      user.setDisplayName(displayName);
      user.setGivenName(givenName);
      user.setSurname(surname);
      user.setNIF(NIF);
      user.setCIF(CIF);
      user.setRepresentant(representant);
      user.setOrganizationName(organizationName);
      user.setEmail(email);
      loadLockUserProperties(user);
      updateLastSuccessLoginDateTime(user, now);
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "loginCertificate", ex);
      throw WSExceptionFactory.create(ex);
    }
    return user;
  }

  @Override
  public String changePassword(
    String userId,
    String oldPassword,
    String newPassword)
  {
    String result = null;
    try
    {
      LOGGER.log(Level.INFO, "changePassword userId:{0}", userId);

      String adminUserId = MatrixConfig.getProperty(ADMIN_USERID);
      String autoLoginUserId = 
        MatrixConfig.getProperty("org.santfeliu.web.autoLogin.userId");      
      
      if (adminUserId.equals(userId) || autoLoginUserId.equals(userId))
        throw new Exception("security:CAN_NOT_CHANGE_PASSWORD");

      DBUser dbUser = selectUser(userId);
      if (dbUser != null)
      {
        if (isUserInLDAP(userId))
          throw new Exception("security:CAN_NOT_CHANGE_PASSWORD");

        if (isValidDatabasePassword(userId, oldPassword, dbUser.getPassword()))
        {
          checkPasswordFormat(newPassword);
          String newHash = calcHash(newPassword);
          Date now = new Date();
          SimpleDateFormat ddf = new SimpleDateFormat("yyyyMMdd");
          SimpleDateFormat tdf = new SimpleDateFormat("HHmmss");
          String changeUserId = UserCache.getUser(wsContext).getUserId();

          dbUser.setPassword(newHash);
          dbUser.setChangeUserId(changeUserId);
          dbUser.setStddmod(ddf.format(now));
          dbUser.setStdhmod(tdf.format(now));
          updateUser(dbUser);

          result = "ok";
        }
        else throw new Exception("security:INVALID_IDENTIFICATION");
      }
      else throw new Exception("security:INVALID_IDENTIFICATION");
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.WARNING, "changePassword", ex);
      throw WSExceptionFactory.create(ex);
    }
    return result;
  }

  @Override
  public List<Role> findRoles(RoleFilter filter)
  {
    try
    {
      LOGGER.log(Level.INFO, "findRoles");
      if (!isMatrixAdmin() && !isUserAdmin()) 
        throw new Exception("ACTION_DENIED");
    
      int roleCount = filter.getRoleId() != null ? filter.getRoleId().size() : 0;
      Query query;
      if (roleCount > 1)
        query = entityManager.createNamedQuery("findRolesMultipleId");
      else
        query = entityManager.createNamedQuery("findRolesSingleId");
      setRoleFilterParameters(query, filter, roleCount);
      List<DBRole> dbRoles = query.getResultList();
      List<Role> roles = new ArrayList<>();
      for (DBRole dbRole : dbRoles)
      {
        Role role = new Role();
        dbRole.copyTo(role);
        roles.add(role);
      }
      return roles;
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "findRoles failed");
      throw WSExceptionFactory.create(ex);
    }
  }

  @Override
  public int countRoles(RoleFilter filter)
  {
    try
    {
      LOGGER.log(Level.INFO, "countRoles");
      if (!isMatrixAdmin() && !isUserAdmin()) 
        throw new Exception("ACTION_DENIED");
      
      int roleCount = filter.getRoleId() != null ? filter.getRoleId().size() : 0;
      Query query;
      if (roleCount > 1)
        query = entityManager.createNamedQuery("countRolesMultipleId");
      else
        query = entityManager.createNamedQuery("countRolesSingleId");
      setRoleFilterParameters(query, filter, roleCount);
      query.setFirstResult(0);
      query.setMaxResults(1);
      Number count = (Number)query.getSingleResult();
      return count.intValue();
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "countRoles failed");
      throw WSExceptionFactory.create(ex);
    }
  }

  @Override
  public Role loadRole(String roleId)
  {
    try
    {
      Role role;
      LOGGER.log(Level.INFO, "loadRole roleId:{0}", roleId);
      if (!isMatrixAdmin() && !isUserAdmin()) 
        throw new Exception("ACTION_DENIED");        

      DBRole dbRole = entityManager.find(DBRole.class, roleId);
      if (dbRole == null)
        throw new WebServiceException("security:ROLE_NOT_FOUND");

      role = new Role();
      dbRole.copyTo(role);
      return role;
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "loadRole failed");
      throw WSExceptionFactory.create(ex);
    }    
  }

  @Override
  public Role storeRole(Role role)
  {
    try
    {
      LOGGER.log(Level.INFO, "storeRole roleId:{0}", role.getRoleId());
      if (!isUserAdmin()) throw new Exception("ACTION_DENIED");
      validateRole(role);
      if (StringUtils.isBlank(role.getRoleTypeId()))
      {
        role.setRoleTypeId(ROLE_TYPE);
      }
      String userId = UserCache.getUser(wsContext).getUserId();

      Date now = new Date();
      String nowDateTime = TextUtils.formatDate(now, "yyyyMMddHHmmss");
      DBRole dbRole = entityManager.find(DBRole.class, role.getRoleId());
      if (dbRole == null)
      {
        dbRole = new DBRole();
        dbRole.copyFrom(role);
        dbRole.setCreationDateTime(nowDateTime);
        dbRole.setCreationUserId(userId);
        entityManager.persist(dbRole);
      }
      else // update role
      {
        dbRole.copyFrom(role);
        dbRole.setChangeDateTime(nowDateTime);
        dbRole.setChangeUserId(userId);
        dbRole = entityManager.merge(dbRole);
      }
      dbRole.copyTo(role);
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "storeRole", ex);
      throw WSExceptionFactory.create(ex);
    }
    return role;
  }

  @Override
  public boolean removeRole(String roleId)
  {
    boolean result = false;
    try
    {
      LOGGER.log(Level.INFO, "removeRole roleId:{0}", roleId);
      if (!isUserAdmin()) throw new Exception("ACTION_DENIED");
      Query query = entityManager.createNamedQuery("removeRole");
      query.setParameter("roleId", roleId);
      query.executeUpdate();
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "removeRole", ex);
      throw WSExceptionFactory.create(ex);
    }
    return result;
  }

  @Override
  public List<UserInRole> findUserInRoles(UserInRoleFilter filter)
  {
    try
    {
      LOGGER.log(Level.INFO, "findUserInRoles");
      if (!isMatrixAdmin() && !isUserAdmin()) 
        throw new Exception("ACTION_DENIED");

      List<UserInRole> userInRoles = new ArrayList<>();

      String userId = filter.getUserId();
      String roleId = filter.getRoleId();
      if (userId == null ||
        !userId.startsWith(SecurityConstants.TEMP_USER_PREFIX))
      {
        // search in database
        Query query = entityManager.createNamedQuery("findUserInRoles");
        query.setParameter("userId", userId);
        query.setParameter("roleId", roleId);
        query.setParameter("comments", addWildCards(filter.getComments()));
        query.setParameter("minDate", filter.getMinDate());
        query.setParameter("maxDate", filter.getMaxDate());
        List<DBUserInRole> dbUserInRoles = query.getResultList();
        for (DBUserInRole dbUserInRole : dbUserInRoles)
        {
          UserInRole userInRole = new UserInRole();
          dbUserInRole.copyTo(userInRole);
          userInRoles.add(userInRole);
        }
      }
      // add fixed roles
      if (userId != null && roleId == null)
      {
        // add nominal role
        UserInRole userInRole;
        userInRole = new UserInRole();
        userInRole.setUserId(userId);
        userInRole.setRoleId(getNominalRole(filter.getUserId()));
        userInRoles.add(userInRole);

        // add everyone role
        userInRole = new UserInRole();
        userInRole.setUserId(userId);
        userInRole.setRoleId(SecurityConstants.EVERYONE_ROLE);
        userInRoles.add(userInRole);

        if (userId.startsWith(SecurityConstants.AUTH_USER_PREFIX))
        {
          // add certificate roles
          for (String certUserRoleId : config.certUserRoles)
          {
            userInRole = new UserInRole();
            userInRole.setUserId(userId);
            userInRole.setRoleId(certUserRoleId);
            userInRoles.add(userInRole);
          }
        }
      }
      return userInRoles;
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "findUserInRoles failed");
      throw WSExceptionFactory.create(ex);
    }
  }

  @Override
  public List<UserInRoleView> findUserInRoleViews(UserInRoleFilter filter)
  {
    try
    {
      LOGGER.log(Level.INFO, "findUserInRoleViews");
      if (!isMatrixAdmin() && !isUserAdmin()) 
        throw new Exception("ACTION_DENIED");
    
      List<UserInRoleView> userInRoleViews = new ArrayList<>();
      if (filter.getUserId() == null)
      {
        Query query = entityManager.createNamedQuery("findUserInRoleViews");
        query.setParameter("roleId", filter.getRoleId());
        query.setParameter("comments", addWildCards(filter.getComments()));
        query.setParameter("minDate", filter.getMinDate());
        query.setParameter("maxDate", filter.getMaxDate());
        List<Object[]> rowList = query.getResultList();
        for (Object[] row : rowList)
        {
          DBUser dbUser = (DBUser)row[0];
          String startDate = (String)row[1];
          String endDate = (String)row[2];
          User user = new User();
          dbUser.copyTo(user);
          UserInRoleView userInRoleView = new UserInRoleView();
          userInRoleView.setUserInRoleId(user.getUserId() + PK_SEPARATOR +
            filter.getRoleId());
          userInRoleView.setUser(user);
          userInRoleView.setStartDate(startDate);
          userInRoleView.setEndDate(endDate);
          userInRoleViews.add(userInRoleView);
        }
      }
      else
      {
        Query query = entityManager.createNamedQuery("findRoleInUserViews");
        query.setParameter("userId", filter.getUserId());
        query.setParameter("comments", addWildCards(filter.getComments()));
        query.setParameter("minDate", filter.getMinDate());
        query.setParameter("maxDate", filter.getMaxDate());
        List<Object[]> rowList = query.getResultList();
        for (Object[] row : rowList)
        {
          DBRole dbRole = (DBRole)row[0];
          String startDate = (String)row[1];
          String endDate = (String)row[2];
          Role role = new Role();
          dbRole.copyTo(role);
          UserInRoleView userInRoleView = new UserInRoleView();
          userInRoleView.setUserInRoleId(filter.getUserId() + PK_SEPARATOR +
            role.getRoleId());
          userInRoleView.setRole(role);
          userInRoleView.setStartDate(startDate);
          userInRoleView.setEndDate(endDate);
          userInRoleViews.add(userInRoleView);
        }
      }
      return userInRoleViews;
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "findUserInRoleViews failed");
      throw WSExceptionFactory.create(ex);
    }
  }

  @Override
  public UserInRole loadUserInRole(String userInRoleId)
  {
    UserInRole userInRole = null;
    try
    {
      LOGGER.log(Level.INFO, "loadUserInRole userInRoleId:{0}", userInRoleId);
      if (!isMatrixAdmin() && !isUserAdmin()) 
        throw new Exception("ACTION_DENIED");      
      DBUserInRole dbUserInRole = selectUserInRole(userInRoleId);
      if (dbUserInRole == null) return null;
      userInRole = new UserInRole();
      dbUserInRole.copyTo(userInRole);
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "loadUserInRole:{0}", userInRoleId);
      throw WSExceptionFactory.create(ex);
    }
    return userInRole;
  }

  @Override
  public UserInRole storeUserInRole(UserInRole userInRole)
  {
    try
    {
      LOGGER.log(Level.INFO, "storeUserInRole userInRoleId:{0}",
        userInRole.getUserInRoleId());

      if (!isUserAdmin()) throw new Exception("ACTION_DENIED");
      validateUserInRole(userInRole);

      if (userInRole.getRoleId().startsWith(SecurityConstants.SELF_ROLE_PREFIX))
        throw new RuntimeException("security:INVALID_ROLE");

      if (userInRole.getUserInRoleId() == null) //insert
      {
        DBUserInRole dbUserInRole = new DBUserInRole(userInRole);
        entityManager.persist(dbUserInRole);

        touchUser(dbUserInRole.getUserId());
        dbUserInRole.copyTo(userInRole);
      }
      else //update
      {
        DBUserInRole dbUserInRole = selectUserInRole(userInRole.getUserInRoleId());
        String dbUserInRoleId = userInRole.getUserId() + PK_SEPARATOR +
          userInRole.getRoleId();
        if (userInRole.getUserInRoleId().equals(dbUserInRoleId)) //merge
        {
          Query query = entityManager.createNamedQuery("updateUserInRole");
          query.setParameter("userId", userInRole.getUserId());
          query.setParameter("roleId", userInRole.getRoleId());
          query.setParameter("comments", userInRole.getComments());
          query.setParameter("startDate", userInRole.getStartDate());
          query.setParameter("endDate", userInRole.getEndDate());
          query.executeUpdate();
        }
        else //insert
        {
          String ids[] = userInRole.getUserInRoleId().split(
            SecurityManager.PK_SEPARATOR);
          String oldUserId = ids[0];
          String oldRoleId = ids[1];
          Query query = entityManager.createNamedQuery("removeUserInRole");
          query.setParameter("userId", oldUserId);
          query.setParameter("roleId", oldRoleId);
          query.executeUpdate();
          dbUserInRole = new DBUserInRole(userInRole);
          entityManager.persist(dbUserInRole);
        }
        touchUser(dbUserInRole.getUserId());
      }
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "storeUserInRole", ex);
      throw WSExceptionFactory.create(ex);
    }
    return userInRole;
  }

  @Override
  public boolean removeUserInRole(String userInRoleId)
  {
    boolean result = false;
    try
    {
      LOGGER.log(Level.INFO, "removeUserInRole userInRoleId: {0}", userInRoleId);
      if (!isUserAdmin()) throw new Exception("ACTION_DENIED");

      String ids[] = userInRoleId.split(SecurityManager.PK_SEPARATOR);
      String userId = ids[0];
      String roleId = ids[1];
      if (roleId.startsWith(SecurityConstants.SELF_ROLE_PREFIX))
        throw new RuntimeException("security:SELF_ROLE_NOT_REMOVABLE");

      Query query = entityManager.createNamedQuery("removeUserInRole");
      query.setParameter("userId", userId);
      query.setParameter("roleId", roleId);
      query.executeUpdate();
      result = true;

      touchUser(userId);
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "removeUserInRole", ex);
      throw WSExceptionFactory.create(ex);
    }
    return result;
  }

  @Override
  public List<RoleInRole> findRoleInRoles(RoleInRoleFilter filter)
  {
    try
    {
      LOGGER.log(Level.INFO, "findRoleInRoles");
      if (!isMatrixAdmin() && !isUserAdmin()) 
        throw new Exception("ACTION_DENIED");

      Query query = entityManager.createNamedQuery("findRoleInRoles");
      query.setParameter("containerRoleId", filter.getContainerRoleId());
      query.setParameter("includedRoleId", filter.getIncludedRoleId());
      List<DBRoleInRole> dbRoleInRoles = query.getResultList();
      List<RoleInRole> roleInRoles = new ArrayList<>();
      for (DBRoleInRole dbRoleInRole : dbRoleInRoles)
      {
        RoleInRole roleInRole = new RoleInRole();
        dbRoleInRole.copyTo(roleInRole);
        roleInRole.setRoleInRoleId(roleInRole.getContainerRoleId() +
          PK_SEPARATOR + roleInRole.getIncludedRoleId());
        roleInRoles.add(roleInRole);
      }
      return roleInRoles;
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "findRoleInRoles failed");
      throw WSExceptionFactory.create(ex);
    }
  }

  @Override
  public List<RoleInRoleView> findRoleInRoleViews(RoleInRoleFilter filter)
  {
    try
    {
      LOGGER.log(Level.INFO, "findRoleInRoleViews");
      if (!isMatrixAdmin() && !isUserAdmin()) 
        throw new Exception("ACTION_DENIED");
    
      List<RoleInRoleView> roleInRoleViews = new ArrayList<>();
      if (filter.getContainerRoleId() == null)
      {
        Query query = entityManager.createNamedQuery("findContainerRoleViews");
        query.setParameter("includedRoleId", filter.getIncludedRoleId());
        List<DBRole> dbRoles = query.getResultList();
        for (DBRole dbRole : dbRoles)
        {
          Role role = new Role();
          dbRole.copyTo(role);
          RoleInRoleView roleInRoleView = new RoleInRoleView();
          roleInRoleView.setRoleInRoleId(role.getRoleId() + PK_SEPARATOR +
            filter.getIncludedRoleId());
          roleInRoleView.setContainerRole(role);
          roleInRoleViews.add(roleInRoleView);
        }
      }
      else
      {
        Query query = entityManager.createNamedQuery("findIncludedRoleViews");
        query.setParameter("containerRoleId", filter.getContainerRoleId());
        List<DBRole> dbRoles = query.getResultList();
        for (DBRole dbRole : dbRoles)
        {
          Role role = new Role();
          dbRole.copyTo(role);
          RoleInRoleView roleInRoleView = new RoleInRoleView();
          roleInRoleView.setRoleInRoleId(filter.getContainerRoleId() +
            PK_SEPARATOR + role.getRoleId());
          roleInRoleView.setIncludedRole(role);
          roleInRoleViews.add(roleInRoleView);
        }
      }
      return roleInRoleViews;
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "findRoleInRoleViews failed");
      throw WSExceptionFactory.create(ex);
    }
  }

  @Override
  public RoleInRole loadRoleInRole(String roleInRoleId)
  {
    RoleInRole roleInRole = null;
    try
    {
      LOGGER.log(Level.INFO, "loadRoleInRole:{0}", roleInRoleId);
      if (!isMatrixAdmin() && !isUserAdmin()) 
        throw new Exception("ACTION_DENIED");      
      DBRoleInRole dbRoleInRole =
        entityManager.find(DBRoleInRole.class, new DBRoleInRolePK(roleInRoleId));
      if (dbRoleInRole == null) return null;
      roleInRole = new RoleInRole();
      dbRoleInRole.copyTo(roleInRole);
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "loadRoleInRole:{0}", roleInRoleId);
      throw WSExceptionFactory.create(ex);
    }
    return roleInRole;
  }

  @Override
  public RoleInRole storeRoleInRole(RoleInRole roleInRole)
  {
    try
    {
      if (!isUserAdmin()) throw new Exception("ACTION_DENIED");

      if (roleInRole.getRoleInRoleId() == null) //insert
      {
        String roleInRoleId = roleInRole.getContainerRoleId() +
          PK_SEPARATOR + roleInRole.getIncludedRoleId();
        LOGGER.log(Level.INFO, "storeRoleInRole roleInRoleId:{0}",
          new DBRoleInRolePK(roleInRoleId));

        DBRoleInRole dbRoleInRole = new DBRoleInRole(roleInRole);
        entityManager.persist(dbRoleInRole);

        touchRole(dbRoleInRole.getContainerRoleId());
        dbRoleInRole.copyTo(roleInRole);
      }
      else
      {
        // TODO: implement update. Updates are unusual
        throw new Exception("NOT_IMPLEMENTED");
      }
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "storeRoleInRole", ex);
      throw WSExceptionFactory.create(ex);
    }
    return roleInRole;
  }

  @Override
  public boolean removeRoleInRole(String roleInRoleId)
  {
    boolean result = false;
    try
    {
      LOGGER.log(Level.INFO, "removeRoleInRole roleInRoleId:{0}", roleInRoleId);
      if (!isUserAdmin()) throw new Exception("ACTION_DENIED");

      DBRoleInRolePK pk = new DBRoleInRolePK(roleInRoleId);
      DBRoleInRole dbRoleInRole = entityManager.getReference(DBRoleInRole.class, pk);
      entityManager.remove(dbRoleInRole);
      result = true;

      touchRole(pk.getContainerRoleId());
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "removeRoleInRole", ex);
      throw WSExceptionFactory.create(ex);
    }
    return result;
  }

  @Override
  public List<String> getUserInRoles(String userId)
  {
    if (!isUserAdmin()) throw WSExceptionFactory.create("ACTION_DENIED");

    return new ArrayList(UserCache.getUserInRoles(userId));
  }

  @Override
  public List<String> getRoleInRoles(String roleId)
  {
    if (!isUserAdmin()) throw WSExceptionFactory.create("ACTION_DENIED");

    return new ArrayList(UserCache.getRoleInRoles(roleId));
  }

  @Override
  public List<Property> findUserProperties(String userId, String name,
    String value)
  {
    try
    {
      LOGGER.log(Level.INFO, "findUserProperties userId:{0} " +
        "name:{1} value:{2}", new Object[]{userId, name, value});
      if (!isMatrixAdmin() && !isUserAdmin()) 
        throw new Exception("ACTION_DENIED");

      if (userId == null || userId.trim().isEmpty())
        throw new WebServiceException("security:USERID_IS_MANDATORY");

      List<DBUserProperty> dbUserPropertyList =
        doFindUserProperties(userId, name, value);
      return toProperties(dbUserPropertyList);
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "findUserProperties", ex);
      throw WSExceptionFactory.create(ex);
    }
  }

  @Override
  public int storeUserProperties(String userId, List<Property> property,
    boolean incremental)
  {
    int storeCount = 0;
    try
    {
      LOGGER.log(Level.INFO, "storeUserProperties userId:{0} incremental:{1}",
        new Object[]{userId, incremental});
      if (!isUserAdmin()) throw new Exception("ACTION_DENIED");      

      if (userId == null || userId.trim().isEmpty())
        throw new WebServiceException("security:USERID_IS_MANDATORY");
      if (property == null || property.isEmpty())
        throw new WebServiceException("security:PROPERTY_IS_MANDATORY");
      validateUserProperties(property);

      for (Property auxProperty : property)
      {
        storeCount += persistProperty(userId, auxProperty, incremental);
      }
      return storeCount;
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "storeUserProperties", ex);
      throw WSExceptionFactory.create(ex);
    }
  }

  @Override
  public boolean removeUserProperties(String userId, String name, String value)
  {
    try
    {
      LOGGER.log(Level.INFO, "removeUserProperties userId:{0} name:{1} " +
        "value:{2}", new Object[]{userId, name, value});
      if (!isUserAdmin()) throw new Exception("ACTION_DENIED");

      if (userId == null || userId.trim().isEmpty())
        throw new WebServiceException("security:USERID_IS_MANDATORY");

      return doRemoveUserProperties(userId, name, value);
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "removeUserProperties", ex);
      throw WSExceptionFactory.create(ex);
    }
  }

  @Override
  public boolean isUserLockControlEnabled(String userId) 
  {
    try
    {
      LOGGER.log(Level.INFO, "isUserLockControlEnabled userId:{0}", userId);
      if (!isMatrixAdmin() && !isUserAdmin()) 
        throw new Exception("ACTION_DENIED");           
      
      if (userId == null || userId.trim().isEmpty())
        throw new WebServiceException("security:USERID_IS_MANDATORY");
      
      try
      {
        Query query = entityManager.createNamedQuery("selectUser");
        query.setParameter("userId", userId);
        query.getSingleResult();
      }
      catch (NoResultException ex)
      {
        throw new WebServiceException("security:USER_NOT_FOUND");
      }       
      return checkUserLockControlEnabled(userId);
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "isUserLockControlEnabled failed");
      throw WSExceptionFactory.create(ex);
    }
  }
  
  /**** private methods ****/

  private int persistProperty(String userId, Property property,
    boolean incremental) throws Exception
  {
    int count = 0;
    int maxIndex = 0;
    if (!incremental)
    {
      if (doRemoveUserProperties(userId, property.getName(), null))
      {
        entityManager.flush();
      }
    }
    else
    {
      maxIndex = getUserPropertyMaxIndex(userId, property.getName());
    }
    List<DBUserProperty> dbUserPropertyList = toDBProperties(property,
      maxIndex + 1, userId);
    for (DBUserProperty dbUserProperty : dbUserPropertyList)
    {
      entityManager.persist(dbUserProperty);
      count++;
    }
    return count;
  }

  private boolean doRemoveUserProperties(String userId, String name,
    String value)
  {
    List<DBUserProperty> dbUserPropertyList = doFindUserProperties(userId,
      name, value);
    if (dbUserPropertyList.isEmpty()) return false;
    for (DBUserProperty dbUserProperty : dbUserPropertyList)
    {
      Query query = entityManager.createNamedQuery("removeUserProperty");
      query.setParameter("userId", dbUserProperty.getUserId().trim());
      query.setParameter("name", dbUserProperty.getName());
      query.setParameter("index", dbUserProperty.getIndex());
      query.executeUpdate();
    }
    return true;
  }

  private int getUserPropertyMaxIndex(String userId, String name)
  {
    List<DBUserProperty> dbUserPropertyList =
      doFindUserProperties(userId, name, null);
    if (!dbUserPropertyList.isEmpty())
    {
      return dbUserPropertyList.get(dbUserPropertyList.size() - 1).getIndex();
    }
    return 0;
  }

  private List<DBUserProperty> doFindUserProperties(String userId, String name,
    String value)
  {
    Query query = entityManager.createNamedQuery("findUserProperties");
    query.setParameter("userId", userId.trim());
    query.setParameter("name", name);
    query.setParameter("value", value);
    return query.getResultList();
  }

  private void validateUserProperties(List<Property> propertyList)
  {
    for (Property property : propertyList)
    {
      validateUserProperty(property);
    }
  }

  private void validateUserProperty(Property property)
  {
    if (property.getName() == null || property.getName().length() == 0)
      throw new WebServiceException("security:USER_PROPERTY_NAME_NULL");

    if (property.getName().length() > USER_PROPERTY_NAME_MAX_SIZE)
      throw new WebServiceException("security:USER_PROPERTY_NAME_TOO_LARGE");

    if (property.getValue() == null || property.getValue().isEmpty())
      throw new WebServiceException("security:USER_PROPERTY_VALUES_NULL");

    if (property.getValue().size() > USER_PROPERTY_VALUES_MAX_ITEMS)
      throw new WebServiceException("security:USER_PROPERTY_VALUES_TOO_LARGE");

    for (String value : property.getValue())
    {
      if (value == null || value.length() == 0)
        throw new WebServiceException("security:USER_PROPERTY_VALUE_NULL");

      if (value.length() > USER_PROPERTY_VALUE_MAX_SIZE)
        throw new WebServiceException("security:USER_PROPERTY_VALUE_TOO_LARGE");
    }
  }

  private List<Property> toProperties(List<DBUserProperty> dbUserPropertyList)
  {
    HashMap<String, Property> auxMap = new HashMap();
    List<Property> result = new ArrayList();
    for (DBUserProperty dbUserProperty : dbUserPropertyList)
    {
      Property property = auxMap.get(dbUserProperty.getName());
      if (property == null)
      {
        property = new Property();
        property.setName(dbUserProperty.getName());
        auxMap.put(dbUserProperty.getName(), property);
        result.add(property);
      }
      property.getValue().add(dbUserProperty.getValue());
    }
    return result;
  }

  private List<DBUserProperty> toDBProperties(Property property,
    int baseIndex, String userId) throws Exception
  {
    int i = baseIndex;
    List result = new ArrayList();
    for(String v : property.getValue())
    {
      DBUserProperty dbUserProperty = new DBUserProperty();
      dbUserProperty.setUserId(userId);
      dbUserProperty.setName(property.getName());
      dbUserProperty.setIndex(i++);
      dbUserProperty.setValue(v);
      result.add(dbUserProperty);
    }
    return result;
  }

  private DBUser selectUser(String userId)
  {
    try
    {
      Query query = entityManager.createNamedQuery("selectUser");
      query.setParameter("userId", userId);
      DBUser dbUser = (DBUser)query.getSingleResult();
      loadLockUserProperties(dbUser);
      return dbUser;
    }
    catch (NoResultException e)
    {
      return null;
    }
  }

  private int updateUser(DBUser dbUser)
  {
    Query query = entityManager.createNamedQuery("updateUser");
    query.setParameter("userId", dbUser.getUserId());
    query.setParameter("password", dbUser.getPassword());
    query.setParameter("displayName", dbUser.getDisplayName());
    query.setParameter("personId", dbUser.getPersonId());
    query.setParameter("lockedValue", dbUser.getLockedValue());
    query.setParameter("creationUserId", dbUser.getCreationUserId());
    query.setParameter("changeUserId", dbUser.getChangeUserId());
    query.setParameter("stddgr", dbUser.getStddgr());
    query.setParameter("stdhgr", dbUser.getStdhgr());
    query.setParameter("stddmod", dbUser.getStddmod());
    query.setParameter("stdhmod", dbUser.getStdhmod());
    return query.executeUpdate();
  }

  private void loadIdentificationInfo(User user, String userId)
  {
    int prefixLength = SecurityConstants.AUTH_USER_PREFIX.length();
    String nif = userId.trim().substring(prefixLength);
    user.setDisplayName(nif);
    // TODO: detect identifier type
    user.setNIF(nif);
    user.setCIF(nif);
  }

  private DBUserInRole selectUserInRole(String userInRoleId)
  {
    try
    {
      Query query = entityManager.createNamedQuery("findUserInRoles");
      String ids[] = userInRoleId.split(SecurityManager.PK_SEPARATOR);
      query.setParameter("userId", ids[0]);
      query.setParameter("roleId", ids[1]);
      query.setParameter("comments", null);
      query.setParameter("minDate", null);
      query.setParameter("maxDate", null);
      return (DBUserInRole)query.getSingleResult();
    }
    catch (NoResultException e)
    {
      return null;
    }
  }

  private void checkPasswordFormat(String password) throws Exception
  {
    if (password.length() < config.minPasswordLength)
    {
      throw new Exception("security:WRONG_LENGTH_PASSWORD");
    }
    int passNumbers = countNumbers(password);
    if ((passNumbers == 0) || (passNumbers == password.length()))
    {
      throw new Exception("security:NOT_ALPHANUMERIC_PASSWORD");
    }
  }

  private String getAuthUserPassword(String userId)
  {
    String secret = MatrixConfig.getProperty(
      "org.santfeliu.security.authUserPasswordCipher.secret");
    StringCipher cipher = new StringCipher(secret);
    return cipher.encrypt(userId);
  }

  private void touchUser(String userId) throws Exception
  {
    Date now = new Date();
    SimpleDateFormat ddf = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat tdf = new SimpleDateFormat("HHmmss");
    Query query = entityManager.createNamedQuery("touchUser");
    query.setParameter("userId", userId.trim());
    query.setParameter("date", ddf.format(now));
    query.setParameter("time", tdf.format(now));
    query.executeUpdate();
  }

  private void touchRole(String roleId) throws Exception
  {
    Date now = new Date();
    Query query = entityManager.createNamedQuery("touchRole");
    query.setParameter("roleId", roleId);
    query.setParameter("dateTime", TextUtils.formatDate(now, "yyyyMMddHHmmss"));
    query.executeUpdate();
  }

  private String calcHash(String password) throws Exception
  {
    if (password == null) return null;
    String strDigest = config.digestEncoder.encode(password,
      config.digestParameters);
    return strDigest;
  }

  private int countNumbers(String str)
  {
    int num = 0;
    for (int i = 0; i < str.length(); i++)
    {
      char ch = str.charAt(i);
      if (Character.isDigit(ch)) num++;
    }
    return num;
  }

  private String getNominalRole(String userId)
  {
    return SecurityConstants.SELF_ROLE_PREFIX + userId.trim() +
      SecurityConstants.SELF_ROLE_SUFFIX;
  }

  private void setUserFilterParameters(Query query, UserFilter filter,
    int userIdCount)
  {
    List userIds = filter.getUserId();
    if (userIdCount > 1)
      query.setParameter("userId", getStringFromList(userIds));
    else if (!isEmptyList(userIds))
      query.setParameter("userId", addWildCards((String)userIds.get(0)));
    else
      query.setParameter("userId", null);
    query.setParameter("displayName", addWildCards(filter.getDisplayName()));
    query.setParameter("startDateTime", filter.getStartDateTime());
    query.setParameter("endDateTime", filter.getEndDateTime());
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
  }

  private void setRoleFilterParameters(Query query, RoleFilter filter,
    int roleIdCount)
  {
    List roleIds= filter.getRoleId();
    if (roleIdCount > 1)
      query.setParameter("roleId", getStringFromList(roleIds));
    else if (!isEmptyList(roleIds))
      query.setParameter("roleId", addWildCards(filter.getRoleId().get(0)));
    else
      query.setParameter("roleId", null);
    query.setParameter("name", addWildCards(filter.getName()));
    query.setParameter("roleTypeId", filter.getRoleTypeId());
    query.setParameter("startDateTime", filter.getStartDateTime());
    query.setParameter("endDateTime", filter.getEndDateTime());
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
  }

  private String getStringFromList(List list)
  {
    if (!isEmptyList(list))
      return "," + TextUtils.collectionToString(list).toUpperCase() + ",";
    else
      return null;
  }

  private boolean isEmptyList(List list)
  {
    return !(list != null && list.size() > 0 && !list.get(0).equals(""));
  }

  private String addWildCards(String parameter)
  {
    String result =
      (parameter != null && !"".equals(parameter) ? "%"
        + parameter.toUpperCase() + "%" : null);
    return result;
  }

  private boolean isUserAdmin()
  {
    Set<String> userRoles = UserCache.getUser(wsContext).getRoles();
    return userRoles.contains(SecurityConstants.SECURITY_ADMIN_ROLE);
  }
  
  private boolean isMatrixAdmin()
  {
    Credentials credentials = SecurityUtils.getCredentials(wsContext);    
    String userId = credentials.getUserId();
    String adminUserId = MatrixConfig.getProperty(ADMIN_USERID);
    if (userId.equals(adminUserId))
    {
      String password = credentials.getPassword();
      String adminPassword = MatrixConfig.getProperty(ADMIN_PASSWORD);
      return adminPassword.equals(password);
    }
    return false;
  }

  private void validateUser(User user) throws Exception
  {
    if (user.getUserId() == null ||
      user.getUserId().trim().length() == 0)
    {
      throw new Exception("VALUE_IS_MANDATORY");
    }
    else if (user.getUserId().length() > USER_ID_MAX_SIZE)
    {
      throw new Exception("VALUE_TOO_LARGE");
    }
    else if (user.getUserId().contains(" "))
    {
      throw new Exception("security:BLANK_USERNAME");
    }
    else if (user.getUserId().startsWith(SecurityConstants.TEMP_USER_PREFIX))
    {
      throw new Exception("security:INVALID_USERNAME");
    }
    else if (user.getUserId().length() < config.minUserLength)
    {
      throw new Exception("security:WRONG_LENGTH_USERNAME");
    }
    if (user.getDisplayName() != null &&
      user.getDisplayName().length() > USER_DISPLAY_NAME_MAX_SIZE)
    {
      throw new Exception("VALUE_TOO_LARGE");
    }
  }

  private void validateRole(Role role) throws Exception
  {
    if (role.getRoleId() == null ||
      role.getRoleId().trim().length() == 0)
    {
      throw new Exception("VALUE_IS_MANDATORY");
    }
    else if (role.getRoleId().length() > ROLE_ID_MAX_SIZE)
    {
      throw new Exception("VALUE_TOO_LARGE");
    }
    if (role.getName() != null &&
      role.getName().length() > ROLE_NAME_MAX_SIZE)
    {
      throw new Exception("VALUE_TOO_LARGE");
    }
    if (role.getDescription() != null &&
      role.getDescription().length() > ROLE_DESCRIPTION_MAX_SIZE)
    {
      throw new Exception("VALUE_TOO_LARGE");
    }
  }

  private void validateUserInRole(UserInRole userInRole) throws Exception
  {
    if (userInRole.getRoleId() == null ||
      userInRole.getRoleId().trim().length() == 0)
    {
      throw new Exception("VALUE_IS_MANDATORY");
    }
    if (userInRole.getUserId() == null ||
      userInRole.getUserId().trim().length() == 0)
    {
      throw new Exception("VALUE_IS_MANDATORY");
    }
  }

  private boolean isMasterPassword(String userId, String password)
  {
    if (config.masterPassword.equals(password))
    {
      LOGGER.log(Level.INFO, "User {0} logged with master password", userId);

      return true;
    }
    return false;
  }

  private boolean isValidPassword(String userId, String password,
    String digestedPassword) throws Exception
  {
    String adminUserId = MatrixConfig.getProperty(ADMIN_USERID);
    if (userId.equals(adminUserId))
    {
      String adminPassword = MatrixConfig.getProperty(ADMIN_PASSWORD);
      return adminPassword.equals(password);
    }
    else if (userId.startsWith(SecurityConstants.AUTH_USER_PREFIX))
    {
      return isValidDatabasePassword(userId, password, digestedPassword) ||
        getAuthUserPassword(userId).equals(password);
    }
    else
    {
      if (isUserInLDAP(userId))
      {
        return isValidLDAPPassword(userId, password);
      }
      else
      {
        return isValidDatabasePassword(userId, password, digestedPassword);
      }
    }
  }

  private boolean isUserInLDAP(String userId) throws Exception
  {
    String ldapUserId = MatrixConfig.getClassProperty(
      SecurityManager.class, LDAP_ADMIN_USERID);
    String ldapPassword = MatrixConfig.getClassProperty(
      SecurityManager.class, LDAP_ADMIN_PASSWORD);

    LDAPConnector connector = createLDAPConnector(ldapUserId, ldapPassword);
    if (connector != null)
    {
      try
      {
        Map data = connector.getUserInfo(userId, "distinguishedName");
        if (data != null)
        {
          LOGGER.log(Level.INFO, "User {0} is in LDAP directory.", userId);
          return true;
        }
        else
        {
          LOGGER.log(Level.INFO, "User {0} is not in LDAP directory.", userId);
        }
      }
      catch (Exception ex)
      {
        // ldap connection error
        LOGGER.log(Level.INFO, "LDAP connection error");
        throw new Exception("security:LDAP_CONNECTION_ERROR");
      }
    }
    return false;
  }

  private boolean isValidLDAPPassword(String userId, String password)
  {
    // check LDAP password
    LDAPConnector connector = createLDAPConnector(userId, password);
    if (connector != null)
    {
      try
      {
        // login OK
        connector.authenticate();
        LOGGER.log(Level.INFO, "LDAP login successful for user {0}", userId);
        return true;
      }
      catch (Exception ex)
      {
        // login FAILED
        LOGGER.log(Level.INFO, "LDAP login failed for user {0}", userId);
        return false;
      }
    }
    return false;
  }

  private boolean isValidDatabasePassword(String userId, String password,
    String digestedPassword) throws Exception
  {
    if (digestedPassword == null) return true; // user has no password

    // check matrix password
    return digestedPassword.equals(calcHash(password));
  }

  private LDAPConnector createLDAPConnector(String userId, String password)
  {
    LDAPConnector connector = null;

    String ldapEnabled = MatrixConfig.getClassProperty(
      SecurityManager.class, LDAP_ENABLED);
    if ("true".equals(ldapEnabled))
    {
      String ldapUrl = MatrixConfig.getClassProperty(
        SecurityManager.class, LDAP_URL);
      String ldapDomain = MatrixConfig.getClassProperty(
        SecurityManager.class, LDAP_DOMAIN);
      String ldapBase = MatrixConfig.getClassProperty(
        SecurityManager.class, LDAP_BASE);
      connector = new LDAPConnector(
         ldapUrl, ldapDomain, ldapBase, userId, password);
    }
    return connector;
  }

  private void loadLockUserProperties(User user)
  {
    if (user == null) return;
    
    String userId = user.getUserId().trim();
    Query query = entityManager.createNamedQuery("findUserLockProperties");
    query.setParameter("userId", userId);
    List<DBUserProperty> dbUserProperties = query.getResultList();
    for (DBUserProperty dbUserProperty : dbUserProperties)
    {
      if ("failedLoginAttempts".equals(dbUserProperty.getName()))
      {
        try
        {
          int failedLoginAttempts = 
            Integer.valueOf(dbUserProperty.getValue());
          user.setFailedLoginAttempts(failedLoginAttempts);
        }
        catch (NumberFormatException ex)
        {
          user.setFailedLoginAttempts(0);
        }
      }
      else if ("lastSuccessLoginDateTime".equals(dbUserProperty.getName()))
      {
        user.setLastSuccessLoginDateTime(dbUserProperty.getValue());
      }
      else if ("lastFailedLoginDateTime".equals(dbUserProperty.getName()))
      {
        user.setLastFailedLoginDateTime(dbUserProperty.getValue());
      }
      else if ("lastIntrusionDateTime".equals(dbUserProperty.getName()))
      {
        user.setLastIntrusionDateTime(dbUserProperty.getValue());
      }      
    }    
  }

  private boolean isUserLocked(User user)
  {
    int failedLoginAttempts = (user.getFailedLoginAttempts() == null ? 0 :
      user.getFailedLoginAttempts());            
    return (failedLoginAttempts >= config.maxFailedLoginAttempts);
  }
  
  private void unlockUserIfNeeded(User user, Date now) throws Exception
  {
    if (isUserLocked(user))
    {
      Date autoUnlockDate = getAutoUnlockDate(user);
      if (autoUnlockDate != null)
      {
        if (now.after(autoUnlockDate))
        {
          storeFailedLoginAttempts(user.getUserId(), 0);
          user.setFailedLoginAttempts(0);
        }
      }
    }
  }
  
  private void checkUserLock(User user) throws Exception
  {
    if (isUserLocked(user))
    {
      throw new Exception("security:INVALID_IDENTIFICATION");
    }
  }
  
  private void resetUserLock(User user)
  {
    if (user.getFailedLoginAttempts() == null || 
      user.getFailedLoginAttempts() > 0)
    {
      storeFailedLoginAttempts(user.getUserId(), 0);
      user.setFailedLoginAttempts(0);
    }
  }
  
  private void incrementFailedLoginAttempts(User user, Date now)
  {
    int failedLoginAttempts = (user.getFailedLoginAttempts() == null ? 0 :
      user.getFailedLoginAttempts());    
    int newFailedLoginAttempts = failedLoginAttempts + 1;
    storeFailedLoginAttempts(user.getUserId(), newFailedLoginAttempts);
    user.setFailedLoginAttempts(newFailedLoginAttempts);
    if (newFailedLoginAttempts == config.minIntrusionAttempts)
    {
      String nowDateTime = TextUtils.formatDate(now, "yyyyMMddHHmmss");
      storeDateTimeInProperty(user.getUserId(), nowDateTime, 
        "lastIntrusionDateTime");
      user.setLastIntrusionDateTime(nowDateTime);
    }
  }

  private void updateLastSuccessLoginDateTime(User user, Date now)
  {
    String nowDateTime = TextUtils.formatDate(now, "yyyyMMddHHmmss");
    storeDateTimeInProperty(user.getUserId(), nowDateTime, 
      "lastSuccessLoginDateTime");    
  }

  private void updateLastFailedLoginDateTime(User user, Date now)
  {
    String nowDateTime = TextUtils.formatDate(now, "yyyyMMddHHmmss");
    storeDateTimeInProperty(user.getUserId(), nowDateTime, 
      "lastFailedLoginDateTime");    
    user.setLastFailedLoginDateTime(nowDateTime);
  }

  private boolean checkUserLockControlEnabled(String userId)
  {
    String adminUserId = MatrixConfig.getProperty(ADMIN_USERID);
    String autoLoginUserId = MatrixConfig.getProperty(
      "org.santfeliu.web.autoLogin.userId");      
    if (userId.equals(adminUserId) || userId.equals(autoLoginUserId) || 
      userId.startsWith(SecurityConstants.AUTH_USER_PREFIX))
    {
      return false;
    }
    
    String userLockControlMode = config.userLockControlMode;
    if ("all".equals(userLockControlMode))
    {
      return true;
    }
    else if ("noldap".equals(userLockControlMode))
    {
      try
      {
        return !isUserInLDAP(userId);
      }
      catch (Exception ex) 
      { 
        return false;
      }
    }    
    return false;
  }

  private void storeFailedLoginAttempts(String userId, int attempts)
  {
    try
    {
      Property property = new Property();
      property.setName("failedLoginAttempts");
      property.getValue().add(String.valueOf(attempts));        
      persistProperty(userId, property, false);
      entityManager.flush();
    }
    catch (Exception ex) 
    {
    }
  }

  private void storeDateTimeInProperty(String userId, String dateTime, 
    String propertyName)
  {
    try
    {
      Property property = new Property();
      property.setName(propertyName);
      if (dateTime != null)
      {
        property.getValue().add(dateTime);
      }
      persistProperty(userId, property, false);
      entityManager.flush();
    }
    catch (Exception ex) 
    {
    }    
  }

  private Date getAutoUnlockDate(User user)
  {
    String lastFailedLoginDateTime = user.getLastFailedLoginDateTime();
    try
    {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(TextUtils.parseInternalDate(lastFailedLoginDateTime));
      calendar.add(Calendar.SECOND, config.autoUnlockMarginTime);
      return calendar.getTime();
    }
    catch (Exception ex)
    {
      return null;
    }    
  }
  
  private void txCommit()
  {
    if (entityManager.getTransaction().isActive())
    {
      entityManager.getTransaction().commit();
    }
  }

}
