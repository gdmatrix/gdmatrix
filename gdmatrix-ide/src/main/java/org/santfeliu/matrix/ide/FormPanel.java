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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import org.santfeliu.swing.form.util.AccessibilityUtils;
import org.santfeliu.swing.form.BordersDialog;
import org.santfeliu.swing.form.ComponentView;
import org.santfeliu.swing.form.FormDesigner;
import org.santfeliu.swing.form.event.FormChangeListener;
import org.santfeliu.swing.form.event.FormSelectionListener;
import org.santfeliu.swing.form.ie.HtmlFormExporter;
import org.santfeliu.swing.form.ie.HtmlFormImporter;
import org.santfeliu.swing.form.view.ScriptView;
import org.santfeliu.swing.layout.WrapLayout;
import org.santfeliu.swing.palette.Palette;


/**
 *
 * @author unknown
 */
public class FormPanel extends DocumentPanel
{
  private BorderLayout borderLayout = new BorderLayout();
  private JScrollPane scrollPane = new JScrollPane();
  private FormDesigner designer = new FormDesigner();
  private JToolBar toolBar = new JToolBar();
  private JButton frontButton = new JButton();
  private JButton bottomButton = new JButton();
  private JButton sortByPositionButton = new JButton();
  private JButton bordersButton = new JButton();
  private JButton backgroundButton = new JButton();
  private JToggleButton gridButton = new JToggleButton();
  private JButton scriptButton = new JButton();
  private JToggleButton accessibilityButton = new JToggleButton();
  private JButton repairOutputOrderButton = new JButton();
  private JButton repairTabIndexesButton = new JButton();
  private JButton repairLabelsButton = new JButton();  
  private JCheckBox showIdsCheckBox = new JCheckBox();
  private JCheckBox showTabIndexesCheckBox = new JCheckBox();
  private JCheckBox showCoordinatesCheckBox = new JCheckBox();
  private JCheckBox showOutputOrderCheckBox = new JCheckBox();   
  private UndoManager undoManager = new UndoManager()
  {
    @Override
    public boolean addEdit(UndoableEdit edit)
    {
      boolean result = super.addEdit(edit);
      getMainPanel().updateActions();
      return result;
    }
  };
  
  public FormPanel()
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

  public FormDesigner getFormDesigner()
  {
    return designer;
  }
  
  @Override
  public void activate()
  {
    Collection views = getFormDesigner().getSelection();
    if (views.size() == 1)
    {
      Object object = views.iterator().next();
      getMainPanel().setEditObject(object);
    }
    else
    {
      getMainPanel().setEditObject(null);
    }
    getMainPanel().getPalette().setSelectedCategory("visualform");
    getMainPanel().setRightPanelVisible(true);
  }
  
  @Override
  public void open(InputStream is) throws Exception
  {    
    HtmlFormImporter importer = new HtmlFormImporter();
    importer.importPanel(is, designer);
    undoManager.discardAllEdits();
  }

  @Override
  public void save(OutputStream os) throws Exception
  {
    HtmlFormExporter exporter = new HtmlFormExporter();
    exporter.exportPanel(os, designer);
    undoManager.discardAllEdits();
  }

  @Override
  public void copy()
  {
    designer.copySelection();
  }

  @Override
  public void paste()
  {
    designer.paste(null);
  }

  @Override
  public void delete()
  {
    designer.removeSelection();
  }

  public void addComponentView(Point point)
  {
    try
    {
      Palette palette = getMainPanel().getPalette();
      String viewClassName = palette.getSelectedElementAttribute("class");
      if (viewClassName != null)
      {
        Class viewClass = Class.forName(viewClassName);
        ComponentView view = (ComponentView)viewClass.newInstance();
        designer.clearSelection();
        designer.selectView(view);
        view.setBounds(point.x, point.y, view.getWidth(), view.getHeight());
        designer.addComponentView(view);
      }
    }
    catch (Exception ex)
    {
      MatrixIDE.log(ex);
    }
    finally
    {
      designer.setCursor(Cursor.getDefaultCursor());
    }
  }

  @Override
  public UndoManager getUndoManager()
  {
    return undoManager;
  }
  
  /* internal methods */

  private void initComponents() throws Exception
  {
    this.setLayout(borderLayout);
    this.setSize(new Dimension(529, 407));
    toolBar.setFloatable(false);
    toolBar.setRollover(true);
    toolBar.setLayout(new WrapLayout(WrapLayout.LEFT, 2, 2));
    toolBar.setMinimumSize(new Dimension(1, 1));
    frontButton.setText("To front");
    frontButton.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/icon/tofront.gif")));
    frontButton.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            frontButton_actionPerformed(e);
          }
        });
    bottomButton.setText("To bottom");
    bottomButton.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/icon/toback.gif")));
    bottomButton.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            bottomButton_actionPerformed(e);
          }
        });
    sortByPositionButton.setText("Sort by position");
    sortByPositionButton.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/icon/sort.gif")));
    sortByPositionButton.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            sortByPositionButton_actionPerformed(e);
          }
        });    
    bordersButton.setText("Borders");
    bordersButton.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/icon/borders.gif")));
    bordersButton.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            bordersButton_actionPerformed(e);
          }
        });
    backgroundButton.setText("Background");
    backgroundButton.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/icon/background.gif")));
    backgroundButton.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            backgroundButton_actionPerformed(e);
          }
        });
    scriptButton.setText("Server script");
    scriptButton.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/icon/javascript.gif")));
    scriptButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        scriptButton_actionPerformed(e);
      }
    });
    gridButton.setText("Snap to grid");
    gridButton.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/icon/grid.gif")));
    gridButton.setSelected(true);
    gridButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        gridButton_actionPerformed(e);
      }
    });

    accessibilityButton.setText("Accessibility");
    accessibilityButton.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/icon/accessibility.gif")));
    accessibilityButton.setSelected(false);
    accessibilityButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        accessibilityButton_actionPerformed(e);
      }
    });
    
    repairOutputOrderButton.setText("Repair output order");
    repairOutputOrderButton.setVisible(false);
    repairOutputOrderButton.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            repairOutputOrderButton_actionPerformed(e);
          }
        });

    repairTabIndexesButton.setText("Repair tab idxs");
    repairTabIndexesButton.setVisible(false);
    repairTabIndexesButton.setEnabled(false);
    repairTabIndexesButton.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            repairTabIndexesButton_actionPerformed(e);
          }
        });
    
    repairLabelsButton.setText("Repair labels");
    repairLabelsButton.setVisible(false);
    repairLabelsButton.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            repairLabelsButton_actionPerformed(e);
          }
        });
    
    showIdsCheckBox.setText("Show ids");
    showIdsCheckBox.setVisible(false);
    showIdsCheckBox.setSelected(true);
    designer.setShowIds(true);
    showIdsCheckBox.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            showIdsCheckBox_actionPerformed(e);
          }
        });

    showTabIndexesCheckBox.setText("Show tab idx");
    showTabIndexesCheckBox.setVisible(false);
    showTabIndexesCheckBox.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            showTabIndexesCheckBox_actionPerformed(e);
          }
        });
    
    showCoordinatesCheckBox.setText("Show coords");
    showCoordinatesCheckBox.setVisible(false);
    showCoordinatesCheckBox.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            showCoordinatesCheckBox_actionPerformed(e);
          }
        });

    showOutputOrderCheckBox.setText("Show output order");
    showOutputOrderCheckBox.setVisible(false);
    showOutputOrderCheckBox.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            showOutputOrderCheckBox_actionPerformed(e);
          }
        });
    
    scrollPane.getViewport().add(designer, null);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    this.add(scrollPane, BorderLayout.CENTER);
    toolBar.add(frontButton, null);
    toolBar.add(bottomButton, null);
    toolBar.add(sortByPositionButton, null);
    toolBar.add(bordersButton, null);
    toolBar.add(backgroundButton, null);    
    toolBar.add(scriptButton, null);
    toolBar.add(gridButton, null);
    toolBar.add(accessibilityButton, null);    
    toolBar.add(showIdsCheckBox, null);
    toolBar.add(showTabIndexesCheckBox, null);
    toolBar.add(showCoordinatesCheckBox, null);
    toolBar.add(showOutputOrderCheckBox, null);
    toolBar.add(repairLabelsButton, null);
    toolBar.add(repairTabIndexesButton, null);
    toolBar.add(repairOutputOrderButton, null);
    
    Color borderColor = UIManager.getColor("Panel.background").darker();
    toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor));
    this.add(toolBar, BorderLayout.NORTH);

    designer.setGridSize(8);
    designer.addSelectionListener(new FormSelectionListener()
    {
      @Override
      public void selectionChanged(ChangeEvent event)
      {
        Collection selectedViews = designer.getSelection();
        if (selectedViews.size() == 1)
        {
          Iterator iter = selectedViews.iterator();
          ComponentView view = (ComponentView)iter.next();
          getMainPanel().setEditObject(view);
        }
        else
        {
          getMainPanel().setEditObject(null);
        }
      }
    });
    designer.addChangeListener(new FormChangeListener()
    {
      @Override
      public void formChanged(ChangeEvent event)
      {
        setModified(true);
        getMainPanel().updateEditObject();
      }
    });
    designer.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent e)
      {
        System.out.println(e);
        if (e.getKeyCode() == KeyEvent.VK_DELETE)
        {
          delete();
        }
      }
    });
    designer.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseEntered(MouseEvent event)
      {
        String selectedElement =
          getMainPanel().getPalette().getSelectedElement();
        if (selectedElement != null)
        {
          designer.setMouseHandlerEnabled(false);
          designer.setCursor(DragSource.DefaultCopyDrop);
        }
      }

      @Override
      public void mouseExited(MouseEvent event)
      {
        designer.setCursor(Cursor.getDefaultCursor());
        designer.setMouseHandlerEnabled(true);
      }

      @Override
      public void mouseReleased(MouseEvent event)
      {
        Palette palette = getMainPanel().getPalette();
        if (palette.getSelectedElement() != null)
        {
          addComponentView(designer.roundPoint(event.getPoint()));
          palette.clearSelectedElement();
          designer.setCursor(Cursor.getDefaultCursor());
          designer.setMouseHandlerEnabled(true);
        }
      }
    });
    
    designer.addUndoableEditListener(undoManager);
    
    DropTarget dropTarget = new DropTarget(designer, new DropTargetAdapter()
    {
      @Override
      public void drop(DropTargetDropEvent event)
      {
        addComponentView(designer.roundPoint(event.getLocation()));
        event.dropComplete(true);
        designer.setCursor(Cursor.getDefaultCursor());
      }
    });
    dropTarget.setActive(true);
  }

  private void frontButton_actionPerformed(ActionEvent e)
  {
    designer.toFront();
  }

  private void bottomButton_actionPerformed(ActionEvent e)
  {
    designer.toBottom();
  }

  private void sortByPositionButton_actionPerformed(ActionEvent e)
  {
    designer.sortComponentsByPosition();
  }

  private void bordersButton_actionPerformed(ActionEvent e)
  {
    Collection collection = designer.getSelection();
    if (collection.size() > 0)
    {
      Iterator iter = collection.iterator();
      ComponentView view = (ComponentView)iter.next();
      BordersDialog dialog =
        new BordersDialog((Frame)this.getTopLevelAncestor());
      dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

      dialog.setBorderTopColor(view.getBorderTopColor());
      dialog.setBorderBottomColor(view.getBorderBottomColor());
      dialog.setBorderLeftColor(view.getBorderLeftColor());
      dialog.setBorderRightColor(view.getBorderRightColor());

      dialog.setBorderTopWidth(view.parseWidth(view.getBorderTopWidth()));
      dialog.setBorderBottomWidth(view.parseWidth(view.getBorderBottomWidth()));
      dialog.setBorderLeftWidth(view.parseWidth(view.getBorderLeftWidth()));
      dialog.setBorderRightWidth(view.parseWidth(view.getBorderRightWidth()));

      dialog.setBorderTopStyle(view.getBorderTopStyle());
      dialog.setBorderBottomStyle(view.getBorderBottomStyle());
      dialog.setBorderLeftStyle(view.getBorderLeftStyle());
      dialog.setBorderRightStyle(view.getBorderRightStyle());

      dialog.setLocationRelativeTo(this);
      int result = dialog.showDialog();
      if (result == BordersDialog.OK)
      {
        do
        {
          if (dialog.isApplyTop())
          {
            view.setBorderTopWidth(dialog.getBorderTopWidth() + "px");
            view.setBorderTopColor(dialog.getBorderTopColor());
            view.setBorderTopStyle(dialog.getBorderTopStyle());
          }
          if (dialog.isApplyBottom())
          {
            view.setBorderBottomWidth(dialog.getBorderBottomWidth() + "px");
            view.setBorderBottomColor(dialog.getBorderBottomColor());
            view.setBorderBottomStyle(dialog.getBorderBottomStyle());
          }
          if (dialog.isApplyLeft())
          {
            view.setBorderLeftWidth(dialog.getBorderLeftWidth() + "px");
            view.setBorderLeftColor(dialog.getBorderLeftColor());
            view.setBorderLeftStyle(dialog.getBorderLeftStyle());
          }
          if (dialog.isApplyRight())
          {
            view.setBorderRightWidth(dialog.getBorderRightWidth() + "px");
            view.setBorderRightColor(dialog.getBorderRightColor());
            view.setBorderRightStyle(dialog.getBorderRightStyle());
          }
          if (iter.hasNext()) view = (ComponentView)iter.next();
          else view = null;
        } while (view != null);
        setModified(true);
        getMainPanel().updateEditObject();
        designer.repaint();
      }
    }
  }

  private void backgroundButton_actionPerformed(ActionEvent e)
  {
    Collection collection = designer.getSelection();
    Iterator iter = collection.iterator();
    if (iter.hasNext())
    {
      ComponentView view = (ComponentView)iter.next();
      Color color =
        JColorChooser.showDialog(this, "Select color", view.getBackground());
      if (color != null)
      {
        do
        {
          view.setBackground(color);
          if (iter.hasNext()) view = (ComponentView)iter.next();
          else view = null;
        } while (view != null);
        setModified(true);
        getMainPanel().updateEditObject();
        designer.repaint();
      }
    }
  }

  private void gridButton_actionPerformed(ActionEvent e)
  {
    designer.setSnapToGrid(gridButton.isSelected());
  }
      
  private void accessibilityButton_actionPerformed(ActionEvent e)
  {
    designer.setShowAccessibility(accessibilityButton.isSelected());
    if (accessibilityButton.isSelected())
    {
      repairOutputOrderButton.setVisible(true);
      repairTabIndexesButton.setVisible(true);
      repairLabelsButton.setVisible(true);
      showIdsCheckBox.setVisible(true);
      showTabIndexesCheckBox.setVisible(true);
      showCoordinatesCheckBox.setVisible(true);
      showOutputOrderCheckBox.setVisible(true);
    }
    else
    {
      repairOutputOrderButton.setVisible(false);
      repairTabIndexesButton.setVisible(false);
      repairLabelsButton.setVisible(false);
      showIdsCheckBox.setVisible(false);
      showTabIndexesCheckBox.setVisible(false);
      showCoordinatesCheckBox.setVisible(false);
      showOutputOrderCheckBox.setVisible(false);
    }
  }

  private void repairOutputOrderButton_actionPerformed(ActionEvent e)
  {
    if (AccessibilityUtils.repairOutputOrder(designer.getComponentViews()))
    {
      designer.fireAccessibilityChangeEvent();
      setModified(true);
    }
    designer.repaint();
  }
  
  private void repairTabIndexesButton_actionPerformed(ActionEvent e)
  {        
    if (AccessibilityUtils.repairTabIndexes(designer.getComponentViews()))
    {
      designer.fireAccessibilityChangeEvent();
      setModified(true);
    }
    designer.repaint();
  }

  private void repairLabelsButton_actionPerformed(ActionEvent e)
  {
    if (AccessibilityUtils.repairLabels(designer.getComponentViews()))
    {
      designer.fireAccessibilityChangeEvent();
      setModified(true);
    }
    designer.repaint();
  }
  
  private void showIdsCheckBox_actionPerformed(ActionEvent e)
  {
    designer.setShowIds(showIdsCheckBox.isSelected());
    designer.repaint();
  }              

  private void showTabIndexesCheckBox_actionPerformed(ActionEvent e)
  {
    designer.setShowTabIndexes(showTabIndexesCheckBox.isSelected());
    designer.repaint();
  }              

  private void showCoordinatesCheckBox_actionPerformed(ActionEvent e)
  {
    designer.setShowCoordinates(showCoordinatesCheckBox.isSelected());
    designer.repaint();
  }

  private void showOutputOrderCheckBox_actionPerformed(ActionEvent e)
  {
    designer.setShowOutputOrder(showOutputOrderCheckBox.isSelected());
    designer.repaint();
  }
  
  private void scriptButton_actionPerformed(ActionEvent e)
  {
    String type = "serverscript";
    String code = null;
    Iterator iter = designer.getComponentViews().iterator();
    ScriptView scriptView = null;
    if (iter.hasNext())
    {
      ComponentView view = (ComponentView)iter.next();
      if (view instanceof ScriptView)
      {
        scriptView = (ScriptView)view;
        type = scriptView.getType();
        code = scriptView.getCode();
      }
    }
    ScriptEditor editor = new ScriptEditor((Frame)getTopLevelAncestor());
    editor.setCode(code);
    editor.setSize(640, 400);
    editor.setLocationRelativeTo(this);
    if (editor.showDialog())
    {
      code = editor.getCode();
      if (code != null && code.trim().length() > 0)
      {
        if (scriptView == null)
        {
          scriptView = new ScriptView();
          scriptView.setType(type);
          scriptView.setCode(code);
          designer.insertComponentView(scriptView, 0);
        }
        else
        {
          scriptView.setType(type);
          scriptView.setCode(code);
        }
      }
      else if (scriptView != null)
      {
        designer.removeComponentView(scriptView);
      }
      setModified(true);
    }
  }
}
