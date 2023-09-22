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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.santfeliu.faces.matrixclient.model.ScanMatrixClientModel;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.security.util.StringCipher;
import org.santfeliu.security.util.URLCredentialsCipher;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.Properties;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.servlet.ScannerServlet;
import org.santfeliu.workflow.form.Form;


/**
 *
 * @author blanquepa
 */
@Named
@RequestScoped
public class ScanDocumentWorkflowBean extends WorkflowBean
{
  public static final String ERROR_PREFIX = "ERROR: ";

  private String message;
  private String result;
  private String resultVar;
  private ScanMatrixClientModel model;
  private String token;

  @Inject
  WorkflowInstanceBean instanceBean;

  public ScanDocumentWorkflowBean()
  {
    model = new ScanMatrixClientModel();
    resultVar = "result";
  }

  public void setMessage(String message)
  {
    this.message = message;
  }

  public String getMessage()
  {
    return message;
  }

  public void setResult(String result)
  {
    this.result = result;
  }

  public String getResult()
  {
    return result;
  }

  public String getResultVar()
  {
    return resultVar;
  }

  public void setResultVar(String resultVar)
  {
    this.resultVar = resultVar;
  }

  @Override
  public String show(Form form)
  {
    Properties parameters = form.getParameters();

    instanceBean.setForwardEnabled(false);
    instanceBean.setBackwardEnabled(false);

    for (Object key : parameters.keySet())
    {
      String parameterName = (String)key;
      Object value = parameters.get(parameterName);
      if (value != null)
      {
        if (parameterName.equals("message"))
          message = String.valueOf(value);
        else if (parameterName.equals("resultVar"))
          resultVar = String.valueOf(value);
        else
          addDocumentParameter(parameterName, parameters.get(parameterName));
      }
    }
    //system token
    token =  getToken();
    addDocumentParameter(ScannerServlet.TOKEN_HEADER, token);

    return "/pages/workflow/scan_form.xhtml";
  }

  @Override
  public Map submit()
  {
    HashMap<String, String> variables = new HashMap<>();
    variables.put(resultVar, result);
    variables.put("token", token);
    return variables;
  }

  //MatrixClient methods
  public ScanMatrixClientModel getModel()
  {
    return model;
  }

  public void setModel(ScanMatrixClientModel model)
  {
    this.model = model;
  }

  public void documentScanned()
  {
    try
    {
      result = (String)model.parseResult();
      if (result != null)
      {
        instanceBean.forward();
      }
    }
    catch (Exception ex)
    {
      renderErrorMessage(ex.getMessage());
      instanceBean.updateForm();
    }
  }

  private String getToken()
  {
    //DateTime
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    String dt = df.format(new Date());

    //Credentials
    String secret =
      MatrixConfig.getProperty(ScannerServlet.CIPHER_SECRET_PROPERTY);
    StringCipher strCipher = new StringCipher(secret);

    Credentials credentials =
      UserSessionBean.getCurrentInstance().getCredentials();
    URLCredentialsCipher urlCipher = new URLCredentialsCipher(secret);
    String urlCipherString =
      urlCipher.putCredentials(model.getServletUrl(), credentials);
    String ciphCredentials = strCipher.encrypt(urlCipherString);

    return strCipher.encrypt(ScannerServlet.formatToken(
      ScannerServlet.TOKEN_PREFIX, dt, ciphCredentials));
  }

  private void addDocumentParameter(String name, Object value)
  {
    model.putParameter(ScannerServlet.HEADERS_PREFIX + name, value);
  }

  private void renderErrorMessage(String message)
  {
    try
    {
      ResourceBundle bundle = ResourceBundle.getBundle(
        "org.santfeliu.workflow.web.resources.WorkflowBundle", getLocale());
      String localizedMessage = bundle.getString(message);
      error(localizedMessage);
    }
    catch (Exception ex)
    {
      error(message);
    }
  }
}
