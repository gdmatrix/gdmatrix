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
package org.santfeliu.swing.border;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import javax.swing.border.AbstractBorder;

/**
 *
 * @author realor
 */
public class SouthBorder extends AbstractBorder
{
  private final Color color;
  private final boolean dashed;

  public SouthBorder(Color color)
  {
    this.color = color;
    this.dashed = false;
  }

  public SouthBorder(Color color, boolean dashed)
  {
    this.color = color;
    this.dashed = dashed;
  }

  @Override
  public Insets getBorderInsets(Component c)
  {
    return new Insets(0, 0, 0, 0);
  }

  @Override
  public void paintBorder(Component c, Graphics g, int x, int y, int width,
                          int height)
  {
    g.setColor(color);
    if (dashed)
    {
      ((Graphics2D) g).setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
                                                 BasicStroke.JOIN_MITER,
                                                 1.0f, new float[]
            { 4.0f, 2.0f }, 0.0f));
    }
    else
    {
      ((Graphics2D) g).setStroke(new BasicStroke(1));
    }
    g.drawLine(x, height - 1, x + width, height - 1);
  }
}
