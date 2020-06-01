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
package org.santfeliu.workflow.swing;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 *
 * @author realor
 */
public class WrapLabel extends JComponent
{
  public static final int LEFT = JLabel.LEFT;
  public static final int CENTER = JLabel.CENTER;
  public static final int RIGHT = JLabel.RIGHT;

  private String text;
  private String delimiters = " \n.,;";
  private int alignment = CENTER;
  private Icon icon;
  private int iconTextGap = 4;

  public WrapLabel()
  {
  }

  public String getDelimiters()
  {
    return delimiters;
  }

  public void setDelimiters(String delimiters)
  {
    this.delimiters = delimiters;
  }

  public void setAlignment(int alignment)
  {
    this.alignment = alignment;
  }

  public int getAlignment()
  {
    return alignment;
  }

  public void setText(String text)
  {
    this.text = text;
  }

  public String getText()
  {
    return text;
  }

  public void setIcon(Icon icon)
  {
    this.icon = icon;
  }

  public Icon getIcon()
  {
    return icon;
  }

  public void setIconTextGap(int iconTextGap)
  {
    this.iconTextGap = iconTextGap;
  }

  public int getIconTextGap()
  {
    return iconTextGap;
  }

  @Override
  public void paintComponent(Graphics g)
  {
    if (isOpaque())
    {
      g.setColor(getBackground());
      g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
    }

    if (icon != null)
    {
      Insets insets = getInsets();
      int imagex = insets.left +
        (getIconAreaWidth() - icon.getIconWidth()) / 2;
      int imagey = insets.top +
        (getHeight() - insets.top - insets.bottom - icon.getIconHeight()) / 2;
      icon.paintIcon(this, g, imagex, imagey);
    }

    g.setColor(getForeground());
    g.setFont(getFont());
    FontMetrics metrics = g.getFontMetrics();
    int textHeight = metrics.getHeight();
    int ascent = metrics.getAscent();
    int height = getHeight();

    List<StringBuilder> lines = getLines(text, g);
    Insets insets = getInsets();

    int offset = height - (insets.top + lines.size() *
                           textHeight + insets.bottom);
    int y = Math.max((int)Math.ceil((double)offset / 2.0) + ascent,
                     ascent);
    for (int l = 0; l < lines.size(); l++)
    {
      StringBuilder buffer = lines.get(l);
      drawLine(g, buffer.toString(), y);
      y += textHeight;
    }
  }

  @Override
  public Dimension getPreferredSize()
  {
    return getMinimumSize();
  }

  @Override
  public Dimension getMinimumSize()
  {
    Graphics g = this.getGraphics();
    FontMetrics metrics = g.getFontMetrics();
    g.dispose();
    int textHeight = metrics.getHeight();

    List<StringBuilder> lines = getLines(text, g);
    Insets insets = getInsets();

    int width = getWidth();
    int height = insets.top + insets.bottom +
      Math.max(getIconAreaHeight(), lines.size() * textHeight);

    return new Dimension(width, height);
  }

  @Override
  public Dimension getMaximumSize()
  {
    return new Dimension(1000, 1000);
  }

  private int getIconAreaWidth()
  {
    return (icon == null) ? 0 : icon.getIconWidth() + iconTextGap;
  }

  private int getIconAreaHeight()
  {
    return (icon == null) ? 0 : icon.getIconHeight();
  }

  private List<StringBuilder> getLines(String text, Graphics g)
  {
    List<StringBuilder> lines = new ArrayList<>();
    if (text == null) return lines;

    Font font = getFont();
    FontMetrics metrics = g.getFontMetrics();
    int textHeight = metrics.getHeight();
    int ascent = metrics.getAscent();
    int maxWidth = getWidth();
    int iconWidth = getIconAreaWidth();

    FontRenderContext context = new FontRenderContext(
      new AffineTransform(), true, true);

    Rectangle2D bounds = font.getStringBounds(" ", context);
    int blankWidth = (int)bounds.getWidth();

    Insets insets = getInsets();
    int x = -1;
    int y = ascent;
    StringBuilder buffer = null;
    StringTokenizer tokenizer = new StringTokenizer(text, delimiters, true);

    while (tokenizer.hasMoreTokens())
    {
      String token = tokenizer.nextToken();
      bounds = font.getStringBounds(token, context);
      int tokenWidth = (int)bounds.getWidth();
      if (x == -1)
      {
        buffer = new StringBuilder();
        lines.add(buffer);
        buffer.append(token);
        x += insets.left + tokenWidth + iconWidth;
      }
      else if (x + blankWidth + tokenWidth + insets.right < maxWidth)
      {
        buffer.append(token);
        x += blankWidth + tokenWidth;
      }
      else
      {
        buffer = new StringBuilder();
        lines.add(buffer);
        buffer.append(token);
        x = insets.left + tokenWidth + iconWidth;
        y += textHeight;
      }
    }
    return lines;
  }

  private void drawLine(Graphics g, String line, int y)
  {
    Insets insets = getInsets();
    FontRenderContext context = new FontRenderContext(
      new AffineTransform(), true, true);

    Rectangle2D bounds = getFont().getStringBounds(line, context);
    int lineWidth = (int)bounds.getWidth();
    int width = getWidth();
    int iconWidth = getIconAreaWidth();

    if (alignment == LEFT)
    {
      g.drawString(line, insets.left + iconWidth, y);
    }
    else if (alignment == CENTER)
    {
      g.drawString(line, insets.left + iconWidth +
       (width - insets.left - insets.right - lineWidth - iconWidth) / 2, y);
    }
    else if (alignment == RIGHT)
    {
      g.drawString(line, width - lineWidth - insets.right + 1, y);
    }
  }
}
