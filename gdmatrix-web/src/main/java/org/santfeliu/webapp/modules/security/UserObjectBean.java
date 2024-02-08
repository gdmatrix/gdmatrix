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
import java.util.Calendar;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.security.SecurityMetaData;
import org.matrix.security.User;
import org.santfeliu.util.TextUtils;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import static org.santfeliu.webapp.modules.security.SecurityModuleBean.getPort;

/**
 *
 * @author realor
 */
@Named
@ViewScoped
public class UserObjectBean extends ObjectBean
{
  private User user = new User();
  private Boolean renderUserLockPanel;
  private SecurityMetaData metaData;
  
  @Inject
  UserTypeBean userTypeBean;

  @Inject
  UserFinderBean userFinderBean;

  @PostConstruct
  public void init()
  {
  }

  public User getUser()
  {
    return user;
  }

  public void setUser(User user)
  {
    this.user = user;
  }

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.USER_TYPE;
  }

  @Override
  public UserTypeBean getTypeBean()
  {
    return userTypeBean;
  }

  @Override
  public User getObject()
  {
    return isNew() ? null : user;
  }

  @Override
  public String getDescription()
  {
    return isNew() ? "" : getDescription(user.getUserId());
  }

  public String getDescription(String userId)
  {
    return getTypeBean().getDescription(userId);
  }

  @Override
  public UserFinderBean getFinderBean()
  {
    return userFinderBean;
  }

  @Override
  public void loadObject() throws Exception
  {
    if (!NEW_OBJECT_ID.equals(objectId))
    {
      user = getPort(false).loadUser(objectId);
      renderUserLockPanel = null;
    }
    else
    {
      user = new User();
      renderUserLockPanel = false;
    }
  }

  @Override
  public void storeObject() throws Exception
  {
    user = getPort(false).storeUser(user);
    setObjectId(user.getUserId());
    userFinderBean.outdate();
    renderUserLockPanel = null;
  }

  @Override
  public void removeObject() throws Exception
  {
    getPort(false).removeUser(user.getUserId());
    userFinderBean.outdate();
    renderUserLockPanel = null;
  }

  public boolean isLocked()
  {
    return user.isLocked() != null && user.isLocked();
  }

  public void setLocked(boolean locked)
  {
    user.setLocked(locked);
  }

  public Boolean getRenderUserLockPanel()
  {
    if (renderUserLockPanel == null)
    {
      renderUserLockPanel = false;
      if (!isNew())
      {
        try
        {
          renderUserLockPanel =
            getPort(false).isUserLockControlEnabled(getObjectId());
        }
        catch (Exception ex)
        {
        }
      }
    }
    return renderUserLockPanel;
  }

  public void setRenderUserLockPanel(Boolean renderUserLockPanel)
  {
    this.renderUserLockPanel = renderUserLockPanel;
  }

  public boolean isUserLocked()
  {
    return "locked".equals(getUserState());
  }

  public boolean isUserUnlocked()
  {
    return "unlocked".equals(getUserState());
  }

  public boolean isUserUnlockedAuto()
  {
    return "unlocked_auto".equals(getUserState());
  }

  public Integer getAttemptsToLock()
  {
    try
    {
      int maxFailedLoginAttempts = 
        getSecurityMetaData().getMaxFailedLoginAttempts();
      int failedLoginAttempts = (user.getFailedLoginAttempts() == null ? 0 :
        user.getFailedLoginAttempts());
      return (maxFailedLoginAttempts - failedLoginAttempts);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  public String getAutoUnlockDateTime()
  {
    try
    {
      return TextUtils.formatDate(getAutoUnlockDate(), "dd/MM/yyyy HH:mm:ss");
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  public Integer getAttemptsToIntrusion()
  {
    try
    {
      int minIntrusionAttempts = 
        getSecurityMetaData().getMinIntrusionAttempts();
      int failedLoginAttempts = (user.getFailedLoginAttempts() == null ? 0 :
        user.getFailedLoginAttempts());
      return (minIntrusionAttempts - failedLoginAttempts);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  @Override
  public Serializable saveState()
  {
    return new Object[] { user, renderUserLockPanel, metaData };
  }

  @Override
  public void restoreState(Serializable state)
  {
    Object[] array = (Object[])state;
    this.user = (User)array[0];
    this.renderUserLockPanel = (Boolean)array[1];
    this.metaData = (SecurityMetaData)array[2];
  }

  private String getUserState()
  {
    try
    {
      int failedLoginAttempts = (user.getFailedLoginAttempts() == null ? 0 :
        user.getFailedLoginAttempts());
      int maxFailedLoginAttempts = 
        getSecurityMetaData().getMaxFailedLoginAttempts();
      if (failedLoginAttempts < maxFailedLoginAttempts)
      {
        return "unlocked";
      }
      else
      {
        Date autoUnlockDate = getAutoUnlockDate();
        if (autoUnlockDate != null)
        {
          Date now = new java.util.Date();
          if (now.after(autoUnlockDate))
          {
            return "unlocked_auto";
          }
          else
          {
            return "locked";
          }
        }
        else
        {
          return null;
        }
      }
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  private Date getAutoUnlockDate()
  {
    try
    {
      int autoUnlockMarginTime = 
        getSecurityMetaData().getAutoUnlockMarginTime();
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(TextUtils.parseInternalDate(
        user.getLastFailedLoginDateTime()));
      calendar.add(Calendar.SECOND, autoUnlockMarginTime);
      return calendar.getTime();
    }
    catch (Exception ex)
    {
      return null;
    }
  }
  
  private SecurityMetaData getSecurityMetaData() throws Exception
  {
    if (metaData == null)
    {
      metaData = getPort(false).getSecurityMetaData();
    }
    return metaData;
  }  

}
