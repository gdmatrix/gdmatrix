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
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.net.URL;
import javax.swing.JLabel;
import org.santfeliu.swing.form.ComponentView;

/**
 *
 * @author unknown
 */
public class ImageView extends ComponentView
{
  private String url;
  private String alt = "";

  public ImageView()
  {
    setWidth(128);
    setHeight(128);
  }
  
  @Override
  public void paintView(Graphics g)
  {
    Image image = null;
    int x = parseWidth(getBorderLeftWidth());
    int y = parseWidth(getBorderTopWidth());
    int width = getWidth() - parseWidth(getBorderLeftWidth()) - 
      parseWidth(getBorderRightWidth()) + 1;
    int height = getHeight() - parseWidth(getBorderTopWidth()) - 
      parseWidth(getBorderBottomWidth()) + 1;
    try
    {
      image = Toolkit.getDefaultToolkit().getImage(new URL(url));
      MediaTracker tracker = new MediaTracker(new JLabel());
      tracker.addImage(image, 0);
      tracker.waitForAll();
      g.drawImage(image, x, y, width, height, this);
    }
    catch (Throwable ex)
    {
      g.setColor(Color.lightGray);
      g.fillRect(x, y, width, height);
    }
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public String getUrl()
  {
    return url;
  }

  public void setAlt(String alt)
  {
    this.alt = alt;
  }

  public String getAlt()
  {
    return alt;
  }

  @Override
  public Object clone() throws CloneNotSupportedException
  {
    ImageView clone = (ImageView)super.clone();
    clone.url = url;
    clone.alt = alt;
    return clone;
  }
}
