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
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.santfeliu.faces.component.jquery.JQueryRenderUtils;


/**
 *
 * @author blanquepa
 */
public class JQueryUIRenderUtils 
{
  public static final String DEFAULT_THEME = "custom";  
  public static final String JQUERY_VERSION = "1.10.2";
  public static final String JQUERY_UI_VERSION = "1.11.4"; 
  
  private UIComponent component;
  private String theme;
  
  public JQueryUIRenderUtils(UIComponent component)
  {
    this.component = component;
  }

  public void encodeLibraries(FacesContext context, ResponseWriter writer) throws IOException
  {
    String contextPath = context.getExternalContext().getRequestContextPath();    
    writer.startElement("link", component);
    writer.writeAttribute("rel", "stylesheet", null);
    writer.writeAttribute("href", contextPath + "/plugins/jquery/ui/" + JQUERY_UI_VERSION + "/themes/" + getTheme() + "/jquery-ui.css", null);

    JQueryRenderUtils.encodeLibraries(context, writer, component);

    writer.startElement("script", component);
    writer.writeAttribute("src", contextPath +  "/plugins/jquery/ui/" + JQUERY_UI_VERSION + "/jquery-ui.js", null);
    writer.endElement("script");
    
    encodeEscapeClientIdFunction(writer);
  }
  
  public void encodeEscapeClientIdFunction(ResponseWriter writer) throws IOException
  {
    writer.startElement("script", component);
    writer.writeText("function escapeClientId(id){return '#' + id.replace(/:/g,'\\\\:');}", null);
    writer.endElement("script");
  }
  
  public String getJQueryUIVersion()
  {
    return JQUERY_UI_VERSION;
  }
  
  public String getJQueryVersion()
  {
    return JQUERY_VERSION;
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
}
