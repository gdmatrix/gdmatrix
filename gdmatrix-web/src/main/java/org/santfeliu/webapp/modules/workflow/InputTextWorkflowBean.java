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
package org.santfeliu.webapp.modules.workflow;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.inject.Named;
import org.santfeliu.util.Properties;
import org.santfeliu.workflow.form.Form;


/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class InputTextWorkflowBean extends WorkflowBean implements Serializable
{
  private String message;
  private String text;
  private String varName;

  @Inject
  WorkflowInstanceBean instanceBean;

  public InputTextWorkflowBean()
  {
  }

  public String getVarName()
  {
    return varName;
  }

  public String getMessage()
  {
    return message;
  }

  public void setText(String text)
  {
    this.text = text;
  }

  public String getText()
  {
    return text;
  }

  public void validateText(FacesContext context, UIComponent component,
    Object value)
  {
    String s = value.toString();
    if (s.replaceAll("\n", "").trim().length() == 0)
    {
      throw new ValidatorException(
        new FacesMessage("Has d'escriure alguna cosa"));
    }
  }

  @Override
  public String show(Form form)
  {
    Properties parameters = form.getParameters();

    Object value;
    value = parameters.get("var");
    if (value != null) varName = String.valueOf(value);
    value = parameters.get("message");
    if (value != null) message = String.valueOf(value);
    value = parameters.get("value");
    if (value instanceof String)
    {
      text = (String)value;
    }
    else
    {
      value = instanceBean.getVariables().get(varName);
      if (value instanceof String)
      {
        text = (String)value;
      }
      else text = null;
    }
    return "/pages/workflow/input_text_form.xhtml";
  }

  @Override
  public Map submit()
  {
    HashMap variables = new HashMap();
    variables.put(this.varName, text);
    return variables;
  }
}
