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
package org.santfeliu.webapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.santfeliu.dic.Type;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.webapp.NavigatorBean.BaseTypeInfo;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author realor
 */
@Named
@ViewScoped
public class RecentObjectsBean implements Serializable
{
  private TreeNode treeNode;
  private int updateCount = -1;

  @Inject
  NavigatorBean navigatorBean;

  public TreeNode getTreeNode()
  {
    if (WebUtils.isRenderResponsePhase() &&
        navigatorBean.getUpdateCount() != updateCount)
    {
      treeNode = null;
    }

    if (treeNode == null)
    {      
      System.out.println(">>> getRecentObjectsTreeNode");

      treeNode = new DefaultTreeNode("", null);

      List<String> baseTypeIdList = navigatorBean.getBaseTypeIdList();
      for (String baseTypeId : baseTypeIdList)
      {
        BaseTypeInfo baseTypeInfo = navigatorBean.getBaseTypeInfo(baseTypeId);
        TypeBean typeBean = TypeBean.getInstance(baseTypeId);

        RecentType recentType = new RecentType();
        recentType.baseTypeId = baseTypeId;
        recentType.description = baseTypeInfo.getLabel();
        recentType.icon = baseTypeInfo.getIcon();

        TreeNode typeNode = new DefaultTreeNode(recentType);

        List<String> recentObjectIdList = baseTypeInfo.getRecentObjectIdList();
        for (String objectId : recentObjectIdList)
        {
          if (!NavigatorBean.NEW_OBJECT_ID.equals(objectId))
          {
            RecentObject recentObject = new RecentObject();
            recentObject.objectId = objectId;
            if (typeBean == null)
            {
              recentObject.description =
                baseTypeId + " " + recentObject.getObjectId();
            }
            else
            {
              recentObject.description = typeBean.getDescription(objectId);
            }
            typeNode.getChildren().add(new DefaultTreeNode(recentObject));
          }
        }
        if (typeNode.getChildCount() > 0)
        {
          treeNode.getChildren().add(typeNode);
          typeNode.setType("group");
          typeNode.setExpanded(baseTypeInfo.isFeatured());
        }
      }
      updateCount = navigatorBean.getUpdateCount();
      removeDuplicatedNodes();
    }
    return treeNode;
  }
  
  public void onNodeExpand(NodeExpandEvent event)
  {
    RecentType recentType = (RecentType)event.getTreeNode().getData();
    String baseTypeId = recentType.baseTypeId;
    BaseTypeInfo baseTypeInfo = navigatorBean.getBaseTypeInfo(baseTypeId);
    baseTypeInfo.setFeatured(true);
  }

  public void onNodeCollapse(NodeCollapseEvent event)
  {
    RecentType recentType = (RecentType)event.getTreeNode().getData();
    String baseTypeId = recentType.baseTypeId;
    BaseTypeInfo baseTypeInfo = navigatorBean.getBaseTypeInfo(baseTypeId);
    baseTypeInfo.setFeatured(false);
  }

  public boolean isCurrentBaseType(TreeNode node)
  {
    RecentType type = (RecentType)node.getParent().getData();
    String baseTypeId = type.baseTypeId;
    BaseTypeInfo baseTypeInfo = navigatorBean.getBaseTypeInfo();
    if (baseTypeInfo == null) return false;
    return baseTypeId.equals(baseTypeInfo.getBaseTypeId());
  }

  public boolean isSelectedNode(TreeNode node)
  {
    BaseTypeInfo baseTypeInfo = navigatorBean.getBaseTypeInfo();
    if (baseTypeInfo == null) return false;
    RecentObject recentObject = (RecentObject)node.getData();
    return recentObject.getObjectId().equals(baseTypeInfo.getObjectId());
  }

  private void removeDuplicatedNodes() 
  {
    List<TreeNode> nodesToDelete = new ArrayList<>();
    List<TreeNode> recObjNodes = getRecentObjectNodes(treeNode);
    
    Map<String, List<TreeNode>> groupedNodes = new HashMap<>();
    for (TreeNode node : recObjNodes) 
    {
      RecentObject recObj = (RecentObject)node.getData();
      String objectId = recObj.getObjectId();
      if (!groupedNodes.containsKey(objectId))
      {
        groupedNodes.put(objectId, new ArrayList<>());
      }
      groupedNodes.get(objectId).add(node);
    }
    
    for (List<TreeNode> nodeGroup : groupedNodes.values()) 
    {
      if (nodeGroup.size() > 1) 
      {        
        for (TreeNode currentNode : nodeGroup) 
        {
          TreeNode currentParentNode = currentNode.getParent();
          RecentType currentParentType = 
            (RecentType)currentParentNode.getData();
          for (TreeNode otherNode : nodeGroup) 
          {
            if (otherNode != currentNode)
            {
              TreeNode otherParentNode = otherNode.getParent();
              RecentType otherParentType = 
                (RecentType)otherParentNode.getData();
              if (otherParentType.isDescendantOf(currentParentType)) 
              {
                nodesToDelete.add(currentNode);
                break;
              }
            }
          }
        }
      }
    }
    
    for (TreeNode node : nodesToDelete)
    {
      TreeNode recentTypeNode = node.getParent();
      recentTypeNode.getChildren().remove(node);
      if (recentTypeNode.getChildren().isEmpty())
      {
        treeNode.getChildren().remove(recentTypeNode);                  
      }      
    }
  }

  private List<TreeNode> getRecentObjectNodes(TreeNode node) 
  {
    List<TreeNode> result = new ArrayList<>();
    if (node.getData() instanceof RecentObject) 
    {
      result.add(node);
    }
    for (Object child : node.getChildren()) 
    {
      result.addAll(getRecentObjectNodes((TreeNode)child));
    }
    return result;
  }  
  
  public class RecentType
  {
    String baseTypeId;
    String description;
    String icon;

    public String getBaseTypeId()
    {
      return baseTypeId;
    }

    public String getDescription()
    {
      return description;
    }

    public String getIcon()
    {
      return icon;
    }

    public boolean isDescendantOf(RecentType other)
    {
      Type type = TypeCache.getInstance().getType(baseTypeId);
      if (type != null)
      {
        return type.isDerivedFrom(other.getBaseTypeId());          
      }
      return false;
    }

    @Override
    public String toString()
    {
      return baseTypeId;
    }    
  }

  public class RecentObject
  {
    String objectId;
    String description;

    public String getObjectId()
    {
      return objectId;
    }

    public String getDescription()
    {
      return description;
    }

    @Override
    public String toString()
    {
      return objectId;
    }
  }
}
