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
package org.santfeliu.web.ant.stats;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.List;

/**
 *
 * @author unknown
 */
public class AccessProbe extends CounterProbe
{
  private String host = "http://localhost";

  public String getHost()
  {
    return host;
  }

  public void setHost(String host)
  {
    this.host = host;
  }

  @Override
  public void processLine(Line line)
  {
    StringBuilder buffer = new StringBuilder();
    String path = line.getPath();
    String[] rutaSplit = path.split("/");
    for (String part : rutaSplit)
    {
      int primer = part.indexOf("[");
      if (primer != -1)
      {
        int ultim = part.indexOf("]:");
        if (ultim != -1)
        {
          part = part.substring(0, primer) + part.substring(ultim + 2);
          buffer.append("/");
          buffer.append(String.valueOf(part));
        }
      }
      path = buffer.toString();
    }
    increment(line.getNodeId(), path);
  }

  @Override
  public void printBody(PrintWriter writer) throws IOException
  {
    DecimalFormat df = new DecimalFormat("#,###,###,##0.00");
    DecimalFormat df2 = new DecimalFormat("#,###,###,###,##0");
    List<Counter> counters = sortResults(0);
    int days = getStatistics().getDays();
    int total = getStatistics().getAccessCount();
    
    // visit ranking
    System.out.println(days);
    writer.println("<table id=\"" + getName() + "\">");

    // header
    writer.println("<thead>");
    writer.println("<tr><td>&nbsp;#&nbsp;</td><td>Ruta</td>"
      + "<td>Accessos</td><td>per&nbsp;dia</td><td>%&nbsp;total</td></tr>");
    writer.println("</thead>");

    writer.println("<tbody>");
    //
    int i = 1;
    for (Counter counter : counters)
    {      
      String rowClass = (i % 2 == 0) ? "row0" : "row1";

      writer.println("<tr class=\"" + rowClass + "\">");
      writer.println("<td class=\"text\">" + i + ".</td>");
      writer.print("<td>");
      writer.print("<a href=\"" + host + "/go.faces?xmid=" + counter.value + "\">");
      writer.print(counter.description);
      writer.print("</a>");
      writer.print("</td>");
      
      writer.print("<td class=\"number\">");
      writer.print(df2.format(counter.getCounter()));
      writer.print("</td>");
      
      writer.print("<td class=\"number\">");
      writer.print(df.format((double)counter.getCounter() / days));
      writer.print("</td>");
      writer.print("<td class=\"number\">");
      writer.print(df.format(100.0 * counter.getCounter() / total));
      writer.print("</td>");
      
      writer.println("</tr>");
      i++;
    }
    writer.println("</tbody>");
    writer.println("</table>");
  }

  @Override
  public void printValue(PrintWriter writer, Counter counter) throws IOException
  {
    writer.print("<a href=\"" + host + "/go.faces?xmid=" + counter.value + "\">");
    writer.print(counter.description);
    writer.print("</a>");
  }
}
