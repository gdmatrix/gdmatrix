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

import cat.aoc.valid.ValidClient;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.servlet.http.HttpServletRequest;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;
import static org.santfeliu.security.web.ValidServlet.ACCESS_TOKEN_ATTRIBUTE;
import static org.santfeliu.security.web.ValidServlet.ACTION_ATTRIBUTE;
import static org.santfeliu.security.web.ValidServlet.AUTH_ACTION;
import static org.santfeliu.security.web.ValidServlet.RETURN_PARAMS_ATTRIBUTE;

/**
 *
 * @author realor
 */
public class ValidBean extends FacesBean
{
  public ValidBean()
  {
    createMessages();
  }

  public void login()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    ExternalContext extContext = getExternalContext();
    HttpServletRequest request = (HttpServletRequest)extContext.getRequest();
    String returnParams = request.getQueryString();
    try
    {
      ValidClient client = new ValidClient();
      client.setBaseUrl(MatrixConfig.getProperty("valid.baseUrl"));
      client.setClientId(MatrixConfig.getProperty("valid.clientId"));
      client.setClientSecret(MatrixConfig.getProperty("valid.clientSecret"));
      client.setRedirectUrl(MatrixConfig.getProperty("valid.redirectUrl"));
      userSessionBean.setAttribute(ACTION_ATTRIBUTE, AUTH_ACTION);
      userSessionBean.setAttribute(RETURN_PARAMS_ATTRIBUTE, returnParams);
      String loginUrl = client.generateOAuthLoginUrl("");
      extContext.redirect(loginUrl);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void logout() // called from UserSessionBean.logout()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String accessToken =
      (String)userSessionBean.getAttribute(ACCESS_TOKEN_ATTRIBUTE);
    if (accessToken != null)
    {
      try
      {
        ValidClient client = new ValidClient();
        client.setBaseUrl(MatrixConfig.getProperty("valid.baseUrl"));
        client.setClientId(MatrixConfig.getProperty("valid.clientId"));
        client.setClientSecret(MatrixConfig.getProperty("valid.clientSecret"));
        client.setRedirectUrl(MatrixConfig.getProperty("valid.redirectUrl"));
        client.revokeAccessToken(accessToken);
        client.logoutAuthorizationCode(accessToken);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
  }

  private void createMessages()
  {
    String error = (String)getExternalContext().getRequestMap().get("error");
    if (error != null)
    {
      if ("NO_SESSION".equals(error))
      {
        message("org.santfeliu.security.web.resources.ValidBundle",
        "noSession", new Object[]{}, FacesMessage.SEVERITY_INFO);
      }
      else if ("SESSION_CANCEL".equals(error))
      {
        message("org.santfeliu.security.web.resources.ValidBundle",
        "sessionCancel", new Object[]{}, FacesMessage.SEVERITY_INFO);
      }
      else
      {
        error(error);
      }
    }
  }

  public String getTitle()
  {
    return "VALid";
  }
}


