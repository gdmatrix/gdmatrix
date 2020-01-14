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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.santfeliu.util.Properties;

/**
 *
 * @author unknown
 */
public class LoginDialog extends JDialog
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel southPanel = new JPanel();
  private JButton okButton = new JButton();
  private JButton cancelButton = new JButton();
  private JPanel centerPanel = new JPanel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JTextField usernameTextField = new JTextField();
  private JLabel usernameLabel = new JLabel();
  private JLabel passwordLabel = new JLabel();
  private JLabel serviceURLLabel = new JLabel();
  private JPasswordField passwordField = new JPasswordField();
  private JComboBox serviceURLComboBox = new JComboBox();

  public static int OK = 1;
  public static int CANCEL = 0;
  
  private int selectedOption = CANCEL;

  private ResourceBundle resourceBundle = 
    loadResourceBundle(Locale.getDefault());   

  public LoginDialog()
  {
    this(null, "", false);
  }
  
  public LoginDialog(String username)
  {
    this(null, "", false);
    this.usernameTextField.setText(username);
  }

  public LoginDialog(String[] urls)
  {
    this();
    this.setServiceURL(urls);
  }
  
  public LoginDialog(String username, String[] urls)
  {
    this(username);
    this.setServiceURL(urls);
  }

  public LoginDialog(Frame parent, String title, boolean modal)
  {
    super(parent, title, modal);
    try
    {
      jbInit();
      loadServiceURLFile();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
    this.setSize(new Dimension(338, 225));
    this.getContentPane().setLayout(borderLayout1);
    this.setTitle(getLocalizedText("login"));
    this.setModal(true);
    this.setAlwaysOnTop(true);
    okButton.setText(getLocalizedText("ok"));
    okButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            okButton_actionPerformed(e);
          }
        });
    cancelButton.setText(getLocalizedText("cancel"));
    cancelButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            cancelButton_actionPerformed(e);
          }
        });
    centerPanel.setLayout(gridBagLayout1);
    usernameLabel.setText(getLocalizedText("username") + ":");
    passwordLabel.setText(getLocalizedText("password") + ":");
    serviceURLLabel.setText(getLocalizedText("serviceURL") + ":");
    serviceURLComboBox.setEditable(true);
    southPanel.add(okButton, null);
    southPanel.add(cancelButton, null);
    this.getContentPane().add(southPanel, BorderLayout.SOUTH);
    this.getContentPane().add(centerPanel, BorderLayout.CENTER);
    centerPanel.add(usernameTextField, 
                new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
                                       new Insets(10, 10, 10, 10), 0, 0));
    centerPanel.add(usernameLabel, 
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                       new Insets(0, 10, 0, 0), 0, 0));
    centerPanel.add(passwordLabel, 
                new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                       new Insets(0, 10, 0, 0), 0, 0));
    centerPanel.add(serviceURLLabel, 
                new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                       new Insets(0, 10, 0, 0), 0, 0));
    centerPanel.add(passwordField, 
                new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
                                       new Insets(10, 10, 10, 10), 0, 0));
    centerPanel.add(serviceURLComboBox, 
                new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
                                       new Insets(10, 10, 10, 10), 0, 0));
    
    this.getRootPane().setDefaultButton(okButton);

  }

  public void setUsername(String username)
  {
    this.usernameTextField.setText(username);
  }

  public String getUsername()
  {
    return this.usernameTextField.getText();
  }

  public void setPassword(String password)
  {
    this.passwordField.setText(password);
  }

  public String getPassword()
  {
    return String.valueOf(passwordField.getPassword());
  }

  @Deprecated
  public void setServiceURL(String[] urls)
  {
    /*
    for (int i = 0; i < urls.length; i++)
    {
      serviceURLComboBox.addItem(urls[i]);
    }
    */
  }

  public int showDialog()
  {
    this.setVisible(true);
    return selectedOption;
  }
  
  public String getServiceURL()
  {
    return (String)serviceURLComboBox.getSelectedItem();
  }
  
  public int getSelectedOption()
  {
    return selectedOption;
  }

  private void okButton_actionPerformed(ActionEvent e)
  {
    selectedOption = OK;
    updateServiceURLFile();
    this.dispose();
  }

  private void cancelButton_actionPerformed(ActionEvent e)
  {
    selectedOption = CANCEL;
    this.dispose();
  }

  // SERVICE URL FILE METHODS

  private void loadServiceURLFile()
  {
    try
    {
      String lastURL = "";
      String userDir = System.getProperty("user.home");
      File file = new File(userDir, "LoginDialog.properties");
      if (file.exists())
      {
        FileInputStream fis = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(new InputStreamReader(fis));
        if (fis != null) fis.close();
        if (properties.containsProperty("lastURL"))
        {
          lastURL = String.valueOf(properties.getProperty("lastURL"));
        }
        for (Object key : properties.keySet())
        {
          String strKey = String.valueOf(key);
          if (strKey.startsWith("url:"))
          {
            String url = String.valueOf(properties.getProperty(strKey));
            serviceURLComboBox.addItem(url);
            if (url.equalsIgnoreCase(lastURL))
            {
              serviceURLComboBox.setSelectedItem(url);
            }
          }
        }
        if (properties.containsProperty("lastUser"))
        {
          usernameTextField.setText(
            String.valueOf(properties.getProperty("lastUser")));          
        }
      }
      else
      {
        FileOutputStream fos = new FileOutputStream(file);
        if (fos != null) fos.close();
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  private void updateServiceURLFile()
  {
    try
    {
      String userDir = System.getProperty("user.home");
      File file = new File(userDir, "LoginDialog.properties");
      FileInputStream fis = new FileInputStream(file);
      Properties properties = new Properties();
      properties.load(new InputStreamReader(fis));
      if (fis != null) fis.close();
      String selectedUrl = (String)serviceURLComboBox.getSelectedItem();
      if (!existsURL(properties, selectedUrl))
      {
        properties.setProperty("url:" + (getURLCount(properties) + 1),
          selectedUrl);
      }
      properties.setProperty("lastUser", usernameTextField.getText());
      properties.setProperty("lastURL", selectedUrl);
      FileOutputStream fos = new FileOutputStream(file);
      properties.save(new OutputStreamWriter(fos));
      if (fos != null) fos.close();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  private int getURLCount(Map properties)
  {
    int count = 0;
    for (Object key : properties.keySet())
    {
      String strKey = String.valueOf(key);
      if (strKey.startsWith("url:")) count++;
    }
    return count;
  }

  private boolean existsURL(Map properties, String url)
  {
    for (Object key : properties.keySet())
    {
      String strKey = String.valueOf(key);
      if (strKey.startsWith("url:"))
      {
        String strValue = String.valueOf(properties.get(key));
        if (strValue.equalsIgnoreCase(url))
        {
          return true;
        }
      }
    }
    return false;
  }

  private ResourceBundle loadResourceBundle(Locale locale) 
  {    
    return ResourceBundle.getBundle("org.santfeliu.swing.resources.LoginDialogBundle", locale);
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
  
  public static void main(String[] args)
  {
    LoginDialog login = new LoginDialog();
    login.showDialog();
  }
}
