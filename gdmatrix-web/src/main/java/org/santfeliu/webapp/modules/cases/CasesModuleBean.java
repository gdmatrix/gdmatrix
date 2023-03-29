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
package org.santfeliu.webapp.modules.cases;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CaseManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author realor
 */
@Named
@ApplicationScoped
public class CasesModuleBean
{
  private static final String FIND_AS_ADMIN_FOR_PROPERTY = "findAsAdminFor";
  private static final String ALL_USERS = "%";

  public static boolean isFindAsAdmin()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String userId = userSessionBean.getUserId();
    MenuItemCursor cursor = userSessionBean.getSelectedMenuItem();
    List<String> userIdList =
      cursor.getMultiValuedProperty(FIND_AS_ADMIN_FOR_PROPERTY);
    return (userIdList.contains(userId) || userIdList.contains(ALL_USERS));
  }

  public static CaseManagerPort getPort() throws Exception
  {
    return getPort(isFindAsAdmin());
  }

  public static CaseManagerPort getPort(String userId, String password)
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint = wsDirectory.getEndpoint(CaseManagerService.class);

    return endpoint.getPort(CaseManagerPort.class, userId, password);
  }

  public static CaseManagerPort getPort(boolean asAdmin) throws Exception
  {
    String userId;
    String password;
    if (asAdmin)
    {
      userId = MatrixConfig.getProperty("adminCredentials.userId");
      password = MatrixConfig.getProperty("adminCredentials.password");
    } else
    {
      userId = UserSessionBean.getCurrentInstance().getUsername();
      password = UserSessionBean.getCurrentInstance().getPassword();
    }
    return getPort(userId, password);
  }
}
