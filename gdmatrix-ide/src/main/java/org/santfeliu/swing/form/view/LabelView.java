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
package org.santfeliu.swing.form.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import java.awt.Graphics2D;

import java.awt.RenderingHints;


import javax.swing.JLabel;

import org.santfeliu.swing.form.ComponentView;


/**
 *
 * @author unknown
 */
public class LabelView extends ComponentView
{
  private String text = "Text";
  private String forElement;
  private JLabel label = new JLabel();

  public LabelView()
  {
    setWidth(128);
    setHeight(24);
  }

  @Override
  public void paintView(Graphics g)
  {
    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
      RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    label.setOpaque(false);
    if (forElement == null)
    {
      label.setForeground(Color.RED);
    }
    else
    {
      label.setForeground(getForeground() == null ? 
        Color.black : getForeground());
    }
    label.setSize(getWidth() - parseWidth(getBorderLeftWidth()) - 
      parseWidth(getBorderRightWidth()), 
      getHeight() - parseWidth(getBorderTopWidth()) - 
      parseWidth(getBorderBottomWidth()));
    String align = getTextAlign();
    String value = text;
    label.setText(value);
    
    // apply font settings
    Font font = getFont();
    label.setFont(font);
    
    // apply text alignment
    if ("left".equals(align))
    {
      label.setHorizontalAlignment(JLabel.LEFT);
    }
    else if ("center".equals(align))
    {
      label.setHorizontalAlignment(JLabel.CENTER);
    }
    else if ("right".equals(align))
    {
      label.setHorizontalAlignment(JLabel.RIGHT);
    }
    else
    {
      label.setHorizontalAlignment(JLabel.LEFT);
    }
    g.translate(parseWidth(getBorderLeftWidth()), 
      parseWidth(getBorderTopWidth()));
    label.paint(g);
    g.translate(-parseWidth(getBorderLeftWidth()), 
      -parseWidth(getBorderTopWidth()));
  }
  
  @Override
  protected String getAccessibilityInfo(boolean showIds, boolean showTabIndexes, 
    boolean showCoordinates, boolean showOutputOrder)
  {
    StringBuilder sb = new StringBuilder();
    if (showIds)
    {
      sb.append(getId() == null ? "?" : getId());
      sb.append(getForElement() == null ? "" : " for: " + getForElement());    
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
  
  public void setText(String text)
  {
    this.text = nullWhenEmpty(text);
  }

  public String getText()
  {
    return text;
  }

  public String getForElement()
  {
    return forElement;
  }

  public void setForElement(String forElement)
  {
    this.forElement = nullWhenEmpty(forElement);
  }
  
  @Override
  public Object clone() throws CloneNotSupportedException
  {
    LabelView clone = (LabelView)super.clone();
    clone.copyFrom(this);
    clone.text = text;
    clone.forElement = forElement;
    return clone;
  }
}
