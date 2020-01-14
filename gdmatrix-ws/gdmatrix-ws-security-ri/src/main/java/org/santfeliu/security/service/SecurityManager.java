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
import static org.matrix.dic.DictionaryConstants.ROLE_TYPE;
import org.matrix.security.*;

import org.santfeliu.jpa.JPA;
import org.santfeliu.security.SecurityProvider;
import org.santfeliu.security.UserCache;
import org.santfeliu.security.encoder.DigestEncoder;
import org.santfeliu.security.util.LDAPConnector;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;
import org.santfeliu.ws.WSExceptionFactory;


/**
 *
 * @author unknown
 */
@WebService(endpointInterface="org.matrix.security.SecurityManagerPort")
@HandlerChain(file="handlers.xml")
@JPA
public class SecurityManager implements SecurityManagerPort
{
  @Resource
  WebServiceContext wsContext;

  protected static final Logger log = Logger.getLogger("Security");

  @PersistenceContext
  public EntityManager em;
  
  
  // username and password formats
  public static final int USERNAME_LENGTH = 20;
  
  // matrix config properties
  public static final String DIGEST_PARAMETERS = "digestParameters";
  public static final String DIGEST_ENCODER = "digestEncoder";
  public static final String MASTER_PASSWORD = "masterPassword";
  public static final String CERT_USER_ROLES = "certUserRoles";
  public static final String VALIDATE_CERTIFICATE = "validateCertificate";
  public static final String USER_LENGTH = "userLength";
  public static final String PASSWORD_LENGTH = "passwordLength";

  public static final String LDAP_ENABLED = "ldap.enabled";
  public static final String LDAP_URL = "ldap.url";
  public static final String LDAP_DOMAIN = "ldap.domain";
  public static final String LDAP_BASE = "ldap.base";
  public static final String LDAP_ADMIN_USERID = "ldap.adminUserId";
  public static final String LDAP_ADMIN_PASSWORD = "ldap.adminPassword";

  public static final String PK_SEPARATOR = ";";

  private static final int ROLE_DESCRIPTION_MAX_SIZE = 400;
  private static final int ROLE_ID_MAX_SIZE = 20;
  private static final int ROLE_NAME_MAX_SIZE = 100;
  private static final int USER_DISPLAY_NAME_MAX_SIZE = 20;
  private static final int USER_ID_MAX_SIZE = 20;

  private static String masterPassword;
  private static Set<String> certUserRoles;
  private static boolean validateCertificate;
  private static DigestEncoder digestEncoder;
  private static String digestParameters;
  private static int userLength;
  private static int passwordLength;
  
  static
  {
    // initialization
    try
    {
      String base = SecurityManager.class.getName() + ".";
      String digestEncodeClass =
        MatrixConfig.getProperty(base + DIGEST_ENCODER);
      digestEncoder =
        (DigestEncoder)Class.forName(digestEncodeClass).newInstance();
      digestParameters =
        MatrixConfig.getProperty(base + DIGEST_PARAMETERS);
      masterPassword =
        MatrixConfig.getProperty(base + MASTER_PASSWORD);
      validateCertificate = "true".equals(
        MatrixConfig.getProperty(base + VALIDATE_CERTIFICATE));
      String certUserRolesString =
        MatrixConfig.getProperty(base + CERT_USER_ROLES);
      String roles[] = certUserRolesString.split(",");
      certUserRoles = new HashSet<String>();
      for (String role : roles)
        certUserRoles.add(role.trim());
      userLength = Integer.parseInt(
        MatrixConfig.getProperty(base + USER_LENGTH));
      passwordLength = Integer.parseInt(
        MatrixConfig.getProperty(base + PASSWORD_LENGTH));
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "SecurityManager init failed", ex);
      throw new RuntimeException(ex);
    }
  }

  @Override
  public SecurityMetaData getSecurityMetaData()
  {
    SecurityMetaData metaData = new SecurityMetaData();
    metaData.setRoleDescriptionMaxSize(ROLE_DESCRIPTION_MAX_SIZE);
    metaData.setRoleIdMaxSize(ROLE_ID_MAX_SIZE);
    metaData.setRoleNameMaxSize(ROLE_NAME_MAX_SIZE);
    metaData.setUserDisplayNameMaxSize(USER_DISPLAY_NAME_MAX_SIZE);
    metaData.setUserIdMaxSize(USER_ID_MAX_SIZE);
    return metaData;
  }

  @Override
  public List<User> findUsers(UserFilter filter)
  {
    int userCount = (filter.getUserId() != null ? filter.getUserId().size() : 0);
    Query query = null;
    if (userCount > 1)
      query = em.createNamedQuery("findUsersMultipleId");
    else
      query = em.createNamedQuery("findUsersSingleId");
    setUserFilterParameters(query, filter, userCount);
    List<DBUser> dbUsers = query.getResultList();
    List<User> users = new ArrayList<User>();
    for (DBUser dbUser : dbUsers)
    {
      User user = new User();
      dbUser.copyTo(user);
      users.add(user);
    }
    return users;
  }

  @Override
  public int countUsers(UserFilter filter)
  {
    int userCount = (filter.getUserId() != null ? filter.getUserId().size() : 0);
    Query query = null;
    if (userCount > 1)
      query = em.createNamedQuery("countUsersMultipleId");
    else
      query = em.createNamedQuery("countUsersSingleId");
    setUserFilterParameters(query, filter, userCount);
    query.setFirstResult(0);
    query.setMaxResults(1);
    Number count = (Number)query.getSingleResult();
    return count.intValue();
  }
  
  @Override
  public User loadUser(String userId)
  {
    log.log(Level.INFO, "loadUser userId:{0}", userId);

    // checkRoles
    DBUser dbUser = selectUser(userId);
    if (dbUser == null)
      throw new WebServiceException("security:USER_NOT_FOUND");

    User user = new User();
    dbUser.copyTo(user);
    return user;
  }

  @Override
  public User storeUser(User user)
  {
    try
    {
      log.log(Level.INFO, "storeUser userId:{0}", user.getUserId());
      if (!isUserAdmin()) throw new Exception("ACTION_DENIED");
      validateUser(user);

      // check user data
      String userId = user.getUserId().trim();
      String password = user.getPassword();
      if (password != null)
      {
        password = password.trim();
        checkPasswordFormat(password);
      }
      String personId = user.getPersonId();
      if (StringUtils.isBlank(personId))
      {
        personId = 
          MatrixConfig.getProperty("SecurityManager.defaultPersonId");
        if (personId == null) personId = "0";
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
        em.persist(dbUser);
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
      dbUser.copyTo(user);
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "storeUser", ex);
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
      log.log(Level.INFO, "removeUser userId:{0}", userId);
      if (!isUserAdmin()) throw new Exception("ACTION_DENIED");

      Query query = em.createNamedQuery("removeUserInRole");
      query.setParameter("userId", userId);
      query.setParameter("roleId", null);
      query.executeUpdate();
      
      query = em.createNamedQuery("removeUser");
      query.setParameter("userId", userId);
      query.executeUpdate();
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "removeUser", ex);
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
      log.log(Level.INFO, "login userId:{0}", userId);
      if (userId == null || userId.trim().length() == 0)
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
        DBUser dbUser = selectUser(userId);
        if (dbUser != null)
        {
          // check user locked
          if (dbUser.getLockedValue() == 1)
          {
            throw new Exception("security:LOCKED_USER");
          }
          
          // persistent user
          if (isValidPassword(userId, password, dbUser.getPassword()))
          {
            user = new User();
            dbUser.copyTo(user);
            user.setPassword(password);
            if (userId.startsWith(SecurityConstants.AUTH_USER_PREFIX))
            {
              // registered certificate user: #NUMBER
              loadIdentificationInfo(user, userId);
            }
          }
          else throw new Exception("security:INVALID_PASSWORD");
        }
        else if (userId.startsWith(SecurityConstants.AUTH_USER_PREFIX))
        {
          // unregistered certificate user: #NUMBER
          if (password.equals(masterPassword))
          {
            user = new User();
            user.setUserId(userId.trim());
            user.setPassword(password);
            loadIdentificationInfo(user, userId);
          }
          else throw new Exception("security:INVALID_PASSWORD");
        }
        else throw new Exception("security:INVALID_USERNAME");
      }
    }
    catch (Exception ex)
    {
      log.log(Level.WARNING, "login", ex);
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
      log.log(Level.INFO, "loginCertificate");
      String userId = null;
      String password = null;
      String displayName = null;
      String givenName = null;
      String surname = null;
      String NIF = null;
      String CIF = null;
      String organizationName = null;
      String email = null;

      if (validateCertificate) // use Certificate validation service
      {
        Map attributes = new HashMap();
        boolean valid = false;

        SecurityProvider provider = SecurityUtils.getSecurityProvider();
        valid = provider.validateCertificate(certData, attributes);

        if (!valid) throw new Exception("INVALID_CERTIFICATE");

        NIF = (String)attributes.get(SecurityProvider.NIF);
        CIF = (String)attributes.get(SecurityProvider.CIF);

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

        password = getMasterPassword();
        displayName = (String)attributes.get(SecurityProvider.COMMON_NAME);
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

        displayName = (String)attributes.get("CN");
        givenName = (String)attributes.get("GIVENNAME");
        surname = (String)attributes.get("SURNAME");
        email = (String)attributes.get("SAN-1");
        organizationName = (String)attributes.get("O");

        userId = SecurityConstants.AUTH_USER_PREFIX + NIF;
        password = getMasterPassword();
      }
      user = new User();
      user.setUserId(userId.trim());
      user.setPassword(password);
      user.setDisplayName(displayName);
      user.setGivenName(givenName);
      user.setSurname(surname);
      user.setNIF(NIF);
      user.setCIF(CIF);
      user.setOrganizationName(organizationName);
      user.setEmail(email);
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "loginCertificate", ex);
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
      log.log(Level.INFO, "changePassword userId:{0}", userId);
      DBUser dbUser = selectUser(userId);
      if (dbUser != null)
      {
        if (isUserInLDAP(userId))
          throw new Exception("NOT_IMPLEMENTED");

        if (isValidMatrixPassword(userId, oldPassword, dbUser.getPassword()))
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
        else throw new Exception("security:INVALID_PASSWORD");
      }
      else throw new Exception("security:INVALID_USERNAME");
    }
    catch (Exception ex)
    {
      log.log(Level.WARNING, "changePassword", ex);
      throw WSExceptionFactory.create(ex);
    }
    return result;
  }

  @Override
  public List<Role> findRoles(RoleFilter filter)
  {
    int roleCount = (filter.getRoleId() != null ? filter.getRoleId().size() : 0);
    Query query = null;
    if (roleCount > 1)
      query = em.createNamedQuery("findRolesMultipleId");
    else
      query = em.createNamedQuery("findRolesSingleId");
    setRoleFilterParameters(query, filter, roleCount);
    List<DBRole> dbRoles = query.getResultList();
    List<Role> roles = new ArrayList<Role>();
    for (DBRole dbRole : dbRoles)
    {
      Role role = new Role();
      dbRole.copyTo(role);
      roles.add(role);
    }
    return roles;
  }

  @Override
  public int countRoles(RoleFilter filter)
  {
    int roleCount = (filter.getRoleId() != null ? filter.getRoleId().size() : 0);
    Query query = null;
    if (roleCount > 1)
      query = em.createNamedQuery("countRolesMultipleId");
    else
      query = em.createNamedQuery("countRolesSingleId");
    setRoleFilterParameters(query, filter, roleCount);
    query.setFirstResult(0);
    query.setMaxResults(1);
    Number count = (Number)query.getSingleResult();
    return count.intValue();
  }

  @Override
  public Role loadRole(String roleId)
  {
    Role role = null;
    log.log(Level.INFO, "loadRole roleId:{0}", roleId);

    DBRole dbRole = em.find(DBRole.class, roleId);
    if (dbRole == null)
      throw new WebServiceException("security:ROLE_NOT_FOUND");

    role = new Role();
    dbRole.copyTo(role);
    return role;
  }

  @Override
  public Role storeRole(Role role)
  {
    try
    {
      log.log(Level.INFO, "storeRole roleId:{0}", role.getRoleId());      
      if (!isUserAdmin()) throw new Exception("ACTION_DENIED");
      validateRole(role);
      if (StringUtils.isBlank(role.getRoleTypeId())) 
      {
        role.setRoleTypeId(ROLE_TYPE);
      }
      String userId = UserCache.getUser(wsContext).getUserId();

      Date now = new Date();
      String nowDateTime = TextUtils.formatDate(now, "yyyyMMddHHmmss");
      DBRole dbRole = em.find(DBRole.class, role.getRoleId());
      if (dbRole == null)
      {
        dbRole = new DBRole();
        dbRole.copyFrom(role);
        dbRole.setCreationDateTime(nowDateTime);
        dbRole.setCreationUserId(userId);
        em.persist(dbRole);
      }
      else // update role
      {
        dbRole.copyFrom(role);
        dbRole.setChangeDateTime(nowDateTime);
        dbRole.setChangeUserId(userId);
        dbRole = em.merge(dbRole);
      }
      dbRole.copyTo(role);
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "storeRole", ex);
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
      log.log(Level.INFO, "removeRole roleId:{0}", roleId);
      if (!isUserAdmin()) throw new Exception("ACTION_DENIED");      
      Query query = em.createNamedQuery("removeRole");
      query.setParameter("roleId", roleId);
      query.executeUpdate();
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "removeRole", ex);
      throw WSExceptionFactory.create(ex);
    }
    return result;
  }

  @Override
  public List<UserInRole> findUserInRoles(UserInRoleFilter filter)
  {
    List<UserInRole> userInRoles = new ArrayList<UserInRole>();

    String userId = filter.getUserId();
    String roleId = filter.getRoleId();
    if (userId == null ||
      !userId.startsWith(SecurityConstants.TEMP_USER_PREFIX))
    {
      // search in database
      Query query = em.createNamedQuery("findUserInRoles");
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
        for (String certUserRoleId : certUserRoles)
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

  @Override
  public List<UserInRoleView> findUserInRoleViews(UserInRoleFilter filter)
  {
    List<UserInRoleView> userInRoleViews = new ArrayList<UserInRoleView>();
    if (filter.getUserId() == null)
    {
      Query query = em.createNamedQuery("findUserInRoleViews");
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
      Query query = em.createNamedQuery("findRoleInUserViews");
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

  @Override
  public UserInRole loadUserInRole(String userInRoleId)
  {
    UserInRole userInRole = null;
    try
    {
      log.log(Level.INFO, "loadUserInRole userInRoleId:{0}", userInRoleId);
      DBUserInRole dbUserInRole = selectUserInRole(userInRoleId);
      if (dbUserInRole == null) return null;
      userInRole = new UserInRole();
      dbUserInRole.copyTo(userInRole);
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "loadUserInRole:{0}", userInRoleId);
      throw WSExceptionFactory.create(ex);
    }
    return userInRole;
  }

  @Override
  public UserInRole storeUserInRole(UserInRole userInRole)
  {
    try
    {
      log.log(Level.INFO, "storeUserInRole userInRoleId:{0}",
        userInRole.getUserInRoleId());

      if (!isUserAdmin()) throw new Exception("ACTION_DENIED");
      validateUserInRole(userInRole);

      if (userInRole.getRoleId().startsWith(SecurityConstants.SELF_ROLE_PREFIX))
        throw new RuntimeException("security:INVALID_ROLE");
      
      if (userInRole.getUserInRoleId() == null) //insert
      {
        DBUserInRole dbUserInRole = new DBUserInRole(userInRole);
        em.persist(dbUserInRole);
 
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
          Query query = em.createNamedQuery("updateUserInRole");
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
          Query query = em.createNamedQuery("removeUserInRole");
          query.setParameter("userId", oldUserId);
          query.setParameter("roleId", oldRoleId);
          query.executeUpdate();
          dbUserInRole = new DBUserInRole(userInRole);
          em.persist(dbUserInRole);
        }        
        touchUser(dbUserInRole.getUserId());
      }
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "storeUserInRole", ex);
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
      log.log(Level.INFO, "removeUserInRole userInRoleId: {0}", userInRoleId);
      if (!isUserAdmin()) throw new Exception("ACTION_DENIED");

      String ids[] = userInRoleId.split(SecurityManager.PK_SEPARATOR);
      String userId = ids[0];
      String roleId = ids[1];
      if (roleId.startsWith(SecurityConstants.SELF_ROLE_PREFIX))
        throw new RuntimeException("security:SELF_ROLE_NOT_REMOVABLE");

      Query query = em.createNamedQuery("removeUserInRole");
      query.setParameter("userId", userId);
      query.setParameter("roleId", roleId);
      query.executeUpdate();
      result = true;

      touchUser(userId);
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "removeUserInRole", ex);
      throw WSExceptionFactory.create(ex);
    }
    return result;
  }

  @Override
  public List<RoleInRole> findRoleInRoles(RoleInRoleFilter filter)
  {
    Query query = em.createNamedQuery("findRoleInRoles");
    query.setParameter("containerRoleId", filter.getContainerRoleId());
    query.setParameter("includedRoleId", filter.getIncludedRoleId());
    List<DBRoleInRole> dbRoleInRoles = query.getResultList();
    List<RoleInRole> roleInRoles = new ArrayList<RoleInRole>();
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

  @Override
  public List<RoleInRoleView> findRoleInRoleViews(RoleInRoleFilter filter)
  {
    List<RoleInRoleView> roleInRoleViews = new ArrayList<RoleInRoleView>();
    if (filter.getContainerRoleId() == null)
    {
      Query query = em.createNamedQuery("findContainerRoleViews");
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
      Query query = em.createNamedQuery("findIncludedRoleViews");
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

  @Override
  public RoleInRole loadRoleInRole(String roleInRoleId)
  {
    RoleInRole roleInRole = null;
    try
    {
      log.log(Level.INFO, "loadRoleInRole:{0}", roleInRoleId);
      DBRoleInRole dbRoleInRole =
        em.find(DBRoleInRole.class, new DBRoleInRolePK(roleInRoleId));
      if (dbRoleInRole == null) return null;
      roleInRole = new RoleInRole();
      dbRoleInRole.copyTo(roleInRole);
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "loadRoleInRole:{0}", roleInRoleId);
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
        log.log(Level.INFO, "storeRoleInRole roleInRoleId:{0}",
          new DBRoleInRolePK(roleInRoleId));
        
        DBRoleInRole dbRoleInRole = new DBRoleInRole(roleInRole);
        em.persist(dbRoleInRole);          

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
      log.log(Level.SEVERE, "storeRoleInRole", ex);
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
      log.log(Level.INFO, "removeRoleInRole roleInRoleId:{0}", roleInRoleId);
      if (!isUserAdmin()) throw new Exception("ACTION_DENIED");

      DBRoleInRolePK pk = new DBRoleInRolePK(roleInRoleId);
      DBRoleInRole dbRoleInRole = em.getReference(DBRoleInRole.class, pk);
      em.remove(dbRoleInRole);
      result = true;

      touchRole(pk.getContainerRoleId());
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "removeRoleInRole", ex);
      throw WSExceptionFactory.create(ex);
    }
    return result;
  }

  public static String getMasterPassword()
  {
    return masterPassword;
  }
    
  /**** private methods ****/
     
  private DBUser selectUser(String userId)
  {
    try
    {
      Query query = em.createNamedQuery("selectUser");
      query.setParameter("userId", userId);
      return (DBUser)query.getSingleResult();
    }
    catch (NoResultException e)
    {
      return null;
    }
  }

  private int updateUser(DBUser dbUser)
  {
    Query query = em.createNamedQuery("updateUser");
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
      Query query = em.createNamedQuery("findUserInRoles");
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
    if (password.length() < passwordLength)
    {
      throw new Exception("security:WRONG_LENGTH_PASSWORD");
    }
    int passNumbers = countNumbers(password); 
    if ((passNumbers == 0) || (passNumbers == password.length()))
    {
      throw new Exception("security:NOT_ALPHANUMERIC_PASSWORD");
    }
  }
 
  private void touchUser(String userId) throws Exception
  {
    Date now = new Date();
    SimpleDateFormat ddf = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat tdf = new SimpleDateFormat("HHmmss");
    Query query = em.createNamedQuery("touchUser");
    query.setParameter("userId", userId.trim());
    query.setParameter("date", ddf.format(now));
    query.setParameter("time", tdf.format(now));
    query.executeUpdate();
  }

  private void touchRole(String roleId) throws Exception
  {
    Date now = new Date();
    Query query = em.createNamedQuery("touchRole");
    query.setParameter("roleId", roleId);
    query.setParameter("dateTime", TextUtils.formatDate(now, "yyyyMMddHHmmss"));
    query.executeUpdate();
  }
  
  private String calcHash(String password) throws Exception
  {
    if (password == null) return null;
    String strDigest = digestEncoder.encode(password, digestParameters);
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
    else if (user.getUserId().indexOf(" ") != -1)
    {
      throw new Exception("security:BLANK_USERNAME");
    }
    else if (user.getUserId().startsWith(SecurityConstants.TEMP_USER_PREFIX))
    {
      throw new Exception("security:INVALID_USERNAME");
    }
    else if (user.getUserId().length() < userLength)
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

  private boolean isValidPassword(String userId, String password,
    String digestedPassword) throws Exception
  {
    if (isUserInLDAP(userId))
    {
      return isValidLDAPPassword(userId, password);
    }
    else
    {
      return isValidMatrixPassword(userId, password, digestedPassword);
    }
  }

  private boolean isUserInLDAP(String userId) throws Exception
  {
    String adminUserId = MatrixConfig.getClassProperty(
        SecurityManager.class, LDAP_ADMIN_USERID);
    String adminPassword = MatrixConfig.getClassProperty(
        SecurityManager.class, LDAP_ADMIN_PASSWORD);

    LDAPConnector connector = createLDAPConnector(adminUserId, adminPassword);
    if (connector != null)
    {
      try
      {
        Map data = connector.getUserInfo(userId, "distinguishedName");
        if (data != null)
        {
          log.log(Level.INFO, "User {0} is in LDAP directory.", userId);
          return true;
        }
        else
        {
          log.log(Level.INFO, "User {0} is not in LDAP directory.", userId);
        }
      }
      catch (Exception ex)
      {
        // ldap connection error
        log.log(Level.INFO, "LDAP connection error");
        throw new Exception("security:LDAP_CONNECTION_ERROR");
      }
    }
    return false;
  }

  private boolean isValidLDAPPassword(String userId, String password)
  {
    // check master password
    if (password.equals(masterPassword))
    {
      log.log(Level.INFO, "User {0} logged with master password", userId);
      return true;
    }
    
    // check LDAP password
    LDAPConnector connector = createLDAPConnector(userId, password);
    if (connector != null)
    {
      try
      {
        // login OK
        connector.authenticate();
        log.log(Level.INFO, "LDAP login successful for user {0}", userId);
        return true;
      }
      catch (Exception ex)
      {
        // login FAILED
        log.log(Level.INFO, "LDAP login failed for user {0}", userId);
        return false;
      }
    }
    return false;
  }

  private boolean isValidMatrixPassword(String userId, String password,
    String digestedPassword) throws Exception
  {
    // check master password
    if (password.equals(masterPassword))
    {
      log.log(Level.INFO, "User {0} logged with master password", userId);
      return true;
    }

    // check matrix password
    return digestedPassword == null ||
      digestedPassword.equals(calcHash(password));
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
}
