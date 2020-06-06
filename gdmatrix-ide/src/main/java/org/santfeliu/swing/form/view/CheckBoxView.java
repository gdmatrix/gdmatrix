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

import java.awt.Graphics;
import javax.swing.ImageIcon;
import org.santfeliu.swing.form.ComponentView;

/**
 *
 * @author realor
 */
public class CheckBoxView extends ComponentView
{
  private String variable = "var";
  private boolean checked;
  private static ImageIcon selectedIcon;
  private static ImageIcon unselectedIcon;
  private Integer tabindex;
  private String onChange;

  public CheckBoxView()
  {
    setWidth(24);
    setHeight(24);
    if (selectedIcon == null)
    {
      try
      {
        selectedIcon = new ImageIcon(getClass().getResource(
          "/org/santfeliu/swing/form/resources/images/sel_checkbox.gif"));
      }
      catch (Exception ex)
      {
      }
    }
    if (unselectedIcon == null)
    {
      try
      {
        unselectedIcon = new ImageIcon(getClass().getResource(
          "/org/santfeliu/swing/form/resources/images/unsel_checkbox.gif"));
      }
      catch (Exception ex)
      {
      }
    }
  }

  public void setVariable(String variable)
  {
    this.variable = variable;
  }

  public String getVariable()
  {
    return variable;
  }

  public void setChecked(boolean checked)
  {
    this.checked = checked;
  }

  public boolean isChecked()
  {
    return checked;
  }

  public void setTabindex(Integer tabindex)
  {
    this.tabindex = tabindex;
  }

  @Override
  public Integer getTabindex()
  {
    return tabindex;
  }
  
  public String getOnChange()
  {
    return onChange;
  }
  
  public void setOnChange(String onChange)
  {
    this.onChange = onChange;
  }

  @Override
  public void paintView(Graphics g)
  {
    int x = parseWidth(getBorderLeftWidth());
    int y = parseWidth(getBorderTopWidth());
    int width = getWidth() - parseWidth(getBorderLeftWidth()) - 
      parseWidth(getBorderRightWidth()) + 1;
    int height = getHeight() - parseWidth(getBorderTopWidth()) - 
      parseWidth(getBorderBottomWidth()) + 1;

    ImageIcon icon = checked ? selectedIcon : unselectedIcon;   
    int iconWidth = icon.getIconWidth();
    int iconHeight = icon.getIconHeight();   
    if (icon != null)
    {
      g.drawImage(icon.getImage(), 
      x + (width - iconWidth) / 2, 
      y + (height - iconHeight) / 2, this);
    }
  }
  
  @Override
  public Object clone() throws CloneNotSupportedException
  {
    CheckBoxView clone = (CheckBoxView)super.clone();
    clone.copyFrom(this);
    clone.variable = variable;
    clone.checked = checked;
    clone.tabindex = tabindex;
    return clone;
  }
}
