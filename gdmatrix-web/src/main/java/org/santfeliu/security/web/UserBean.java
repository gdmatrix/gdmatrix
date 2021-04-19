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

import org.matrix.security.User;
import org.santfeliu.web.obj.ObjectBean;

/**
 *
 * @author realor
 */
public class UserBean extends ObjectBean
{
  public UserBean()
  {
  }

  @Override
  public String getObjectTypeId()
  {
    return "User";
  }

  @Override
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
  
  @Override
  public String getDescription()
  {
    UserMainBean userMainBean = (UserMainBean)getBean("userMainBean");
    User user = userMainBean.getUser();
    return getUserDescription(user);
  }   
  
  @Override
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
    StringBuilder buffer = new StringBuilder();  
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
