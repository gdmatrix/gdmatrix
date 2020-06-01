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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.PlainView;
import javax.swing.text.Segment;
import javax.swing.text.Utilities;


/**
 *
 * @author realor
 */
public class HighlightView extends PlainView
{
  private HighlightedEditorKit kit;

  public HighlightView(Element elem, HighlightedEditorKit kit)
  {
    super(elem);
    this.kit = kit;
  }

  @Override
  protected int drawUnselectedText(Graphics g, int x, int y, int p0, int p1)
    throws BadLocationException
  {
    Component component = getContainer();
    HighlightedDocument doc = (HighlightedDocument)getDocument();
    Segment text = getLineBuffer();
    int t = findIndexOfTokenContaining(p0);
    while (p0 < p1)
    {
      int p2 = getToken(t + 1).position;
      if (p2 > p1) p2 = p1;
      doc.getText(p0, p2 - p0, text);
      int type = getToken(t).type;
      g.setColor(kit.getColorOf(type, component));
      g.setFont(kit.getFontOf(type, component));
      Graphics2D g2 = (Graphics2D)g;
      g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
      g2.setRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST, 100);
      x = Utilities.drawTabbedText(text, x, y, g, this, p0);
      p0 = p2;
      t++;
    }
    doc.repaintAll(component); // repaint all when necessary
    return x;
  }

  @Override
  protected int drawSelectedText(Graphics g, int x, int y, int p0, int p1)
    throws BadLocationException
  {
    return drawUnselectedText(g, x, y, p0, p1);
  }
  
  private int findIndexOfTokenContaining(int p0)
  {
    HighlightedDocument doc = (HighlightedDocument)getDocument();
    ArrayList tokens = doc.getTokens();
    boolean found = false;
    int index = 0;
    while (!found && index < tokens.size() - 1)
    {
      Token token1 = (Token)tokens.get(index);
      Token token2 = (Token)tokens.get(index + 1);
      if (token1.position <= p0 && p0 < token2.position)
      {
        found = true;
      }
      else
      {
        index++;
      }
    }
    return found ? index : 0;
  }
  
  private Token getToken(int index)
  {
    HighlightedDocument doc = (HighlightedDocument)getDocument();
    ArrayList tokens = doc.getTokens();
    return (Token)tokens.get(index);
  }
}
