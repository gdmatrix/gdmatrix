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

import java.io.Serializable;
import java.util.Enumeration;
import javax.faces.application.FacesMessage;
import javax.faces.component.html.HtmlInputSecret;
import javax.faces.component.html.HtmlInputText;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.matrix.security.SecurityConstants;
import org.santfeliu.cms.CMSListener;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.HttpUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import static javax.faces.render.ResponseStateManager.VIEW_STATE_PARAM;

/**
 *
 * @author realor
 */
public class LoginBean extends WebBean implements Serializable
{
  public static final String LOGIN_TITLE_PROP = "loginTitle";
  public static final String LOGIN_IMAGE_PROP = "loginImage";
  public static final String AUTHENTICATION_LEVEL_PARAM = "authenticationLevel";
  public static final String SIGNATURE_LEVEL_PARAM = "signatureLevel";

  // simple login page
  private String loginTitle;
  private String loginImage;
  private String queryString;
  private int requestedAuthenticationLevel = 0;
  private int requestedSignatureLevel = 0;

  // embedded login fields
  private transient HtmlInputText usernameInputText = new HtmlInputText();
  private transient HtmlInputSecret passwordInputSecret = new HtmlInputSecret();

  // login message
  private transient String loginMessage;

  public LoginBean()
  {
  }

  public String getLoginTitle()
  {
    return loginTitle;
  }

  public String getLoginImage()
  {
    return loginImage;
  }

  public void setUsernameInputText(HtmlInputText usernameInputText)
  {
    this.usernameInputText = usernameInputText;
  }

  public HtmlInputText getUsernameInputText()
  {
    return usernameInputText;
  }

  public void setPasswordInputSecret(HtmlInputSecret passwordInputSecret)
  {
    this.passwordInputSecret = passwordInputSecret;
  }

  public HtmlInputSecret getPasswordInputSecret()
  {
    return passwordInputSecret;
  }

  public void setLoginMessage(String loginMessage)
  {
    this.loginMessage = loginMessage;
  }

  public String getLoginMessage()
  {
    return loginMessage;
  }

  public void setQueryString(String queryString)
  {
    this.queryString = queryString;
  }

  public String getQueryString()
  {
    return queryString;
  }

  public int getRequestedAuthenticationLevel()
  {
    return requestedAuthenticationLevel;
  }

  public int getRequestedSignatureLevel()
  {
    return requestedSignatureLevel;
  }

  public String getLoginURL()
  {
    ExternalContext extContext = getExternalContext();
    HttpServletRequest request = (HttpServletRequest)extContext.getRequest();

    String url = HttpUtils.getServerSecureURL(request,
      CMSListener.LOGIN_URI, request.getQueryString());

    return url;
  }

  public String getLoginCertificateURL()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    ExternalContext externalContext = context.getExternalContext();
    HttpServletRequest request =
      (HttpServletRequest)externalContext.getRequest();

    return HttpUtils.getClientSecureURL(request, CMSListener.GO_URI,
      request.getQueryString());
  }

  public String getExitURL()
  {
    return getCancelURL();
  }

  public String getCancelURL()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    ExternalContext externalContext = context.getExternalContext();
    HttpServletRequest request =
    (HttpServletRequest)externalContext.getRequest();
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor menuItem = userSessionBean.getSelectedMenuItem();
    if (menuItem.isNull())
    {
      menuItem = userSessionBean.getMenuModel().getRootMenuItem();
    }
    String requestQueryString = CMSListener.XMID_PARAM + "=" + menuItem.getMid();
    return HttpUtils.getServerSecureURL(request, CMSListener.GO_URI,
      requestQueryString);
  }

  public boolean isLoginCertificateFailed()
  {
    ExternalContext extContext = getExternalContext();
    HttpServletRequest request = (HttpServletRequest)extContext.getRequest();

    String port =
      MatrixConfig.getProperty("org.santfeliu.web.clientSecurePort");
    return port.equals(String.valueOf(HttpUtils.getServerPort(request)));
  }

  // **** action methods ****

  // login from page with username/password
  public String login()
  {
    try
    {
      loginMessage = null;
      String username = (String)usernameInputText.getValue();
      String password = (String)passwordInputSecret.getValue();
      if (username != null && username.trim().length() > 0)
      {
        UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
        userSessionBean.login(username, password);
        userSessionBean.executeSelectedMenuItem();
      }
    }
    catch (Exception ex)
    {
      FacesMessage message = FacesUtils.getFacesMessage(ex);
      loginMessage = message.getSummary();
    }
    return null;
  }

  // redirect to original page if login successfull
  public String loginRedirect()
  {
    try
    {
      String username = (String)usernameInputText.getValue();
      String password = (String)passwordInputSecret.getValue();
      if (username != null && username.trim().length() > 0)
      {
        UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
        userSessionBean.login(username, password);

        FacesContext context = getFacesContext();
        HttpServletRequest request =
          (HttpServletRequest)context.getExternalContext().getRequest();
        HttpServletResponse response =
          (HttpServletResponse)context.getExternalContext().getResponse();

        String url = HttpUtils.getServerSecureURL(request,
          CMSListener.GO_URI, queryString);

        response.sendRedirect(url);
        context.responseComplete();
      }
    }
    catch (Exception ex)
    {
      FacesMessage message = FacesUtils.getFacesMessage(ex);
      loginMessage = message.getSummary();
    }
    return null;
  }

  // login from page with certificate
  public String loginCertificate()
  {
    loginMessage = null;
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    try
    {
      userSessionBean.loginCertificate();
    }
    catch (Exception ex)
    {
      userSessionBean.showLoginPage(ex);
      return null;
    }

    try
    {
      userSessionBean.executeSelectedMenuItem();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  // logout from page
  public String logout()
  {
    usernameInputText.setValue(null);
    passwordInputSecret.setValue(null);
    loginMessage = null;
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    userSessionBean.logout(); // redirect
    return null;
  }

  public String showLogin()
  {
    ExternalContext extContext = getExternalContext();
    HttpServletRequest request = (HttpServletRequest)extContext.getRequest();
    readParameters(request);

    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor menuItem =
      (MenuItemCursor)request.getAttribute(CMSListener.NEXT_MENU_ITEM_ATTR);
    if (menuItem == null) menuItem = userSessionBean.getSelectedMenuItem();

    loginTitle = menuItem.getProperty(LOGIN_TITLE_PROP);
    if (loginTitle == null)
      loginTitle = MatrixConfig.getProperty("org.santfeliu.web.loginTitle");
    loginImage = menuItem.getProperty(LOGIN_IMAGE_PROP);

    String userId = request.getParameter(SecurityConstants.USERID_PARAMETER);
    if (userId != null && !userSessionBean.getUserId().equals(userId))
      usernameInputText.setValue(userId);

    return "login";
  }

  private void readParameters(HttpServletRequest request)
  {
    StringBuilder buffer = new StringBuilder();
    String method = request.getMethod();
    if ("GET".equals(method))
    {
      Enumeration names = request.getParameterNames();
      while (names.hasMoreElements())
      {
        String name = (String)names.nextElement();
        String value = request.getParameter(name);
        if (AUTHENTICATION_LEVEL_PARAM.equals(name))
        {
          try
          {
            requestedAuthenticationLevel = Integer.parseInt(value);
          }
          catch (Exception ex)
          {
            requestedAuthenticationLevel = 0;
          }
        }
        else if (SIGNATURE_LEVEL_PARAM.equals(name))
        {
          try
          {
            requestedSignatureLevel = Integer.parseInt(value);
          }
          catch (Exception ex)
          {
            requestedSignatureLevel = 0;
          }
        }
        else
        {
          if (buffer.length() > 0)
          {
            buffer.append("&");
          }
          buffer.append(name).append("=").append(value);
        }
      }
    }
    else // POST
    {
      String mid = request.getParameter(CMSListener.XMID_PARAM);
      if (mid == null) request.getParameter(CMSListener.SMID_PARAM);
      if (mid != null)
      {
        buffer.append(CMSListener.XMID_PARAM + "=").append(mid);
      }
    }
    if (buffer.length() > 0)
    {
      queryString = buffer.toString();
    }
    else
    {
      queryString = null;
    }
  }
}
