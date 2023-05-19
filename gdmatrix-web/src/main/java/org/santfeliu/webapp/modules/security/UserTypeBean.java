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
import org.matrix.security.SecurityConstants;
import org.matrix.security.UserFilter;
import org.matrix.security.User;
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
public class UserTypeBean extends TypeBean<User, UserFilter>
{
  private static final String BUNDLE_PREFIX = "$$securityBundle.";
  
  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.USER_TYPE;
  }

  @Override
  public String getObjectId(User user)
  {
    return user.getUserId();
  }

  @Override
  public String describe(User user)
  {
    String userId = user.getUserId();
    return userId + " (" + user.getDisplayName() + ")";
  }

  @Override
  public User loadObject(String objectId)
  {
    try
    {
      return getPort(true).loadUser(objectId);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  @Override
  public String getTypeId(User user)
  {
    return DictionaryConstants.USER_TYPE;
  }

  @Override
  public ObjectSetup createObjectSetup()
  {
    ObjectSetup objectSetup = new ObjectSetup();
    objectSetup.setViewId("/pages/security/user.xhtml");

    List<EditTab> editTabs = new ArrayList<>();
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_main", "/pages/security/user_main.xhtml"));
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_roles", "/pages/security/user_roles.xhtml", "userRolesTabBean"));
    objectSetup.setEditTabs(editTabs);

    return objectSetup;
  }

  @Override
  public UserFilter queryToFilter(String query, String typeId)
  {
    UserFilter filter = new UserFilter();

    if (query != null)
    {
      if (query.length() > 0 &&
          ((query.charAt(0) >= 'a' && query.charAt(0) <= 'z') ||
           query.startsWith(SecurityConstants.AUTH_USER_PREFIX)))
      {
        filter.getUserId().add("%" + query + "%");
      }
      else
      {
        filter.setDisplayName(query);
      }
    }
    filter.setMaxResults(10);

    return filter;
  }

  @Override
  public String filterToQuery(UserFilter filter)
  {
    String query = filter.getUserId().isEmpty() ?
      filter.getDisplayName() : filter.getUserId().get(0);

    if (query == null) query = "";

    if (query.startsWith("%")) query = query.substring(1);
    if (query.endsWith("%")) query = query.substring(0, query.length() - 1);

    return query;
  }

  @Override
  public List<User> find(UserFilter filter)
  {
    try
    {
      return getPort(true).findUsers(filter);
    }
    catch (Exception ex)
    {
      return Collections.EMPTY_LIST;
    }
  }

}
