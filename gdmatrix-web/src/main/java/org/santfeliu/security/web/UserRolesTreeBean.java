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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.myfaces.custom.tree2.HtmlTree;
import org.apache.myfaces.custom.tree2.TreeModelBase;
import org.apache.myfaces.custom.tree2.TreeNodeChecked;
import org.matrix.security.SecurityConstants;
import org.santfeliu.security.UserCache;
import org.santfeliu.web.obj.PageBean;

/**
 *
 * @author lopezrj
 */
public class UserRolesTreeBean extends PageBean
{
  private static final String ROOT_NODE_ID = "-1";
  
  private TreeModelBase treeModel;
  private transient HtmlTree tree;
  
  public UserRolesTreeBean()
  {    
    load();
  }

  public void setTreeModel(TreeModelBase treeModel)
  {
    this.treeModel = treeModel;
  }

  public TreeModelBase getTreeModel()
  {
    return treeModel;
  }

  public void setTree(HtmlTree tree)
  {
    this.tree = tree;
  }

  public HtmlTree getTree()
  {
    return tree;
  }
  
  public String show()
  {
    return "user_roles_tree";
  }
  
  public String load()
  {
    try
    {
      UserRolesTreeNode rootNode = new UserRolesTreeNode();
      rootNode.setType("User");
      rootNode.setIdentifier(ROOT_NODE_ID);      
      if (!isNew())
      {
        String userId = getObjectId();        
        rootNode.setDescription(userId);
        loadRoles(rootNode);
      }
      else
      {
        rootNode.setDescription("");
      }
      treeModel = new TreeModelBase(rootNode);
      tree = new HtmlTree();
      tree.setModel(treeModel);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return show();
  }

  public String expandAll()
  {
    tree.expandAll();
    return null;
  }
  
  public String collapseAll()
  {
    tree.collapseAll();
    return null;
  }
  
  private void loadRoles(UserRolesTreeNode nodeRoot) throws Exception
  {
    addNodes(nodeRoot, new HashSet<String>());
  }
  
  private void addNodes(UserRolesTreeNode node, Set<String> addedRoles)
  {
    List<String> roles = node.getRoles();    
    for (String role : roles)
    {
      if (!role.startsWith(SecurityConstants.SELF_ROLE_PREFIX))
      {
        UserRolesTreeNode t = new UserRolesTreeNode();
        t.setDescription(role);
        t.setType("Role");
        t.setIdentifier(role);
        t.setCycle(addedRoles.contains(role));
        node.getChildren().add(t);
        if (!t.isCycle())
        {
          Set<String> auxAddedRoles = new HashSet<String>();
          auxAddedRoles.addAll(addedRoles);
          auxAddedRoles.add(role);
          addNodes(t, auxAddedRoles);
        }        
      }
    }
  }
  
  public class UserRolesTreeNode extends TreeNodeChecked
  {
    private boolean cycle;

    public String getRoleId()
    {
      return getIdentifier();
    }

    public String getUserId()
    {
      return getObjectId();
    }
    
    public List<String> getRoles()
    {
      List<String> auxList = new ArrayList<String>();      
      if ("User".equals(getType()))
      {
        auxList.addAll(UserCache.getUserInRoles(getUserId(), false));
      }
      else //Role
      {
        auxList.addAll(UserCache.getRoleInRoles(getRoleId(), false));
      }
      Collections.sort(auxList);
      return auxList;
    }    
    
    public boolean isCycle() 
    {
      return cycle;
    }

    public void setCycle(boolean cycle) 
    {
      this.cycle = cycle;
    }        
  }

}
