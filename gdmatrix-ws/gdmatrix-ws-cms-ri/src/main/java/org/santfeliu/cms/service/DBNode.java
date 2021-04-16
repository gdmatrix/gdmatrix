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

import java.util.List;
import org.matrix.cms.Node;
import org.matrix.cms.Workspace;
import org.matrix.security.User;
import org.matrix.util.WSEndpoint;
import org.santfeliu.util.audit.ChangeAuditable;

/**
 *
 * @author lopezrj
 */
public class DBNode extends Node implements ChangeAuditable
{
  private List<DBProperty> properties;  
  private String path;
  
  private DBWorkspace workspace;
  private DBNode parentNode;
  private List<DBNode> children;

  public DBNode()
  {
  }
  
  public DBNode(Node node, WSEndpoint endpoint)
  {
    copyFrom(node, endpoint);
  }

  public List<DBProperty> getProperties()
  {
    return properties;
  }

  public void setProperties(List<DBProperty> properties)
  {
    this.properties = properties;
  }

  public String getPath()
  {
    return path;
  }

  public void setPath(String path)
  {
    this.path = path;
  }

  public DBWorkspace getWorkspace() 
  {
    return workspace;
  }

  public void setWorkspace(DBWorkspace workspace) 
  {
    this.workspace = workspace;
  }

  public DBNode getParentNode() 
  {
    return parentNode;
  }

  public void setParentNode(DBNode parentNode) 
  {
    this.parentNode = parentNode;
  }

  public List<DBNode> getChildren() 
  {
    return children;
  }

  public void setChildren(List<DBNode> children) 
  {
    this.children = children;
  }

  public void copyTo(Node node, WSEndpoint endpoint)
  {
    node.setWorkspaceId(endpoint.toGlobalId(Workspace.class,
      this.getWorkspaceId()));
    node.setNodeId(endpoint.toGlobalId(Node.class, this.getNodeId()));
    node.setParentNodeId(endpoint.toGlobalId(Node.class,
      this.getParentNodeId()));
    node.setIndex(this.getIndex());
    node.setName(this.getName());
    node.setChangeDateTime(this.getChangeDateTime());
    node.setChangeUserId(endpoint.toGlobalId(User.class, 
      this.getChangeUserId()));
    node.setSyncDateTime(this.getSyncDateTime());
    node.setSyncUserId(endpoint.toGlobalId(User.class,
      this.getSyncUserId()));
  }

  public void copyFrom(Node node, WSEndpoint endpoint)
  {
    setWorkspaceId(endpoint.toLocalId(Workspace.class, node.getWorkspaceId()));
    setNodeId(endpoint.toLocalId(Node.class, node.getNodeId()));
    setParentNodeId(endpoint.toLocalId(Node.class, node.getParentNodeId()));
    setIndex(node.getIndex());
    setName(node.getName());
    setChangeDateTime(node.getChangeDateTime());
    setChangeUserId(endpoint.toLocalId(User.class, node.getChangeUserId()));
    setSyncDateTime(node.getSyncDateTime());
    setSyncUserId(endpoint.toLocalId(User.class, node.getSyncUserId()));
  }
}
