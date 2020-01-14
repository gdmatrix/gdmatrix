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

import org.santfeliu.faces.FacesBean;
import org.santfeliu.faces.browser.HtmlBrowser;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author unknown
 */
public class PasswordBean extends FacesBean
{
  private String oldPassword;
  private String newPassword1;
  private String newPassword2;
  private String passwordMessage;
  private boolean showMessages;
  private HtmlBrowser browser;
  private HtmlBrowser browserOk;
  
  public PasswordBean()
  {
  }

  public void setOldPassword(String oldPassword)
  {
    this.oldPassword = oldPassword;
  }

  public String getOldPassword()
  {
    return oldPassword;
  }

  public void setNewPassword1(String newPassword1)
  {
    this.newPassword1 = newPassword1;
  }

  public String getNewPassword1()
  {
    return newPassword1;
  }

  public void setNewPassword2(String newPassword2)
  {
    this.newPassword2 = newPassword2;
  }

  public String getNewPassword2()
  {
    return newPassword2;
  }

  public void setPasswordMessage(String passwordMessage)
  {
    this.passwordMessage = passwordMessage;
  }

  public String getPasswordMessage()
  {
    return passwordMessage;
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
      browser.setUrl(getBrowserUrl("changePassDoccod"));
  }

  public HtmlBrowser getBrowser()
  {
    return browser;
  }

  public void setBrowserOk(HtmlBrowser browserOk)
  {
    this.browserOk = browserOk;
    if (browserOk.getUrl() == null)
      browserOk.setUrl(getBrowserUrl("changePassDoccodOk"));
  }

  public HtmlBrowser getBrowserOk()
  {
    return browserOk;
  }
  // action methods
  public String changePassword()
  {
    try
    {
      showMessages = true;
      if (newPassword1.equals(newPassword2))
      {
        UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
        String userId = userSessionBean.getUserId();
        SecurityManagerPort port = SecurityConfigBean.getPort();
        port.changePassword(userId, oldPassword, newPassword1);
        return "ok";
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
