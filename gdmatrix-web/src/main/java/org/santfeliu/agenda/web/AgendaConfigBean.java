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
package org.santfeliu.agenda.web;

import java.net.URL;
import java.util.List;
import org.santfeliu.agenda.client.AgendaManagerClient;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 */
public class AgendaConfigBean
{
  public static final String LIST_IMAGE_TYPE =
    "EventDocumentListImage";
  public static final String DETAILS_IMAGE_TYPE =
    "EventDocumentDetailsImage";
  public static final String LIST_AND_DETAILS_IMAGE_TYPE =
    "EventDocumentListAndDetailsImage";
  public static final String EXTENDED_INFO_TYPE =
    "EventDocumentExtendedInfo";

  public AgendaConfigBean()
  {
  }

  public static boolean isRunAsAdmin()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String userId = userSessionBean.getUserId();
    MenuItemCursor cursor = userSessionBean.getSelectedMenuItem();
    List<String> userIdList = 
      cursor.getMultiValuedProperty(EventSearchBean.RUN_AS_ADMIN_FOR_PROPERTY);
    return userIdList.contains(userId);
  }
  
  public static boolean isFindAsAdmin()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String userId = userSessionBean.getUserId();
    MenuItemCursor cursor = userSessionBean.getSelectedMenuItem();
    List<String> userIdList = 
      cursor.getMultiValuedProperty(EventSearchBean.FIND_AS_ADMIN_FOR_PROPERTY);
    return (userIdList.contains(userId) || userIdList.contains(EventSearchBean.ALL_USERS));
  }  

  public static AgendaManagerClient getPort() throws Exception
  {
    String userId;
    String password;
    if (isRunAsAdmin() || isFindAsAdmin())
    {
       userId = MatrixConfig.getProperty("adminCredentials.userId");
       password = MatrixConfig.getProperty("adminCredentials.password");
    }
    else
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      userId = userSessionBean.getUserId();
      password = userSessionBean.getPassword();
    }    
    return new AgendaManagerClient(userId, password);
  }  
  
  public static AgendaManagerClient getPort(String userId, String password) throws Exception
  {
    return new AgendaManagerClient(userId, password);
  }

  public static AgendaManagerClient getPort(URL wsDirectoryURL, String userId, String password) throws Exception
  {
    return new AgendaManagerClient(wsDirectoryURL, userId, password);
  }
  
  public String getEventTypeDescription(String typeName)
  {
    if (typeName == null) return "";

    String description = typeName;
    Type type =
      TypeCache.getInstance().getType(typeName);
    if (type != null)
      description = type.getDescription();

    return (description != null ? description : typeName);
  }
}
