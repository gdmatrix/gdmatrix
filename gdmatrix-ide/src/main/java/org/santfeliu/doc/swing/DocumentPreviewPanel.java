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
package org.santfeliu.doc.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JPanel;


/**
 *
 * @author unknown
 */
public class DocumentPreviewPanel extends JPanel
  implements PropertyChangeListener
{
  private int imageWidth, imageHeight;
  private ImageIcon icon;
  private Image image = null;
  private Image scaledImage = null;
  private static final int ACCSIZE = 155;
  private Color bg;
  private String message;
  private DocumentListPanel documentListPanel = null;

  public DocumentPreviewPanel()
  {
    setPreferredSize(new Dimension(ACCSIZE, -1));
    bg = getBackground();
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public DocumentPreviewPanel(DocumentListPanel documentListPanel)
  {
    this.documentListPanel = documentListPanel;
    setPreferredSize(new Dimension(ACCSIZE, -1));
    bg = getBackground();
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public void showMessage(String message)
  {
    this.message = message;
    repaint();
  }

  public void propertyChange(PropertyChangeEvent e)
  {
    String propertyName = e.getPropertyName();

    // Make sure we are responding to the right event.
    if (propertyName.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY))
    {
      File selection = (File)e.getNewValue();
      String name;

      if (selection == null)
      {
        image = null;
        scaledImage = null;
      }
      else
      {
        name = selection.getAbsolutePath();
        /*
         * Make reasonably sure we have an image format that AWT can
         * handle so we don't try to draw something silly.
         */
        if ((name != null) && name.toLowerCase().endsWith(".jpg") || 
            name.toLowerCase().endsWith(".jpeg") || 
            name.toLowerCase().endsWith(".gif") || 
            name.toLowerCase().endsWith(".png"))
        {
          icon = new ImageIcon(name);
          image = icon.getImage();
          scaleImage();
        }
        else
        {
          image = null;
          scaledImage = null;
        }
      }
      message = null;
      repaint();
    }
  }

  private void scaleImage()
  {
    imageWidth = image.getWidth(this);
    imageHeight = image.getHeight(this);
    Insets insets = this.getInsets();
    
    int width = this.getWidth() - (insets.left + insets.right);
    int height = this.getHeight() - (insets.top + insets.bottom);
    double ratio = (double)imageWidth / (double)imageHeight;

    if (imageWidth < width && imageHeight < height)
    {
      scaledImage = image;
    }
    else
    {
      if (imageWidth > width && (int)((double)width / ratio) <= height)
      {
        imageWidth = width;
        imageHeight = (int)(imageWidth / ratio);
      }
      else if (imageHeight > height && (int)(ratio * height) <= width)
      {
        imageHeight = height;
        imageWidth = (int)(ratio * imageHeight);
      }
      scaledImage = 
        image.getScaledInstance(imageWidth, imageHeight, Image.SCALE_FAST);
    }
  }

  public void paintComponent(Graphics g)
  {
    g.setColor(bg);
    /*
     * If we don't do this, we will end up with garbage from previous
     * images if they have larger sizes than the one we are currently
     * drawing. Also, it seems that the file list can paint outside
     * of its rectangle, and will cause odd behavior if we don't clear
     * or fill the rectangle for the accessory before drawing. This might
     * be a bug in JFileChooser.
     */
    g.fillRect(0, 0, getWidth(), getHeight());
    if (message != null)
    {
      g.setColor(Color.black);
      Rectangle2D rect = g.getFontMetrics().getStringBounds(message, g);
      int xoffset = (int)(getWidth() - rect.getWidth()) / 2;
      int yoffset = getHeight() / 2;
      if (xoffset < 0) xoffset = 0;
      g.drawString(message, xoffset, yoffset);
    }
    else if (scaledImage != null)
    {
      Insets insets = getInsets();
      int x = insets.left + 
        (getWidth() - insets.left - insets.right - imageWidth) / 2;
      int y = insets.top + 
        (getHeight() - insets.top - insets.bottom - imageHeight) / 2;
      g.drawImage(scaledImage, 
                  x, y, imageWidth, imageHeight, this);
    }
  }

  public static void main(String[] args)
  {
    JFileChooser chooser = new JFileChooser();
    DocumentPreviewPanel preview = new DocumentPreviewPanel();
    chooser.setAccessory(preview);
    chooser.addPropertyChangeListener(preview);
    chooser.showDialog(null, preview.getDocumentListPanel().getLocalizedText("open"));
  }

  private void jbInit()
    throws Exception
  {
    String borderTitle = documentListPanel != null ? 
      documentListPanel.getLocalizedText("preview") + ":" : "";
    this.setBorder(BorderFactory.createTitledBorder(borderTitle));
    this.setOpaque(true);
    this.addComponentListener(new ComponentAdapter()
    {
      public void componentResized(ComponentEvent e)
      {
        if (image != null)
        {
          scaleImage();
          repaint();
        }
      }
    });
  }

  public void setDocumentListPanel(DocumentListPanel documentListPanel)
  {
    this.documentListPanel = documentListPanel;
  }

  public DocumentListPanel getDocumentListPanel()
  {
    return documentListPanel;
  }
}

