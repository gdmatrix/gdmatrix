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
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.security.UserInRole;
import org.matrix.security.UserInRoleFilter;
import org.matrix.security.UserInRoleView;
import org.primefaces.PrimeFaces;
import static org.santfeliu.security.web.SecurityConfigBean.getPort;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;

/**
 *
 * @author realor
 */
@Named
@ViewScoped
public class RoleUsersTabBean extends TabBean
{
  private List<UserInRoleView> rows;
  private int firstRow;
  private UserInRole editing;

  @Inject
  RoleObjectBean roleObjectBean;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return roleObjectBean;
  }

  public List<UserInRoleView> getRows()
  {
    return rows;
  }

  public void setRows(List<UserInRoleView> rows)
  {
    this.rows = rows;
  }

  public UserInRole getEditing()
  {
    return editing;
  }

  public void setEditing(UserInRole userInRole)
  {
    this.editing = userInRole;
  }

  public String getUserId()
  {
    return editing == null ? NEW_OBJECT_ID : editing.getUserId();
  }

  public void setUserId(String userId)
  {
    if (editing != null)
    {
      editing.setUserId(userId);
      showDialog();
    }
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }

  @Override
  public void load()
  {
    System.out.println("load user in roles:" + getObjectId());
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        UserInRoleFilter filter = new UserInRoleFilter();
        filter.setRoleId(getObjectId());
        rows = getPort(false).findUserInRoleViews(filter);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else rows = Collections.EMPTY_LIST;
  }

  @Override
  public void store()
  {
    try
    {
      editing.setRoleId(getObjectId());
      getPort(false).storeUserInRole(editing);
      load();
      editing = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void cancel()
  {
    info("CANCEL_OBJECT");
    editing = null;
  }

  public void create()
  {
    editing = new UserInRole();
  }

  public void edit(UserInRoleView userInRoleView)
  {
    if (userInRoleView != null)
    {
      try
      {
        editing = getPort(false).loadUserInRole(userInRoleView.getUserInRoleId());
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else
    {
      create();
    }
  }

  public void remove(UserInRoleView userInRoleView)
  {
    if (userInRoleView != null)
    {
      try
      {
        getPort(false).removeUserInRole(userInRoleView.getUserInRoleId());
        load();
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ editing };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      editing = (UserInRole)stateArray[0];

      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private void showDialog()
  {
    try
    {
      PrimeFaces current = PrimeFaces.current();
      current.executeScript("PF('roleUsersDialog').show();");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

}
