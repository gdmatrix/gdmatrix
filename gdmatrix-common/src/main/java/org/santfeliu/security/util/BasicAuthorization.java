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
package org.santfeliu.security.util;

import java.util.Base64;


/**
 *
 * @author realor
 */
public class BasicAuthorization
{
  private String userId;
  private String password;

  public void fromString(String autho)
  {
    if (autho != null && autho.startsWith("Basic "))
    {
      String basic = autho.substring(6);
      String userPassString = new String(Base64.getMimeDecoder().decode(basic));
      String[] userPass = userPassString.split(":");
      if (userPass.length > 0)
      {
        this.userId = userPass[0].trim();
        if (userPass.length > 1)
        {
          this.password = userPass[1];
        }
      }
    }
  }

  @Override
  public String toString()
  {
    String pass = (password == null) ? "" : password;
    String userPassString = userId + ":" + pass;
    String autho = "Basic " +
      Base64.getMimeEncoder().encodeToString(userPassString.getBytes());

    return autho;
  }

  public void setUserId(String userId)
  {
    this.userId = userId;
  }

  public String getUserId()
  {
    return userId;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public String getPassword()
  {
    return password;
  }

  public static void main(String[] args)
  {
    BasicAuthorization a = new BasicAuthorization();
    a.setUserId("realor");
    a.setPassword("áéíò");
    String autho = a.toString();
    System.out.println(autho);
    a.fromString(autho);
    System.out.println(a.getUserId());
    System.out.println(a.getPassword());
  }
}
