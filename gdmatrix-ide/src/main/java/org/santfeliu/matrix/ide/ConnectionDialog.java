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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JToolBar;

/**
 *
 * @author realor
 */
public class ConnectionDialog extends JDialog
{
  private DefaultComboBoxModel model = new DefaultComboBoxModel();

  private BorderLayout borderLayout1 = new BorderLayout();
  private BorderLayout borderLayout2 = new BorderLayout();
  private BorderLayout borderLayout3 = new BorderLayout();
  private BorderLayout borderLayout4 = new BorderLayout();
  private GridBagLayout gridBagLayout = new GridBagLayout();
  private JPanel mainPanel = new JPanel();
  private JPanel southPanel = new JPanel();
  private JPanel centerPanel = new JPanel();
  private JPanel comboPanel = new JPanel();
  private JButton acceptButton = new JButton();
  private JButton cancelButton = new JButton();
  private JLabel urlLabel = new JLabel();
  private JToolBar toolBar = new JToolBar();
  private JTextField urlTextField = new JTextField();
  private JLabel usernameLabel = new JLabel();
  private JTextField usernameTextField = new JTextField();
  private JLabel passwordLabel = new JLabel();
  private JPasswordField passwordTextField = new JPasswordField();
  private JPanel northPanel = new JPanel();
  private JComboBox connComboBox = new JComboBox();
  private JButton addButton = new JButton();
  private JButton removeButton = new JButton();
  private int option = JOptionPane.CANCEL_OPTION;
  private JTextField nameTextField = new JTextField();
  private JLabel nameLabel = new JLabel();
  private JLabel connLabel = new JLabel();
  private static int counter = 0;

  public ConnectionDialog(Frame owner)
  {
    super(owner, true);
    try
    {
      initComponents();
    }
    catch (Exception ex)
    {
      MatrixIDE.log(ex);
    }
  }

  public int showDialog()
  {
    pack();
    setLocationRelativeTo(getParent());
    setVisible(true);
    return option;
  }

  public void setConnections(List connections)
  {
    model.removeAllElements();
    if (connections.isEmpty()) counter = 0;
    for (int i = 0; i < connections.size(); i++)
    {
      ConnectionParameters connParams =
        (ConnectionParameters)connections.get(i);
      ConnectionParameters connParams2 = new ConnectionParameters();
      connParams2.setName(connParams.getName());
      connParams2.setURL(connParams.getURL());
      connParams2.setUsername(connParams.getUsername());
      connParams2.setPassword(connParams.getPassword());
      model.addElement(connParams2);
    }
  }

  public List getConnections()
  {
    List list = new ArrayList();
    for (int i = 0; i < model.getSize(); i++)
    {
      list.add(model.getElementAt(i));
    }
    return list;
  }

  private void initComponents() throws Exception
  {
    this.getContentPane().setLayout(borderLayout1);
    this.setTitle("Connections");
    this.setModal(true);
    northPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    centerPanel.setLayout(gridBagLayout);
    centerPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    acceptButton.setText("Accept");
    acceptButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        acceptButton_actionPerformed(e);
      }
    });
    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        cancelButton_actionPerformed(e);
      }
    });
    urlLabel.setText("URL:");
    usernameLabel.setText("Username:");
    passwordLabel.setText("Password:");
    connComboBox.setModel(model);
    addButton.setToolTipText("Add connection");
    addButton.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/images/add.gif")));
    addButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        newButton_actionPerformed(e);
      }
    });
    addButton.setMargin(new Insets(2, 2, 2, 2));
    removeButton.setToolTipText("Remove connection");
    removeButton.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/images/remove.gif")));
    removeButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        removeButton_actionPerformed(e);
      }
    });
    removeButton.setMargin(new Insets(2, 2, 2, 2));
    nameLabel.setText("Name:");
    connLabel.setText("Connection:");
    southPanel.add(acceptButton, null);
    southPanel.add(cancelButton, null);

    Dimension dim = nameTextField.getPreferredSize();
    dim.width = 5 * dim.height;
    nameTextField.setPreferredSize(dim);
    nameTextField.setMinimumSize(dim);
    usernameTextField.setPreferredSize(dim);
    usernameTextField.setMinimumSize(dim);
    passwordTextField.setPreferredSize(dim);
    passwordTextField.setMinimumSize(dim);
    dim = urlTextField.getPreferredSize();
    dim.width = 12 * dim.height;
    urlTextField.setPreferredSize(dim);

    centerPanel.add(nameLabel,
      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 3, 2, 3), 0, 0));
    centerPanel.add(nameTextField,
      new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(2, 6, 2, 3), 0, 0));

    centerPanel.add(urlLabel,
      new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 3, 2, 3), 0, 0));
    centerPanel.add(urlTextField,
      new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
        new Insets(2, 6, 2, 3), 0, 0));

    centerPanel.add(usernameLabel,
      new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 3, 2, 3), 0, 0));
    centerPanel.add(usernameTextField,
      new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(2, 6, 2, 3), 0, 0));

    centerPanel.add(passwordLabel,
      new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 3, 2, 3), 0, 0));
    centerPanel.add(passwordTextField,
      new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(2, 6, 2, 3), 0, 0));

    northPanel.setLayout(borderLayout2);
    comboPanel.setLayout(borderLayout3);
    comboPanel.add(connComboBox, BorderLayout.CENTER);
    comboPanel.setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 4));

    northPanel.add(connLabel, BorderLayout.WEST);
    northPanel.add(comboPanel, BorderLayout.CENTER);
    northPanel.add(toolBar, BorderLayout.EAST);
    toolBar.add(addButton, null);
    toolBar.add(removeButton, null);
    toolBar.setRollover(true);
    toolBar.setFloatable(false);

    this.getContentPane().add(mainPanel, BorderLayout.CENTER);
    mainPanel.setLayout(borderLayout4);
    mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

    mainPanel.add(northPanel, BorderLayout.NORTH);
    mainPanel.add(centerPanel, BorderLayout.CENTER);
    mainPanel.add(southPanel, BorderLayout.SOUTH);

    showConnectionParameters(null);
    connComboBox.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        showConnectionParameters(
          (ConnectionParameters) model.getSelectedItem());
      }
    });
    this.nameTextField.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyReleased(KeyEvent e)
      {
        ConnectionParameters connParams
          = (ConnectionParameters) model.getSelectedItem();
        connParams.setName(nameTextField.getText());
        connComboBox.repaint();
      }
    });
    this.urlTextField.addFocusListener(new FocusAdapter()
    {
      @Override
      public void focusLost(FocusEvent e)
      {
        ConnectionParameters connParams
          = (ConnectionParameters) model.getSelectedItem();
        connParams.setURL(urlTextField.getText());
      }
    });
    this.usernameTextField.addFocusListener(new FocusAdapter()
    {
      @Override
      public void focusLost(FocusEvent e)
      {
        ConnectionParameters connParams
          = (ConnectionParameters) model.getSelectedItem();
        connParams.setUsername(usernameTextField.getText());
      }
    });
    this.passwordTextField.addFocusListener(new FocusAdapter()
    {
      @Override
      public void focusLost(FocusEvent e)
      {
        ConnectionParameters connParams
          = (ConnectionParameters) model.getSelectedItem();
        connParams.setPassword(new String(passwordTextField.getPassword()));
      }
    });
  }

  private void showConnectionParameters(ConnectionParameters parameters)
  {
    if (parameters == null)
    {
      nameTextField.setText(null);
      urlTextField.setText(null);
      usernameTextField.setText(null);
      passwordTextField.setText(null);
      nameTextField.setEditable(false);
      urlTextField.setEditable(false);
      usernameTextField.setEditable(false);
      passwordTextField.setEditable(false);
    }
    else
    {
      nameTextField.setEditable(true);
      urlTextField.setEditable(true);
      usernameTextField.setEditable(true);
      passwordTextField.setEditable(true);
      nameTextField.setText(parameters.getName());
      urlTextField.setText(parameters.getURL());
      urlTextField.setCaretPosition(0);
      usernameTextField.setText(parameters.getUsername());
      passwordTextField.setText(parameters.getPassword());
    }
  }

  private void newButton_actionPerformed(ActionEvent e)
  {
    ConnectionParameters connParams = new ConnectionParameters();
    String name = "new";
    counter++;
    if (counter > 1) name += "-" + counter;
    connParams.setName(name);
    connParams.setURL("http://<server>:<port>/wsdirectory");
    model.addElement(connParams);
    connComboBox.setSelectedItem(connParams);
  }

  private void removeButton_actionPerformed(ActionEvent e)
  {
    int index = connComboBox.getSelectedIndex();
    if (index >= 0)
    {
      ConnectionParameters connParams =
        (ConnectionParameters)model.getSelectedItem();
      int result = JOptionPane.showConfirmDialog(this,
        "Delete connection " + connParams.getName() + "?",
        "Delete connection", JOptionPane.YES_NO_OPTION);
      if (result == JOptionPane.YES_OPTION)
      {
        model.removeElement(connParams);
      }
    }
  }

  private void acceptButton_actionPerformed(ActionEvent e)
  {
    this.option = JOptionPane.OK_OPTION;
    this.setVisible(false);
    this.dispose();
  }

  private void cancelButton_actionPerformed(ActionEvent e)
  {
    this.option = JOptionPane.CANCEL_OPTION;
    this.setVisible(false);
    this.dispose();
  }
}
