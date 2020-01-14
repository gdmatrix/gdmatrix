package org.santfeliu.security.web;

import org.matrix.security.Role;
import org.santfeliu.web.obj.ObjectBean;

public class RoleBean extends ObjectBean
{
  public RoleBean()
  {
  }

  public String getObjectTypeId()
  {
    return "Role";
  }

  public String remove()
  {
    try
    {
      if (!isNew())
      {
        SecurityConfigBean.getPort().removeRole(getObjectId());
        removed();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }    
    return getControllerBean().show();
  }
  
  public String getDescription()
  {
    RoleMainBean roleMainBean = (RoleMainBean)getBean("roleMainBean");
    Role role = roleMainBean.getRole();
    return getRoleDescription(role);
  }   
  
  public String getDescription(String oid)
  {
    String description = "";
    try
    {
      Role role = SecurityConfigBean.getPort().loadRole(oid);
      description = getRoleDescription(role);
    }
    catch (Exception ex)
    {
      error(ex.getMessage());
    }
    return description;
  }
  
  private String getRoleDescription(Role role)
  {
    StringBuffer buffer = new StringBuffer();  
    if (role.getName() != null)
    {
      buffer.append(role.getName());
      buffer.append(" : ");
    }
    buffer.append("(");
    buffer.append(role.getRoleId());
    buffer.append(")");
    return buffer.toString();
  }

}
