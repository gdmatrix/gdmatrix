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

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.security.Role;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import static org.santfeliu.webapp.modules.security.SecurityModuleBean.getPort;

/**
 *
 * @author realor
 */
@Named
@ViewScoped
public class RoleObjectBean extends ObjectBean
{
  private Role role = new Role();

  @Inject
  RoleTypeBean roleTypeBean;

  @Inject
  RoleFinderBean roleFinderBean;

  @PostConstruct
  public void init()
  {
  }

  public Role getRole()
  {
    return role;
  }

  public void setRole(Role role)
  {
    this.role = role;
  }

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.PERSON_TYPE;
  }

  @Override
  public RoleTypeBean getTypeBean()
  {
    return roleTypeBean;
  }

  @Override
  public Role getObject()
  {
    return isNew() ? null : role;
  }

  @Override
  public String getDescription()
  {
    return isNew() ? "" : getDescription(role.getRoleId());
  }

  public String getDescription(String roleId)
  {
    return getTypeBean().getDescription(roleId);
  }

  @Override
  public RoleFinderBean getFinderBean()
  {
    return roleFinderBean;
  }

  @Override
  public void loadObject() throws Exception
  {
    if (!NEW_OBJECT_ID.equals(objectId))
      role = getPort(false).loadRole(objectId);
    else
      role = new Role();
  }

  @Override
  public void storeObject() throws Exception
  {
    role = getPort(false).storeRole(role);
    setObjectId(role.getRoleId());
    roleFinderBean.outdate();
  }

  @Override
  public Serializable saveState()
  {
    return role;
  }

  @Override
  public void restoreState(Serializable state)
  {
    this.role = (Role)state;
  }
}
