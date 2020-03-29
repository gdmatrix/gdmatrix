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
package org.santfeliu.matrix.ide;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.santfeliu.swing.Utilities;


/**
 *
 * @author realor
 */
public class ConnectionPanel extends JPanel
{
  private List connections;
  private DefaultComboBoxModel model = new DefaultComboBoxModel();
  private JLabel connLabel = new JLabel();
  private JButton editConnButton = new JButton();
  private JToolBar connToolBar = new JToolBar();
  private JComboBox connComboBox = new JComboBox()
  {
    @Override
    public Dimension getPreferredSize()
    {
      Dimension preferred = super.getPreferredSize();
      return new Dimension(120, preferred.height);
    }
  };


  public ConnectionPanel()
  {
    try
    {
      jbInit();
    }
    catch (Exception ex)
    {
      MatrixIDE.log(ex);
    }
  }

  private void jbInit()
    throws Exception
  {
    connLabel.setText("Connection:");
    connComboBox.setModel(model);
    
    Dimension dim = new Dimension(24, 24);

    editConnButton.setIcon(loadIcon(
      "/org/santfeliu/matrix/ide/resources/images/connection.gif"));

    editConnButton.setMinimumSize(dim);
    editConnButton.setPreferredSize(dim);
    editConnButton.setToolTipText("Edit connections...");
    editConnButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            editConnButton_actionPerformed(e);
          }
        });    

    connToolBar.setFloatable(false);
    connToolBar.setRollover(true);
    connToolBar.setBorderPainted(false);
    this.add(connLabel, null);
    this.add(connComboBox, null);
    this.add(connToolBar, null);
    connToolBar.add(editConnButton, null);
  }
  
  protected ImageIcon loadIcon(String path)
  {
    try
    {
      return new ImageIcon(getClass().getResource(path));
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  private void editConnButton_actionPerformed(ActionEvent e)
  {
    Frame frame = (Frame)Utilities.getParentWindow(this);
    ConnectionDialog dialog = new ConnectionDialog(frame);    
    dialog.setConnections(connections);
    Utilities.centerWindow(frame, dialog);
    if (dialog.showDialog() == JOptionPane.OK_OPTION)
    {
      connections.clear();
      connections.addAll(dialog.getConnections());
      updateComboBox();
    }
    repaint();
  }

  public void setConnections(List connections)
  {
    this.connections = connections;
    updateComboBox();
  }

  public ConnectionParameters getSelectedConnection()
  {
    return (ConnectionParameters)model.getSelectedItem();
  }
  
  private void updateComboBox()
  {
    model.removeAllElements();
    for (int i = 0; i < connections.size(); i++)
    {
      ConnectionParameters connParams = 
        (ConnectionParameters)connections.get(i);
      model.addElement(connParams);
    }    
  }
}
