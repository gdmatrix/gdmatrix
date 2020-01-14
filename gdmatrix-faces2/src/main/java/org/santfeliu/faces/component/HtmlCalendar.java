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
package org.santfeliu.faces.component;

import java.io.IOException;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.el.ValueExpression;

import javax.faces.application.FacesMessage;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ValueChangeEvent;

import org.santfeliu.faces.FacesUtils;

/**
 *
 * @author unknown
 */
@FacesComponent(value = "HtmlCalendar")
public class HtmlCalendar extends UIInput
{
  public static final String DATE_TIME_SEPARATOR = "|";
  public static final String DEFAULT_INTERNAL_FORMAT = "yyyyMMdd";
  public static final String DEFAULT_EXTERNAL_FORMAT = "dd/MM/yyyy";

  protected static final String INVALID_DATE_FORMAT = "INVALID_DATE_FORMAT";
  public static final String JS_CALENDAR_ENCODED = "JS_CALENDAR_ENCODED";

  private String _externalFormat;
  private String _internalFormat;
  private String _style;
  private String _styleClass;
  private String _buttonImage;
  private String _buttonStyle;
  private String _buttonStyleClass;
  private Boolean _disabled;
  private String _dateTimeSeparator;

  public HtmlCalendar()
  {
    setRendererType(null);
  }

  public void setExternalFormat(String format)
  {
    this._externalFormat = format;
  }

  public String getExternalFormat()
  {
    if (_externalFormat != null) return _externalFormat;
    ValueExpression ve = getValueExpression("externalFormat");
    String format = ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
    return format == null ? DEFAULT_EXTERNAL_FORMAT : format;
  }

  public void setInternalFormat(String format)
  {
    this._internalFormat = format;
  }

  public String getInternalFormat()
  {
    if (_internalFormat != null) return _internalFormat;
    ValueExpression ve = getValueExpression("internalFormat");
    String format = ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
    return format == null ? DEFAULT_INTERNAL_FORMAT : format;
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

  public void setButtonImage(String buttonImage)
  {
    this._buttonImage = buttonImage;
  }

  public String getButtonImage()
  {
    if (_buttonImage != null) return _buttonImage;
    ValueExpression ve = getValueExpression("buttonImage");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setButtonStyle(String buttonStyle)
  {
    this._buttonStyle = buttonStyle;
  }

  public String getButtonStyle()
  {
    if (_buttonStyle != null) return _buttonStyle;
    ValueExpression ve = getValueExpression("buttonStyle");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setButtonStyleClass(String buttonStyleClass)
  {
    this._buttonStyleClass = buttonStyleClass;
  }

  public String getButtonStyleClass()
  {
    if (_buttonStyleClass != null) return _buttonStyleClass;
    ValueExpression ve = getValueExpression("buttonStyleClass");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setDisabled(boolean disabled)
  {
    this._disabled = Boolean.valueOf(disabled);
  }

  public boolean isDisabled()
  {
    if (_disabled != null) return _disabled.booleanValue();
    ValueExpression ve = getValueExpression("disabled");
    Boolean v = ve != null ? (Boolean)ve.getValue(getFacesContext().getELContext()) : null;
    return v != null ? v.booleanValue() : false;
  }
  
  public void setDateTimeSeparator(String dateTimeSeparator)
  {
    this._dateTimeSeparator = dateTimeSeparator;
  }

  public String getDateTimeSeparator()
  {
    if (_dateTimeSeparator != null) return _dateTimeSeparator;
    ValueExpression ve = getValueExpression("dateTimeSeparator");
    String format = ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
    return format == null ? DATE_TIME_SEPARATOR : format;
  }  

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    Locale locale = context.getViewRoot().getLocale();
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.faces.component.resources.ComponentBundle", locale); 
    
    ResponseWriter writer = context.getResponseWriter();
    String clientId = getClientId(context);
    Map requestMap = context.getExternalContext().getRequestMap();
    if (requestMap.get(JS_CALENDAR_ENCODED) == null)
    {
      requestMap.put(JS_CALENDAR_ENCODED, "true");
      encodeJavascript(context, writer);
    }
    encodeDateInputText(context, writer, clientId);
    encodeDateButton(context, writer, clientId, bundle);
    if (getExternalFormat().contains(getDateTimeSeparator()))
    {
      encodeTimeInputText(context, writer, clientId);
      encodeTimeButton(context, writer, clientId, bundle);
    }
  }

  @Override
  public void decode(FacesContext context)
  {
    if (isDisabled()) return;
    
    Map paramsMap = context.getExternalContext().getRequestParameterMap();
    String clientId = getClientId(context);

    String dateValue = (String)paramsMap.get(clientId + "_date");
    if (dateValue == null) dateValue = "";
    String value = dateValue;

    if (getExternalFormat().contains(getDateTimeSeparator()))
    {
      String timeValue = (String)paramsMap.get(clientId + "_time");
      if (timeValue == null) timeValue = "";
      value += getDateTimeSeparator() + timeValue;
    }
    setSubmittedValue(value);
  }

  @Override
  public void validate(FacesContext context)
  {
    setValid(true);
    if (context == null) throw new NullPointerException("context");
    Object submittedValue = getSubmittedValue();
    if (submittedValue == null) return;

    Object convertedValue = convertValue(context, submittedValue);

    if (!isValid()) return;

    validateValue(context, convertedValue);

    if (!isValid()) return;

    Object previousValue = getValue();
    setValue(convertedValue);
    setSubmittedValue(null);
    if (compareValues(previousValue, convertedValue))
    {
      queueEvent(new ValueChangeEvent(this, previousValue, convertedValue));
    }
  }

  private Object convertValue(FacesContext context, Object submittedValue)
  {
    if (submittedValue == null) return null;
    String value = submittedValue.toString().trim();
    if (value.length() == 0) return null;
    if (value.startsWith(getDateTimeSeparator())) return null;

    String internalFormat = getInternalFormat();
    String externalFormat = getExternalFormat();
    if (value.endsWith(getDateTimeSeparator()))
    {
      externalFormat = getExternalDateFormat();
      value = value.substring(0, value.length() - 1);
    }
    if (value.contains(getDateTimeSeparator()))
    {
      String hvalue =
        value.substring(value.indexOf(getDateTimeSeparator()));
      String hExternalFormat =
        externalFormat.substring(externalFormat.indexOf(getDateTimeSeparator()));
      if (hvalue.length() < hExternalFormat.length())
      {
        value = value.substring(0, value.indexOf(getDateTimeSeparator()));
        int j = 0;
        for (int i = 0; i < hExternalFormat.length(); i++)
        {
          char fchar = hExternalFormat.charAt(i);
          if (fchar == 'H' || fchar == 'm' || fchar == 's')
          {
            if (j < hvalue.length())
            {
              char c = hvalue.charAt(j);
              if (c <= '9' && c >= '0')
                value += c;
              else
              {
                value = value.substring(0, (value.length() - 1)) + 
                  '0' + value.substring(value.length() - 1);
                j--;
              }
            }
            else
            {
              char c = value.charAt(value.length() - 1);
              if (c <= '9' && c >= '0')
              {
                value = value.substring(0, (value.length() - 1)) +
                  '0' + value.substring(value.length() - 1);
              }
              else
                value += '0';
            }
          }
          else
            value += fchar;
          j++;
        }
      }
    }
    SimpleDateFormat df1 = new SimpleDateFormat(internalFormat);
    SimpleDateFormat df2 = new SimpleDateFormat(externalFormat);
    df2.setLenient(false);
    try
    {
      Date date = df2.parse(value.toString());
      String s = df1.format(date);
      return s;
    }
    catch (Exception ex)
    {
      FacesUtils.addMessage(this, INVALID_DATE_FORMAT, new Object[]{value}, 
        FacesMessage.SEVERITY_ERROR);
      setValid(false);
      return null;
    }
  }

  protected String getDateValue()
  {
    String date = "";
    Object value = getSubmittedValue();
    if (value != null)
    {
      // invalid value
      date = value.toString();
      int index = date.indexOf(getDateTimeSeparator());
      if (index != -1) date = date.substring(0, index);
    }
    else
    {
      value = getValue(); // take value from backed bean in internal format
      if (value != null)
      {
        // apply conversion
        String internalFormat = getInternalFormat();
        String externalFormat = getExternalDateFormat();
        SimpleDateFormat df1 = new SimpleDateFormat(internalFormat);
        SimpleDateFormat df2 = new SimpleDateFormat(externalFormat);
        try
        {
          Date d = df1.parse(value.toString());
          date = df2.format(d);
        }
        catch (Exception ex)
        {
        }
      }
    }
    return date;
  }

  protected String getTimeValue()
  {
    String time = "";
    Object value = getSubmittedValue();
    if (value != null)
    {
      // invalid value
      time = value.toString();
      int index = time.indexOf(getDateTimeSeparator());
      if (index != -1) time = time.substring(index + 1);
    }
    else
    {
      value = getValue(); // take value from backed bean in internal format
      if (value != null)
      {
        // apply conversion
        String externalFormat = getExternalTimeFormat();
        if (externalFormat != null)
        {
          String internalFormat = getInternalFormat();
          SimpleDateFormat df1 = new SimpleDateFormat(internalFormat);
          SimpleDateFormat df2 = new SimpleDateFormat(externalFormat);
          try
          {
            Date d = df1.parse(value.toString());
            time = df2.format(d);
          }
          catch (Exception ex)
          {
          }
        }
      }
    }
    return time;
  }

  private void encodeJavascript(FacesContext context, ResponseWriter writer)
    throws IOException
  {
    writer.startElement("script", this);
    writer.writeAttribute("type", "text/javascript", null);
    String contextPath = context.getExternalContext().getRequestContextPath();
    writer.writeAttribute("src",
      contextPath + "/plugins/calendar/calendar.js", null);
    writer.endElement("script");
  }

  private void encodeDateInputText(FacesContext context,
    ResponseWriter writer, String clientId) throws IOException
  {
    writer.startElement("input", this);
    writer.writeAttribute("id", clientId + "_date", "id");
    writer.writeAttribute("name", clientId + "_date", "name");
    writer.writeAttribute("value", getDateValue(), null);
    writer.writeAttribute("maxlength",
      getExternalDateFormat().length(), "maxlength");
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
    writer.endElement("input");
  }

  private void encodeTimeInputText(FacesContext context,
    ResponseWriter writer, String clientId) throws IOException
  {
    writer.startElement("input", this);
    writer.writeAttribute("id", clientId + "_time", "id");
    writer.writeAttribute("name", clientId + "_time", "name");
    writer.writeAttribute("value", getTimeValue(), null);
    if (isDisabled())
    {
      writer.writeAttribute("disabled", "true", null);
    }
    writer.writeAttribute("maxlength",
      getExternalTimeFormat().length(), "maxlength");
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

  private void encodeDateButton(FacesContext context,
    ResponseWriter writer, String clientId, ResourceBundle bundle) throws IOException
  {
    String language = context.getViewRoot().getLocale().getLanguage();
    String contextPath = context.getExternalContext().getRequestContextPath();
    String image = getButtonImage();
    if (image == null)
    {
      image = "/plugins/calendar/calendar.gif";
    }
    writer.startElement("img", this);
    writer.writeAttribute("id", clientId + "_calendar", null);
    writer.writeAttribute("src", contextPath + image, null);
    if (!isDisabled())
    {
      writer.writeAttribute("onclick",
        "javascript:showCalendarId('" + clientId + "_date" +
        "', '" + language + "');", null);
    }
    String style = getButtonStyle();
    if (style != null)
    {
      writer.writeAttribute("style", style, "style");
    }
    String styleClass = getButtonStyleClass();
    if (styleClass != null)
    {
      writer.writeAttribute("class", styleClass, "styleClass");
    }
    writer.writeAttribute("alt", bundle.getString("calendar"), null);
    writer.endElement("img");
  }

  private void encodeTimeButton(FacesContext context,
    ResponseWriter writer, String clientId, ResourceBundle bundle) throws IOException
  {
    String contextPath = context.getExternalContext().getRequestContextPath();
    String image = "/plugins/calendar/clock.gif";

    String format = getExternalTimeFormat();
    writer.startElement("img", this);
    writer.writeAttribute("id", clientId + "_clock", null);
    writer.writeAttribute("src", contextPath + image, null);
    writer.writeAttribute("onclick",
      "javascript:alert('" + format + "');", null);
    String style = getButtonStyle();
    if (style != null)
    {
      writer.writeAttribute("style", style, "style");
    }
    String styleClass = getButtonStyleClass();
    if (styleClass != null)
    {
      writer.writeAttribute("class", styleClass, "styleClass");
    }
    writer.writeAttribute("alt",  bundle.getString("clock"), null);
    writer.endElement("img");
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

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[10];
    values[0] = super.saveState(context);
    values[1] = _externalFormat;
    values[2] = _internalFormat;
    values[3] = _style;
    values[4] = _styleClass;
    values[5] = _buttonImage;
    values[6] = _buttonStyle;
    values[7] = _buttonStyleClass;
    values[8] = _disabled;
    values[9] = _dateTimeSeparator;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _externalFormat = (String)values[1];
    _internalFormat = (String)values[2];
    _style = (String)values[3];
    _styleClass = (String)values[4];
    _buttonImage = (String)values[5];
    _buttonStyle = (String)values[6];
    _buttonStyleClass = (String)values[7];
    _disabled = (Boolean)values[8];
    _dateTimeSeparator = (String)values[9];
  }
}
