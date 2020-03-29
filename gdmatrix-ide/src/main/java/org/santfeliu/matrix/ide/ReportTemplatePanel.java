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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.text.EditorKit;
import org.santfeliu.swing.Utilities;
import org.santfeliu.swing.layout.WrapLayout;
import org.santfeliu.swing.text.ReportTemplateEditorKit;
import org.santfeliu.swing.text.SymbolHighlighter;
/**
 *
 * @author blanquepa
 */
public class ReportTemplatePanel extends TextPanel
{
  private JToolBar toolBar = new JToolBar();
  private JButton executeReportButton = new JButton();
  
  public ReportTemplatePanel()
  {
    super();
    toolBar.setFloatable(false);
    toolBar.setRollover(true);
    toolBar.setLayout(new WrapLayout(WrapLayout.LEFT, 2, 2));
    toolBar.setMinimumSize(new Dimension(1, 1));

    executeReportButton.setText("Execute report");
    executeReportButton.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/images/run.gif")));
    executeReportButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        execButton_actionPerformed(e);
      }
    }); 
    toolBar.add(executeReportButton, null);
    
    Color borderColor = UIManager.getColor("Panel.background").darker();
    toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor));
    this.add(toolBar, BorderLayout.NORTH); 
    
    SymbolHighlighter symbolHighlighter = 
      new SymbolHighlighter(textEditor.getTextPane(), "({[", ")}]");
    symbolHighlighter.setScriptletTags("<%", "%>");    
  }
  
  @Override
  protected String getCategoryName()
  {
    return "template";
  }  
  
  @Override
  protected String getContentType()
  {
    return "text/template";
  }

  @Override
  protected EditorKit getEditorKit()
  {
    return new ReportTemplateEditorKit();
  }

  @Override
  protected String getNewDocument()
  {
    return "<html>\n" +
    "  <head>\n" + 
    "    <title>new</title>\n" + 
    "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" + 
    "  </head>\n" + 
    "  <body>\n" + 
    "   <%  %>\n" +
    "  </body>\n" + 
    "</html>\n";
  }

  @Override
  protected String getCharset()
  {
    return "utf8";
  }
  
  private void execButton_actionPerformed(ActionEvent e)
  {
    try
    {
      Frame frame = (Frame)Utilities.getParentWindow(this);      
      ReportDialog reportDialog = new ReportDialog(frame, getMainPanel());
      reportDialog.setLocationRelativeTo(null);
      reportDialog.setVisible(true);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }  
 
  
}
