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
package org.santfeliu.faces;

import java.io.IOException;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 *
 * @author unknown
 */
public class HtmlRenderUtils
{
  public static final String HIDDEN_LINK_ID = "_idsflink";
  public static final String HIDDEN_LINK_RENDERED = "HIDDEN_LINK_RENDERED";
  public static final String OVERLAY_RENDERED = "OVERLAY_RENDERED";

  public static void renderOverlay(ResponseWriter writer) throws IOException
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Map requestMap = context.getExternalContext().getRequestMap();

    if (!requestMap.containsKey(OVERLAY_RENDERED))
    {
      writer.startElement("div", null);
      writer.writeAttribute("id", "_overlay_", null);
      writer.startElement("div", null);
      writer.writeAttribute("id", "_overlay_loading", null);
      writer.endElement("div");      
      writer.endElement("div");
      requestMap.put(OVERLAY_RENDERED, Boolean.TRUE);
    }
  }

  public static void renderHiddenLink(UIComponent component, 
    FacesContext context) throws IOException
  {
    Map requestMap = context.getExternalContext().getRequestMap();
    if (!requestMap.containsKey(HIDDEN_LINK_RENDERED))
    {
      String formId = FacesUtils.getParentFormId(component, context);
      if (formId != null)
      {
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("input", component);
        writer.writeAttribute("type", "hidden", null);
        writer.writeAttribute("name", formId + ":" + HIDDEN_LINK_ID, null);
        writer.endElement("input");
      }
      requestMap.put(HIDDEN_LINK_RENDERED, Boolean.TRUE);
    }
  }
}
