package org.santfeliu.security.web;

import java.util.List;
import org.matrix.security.Role;
import org.matrix.security.RoleInRole;
import org.matrix.security.RoleInRoleFilter;
import org.matrix.security.RoleInRoleView;
import org.santfeliu.web.obj.PageBean;

public class RoleRolesBean extends PageBean
{
  private List<RoleInRoleView> rows;
  private List<RoleInRoleView> containers;
  private String editingRoleId;

  public RoleRolesBean()
  {
    load();
  }

  public String getEditingRoleId()
  {
    return editingRoleId;
  }

  public void setEditingRoleId(String editingRoleId)
  {
    this.editingRoleId = editingRoleId;
  }

  public List<RoleInRoleView> getRows()
  {
    return rows;
  }

  public void setRows(List<RoleInRoleView> rows)
  {
    this.rows = rows;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  public List<RoleInRoleView> getContainers()
  {
    return containers;
  }

  public void setContainers(List<RoleInRoleView> containers)
  {
    this.containers = containers;
  }

  public int getContainersCount()
  {
    return (getContainers() == null ? 0 : getContainers().size());
  }
  
  public String show()
  {
    return "role_roles";
  }

  public String showRoleInRole()
  {
    return getControllerBean().showObject("Role",
      (String)getValue("#{row.includedRole.roleId}"));
  }

  public String showContainerRoleInRole()
  {
    return getControllerBean().showObject("Role",
      (String)getValue("#{container.containerRole.roleId}"));
  }

  public String storeRoleInRole()
  {
    try
    {
      if (editingRoleId == null || editingRoleId.trim().length() == 0)
      {
        throw new Exception("VALUE_IS_MANDATORY");
      }
      else
      {
        RoleInRole roleInRole = new RoleInRole();
        roleInRole.setIncludedRoleId(editingRoleId);
        roleInRole.setContainerRoleId(getRole().getRoleId());
        try
        {
          SecurityConfigBean.getPort().storeRoleInRole(roleInRole);
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
      editingRoleId = null;
      load();
    }
    return null;
  }
  
  public String removeRoleInRole()
  {
    try
    {
      RoleInRoleView row =
        (RoleInRoleView)getExternalContext().getRequestMap().get("row");
      SecurityConfigBean.getPort().removeRoleInRole(row.getRoleInRoleId());
      load();
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
      "#{roleRolesBean.editingRoleId}");
  }

  private void load()
  {
    try
    {
      if (!isNew())
      {
        RoleInRoleFilter filter = new RoleInRoleFilter();
        filter.setContainerRoleId(getRole().getRoleId());
        rows = SecurityConfigBean.getPort().findRoleInRoleViews(filter);

        filter = new RoleInRoleFilter();
        filter.setIncludedRoleId(getRole().getRoleId());
        containers = SecurityConfigBean.getPort().findRoleInRoleViews(filter);
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
