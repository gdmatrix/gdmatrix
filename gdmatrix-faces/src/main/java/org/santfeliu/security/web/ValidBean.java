package org.santfeliu.security.web;

import cat.aoc.valid.ValidClient;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.servlet.http.HttpServletRequest;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author realor
 */
public class ValidBean extends FacesBean
{
  public ValidBean()
  {
    createMessages();
  }
  
  public String getLoginURL()
  {
    ExternalContext extContext = getExternalContext();
    HttpServletRequest request = (HttpServletRequest)extContext.getRequest();

    return "/valid?" + request.getQueryString();    
  }
  
  private void createMessages()
  {
    String error = (String)getExternalContext().getRequestMap().get("error");
    if (error != null)
    {
      if ("NO_SESSION".equals(error))
      {
        message("org.santfeliu.security.web.resources.ValidBundle",
        "noSession", new Object[]{}, FacesMessage.SEVERITY_INFO);
      }
      else if ("SESSION_CANCEL".equals(error))
      {
        message("org.santfeliu.security.web.resources.ValidBundle",
        "sessionCancel", new Object[]{}, FacesMessage.SEVERITY_INFO);        
      }
      else
      {       
        error(error);
      }
    }
  }
  
  public String getTitle()
  {
    return "VALid";
  }
  
  public void logout() // called from UserSessionBean.logout()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String accessToken = (String)userSessionBean.getAttribute("accessToken");
    if (accessToken != null)
    {
      try
      {
        ValidClient client = new ValidClient();
        client.setBaseUrl(MatrixConfig.getProperty("valid.baseUrl"));
        client.setClientId(MatrixConfig.getProperty("valid.clientId"));
        client.setClientSecret(MatrixConfig.getProperty("valid.clientSecret"));
        client.setRedirectUrl(MatrixConfig.getProperty("valid.redirectUrl"));
        client.revokeAccessToken(accessToken);
        client.logoutAuthorizationCode(accessToken);
      }
      catch (Exception ex)
      {
      }
    }
  }
}


