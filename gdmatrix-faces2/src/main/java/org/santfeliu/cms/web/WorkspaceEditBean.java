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
package org.santfeliu.cms.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import org.matrix.cms.Workspace;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author unknown
 */
public class WorkspaceEditBean extends FacesBean implements Serializable
{
  private Workspace workspace;

  public WorkspaceEditBean()
  {
    try
    {
      String workspaceId =
        UserSessionBean.getCurrentInstance().getWorkspaceId();
      workspace = CMSConfigBean.getPort().loadWorkspace(workspaceId);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public Workspace getWorkspace()
  {
    return workspace;
  }

  public void setWorkspace(Workspace workspace)
  {
    this.workspace = workspace;
  }

  public SelectItem[] getRefWorkspaceItems()
  {
    List<SelectItem> auxList = new ArrayList<SelectItem>();
    SelectItem noReferenceItem = new SelectItem();
    noReferenceItem.setLabel(getLocalizedText("noReferenceWorkspace"));
    noReferenceItem.setValue("");
    auxList.add(noReferenceItem);
    SelectItem[] workspaceItems = 
      CMSToolbarBean.getCurrentInstance().getWorkspaceItems();
    for (SelectItem selectItem : workspaceItems)
    {
      if (!((String)selectItem.getValue()).equals(workspace.getWorkspaceId()))
      {
        auxList.add(selectItem);
      }
    }
    SelectItem[] result = new SelectItem[auxList.size()];
    for (int i = 0; i < result.length; i++)
    {
      result[i] = auxList.get(i);
    }
    return result;
  }

  public String save()
  {
    try
    {
      if ("".equals(workspace.getRefWorkspaceId()))
      {
        workspace.setRefWorkspaceId(null);
      }
      CMSConfigBean.getPort().storeWorkspace(workspace);    
      return "node_edit";      
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String cancel()
  {
    return "node_edit";
  }

  private String getLocalizedText(String text)
  {
    String result = null;
    try
    {
      Locale locale =
        FacesContext.getCurrentInstance().getViewRoot().getLocale();
      result = loadResourceBundle(locale).getString(text);
    }
    catch (MissingResourceException ex)
    {
      result = "{" + text + "}";
    }
    return result;
  }
  
  private ResourceBundle loadResourceBundle(Locale locale)
  {
    return ResourceBundle.getBundle(
      "org.santfeliu.cms.web.resources.CMSBundle", locale);
  }

}
