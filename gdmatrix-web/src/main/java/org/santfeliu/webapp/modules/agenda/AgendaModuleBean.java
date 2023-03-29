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
package org.santfeliu.webapp.modules.agenda;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.matrix.agenda.AgendaManagerPort;
import org.matrix.agenda.AgendaManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.agenda.client.AgendaManagerClient;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author lopezrj-sf
 */
@Named
@ApplicationScoped
public class AgendaModuleBean
{
  private static final String RUN_AS_ADMIN_FOR_PROPERTY = "runAsAdminFor";
  private static final String FIND_AS_ADMIN_FOR_PROPERTY = "findAsAdminFor";
  private static final String ALL_USERS = "%";

  public static boolean isRunAsAdmin()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String userId = userSessionBean.getUserId();
    MenuItemCursor cursor = userSessionBean.getSelectedMenuItem();
    List<String> userIdList =
      cursor.getMultiValuedProperty(RUN_AS_ADMIN_FOR_PROPERTY);
    return userIdList.contains(userId);
  }

  public static boolean isFindAsAdmin()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String userId = userSessionBean.getUserId();
    MenuItemCursor cursor = userSessionBean.getSelectedMenuItem();
    List<String> userIdList =
      cursor.getMultiValuedProperty(FIND_AS_ADMIN_FOR_PROPERTY);
    return (userIdList.contains(userId) || userIdList.contains(ALL_USERS));
  }

  public static AgendaManagerPort getPort(String userId, String password)
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint = wsDirectory.getEndpoint(AgendaManagerService.class);

    return endpoint.getPort(AgendaManagerPort.class, userId, password);
  }

  public static AgendaManagerPort getPort(boolean asAdmin) throws Exception
  {
    String userId;
    String password;
    if (asAdmin)
    {
      userId = MatrixConfig.getProperty("adminCredentials.userId");
      password = MatrixConfig.getProperty("adminCredentials.password");
    } 
    else
    {
      userId = UserSessionBean.getCurrentInstance().getUsername();
      password = UserSessionBean.getCurrentInstance().getPassword();
    }
    return getPort(userId, password);
  }
  
  public static AgendaManagerClient getClient() throws Exception
  {
    return getClient(isRunAsAdmin() || isFindAsAdmin());
  }

  public static AgendaManagerClient getClient(String userId, 
    String password)
  {
    return new AgendaManagerClient(userId, password);
  }
  
  public static AgendaManagerClient getClient(boolean asAdmin) 
  {
    String userId;
    String password;
    if (asAdmin)
    {
      userId = MatrixConfig.getProperty("adminCredentials.userId");
      password = MatrixConfig.getProperty("adminCredentials.password");
    } 
    else
    {
      userId = UserSessionBean.getCurrentInstance().getUsername();
      password = UserSessionBean.getCurrentInstance().getPassword();
    }
    return getClient(userId, password);
  }
}
