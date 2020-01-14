package org.santfeliu.security.web;

import java.io.Serializable;
import org.matrix.security.SecurityManagerPort;
import org.matrix.security.SecurityManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;

public class SecurityConfigBean implements Serializable
{
  public SecurityConfigBean()
  {
  }
  
  public static SecurityManagerPort getPort() throws Exception
  {
    return getPort(false);
  }

  public static SecurityManagerPort getPort(boolean adminMode) throws Exception
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint = wsDirectory.getEndpoint(SecurityManagerService.class);

    if (adminMode) // Connect as admin
    {
      return endpoint.getPort(SecurityManagerPort.class,
        MatrixConfig.getProperty("adminCredentials.userId"),
        MatrixConfig.getProperty("adminCredentials.password"));
    }
    else
    {
      return endpoint.getPort(SecurityManagerPort.class,
        UserSessionBean.getCurrentInstance().getUsername(),
        UserSessionBean.getCurrentInstance().getPassword());
    }
  }
}
