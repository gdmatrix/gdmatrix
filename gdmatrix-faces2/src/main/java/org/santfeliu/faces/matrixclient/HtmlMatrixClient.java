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
package org.santfeliu.faces.matrixclient;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;
import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.matrixclient.model.HtmlMatrixClientModel;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.security.util.StringCipher;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 */
@FacesComponent(value = "HtmlMatrixClient")
public class HtmlMatrixClient extends UICommand implements Serializable
{
  private static final String MATRIX_CLIENT_ENCODED = "MATRIX_CLIENT_ENCODED";
  private static final String WSDIRECTORY_PATH = "wsdirectory";
  private static final int CLIENT_VERSION = 6;
  
  private String _command;
  private Map _result;
  private Map _properties;
  private String _function;
  private String _helpUrl;
  
  private HtmlMatrixClientModel _model;  

  public HtmlMatrixClient()
  {
  }

  @Override
  public String getFamily()
  {
    return "MatrixClient";
  }

  public void setCommand(String command)
  {
    this._command = command;
  }

  public String getCommand()
  {
    if (_command != null) return _command;
    ValueExpression ve = getValueExpression("command");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setResult(Map result)
  {
    this._result = result;
  }

  public Map getResult()
  {
    if (_result != null) return _result;
    ValueExpression ve = getValueExpression("result");
    return ve != null ? (Map)ve.getValue(getFacesContext().getELContext()) : null;
  }
  
  public void setProperties(Map properties)
  {
    this._properties = properties;
  }

  public Map getProperties()
  {
    if (_properties != null) return _properties;
    ValueExpression ve = getValueExpression("properties");
    return ve != null ? (Map)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setFunction(String function)
  {
    this._function = function;
  }

  public String getFunction()
  {
    if (_function != null) return _function;
    ValueExpression ve = getValueExpression("function");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public String getHelpUrl() 
  {
    if (_helpUrl != null) return _helpUrl;
    ValueExpression ve = getValueExpression("helpUrl");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setHelpUrl(String _helpUrl) {
    this._helpUrl = _helpUrl;
  }

  public void setModel(HtmlMatrixClientModel _model)
  {
    this._model = _model;
  }

  public HtmlMatrixClientModel getModel()
  {
    if (_model != null) return _model;
    ValueExpression ve = getValueExpression("model");
    return ve != null ? (HtmlMatrixClientModel)ve.getValue(getFacesContext().getELContext()) : null;
  }
  
  @Override
  public void decode(FacesContext context)
  {
    try
    {
      String clientId = getClientId(context);
      Map parameterMap = context.getExternalContext().getRequestParameterMap();
      Object command = parameterMap.get(clientId + "_command");
      if (command != null) 
      {
        String sCommand = String.valueOf(command);
        if (sCommand.length() > 0)
          _command = sCommand;

        Object result = parameterMap.get(clientId + "_result");                
        String sResult = String.valueOf(result);
        if (sResult.length() > 0)        
        {
          Map resultMap = (Map) new JSONParser().parse(sResult);          
          _result = resultMap;
          if (getModel() != null)
            getModel().setResult(resultMap);
          queueEvent(new ActionEvent(this));        
        }
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    if (!isRendered()) return;
    try
    {
      HttpServletRequest request = 
        (HttpServletRequest)context.getExternalContext().getRequest();

      String encoded = (String)request.getAttribute(MATRIX_CLIENT_ENCODED);

      request.setAttribute(MATRIX_CLIENT_ENCODED, "done");

      String clientId = getClientId(context);
      ResponseWriter writer = context.getResponseWriter();

      String formId = FacesUtils.getParentFormId(this, context);

      // encode javascript functions
      if (encoded == null)
      {
        // encode css
        writer.startElement("link", this);
        writer.writeAttribute("type", "text/css", null);
        writer.writeAttribute("rel", "stylesheet", null);
        writer.writeAttribute("href",
          context.getExternalContext().getRequestContextPath() + 
          "/plugins/client/styles.css?v=" + CLIENT_VERSION, null);
        writer.endElement("link");
      
        // encode script
        writer.startElement("script", this);
        writer.writeAttribute("type", "text/javascript", null);
        writer.writeAttribute("src",
          context.getExternalContext().getRequestContextPath() +
          "/plugins/client/matrix-client.js?v=" + CLIENT_VERSION, null);
        writer.endElement("script");
      }
      
      //in page
      writer.startElement("script", this);
      writer.writeAttribute("type", "text/javascript", null);
      writer.writeText(getFunctionToExecute(context, formId, clientId), null);                
      writer.endElement("script");

      // encode buttons
      writer.startElement("input", this);
      writer.writeAttribute("type", "hidden", null);
      writer.writeAttribute("name", clientId + "_command", null);
      writer.writeAttribute("value", "", null);
      writer.endElement("input");

      writer.startElement("input", this);
      writer.writeAttribute("type", "hidden", null);
      writer.writeAttribute("name", clientId + "_result", null);
      writer.writeAttribute("value", "", null);
      writer.endElement("input");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  @Override
  public void encodeEnd(FacesContext context) throws IOException
  {
  }

  @Override
  public void processUpdates(FacesContext context)
  {
    if (context == null) throw new NullPointerException("context");
    if (!isRendered()) return;
    super.processUpdates(context);
    try
    {
      updateModel(context);
    }
    catch (RuntimeException e)
    {
      context.renderResponse();
      throw e;
    }
  }

  public void updateModel(FacesContext context)
  {
    ValueExpression ve = getValueExpression("command");
    if (ve != null)
    {
      try
      {
        ve.setValue(context.getELContext(), _command);
      }
      catch (RuntimeException e)
      {
      }
    }

    ve = getValueExpression("result");
    if (ve == null) return;
    {
      try
      {
        ve.setValue(context.getELContext(), _result);
      }
      catch (RuntimeException e)
      {
      }
    }
  }
 
  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[7];
    values[0] = super.saveState(context);
    values[1] = _command;
    values[2] = _result;
    values[3] = _properties;
    values[4] = _function;
    values[5] = _model;
    values[6] = _helpUrl;
    return values;
  }
  
  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _command = (String)values[1];
    _result = (Map)values[2];
    _properties = (Map)values[3];
    _function = (String)values[4];
    _model = (HtmlMatrixClientModel)values[5];
    _helpUrl = (String)values[6];
  }

  //Javascript functions writing
  private String getFunctionToExecute(FacesContext context, String formId, String clientId)
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    HttpServletRequest request = 
      (HttpServletRequest)context.getExternalContext().getRequest();
    
    StringBuilder sb = new StringBuilder();
    sb.append("function " + getFunction() + "(parameters)");
    sb.append("{");
    sb.append("if (parameters === undefined || parameters === null) parameters = {};");
    sb.append("parameters.command = '" + getCommand()+ "';");

    
    String secret = 
      MatrixConfig.getProperty("org.santfeliu.security.urlCredentialsCipher.secret");
    StringCipher cipher = new StringCipher(secret);
    Credentials credentials = userSessionBean.getCredentials();
    String session = cipher.encrypt(credentials.getUserId() + ":" + credentials.getPassword());
    
    sb.append("parameters.credentials = '" + session + "';");
    sb.append("parameters.userId = '" + userSessionBean.getUsername() + "';");
    sb.append("parameters.password = '" + userSessionBean.getPassword() + "';"); 
    sb.append("parameters.credentials = '" + userSessionBean.getCredentials()+ "';");     
    if (getHelpUrl() != null)
      sb.append("parameters.helpUrl = '" + getHelpUrl() +"';");
    String wsdir = "";
    if ("localhost".equals(request.getServerName()))
      wsdir = "http://localhost/" + WSDIRECTORY_PATH;
    else
      wsdir = request.getScheme() + "://" + request.getServerName() + ":" 
        + request.getServerPort()  + "/" + WSDIRECTORY_PATH;
    sb.append("parameters.wsdir = '" + wsdir + "';");
    sb.append(appendParameters());
    sb.append("document.forms['" + formId + "']['" + clientId + "_command'].value='" + getCommand() + "';");
    sb.append("if (callback === undefined){");
    sb.append("var callback = function(result){");
    sb.append("if (showOverlay) showOverlay();");
    sb.append("var sresult = JSON.stringify(result);");
    sb.append("document.forms['" + formId + "']['" + clientId + "_result'].value=sresult;");
    sb.append("document.forms['" + formId + "'].submit();" );
    sb.append("};}");
    sb.append("executeCommand(parameters,callback);");
    sb.append("}");    

    return sb.toString();
  }
  
  private String appendParameters()
  {
    StringBuilder sb = new StringBuilder();
    HtmlMatrixClientModel model = getModel();
    Map properties = model != null ? model.getParameters() : getProperties();
    if (properties != null && !properties.isEmpty())
    {
      Iterator it = properties.entrySet().iterator();
      while (it.hasNext())
      {
        Map.Entry<String,Object> entry = (Map.Entry) it.next();
        String key = entry.getKey();
        Object value = entry.getValue();
        if (value instanceof Map)
        {
          String json = JSONObject.toJSONString((Map)value);
          sb.append("parameters." + key + "='" + escapeString(json) + "';");
        }
        else
          sb.append("parameters." + key + "='" + escapeString(String.valueOf(value)) + "';");
      }
    }
    return sb.toString();
  }
  
  private String escapeString(String value)
  {
    if (value != null)
    {
      value = value.replaceAll("\"", "\\\\\"");
      value = value.replaceAll("\'", "\\\\\'");  
    }
    return value;
  }
}
