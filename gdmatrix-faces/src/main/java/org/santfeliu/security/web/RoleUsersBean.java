package org.santfeliu.security.web;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.security.Role;
import org.matrix.security.UserInRole;
import org.matrix.security.UserInRoleFilter;
import org.matrix.security.UserInRoleView;
import org.santfeliu.web.obj.PageBean;
import org.santfeliu.web.obj.PageFinder;

public class RoleUsersBean extends PageBean
{
  private List<UserInRoleView> rows;
  private UserInRole editingUser;

  private int firstRowIndex;

  public RoleUsersBean()
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

  public UserInRole getEditingUser()
  {
    return editingUser;
  }

  public void setEditingUser(UserInRole editingUser)
  {
    this.editingUser = editingUser;
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
    return "role_users";
  }

  public String showUserInRole()
  {
    return getControllerBean().showObject("User",
       (String)getValue("#{row.user.userId}"));
  }

  public String createUserInRole()
  {
    editingUser = new UserInRole();
    editingUser.setRoleId(getObjectId());
    return null;
  }

  public String editUserInRole()
  {
    try
    {
      UserInRoleView row =
        (UserInRoleView)getExternalContext().getRequestMap().get("row");
      String userInRoleId = row.getUserInRoleId();
      editingUser = SecurityConfigBean.getPort().loadUserInRole(userInRoleId);
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
      if (editingUser.getUserId() == null ||
        editingUser.getUserId().trim().length() == 0)
      {
        throw new Exception("VALUE_IS_MANDATORY");
      }
      else
      {
        try
        {          
          SecurityConfigBean.getPort().storeUserInRole(editingUser);
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
      String auxUserInRoleId = editingUser.getUserId() + ";" + getObjectId();
      editingUser = null;
      load();
      firstRowIndex = PageFinder.findFirstRowIndex(rows, getPageSize(),
        "userInRoleId", auxUserInRoleId);
    }
    return null;
  }

  public String cancelUserInRole()
  {
    editingUser = null;
    return null;
  }

  public String removeUserInRole()
  {
    try
    {
      UserInRoleView row =
        (UserInRoleView)getExternalContext().getRequestMap().get("row");
      SecurityConfigBean.getPort().removeUserInRole(row.getUserInRoleId());
      editingUser = null;
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

  public List<SelectItem> getUserItems()
  {
    UserBean userBean = (UserBean)getBean("userBean");
    return userBean.getSelectItems(editingUser.getUserId());
  }

  public String searchUser()
  {
    return getControllerBean().searchObject("User",
      "#{roleUsersBean.editingUser.userId}");
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
        filter.setRoleId(getRole().getRoleId());
        rows = SecurityConfigBean.getPort().findUserInRoleViews(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private Role getRole()
  {
    RoleMainBean roleMainBean = (RoleMainBean)getBean("roleMainBean");
    return roleMainBean.getRole();
  }

}
