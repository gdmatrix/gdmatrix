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
package org.santfeliu.webapp.modules.geo.pdfgen;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import org.santfeliu.pdfgen.PdfGenerator;

/**
 *
 * @author realor
 */
public class MapTextAreaNode extends MapRectNode
{
  public MapTextAreaNode(String argument)
  {
    super(argument);
  }

  public String getProperty()
  {
    return getArgument();
  }

  @Override
  public void primitivePaint(Graphics2D g2d)
  {
    try
    {
      Map context = PdfGenerator.getCurrentInstance().getContext();
      MapContext.init(context);

      String textValue = null;
      Object value = MapContext.getProperty(context, getProperty());
      if (value != null) textValue = value.toString();

      if (textValue != null)
      {
        g2d.setColor(Color.BLACK);
        Rectangle2D boxBounds = this.getBounds();
        int x = (int)boxBounds.getX();
        int y = (int)boxBounds.getY();
        int fontHeight = (int)boxBounds.getHeight();
        Font font = new Font("Arial", Font.PLAIN, fontHeight);
        g2d.setFont(font);
        FontMetrics fontMetrics = g2d.getFontMetrics(font);
        Rectangle2D stringBounds = fontMetrics.getStringBounds(textValue, g2d);
        int offsetX =
          x + (int)((boxBounds.getWidth() - stringBounds.getWidth()) / 2);
        int offsetY = y + fontHeight;

        //g2d.drawRect(x, y, w, h);
        g2d.drawString(textValue, offsetX, offsetY);
      }
    }
    catch (Exception ex)
    {
    }
  }
}
