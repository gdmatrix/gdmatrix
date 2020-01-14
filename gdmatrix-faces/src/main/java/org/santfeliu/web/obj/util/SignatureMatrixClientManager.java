package org.santfeliu.web.obj.util;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.santfeliu.web.ApplicationBean;
import org.santfeliu.web.HttpUtils;

/**
 *
 * @author blanquepa
 */
public class SignatureMatrixClientManager extends MatrixClientManager
{
  public static final String SIGN_COMMAND = "sign";
  
  public SignatureMatrixClientManager()
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

    String servletURL = protocol + "://" + serverName + ":" + port +
        contextPath + "/signatures";
    
    addParameter(SIGN_COMMAND, "signatureServletUrl", servletURL);
  }
  
  public String parseResult() throws Exception
  {
    return (String)parseResult(SIGN_COMMAND);
  }
 
}
