package org.santfeliu.web.servlet;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author realor
 */
public class WorkServlet extends HttpServlet
{
  public static final String URL_PATTERN = "/work";
  public static final String URL_PARAM = "url";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
  {
    String action = request.getParameter(URL_PARAM);
    if (action == null)
    {
      String contextPath = request.getContextPath();
      String URI = request.getRequestURI();
      action = contextPath + URI.substring(5);
    }
    request.setAttribute("action", action);
    response.setHeader("Pragma", "NO-CACHE");
    response.setIntHeader("Expires", -1);

    RequestDispatcher dispatcher =
      request.getRequestDispatcher("/common/util/work.jsp");
    dispatcher.forward(request, response);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
  {
    doGet(request, response);
  }
}
