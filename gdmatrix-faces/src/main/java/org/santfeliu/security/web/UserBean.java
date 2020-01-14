package org.santfeliu.security.web;

import org.matrix.security.User;
import org.santfeliu.web.obj.ObjectBean;

public class UserBean extends ObjectBean
{
  public UserBean()
  {
  }

  public String getObjectTypeId()
  {
    return "User";
  }

  public String remove()
  {
    try
    {
      if (!isNew())
      {
        SecurityConfigBean.getPort().removeUser(getObjectId());
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
    UserMainBean userMainBean = (UserMainBean)getBean("userMainBean");
    User user = userMainBean.getUser();
    return getUserDescription(user);
  }   
  
  public String getDescription(String oid)
  {
    String description = "";
    try
    {
      User user = SecurityConfigBean.getPort().loadUser(oid);
      description = getUserDescription(user);
    }
    catch (Exception ex)
    {
      error(ex.getMessage());
    }
    return description;
  }
  
  private String getUserDescription(User user)
  {
    StringBuffer buffer = new StringBuffer();  
    if (user.getDisplayName() != null)
    {
      buffer.append(user.getDisplayName());
      buffer.append(" : ");
    }
    buffer.append("(");
    buffer.append(user.getUserId());
    buffer.append(")");
    return buffer.toString();
  }

}
