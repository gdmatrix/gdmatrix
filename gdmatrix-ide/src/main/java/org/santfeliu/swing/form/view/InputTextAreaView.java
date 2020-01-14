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
import java.awt.Graphics;
import javax.swing.ImageIcon;

/**
 *
 * @author unknown
 */
public class InputTextAreaView extends InputTextView
{
  private static final int SCROLL_WIDTH = 16;
  private static ImageIcon scrollIcon;
  private Integer tabindex;

  public InputTextAreaView()
  {
    setWidth(256);
    setHeight(64);

    if (scrollIcon == null)
    {
      try
      {
        scrollIcon = new ImageIcon(getClass().getResource(
          "/org/santfeliu/swing/form/resources/scroll.gif"));
      }
      catch (Exception ex)
      {
      }
    }
  }

  @Override
  public int getDefaultBorderWidth()
  {
    return 1;
  }

  @Override
  public void paintView(Graphics g)
  {
    if (getBackground() == null)
    {
      int width = getWidth();
      int height = getHeight();
      g.setColor(Color.white);
      g.fillRect(0, 0, width, height);
      if (getBorderTopWidth() == null)
      {
        g.setColor(Color.lightGray);
        g.drawLine(0, 1, width, 1);
      }
    }
  
    int defaultBorderWidth = getDefaultBorderWidth();
    int brw = parseWidth(getBorderRightWidth(), defaultBorderWidth);
    int btw = parseWidth(getBorderTopWidth(), defaultBorderWidth);
    int bbw = parseWidth(getBorderBottomWidth(), defaultBorderWidth);
    int x = getWidth() - brw - SCROLL_WIDTH;
    int y = btw;
    int width = SCROLL_WIDTH;
    int height = getHeight() - btw - bbw;
    
    g.setColor(Color.lightGray);
    g.fillRect(x, y, width, height);
    g.setColor(Color.gray);
    g.drawRect(x, y, width, height);

    int iconHeight = scrollIcon.getIconHeight();
    int iconWidth = scrollIcon.getIconWidth();
    g.drawImage(scrollIcon.getImage(), 
      x + (width - iconWidth) / 2, 
      y + (height - iconHeight) / 2,
      this);
  }
  
  @Override
  public void setTabindex(Integer tabindex)
  {
    this.tabindex = tabindex;
  }

  @Override
  public Integer getTabindex()
  {
    return tabindex;
  }

  @Override
  public Object clone() throws CloneNotSupportedException
  {
    InputTextAreaView clone = (InputTextAreaView)super.clone();
    clone.tabindex = tabindex;
    return clone;
  }
}
