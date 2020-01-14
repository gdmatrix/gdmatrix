package org.santfeliu.faces.matrixclient.model;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.santfeliu.web.ApplicationBean;
import org.santfeliu.web.HttpUtils;

/**
 *
 * @author blanquepa
 */
public abstract class ServletMatrixClientModel extends DefaultMatrixClientModel
{
  protected abstract String getServletName();
  
  public String getServletUrl()
  {
    HttpServletRequest request = 
      (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();

    String serverName = HttpUtils.getServerName(request);
    String port = ApplicationBean.getCurrentInstance().getServerSecurePort();
    String contextPath = request.getContextPath();   
    String protocol = "https";
    if ("localhost".equals(serverName))
    {
      protocol = "http";
      port = ApplicationBean.getCurrentInstance().getDefaultPort();
    }

    return protocol + "://" + serverName + ":" + port +
        contextPath + "/" + getServletName();    
  }  
}
