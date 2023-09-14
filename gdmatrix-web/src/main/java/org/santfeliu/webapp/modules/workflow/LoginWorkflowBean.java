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
package org.santfeliu.webapp.modules.workflow;

import cat.aoc.valid.ValidClient;
import java.util.Map;
import java.util.UUID;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.workflow.form.Form;
import static org.santfeliu.security.web.ValidServlet.ACTION_ATTRIBUTE;
import static org.santfeliu.security.web.ValidServlet.AUTH_ACTION;
import static org.santfeliu.security.web.ValidServlet.RETURN_PARAMS_ATTRIBUTE;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class LoginWorkflowBean extends WorkflowBean
{
  private String message;
  private String formVariable;
  private String userId;
  private String password;
  private boolean loginByPassword;
  private boolean loginByCertificate;
  private boolean loginByValid;

  @Inject
  WorkflowInstanceBean instanceBean;

  public String getMessage()
  {
    return message;
  }

  public void setMessage(String message)
  {
    this.message = message;
  }

  public String getUserId()
  {
    return userId;
  }

  public void setUserId(String userId)
  {
    this.userId = userId;
  }

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public boolean isLoginByCertificate()
  {
    return loginByCertificate;
  }

  public boolean isLoginByPassword()
  {
    return loginByPassword;
  }

  public boolean isLoginByValid()
  {
    return loginByValid;
  }

  @Deprecated
  public boolean isLoginByMobileid()
  {
    return false;
  }

  @Override
  public String show(Form form)
  {
    formVariable = form.getVariable();
    message = (String)form.getParameters().get("message");
    loginByPassword = "true".equals(form.getParameters().get("password"));
    loginByCertificate = "true".equals(form.getParameters().get("certificate"));
    loginByValid = "true".equals(form.getParameters().get("valid"));

    instanceBean.setAccessToken(UUID.randomUUID().toString() +
      WorkflowInstanceListBean.LOGIN_ACCESS_TOKEN_SEPARATOR + formVariable);
    instanceBean.setForwardEnabled(false);

    return "/pages/workflow/login_form.xhtml";
  }

  public void login()
  {
    try
    {
      if (!StringUtils.isBlank(userId) && !StringUtils.isBlank(password))
      {
        UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
        userSessionBean.login(userId, password);

        instanceBean.forward();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void loginValid()
  {
    try
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();

      ValidClient client = new ValidClient();
      client.setBaseUrl(MatrixConfig.getProperty("valid.baseUrl"));
      client.setClientId(MatrixConfig.getProperty("valid.clientId"));
      client.setClientSecret(MatrixConfig.getProperty("valid.clientSecret"));
      client.setRedirectUrl(MatrixConfig.getProperty("valid.redirectUrl"));

      String wfInstanceId = instanceBean.getInstanceId();
      String wfAccessToken = instanceBean.getAccessToken();
      if (wfAccessToken == null) wfAccessToken = "";

      String mid = userSessionBean.getSelectedMid();
      String returnParams = "xmid=" + mid + "&" +
        WorkflowInstanceListBean.INSTANCEID_PARAM + "=" + wfInstanceId + "&" +
        WorkflowInstanceListBean.ACCESS_TOKEN_PARAM + "=" + wfAccessToken;

      userSessionBean.setAttribute(ACTION_ATTRIBUTE, AUTH_ACTION);
      userSessionBean.setAttribute(RETURN_PARAMS_ATTRIBUTE, returnParams);
      String loginUrl = client.generateOAuthLoginUrl("");
      getExternalContext().redirect(loginUrl);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void loginCertificate()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    try
    {
      userSessionBean.loginCertificate();
    }
    catch (Exception ex)
    {
      userSessionBean.showLoginPage(ex);
      return;
    }

    try
    {
      instanceBean.forward();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  @Override
  public Map submit()
  {
    try
    {
      instanceBean.login(formVariable);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
}
