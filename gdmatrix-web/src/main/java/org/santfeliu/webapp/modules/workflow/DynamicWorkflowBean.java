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
public class DynamicWorkflowBean extends WorkflowBean implements Serializable
{
  private String selector;
  private boolean useCache = false;
  private Map data = new HashMap();

  @Inject
  WorkflowInstanceBean instanceBean;

  public org.santfeliu.form.Form getForm()
  {
    try
    {
      // in post back always use cache
      org.santfeliu.form.FormFactory formFactory =
        org.santfeliu.form.FormFactory.getInstance();

      if (!useCache && getFacesContext().getRenderResponse())
      {
        formFactory.clearForm(selector);
      }
      return formFactory.getForm(selector, data);
    }
    catch (Exception ex)
    {
      error(ex);
      return null;
    }
  }

  public Map getData()
  {
    return data;
  }

  public void setData(Map data)
  {
    this.data = data;
  }

  // actions
  @Override
  public String show(Form form)
  {
    data.clear();
    data.putAll(instanceBean.getVariables());
    Properties parameters = form.getParameters();
    selector = (String)parameters.getProperty("selector");
    if (parameters.containsKey("useCache"))
    {
      useCache = (Boolean)parameters.getProperty("useCache");
    }
    return "/pages/workflow/dynamic_form.xhtml";
  }

  @Override
  public Map submit()
  {
    // remove unchanged variables
    Map variables = instanceBean.getVariables();
    for (Object key : variables.keySet())
    {
      String name = key.toString();
      Object oldValue = variables.get(name);
      Object newValue = data.get(name);
      boolean changed = (oldValue == null && newValue != null) ||
        !oldValue.equals(newValue);
      if (!changed) data.remove(name);
    }
    return data;
  }

  public void buttonPressed()
  {
    instanceBean.forward();
  }
}
