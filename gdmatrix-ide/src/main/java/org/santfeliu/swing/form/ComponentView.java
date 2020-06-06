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
package org.santfeliu.swing.form;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;

import java.awt.image.ImageObserver;


/**
 *
 * @author realor
 */
public class ComponentView implements ImageObserver, Cloneable
{
  public static final String LEFT_TEXT_ALIGN = "left";
  public static final String CENTER_TEXT_ALIGN = "center";
  public static final String RIGHT_TEXT_ALIGN = "right";

  public static final int DEFAULT_WIDTH = 0;
  public static final int DEFAULT_FONT_SIZE = 12;

  public static final int NORTH_WEST = 0;
  public static final int NORTH = 1;
  public static final int NORTH_EAST = 2;
  public static final int WEST = 3;
  public static final int EAST = 4;
  public static final int SOUTH_WEST = 5;
  public static final int SOUTH = 6;
  public static final int SOUTH_EAST = 7;
  public static final int INTERNAL = 8;

  public static final BasicStroke selectionStroke =
    new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER,
    1f, new float[]{4f, 4f}, 0.0f);

  private int x;
  private int y;
  private int width;
  private int height;
  private final Point points[] = new Point[8];

  private String textAlign = LEFT_TEXT_ALIGN;
  private String fontFamily;
  private int fontSize;

  private String id;
  private Color foreground;
  private Color background;

  private String borderTopWidth;
  private String borderBottomWidth;
  private String borderLeftWidth;
  private String borderRightWidth;

  private Color borderTopColor;
  private Color borderBottomColor;
  private Color borderLeftColor;
  private Color borderRightColor;

  private String borderTopStyle;
  private String borderBottomStyle;
  private String borderLeftStyle;
  private String borderRightStyle;

  private String styleClass;
  private String renderer;

  private Integer outputOrder;

  public ComponentView()
  {
    for (int i = 0; i < points.length; i++)
    {
      points[i] = new Point();
    }
  }

  public String getComponentType()
  {
    String suffix = "View";
    String name = getClass().getSimpleName();
    if (name.endsWith(suffix))
    {
      name = name.substring(0, name.length() - suffix.length());
    }
    return name;
  }

  public void setBounds(int x, int y, int width, int height)
  {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    updatePoints();
  }

  public void setBounds(Rectangle rect)
  {
    this.x = rect.x;
    this.y = rect.y;
    this.width = rect.width;
    this.height = rect.height;
    updatePoints();
  }

  public Rectangle getBounds()
  {
    return new Rectangle(x, y, width, height);
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = nullWhenEmpty(id);
  }

  public int getX()
  {
    return x;
  }

  public void setX(int x)
  {
    this.x = x;
    updatePoints();
  }

  public int getY()
  {
    return y;
  }

  public void setY(int y)
  {
    this.y = y;
    updatePoints();
  }

  public int getWidth()
  {
    return width;
  }

  public void setWidth(int width)
  {
    this.width = width;
    updatePoints();
  }

  public int getHeight()
  {
    return height;
  }

  public void setHeight(int height)
  {
    this.height = height;
    updatePoints();
  }

  public int getContentWidth()
  {
    int bordersWidth =
      parseWidth(borderLeftWidth) + parseWidth(borderRightWidth);
    int contentWidth = width - bordersWidth;
    return contentWidth > 0 ? contentWidth : 0;
  }

  public void setContentWidth(int contentWidth)
  {
    int bordersWidth =
      parseWidth(borderLeftWidth) + parseWidth(borderRightWidth);
    setWidth(contentWidth + bordersWidth);
  }

  public int getContentHeight()
  {
    int bordersHeight =
      parseWidth(borderTopWidth) + parseWidth(borderBottomWidth);
    int contentHeight = height - bordersHeight;
    return contentHeight > 0 ? contentHeight : 0;
  }

  public void setContentHeight(int contentHeight)
  {
    int bordersHeight =
      parseWidth(borderTopWidth) + parseWidth(borderBottomWidth);
    setHeight(contentHeight + bordersHeight);
  }

  public void setTextAlign(String textAlign)
  {
    this.textAlign = textAlign;
  }

  public String getTextAlign()
  {
    return textAlign;
  }

  public void setForeground(Color foreground)
  {
    this.foreground = foreground;
  }

  public Color getForeground()
  {
    return foreground;
  }

  public void setBackground(Color background)
  {
    this.background = background;
  }

  public Color getBackground()
  {
    return background;
  }

  public void setFontFamily(String fontFamily)
  {
    this.fontFamily = nullWhenEmpty(fontFamily);
  }

  public String getFontFamily()
  {
    return fontFamily;
  }

  public void setFontSize(int fontSize)
  {
    this.fontSize = fontSize;
  }

  public int getFontSize()
  {
    return fontSize;
  }

  public Font getFont()
  {
    String family = (fontFamily == null || fontFamily.trim().length() == 0) ?
      "Arial" : fontFamily;
    int size = (fontSize == 0) ? DEFAULT_FONT_SIZE : fontSize;
    Font font = new Font(family, Font.PLAIN, size);
    return font;
  }

  public void setBorderTopWidth(String borderTopWidth)
  {
    this.borderTopWidth = nullWhenEmpty(borderTopWidth);
  }

  public String getBorderTopWidth()
  {
    return borderTopWidth;
  }

  public void setBorderBottomWidth(String borderBottomWidth)
  {
    this.borderBottomWidth = nullWhenEmpty(borderBottomWidth);
  }

  public String getBorderBottomWidth()
  {
    return borderBottomWidth;
  }

  public void setBorderLeftWidth(String borderLeftWidth)
  {
    this.borderLeftWidth = nullWhenEmpty(borderLeftWidth);
  }

  public String getBorderLeftWidth()
  {
    return borderLeftWidth;
  }

  public void setBorderRightWidth(String borderRightWidth)
  {
    this.borderRightWidth = nullWhenEmpty(borderRightWidth);
  }

  public String getBorderRightWidth()
  {
    return borderRightWidth;
  }

  public void setBorderTopColor(Color borderTopColor)
  {
    this.borderTopColor = borderTopColor;
  }

  public Color getBorderTopColor()
  {
    return borderTopColor;
  }

  public void setBorderBottomColor(Color borderBottomColor)
  {
    this.borderBottomColor = borderBottomColor;
  }

  public Color getBorderBottomColor()
  {
    return borderBottomColor;
  }

  public void setBorderLeftColor(Color borderLeftColor)
  {
    this.borderLeftColor = borderLeftColor;
  }

  public Color getBorderLeftColor()
  {
    return borderLeftColor;
  }

  public void setBorderRightColor(Color borderRightColor)
  {
    this.borderRightColor = borderRightColor;
  }

  public Color getBorderRightColor()
  {
    return borderRightColor;
  }

  public void setBorderTopStyle(String borderTopStyle)
  {
    this.borderTopStyle = nullWhenEmpty(borderTopStyle);
  }

  public String getBorderTopStyle()
  {
    return borderTopStyle;
  }

  public void setBorderBottomStyle(String borderBottomStyle)
  {
    this.borderBottomStyle = nullWhenEmpty(borderBottomStyle);
  }

  public String getBorderBottomStyle()
  {
    return borderBottomStyle;
  }

  public void setBorderLeftStyle(String borderLeftStyle)
  {
    this.borderLeftStyle = nullWhenEmpty(borderLeftStyle);
  }

  public String getBorderLeftStyle()
  {
    return borderLeftStyle;
  }

  public void setBorderRightStyle(String borderRightStyle)
  {
    this.borderRightStyle = nullWhenEmpty(borderRightStyle);
  }

  public String getBorderRightStyle()
  {
    return borderRightStyle;
  }

  public void setStyleClass(String styleClass)
  {
    this.styleClass = nullWhenEmpty(styleClass);
  }

  public String getStyleClass()
  {
    return styleClass;
  }

  public String getRenderer()
  {
    return renderer;
  }

  public void setRenderer(String renderer)
  {
    this.renderer = nullWhenEmpty(renderer);
  }

  public Integer getOutputOrder()
  {
    return outputOrder;
  }

  public void setOutputOrder(Integer outputOrder)
  {
    this.outputOrder = outputOrder;
  }

  public Point getDragPoint(int position)
  {
    return points[position];
  }

  public int getDragPosition(Point point)
  {
    boolean found = false;
    int position = -1;
    while (!found && position < 7)
    {
      position++;
      Point p = points[position];
      found = areOverlaped(p, point);
    }
    if (!found)
    {
      if (contains(point))
      {
        found = true;
        position = INTERNAL;
      }
    }
    return found ? position : -1;
  }

  public final void paint(Graphics g)
  {
    paint(g, false, false, false, false, false);
  }

  public final void paint(Graphics g, boolean accessibility,
    boolean showIds, boolean showTabIndexes, boolean showCoordinates,
    boolean showOutputOrder)
  {
    g.translate(x, y);
    paintBackground(g);
    if (accessibility)
    {
      paintAccessibility(g, showIds, showTabIndexes, showCoordinates,
        showOutputOrder);
    }
    else
    {
      paintView(g);
    }
    paintBorder(g);
    g.translate(-x, -y);
  }

  protected void paintBackground(Graphics g)
  {
    if (background != null)
    {
      g.setColor(background);
      g.fillRect(0, 0, width, height);
    }
  }

  protected void paintView(Graphics g)
  {
  }

  protected void paintAccessibility(Graphics g, boolean showIds,
    boolean showTabIndexes, boolean showCoordinates, boolean showOutputOrder)
  {
    String acc = getAccessibilityInfo(showIds, showTabIndexes, showCoordinates,
      showOutputOrder);
    g.setColor(Color.BLUE);
    g.drawString(acc, 6, 16);
  }

  protected String getAccessibilityInfo(boolean showIds, boolean showTabIndexes,
    boolean showCoordinates, boolean showOutputOrder)
  {
    StringBuilder sb = new StringBuilder();
    if (showIds)
    {
      sb.append(id == null ? "?" : id);
    }
    if (showCoordinates)
    {
      sb.append(" (").append(getX()).append(", ").append(getY()).append(")");
    }
    if (showOutputOrder)
    {
      sb.append(" o[").append(getOutputOrder() == null ? "?" :
        getOutputOrder().toString()).append("]");
    }
    if (showTabIndexes && getTabindex() != null)
    {
      sb.append(" t[").append(getTabindex().toString()).append("]");
    }
    return sb.toString();
  }

  public Integer getTabindex()
  {
    return null;
  }

  public int getDefaultBorderWidth()
  {
    return DEFAULT_WIDTH;
  }

  protected void paintBorder(Graphics g)
  {
    int defaultBorderWidth = getDefaultBorderWidth();
    int nBorderTopWidth = parseWidth(borderTopWidth, defaultBorderWidth);
    if (nBorderTopWidth > 0 && !"none".equals(borderTopStyle))
    {
      if (borderTopColor == null) g.setColor(Color.black);
      else g.setColor(borderTopColor);
      g.fillRect(0, 0, width, nBorderTopWidth);
    }
    int nBorderBottomWidth = parseWidth(borderBottomWidth, defaultBorderWidth);
    if (nBorderBottomWidth > 0 && !"none".equals(borderBottomStyle))
    {
      if (borderBottomColor == null) g.setColor(Color.black);
      else g.setColor(borderBottomColor);
      g.fillRect(0, height - nBorderBottomWidth + 1, width, nBorderBottomWidth);
    }
    int nBorderLeftWidth = parseWidth(borderLeftWidth, defaultBorderWidth);
    if (nBorderLeftWidth > 0 && !"none".equals(borderLeftStyle))
    {
      if (borderLeftColor == null) g.setColor(Color.black);
      else g.setColor(borderLeftColor);
      g.fillRect(0, 0, nBorderLeftWidth, height + 1);
    }
    int nBorderRightWidth = parseWidth(borderRightWidth, defaultBorderWidth);
    if (nBorderRightWidth > 0 && !"none".equals(borderRightStyle))
    {
      if (borderRightColor == null) g.setColor(Color.black);
      else g.setColor(borderRightColor);
      g.fillRect(width - nBorderRightWidth + 1, 0,
        nBorderRightWidth, height + 1);
    }
  }

  protected String nullWhenEmpty(String s)
  {
    if (s == null) return null;
    if (s.trim().length() == 0) return null;
    return s;
  }

  public void paintSelection(Graphics g)
  {
    Graphics2D g2 = (Graphics2D)g;
    g.setColor(Color.lightGray);
    g2.setStroke(selectionStroke);
    g.drawRect(x, y, width, height);
    g.setColor(Color.black);
    for (int i = 0; i < 8; i++)
    {
      Point point = points[i];
      g.fillRect(point.x - 2, point.y - 2, 5, 5);
    }
    g2.setStroke(new BasicStroke());
  }

  public boolean contains(Point point)
  {
    return point.x >= x &&
           point.x < x + width &&
           point.y >= y &&
           point.y < y + height;
  }

  public void move(int deltax, int deltay)
  {
    x += deltax;
    y += deltay;
    updatePoints();
  }

  public Rectangle getResizedBounds(int position, int deltax, int deltay)
  {
    Rectangle rect = getBounds();
    switch (position)
    {
      case NORTH_WEST:
        rect.x += deltax;
        rect.y += deltay;
        rect.width -= deltax;
        rect.height -= deltay;
        break;
      case NORTH:
        rect.y += deltay;
        rect.height -= deltay;
        break;
      case NORTH_EAST:
        rect.y += deltay;
        rect.width += deltax;
        rect.height -= deltay;
        break;
      case WEST:
        rect.x += deltax;
        rect.width -= deltax;
        break;
      case EAST:
        rect.width += deltax;
        break;
      case SOUTH_WEST:
        rect.x += deltax;
        rect.width -= deltax;
        rect.height += deltay;
        break;
      case SOUTH:
        rect.height += deltay;
        break;
      case SOUTH_EAST:
        rect.width += deltax;
        rect.height += deltay;
        break;
      case INTERNAL:
        rect.x += deltax;
        rect.y += deltay;
        break;
    }
    return rect;
  }

  private void updatePoints()
  {
    // 1st. line
    points[NORTH_WEST].x = x;
    points[NORTH_WEST].y = y;

    points[NORTH].x = x + width / 2;
    points[NORTH].y = y;

    points[NORTH_EAST].x = x + width;
    points[NORTH_EAST].y = y;

    // 2nd line
    points[WEST].x = x;
    points[WEST].y = y + height / 2;

    points[EAST].x = x + width;
    points[EAST].y = y + height / 2;

    // 3rd. line
    points[SOUTH_WEST].x = x;
    points[SOUTH_WEST].y = y + height;

    points[SOUTH].x = x + width / 2;
    points[SOUTH].y = y + height;

    points[SOUTH_EAST].x = x + width;
    points[SOUTH_EAST].y = y + height;
  }

  private boolean areOverlaped(Point p1, Point p2)
  {
    return Math.abs(p1.x - p2.x) < 3 &&
           Math.abs(p1.y - p2.y) < 3;
  }

  @Override
  public boolean imageUpdate(Image img, int infoflags,
                             int x, int y, int width, int height)
  {
    return true;
  }

  public int parseWidth(String text)
  {
    return parseWidth(text, getDefaultBorderWidth());
  }
  /**
   * return width in pixels from text
   * @param text
   * @param defaultWidth
   * @return
   */
  public int parseWidth(String text, int defaultWidth)
  {
    if (text == null) return defaultWidth;
    // TODO: attend units
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < text.length(); i++)
    {
      char ch = text.charAt(i);
      if (Character.isDigit(ch))
      {
        buffer.append((char)ch);
      }
    }
    text = buffer.toString();
    if (text.length() == 0) return defaultWidth;
    else return Integer.parseInt(text);
  }

  public void copyFrom(ComponentView other)
  {
    x = other.x;
    y = other.y;
    width = other.width;
    height = other.height;

    textAlign = other.textAlign;
    fontFamily = other.fontFamily;
    fontSize = other.fontSize;

    id = other.id;
    foreground = other.foreground;
    background = other.background;

    borderTopWidth = other.borderTopWidth;
    borderBottomWidth = other.borderBottomWidth;
    borderLeftWidth = other.borderLeftWidth;
    borderRightWidth = other.borderRightWidth;

    borderTopColor = other.borderTopColor;
    borderBottomColor = other.borderBottomColor;
    borderLeftColor = other.borderLeftColor;
    borderRightColor = other.borderRightColor;

    borderTopStyle = other.borderTopStyle;
    borderBottomStyle = other.borderBottomStyle;
    borderLeftStyle = other.borderLeftStyle;
    borderRightStyle = other.borderRightStyle;

    styleClass = other.styleClass;
    renderer = other.renderer;

    outputOrder = other.outputOrder;

    updatePoints();
  }

  @Override
  public Object clone() throws CloneNotSupportedException
  {
    try
    {
      ComponentView clone = getClass().newInstance();
      clone.copyFrom(this);
      return clone;
    }
    catch (Exception ex)
    {
      throw new CloneNotSupportedException();
    }
  }
}
