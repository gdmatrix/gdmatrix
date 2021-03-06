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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import org.apache.commons.lang.StringUtils;
import org.apache.myfaces.renderkit.html.ext.HtmlTextareaRenderer;
import org.santfeliu.faces.fckeditor.component.html.FCKFaceEditor;
import org.santfeliu.faces.fckeditor.util.Util;

/**
 *
 * @author unknown
 */
@FacesRenderer(componentFamily="org.fckfaces.FCKFacesFamily",
	rendererType="org.fckfaces.EditorRenderer")
public class FCKFaceEditorRenderer extends HtmlTextareaRenderer
{
  public static final String CUSTOM_CONFIGURATION_PATH = 
    "org.fckfaces.CUSTOM_CONFIGURATIONS_PATH";
  
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
    writer.startElement("script", component.getParent());
    writer.writeAttribute("type", "text/javascript", null);
    writer.writeAttribute("src", 
                          Util.internalPath("/plugins/FCKeditor/fckeditor.js"), 
                          null);
    writer.endElement("script");

    writer.startElement("script", component.getParent());

    String toolBar = "Default";
    if (StringUtils.isNotBlank(ckEditor.getToolbarSet()))
    {
      toolBar = ckEditor.getToolbarSet();
    }

    String heightJS = "";
    String widthJS = "";
    String configPathJS = "";
    String configPropertiesJS = "";

    if (StringUtils.isNotBlank(ckEditor.getHeight()))
    {
      heightJS = "oFCKeditor.Height = '" + ckEditor.getHeight() + "';\r\n";
    }

    if (StringUtils.isNotBlank(ckEditor.getWidth()))
    {
      widthJS = "oFCKeditor.Width = '" + ckEditor.getWidth() + "';\r\n";
    }

    if (StringUtils.isNotBlank(cstConfigPathParam))
    {
      cstConfigPathParam = Util.externalPath(cstConfigPathParam);
      configPathJS = 
          "   oFCKeditor.Config['CustomConfigurationsPath']='" + cstConfigPathParam + 
          "';\r\n";
    }
    
    Map configProperties = ckEditor.getConfigProperties();
    if (configProperties != null)
      configPropertiesJS = getConfigPropertiesJS(configProperties);
    else
      System.out.println("NO CONFIG PROPERTIES");

    String js = 
      "function applyEditor" + ckEditor.getId() + "() {" + "	var sBasePath = '" + 
      Util.internalPath("/plugins/FCKeditor/") + "';\r\n" + 
      "	var sTextAreaName = '" + ckEditor.getClientId(context) + "';\r\n" + 
      "	var oFCKeditor = new FCKeditor( sTextAreaName ) ;\r\n" + 
      configPathJS + configPropertiesJS + 
      "	oFCKeditor.BasePath	= sBasePath ;\r\n" + 
      "	oFCKeditor.ToolbarSet='" + toolBar + "';\r\n" + heightJS + 
      widthJS + "	oFCKeditor.ReplaceTextarea(); \r\n" + 
      "	var oTextbox = document.getElementById(sTextAreaName);\r\n" + 
      "	if(oTextbox.hasChildNodes()) {\r\n" + "		var oTextNode;\r\n" + 
      "		var oParentNode = oTextbox.parentNode;\r\n" + 
      "		if(oTextbox.childNodes.length > 1) {\r\n" + 
      "			for(var i = 0; i < oTextbox.childNodes.length; i++) {\r\n" + 
      "				if(oTextbox.childNodes.item(i).nodeType != 3 ) { //Not a Text node\r\n" + 
      "					oParentNode.appendChild(oTextbox.removeChild(oTextbox.childNodes.item(i)));\r\n" + 
      "					i = i - 1;\r\n" + "				}\r\n" + "			}\r\n" + "		}\r\n" + 
      "	}\r\n" + "}" + "applyEditor" + ckEditor.getId() + "();";

    writer.writeText(js, null);
    writer.endElement("script");
  }  
  
  private String getConfigPropertiesJS(Map configProperties)
  {
    System.out.println("Setting FCKEditor configuration properties");
    System.out.println(configProperties);
    String configPropertiesJS = "";
    Set entries = configProperties.entrySet();
    Iterator it = entries.iterator();
    while (it.hasNext())
    {
      Map.Entry entry = (Map.Entry)it.next();
      String key = (String)entry.getKey();
      if (key != null && !key.equals("CustomConfigurationsPath"))
      {
        Object value = entry.getValue();
        if (value instanceof List)
        {
          List values = (List) value;
          if (values != null && !values.isEmpty())
          {
            StringBuilder buffer = new StringBuilder();
 
            for (int i = 0; i < values.size(); i++)
            {
              if (i > 0) 
                buffer.append(",");
              buffer.append(values.get(i));
            }

            value = buffer.toString();
          }
        }

        configPropertiesJS = configPropertiesJS +  
          "   oFCKeditor.Config['" + key + "']='" + value.toString() + 
          "';\r\n";

      }
    }
    
    return configPropertiesJS;
  }  
}
