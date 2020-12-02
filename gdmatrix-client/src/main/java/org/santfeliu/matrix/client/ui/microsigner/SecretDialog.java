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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.border.BevelBorder;


/**
 *
 * @author realor
 */
public class SecretDialog extends JDialog
{
  private JLabel pinLabel = new JLabel();
  private JPasswordField passwordField = new JPasswordField(); 
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JButton acceptButton = new JButton();

  private String secret = null;

  public SecretDialog(Frame parent)
  {
    super(parent, MicroSigner.TITLE, true);
    try
    {
      initComponents();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public void setText(String text)
  {
    pinLabel.setText(text);
  }

  private void initComponents() throws Exception
  {
    this.setSize(new Dimension(240, 170));
    this.getContentPane().setLayout(gridBagLayout1);
    pinLabel.setText(MicroSigner.getLocalizedText("EnterPIN"));
    passwordField.setFont(new Font("Monospaced", 0, 18));
    passwordField.setBackground(new Color(255, 165, 132));
    passwordField.setBorder(BorderFactory.createBevelBorder(BevelBorder
                                                            .LOWERED));
    passwordField.addKeyListener(new KeyAdapter()
    {
      public void keyReleased(KeyEvent e)
      {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
        {
          done();
        }
      }
    });
    acceptButton.setText(MicroSigner.getLocalizedText("Accept"));
    acceptButton.addActionListener(new ActionListener() {
                                     public void actionPerformed(ActionEvent e)
                                     {
                                       acceptButton_actionPerformed(e);
                                     }
                                   }
    );
    this.getContentPane().add(pinLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,GridBagConstraints
                                                               .HORIZONTAL,
                                                               new Insets(5,
                                                                                       5,
                                                                                       5,
                                                                                       5),
                                                               276, 0));
    this.getContentPane().add(passwordField, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,GridBagConstraints
                                                                      .HORIZONTAL,
                                                                      new Insets(5,
                                                                                              5,
                                                                                              5,
                                                                                              5),
                                                                      0,
                                                                      0));
    this.getContentPane()
    .add(acceptButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,GridBagConstraints
                                                                   .NONE,
                                                                   new Insets(8,
                                                                                     0,
                                                                                     5,
                                                                                     0),
                                                                   0, 0));
  }

  public String getSecret()
  {
    return secret;
  }

  private void acceptButton_actionPerformed(ActionEvent e)
  {
    done();
  }
 
  private void done()
  {
    secret = new String(passwordField.getPassword());
    this.setVisible(false);
    this.dispose();
  }
}
