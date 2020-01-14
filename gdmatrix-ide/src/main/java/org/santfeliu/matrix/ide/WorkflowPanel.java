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
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.TransferHandler;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import org.jgraph.JGraph;
import org.jgraph.event.GraphModelListener;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphUndoManager;
import org.santfeliu.workflow.Workflow;
import org.santfeliu.workflow.io.WorkflowReader;
import org.santfeliu.workflow.io.WorkflowWriter;
import org.santfeliu.matrix.ide.action.RemoveAction;
import org.santfeliu.swing.palette.Palette;
import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.swing.graph.WorkflowCellViewFactory;
import org.santfeliu.workflow.swing.graph.WorkflowEdge;
import org.santfeliu.workflow.swing.graph.WorkflowModel;
import org.santfeliu.workflow.swing.graph.WorkflowVertex;
import org.santfeliu.workflow.util.WorkflowFixer;
import com.l2fprod.common.propertysheet.Property;


/**
 *
 * @author unknown
 */
public class WorkflowPanel extends DocumentPanel
{
  private BorderLayout borderLayout = new BorderLayout();
  private JScrollPane scrollPane = new JScrollPane();
  private JGraph graph = new JGraph();
  private WorkflowModel workflowModel;
  private GraphUndoManager undoManager = new GraphUndoManager()
  {
    @Override
    public boolean addEdit(UndoableEdit edit)
    {
      boolean result = super.addEdit(edit);
      getMainPanel().updateActions();
      return result;
    }
  };
  private String textToFind;
  private int findIndex;

  public WorkflowPanel()
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

  public void setWorkflow(Workflow workflow)
  {
    this.workflowModel = new WorkflowModel(workflow);
    graph.setModel(workflowModel);
    undoManager.discardAllEdits();
    workflowModel.addUndoableEditListener(undoManager);
    workflowModel.addGraphModelListener(new GraphModelListener()
    {
      public void graphChanged(org.jgraph.event.GraphModelEvent event)
      {
        setModified(true);
      }    
    });
  }

  public Workflow getWorkflow()
  {
    return workflowModel.getWorkflow();
  }
    
  public JGraph getGraph()
  {
    return graph;
  }

  @Override
  public UndoManager getUndoManager()
  {
    return undoManager;
  }

  @Override
  public void activate()
  {
    if (getGraph().getSelectionCount() == 0)
    {
      Workflow workflow = getWorkflow();
      getMainPanel().setEditObject(workflow);
    }
    else
    {
      Object cell = getGraph().getSelectionCell();
      if (cell instanceof DefaultGraphCell)
      {
        getMainPanel().setEditObject(((DefaultGraphCell)cell).getUserObject());
      }
    }
    getMainPanel().getPalette().setSelectedCategory("workflow");
    getMainPanel().setRightPanelVisible(true);

    // set colors on activate: BUG in JGraph?
    graph.setGridColor(new Color(240, 240, 240));
    graph.setBackground(Color.WHITE);
  }

  @Override
  public void create()
  {
    Workflow workflow = new Workflow();
    workflow.setName(getDisplayName());
    setWorkflow(workflow);
    updateGrid();
  }

  @Override
  public void open(InputStream os) throws Exception
  {
    WorkflowReader reader = new WorkflowReader();
    Workflow workflow = reader.read(os);
    
    setDisplayName(workflow.getName());
    setDescription(workflow.getDescription());
    setWorkflow(workflow);
    updateGrid();

    // fix workflow
    String fixOnOpen = Options.get("workflowFixOnOpen");
    if ("true".equals(fixOnOpen))
    {
      String fixRulesURL = Options.get("workflowFixRulesURL");
      if (fixRulesURL != null && fixRulesURL.trim().length() > 0)
      {
        try
        {
          URL url = new URL(fixRulesURL);
          WorkflowFixer fixer = WorkflowFixer.getInstance(url);
          List<WorkflowFixer.Issue> issues =
            fixer.check(workflow.getNodes());
          if (!issues.isEmpty())
          {
            MatrixIDE ide = getMainPanel().getIDE();
            WorkflowFixerDialog dialog = new WorkflowFixerDialog(ide, false);
            dialog.init(this, issues);
            dialog.setSize(760, 400);
            dialog.setLocationRelativeTo(ide);
            dialog.setVisible(true);
          }
        }
        catch (Exception ex)
        {
          // ignore errors
        }
      }
    }
  }
  
  @Override
  public void save(OutputStream os) throws Exception
  {
    Workflow workflow = getWorkflow();
    workflow.setName(getDisplayName());
    workflow.setDescription(getDescription());
    WorkflowWriter writer = new WorkflowWriter();
    writer.write(workflow, os);
    getMainPanel().setEditObject(workflow);
  }

  @Override
  public void copy()
  {
    // deselect isolated transitions
    HashSet vertexSet = new HashSet();
    Object[] cells = graph.getSelectionCells();
    for (int i = 0; i < cells.length; i++) // find selected nodes
    {
      Object cell = cells[i];
      if (cell instanceof WorkflowVertex)
      {
        WorkflowVertex vertex = (WorkflowVertex)cell;
        String nodeId = vertex.getNode().getId();
        vertexSet.add(nodeId);
      }
    }
    HashSet selection = new HashSet();
    for (int i = 0; i < cells.length; i++)
    {
      Object cell = cells[i];
      if (cell instanceof WorkflowEdge)
      {
        WorkflowEdge edge = (WorkflowEdge)cell;
        String nodeId = edge.getTransition().getSourceNode().getId();
        String nextNodeId = edge.getTransition().getNextNodeId();
        if (vertexSet.contains(nodeId) && vertexSet.contains(nextNodeId))
        {
          selection.add(cell);
        }
      }
      else
      {
        selection.add(cell);
      }
    }
    graph.setSelectionCells(selection.toArray());
    ActionEvent actionEvent = new ActionEvent(getGraph(), 0, "copy", 0);
    TransferHandler.getCopyAction().actionPerformed(actionEvent);
  }

  @Override
  public void paste()
  {
    ActionEvent actionEvent = new ActionEvent(getGraph(), 0, "paste", 0);
    TransferHandler.getPasteAction().actionPerformed(actionEvent);
  }

  @Override
  public void delete()
  {
    try
    {
      HashSet cellsToRemove = new HashSet();
      Object[] cells = graph.getSelectionCells();
      cells = graph.getDescendants(cells);
      
      for (int i = 0; i < cells.length; i++)
      {
        cellsToRemove.add(cells[i]);
      }
      for (int i = 0; i < cells.length; i++)
      {
        if (cells[i] instanceof WorkflowVertex)
        {
          WorkflowVertex vertex = (WorkflowVertex)cells[i];
          Iterator iter = graph.getModel().edges(vertex.getFirstChild());
          while (iter.hasNext())
          {
            Object edge  = iter.next();
            if (edge instanceof WorkflowEdge)
            {
              cellsToRemove.add(edge);
            }
          }
        }
      }
      graph.getModel().remove(cellsToRemove.toArray());
    }
    catch (Exception ex)
    {
      MatrixIDE.log(ex);
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
        WorkflowModel model = (WorkflowModel)getGraph().getModel();
        if (!text.equals(textToFind))
        {
          textToFind = text;
          findIndex = -1;
        }
        int oldFindIndex = findIndex;                
        List roots = model.getRoots();
        Object root = null;
        findIndex++;
        while (findIndex < roots.size() && !found)
        {
          root = roots.get(findIndex);
          found = found(root, text);
          if (!found) findIndex++;
        }
        if (found)
        {
          WorkflowVertex vertex = (WorkflowVertex)root;
          graph.setSelectionCell(vertex);
          graph.scrollCellToVisible(vertex);
        }
        else findIndex = oldFindIndex;
        return found;
      }

      @Override      
      protected boolean previous(String text)
      {
        boolean found = false;
        WorkflowModel model = (WorkflowModel)getGraph().getModel();
        if (!text.equals(textToFind))
        {
          textToFind = text;
          findIndex = model.getRoots().size() - 1;
        }
        int oldFindIndex = findIndex;        
        List roots = model.getRoots();
        Object root = null;
        findIndex--;
        while (findIndex > 0 && !found)
        {
          root = roots.get(findIndex);
          found = found(root, text);
          if (!found) findIndex--;
        }
        if (found)
        {
          WorkflowVertex vertex = (WorkflowVertex)root;
          graph.setSelectionCell(vertex);
          graph.scrollCellToVisible(vertex);
        }
        else findIndex = oldFindIndex;
        return found;
      }

      private boolean found(Object root, String text)
      {
        WorkflowNode node = null;
        boolean found = false;
        if (root instanceof WorkflowVertex)
        {
          WorkflowVertex vertex = (WorkflowVertex)root;
          node = vertex.getNode();
          found = node.containsText(textToFind);
        }
        return found;
      }
    };

    dialog.setTitle("Find node");
    dialog.setTextToFind(textToFind);
    dialog.setLabelText("Enter nodeId or description:");
    dialog.showDialog();
  }

  @Override
  public void setZoom(double size)
  {
    graph.setScale(size / 100.0);
  }
  
  @Override
  public double getZoom()
  {
    return graph.getScale() * 100.0;
  }

  @Override
  public boolean isFindEnabled()
  {
    return true;
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
            int margin = 20;
            Graphics2D g2 = (Graphics2D)g;

            if (workflowModel.getWorkflow().getNodesCount() > 0)
            {
              JGraph printGraph = new JGraph();
              printGraph.setSize(800, 600);
              printGraph.setAntiAliased(false);
              printGraph.setGridEnabled(false);
              printGraph.setPortsVisible(false);
              printGraph.getGraphLayoutCache().setFactory(
                new WorkflowCellViewFactory());
              printGraph.setModel(workflowModel);
              Rectangle2D bounds =
                printGraph.getCellBounds(WorkflowModel.getRoots(workflowModel));

              double graphWidth = bounds.getWidth();
              double graphHeight = bounds.getHeight();
              printGraph.setSize(
                (int)Math.ceil(graphWidth),
                (int)Math.ceil(graphHeight));

              Frame frame = new Frame();
              frame.add(printGraph);
              frame.setSize((int)Math.ceil(graphWidth),
                (int)Math.ceil(graphHeight));
              frame.pack();

              double imgX = pageFormat.getImageableX();
              double imgY = pageFormat.getImageableY() + margin;
              double imgWidth = pageFormat.getImageableWidth();
              double imgHeight = pageFormat.getImageableHeight() - margin;
              double xScale = imgWidth / graphWidth;
              double yScale = imgHeight / graphHeight;
              double scale = Math.min(xScale, yScale);

              Point2D center = new Point2D.Double();
              center.setLocation(bounds.getCenterX(), bounds.getCenterY());

              printGraph.setScale(scale, center);
              g2.translate(imgX, imgY);
              g2.translate(-scale * bounds.getX(), -scale * bounds.getY());
              printGraph.paint(g);
              g2.translate(scale * bounds.getX(), scale * bounds.getY());
              g2.translate(-imgX, -imgY);
              frame.dispose();
            }

            String title = WorkflowPanel.super.getDisplayName();
            if (title != null)
            {
              String description = WorkflowPanel.super.getDescription();
              if (description != null)
              {
                title += ": " + description.trim();
              }
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
      ex.printStackTrace();
      MatrixIDE.log(ex);
    }
  }

  @Override
  public void objectPropertyChanged(Object editObject, Property property)
  {
    if (editObject instanceof Workflow)
    {
      Workflow workflow = (Workflow)editObject;
      setDisplayName(workflow.getName());
      setDescription(workflow.getDescription());
      updateGrid();
    }
  }

  public void addNode(Point point)
  {
    try
    {
      Palette palette = getMainPanel().getPalette();
      String nodeClassName = palette.getSelectedElementAttribute("class");
      if (nodeClassName != null)
      {
        Class nodeClass = Class.forName(nodeClassName);
        WorkflowModel model = (WorkflowModel)graph.getModel();
        WorkflowNode node = (WorkflowNode)nodeClass.newInstance();
        Map attributes = new HashMap();
        WorkflowVertex vertex = new WorkflowVertex(node);
        vertex.add(new DefaultPort());
        Map map = new HashMap();

        Workflow workflow = workflowModel.getWorkflow();
        int nodeWidth = workflow.getNodeWidth();
        int nodeHeight = workflow.getNodeHeight();
        GraphConstants.setBounds(map, 
          getNodeBounds(point, nodeWidth, nodeHeight));
        GraphConstants.setResize(map, false);
        GraphConstants.setEditable(map, false);
        GraphConstants.setOpaque(map, true);
        GraphConstants.setBackground(map, Color.yellow);

        attributes.put(vertex, map);
        model.insert(new Object[]{vertex}, attributes, null, null, null);
      }
    }
    catch (Exception ex)
    {
      MatrixIDE.log(ex);
    }
    finally
    {
      graph.setCursor(Cursor.getDefaultCursor());
    }
  }

  public void updateGrid()
  {
    Workflow workflow = workflowModel.getWorkflow();
    graph.setGridSize(workflow.getGridSize());
    graph.setGridVisible(workflow.isGridVisible());
    graph.setGridEnabled(workflow.isGridEnabled());
  }

  /* internal methods */

  private void initComponents() throws Exception
  {
    this.setLayout(borderLayout);
    this.add(scrollPane, BorderLayout.CENTER);
    scrollPane.getViewport().add(graph, null);
    graph.setPortsVisible(true);
    graph.setConnectable(false);
    graph.setCloneable(true);
    graph.setDisconnectable(false);
    graph.setGridEnabled(true);
    graph.setGridSize(8);
    graph.setGridVisible(true);
    graph.setGridMode(JGraph.LINE_GRID_MODE);
    graph.setGridColor(new Color(240, 240, 240));
    graph.setBackground(Color.WHITE);
    graph.setAntiAliased(true);
    graph.setDoubleBuffered(false);
    graph.getGraphLayoutCache().setFactory(new WorkflowCellViewFactory());
    graph.setMarqueeHandler(new WorkflowMarqueeHandler(this));
    graph.addGraphSelectionListener(new GraphSelectionListener()
    {
      public void valueChanged(GraphSelectionEvent e)
      {
        JGraph graph = (JGraph)e.getSource();
        Object cell = graph.getSelectionCell();
        if (cell instanceof DefaultGraphCell)
        {
          DefaultGraphCell graphCell = (DefaultGraphCell)cell;
          Object object = graphCell.getUserObject();
          getMainPanel().setEditObject(object);
        }
        else
        {
          getMainPanel().setEditObject(workflowModel.getWorkflow());
        }
      }
    });
    scrollPane.setBorder(BorderFactory.createEmptyBorder());

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
      public void mouseExited(MouseEvent event)
      {
        graph.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
    });

    graph.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent event)
      {
        if (event.getKeyCode() == KeyEvent.VK_DELETE)
        {
          RemoveAction action = new RemoveAction();
          action.setIDE(getMainPanel().getIDE());
          action.actionPerformed(new ActionEvent(this, 1200, "remove"));
        }
      }
    });
    DropTarget dropTarget = new DropTarget(graph, new DropTargetAdapter()
    {
      @Override
      public void drop(DropTargetDropEvent event)
      {
        addNode(event.getLocation());
        event.dropComplete(true);
        graph.setCursor(Cursor.getDefaultCursor());
      }
    });
    dropTarget.setActive(true);
  }

  private Rectangle2D getNodeBounds(Point point, int width, int height)
  {
    int x = (int)point.getX();
    int y = (int)point.getY();

    double scale = graph.getScale();

    if (graph.isGridEnabled())
    {
      int gridSize = (int)graph.getGridSize();
      width = (width / gridSize) * gridSize;
      height = (height / gridSize) * gridSize;
      x = (x / gridSize) * gridSize;
      y = (y / gridSize) * gridSize;
    }
    x = (int)(x / scale);
    y = (int)(y / scale);

    Rectangle2D rect = new Rectangle2D.Double();
    rect.setFrame(x, y, width, height);
    return rect;
  }
}
