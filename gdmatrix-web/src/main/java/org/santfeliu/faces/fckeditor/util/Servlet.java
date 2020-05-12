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
package org.santfeliu.faces.fckeditor.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author srecinto
 *
 */
public class Servlet extends HttpServlet
{

  private static final long serialVersionUID = 7260045528613530636L;

  private static final String modify = calcModify();

  private String customResourcePath;

  private static final String calcModify()
  {
    Date mod = new Date(System.currentTimeMillis());
    SimpleDateFormat sdf = 
      new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    return sdf.format(mod);
  }

  public void init(ServletConfig config)
    throws ServletException
  {
    super.init(config);
    customResourcePath = config.getInitParameter("customResourcePath");
  }

  public void doGet(HttpServletRequest request, 
                    HttpServletResponse response)
    throws ServletException, IOException
  {

    // search the resource in classloader
    ClassLoader cl = this.getClass().getClassLoader();
    String uri = request.getRequestURI();
    String path = 
      uri.substring(uri.indexOf(Util.FCK_FACES_RESOURCE_PREFIX) + 
                    Util.FCK_FACES_RESOURCE_PREFIX.length() + 1);

    if (getCustomResourcePath() != null)
    { //Use custom path to FCKeditor
      this.getServletContext().getRequestDispatcher(getCustomResourcePath() + 
                                                    path).forward(request, 
                                                                  response);
    }
    else
    { //Use default FCKeditor bundled up in the jar
      if (uri.endsWith(".jsf") || uri.endsWith(".faces"))
      {
        response.setContentType("text/html;");
      }
      else
      {
        response.setHeader("Cache-Control", "public");
        response.setHeader("Last-Modified", modify);
      }
      if (uri.endsWith(".css"))
      {
        response.setContentType("text/css;");
      }
      else if (uri.endsWith(".js"))
      {
        response.setContentType("text/javascript;");
      }
      else if (uri.endsWith(".gif"))
      {
        response.setContentType("image/gif;");
      }

      InputStream is = cl.getResourceAsStream(path);
      // if no resource found in classloader return nothing
      if (is == null)
        return;
      // resource found, copying on output stream
      OutputStream out = response.getOutputStream();
      byte[] buffer = new byte[2048];
      BufferedInputStream bis = new BufferedInputStream(is);
      int read = 0;
      read = bis.read(buffer);
      while (read != -1)
      {
        out.write(buffer, 0, read);
        read = bis.read(buffer);
      }
      bis.close();
      out.flush();
      out.close();
    }
  }

  public String getCustomResourcePath()
  {
    return customResourcePath;
  }

  public void setCustomResourcePath(String customResourcePath)
  {
    this.customResourcePath = customResourcePath;
  }

}
