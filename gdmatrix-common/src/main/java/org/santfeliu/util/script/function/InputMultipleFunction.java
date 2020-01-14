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

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/*
 * Usage: inputMultiple(name, inputOptions, buttonOptions)
 *
 */
/**
 *
 * @author unknown
 */
public class InputMultipleFunction extends BaseFunction
{
  @Override
  public Object call(Context cx, Scriptable scope, Scriptable thisObj,
    Object[] args)
  {
    if (args == null || args.length < 1)
      return "";
    String name = (String)args[0];
    Scriptable inputOptions = null;
    Scriptable buttonOptions = null;
    if (args.length >= 2)
      inputOptions = (Scriptable)args[1];
    if (args.length == 3)
      buttonOptions = (Scriptable)args[2];

    StringBuilder buffer = new StringBuilder();

    //JavaScript
    buffer.append("<script type=\"text/javascript\">");
    buffer.append("function removeInput(inputName)");
    buffer.append("{");
    buffer.append("var elementId = inputName + '_id';");    
    buffer.append("var element = document.getElementById(elementId).lastChild;");
    buffer.append("var parent = element.parentNode;");
    buffer.append("if (parent.childNodes.length > 1) parent.removeChild(element);");
    buffer.append("return;");
    buffer.append("}");
    buffer.append("function addInput(inputName)");
    buffer.append("{");
    buffer.append("var elementId = inputName + '_id';");
    buffer.append("var element = document.getElementById(elementId).lastChild;");
    buffer.append("var newInput = element.cloneNode();");
    buffer.append("newInput.value='';");
    buffer.append("element.parentNode.insertBefore(newInput, element.nextSibling);");
    buffer.append("return;");
    buffer.append("}");
    buffer.append("</script>");

    //HTML
    buffer.append("<div id=\"").append(name).append("_id\">");
    buffer.append("<input type=\"text\" name=\"").append(name).append("\" ");

    if (inputOptions != null)
    {
      Object[] ids = inputOptions.getIds();
      for (int i = 0; i < ids.length; i++)
      {
        String attrName = ids[i].toString();
        appendAttribute(buffer, inputOptions, scope, attrName);
      }
    }
    buffer.append("multiple=\"true\" />");
    buffer.append("</div>");
    buffer.append("<input type=\"button\" name=\"").append(name).append("_button1\" onclick=\"addInput('" + name + "')\" value=\"+\"");
    if (buttonOptions != null)
    {
      Object[] ids = buttonOptions.getIds();
      for (int i = 0; i < ids.length; i++)
      {
        String attrName = ids[i].toString();
        appendAttribute(buffer, buttonOptions, scope, attrName);
      }
    }
    buffer.append(" />");
    buffer.append("<input type=\"button\" name=\"").append(name).append("_button2\" onclick=\"removeInput('" + name + "')\" value=\"-\"");
    if (buttonOptions != null)
    {
      Object[] ids = buttonOptions.getIds();
      for (int i = 0; i < ids.length; i++)
      {
        String attrName = ids[i].toString();
        appendAttribute(buffer, buttonOptions, scope, attrName);
      }
    }
    buffer.append(" />");


    return buffer.toString();
  }

  private void appendAttribute(StringBuilder buffer, Scriptable options,
    Scriptable scope, String attrName)
  {
    String attrValue = options.get(attrName, scope).toString();
    if (attrName.startsWith("_"))
      attrName = attrName.substring(1);
    buffer.append(attrName).append("=").append(attrValue).append(" ");
  }
}
