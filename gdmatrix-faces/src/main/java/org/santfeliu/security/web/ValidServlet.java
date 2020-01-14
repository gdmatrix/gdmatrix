package org.santfeliu.security.web;

import cat.aoc.valid.ValidClient;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.FacesServlet;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author realor
 */
public class ValidServlet extends HttpServlet
{
  private final ValidClient client = new ValidClient();  
	public final static String URLPARAM_AUTORIZATIONCODE = "code";
	public final static String URLPARAM_ERROR = "error";
	public final static String URLPARAM_STATE = "state";	
  public final static String URLPARAM_ERROR_CANCELLED = "SESSION_CANCEL";

  @Override
  public void init() throws ServletException
  {
    client.setBaseUrl(MatrixConfig.getProperty("valid.baseUrl"));
    client.setClientId(MatrixConfig.getProperty("valid.clientId"));
    client.setClientSecret(MatrixConfig.getProperty("valid.clientSecret"));
    client.setRedirectUrl(MatrixConfig.getProperty("valid.redirectUrl"));
  }
  
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws ServletException, IOException
  {
    HttpSession session = request.getSession(true);
    UserSessionBean userSessionBean = UserSessionBean.getInstance(session);
    String queryString = request.getQueryString();

    if (userSessionBean == null)
    {
      showError(request, response, "NO_SESSION");
      return;
    }
    
    if (queryString != null && userSessionBean.isCertificateUser())
    {
      String uri = FacesServlet.GO_URI + "?" + queryString;
      response.sendRedirect(uri);
      return;
    }

    String state = request.getParameter(URLPARAM_STATE);
    if (state == null)
    {
      // redirect to VALID login page
      state = queryString == null ?
        FacesServlet.XMID_PARAM + "=" + userSessionBean.getSelectedMid() : 
        queryString;
      
      String authUrl = client.generateOAuthLoginUrl(encodeState(state));
      response.sendRedirect(authUrl);
      return;
    }

    String error = request.getParameter(URLPARAM_ERROR);
    if (error != null)
    {
      // login failed
      showError(request, response, error);
      return;
    }

    try
    {
      // Validation
      String authorizationCode = request.getParameter(URLPARAM_AUTORIZATIONCODE);
      JSONObject accessTokenObject = client.getAccessToken(authorizationCode);
      error = (String)accessTokenObject.get("error");
      if (error != null) throw new Exception(error);

      String accessToken = (String)accessTokenObject.get("access_token");
      JSONObject userInfoObject = client.getUserInfo(accessToken);

      String givenName = (String)userInfoObject.get("name");
      String surname = (String)userInfoObject.get("surname1");
      String surname2 = (String)userInfoObject.get("surname2");
      if (surname != null && surname2 != null) surname += " " + surname2;

      String displayName = givenName;
      if (surname != null) displayName += " " + surname;
      String identifier = (String)userInfoObject.get("identifier");
      identifier = identifier.toUpperCase();
      String email = (String)userInfoObject.get("email");
      String identifierType = (String)userInfoObject.get("indentifierType");

      userSessionBean.loginMobile(identifier, "VALID", "#{validBean.logout}");
      userSessionBean.setDisplayName(displayName);
      userSessionBean.setGivenName(givenName);
      userSessionBean.setSurname(surname);
      userSessionBean.setEmail(email);
      userSessionBean.setAttribute("accessToken", accessToken);
      if ("1".equals(identifierType))
      {
        userSessionBean.setNIF(identifier);
      }
      response.sendRedirect(FacesServlet.GO_URI + "?" + decodeState(state));
    }
    catch (Exception ex)
    {
      String message = ex.getMessage();
      if (message == null) message = ex.toString();  
      showError(request, response, message);
    }
  }

  private void showError(HttpServletRequest request, 
    HttpServletResponse response, String message)
    throws IOException, ServletException
  {
    request.setAttribute("error", message);
    RequestDispatcher dispatcher = 
      request.getRequestDispatcher("/common/security/valid_error.faces");
    dispatcher.forward(request, response);
  }
  
  private String encodeState(String state) throws UnsupportedEncodingException
  {
    byte[] data = Base64.encodeBase64(state.getBytes("UTF-8"));
    return new String(data, "UTF-8");
  }
  
  private String decodeState(String state) throws UnsupportedEncodingException
  {
    byte[] data = Base64.decodeBase64(state.getBytes("UTF-8"));
    return new String(data, "UTF-8");
  }
}
