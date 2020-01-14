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
package org.santfeliu.swing.form.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.santfeliu.swing.form.ComponentEditor;
import org.santfeliu.swing.form.ComponentView;
import org.santfeliu.swing.form.view.OutputTextView;

/**
 *
 * @author unknown
 */
public class OutputTextEditor extends JPanel
  implements ComponentEditor
{
  private OutputTextView labelView;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JLabel textLabel = new JLabel();
  private JTextField textField = new JTextField();

  public OutputTextEditor()
  {
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public Component getEditingComponent(ComponentView view)
  {
    this.labelView = (OutputTextView)view;
    String text = labelView.getText();
    textField.setText(text);
    textField.setCaretPosition(0);
    return this;
  }
  
  public void stopEditing()
  {
    labelView.setText(textField.getText());
  }
  
  public void cancelEditing()
  {
  }

  private void jbInit()
    throws Exception
  {
    this.setSize(new Dimension(434, 358));
    this.setLayout(borderLayout1);
    textLabel.setText("Text:");
    this.add(textLabel, BorderLayout.NORTH);
    this.add(textField, BorderLayout.CENTER);
    textField.setPreferredSize(new Dimension(200, 24));
  }
}
