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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.matrix.security.SecurityConstants;

/**
 *
 * @author realor
 */
public class User extends org.matrix.security.User implements Serializable
{
  private Set<String> roles;

  public User()
  {
  }

  public User(org.matrix.security.User user)
  {
    userId = user.getUserId();
    password = user.getPassword();
    displayName = user.getDisplayName();
    personId = user.getPersonId();
    givenName = user.getGivenName();
    surname = user.getSurname();
    nif = user.getNIF();
    cif = user.getCIF();
    representant = user.isRepresentant();
    organizationName = user.getOrganizationName();
    email = user.getEmail();
    changeDateTime = user.getChangeDateTime();
  }

  public boolean isCertficateUser()
  {
    return userId.startsWith(SecurityConstants.AUTH_USER_PREFIX);
  }

  public boolean isInRole(String roleId)
  {
    return getRoles().contains(roleId);
  }

  public void setRoles(Set<String> roles)
  {
    this.roles = roles;
  }

  public Set<String> getRoles()
  {
    if (roles == null) roles = new HashSet<>();
    return roles;
  }

  public List<String> getRolesList()
  {
    ArrayList<String> rolesList = new ArrayList<>();
    rolesList.addAll(getRoles());
    return rolesList;
  }

  public String getRolesString()
  {
    StringBuffer strRoles = null;
    if (getRoles().size() > 0)
    {
      strRoles = new StringBuffer();
      Iterator<String> iter = getRoles().iterator();
      strRoles.append(iter.next());
      while (iter.hasNext())
      {
        strRoles.append(",");
        strRoles.append(iter.next());
      }
    }
    return (strRoles != null? strRoles.toString() : null);
  }

  @Override
  public String toString()
  {
    StringBuilder buffer = new StringBuilder("{");
    buffer.append(userId);
    buffer.append(", ");
    buffer.append("displayName");
    buffer.append(", ");
    buffer.append(roles);
    buffer.append("}");
    return buffer.toString();
  }
}
