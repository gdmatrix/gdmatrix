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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.security.KeyStore;
import java.security.Provider;

import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;


/**
 *
 * @author unknown
 */
public class KeyStoreDialog extends JDialog
{
  private KeyStoreNode ksNode;
  private BorderLayout borderLayout = new BorderLayout();
  private JPanel centerPanel = new JPanel();
  private GridBagLayout gridBagLayout = new GridBagLayout();
  
  private JPasswordField ksPasswordField = new JPasswordField();
  private JTextField ksPathTextField = new JTextField();
  private JComboBox ksTypeComboBox = new JComboBox();
  private JComboBox provClassNameComboBox = new JComboBox();
  private JLabel ksPasswordLabel = new JLabel();
  private JLabel ksPathLabel = new JLabel();
  private JLabel ksTypeLabel = new JLabel();
  private JLabel provClassNameLabel = new JLabel();
  private JPanel southPanel = new JPanel();
  private JButton acceptButton = new JButton();
  private JButton cancelButton = new JButton();
  private JLabel provVersionLabel = new JLabel();
  private JLabel providerLabel = new JLabel();

  public KeyStoreDialog(Frame owner)
  {
    super(owner);
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public void setKeyStoreNode(KeyStoreNode ksNode)
  {
    this.ksNode = ksNode;
    KeyStore keyStore = ksNode.getKeyStore();
    
    Provider provider = keyStore.getProvider();
    provVersionLabel.setText(MicroSigner.getLocalizedText("ProviderVersion"));
    
    String provClassName = provider.getClass().getName();
    provClassNameComboBox.setSelectedItem(provClassName);

    String ksType = keyStore.getType();
    ksTypeComboBox.setSelectedItem(ksType);
    
    ksPathTextField.setText(ksNode.getKeyStorePath());
    ksPasswordField.setText(ksNode.getKeyStorePassword());
  }
  
  public KeyStoreNode getKeyStoreNode()
  {
    return ksNode;
  }

  private void jbInit() throws Exception
  {
    setTitle(MicroSigner.TITLE);
    getContentPane().setLayout(borderLayout);
    getContentPane().add(centerPanel, BorderLayout.CENTER);
    getContentPane().add(southPanel, BorderLayout.SOUTH);   
    centerPanel.setLayout(gridBagLayout);
    centerPanel.setBorder(BorderFactory.createTitledBorder(
      MicroSigner.getLocalizedText("KeyStoreSetup")));

    provClassNameLabel.setText(
      MicroSigner.getLocalizedText("ProviderClassName"));
    ksPasswordLabel.setText(
      MicroSigner.getLocalizedText("KeyStorePassword"));
    ksPathLabel.setText(
      MicroSigner.getLocalizedText("KeyStorePath"));
    ksTypeLabel.setText(
      MicroSigner.getLocalizedText("KeyStoreType"));
    acceptButton.setText(MicroSigner.getLocalizedText("Accept"));
    acceptButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        acceptButton_actionPerformed(e);
      }
    });
    cancelButton.setText(MicroSigner.getLocalizedText("Cancel"));
    cancelButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        cancelButton_actionPerformed(e);
      }
    });
    provVersionLabel.setText(
      MicroSigner.getLocalizedText("ProviderVersion"));
    centerPanel.add(ksPasswordField, 
      new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, 
      GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 100, 0));
    centerPanel.add(ksPathTextField, 
      new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, 
      GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
    centerPanel.add(ksTypeComboBox, 
      new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
      GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 60, 0));
    centerPanel.add(provClassNameComboBox, 
      new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
      GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
    centerPanel.add(ksPasswordLabel, 
      new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
      GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    centerPanel.add(ksPathLabel, 
      new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
      GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    centerPanel.add(ksTypeLabel, 
      new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
      GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    centerPanel.add(provClassNameLabel, 
      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
      GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    centerPanel.add(provVersionLabel, 
      new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
      GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    centerPanel.add(providerLabel, 
      new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
      GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

    provClassNameComboBox.setEditable(true);
    int prefHeight = ksTypeComboBox.getPreferredSize().height;
    provClassNameComboBox.setPreferredSize(new Dimension(350, prefHeight));
    provClassNameComboBox.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        provClassNameComboBox_actionPerformed(e);
      }
    });
    southPanel.add(acceptButton, null);
    southPanel.add(cancelButton, null);

    provClassNameComboBox.addItem("sun.security.mscapi.SunMSCAPI");
    provClassNameComboBox.addItem("sun.security.pkcs11.SunPKCS11");
    provClassNameComboBox.addItem("sun.security.provider.Sun");
    provClassNameComboBox.addItem("apple.security.AppleProvider");
    provClassNameComboBox.addItem("com.sun.net.ssl.internal.ssl.Provider");
    provClassNameComboBox.addItem("org.bouncycastle.jce.provider.BouncyCastleProvider");
    provClassNameComboBox.addItem("iaik.security.provider.IAIK");
  }

  private void acceptButton_actionPerformed(ActionEvent e)
  {
    try
    {
      String provClassName = (String)provClassNameComboBox.getSelectedItem();
      String ksType = (String)ksTypeComboBox.getSelectedItem();
      String ksPath = ksPathTextField.getText();
      String ksPassword = new String(ksPasswordField.getPassword());

      ksNode = MicroSigner.loadKeyStore(provClassName, ksType, 
                                        ksPath, ksPassword, ksNode);

      this.setVisible(false);
      this.dispose();
    }
    catch (Throwable ex)
    {
      JOptionPane.showMessageDialog(this, MicroSigner.getLocalizedText(ex),
        "ERROR", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void cancelButton_actionPerformed(ActionEvent e)
  {
    ksNode = null;
    this.setVisible(false);
    this.dispose();
  }

  private void provClassNameComboBox_actionPerformed(ActionEvent e)
  {
    try
    {
      String provClassName = (String)provClassNameComboBox.getSelectedItem();
      ksTypeComboBox.removeAllItems();
      if ("sun.security.pkcs11.SunPKCS11".equals(provClassName))
      {
        providerLabel.setText("SunPKCS11");
        ksTypeComboBox.addItem("PKCS11");
      }
      else
      {
        Provider provider = MicroSigner.loadProvider(provClassName);
        Enumeration enu = provider.propertyNames();
        while (enu.hasMoreElements())
        {
          String property = (String)enu.nextElement();
          if (property.startsWith("KeyStore."))
          {
            String ksType = property.substring(9);
            ksTypeComboBox.addItem(ksType);
          }
        }
        if (ksNode != null && ksNode.getKeyStore() != null)
        {
          ksTypeComboBox.setSelectedItem(ksNode.getKeyStore().getType());
        }
        providerLabel.setText(provider.toString());
      }
    }
    catch (Throwable ex)
    {
      ex.printStackTrace();
      providerLabel.setText(
        MicroSigner.getLocalizedText("ProviderNotAvailable"));
      ksTypeComboBox.removeAllItems();
    }
  }
}
