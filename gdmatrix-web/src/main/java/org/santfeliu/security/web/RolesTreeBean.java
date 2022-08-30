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
package org.santfeliu.security.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.santfeliu.security.UserCache;
import org.santfeliu.web.obj.PageBean;

/**
 *
 * @author lopezrj
 */
public abstract class RolesTreeBean extends PageBean
{
  private TreeNode<NodeInfo> root;
  
  private String inputSearchText;
  private Set<String> foundRoles;
  
  public RolesTreeBean()
  {    
    load();
  }

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
  
  public Set<String> getFoundRoles()
  {
    if (foundRoles == null)
    {
      foundRoles = new HashSet<>();
    }
    return foundRoles;
  }

  public void setFoundRoles(Set<String> foundRoles)
  {
    this.foundRoles = foundRoles;
  }  
  
  @Override
  public abstract String show();
  
  public String search()
  {
    try
    {
      //Search for roles
      collapseAll();
      getFoundRoles().clear();
      if (inputSearchText != null && !inputSearchText.trim().isEmpty())
      {
        searchNodes(root.getChildren().get(0), 
          inputSearchText.trim().toLowerCase());
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
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

  protected abstract TreeNode getMainTreeNode();
      
  private void load()
  {
    try
    {
      root = new DefaultTreeNode("", null);      
      if (!isNew())
      {
        TreeNode mainNode = getMainTreeNode();
        Set<String> loadedNodes = new HashSet<>();
        if (getRoleId(mainNode) != null)
        {
          loadedNodes.add(getRoleId(mainNode));
        }
        loadNodes(mainNode, loadedNodes);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  private void searchNodes(TreeNode<NodeInfo> node, String inputSearchString)
  {
    String roleId = getRoleId(node);
    if (roleId != null && roleId.toLowerCase().contains(inputSearchString))
    {
      openAncestors(node);
      getFoundRoles().add(roleId);
    }
    
    for (TreeNode child : node.getChildren())
    {
      searchNodes(child, inputSearchString);
    }    
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
  
  private void openAncestors(TreeNode node) 
  {
    TreeNode auxNode = node.getParent();
    while (auxNode != null)
    {
      auxNode.setExpanded(true);
      auxNode = auxNode.getParent();
    }
  }  
  
  protected abstract class NodeInfo implements Serializable
  {
    private final String nodeId;
    
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

    public boolean isFound()
    {
      return getFoundRoles().contains(getRoleId());      
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
