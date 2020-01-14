package org.santfeliu.web.servlet;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.santfeliu.form.Form;
import org.santfeliu.form.FormFactory;
import org.santfeliu.web.servlet.form.FormRenderer;
import org.santfeliu.web.servlet.form.NullFormRenderer;

/**
 *
 * @author realor
 */
public class FormServlet extends HttpServlet
{
  static final FormRenderer NULL_FORM_RENDERER = new NullFormRenderer();
  static final String SELECTOR_PARAMETER = "selector";
  static final String RENDERER_PARAMETER = "renderer";

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
  {
    try
    {
      System.out.println("\nFormServlet " + req.getMethod() + ": " +
        req.getQueryString());
      resp.setContentType("text/html");
      resp.setCharacterEncoding("UTF-8");
      Writer writer = resp.getWriter();

      Map data = readData(req);
      Form form = null;
      FormRenderer renderer = NULL_FORM_RENDERER;

      // getForm
      String selector = req.getParameter(SELECTOR_PARAMETER);
      if (selector != null)
      {
        FormFactory instance = FormFactory.getInstance();
        form = instance.getForm(selector, data);
      }
      
      // get FormRenderer
      if (form != null)
      {
        String rendererClassName = req.getParameter(RENDERER_PARAMETER);
        if (rendererClassName != null)
        {
          Class rendererClass = Class.forName(rendererClassName);
          renderer = (FormRenderer)rendererClass.newInstance();
        }
      }
      renderer.renderForm(form, data, writer);
    }
    catch (Exception ex)
    {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
  {
    doGet(req, resp);
  }

  private Map readData(HttpServletRequest req)
  {
    Map data = new HashMap();
    Enumeration enu = req.getParameterNames();
    while (enu.hasMoreElements())
    {
      String name = (String)enu.nextElement();
      if (!name.equals(SELECTOR_PARAMETER) && !name.equals(RENDERER_PARAMETER))
      {
        String value = req.getParameter(name);
        data.put(name, value);
      }
    }
    return data;
  }
}
