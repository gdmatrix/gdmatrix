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
