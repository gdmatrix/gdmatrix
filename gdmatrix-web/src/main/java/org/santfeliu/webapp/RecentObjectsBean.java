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

import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.santfeliu.webapp.NavigatorBean.BaseTypeInfo;

/**
 *
 * @author realor
 */
@Named("recentObjectsBean")
@RequestScoped
public class RecentObjectsBean
{
  private TreeNode treeNode;
  private int updateCount = -1;

  @Inject
  NavigatorBean navigatorBean;

  public TreeNode getTreeNode()
  {
    if (navigatorBean.getUpdateCount() != updateCount)
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
