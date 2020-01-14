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
package org.santfeliu.workflow.web;

import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.workflow.form.Form;

/**
 *
 * @author realor
 */
public class LoginFormBean extends FormBean
{
  private String message;
  private String formVariable;
  private String userId;
  private String password;
  private boolean loginByPassword;
  private boolean loginByCertificate;
  private boolean loginByValid;
  private boolean loginByMobileid;

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

  public boolean isLoginByMobileid()
  {
    return loginByMobileid;
  }
  
  public String getValidUrl()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String mid = userSessionBean.getMenuModel().getSelectedMid();
    
    InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
    String instanceId = instanceBean.getInstanceId();
    String accessToken = instanceBean.getAccessToken();
    if (accessToken == null) accessToken = "";
    
    return "/valid?xmid=" + mid + "&" + 
      InstanceListBean.ACCESS_TOKEN_PARAM + "=" + accessToken + "&" + 
      InstanceListBean.INSTANCEID_PARAM + "=" + instanceId;
  }

  public String getMobileidUrl()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String mid = userSessionBean.getMenuModel().getSelectedMid();
    
    InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
    String instanceId = instanceBean.getInstanceId();
    String accessToken = instanceBean.getAccessToken();
    if (accessToken == null) accessToken = "";
    
    return "/mobileid?xmid=" + mid + "&" + 
      InstanceListBean.ACCESS_TOKEN_PARAM + "=" + accessToken + "&" + 
      InstanceListBean.INSTANCEID_PARAM + "=" + instanceId;
  }
  
  @Override
  public String show(Form form)
  {
    formVariable = form.getVariable();
    message = (String)form.getParameters().get("message");
    loginByPassword = "true".equals(form.getParameters().get("password"));
    loginByCertificate = "true".equals(form.getParameters().get("certificate"));
    loginByValid = "true".equals(form.getParameters().get("valid"));
    loginByMobileid = "true".equals(form.getParameters().get("mobileid"));

    InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
    instanceBean.setAccessToken(UUID.randomUUID().toString() + 
      InstanceListBean.LOGIN_ACCESS_TOKEN_SEPARATOR + formVariable);    
    instanceBean.setForwardEnabled(false);

    return "login_form";
  }

  public String login()
  {
    try
    {
      if (!StringUtils.isBlank(userId) && !StringUtils.isBlank(password))
      {
        UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
        userSessionBean.login(userId, password);

        InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
        return instanceBean.forward();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String loginCertificate()
  {
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
      InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
      return instanceBean.forward();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  @Override
  public Map submit()
  {
    try
    {
      InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
      instanceBean.login(formVariable);      
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
}
