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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.faces.component.jquery.JQueryRenderUtils;
import org.santfeliu.web.UserSessionBean;


/**
 *
 * @author blanquepa
 */
public class JQueryUIRenderUtils 
{
  public static final String DEFAULT_THEME = "custom";  
  public static final String JQUERY_UI_VERSION = "last"; 
  public static final String JQUERYUI_ENCODED = "_JQUERYUI_ENCODED_";  
  public static final String JS_CALENDAR_ENCODED = "_JS_CALENDAR_ENCODED_";  
  
  private final UIComponent component;
  private String theme;
  private Boolean renderLibraries = true;
  private final ResourceBundle bundle;

  public JQueryUIRenderUtils(UIComponent component)
  {
    this.component = component; 
    Locale locale = UserSessionBean.getCurrentInstance().getViewLocale();
    bundle = ResourceBundle.getBundle(
      "org.santfeliu.faces.component.resources.ComponentBundle", locale);    
  }

  public void encodeLibraries(FacesContext context, ResponseWriter writer) 
    throws IOException
  {
    Map requestMap = context.getExternalContext().getRequestMap();
    renderLibraries = 
      renderLibraries && requestMap.get(JQUERYUI_ENCODED) == null;
    if (renderLibraries)
    {
      requestMap.put(JQUERYUI_ENCODED, "true");
      String contextPath = context.getExternalContext().getRequestContextPath();    
      writer.startElement("link", component);
      writer.writeAttribute("rel", "stylesheet", null);
      writer.writeAttribute("href", contextPath + "/plugins/jquery/ui/" 
        + JQUERY_UI_VERSION + "/themes/" + getTheme() + "/jquery-ui.css", null);

      JQueryRenderUtils.encodeLibraries(context, writer, component);

      writer.startElement("script", component);
      writer.writeAttribute("src", contextPath +  "/plugins/jquery/ui/" 
        + JQUERY_UI_VERSION + "/jquery-ui.js", null);
      writer.endElement("script");
    }
    
    encodeEscapeClientIdFunction(writer);
  }
  
  public void encodeDatePickerLibraries(FacesContext context, 
    ResponseWriter writer) throws IOException
  {   
    Map requestMap = context.getExternalContext().getRequestMap();
    if (requestMap.get(JS_CALENDAR_ENCODED) == null)
    {
      requestMap.put(JS_CALENDAR_ENCODED, "true");
      
      //DatePicker
      String contextPath = context.getExternalContext().getRequestContextPath();    
      writer.startElement("script", component);
      writer.writeAttribute("src", contextPath +  
        "/plugins/jquery/datepicker/datepicker-ca.js", null);
      writer.endElement("script");

      writer.startElement("script", component);
      writer.writeAttribute("src", contextPath +  
        "/plugins/jquery/datepicker/datepicker-es.js", null);
      writer.endElement("script");

      writer.startElement("script", component);
      writer.writeAttribute("src", contextPath +  
        "/plugins/jquery/datepicker/accessibility.js", null);
      writer.endElement("script");

      writer.startElement("link", component);
      writer.writeAttribute("rel", "stylesheet", null);
      writer.writeAttribute("href", contextPath +  
        "/plugins/jquery/datepicker/datepicker.css", null);    

      //Add timepicker extension to datepicker
      writer.startElement("script", component);
      writer.writeAttribute("src", contextPath +  
        "/plugins/jquery/timepicker/jquery-ui-timepicker-addon.js", null);
      writer.endElement("script");

      writer.startElement("script", component);
      writer.writeAttribute("src", contextPath +  
        "/plugins/jquery/timepicker/jquery-ui-timepicker-es.js", null);
      writer.endElement("script");        

      writer.startElement("script", component);
      writer.writeAttribute("src", contextPath +  
        "/plugins/jquery/timepicker/jquery-ui-timepicker-ca.js", null);
      writer.endElement("script");        

      writer.startElement("script", component);
      writer.writeAttribute("src", contextPath +  
        "/plugins/jquery/timepicker/accessibility.js", null);
      writer.endElement("script");    

      writer.startElement("link", component);
      writer.writeAttribute("rel", "stylesheet", null);
      writer.writeAttribute("href", contextPath +  
        "/plugins/jquery/timepicker/jquery-ui-timepicker-addon.css", null); 
    }
  } 
  
  private void encodeDatePickerBegin(ResponseWriter writer) throws IOException
  {
    String language = 
      UserSessionBean.getCurrentInstance().getViewLanguage();   
    
    writer.startElement("script", component);
    writer.writeText("$(function() {", null);
    writer.writeText("$.datepicker"
      + ".setDefaults($.datepicker.regional['" + language + "']);", null);    
  }
  
  private void encodeDatePickerEnd(ResponseWriter writer) throws IOException
  {
    writer.writeText("});", null);
    writer.endElement("script");       
  }
  
  public void encodeDatePicker(ResponseWriter writer, String fieldName,
    String datePattern, String buttonText, String inputStyle) throws IOException
  {
    encodeDatePickerBegin(writer);
    
    if (buttonText == null && bundle != null)
      buttonText = bundle.getString("calendar");
    if (datePattern == null)
      datePattern = "dd/MM/yyyy";    
    String placeholder = datePattern.toUpperCase();
    datePattern = translateDateTimePattern(datePattern);
    if (inputStyle != null)
      inputStyle = resizeDateInputWidth(inputStyle);      
     
    String code = "$( \"input[name='" + fieldName + "']\" )"
      + ".datepicker({"
      + "showOn: 'button',"
      + "buttonImage: '/plugins/jquery/datepicker/calendar.gif',"
      + "buttonImageOnly: false,"
      + "showButtonPanel: true,"
      + "onClose: function (){ this.focus();},"
      + "dateFormat: '" + datePattern + "',"
      + "changeMonth: true,"
      + "changeYear: true"
      + "});" 
      + "var dpInput = $('[name=\"" + fieldName + "\"]');"
      + (inputStyle != null ? "dpInput.attr('style','" + inputStyle + "');" : "")
      + "dpInput.attr('placeholder','" + placeholder + "');"      
      + "var dpButton = dpInput.next();"
      + "dpButton.attr('title', '" + buttonText + "');"    
      + "dpButton.contents().first().attr('alt', '" + buttonText + "');"   
      + "dpButton.contents().first().attr('title', '" + buttonText + "');" 
      + "dpButton.attr('style','" + getDatePickerButtonStyle(inputStyle) + "');"            
      + "makeDatePickerAccessible('" + fieldName + "');";

      writer.writeText(code, null);
      
      encodeDatePickerEnd(writer);
  }
  
  public void encodeDateTimePicker(ResponseWriter writer, String fieldName,
    String datePattern, String timePattern, String buttonText, 
    String inputStyle) throws IOException
  {
    encodeDatePickerBegin(writer);
    
    if (buttonText == null && bundle != null)
      buttonText = bundle.getString("calendar"); 
    if (datePattern == null)
      datePattern = "dd/mm/yy";
    if (timePattern == null)
      timePattern = "hh:mm";
    String placeholder = 
      datePattern.toUpperCase() + " " + timePattern.toUpperCase();
    datePattern = translateDateTimePattern(datePattern);
    
    if (inputStyle != null)
      inputStyle = resizeDateInputWidth(inputStyle);  
  
    String code = "$( \"input[name='" + fieldName +"']\" )"
      + ".datetimepicker({" 
      + "showOn: 'button',"
      + "buttonImage: '/plugins/jquery/datepicker/calendar.gif',"
      + "buttonImageOnly: false,"
      + "showButtonPanel: true,"
      + "onClose: function (){ this.focus();},"
      + "dateFormat: '" + datePattern + "',"
      + "timeFormat: '" + timePattern + "',"
      + "separator: ' ',"
      + "changeMonth: true,"
      + "changeYear: true,"
      + "controlType: 'select',"
      + "oneLine: true"
      + "});"  
      + "var dpInput = $('[name=\"" + fieldName + "\"]');"
      + (inputStyle != null ? "dpInput.attr('style','" + inputStyle + "');" : "")
      + "dpInput.attr('placeholder','" + placeholder + "');"        
      + "var dpButton = dpInput.next();"  
      + "dpButton.contents().first().attr('alt', '" + buttonText + "');"   
      + "dpButton.contents().first().attr('title', '" + buttonText + "');"        
      + "dpButton.attr('style','" + getDatePickerButtonStyle(inputStyle) + "');"            
      + "makeDatePickerAccessible('" + fieldName + "');";            
    writer.writeText(code, null);
    
    encodeDatePickerEnd(writer);    
  }
  
  /**
   * Extracts left, top and width from input field and generates the button
   * position next to control.
   */
  private String getDatePickerButtonStyle(String style)
  { 
    StringBuilder sb = new StringBuilder();    
    try
    {
      if (StringUtils.isBlank(style))
        return "inherit";
      
      sb.append("position:absolute;");
      //Extract left, top and width attibuttes
      Pattern pattern = 
        Pattern.compile("(.*;)?left:(\\d*)px;top:(\\d*)px.*;width:(\\d*)px.*");
      Matcher matcher = pattern.matcher(style);
      matcher.find();      
      String left = matcher.group(2);
      String top = matcher.group(3);    
      String width = matcher.group(4);       
      int margin = 4;
      Integer ileft = Integer.valueOf(left) + Integer.valueOf(width) + margin;
      sb.append("left:").append(String.valueOf(ileft)).append("px;");
      sb.append("top:").append(top).append("px;");
    }
    catch (Exception ex)
    {
      sb.append("inherit");
    }
    return sb.toString();
  }  
  
  private String resizeDateInputWidth(String style)
  { 
    String result = style;    
 
    try
    {
      //Extract left, top and width attributtes
      Pattern pattern = 
        Pattern.compile("(.*;)?width:(\\d*)px.*");
      Matcher matcher = pattern.matcher(style);
      matcher.find();
      String width = matcher.group(2);       
      Integer iwidth = Integer.valueOf(width);
      if (iwidth > 16) 
        iwidth = (iwidth - 16);
      result = style.replaceAll("width:" + width + "px", 
        "width:" + String.valueOf(iwidth) + "px");
    }
    catch (Exception ex)
    {
      result = style;
    }
      
    return result;
  }  
  
  /**
   * Translate dateTime pattern from java Date format to jQuery dateTimePicker 
   * format.
   */
  private String translateDateTimePattern(String pattern)
  {
    pattern = pattern.toLowerCase();
    pattern = pattern.replaceAll("yyyy", "yy");
    return pattern;
  }  
  
  public void encodeEscapeClientIdFunction(ResponseWriter writer) 
    throws IOException
  {
    writer.startElement("script", component);
    String code = 
      "function escapeClientId(id){return '#' + id.replace(/:/g,'\\\\:');}";
    writer.writeText(code, null);
    writer.endElement("script");
  }
  
  public String getJQueryUIVersion()
  {
    return JQUERY_UI_VERSION;
  }
  
  public String getJQueryVersion()
  {
    return JQueryRenderUtils.JQUERY_VERSION;
  }
  
  public String getDefaultTheme()
  {
    return DEFAULT_THEME;
  }

  public String getTheme()
  {
    return theme != null ? theme : getDefaultTheme();
  }

  public void setTheme(String theme)
  {
    this.theme = theme;
  }
  
  public String getFieldId(String clientId, String name)
  {
    return clientId + ":" + name;
  }   

  public Boolean isRenderLibraries()
  {
    return renderLibraries;
  }

  public void setRenderLibraries(Boolean renderLibraries)
  {
    this.renderLibraries = renderLibraries;
  }
 
}
