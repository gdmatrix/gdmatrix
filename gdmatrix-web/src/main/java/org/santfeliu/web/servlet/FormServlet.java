/*
 * GDMatrix
 *  
 * Copyright (C) 2020, Ajuntament de Sant Feliu de Llobregat
 *  
 * This program is licensed and may be used, modified and redistributed under 
 * the terms of the European Public License (EUPL), either version 1.1 or (at 
 * your option) any later version as soon as they are approved by the European 
 * Commission.
 *  
 * Alternatively, you may redistribute and/or modify this program under the 
 * terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation; either  version 3 of the License, or (at your option) 
 * any later version. 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *    
 * See the licenses for the specific language governing permissions, limitations 
 * and more details.
 *    
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along 
 * with this program; if not, you may find them at: 
 *    
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/ 
 * and 
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.santfeliu.web.servlet;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
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
        String nocache = (String) data.get("nocache");
        if (nocache != null && !nocache.equalsIgnoreCase("false"))
        {
          form = instance.getForm(selector, data, true);
          if (form != null)
          {
            SimpleDateFormat df = 
              new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
            String lastModified = df.format(new Date());      
            resp.setHeader("last-modified", lastModified);
          }
        }
        else
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
