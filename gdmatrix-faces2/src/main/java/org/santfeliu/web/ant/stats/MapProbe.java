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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public abstract class MapProbe extends Probe
{
  protected Object[][] visits = new Object[7][24];
  protected SimpleDateFormat df;
  protected Calendar calendar;

  private String dateFormat = "dd/MM/yyyy-HH:mm:ss";

  public String getDateFormat()
  {
    return dateFormat;
  }

  public void setDateFormat(String dateFormat)
  {
    this.dateFormat = dateFormat;
  }

  @Override
  public void init()
  {
    df = new SimpleDateFormat(dateFormat, new Locale("es"));
    calendar = Calendar.getInstance(new Locale("es"));
  }

  public abstract void processLine(Line line);

  @Override
  public void printBody(PrintWriter writer) throws IOException
  {
    String weekDays[];
    int firstDay = 1; // monday
    weekDays = new String[7];
    weekDays[0] = "diumenge";
    weekDays[1] = "dilluns";
    weekDays[2] = "dimarts";
    weekDays[3] = "dimecres";
    weekDays[4] = "dijous";
    weekDays[5] = "divendres";
    weekDays[6] = "dissabte";

    //
    writer.println("<table id=\"" + getName() + "\">");
    writer.println("<thead>");
    writer.println("<tr>");
    writer.println("<td>Interval</td>");
    // printar dies
    for (int dayInWeek = 0; dayInWeek < 7; dayInWeek++)
    {
      int diw = (dayInWeek + firstDay) % 7;
      writer.print("<td>");
      writer.print(weekDays[diw]);
      writer.print("</td>");
    }
    writer.println("</tr>");
    writer.println("</thead>");

    //
    // Trobar maxim i minim en el rang de dades
    int minim = -1;
    int maxim = 0;
    for (int dayInWeek = 0; dayInWeek < 7; dayInWeek++)
    {
      for (int hour = 0; hour < 24; hour++)
      {
        if (visits[dayInWeek][hour] != null)
        {
          if (getCellValue(dayInWeek,hour) < minim || minim == -1)
          {
            minim = getCellValue(dayInWeek,hour);
          }
          if (getCellValue(dayInWeek,hour) > maxim)
          {
            maxim = getCellValue(dayInWeek,hour);
          }
        }
      }
    }
    //Interval de color: 0 a 255
    float intervalColor = (float) 255;
    float gradient = intervalColor / maxim;

    // print hours
    for (int hour = 0; hour < 24; hour++)
    {
      writer.print("<tr>\n");
      writer.print("<td class=\"interval\">");
      writer.print("" + hour + ":00h-" + ((hour + 1) % 24) + ":00h");
      writer.println("</td>");
      for (int dayInWeek = 0; dayInWeek < 7; dayInWeek++)
      {
        int diw = (dayInWeek + firstDay) % 7;

        if (visits[diw][hour] == null)
        {
          writer.print("<td class=\"cell\" " +
            "style=\"background-color:white\">0");
        }
        else
        {
          float numVisites = (float)(getCellValue(diw,hour));
          int valorColor = 255 - (int)(gradient * numVisites);
          String textColor = "";
          if (valorColor > 150)
          {
            textColor = "black";
          }
          else
          {
            textColor = "white";
          }
          String valorColorHx = Integer.toHexString(valorColor);
          if (valorColorHx.length() != 2)
          {
            valorColorHx = "0" + valorColorHx;
          }
          valorColorHx = "#" + valorColorHx + valorColorHx + valorColorHx;
          int visites = getCellValue(diw, hour);
          writer.print("<td class=\"cell\" " +
           "style=\"background-color:" + valorColorHx +
            ";color:" + textColor + "\">" + visites + "\n");
        }
        writer.print("</td>\n");
      }
      writer.print("</tr>\n");
    }
    writer.println("</table>");
  }
  
  protected abstract int getCellValue(int x, int y);

}
