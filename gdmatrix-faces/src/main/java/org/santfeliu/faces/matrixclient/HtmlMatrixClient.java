package org.santfeliu.faces.matrixclient;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
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
    ValueBinding vb = getValueBinding("command");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setResult(Map result)
  {
    this._result = result;
  }

  public Map getResult()
  {
    if (_result != null) return _result;
    ValueBinding vb = getValueBinding("result");
    return vb != null ? (Map)vb.getValue(getFacesContext()) : null;
  }
  
  public void setProperties(Map properties)
  {
    this._properties = properties;
  }

  public Map getProperties()
  {
    if (_properties != null) return _properties;
    ValueBinding vb = getValueBinding("properties");
    return vb != null ? (Map)vb.getValue(getFacesContext()) : null;
  }

  public void setFunction(String function)
  {
    this._function = function;
  }

  public String getFunction()
  {
    if (_function != null) return _function;
    ValueBinding vb = getValueBinding("function");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public String getHelpUrl() 
  {
    if (_helpUrl != null) return _helpUrl;
    ValueBinding vb = getValueBinding("helpUrl");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
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
    ValueBinding vb = getValueBinding("model");
    return vb != null ? (HtmlMatrixClientModel)vb.getValue(getFacesContext()) : null;
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
    ValueBinding vb = getValueBinding("command");
    if (vb != null)
    {
      try
      {
        vb.setValue(context, _command);
      }
      catch (RuntimeException e)
      {
      }
    }

    vb = getValueBinding("result");
    if (vb == null) return;
    {
      try
      {
        vb.setValue(context, _result);
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
