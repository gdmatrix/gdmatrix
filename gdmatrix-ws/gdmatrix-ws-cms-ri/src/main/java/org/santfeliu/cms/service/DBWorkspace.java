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

import java.util.Collection;
import org.matrix.cms.Workspace;
import org.matrix.security.User;
import org.matrix.util.WSEndpoint;
import org.santfeliu.util.audit.CreationAuditable;

/**
 *
 * @author lopezrj
 */
public class DBWorkspace extends Workspace implements CreationAuditable
{   
  private DBWorkspace refWorkspace;
  private Collection<DBWorkspace> children;
  
  private Collection<DBNode> nodes;
  
  public DBWorkspace()
  {
  }
  
  public DBWorkspace(Workspace workspace, WSEndpoint endpoint)
  {
    copyFrom(workspace, endpoint);
  }

  public DBWorkspace getRefWorkspace() 
  {
    return refWorkspace;
  }

  public void setRefWorkspace(DBWorkspace refWorkspace) 
  {
    this.refWorkspace = refWorkspace;
  }

  public Collection<DBWorkspace> getChildren() 
  {
    return children;
  }

  public void setChildren(Collection<DBWorkspace> children) 
  {
    this.children = children;
  }

  public Collection<DBNode> getNodes() 
  {
    return nodes;
  }

  public void setNodes(Collection<DBNode> nodes) 
  {
    this.nodes = nodes;
  }

  public void copyTo(Workspace workspace, WSEndpoint endpoint)
  {    
    workspace.setWorkspaceId(endpoint.toGlobalId(Workspace.class,
      this.getWorkspaceId()));
    workspace.setName(this.getName());
    workspace.setDescription(this.getDescription());
    workspace.setCreationDateTime(this.getCreationDateTime());
    workspace.setCreationUserId(endpoint.toGlobalId(User.class, 
      this.getCreationUserId()));
    workspace.setRefWorkspaceId(endpoint.toGlobalId(Workspace.class,
      this.getRefWorkspaceId()));
  }

  public void copyFrom(Workspace workspace, WSEndpoint endpoint)
  {
    setWorkspaceId(endpoint.toLocalId(Workspace.class, 
      workspace.getWorkspaceId()));
    setName(workspace.getName());
    setDescription(workspace.getDescription());
    setCreationDateTime(workspace.getCreationDateTime());
    setCreationUserId(endpoint.toLocalId(User.class, 
      workspace.getCreationUserId()));
    setRefWorkspaceId(endpoint.toLocalId(Workspace.class,
      workspace.getRefWorkspaceId()));
  }

}
