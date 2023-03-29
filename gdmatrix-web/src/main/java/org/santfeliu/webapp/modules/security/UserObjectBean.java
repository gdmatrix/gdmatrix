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
import org.matrix.security.User;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import static org.santfeliu.webapp.modules.security.SecurityModuleBean.getPort;

/**
 *
 * @author realor
 */
@Named
@ViewScoped
public class UserObjectBean extends ObjectBean
{
  private User user = new User();

  @Inject
  UserTypeBean userTypeBean;

  @Inject
  UserFinderBean userFinderBean;

  @PostConstruct
  public void init()
  {
  }

  public User getUser()
  {
    return user;
  }

  public void setUser(User user)
  {
    this.user = user;
  }

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.USER_TYPE;
  }

  @Override
  public UserTypeBean getTypeBean()
  {
    return userTypeBean;
  }

  @Override
  public User getObject()
  {
    return isNew() ? null : user;
  }

  @Override
  public String getDescription()
  {
    return isNew() ? "" : getDescription(user.getUserId());
  }

  public String getDescription(String userId)
  {
    return getTypeBean().getDescription(userId);
  }

  @Override
  public UserFinderBean getFinderBean()
  {
    return userFinderBean;
  }

  @Override
  public void loadObject() throws Exception
  {
    if (!NEW_OBJECT_ID.equals(objectId))
      user = getPort(false).loadUser(objectId);
    else
      user = new User();
  }

  @Override
  public void storeObject() throws Exception
  {
    user = getPort(false).storeUser(user);
    setObjectId(user.getUserId());
    userFinderBean.outdate();
  }

  @Override
  public void removeObject() throws Exception
  {
    getPort(false).removeUser(user.getUserId());

    userFinderBean.outdate();
  }

  public boolean isLocked()
  {
    return user.isLocked() != null && user.isLocked();
  }

  public void setLocked(boolean locked)
  {
    user.setLocked(locked);
  }

  @Override
  public Serializable saveState()
  {
    return user;
  }

  @Override
  public void restoreState(Serializable state)
  {
    this.user = (User)state;
  }
}
