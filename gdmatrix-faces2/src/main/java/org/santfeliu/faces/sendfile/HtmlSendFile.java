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
package org.santfeliu.faces.sendfile;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;
import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.web.HttpUtils;

/**
 *
 * @author unknown
 */
@FacesComponent(value = "HtmlSendFile")
public class HtmlSendFile extends UICommand implements Serializable
{
  private static final String SEND_FILE_ENCODED = "SEND_FILE_ENCODED";
  private String _maxFileSize;
  private String _validExtensions;
  private String _style;
  private String _styleClass;
  private String _buttonClass;
  private String _command;
  private String _result;
  private String _width;
  private String _height;
  private Map _fileProperties;
  private Map _docTypes;
  private String _port;

  public HtmlSendFile()
  {
  }

  @Override
  public String getFamily()
  {
    return "SendFile";
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

  public void setResult(String result)
  {
    this._result = result;
  }

  public String getResult()
  {
    if (_result != null) return _result;
    ValueExpression ve = getValueExpression("result");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public String getMaxFileSize()
  {
    if (_maxFileSize != null) return _maxFileSize;
    ValueExpression ve = getValueExpression("maxFileSize");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setMaxFileSize(String maxFileSize)
  {
    _maxFileSize = maxFileSize;
  }

  public String getValidExtensions()
  {
    if (_validExtensions != null) return _validExtensions;
    ValueExpression ve = getValueExpression("validExtensions");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setValidExtensions(String validExtensions)
  {
    _validExtensions = validExtensions;
  }

  public void setStyle(String style)
  {
    this._style = style;
  }

  public String getStyle()
  {
    if (_style != null) return _style;
    ValueExpression ve = getValueExpression("style");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setStyleClass(String styleClass)
  {
    this._styleClass = styleClass;
  }

  public String getStyleClass()
  {
    if (_styleClass != null) return _styleClass;
    ValueExpression ve = getValueExpression("styleClass");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setButtonClass(String buttonClass)
  {
    this._buttonClass = buttonClass;
  }

  public String getButtonClass()
  {
    if (_buttonClass != null) return _buttonClass;
    ValueExpression ve = getValueExpression("buttonClass");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setWidth(String width)
  {
    this._width = width;
  }

  public String getWidth()
  {
    return _width;
  }

  public void setHeight(String height)
  {
    this._height = height;
  }

  public String getHeight()
  {
    return _height;
  }
  
  public void setFileProperties(Map properties)
  {
    this._fileProperties = properties;
  }

  public Map getFileProperties()
  {
    if (_fileProperties != null) return _fileProperties;
    ValueExpression ve = getValueExpression("fileProperties");
    return ve != null ? (Map)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setDocTypes(Map docTypes)
  {
    this._docTypes = docTypes;
  }

  public Map getDocTypes()
  {
    if (_docTypes != null) return _docTypes;
    ValueExpression ve = getValueExpression("docTypes");
    return ve != null ? (Map)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setPort(String port)
  {
    this._port = port;
  }

  public String getPort()
  {
    if (_port != null) return _port;
    ValueExpression ve = getValueExpression("port");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : "443";
  }

  @Override
  public void decode(FacesContext context)
  {
    try
    {
      String clientId = getClientId(context);
      Map parameterMap = context.getExternalContext().getRequestParameterMap();
      Object command = parameterMap.get(clientId + "_command");
      Object result = parameterMap.get(clientId + "_result");
      if (command != null && result != null)
      {
        String sCommand = String.valueOf(command);
        String sResult = String.valueOf(result);
        if (sCommand.length() > 0 && sResult.length() > 0)
        {
          _command = sCommand;
          _result = sResult;
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

      String encoded = (String)request.getAttribute(SEND_FILE_ENCODED);
      if (encoded == null)
      {
        request.setAttribute(SEND_FILE_ENCODED, "done");
        String serverName = HttpUtils.getServerName(request);
        String contextPath = request.getContextPath();

        String clientId = getClientId(context);
        ResponseWriter writer = context.getResponseWriter();

        String formId = FacesUtils.getParentFormId(this, context);

        // encode javascript functions
        writer.startElement("script", this);
        writer.writeAttribute("type", "text/javascript", null);
        writer.writeText(getSendFileFunction(formId), null);
        writer.writeText(getStopEditingFunction(), null);
        writer.writeText(getEditFileFunction(), null);
        writer.writeText(getUpdateFileFunction(), null);
        writer.writeText(getConfigureFunction(), null);
        writer.writeText(getEndTransmissionFunction(formId, clientId), null);
        writer.writeText(getShowOverlayFunction(), null);
        writer.writeText(getHideOverlayFunction(), null);
        writer.writeText(getShowEditPanelFunction(), null);
        writer.endElement("script");

        // encode overlay
        writer.startElement("div", this);
        writer.writeAttribute("id", "_sfOverlay_", null);
        writer.writeAttribute("style",
          "position:absolute;top:0px;left:0px;width:0px;height:0px;" +
          "z-index:1000;visibility:hidden;background-color:gray;" +
          "filter:alpha(opacity=70);-moz-opacity:0.7;opacity:0.7;", null);
        writer.endElement("div");

        // encode editPanel layer
        writer.startElement("div", this);
        writer.writeAttribute("id", "_sfEditPanel_", null);
        writer.writeAttribute("style",
          "position:absolute;top:0px;left:0px;z-index:2000;" +
          "visibility:hidden;background-color:orange;border:black 3px solid", null);

        writer.startElement("div", this);
        writer.writeAttribute("id", "_sfEditPanelMsg_", null);
        writer.writeAttribute("style", "background:#F8F8F8;margin:4px", null);
        writer.writeText("Editing...", null);
        writer.endElement("div");

        writer.startElement("div", this);
        writer.writeAttribute("style", "text-align:center", null);
        writer.startElement("input", this);
        writer.writeAttribute("id", "_sfEditButton_", null);
        writer.writeAttribute("type", "button", null);
        writer.writeAttribute("onclick", "javascript:htmlSendFile_stopEditing();", null);
        writer.writeAttribute("value", "Push this button to finish.", null);
        writer.writeAttribute("style", "background:orange;border:2px bevel;margin-bottom:6px", null);
        writer.endElement("input");
        writer.endElement("div");

        writer.endElement("div");

        // encode applet
        writer.startElement("applet", this);
        writer.writeAttribute("name", "SendFile", null);

        // TODO: depends on applet directory
        String codebaseURL = "https://" + serverName + ":" + getPort() +
          contextPath + "/plugins/sendfile";
        writer.writeAttribute("codebase", codebaseURL, null);

        writer.writeAttribute("archive", "sendfile.jar", null);
        writer.writeAttribute("code", "org.santfeliu.doc.sendfile.SendFileApplet", null);
        writer.writeAttribute("width", "0", null);
        writer.writeAttribute("height", "0", null);
      
        String servletURL = "https://" + serverName + ":" + getPort() +
          contextPath + "/documents";
        writer.startElement("param", this);
        writer.writeAttribute("name", "servletURL", null);
        writer.writeAttribute("value", servletURL, null);
        writer.endElement("param");

        String language = context.getViewRoot().getLocale().getLanguage();
        writer.startElement("param", this);
        writer.writeAttribute("name", "language", null);
        writer.writeAttribute("value", language, null);
        writer.endElement("param");

        HttpSession session =
          (HttpSession)context.getExternalContext().getSession(false);
        if (session != null)
        {
          String sessionId = session.getId();
          writer.startElement("param", this);
          writer.writeAttribute("name", "sessionId", null);
          writer.writeAttribute("value", sessionId, null);
          writer.endElement("param");
        }

        String maxFileSize = getMaxFileSize();
        if (maxFileSize != null)
        {
          writer.startElement("param", this);
          writer.writeAttribute("name", "maxFileSize", null);
          writer.writeAttribute("value", maxFileSize, null);
          writer.endElement("param");
        }

        String validExtensions = getValidExtensions();
        if (validExtensions != null)
        {
          writer.startElement("param", this);
          writer.writeAttribute("name", "validExtensions", null);
          writer.writeAttribute("value", validExtensions, null);
          writer.endElement("param");
        }
        writer.endElement("applet");

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
    if (ve == null) return;
    try
    {
      ve.setValue(context.getELContext(), _command);
    }
    catch (RuntimeException e)
    {
    }

    ve = getValueExpression("result");
    if (ve == null) return;
    try
    {
      ve.setValue(context.getELContext(), _result);
    }
    catch (RuntimeException e)
    {
    }
  }
 
  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[13];
    values[0] = super.saveState(context);
    values[1] = _maxFileSize;
    values[2] = _validExtensions;
    values[3] = _style;
    values[4] = _styleClass;
    values[5] = _command;
    values[6] = _result;
    values[7] = _width;
    values[8] = _height;
    values[9] = _fileProperties;
    values[10] = _docTypes;
    values[11] = _buttonClass;
    values[12] = _port;
    return values;
  }
  
  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _maxFileSize = (String)values[1];
    _validExtensions = (String)values[2];
    _style = (String)values[3];
    _styleClass = (String)values[4];
    _command = (String)values[5];
    _result = (String)values[6];
    _width = (String)values[7];
    _height = (String)values[8];
    _fileProperties = (Map)values[9];
    _docTypes = (Map)values[10];
    _buttonClass = (String)values[11];
    _port = (String)values[12];
  }

  //Javascript functions writing
  private String getEndTransmissionFunction(String formId, String clientId)
  {
    StringBuffer sb = new StringBuffer();
    sb.append("function htmlSendFile_endTransmission(command,result)");
    sb.append("{");
    sb.append("document.forms['" + formId + "']['" + clientId + "_command'].value=command;");
    sb.append("document.forms['" + formId + "']['" + clientId + "_result'].value=result;");
    sb.append("document.forms['" + formId + "'].submit();" );
    sb.append("return false;" );
    sb.append("}\n");
    return sb.toString();
  }

  private String getSendFileFunction(String formId)
  {
    Map properties = getFileProperties();
    StringBuffer sb = new StringBuffer();
    
    sb.append("function htmlSendFile_send(){");
    sb.append("var a = document.applets['SendFile'];");
    
    //Properties
    if (properties != null)
    {
      Set entrySet = properties.entrySet();
      Iterator it = entrySet.iterator();
      while (it.hasNext())
      {
        Map.Entry entry = (Map.Entry)it.next();
        String key = (String)entry.getKey();
        Object value = entry.getValue();
        if (value != null)
        {
          if (value instanceof Collection)
          {
            for (Object item : (Collection)value)
            {
              appendParameter(sb, key, item.toString(), formId);
            }
          }
          else
          {
            appendParameter(sb, key, value.toString(), formId);
          }
        }
      }
    }

    //DocTypes
    Map docTypes = getDocTypes();
    if (docTypes != null)
    {
      Set<Map.Entry> docTypeSet = docTypes.entrySet();
      for(Map.Entry entry : docTypeSet)
      {
        String docTypeId = (String)entry.getKey();
        String docTypeDesc = (String)entry.getValue();
        if (docTypeId != null)
        {
          appendDocType(sb, docTypeId, docTypeDesc);
        }
      }
    }
    sb.append("a.sendFile();}\n");

    return sb.toString();
  }

  private String getEditFileFunction()
  {
    StringBuffer sb = new StringBuffer();

    sb.append("function htmlSendFile_edit(docId, language){");
    sb.append("var a = document.applets['SendFile'];");
    sb.append("a.editFile(docId, language);}\n");
    return sb.toString();
  }

  private String getUpdateFileFunction()
  {
    StringBuffer sb = new StringBuffer();

    sb.append("function htmlSendFile_update(docId){");
    sb.append("var a = document.applets['SendFile'];");
    sb.append("a.updateFile(docId);}\n");
    return sb.toString();
  }

  private String getStopEditingFunction()
  {
    StringBuffer sb = new StringBuffer();

    sb.append("function htmlSendFile_stopEditing(){");
    sb.append("var a = document.applets['SendFile'];");
    sb.append("a.stopEditing();}\n");
    return sb.toString();
  }

  private String getConfigureFunction()
  {
    StringBuffer sb = new StringBuffer();

    sb.append("function htmlSendFile_config(){");
    sb.append("var a = document.applets['SendFile'];");
    sb.append("a.configure();}\n");
    return sb.toString();
  }

  private String getShowOverlayFunction()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("function htmlSendFile_showOverlay(){");
    sb.append("var overlay=document.getElementById('_sfOverlay_');\n");
    sb.append("var ow=0; var oh=0; bh=document.body.clientHeight+100;\n");
    sb.append("if(typeof(window.innerWidth)=='number')\n");
    sb.append("{ow=window.innerWidth;oh=Math.max(window.innerHeight,bh);");
    sb.append("overlay.style.width=ow+'px';overlay.style.height=oh+'px';\n}");
    sb.append("else{ow=document.documentElement.clientWidth;");
    sb.append("oh=Math.max(document.documentElement.clientHeight,bh);");
    sb.append("overlay.style.width=ow;overlay.style.height=oh;}\n");
    sb.append("overlay.style.visibility=\"visible\";}\n");
    return sb.toString();
  }

  private String getShowEditPanelFunction()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("function htmlSendFile_showEditPanel(message,buttonLabel){");
    sb.append("var editPanelMsg=document.getElementById('_sfEditPanelMsg_');\n");
    sb.append("editPanelMsg.innerHTML=message;\n");
    sb.append("var editButton=document.getElementById('_sfEditButton_');\n");
    sb.append("editButton.value=buttonLabel;\n");

    sb.append("var editPanel=document.getElementById('_sfEditPanel_');\n");
    sb.append("var epw=500; var eph=200; bh=document.body.clientHeight+10;\n");

    sb.append("if(typeof(window.innerWidth)=='number')\n");
    sb.append("{epleft=(window.innerWidth-epw)/2;\n");
    sb.append("eptop=200;\n");
    sb.append("editPanel.style.width=epw+'px';\n");
    sb.append("editPanel.style.left=epleft+'px'\n;");
    sb.append("editPanel.style.top=eptop+'px';}\n");

    sb.append("else{epleft=(document.documentElement.clientWidth-epw)/2;\n");
    sb.append("eptop=(Math.max(document.documentElement.clientHeight,bh)-eph)/2;\n");
    sb.append("editPanel.style.width=epw;\n");
    sb.append("editPanel.style.left=epleft\n;");
    sb.append("editPanel.style.top=eptop;}\n");

    sb.append("editPanel.style.visibility=\"visible\";}\n");
    return sb.toString();
  }

  private String getHideOverlayFunction()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("function htmlSendFile_hideOverlay() {");
    sb.append("var overlay=document.getElementById('_sfOverlay_');\n");
    sb.append("overlay.style.visibility=\"hidden\";}\n");
    return sb.toString();
  }

  private void appendParameter(StringBuffer sb, String key, String svalue, String formId)
  {
    svalue = svalue.replaceAll("'", "\\\\'"); // quote '
    if (key.startsWith("$"))
    {
      key = key.substring(1);
      sb.append("var value = document.forms['" + formId +
        "']['" + svalue + "'].value;");
      sb.append("a.setParameter('" + key + "',value);");
    }
    else
    {
      sb.append("a.setParameter('" + key + "','" + svalue + "');");
    }
  }

  private void appendDocType(StringBuffer sb, String docTypeId, String docTypeDesc)
  {
    docTypeDesc = docTypeDesc.replaceAll("'", "\\\\'");
    sb.append("a.setDocType('" + docTypeId + "','" + docTypeDesc + "');");
  }  
}
