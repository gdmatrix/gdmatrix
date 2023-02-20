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
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.text.EditorKit;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.swing.layout.WrapLayout;
import org.santfeliu.swing.text.JavaScriptDocument;
import org.santfeliu.swing.text.JavaScriptEditorKit;
import org.santfeliu.swing.text.SymbolHighlighter;

/**
 *
 * @author realor
 */
public class ObjectSetupPanel extends TextPanel
{
  private final JToolBar toolBar = new JToolBar();
  private final JButton shiftRightButton = new JButton();
  private final JButton shiftLeftButton = new JButton();

  public ObjectSetupPanel()
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
  protected String getContentType()
  {
    return "application/json";
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

    shiftLeftButton.setText("Shift left");
    shiftLeftButton.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/images/shift_line_left.png")));
    shiftLeftButton.addActionListener((ActionEvent e) ->
    {
      shiftLeftButton_actionPerformed(e);
    });
    toolBar.add(shiftLeftButton, null);

    shiftRightButton.setText("Shift right");
    shiftRightButton.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/images/shift_line_right.png")));
    shiftRightButton.addActionListener((ActionEvent e) ->
    {
      shiftRightButton_actionPerformed(e);
    });
    toolBar.add(shiftRightButton, null);

    Color borderColor = UIManager.getColor("Panel.background").darker();
    toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor));
    this.add(toolBar, BorderLayout.NORTH);

    SymbolHighlighter symbolHighlighter =
      new SymbolHighlighter(textEditor.getTextPane(), "({[", ")}]");

  }

  private void shiftLeftButton_actionPerformed(ActionEvent e)
  {
    int indentSpaces = Options.getIndentSpaces();
    removeStringFromSelection(getIndent(indentSpaces));
  }

  private void shiftRightButton_actionPerformed(ActionEvent e)
  {
    int indentSpaces = Options.getIndentSpaces();
    insertStringToSelection(getIndent(indentSpaces));
  }

  private String getIndent(int size)
  {
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < size; i++)
    {
      buffer.append(" ");
    }
    return buffer.toString();
  }

  private void insertStringToSelection(String text)
  {
    try
    {
      JTextPane textPane = textEditor.getTextPane();
      JavaScriptDocument document = (JavaScriptDocument)textPane.getDocument();
      int start = textPane.getSelectionStart();
      int end = textPane.getSelectionEnd();
      if (start == end) return;

      String code = document.getText(start, end - start);
      String[] lines = code.split("\n");
      for (int i = 0; i < lines.length; i++)
      {
        String line = lines[i];
        if (!StringUtils.isBlank(line))
        {
          lines[i] = text + line;
        }
      }
      code = String.join("\n", lines);
      document.replace(start, end - start, code, null);

      textPane.setSelectionStart(start);
      textPane.setSelectionEnd(start + code.length());
    }
    catch (Exception ex)
    {
    }
  }

  private void removeStringFromSelection(String text)
  {
    try
    {
      JTextPane textPane = textEditor.getTextPane();
      JavaScriptDocument document = (JavaScriptDocument)textPane.getDocument();
      int start = textPane.getSelectionStart();
      int end = textPane.getSelectionEnd();
      if (start == end) return;

      String code = document.getText(start, end - start);
      String[] lines = code.split("\n");
      for (int i = 0; i < lines.length; i++)
      {
        String line = lines[i];
        int index = line.indexOf(text);
        if (index >= 0 && line.substring(0, index).trim().length() == 0)
        {
          lines[i] = line.substring(0, index) +
            line.substring(index + text.length());
        }
      }
      code = String.join("\n", lines);
      document.replace(start, end - start, code, null);

      textPane.setSelectionStart(start);
      textPane.setSelectionEnd(start + code.length());
    }
    catch (Exception ex)
    {
    }
  }
}
