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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.Vector;
import javax.swing.ImageIcon;
import org.santfeliu.swing.form.ComponentView;

/**
 *
 * @author realor
 */
public class SelectBoxView extends ComponentView
{
  private static final int BAR_WIDTH = 16;
  private static ImageIcon arrowIcon;

  private String variable = "var";
  private Vector options = new Vector();
  private String connection;
  private String sql;
  private String username;
  private String password;
  private String dataref;
  private Integer tabindex;
  private String disabled;
  private Integer size;
  private Boolean multiple;
  private String onChange;
  private Boolean translate;
    
  public SelectBoxView()
  {
    setWidth(128);
    setHeight(24);
    setFontSize(DEFAULT_FONT_SIZE);

    if (arrowIcon == null)
    {
      try
      {
        arrowIcon = new ImageIcon(getClass().getResource(
          "/org/santfeliu/swing/form/resources/images/arrow.gif"));
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
    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
      RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    g.setColor(Color.black);
    Rectangle rect = getSelectBoxBounds();
  
    int blw = 1;
    int brw = 1;
    int btw = 1;
    int bbw = 1;

    int x = rect.width - brw - BAR_WIDTH;
    int y = rect.y + btw;
    int width = BAR_WIDTH;
    int height = rect.height - btw - bbw;

    g.setColor(getForeground() == null ?
      Color.black : getForeground());
    g.setFont(getFont());
    Rectangle boxBounds = getSelectBoxBounds();
    int optionHeight = getOptionHeight();
    if (options.size() > 0)
    {
      int visibleOptions = (getSize() == null) ? 1 : getSize();
      visibleOptions = Math.min(visibleOptions, options.size());
      y = boxBounds.y;
      for (int i = 0; i < visibleOptions; i++)
      {
        String[] option = (String[])options.get(i);
        String label = option[0];
        Rectangle2D bounds = g.getFontMetrics().getStringBounds(label, g);
        double labelY = y + (optionHeight - bounds.getHeight()) / 2 +
          bounds.getHeight();
        g.drawString(label, blw + 1, (int)labelY);
        y += optionHeight;
      }
    }

    // draw arrow
    if (getSize() == null)
    {
      g.setColor(Color.lightGray);
      y = boxBounds.y + 1;
      g.fillRect(x, y, width, height);
      g.setColor(Color.gray);
      g.drawRect(x, y, width, height);

      int iconHeight = arrowIcon.getIconHeight();
      int iconWidth = arrowIcon.getIconWidth();
      g.drawImage(arrowIcon.getImage(),
        x + (width - iconWidth) / 2,
        y + (height - iconHeight) / 2,
        this);
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
  
  // String[2] options
  public Vector getOptions()
  {
    return options;
  }

  public void setConnection(String connection)
  {
    this.connection = nullWhenEmpty(connection);
  }

  public String getConnection()
  {
    return connection;
  }

  public void setSql(String sql)
  {
    this.sql = nullWhenEmpty(sql);
  }

  public String getSql()
  {
    return sql;
  }

  public void setUsername(String username)
  {
    this.username = nullWhenEmpty(username);
  }

  public String getUsername()
  {
    return username;
  }

  public void setPassword(String password)
  {
    this.password = nullWhenEmpty(password);
  }

  public String getPassword()
  {
    return password;
  }

  public String getDataref()
  {
    return dataref;
  }

  public void setDataref(String dataref)
  {
    this.dataref = dataref;
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
  
  public String getOnChange()
  {
    return onChange;
  }
  
  public void setOnChange(String onChange)
  {
    this.onChange = onChange;
  }
  
  public Integer getSize()
  {
    return size;
  }

  public void setSize(Integer size)
  {
    if (size != null && size == 0) size = null;
    this.size = size;
  }

  public Boolean getMultiple()
  {
    return multiple;
  }

  public void setMultiple(Boolean multiple)
  {
    this.multiple = multiple;
  }

  public Boolean getTranslate()
  {
    return translate;
  }

  public void setTranslate(Boolean translate)
  {
    this.translate = translate;
  }

  @Override
  public void setBorderLeftWidth(String borderLeftWidth)
  {
    // border is always 1
  }

  @Override
  public void setBorderRightWidth(String borderRightWidth)
  {
    // border is always 1
  }

  @Override
  public void setBorderTopWidth(String borderTopWidth)
  {
    // border is always 1
  }

  @Override
  public void setBorderBottomWidth(String borderBootomWidth)
  {
    // border is always 1
  }

  @Override
  protected void paintBackground(Graphics g)
  {
    Color background = getBackground();
    if (background == null) background = Color.white;
    g.setColor(background);
    Rectangle rect = getSelectBoxBounds();
    g.fillRect(rect.x, rect.y, rect.width, rect.height);
  }

  @Override
  protected void paintBorder(Graphics g)
  {
    Rectangle rect = getSelectBoxBounds();
    g.setColor(Color.gray);
    g.drawRect(rect.x, rect.y, rect.width, rect.height);
  }

  public Rectangle getSelectBoxBounds()
  {
    int optionHeight = getOptionHeight();
    int sizeValue = (getSize() == null) ? 1 : getSize();

    int boxHeight = (sizeValue * optionHeight) + 4; // border + padding
    int boxX = 0;
    int boxY = 1 + (getContentHeight() - boxHeight) / 2;
    int boxWidth = getWidth();
    
    return new Rectangle(boxX, boxY, boxWidth, boxHeight);
  }

  private int getOptionHeight()
  {
    int fontSize = getFontSize();
    if (fontSize == 0) fontSize = DEFAULT_FONT_SIZE;
    int optionHeight = fontSize + 2; // padding (1 top + 1 bottom)
    return optionHeight;
  }

  @Override
  public Object clone() throws CloneNotSupportedException
  {
    SelectBoxView clone = (SelectBoxView)super.clone();
    clone.variable = variable;
    clone.options.addAll(options);
    clone.connection = connection;
    clone.sql = sql;
    clone.username = username;
    clone.password = password;
    clone.dataref = dataref;
    clone.tabindex = tabindex;
    clone.disabled = disabled;
    clone.size = size;
    clone.multiple = multiple;
    return clone;
  }
}
