package org.santfeliu.security.web;

import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.security.SecurityManagerPort;
import org.matrix.security.User;
import org.matrix.security.UserFilter;
import org.santfeliu.kernel.web.PersonBean;
import org.santfeliu.web.obj.PageBean;

public class UserMainBean extends PageBean
{
  private User user;
  private String passwordInput;

  public UserMainBean()
  {
    load();
  }

  //Accessors
  public User getUser()
  {
    return user;
  }

  public void setUser(User user)
  {
    this.user = user;
  }

  public String getPasswordInput()
  {
    return passwordInput;
  }

  public void setPasswordInput(String passwordInput)
  {
    this.passwordInput = passwordInput;
  }
  
  public boolean isLocked()
  {
    return user.isLocked() != null && user.isLocked();
  }
  
  public void setLocked(boolean locked)
  {
    user.setLocked(locked);
  }

  //Actions  
  public String show()
  {
    return "user_main";
  }

  @Override
  public String store()
  {
    try
    {
      SecurityManagerPort port = SecurityConfigBean.getPort();
      if (isNew())
      {
        String userId = user.getUserId();
        if (userId != null && userId.trim().length() > 0)
        {
          // check if user exists
          UserFilter filter = new UserFilter();
          filter.getUserId().add(userId);
          if (port.countUsers(filter) > 0)
          {
            error("USER_ALREADY_EXISTS");
            return show();
          }
        }
      }
      passwordInput = passwordInput.trim();
      user.setPassword(passwordInput.length() > 0 ? passwordInput : null);
      user = port.storeUser(user);
      setObjectId(user.getUserId());
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return show();
  }

  public String searchPerson()
  {
    return getControllerBean().searchObject("Person",
      "#{userMainBean.user.personId}");
  }

  public String showPerson()
  {
    //if (user.getPersonId() == null) return null;
    return getControllerBean().showObject("Person", user.getPersonId());    
  }

  public boolean isRenderShowPersonButton()
  {
    return user.getPersonId() != null && user.getPersonId().trim().length() > 0;
  }

  public List<SelectItem> getPersonSelectItems()
  {
    PersonBean personBean = (PersonBean)getBean("personBean");
    return personBean.getSelectItems(user.getPersonId());
  }

  private void load()
  {
    if (isNew())
    {
      user = new User();
    }
    else
    {
      try
      {
        user = SecurityConfigBean.getPort().loadUser(getObjectId());
      }
      catch (Exception ex)
      {
        getObjectBean().clearObject();
        error(ex);
        user = new User();
      }
      passwordInput = "";
    }
  }
}
