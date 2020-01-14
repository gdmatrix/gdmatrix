package org.santfeliu.security.web;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.santfeliu.web.FacesServlet;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author realor
 */
public class MobileIdServlet extends HttpServlet
{
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws ServletException, IOException
  {
    HttpSession session = request.getSession(true);
    UserSessionBean userSessionBean = UserSessionBean.getInstance(session);
    String queryString = request.getQueryString();

    if (queryString != null &&
      userSessionBean != null && userSessionBean.isCertificateUser())
    {
      String uri = FacesServlet.GO_URI + "?" + queryString;
      response.sendRedirect(uri);
    }
    else
    {
      String uri = "/common/security/login_mobileid.faces";
      RequestDispatcher requestDispatcher = getServletContext().
        getRequestDispatcher(uri);
      requestDispatcher.forward(request, response);
    }
  }
}