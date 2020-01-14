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

/**
 *
 * @author lopezrj
 */
public class DBProperty
{
  private String workspaceId;
  private String nodeId;
  private String name;
  private int index;
  private String value;

  private DBNode node;

  public DBProperty()
  {
  }

  public String getWorkspaceId()
  {
    return workspaceId;
  }

  public void setWorkspaceId(String workspaceId)
  {
    this.workspaceId = workspaceId;
  }

  public String getNodeId()
  {
    return nodeId;
  }

  public void setNodeId(String nodeId)
  {
    this.nodeId = nodeId;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public int getIndex()
  {
    return index;
  }

  public void setIndex(int index)
  {
    this.index = index;
  }

  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public DBNode getNode()
  {
    return node;
  }

  public void setNode(DBNode node)
  {
    this.node = node;
  }

  public boolean equals(DBProperty dbProperty)
  {
    return
    (
      this.getWorkspaceId().equals(dbProperty.getWorkspaceId()) &&
      this.getNodeId().equals(dbProperty.getNodeId()) &&
      this.getName().equals(dbProperty.getName()) &&
      this.getValue().equals(dbProperty.getValue()) &&
      this.getIndex() == dbProperty.getIndex()
    );
  }
  
}
