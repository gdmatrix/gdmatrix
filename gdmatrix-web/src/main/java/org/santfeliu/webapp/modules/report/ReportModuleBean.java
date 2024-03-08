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
package org.santfeliu.webapp.modules.report;

import java.io.Serializable;
import java.util.Map;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.MTOMFeature;
import org.matrix.report.ReportManagerPort;
import org.matrix.report.ReportManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import com.sun.xml.ws.developer.JAXWSProperties;
import java.util.List;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.matrix.doc.DocumentConstants;
import org.matrix.report.Report;
import org.matrix.security.AccessControl;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;
import static org.matrix.dic.DictionaryConstants.READ_ACTION;
import static org.matrix.dic.DictionaryConstants.EXECUTE_ACTION;

/**
 *
 * @author blanquepa
 */
@Named
@ApplicationScoped
public class ReportModuleBean implements Serializable
{
  public static ReportManagerPort getPort(Credentials credentials)
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
  
  public static ReportManagerPort getPort() throws Exception
  {
    return getPort(getReportAdminCredentials());
  }
  
  public static Credentials getExecutionCredentials(boolean runAsAdmin)
  {
    Credentials credentials;
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();

    if (runAsAdmin)
    {
      credentials = ReportModuleBean.getReportAdminCredentials();
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
  
  public static boolean canUserExecuteReport(Report report)
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance(); 
    MenuItemCursor cursor = 
      userSessionBean.getMenuModel().getSelectedMenuItem();      
    Set<String> roles = userSessionBean.getRoles();
    List<AccessControl> acl = report.getAccessControl();
    Type type = TypeCache.getInstance().getType(report.getDocTypeId()); 

    return roles.contains(DocumentConstants.DOC_ADMIN_ROLE)
      || DictionaryUtils.canPerformAction(EXECUTE_ACTION, roles, acl, type)
      || DictionaryUtils.canPerformAction(READ_ACTION, roles, acl, type)
      || "true".equals(cursor.getProperty(ReportViewerBean.RUN_AS_ADMIN_PROPERTY));
  }  
}
