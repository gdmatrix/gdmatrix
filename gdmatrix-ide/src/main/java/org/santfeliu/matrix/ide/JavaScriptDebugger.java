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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.santfeliu.swing.text.JavaScriptEditorKit;
import org.santfeliu.swing.text.SymbolHighlighter;
import org.santfeliu.swing.text.TextEditor;
import org.santfeliu.util.script.ScriptableBase;


/**
 *
 * @author realor
 */
public class JavaScriptDebugger extends JDialog
{
  private Runner runner;
  private TreeMap persistentVariables = new TreeMap();
  private Object result;
  private Exception error;
  private BorderLayout borderLayout = new BorderLayout();
  private JToolBar toolBar = new JToolBar();
  private JButton resetButton = new JButton();
  private JButton runButton = new JButton();
  private JButton stopButton = new JButton();
  private JSplitPane splitPane = new JSplitPane();
  private JScrollPane scrollPane = new JScrollPane();
  private TextEditor textEditor = new TextEditor();
  private JTable varsTable = new JTable();
  private JButton clearButton = new JButton();
  private ImageIcon textIcon;
  private ImageIcon numberIcon;
  private ImageIcon booleanIcon;
  private DefaultTableModel tableModel = new DefaultTableModel()
  {
    @Override
    public boolean isCellEditable(int row, int column)
    {
      return false;
    }
  };

  public JavaScriptDebugger(Frame parent)
  {
    super(parent, false);
    try
    {
      initComponents();
    }
    catch(Exception e)
    {      
    }
  }

  private void initComponents() throws Exception
  {    
    this.setTitle("Javascript debugger");
    this.setSize(new Dimension(540, 450));
    this.getContentPane().setLayout(borderLayout);
    toolBar.setFloatable(false);
    resetButton.setText("Reset");
    resetButton.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          resetButton_actionPerformed(e);
        }
      });
    runButton.setText("Run");
    runButton.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          runButton_actionPerformed(e);
        }
      });
    stopButton.setText("Stop");
    stopButton.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          stopButton_actionPerformed(e);
        }
      });
    splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    splitPane.setDividerLocation(200);
    textEditor.getTextPane().setFont(Options.getEditorFont());
    clearButton.setText("Clear");
    clearButton.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          clearButton_actionPerformed(e);
        }
      });
    toolBar.add(runButton, null);
    toolBar.add(stopButton, null);
    toolBar.add(resetButton, null);    
    toolBar.add(clearButton, null);
    this.getContentPane().add(toolBar, BorderLayout.NORTH);
    splitPane.add(textEditor, JSplitPane.LEFT);
    scrollPane.getViewport().add(varsTable, null);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    splitPane.add(scrollPane, JSplitPane.RIGHT);
    this.getContentPane().add(splitPane, BorderLayout.CENTER);
    varsTable.setAutoCreateColumnsFromModel(false);
    tableModel.addColumn("Variable");
    tableModel.addColumn("Value");
    varsTable.setModel(tableModel);
    varsTable.addColumn(new TableColumn(0, 100, new VariableCellRenderer(), null));
    varsTable.addColumn(new TableColumn(1, 300, null, null));
    varsTable.getTableHeader().setReorderingAllowed(false);
    varsTable.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyReleased(KeyEvent e)
      {
        varsTable_keyReleased(e);
      }
    });
    textEditor.getTextPane().setEditorKitForContentType("text/javascript",
      new JavaScriptEditorKit());
    textEditor.getTextPane().setContentType("text/javascript");
    
    SymbolHighlighter cancelMatcher =
      new SymbolHighlighter(textEditor.getTextPane(), "({[", ")}]");

    textEditor.getTextPane().setSelectionColor(new Color(198, 198, 198));
    numberIcon = 
        new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/images/number_type.gif"));

    textIcon = new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/images/text_type.gif"));

    booleanIcon = new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/images/boolean_type.gif"));

    ImageIcon icon;
    icon = new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/images/run.gif"));
    runButton.setIcon(icon);

    icon = new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/images/stop.gif"));
    stopButton.setIcon(icon);

    icon = new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/images/reset.gif"));
    resetButton.setIcon(icon);

    icon = new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/images/clear.gif"));
    clearButton.setIcon(icon);

    stopButton.setEnabled(false);
  }

  private void runButton_actionPerformed(ActionEvent e)
  {
    run(textEditor.getTextPane().getText());
  }
  
  private void clearButton_actionPerformed(ActionEvent e)
  {
    textEditor.getTextPane().setText("");
  }

  private void resetButton_actionPerformed(ActionEvent e)
  {
    reset();
    updateTable();
  }

  private void stopButton_actionPerformed(ActionEvent e)
  {
    try
    {
      stop();
    }
    catch (Exception ex)
    {
      System.out.println(ex);
    }
  }

  private void varsTable_keyReleased(KeyEvent e)
  {
    if (e.getKeyCode() == KeyEvent.VK_DELETE)
    {
      int[] rows = varsTable.getSelectedRows();
      for (int row : rows)
      {
        String variable = (String)tableModel.getValueAt(row, 0);
        persistentVariables.remove(variable);
      }
      updateTable();
    }
  }

  /* actions */
  private void reset()
  {
    result = null;
    error = null;
    persistentVariables.clear();
  }

  private void run(String code)
  {
    if (runner == null)
    {
      result = null;
      error = null;
      runner = new Runner(code);
      runner.start();
      updateButtons(true);
    }
  }
  
  private void stop() throws Exception
  {
    if (runner != null)
    {
      runner.end();
    }
  }
  
  private void updateButtons(boolean running)
  {
    runButton.setEnabled(!running);
    stopButton.setEnabled(running);
    resetButton.setEnabled(!running);        
    clearButton.setEnabled(!running);
    textEditor.setEnabled(!running);
  }
  
  private void updateTable()
  {
    tableModel.setRowCount(0);
    if (result != null)
    {
      tableModel.addRow(new Object[]{"<RESULT>", result});
    }
    Iterator iter = persistentVariables.entrySet().iterator();
    while (iter.hasNext())
    {
      Map.Entry entry = (Map.Entry)iter.next();
      Object variable = entry.getKey();
      Object value = entry.getValue();
      tableModel.addRow(new Object[]{variable, value});
    }
  }
  
  private void showError()
  {
    String message = error.getMessage();
    if (message == null) message = error.toString();

    int i1 = message.indexOf("(#");
    if (i1 != -1)
    {
      String fragment = message.substring(i1 + 2);
      int i2 = fragment.indexOf(")");
      if (i2 != -1)
      {
        String line = fragment.substring(0, i2);
        message = "Line " + line + ": "  + message.substring(0, i1);
      }
    }

    JOptionPane.showMessageDialog(
      JavaScriptDebugger.this, 
      message, "ERROR", JOptionPane.ERROR_MESSAGE);
  }
  
  public class Runner extends Thread
  {
    private final String code;
    
    public Runner(String code)
    {
      this.code = code;
    }
    
    @Override
    public void run()
    {
      Context cx = ContextFactory.getGlobal().enterContext();
      try
      {
        Scriptable scope = 
          new ScriptableBase(cx, persistentVariables);
        result = cx.evaluateString(scope, code, "", 1, null);
      }
      catch (Exception ex)
      {
        error = ex;
      }
      catch (ThreadDeath d)
      {
      }
      finally
      {
        Context.exit();
        runner = null;
        SwingUtilities.invokeLater(new Runnable()
        {
          @Override
          public void run()
          {
            updateButtons(false);
            updateTable();
            if (error != null)
            {
              showError();
            }
          }
        });
      }
    }
    
    public void end() throws Exception
    {
      Method method = this.getClass().getMethod("stop", new Class[0]);
      if (method != null)
      {
        method.invoke(this, new Object[0]);
      }
    }
  }

  public class VariableCellRenderer extends DefaultTableCellRenderer
  {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus, int row, int column)  
    {
      if (isSelected)
      {
        setBackground(table.getSelectionBackground());
        setForeground(table.getSelectionForeground());
      }
      else
      {
        setBackground(table.getBackground());
        setForeground(table.getForeground());
      }
      setEnabled(table.isEnabled());
      setFont(table.getFont());
      
      setText(String.valueOf(value));
      Object varValue = table.getValueAt(row, column + 1);
      if (varValue instanceof Number)
      {
        setIcon(numberIcon);  
      }
      else if (varValue instanceof String)
      {
        setIcon(textIcon);
      }
      else if (varValue instanceof Boolean)
      {
        setIcon(booleanIcon);
      }
      else
      {
        setIcon(null);
      }
      return this;
    }
  }
}