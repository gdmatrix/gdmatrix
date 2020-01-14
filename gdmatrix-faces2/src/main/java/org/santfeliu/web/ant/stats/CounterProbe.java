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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public abstract class CounterProbe extends Probe
{
  private String columnName = "value";
  private String othersDescription = "Others";
  private int total;
  private HashMap<String, Counter> map = new HashMap();

  public String getColumnName()
  {
    return columnName;
  }

  public void setColumnName(String columnName)
  {
    this.columnName = columnName;
  }

  public String getOthersDescription()
  {
    return othersDescription;
  }

  public void setOthersDescription(String othersDescription)
  {
    this.othersDescription = othersDescription;
  }

  protected void increment(String value)
  {
    increment(value, value);
  }

  protected void increment(String value, String description)
  {
    total++;
    Counter counter = map.get(value);
    if (counter == null)
    {
      counter = new Counter();
      counter.value = value;
      counter.description = description;
      map.put(value, counter);
    }
    counter.counter++;
  }

  @Override
  public void printBody(PrintWriter writer) throws IOException
  {
    DecimalFormat df = new DecimalFormat("#,###,###,##0.00");
    DecimalFormat df2 = new DecimalFormat("#,###,###,###,##0");
    
    List<Counter> counters = sortResults(0);

    writer.println("<table id=\"" + getName() + "\">");

    // header
    writer.println("<thead>");
    writer.println("<tr><td>" + columnName + "</td><td>total</td><td>%</tr>");
    writer.println("</thead>");

    writer.println("<tbody>");
    int i = 0;
    for (Counter counter : counters)
    {
      String rowClass = (i % 2 == 0) ? "row0" : "row1";

      writer.println("<tr class=\"" + rowClass + "\">");
      writer.println("<td class=\"text\">");
      printValue(writer, counter);
      writer.println("</td>");
      //
      writer.println("<td class=\"number\">");
      writer.println(df2.format(counter.getCounter()));
      writer.println("</td>");
      //
      writer.println("<td class=\"number\">");
      writer.println(df.format(100.0 * counter.getCounter() / total));
      writer.println("</td>");
      //
      writer.println("</tr>");
      i++;
    }
    writer.println("</tbody>");

    // footer
    writer.println("<tfoot><tr>");
    writer.println("<td class=\"text\">Total:</td>");
    writer.println("<td class=\"number\">" + df2.format(total) + "</td>");
    writer.println("<td class=\"number\">&nbsp;</td>");
    writer.println("</tr></tfoot>");

    writer.println("</table>");
  }

  protected void printValue(PrintWriter writer, Counter counter)
    throws IOException
  {
    writer.print(counter.description);
  }
  
  protected class Counter
  {
    String value;
    String description;
    int counter;

    public int getCounter()
    {
      return counter;
    }

    public void setCounter(int counter)
    {
      this.counter = counter;
    }

    public String getDescription()
    {
      return description;
    }

    public void setDescription(String description)
    {
      this.description = description;
    }

    public String getValue()
    {
      return value;
    }

    public void setValue(String value)
    {
      this.value = value;
    }
  }

  class CounterComparator implements Comparator
  {
    public int compare(Object o1, Object o2)
    {
      Counter c1 = (Counter)o1;
      Counter c2 = (Counter)o2;
      return c2.counter - c1.counter;
    }
  }

  class ValueComparator implements Comparator
  {
    public int compare(Object o1, Object o2)
    {
      Counter c1 = (Counter)o1;
      Counter c2 = (Counter)o2;

      return c1.value.compareTo(c2.value);
    }
  }

  protected List<Counter> sortResults(int type)
  {
    List<Counter> counters = new ArrayList<Counter>();
    counters.addAll(map.values());
    if (type == 0)
    {
      Collections.sort(counters, new CounterComparator());
    }
    else
    {
      Collections.sort(counters, new ValueComparator());
    }
    return counters;
  }
}
