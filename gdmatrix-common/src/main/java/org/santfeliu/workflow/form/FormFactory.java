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
package org.santfeliu.workflow.form;

import org.matrix.workflow.WorkflowConstants;

import org.santfeliu.util.LineTokenizer;
import org.santfeliu.util.Properties;


/**
 *
 * @author unknown
 */
public class FormFactory
{
  public FormFactory()
  {
  }

  public static Form createInstance(String type)
  {
    Form form = null;
    try
    {
      if (type.length() > 0)
      {
        String formClassName = "org.santfeliu.workflow.form." + 
          Character.toUpperCase(type.charAt(0)) + 
          type.substring(1) + "Form";
        form = (Form)Class.forName(formClassName).newInstance();
      }
    }
    catch (Exception ex)
    {
      // ignore
    }
    return form == null ? new Form() : form;
  }
  
  public static Form parse(String value)
  {
    Form form;
    int index = value.indexOf(" ");
    if (index == -1) // FORWARD or BACKWARD state
    {
      form = new Form();
      form.setState(value);
      return form;
    }
    String state = value.substring(0, index);
    String definition = value.substring(index + 1).trim();
    String error = "";
    Properties parameters = new Properties();

    int index2 = definition.indexOf(WorkflowConstants.FORM_PARAMETERS_SEPARATOR);
    if (index2 != -1)
    {
      String params = definition.substring(
        index2 + WorkflowConstants.FORM_PARAMETERS_SEPARATOR.length());
      definition = definition.substring(0, index2).trim();
      parameters.loadFromString(params);
    }
    LineTokenizer tokenizer = new LineTokenizer(definition, " ");
    String type = tokenizer.nextToken();
    String group = tokenizer.nextToken();
    String description = tokenizer.nextToken();
    boolean forwardEnabled = false;
    boolean backwardEnabled = false;
    if (tokenizer.countTokens() >= 4) // buttons definition
    {
      String buttons = tokenizer.nextToken(); // [FB] or [F] or [B] or []
      forwardEnabled = 
        (buttons.contains(WorkflowConstants.FORWARD_BUTTON_ENABLED));
      backwardEnabled = 
        (buttons.contains(WorkflowConstants.BACKWARD_BUTTON_ENABLED));
    }
    if (tokenizer.countTokens() >= 5) // error message
    {
      error = tokenizer.nextToken();
    }
    form = createInstance(type);
    form.setState(state);
    form.setType(type);
    form.setDescription(description);
    form.setForwardEnabled(forwardEnabled);
    form.setBackwardEnabled(backwardEnabled);
    form.setGroup(group);
    form.setError(error);
    form.setParameters(parameters);
    return form;
  }
  
  public static String format(Form form)
  {
    String formValue = 
      form.getState() + " " + 
      form.getType() + " " + 
      form.getGroup() + " \"" + 
      form.getDescription() + "\" ";

    formValue += "[";
    if (form.isForwardEnabled())
      formValue += WorkflowConstants.FORWARD_BUTTON_ENABLED;
    if (form.isBackwardEnabled()) 
      formValue += WorkflowConstants.BACKWARD_BUTTON_ENABLED;
    formValue += "]";

    if (form.getError().length() > 0)
    {
      formValue += " \"" + form.getError() + "\"";
    }

    if (form.getParameters().size() > 0)
    {
      formValue += 
        WorkflowConstants.FORM_PARAMETERS_SEPARATOR + 
        form.getParameters().saveToString();
    }
    return formValue;
  }
}
