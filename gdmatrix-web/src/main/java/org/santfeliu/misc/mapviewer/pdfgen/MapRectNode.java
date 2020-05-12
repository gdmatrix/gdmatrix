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

import com.lowagie.text.pdf.PdfGraphics2D;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import org.apache.batik.gvt.ShapeNode;

/**
 *
 * @author realor
 */
public class MapRectNode extends ShapeNode
{
  private String argument;

  public MapRectNode()
  {
  }

  public MapRectNode(String argument)
  {
    this.argument = argument;
  }

  public String getArgument()
  {
    return argument;
  }

  public void setArgument(String argument)
  {
    this.argument = argument;
  }

  @Override
  public void primitivePaint(Graphics2D g2d)
  {
    super.primitivePaint(g2d);
    localPaint(g2d);
  }

  protected final void localPaint(Graphics2D g2d)
  {
    Rectangle2D bounds = getBounds();
    PdfGraphics2D pdf = (PdfGraphics2D)g2d;
    g2d.translate(bounds.getX(), bounds.getY());
    localPrimitivePaint(pdf, bounds);
    g2d.translate(-bounds.getX(), -bounds.getY());
  }
  
  protected void localPrimitivePaint(Graphics2D g2d, Rectangle2D bounds)
  {
  }
}
