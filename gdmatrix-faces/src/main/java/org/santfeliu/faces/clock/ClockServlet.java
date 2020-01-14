package org.santfeliu.faces.clock;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author realor
 */
public class ClockServlet extends HttpServlet
{
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    response.setContentType("text/plain");
    response.addHeader("Cache-Control", "no-cache");
    response.addHeader("Expires", "0");
    response.getOutputStream().print(System.currentTimeMillis());
  }
}
