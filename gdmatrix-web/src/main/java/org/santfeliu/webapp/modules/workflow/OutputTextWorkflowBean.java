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
import javax.inject.Named;
import org.santfeliu.util.Properties;
import org.santfeliu.workflow.form.Form;


/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class OutputTextWorkflowBean extends WorkflowBean implements Serializable
{
  private String message;
  private boolean html;

  public OutputTextWorkflowBean()
  {
  }

  public void setMessage(String message)
  {
    this.message = message;
  }

  public String getMessage()
  {
    return message;
  }

  public void setHtml(boolean html)
  {
    this.html = html;
  }

  public boolean isHtml()
  {
    return html;
  }

  @Override
  public String show(Form form)
  {
    Properties parameters = form.getParameters();

    Object value;
    value = parameters.get("message");
    if (value != null) message = String.valueOf(value);
    value = parameters.get("html");
    if (value != null) html = "true".equalsIgnoreCase(String.valueOf(value));

    return "/pages/workflow/output_text_form.xhtml";
  }

  @Override
  public Map submit()
  {
    HashMap variables = new HashMap();
    return variables;
  }
}
