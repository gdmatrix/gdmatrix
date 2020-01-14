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
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Calendar;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;


/**
 *
 * @author unknown
 */
public class CommitDialog
  extends JDialog
{
  private BorderLayout borderLayout = new BorderLayout();
  private JPanel centerPanel = new JPanel();
  private JPanel southPanel = new JPanel();
  private String selectedOption = null;
  private ButtonGroup buttonGroup = new ButtonGroup();
  private JTextField txtCommitTime = new JTextField();
  private JRadioButton radioButton1 = new JRadioButton();
  private JRadioButton radioButton2 = new JRadioButton();
  private JRadioButton radioButton3 = new JRadioButton();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JButton cancelButton = new JButton();
  private JButton acceptButton = new JButton();

  public static String COMMIT_NO_HISTORY = "COMMIT_NOHIST";
  public static String COMMIT_NOW = "COMMIT_NOW";
  public static String COMMIT_AT_TIME = "COMMIT_TIME";

  private ResourceBundle resourceBundle = 
    loadResourceBundle(Locale.getDefault());   

  public CommitDialog()
  {
    this(null, "Commit", true);
  }

  public CommitDialog(Frame parent, String title, boolean modal)
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
    this.setSize(new Dimension(300, 225));
    this.getContentPane().setLayout(gridBagLayout1);
    this.getContentPane().setLayout(borderLayout);
    this.getContentPane().add(centerPanel, BorderLayout.CENTER);
    this.getContentPane().add(southPanel, BorderLayout.SOUTH);
    centerPanel.setLayout(gridBagLayout1);

    centerPanel.setBorder(BorderFactory.createTitledBorder(""));
    centerPanel.add(txtCommitTime, 
                    new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                                     new Insets(0, 4, 0, 
                                                                4), 0, 0));
    centerPanel.add(radioButton3, 
                    new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                                           new Insets(2, 4, 2, 0), 0, 0));
    centerPanel.add(radioButton2, 
                    new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
                                           new Insets(2, 4, 2, 0), 0, 0));
    centerPanel.add(radioButton1, 
                    new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
                                                     new Insets(2, 4, 2, 
                                                                0), 0, 0));
    southPanel.add(acceptButton, null);
    southPanel.add(cancelButton, null);

    txtCommitTime.setMinimumSize(new Dimension(100, 19));
    txtCommitTime.setMaximumSize(new Dimension(2147483647, 19));
    txtCommitTime.setPreferredSize(new Dimension(150, 19));
    radioButton3.setText(getLocalizedText("atTime") + ":");
    radioButton3.setMnemonic(3);
    radioButton2.setText(getLocalizedText("now"));
    radioButton2.setMnemonic(2);
    radioButton1.setText(getLocalizedText("withoutHistory"));
    radioButton1.setMnemonic(1);
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
    buttonGroup.add(radioButton1);
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
        selectedOption = COMMIT_NO_HISTORY;
      }
      else if (option == 2) 
      {
        selectedOption = COMMIT_NOW;      
      }
      else if (option == 3) 
      {
        selectedOption = COMMIT_AT_TIME;      
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
  
/*
  public static void main(String[] args) 
  {
    CommitDialog cd = new CommitDialog();
    cd.setVisible(true);
  }
*/

  public void setSelectedOption(String selectedOption)
  {
    this.selectedOption = selectedOption;
  }

  public String getSelectedOption()
  {
    return selectedOption;
  }
  
  public Calendar getSelectedTime() throws ParseException
  {
    SimpleDateFormat formatDay = new SimpleDateFormat("dd/MM/yyyy");
    Calendar cal = Calendar.getInstance();
    cal.setTime(formatDay.parse(txtCommitTime.getText()));
    return cal;
  }
  
  private ResourceBundle loadResourceBundle(Locale locale) 
  {    
    return ResourceBundle.getBundle("org.santfeliu.swing.resources.CommitDialogBundle", locale);
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
