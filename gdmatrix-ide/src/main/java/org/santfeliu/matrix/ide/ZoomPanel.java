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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author unknown
 */
public class ZoomPanel extends JPanel
{
  protected MatrixIDE ide;
  private FlowLayout flowLayout = new FlowLayout();
  private JLabel zoomLabel = new JLabel();
  private JComboBox zoomComboBox = new JComboBox()
  {
    @Override
    public Dimension getPreferredSize()
    {
      Dimension preferred = super.getPreferredSize();      
      return new Dimension(70, preferred.height);
    }
  };

  public ZoomPanel()
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

  private void jbInit() throws Exception
  {
    this.setLayout(flowLayout);
    this.add(zoomLabel, null);
    this.add(zoomComboBox, null);
    zoomLabel.setText("Zoom:");
    zoomComboBox.setEditable(true);
    zoomComboBox.addItem("400%");
    zoomComboBox.addItem("200%");
    zoomComboBox.addItem("150%");
    zoomComboBox.addItem("100%");
    zoomComboBox.addItem("75%");
    zoomComboBox.addItem("70%");
    zoomComboBox.addItem("60%");
    zoomComboBox.addItem("50%");
    zoomComboBox.addItem("40%");
    zoomComboBox.addItem("30%");
    zoomComboBox.addItem("25%");
    zoomComboBox.addItem("20%");
    zoomComboBox.addItem("10%");
    zoomComboBox.addItem("5%");
    zoomComboBox.setSelectedItem("100%");
    zoomComboBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        String value = (String)zoomComboBox.getSelectedItem();
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < value.length(); i++)
        {
          char ch = value.charAt(i);
          if (Character.isDigit(ch) || ch == '.') buffer.append(ch);
        }
        try
        {
          double size = Double.parseDouble(buffer.toString());
          DocumentPanel panel = ide.getMainPanel().getActivePanel();
          if (panel != null) panel.setZoom(size);
        }
        catch (NumberFormatException ex)
        {
        }
      }
    });
  }

  public void setIDE(MatrixIDE ide)
  {
    this.ide = ide;
  }
  
  public MatrixIDE getIDE()
  {
    return ide;
  }
  
  public void updateZoomValue()
  {
    DocumentPanel panel = ide.getMainPanel().getActivePanel();
    if (panel != null)
    {
      double size = panel.getZoom();
      DecimalFormat df = new DecimalFormat("0", 
        new DecimalFormatSymbols(new Locale("en")));
      String value = df.format(size) + "%";
      zoomComboBox.setSelectedItem(value);
    }
  }  
}
