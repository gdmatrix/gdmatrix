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
package org.santfeliu.faces.component.jqueryui;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 * A Calendar component to show in input date,time or datetime fields. It uses
 * the JQuery-UI set of components integrated as a plugin in "/plugins/jquery".
 * 
 * This component extends HtmlCalendar because is conceived as an evolution
 * to replace it wherever is used.
 * 
 * It preserves all attributes of HtmlCalendar, and extends functionallity with 
 * other non mandatory, to allow replace HtmlCalendar without changing 
 * current JSP pages.
 * 
 * However HtmlJQueryUICalendar doesn't need calendar plugin to work properly
 * 
 * @author blanquepa
 */
@FacesComponent(value = "HtmlJQueryUICalendar")
public class HtmlCalendar extends org.santfeliu.faces.component.HtmlCalendar
{
  private Boolean _singleInput;
  private JQueryUIRenderUtils jQueryUIEncoder = new JQueryUIRenderUtils(this);
  private String _dayLabel;
  private String _hourLabel; 
  private ResourceBundle bundle;
  private String name = "date";
  
  public HtmlCalendar()
  {
    setRendererType("JQueryUI");
  }
  
  public HtmlCalendar(String name)
  {
    this();
    this.name = name;
  }
  
  public void setSingleInput(boolean singleInput)
  {
    this._singleInput = singleInput;
  }

  public boolean isSingleInput()
  {
    if (_singleInput != null) return _singleInput;
    ValueExpression ve = getValueExpression("singleInput");
    Boolean v = ve != null ? 
      (Boolean)ve.getValue(getFacesContext().getELContext()) : null;
    return v != null ? v : false;
  } 

  public String getDayLabel()
  {
    if (_dayLabel != null) return _dayLabel;
    ValueExpression ve = getValueExpression("dayLabel");
    return ve != null ? 
      (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setDayLabel(String _dayLabel)
  {
    this._dayLabel = _dayLabel;
  }

  public String getHourLabel()
  {
    if (_hourLabel != null) return _hourLabel;
    ValueExpression ve = getValueExpression("hourLabel");
    return ve != null ? 
      (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setHourLabel(String _hourLabel)
  {
    this._hourLabel = _hourLabel;
  }
  
  public void setTheme(String theme)
  {
    jQueryUIEncoder.setTheme(theme);
  }

  public String getTheme()
  {
    if (jQueryUIEncoder.getTheme() != null) return jQueryUIEncoder.getTheme();
    ValueExpression ve = getValueExpression("theme");
    String theme = ve != null ? 
      (String)ve.getValue(getFacesContext().getELContext()) : null;
    return theme == null ? jQueryUIEncoder.getDefaultTheme() : theme;
  }  
  
  public void setIncludeLibraries(boolean _includeLibraries)
  {
    jQueryUIEncoder.setRenderLibraries(_includeLibraries);
  }
  
  public boolean isIncludeLibraries()
  {
    if (jQueryUIEncoder.isRenderLibraries() != null) 
      return jQueryUIEncoder.isRenderLibraries() ;
    ValueExpression ve = getValueExpression("includeLibraries");
    Boolean v = ve != null ? 
      (Boolean)ve.getValue(getFacesContext().getELContext()) : null;
    return v != null ? v : false;
  }   
  
  public boolean hasDateTimeFormat()
  {
    return getExternalFormat().contains(getDateTimeSeparator());
  }
  
 @Override
  public void encodeBegin(FacesContext context) throws IOException
  {   
    ResponseWriter writer = context.getResponseWriter();
    String clientId = getClientId(context);     
    encodeCalendar(context, writer, clientId);
  }
  
  public void encodeCalendar(FacesContext context, ResponseWriter writer,
    String clientId) 
    throws IOException
  {
    Locale locale = context.getViewRoot().getLocale();
    bundle = ResourceBundle.getBundle(
      "org.santfeliu.faces.component.resources.ComponentBundle", locale);     
    
    encodeDateInputText(context, writer, clientId);   
    if (hasDateTimeFormat() && !isSingleInput())
    {
      encodeTimeInputText(context, writer, clientId);
    }
    encodeJavascript(context, writer, clientId);    
  }

  @Override
  public void decode(FacesContext context)
  {
    if (isDisabled()) return;
    
    Map paramsMap = context.getExternalContext().getRequestParameterMap();
    String clientId = getClientId(context);

    String dateValue = (String)paramsMap.get(getFieldId(clientId, this.name));
    if (dateValue == null) dateValue = "";
    String value = dateValue;

    if (hasDateTimeFormat() && !isSingleInput())
    {
      String timeValue = (String)paramsMap.get(getFieldId(clientId, "time"));
      if (timeValue == null) timeValue = "";
      value += getDateTimeSeparator() + timeValue;
    }
    setSubmittedValue(value);
  }

  private void encodeJavascript(FacesContext context, ResponseWriter writer,
    String clientId)
    throws IOException
  {
    encodeDatePicker(context, writer, clientId);
  }
  
  protected void encodeDatePicker(FacesContext context, ResponseWriter writer,
    String clientId) 
    throws IOException
  {
    String externalFormat = getExternalFormat();
    if (externalFormat == null)
      externalFormat = DEFAULT_EXTERNAL_FORMAT;
    
    jQueryUIEncoder.encodeLibraries(context, writer); 
    jQueryUIEncoder.encodeDatePickerLibraries(context, writer);    

    if (hasDateTimeFormat() && isSingleInput())
    {
      String[] pattern = externalFormat.split("\\" + getDateTimeSeparator());
      String datePattern = pattern[0];
      String timePattern = pattern[1]; 
      String fieldName = getFieldId(clientId, name); 
      String titleAttr = bundle.getString("calendar");
      
      jQueryUIEncoder.encodeDateTimePicker(writer, fieldName, datePattern, 
        timePattern, titleAttr, null);
    }
    else
    {
      String datePattern = externalFormat;
      if (hasDateTimeFormat())
      {
        String[] pattern = externalFormat.split("\\" + getDateTimeSeparator());
        datePattern = pattern[0];
      }

      String fieldName = getFieldId(clientId, name); 
      String titleAttr = bundle.getString("calendar");

      jQueryUIEncoder.encodeDatePicker(writer, fieldName, datePattern, 
        titleAttr, null);
    }
  } 
  
  private void encodeDateInputText(FacesContext context,
    ResponseWriter writer, String clientId) throws IOException
  {
    if (getDayLabel() != null)
    {
      writer.startElement("label", this);
      writer.writeAttribute("for", getFieldId(clientId, name), "for");
      writer.writeAttribute("class", "element-invisible", "class");
      writer.write(getDayLabel());
      writer.endElement("label");      
    }
    writer.startElement("input", this);
    writer.writeAttribute("id", getFieldId(clientId, name), "id");
    writer.writeAttribute("name", getFieldId(clientId, name), "name");
    writer.writeAttribute("value", getDateValue(), null);
    int formatLength = (hasDateTimeFormat() && isSingleInput() ? 
      getExternalFormat().length() : getExternalDateFormat().length());
    writer.writeAttribute("maxlength", formatLength, "maxlength");
    if (isDisabled())
    {
      writer.writeAttribute("disabled", "true", null);
    }
    String style = getStyle();
    if (style != null)
    {
      writer.writeAttribute("style", style, "style");
    }
    String styleClass = getStyleClass();
    if (styleClass != null)
    {
      writer.writeAttribute("class", styleClass, "styleClass");
    }

    writer.writeAttribute("placeholder", 
      getExternalDateFormat().toUpperCase(), null);
    writer.endElement("input");
  }

  private void encodeTimeInputText(FacesContext context,
    ResponseWriter writer, String clientId) throws IOException
  {
    if (getHourLabel() != null)
    {
      writer.startElement("label", this);
      writer.writeAttribute("for", getFieldId(clientId, "time"), "for");
      writer.writeAttribute("class", "element-invisible", "class");
      writer.write(getHourLabel());
      writer.endElement("label");
    }
    writer.startElement("input", this);
    writer.writeAttribute("id",  getFieldId(clientId, "time"), "id");
    writer.writeAttribute("name", getFieldId(clientId, "time"), "name");
    writer.writeAttribute("value", getTimeValue(), null);
    if (isDisabled())
    {
      writer.writeAttribute("disabled", "true", null);
    }
    
    if (hasDateTimeFormat())
      writer.writeAttribute("placeholder", 
        getExternalTimeFormat().toUpperCase(), null);
    
    int formatLength = (hasDateTimeFormat() && isSingleInput() ? 
      getExternalFormat().length() : getExternalTimeFormat().length());
    
    writer.writeAttribute("maxlength", formatLength, "maxlength");
    String style = getStyle();
    if (style != null)
    {
      writer.writeAttribute("style", style, "style");
    }
    String styleClass = getStyleClass();
    if (styleClass != null)
    {
      writer.writeAttribute("class", styleClass, "styleClass");
    }
    writer.endElement("input");
  }

  private String getExternalDateFormat()
  {
    String format = getExternalFormat();
    int index = format.indexOf(getDateTimeSeparator());
    return (index == -1) ? format : format.substring(0, index);
  }

  private String getExternalTimeFormat()
  {
    String format = getExternalFormat();
    int index = format.indexOf(getDateTimeSeparator());
    return (index == -1) ? null : format.substring(index + 1);
  }
  
  private String getFieldId(String clientId, String name)
  {
    return clientId + ":" + name;
  } 
    
  @Override
  public Object saveState(FacesContext context)
  {
    Object svalues[] = (Object[])super.saveState(context);
    Object values[] = new Object[15];
    System.arraycopy(svalues, 0, values, 0, svalues.length);
    values[10] = _singleInput;
    values[11] = jQueryUIEncoder.getTheme();
    values[12] = _dayLabel;
    values[13] = _hourLabel;
    values[14] = jQueryUIEncoder.isRenderLibraries();
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values);
    _singleInput = (Boolean)values[10];
    jQueryUIEncoder.setTheme((String)values[11]);
    _dayLabel = (String)values[12];
    _hourLabel = (String)values[13];    
    jQueryUIEncoder.setRenderLibraries((Boolean)values[14]);
  }  
}
