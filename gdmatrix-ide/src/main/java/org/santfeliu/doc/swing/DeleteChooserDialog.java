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
import java.awt.Dimension;
import java.awt.Frame;

import java.awt.Rectangle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Locale;
import java.util.MissingResourceException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import javax.swing.JRadioButton;

import java.util.ResourceBundle;

import org.santfeliu.swing.layout.VerticalFlowLayout;


/**
 *
 * @author unknown
 */
public class DeleteChooserDialog
  extends JDialog
{
  private JPanel mainPanel = new JPanel();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel optionsPanel = new JPanel();
  private JPanel buttonsPanel = new JPanel();
  private JButton confirmButton = new JButton();
  private JButton cancelButton = new JButton();
  private VerticalFlowLayout verticalFlowLayout1 = 
    new VerticalFlowLayout();
  private JRadioButton logicalDeleteRadio = new JRadioButton();
  private JRadioButton currentVersionDeleteRadio = new JRadioButton();
  private JRadioButton purgeRadio = new JRadioButton();
  private JRadioButton allVersionsDeleteRadio = new JRadioButton();
  private String selectedOption = null;
  private ButtonGroup radioGroup = new ButtonGroup();
  
  public static String LOGICAL = "LOGICAL";
  public static String CURRENT_VERSION = "CURRENT_VERSION";
  public static String PURGE = "PURGE";
  public static String ALL_VERSIONS = "ALL_VERSIONS";

  private ResourceBundle resourceBundle = 
    loadResourceBundle(Locale.getDefault());

  public DeleteChooserDialog()
  {
    this(null, "", true);
  }

  public DeleteChooserDialog(Frame parent, String title, boolean modal)
  {
    super(parent, title, modal);
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
    this.setSize( new Dimension( 200, 200 ) );
    this.getContentPane().setLayout( null );
    this.setTitle(getLocalizedText("delete"));
    mainPanel.setBounds(new Rectangle(0, 0, 195, 165));
    mainPanel.setLayout(borderLayout1);
    optionsPanel.setLayout(verticalFlowLayout1);
    confirmButton.setText(getLocalizedText("deleteChooserOk"));
    confirmButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            confirmButton_actionPerformed(e);
          }
        });
    cancelButton.setText(getLocalizedText("deleteChooserCancel"));
    cancelButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            cancelButton_actionPerformed(e);
          }
        });
    logicalDeleteRadio.setText(getLocalizedText("deleteChooserLogicalDelete"));
    logicalDeleteRadio.getModel().setMnemonic(1);
    currentVersionDeleteRadio.setText(getLocalizedText("deleteChooserCurrentVersion"));
    currentVersionDeleteRadio.getModel().setMnemonic(2);
    purgeRadio.setText(getLocalizedText("deleteChooserPurge"));
    purgeRadio.getModel().setMnemonic(3);
    allVersionsDeleteRadio.setText(getLocalizedText("deleteChooserAllVersions"));
    allVersionsDeleteRadio.getModel().setMnemonic(4);
    optionsPanel.add(logicalDeleteRadio, null);
    optionsPanel.add(currentVersionDeleteRadio, null);
    optionsPanel.add(purgeRadio, null);
    optionsPanel.add(allVersionsDeleteRadio, null);
    mainPanel.add(optionsPanel, BorderLayout.CENTER);
    buttonsPanel.add(confirmButton, null);
    buttonsPanel.add(cancelButton, null);
    mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
    this.getContentPane().add(mainPanel, null);
    radioGroup.add(logicalDeleteRadio);
    radioGroup.add(currentVersionDeleteRadio);
    radioGroup.add(purgeRadio);
    radioGroup.add(allVersionsDeleteRadio);
    radioGroup.setSelected(logicalDeleteRadio.getModel(), true);
  }

  private void confirmButton_actionPerformed(ActionEvent e)
  {
    try 
    {
      int option = radioGroup.getSelection().getMnemonic();
      if (option == 1) 
      {
        selectedOption = this.LOGICAL;
      }
      else if (option == 2) 
      {
        selectedOption = this.CURRENT_VERSION;      
      }
      else if (option == 3) 
      {
        selectedOption = this.PURGE;      
      }
      else if (option == 4) 
      {
        selectedOption = this.ALL_VERSIONS;      
      }
      else
      {
        selectedOption = null;
        dispose();
        throw new Exception(getLocalizedText("deleteChooserNoOptionSelected"));
      }
      dispose();
    }
    catch (Exception ex) 
    {
      ex.printStackTrace();
    }  
  }

  private void cancelButton_actionPerformed(ActionEvent e)
  {
    selectedOption = null;
    dispose();
  }

  public void setSelectedOption(String selectedOption)
  {
    this.selectedOption = selectedOption;
  }

  public String getSelectedOption()
  {
    return selectedOption;
  }
  
  protected ResourceBundle loadResourceBundle(Locale locale) 
  {    
    return ResourceBundle.getBundle("org.santfeliu.doc.swing.resources.DocumentPanelBundle", locale);
  }
  
  public String getLocalizedText(String text) 
  {
    String result = null;
    try
    {
      result = resourceBundle.getString(text);
    }
    catch (MissingResourceException ex)
    {
      result = "{" + text + "}";
    }
    return result;
  }

}
