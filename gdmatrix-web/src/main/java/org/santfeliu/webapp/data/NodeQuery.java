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
package org.santfeliu.webapp.data;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.matrix.cms.CMSManagerPort;
import org.matrix.cms.Node;
import org.matrix.cms.NodeFilter;
import org.matrix.cms.Property;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.webapp.modules.cms.CMSModuleBean;

/**
 *
 * @author blanquepa
 */
public class NodeQuery extends DataQuery<Node>
{
  NodeFilter filter;

  public NodeQuery(Credentials credentials)
  {
    super(credentials);
    filter = new NodeFilter();
  }
  
  public NodeQuery workspaceId(String workspaceId)
  {
    filter.getWorkspaceId().add(workspaceId);
    return this;
  }
  
  public NodeQuery nodeId(String nodeId)
  {
    filter.getNodeId().add(nodeId);
    return this;
  }
  
  public NodeQuery parentNodeId(String nodeId)
  {
    filter.getParentNodeId().add(nodeId);
    return this;
  }  
  
  @Override
  public DataQuery<Node> eq(String field, Object value)
  {
    Property property = new Property();
    property.setName(field);
    property.getValue().add(String.valueOf(value));
    filter.getProperty().add(property);
    return this;
  }  
  
  @Override
  public NodeQuery firstResult(Number firstResult)
  {
    filter.setFirstResult(firstResult.intValue());
    return this;
  }
  
  @Override
  public NodeQuery maxResults(Number maxResults)
  {
    filter.setMaxResults(maxResults.intValue());
    return this;
  }  
  
  @Override
  public List<Node> execute()
  {
    CMSManagerPort port = 
      CMSModuleBean.getPort(credentials.getUserId(), credentials.getPassword());
    
    List<Node> nodes = port.findNodes(filter);
    
    nodes.sort(Comparator.comparing(Node::getIndex));
    
    return nodes;  
  }
  
  public Map<String, String> propertyMap(Node node) //TODO: return Map<String, List<String>>
  {
    Map<String, String> result = new HashMap();
    for (Property prop : node.getProperty())
    {
      String value = !prop.getValue().isEmpty() ? prop.getValue().get(0) : "";
      result.put(prop.getName(), value);
    }
    return result;
  }
  
}
