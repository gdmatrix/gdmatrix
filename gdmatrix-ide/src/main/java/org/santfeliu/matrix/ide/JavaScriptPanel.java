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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.text.EditorKit;
import org.santfeliu.swing.FlatSplitPane;
import org.santfeliu.swing.layout.WrapLayout;
import org.santfeliu.swing.text.JavaScriptEditorKit;
import org.santfeliu.swing.text.SymbolHighlighter;

/**
 *
 * @author realor
 */
public class JavaScriptPanel extends TextPanel
{
  private final JToolBar toolBar = new JToolBar();
  private final JButton runButton = new JButton();
  private final JButton stopButton = new JButton();
  private final JToggleButton outputButton = new JToggleButton();
  private final JButton clearButton = new JButton();
  private final JScrollPane scrollPane = new JScrollPane();
  private final JTextArea outputTextArea = new JTextArea();
  private final JSplitPane splitPane = new FlatSplitPane();
  private JavaScriptRunner runner;

  public JavaScriptPanel()
  {
    try
    {
      initComponents();
    }
    catch (Exception ex)
    {
      MatrixIDE.log(ex);
    }
  }

  @Override
  public String getCategoryName()
  {
    return "javascript";
  }

  @Override
  protected String getContentType()
  {
    return "text/javascript";
  }

  @Override
  protected EditorKit getEditorKit()
  {
    return new JavaScriptEditorKit();
  }

  @Override
  protected String getNewDocument()
  {
    return "/** title **/";
  }

  @Override
  protected String getCharset()
  {
    return "utf8";
  }

  private void initComponents()
  {
    toolBar.setFloatable(false);
    toolBar.setRollover(true);
    toolBar.setLayout(new WrapLayout(WrapLayout.LEFT, 2, 2));
    toolBar.setMinimumSize(new Dimension(1, 1));

    outputTextArea.setEditable(false);
    JTextPane textPane = textEditor.getTextPane();
    outputTextArea.setBackground(textPane.getBackground());
    outputTextArea.setForeground(textPane.getForeground());
    outputTextArea.setFont(textPane.getFont());
    scrollPane.getViewport().add(outputTextArea);

    runButton.setText("Run");
    runButton.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/images/run.gif")));
    runButton.addActionListener((ActionEvent e) ->
    {
      runButton_actionPerformed(e);
    });
    toolBar.add(runButton, null);

    stopButton.setText("Stop");
    stopButton.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/images/stop.gif")));
    stopButton.addActionListener((ActionEvent e) ->
    {
      stopButton_actionPerformed(e);
    });
    toolBar.add(stopButton, null);
    stopButton.setEnabled(false);

    outputButton.setText("Show output");
    outputButton.addActionListener((ActionEvent e) ->
    {
      outputButton_actionPerformed(e);
    });
    toolBar.add(outputButton, null);

    clearButton.setText("Clear");
    clearButton.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/images/clear.gif")));
    clearButton.addActionListener((ActionEvent e) ->
    {
      clearButton_actionPerformed(e);
    });
    toolBar.add(clearButton, null);
    clearButton.setEnabled(false);


    Color borderColor = UIManager.getColor("Panel.background").darker();
    toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor));
    this.add(toolBar, BorderLayout.NORTH);

    SymbolHighlighter symbolHighlighter =
      new SymbolHighlighter(textEditor.getTextPane(), "({[", ")}]");

    splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
  }

  private void runButton_actionPerformed(ActionEvent e)
  {
    if (runner == null)
    {
      if (!outputButton.isSelected())
      {
        outputButton.setSelected(true);
        showOutput();
      }
      runButton.setEnabled(false);
      stopButton.setEnabled(true);
      outputTextArea.setText("");
      JTextPane textPane = textEditor.getTextPane();
      String code = textPane.getSelectedText();
      if (code == null)
      {
        code = textPane.getText();
      }
      runner = new JavaScriptRunner(code, outputTextArea);
      runner.getVariables().put("ide", getMainPanel().getIDE());
      runner.setResultConsumer(result ->
      {
        runner = null;
        runButton.setEnabled(true);
        stopButton.setEnabled(false);
      });
      runner.start();
    }
  }

  private void stopButton_actionPerformed(ActionEvent e)
  {
    if (runner != null)
    {
      runner.end();
    }
  }

  private void outputButton_actionPerformed(ActionEvent e)
  {
    if (outputButton.isSelected())
    {
      showOutput();
    }
    else
    {
      hideOutput();
    }
  }

  private void clearButton_actionPerformed(ActionEvent e)
  {
    outputTextArea.setText("");
  }

  private void showOutput()
  {
    remove(textEditor);
    add(splitPane, BorderLayout.CENTER);
    splitPane.setTopComponent(textEditor);
    splitPane.setBottomComponent(scrollPane);
    splitPane.setDividerLocation((int)(0.7 * textEditor.getHeight()));
    clearButton.setEnabled(true);
    invalidate();
    revalidate();
  }

  private void hideOutput()
  {
    remove(splitPane);
    add(textEditor);
    clearButton.setEnabled(false);
    invalidate();
    revalidate();
  }
}
