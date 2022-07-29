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
package org.matrix.pf.cms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.matrix.cms.Node;
import org.matrix.cms.Property;
import org.santfeliu.cms.CMSCache;
import org.santfeliu.cms.CNode;
import org.santfeliu.cms.web.CMSConfigBean;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.web.ApplicationBean;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 */
public class CMSConfigHelper implements Serializable
{
  private final Map<String, Property> properties = new HashMap<>();
  
  public void setMultivaluedProperty(String key, List<String> values)
  {
    Property prop = new Property();
    prop.setName(key);
    prop.getValue().clear();
    if (values != null)
      prop.getValue().addAll(values);
    properties.put(key, prop);
  }  
  
  public void setProperty(String key, String value)
  {
    Property prop = new Property();
    prop.setName(key);
    prop.getValue().clear();
    if (value != null && !value.trim().isEmpty())
      prop.getValue().add(value);
    properties.put(key, prop);
  }  
  
  public void saveProperties()
    throws Exception
  {
    List<Property> propertyList = new ArrayList(properties.values());    
    CNode cNode = getSelectedCNode();
    if (cNode != null)
    {
      String nodeName = cNode.getNode().getName();
      saveProperties(nodeName, propertyList);
    }    
  }
  
  public void saveProperties(String nodeName, List<Property> propertyList) 
    throws Exception
  {
    CNode selectedCNode = getSelectedCNode();
    Node selectedNode = selectedCNode.getNode();
    MergedNode mNode = new MergedNode(selectedNode);
    selectedNode = mNode.mergeProperties(propertyList);
    selectedNode.setName(nodeName);      
    CMSConfigBean.getPort().storeNode(selectedNode);
    //updateCache();    
  }
  
  //SHARED METHODS
  
  public CNode getSelectedCNode()
  {
    CMSCache cmsCache = ApplicationBean.getCurrentInstance().getCmsCache();
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String workspaceId = userSessionBean.getWorkspaceId();
    String nodeId = userSessionBean.getSelectedMid();
    CNode selectedCNode =
      cmsCache.getWorkspace(workspaceId).getNode(nodeId);
    return selectedCNode;
  }  
  
  public void updateCache()
  {
    String workspaceId =
      UserSessionBean.getCurrentInstance().getWorkspaceId();
    updateCache(workspaceId);
  }
  
  public void updateCache(String workspaceId)
  {
    CMSCache cmsCache = ApplicationBean.getCurrentInstance().getCmsCache();
    cmsCache.getWorkspace(workspaceId).purge();
  }

  public boolean nodeIsVisible(String nodeId)
  {
    try
    {
      getMenuModel().getMenuItemByMid(nodeId);
      return true;
    }
    catch (Exception ex)
    {
      return false;
    }
  }

  public String getNewVisibleNodeId()
  {
    MenuItemCursor newCursor = getCursor().getPrevious();
    if (newCursor.getMid() == null) //no previous visible node
    {
      newCursor = getCursor().getNext();
      if (newCursor.getMid() == null) //no next visible node -> go to parent
      {
        newCursor = getCursor().getParent();
        if (newCursor.getMid() == null)
        {
          String mid = UserSessionBean.getCurrentInstance().getSelectedMid();          
          return getRootNodeId(mid);          
        }
      }
    }
    return newCursor.getMid();
  }
  
  public String getNodeLabel(MenuItemCursor mic)
  {
    String description = mic.getDirectProperty("description");
    String label = mic.getDirectProperty("label");
    return (description != null ? 
      description : (label != null ? label : mic.getMid()));
  }

  private MenuItemCursor getCursor()
  {
    return UserSessionBean.getCurrentInstance().getMenuModel().
      getSelectedMenuItem();
  }

  private MenuModel getMenuModel()
  {
    return UserSessionBean.getCurrentInstance().getMenuModel();
  }

  private String getRootNodeId(String mid)
  {
    CMSCache cmsCache = ApplicationBean.getCurrentInstance().getCmsCache();
    String workspaceId = UserSessionBean.getCurrentInstance().getWorkspaceId();
    CNode cNode = cmsCache.getWorkspace(workspaceId).getNode(mid);
    return cNode.getRoot().getNodeId();
  }  
  
  //TODO: Keep properties sorted as original node
  public class MergedNode
  {
    private final Node node;
    private final Map<String, Property> propMap = new TreeMap<>();
    
    public MergedNode(Node node)
    {
      this.node = node;
      for (Property prop : node.getProperty())
      {
        propMap.put(prop.getName(), prop);
      }
    }
        
    public Node mergeProperties(List<Property> propertyList)
    {
      for (Property prop : propertyList)
      {
        String propName = prop.getName();
        propMap.put(propName, prop);
      }
      node.getProperty().clear();
      node.getProperty().addAll(propMap.values());
      return node;
    }
        
    private boolean contains(String propName)
    {
      return propMap.get(propName) != null;
    }    
    
  }

}
