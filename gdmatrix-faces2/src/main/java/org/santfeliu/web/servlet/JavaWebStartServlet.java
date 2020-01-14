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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.santfeliu.util.template.WebTemplate;


/**
 *
 * @author unknown
 */
public class JavaWebStartServlet extends HttpServlet
{
  public JavaWebStartServlet()
  {
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    try
    {
      // TODO: CHECK URL
      String urlString = request.getRequestURL().toString();
      System.out.println("\n\nURL:" + urlString);
      
      int index = urlString.lastIndexOf("/");
      String jwsURL = urlString.substring(0, index);
      String filename = urlString.substring(index + 1);
      index = jwsURL.lastIndexOf("/");
      String context = jwsURL.substring(0, index);

      String uri = request.getRequestURI();
      int semicolon = uri.indexOf(";");
      if (semicolon > 0)
        uri = uri.substring(0, semicolon);
      String realPath = getServletContext().getRealPath(uri);

      File file = new File(realPath);
      String jnlpTemplate = readFile(file);

      HashMap variables = new HashMap();
      variables.put("jwsURL", jwsURL);
      variables.put("filename", filename);
      variables.put("context", context);

      Enumeration enu = request.getParameterNames();
      while (enu.hasMoreElements())
      {
        String name = (String)enu.nextElement();
        String value = request.getParameter(name);
        variables.put(name, value);
      }

      // apply values to template
      jnlpTemplate = WebTemplate.create(jnlpTemplate).merge(variables);

      // write to response
      response.setContentType("application/x-java-jnlp-file");
      response.setDateHeader("Date", System.currentTimeMillis());
      response.setDateHeader("Last-Modified", file.lastModified());

      PrintWriter writer = response.getWriter();
      writer.write(jnlpTemplate);
      writer.close();
    }
    catch (FileNotFoundException ex)
    {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  private String readFile(File file) throws IOException
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    byte[] buffer = new byte[4096];
    FileInputStream is = new FileInputStream(file);
    try
    {
      int numRead = is.read(buffer);
      while (numRead > 0)
      {
        bos.write(buffer, 0, numRead);
        numRead = is.read(buffer);
      }
    }
    finally
    {
      is.close();
    }
    return bos.toString("UTF-8");
  }
}
