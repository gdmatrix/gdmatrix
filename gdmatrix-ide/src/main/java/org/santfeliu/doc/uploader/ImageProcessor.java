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

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author real
 */
public class ImageProcessor extends Thread
{
  private File file;
  private int maxSize;
  private String overlappedText;

  public ImageProcessor(File file)
  {
    this.file = file;
  }

  public int getMaxSize()
  {
    return maxSize;
  }

  public void setMaxSize(int maxSize)
  {
    this.maxSize = maxSize;
  }

  public String getOverlappedText()
  {
    return overlappedText;
  }

  public void setOverlappedText(String overlappedText)
  {
    this.overlappedText = overlappedText;
  }
  
  public BufferedImage processImage() throws IOException
  {
    BufferedImage sourceImage = ImageIO.read(file);    
    BufferedImage image = sourceImage;
    if (maxSize > 0)
    {
      int width = sourceImage.getWidth();
      int height = sourceImage.getHeight();
      if (width > maxSize && width >= height)
      {
        height = (maxSize * height) / width;
        width = maxSize;
      }
      else if (height > maxSize && height >= width)
      {        
        width = (maxSize * width) / height;
        height = maxSize;
      }
      Image scaledImage = sourceImage.getScaledInstance(width, height, 
        Image.SCALE_SMOOTH);
      if (scaledImage instanceof BufferedImage)
      {        
        image = (BufferedImage)scaledImage;
      }
      else
      {
        BufferedImage scaledBufferedImage = 
          new BufferedImage(width, height, sourceImage.getType());
        Graphics g = scaledBufferedImage.getGraphics();
        g.drawImage(scaledImage, 0, 0, null);
        g.dispose();
        image = scaledBufferedImage;
      }
    }
    return image;
  }
  
  public File processImageAndSave() throws IOException
  {
    if (maxSize == 0) return file;
    else
    {
      BufferedImage image = processImage();
      String filename = file.getName();
      int index = filename.lastIndexOf(".");
      String format = index == -1 ? filename : filename.substring(index + 1);
      File tempFile = File.createTempFile("processedImage", "." + format);
      ImageIO.write(image, format, tempFile);
      return tempFile;
    }
  }
}
