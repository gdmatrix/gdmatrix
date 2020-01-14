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


import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;


/**
 *
 * @author unknown
 */
public class ProgressDialog extends JDialog
{
  private JPanel mainPanel;
  private JProgressBar progressBar;
  private JLabel statusLabel;
  private JButton cancelButton;
  private JPanel southPanel;
  private boolean canceled = false;

  public ProgressDialog(String title, int width, int height)
  {
    super((Frame)null, title, false);    
    initComponents();
    setSize(width, height);
  }

  private void initComponents()
  {
    setLayout(new BorderLayout());

    mainPanel = new JPanel();
    progressBar = new JProgressBar();
    statusLabel = new JLabel();
    cancelButton = new JButton();
    southPanel = new JPanel();

    mainPanel.setLayout(new BorderLayout());
    mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    add(mainPanel, BorderLayout.CENTER);

    mainPanel.add(statusLabel, BorderLayout.NORTH);
    mainPanel.add(progressBar, BorderLayout.CENTER);
    mainPanel.add(southPanel, BorderLayout.SOUTH);

    progressBar.setStringPainted(true);
    southPanel.add(cancelButton, null);
    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        canceled = true;
      }
    });
    this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    setAlwaysOnTop(true);
    setLocationRelativeTo(null);
    setVisible(false);
  }

  public void setCancelEnabled(boolean enabled)
  {
    cancelButton.setEnabled(enabled);
  }

  public boolean isCancelEnabled()
  {
    return cancelButton.isEnabled();
  }

  public void setValue(int value)
  {
    progressBar.setValue(value);
  }
  
  public void setMinimum(int value)
  {
    progressBar.setMinimum(value);
  }
  
  public void setMaximum(int value)
  {
    progressBar.setMaximum(value);
  }
  
  public void setStringPainted(boolean b)
  {
    progressBar.setStringPainted(b);
  }
  
  public void setStatus(String text)
  {
    statusLabel.setText(text);
  }
  
  public void setCancelButtonText(String text)
  {
    cancelButton.setText(text);
  }

  public boolean isCanceled()
  {
    return canceled;
  }

  public void setCanceled(boolean canceled)
  {
    this.canceled = canceled;
  }
}
