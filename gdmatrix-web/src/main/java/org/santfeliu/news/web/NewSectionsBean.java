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
package org.santfeliu.news.web;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.myfaces.custom.tree2.HtmlTree;
import org.apache.myfaces.custom.tree2.TreeModelBase;
import org.matrix.cms.CMSManagerPort;
import org.matrix.cms.Node;
import org.matrix.cms.NodeFilter;
import org.matrix.cms.Property;
import org.matrix.dic.DictionaryConstants;
import org.matrix.news.NewSection;
import org.santfeliu.cms.web.CMSConfigBean;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.TypifiedPageBean;

/**
 *
 * @author unknown
 */
public class NewSectionsBean extends TypifiedPageBean
{
  private static final String PARAM_SECTION_ID = "sectionId";
  private static final String PARAM_SECTION_DESC = "sectionDesc";
  
  private TreeModelBase treeModel;
  private transient HtmlTree tree;
  
  public NewSectionsBean()
  {
    super(DictionaryConstants.NEW_SECTION, "WEBMASTER");
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
    return "new_sections";
  }
  
  public String load()
  {
    try
    {
      NewSectionTreeNode rootNode = new NewSectionTreeNode();
      rootNode.setDescription("News Modules");
      rootNode.setType("NoModule");
      rootNode.setIdentifier("-1");
      treeModel = new TreeModelBase(rootNode);
      loadSections();
      tree = new HtmlTree();
      tree.setModel(treeModel);
      if (!isNew())
      {
        String newId = getObjectId();
        if (newId == null)
        {
          getObjectBean().removed();
        }
        else
        {
          List<NewSection> newSections = NewsConfigBean.getPort().
            findNewSectionsFromCache(newId);
          for (NewSection newSection : newSections)
          {
            markSections(rootNode, newSection, "0");
          }          
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return show();
  }

  public String store() //TODO Optimize
  {
    try
    {
      Set currentSectionsSet = new HashSet();
      List<NewSection> currentNewSectionList = 
        NewsConfigBean.getPort().findNewSectionsFromCache(getObjectId());
      for (NewSection ns : currentNewSectionList) 
        currentSectionsSet.add(ns.getSectionId());
      storeSectionsInTree((NewSectionTreeNode)treeModel.getNodeById("-1"), 
        currentSectionsSet);      
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return load();
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

  public void showSection() throws Exception
  {
    NewSectionTreeNode node = (NewSectionTreeNode)getFacesContext().
      getExternalContext().getRequestMap().get("node");
    getControllerBean().setCurrentMid(node.getMid());
  }
  
  public boolean isEnabledNode()
  {
    NewSectionTreeNode node = (NewSectionTreeNode)getFacesContext().
      getExternalContext().getRequestMap().get("node");
    return UserSessionBean.getCurrentInstance().isUserInRole(
      node.getUpdateRoles());
  }
  
  public String switchSticky()
  {
    NewSectionTreeNode node = (NewSectionTreeNode)getFacesContext().
      getExternalContext().getRequestMap().get("node");
    node.setSticky(!node.isSticky());
    return null;
  }
  
  private void loadSections() throws Exception
  {
    MenuModel menuModel = UserSessionBean.getCurrentInstance().getMenuModel();
    String workspaceId = UserSessionBean.getCurrentInstance().getWorkspaceId();
    CMSManagerPort port = CMSConfigBean.getPort();
    NodeFilter nodeFilter = new NodeFilter();
    Property property = new Property();
    property.setName("action");
    property.getValue().add("%newSearchBySection%");
    nodeFilter.getProperty().add(property);
    nodeFilter.getWorkspaceId().add(workspaceId);
    List<Node> nodeList = port.findNodes(nodeFilter);
    List<String> nodeIdList = new ArrayList<String>();
    for (Node node : nodeList)
    {
      nodeIdList.add(node.getNodeId());
    }
    List<MenuItemCursor> menuItemList = menuModel.getMenuItemsByMid(nodeIdList);
    for (MenuItemCursor nodeMic : menuItemList)
    {    
      Map directProperties = (Map)nodeMic.getDirectProperties();
      if (directProperties.get(PARAM_SECTION_ID) == null)
      {
        MenuItemCursor[] path = nodeMic.getCursorPath();
        List<MenuItemCursor> pathList = new ArrayList<MenuItemCursor>();
        for (MenuItemCursor mic : path) pathList.add(mic);
        NewSectionTreeNode nodeRoot =
          (NewSectionTreeNode)treeModel.getNodeById("-1");
        appendPathInTree(pathList, nodeRoot, nodeMic.getMid());
      }
    }
  }
  
  private boolean appendPathInTree(List<MenuItemCursor> path, 
    NewSectionTreeNode node, String nodeId)  
  {
    boolean createNode = true;
    if ((path != null) && (path.size() > 0))
    {
      NewSectionTreeNode foundChild = null;
      MenuItemCursor mic = path.get(0);
      Map micProperties = (Map)mic.getDirectProperties();
      Object value = micProperties.get(PARAM_SECTION_DESC);
      if (value == null) value = micProperties.get("description");
      if (value == null) value = micProperties.get("label");
      if (value == null) value = mic.getMid();
      int i = 0;
      for (Object child : node.getChildren())
      {
        if (((NewSectionTreeNode)child).getDescription().
          equalsIgnoreCase((String)value))
        {
          foundChild = (NewSectionTreeNode)child;
        }
        i++;
      }
      if (foundChild == null) // not found -> Node must be created
      {
        NewSectionTreeNode t = new NewSectionTreeNode();
        t.setDescription((String)value);
        t.setType("NoModule");
        if (path.size() == 1) // Node is a module
        {
          List<String> editRoles =
            mic.getMultiValuedProperty(MenuModel.EDIT_ROLES);
          createNode = 
            UserSessionBean.getCurrentInstance().isUserInRole(editRoles);                  
          if (createNode)
          {
            t.setIdentifier(nodeId);
            t.setType("Module");
            t.setMid(mic.getMid());
            if (editRoles != null)
            {
              t.setUpdateRoles(new HashSet<String>(editRoles));
            }            
          }
        }
        else // Node is an intermediate node
        {
          t.setIdentifier(path.get(0).getMid());
          path.remove(0);
          createNode = appendPathInTree(path, t, nodeId);
        }
        if (createNode) node.getChildren().add(t);
      }
      else //found
      {
        if (path.size() == 1)
        {
          foundChild.setType("Module");
          //New code
          foundChild.setMid(mic.getMid());
          List<String> editRoles =
            mic.getMultiValuedProperty(MenuModel.EDIT_ROLES);
          if (editRoles != null)
          {
            foundChild.setUpdateRoles(new HashSet<String>(editRoles));
          }
          //End
        }
        else
        {
          path.remove(0);
          appendPathInTree(path, foundChild, nodeId);
        }
      }
    }
    return createNode;
  }
    
  private void markSections(NewSectionTreeNode node, NewSection newSection, 
    String nodePath)
  {
    if (node.getIdentifier().equalsIgnoreCase(newSection.getSectionId()))
    {
      node.setChecked(true);
      node.setSticky(newSection.isSticky());
      tree.expandPath(treeModel.getPathInformation(nodePath));
      node.setNewSection(newSection);
    }
    int i = 0;
    for (Object child : node.getChildren())
    {
      String extendedNodePath = nodePath + String.valueOf(":" + i);
      markSections((NewSectionTreeNode)child, newSection, extendedNodePath);
      i++;
    }    
  }
  
  private void storeSectionsInTree(NewSectionTreeNode node, 
    Set currentSectionsSet) throws Exception
  {
    try
    {
      if (node.isChecked()) // selected item
      {
        NewSection ns;
        if (currentSectionsSet.contains(node.getIdentifier()))
          // It is in the DB
        {
          // Update prio
          String newSectionId = node.getNewSection().getNewSectionId();
          ns = NewsConfigBean.getPort().loadNewSectionFromCache(newSectionId);
          ns.setNewSectionId(newSectionId);
        }
        else // New Publication
        {
          ns = new NewSection();
          ns.setNewId(getObjectId());
          ns.setSectionId(node.getIdentifier());
        }        
        ns.setSticky(node.isSticky());
        NewsConfigBean.getPort().storeNewSection(ns);
      }
      else // not selected item
      {
        if (currentSectionsSet.contains(node.getIdentifier())) // Delete
        {
          String newSectionId = node.getNewSection().getNewSectionId();
          NewsConfigBean.getPort().removeNewSection(newSectionId);
        }
      }
      for (Object child : node.getChildren())
      {
        storeSectionsInTree((NewSectionTreeNode)child, currentSectionsSet);
      }
    }
    catch (Exception ex)
    {
      throw new Exception("ERROR_IN_SECTIONS_TREE");
    }
  }    
}
