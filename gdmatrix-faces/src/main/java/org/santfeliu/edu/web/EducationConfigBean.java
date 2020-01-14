package org.santfeliu.edu.web;

import java.io.Serializable;
import org.matrix.edu.EducationManagerPort;

import org.matrix.edu.EducationManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.web.UserSessionBean;

public class EducationConfigBean implements Serializable
{
  public EducationConfigBean()
  {
  }
  
  public static EducationManagerPort getPort() throws Exception
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint =
      wsDirectory.getEndpoint(EducationManagerService.class);
    return endpoint.getPort(EducationManagerPort.class,
      UserSessionBean.getCurrentInstance().getUserId(),
      UserSessionBean.getCurrentInstance().getPassword());
  }
}
