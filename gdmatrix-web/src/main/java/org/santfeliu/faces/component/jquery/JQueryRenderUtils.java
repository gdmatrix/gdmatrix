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
package org.santfeliu.faces.component.jquery;

import java.io.IOException;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 *
 * @author blanquepa
 */
public class JQueryRenderUtils
{
  public static final String JQUERY_VERSION = "1.10.2";
  public static final String JQUERY_ENCODED = "JQUERY_ENCODED";  
  
  public static void encodeLibraries(FacesContext context, 
    ResponseWriter writer, UIComponent component) throws IOException
  {
    ExternalContext externalContext = context.getExternalContext();
    String contextPath = externalContext.getRequestContextPath();
    Map requestMap = externalContext.getRequestMap();
    if (!isJQueryPresent(externalContext))
    {
      requestMap.put(JQUERY_ENCODED, "true");
      writer.startElement("script", component);
      writer.writeAttribute("src", contextPath + "/plugins/jquery/jquery-" 
        + JQUERY_VERSION + ".js", null);
      writer.endElement("script");
    }    
  }  
  
  public static boolean isJQueryPresent(ExternalContext externalContext)
  {
    Map requestMap = externalContext.getRequestMap();
    return (requestMap.get(JQUERY_ENCODED) != null);
  }
  
  public static boolean isJQueryPresent()
  {
    FacesContext ctx = FacesContext.getCurrentInstance();
    return isJQueryPresent(ctx.getExternalContext());
  }
  
}
