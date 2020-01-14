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

import java.awt.image.BufferedImage;

/**
 *
 * @author realor
 */
public class ImagePanel extends javax.swing.JPanel
{
  ImageViewer viewer;
    
  public ImagePanel(BufferedImage image)
  {
    initComponents();
    
    this.viewer = new ImageViewer(image);
    this.scrollPane.setViewportView(viewer);

    String color;
    switch (image.getType())
    {
      case BufferedImage.TYPE_BYTE_BINARY:
        color = "B/W";
        break;
      case BufferedImage.TYPE_BYTE_GRAY:
        color = "GRAY";
        break;
      default:
        color = "RGB";
        break;
    }
    String type = color + " " + image.getWidth() + " x " + image.getHeight() + " px, type: " + 
      image.getType() + ", components: " +
      image.getColorModel().getComponentSize()[0] + ", bits: " + 
      image.getColorModel().getPixelSize();
    this.imageTypeLabel.setText(type);
  }

  public BufferedImage getImage()
  {
    return viewer.getImage();
  }
  
  public void setZoomFactor(double factor)
  {
    viewer.setZoomFactor(factor);
    scrollPane.setViewportView(viewer);
  }
  
  public double getZoomFactor()
  {
    return viewer.getZoomFactor();
  }
    
  public void zoomIn()
  {    
    setZoomFactor(viewer.getZoomFactor() * 2);
  }
  
  public void zoomOut()
  {
    setZoomFactor(viewer.getZoomFactor() / 2);  
  }
  
  public void rotateLeft()
  {
    viewer.rotateLeft();
    scrollPane.setViewportView(viewer);
  }

  public void rotateRight()
  {
    viewer.rotateRight();
    scrollPane.setViewportView(viewer);
  }
  
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {

    scrollPane = new javax.swing.JScrollPane();
    imageTypeLabel = new javax.swing.JLabel();

    setLayout(new java.awt.BorderLayout());
    add(scrollPane, java.awt.BorderLayout.CENTER);
    add(imageTypeLabel, java.awt.BorderLayout.SOUTH);
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel imageTypeLabel;
  private javax.swing.JScrollPane scrollPane;
  // End of variables declaration//GEN-END:variables
}
