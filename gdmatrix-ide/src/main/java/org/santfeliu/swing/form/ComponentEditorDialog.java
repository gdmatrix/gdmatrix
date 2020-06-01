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
package org.santfeliu.swing.form;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


/**
 *
 * @author realor
 */
public class ComponentEditorDialog extends JDialog
{
  public static final int OK_OPTION = JOptionPane.OK_OPTION;
  public static final int CANCEL_OPTION = JOptionPane.CANCEL_OPTION;

  private int option;
  private ComponentView view;
  private ComponentEditor editor;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel southPanel = new JPanel();
  private JButton acceptButton = new JButton();
  private JButton cancelButton = new JButton();

  public ComponentEditorDialog(Frame parent)
  {
    super(parent, true);
    try
    {
      initComponents();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void initComponents()
    throws Exception
  {
    this.getContentPane().setLayout(borderLayout1);
    acceptButton.setText("Accept");
    acceptButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            acceptButton_actionPerformed(e);
          }
        });
    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            cancelButton_actionPerformed(e);
          }
        });
    southPanel.add(acceptButton, null);
    southPanel.add(cancelButton, null);
    this.getContentPane().add(southPanel, BorderLayout.SOUTH);
    this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    this.setSize(new Dimension(433, 413));
  }

  public int editView(Component parent, ComponentView view,
    ComponentEditor editor)
  {
    this.editor = editor;
    this.view = view;
    Component component = editor.getEditingComponent(view);
    this.getContentPane().add(component, BorderLayout.CENTER);
    this.pack();
    this.setLocationRelativeTo(parent);
    this.setTitle(view.getComponentType());
    this.setVisible(true);
    return option;
  }

  private void acceptButton_actionPerformed(ActionEvent e)
  {
    try
    {
      editor.stopEditing();
      dispose();
      option = OK_OPTION;
    }
    catch (Exception ex)
    {
      JOptionPane.showMessageDialog(this, ex.getMessage(),
        "ERROR", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void cancelButton_actionPerformed(ActionEvent e)
  {
    editor.cancelEditing();
    dispose();
    option = CANCEL_OPTION;
  }
}
