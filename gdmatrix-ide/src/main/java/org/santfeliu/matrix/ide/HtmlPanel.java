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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.TransferHandler;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.undo.UndoManager;
import org.santfeliu.swing.FlatSplitPane;
import org.santfeliu.swing.text.TextEditor;
import org.santfeliu.swing.text.XMLEditorKit;

/**
 *
 * @author realor
 */
public class HtmlPanel extends DocumentPanel
{
  private static final String CHARSET = "utf-8";
  private BorderLayout borderLayout1 = new BorderLayout();
  private JScrollPane scrollPane = new JScrollPane();
  private TextEditor textEditor = new TextEditor();
  private JTextPane viewTextPane = new JTextPane();
  private JSplitPane splitPane = new FlatSplitPane();
  private UndoManager undoManager = new UndoManager();
  private UndoHandler undoHandler = new UndoHandler();
  private Thread updateViewThread = null;

  protected int findIndexStart = 0;
  protected int findIndexEnd = 0;  
  
  public HtmlPanel()
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

  private void initComponents() throws Exception
  {
    this.setSize(new Dimension(471, 408));
    this.setLayout(borderLayout1);
    textEditor.getTextPane().setFont(getEditorFont());
    scrollPane.getViewport().add(viewTextPane, null);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());

    splitPane.add(textEditor, JSplitPane.TOP);
    splitPane.add(scrollPane, JSplitPane.BOTTOM);
    splitPane.setDividerLocation(200);
    splitPane.setBorder(BorderFactory.createEmptyBorder());

    this.add(splitPane, BorderLayout.CENTER);
    splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

    textEditor.getTextPane().setEditorKitForContentType(
      "text/html", new XMLEditorKit());
    textEditor.getTextPane().setContentType("text/html");
    textEditor.getTextPane().setSelectionColor(Color.LIGHT_GRAY);
    textEditor.getTextPane().getDocument().addDocumentListener(
      new DocumentListener()
    {
      public void insertUpdate(DocumentEvent e)
      {
        updateView(1000);
        setModified(true);
      }

      public void removeUpdate(DocumentEvent e)
      {
        updateView(1000);
        setModified(true);
      }

      public void changedUpdate(DocumentEvent e)
      {
        updateView(1000);
        setModified(true);
      }
    });

    viewTextPane.setEditorKitForContentType("text/html", new HTMLEditorKit());
    viewTextPane.setContentType("text/html");
    viewTextPane.setEditable(false);
  }

  @Override
  public UndoManager getUndoManager()
  {
    return undoManager;
  }

  @Override
  public boolean isFindEnabled()
  {
    return true;
  } 
  
  @Override
  public void find()
  {
    findIndexStart = 0;
    findIndexEnd = 0;
    JEditorPane textPane = textEditor.getTextPane();
    textPane.requestFocus();

    FindDialog dialog = new FindDialog(getMainPanel().getIDE())
    {
      @Override
      protected boolean next(String text)
      {
        boolean found = false;
        JEditorPane textPane = textEditor.getTextPane();
        String xmlText = textPane.getText();
        if (findIndexEnd >= xmlText.length()) return false;

        int index = xmlText.indexOf(text, findIndexEnd);
        if (index != -1)
        {
          findIndexStart = index;
          findIndexEnd = index + text.length();
          textPane.select(findIndexStart, findIndexEnd);
          scrollToSelection();
          textPane.repaint();
          found = true;
        }
        return found;
      }

      @Override
      protected boolean previous(String text)
      {
        boolean found = false;
        JEditorPane textPane = textEditor.getTextPane();
        String xmlText = textPane.getText();
        if (findIndexStart >= xmlText.length())
        {
          findIndexStart = xmlText.length() - 1;
          findIndexEnd = xmlText.length() - 1;
        }
        else
        {
          xmlText = xmlText.substring(0, findIndexStart);
        }

        int index = xmlText.lastIndexOf(text);
        if (index != -1)
        {
          findIndexStart = index;
          findIndexEnd = index + text.length();
          textPane.select(findIndexStart, findIndexEnd);
          scrollToSelection();
          textPane.repaint();
          found = true;
        }
        return found;
      }

      protected void scrollToSelection()
      {
        try
        {
          JEditorPane textPane = textEditor.getTextPane();
          Rectangle rect = textPane.modelToView(findIndexStart);
          rect.union(textPane.modelToView(findIndexEnd));
          rect.grow(100, 100);
          textPane.scrollRectToVisible(rect);
        }
        catch (BadLocationException ex)
        {
        }
      }
    };

    dialog.setTitle("Find text");
    dialog.setLabelText("Enter text to find:");
    dialog.showDialog();
  }  
  
  @Override
  public void create()
  {
    textEditor.getTextPane().setText("<html>\n" +
    "  <head>\n" +
    "    <title>new</title>\n" +
    "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" +
    "  </head>\n" +
    "  <body>\n" +
    "  </body>\n" +
    "</html>\n");
    undoManager.discardAllEdits();
    textEditor.getTextPane().getDocument().removeUndoableEditListener(undoHandler);
    textEditor.getTextPane().getDocument().addUndoableEditListener(undoHandler);
  }

  @Override
  public void open(InputStream is) throws Exception
  {
    StringBuilder buffer = new StringBuilder();
    BufferedReader reader =
      new BufferedReader(new InputStreamReader(is, CHARSET));
    try
    {
      String line = reader.readLine();
      while (line != null)
      {
        buffer.append(line);
        buffer.append("\n");
        line = reader.readLine();
      }
    }
    finally
    {
      reader.close();
    }
    textEditor.getTextPane().setText(buffer.toString());
    undoManager.discardAllEdits();
    textEditor.getTextPane().getDocument().removeUndoableEditListener(undoHandler);
    textEditor.getTextPane().getDocument().addUndoableEditListener(undoHandler);
    textEditor.getTextPane().setCaretPosition(0);
    updateView(0);
  }

  @Override
  public void save(OutputStream os) throws Exception
  {
    String text = textEditor.getTextPane().getText();
    os.write(text.getBytes(CHARSET));
  }

  @Override
  public void copy()
  {
    ActionEvent actionEvent = new ActionEvent(textEditor, 0, "copy", 0);
    TransferHandler.getCopyAction().actionPerformed(actionEvent);
  }

  @Override
  public void paste()
  {
    ActionEvent actionEvent = new ActionEvent(textEditor, 0, "paste", 0);
    TransferHandler.getPasteAction().actionPerformed(actionEvent);
  }

  @Override
  public void delete()
  {
    try
    {
      int start = textEditor.getTextPane().getSelectionStart();
      int end = textEditor.getTextPane().getSelectionEnd();
      textEditor.getTextPane().getDocument().remove(start, end - start);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  private void updateView(long time)
  {
    if (updateViewThread == null)
    {
      updateViewThread = new UpdateViewThread(time);
      updateViewThread.start();
    }
  }

  class UndoHandler implements UndoableEditListener
  {
    public void undoableEditHappened(UndoableEditEvent e)
    {
      undoManager.addEdit(e.getEdit());
      getMainPanel().updateActions();
    }
  }

  class UpdateViewThread extends Thread
  {
    long time;

    UpdateViewThread(long time)
    {
      this.time = time;
    }

    public void run()
    {
      try
      {
        Thread.sleep(time);
      }
      catch (Exception ex)
      {
      }
      String text = textEditor.getTextPane().getText();
      int index = text.indexOf("<HEAD");
      if (index == -1) index = text.indexOf("<head");
      if (index != -1)
      {
        int index2 = text.indexOf("</HEAD>");
        if (index2 == -1) index2 = text.indexOf("</head>");
        if (index2 != -1)
        {
          text = text.substring(0, index) + text.substring(index2 + 7);
        }
      }
      viewTextPane.setText(text);
      updateViewThread = null;
    }
  }
}
