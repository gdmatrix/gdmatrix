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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.news.NewSection;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.model.CheckboxTreeNode;
import org.primefaces.model.TreeNode;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.web.UserSessionBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.modules.news.NewSectionsTabBean.SectionTreeData;

/**
 *
 * @author blanquepa
 */
@Named
@ViewScoped
public class NewSectionsTabBean extends TabBean
{
  private static final String PARAM_SECTION_ID = "sectionId";
  private static final String PARAM_SECTION_DESC  = "sectionDesc";
  
  private SectionTreeNode root;
  private List<TreeNode> selected;
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

  public CheckboxTreeNode getRoot()
  {
    return root;
  }

  public void setRoot(SectionTreeNode root)
  {
    this.root = root;
  }

  public List<TreeNode> getSelected()
  {
    return selected;
  }
  
  public void setSelected(List<TreeNode> selected)
  {
    this.selected = selected;
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
        
        selected = new ArrayList<>(); 

        List<NewSection> newSections = NewsModuleBean.getPort(false).
          findNewSections(getObjectId());

        for (NewSection newSection : newSections)
        {
          SectionTreeNode node = nodeMap.get(newSection.getSectionId());
          if (node != null)
          {
            node.setSelected(true, false, false);
            NewSection section = (NewSection)node.getData();
            section.setNewSectionId(newSection.getNewSectionId());
            section.setSticky(newSection.isSticky());
            selected.add(node);            
          }
        }    
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else
      selected = Collections.EMPTY_LIST;
  }
      
  public void onNodeSelect(NodeSelectEvent event) 
  {  
    try
    {
      SectionTreeNode node = (SectionTreeNode) event.getTreeNode();

      NewSection ns = node.getData();
      ns.setNewId(getObjectId());
      NewSection stored = NewsModuleBean.getPort(false).storeNewSection(ns);
      ns.setNewSectionId(stored.getNewSectionId());
      growl("NEW_SECTIONS_PUBLISHED", new Object[]{node});
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }     
  
  public void onNodeUnselect(NodeUnselectEvent event) 
  {
    try 
    {
      SectionTreeNode node = (SectionTreeNode) event.getTreeNode();
      NewSection ns = node.getData();    
      if (ns.getNewSectionId() != null)    
      {
        NewsModuleBean.getPort(false).removeNewSection(ns.getNewSectionId());    
        growl("NEW_SECTIONS_UNPUBLISHED", new Object[]{node});
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
        while(node.getParent() != null)
        {
          if (!node.isLeaf()) node.setExpanded(true);
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
  
  @Override
  public Serializable saveState()
  {
    return new Object[]{};
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
    
  private void loadSectionsTree() throws Exception
  {
    root = new SectionTreeNode("Root");
        
    List<MenuItemCursor> menuItemList  = newObjectBean.getSectionNodes();
    
    for (MenuItemCursor nodeMic : menuItemList)
    {    
//      if (nodeMic.getDirectProperty(PARAM_SECTION_ID) == null)
//      {
        TreeNode node = createTreeNode(nodeMic);
        if (node != null) 
          node.setSelectable(true);
//      }
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
        List<String> editRoles =
          mic.getMultiValuedProperty(MenuModel.EDIT_ROLES);
        if (UserSessionBean.getCurrentInstance().isUserInRole(editRoles))         
        {
          String value = mic.getDirectProperty(PARAM_SECTION_DESC);
          if (value == null) value = mic.getDirectProperty("description");
          if (value == null) value = mic.getDirectProperty("label");
          if (value == null) value = mid;   

          node = new SectionTreeNode(mid, value, parentNode);
          node.setSelectable(false);
                
          nodeMap.put(mid, node);
        }
      }
    }
      
    return node;    
  }
  
  public class SectionTreeNode extends CheckboxTreeNode<SectionTreeData>
  {  
    public SectionTreeNode(String sectionId, String label, TreeNode parent)
    {
      super(null, parent);
      SectionTreeData data = new SectionTreeData(label);
      data.setSectionId(sectionId);
      data.setNewId(getObjectId());
      setData(data);
    }
        
    public SectionTreeNode(String label)
    {
      super();
      setData(new SectionTreeData(label));
    }
    
    public void setSticky(boolean sticky)
    {
      ((NewSection)getData()).setSticky(sticky);
    }
    
    public boolean isSticky()
    {
      return ((NewSection)getData()).isSticky();
    }
  }
  
  public class SectionTreeData extends NewSection
  {
    String label;

    public SectionTreeData(String label)
    {
      this.label = label;
    }

    @Override
    public String toString()
    {
      return label;
    }       
  }  
      
//  public class Section 
//  {
//    private String newSectionId;
//    private final String sectionId;
//    private String label;
//    private boolean sticky;
//
//    
//    public Section(NewSection newSection)
//    {
//      this(newSection.getNewSectionId(), newSection.getSectionId(), null);
//    }
//    
//    public Section(String sectionId, String label)
//    {
//      this(null, sectionId, label);
//    }
//
//    private Section(String newSectionId, String sectionId, String label)
//    {
//      this.newSectionId = newSectionId;
//      this.sectionId = sectionId;
//      this.label = label;
//    }
//
//    public void setNewSectionId(String newSectionId)
//    {
//      this.newSectionId = newSectionId;
//    }
//
//    public String getNewSectionId()
//    {
//      return newSectionId;
//    }
//
//    public String getSectionId()
//    {
//      return sectionId;
//    }
//    
//    public void setLabel(String label)
//    {
//      this.label = label;
//    }    
//    
//    public String getLabel()
//    {
//      return label;
//    }
//
//    public boolean isSticky()
//    {
//      return sticky;
//    }
//
//    public void setSticky(boolean sticky)
//    {
//      this.sticky = sticky;
//    }
//    
//    @Override
//    public boolean equals(Object obj)
//    {
//      if (this == obj)
//        return true;
//      if (obj == null)
//        return false;
//      if (getClass() != obj.getClass())
//        return false;
//      final Section other = (Section) obj;
//      return Objects.equals(this.sectionId, other.sectionId);
//    }
//
//    @Override
//    public int hashCode()
//    {
//      int hash = 7;
//      hash = 83 * hash + Objects.hashCode(this.sectionId);
//      return hash;
//    }
//
//    @Override
//    public String toString()
//    {
//      return label;
//    }
//    
//    
//  }
  


}
