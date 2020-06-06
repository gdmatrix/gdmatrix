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
import javax.swing.JButton;
import org.santfeliu.swing.form.ComponentView;

/**
 *
 * @author realor
 */
public class ButtonView extends ComponentView
{
  private JButton button = new JButton();
  private String variable = "action";
  private String text = "Send";
  private Integer tabindex;

  public ButtonView()
  {
    setWidth(64);
    setHeight(24);
    setTextAlign("center");
  }

  public String getVariable()
  {
    return variable;
  }

  public void setVariable(String variable)
  {
    this.variable = variable;
  }

  public String getText()
  {
    return text;
  }

  public void setText(String text)
  {
    this.text = text;
  }

  @Override
  public Integer getTabindex()
  {
    return tabindex;
  }

  public void setTabindex(Integer tabindex)
  {
    this.tabindex = tabindex;
  }

  @Override
  public void paintView(Graphics g)
  {
    button.setForeground(getForeground() == null ?
      Color.black : getForeground());
    Color background = getBackground();
    if (background != null)
    {
      button.setBackground(background);
    }
    button.setSize(getWidth() - parseWidth(getBorderLeftWidth()) -
      parseWidth(getBorderRightWidth()),
      getHeight() - parseWidth(getBorderTopWidth()) -
      parseWidth(getBorderBottomWidth()));
    String align = getTextAlign();
    String value = text;
    button.setText(value);

    // apply font settings
    Font font = getFont();
    button.setFont(font);

    // apply text alignment
    if ("left".equals(align))
    {
      button.setHorizontalAlignment(JButton.LEFT);
    }
    else if ("center".equals(align))
    {
      button.setHorizontalAlignment(JButton.CENTER);
    }
    else if ("right".equals(align))
    {
      button.setHorizontalAlignment(JButton.RIGHT);
    }
    else
    {
      button.setHorizontalAlignment(JButton.LEFT);
    }
    g.translate(parseWidth(getBorderLeftWidth()),
      parseWidth(getBorderTopWidth()));
    button.paint(g);
    g.translate(-parseWidth(getBorderLeftWidth()),
      -parseWidth(getBorderTopWidth()));
  }

  @Override
  public Object clone() throws CloneNotSupportedException
  {
    ButtonView clone = (ButtonView)super.clone();
    clone.variable = variable;
    clone.text = text;
    clone.tabindex = tabindex;
    return clone;
  }
}
