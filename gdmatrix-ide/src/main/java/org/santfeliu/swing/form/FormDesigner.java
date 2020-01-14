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
package org.santfeliu.swing.form;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.santfeliu.swing.Utilities;
import org.santfeliu.swing.form.event.FormChangeListener;
import org.santfeliu.swing.form.event.FormSelectionListener;


/**
 *
 * @author unknown
 */
public class FormDesigner extends JPanel
{
  private ArrayList<ComponentView> components =
    new ArrayList<ComponentView>();
  private ArrayList<ComponentView> componentsBackup =
    new ArrayList<ComponentView>();
  private HashSet<ComponentView> selection = new HashSet<ComponentView>();
  private static ArrayList<ComponentView> copy = new ArrayList<ComponentView>();
  private ArrayList<FormSelectionListener> selectionListeners =
    new ArrayList<FormSelectionListener>();
  private ArrayList<FormChangeListener> changeListeners =
    new ArrayList<FormChangeListener>();
  private UndoableEditListener undoableEditListener;
  
  private boolean dragging = false;
  private Drag drag;
  private Point windowStart = null;
  private Rectangle selectionWindow = null;
  private ArrayList rubberbands = new ArrayList();
  private int gridSize = 8;
  private boolean snapToGrid = true;
  private Color gridColor = new Color(240, 240, 240);
  private boolean mouseHandlerEnabled = true;
  private boolean showAccessibility = false;
  private boolean showIds = false;
  private boolean showTabIndexes = false;
  private boolean showCoordinates = false;
  private boolean showOutputOrder = false;
  
  public FormDesigner()
  {
    try
    {
      initComponents();
    }
    catch(Exception e)
    {
    }
  }

  public void addSelectionListener(FormSelectionListener l)
  {
    selectionListeners.add(l);
  }

  public void removeSelectionListener(FormSelectionListener l)
  {
    selectionListeners.remove(l);
  }

  public void addChangeListener(FormChangeListener l)
  {
    changeListeners.add(l);
  }

  public void removeChangeListener(FormChangeListener l)
  {
    changeListeners.remove(l);
  }

  public void addUndoableEditListener(UndoableEditListener l)
  {
    undoableEditListener = l;
  }

  public void removeUndoableEditListener()
  {
    undoableEditListener = null;
  }
  
  public boolean isShowAccessibility()
  {
    return showAccessibility;
  }

  public void setShowAccessibility(boolean showAccessibility)
  {
    this.showAccessibility = showAccessibility;
    this.repaint();
  }

  public boolean isShowIds()
  {
    return showIds;
  }

  public void setShowIds(boolean showIds)
  {
    this.showIds = showIds;
    this.repaint();
  }

  public boolean isShowTabIndexes()
  {
    return showTabIndexes;
  }

  public void setShowTabIndexes(boolean showTabIndexes)
  {
    this.showTabIndexes = showTabIndexes;
    this.repaint();
  }

  public boolean isShowCoordinates()
  {
    return showCoordinates;
  }

  public void setShowCoordinates(boolean showCoordinates)
  {
    this.showCoordinates = showCoordinates;
    this.repaint();    
  }

  public boolean isShowOutputOrder()
  {
    return showOutputOrder;
  }

  public void setShowOutputOrder(boolean showOutputOrder)
  {
    this.showOutputOrder = showOutputOrder;
    this.repaint();    
  }
  
  @Override
  public void paintComponent(Graphics g)
  {
    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());
    paintGrid(g);
    g.setColor(Color.black);
    for (ComponentView view : components)
    {
      view.paint(g, showAccessibility, showIds, showTabIndexes, 
        showCoordinates, showOutputOrder);
    }
    for (ComponentView view : selection)
    {
      view.paintSelection(g);
    }
  }
  
  public void setMouseHandlerEnabled(boolean mouseHandlerEnabled)
  {
    this.mouseHandlerEnabled = mouseHandlerEnabled;
  }

  public boolean isMouseHandlerEnabled()
  {
    return mouseHandlerEnabled;
  }

  public void setGridSize(int gridSize)
  {
    this.gridSize = gridSize;
  }

  public int getGridSize()
  {
    return gridSize;
  }

  public void setSnapToGrid(boolean snapToGrid)
  {
    this.snapToGrid = snapToGrid;
  }

  public boolean isSnapToGrid()
  {
    return snapToGrid;
  }

  public void setGridColor(Color gridColor)
  {
    this.gridColor = gridColor;
  }

  public Color getGridColor()
  {
    return gridColor;
  }

  public Collection getComponentViews()
  {
    return components;
  }

  @Override
  public Dimension getMinimumSize()
  {
    int maxx = 0;
    int maxy = 0;
    Iterator<ComponentView> iter = components.iterator();
    while (iter.hasNext())
    {
      ComponentView view = iter.next();
      int x = view.getX() + view.getWidth();
      if (x > maxx)
      {
        maxx = x;
      }
      int y = view.getY() + view.getHeight();
      if (y > maxy)
      {
        maxy = y;
      }
    }
    return new Dimension(maxx, maxy);
  }
  
  @Override
  public Dimension getPreferredSize()
  {
    Dimension dim = getMinimumSize();
    dim.width += 20;
    dim.height += 20;
    return dim;
  }
  
  public void addComponentView(ComponentView view)
  {
    components.add(view);
    fireFormChangeEvent(new ChangeEvent(this));
    repaint();
  }
  
  public void insertComponentView(ComponentView view, int index)
  {
    components.add(index, view);
    fireFormChangeEvent(new ChangeEvent(this));
    repaint();
  }

  public void removeComponentView(ComponentView view)
  {
    components.remove(view);
    fireFormChangeEvent(new ChangeEvent(this));
    repaint();
  }

  public void toFront()
  {
    ArrayList<ComponentView> v = new ArrayList<ComponentView>();
    Iterator<ComponentView> iter = components.iterator();
    while (iter.hasNext())
    {
      ComponentView view = iter.next();
      if (selection.contains(view))
      {
        iter.remove();
        v.add(view);
      }
    }
    for (int i = 0; i < v.size(); i++)
    {
      components.add(v.get(i));
    }
    fireFormChangeEvent(new ChangeEvent(this));
    repaint();    
  }
  
  public void toBottom()
  {
    ArrayList<ComponentView> v = new ArrayList<ComponentView>();
    Iterator<ComponentView> iter = components.iterator();
    while (iter.hasNext())
    {
      ComponentView view = iter.next();
      if (selection.contains(view))
      {
        iter.remove();
        v.add(view);
      }
    }
    for (int i = 0; i < v.size(); i++)
    {
      components.add(0, v.get(i));
    }
    fireFormChangeEvent(new ChangeEvent(this));
    repaint();    
  }

  public void sortComponentsByPosition()
  {
    Collections.sort(components, new Comparator<ComponentView>()
    {
      public int compare(ComponentView o1, ComponentView o2)
      {
        if (o1.getY() != o2.getY()) return o1.getY() - o2.getY();
        return o1.getX() - o2.getX();
      }
    });
    fireFormChangeEvent(new ChangeEvent(this));
    repaint();
  }
  
  public Collection getSelection()
  {
    return selection;
  }

  public void clearSelection()
  {
    selection.clear();
    fireSelectionChangeEvent(new ChangeEvent(this));
    repaint();
  }

  public void copySelection()
  {
    copy.clear();
    for (ComponentView view : selection)
    {
      try
      {
        ComponentView clone = (ComponentView)view.clone();
        copy.add(clone);
      }
      catch (CloneNotSupportedException ex)
      {
      }
    }
  }

  public void paste(Point point)
  {
    if (!copy.isEmpty())
    {
      int minx = Integer.MAX_VALUE;
      int miny = Integer.MAX_VALUE;
      for (ComponentView view : copy)
      {
        if (view.getX() < minx) minx = view.getX();
        if (view.getY() < miny) miny = view.getY();
      }
      if (point == null)
      {
        point = new Point(minx + 2 * gridSize, miny + 2 * gridSize);
      }
      selection.clear();
      for (ComponentView view : copy)
      {
        try
        {
          // move view
          int vx = view.getX() - minx + point.x;
          int vy = view.getY() - miny + point.y;
          view.setX(vx);
          view.setY(vy);
          // clone it
          ComponentView clone = (ComponentView)view.clone();
          components.add(clone);
          selection.add(clone);
          fireFormChangeEvent(new ChangeEvent(this));
          repaint();
        }
        catch (CloneNotSupportedException ex)
        {
        }
      }      
    }
  }

  public void removeSelection()
  {
    Iterator<ComponentView> iter = selection.iterator();
    while (iter.hasNext())
    {
      ComponentView view = iter.next();
      components.remove(view);
    }
    selection.clear();
    fireSelectionChangeEvent(new ChangeEvent(this));
    fireFormChangeEvent(new ChangeEvent(this));
    repaint();
  }

  public void selectView(ComponentView view)
  {
    selection.add(view);
    fireSelectionChangeEvent(new ChangeEvent(this));
    repaint();
  }

  public boolean isViewSelected(ComponentView view)
  {
    return selection.contains(view);
  }

  public void selectView(Point point)
  {
    ComponentView view = findView(point);
    if (view != null)
    {
      selection.add(view);
      fireSelectionChangeEvent(new ChangeEvent(this));
      repaint();
    }
  }

  public void selectViews(Rectangle rect)
  {
    Iterator iter = components.iterator();
    while (iter.hasNext())
    {
      ComponentView view = (ComponentView)iter.next();
      if (rect.contains(view.getBounds()))
      {
        selection.add(view);
      }
      else
      {
        selection.remove(view);
      }
    }
    fireSelectionChangeEvent(new ChangeEvent(this));
    repaint();
  }

  public Point roundPoint(Point point)
  {
    Point result;
    if (snapToGrid)
    {
      int x = (int)(Math.round((double)point.x / gridSize)) * gridSize;
      int y = (int)(Math.round((double)point.y / gridSize)) * gridSize;
      result = new Point(x, y);
    }
    else result = point;
    return result;
  }
  
  public void fireAccessibilityChangeEvent()
  {
    fireFormChangeEvent(new ChangeEvent(this));
  }
  
  protected void fireSelectionChangeEvent(ChangeEvent event)
  {
    Iterator iter = selectionListeners.iterator();
    while (iter.hasNext())
    {
      FormSelectionListener l = (FormSelectionListener)iter.next();
      l.selectionChanged(event);
    }
  }

  protected void fireFormChangeEvent(ChangeEvent event)
  {
    Iterator iter = changeListeners.iterator();
    while (iter.hasNext())
    {
      FormChangeListener l = (FormChangeListener)iter.next();
      l.formChanged(event);
    }
    if (undoableEditListener != null)
    {
      undoableEditListener.undoableEditHappened(new UndoableEditEvent(this, 
        new FormDesignerUndoableEdit()));
    }
  }

  private void initComponents() throws Exception
  {
    this.setSize(new Dimension(397, 314));
    this.setBackground(Color.white);
    this.setOpaque(true);
    addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseEntered(MouseEvent event)
      {
        if (!mouseHandlerEnabled) return;
        if (!FormDesigner.this.isFocusOwner()) requestFocus();
      }

      @Override
      public void mousePressed(MouseEvent event)
      {
        if (!mouseHandlerEnabled) return;
        if (!FormDesigner.this.isFocusOwner()) requestFocus();
        if (event.getClickCount() == 2)
        {
          Point point = event.getPoint();
          ComponentView view = FormDesigner.this.findView(point);
          if (view != null)
          {
            ComponentEditor editor = 
              ComponentEditorFactory.getComponentEditor(view);
            if (editor != null)
            {
              Frame frame = (Frame)Utilities.getParentWindow(FormDesigner.this);
              ComponentEditorDialog dialog = new ComponentEditorDialog(frame);
              int option = dialog.editView(FormDesigner.this, view, editor);
              if (option == ComponentEditorDialog.OK_OPTION)
              {
                fireFormChangeEvent(new ChangeEvent(this));
              }
              FormDesigner.this.repaint();
            }
          }
        }
      }

      @Override
      public void mouseReleased(MouseEvent event)
      {
        if (!mouseHandlerEnabled) return;
        Point point = event.getPoint();
        if (!dragging) // selection by inner point
        {
          if (selectionWindow != null)
          {
            selectViews(selectionWindow);
            windowStart = null;
            selectionWindow = null;
            repaint();
          }
          else
          {
            if ((event.getModifiers() & MouseEvent.SHIFT_MASK) == 0)
            {
              clearSelection();
            }
            selectView(point);
          }
        }
        else // dragging
        {
          Point endPoint = roundPoint(point);
          Point dragPoint = 
            (drag.getPosition() == ComponentView.INTERNAL) ?
             roundPoint(drag.getDragPoint()) : drag.getDragPoint();
          int deltax = endPoint.x - dragPoint.x;
          int deltay = endPoint.y - dragPoint.y;
          Iterator iter = components.iterator();
          while (iter.hasNext())
          {
            ComponentView view = (ComponentView)iter.next();
            if (selection.contains(view))
            {
              Rectangle rect = 
                view.getResizedBounds(drag.getPosition(), deltax, deltay);
              view.setBounds(rect);
              fireFormChangeEvent(new ChangeEvent(this));
            }
          }
          dragging = false;
          drag = null;
          rubberbands.clear();
        }
        revalidate();
        repaint();
      }
    });
    
    addMouseMotionListener(new MouseMotionAdapter()
    {
      @Override
      public void mouseMoved(MouseEvent event)
      {
        if (!mouseHandlerEnabled) return;
        drag = findDrag(event.getPoint(), true);
        if (drag != null)
        {
          int position = drag.getPosition();
          switch (position)
          {
            case ComponentView.NORTH_WEST:
              setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
              break;
            case ComponentView.NORTH:
              setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
              break;
            case ComponentView.NORTH_EAST:
              setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
              break;
            case ComponentView.WEST:
              setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
              break;
            case ComponentView.EAST:
              setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
              break;
            case ComponentView.SOUTH_WEST:
              setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
              break;
            case ComponentView.SOUTH:
              setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
              break;
            case ComponentView.SOUTH_EAST:
              setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
              break;            
            case ComponentView.INTERNAL:
              setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
              break;
            default:
              setCursor(Cursor.getDefaultCursor());
              drag = null;
          }
        }
        else
        {
          setCursor(Cursor.getDefaultCursor());
          drag = null;
        }
        dragging = false;
      }

      @Override
      public void mouseDragged(MouseEvent event)
      {
        if (!mouseHandlerEnabled) return;
        if (!dragging)
        {
          if (drag == null) // cursor no over drag point
          {
            if (selectionWindow == null)
            {
              windowStart = event.getPoint();
              selectionWindow = 
                new Rectangle(windowStart.x, windowStart.y, 1, 1);
              paintWindow();
              setCursor(Cursor.getDefaultCursor());
            }
            else
            {
              paintWindow();
              Point windowEnd = event.getPoint();
              selectionWindow.x = Math.min(windowStart.x, windowEnd.x);
              selectionWindow.y = Math.min(windowStart.y, windowEnd.y);
              selectionWindow.width = Math.abs(windowStart.x - windowEnd.x);
              selectionWindow.height = Math.abs(windowStart.y - windowEnd.y);              
              paintWindow();
            }
          }
          else // cursor over drag point
          {
            // start dragging
            rubberbands.clear();
            Iterator iter = components.iterator();
            while (iter.hasNext())
            {
              ComponentView view = (ComponentView)iter.next();
              if (selection.contains(view))
              {
                Rectangle rubberband = view.getBounds();
                rubberbands.add(rubberband);
              }
            }
            paintRubberbands();
            dragging = true;
          }
        }
        else
        {
          paintRubberbands(); // remove previous
          rubberbands.clear();
          Point endPoint = roundPoint(event.getPoint());
          Point dragPoint = 
            (drag.getPosition() == ComponentView.INTERNAL) ?
             roundPoint(drag.getDragPoint()) : drag.getDragPoint();
          int deltax = endPoint.x - dragPoint.x;
          int deltay = endPoint.y - dragPoint.y;
          Iterator iter = components.iterator();
          while (iter.hasNext())
          {
            ComponentView view = (ComponentView)iter.next();
            if (selection.contains(view))
            {
              Rectangle rubberband = 
                view.getResizedBounds(drag.getPosition(), deltax, deltay);
              rubberbands.add(rubberband);
            }
          }
          paintRubberbands(); // paint next
        }
      }
    });
  }

  private Drag findDrag(Point point, boolean selectViews)
  {
    Drag newDrag = null;
    int i = components.size() - 1;
    while (i >= 0 && newDrag == null)
    {
      ComponentView view = (ComponentView)components.get(i);
      if (selection.contains(view) || !selectViews)
      {
        int position = view.getDragPosition(point);
        if (position != -1)
        {
          newDrag = new Drag(view, position, point);
        }
      }
      i--;
    }
    return newDrag;
  }
  
  private ComponentView findView(Point point)
  {
    ComponentView view = null;
    int i = components.size() - 1;
    while (i >= 0 && view == null)
    {
      ComponentView v = components.get(i);
      if (v.contains(point)) view = v;
      i--;
    }
    return view;
  }
  
  private void paintRubberbands()
  {
    Graphics g = getGraphics();
    for (int i = 0; i < rubberbands.size(); i++)
    {
      Rectangle rubberband = (Rectangle)rubberbands.get(i);
      g.setXORMode(Color.cyan);
      g.drawRect(rubberband.x - 1, rubberband.y - 1, 
        rubberband.width + 2, rubberband.height + 2);
      g.drawRect(rubberband.x, rubberband.y, 
        rubberband.width, rubberband.height);
      g.drawRect(rubberband.x + 1, rubberband.y + 1, 
        rubberband.width - 2, rubberband.height - 2);
    }
    g.dispose();
  }
  
  private void paintWindow()
  {
    Graphics g = getGraphics();
    g.setXORMode(Color.cyan);
    g.drawRect(selectionWindow.x, selectionWindow.y, 
      selectionWindow.width, selectionWindow.height);
    g.dispose();
  }
  
  private void paintGrid(Graphics g)
  {
    int xcount = getWidth() / gridSize;
    int ycount = getHeight() / gridSize;
    if (false)
    {
      g.setColor(gridColor);
      for (int j = 0; j < ycount; j++)
      {
        for (int i = 0; i < xcount; i++)
        {
          int x = gridSize * i;
          int y = gridSize * j;
          g.drawLine(x, y, x, y);        
        }
      }
    }
    else
    {
      g.setColor(gridColor);
      for (int j = 0; j <= ycount; j++)
      {
        int y = gridSize * j;
        g.drawLine(0, y, getWidth(), y);
      }
      for (int i = 0; i <= xcount; i++)
      {
        int x = gridSize * i;
        g.drawLine(x, 0, x, getHeight());        
      }
    }
  }
  
  private List<ComponentView> getCloneList(List<ComponentView> list)
  {
    List<ComponentView> result = new ArrayList<ComponentView>();
    for (ComponentView view : list)
    {
      try
      {
        ComponentView clone = (ComponentView)view.clone();
        result.add(clone);
      }
      catch (CloneNotSupportedException ex)
      {
      }
    }
    return result;
  }
  
  class FormDesignerUndoableEdit extends AbstractUndoableEdit
  {
    private ArrayList<ComponentView> backupBefore = 
      new ArrayList<ComponentView>();
    private ArrayList<ComponentView> backupAfter = 
      new ArrayList<ComponentView>();
    
    public FormDesignerUndoableEdit()
    {
      init();
    }
    
    @Override
    public void redo() throws CannotRedoException
    {
      super.redo();      
      selection.clear();      
      components.clear();
      components.addAll(getCloneList(backupAfter));      
      componentsBackup.clear();
      componentsBackup.addAll(getCloneList(components));      
      repaint();
    }
    
    @Override
    public void undo() throws CannotUndoException
    {
      super.undo();      
      selection.clear();      
      components.clear();
      components.addAll(getCloneList(backupBefore));      
      componentsBackup.clear();
      componentsBackup.addAll(getCloneList(components));      
      repaint();
    }
    
    private void init()
    {
      backupBefore.addAll(getCloneList(componentsBackup));
      backupAfter.addAll(getCloneList(components));
      componentsBackup.clear();
      componentsBackup.addAll(getCloneList(components));
    }
    
  }
  
}
