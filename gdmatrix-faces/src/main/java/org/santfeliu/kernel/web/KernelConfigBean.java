package org.santfeliu.kernel.web;

import java.io.Serializable;

import org.matrix.kernel.KernelManagerPort;

import org.matrix.kernel.KernelManagerService;
import org.matrix.kernel.KernelMetaData;

import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.web.UserSessionBean;

public class KernelConfigBean implements Serializable
{
  private KernelMetaData metaData;

  public KernelConfigBean()
  {
  }

  public static KernelManagerPort getPort()
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint = wsDirectory.getEndpoint(KernelManagerService.class);
    return endpoint.getPort(KernelManagerPort.class,
      UserSessionBean.getCurrentInstance().getUsername(),
      UserSessionBean.getCurrentInstance().getPassword());
  }
  
  public KernelMetaData getMetaData() throws Exception
  {
    if (metaData == null)
    {
      metaData = getPort().getKernelMetaData();
    }
    return metaData;
  }
}
