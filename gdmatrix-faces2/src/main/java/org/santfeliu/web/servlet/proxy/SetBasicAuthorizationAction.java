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
package org.santfeliu.web.servlet.proxy;

import java.net.HttpURLConnection;
import javax.servlet.http.HttpServletRequest;
import org.matrix.security.SecurityConstants;
import org.santfeliu.security.User;
import org.santfeliu.security.util.BasicAuthorization;

/**
 *
 * @author realor
 */
public class SetBasicAuthorizationAction extends ProxyAction
{
  private String userId;
  private String password;

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public String getUserId()
  {
    return userId;
  }

  public void setUserId(String userId)
  {
    this.userId = userId;
  }

  @Override
  public void execute(HttpURLConnection conn, HttpServletRequest req, User user)
  {
    BasicAuthorization basic = new BasicAuthorization();
    basic.setUserId(userId == null ? user.getUserId() : userId);
    basic.setPassword(password == null ? user.getPassword() : password);
    if (!SecurityConstants.ANONYMOUS.equals(basic.getUserId()))
    {
      conn.setRequestProperty("Authorization", basic.toString());
    }
  }

  @Override
  public String toString()
  {
    return "SetBasicAuthorization " + userId + " " + password;
  }
}
