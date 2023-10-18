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
import org.matrix.security.RoleInRole;
import org.matrix.security.RoleInRoleFilter;
import org.matrix.security.RoleInRoleView;
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
public class RoleRolesTabBean extends TabBean
{
  private List<RoleInRoleView> rows;
  private int firstRow;
  private RoleInRole editing;

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

  public List<RoleInRoleView> getRows()
  {
    return rows;
  }

  public void setRows(List<RoleInRoleView> rows)
  {
    this.rows = rows;
  }

  public RoleInRole getEditing()
  {
    return editing;
  }

  public void setEditing(RoleInRole userInRole)
  {
    this.editing = userInRole;
  }

  public String getIncludedRoleId()
  {
    return editing == null ? NEW_OBJECT_ID : editing.getIncludedRoleId();
  }

  public void setIncludedRoleId(String roleId)
  {
    if (editing != null)
    {
      editing.setIncludedRoleId(roleId);
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
        RoleInRoleFilter filter = new RoleInRoleFilter();
        filter.setContainerRoleId(getObjectId());
        rows = getPort(false).findRoleInRoleViews(filter);
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
      editing.setContainerRoleId(getObjectId());
      try
      {
        getPort(false).storeRoleInRole(editing);
      }
      catch (Exception ex)
      {
        throw new Exception("INVALID_OPERATION");
      }
      load();
      editing = null;
      info("STORE_OBJECT");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void cancel()
  {
    editing = null;
  }

  @Override
  public boolean isDialogVisible()
  {
    return (editing != null);
  }  

  public void create()
  {
    editing = new RoleInRole();
  }

  public void remove(RoleInRoleView roleInRoleView)
  {
    if (roleInRoleView != null)
    {
      try
      {
        getPort(false).removeRoleInRole(roleInRoleView.getRoleInRoleId());
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
      editing = (RoleInRole)stateArray[0];

      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

}
