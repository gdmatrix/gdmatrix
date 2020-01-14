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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import org.santfeliu.matrix.ide.Options;
import org.santfeliu.swing.form.ComponentEditor;
import org.santfeliu.swing.form.ComponentView;
import org.santfeliu.swing.form.view.OutputTextAreaView;
import org.santfeliu.swing.text.TextEditor;
import org.santfeliu.swing.text.XMLEditorKit;


/**
 *
 * @author unknown
 */
public class OutputTextAreaEditor extends JPanel
  implements ComponentEditor
{
  private OutputTextAreaView textAreaView;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JLabel textLabel = new JLabel();
  private TextEditor textEditor = new TextEditor();

  public OutputTextAreaEditor()
  {
    try
    {
      initComponents();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  @Override
  public Component getEditingComponent(ComponentView view)
  {
    this.textAreaView = (OutputTextAreaView)view;
    String text = textAreaView.getText();
    textEditor.getTextPane().setText(text);
    textEditor.getTextPane().setCaretPosition(0);
    textEditor.getTextPane().setPreferredSize(new Dimension(640, 300));
    return this;
  }
  
  @Override
  public void stopEditing()
  {
    textAreaView.setText(textEditor.getTextPane().getText());
  }
  
  @Override
  public void cancelEditing()
  {
  }

  private void initComponents()
    throws Exception
  {
    this.setSize(new Dimension(434, 358));
    this.setLayout(borderLayout1);
    this.setBorder(new EmptyBorder(4, 4, 4, 4));
    textLabel.setText("Text (with markup):");
    textLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
    this.add(textLabel, BorderLayout.NORTH);
    this.add(textEditor, BorderLayout.CENTER);
    textEditor.setBorder(new LineBorder(Color.LIGHT_GRAY));
    JTextPane textPane = textEditor.getTextPane();
    textPane.setEditorKitForContentType("text/xml", new XMLEditorKit());
    textPane.setContentType("text/xml");
    textPane.setSelectionColor(new Color(198, 198, 198));
    textPane.setFont(Options.getEditorFont());
  }
}
