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
package org.santfeliu.util.script;

import org.mozilla.javascript.Scriptable;
import org.santfeliu.util.script.function.CloneDocumentFunction;
import org.santfeliu.util.script.function.DateTimePickerForFunction;
import org.santfeliu.util.script.function.GetBeanFunction;
import org.santfeliu.util.script.function.HtmlEditorFunction;
import org.santfeliu.util.script.function.ObjectActionsManagerFunction;
import org.santfeliu.util.script.function.WebMessageFunction;

/**
 *
 * @author blanquepa
 */
public class WebFunctionFactory
{
  private static DateTimePickerForFunction dateTimePickerForFunction = new DateTimePickerForFunction();
  private static CloneDocumentFunction cloneDocumentFunction = new CloneDocumentFunction();
  private static ObjectActionsManagerFunction objectActionsManagerFunction = new ObjectActionsManagerFunction();
  private static WebMessageFunction webMessageFunction = new WebMessageFunction();
  private static HtmlEditorFunction htmlEditorFunction = new HtmlEditorFunction();
  private static GetBeanFunction getBeanFunction = new GetBeanFunction();
  
  public static void initFunctions(Scriptable scriptable)
  {
    // init built-in functions
    FunctionFactory.initFunctions(scriptable);
    scriptable.put("dateTimePickerFor", scriptable, dateTimePickerForFunction);
    scriptable.put("cloneDocument", scriptable, cloneDocumentFunction);
    scriptable.put("ObjectActionsManager", scriptable, objectActionsManagerFunction);
    scriptable.put("webMessage", scriptable, webMessageFunction);
    scriptable.put("htmlEditor", scriptable, htmlEditorFunction);    
    scriptable.put("getBean", scriptable, getBeanFunction);    
  }
}
