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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.santfeliu.util.Properties;
import org.santfeliu.workflow.form.Form;


/**
 *
 * @author realor
 * @author lopezrj
 */
@Named
@RequestScoped
public class SelectMenuWorkflowBean extends WorkflowBean implements Serializable
{
  private String varName;
  private String message;
  private List options;
  private String selectedCode;
  private String layout = "list";
  private String cssCustom;
  private String cssFileUrl;

  @Inject
  WorkflowInstanceBean instanceBean;

  public SelectMenuWorkflowBean()
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

  public void setOptions(List options)
  {
    this.options = options;
  }

  public List getOptions()
  {
    return options;
  }

  public String getLayout()
  {
    return layout;
  }

  public void setLayout(String layout)
  {
    this.layout = layout;
  }

  public String getCssCustom()
  {
    return cssCustom;
  }

  public void setCssCustom(String cssCustom)
  {
    this.cssCustom = cssCustom;
  }

  public String getCssFileUrl()
  {
    return cssFileUrl;
  }

  public void setCssFileUrl(String cssFileUrl)
  {
    this.cssFileUrl = cssFileUrl;
  }

  @Override
  public String show(Form form)
  {
    Properties parameters = form.getParameters();

    instanceBean.setForwardEnabled(false);

    Object value;
    value = parameters.get("var");
    if (value != null) varName = String.valueOf(value);
    value = parameters.get("message");
    if (value != null) message = String.valueOf(value);

    options = new ArrayList();
    int i = 0;
    Object code = parameters.get("code" + i);
    while (code != null)
    {
      String scode = code.toString();
      String label = String.valueOf(parameters.get("label" + i));
      Map option = new HashMap();
      option.put("code", scode);
      option.put("label", label);
      options.add(option);
      i++;
      code = parameters.get("code" + i);
    }

    value = parameters.get("layout");
    if (value != null) layout = String.valueOf(value);
    value = parameters.get("cssCustom");
    cssCustom = (value != null ? String.valueOf(value) : null);
    value = parameters.get("cssFileUrl");
    cssFileUrl = (value != null ? String.valueOf(value) : null);

    return "/pages/workflow/select_menu_form.xhtml";
  }

  @Override
  public Map submit()
  {
    HashMap variables = new HashMap();
    variables.put(varName, selectedCode);
    return variables;
  }

  public void selectOption()
  {
    Map option = (Map)getRequestMap().get("option");
    selectedCode = String.valueOf(option.get("code"));

    instanceBean.forward();
  }
}
