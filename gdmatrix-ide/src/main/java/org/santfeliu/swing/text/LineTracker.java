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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

/**
 *
 * @author realor
 */
public class LineTracker implements Highlighter.HighlightPainter,
  CaretListener, MouseListener, MouseMotionListener
{
  private JTextComponent textComponent;
  private Color color;
  private Rectangle lastView;
  private boolean enabled;
  private Object tag;

  public LineTracker(JTextComponent component)
  {
    this(component, null);
    setLighterColor(component.getSelectionColor(), 2);
  }

  public LineTracker(JTextComponent component, Color color)
  {
    this.textComponent = component;
    setColor(color);
  }

  public void setColor(Color color)
  {
    this.color = color;
  }

  public void setLighterColor(Color color, double factor)
  {
    int red = Math.min(255, (int) (color.getRed() * factor));
    int green = Math.min(255, (int) (color.getGreen() * factor));
    int blue = Math.min(255, (int) (color.getBlue() * factor));
    setColor(new Color(red, green, blue));
  }

  public boolean isEnabled()
  {
    return enabled;
  }

  public void setEnabled(boolean enabled)
  {
    if (this.enabled != enabled)
    {
      if (enabled)
      {
        textComponent.addCaretListener(this);
        textComponent.addMouseListener(this);
        textComponent.addMouseMotionListener(this);
        try
        {
          tag = textComponent.getHighlighter().addHighlight(0, 0, this);
        }
        catch (BadLocationException ble)
        {
        }
      }
      else
      {
        textComponent.removeCaretListener(this);
        textComponent.removeMouseListener(this);
        textComponent.removeMouseMotionListener(this);
        textComponent.getHighlighter().removeHighlight(tag);
      }
      this.enabled = enabled;
    }
  }

  @Override
  public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c)
  {
    try
    {
      Rectangle r = c.modelToView(c.getCaretPosition());
      g.setColor(color);
      g.fillRect(0, r.y, c.getWidth(), r.height);

      if (lastView == null)
      {
        lastView = r;
      }
    }
    catch (BadLocationException ble)
    {
      System.out.println(ble);
    }
  }

  private void resetHighlight()
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          int offset = textComponent.getCaretPosition();
          Rectangle currentView = textComponent.modelToView(offset);

          if (lastView != null && lastView.y != currentView.y)
          {
            textComponent.repaint(0, lastView.y, textComponent.getWidth(),
              lastView.height);
            lastView = currentView;
          }
        }
        catch (BadLocationException ble)
        {
        }
      }
    });
  }

  @Override
  public void caretUpdate(CaretEvent e)
  {
    resetHighlight();
  }

  @Override
  public void mousePressed(MouseEvent e)
  {
    resetHighlight();
  }

  @Override
  public void mouseClicked(MouseEvent e)
  {
  }

  @Override
  public void mouseEntered(MouseEvent e)
  {
  }

  @Override
  public void mouseExited(MouseEvent e)
  {
  }

  @Override
  public void mouseReleased(MouseEvent e)
  {
  }

  @Override
  public void mouseDragged(MouseEvent e)
  {
    resetHighlight();
  }

  @Override
  public void mouseMoved(MouseEvent e)
  {
  }
}
