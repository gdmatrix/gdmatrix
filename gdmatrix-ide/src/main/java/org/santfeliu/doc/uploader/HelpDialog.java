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
package org.santfeliu.doc.uploader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.swing.text.html.HTMLEditorKit;

/**
 *
 * @author realor
 */
public class HelpDialog extends javax.swing.JDialog
{

  /** Creates new form HelpDialog */
  public HelpDialog(java.awt.Frame parent, boolean modal)
  {
    super(parent, modal);
    initComponents();
    readFile();
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    southPanel = new javax.swing.JPanel();
    closeButton = new javax.swing.JButton();
    scrollPane = new javax.swing.JScrollPane();
    helpTextPane = new javax.swing.JTextPane();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setTitle("Help");

    closeButton.setText("Close");
    closeButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        closeButtonActionPerformed(evt);
      }
    });
    southPanel.add(closeButton);

    getContentPane().add(southPanel, java.awt.BorderLayout.PAGE_END);

    helpTextPane.setEditable(false);
    helpTextPane.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
    scrollPane.setViewportView(helpTextPane);

    getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);

    pack();
  }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_closeButtonActionPerformed
    {//GEN-HEADEREND:event_closeButtonActionPerformed
      setVisible(false);
      dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

  /**
   * @param args the command line arguments
   */
  public static void main(String args[])
  {
    java.awt.EventQueue.invokeLater(new Runnable()
    {

      public void run()
      {
        HelpDialog dialog = new HelpDialog(new javax.swing.JFrame(), true);
        dialog.addWindowListener(new java.awt.event.WindowAdapter()
        {

          public void windowClosing(java.awt.event.WindowEvent e)
          {
            System.exit(0);
          }
        });
        dialog.setVisible(true);
      }
    });
  }
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton closeButton;
  private javax.swing.JTextPane helpTextPane;
  private javax.swing.JScrollPane scrollPane;
  private javax.swing.JPanel southPanel;
  // End of variables declaration//GEN-END:variables

  private void readFile()
  {
    try
    {
      StringBuilder buffer = new StringBuilder();
      BufferedReader reader = new BufferedReader(
        new InputStreamReader(getClass().getResourceAsStream(
        "resources/help_ca.html")));
      try
      {        
        String line = reader.readLine();
        while (line != null)
        {
          buffer.append(line).append("\n");
          line = reader.readLine();
        }
      }
      finally
      {
        reader.close();
      }
      helpTextPane.setContentType("text/html");
      helpTextPane.setEditorKit(new HTMLEditorKit());
      String text = buffer.toString();
      System.out.println(text);
      helpTextPane.setText(text);
      helpTextPane.setCaretPosition(0);
    }
    catch (Exception ex)
    {
    }
  }
}
