package org.santfeliu.security.web;

import static org.matrix.dic.DictionaryConstants.ROLE_TYPE;
import org.matrix.security.Role;
import static org.matrix.security.SecurityConstants.SECURITY_ADMIN_ROLE;
import org.santfeliu.web.obj.TypifiedPageBean;

public class RoleMainBean extends TypifiedPageBean
{
  private Role role;

  public RoleMainBean()
  {
    super(ROLE_TYPE, SECURITY_ADMIN_ROLE);
    load();
  }

  //Accessors
  public Role getRole()
  {
    return role;
  }

  public void setRole(Role role)
  {
    this.role = role;
  }

  public boolean isRenderShowTypeButton()
  {
    return getRole().getRoleTypeId() != null &&
      getRole().getRoleTypeId().trim().length() > 0;
  }

  //Actions  
  
  public String showType()
  {
    return getControllerBean().showObject("Type",
      getRole().getRoleTypeId());
  }

  @Override
  public String show()
  {
    return "role_main";
  }

  @Override
  public String store()
  {
    try
    {
      role = SecurityConfigBean.getPort().storeRole(role);
      setObjectId(role.getRoleId());
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return show();
  }

  private void load()
  {
    if (isNew())
    {
      role = new Role();
    }
    else
    {
      try
      {
        role = SecurityConfigBean.getPort().loadRole(getObjectId());
      }
      catch (Exception ex)
      {
        getObjectBean().clearObject();
        error(ex);
        role = new Role();
      }
    }
  }
}
