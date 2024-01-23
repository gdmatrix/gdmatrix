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
package org.santfeliu.webapp.modules.news;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.news.NewSection;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.web.UserSessionBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import static org.santfeliu.webapp.modules.news.NewObjectBean.NEWSECTION_PROPERTY;
import org.santfeliu.webapp.modules.news.NewSectionsTabBean.SectionTreeData;

/**
 *
 * @author blanquepa
 */
@Named
@ViewScoped
public class NewSectionsTabBean extends TabBean
{
  private static final String SELECTABLE = "selectable";
  private static final String UNSELECTABLE = "unselectable";
    
  private static final String SECTIONID_PROPERTY = "sectionId";  

  private SectionTreeNode root;
  private Map<String, SectionTreeNode> nodeMap;
  private SectionTreeNode ctxNode;

  @Inject
  NewObjectBean newObjectBean;


  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
  }

  public TreeNode getCtxNode()
  {
    return ctxNode;
  }

  public void setCtxNode(SectionTreeNode ctxNode)
  {
    this.ctxNode = ctxNode;
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return newObjectBean;
  }

  public TreeNode getRoot()
  {
    return root;
  }

  public void setRoot(SectionTreeNode root)
  {
    this.root = root;
  }

  @Override
  public void load()
  {
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        nodeMap = new HashMap<>();

        loadSectionsTree();

        List<NewSection> newSections = NewsModuleBean.getPort(false).
          findNewSections(getObjectId());

        for (NewSection newSection : newSections)
        {
          SectionTreeNode node = nodeMap.get(newSection.getSectionId());
          if (node != null)
          {
            node.setType(SELECTABLE);      
            SectionTreeData std = (SectionTreeData) node.getData();
            std.setNewSectionId(newSection.getNewSectionId());
            std.setSticky(newSection.isSticky());
            std.setChecked(true);
            expandParents(node);
          }
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
  }
  
  public void onNodeSelect(NodeSelectEvent event)
  {
    try
    {
      SectionTreeNode node = (SectionTreeNode) event.getTreeNode();
      if (SELECTABLE.equals(node.getType()))
      {
        SectionTreeData std = node.getData();

        std.setNewId(getObjectId());

        if (!std.isChecked())
        {
          NewSection ns = NewsModuleBean.getPort(false).storeNewSection(std);
          if (std.getNewSectionId() == null)
            std.setNewSectionId(ns.getNewSectionId());
          std.setChecked(true);
          growl("NEW_SECTIONS_PUBLISHED", new Object[]{node});
        }
        else
        {
          NewsModuleBean.getPort(false).removeNewSection(std.getNewSectionId());
          std.setChecked(false);
          std.setSticky(false);
          growl("NEW_SECTIONS_UNPUBLISHED", new Object[]{node});
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void onContextMenu(NodeSelectEvent event)
  {
    try
    {
      ctxNode = (SectionTreeNode) event.getTreeNode();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public void switchSticky()
  {
    if (ctxNode != null)
    {
      try
      {
        NewSection newSection = ctxNode.getData();
        newSection.setSticky(!newSection.isSticky());
        NewsModuleBean.getPort(false).storeNewSection(newSection);
        TreeNode node = ctxNode;
        while (node.getParent() != null)
        {
          if (!node.isLeaf())
            node.setExpanded(true);
          node = node.getParent();
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
  }

  public void expandAll()
  {
    nodeMap.values().stream().forEach(node -> node.setExpanded(true));
  }

  public void collapseAll()
  {
    nodeMap.values().stream().forEach(node -> node.setExpanded(false));
  }
  
  public void expandChecked()
  {
    collapseAll();
    
    for (SectionTreeNode node : nodeMap.values())
    {
      SectionTreeData data = (SectionTreeData) node.getData();
      if (data.isChecked()) 
        expandParents(node);      
    }
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]
    {
    };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[]) state;
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private void loadSectionsTree() throws Exception
  {
    root = new SectionTreeNode("Root", false);

    List<MenuItemCursor> menuItemList = newObjectBean.getSectionNodes();

    for (MenuItemCursor nodeMic : menuItemList)
    {
      //Don't use nodes refering other sections (containing 'sectionId')
      String sectionId = nodeMic.getDirectProperty(SECTIONID_PROPERTY);
      if (sectionId == null)
      {
        List<String> editRoles = 
          nodeMic.getMultiValuedProperty(MenuModel.EDIT_ROLES);
        
        if (UserSessionBean.getCurrentInstance().isUserInRole(editRoles))
        {
          TreeNode node = createTreeNode(nodeMic);
          node.setType(SELECTABLE);        
        }
      }
    }
  }

  private TreeNode createTreeNode(MenuItemCursor mic)
  {
    SectionTreeNode node = null;

    if (mic != null)
    {
      MenuItemCursor parentMic = mic.getParent();
      TreeNode parentNode = root;
      if (!parentMic.isRoot())
        parentNode = createTreeNode(parentMic);

      String mid = mic.getMid();

      node = nodeMap.get(mid);
      if (node == null && parentNode != null)
      {
        String section = mic.getDirectProperty(NEWSECTION_PROPERTY);
        if (section == null)
          section = mic.getDirectProperty("description");
        if (section == null)
          section = mic.getDirectProperty("label");
        if (section == null)
          section = mid;

        node = new SectionTreeNode(mid, section, false, parentNode);
        node.setType(UNSELECTABLE);
      
        nodeMap.put(mid, node);
      }
    }

    return node;
  }
  
  private void expandParents(TreeNode node)
  {
    TreeNode parent = node.getParent();
    while(parent != null)
    {
      parent.setExpanded(true);
      parent = parent.getParent();
    }    
  }  

  public class SectionTreeNode extends DefaultTreeNode<SectionTreeData>
  {

    public SectionTreeNode(String sectionId, String label, boolean checked, 
      TreeNode parent)
    {
      super(null, parent);
      SectionTreeData data = new SectionTreeData(label, checked);
      data.setSectionId(sectionId);
      data.setNewId(getObjectId());
      setData(data);
    }

    public SectionTreeNode(String label, boolean checked)
    {
      super();
      setData(new SectionTreeData(label, checked));
    }

    public void setSticky(boolean sticky)
    {
      ((NewSection) getData()).setSticky(sticky);
    }

    public boolean isSticky()
    {
      return ((NewSection) getData()).isSticky();
    }
    
  }

  public class SectionTreeData extends NewSection
  {
    boolean checked;
    String label;

    public SectionTreeData(String label, boolean checked)
    {
      this.checked = checked;
      this.label = label;
    }

    public boolean isChecked()
    {
      return checked;
    }

    public void setChecked(boolean checked)
    {
      this.checked = checked;
    }

    @Override
    public String toString()
    {
      return label;
    }
  }

}
