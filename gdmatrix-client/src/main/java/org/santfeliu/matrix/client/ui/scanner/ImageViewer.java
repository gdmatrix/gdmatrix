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
package org.santfeliu.matrix.client.ui.scanner;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/**
 *
 * @author realor
 */
public class ImageViewer extends JComponent
{
  BufferedImage image;
  Image scaledImage;
  double zoomFactor = 0.25;
  
  public ImageViewer(BufferedImage image)
  {
    this.image = image;
  }

  public BufferedImage getImage()
  {
    return image;
  }

  public double getZoomFactor()
  {
    return zoomFactor;
  }

  public void setZoomFactor(double zoomFactor)
  {
    if (zoomFactor >= 1) zoomFactor = 1;
    else if (zoomFactor < 0.0625) zoomFactor = 0.0625;
    this.zoomFactor = zoomFactor;
    scaledImage = null;
  }
  
  public void rotateLeft()
  {
    int srcWidth = image.getWidth();
    int srcHeight = image.getHeight();
    
    BufferedImage dest = new BufferedImage(srcHeight, srcWidth, image.getType());
    for (int x = 0; x < srcWidth; x++)
    {
      for (int y = 0; y < srcHeight; y++)
      {
        int rgb = image.getRGB(x, y);
        dest.setRGB(y, srcWidth - x - 1, rgb);
      }
    }
    image = dest;
    scaledImage = null;
    repaint();
  }

  public void rotateRight()
  {
    int srcWidth = image.getWidth();
    int srcHeight = image.getHeight();
    
    BufferedImage dest = new BufferedImage(srcHeight, srcWidth, image.getType());
    for (int x = 0; x < srcWidth; x++)
    {
      for (int y = 0; y < srcHeight; y++)
      {
        int rgb = image.getRGB(x, y);
        dest.setRGB(srcHeight - y - 1, x, rgb);
      }
    }
    image = dest;
    scaledImage = null;
    repaint();
  }  
  
  @Override
  public void paintComponent(Graphics g)
  {
    if (zoomFactor == 1)
    {
      g.drawImage(image, 0, 0, this);
    }
    else
    {
      if (scaledImage == null)
      {
        Dimension size = getScaledImageSize();
        scaledImage = image.getScaledInstance(size.width, size.height, 
          Image.SCALE_SMOOTH);
      }
      int width = scaledImage.getWidth(this);
      int height = scaledImage.getHeight(this);
      int marginx = (getWidth() - width) / 2;
      int marginy = (getHeight() - height) / 2;
      g.drawImage(scaledImage, marginx, marginy, this);
      g.setColor(new Color(200, 200, 200));
      g.drawRect(marginx, marginy, 
        scaledImage.getWidth(this), scaledImage.getHeight(this));
    }
  }

  private Dimension getScaledImageSize()
  {
    int width = (int)(image.getWidth() * zoomFactor);
    int height = (int)(image.getHeight() * zoomFactor);
    return new Dimension(width, height);    
  }
  
  @Override
  public Dimension getPreferredSize()
  {
    if (zoomFactor == 1)
    {
      return new Dimension(image.getWidth(), image.getHeight());
    }
    else
    {
      return getScaledImageSize();
    }
  }
}
