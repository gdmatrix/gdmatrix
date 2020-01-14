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

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 *
 * @author realor
 */
public class MessageWindow extends JDialog
{
  private final JPanel panel;
  private final JLabel label;
  
  public MessageWindow()
  {
    this(null);
  }
  
  public MessageWindow(Frame owner)
  {
    super(owner);
    setAlwaysOnTop(true);
    setAutoRequestFocus(true);
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    setResizable(false);
    panel = new JPanel();
    panel.setBackground(new Color(250, 250, 160));
    panel.setOpaque(true);
    label = new JLabel();
    setUndecorated(true);
    getContentPane().add(panel);
    panel.setBorder(new LineBorder(Color.BLACK, 3));
    panel.add(label);
    label.setFont(new Font("Arial", Font.BOLD, 16));
    label.setBorder(new EmptyBorder(20, 40, 20, 40));
  }
  
  public void showWindow(String text)
  {
    label.setText(text);
    pack();
    setLocationRelativeTo(null);
    setVisible(true);
  }
  
  public void hideWindow()
  {
    setVisible(false);
    dispose();
  }  
}
