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
package org.santfeliu.cases.web;


import java.io.Serializable;
import java.util.List;
import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CaseManagerService;
import org.matrix.dic.Type;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;

import org.santfeliu.dic.TypeCache;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;


/**
 *
 * @author unknown
 */
public class CaseConfigBean implements Serializable
{
  public CaseConfigBean()
  {
  }
  
  public static CaseManagerPort getPort() throws Exception
  {
    String userId;
    String password;      
    if (isFindAsAdmin())
    {
       userId = MatrixConfig.getProperty("adminCredentials.userId");
       password = MatrixConfig.getProperty("adminCredentials.password");
    }
    else
    {
      userId = UserSessionBean.getCurrentInstance().getUsername();
      password = UserSessionBean.getCurrentInstance().getPassword();
    }
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint = wsDirectory.getEndpoint(CaseManagerService.class);
    return endpoint.getPort(CaseManagerPort.class, userId, password);
  }
  
  public String getCaseTypeDescription(String typeName)
  {
    if (typeName == null) return null;
    
    Type type = TypeCache.getInstance().getType(typeName);
    if (type != null)
      return type.getDescription() != null ? type.getDescription() : typeName;
    else
      return typeName;
  }
  
  public static boolean isFindAsAdmin()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String userId = userSessionBean.getUserId();
    MenuItemCursor cursor = userSessionBean.getSelectedMenuItem();
    List<String> userIdList = 
      cursor.getMultiValuedProperty(CaseSearchBean.FIND_AS_ADMIN_FOR_PROPERTY);
    return (userIdList.contains(userId) || userIdList.contains(CaseSearchBean.ALL_USERS));
  }  
  
}
