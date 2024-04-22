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
import org.santfeliu.swing.form.ComponentView;

/**
 *
 * @author realor
 */
public class InputTextView extends ComponentView
{
  private String variable = "var";
  private String format;
  private Integer maxLength;
  private boolean required;
  private Integer tabindex;
  private String disabled;
  private String info;

  public InputTextView()
  {
    setWidth(128);
    setHeight(24);
  }

  @Override
  public int getDefaultBorderWidth()
  {
    return 2;
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
        g.drawLine(0, 2, width, 2);
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

  public void setFormat(String format)
  {
    this.format = nullWhenEmpty(format);
  }

  public String getFormat()
  {
    return format;
  }

  public void setMaxLength(Integer maxLength)
  {
    this.maxLength = maxLength;
  }

  public Integer getMaxLength()
  {
    return maxLength;
  }

  public void setRequired(boolean required)
  {
    this.required = required;
  }

  public boolean isRequired()
  {
    return required;
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

  public void setDisabled(String disabled)
  {
    this.disabled = disabled;
  }

  public String getDisabled()
  {
    return disabled;
  }

  public String getInfo()
  {
    return info;
  }

  public void setInfo(String info)
  {
    this.info = info;
  }
  
  @Override
  public Object clone() throws CloneNotSupportedException
  {
    InputTextView clone = (InputTextView)super.clone();
    clone.variable = variable;
    clone.format = format;
    clone.maxLength = maxLength;
    clone.required = required;
    clone.tabindex = tabindex;
    clone.disabled = disabled;
    clone.info = info;
    return clone;
  }
}
