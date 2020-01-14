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
package org.santfeliu.matrix.ide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.matrix.security.RoleInRole;
import org.matrix.security.RoleInRoleFilter;
import org.matrix.security.SecurityManagerPort;
import org.matrix.security.User;
import org.matrix.security.UserInRoleFilter;
import org.matrix.security.UserInRoleView;

/**
 *
 * @author realor
 */
public class RolesReporter
{
  private SecurityManagerPort port;
  private StringBuilder builder = new StringBuilder();
  private HashMap<String, org.matrix.security.Role> roleMap = new HashMap();
  private HashMap<String, List<String>> roleInRoleMap = new HashMap();

  public RolesReporter(SecurityManagerPort port)
  {
    this.port = port;
  }

  public void begin()
  {
    builder.setLength(0);
    roleMap.clear();
    roleInRoleMap.clear();
    builder.append("<html><body><ul>");
  }

  public void end()
  {
    builder.append("</ul></body></html>");
  }
  
  public void report(String roleId)
  {
    org.matrix.security.Role svcRole = getRole(roleId);
    if (svcRole != null)
    {
      builder.append("<li><b>");
      builder.append(svcRole.getRoleId());
      org.matrix.security.Role role = getRole(roleId);
      if (role != null)
      {
        builder.append(", ");
        builder.append(role.getName());
      }
      builder.append("</b>:<br>");
      
      // print roles in role
      Set<String> inRoleIds = new HashSet();
      explodeRole(roleId, inRoleIds);
      inRoleIds.remove(roleId);
      if (!inRoleIds.isEmpty())
      {
        builder.append("Roles:<ul>\n");
        for (String inRoleId : inRoleIds)
        {
          if (!inRoleId.equals(roleId))
          {
            builder.append("<li>");
            builder.append(inRoleId);
            role = getRole(inRoleId);
            if (role != null)
            {
              builder.append(", ");
              builder.append(role.getName());
            }
            builder.append("</li>\n");
          }
        }
        builder.append("</ul><br>\n");
      }

      // print users in role
      List<User> users = getUsersInRole(roleId);
      if (!users.isEmpty())
      {
        builder.append("Users:<ul>\n");
        for (User user : users)
        {
          builder.append("<li>");
          builder.append(user.getUserId());
          builder.append(": ");
          builder.append(user.getDisplayName());
          builder.append("</li>\n");
        }
        builder.append("</ul>\n");
      }

      builder.append("</li>\n");
    }
  }

  // explodes a Role
  private void explodeRole(String roleId, Set<String> explodedRoleIds)
  {
    explodedRoleIds.add(roleId);
    List<String> inRoleIds = getInRoleIds(roleId);
    for (String inRoleId : inRoleIds)
    {
      if (!explodedRoleIds.contains(inRoleId))
      {
        explodeRole(inRoleId, explodedRoleIds);
      }
    }
  }

  private List<User> getUsersInRole(String roleId)
  {
    UserInRoleFilter filter = new UserInRoleFilter();
    filter.setRoleId(roleId);
    String strDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
    filter.setMinDate(strDate);
    filter.setMaxDate(strDate);
    List<UserInRoleView> views = port.findUserInRoleViews(filter);
    List<User> users = new ArrayList();
    for (UserInRoleView view : views)
    {
      users.add(view.getUser());
    }
    return users;
  }

  // return Role object identified by roleId
  private org.matrix.security.Role getRole(String roleId)
  {
    org.matrix.security.Role svcRole = roleMap.get(roleId);
    if (svcRole == null)
    {
      try
      {
        svcRole = port.loadRole(roleId);
        roleMap.put(roleId, svcRole);
      }
      catch (Exception ex)
      {
      }
    }
    return svcRole;
  }

  // return roles included into roleId
  private List<String> getInRoleIds(String roleId)
  {
    List<String> inRoleIds = roleInRoleMap.get(roleId);
    if (inRoleIds == null)
    {
      inRoleIds = new ArrayList();
      RoleInRoleFilter filter = new RoleInRoleFilter();
      filter.setContainerRoleId(roleId);
      List<RoleInRole> inRoles = port.findRoleInRoles(filter);
      roleInRoleMap.put(roleId, inRoleIds);
      for (RoleInRole inRole : inRoles)
      {
        inRoleIds.add(inRole.getIncludedRoleId());
      }
    }
    return inRoleIds;
  }

  @Override
  public String toString()
  {
    return builder.toString();
  }
}
