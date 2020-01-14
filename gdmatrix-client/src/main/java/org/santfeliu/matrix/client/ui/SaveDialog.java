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
package org.santfeliu.matrix.client.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author realor
 */
public class SaveDialog extends JFrame
{
  public static final int NO = 0;
  public static final int YES = 1;
  
  private final JPanel panel;
  private final JLabel label;
  private final JPanel southPanel;
  private final JButton yesButton;
  private final JButton noButton;
  private final Object lock = new Object();
  private int response = -1;
  
  public SaveDialog() 
  {
    setTitle("Matrix");
    setAlwaysOnTop(true);
    setAutoRequestFocus(true);
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    setResizable(false);
    panel = new JPanel();
    label = new JLabel();
    label.setHorizontalAlignment(JLabel.CENTER);
    southPanel = new JPanel();
    yesButton = new JButton("SI");
    noButton = new JButton("NO");  
    getContentPane().add(panel);    
    panel.setLayout(new BorderLayout());
    panel.add(label, BorderLayout.CENTER);
    panel.add(southPanel, BorderLayout.SOUTH);
    southPanel.add(yesButton);
    southPanel.add(noButton);
    yesButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        sendResponse(YES);
      }
    });
    noButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        sendResponse(NO);
      }
    });
  }
   
  public void showMessage(String text)
  {
    this.label.setText(text);
    this.setSize(300, 200);
    this.setLocationRelativeTo(null);
    this.setVisible(true);
  }
  
  public int waitForResponse() throws InterruptedException
  {
    while (response == -1)
    {
      synchronized (lock)
      {
        lock.wait();
      }
    }
    return response;
  }

  private void sendResponse(int response)
  {
    this.response = response;
    setVisible(false);
    dispose();
    synchronized (lock)
    {
      lock.notifyAll();
    }
  }
}
