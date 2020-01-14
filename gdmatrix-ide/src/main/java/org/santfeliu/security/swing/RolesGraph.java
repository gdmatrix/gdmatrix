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
package org.santfeliu.security.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author realor
 */
public class RolesGraph extends JComponent
  implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener
{
  private Set<Role> roles = new HashSet();
  private Set<Role> selection = new HashSet();
  private Point2D center = new Point2D.Double(5, 5);
  private double zoom = 100;
  private double minZoom = 1;
  private double maxZoom = 400;
  private Point dragPoint;
  private Point movePoint;
  private boolean panning = false;
  private boolean moving = false;
  private boolean boxing = false;
  private boolean linking = false;
  private Point boxOrigin = null;
  private Rectangle box = new Rectangle();
  private int count;
  private double zoomLimit = 30;
  private boolean paintOnlySelection = false;
  private Role targetRole;
  private Color selectionColor = new Color(255, 100, 100);
  private Color arrowColor = Color.GRAY;
  private Stroke normalStroke = new BasicStroke(1);
  private Stroke dashedStroke =
    new BasicStroke(1, 0, 0, 1f, new float[]{4, 4}, 0);
  private List<ActionListener> actionListeners = new ArrayList();
  private List<ChangeListener> changeListeners = new ArrayList();
  private List<ChangeListener> selectionListeners = new ArrayList();

  public RolesGraph()
  {
    init();
  }

  public void addRole(Role role)
  {
    roles.add(role);
    fireStateChanged(new ChangeEvent(this));
    repaint();
  }

  public void addRoles(Collection<Role> roles)
  {
    this.roles.addAll(roles);
    fireStateChanged(new ChangeEvent(this));
    repaint();
  }
  
  public void removeRole(Role role)
  {
    roles.remove(role);
    removeReferences(role);
    fireStateChanged(new ChangeEvent(this));
    repaint();
  }

  public void removeRoles()
  {
    roles.clear();
    repaint();
  }

  public Role getRole(String roleId)
  {
    Role role = null;
    Iterator<Role> iter = roles.iterator();
    while (iter.hasNext() && role == null)
    {
      Role curRole = iter.next();
      if (curRole.getRoleId().equals(roleId))
      {
        role = curRole;
      }
    }
    return role;
  }

  public Color getSelectionColor()
  {
    return selectionColor;
  }

  public void setSelectionColor(Color selectionColor)
  {
    this.selectionColor = selectionColor;
  }

  public Color getArrowColor()
  {
    return arrowColor;
  }

  public void setArrowColor(Color arrowColor)
  {
    this.arrowColor = arrowColor;
  }

  public Stroke getDashedStroke()
  {
    return dashedStroke;
  }

  public void setDashedStroke(Stroke dashedStroke)
  {
    this.dashedStroke = dashedStroke;
  }

  public Stroke getNormalStroke()
  {
    return normalStroke;
  }

  public void setNormalStroke(Stroke normalStroke)
  {
    this.normalStroke = normalStroke;
  }

  public boolean isPaintOnlySelection()
  {
    return paintOnlySelection;
  }

  public void setPaintOnlySelection(boolean paintOnlySelection)
  {
    this.paintOnlySelection = paintOnlySelection;
    repaint();
  }

  public Collection<Role> getRoles()
  {
    return Collections.unmodifiableCollection(roles);
  }
  
  public Collection<Role> getSelection()
  {
    return Collections.unmodifiableCollection(selection);
  }

  public void clearSelection()
  {
    selection.clear();
    repaint();
    fireSelectionChanged(new ChangeEvent(this));
  }

  public void select(Role role)
  {
    selection.add(role);
    fireSelectionChanged(new ChangeEvent(this));
  }
  
  public void select(Collection<Role> roles)
  {
    selection.addAll(roles);
    repaint();
    fireSelectionChanged(new ChangeEvent(this));
  }

  public void removeSelection()
  {
    List<Role> rolesToRemove = new ArrayList();
    rolesToRemove.addAll(selection);
    for (Role role : rolesToRemove)
    {
      selection.remove(role);
      removeRole(role);
    }
    fireSelectionChanged(new ChangeEvent(this));
  }

  public Point2D getCenter()
  {
    return center;
  }
  
  public void setCenter(Point2D center)
  {
    this.center.setLocation(center);
    repaint();
  }

  public void centerOn(Collection<Role> roles)
  {
    centerOn(roles, 0.5);
  }

  public void centerOn(Collection<Role> roles, double factor)
  {
    if (roles.isEmpty()) return;

    if (roles.size() == 1)
    {
      Role role = roles.iterator().next();
      setCenter(role.getPosition());
    }
    else
    {
      Rectangle2D bounds = getBounds(roles);
      double cx = bounds.getCenterX();
      double cy = bounds.getCenterY();      
      zoom = factor * Math.min(
        getWidth() / bounds.getWidth(),
        getHeight() / bounds.getHeight());
      setCenter(new Point2D.Double(cx, cy));
    }
  }

  public Rectangle2D getBounds(Collection<Role> roles)
  {
    double minX = Double.MAX_VALUE;
    double minY = Double.MAX_VALUE;
    double maxX = -Double.MAX_VALUE;
    double maxY = -Double.MAX_VALUE;

    Rectangle2D bounds = new Rectangle2D.Double();
    for (Role role : roles)
    {
      Point2D point = role.getPosition();
      double x = point.getX();
      double y = point.getY();

      if (x < minX) minX = x;
      else if (x > maxX) maxX = x;

      if (y < minY) minY = y;
      else if (y > maxY) maxY = y;
    }
    bounds.setFrameFromDiagonal(minX, minY, maxX, maxY);
    return bounds;
  }

  public double getZoomLimit()
  {
    return zoomLimit;
  }

  public void setZoomLimit(double zoomLimit)
  {
    this.zoomLimit = zoomLimit;
  }

  public double getZoom()
  {
    return zoom;
  }

  public void setZoom(double zoom)
  {    
    if (zoom < minZoom) this.zoom = minZoom;
    else if (zoom > maxZoom) this.zoom = maxZoom;
    else this.zoom = zoom;
    repaint();
  }

  public void selectConnected(int levels)
  {
    ArrayList<Role> list = new ArrayList();
    list.addAll(selection);
    for (Role role : list)
    {
      selectConnected(role, levels);
    }
    repaint();
  }

  public void addActionListener(ActionListener l)
  {
    actionListeners.add(l);
  }

  public void removeActionListener(ActionListener l)
  {
    actionListeners.remove(l);
  }

  public void addChangeListener(ChangeListener l)
  {
    changeListeners.add(l);
  }

  public void removeChangeListener(ChangeListener l)
  {
    changeListeners.remove(l);
  }

  public void addSelectionListener(ChangeListener l)
  {
    selectionListeners.add(l);
  }

  public void removeSelectionListener(ChangeListener l)
  {
    selectionListeners.remove(l);
  }

  @Override
  public void paintComponent(Graphics g)
  {
    Graphics2D g2 = (Graphics2D)g;
    g2.setStroke(normalStroke);
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON);

    if (isOpaque())
    {
      g2.setColor(getBackground());
      g2.fillRect(0, 0, getWidth(), getHeight());
    }

    // project points
    for (Role role : roles)
    {
      Point2D position = role.getPosition();
      Point point = transformToDevice(position);
      role.setDevicePosition(point);
    }

    for (Role role : roles)
    {
      if (selection.contains(role) || !paintOnlySelection)
      {
        Point point = role.getDevicePosition();
        // draw arrows
        g2.setColor(selection.contains(role) ? Color.BLACK : arrowColor);
        for (Role inRole : role.getInRoles())
        {
          Point toPoint = inRole.getDevicePosition();
          paintArrow(g2, point, toPoint);
        }
      }
    }

    for (Role role : roles)
    {
      paintRole(g2, role);
    }

    if (boxing)
    {
      g2.setColor(Color.RED);
      g2.setStroke(dashedStroke);
      g2.draw(box);
    }
    if (linking)
    {
      Iterator<Role> iter = selection.iterator();
      if (iter.hasNext())
      {
        Role role = iter.next();
        Point p1 = role.getDevicePosition();
        Point p2 = movePoint;
        if (p1 != null && p2 != null)
        {
          g2.setColor(Color.RED);
          g2.setStroke(dashedStroke);
          g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
      }
    }
    g2.setStroke(normalStroke);    
  }

  public void mouseClicked(MouseEvent e)
  {
  }

  public void mousePressed(MouseEvent e)
  {
    requestFocus();
    if (e.getButton() != MouseEvent.BUTTON1)
    {
      panning = true;
      setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }
    else if (e.getButton() == MouseEvent.BUTTON1)
    {
      Role role = getClosestRole(e.getPoint());
      if (role != null) // role selected
      {
        if (e.isShiftDown() && selection.size() == 1) // add connection
        {
          Role fromRole = selection.iterator().next();
          if (fromRole != role)
          {
            if (fromRole.getInRoles().contains(role))
            {
              fromRole.getInRoles().remove(role);
            }
            else
            {
              fromRole.getInRoles().add(role);
            }
            fireStateChanged(new ChangeEvent(this));
          }
        }
        else if (e.getClickCount() > 1 && selection.contains(role) &&
           !e.isControlDown() && !e.isShiftDown()) // edit role
        {
          fireActionPerformed(new ActionEvent(this, 0, "edit"));
        }
        else
        {
          if (e.isControlDown())
          {
            selection.add(role);
          }
          else
          {
            selection.clear();
            selection.add(role);
          }
          fireSelectionChanged(new ChangeEvent(this));
          moving = true;
        }
      }
      else // no role selected
      {
        if (!e.isControlDown()) selection.clear();
        // create new role
        if (e.getClickCount() > 1 && !e.isControlDown() && !e.isShiftDown())
        {
          role = new Role();
          role.setRoleId("NEW-" + count++);
          role.setPosition(transformToEye(e.getPoint()));
          addRole(role);
          selection.clear();
          selection.add(role);
          fireActionPerformed(new ActionEvent(this, 0, "new"));
          fireSelectionChanged(new ChangeEvent(this));
        }
        else if (!e.isShiftDown())
        {
          boxing = true;
          Point point = e.getPoint();
          boxOrigin = point;
          box.setBounds(point.x, point.y, 0, 0);
        }
      }
      repaint();
    }
  }

  public void mouseReleased(MouseEvent e)
  {
    panning = false;
    if (moving)
    {
      moving = false;
      if (dragPoint != null) fireStateChanged(new ChangeEvent(this));
    }
    if (boxing)
    {
      boxing = false;
      repaint();
      fireSelectionChanged(new ChangeEvent(this));
    }
    linking = false;
    dragPoint = null;
    movePoint = null;
    setCursor(Cursor.getDefaultCursor());
  }

  public void mouseEntered(MouseEvent e)
  {
  }

  public void mouseExited(MouseEvent e)
  {
  }

  public void mouseDragged(MouseEvent e)
  {
    if (panning)
    {
      Point point = e.getPoint();
      if (dragPoint != null)
      {
        double deltaX = (dragPoint.x - point.x) / zoom;
        double deltaY = (point.y - dragPoint.y) / zoom;
        center.setLocation(
          center.getX() + deltaX,
          center.getY() + deltaY);
        repaint();
      }
      dragPoint = point;
    }
    else if (moving)
    {
      Point point = e.getPoint();
      if (dragPoint != null)
      {
        double deltaX = (dragPoint.x - point.x) / zoom;
        double deltaY = (point.y - dragPoint.y) / zoom;
        for (Role role : selection)
        {
          Point2D position = role.getPosition();
          position.setLocation(
            position.getX() - deltaX,
            position.getY() - deltaY);
        }
        repaint();
      }
      dragPoint = point;
    }
    else if (boxing)
    {
      Point point = e.getPoint();
      box.setFrameFromDiagonal(boxOrigin.x, boxOrigin.y, point.x, point.y);

      if (!e.isControlDown()) selection.clear();
      selectInsideBox();
      repaint();
    }
  }

  public void mouseMoved(MouseEvent e)
  {
    if (linking)
    {
      targetRole = getClosestRole(e.getPoint());
      repaint();
    }
    movePoint = e.getPoint();
  }

  public void mouseWheelMoved(MouseWheelEvent e)
  {
    int rot = e.getWheelRotation();
    int amount = e.getScrollAmount();
    double newZoom;
    if (rot > 0)
    {
      newZoom = zoom * (1 + 0.1 * (double)amount);
      setZoom(newZoom);
    }
    else if (rot < 0)
    {
      newZoom = zoom / (1 + 0.1 * (double)amount);
      setZoom(newZoom);
    }
  }

  public void keyTyped(KeyEvent e)
  {
  }

  public void keyPressed(KeyEvent e)
  {
    if (e.getKeyCode() == KeyEvent.VK_SHIFT)
    {
      if (selection.size() == 1)
      {
        linking = true;
        if (movePoint != null) targetRole = getClosestRole(movePoint);
        repaint();
      }
    }
  }

  public void keyReleased(KeyEvent e)
  {
    linking = false;
    repaint();
  }    

  public Point2D transformToEye(Point point)
  {
    double winWidth = ((double)getWidth()) / zoom;
    double winHeight = ((double)getHeight()) / zoom;

    double x = ((double)point.x / zoom) - 0.5 * winWidth + center.getX();
    double y = center.getY() + 0.5 * winHeight - (double)point.y / zoom;

    return new Point2D.Double(x, y);
  }

  public Point transformToDevice(Point2D position)
  {
    Point devicePoint = new Point();
    double winWidth = ((double)getWidth()) / zoom;
    double winHeight = ((double)getHeight()) / zoom;

    devicePoint.x = (int)Math.round(zoom *
      (position.getX() + 0.5 * winWidth - center.getX()));
    devicePoint.y = (int)Math.round(zoom *
      (center.getY() - position.getY() + 0.5 * winHeight));

    return devicePoint;
  }

  // private methods

  private Role getClosestRole(Point point)
  {
    Role bestRole = null;
    if (zoom < zoomLimit)
    {
      double bestDistance = Double.MAX_VALUE;
      for (Role role : roles)
      {
        Point pos = role.getDevicePosition();
        double distance = Math.sqrt(
          (point.x - pos.x) * (point.x - pos.x) +
          (point.y - pos.y) * (point.y - pos.y));
        if (distance < 10)
        {
          if (distance < bestDistance)
          {
            bestDistance = distance;
            bestRole = role;
          }
        }
      }
    }
    else
    {
      Iterator<Role> iter = roles.iterator();
      while (iter.hasNext() && bestRole == null)
      {
        Role role = iter.next();
        if (role.getBox().contains(point)) bestRole = role;
      }
    }
    return bestRole;
  }

  private void selectInsideBox()
  {
    for (Role role : roles)
    {
      Point position = role.getDevicePosition();
      if (box.contains(position))
      {
        selection.add(role);
      }
    }
  }

  private void selectConnected(Role role, int levels)
  {
    levels--;
    for (Role inRole : role.getInRoles())
    {
      if (!selection.contains(inRole))
      {
        selection.add(inRole);
        if (levels > 0)
        {
          selectConnected(inRole, levels);
        }
      }
    }
  }

  private void removeReferences(Role inRole)
  {
    for (Role role : roles)
    {
      role.getInRoles().remove(inRole);
    }
  }

  protected void fireActionPerformed(ActionEvent event)
  {
    for (ActionListener l : actionListeners)
    {
      l.actionPerformed(event);
    }
  }

  protected void fireStateChanged(ChangeEvent event)
  {
    for (ChangeListener l : changeListeners)
    {
      l.stateChanged(event);
    }
  }

  protected void fireSelectionChanged(ChangeEvent event)
  {
    for (ChangeListener l : selectionListeners)
    {
      l.stateChanged(event);
    }
  }

  private void init()
  {    
    addMouseListener(this);
    addMouseMotionListener(this);
    addMouseWheelListener(this);
    addKeyListener(this);
  }

  private void paintRole(Graphics2D g2, Role role)
  {
    // draw roles

    Point point = role.getDevicePosition();
    if (zoom > zoomLimit)
    {
      setFont(g2, zoom);
      int margin = 3;
      String roleId = role.getRoleId();
      Rectangle2D bounds = g2.getFontMetrics().getStringBounds(roleId, g2);
      int x = point.x - (int)Math.round(bounds.getWidth() / 2) - margin;
      int y = point.y - (int)Math.round(bounds.getHeight() / 2) - margin;
      int width = (int)bounds.getWidth() + 2 * margin - 1;
      int height = (int)bounds.getHeight() + 2 * margin - 1;
      Color fillColor;
      if (linking && role == targetRole) fillColor = Color.YELLOW;
      else if (selection.contains(role)) fillColor = selectionColor;
      else fillColor = Color.WHITE;
      g2.setColor(fillColor);
      g2.fillRect(x, y, width, height);
      g2.setColor(Color.BLACK);
      g2.drawRect(x, y, width, height);
      g2.drawString(role.getRoleId(), x + margin, y + 
        margin + (int)(0.8 * bounds.getHeight()));
      role.getBox().setBounds(x, y, width, height);
    }
    else
    {
      Color color = (selection.contains(role)) ? selectionColor : Color.BLACK;
      g2.setColor(color);
      g2.fillOval(point.x - 4, point.y - 4, 8, 8);
    }
  }

  private void paintArrow(Graphics2D g2, Point point, Point toPoint)
  {
    double dx = toPoint.x - point.x;
    double dy = toPoint.y - point.y;
    double modulus = Math.sqrt(dx * dx + dy * dy);
    dx = dx / modulus;
    dy = dy / modulus;

    double distance = modulus / 2;
    double distance2 = distance + 0.10 * zoom;

    double pt1x = toPoint.x - distance * dx;
    double pt1y = toPoint.y - distance * dy;

    double pt2x = toPoint.x - distance2 * dx;
    double pt2y = toPoint.y - distance2 * dy;

    double factor = 0.05 * zoom;

    double pt2ax = pt2x - dy * factor;
    double pt2ay = pt2y + dx * factor;

    double pt2bx = pt2x + dy * factor;
    double pt2by = pt2y - dx * factor;

    g2.drawLine(point.x, point.y, toPoint.x, toPoint.y);

    GeneralPath poligon = new GeneralPath();
    poligon.moveTo(pt1x, pt1y);
    poligon.lineTo(pt2ax, pt2ay);
    poligon.lineTo(pt2bx, pt2by);
    poligon.closePath();
    g2.fill(poligon);
  }
  
  public static void main(String[] args)
  {
    JFrame frame = new JFrame("Roles");
    frame.getContentPane().add(new RolesGraph());
    frame.setSize(500, 500);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }

  private void setFont(Graphics2D g2, double zoom)
  {
    int size = (int)(zoom / 8.0);
    Font font = new Font("Arial", Font.PLAIN, size);
    g2.setFont(font);
  }
}
