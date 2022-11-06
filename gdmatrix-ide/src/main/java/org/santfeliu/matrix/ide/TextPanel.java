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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.JEditorPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.undo.UndoManager;
import org.santfeliu.swing.palette.Palette;
import org.santfeliu.swing.text.TextEditor;
import org.santfeliu.swing.undo.DelayUndoManager;

/**
 *
 * @author realor
 */
public abstract class TextPanel extends DocumentPanel
{
  protected BorderLayout borderLayout = new BorderLayout();
  protected TextEditor textEditor = new TextEditor();
  protected UndoHandler undoHandler = new UndoHandler();
  protected DelayUndoManager undoManager = new DelayUndoManager()
  {
    @Override
    public void onNewEdit()
    {
      getMainPanel().updateActions();
    }
  };
  protected int findIndexStart = 0;
  protected int findIndexEnd = 0;

  public TextPanel()
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
  public UndoManager getUndoManager()
  {
    return undoManager;
  }

  @Override
  public void create()
  {
    final JTextPane textPane = textEditor.getTextPane();
    textPane.setText(getNewDocument());
    undoManager.discardAllEdits();
    textPane.getDocument().removeUndoableEditListener(undoHandler);
    textPane.getDocument().addUndoableEditListener(undoHandler);
    textPane.setCaretPosition(0);
    SwingUtilities.invokeLater(() ->
    {
      textPane.requestFocus();
    });
  }

  @Override
  public void open(InputStream is) throws Exception
  {
    StringBuilder buffer = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(
      new InputStreamReader(is, getCharset())))
    {
      String line = reader.readLine();
      while (line != null)
      {
        buffer.append(line);
        buffer.append("\n");
        line = reader.readLine();
      }
    }
    textEditor.getTextPane().setText(buffer.toString());
    undoManager.discardAllEdits();
    textEditor.getTextPane().getDocument().removeUndoableEditListener(undoHandler);
    textEditor.getTextPane().getDocument().addUndoableEditListener(undoHandler);
    textEditor.getTextPane().setCaretPosition(0);
  }

  @Override
  public void save(OutputStream os) throws Exception
  {
    String text = textEditor.getTextPane().getText();
    os.write(text.getBytes(getCharset()));
  }

  @Override
  public void close()
  {
    undoManager.discardAllEdits();
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
  public boolean isFindEnabled()
  {
    return true;
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
      MatrixIDE.log(ex);
    }
  }

  public class UndoHandler implements UndoableEditListener
  {
    @Override
    public void undoableEditHappened(UndoableEditEvent e)
    {
      undoManager.addEdit(e.getEdit());
    }
  }

  protected String getCategoryName()
  {
    return Palette.EMPTY;
  }

  @Override
  public void activate()
  {
    getMainPanel().getPalette().setSelectedCategory(getCategoryName());
    getMainPanel().setRightPanelVisible(true);

    getMainPanel().setEditObject(null);
    Font font = textEditor.getTextPane().getFont();
    if (!font.equals(Options.getEditorFont()))
    {
      textEditor.getTextPane().setFont(getEditorFont());
      textEditor.repaint();
    }
  }

  @Override
  public void print()
  {
    try
    {
      Printable printable = new Printable()
      {
        List<String> textLines;
        int[] pageBreaks;

        @Override
        public int print(Graphics g, PageFormat pageFormat, int pageIndex)
               throws PrinterException
        {
          Font font = getPrintFont();
          FontMetrics metrics = g.getFontMetrics(font);
          int lineHeight = metrics.getHeight();

          if (textLines == null)
          {
            textLines = getTextLines(pageFormat, metrics, g);
            int linesPerPage =
              (int)(pageFormat.getImageableHeight() / lineHeight);
            int numBreaks = (textLines.size() - 1) / linesPerPage;
            pageBreaks = new int[numBreaks];
            for (int b = 0; b < numBreaks; b++)
            {
              pageBreaks[b] = (b + 1) * linesPerPage;
            }
          }

          if (pageIndex > pageBreaks.length)
          {
            return NO_SUCH_PAGE;
          }

          Graphics2D g2d = (Graphics2D)g;
          g2d.setFont(font);
          g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

          int y = 0;
          int start = (pageIndex == 0) ? 0 : pageBreaks[pageIndex - 1];
          int end = (pageIndex == pageBreaks.length) ?
            textLines.size() : pageBreaks[pageIndex];
          for (int line = start; line < end; line++)
          {
            y += lineHeight;
            g.drawString(textLines.get(line), 0, y);
          }

          return PAGE_EXISTS;
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


  /* internal methods */

  protected abstract String getContentType();

  protected abstract EditorKit getEditorKit();

  protected abstract String getNewDocument();

  protected abstract String getCharset();

  protected Font getPrintFont()
  {
    return getEditorFont().deriveFont(10f);
  }

  protected List<String> getTextLines(PageFormat pageFormat,
    FontMetrics metrics, Graphics g)
  {
    ArrayList<String> lines = new ArrayList();
    String text = textEditor.getTextPane().getText();
    String lineArray[] = text.split("\n");
    for (String line : lineArray)
    {
      StringBuilder builder = new StringBuilder();
      StringTokenizer tokenizer =
        new StringTokenizer(line, getWordDelimiters(), true);
      if (tokenizer.countTokens() > 0)
      {
        builder.append(tokenizer.nextToken());
        while (tokenizer.hasMoreTokens())
        {
          String word = tokenizer.nextToken();
          Rectangle2D bounds =
            metrics.getStringBounds(builder.toString() + word, g);
          if (bounds.getWidth() < pageFormat.getImageableWidth())
          {
            builder.append(word);
          }
          else
          {
            lines.add(builder.toString());
            builder.setLength(0);
            builder.append(word);
          }
        }
      }
      lines.add(builder.toString());
    }
    return lines;
  }

  protected String getWordDelimiters()
  {
    return " \t.,:;-";
  }

  private void initComponents() throws Exception
  {
    this.setSize(new Dimension(471, 408));
    this.setLayout(borderLayout);
    textEditor.getTextPane().setFont(getEditorFont());
    this.add(textEditor, BorderLayout.CENTER);

    String contentType = getContentType();
    EditorKit editorKit = getEditorKit();

    textEditor.getTextPane().setEditorKitForContentType(
      contentType, editorKit);

    textEditor.getTextPane().setContentType(contentType);
    textEditor.getTextPane().setBackground(Color.WHITE);
    textEditor.getTextPane().setSelectionColor(Color.LIGHT_GRAY);
    textEditor.getTextPane().getDocument().addDocumentListener(new DocumentListener()
    {
      @Override
      public void insertUpdate(DocumentEvent e)
      {
        setModified(true);
      }

      @Override
      public void removeUpdate(DocumentEvent e)
      {
        setModified(true);
      }

      @Override
      public void changedUpdate(DocumentEvent e)
      {
        setModified(true);
      }
    });

    DropTarget dropTarget = new DropTarget(textEditor.getTextPane(),
      new DropTargetAdapter()
    {
      @Override
      public void drop(DropTargetDropEvent event)
      {
        try
        {
          Transferable transferable = event.getTransferable();
          String text =
            (String)transferable.getTransferData(DataFlavor.stringFlavor);
          event.dropComplete(true);
          JTextPane textPane = textEditor.getTextPane();
          textPane.getDocument().insertString(
            textPane.getCaretPosition(), text, null);
          textPane.setCursor(Cursor.getDefaultCursor());
        }
        catch (Exception ex)
        {
          MatrixIDE.log(ex);
        }
      }
    });
    dropTarget.setActive(true);
    textEditor.getTextPane().setDropTarget(dropTarget);

    textEditor.getTextPane().addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseEntered(MouseEvent event)
      {
        if (getMainPanel().getPalette().getSelectedElement() != null)
        {
          textEditor.getTextPane().setCursor(DragSource.DefaultCopyDrop);
        }
      }

      @Override
      public void mouseReleased(MouseEvent event)
      {
        if (getMainPanel().getPalette().getSelectedElement() != null)
        {
          JTextPane textPane = textEditor.getTextPane();
          Palette palette = getMainPanel().getPalette();
          String text = palette.getSelectedElementAttribute("text");
          if (text == null) text = palette.getSelectedElement();
          if (text != null)
          {
            int position = textPane.getCaretPosition();
            try
            {
              textPane.getDocument().insertString(position, text, null);
            }
            catch (Exception ex)
            {
            }
          }
          palette.clearSelectedElement();
          textPane.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        }
      }
    });
  }
}
