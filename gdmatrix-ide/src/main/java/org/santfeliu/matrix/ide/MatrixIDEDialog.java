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
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import org.santfeliu.matrix.MatrixInfo;

/**
 *
 * @author realor
 */
public class MatrixIDEDialog extends JDialog
{
  private static final Color BACKGROUND_COLOR = Color.WHITE;

  private JPanel northPanel = new JPanel();
  private JLabel matrixLabel = new JLabel();
  private JLabel matrixVersionLabel = new JLabel();
  private JTabbedPane tabbedPane = new JTabbedPane();
  private JPanel southPanel = new JPanel();
  private JButton acceptButton = new JButton();

  private JPanel aboutPanel = new JPanel();
  private JPanel propertiesPanel = new JPanel();
  private JPanel versionPanel = new JPanel();
  private JPanel creditsPanel = new JPanel();
  private JPanel licensePanel = new JPanel();

  private JTable propertiesTable = new JTable();
  private JTable versionTable = new JTable();
  private JTable teamTable = new JTable();

  private JScrollPane scrollPane1 = new JScrollPane();
  private JScrollPane scrollPane2 = new JScrollPane();
  private JScrollPane scrollPane3 = new JScrollPane();
  private JScrollPane scrollPane4 = new JScrollPane();

  private BorderLayout borderLayout1 = new BorderLayout();
  private BorderLayout borderLayout2 = new BorderLayout();
  private BorderLayout borderLayout3 = new BorderLayout();
  private BorderLayout borderLayout4 = new BorderLayout();
  private BorderLayout borderLayout5 = new BorderLayout();
  private BorderLayout borderLayout6 = new BorderLayout();
  private BorderLayout borderLayout7 = new BorderLayout();

  private JTextArea licenseTextArea = new JTextArea();
  private JLabel teamLabel = new JLabel();
  private JLabel imageLabel = new JLabel();

  public MatrixIDEDialog()
  {
    this(null, true);
  }

  public MatrixIDEDialog(Frame parent, boolean modal)
  {
    super(parent, modal);
    try
    {
      initComponents();
    }
    catch(Exception ex)
    {
      MatrixIDE.log(ex);
    }
  }

  private void initComponents()
    throws Exception
  {
    this.setTitle("About");
    this.setSize(new Dimension(520, 350));
    this.getContentPane().setLayout(borderLayout1);
    matrixLabel.setOpaque(false);
    matrixLabel.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/images/gdmatrix/ide_banner.png")));

    matrixVersionLabel.setText(MatrixInfo.getFullVersion());
    
    imageLabel.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/images/MatrixIDE.png")));

    northPanel.setLayout(borderLayout7);
    northPanel.setBackground(BACKGROUND_COLOR);
    northPanel.add(matrixLabel, BorderLayout.WEST);
    northPanel.add(matrixVersionLabel, BorderLayout.CENTER);
    
    tabbedPane.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
    acceptButton.setText("Accept");
    acceptButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            acceptButton_actionPerformed(e);
          }
        });
    aboutPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    aboutPanel.setLayout(borderLayout5);

    propertiesPanel.setLayout(borderLayout2);
    propertiesPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    scrollPane1.getViewport().add(propertiesTable, null);
    propertiesPanel.add(scrollPane1, BorderLayout.CENTER);

    versionPanel.setLayout(borderLayout6);
    versionPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    scrollPane4.getViewport().add(versionTable, null);
    versionPanel.add(scrollPane4, BorderLayout.CENTER);

    creditsPanel.setLayout(borderLayout3);
    creditsPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    scrollPane2.getViewport().add(teamTable, null);
    creditsPanel.add(scrollPane2, BorderLayout.CENTER);
    creditsPanel.add(teamLabel, BorderLayout.NORTH);
    teamLabel.setText("The matrix team:");

    licensePanel.setLayout(borderLayout4);
    licensePanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    licenseTextArea.setEditable(false);
    scrollPane3.getViewport().add(licenseTextArea, null);
    licensePanel.add(scrollPane3, BorderLayout.CENTER);

    imageLabel.setBackground(Color.white);
    imageLabel.setHorizontalAlignment(JLabel.CENTER);
    imageLabel.setOpaque(true);    
    
    this.getContentPane().add(northPanel, BorderLayout.NORTH);

    aboutPanel.add(imageLabel, BorderLayout.CENTER);

    tabbedPane.addTab("About", aboutPanel);
    tabbedPane.addTab("Version", versionPanel);
    tabbedPane.addTab("Credits", creditsPanel);
    tabbedPane.addTab("License", licensePanel);
    tabbedPane.addTab("Properties", propertiesPanel);

    this.getContentPane().add(tabbedPane, BorderLayout.CENTER);
    southPanel.add(acceptButton, null);
    this.getContentPane().add(southPanel, BorderLayout.SOUTH);

    loadTable(propertiesTable, System.getProperties());
    loadTable(versionTable, MatrixInfo.getProperties());

    DefaultTableModel teamModel = new DefaultTableModel() {
      @Override
      public boolean isCellEditable(int row, int column)
      {
        return false;
      }
    };
    teamModel.addColumn("Name");
    teamModel.addColumn("Email");
    teamTable.setModel(teamModel);
    for (String[] member : MatrixInfo.getTeam())
    {
      teamModel.addRow(member);
    }
    licenseTextArea.setText(MatrixInfo.getLicense());
    licenseTextArea.setFont(new Font("Dialog", 0, 12));
    licenseTextArea.setCaretPosition(0);
  }

  private void acceptButton_actionPerformed(ActionEvent e)
  {
    setVisible(false);
    dispose();
  }

  private void loadTable(JTable table, Properties properties)
  {
    DefaultTableModel propertiesModel = new DefaultTableModel() {
      @Override
      public boolean isCellEditable(int row, int column)
      {
        return false;
      }
    };
    propertiesModel.addColumn("Property");
    propertiesModel.addColumn("Value");
    table.setModel(propertiesModel);
    table.getColumnModel().getColumn(0).setPreferredWidth(100);
    table.getColumnModel().getColumn(1).setPreferredWidth(200);
    Enumeration enu = properties.propertyNames();
    List propertyNames = new ArrayList();
    while (enu.hasMoreElements())
    {
      String propertyName = (String)enu.nextElement();
      propertyNames.add(propertyName);
    }
    String propArray[] = new String[propertyNames.size()];
    propertyNames.toArray(propArray);
    Arrays.sort(propArray);
    for (int i = 0; i < propArray.length; i++)
    {
      String value = properties.getProperty(propArray[i]);
      propertiesModel.addRow(new Object[]{propArray[i], value});
    }
  }
}
