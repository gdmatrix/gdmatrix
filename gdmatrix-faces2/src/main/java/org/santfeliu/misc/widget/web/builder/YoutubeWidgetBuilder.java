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
package org.santfeliu.misc.widget.web.builder;

import org.santfeliu.misc.widget.web.WidgetDefinition;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;
import org.santfeliu.faces.component.HtmlObject;
import org.santfeliu.util.script.WebScriptableBase;
import org.santfeliu.util.template.Template;
import org.santfeliu.util.template.WebTemplate;
import org.santfeliu.web.HttpUtils;

/**
 *
 * @author realor
 */
public class YoutubeWidgetBuilder extends WidgetBuilder
{
  @Override
  public UIComponent getComponent(WidgetDefinition widgetDef,
    FacesContext context)
  {
    Map properties = widgetDef.getProperties();
    Map mutableProperties = new HashMap();
    mutableProperties.putAll(properties);

    String forceHttps = (String)mutableProperties.get("forceHttps");
    if (forceHttps != null && forceHttps.equalsIgnoreCase("true"))
    {
      HttpServletRequest request = 
        (HttpServletRequest)context.getExternalContext().getRequest();
      if (HttpUtils.isSecure(request))
      {
        String code = (String)mutableProperties.get("code");
        code = code.replaceAll("http://", "https://");
        mutableProperties.put("code", code);
      }
    }

    HtmlObject component = new HtmlObject();
    component.setValue(getValue(mutableProperties));
    component.setHttpsDisableOnAgent((String)mutableProperties.get("httpsDisableOnAgent"));
    String disabledMessage = (String)mutableProperties.get("disabledMessage");
    if (disabledMessage != null)
    {
      if (isValueReference(disabledMessage))
        UIComponentTagUtils.setValueBinding(context, component, "disabledMessage",
          disabledMessage);
      else
        component.setDisabledMessage(disabledMessage);
    }
    return component;
  }

  private Object getValue(Map properties)
  {
    try
    {
      InputStream is = getClass().getResourceAsStream("youtube.jsp");
      WebTemplate template = WebTemplate.create(new InputStreamReader(is));
      return template.merge(properties);
    }
    catch (IOException ex)
    {
      return null;
    }
  }
}
