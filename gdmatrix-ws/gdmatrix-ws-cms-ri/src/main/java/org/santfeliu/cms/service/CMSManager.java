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
package org.santfeliu.cms.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import org.apache.commons.lang.StringUtils;
import org.matrix.cms.CMSConstants;
import org.matrix.cms.CMSManagerPort;
import org.matrix.cms.Node;
import org.matrix.cms.NodeFilter;
import org.matrix.cms.Property;
import org.matrix.cms.Workspace;
import org.matrix.cms.WorkspaceFilter;
import org.matrix.cms.NodeChange;
import org.matrix.cms.NodeChangeType;
import org.santfeliu.jpa.JPA;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.cms.store.JPQLFindNodesPropertiesQueryBuilder;
import org.santfeliu.cms.store.JPQLFindNodesQueryBuilder;
import org.santfeliu.jpa.JPAQuery;
import org.santfeliu.security.User;
import org.santfeliu.security.UserCache;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;
import org.santfeliu.util.audit.Auditor;
import org.santfeliu.ws.WSUtils;

/**
 *
 * @author lopezrj
 */
@WebService(endpointInterface = "org.matrix.cms.CMSManagerPort")
@HandlerChain(file="handlers.xml")
@JPA
public class CMSManager implements CMSManagerPort
{
  @Resource
  WebServiceContext wsContext;

  @PersistenceContext
  public EntityManager entityManager;

  protected static final Logger log = Logger.getLogger("CMS");

  private static final long DB_REMOVED_NODE_LIFETIME =
    getDBRemovedNodeLifetime();
  private static final long DB_REMOVE_TIME =
    getDBRemoveTime();
  private static long lastDBRemoveMillis = System.currentTimeMillis();

  @Override
  public Workspace loadWorkspace(String workspaceId)
  {
    log.log(Level.INFO, "loadWorkspace {0}", new Object[]{workspaceId});
    if (workspaceId == null)
      throw new WebServiceException("cms:WORKSPACEID_IS_MANDATORY");

    WSEndpoint endpoint = getWSEndpoint();
    DBWorkspace dbWorkspace = entityManager.find(DBWorkspace.class, 
      endpoint.toLocalId(Workspace.class, workspaceId));
    if (dbWorkspace == null)
      throw new WebServiceException("cms:WORKSPACE_NOT_FOUND");
    Workspace workspace = new Workspace();
    dbWorkspace.copyTo(workspace, endpoint);
    return workspace;
  }

  @Override
  public Workspace storeWorkspace(Workspace workspace)
  {
    String workspaceId = workspace.getWorkspaceId();
    log.log(Level.INFO, "storeWorkspace {0}", new Object[]{workspaceId});
    WSEndpoint endpoint = getWSEndpoint();
    if (workspaceId == null) //insert
    {
      User user = UserCache.getUser(wsContext);
      String localUserId = endpoint.toLocalId(User.class, user.getUserId());
      DBWorkspace dbWorkspace = new DBWorkspace(workspace, endpoint);
      Auditor.auditCreation(dbWorkspace, localUserId);
      entityManager.persist(dbWorkspace);
      dbWorkspace.copyTo(workspace, endpoint);
    }
    else //update
    {
      String localWorkspaceId =
        endpoint.toLocalId(Workspace.class, workspaceId);
      DBWorkspace dbWorkspace = entityManager.find(DBWorkspace.class,
        localWorkspaceId);
      if (dbWorkspace == null)
        throw new WebServiceException("cms:WORKSPACE_NOT_FOUND");
      String localRefWorkspaceId = endpoint.toLocalId(Workspace.class,
        workspace.getRefWorkspaceId());
      if (isWorkspaceCycleDetected(localWorkspaceId, localRefWorkspaceId))
        throw new WebServiceException("cms:WORKSPACE_CYCLE_DETECTED");
      dbWorkspace.copyFrom(workspace, endpoint);
      entityManager.merge(dbWorkspace);
    }
    return workspace;
  }

  @Override
  public Workspace copyWorkspace(String fromWorkspaceId, String toWorkspaceId)
  {
    log.log(Level.INFO, "copyWorkspace {0} {1}", new Object[]{fromWorkspaceId,
      toWorkspaceId});
    if (fromWorkspaceId == null)
      throw new WebServiceException("cms:FROMWORKSPACEID_IS_MANDATORY");

    WSEndpoint endpoint = getWSEndpoint();
    Workspace workspace = new Workspace();
    String localFromWorkspaceId = endpoint.toLocalId(Workspace.class,
      fromWorkspaceId);
    DBWorkspace dbFromWorkspace = entityManager.find(DBWorkspace.class,
      localFromWorkspaceId);
    if (dbFromWorkspace == null)
      throw new WebServiceException("cms:WORKSPACE_NOT_FOUND");
    String localToWorkspaceId = null;
    DBWorkspace newDBWorkspace = null;
    if (toWorkspaceId == null) //Clone workspace
    {
      dbFromWorkspace.copyTo(workspace, endpoint);
      newDBWorkspace = new DBWorkspace(workspace, endpoint);
      newDBWorkspace.setWorkspaceId(null);
      newDBWorkspace.setName("New workspace");
      newDBWorkspace.setRefWorkspaceId(dbFromWorkspace.getWorkspaceId());
      User user = UserCache.getUser(wsContext);
      String localUserId = endpoint.toLocalId(User.class, user.getUserId());
      Auditor.auditCreation(newDBWorkspace, localUserId);
      entityManager.persist(newDBWorkspace);
      localToWorkspaceId = newDBWorkspace.getWorkspaceId();
    }
    else //Delete workspace
    {
      localToWorkspaceId = endpoint.toLocalId(Workspace.class, toWorkspaceId);
      newDBWorkspace = entityManager.find(DBWorkspace.class,
        localToWorkspaceId);
      if (newDBWorkspace == null)
        throw new WebServiceException("cms:WORKSPACE_NOT_FOUND");
      if (isWorkspaceCycleDetected(localToWorkspaceId, localFromWorkspaceId))
        throw new WebServiceException("cms:WORKSPACE_CYCLE_DETECTED");
      newDBWorkspace.setRefWorkspaceId(dbFromWorkspace.getWorkspaceId());
      entityManager.merge(newDBWorkspace);
      Query query = entityManager.createNamedQuery("removeWorkspaceProperties");
      query.setParameter("workspaceId", localToWorkspaceId);
      query.executeUpdate();
      query = entityManager.createNamedQuery("removeWorkspaceNodes");
      query.setParameter("workspaceId", localToWorkspaceId);
      query.executeUpdate();
    }
    Query query = entityManager.createNativeQuery("insert into " +
      "cms_node(workspaceid,nodeid,parentnodeid,idx,name,changedt," +
      "changeuserid,syncdt,syncuserid,path) " +
      "(select ?,nodeid,parentnodeid,idx,name,changedt," +
      "changeuserid,syncdt,syncuserid,path from cms_node " +
      "where workspaceid=?)");
    query.setParameter(1, localToWorkspaceId);
    query.setParameter(2, localFromWorkspaceId);
    query.executeUpdate();
    query = entityManager.createNativeQuery("insert into " +
      "cms_property(workspaceid,nodeid,name,idx,value) " +
      "(select ?,nodeid,name,idx,value " +
      "from cms_property where workspaceid=?)");
    query.setParameter(1, localToWorkspaceId);
    query.setParameter(2, localFromWorkspaceId);
    query.executeUpdate();
    newDBWorkspace.copyTo(workspace, endpoint);
    return workspace;
  }

  @Override
  public boolean removeWorkspace(String workspaceId)
  {
    log.log(Level.INFO, "removeWorkspace {0}", new Object[]{workspaceId});
    if (workspaceId == null)
      throw new WebServiceException("cms:WORKSPACEID_IS_MANDATORY");

    WSEndpoint endpoint = getWSEndpoint();
    DBWorkspace dbWorkspace = null;
    String localWorkspaceId = endpoint.toLocalId(Workspace.class, workspaceId);
    try
    {
      dbWorkspace = entityManager.getReference(DBWorkspace.class, 
        localWorkspaceId);
    }
    catch (EntityNotFoundException ex)
    {
      return false;
    }
    Query query = entityManager.createNamedQuery("removeWorkspaceProperties");
    query.setParameter("workspaceId", localWorkspaceId);
    query.executeUpdate();
    query = entityManager.createNamedQuery("removeWorkspaceNodes");
    query.setParameter("workspaceId", localWorkspaceId);
    query.executeUpdate();
    entityManager.remove(dbWorkspace);
    return true;
  }

  @Override
  public int countWorkspaces(WorkspaceFilter filter)
  {
    log.log(Level.INFO, "countWorkspaces");
    WSEndpoint endpoint = getWSEndpoint();
    Query query = entityManager.createNamedQuery("countWorkspaces");
    applyWorkspaceFilter(query, filter, endpoint);
    Number number = (Number)query.getSingleResult();
    return number.intValue();
  }

  @Override
  public List<Workspace> findWorkspaces(WorkspaceFilter filter) 
  {
    log.log(Level.INFO, "findWorkspaces");
    WSEndpoint endpoint = getWSEndpoint();
    ArrayList<Workspace> workspaceList = new ArrayList<Workspace>();
    Query query = entityManager.createNamedQuery("findWorkspaces");
    applyWorkspaceFilter(query, filter, endpoint);
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
    List<DBWorkspace> dbWorkspaceList = query.getResultList();
    for (DBWorkspace dbWorkspace : dbWorkspaceList)
    {
      Workspace workspace = new Workspace();
      dbWorkspace.copyTo(workspace, endpoint);
      workspaceList.add(workspace);
    }
    return workspaceList;
  }

  @Override
  public Node loadNode(String workspaceId, String nodeId)
  {
    log.log(Level.INFO, "loadNode workspace: {0}, node: {1}",
      new Object[]{workspaceId, nodeId});
    if (workspaceId == null)
      throw new WebServiceException("cms:WORKSPACEID_IS_MANDATORY");
    if (StringUtils.isBlank(nodeId))
      throw new WebServiceException("cms:NODEID_IS_MANDATORY");

    WSEndpoint endpoint = getWSEndpoint();
    String localWorkspaceId = endpoint.toLocalId(Workspace.class, workspaceId);
    String localNodeId = endpoint.toLocalId(Node.class, nodeId);
    localNodeId = String.valueOf(new Integer(localNodeId));    
    DBNodePK dbNodePK = new DBNodePK(localWorkspaceId, localNodeId);
    DBNode dbNode = entityManager.find(DBNode.class, dbNodePK);
    
    if (dbNode == null || dbNode.getIndex() == -1)
    {
      throw new WebServiceException("cms:NODE_NOT_FOUND");
    }

    Node node = new Node();
    dbNode.copyTo(node, endpoint);
    List<DBProperty> dbPropertyList = dbNode.getProperties();
    node.getProperty().addAll(getPropertyList(dbPropertyList));
    return node;
  }

  @Override
  public Node storeNode(Node node)
  {
    try
    {
      String workspaceId = node.getWorkspaceId();
      String nodeId = node.getNodeId();
      log.log(Level.INFO, "storeNode workspace: {0}, node: {1}",
        new Object[]{workspaceId, nodeId});
      if (workspaceId == null)
        throw new WebServiceException("cms:WORKSPACEID_IS_MANDATORY");
      if (node.getIndex() < 0)
        throw new WebServiceException("cms:INVALID_NODE_INDEX");

      User user = UserCache.getUser(wsContext);
      WSEndpoint endpoint = getWSEndpoint();
      String localWorkspaceId = endpoint.toLocalId(Workspace.class, workspaceId);
      String localParentNodeId = endpoint.toLocalId(Node.class,
        node.getParentNodeId());
      String localUserId = endpoint.toLocalId(User.class, user.getUserId());

      DBNode dbNode = null;
      if (nodeId == null) //insert
      {
        if (localParentNodeId != null)
        {
          shiftNodesRight(localWorkspaceId, localParentNodeId, node.getIndex(),
            localUserId);
        }
        dbNode = new DBNode(node, endpoint);
        Auditor.auditChange(dbNode, localUserId);
        entityManager.persist(dbNode);
        try
        {
          entityManager.flush();
        }
        catch (Exception ex)
        {
          throw new WebServiceException("cms:NODE_CREATION_ERROR");
        }
        String newPath = getParentPath(dbNode) + dbNode.getNodeId() + "/";
        dbNode.setPath(newPath);
        dbNode.copyTo(node, endpoint);
        insertNodeProperties(node, endpoint);
      }
      else //update
      {
        String localNodeId = endpoint.toLocalId(Node.class, nodeId);
        DBNodePK dbNodePK = new DBNodePK(localWorkspaceId, localNodeId);
        dbNode = entityManager.find(DBNode.class, dbNodePK);
        if (dbNode == null) throw new WebServiceException("cms:NODE_NOT_FOUND");
        String oldParentNodeId = dbNode.getParentNodeId();
        String oldPath = dbNode.getPath();
        boolean updatePath = mustUpdatePath(oldParentNodeId, localParentNodeId);
        if (isNodeCycleDetected(localWorkspaceId, localNodeId,
          localParentNodeId))
            throw new WebServiceException("cms:NODE_CYCLE_DETECTED");
        if (isShiftRequired(dbNode.getParentNodeId(),
          dbNode.getIndex(), localParentNodeId, node.getIndex()))
        {
          shiftNodesRight(localWorkspaceId, localParentNodeId, node.getIndex(),
            localUserId);
        }
        dbNode.copyFrom(node, endpoint);        
        Auditor.auditChange(dbNode, localUserId);
        entityManager.merge(dbNode);
        absUpdateNodeProperties(node, endpoint);
        String newPath = getParentPath(dbNode) + localNodeId + "/";
        if (updatePath)
        {
          updateNodePath(localWorkspaceId, oldPath, newPath);
        }
        dbNode.copyTo(node, endpoint);
      }
      return node;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "storeNode", ex);
      throw new WebServiceException(ex);
    }
  }

  @Override
  public boolean removeNode(String workspaceId, String nodeId)
  {
    try
    {
      log.log(Level.INFO, "removeNode workspace: {0}, node {1}",
        new Object[]{workspaceId, nodeId});
      if (workspaceId == null)
        throw new WebServiceException("cms:WORKSPACEID_IS_MANDATORY");
      if (nodeId == null)
        throw new WebServiceException("cms:NODEID_IS_MANDATORY");

      WSEndpoint endpoint = getWSEndpoint();
      String localWorkspaceId =
        endpoint.toLocalId(Workspace.class, workspaceId);
      removeDBNodes(localWorkspaceId);

      User user = UserCache.getUser(wsContext);
      String localNodeId = endpoint.toLocalId(Node.class, nodeId);
      String localUserId = endpoint.toLocalId(User.class, user.getUserId());

      DBNodePK dbNodePK = new DBNodePK(localWorkspaceId, localNodeId);
      DBNode dbNode = entityManager.find(DBNode.class, dbNodePK);
      if (dbNode == null) return false;
      if (dbNode.getParentNodeId() != null)
      {
        String localParentNodeId = endpoint.toLocalId(Node.class,
          dbNode.getParentNodeId());
        shiftNodesLeft(localWorkspaceId, localParentNodeId, dbNode.getIndex(),
          localUserId);
      }
      removeNodeAndDescendants(localWorkspaceId, localNodeId, localUserId);
      return true;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "removeNode", ex);
      throw new WebServiceException(ex);
    }
  }

  @Override
  public int countNodes(NodeFilter filter)
  {
    try
    {
      log.log(Level.INFO, "countNodes");
      WSEndpoint endpoint = getWSEndpoint();

      JPQLFindNodesQueryBuilder queryBuilder = new JPQLFindNodesQueryBuilder();
      queryBuilder.setCounterQuery(true);
      queryBuilder.setFilter(getLocalNodeFilter(filter, endpoint));
      Query query = queryBuilder.getQuery(entityManager);
      Number number = (Number)query.getSingleResult();
      return number.intValue();
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "countNodes", ex);
      throw new WebServiceException(ex);
    }
  }

  @Override
  public List<Node> findNodes(NodeFilter filter)
  {
    try
    {
      log.log(Level.INFO, "findNodes");
      WSEndpoint endpoint = getWSEndpoint();

      List<Node> nodeList = new ArrayList<Node>();
      Map<String, Node> auxNodeMap = new HashMap<String, Node>();
      Map<String, Map> workspaceMap = new HashMap<String, Map>();
      Query query = null;
      if (isExplorationQuery(filter))
      {
        String workspaceId = filter.getWorkspaceId().get(0);
        String parentNodeId = filter.getParentNodeId().get(0);
        if (CMSConstants.NULL_PARENT_SYMBOL.equals(parentNodeId))
        {
          query = entityManager.createNamedQuery("exploreRoots");
        }
        else
        {
          query = entityManager.createNamedQuery("exploreNode");
          String localParentNodeId =
            endpoint.toLocalId(Node.class, parentNodeId);
          query.setParameter("parentNodeId", localParentNodeId);
        }
        String localWorkspaceId =
          endpoint.toLocalId(Workspace.class, workspaceId);
        query.setParameter("workspaceId", localWorkspaceId);
        query.setFirstResult(filter.getFirstResult());
        query.setMaxResults(filter.getMaxResults());
      }
      else
      {
        JPQLFindNodesQueryBuilder queryBuilder =
          new JPQLFindNodesQueryBuilder();
        queryBuilder.setCounterQuery(false);
        queryBuilder.setFilter(getLocalNodeFilter(filter, endpoint));
        query = queryBuilder.getQuery(entityManager);
      }
      List<DBNode> dbNodeList = query.getResultList();
      List<String> localNodeIdList = new ArrayList<String>();
      for (DBNode dbNode : dbNodeList)
      {
        String localWorkspaceId = dbNode.getWorkspaceId();
        String localNodeId = dbNode.getNodeId();
        if (!localNodeIdList.contains(localNodeId))
        {
          localNodeIdList.add(localNodeId);  
        }
        if (!workspaceMap.containsKey(localWorkspaceId))
        {
          workspaceMap.put(localWorkspaceId, new HashMap<String, List>());
        }
        Map<String, List> nodeMap = workspaceMap.get(localWorkspaceId);
        if (!nodeMap.containsKey(localNodeId))
        {
          nodeMap.put(localNodeId, new ArrayList<DBProperty>());
        }
        Node node = new Node();
        dbNode.copyTo(node, endpoint);
        nodeList.add(node);
        String pk = localWorkspaceId + ";" + localNodeId;
        auxNodeMap.put(pk, node);
      }
      if (localNodeIdList.size() > 0)
      {
        List<String> localWorkspaceIdList = 
          endpoint.toLocalIds(Workspace.class, filter.getWorkspaceId());
        JPQLFindNodesPropertiesQueryBuilder queryBuilder =
          new JPQLFindNodesPropertiesQueryBuilder();
        queryBuilder.setWorkspaceIdList(localWorkspaceIdList);
        queryBuilder.setNodeIdList(localNodeIdList);
        query = queryBuilder.getQuery(entityManager);
        List<DBProperty> dbPropertyList = query.getResultList();
        for (DBProperty dbProperty : dbPropertyList)
        {
          String localWorkspaceId = dbProperty.getWorkspaceId();
          String localNodeId = dbProperty.getNodeId();
          Map<String, List> nodeMap = workspaceMap.get(localWorkspaceId);
          if (nodeMap != null)
          {
            List<DBProperty> nodeDBPropertyList =
              (List<DBProperty>)nodeMap.get(localNodeId);
            if (nodeDBPropertyList != null) nodeDBPropertyList.add(dbProperty);
          }
        }
        for (String localWorkspaceId : workspaceMap.keySet())
        {
          Map<String, List> nodeMap = workspaceMap.get(localWorkspaceId);
          for (String localNodeId : nodeMap.keySet())
          {
            List<DBProperty> nodeDBPropertyList =
              (List<DBProperty>)nodeMap.get(localNodeId);
            List<Property> nodePropertyList = getPropertyList(nodeDBPropertyList);
            String pk = localWorkspaceId + ";" + localNodeId;
            Node node = auxNodeMap.get(pk);
            if (node != null)
            {
              node.getProperty().addAll(nodePropertyList);
            }
          }
        }
      }
      return nodeList;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "findNodes", ex);
      throw new WebServiceException(ex);
    }
  }

  @Override
  public List<Node> findModifiedNodes(String workspaceId, String dateTime)
  {
    log.log(Level.INFO, "findModifiedNodes workspace: {0}, dateTime: {1}",
      new Object[]{workspaceId, dateTime});
    if (workspaceId == null)
      throw new WebServiceException("cms:WORKSPACEID_IS_MANDATORY");
    if (dateTime == null)
      throw new WebServiceException("cms:DATETIME_IS_MANDATORY");
    WSEndpoint endpoint = getWSEndpoint();
    String localWorkspaceId = endpoint.toLocalId(Workspace.class, workspaceId);
    List<Node> nodeList = new ArrayList<Node>();
    nodeList.add(getClockNode());
    Query query = entityManager.createNamedQuery("findModifiedNodes");
    query.setParameter("workspaceId", localWorkspaceId);
    query.setParameter("dateTime", dateTime);
    List<DBNode> dbNodeList = query.getResultList();
    for (DBNode dbNode : dbNodeList)
    {
      Node node = new Node();
      dbNode.copyTo(node, endpoint);
      nodeList.add(node);
      //Properties are not returned
    }
    return nodeList;
  }

  @Override
  public List<NodeChange> findNodeChanges(String fromWorkspaceId,
    String toWorkspaceId, String baseNodeId)
  {
    log.log(Level.INFO, "findNodeChanges fromWorkspace: {0}, toWorkspace: {1}",
      new Object[]{fromWorkspaceId, toWorkspaceId});
    if (fromWorkspaceId == null)
      throw new WebServiceException("cms:FROMWORKSPACEID_IS_MANDATORY");
    if (toWorkspaceId == null)
      throw new WebServiceException("cms:TOWORKSPACEID_IS_MANDATORY");
    WSEndpoint endpoint = getWSEndpoint();
    String localFromWorkspaceId = endpoint.toLocalId(Workspace.class,
      fromWorkspaceId);
    String localToWorkspaceId = endpoint.toLocalId(Workspace.class,
      toWorkspaceId);

    List<NodeChange> nodeChangeList = new ArrayList<NodeChange>();
    nodeChangeList.addAll(findCreatedNodes(localFromWorkspaceId,
      localToWorkspaceId, baseNodeId, endpoint));
    nodeChangeList.addAll(findRemovedNodes(localFromWorkspaceId,
      localToWorkspaceId, baseNodeId, endpoint));
    nodeChangeList.addAll(findUpdatedNodes(localFromWorkspaceId,
      localToWorkspaceId, baseNodeId, endpoint));
//    if (baseNodeId != null && baseNodeId.size() > 0)
//    {
//      String localBaseNodeId = endpoint.toLocalId(Node.class,
//        baseNodeId);
//      nodeChangeList = filterNodeChangeList(nodeChangeList, localBaseNodeId,
//        localFromWorkspaceId);
//    }
    return sortNodeChangeListByDepth(nodeChangeList);
  }

  @Override
  public int syncNodes(String fromWorkspaceId, String toWorkspaceId,
    List<NodeChange> nodeChanges)
  {
    try
    {
      log.log(Level.INFO, "syncNodes fromWorkspace: {0}, toWorkspace: {1}",
        new Object[]{fromWorkspaceId, toWorkspaceId});
      if (fromWorkspaceId == null)
        throw new WebServiceException("cms:FROMWORKSPACEID_IS_MANDATORY");
      if (toWorkspaceId == null)
        throw new WebServiceException("cms:TOWORKSPACEID_IS_MANDATORY");

      User user = UserCache.getUser(wsContext);
      WSEndpoint endpoint = getWSEndpoint();
      int syncNodesCount = 0;
      String localToWorkspaceId = endpoint.toLocalId(Workspace.class,
        toWorkspaceId);
      String localUserId = endpoint.toLocalId(User.class, user.getUserId());

      List<String> removedNodeIdList = new ArrayList<String>();
      List<NodeChange> sortedNodeChangeList =
        sortNodeChangeListByDepth(nodeChanges);
      for (NodeChange nodeChange : sortedNodeChangeList)
      {
        Node node = nodeChange.getNode();
        if (nodeChange.getType() == NodeChangeType.CREATED)
        {
          String localNodeId =
            endpoint.toLocalId(Node.class, node.getNodeId());
          //Node existed but was deleted
          if (isNodeInDB(localToWorkspaceId, localNodeId))
          {
            nodeChange.setType(NodeChangeType.UPDATED);
          }
          else
          {
            node.setWorkspaceId(toWorkspaceId);
            DBNode dbNode = new DBNode(node, endpoint);
            entityManager.persist(dbNode);
            try
            {
              entityManager.flush();
            }
            catch (Exception ex)
            {
              throw new WebServiceException("cms:NODE_CREATION_ERROR");
            }
            String newPath = getParentPath(dbNode) + dbNode.getNodeId() + "/";
            dbNode.setPath(newPath);
            auditSync(localToWorkspaceId, stringToList(dbNode.getNodeId()),
              localUserId);
            insertNodeProperties(node, endpoint);
          }
        }
        if (nodeChange.getType() == NodeChangeType.REMOVED)
        {
          removedNodeIdList.add(node.getNodeId());
        }
        if (nodeChange.getType() == NodeChangeType.UPDATED)
        {
          String localNodeId =
            endpoint.toLocalId(Node.class, node.getNodeId());
          DBNodePK dbNodePK = new DBNodePK(localToWorkspaceId, localNodeId);
          DBNode oldDBNode = entityManager.find(DBNode.class, dbNodePK);
          if (oldDBNode == null)
            throw new WebServiceException("cms:NODE_NOT_FOUND");
          String oldParentNodeId = oldDBNode.getParentNodeId();
          String oldPath = oldDBNode.getPath();
          node.setWorkspaceId(toWorkspaceId);
          DBNode dbNode = new DBNode(node, endpoint);
          dbNode.setPath(oldPath);
          boolean updatePath =
            mustUpdatePath(oldParentNodeId, dbNode.getParentNodeId());
          entityManager.merge(dbNode);
          auditSync(localToWorkspaceId, stringToList(dbNode.getNodeId()),
            localUserId);
          absUpdateNodeProperties(node, endpoint);
          String newPath = getParentPath(dbNode) + dbNode.getNodeId() + "/";
          if (updatePath)
          {
            updateNodePath(dbNode.getWorkspaceId(), oldPath, newPath);
          }
        }
        syncNodesCount++;
      }
      //Remove
      if (!removedNodeIdList.isEmpty())
      {
        List<String> localRemovedNodeIdList = endpoint.toLocalIds(Node.class,
          removedNodeIdList);
        removeNodeList(localToWorkspaceId, localRemovedNodeIdList, localUserId,
          true);
      }
      return syncNodesCount;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "syncNodes", ex);
      throw new WebServiceException(ex);
    }
  }

  //PRIVATE METHODS: All id fields are local

  private boolean mustUpdatePath(String oldParentNodeId, String newParentNodeId)
  {
    boolean updatePath = true;
    if (oldParentNodeId == null && newParentNodeId == null)
    {
      updatePath = false;
    }
    else if (oldParentNodeId != null && newParentNodeId != null)
    {
      updatePath = !(oldParentNodeId.equals(newParentNodeId));
    }
    return updatePath;
  }

  private String getParentPath(DBNode dbNode)
  {
    String localParentPath = "/";
    String localParentNodeId = dbNode.getParentNodeId();
    String localWorkspaceId = dbNode.getWorkspaceId();
    if (localParentNodeId != null)
    {
      try
      {
        localParentPath = findNodePath(localWorkspaceId, localParentNodeId);
      }
      catch (NoResultException ex)
      {
        throw new WebServiceException("cms:PATH_NOT_FOUND");
      }
    }
    return localParentPath;
  }
  
  private void removeNodeAndDescendants(String workspaceId, String nodeId,
    String userId) throws Exception
  {
    Date date = new Date();
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    String dateTime = df.format(date);
    JPAQuery query = new JPAQuery(
      entityManager.createNamedQuery("markNodeAndDescendantsAsRemoved"));
    query.setParameter("workspaceId", workspaceId);
    query.setParameter("nodeId", nodeId);
    query.setParameter("changeDateTime", dateTime);
    query.setParameter("changeUserId", userId);
    query.executeUpdate();
  }

  private void removeNodeList(String workspaceId, List<String> nodeIdList,
    String userId, boolean auditSync) throws Exception
  {
    //String nodeIdString = listToString(nodeIdList);
    Date date = new Date();
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    String dateTime = df.format(date);
    JPAQuery query = new JPAQuery(
      entityManager.createNamedQuery("markNodesAsRemoved"));
    query.setParameter("workspaceId", workspaceId);
    query.setIdParameter("nodeId", nodeIdList);
    query.setParameter("changeDateTime", dateTime);
    query.setParameter("changeUserId", userId);
    query.executeUpdate();
    if (auditSync)
    {
      auditSync(workspaceId, nodeIdList, userId);
    }
  }

  private void auditSync(String workspaceId, List<String> nodeIdList,
    String userId) throws Exception
  {
    //String nodeIdString = listToString(nodeIdList);
    Date date = new Date();
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    String dateTime = df.format(date);
    JPAQuery query = new JPAQuery(entityManager.createNamedQuery("auditSync"));
    query.setParameter("workspaceId", workspaceId);
    query.setIdParameter("nodeId", nodeIdList);
    query.setParameter("syncDateTime", dateTime);
    query.setParameter("syncUserId", userId);
    query.executeUpdate();
  }

  private Node getClockNode()
  {
    Node node = new Node();
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    String dateTime = df.format(new Date());
    node.setChangeDateTime(dateTime);
    return node;
  }

  private void applyWorkspaceFilter(Query query, WorkspaceFilter filter,
    WSEndpoint endpoint)
  {
    List<String> localWorkspaceIdList = endpoint.toLocalIds(Workspace.class,
      filter.getWorkspaceId());
    query.setParameter("workspaceId", listToString(localWorkspaceIdList));
    query.setParameter("description", 
      conditionalUpperCase(filter.getDescription(), false));
    query.setParameter("name", conditionalUpperCase(filter.getName(), false));
  }

  //TODO Use WSEndpoint toLocal method
  private NodeFilter getLocalNodeFilter(NodeFilter filter, WSEndpoint endpoint)
    throws Exception
  {
    NodeFilter result = new NodeFilter();
    result.setChangeDateTime1(filter.getChangeDateTime1());
    result.setChangeDateTime2(filter.getChangeDateTime2());
    result.setFirstResult(filter.getFirstResult());
    result.setMaxResults(filter.getMaxResults());
    result.setName(filter.getName());
    result.setPropertyCaseSensitive(filter.isPropertyCaseSensitive());
    result.getProperty().addAll(filter.getProperty());
    result.getWorkspaceId().addAll(endpoint.toLocalIds(Workspace.class,
      filter.getWorkspaceId()));
    result.getNodeId().addAll(endpoint.toLocalIds(Node.class,
      filter.getNodeId()));
    result.getParentNodeId().addAll(endpoint.toLocalIds(Node.class,
      filter.getParentNodeId()));
    result.getChangeUserId().addAll(endpoint.toLocalIds(User.class,
      filter.getChangeUserId()));
    return result;
  }

  private String conditionalUpperCase(String value, boolean caseSensitive)
  {
    if (value == null || value.length() == 0)
    {
      return null;
    }
    else if (caseSensitive)
    {
      return value;
    }
    else
    {
      return value.toUpperCase();
    }
  }

  private String listToString(List<String> list)
  {
    String result = TextUtils.collectionToString(list, ",");
    if (result != null) result = "," + result + ",";
    return result;
  }

  private List<String> stringToList(String s)
  {
    List<String> result = new ArrayList<String>();
    String[] sArray = s.split(",");
    for (String item : sArray)
    {
      if (item.trim().length() > 0)
      {
        result.add(item.trim());
      }
    }
    return result;
  }

  private void absUpdateNodeProperties(Node node, WSEndpoint endpoint)
    throws Exception
  {
    String workspaceId = endpoint.toLocalId(Workspace.class,
      node.getWorkspaceId());
    List<String> localWorkspaceIdList = new ArrayList<String>();
    localWorkspaceIdList.add(workspaceId);
    String nodeId = endpoint.toLocalId(Node.class, node.getNodeId());
    List<String> localNodeIdList = new ArrayList<String>();
    localNodeIdList.add(nodeId);

    JPQLFindNodesPropertiesQueryBuilder queryBuilder =
      new JPQLFindNodesPropertiesQueryBuilder();
    queryBuilder.setWorkspaceIdList(localWorkspaceIdList);
    queryBuilder.setNodeIdList(localNodeIdList);
    Query query = queryBuilder.getQuery(entityManager);
    List<DBProperty> oldDBPropertyList = query.getResultList();
    List<DBProperty> newDBPropertyList = getDBPropertyList(node.getProperty(),
      workspaceId, nodeId);
    List<DBProperty> dbPropertiesToRemove = new ArrayList<DBProperty>();
    dbPropertiesToRemove.addAll(oldDBPropertyList);
    List<DBProperty> dbPropertiesToInsert = new ArrayList<DBProperty>();
    dbPropertiesToInsert.addAll(newDBPropertyList);
    for (DBProperty oldDBProperty : oldDBPropertyList)
    {
      boolean found = false;
      for (int i = 0; i < newDBPropertyList.size() && !found; i++)
      {
        DBProperty newDBProperty = newDBPropertyList.get(i);
        if (oldDBProperty.equals(newDBProperty))
        {
          dbPropertiesToRemove.remove(oldDBProperty);
          dbPropertiesToInsert.remove(newDBProperty);
          found = true;
        }
      }
    }

    for (DBProperty dbProperty : dbPropertiesToRemove)
    {
      query = entityManager.createNamedQuery("removeProperty");
      query.setParameter("workspaceId", dbProperty.getWorkspaceId());
      query.setParameter("nodeId", dbProperty.getNodeId());
      query.setParameter("name", dbProperty.getName());
      query.setParameter("index", dbProperty.getIndex());
      query.executeUpdate();
    }
    for (DBProperty dbProperty : dbPropertiesToInsert)
    {
      entityManager.persist(dbProperty);
    }
  }

  private void insertNodeProperties(Node node, WSEndpoint endpoint)
  {
    String workspaceId = endpoint.toLocalId(Workspace.class,
      node.getWorkspaceId());
    String nodeId = endpoint.toLocalId(Node.class, node.getNodeId());
    List<DBProperty> dbPropertyList = getDBPropertyList(node.getProperty(),
      workspaceId, nodeId);
    for (DBProperty dbProperty : dbPropertyList)
    {
      entityManager.persist(dbProperty);
    }
  }

  private List<Property> getPropertyList(List<DBProperty> dbPropertyList)
  {
    List<Property> propertyList = new ArrayList<Property>();
    Map<String, List<DBProperty>> dbPropertyMap =
      new HashMap<String, List<DBProperty>>();
    for (DBProperty dbProperty : dbPropertyList)
    {
      String name = dbProperty.getName();
      if (!dbPropertyMap.containsKey(name))
      {
        dbPropertyMap.put(name, new ArrayList<DBProperty>());
      }
      dbPropertyMap.get(name).add(dbProperty);
    }
    for (String name : dbPropertyMap.keySet())
    {
      Property property = new Property();
      property.setName(name);
      while (!dbPropertyMap.get(name).isEmpty())
      {
        int minIndex = Integer.MAX_VALUE;
        DBProperty minDbProperty = null;
        for (DBProperty dbProperty : dbPropertyMap.get(name))
        {
          if (dbProperty.getIndex() < minIndex)
          {
            minIndex = dbProperty.getIndex();
            minDbProperty = dbProperty;
          }
        }
        property.getValue().add(minDbProperty.getValue());
        dbPropertyMap.get(name).remove(minDbProperty);
      }
      propertyList.add(property);
    }
    return propertyList;
  }

  private List<DBProperty> getDBPropertyList(List<Property> propertyList,
    String localWorkspaceId, String localNodeId)
  {
    List<DBProperty> dbPropertyList = new ArrayList<DBProperty>();
    for (Property property : propertyList)
    {
      int index = 1;
      String name = property.getName();
      for (String value : property.getValue())
      {
        DBProperty dbProperty = new DBProperty();
        dbProperty.setWorkspaceId(localWorkspaceId);
        dbProperty.setNodeId(localNodeId);
        dbProperty.setName(name);
        dbProperty.setIndex(index++);
        dbProperty.setValue(value);
        dbPropertyList.add(dbProperty);
      }
    }
    return dbPropertyList;
  }

  private void shiftNodesLeft(String workspaceId, String parentNodeId,
    int index, String userId)
  {
    shiftNodes(workspaceId, parentNodeId, index, userId, true);
  }

  private void shiftNodesRight(String workspaceId, String parentNodeId,
    int index, String userId)
  {
    shiftNodes(workspaceId, parentNodeId, index, userId, false);
  }
  
  private void shiftNodes(String workspaceId, String parentNodeId, int index, 
    String userId, boolean left)
  {
    Date date = new Date();
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    String dateTime = df.format(date);
    Query query = entityManager.createNamedQuery("shiftNodes");
    int shift = (left ? -1 : 1);
    query.setParameter("workspaceId", workspaceId);
    query.setParameter("parentNodeId", parentNodeId);
    query.setParameter("index", index);
    query.setParameter("shift", shift);
    query.setParameter("changeDateTime", dateTime);
    query.setParameter("changeUserId", userId);
    query.executeUpdate();
  }

  private WSEndpoint getWSEndpoint()
  {
    String endpointName = WSUtils.getServletAdapter(wsContext).getName();
    return WSDirectory.getInstance().getEndpoint(endpointName);
  }

  private List<NodeChange> findFullNodesToSync(String queryName,
    NodeChangeType nodeChangeType, String fromWorkspaceId,
    String toWorkspaceId, String baseNodeId, WSEndpoint endpoint)
  {
    List<NodeChange> nodeChangeList = new ArrayList<NodeChange>();
    Query query = entityManager.createNamedQuery(queryName);
    query.setParameter("fromWorkspaceId", fromWorkspaceId);
    query.setParameter("toWorkspaceId", toWorkspaceId);
    query.setParameter("baseNodeId", getPathItemSearchExpression(baseNodeId));
    List<Object[]> rowList = query.getResultList();
    List<Node> nodeList = rowListToNodeList(rowList, endpoint);
    for (Node node : nodeList)
    {
      NodeChange nodeChange = new NodeChange();
      nodeChange.setNode(node);
      nodeChange.setType(nodeChangeType);
      nodeChangeList.add(nodeChange);
    }
    return nodeChangeList;
  }

  private List<NodeChange> findCreatedNodes(String fromWorkspaceId,
    String toWorkspaceId, String baseNodeId, WSEndpoint endpoint)
  {
    return findFullNodesToSync("findCreatedNodes", NodeChangeType.CREATED,
      fromWorkspaceId, toWorkspaceId, baseNodeId, endpoint);
  }

  private List<NodeChange> findUpdatedNodes(String fromWorkspaceId,
    String toWorkspaceId, String baseNodeId, WSEndpoint endpoint)
  {
    return findFullNodesToSync("findUpdatedNodes", NodeChangeType.UPDATED,
      fromWorkspaceId, toWorkspaceId, baseNodeId, endpoint);
  }

  private List<NodeChange> findRemovedNodes(String fromWorkspaceId,
    String toWorkspaceId, String baseNodeId, WSEndpoint endpoint)
  {
    List<NodeChange> nodeChangeList = new ArrayList<NodeChange>();
    Query query = entityManager.createNamedQuery("findRemovedNodes");
    query.setParameter("fromWorkspaceId", fromWorkspaceId);
    query.setParameter("toWorkspaceId", toWorkspaceId);
    query.setParameter("baseNodeId", getPathItemSearchExpression(baseNodeId));
    List<DBNode> dbNodeList = query.getResultList();
    for (DBNode dbNode : dbNodeList)
    {
      Node node = new Node();
      dbNode.copyTo(node, endpoint);
      NodeChange nodeChange = new NodeChange();
      nodeChange.setNode(node);
      nodeChange.setType(NodeChangeType.REMOVED);
      nodeChangeList.add(nodeChange);
    }
    return nodeChangeList;
  }

  private List<NodeChange> sortNodeChangeListByDepth(List<NodeChange>
    nodeChangeList)
  {
    List<NodeChange> sortedNodeChangeList = new ArrayList<NodeChange>();
    Map<String, NodeChange> nodeChangeMap = new HashMap<String, NodeChange>();
    for (NodeChange nodeChange : nodeChangeList)
    {
      nodeChangeMap.put(nodeChange.getNode().getNodeId(), nodeChange);
    }
    Set<NodeChange> nodeChangeSet = new HashSet<NodeChange>();
    nodeChangeSet.addAll(nodeChangeList);
    while (!nodeChangeSet.isEmpty())
    {
      int minDepth = Integer.MAX_VALUE;
      NodeChange minNodeChange = null;
      for (NodeChange nodeChange : nodeChangeSet)
      {
        int depth = getDepth(nodeChange.getNode().getNodeId(), nodeChangeMap);
        if (depth < minDepth)
        {
          minDepth = depth;
          minNodeChange = nodeChange;
        }
      }
      sortedNodeChangeList.add(minNodeChange);
      nodeChangeSet.remove(minNodeChange);
    }
    return sortedNodeChangeList;
  }

  private int getDepth(String nodeId, Map<String, NodeChange> nodeChangeMap)
  {
    if (nodeId == null) return 0;
    if (!nodeChangeMap.containsKey(nodeId)) return 0;
    Node node = nodeChangeMap.get(nodeId).getNode();
    return getDepth(node.getParentNodeId(), nodeChangeMap) + 1;
  }

  private String getPathItemSearchExpression(String nodeId)
  {
    return (nodeId == null ? null : "%/" + nodeId + "/%");
  }

  private boolean isShiftRequired(String oldParentNodeId, int oldIndex,
    String newParentNodeId, int newIndex)
  {
    if (newParentNodeId == null)
    {
      return false;
    }
    else
    {
      if (oldParentNodeId == null)
      {
        return true;
      }
      else
      {
        return (!oldParentNodeId.equals(newParentNodeId) ||
          oldIndex != newIndex);
      }
    }
  }

  //This method converts a sorted List<DBNode, DBProperty> to List<Node>.
  private List<Node> rowListToNodeList(List<Object[]> rowList,
    WSEndpoint endpoint)
  {
    ArrayList<Node> nodeList = new ArrayList<Node>();
    String lastWorkspaceId = null;
    String lastNodeId = null;
    Node node = null;
    List<DBProperty> dbPropertyList = new ArrayList<DBProperty>();
    for (Object[] row : rowList)
    {
      DBNode dbNode = (DBNode)row[0];
      DBProperty dbProperty = (DBProperty)row[1];
      if (!dbNode.getWorkspaceId().equals(lastWorkspaceId) ||
        !dbNode.getNodeId().equals(lastNodeId))
      {
        if (node != null)
        {
          node.getProperty().addAll(getPropertyList(dbPropertyList));
          dbPropertyList.clear();
        }
        node = new Node();
        dbNode.copyTo(node, endpoint);
        nodeList.add(node);
        lastWorkspaceId = dbNode.getWorkspaceId();
        lastNodeId = dbNode.getNodeId();
      }
      if (dbProperty != null) dbPropertyList.add(dbProperty);
    }
    if (node != null)
    {
      node.getProperty().addAll(getPropertyList(dbPropertyList));
    }
    return nodeList;
  }

  private boolean isNodeInDB(String workspaceId, String nodeId)
  {
    Query query = entityManager.createNamedQuery("countNode");
    query.setParameter("workspaceId", workspaceId);
    query.setParameter("nodeId", nodeId);
    Number number = (Number)query.getSingleResult();
    return (number.intValue() > 0);
  }

  private String findNodePath(String workspaceId, String nodeId)
  {
    Query query = entityManager.createNamedQuery("findNodePath");
    query.setParameter("workspaceId", workspaceId);
    query.setParameter("nodeId", nodeId);
    return (String)query.getSingleResult();
  }

  private boolean isNodeCycleDetected(String workspaceId, String nodeId,
    String newParentNodeId)
  {
    if (newParentNodeId == null) return false;
    if (newParentNodeId.equals(nodeId))
    {
      return true;
    }
    else
    {
      DBNodePK dbParentNodePK = new DBNodePK(workspaceId, newParentNodeId);
      DBNode dbNewParentNode = entityManager.find(DBNode.class, dbParentNodePK);
      if (dbNewParentNode == null)
        throw new WebServiceException("cms:PARENT_NODE_NOT_FOUND");
      return isNodeCycleDetected(workspaceId, nodeId,
        dbNewParentNode.getParentNodeId());
    }
  }

  private boolean isWorkspaceCycleDetected(String workspaceId,
    String newRefWorkspaceId) //detection of cycles between workspaces
  {
    if (newRefWorkspaceId == null) return false;
    if (newRefWorkspaceId.equals(workspaceId))
    {
      return true;
    }
    else
    {
      DBWorkspace dbNewRefWorkspace = entityManager.find(DBWorkspace.class,
        newRefWorkspaceId);
      if (dbNewRefWorkspace == null)
        throw new WebServiceException("cms:REF_WORKSPACE_NOT_FOUND");
      return isWorkspaceCycleDetected(workspaceId,
        dbNewRefWorkspace.getRefWorkspaceId());
    }
  }

  private boolean isExplorationQuery(NodeFilter filter)
  {
    return filter.getChangeDateTime1() == null &&
      filter.getChangeDateTime2() == null &&
      filter.getChangeUserId().isEmpty() &&
      filter.getName() == null &&
      filter.getNodeId().isEmpty() &&
      filter.getParentNodeId().size() == 1 &&
      filter.getProperty().isEmpty() &&
      filter.getWorkspaceId().size() == 1;
  }

  private void removeDBNodes(String workspaceId) throws Exception
  {
    long nowMillis = System.currentTimeMillis();
    if (mustRemoveDBNodes(nowMillis))
    {
      Query query = entityManager.createNamedQuery("findMarkedAsRemovedNodes");
      query.setParameter("workspaceId", workspaceId);
      List<DBNode> dbNodeList = query.getResultList();
      List<DBNode> auxDBNodeList = new ArrayList<DBNode>();
      for (DBNode dbNode : dbNodeList)
      {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        Date removeDate = df.parse(dbNode.getChangeDateTime());
        long lifeMillis = nowMillis - removeDate.getTime();
        if (lifeMillis > DB_REMOVED_NODE_LIFETIME)
        {
          auxDBNodeList.add(dbNode);
        }
      }
      List<String> nodeIdList = getDBNodesRootList(auxDBNodeList);
      removeNodesAndDescendants(workspaceId, nodeIdList);
      lastDBRemoveMillis = nowMillis;
    }
  }

  private List<String> getDBNodesRootList(List<DBNode> dbNodeList)
  {
    List<String> result = new ArrayList<String>();
    Map<String, String> pathMap = new HashMap<String, String>();
    for (DBNode dbNode : dbNodeList)
    {
      pathMap.put(dbNode.getNodeId(), dbNode.getPath());
    }
    for (String nodeId : pathMap.keySet())
    {
      String[] path = pathMap.get(nodeId).split("/");
      boolean found = false;
      for (int i = 0; i < path.length - 1 && !found; i++)
      {
        String pathItem = path[i];
        if (pathMap.keySet().contains(pathItem))
        {
          found = true;
        }
      }
      if (!found) result.add(nodeId);
    }
    return result;
  }

  private boolean mustRemoveDBNodes(long nowMillis)
  {
    return nowMillis - lastDBRemoveMillis > DB_REMOVE_TIME;
  }

  private void removeNodesAndDescendants(String workspaceId,
    List<String> nodeIdList) throws Exception
  {
    for (String nodeId : nodeIdList)
    {
      //Properties
      Query query =
        entityManager.createNamedQuery("removeNodeAndDescendantsProperties");
      query.setParameter("workspaceId", workspaceId);
      query.setParameter("nodeId", nodeId);
      query.executeUpdate();
      //Nodes
      query = entityManager.createNamedQuery("removeNodeAndDescendants");
      query.setParameter("workspaceId", workspaceId);
      query.setParameter("nodeId", nodeId);
      query.executeUpdate();
    }
  }

  private void updateNodePath(String workspaceId, String oldPath,
    String newPath)
  {
    Query query =
      entityManager.createNamedQuery("updateNodeAndDescendantsPath");
    query.setParameter("newPath", newPath);
    query.setParameter("oldPath", oldPath);
    query.setParameter("oldPathPattern", oldPath + "%");
    query.setParameter("workspaceId", workspaceId);
    query.executeUpdate();
  }

  private static long getDBRemovedNodeLifetime()
  {
    String value = MatrixConfig.getProperty(
      "org.santfeliu.cms.service.CMSManager.dbRemovedNodeLifetime");
    try
    {
      if (value != null)
      {
        return Long.parseLong(value);
      }
    }
    catch (Exception ex)
    {

    }
    return 7 * 24 * 60 * 60 * 1000; //7 days
  }

  private static long getDBRemoveTime()
  {
    String value = MatrixConfig.getProperty(
      "org.santfeliu.cms.service.CMSManager.dbRemoveTime");
    try
    {
      if (value != null)
      {
        return Long.parseLong(value);
      }
    }
    catch (Exception ex)
    {

    }
    return 4 * 24 * 60 * 60 * 1000; //4 days
  }

}
