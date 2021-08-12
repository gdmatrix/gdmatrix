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
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 */
public class DateTimePickerForFunction extends BaseFunction
{
  @Override
  public Object call(Context cx, Scriptable scope, Scriptable thisObj,
    Object[] args)
  {
    String fieldName = (String)args[0];
    String language = UserSessionBean.getCurrentInstance().getViewLanguage();

    StringBuilder buffer = new StringBuilder();

    buffer.append("&lt;link rel=\"stylesheet\" href=\"/plugins/jquery/ui/last/themes/smoothness/jquery-ui.css\"&gt;");
    buffer.append("<script src=\"/plugins/jquery/jquery-last.min.js\"></script>");
    buffer.append("<script src=\"/plugins/jquery/ui/last/jquery-ui.js\"></script>");
    buffer.append("<script src=\"/plugins/jquery/datepicker/datepicker-ca.js\"></script>");
    buffer.append("<script src=\"/plugins/jquery/datepicker/datepicker-es.js\"></script>");
    buffer.append("<script src=\"/plugins/jquery/timepicker/jquery-ui-timepicker-addon.js\"></script>");    
    buffer.append("<script src=\"/plugins/jquery/timepicker/jquery-ui-timepicker-es.js\"></script>");
    buffer.append("<script src=\"/plugins/jquery/timepicker/jquery-ui-timepicker-ca.js\"></script>");
    buffer.append("&lt;link rel=\"stylesheet\" href=\"/plugins/jquery/timepicker/jquery-ui-timepicker-addon.css\"&gt;");    
    
    buffer.append("<script>");
    buffer.append("$(function() {");
    buffer.append("$( \"#").append(fieldName).append("\" ).datetimepicker( $.timepicker.regional[ \"").append(language).append("\" ] );");
    buffer.append("});");
    buffer.append("</script>    ");
//    buffer.append("<input type=\"text\" id=\"").append(fieldName).append("\" name=\"").append(fieldName).append("\" format=\"datetime:dd/MM/yyyy HH:mm\">");
    return buffer.toString();
  }  
}
