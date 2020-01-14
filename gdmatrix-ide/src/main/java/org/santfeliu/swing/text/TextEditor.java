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
package org.santfeliu.swing.text;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author realor
 */
public class TextEditor extends JPanel
{
  private int margin;
  private final BorderLayout layout = new BorderLayout();    
  private final JTextPane textPane = new JTextPane();
  private NumbersPanel numbersPanel = new NumbersPanel();
  private final JScrollPane scrollPane = new JScrollPane(textPane);
  private Color numberForegroundColor = new Color(80, 80, 80);
  private Color numberBackgroundColor = new Color(255, 255, 230);
  private final LineTracker lineTracker;

  public TextEditor()
  {
    setLayout(layout);
    add(numbersPanel, BorderLayout.WEST);
    add(scrollPane, BorderLayout.CENTER);
    textPane.setMargin(new Insets(0, 1, 0, 0));
    textPane.putClientProperty("caretWidth", 2);
    
    lineTracker = 
      new LineTracker(textPane, new Color(240, 240, 255));
    lineTracker.setEnabled(true);
    
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    scrollPane.getViewport().addChangeListener(new ChangeListener()
    {
      @Override
      public void stateChanged(ChangeEvent e)
      {
        numbersPanel.repaint();
      }
    }) ;
  }

  public JTextPane getTextPane()
  {
    return textPane;
  }
  
  public LineTracker getLineTracker()
  {
    return lineTracker;
  }

  public Color getNumberForegroundColor()
  {
    return numberForegroundColor;
  }

  public void setNumberForegroundColor(Color numberForegroundColor)
  {
    this.numberForegroundColor = numberForegroundColor;
  }
  
  public Color getNumberBackgroundColor()
  {
    return numberBackgroundColor;
  }

  public void setNumberBackgroundColor(Color numberBackgroundColor)
  {
    this.numberBackgroundColor = numberBackgroundColor;
  }

  private void updateMargin()
  {
    Graphics g = getGraphics();
    margin = 6 + (int)getFontMetrics(textPane.getFont()).
      getStringBounds("0000", g).getWidth();
  }

  public class NumbersPanel extends JPanel
  {
    @Override
    public Dimension getPreferredSize()
    {
      updateMargin();
      return new Dimension(margin, getWidth());
    }

    @Override
    public void paint(Graphics g)
    {
      Graphics2D g2 = (Graphics2D)g;
     
      g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
      g2.setRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST, 100);

      g2.setFont(textPane.getFont());

      g2.setColor(numberBackgroundColor);
      g2.fillRect(0, 0, getWidth(), getHeight());
      g2.setColor(Color.GRAY);
      g2.drawLine(getWidth() - 2, 0, getWidth() - 2, getHeight());
      g2.setColor(numberForegroundColor);

      int descent = g.getFontMetrics().getDescent();
      int charHeight = g.getFontMetrics().getHeight();
      int charWidth = 
        (int)g2.getFontMetrics().getStringBounds("0", g).getWidth();      

      Rectangle rect = scrollPane.getViewport().getViewRect();
      int minY = (int)rect.getMinY();
      int maxY = minY + (int)rect.getHeight();

      int firstLine = minY / charHeight;
      int lastLine = (maxY / charHeight) + 1;
      
      for (int line = firstLine; line <= lastLine; line++)
      {
        String lineNumber = String.valueOf(line);
        if (lineNumber.length() > 4)
        {
          lineNumber = lineNumber.substring(lineNumber.length() - 4);
        }
        int x = margin - 4 - lineNumber.length() * charWidth;
        int y = line * charHeight - minY - descent;
        g2.drawString(lineNumber, x, y);
      }
    }
  }
}
