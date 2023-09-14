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
import org.santfeliu.faces.FacesBean;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class StartWorkflowInstanceBean extends FacesBean implements Serializable
{
  private String url;
  private Map values = new HashMap();

  @Inject
  WorkflowInstanceBean instanceBean;

  public StartWorkflowInstanceBean()
  {
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public String getUrl()
  {
    return url;
  }

  public void setValues(Map values)
  {
    this.values = values;
  }

  public Map getValues()
  {
    return values;
  }

  // action methods
  public String loadForm()
  {
    try
    {
      MenuItemCursor cursor =
        UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();

      url = (String)cursor.getProperties().get("url");
      if (url == null)
      {
        String doccod = (String)cursor.getProperties().get("doccod");
        if (doccod != null)
        {
          url = getContextURL() + "/documents/" + doccod;
        }
      }
      return url == null ? "blank" : "start_instance";
    }
    catch (Exception ex)
    {
      error(ex.getLocalizedMessage());
    }
    return null;
  }

  public void startInstance()
  {
    try
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      MenuItemCursor cursor = userSessionBean.getMenuModel().getSelectedMenuItem();
      String workflowName = (String)cursor.getProperties().get("workflowName");
      if (workflowName == null)
        throw new Exception("UNDEFINED_WORKFLOWNAME_NODE_PROPERTY");

      instanceBean.createInstance(workflowName, workflowName, true, values);
    }
    catch (Exception ex)
    {
      error(ex.getMessage());
    }
  }
}
