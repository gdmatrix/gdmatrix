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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.apache.commons.collections.map.LRUMap;
import org.matrix.cms.CMSConstants;
import org.matrix.cms.CMSManagerPort;
import org.matrix.cms.Node;
import org.matrix.cms.NodeFilter;
import org.matrix.cms.Property;
import org.matrix.cms.Workspace;
import org.santfeliu.jmx.CacheMBean;

/**
 *
 * @author lopezrj-sf
 */
public class CWorkspace
{
  private static final long FAST_PURGE_TIME = 1 * 60 * 1000; // 1 minute
  private static final long PARENTS_PURGE_TIME = 10 * 60 * 1000; // 10 minutes

  private static final String PARAM_DESKTOP_MAIN_NODE = "desktopMainNode";
  private static final String PARAM_MOBILE_MAIN_NODE = "mobileMainNode";

  private final CMSCache cmsCache;
  private final Workspace workspace;
  private final Map cNodeMap;
  private String purgeDateTime = getInitDateTime();
  private long lastPurgeMillis = 0;
  private long lastParentsPurgeMillis = 0;
  private final Map<String, String> parentsMap =
    Collections.synchronizedMap(new HashMap<>());
  private final Map<String, List<String>> childrenMap =
    Collections.synchronizedMap(new HashMap<>());
  private final int cacheMaxSize;
  private String mainNodeId = null;
  private String mobileMainNodeId = null;
  
  public static final String NULL_MAIN_NODE = "-";

  public CWorkspace(CMSCache cmsCache, Workspace workspace, int cacheMaxSize)
  {
    this.cmsCache = cmsCache;
    this.workspace = workspace;
    this.cacheMaxSize = cacheMaxSize;
    this.cNodeMap = Collections.synchronizedMap(new LRUMap(cacheMaxSize));
  }

  public CMSCache getCmsCache()
  {
    return cmsCache;
  }

  public Workspace getWorkspace()
  {
    return workspace;
  }

  public int getNodeCount()
  {
    return cNodeMap.keySet().size();
  }

  public int getParentsCount()
  {
    return parentsMap.keySet().size();
  }

  public int getChildrenCount()
  {
    return childrenMap.keySet().size();
  }

  public boolean containsNode(String nodeId)
  {
    return cNodeMap.containsKey(nodeId);
  }

  public CNode getNode(String nodeId)
  {
    long nowMillis = System.currentTimeMillis();
    if (mustPurgeParentChildren(nowMillis))
    {
      purgeParentChildren(nowMillis);
    }
    
    CNode cNode = (CNode)cNodeMap.get(nodeId);
    if (cNode == null)
    {
      try
      {
        Node node = getPort().loadNode(workspace.getWorkspaceId(), nodeId);
        cNode = new CNode(this, node);
        putNode(cNode);
      }
      catch (Exception ex)
      {
        //node not found
      }
    }
    return cNode;
  }

  public Map<String, CNode> getNodes(List<String> nodeIdList)
  {
    long nowMillis = System.currentTimeMillis();
    if (mustPurgeParentChildren(nowMillis))
    {
      purgeParentChildren(nowMillis);
    }

    Map<String, CNode> result = new HashMap<>();
    List<String> pendantNodeIdList = new ArrayList<>();
    for (String nodeId : nodeIdList)
    {
      CNode cNode = (CNode)cNodeMap.get(nodeId);
      if (cNode == null)
      {
        pendantNodeIdList.add(nodeId);
      }
      result.put(nodeId, cNode);
    }
    if (!pendantNodeIdList.isEmpty())
    {
      NodeFilter filter = new NodeFilter();
      filter.getWorkspaceId().add(workspace.getWorkspaceId());
      filter.getNodeId().addAll(pendantNodeIdList);
      List<Node> nodeList = getPort().findNodes(filter);
      for (Node node : nodeList)
      {
        CNode cNode = new CNode(this, node);
        putNode(cNode);
        result.put(node.getNodeId(), cNode);
      }
    }
    return result;
  }

  public CNode findNode(String propertyName, String propertyValue)
    throws Exception
  {
    NodeFilter filter = new NodeFilter();
    filter.getWorkspaceId().add(workspace.getWorkspaceId());
    Property property = new Property();
    property.setName(propertyName);
    property.getValue().add(propertyValue);
    filter.getProperty().add(property);
    List<Node> nodeList = getPort().findNodes(filter);
    if (!nodeList.isEmpty())
    {
      String nodeId = nodeList.get(0).getNodeId();
      return getNode(nodeId);
    }
    return null;
  }

  public CNode findNodeByTopic(String topic) throws Exception
  {
    NodeFilter filter = new NodeFilter();
    filter.getWorkspaceId().add(workspace.getWorkspaceId());
    filter.setName(topic);
    List<Node> nodeList = getPort().findNodes(filter);
    if (!nodeList.isEmpty())
    {
      String nodeId = nodeList.get(0).getNodeId();
      return getNode(nodeId);
    }
    return null;
  }

  public void clear()
  {
    cNodeMap.clear();
    parentsMap.clear();
    childrenMap.clear();
    mainNodeId = null;
    mobileMainNodeId = null;
  }

  public void fastPurge()
  {
    long nowMillis = System.currentTimeMillis();
    if (nowMillis > lastPurgeMillis + FAST_PURGE_TIME)
    {
      purge();
    }
  }

  public void purge()
  {
    List<Node> modifiedNodeList = getPort().findModifiedNodes(
      workspace.getWorkspaceId(), purgeDateTime);
    if (!modifiedNodeList.isEmpty())
    {
      Node clockNode = modifiedNodeList.remove(0);
      String newPurgeDateTime = clockNode.getChangeDateTime();
      for (Node node : modifiedNodeList)
      {
        removeModifiedNode(node);
      }
      purgeDateTime = newPurgeDateTime;
      lastPurgeMillis = System.currentTimeMillis();
    }
  }

  public String getFamilyWorkspaceId()
  {
    CWorkspace cWorkspaceAux = this;
    String refWorkspaceId = getWorkspace().getRefWorkspaceId();
    while (refWorkspaceId != null)
    {
      cWorkspaceAux = cmsCache.getWorkspace(refWorkspaceId);
      refWorkspaceId = cWorkspaceAux.getWorkspace().getRefWorkspaceId();
    }
    return cWorkspaceAux.getWorkspace().getWorkspaceId();
  }

  public String getNodeIdByProperty(String propertyName,
    String propertyValue)
  {
    NodeFilter nodeFilter = new NodeFilter();
    nodeFilter.getWorkspaceId().add(workspace.getWorkspaceId());
    Property property = new Property();
    property.setName(propertyName);
    property.getValue().add(propertyValue);
    nodeFilter.getProperty().add(property);

    List<Node> nodeList = getPort().findNodes(nodeFilter);
    return nodeList.isEmpty() ? null :  nodeList.get(0).getNodeId();
  }

  public String getSmallestRootNodeId()
  {
    NodeFilter nodeFilter = new NodeFilter();
    nodeFilter.getWorkspaceId().add(workspace.getWorkspaceId());
    nodeFilter.getParentNodeId().add(CMSConstants.NULL_PARENT_SYMBOL);

    int minValue = Integer.MAX_VALUE;
    List<Node> nodeList = getPort().findNodes(nodeFilter);
    for (Node node : nodeList)
    {
      int intNodeId = Integer.parseInt(node.getNodeId());
      if (intNodeId < minValue)
      {
        minValue = intNodeId;
      }
    }
    return String.valueOf(minValue);
  }

  public String getMainNodeId()
  {
    if (mainNodeId == null)
    {
      mainNodeId = getNodeIdByProperty(PARAM_DESKTOP_MAIN_NODE, "true");
      if (mainNodeId == null) mainNodeId = NULL_MAIN_NODE;
    }
    return mainNodeId;
  }

  public String getMobileMainNodeId()
  {
    if (mobileMainNodeId == null)
    {
      mobileMainNodeId = getNodeIdByProperty(PARAM_MOBILE_MAIN_NODE, "true");
      if (mobileMainNodeId == null) mobileMainNodeId = NULL_MAIN_NODE;
    }
    return mobileMainNodeId;
  }

  protected void putNode(CNode cNode, boolean force)
  {
    String nodeId = cNode.getNode().getNodeId();
    if (force)
    {
      cNodeMap.put(nodeId, cNode);
    }
    else
    {
      CNode currentCNode = (CNode)cNodeMap.get(nodeId);
      if (currentCNode == null)
      {
        cNodeMap.put(nodeId, cNode);
      }
    }
  }

  private void putNode(CNode cNode)
  {
    putNode(cNode, true);
  }

  private void removeModifiedNode(Node node)
  {
    String nodeId = node.getNodeId();
    Node currentNode = null;
    CNode currentCNode = (CNode)cNodeMap.get(nodeId);
    if (currentCNode != null)
    {
      currentNode = currentCNode.getNode();
    }
    purgeParents(currentNode, node);
    cNodeMap.remove(nodeId);
  }

  protected CMSManagerPort getPort()
  {
    return cmsCache.getPort();
  }

  private String getInitDateTime()
  {
    Date date = new Date(System.currentTimeMillis());
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    return df.format(date);
  }

  private void purgeParents(Node currentNode, Node newNode)
  {
    String nodeId = newNode.getNodeId();
    String currentParentNodeId = (currentNode != null ?
      currentNode.getParentNodeId() :
      getParent(nodeId));
    String newParentNodeId = newNode.getParentNodeId();
    if (currentParentNodeId == null && newParentNodeId == null)
    {
      //nothing here
    }
    else
    {
      if (currentParentNodeId != null)
      {
        removeParentChildren(currentParentNodeId);
      }
      if (newParentNodeId != null &&
        !newParentNodeId.equals(currentParentNodeId))
      {
        removeParentChildren(newParentNodeId);
      }
    }
  }

  //PARENTS MANAGEMENT METHODS

  protected synchronized List<String> getSiblings(String nodeId)
  {
    List<String> result = new ArrayList<>();
    String parentNodeId = getParent(nodeId);
    if (parentNodeId != null)
    {
      List<String> children = getChildren(parentNodeId);
      if (children != null)
      {
        result.addAll(children);
        result.remove(nodeId);
      }
    }
    return result;
  }

  protected List<String> getChildren(String parentNodeId)
  {
    return childrenMap.get(parentNodeId);
  }

  protected synchronized void putParentChildren(String childNodeId, 
    String parentNodeId)
  {
    parentsMap.put(childNodeId, parentNodeId);
    if (!childrenMap.containsKey(parentNodeId))
    {
      initParentChildren(parentNodeId);
    }
    childrenMap.get(parentNodeId).add(childNodeId);
  }

  protected void initParentChildren(String parentNodeId)
  {
    childrenMap.put(parentNodeId, new ArrayList<>());
  }

  private String getParent(String childNodeId)
  {
    return parentsMap.get(childNodeId);
  }

  private synchronized void removeParentChildren(String parentNodeId)
  {
    if (childrenMap.containsKey(parentNodeId))
    {
      List<String> children = childrenMap.get(parentNodeId);
      for (String childNodeId : children)
      {
        parentsMap.remove(childNodeId);
      }
      childrenMap.remove(parentNodeId);
    }
  }

  private void purgeParentChildren(long nowMillis)
  {
    List<String> parentNodeIdList = new ArrayList<>();
    parentNodeIdList.addAll(childrenMap.keySet());
    for (String parentNodeId : parentNodeIdList)
    {
      if (!cNodeMap.containsKey(parentNodeId))
      {
        removeParentChildren(parentNodeId);
      }
    }
    lastParentsPurgeMillis = nowMillis;
  }

  private boolean mustPurgeParentChildren(long nowMillis)
  {
    return nowMillis - lastParentsPurgeMillis > PARENTS_PURGE_TIME;
  }

  CWorkspaceMBean getCacheMBean() throws NotCompliantMBeanException
  {
    return new CWorkspaceMBean();
  }

  /* MBean */
  public class CWorkspaceMBean extends StandardMBean implements CacheMBean
  {
    public CWorkspaceMBean() throws NotCompliantMBeanException
    {
      super(CacheMBean.class);
    }

    @Override
    public String getName()
    {
      return "CWorkspace(" + workspace.getWorkspaceId() + ")";
    }

    @Override
    public long getMaxSize()
    {
      return cacheMaxSize;
    }

    @Override
    public long getSize()
    {
      return cNodeMap.size();
    }

    @Override
    public String getDetails()
    {
      return "cNodeMapSize=" + getSize() + "/" + getMaxSize() + "," +
        "childrenMapSize=" + childrenMap.size() + "," +
        "parentsMapSize=" + parentsMap.size();
    }

    @Override
    public void clear()
    {
      CWorkspace.this.clear();
    }

    @Override
    public void update()
    {
      purge();
      purgeParentChildren(System.currentTimeMillis());
    }

  }

}
