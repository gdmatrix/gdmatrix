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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.script.WebScriptableBase;
import org.santfeliu.util.template.Template;
import org.santfeliu.util.template.WebTemplate;
import org.santfeliu.web.HttpUtils;

/**
 *
 * @author realor
 */
public class WSDirectoryServlet extends HttpServlet
{
  private static final String WSDIRECTORY_FILENAME = "wsdirectory.xml";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    PrintWriter writer  = response.getWriter();

    File baseDir = MatrixConfig.getDirectory();
    File dirFile = new File(baseDir, WSDIRECTORY_FILENAME);
    if (dirFile.exists() && dirFile.isFile())
    {
      HashMap variables = new HashMap();
      String base = HttpUtils.getContextURL(request);
      variables.put("base", base);

      String dirTemplate = readFile(dirFile);
      // apply values to template
      String out = WebTemplate.create(dirTemplate).merge(variables);
      response.setContentType("text/xml");
      writer.print(out);
    }
    else
    {
      response.setContentType("text/plain");
      writer.println("WSDirectory file not found at " +
        dirFile.getAbsolutePath());
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
