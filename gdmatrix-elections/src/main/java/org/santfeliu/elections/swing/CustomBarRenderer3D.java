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
package org.santfeliu.elections.swing;

import java.util.Vector;

import org.jfree.chart.renderer.category.BarRenderer3D;

/**
 *
 * @author unknown
 */
public class CustomBarRenderer3D extends BarRenderer3D
{
  private Vector paints = new Vector(); //Vector of java.awt.Paint elements
  
  public CustomBarRenderer3D()
  {
    super();
  }
  
  public void setPaints(Vector paints)
  {
    this.paints = paints;
  }
  
  public java.awt.Paint getItemPaint(int row, int column)
  {
    if (paints != null)
    {
      try
      {
        java.awt.Paint paint = (java.awt.Paint)paints.get(column);
        if (paint != null)
          return paint;
        else
          return super.getItemPaint(row, column);
      }
      catch (Exception e)
      {
        return super.getItemPaint(row, column);
      }
    }
    else
      return super.getItemPaint(row, column);
  }
}
