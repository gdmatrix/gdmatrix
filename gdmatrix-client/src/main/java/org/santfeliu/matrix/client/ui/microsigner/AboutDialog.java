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
package org.santfeliu.matrix.client.ui.microsigner;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;


/**
 *
 * @author realor
 */
public class AboutDialog extends JDialog
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private BorderLayout borderLayout2 = new BorderLayout();
  private JPanel centerPanel = new JPanel();
  private JLabel infoLabel = new JLabel();
  private JScrollPane scrollPane = new JScrollPane();
  private JTable propertiesTable = new JTable();
  private JPanel southPanel = new JPanel();
  private JButton acceptButton = new JButton();

  public AboutDialog(Frame owner)
  {
    super(owner, true);
    try
    {
      initComponents();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void initComponents() throws Exception
  {
    this.setTitle(MicroSigner.TITLE);
    this.setSize(new Dimension(451, 292));
    this.getContentPane().setLayout(borderLayout1);
    this.setResizable(true);
    this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    infoLabel.setText(MicroSigner.TITLE + " v" + 
      MicroSigner.VERSION + " / " + MicroSigner.CREDITS);
    infoLabel.setPreferredSize(new Dimension(428, 32));
    this.getContentPane().add(centerPanel, BorderLayout.CENTER);

    southPanel.add(acceptButton, null);
    this.getContentPane().add(southPanel, BorderLayout.SOUTH);
    centerPanel.setLayout(borderLayout2);
    centerPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    centerPanel.add(infoLabel, BorderLayout.NORTH);
    scrollPane.getViewport().add(propertiesTable, null);
    centerPanel.add(scrollPane, BorderLayout.CENTER);
    DefaultTableModel model = new DefaultTableModel();
    model.addColumn(MicroSigner.getLocalizedText("Property"));
    model.addColumn(MicroSigner.getLocalizedText("Value"));

    propertiesTable.setModel(model);
    propertiesTable.setEnabled(false);
    acceptButton.setText(MicroSigner.getLocalizedText("Accept"));
    acceptButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            acceptButton_actionPerformed(e);
          }
        });
    Properties properties = System.getProperties();
    Enumeration enu = properties.propertyNames();
    Vector propertyNames = new Vector();
    while (enu.hasMoreElements())
    {
      String propertyName = (String)enu.nextElement();
      propertyNames.addElement(propertyName);
    }
    String propArray[] = new String[propertyNames.size()];
    propertyNames.toArray(propArray);
    Arrays.sort(propArray);
    for (int i = 0; i < propArray.length; i++)
    {
      String value = properties.getProperty(propArray[i]);
      model.addRow(new Object[]{propArray[i], value});
    }
    URL url = getClass().getClassLoader().getResource(
      "org/santfeliu/matrix/client/ui/microsigner/resources/images/signature.gif");
    ImageIcon signatureIcon = new ImageIcon(url);
    infoLabel.setIcon(signatureIcon);
  }

  private void acceptButton_actionPerformed(ActionEvent e)
  {
    this.setVisible(false);
    this.dispose();
  }
}
