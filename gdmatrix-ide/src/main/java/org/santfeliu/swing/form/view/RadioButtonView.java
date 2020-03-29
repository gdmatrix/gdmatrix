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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import org.santfeliu.swing.form.ComponentView;

/**
 *
 * @author realor
 */
public class RadioButtonView extends ComponentView
{
  private String variable = "var";
  private String value;
  private String format;
  private boolean checked;
  private Integer tabindex;
  private String onChange;

  public RadioButtonView()
  {
    setWidth(24);
    setHeight(24);
  }

  public void paintView(Graphics g)
  {
    int x = parseWidth(getBorderLeftWidth());
    int y = parseWidth(getBorderTopWidth());
    int width = getWidth() - parseWidth(getBorderLeftWidth()) - 
      parseWidth(getBorderRightWidth());
    int height = getHeight() - parseWidth(getBorderTopWidth()) - 
      parseWidth(getBorderBottomWidth());
    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
      RenderingHints.VALUE_ANTIALIAS_ON);
    
    g.setColor(Color.white);
    g.fillOval(x, y, width, height);
    g.setColor(Color.black);
    g.drawOval(x, y, width, height);
    if (checked)
    {
      g.fillOval(
        x + width / 3, 
        y + height / 3, 
        1 + width / 3, 
        1 + height / 3);
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

  public void setValue(String value)
  {
    this.value = nullWhenEmpty(value);
  }

  public String getValue()
  {
    return value;
  }

  public void setFormat(String format)
  {
    this.format = nullWhenEmpty(format);
  }

  public String getFormat()
  {
    return format;
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
  public Object clone() throws CloneNotSupportedException
  {
    RadioButtonView clone = (RadioButtonView)super.clone();
    clone.variable = variable;
    clone.value = value;
    clone.format = format;
    clone.checked = checked;
    clone.tabindex = tabindex;
    return clone;
  }
}
