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
package org.santfeliu.matrix.ide;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.matrix.security.RoleFilter;
import org.matrix.security.RoleInRole;
import org.matrix.security.RoleInRoleFilter;
import org.matrix.security.RoleInRoleView;
import org.matrix.security.SecurityManagerPort;
import org.santfeliu.security.swing.Role;
import org.santfeliu.security.swing.RolesGraph;
import org.santfeliu.security.swing.RolesGraphIO;
import org.santfeliu.swing.layout.WrapLayout;

/**
 *
 * @author realor
 */
public class RolesGraphPanel extends DocumentPanel
{
  private RolesGraph graph = new RolesGraph();
  private JToolBar toolBar = new JToolBar();
  private JButton explodeButton = new JButton();
  private JButton viewAllButton = new JButton();
  private JButton centerSelectionButton = new JButton();
  private JButton reportButton = new JButton();
  private JButton saveRolesButton = new JButton();
  private JButton readRolesButton = new JButton();
  private JCheckBox paintOnlySelectionCheckBox = new JCheckBox();
  private BorderLayout layout = new BorderLayout();
  private String textToFind;
  private int findIndex;

  public RolesGraphPanel()
  {
    try
    {
      initComponents();
    }
    catch (Exception e)
    {
      MatrixIDE.log(e);
    }
  }

  @Override
  public void activate()
  {
    updatePropertiesPanel();
    getMainPanel().getPalette().setSelectedCategory("rolesgraph");
    getMainPanel().setRightPanelVisible(true);
  }

  @Override
  public void open(InputStream os) throws Exception
  {
    RolesGraphIO io = new RolesGraphIO();
    io.read(os, graph);
  }

  @Override
  public void save(OutputStream os) throws Exception
  {
    RolesGraphIO io = new RolesGraphIO();
    io.write(os, graph);
  }

  @Override
  public void delete()
  {
    if (!graph.getSelection().isEmpty())
    {
      int result = JOptionPane.showConfirmDialog(graph, "Remove selection?",
        "Remove selection", JOptionPane.YES_NO_OPTION);
      if (result == JOptionPane.OK_OPTION)
      {
        graph.removeSelection();
      }
    }
  }

  @Override
  public void find()
  {
    findIndex = -1;
    FindDialog dialog = new FindDialog(getMainPanel().getIDE())
    {
      @Override
      protected boolean next(String text)
      {
        boolean found = false;
        if (!text.equals(textToFind))
        {
          textToFind = text;
          findIndex = -1;
        }
        int oldFindIndex = findIndex;
        Role role = null;
        List<Role> roles = new ArrayList();
        roles.addAll(graph.getRoles());
        findIndex++;
        while (findIndex < roles.size() && !found)
        {
          role = roles.get(findIndex);
          found = found(role, text);
          if (!found) findIndex++;
        }
        if (found)
        {
          graph.clearSelection();
          graph.select(role);
          graph.setCenter(role.getPosition());
        }
        else findIndex = oldFindIndex;
        return found;
      }

      @Override
      protected boolean previous(String text)
      {
        boolean found = false;
        if (!text.equals(textToFind))
        {
          textToFind = text;
          findIndex = -1;
        }
        int oldFindIndex = findIndex;
        Role role = null;
        List<Role> roles = new ArrayList();
        roles.addAll(graph.getRoles());
        findIndex--;
        while (findIndex > 0 && !found)
        {
          role = roles.get(findIndex);
          found = found(role, text);
          if (!found) findIndex--;
        }
        if (found)
        {
          graph.clearSelection();
          graph.select(role);
          graph.setCenter(role.getPosition());
        }
        else findIndex = oldFindIndex;
        return found;
      }

      private boolean found(Role role, String text)
      {
        boolean found = false;
        String roleId = role.getRoleId();
        roleId = (roleId == null) ? "" : roleId.toUpperCase();
        String name = role.getName();
        name = (name == null) ? "" : name.toUpperCase();
        if ((roleId.indexOf(textToFind.toUpperCase()) != -1) ||
          (name.indexOf(textToFind.toUpperCase()) != -1))
          found = true;
        return found;
      }
    };

    dialog.setTitle("Find role");
    dialog.setTextToFind(textToFind);
    dialog.setLabelText("Enter roleId or name:");
    dialog.showDialog();
  }

  @Override
  public boolean isFindEnabled()
  {
    return true;
  }

  @Override
  public void setZoom(double size)
  {
    graph.setZoom(size);
  }

  @Override
  public double getZoom()
  {
    return graph.getZoom();
  }

  @Override
  public void print()
  {
    try
    {
      Printable printable = new Printable()
      {
        public int print(Graphics g, PageFormat pageFormat, int page)
        {
          if (page == 0)
          {            
            Graphics2D g2 = (Graphics2D)g;

            if (!graph.getRoles().isEmpty())
            {
              RolesGraph printGraph = new RolesGraph();
              printGraph.setOpaque(false);
              printGraph.setZoomLimit(0);
              printGraph.setArrowColor(Color.BLACK);
              printGraph.addRoles(graph.getRoles());
              printGraph.setSize(
                (int)pageFormat.getImageableWidth(),
                (int)pageFormat.getImageableHeight());
              printGraph.centerOn(printGraph.getRoles(), 0.9);
              printGraph.setNormalStroke(new BasicStroke(0.2f));

              g2.translate(pageFormat.getImageableX(),
                pageFormat.getImageableY());

              printGraph.print(g2);

              g2.translate(-pageFormat.getImageableX(),
                -pageFormat.getImageableY());
            }

            g2.drawRect(
              (int)pageFormat.getImageableX(),
              (int)pageFormat.getImageableY(),
              (int)pageFormat.getImageableWidth(),
              (int)pageFormat.getImageableHeight());
            
            String title = RolesGraphPanel.super.getDisplayName();
            if (title != null)
            {
              String description = RolesGraphPanel.super.getDescription();
              if (description != null)
              {
                title += ": " + description.trim();
              }
              int margin = 20;
              g2.setFont(new Font("Arial", Font.PLAIN, 10));
              g2.drawString(title,
                (int)pageFormat.getImageableX() + margin / 2,
                (int)pageFormat.getImageableY() + margin / 2);
            }
            return PAGE_EXISTS;
          }
          else
          {
            return NO_SUCH_PAGE;
          }
        }
      };

      PrinterJob job = PrinterJob.getPrinterJob();
      job.setJobName(getDisplayName());
      job.setPrintable(printable);      
      if (job.printDialog())
      {
        job.print();
      }
    }
    catch (Exception ex)
    {
      MatrixIDE.log(ex);
    }
  }

  public void addRole(Point point)
  {
    graph.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    getMainPanel().getPalette().clearSelectedElement();
    Role role = new Role();
    role.setPosition(graph.transformToEye(point));    
    if (showRoleDialog(role))
    {
      graph.addRole(role);
    }
  }

  /* internal methods */

  private void initComponents() throws Exception
  {
    setLayout(layout);
    add(toolBar, BorderLayout.NORTH);
    add(graph, BorderLayout.CENTER);
    explodeButton.setText("Explode");
    explodeButton.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/icon/exploderole.gif")));
    viewAllButton.setText("View all");
    viewAllButton.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/icon/viewall.gif")));

    centerSelectionButton.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/icon/centerselection.gif")));
    centerSelectionButton.setText("Center selection");

    saveRolesButton.setText("Save roles");
    saveRolesButton.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/icon/saverole.gif")));

    readRolesButton.setText("Read roles");
    readRolesButton.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/icon/readrole.gif")));

    reportButton.setText("Report");
    reportButton.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/icon/report.gif")));

    paintOnlySelectionCheckBox.setText("Paint only selection");

    toolBar.setLayout(new WrapLayout(WrapLayout.LEFT, 2, 2));
    toolBar.setMinimumSize(new Dimension(1, 1));

    toolBar.add(explodeButton);
    toolBar.add(viewAllButton);
    toolBar.add(centerSelectionButton);
    toolBar.add(readRolesButton);
    toolBar.add(saveRolesButton);
    toolBar.add(reportButton);
    toolBar.add(paintOnlySelectionCheckBox);
    toolBar.setFloatable(false);
    toolBar.setRollover(true);

    Color borderColor = UIManager.getColor("Panel.background").darker();
    toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor));

    graph.setOpaque(true);
    graph.setBackground(Color.WHITE);

    graph.addChangeListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent e)
      {
        updatePropertiesPanel();
        setModified(true);
      }
    });
    graph.addSelectionListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent e)
      {
        updatePropertiesPanel();
      }
    });
    graph.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (!graph.getSelection().isEmpty())
        {
          Role role = graph.getSelection().iterator().next();
          showRoleDialog(role);
        }
      }
    });
    explodeButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent event)
      {
        graph.selectConnected(Integer.MAX_VALUE);
      }
    });
    viewAllButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent event)
      {
        graph.centerOn(graph.getRoles());
      }
    });
    readRolesButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent event)
      {
        readRoles();
      }
    });
    saveRolesButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent event)
      {
        saveRoles();
      }
    });
    reportButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent event)
      {
        report();
      }
    });
    centerSelectionButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent event)
      {
        graph.centerOn(graph.getSelection());
      }
    });
    paintOnlySelectionCheckBox.addChangeListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent e)
      {
        graph.setPaintOnlySelection(paintOnlySelectionCheckBox.isSelected());
      }
    });
    graph.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent e)
      {
        if (e.getKeyCode() == KeyEvent.VK_DELETE)
        {
          delete();
        }
      }
    });
    graph.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseEntered(MouseEvent event)
      {
        if (getMainPanel().getPalette().getSelectedElement() != null)
        {
          graph.setCursor(DragSource.DefaultCopyDrop);
        }
      }

      @Override
      public void mouseReleased(MouseEvent event)
      {
        if (getMainPanel().getPalette().getSelectedElement() != null)
        {
          addRole(event.getPoint());
        }
      }
    });
    DropTarget dropTarget = new DropTarget(graph, new DropTargetAdapter()
    {
      public void drop(DropTargetDropEvent event)
      {
        addRole(event.getLocation());
      }
    });
    dropTarget.setActive(true);
  }

  private void updatePropertiesPanel()
  {
    Collection selection = graph.getSelection();
    if (selection.size() == 1)
    {
      getMainPanel().setEditObject(selection.iterator().next());
    }
    else
    {
      getMainPanel().setEditObject(null);
    }
  }

  private void readRoles()
  {
    Collection<Role> roles = graph.getSelection();
    if (roles.isEmpty())
    {
      JOptionPane.showMessageDialog(this,
        "Select roles to read from security service.",
        "Update from service",
        JOptionPane.WARNING_MESSAGE);
    }
    else if (JOptionPane.showConfirmDialog(this, "Read roles?",
      "Read from service", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
    {
      try
      {
        int updated = 0;
        int notFound = 0;
        SecurityManagerPort port = getMainPanel().getSecurityManagerPort();
        RoleFilter filter1 = new RoleFilter();
        RoleInRoleFilter filter2 = new RoleInRoleFilter();
        for (Role role : roles)
        {
          filter1.getRoleId().clear();
          filter1.getRoleId().add(role.getRoleId());
          List<org.matrix.security.Role> svcRoles = port.findRoles(filter1);
          if (svcRoles.isEmpty())
          {
            // role not found
            notFound++;
          }
          else
          {
            org.matrix.security.Role svcRole = svcRoles.get(0);
            role.setName(svcRole.getName());
            role.setComments(svcRole.getDescription());
            updated++;

            role.getInRoles().clear();
            filter2.setContainerRoleId(role.getRoleId());
            List<RoleInRole> roleInRoles = port.findRoleInRoles(filter2);
            for (RoleInRole r : roleInRoles)
            {
              String inRoleId = r.getIncludedRoleId();
              Role inRole = graph.getRole(inRoleId);
              if (inRole != null)
              {
                role.getInRoles().add(inRole);
              }
            }
          }
        }
        if (updated > 0)
        {
          if (roles.size() == 1) // refresh properties panel
          {
            getMainPanel().setEditObject(roles.iterator().next());
          }
          graph.repaint();
          setModified(true);
        }
        JOptionPane.showMessageDialog(this,
          MessageFormat.format(
            "Nodes updated: {0}.\n" +
            "Nodes not found: {1}.",
            updated, notFound),
          "Read from service",
          JOptionPane.INFORMATION_MESSAGE);
      }
      catch (Exception ex)
      {
        JOptionPane.showMessageDialog(this,
          ex.toString(), "Read from service", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void saveRoles()
  {
    Collection<Role> roles = graph.getSelection();
    if (roles.isEmpty())
    {
      JOptionPane.showMessageDialog(this, 
        "Select roles to save into security service.",
        "Save to service",
        JOptionPane.WARNING_MESSAGE);
    }
    else if (JOptionPane.showConfirmDialog(this, "Save roles?",
      "Save to service", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
    {
      try
      {
        int updated = 0;
        int connAdded = 0;
        int connRemoved = 0;
        SecurityManagerPort port = getMainPanel().getSecurityManagerPort();
        RoleInRoleFilter filter2 = new RoleInRoleFilter();
        org.matrix.security.Role svcRole = new org.matrix.security.Role();        
        // save all roles
        for (Role role : roles)
        {
          svcRole.setRoleId(role.getRoleId());
          svcRole.setName(role.getName());
          svcRole.setDescription(role.getComments());
          port.storeRole(svcRole);
          updated++;
        }

        List<Role> previousInRoles = new ArrayList();
        for (Role role : roles)
        {
          filter2.setContainerRoleId(role.getRoleId());
          List<RoleInRoleView> views = port.findRoleInRoleViews(filter2);
          for (RoleInRoleView view : views)
          {
            String inRoleId = view.getIncludedRole().getRoleId();
            Role inRole = graph.getRole(inRoleId);
            if (inRole != null) // inRole is in diagram
            {
              if (role.getInRoles().contains(inRole))
              {
                // keep connection, add to previousInRoles list
                previousInRoles.add(inRole);
              }
              else
              {
                // remove connection
                port.removeRoleInRole(view.getRoleInRoleId());
                connRemoved++;
              }
            }
            else
            {
              // node outside diagram, do nothing
            }
          }
          // add new connections
          org.matrix.security.RoleInRole roleInRole =
            new org.matrix.security.RoleInRole();
          for (Role inRole : role.getInRoles())
          {
            if (!previousInRoles.contains(inRole))
            {
              roleInRole.setContainerRoleId(role.getRoleId());
              roleInRole.setIncludedRoleId(inRole.getRoleId());
              port.storeRoleInRole(roleInRole);
              connAdded++;
            }
          }
        }
        JOptionPane.showMessageDialog(this,
          MessageFormat.format("Nodes updated: {0}.\n" +
          "Connections added: {1}.\n" +
          "Connections removed: {2}.", updated, connAdded, connRemoved),
          "Save to service", JOptionPane.INFORMATION_MESSAGE);
      }
      catch (Exception ex)
      {
        JOptionPane.showMessageDialog(this,
          ex.toString(), "Save to service", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void report()
  {
    Collection<Role> roles = graph.getSelection();
    if (roles.isEmpty())
    {
      JOptionPane.showMessageDialog(this,
        "Select roles to report.",
        "Report",
        JOptionPane.WARNING_MESSAGE);
    }
    else
    {
      try
      {
        SecurityManagerPort port = getMainPanel().getSecurityManagerPort();
        RolesReporter reporter = new RolesReporter(port);
        reporter.begin();
        for (Role role : roles)
        {
          reporter.report(role.getRoleId());
        }
        reporter.end();
        MessageDialog dialog =
          new MessageDialog(getMainPanel().getIDE(), true);
        dialog.setMessage("Report", reporter.toString(), "text/html");
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(getMainPanel());
        dialog.setVisible(true);
      }
      catch (Exception ex)
      {
        JOptionPane.showMessageDialog(this,
          ex.toString(), "Report", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private boolean showRoleDialog(Role role)
  {
    RoleDialog dialog = new RoleDialog(getMainPanel().getIDE());
    dialog.setLocationRelativeTo(graph);
    dialog.setRoleId(role.getRoleId());
    dialog.setRoleName(role.getName());
    dialog.setComments(role.getComments());
    boolean update = dialog.showDialog();
    if (update)
    {
      String roleId = dialog.getRoleId();
      if (!roleId.equals(role.getRoleId())) // roleId changed!
      {
        // new roleId already exists?
        if (graph.getRole(roleId) != null)
        {
          update = false;
          JOptionPane.showMessageDialog(graph, "roleId already exists!",
            "WARNING", JOptionPane.WARNING_MESSAGE);
        }
      }
      if (update)
      {
        role.setRoleId(roleId);
        role.setName(dialog.getRoleName());
        role.setComments(dialog.getComments());
        setModified(true);
        graph.repaint();
        getMainPanel().setEditObject(role);
      }
    }
    return update;
  }
}
