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
package org.santfeliu.cms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.matrix.cms.CMSManagerPort;
import org.matrix.cms.Node;
import org.matrix.cms.NodeFilter;
import org.matrix.cms.Property;

/**
 *
 * @author unknown
 */
public class CNode
{
  private final CWorkspace cWorkspace;
  private final Node node;
  private Map<String, Object> propertyMap;

  public CNode(CWorkspace cWorkspace, Node node)
  {
    this.cWorkspace = cWorkspace;
    this.node = node;
    this.propertyMap = new HashMap<String, Object>();
    for (Property property : node.getProperty())
    {
      putProperty(property);
    }
    this.propertyMap = Collections.unmodifiableMap(this.propertyMap);
  }

  public CWorkspace getCWorkspace()
  {
    return cWorkspace;
  }

  public Node getNode()
  {
    return node;
  }

  public List<String> getChildrenNodeIdList()
  {
    synchronized (cWorkspace)
    {
      String nodeId = getNodeId();
      if (cWorkspace.getChildren(nodeId) == null)
      {
        cWorkspace.initParentChildren(nodeId);
        List<String> siblingNodeIdList = cWorkspace.getSiblings(nodeId);
        List<String> finalSiblingNodeIdList = new ArrayList<String>();
        for (String siblingNodeId : siblingNodeIdList)
        {
          if (cWorkspace.getChildren(siblingNodeId) == null)
          {
            cWorkspace.initParentChildren(siblingNodeId);
            finalSiblingNodeIdList.add(siblingNodeId);
          }
        }
        NodeFilter nodeFilter = new NodeFilter();
        nodeFilter.getWorkspaceId().add(getWorkspaceId());
        nodeFilter.getParentNodeId().add(nodeId);
        nodeFilter.getParentNodeId().addAll(finalSiblingNodeIdList);
        List<Node> fullNodeList = getPort().findNodes(nodeFilter);
        Map<String, List<Node>> auxMap = new HashMap<String, List<Node>>();      
        for (Node childNode : fullNodeList)
        {
          String parentNodeId = childNode.getParentNodeId();
          if (!auxMap.containsKey(parentNodeId))
          {
            auxMap.put(parentNodeId, new ArrayList<Node>());
          }
          auxMap.get(parentNodeId).add(childNode);
        }
        for (List<Node> nodeList : auxMap.values())
        {
          nodeList = quicksortNodesByIndex(nodeList);
          for (Node childNode : nodeList)
          {
            CNode childCNode = new CNode(cWorkspace, childNode);
            cWorkspace.putNode(childCNode, false);
            cWorkspace.putParentChildren(childNode.getNodeId(),
              childNode.getParentNodeId());
          }
        }
      }     
      return cWorkspace.getChildren(nodeId);
    }
  }

  public String getWorkspaceId()
  {
    return node.getWorkspaceId();
  }

  public String getNodeId()
  {
    return node.getNodeId();
  }

  public String getParentNodeId()
  {
    return node.getParentNodeId();
  }

  public String getName()
  {
    return node.getName();
  }

  public CNode getParent()
  {
    String parentNodeId = getParentNodeId();
    return (parentNodeId == null ? null : cWorkspace.getNode(parentNodeId));
  }

  public CNode getPreviousSibling()
  {
    CNode resultCNode = null;
    boolean found = false;
    CNode parentCNode = getParent();
    if (parentCNode != null)
    {
      List<String> siblingNodeIdList = parentCNode.getChildrenNodeIdList();
      for (int i = 0; i < siblingNodeIdList.size() && !found; i++)
      {
        if (siblingNodeIdList.get(i).equals(getNodeId()))
        {
          found = true;
          if (i > 0) resultCNode =
            cWorkspace.getNode(siblingNodeIdList.get(i - 1));
        }
      }
    }
    return resultCNode;
  }

  public String getPreviousSiblingNodeId()
  {
    CNode previousSibling = getPreviousSibling();
    if (previousSibling != null) return previousSibling.getNodeId();
    else return null;
  }

  public CNode getNextSibling()
  {
    CNode resultCNode = null;
    boolean found = false;
    CNode parentCNode = getParent();
    if (parentCNode != null)
    {
      List<String> siblingNodeIdList = parentCNode.getChildrenNodeIdList();
      for (int i = 0; i < siblingNodeIdList.size() && !found; i++)
      {
        if (siblingNodeIdList.get(i).equals(getNodeId()))
        {
          found = true;
          if (i < siblingNodeIdList.size() - 1) resultCNode =
            cWorkspace.getNode(siblingNodeIdList.get(i + 1));
        }
      }
    }
    return resultCNode;
  }

  public String getNextSiblingNodeId()
  {
    CNode nextSibling = getNextSibling();
    if (nextSibling != null) return nextSibling.getNodeId();
    else return null;
  }

  public CNode getFirstChild()
  {
    CNode resultCNode = null;
    int childCount = getChildrenNodeIdList().size();
    if (childCount > 0)
    {
      String nodeId = getChildrenNodeIdList().get(0);
      resultCNode = cWorkspace.getNode(nodeId);
    }
    return resultCNode;
  }

  public String getFirstChildNodeId()
  {
    CNode firstChild = getFirstChild();
    if (firstChild != null) return firstChild.getNodeId();
    else return null;
  }

  public CNode getLastChild()
  {
    CNode resultCNode = null;
    int childCount = getChildrenNodeIdList().size();
    if (childCount > 0)
    {
      String nodeId = getChildrenNodeIdList().get(childCount - 1);
      resultCNode = cWorkspace.getNode(nodeId);
    }
    return resultCNode;
  }

  public String getLastChildNodeId()
  {
    CNode lastChild = getLastChild();
    if (lastChild != null) return lastChild.getNodeId();
    else return null;
  }

  public List<CNode> getChildren()
  {
    List<CNode> cNodeList = new ArrayList<CNode>();
    for (String childNodeId : getChildrenNodeIdList())
    {
      CNode cNodeAux = cWorkspace.getNode(childNodeId);
      if (cNodeAux != null)
      {
        cNodeList.add(cNodeAux);
      }
    }
    return cNodeList;
  }

  public int getChildCount()
  {
    return getChildrenNodeIdList().size();
  }

  public CNode getChild(int index)
  {
    //TODO In ForestNode, when index < 0 the first child is returned
    CNode resultCNode = null;
    if (index >= 0 && index < getChildrenNodeIdList().size())
    {
      String nodeId = getChildrenNodeIdList().get(index);
      resultCNode = cWorkspace.getNode(nodeId);
    }
    return resultCNode;
  }

  public boolean isRoot()
  {
    return getParentNodeId() == null;
  }

  public boolean isLeaf()
  {
    return getChildrenNodeIdList().isEmpty();
  }

  public boolean hasTheSameProperties(CNode cNode)
  {
    Map<String, Object> auxPropertyMap = cNode.getPropertiesMap();
    if (propertyMap.size() != auxPropertyMap.size())
    {
      return false;
    }
    else
    {
      for (String propertyName : propertyMap.keySet())
      {
        if (propertyName.endsWith("$"))
        {
          if (!auxPropertyMap.containsKey(propertyName))
          {
            return false;
          }
          else
          {
            List<String> propertyValues =
              (List<String>)propertyMap.get(propertyName);
            List<String> auxPropertyValues =
              (List<String>)auxPropertyMap.get(propertyName);
            if (propertyValues.size() != auxPropertyValues.size())
            {
              return false;
            }
            else
            {
              for (int i = 0; i < propertyValues.size(); i++)
              {
                if (!propertyValues.get(i).equals(auxPropertyValues.get(i)))
                {
                  return false;
                }
              }
            }
          }
        }
      }
    }
    return true;
  }

  public List<String> getDifferentProperties(CNode cNode)
  {
    List<String> result = new ArrayList<String>();
    Map<String, Object> auxPropertyMap = cNode.getPropertiesMap();
    for (String propertyName : propertyMap.keySet())
    {
      if (propertyName.endsWith("$"))
      {
        if (!auxPropertyMap.containsKey(propertyName))
        {          
          result.add(propertyName.substring(0, propertyName.length() - 1));
        }
        else
        {
          List<String> propertyValues =
            (List<String>)propertyMap.get(propertyName);
          List<String> auxPropertyValues =
            (List<String>)auxPropertyMap.get(propertyName);
          if (propertyValues.size() != auxPropertyValues.size())
          {
            result.add(propertyName.substring(0, propertyName.length() - 1));
          }
          else
          {
            boolean stop = false;
            for (int i = 0; i < propertyValues.size() && !stop; i++)
            {
              if (!propertyValues.get(i).equals(auxPropertyValues.get(i)))
              {
                result.add(propertyName.substring(0, propertyName.length() - 1));
                stop = true;
              }
            }
          }
        }
      }
    }
    for (String propertyName : auxPropertyMap.keySet())
    {
      if (propertyName.endsWith("$"))
      {
        if (!propertyMap.containsKey(propertyName))
        {
          result.add(propertyName.substring(0, propertyName.length() - 1));
        }
      }
    }
    return result;
  }
  
  public boolean hasTheSameLocation(CNode cNode)
  {
    if (this.getParentNodeId() == null && cNode.getParentNodeId() == null)
    {
      return true;
    }
    else if (this.getParentNodeId() != null && cNode.getParentNodeId() != null)
    {
      return (this.getParentNodeId().equals(cNode.getParentNodeId())
        && this.getNode().getIndex() == cNode.getNode().getIndex());
    }
    else
    {
      return false;
    }
  }

  public boolean hasTheSameName(CNode cNode)
  {
    if (this.getName() == null && cNode.getName() == null)
    {
      return true;
    }
    else if (this.getName() != null && cNode.getName() != null)
    {
      return this.getName().equals(cNode.getName());
    }
    else
    {
      return false;
    }
  }

  public int getIndexOfChild()
  {
    CNode parent = getParent();
    if (parent == null) return 0;
    CNode child = parent.getFirstChild();
    int index = 0;
    boolean found = false;
    while (child != null && !found)
    {
      if (child.getNodeId().equals(getNodeId()))
      {
        found = true;
      }
      else
      {
        index++;
        child = child.getNextSibling();
      }
    }
    return found ? index : -1;
  }

  public CNode getRoot()
  {
    CNode root = this;
    CNode cNodeAux = this;
    while (cNodeAux != null)
    {
      root = cNodeAux;
      cNodeAux = cNodeAux.getParent();
    }
    return root;
  }

  public int getDepth()
  {
    int depth = 0;
    CNode cNodeAux = this;
    while (cNodeAux != null)
    {
      depth++;
      cNodeAux = cNodeAux.getParent();
    }
    return depth;
  }

  public String[] getNodeIdPath() throws Exception
  {
    String[] nodeIdArray = null;
    int count = getDepth();
    nodeIdArray = new String[count];
    CNode cNodeAux = this;
    while (count > 0 && cNodeAux != null)
    {
      nodeIdArray[count - 1] = cNodeAux.getNodeId();
      cNodeAux = cNodeAux.getParent();
      count--;
    }
    return nodeIdArray;
  }

  public CNode[] getPath() throws Exception
  {
    CNode[] cNodeArray = null;
    int count = getDepth();
    cNodeArray = new CNode[count];
    CNode cNodeAux = this;
    while (count > 0 && cNodeAux != null)
    {
      cNodeArray[count - 1] = cNodeAux;
      cNodeAux = cNodeAux.getParent();
      count--;
    }
    return cNodeArray;
  }

  public List<String> getMultiPropertyValue(String propertyName)
  {
    return (List<String>)propertyMap.get(propertyName + "$");
  }

  public String getSinglePropertyValue(String propertyName)
  {
    return (String)propertyMap.get(propertyName);
  }

  public Property getProperty(String propertyName)
  {
    return getPropertyFromMap(propertyName);
  }

  public List<Property> getProperties()
  {
    return getProperties(false);
  }

  public List<Property> getProperties(boolean sort)
  {    
    List<Property> auxPropertyList = new ArrayList<Property>();
    for (String propertyName : propertyMap.keySet())
    {
      if (!propertyName.endsWith("$"))
      {
        Property property = getPropertyFromMap(propertyName);
        auxPropertyList.add(property);
      }
    }
    if (auxPropertyList.size() > 1 && sort)
    {
      Collections.sort(auxPropertyList, new Comparator()
        {
          public int compare(Object o1, Object o2)
          {
            Property p1 = (Property)o1;
            Property p2 = (Property)o2;
            return p1.getName().compareToIgnoreCase(p2.getName());
          }
        }
      );
    }
    return auxPropertyList;
  }

  public Map getPropertiesMap()
  {
    return propertyMap;
  }

  private CMSManagerPort getPort()
  {
    return cWorkspace.getPort();
  }

  // TODO: replace by Collections.sort(nodeList, comparator)
  private List<Node> quicksortNodesByIndex(List<Node> nodeList)
  {
    if (nodeList == null || nodeList.size() <= 1) return nodeList;
    List<Node> result = new ArrayList<Node>();
    List<Node> lessAuxList = new ArrayList<Node>();
    List<Node> greaterAuxList = new ArrayList<Node>();
    Node pivotNode = nodeList.remove(0);
    for (Node auxNode : nodeList)
    {
      if (auxNode.getIndex() < pivotNode.getIndex())
      {
        lessAuxList.add(auxNode);
      }
      else if (auxNode.getIndex() > pivotNode.getIndex())
      {
        greaterAuxList.add(auxNode);
      }
      else //same index
      {
        int auxNodeId = Integer.valueOf(auxNode.getNodeId());
        int pivotNodeId = Integer.valueOf(pivotNode.getNodeId());
        if (auxNodeId <= pivotNodeId)
        {
          lessAuxList.add(auxNode);
        }
        else
        {
          greaterAuxList.add(auxNode);
        }
      }
    }
    result.addAll(quicksortNodesByIndex(lessAuxList));
    result.add(pivotNode);
    result.addAll(quicksortNodesByIndex(greaterAuxList));
    return result;
  }
  
  private void putProperty(Property property)
  {
    String propertyName = property.getName();
    propertyMap.put(propertyName, property.getValue().get(0));
    propertyMap.put(propertyName + "$",
      Collections.unmodifiableList(new ArrayList<String>(property.getValue())));
  }

  private Property getPropertyFromMap(String propertyName)
  {
    if (propertyMap.containsKey(propertyName + "$"))
    {
      Property property = new Property();
      property.setName(propertyName);
      property.getValue().addAll(
        (List<String>)propertyMap.get(propertyName + "$"));
      return property;
    }
    return null;
  }

}
