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
package org.santfeliu.workflow.util;

import java.awt.geom.Point2D;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import java.util.StringTokenizer;
import java.util.Vector;

/**
 *
 * @author unknown
 */
public class PointsFormat extends Format
{
  public PointsFormat()
  {
  }

  public Point2D[] parsePoints(String source)
  {
    return (Point2D[])parseObject(source, new ParsePosition(0));
  }

  public Object parseObject(String source, ParsePosition pos)
  {
    int index = pos.getIndex();
    StringTokenizer tokenizer = 
      new StringTokenizer(source.substring(index), ";");
    Vector v = new Vector(4);
    while (tokenizer.hasMoreTokens())
    {
      String token = tokenizer.nextToken();
      int sep = token.indexOf(",");
      if (sep != -1)
      {
        try
        {
          double x = Double.parseDouble(token.substring(0, sep).trim());
          double y = Double.parseDouble(token.substring(sep + 1).trim());
          v.addElement(new Point2D.Double(x, y));
        }
        catch (NumberFormatException ex)
        {
        }
      }
    }
    return v.toArray(new Point2D[v.size()]);
  }
  
  public StringBuffer format(Object o, StringBuffer toAppendTo, FieldPosition pos)
  {
    if (o == null) return toAppendTo;
    
    Point2D[] points = (Point2D[])o;

    if (points.length > 0)
    {
      Point2D point = points[0];  
      toAppendTo.append(point.getX());
      toAppendTo.append(",");
      toAppendTo.append(point.getY());
      for (int i = 1; i < points.length; i++)
      {
        toAppendTo.append(";");
        point = points[i];
        toAppendTo.append(point.getX());
        toAppendTo.append(",");
        toAppendTo.append(point.getY());
      }
    }
    return toAppendTo;
  }
  
  public static void main(String[] args)
  {
    PointsFormat f = new PointsFormat();
    Point2D[] p = f.parsePoints("45.3,6;8,23;90,56");
    System.out.println(f.format(p));
  }
}
