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
package org.santfeliu.security.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.matrix.security.SecurityConstants;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.santfeliu.security.UserCache;

/**
 *
 * @author lopezrj
 */
public class UserRolesTreeBean extends RolesTreeBean
{  
  @Override
  public String show()
  {
    return "user_roles_tree";
  }
  
  @Override
  protected TreeNode getMainTreeNode()
  {
    String userId = getObjectId(); 
    TreeNode mainNode = new DefaultTreeNode("User", new UserInfo(userId), 
      getRoot());
    return mainNode;
  }
                
  public class UserInfo extends NodeInfo
  {
    public UserInfo(String userId) 
    {
      super(userId);
    }
    
    public String getUserId() 
    {
      return getNodeId();
    }

    @Override
    protected List<String> getChildrenRoles() 
    {
      List<String> auxList = new ArrayList<>();
      Set<String> userRoles = UserCache.getUserInRoles(getUserId(), false);
      for (String role : userRoles)
      {
        if (!role.startsWith(SecurityConstants.SELF_ROLE_PREFIX) && 
          !role.equals(SecurityConstants.EVERYONE_ROLE))
        {
          auxList.add(role);
        }        
      }
      return auxList;
    }
  }  

}
