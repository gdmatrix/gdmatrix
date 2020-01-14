package org.santfeliu.security.web;

import cat.mobileid.AuthenticateWS;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Map;
import javax.faces.application.FacesMessage;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.faces.beansaver.Savable;
import cat.mobileid.MobileIdWS;
import javax.faces.context.ExternalContext;
import javax.servlet.http.HttpServletRequest;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.FacesServlet;
import org.santfeliu.web.HttpUtils;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author realor
 */
public class MobileIdBean extends FacesBean implements Savable
{
  private boolean waitingForPin = false;
  private String docType;
  private String docNum;
  private String ticket;
  private final String queryString;

  public MobileIdBean()
  {
    HttpServletRequest request =
      (HttpServletRequest)getExternalContext().getRequest();
    queryString = request.getQueryString();
  }

  public String getQueryString()
  {
    return queryString;
  }

  public String getDocType()
  {
    return docType;
  }

  public void setDocType(String docType)
  {
    this.docType = docType;
  }

  public String getDocNum()
  {
    return docNum;
  }

  public void setDocNum(String docNum)
  {
    this.docNum = docNum;
  }

  public boolean isWaitingForPin()
  {
    return waitingForPin;
  }

  public String getLoginURL()
  {
    ExternalContext extContext = getExternalContext();
    HttpServletRequest request = (HttpServletRequest)extContext.getRequest();

    String url = HttpUtils.getServerSecureURL(request,
      "/mobileid", request.getQueryString());

    return url;
  }

  public String beginLogin()
  {
    try
    {
      if (StringUtils.isBlank(docNum))
      {
        message("org.santfeliu.security.web.resources.MobileIdBundle",
          "idNumberMandatory", null, FacesMessage.SEVERITY_ERROR);
        return null;
      }
      docNum = docNum.replaceAll(" ", "").replaceAll("-", "").toUpperCase();

      String subject = MatrixConfig.getProperty("mobileId.authenticate.subject");
      String source = MatrixConfig.getProperty("mobileId.source");

      Map<String, String> result = getClient().authenticateUser(Integer.parseInt(docType),
        docNum,
        subject,
        Integer.parseInt(source),
        MobileIdWS.LEVEL_CERTIFICATED, 0);
      String errorCode = result.get("error");
      if ("0".equals(errorCode))
      {
        ticket = result.get("ticket");
        waitingForPin = true;
      }
      else
      {
        message("org.santfeliu.security.web.resources.MobileIdBundle",
          "mobileIdError_" + errorCode, null, FacesMessage.SEVERITY_ERROR);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String endLogin()
  {
    try
    {
      Map<String, String> result = getClient().checkAuthenticate(ticket);
      System.out.println(result);
      String errorCode = result.get("error");
      if ("0".equals(errorCode))
      {
        UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
        userSessionBean.loginMobile(docNum, "MOBILEID");
        waitingForPin = false;
        ticket = null;
        if (queryString == null)
        {
          getExternalContext().redirect(FacesServlet.GO_URI);
        }
        else
        {
          getExternalContext().redirect(FacesServlet.GO_URI + "?" + queryString);
        }
        getFacesContext().responseComplete();
      }
      else
      {
        message("org.santfeliu.security.web.resources.MobileIdBundle",
          "mobileIdError_" + errorCode, null, FacesMessage.SEVERITY_ERROR);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public AuthenticateWS getClient() throws Exception
  {
    String endpoint = MatrixConfig.getProperty("mobileId.authenticate.endpoint");
    String keyStoreFilename = MatrixConfig.getProperty(
      "mobileId.keyStore.filename");
    String password = MatrixConfig.getProperty(
      "mobileId.keyStore.password");
    char[] keyStorePassword = password.toCharArray();

    File certificateDir = new File(MatrixConfig.getDirectory(), "certificates");
    File certificateFile = new File(certificateDir, keyStoreFilename);
    KeyStore ks = KeyStore.getInstance("PKCS12");
    InputStream is = new FileInputStream(certificateFile);
    try
    {
      ks.load(is, keyStorePassword);
    }
    finally
    {
      is.close();
    }
    return new AuthenticateWS(endpoint, ks, keyStorePassword);
  }
}
