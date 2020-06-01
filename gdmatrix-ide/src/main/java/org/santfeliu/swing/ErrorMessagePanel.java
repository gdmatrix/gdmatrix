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
package org.santfeliu.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;


/**
 *
 * @author realor
 */
public class ErrorMessagePanel
  extends JPanel
{
  private static JDialog dialog;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JButton acceptButton = new JButton();
  private JPanel buttonPanel = new JPanel();
  private JScrollPane stackTraceScrollPanel = new JScrollPane();
  private JTextArea stackTraceTextArea = new JTextArea();
  private JLabel messageLabel = new JLabel();

  public ErrorMessagePanel()
  {
    try
    {
      initComponents();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public Dimension getPreferredSize()
  {
    Dimension size = messageLabel.getPreferredSize();
    if (size.width < 400) size.width = 400;
    else if (size.width > 800) size.width = 800;
    size.height = 300;
    return size;
  }

  private void initComponents()
    throws Exception
  {
    this.setLayout(borderLayout1);
    this.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    acceptButton.setText("Accept");
    acceptButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            acceptButton_actionPerformed(e);
          }
        });
    stackTraceScrollPanel.setBorder(BorderFactory.createTitledBorder("Details"));
    stackTraceTextArea.setFont(new Font("Courier New", 0, 12));
    messageLabel.setOpaque(false);
    messageLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    buttonPanel.add(acceptButton, null);
    stackTraceScrollPanel.getViewport().add(stackTraceTextArea, null);

    this.add(messageLabel, BorderLayout.NORTH);
    this.add(stackTraceScrollPanel, BorderLayout.CENTER);
    this.add(buttonPanel, BorderLayout.SOUTH);
  }

  private void setErrorMessage(Throwable e, Icon icon)
  {
    if (icon == null)
    {
      if (e instanceof Error)
        icon = UIManager.getIcon("OptionPane.errorIcon");
      else if (e instanceof Exception)
        icon = UIManager.getIcon("OptionPane.warningIcon");
      else
        icon = UIManager.getIcon("OptionPane.informationIcon");
    }
    messageLabel.setIcon(icon);
    messageLabel.setText(e.getMessage());

    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    stackTraceTextArea.setText(sw.toString());
    stackTraceTextArea.setCaretPosition(0);
  }

  public static void showErrorMessage(Window owner, Throwable e)
  {
    showErrorMessage(owner, null, null, e);
  }

  public static void showErrorMessage(Window owner, Dimension size, Throwable e)
  {
    showErrorMessage(owner, size, null, e);
  }

  public static void showErrorMessage(Window owner, Dimension size,
    Icon icon, Throwable e)
  {
    ErrorMessagePanel errorPanel = new ErrorMessagePanel();
    errorPanel.setErrorMessage(e, icon);

    if (size == null)
    {
      size = errorPanel.getPreferredSize();
      size.width += 20; // add margin
    }
    int width = size.width;
    int height = size.height;

    dialog = Utilities.createDialog("ERROR", width, height,
      true, owner, errorPanel);
    dialog.setVisible(true);
  }

  public static void main(String args[])
  {
    try
    {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      JFrame frame = new JFrame("Error test");
      frame.setSize(900, 500);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setVisible(true);
      try
      {
        throw new Exception("Exception");
      }
      catch (Exception e)
      {
        ErrorMessagePanel.showErrorMessage(frame, null, e);
      }
    }
    catch (Exception ex)
    {
    }
  }

  private void acceptButton_actionPerformed(ActionEvent e)
  {
    dialog.dispose();
  }
}
