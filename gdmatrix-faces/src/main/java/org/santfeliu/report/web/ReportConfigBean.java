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
