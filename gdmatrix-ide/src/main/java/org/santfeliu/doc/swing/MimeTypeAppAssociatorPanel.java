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
package org.santfeliu.doc.swing;

import java.awt.BorderLayout;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.santfeliu.doc.file.PropertiesMimeTypeAppAssociator;
import org.santfeliu.swing.Utilities;

/**
 *
 * @author unknown
 */
public class MimeTypeAppAssociatorPanel extends JPanel
{
  private JButton acceptButton = new JButton();
  private JButton cancelButton = new JButton();
  private JPanel buttonPanel = new JPanel();
  private BorderLayout borderLayout1 = new BorderLayout();
  private MimeTypesPanel mimeTypesPanel = new MimeTypesPanel();
  private PropertiesMimeTypeAppAssociator associator = new PropertiesMimeTypeAppAssociator();
  private JDialog dialog;
  private String[] mimeTypePanelColumns;

  public MimeTypeAppAssociatorPanel()
  {
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
    this.setLayout(borderLayout1);
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
    this.add(buttonPanel, BorderLayout.SOUTH);
    buttonPanel.add(acceptButton);
    buttonPanel.add(cancelButton, BorderLayout.SOUTH);
    this.add(mimeTypesPanel, BorderLayout.CENTER);
    if (mimeTypePanelColumns == null)
      mimeTypePanelColumns = new String[]{"MimeType", "Operation", "Application"};
    mimeTypesPanel.setMimeTypeColumnLabel(mimeTypePanelColumns[0]);
    mimeTypesPanel.setOperationColumnLabel(mimeTypePanelColumns[1]);
    mimeTypesPanel.setAppPathColumnLabel(mimeTypePanelColumns[2]);
    mimeTypesPanel.setOperationValueLabels("Open", "Edit");
    mimeTypesPanel.setProperties(associator.getProperties());
  }
  
  public void refresh()
    throws Exception
  {
    mimeTypesPanel.setProperties(associator.getProperties());
  }
  
  private void acceptButton_actionPerformed(ActionEvent e)
  {
    try
    {
      associator.setProperties(mimeTypesPanel.getProperties());
      dialog.dispose();
    }
    catch (Exception f)
    {
      JOptionPane.showMessageDialog(this, f.toString(), 
        "ERROR", JOptionPane.ERROR_MESSAGE);      
    }
  }
  
  private void cancelButton_actionPerformed(ActionEvent e)
  {
    dialog.dispose();
    try
    {
      refresh();
    }
    catch (Exception f)
    {
       JOptionPane.showMessageDialog(this, f.toString(), 
         "ERROR", JOptionPane.ERROR_MESSAGE); 
    }
  }  
  
  public void createDialog(String title, int width, int height, Component parent)
  {
    dialog = Utilities.createDialog(title, width, height, false, parent, this);
    Utilities.centerWindow(parent, dialog);
    dialog.setVisible(false);
  }
  
  public void showDialog()
  {
    dialog.setVisible(true);
  }
  
  public void setAcceptButtonText(String text)
  {
    acceptButton.setText(text);
  }
  
  public void setCancelButtonText(String text)
  {
    cancelButton.setText(text);
  }
  
  public void setMimeTypePanel(String[] columnNames, String openLabel, 
    String editLabel)
  {
    mimeTypePanelColumns = columnNames;
    mimeTypesPanel.setMimeTypeColumnLabel(mimeTypePanelColumns[0]);
    mimeTypesPanel.setOperationColumnLabel(mimeTypePanelColumns[1]);
    mimeTypesPanel.setAppPathColumnLabel(mimeTypePanelColumns[2]);
    mimeTypesPanel.setOperationValueLabels(openLabel, editLabel);
  }

}
