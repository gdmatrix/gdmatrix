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
package org.santfeliu.webapp.modules.geo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.santfeliu.faces.maplibre.model.Layer;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.geo.metadata.LegendGroup;
import org.santfeliu.webapp.modules.geo.metadata.LegendItem;
import org.santfeliu.webapp.modules.geo.metadata.LegendLayer;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.santfeliu.webapp.modules.geo.metadata.StyleMetadata.LEGEND;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class GeoMapLegendBean extends WebBean implements Serializable
{
  private LegendTreeNode legendTreeRoot;
  private LegendGroup editingLegendGroup;
  private LegendLayer editingLegendLayer;
  private List<TreeNode> legendSelection;
  private List<TreeNode> legendCut;
  private boolean newNode;

  @Inject
  GeoMapBean geoMapBean;

  public TreeNode getLegendTreeRoot()
  {
    if (legendTreeRoot == null)
    {
      legendTreeRoot = new LegendTreeNode("group", null, null);

      Map<String, Object> metadata = geoMapBean.getStyle().getMetadata();

      LegendGroup legendGroup = (LegendGroup)metadata.get(LEGEND);

      if (legendGroup == null)
      {
        legendGroup = new LegendGroup();
        legendGroup.setLabel("Legend");
        metadata.put(LEGEND, legendGroup);
      }
      populateLegendItem(legendGroup, legendTreeRoot);
    }
    return legendTreeRoot;
  }

  public void updateLegendTreeRoot()
  {
    legendTreeRoot = null;
  }

  public List<TreeNode> getLegendSelection()
  {
    return legendSelection;
  }

  public void setLegendSelection(List<TreeNode> legendSelection)
  {
    this.legendSelection = legendSelection;
  }

  public void cutLegendNodes()
  {
    legendCut = legendSelection;
  }

  public boolean isLegendNodeSelected(String type)
  {
    if (legendSelection != null && legendSelection.size() == 1)
    {
      LegendTreeNode node = (LegendTreeNode)legendSelection.get(0);
      return node.getType().equals(type);
    }
    return false;
  }

  public LegendGroup getEditingLegendGroup()
  {
    return editingLegendGroup;
  }

  public LegendLayer getEditingLegendLayer()
  {
    return editingLegendLayer;
  }

  public boolean isNewNode()
  {
    return newNode;
  }

  public void addLegendGroup()
  {
    if (legendSelection != null && legendSelection.size() == 1)
    {
      editingLegendGroup = new LegendGroup();
      editingLegendGroup.setMode(LegendGroup.MULTIPLE);
      editingLegendGroup.setLabel("Group");
      newNode = true;
      geoMapBean.setDialogVisible(true);
    }
  }

  public void addLegendLayer()
  {
    if (legendSelection != null && legendSelection.size() == 1)
    {
      editingLegendLayer = new LegendLayer();
      newNode = true;
      geoMapBean.setDialogVisible(true);
    }
  }

  public void editLegendGroup()
  {
    editingLegendGroup = (LegendGroup)legendSelection.get(0).getData();
    geoMapBean.setDialogVisible(true);
  }

  public void editLegendLayer()
  {
    editingLegendLayer = (LegendLayer)legendSelection.get(0).getData();
    geoMapBean.setDialogVisible(true);
  }

  public void acceptLegendGroup()
  {
    if (newNode)
    {
      LegendTreeNode groupNode = new LegendTreeNode(editingLegendGroup);
      LegendTreeNode targetNode = (LegendTreeNode)legendSelection.get(0);
      if (targetNode.isGroupNode())
      {
        targetNode.add(groupNode);
      }
      newNode = false;
    }
    editingLegendGroup = null;
    geoMapBean.setDialogVisible(false);
  }

  public void cancelLegendGroup()
  {
    editingLegendGroup = null;
    newNode = false;
    geoMapBean.setDialogVisible(false);
  }

  public void acceptLegendLayer()
  {
    if (isBlank(editingLegendLayer.getLabel()))
    {
      Optional<Layer> layer = geoMapBean.getStyle().getLayers().stream()
        .filter(l -> l.getId().equals(editingLegendLayer.getLayerId()))
        .findAny();
      if (layer.isPresent())
      {
        editingLegendLayer.setLabel(layer.get().getLabel());
      }
      else
      {
        editingLegendLayer.setLabel(editingLegendLayer.getLayerId());
      }
    }
    if (newNode)
    {
      LegendTreeNode layerNode = new LegendTreeNode(editingLegendLayer);
      LegendTreeNode targetNode = (LegendTreeNode)legendSelection.get(0);
      if (targetNode.isGroupNode())
      {
        targetNode.add(layerNode);
      }
      newNode = false;
    }
    editingLegendLayer = null;
    geoMapBean.setDialogVisible(false);
  }

  public void cancelLegendLayer()
  {
    editingLegendLayer = null;
    geoMapBean.setDialogVisible(false);
  }

  public void removeLegendLayerNode(String layerId)
  {
    removeLegendLayerNode(getLegendTreeRoot(), layerId);
  }

  public void removeLegendLayerNode(TreeNode<LegendItem> node, String layerId)
  {
    LegendItem item = node.getData();
    if (item instanceof LegendLayer)
    {
      LegendLayer legendLayer = (LegendLayer)item;
      if (layerId.equals(legendLayer.getLayerId()))
      {
        TreeNode parentNode = node.getParent();
        if (parentNode != null)
        {
          parentNode.getChildren().remove(node);
        }
      }
    }
    else
    {
      List<TreeNode<LegendItem>> children = new ArrayList<>(node.getChildren());
      for (TreeNode<LegendItem> childNode : children)
      {
        removeLegendLayerNode(childNode, layerId);
      }
    }
  }

  public void removeLegendNodes()
  {
    for (TreeNode node : legendSelection)
    {
      TreeNode parentNode = node.getParent();
      if (parentNode != null)
      {
        parentNode.getChildren().remove(node);
      }
    }
  }

  public boolean isTopLegendNode()
  {
    if (legendSelection == null || legendSelection.size() != 1) return false;

    return legendSelection.get(0).getParent() == this.legendTreeRoot;
  }

  public boolean isCutLegendNode(TreeNode node)
  {
    if (legendCut == null || node == null) return false;

    if (legendCut.contains(node)) return true;

    return isCutLegendNode(node.getParent());
  }

  public boolean isLegendPasteEnabled()
  {
    if (legendCut == null) return false;
    if (legendSelection == null) return false;
    if (legendSelection.size() != 1) return false;
    LegendTreeNode node = (LegendTreeNode)legendSelection.get(0);
    return node.isGroupNode();
  }

  public void pasteLegendNodes()
  {
    if (legendCut != null && legendSelection != null)
    {
      if (legendSelection.size() == 1 && !legendCut.isEmpty())
      {
        LegendTreeNode targetNode = (LegendTreeNode)legendSelection.get(0);
        if (targetNode.isGroupNode())
        {
          for (TreeNode node : legendCut)
          {
            LegendTreeNode sourceNode = (LegendTreeNode)node;
            if (targetNode != sourceNode &&
                !targetNode.isDescendant(sourceNode))
            {
              targetNode.add(sourceNode);
            }
          }
        }
      }
      legendCut = null;
    }
  }


  private LegendTreeNode populateLegendItem(LegendItem legendItem,
    LegendTreeNode parentTreeNode)
  {
    LegendTreeNode node = new LegendTreeNode(legendItem);
    node.setType(legendItem.getType());
    node.setExpanded(true);
    if (parentTreeNode != null)
    {
      parentTreeNode.getChildren().add(node);
    }
    if (legendItem instanceof LegendGroup)
    {
      LegendGroup group = (LegendGroup)legendItem;
      for (LegendItem item : group.getChildren())
      {
        populateLegendItem(item, node);
      }
    }
    return node;
  }

  static public class LegendTreeNode extends DefaultTreeNode<LegendItem>
  {
    public LegendTreeNode()
    {
    }

    public LegendTreeNode(LegendItem data)
    {
      this(data, null);
    }

    public LegendTreeNode(LegendItem data, TreeNode parent)
    {
      this(data.getType(), data, parent);
    }

    public LegendTreeNode(String type, LegendItem data, TreeNode parent)
    {
      super(type, data, parent);
    }

    public boolean isGroupNode()
    {
      return "group".equals(getType());
    }

    public void add(LegendTreeNode legendNode)
    {
      getChildren().add(legendNode);
      LegendGroup legendGroup = (LegendGroup)getData();
      legendGroup.getChildren().add(legendNode.getData());
      setExpanded(true);
    }

    public void add(int index, LegendTreeNode legendNode)
    {
      getChildren().add(index, legendNode);
      LegendGroup legendGroup = (LegendGroup)getData();
      legendGroup.getChildren().add(index, legendNode.getData());
      setExpanded(true);
    }

    public boolean isDescendant(TreeNode ancestor)
    {
      TreeNode node = getParent();
      while (node != null && node != ancestor)
      {
        node = node.getParent();
      }
      return node == ancestor;
    }

    @Override
    public void clearParent()
    {
      LegendTreeNode oldParentNode = (LegendTreeNode)getParent();
      if (oldParentNode != null)
      {
        LegendGroup legendGroup = (LegendGroup)oldParentNode.getData();
        LegendItem legendItem = (LegendItem)getData();
        legendGroup.getChildren().remove(legendItem);
      }
      super.clearParent();
    }

    @Override
    public String toString()
    {
      LegendItem legendItem = getData();
      if (legendItem == null) return null;

      String type = legendItem.getType();
      String label = legendItem.getLabel();
      return type + ": " + label;
    }
  }
}
