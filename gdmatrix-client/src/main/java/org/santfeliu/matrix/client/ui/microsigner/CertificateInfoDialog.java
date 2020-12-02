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
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author realor
 */
public class CertificateInfoDialog extends JDialog
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel southPanel = new JPanel();
  private JButton acceptButton = new JButton();
  private JScrollPane scrollPane = new JScrollPane();
  private JTable propertiesTable = new JTable();

  public CertificateInfoDialog()
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
  
  public CertificateInfoDialog(Frame owner)
  {
    super(owner);
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
    this.setTitle(MicroSigner.getLocalizedText("CertInfo"));
    this.setSize(new Dimension(560, 300));
    this.setModal(true);
    this.getContentPane().setLayout(borderLayout1);
    this.getContentPane().add(southPanel, BorderLayout.SOUTH);
    this.getContentPane().add(scrollPane, BorderLayout.CENTER);
    propertiesTable.setEnabled(true);
    scrollPane.getViewport().add(propertiesTable, null);
    acceptButton.setText(MicroSigner.getLocalizedText("Accept"));
    southPanel.add(acceptButton, null);

    acceptButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        acceptButton_actionPerformed(e);
      }
    });
  }

  public void setCertificate(X509Certificate certificate)
  {
    DefaultTableModel model = new DefaultTableModel();
    model.addColumn(MicroSigner.getLocalizedText("Property"));
    model.addColumn(MicroSigner.getLocalizedText("Value"));
    propertiesTable.setAutoCreateColumnsFromModel(false);    
    propertiesTable.setModel(model);
    propertiesTable.getTableHeader().setReorderingAllowed(false);
    propertiesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    JTextField textField = new JTextField();
    textField.setEditable(false);
    DefaultCellEditor editor = new DefaultCellEditor(textField);
    
    propertiesTable.addColumn(new TableColumn(0, 100, null, editor));
    propertiesTable.addColumn(new TableColumn(1, 300, null, editor));

    model.addRow(new Object[]{MicroSigner.getLocalizedText("Type"), 
      certificate.getType()});
    model.addRow(new Object[]{MicroSigner.getLocalizedText("Version"), 
      String.valueOf(certificate.getVersion())});
    model.addRow(new Object[]{MicroSigner.getLocalizedText("SubjectDN"), 
      certificate.getSubjectDN().getName()});    
    model.addRow(new Object[]{MicroSigner.getLocalizedText("IssuerDN"), 
      certificate.getIssuerDN().getName()});
    model.addRow(new Object[]{MicroSigner.getLocalizedText("SerialNumber"), 
      certificate.getSerialNumber().toString(16).toUpperCase()});
    model.addRow(new Object[]{MicroSigner.getLocalizedText("NotValidBefore"), 
      String.valueOf(certificate.getNotBefore())});
    model.addRow(new Object[]{MicroSigner.getLocalizedText("NotValidAfter"), 
      String.valueOf(certificate.getNotAfter())});
    model.addRow(new Object[]{MicroSigner.getLocalizedText("SignatureAlgorithm"), 
      String.valueOf(certificate.getSigAlgName())});
    try
    {
      int i = 1;
      Collection collection = certificate.getSubjectAlternativeNames();
      Iterator iter = collection.iterator();
      while (iter.hasNext())
      {
        Object o = iter.next();
        model.addRow(new Object[]{
          MicroSigner.getLocalizedText("AlternativeName") + i, o.toString()});
        i++;
      }
    }
    catch (Exception ex)
    {      
    }
  }

  private void acceptButton_actionPerformed(ActionEvent e)
  {
    this.setVisible(true);
    this.dispose();
  }  
}
