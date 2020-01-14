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
package org.santfeliu.swing.palette;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author unknown
 */
public class ElementLabel extends JLabel
  implements MouseListener, Transferable
{
  CategoryPane categoryPane;
  String elementName;
  String displayName;
  HashMap<String, String> attributes = new HashMap();

  private ArrayList listeners = new ArrayList();
  private boolean selected = false;
  private int borderType = 0;

  public ElementLabel(CategoryPane categoryPane)
  {
    try
    {
      this.categoryPane = categoryPane;
      initComponents();
    }
    catch (Exception ex)
    {
    }
  }

  public String getElementName()
  {
    return elementName;
  }

  public String getDisplayName()
  {
    return displayName == null ? elementName : displayName;
  }

  public String getAttribute(String name)
  {
    return attributes.get(name);
  }

  public void addActionListener(ActionListener l)
  {
    listeners.add(l);
  }

  public void removeActionListener(ActionListener l)
  {
    listeners.remove(l);
  }
  
  public void setSelected(boolean selected)
  {
    this.selected = selected;
    if (selected)
    {
      borderType = -1;
      repaint();
    }
    else
    {
      borderType = 0;
      repaint();
    }
  }

  public boolean isSelected()
  {
    return selected;
  }

  public CategoryPane getCategoryPane()
  {
    return categoryPane;
  }
  
  @Override
  public void paintComponent(Graphics g)
  {
    super.paintComponent(g);
    Color shadowColor = getBackground().darker();
    if (borderType == 1)
    {
      g.setColor(Color.white);
      g.drawLine(0, 0, getWidth() - 1, 0);
      g.drawLine(0, 0, 0, getHeight() - 1);

      g.setColor(shadowColor);
      g.drawLine(0, getHeight() - 1, getWidth() - 1, getHeight() - 1);
      g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight() - 1);
    }
    else if (borderType == -1)
    {
      g.setColor(shadowColor);
      g.drawLine(0, 0, getWidth() - 1, 0);
      g.drawLine(0, 0, 0, getHeight() - 1);

      g.setColor(Color.white);
      g.drawLine(0, getHeight() - 1, getWidth() - 1, getHeight() - 1);
      g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight() - 1);
    }
  }

  /* Transferable interface */
  @Override
  public DataFlavor[] getTransferDataFlavors()
  {
    return new DataFlavor[]{DataFlavor.stringFlavor};
  }

  @Override
  public boolean isDataFlavorSupported(DataFlavor flavor)
  {
    return true;
  }

  @Override
  public Object getTransferData(DataFlavor flavor)
    throws UnsupportedFlavorException, IOException
  {
    String text = attributes.get("text");
    return text == null ? elementName : text;
  }

  /* MouseListener interface */
  @Override
  public void mouseClicked(MouseEvent e)
  {
  }

  @Override
  public void mousePressed(MouseEvent e)
  {
    setSelected(!selected);
    fireActionPerformed(new ActionEvent(ElementLabel.this, 0, "pressed"));
  }

  @Override
  public void mouseReleased(MouseEvent e)
  {
  }

  @Override
  public void mouseEntered(MouseEvent e)
  {
    if (!selected)
    {
      borderType = 1;
      repaint();
    }
  }

  @Override
  public void mouseExited(MouseEvent e)
  {
    if (selected)
    {
      borderType = -1;
    }
    else
    {
      borderType = 0;
    }
    repaint();
  }

  /* internal methods */

  protected void fireActionPerformed(ActionEvent e)
  {
    Iterator iter = listeners.iterator();
    while (iter.hasNext())
    {
      ActionListener l = (ActionListener)iter.next();
      l.actionPerformed(e);
    }
  }

  private void initComponents()
  {
    setBorder(new EmptyBorder(2, 4, 2, 4));
    addMouseListener(this);
    DragSource dragSource = DragSource.getDefaultDragSource();
    dragSource.createDefaultDragGestureRecognizer(this,
      DnDConstants.ACTION_COPY, new DragGestureListener()
    {
      @Override
      public void dragGestureRecognized(DragGestureEvent event)
      {
        DragSourceListener dragListener = new DragSourceAdapter()
        {
          @Override
          public void dragDropEnd(DragSourceDropEvent event)
          {
            Palette palette = ElementLabel.this.getCategoryPane().getPalette();
            palette.clearSelectedElement();
          }
        };        
        event.startDrag(DragSource.DefaultCopyDrop,
          ElementLabel.this, dragListener);
      }
    });
  }
}
