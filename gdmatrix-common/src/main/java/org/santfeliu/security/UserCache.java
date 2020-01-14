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
package org.santfeliu.security;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import javax.xml.ws.WebServiceContext;

import org.matrix.security.Role;
import org.matrix.security.RoleFilter;
import org.matrix.security.RoleInRole;
import org.matrix.security.RoleInRoleFilter;
import org.matrix.security.SecurityConstants;
import org.matrix.security.SecurityManagerPort;
import org.matrix.security.SecurityManagerService;
import org.matrix.security.UserFilter;
import org.matrix.security.UserInRole;
import org.matrix.security.UserInRoleFilter;

import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.jmx.CacheMBean;
import org.santfeliu.jmx.JMXUtils;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author unknown
 */
public class UserCache
{
  static final Logger logger = Logger.getLogger("UserCache");
  static long UPDATE_TIME = 1000 * 60; // 60 seconds
  static final UserCache cache = new UserCache();

  private Map<String, User> users = 
    Collections.synchronizedMap(new HashMap<String, User>());
  private Map<String, Set<String>> userInRoles =
    Collections.synchronizedMap(new HashMap<String, Set<String>>());
  private Map<String, Set<String>> roleInRoles =
    Collections.synchronizedMap(new HashMap<String, Set<String>>());
  private long lastPurgeMillis;
  private String lastUserChangeDateTime;
  private String lastRoleChangeDateTime;
  private String lastResetDate;

  public UserCache()
  {
    lastPurgeMillis = System.currentTimeMillis();
    lastUserChangeDateTime = toDateTime(lastPurgeMillis);
    lastRoleChangeDateTime = lastUserChangeDateTime;
    lastResetDate = toDate(lastPurgeMillis);
    JMXUtils.registerMBean("UserCache", getCacheMBean());
  }

  public static User login(String userId, String password)
  {
    // synchronize with database
    cache.purge(false);

    SecurityManagerPort port = cache.getSecurityManagerPort();
    User user = new User(port.login(userId, password));
    cache.users.put(userId, new User(user));
    cache.loadUserRoles(user);
    return user;
  }

  public static User loginCertificate(byte[] certData)
  {
    // synchronize with database
    cache.purge(false);

    SecurityManagerPort port = cache.getSecurityManagerPort();
    User user = new User(port.loginCertificate(certData));
    cache.users.put(user.getUserId(), new User(user));
    cache.loadUserRoles(user);
    return user;
  }

  public static User getUser(String userId, String password)
  {
    // check for database synchronization
    cache.purge(true);

    // look for userId entry
    User user = cache.users.get(userId);
    if (user == null ||
      (!cache.validPassword(user.getPassword(), password)))
    {
      SecurityManagerPort port = cache.getSecurityManagerPort();
      user = new User(port.login(userId, password));
      cache.users.put(userId, new User(user));
    }
    cache.loadUserRoles(user);
    return user;
  }

  public static User getUser(Credentials credentials)
  {
    return getUser(credentials.getUserId(), credentials.getPassword());
  }

  public static User getUser(WebServiceContext wsContext)
  {
    return getUser(SecurityUtils.getCredentials(wsContext));
  }

  public static void clear()
  {
    cache.users.clear();
    cache.userInRoles.clear();
    cache.roleInRoles.clear();
  }

  // ***** private methods *****
  
  private boolean validPassword(String realPsw, String enteredPsw)
  {
    return realPsw == null || realPsw.equals(enteredPsw);
  }

  private void loadUserRoles(User user)
  {
    HashSet userRoles = new HashSet();
    String userId = user.getUserId();
    Set<String> roleSet = getUserInRoles(userId);    
    // expode user roles
    Iterator<String> iter = roleSet.iterator();
    while (iter.hasNext())
    {
      cache.explodeRole(iter.next(), userRoles);
    }
    user.setRoles(userRoles);
  }

  private void explodeRole(String roleId, Set<String> explodedRoleSet)
  {
    explodedRoleSet.add(roleId);
    Set<String> roleSet = getRoleInRoles(roleId);
    for (String includedRoleId : roleSet)
    {
      if (!explodedRoleSet.contains(includedRoleId))
      {
        if (includedRoleId.startsWith(SecurityConstants.SELF_ROLE_PREFIX))
        {
          explodedRoleSet.add(includedRoleId);
        }
        else
        {
          explodeRole(includedRoleId, explodedRoleSet);
        }
      }
    }
  }

  private Set<String> getUserInRoles(String userId)
  {
    Set<String> roleSet = userInRoles.get(userId);
    if (roleSet == null)
    {
      roleSet = new HashSet<String>();
      SecurityManagerPort port = cache.getSecurityManagerPort();
      UserInRoleFilter filter = new UserInRoleFilter();
      filter.setUserId(userId);      
      String strDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
      filter.setMinDate(strDate);
      filter.setMaxDate(strDate);
      List<UserInRole> list = port.findUserInRoles(filter);
      for (UserInRole userInRole : list)
      {
        roleSet.add(userInRole.getRoleId());
      }
      userInRoles.put(userId, roleSet);
    }
    return roleSet;
  }

  private Set<String> getRoleInRoles(String roleId)
  {
    Set<String> roleSet = roleInRoles.get(roleId);
    if (roleSet == null)
    {
      roleSet = new HashSet<String>();
      SecurityManagerPort port = cache.getSecurityManagerPort();
      RoleInRoleFilter filter = new RoleInRoleFilter();
      filter.setContainerRoleId(roleId);
      List<RoleInRole> list = port.findRoleInRoles(filter);
      for (RoleInRole roleInRole : list)
      {
        roleSet.add(roleInRole.getIncludedRoleId());
      }
      roleInRoles.put(roleId, roleSet);
    }
    return roleSet;
  }

  private void purge(boolean fast)
  {    
    long now = System.currentTimeMillis();    
    String strNow = toDate(now);
    if (!reset(strNow))
    {
      synchronized (this)
      {
        if (fast && now - cache.lastPurgeMillis < UPDATE_TIME) return;
      }

      SecurityManagerPort port = cache.getSecurityManagerPort();

      // get users && userInRoles to purge
      UserFilter userFilter = new UserFilter();
      String date = toDateTime(toMillis(lastUserChangeDateTime) + 1000);
      userFilter.setStartDateTime(date);
      logger.log(Level.INFO, "===> purge users: {0}", date);
      List<org.matrix.security.User> userList = port.findUsers(userFilter);

      // get roleInRoles to purge
      RoleFilter roleFilter = new RoleFilter();
      date = toDateTime(toMillis(lastRoleChangeDateTime) + 1000);
      roleFilter.setStartDateTime(date);
      logger.log(Level.INFO, "===> purge roles: {0}", date);
      List<Role> roleList = port.findRoles(roleFilter);

      synchronized (this)
      {
        for (org.matrix.security.User user : userList)
        {
          logger.log(Level.INFO, "===> remove user {0}", user.getUserId());
          users.remove(user.getUserId());
          userInRoles.remove(user.getUserId());
          if (lastUserChangeDateTime.compareTo(user.getChangeDateTime()) < 0)
          {
            lastUserChangeDateTime = user.getChangeDateTime();
          }
        }

        for (Role role : roleList)
        {
          logger.log(Level.INFO, "===> remove role {0}", role.getRoleId());
          roleInRoles.remove(role.getRoleId());
          if (lastRoleChangeDateTime.compareTo(role.getChangeDateTime()) < 0)
          {
            lastRoleChangeDateTime = role.getChangeDateTime();
          }
        }
        lastPurgeMillis = now;
      }
    }
  }

  private long toMillis(String sdate)
  {
    try
    {
      SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
      Date date = df.parse(sdate);
      return date.getTime();
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  private String toDateTime(long millis)
  {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    return df.format(new Date(millis));
  }

  private String toDate(long millis)
  {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
    return df.format(new Date(millis));
  }

  private synchronized boolean reset(String strNow)
  {
    if (strNow.compareTo(lastResetDate) > 0)
    {
      clear();
      lastResetDate = strNow;
      return true;
    }
    return false;
  }

  private SecurityManagerPort getSecurityManagerPort()
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint = wsDirectory.getEndpoint(SecurityManagerService.class);
    return endpoint.getPort(SecurityManagerPort.class,
      MatrixConfig.getProperty("adminCredentials.userId"),
      MatrixConfig.getProperty("adminCredentials.password"));    
  }

  private UserCacheMBean getCacheMBean()
  {
    try
    {
      return new UserCacheMBean();
    }
    catch (NotCompliantMBeanException ex)
    {
      return null;
    }
  }

  public class UserCacheMBean extends StandardMBean implements CacheMBean
  {
    public UserCacheMBean() throws NotCompliantMBeanException
    {
      super(CacheMBean.class);
    }

    public String getName()
    {
      return "UserCache";
    }

    public long getMaxSize()
    {
      return Long.MAX_VALUE;
    }

    public long getSize()
    {
      return users.size();
    }

    public String getDetails()
    {
      return "usersSize=" + getSize() + "," +
        "userInRolesSize=" + userInRoles.size() + "," +
        "roleInRolesSize=" + roleInRoles.size();
    }

    public void clear()
    {
      UserCache.clear();
    }

    public void update()
    {
      long nowMillis = System.currentTimeMillis();
      cache.purge(false);
      cache.lastPurgeMillis = nowMillis;
    }
  }
}
