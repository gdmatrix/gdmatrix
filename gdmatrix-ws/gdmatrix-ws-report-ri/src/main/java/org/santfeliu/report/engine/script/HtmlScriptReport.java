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
package org.santfeliu.report.engine.script;

import java.io.File;
import java.io.PrintWriter;
import javax.activation.DataSource;
import org.santfeliu.util.TemporaryDataSource;

/**
 *
 * @author realor
 */
public class HtmlScriptReport extends ScriptReport
{
  private File file;
  private PrintWriter writer;

  @Override
  public String getFormat()
  {
    return "html";
  }

  @Override
  public String getContentType()
  {
    return "text/html";
  }

  @Override
  public void open()
  {
    try
    {
      file = File.createTempFile("temp", "html");
      writer = new PrintWriter(file, "UTF-8");
      writer.println("<html>");
      writer.println("<body>");
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  public void print(String text)
  {
    writer.print(text);
  }

  public void addParagraph(String text)
  {
    writer.print("<p>" + text + "</p>");
  }

  @Override
  public void close()
  {
    try
    {
      writer.println("</body>");
      writer.println("</html>");
      writer.flush();
      writer.close();
    }
    catch (Exception ex)
    {
    }
  }

  @Override
  public DataSource getData()
  {
    return new TemporaryDataSource(file, "text/html");
  }
}
