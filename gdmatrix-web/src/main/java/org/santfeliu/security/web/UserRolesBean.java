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
package org.santfeliu.security.web;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.matrix.security.User;
import org.matrix.security.UserInRole;
import org.matrix.security.UserInRoleFilter;
import org.matrix.security.UserInRoleView;
import org.santfeliu.web.obj.PageBean;
import org.santfeliu.web.obj.PageFinder;

/**
 *
 * @author realor
 */
public class UserRolesBean extends PageBean
{
  private List<UserInRoleView> rows;
  private UserInRole editingRole;

  private int firstRowIndex;

  public UserRolesBean()
  {
    load();
  }

  public int getFirstRowIndex()
  {
    return firstRowIndex;
  }

  public void setFirstRowIndex(int firstRowIndex)
  {
    this.firstRowIndex = firstRowIndex;
  }

  public UserInRole getEditingRole()
  {
    return editingRole;
  }

  public void setEditingRole(UserInRole editingRole)
  {
    this.editingRole = editingRole;
  }

  public List<UserInRoleView> getRows()
  {
    return rows;
  }

  public void setRows(List<UserInRoleView> rows)
  {
    this.rows = rows;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  @Override
  public String show()
  {
    return "user_roles";
  }

  public String showUserInRole()
  {
    return getControllerBean().showObject("Role",
      (String)getValue("#{row.role.roleId}"));
  }

  public String createUserInRole()
  {
    editingRole = new UserInRole();
    editingRole.setUserId(getObjectId());
    return null;
  }

  public String editUserInRole()
  {
    try
    {
      UserInRoleView row =
        (UserInRoleView)getExternalContext().getRequestMap().get("row");
      String userInRoleId = row.getUserInRoleId();
      editingRole = SecurityConfigBean.getPort().loadUserInRole(userInRoleId);
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String storeUserInRole()
  {
    try
    {
      if (editingRole.getRoleId() == null ||
        editingRole.getRoleId().trim().length() == 0)
      {
        throw new Exception("VALUE_IS_MANDATORY");
      }
      else
      {
        try
        {
          SecurityConfigBean.getPort().storeUserInRole(editingRole);
        }
        catch (Exception ex)
        {
          throw new Exception("INVALID_OPERATION");
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    finally
    {
      String auxUserInRoleId = getObjectId() + ";" + editingRole.getRoleId();
      editingRole = null;
      load();
      firstRowIndex = PageFinder.findFirstRowIndex(rows, getPageSize(),
        "userInRoleId", auxUserInRoleId);
    }
    return null;
  }

  public String cancelUserInRole()
  {
    editingRole = null;
    return null;
  }

  public String removeUserInRole()
  {
    try
    {
      UserInRoleView row =
        (UserInRoleView)getExternalContext().getRequestMap().get("row");
      SecurityConfigBean.getPort().removeUserInRole(row.getUserInRoleId());
      editingRole = null;
      load();
      if (firstRowIndex >= rows.size() && firstRowIndex > 0)
      {
        firstRowIndex -= getPageSize();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String searchRole()
  {
    return getControllerBean().searchObject("Role",
      "#{userRolesBean.editingRole.roleId}");
  }

  public String getUserInRoleStyleClass()
  {
    UserInRoleView row =
      (UserInRoleView)getExternalContext().getRequestMap().get("row");
    String startDate = "00000000";
    String endDate = "99999999";
    if (row.getStartDate() != null) startDate = row.getStartDate();
    if (row.getEndDate() != null) endDate = row.getEndDate();
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    String strNow = format.format(new Date());
    if (strNow.compareTo(startDate) < 0) //Future role
    {
      return "futureUserInRole";
    }
    else if (strNow.compareTo(endDate) > 0) //Old role
    {
      return "oldUserInRole";
    }
    else //Existing role
    {
      return "existingUserInRole";
    }
  }

  private void load()
  {
    try
    {
      if (!isNew())
      {
        UserInRoleFilter filter = new UserInRoleFilter();
        filter.setUserId(getUser().getUserId());
        rows = SecurityConfigBean.getPort().findUserInRoleViews(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private User getUser()
  {
    UserMainBean userMainBean = (UserMainBean)getBean("userMainBean");
    return userMainBean.getUser();
  }

}
