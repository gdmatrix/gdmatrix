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
import java.util.List;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import org.matrix.cms.CMSManagerPort;
import org.matrix.cms.CMSManagerService;
import org.matrix.cms.Node;
import org.matrix.cms.Workspace;
import org.matrix.cms.WorkspaceFilter;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.cms.CMSCache;
import org.santfeliu.cms.CWorkspace;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.misc.widget.WidgetCache;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.ApplicationBean;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author unknown
 */
public class CMSToolbarBean extends FacesBean implements Serializable
{
  private SelectItem[] workspaceItems;

  public static CMSToolbarBean getCurrentInstance()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Application application = context.getApplication();
    return (CMSToolbarBean)application.getVariableResolver().
      resolveVariable(context, "cmsToolbarBean");
  }

  public SelectItem[] getWorkspaceItems()
  {
    if (workspaceItems == null)
    {
      try
      {
        CMSManagerPort port = getCMSManagerPort();
        WorkspaceFilter filter = new WorkspaceFilter();
        List<Workspace> workspaceList = port.findWorkspaces(filter);
        workspaceItems = new SelectItem[workspaceList.size()];
        if (workspaceList.size() > 0)
        {
          int i = 0;
          for (Workspace w : workspaceList)
          {
            SelectItem item = new SelectItem();
            item.setLabel(w.getWorkspaceId() + " (" + w.getName() + ")");
            item.setValue(w.getWorkspaceId());
            workspaceItems[i++] = item;
            //Now we update the workspaces in the cache
            CMSCache cmsCache =
              ApplicationBean.getCurrentInstance().getCmsCache();
            if (cmsCache.containsWorkspace(w.getWorkspaceId()))
            {
              CWorkspace cWorkspace = cmsCache.getWorkspace(w.getWorkspaceId());
              cWorkspace.getWorkspace().setName(w.getName());
              cWorkspace.getWorkspace().setDescription(w.getDescription());
              cWorkspace.getWorkspace().setRefWorkspaceId(
                w.getRefWorkspaceId());
            }
          }
        }
      }
      catch (Exception ex)
      {
        workspaceItems = new SelectItem[0];
      }
    }
    return workspaceItems;
  }

  public void setWorkspaceItems(SelectItem[] workspaceItems)
  {
    this.workspaceItems = workspaceItems;
  }

  public boolean isRenderWorkspaceActions()
  {    
    return UserSessionBean.getCurrentInstance().isCmsAdministrator();
  }

  public String editWorkspace()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    userSessionBean.setViewMode(UserSessionBean.EDIT_VIEW);
    return "workspace_edit";
  }

  public String createWorkspace()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    try
    {
      this.workspaceItems = null; //reload combobox
      CMSManagerPort port = getCMSManagerPort();
      Workspace newWorkspace = new Workspace();
      newWorkspace.setName("New workspace");
      newWorkspace.setDescription("New workspace");
      newWorkspace.setRefWorkspaceId(null);
      newWorkspace = port.storeWorkspace(newWorkspace);
      Node rootNode = new Node();
      rootNode.setParentNodeId(null);
      rootNode.setIndex(1);
      rootNode.setWorkspaceId(newWorkspace.getWorkspaceId());
      rootNode = port.storeNode(rootNode);      
      userSessionBean.setWorkspaceId(newWorkspace.getWorkspaceId());
      userSessionBean.setSelectedMid(rootNode.getNodeId());
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return userSessionBean.showEditView();
  }

  public String copyWorkspace()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    try
    {
      this.workspaceItems = null; //reload combobox
      CMSManagerPort port = getCMSManagerPort();      
      Workspace newWorkspace = port.copyWorkspace(userSessionBean.getWorkspaceId(), null);
      userSessionBean.setWorkspaceId(newWorkspace.getWorkspaceId());
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return userSessionBean.showEditView();
  }

  public String removeWorkspace()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    try
    {
      String defaultWorkspaceId =
        MatrixConfig.getProperty("org.santfeliu.web.defaultWorkspaceId");
      this.workspaceItems = null; //reload combobox
      CMSManagerPort port = getCMSManagerPort();
      port.removeWorkspace(userSessionBean.getWorkspaceId());
      CMSCache cmsCache =
        ApplicationBean.getCurrentInstance().getCmsCache();
      cmsCache.removeWorkspace(userSessionBean.getWorkspaceId());
      userSessionBean.setWorkspaceId(defaultWorkspaceId);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return userSessionBean.showEditView();
  }

  public String updateSnapshot()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    userSessionBean.getMenuModel().getCWorkspace().purge();
    userSessionBean.clearWSCallCaches();
    WidgetCache.getInstance().clear(); //reload cached widgets     
    this.workspaceItems = null; //reload combobox
    if (userSessionBean.isRenderViewSelected())
    {
      userSessionBean.executeSelectedMenuItem();
    }
    return null;
  }

  public String loadWorkspace()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    if (userSessionBean.isRenderViewSelected())
    {
      userSessionBean.executeSelectedMenuItem();
    }
    return null;
  }

  public String clearSnapshot()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    userSessionBean.getMenuModel().getCWorkspace().clear();
    userSessionBean.clearWSCallCaches();    
    WidgetCache.getInstance().clear(); //reload cached widgets
    if (userSessionBean.isRenderViewSelected())
    {
      userSessionBean.executeSelectedMenuItem();
    }
    return null;
  }

  private CMSManagerPort getCMSManagerPort() throws Exception
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint = wsDirectory.getEndpoint(CMSManagerService.class);
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return endpoint.getPort(CMSManagerPort.class,
      userSessionBean.getUsername(),
      userSessionBean.getPassword());
  }

}
