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
package org.santfeliu.doc.uploader;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 *
 * @author real
 */
public class ImagePreviewPanel extends JPanel
{
  private static final int LOADING_STATE = 0;
  private static final int LOADED_STATE = 1;
  private static final int SMOOTHED_STATE = 2;
  
  int thumbnailHeight = 140;
  File imageFile;
  Map<File, Thumbnail> cache = 
    Collections.synchronizedMap(new HashMap<File, Thumbnail>());

  public void clearCache()
  {
    cache.clear();
  }

  public void setImageFile(File imageFile)
  {
    this.imageFile = imageFile;
    invalidate();
    revalidate();
    repaint();
  }

  @Override
  public void paintComponent(Graphics g)
  {
    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());
    g.setColor(getForeground());
    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
      RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    Thumbnail thumbnail = cache.get(imageFile);
    if (thumbnail != null && thumbnail.state > LOADING_STATE)
    {
      g.drawImage(thumbnail.image, 0, 0, thumbnail.width, thumbnail.height, this);
      g.drawString("Width: " + thumbnail.sourceWidth, thumbnail.width + 10, 20);
      g.drawString("Height: " + thumbnail.sourceHeight, thumbnail.width + 10, 40);
    }
    else
    {
      g.drawString("Loading...", 10, 20);
      if (thumbnail == null)
      {
        thumbnail = new Thumbnail(imageFile);
        cache.put(imageFile, thumbnail);
        thumbnail.load();
      }
    }
  }

  @Override
  public Dimension getPreferredSize()
  {
    if (imageFile == null) return new Dimension(0, 0);
    else return new Dimension(0, thumbnailHeight);
  }
  
  /* thumbnail */
  class Thumbnail implements Runnable
  {
    File imageFile;
    int sourceWidth;
    int sourceHeight;
    Image image;
    int width;
    int height;
    int state = LOADING_STATE;

    public Thumbnail(File imageFile)
    {
      this.imageFile = imageFile;
    }

    public void load()
    {
      Thread thread = new Thread(this);
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }

    @Override
    public void run()
    {
      try
      {
        System.out.println("Loading..." + imageFile);
        
        // Loading
        BufferedImage sourceImage = ImageIO.read(imageFile);
        sourceImage.setAccelerationPriority(1);
        double ratio = (double)sourceImage.getWidth() / 
          (double)sourceImage.getHeight();
        this.height = thumbnailHeight;
        this.width = (int)(this.height * ratio);
        this.sourceWidth = sourceImage.getWidth();
        this.sourceHeight = sourceImage.getHeight();
        this.image = sourceImage;
        state = LOADED_STATE;
        repaint();
        
        // Smoothing
        Image scaledImage = sourceImage.getScaledInstance(width, height, 
          Image.SCALE_SMOOTH);
        BufferedImage scaledBufferedImage = 
          new BufferedImage(width, height, sourceImage.getType());
        Graphics g = scaledBufferedImage.getGraphics();
        g.drawImage(scaledImage, 0, 0, width, height, null);
        g.dispose();

        this.image = scaledBufferedImage;
        state = SMOOTHED_STATE;
        repaint();
      }
      catch (Exception ex)
      {
      }        
    }
  }  
}

