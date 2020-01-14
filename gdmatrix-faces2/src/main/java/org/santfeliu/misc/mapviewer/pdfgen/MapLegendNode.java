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
package org.santfeliu.misc.mapviewer.pdfgen;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import org.santfeliu.pdfgen.PdfGenerator;

/**
 *
 * @author realor
 */
public class MapLegendNode extends MapRectNode
{
  public MapLegendNode(String argument)
  {
    super(argument);
  }

  @Override
  protected void localPrimitivePaint(Graphics2D g2d, Rectangle2D bounds)
  {
    try
    {
      Map context = PdfGenerator.getCurrentInstance().getContext();
      MapContext.init(context);

      String text = "Legend ";
      Object value = context.get("text");
      if (value != null) text += value.toString();

      Color color = g2d.getColor();
      g2d.setColor(Color.BLACK);
      g2d.drawString(text, 10, 20);
      g2d.drawRect(0, 0, (int)bounds.getWidth(), (int)bounds.getHeight());
      g2d.setColor(color);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
