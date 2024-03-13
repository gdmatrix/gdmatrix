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
package org.santfeliu.webapp.modules.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.matrix.security.Role;
import org.matrix.security.RoleFilter;
import org.matrix.security.SecurityManagerPort;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.santfeliu.security.UserCache;
import org.santfeliu.webapp.TabBean;

/**
 *
 * @author lopezrj-sf
 */
public abstract class RolesTreeTabBean extends TabBean
{
  private TreeNode<NodeInfo> root;  
  private String inputSearchText;
  private Map<String, Role> roleMap = new HashMap();  
  
  public TreeNode getRoot() 
  {
    return root;
  }

  public void setRoot(TreeNode root) 
  {
    this.root = root;
  }

  public String getInputSearchText()
  {
    return inputSearchText;
  }

  public void setInputSearchText(String inputSearchText)
  {
    this.inputSearchText = inputSearchText;
  }

  public String expandAll()
  {    
    setFullTreeState(root, true);
    return null;
  }
  
  public String collapseAll()
  {
    setFullTreeState(root, false);
    return null;
  }  

  protected abstract TreeNode createMainTreeNode();
      
  @Override
  public void load()
  {
    try
    {
      root = new DefaultTreeNode("", null);      
      if (!isNew())
      {
        TreeNode mainNode = createMainTreeNode();
        Set<String> loadedNodes = new HashSet<>();
        if (getRoleId(mainNode) != null)
        {
          loadedNodes.add(getRoleId(mainNode));
        }
        loadNodes(mainNode, loadedNodes);
        fillRolesInfo();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public boolean isTreeRender()
  {
    return getMainTreeNode().getChildCount() > 0;
  }
  
  private void setFullTreeState(TreeNode<NodeInfo> node, boolean expanded)
  {
    node.setExpanded(expanded);
    for (TreeNode child : node.getChildren())
    {
      setFullTreeState(child, expanded);
    }
  }  
  
  private void loadNodes(TreeNode node, Set<String> loadedRoles)
  {
    List<String> roles = getChildrenRoles(node);
    for (String role : roles)
    {
      RoleInfo roleInfo = new RoleInfo(role);
      roleInfo.setCycle(loadedRoles.contains(role));
      TreeNode n = new DefaultTreeNode("Role", roleInfo, node);
      if (!roleInfo.isCycle())
      {
        Set<String> auxLoadedRoles = new HashSet<>();
        auxLoadedRoles.addAll(loadedRoles);
        auxLoadedRoles.add(role);
        loadNodes(n, auxLoadedRoles);
      }
    }
  }
    
  private String getRoleId(TreeNode node)
  {
    if (node.getData() instanceof RoleInfo)
    {
      return ((RoleInfo)node.getData()).getRoleId();
    }
    return null;
  }  
  
  private List<String> getChildrenRoles(TreeNode node)
  {    
    List<String> auxList = ((NodeInfo)node.getData()).getChildrenRoles();
    Collections.sort(auxList);
    return auxList;
  }

  private void fillRolesInfo()
  {
    try
    {
      roleMap.clear();
      SecurityManagerPort port = SecurityModuleBean.getPort(true);
      RoleFilter filter = new RoleFilter();
      filter.getRoleId().addAll(getAllTreeRoles(root));
      List<Role> roleList = port.findRoles(filter);
      for (Role role : roleList)
      {
        roleMap.put(role.getRoleId(), role);
      }
    }
    catch (Exception ex)
    {      
    }
  }

  private Set<String> getAllTreeRoles(TreeNode node)
  {
    Set<String> roleSet = new HashSet();
    if (node.getData() instanceof RoleInfo)
    {
      RoleInfo roleInfo = (RoleInfo)node.getData();
      roleSet.add(roleInfo.getRoleId());
    }
    for (Object child : node.getChildren())
    {
      roleSet.addAll(getAllTreeRoles((TreeNode)child));
    }
    return roleSet;
  }
  
  private TreeNode getMainTreeNode()
  {
    return (TreeNode)getRoot().getChildren().get(0);
  }
  
  protected abstract class NodeInfo implements Serializable
  {
    protected String nodeId;
    
    public NodeInfo(String nodeId)
    {
      this.nodeId = nodeId;
    }

    public String getNodeId() 
    {
      return nodeId;
    }

    protected abstract List<String> getChildrenRoles();
  }
  
  public class RoleInfo extends NodeInfo
  {
    private boolean cycle = false;
    private boolean renderLink = true;

    public RoleInfo(String roleId) 
    {
      super(roleId);            
    }

    public RoleInfo(String roleId, boolean renderLink) 
    {
      this(roleId);
      this.renderLink = renderLink;
    }

    public String getRoleId() 
    {
      return getNodeId();
    }
    
    public boolean isCycle() 
    {
      return cycle;
    }

    public void setCycle(boolean cycle) 
    {
      this.cycle = cycle;
    }        

    public boolean isRenderLink() 
    {
      return renderLink;
    }

    public void setRenderLink(boolean renderLink) 
    {
      this.renderLink = renderLink;
    }

    public String getName()
    {
      return roleMap.get(nodeId).getName();
    }

    public String getRoleTypeId()
    {
      return roleMap.get(nodeId).getRoleTypeId();
    }

    @Override
    protected List<String> getChildrenRoles() 
    {
      List<String> auxList = new ArrayList<>();
      auxList.addAll(UserCache.getRoleInRoles(getRoleId(), false));
      return auxList;
    }
  }
  
}
