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
package org.santfeliu.util.script.function;


import java.util.HashMap;
import java.util.Map;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Context;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.component.jquery.JQueryRenderUtils;

/**
 * Usages:  htmlEditor(fieldName); //Renders FckEditor.
 *          htmlEditor(fieldName, toolBar); //Renders FckEditor with custom toolbar.
 *          htmlEditor(fieldName, 'ckEditor'); //Renders ckEditor with default dimensions.
 *          htmlEditor(fieldName, 'ckEditor', width, height); //Use newer ckEditor with width and height box dimensions.
 * 
 * @author blanquepa
 */
public class HtmlEditorFunction extends BaseFunction
{
  @Override
  public Object call(Context cx, Scriptable scope, Scriptable thisObj,
    Object[] args)
  {
    String fieldName = (String)args[0];
    String toolbarset = "Reduced";
    if (args.length > 1)
      toolbarset = (String)args[1];

    StringBuilder buffer = new StringBuilder();

    if (!toolbarset.equals("ckEditor"))
    {
      buffer.append("<textarea name=\"").append(fieldName).append("\"");
      buffer.append("id=\"").append(fieldName).append("\"/>");
      buffer.append("<script type=\"text/javascript\"");
      buffer.append("src=\"/fckfaces/plugins/FCKeditor/fckeditor.js\">");
      buffer.append("</script>");
      buffer.append("<script>");
      buffer.append("var sBasePath = '/fckfaces/plugins/FCKeditor/';");
      buffer.append("var sTextAreaName = '").append(fieldName).append("'; ");
      buffer.append("var oFCKeditor = new FCKeditor( sTextAreaName ) ; ");
      buffer.append("oFCKeditor.BasePath = sBasePath ; ");
      buffer.append("oFCKeditor.ToolbarSet = '").append(toolbarset).append("'; ");
      buffer.append("oFCKeditor.Height = '100%'; ");
      buffer.append("oFCKeditor.Width = '100%'; ");
      buffer.append("oFCKeditor.ReplaceTextarea(); ");
      buffer.append("var oTextbox = document.getElementById(sTextAreaName); ");
      buffer.append("if(oTextbox.hasChildNodes()) { ");
      buffer.append("var oTextNode; ");
      buffer.append("var oParentNode = oTextbox.parentNode; ");
      buffer.append("if(oTextbox.childNodes.length > 1) { ");
      buffer.append("for(var i = 0; i < oTextbox.childNodes.length; i++) { ");
      buffer.append("if(oTextbox.childNodes.item(i).nodeType != 3 ) { ");
      buffer.append("oParentNode.appendChild(oTextbox.removeChild(oTextbox.childNodes.item(i))); ");
      buffer.append("i = i - 1;}}}}");
      buffer.append("</script>");
    }
    else
    {
      String height = "";
      String width = "";
      if (args.length >= 4)
      {
        int w = Integer.parseInt((String) args[2]) - 2;
        int h = Integer.parseInt((String) args[3]) - 78; //Toolbar height
        height = String.valueOf(h);
        width = String.valueOf(w);
      }
      Map configMap = new HashMap();
      configMap.put("toolbarCanCollapse", "true");
      configMap.put("language",  FacesUtils.getViewLanguage());
      configMap.put("height", height);
      configMap.put("width", width);
      configMap.put("removeButtons", "Scayt,About,A11ychecker,Find,Replace,Anchor,Superscript,Subscript,RemoveFormat,Outdent,Indent,Blockquote,Styles,Format,SpecialChar,HorizontalRule");
      configMap.put("removePlugins", "scayt,elementspath,resize");
      configMap.put("toolbarGroups", "[" +
        "{ name: 'document', groups: [ 'mode', 'document', 'doctools' ] }," +
        "{ name: 'clipboard', groups: [ 'clipboard', 'undo' ] }," +
        "{ name: 'editing', groups: [ 'find', 'selection', 'editing' ] }," +
        "{ name: 'links', groups: [ 'links' ] }," +
        "{ name: 'insert', groups: [ 'insert' ] }," +
        "{ name: 'forms', groups: [ 'forms' ] }," +
        "{ name: 'tools', groups: [ 'tools' ] }," +
        "{ name: 'others', groups: [ 'others' ] }," +
        "{ name: 'basicstyles', groups: [ 'basicstyles', 'cleanup' ] }," +
        "{ name: 'paragraph', groups: [ 'list', 'indent', 'blocks', 'align', 'bidi', 'paragraph' ] }," +
        "{ name: 'styles', groups: [ 'styles' ] }," +
        "{ name: 'colors', groups: [ 'colors' ] }," +
        "{ name: 'about', groups: [ 'about' ] }" +
        "]");
      String config = toString(configMap);      
      
      buffer.append("<textarea name=\"").append(fieldName).append("\"");
      buffer.append("id=\"").append(fieldName).append("\"/>");
      buffer.append("<script src=\"/plugins/ckeditor/ckeditor.js\">");
      buffer.append("</script>");
      if (!JQueryRenderUtils.isJQueryPresent())
      {
        buffer.append("<script src=\"/plugins/jquery/jquery-last.min.js\">");
        buffer.append("</script>");
      }
      buffer.append("<script>");
      buffer.append("CKEDITOR.replace( '" + fieldName + "' , " + config + " );");
      buffer.append("</script>");
    }

    return buffer.toString();
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
}
