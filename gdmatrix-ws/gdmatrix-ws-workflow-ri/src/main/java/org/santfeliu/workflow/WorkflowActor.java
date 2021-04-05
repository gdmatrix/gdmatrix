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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.matrix.workflow.WorkflowConstants;

/**
 *
 * @author realor
 */
public abstract class WorkflowActor
{
  private String name;
  private Set roles;
  
  public WorkflowActor()
  {
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }
  
  public void setRoles(Set roles)
  {
    this.roles = roles;
  }

  public Set getRoles()
  {
    if (roles == null) roles = new HashSet();
    return roles;
  }

  public boolean hasAnyRole(Set rolesSet)
  {
    boolean hasAnyRole = false;
    Iterator iter = rolesSet.iterator();
    while (iter.hasNext() && !hasAnyRole)
    {
      String role = (String)iter.next();
      hasAnyRole = roles.contains(role);
    }
    return hasAnyRole;
  }

  public boolean isWorkflowAdministrator()
  {
    return getRoles().contains(WorkflowConstants.WORKFLOW_ADMIN_ROLE);
  }
}
