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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.swing.form.ComponentEditor;
import org.santfeliu.swing.form.ComponentView;
import org.santfeliu.swing.form.view.LabelView;


/**
 *
 * @author unknown
 */
public class LabelEditor extends JPanel
  implements ComponentEditor
{
  private LabelView labelView;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel textLabel = new JLabel();
  private JTextField textTextField = new JTextField();
  private JLabel forLabel = new JLabel();
  private JTextField forTextField = new JTextField();

  public LabelEditor()
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
    this.labelView = (LabelView)view;
    textTextField.setText(labelView.getText());
    forTextField.setText(labelView.getForElement());
    return this;
  }
  
  public void stopEditing() throws Exception
  {
    String text = textTextField.getText();
    if (StringUtils.isBlank(text))
      throw new Exception("text is mandatory");
  
    labelView.setText(text);
    labelView.setForElement(forTextField.getText());
  }
  
  public void cancelEditing()
  {
  }

  private void jbInit()
    throws Exception
  {
    this.setLayout(gridBagLayout1);
    this.setPreferredSize(new Dimension(300, 100));
    this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    forLabel.setText("For element:");
    textLabel.setText("Text:");
    this.add(textLabel, 
             new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                    new Insets(2, 2, 2, 6), 0, 0));
    this.add(textTextField, 
             new GridBagConstraints(1, 0, 2, 1, 1.0, 0.0,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
                                    new Insets(2, 2, 2, 2), 0, 0));
    this.add(forLabel, 
             new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                    new Insets(2, 2, 2, 6), 0, 0));
    this.add(forTextField, 
             new GridBagConstraints(1, 1, 2, 1, 1.0, 0.0,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
                                    new Insets(2, 2, 2, 2), 0, 0));
  }
}
