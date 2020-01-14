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
package org.santfeliu.web.obj.util;

import java.util.Map;

/**
 * This parameters processor manage the parameters in a CKEditor call to custom 
 * file manager call according to its File Browser API. 
 *
 * @author blanquepa
 */
public class CKEditorParametersProcessor extends ParametersProcessor
{
  public static final String EDITOR_INSTANCE_PARAM = "CKEditor";
  public static final String LANGUAGE_PARAM = "langCode";
  public static final String CALLBACK_PARAM = "CKEditorFuncNum";  
  
  private String editorInstance;
  private String callbackReference;
  private String language;

  @Override
  public String processParameters(Map parameters)
  {
    this.editorInstance = (String) parameters.get(EDITOR_INSTANCE_PARAM);
    this.language = (String) parameters.get(LANGUAGE_PARAM);
    this.callbackReference = (String) parameters.get(CALLBACK_PARAM);
    
    return null;
  }

  public String getEditorInstance()
  {
    return editorInstance;
  }

  public String getCallbackReference()
  {
    return callbackReference;
  }

  public String getLanguage()
  {
    return language;
  }
 
}
