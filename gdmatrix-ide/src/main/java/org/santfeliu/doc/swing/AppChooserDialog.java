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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;


/**
 *
 * @author unknown
 */
public class AppChooserDialog
  extends JDialog
{
  private BorderLayout borderLayout = new BorderLayout();
  private JPanel centerPanel = new JPanel();
  private JPanel southPanel = new JPanel();
  private String selectedOption = null;
  private ButtonGroup buttonGroup = new ButtonGroup();
  private JRadioButton radioButton2 = new JRadioButton();
  private JRadioButton radioButton3 = new JRadioButton();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JButton cancelButton = new JButton();
  private JButton acceptButton = new JButton();
  private JButton exploreButton = new JButton();
  
  public static String DEFAULT = "DEFAULT";
  public static String NEW = "NEW";
  
  private String appPath;
  //private boolean defaultOption = false;
  private static File exploreDir;
  private JCheckBox checkDefault = new JCheckBox();

  //private DocumentBasePanel documentBasePanel;
  private ResourceBundle resourceBundle = 
    loadResourceBundle(Locale.getDefault());


  public AppChooserDialog()
  {
    this(null, null, true);
  }
  
  public AppChooserDialog(Frame parent, String title, boolean modal) 
  {
    super(parent, title, modal);
    if (title == null) setTitle(getLocalizedText("selectApplication"));
    try
    {
      //this.documentBasePanel = documentBasePanel;
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

/*
  public AppChooserDialog(Frame parent, String title, boolean modal)
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
*/

  private void jbInit()
    throws Exception
  {
    this.setSize(new Dimension(254, 168));
    this.getContentPane().setLayout(gridBagLayout1);
    this.getContentPane().setLayout(borderLayout);
    this.getContentPane().add(centerPanel, BorderLayout.CENTER);
    this.getContentPane().add(southPanel, BorderLayout.SOUTH);
    centerPanel.setLayout(gridBagLayout1);

    centerPanel.setBorder(BorderFactory.createTitledBorder(""));
    centerPanel.add(radioButton3, 
                    new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                                           new Insets(10, 4, 2, 19), 0, 
                                           0));
    centerPanel.add(radioButton2, 
                    new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                                           new Insets(0, 4, 2, 0), 0, 0));
    centerPanel.add(exploreButton, 
                    new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                           new Insets(10, 0, 0, 0), 0, 0));
    centerPanel.add(checkDefault, 
                    new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                           new Insets(10, 4, 0, 0), 0, 0));
    southPanel.add(acceptButton, null);
    southPanel.add(cancelButton, null);

    radioButton3.setText(getLocalizedText("selectApplication"));
    radioButton3.setMnemonic('\u0002');
    radioButton3.setActionCommand(getLocalizedText("atTime"));
    radioButton2.setText(getLocalizedText("defaultApplication"));
    radioButton2.setMnemonic('\u0001');
    cancelButton.setText(getLocalizedText("cancel"));
    cancelButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            cancelButton_actionPerformed(e);
          }
        });
    acceptButton.setText(getLocalizedText("accept"));
    acceptButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            acceptButton_actionPerformed(e);
          }
        });
    exploreButton.setText(getLocalizedText("explore"));
    exploreButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            exploreButton_actionPerformed(e);
          }
        });
    checkDefault.setText(getLocalizedText("makeOptionDefault"));
    checkDefault.setSelected(true);
    buttonGroup.add(radioButton2);
    buttonGroup.add(radioButton3);
    buttonGroup.setSelected(radioButton2.getModel(), true);
  }

  private void acceptButton_actionPerformed(ActionEvent e)
  {
    try 
    {
      int option = buttonGroup.getSelection().getMnemonic();
      if (option == 1) 
      {
        selectedOption = DEFAULT;
      }
      else if (option == 2) 
      {
        selectedOption = NEW;      
      }
      else
      {
        selectedOption = null;
        dispose();
        throw new Exception(getLocalizedText("noOptionSelected"));
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
  
  private void exploreButton_actionPerformed(ActionEvent e)
  {
    radioButton3.setSelected(true);
    
    JFileChooser dialog = new JFileChooser();
    if (exploreDir != null)
    {
      dialog.setCurrentDirectory(exploreDir);
    }
    
    int result = dialog.showDialog(this, getLocalizedText("open"));
    if (result == JFileChooser.APPROVE_OPTION)
    {
      File file = dialog.getSelectedFile();
      if (file != null)
      {
        appPath = file.getAbsolutePath();
        exploreDir = file.getParentFile();
      }
    }  
  }  
  
  public void setExploreOnlyMode()
  {
    radioButton2.setVisible(false);
    radioButton3.setSelected(true);
  }

  public void setSelectedOption(String selectedOption)
  {
    this.selectedOption = selectedOption;
  }

  public String getSelectedOption()
  {
    return selectedOption;
  }

  public void setAppPath(String appPath)
  {
    this.appPath = appPath;
    radioButton2.setText(getLocalizedText("run") + " " + appPath);
  }

  public String getAppPath()
  {
    return appPath;
  }

  public boolean isDefaultChecked()
  {
    return checkDefault.isSelected();
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
