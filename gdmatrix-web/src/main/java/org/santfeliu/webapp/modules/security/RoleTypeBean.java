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
package org.santfeliu.webapp.modules.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.security.Role;
import org.matrix.security.RoleFilter;
import org.matrix.security.SecurityConstants;
import org.santfeliu.webapp.TypeBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.setup.ObjectSetup;
import static org.santfeliu.webapp.modules.security.SecurityModuleBean.getPort;

/**
 *
 * @author realor
 */
@Named
@ApplicationScoped
public class RoleTypeBean extends TypeBean<Role, RoleFilter>
{
  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.ROLE_TYPE;
  }

  @Override
  public String getObjectId(Role role)
  {
    return role.getRoleId();
  }

  @Override
  public String describe(Role role)
  {
    String roleId = role.getRoleId().trim();
    if (roleId.startsWith(SecurityConstants.SELF_ROLE_PREFIX) &&
        roleId.endsWith(SecurityConstants.SELF_ROLE_SUFFIX))
    {
      return roleId;
    }
    return role.getRoleId() + " (" + role.getName() + ")";
  }

  @Override
  public Role loadObject(String objectId)
  {
    try
    {
      String roleId = objectId;
      if (roleId.startsWith(SecurityConstants.SELF_ROLE_PREFIX) &&
        roleId.endsWith(SecurityConstants.SELF_ROLE_SUFFIX))
      {
        Role nominalRole = new Role();
        nominalRole.setRoleId(roleId);
        return nominalRole;
      }
      return getPort(true).loadRole(objectId);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  @Override
  public String getTypeId(Role role)
  {
    return role.getRoleTypeId();
  }

  @Override
  public ObjectSetup createObjectSetup()
  {
    ObjectSetup objectSetup = new ObjectSetup();
    objectSetup.setViewId("/pages/security/role.xhtml");

    List<EditTab> editTabs = new ArrayList<>();
    editTabs.add(new EditTab("Main", "/pages/security/role_main.xhtml"));
    objectSetup.setEditTabs(editTabs);

    return objectSetup;
  }

  @Override
  public RoleFilter queryToFilter(String query, String typeId)
  {
    RoleFilter filter = new RoleFilter();

    if (query != null)
    {
      if (query.length() > 0 &&
          query.charAt(0) >= 'A' && query.charAt(0) <= 'Z')
      {
        filter.getRoleId().add("%" + query + "%");
      }
      else
      {
        filter.setName(query);
      }
    }
    filter.setMaxResults(10);

    return filter;
  }

  @Override
  public String filterToQuery(RoleFilter filter)
  {
    String query = filter.getRoleId().isEmpty() ?
      filter.getName() : filter.getRoleId().get(0);

    if (query.startsWith("%")) query = query.substring(1);
    if (query.endsWith("%")) query = query.substring(0, query.length() - 1);

    return query;
  }

  @Override
  public List<Role> find(RoleFilter filter)
  {
    try
    {
      return getPort(true).findRoles(filter);
    }
    catch (Exception ex)
    {
      return Collections.EMPTY_LIST;
    }
  }

}
