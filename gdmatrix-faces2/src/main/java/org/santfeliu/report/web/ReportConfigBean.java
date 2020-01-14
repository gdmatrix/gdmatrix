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
package org.santfeliu.report.web;

import java.io.Serializable;
import java.util.Map;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.MTOMFeature;
import org.matrix.report.ReportManagerPort;
import org.matrix.report.ReportManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import com.sun.xml.ws.developer.JAXWSProperties;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author realor
 */
public class ReportConfigBean implements Serializable
{
  public static ReportManagerPort getReportManagerPort(Credentials credentials)
    throws Exception
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint = wsDirectory.getEndpoint(ReportManagerService.class);
    ReportManagerPort port = endpoint.getPort(ReportManagerPort.class,
      credentials.getUserId(), credentials.getPassword(), new MTOMFeature());

    Map<String, Object> context = ((BindingProvider)port).getRequestContext();
    context.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);

    return port;
  }

  public static Credentials getExecutionCredentials()
  {
    Credentials credentials;
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor cursor = userSessionBean.getMenuModel().getSelectedMenuItem();
    if ("true".equals(cursor.getProperty(ReportBean.RUN_AS_ADMIN_PROPERTY)))
    {
      credentials = ReportConfigBean.getReportAdminCredentials();
    }
    else
    {
      credentials = userSessionBean.getCredentials();
    }
    return credentials;
  }

  public static Credentials getReportAdminCredentials()
  {
    String userId =
      MatrixConfig.getProperty("adminCredentials.userId");
    String password =
      MatrixConfig.getProperty("adminCredentials.password");
    return new Credentials(userId, password);
  }
}
