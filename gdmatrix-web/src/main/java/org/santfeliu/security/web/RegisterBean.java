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

import org.matrix.security.SecurityManagerPort;
import org.matrix.security.User;

import org.santfeliu.faces.FacesBean;
import org.santfeliu.faces.browser.HtmlBrowser;
import org.santfeliu.faces.menu.model.MenuItemCursor;

import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author realor
 */
public class RegisterBean extends FacesBean
{
  private String username;
  private String displayName;
  private String password1;
  private String password2;
  private boolean showMessages;
  private HtmlBrowser browser;
  private HtmlBrowser browserOk;
  

  public RegisterBean()
  {
   
  }

  public void setUsername(String username)
  {
    this.username = username;
  }

  public String getUsername()
  {
    return username;
  }

  public void setDisplayName(String displayName)
  {
    this.displayName = displayName;
  }

  public String getDisplayName()
  {
    return displayName;
  }

  public void setPassword1(String password1)
  {
    this.password1 = password1;
  }

  public String getPassword1()
  {
    return password1;
  }

  public void setPassword2(String password2)
  {
    this.password2 = password2;
  }

  public String getPassword2()
  {
    return password2;
  }

  public void setShowMessages(boolean showMessages)
  {
    this.showMessages = showMessages;
  }

  public boolean isShowMessages()
  {
    return showMessages;
  }

  public void setBrowser(HtmlBrowser browser)
  {
    this.browser = browser;
    if (browser.getUrl() == null)
      browser.setUrl(getBrowserUrl("registerDoccod"));    
  }

  public HtmlBrowser getBrowser()
  {
    return browser;
  }

  public void setBrowserOk(HtmlBrowser browserOk)
  {
    this.browserOk = browserOk;
    if (browserOk.getUrl() == null)
      browserOk.setUrl(getBrowserUrl("registerDoccodOk"));
  }

  public HtmlBrowser getBrowserOk()
  {
    return browserOk;
  }
  
  // action methods  
  public String register()
  {
    try
    {
      showMessages = true;
      if (password1.equals(password2))
      {
        SecurityManagerPort port = SecurityConfigBean.getPort(true);
        try
        {
          port.loadUser(username);
          error("USER_ALREADY_EXISTS");
        }
        catch (Exception ex)
        {
          // user not found
          User user = new User();
          user.setUserId(username);
          user.setPassword(password1);
          user.setDisplayName(displayName);
          port.storeUser(user);
          return "ok";
        }
      }
      else
      {
        error("PASSWORD_MISMATCH");
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  //private Methods
  private String getBrowserUrl(String MIproperty)
  { 
    MenuItemCursor cursor = 
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    String docId = (String)cursor.getProperties().get(MIproperty);
    if (docId == null) return null;

    return getContextURL() + "/documents/" + docId;
  }
}
