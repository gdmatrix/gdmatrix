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
package org.santfeliu.swing.text;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import java.util.HashMap;

import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public abstract class HighlightedEditorKit extends StyledEditorKit
  implements ViewFactory
{
  public static final int PLAIN = Font.PLAIN;
  public static final int BOLD = Font.BOLD;
  public static final int ITALIC = Font.ITALIC;

  private HashMap colors = new HashMap();
  private HashMap styles = new HashMap();

  public HighlightedEditorKit()
  {
  }

  @Override
  public abstract Document createDefaultDocument();

  @Override
  public ViewFactory getViewFactory()
  {
    return this;
  }

  public View create(Element elem)
  {
    return new HighlightView(elem, this);
  }

  public void register(int type, Color color, int style)
  {
    Integer integer = type;
    colors.put(integer, color);
    styles.put(integer, style);
  }

  public Font getFontOf(int type, Component component)
  {
    Font font = component.getFont();
    Integer style = (Integer)styles.get(type);
    if (style != null && font.getStyle() != style)
    {
      font = new Font(font.getName(), style, font.getSize());
    }
    return font;
  }

  public Color getColorOf(int type, Component component)
  {
    Color color = (Color)colors.get(type);
    if (color == null) color = component.getForeground();
    return color;
  }
}
