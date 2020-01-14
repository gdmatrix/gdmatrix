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
package org.santfeliu.workflow;

import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author unknown
 */
public class Workflow 
{
  public static final int DEFAULT_FORMAT = 2;

  private String name = "new";
  private String version = "1";
  private int format = DEFAULT_FORMAT;
  private String description = "new";
  private LinkedList nodes;
  private String firstNodeId = "0";
  private boolean undoable = true;
  private int gridSize = 16;
  private int nodeWidth = 160;
  private int nodeHeight = 96;
  private boolean gridVisible = true;
  private boolean gridEnabled = true;

  public Workflow()
  {
    nodes = new LinkedList();
  }

  public void addNode(WorkflowNode node)
    throws WorkflowException
  {
    if (node.workflow != null) 
      throw new WorkflowException("node not free");
    nodes.add(node);
    node.workflow = this;
  }
  
  public void removeNode(WorkflowNode node)
  {
    nodes.remove(node);
    node.workflow = null;
  }
  
  public WorkflowNode getNode(String nodeId)
  {
    WorkflowNode node = null;
    Iterator iter = nodes.iterator();
    boolean found = false;
    while (iter.hasNext() && !found)
    {
      node = (WorkflowNode)iter.next();
      if (node.getId().equals(nodeId)) found = true;
    }
    return found ? node : null;
  }

  public int getNodesCount()
  {
    return nodes.size();
  }

  public WorkflowNode[] getNodes()
  {
    return (WorkflowNode[])nodes.toArray(new WorkflowNode[nodes.size()]);
  }

  public void setFirstNodeId(String firstNodeId)
  {
    this.firstNodeId = firstNodeId;
  }

  public String getFirstNodeId()
  {
    return firstNodeId;
  }

  public void setUndoable(boolean undoable)
  {
    this.undoable = undoable;
  }

  public boolean isUndoable()
  {
    return undoable;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  public void setVersion(String version)
  {
    this.version = version;
  }

  public String getVersion()
  {
    return version;
  }

  public void setFormat(int format)
  {
    this.format = format;
  }

  public int getFormat()
  {
    return format;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getDescription()
  {
    return description;
  }

  public int getGridSize()
  {
    return gridSize;
  }

  public void setGridSize(int gridSize)
  {
    if (gridSize <= 4) gridSize = 4;
    this.gridSize = gridSize;
  }

  public int getNodeWidth()
  {
    return nodeWidth;
  }

  public void setNodeWidth(int nodeWidth)
  {
    if (nodeWidth <= 10) nodeWidth = 10;
    this.nodeWidth = nodeWidth;
  }

  public int getNodeHeight()
  {
    return nodeHeight;
  }

  public void setNodeHeight(int nodeHeight)
  {
    if (nodeHeight <= 10) nodeHeight = 10;
    this.nodeHeight = nodeHeight;
  }

  public boolean isGridVisible()
  {
    return gridVisible;
  }

  public void setGridVisible(boolean visible)
  {
    this.gridVisible = visible;
  }

  public boolean isGridEnabled()
  {
    return gridEnabled;
  }

  public void setGridEnabled(boolean enabled)
  {
    this.gridEnabled = enabled;
  }

  @Override
  public String toString()
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append("name=");
    buffer.append(name);
    buffer.append("\n");

    buffer.append("version=");
    buffer.append(version);
    buffer.append("\n");

    buffer.append("description=");
    buffer.append(description);
    buffer.append("\n");

    buffer.append("persistent=");
    buffer.append(undoable);
    buffer.append("\n");

    buffer.append("firstnode=");
    buffer.append(firstNodeId);
    buffer.append("\n");
    
    Iterator iter = nodes.iterator();
    while (iter.hasNext())
    {
      WorkflowNode wfNode = (WorkflowNode)iter.next();
      buffer.append(wfNode.toString());
      buffer.append("\n");
    }
    return buffer.toString();
  }
}
