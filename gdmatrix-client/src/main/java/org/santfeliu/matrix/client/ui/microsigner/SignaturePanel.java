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
package org.santfeliu.matrix.client.ui.microsigner;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;

import java.awt.geom.Rectangle2D;

import java.net.URL;

import java.security.MessageDigest;
import java.security.cert.X509Certificate;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;


/**
 *
 * @author realor
 */
public class SignaturePanel extends JPanel implements Runnable
{
  private Thread animator;
  private boolean stop = false;
  private int step = 0;
  private List<Point> glyph = new ArrayList<Point>();
  private ImageIcon signatureIcon;
  private X509Certificate certificate;
  private byte[] data = {5, -8, 52, -46, 34, 111, -85, -52};
  private String signerName = "Signer name";

  public SignaturePanel()
  {
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  private void jbInit() throws Exception
  {
    setBorder(new LineBorder(Color.gray, 1));    
    URL url;
    url = getClass().getClassLoader().getResource(
      "org/santfeliu/matrix/client/ui/microsigner/resources/images/signature.gif");
    signatureIcon = new ImageIcon(url);
  }
  
  public void setCertificate(X509Certificate certificate, byte[] signatureData)
  {
    this.certificate = certificate;
    try
    {
      signerName = null;
      MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
      data = messageDigest.digest(signatureData);
      glyph.clear();
      String subject = certificate.getSubjectDN().getName();
      int index = subject.indexOf("CN=");
      if (index != -1)
      {
        subject = subject.substring(index + 3);
        index = subject.indexOf(",");
        if (index != -1)
        {
          signerName = subject.substring(0, index);
        }
        else
        {
          signerName = subject.substring(0);
        }
      }
    }
    catch (Exception ex)
    {
    }
  }
  
  @Override
  public void paintComponent(Graphics g)
  {
    g.setColor(Color.white);
    ((Graphics2D)g).setRenderingHint(
      RenderingHints.KEY_ANTIALIASING, 
      RenderingHints.VALUE_ANTIALIAS_ON);

    ((Graphics2D)g).setRenderingHint(
      RenderingHints.KEY_TEXT_ANTIALIASING, 
      RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    g.fillRect(0, 0, getWidth(), getHeight());
    g.setColor(Color.black);
    String text = MicroSigner.getLocalizedText("Signing");
    g.drawString(text, 20, 20);
    for (int i = 1; i < glyph.size() - 2; i++)
    {
      Point p1 = glyph.get(i);
      Point p2 = glyph.get(i + 1);
      g.drawLine(p1.x, p1.y, p2.x, p2.y);
    }
    if (glyph.size() > 0)
    {
      Point lastPoint = glyph.get(glyph.size() - 1);
      if (signatureIcon != null)
      {
        Image image = signatureIcon.getImage();
        int imageWidth = image.getWidth(this);
        int imageHeight = image.getHeight(this);
        g.drawImage(image, lastPoint.x, lastPoint.y - imageHeight, 
          imageWidth, imageHeight, this);
      }
    }
    if (signerName != null)
    {
      Rectangle2D rect = g.getFontMetrics().getStringBounds(signerName, g);
      int rectWidth = (int)rect.getWidth();
      int rectHeight = (int)rect.getHeight();
      g.drawString(signerName, 
        getWidth() - rectWidth - 20, 
        getHeight() - rectHeight);
    }
  }
  
  public void run()
  {
    while (!stop)
    {
      try
      {
        Thread.sleep(10);
      }
      catch (Exception ex)
      {
      }
      
      double elem1 = data[0] / 128.0;
      double elem2 = data[1 % data.length] / 128.0;
      double elem3 = data[2 % data.length] / 128.0;
      double elem4 = data[3 % data.length] / 128.0;
      double elem5 = data[4 % data.length] / 128.0;
      double elem6 = data[5 % data.length] / 128.0;
      double elem7 = data[6 % data.length] / 128.0;
      double elem8 = data[7 % data.length] / 128.0;
      
      int width = getWidth();
      int height = getHeight();
      double alfa = step * 0.2;
      double cx = width / 2 - (width / 4) * Math.cos(elem7 * alfa + elem8);
      double cy = height / 2;
      double rx = (width * 0.2 - 20) * Math.sin(elem1 * (alfa + elem5) + elem3);
      double ry = (height * 0.5 - 40) * Math.cos(elem2 * (alfa + elem6) + elem4);

      double x = cx + Math.cos(alfa) * rx;
      double y = cy - Math.sin(alfa) * ry;
      glyph.add(new Point((int)Math.round(x), (int)Math.round(y)));
      step++;
      
      repaint();
    }
  }
  
  public void startAnimation()
  {
    if (animator == null)
    {
      animator = new Thread(this);
      animator.start();
    }
  }
  
  public void stopAnimation()
  {
    if (animator != null)
    {
      stop = true;
      animator = null;
    }
  }
  
  public static void main(String args[])
  {
    try
    {
      JFrame frame = new JFrame();
      frame.setSize(600, 300);
      SignaturePanel sp = new SignaturePanel();
      frame.getContentPane().add(sp);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      sp.startAnimation();
      frame.setVisible(true);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
