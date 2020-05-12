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

import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.security.SecurityManagerPort;
import org.matrix.security.User;
import org.matrix.security.UserFilter;
import org.santfeliu.kernel.web.PersonBean;
import org.santfeliu.web.obj.PageBean;

/**
 *
 * @author unknown
 */
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
