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
package org.santfeliu.security.service;

import org.matrix.security.User;

import org.santfeliu.jpa.JPAUtils;

/**
 *
 * @author realor
 */
public class DBUser extends User
{
  private int lockedValue;
  private String stddgr;
  private String stdhgr;
  private String stddmod;
  private String stdhmod;

  public DBUser()
  {
  }
  
  public DBUser(User user)
  {
    this.userId = user.getUserId();
    this.displayName = user.getDisplayName();
    this.failedLoginAttempts = user.getFailedLoginAttempts();
    if (user.isLocked() != null && user.isLocked())
    {
      lockedValue = 1;
    }
    else
    {
      lockedValue = 0;
    }
  }

  public void copyTo(User user)
  {
    JPAUtils.copy(this, user);

    user.setUserId(userId.trim());
    user.setPassword(null);
    user.setLocked(lockedValue == 1);
    user.setLockControlEnabled(lockControlEnabled);
    
    if (stddgr != null && stdhgr != null)
    {
      user.setCreationDateTime(stddgr + stdhgr);
    }

    if (stddmod != null && stdhmod != null)
    {
      user.setChangeDateTime(stddmod + stdhmod);
    }    
  }
  
  public void copyFrom(User user)
  {
    // only used to update display & locked fields
    this.displayName = user.getDisplayName();
    this.locked = user.isLocked();
    this.failedLoginAttempts = user.getFailedLoginAttempts();
    if (user.isLocked() != null && user.isLocked())
    {
      this.lockedValue = 1;
    }
    else      
    {
      this.lockedValue = 0;
    }
  }
  
  public int getLockedValue()
  {
    return lockedValue;
  }

  public void setLockedValue(int lockedValue)
  {
    this.lockedValue = lockedValue;
  }
  
  public String getStddgr() 
  {
    return stddgr;
  }

  public void setStddgr(String stddgr) 
  {
    this.stddgr = stddgr;
  }

  public String getStdhgr() 
  {
    return stdhgr;
  }

  public void setStdhgr(String stdhgr) 
  {
    this.stdhgr = stdhgr;
  }

  public String getStddmod()
  {
    return stddmod;
  }

  public void setStddmod(String stddmod)
  {
    this.stddmod = stddmod;
  }

  public String getStdhmod()
  {
    return stdhmod;
  }

  public void setStdhmod(String stdhmod)
  {
    this.stdhmod = stdhmod;
  }
}
