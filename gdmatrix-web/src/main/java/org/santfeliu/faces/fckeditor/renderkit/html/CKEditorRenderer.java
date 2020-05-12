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
package org.santfeliu.faces.fckeditor.renderkit.html;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import org.apache.commons.lang.StringUtils;
import org.apache.myfaces.renderkit.html.ext.HtmlTextareaRenderer;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.component.jquery.JQueryRenderUtils;
import org.santfeliu.faces.fckeditor.component.html.FCKFaceEditor;
import org.santfeliu.faces.fckeditor.util.Util;

/**
 *
 * @author blanquepa
 */
@FacesRenderer(componentFamily="org.fckfaces.FCKFacesFamily",
	rendererType="org.ckfaces.EditorRenderer")
public class CKEditorRenderer extends HtmlTextareaRenderer
{
  public static final String CUSTOM_CONFIGURATION_PATH = 
    "org.ckfaces.CUSTOM_CONFIGURATIONS_PATH";
  
  @Override
  public void encodeEnd(FacesContext context, UIComponent component)
    throws IOException
  {
    super.encodeEnd(context, component);
  
    FCKFaceEditor ckEditor = (FCKFaceEditor) component;

    ResponseWriter writer = context.getResponseWriter();
    
    //Initial Configuration
    final ExternalContext external = context.getExternalContext();
    String cstConfigPathParam = 
      external.getInitParameter(CUSTOM_CONFIGURATION_PATH);

    //Initial JS link
    writer.startElement("script", ckEditor);
    writer.writeAttribute("src", 
                          Util.internalPath("/plugins/ckeditor/ckeditor.js"), 
                          null);
    writer.endElement("script");

    JQueryRenderUtils.encodeLibraries(context, writer, component);

    writer.startElement("script", ckEditor);
    
    Map configMap = new HashMap();
    configMap.put("language",  FacesUtils.getViewLanguage());
    if (StringUtils.isNotBlank(ckEditor.getHeight()))
      configMap.put("height", ckEditor.getHeight());
    if (StringUtils.isNotBlank(ckEditor.getWidth()))
      configMap.put("width", ckEditor.getWidth());
    if (StringUtils.isNotBlank(cstConfigPathParam))
    {
      cstConfigPathParam = Util.externalPath(cstConfigPathParam);
      configMap.put("customConfig", cstConfigPathParam);
    }
    
    //Contents CSS
    Map configProperties = ckEditor.getConfigProperties();
    if (configProperties != null)
    {
      List styles = (List) configProperties.get("ContentsCSS");
      if (styles != null && !styles.isEmpty())
      {
        StringBuilder buffer = new StringBuilder();
        buffer.append("[");
        for (int i = 0; i < styles.size(); i++)
        {
          if (i > 0) 
            buffer.append(",");
          buffer.append("'").append(styles.get(i)).append("'");
        }
        buffer.append("]");
        configMap.put("contentsCss", buffer.toString()); 
        configProperties.remove("ContentsCSS");
        configProperties.remove("EditorAreaCSS");      
      }
      configMap.putAll(convertLegacyProperties(configProperties));      
    }
   
    String config = toString(configMap);
    
    String js = "CKEDITOR.replace( '" + ckEditor.getClientId(context) + "' , " + config + " );";

    writer.writeText(js, null);
    writer.endElement("script");
  }
  
  private String toString(Map map)
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append("{");
    for (Object key : map.keySet())
    {
      buffer.append(key);
      buffer.append(": ");
      String value = (String)map.get(key);
      if (!value.startsWith("[")) 
        buffer.append("\"");
      buffer.append(value);
      if (!value.startsWith("["))       
        buffer.append("\"");
      buffer.append(",");
    }
    buffer.deleteCharAt(buffer.length()-1);
    buffer.append("}");
    return buffer.toString();
  }
  
  private Map convertLegacyProperties(Map map)
  {
    /* 
      Complete conversions matrix at: 
      https://docs-old.ckeditor.com/CKEditor_3.x/Developers_Guide/FCKeditor_CKEditor_Configuration_Mapping
    */
    String[][] props = 
    {
      {"DefaultLanguage", "defaultLanguage"},
    };
   
    for (int i = 0; i < props.length; i++)
    {
      if (map.containsKey(props[i][0]) && !map.containsKey(props[i][1]))
      {
        map.put(props[i][1], map.get(props[i][0]));
        map.remove(props[i][0]);
      }
    }
        
    return map;
  }
  
  
}
