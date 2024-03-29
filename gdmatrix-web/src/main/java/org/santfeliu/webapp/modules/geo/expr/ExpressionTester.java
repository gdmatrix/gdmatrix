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
package org.santfeliu.webapp.modules.geo.expr;

import org.santfeliu.webapp.modules.geo.io.OgcWriter;
import org.santfeliu.webapp.modules.geo.io.OgcReader;
import org.santfeliu.webapp.modules.geo.io.CqlReader;
import org.santfeliu.webapp.modules.geo.io.CqlWriter;
import java.awt.Color;
import java.awt.event.MouseEvent;

/**
 *
 * @author realor
 */
public class ExpressionTester extends javax.swing.JFrame
{

  /**
   * Creates new form ExpressionTester
   */
  public ExpressionTester()
  {
    initComponents();
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jScrollPane3 = new javax.swing.JScrollPane();
    jList1 = new javax.swing.JList();
    scrollPane1 = new javax.swing.JScrollPane();
    cqlTextArea = new javax.swing.JTextArea();
    scrollPane2 = new javax.swing.JScrollPane();
    nativeTextArea = new javax.swing.JTextArea();
    scrollPane3 = new javax.swing.JScrollPane();
    ogcTextArea = new javax.swing.JTextArea();
    cqlLabel = new javax.swing.JLabel();
    nativeLabel = new javax.swing.JLabel();
    ogcLabel = new javax.swing.JLabel();
    helpLabel = new javax.swing.JLabel();

    jList1.setModel(new javax.swing.AbstractListModel() {
      String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
      public int getSize() { return strings.length; }
      public Object getElementAt(int i) { return strings[i]; }
    });
    jScrollPane3.setViewportView(jList1);

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("ExpressionTester");

    cqlTextArea.setColumns(20);
    cqlTextArea.setFont(new java.awt.Font("Courier New", 1, 14));
    cqlTextArea.setRows(5);
    cqlTextArea.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        cqlTextAreaMouseClicked(evt);
      }
    });
    scrollPane1.setViewportView(cqlTextArea);

    nativeTextArea.setEditable(false);
    nativeTextArea.setColumns(20);
    nativeTextArea.setFont(new java.awt.Font("Courier New", 1, 14));
    nativeTextArea.setForeground(new java.awt.Color(102, 102, 102));
    nativeTextArea.setRows(5);
    scrollPane2.setViewportView(nativeTextArea);

    ogcTextArea.setColumns(20);
    ogcTextArea.setFont(new java.awt.Font("Courier New", 1, 14));
    ogcTextArea.setRows(5);
    ogcTextArea.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        ogcTextAreaMouseClicked(evt);
      }
    });
    scrollPane3.setViewportView(ogcTextArea);

    cqlLabel.setText("CQL Expression:");

    nativeLabel.setText("Native expression:");

    ogcLabel.setText("OGC Expression:");

    helpLabel.setText("Mouse right-click on text area to convert expression");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(scrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 544, Short.MAX_VALUE)
          .addComponent(scrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 544, Short.MAX_VALUE)
          .addComponent(scrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 544, Short.MAX_VALUE)
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(cqlLabel)
              .addComponent(nativeLabel)
              .addComponent(ogcLabel))
            .addGap(0, 454, Short.MAX_VALUE))
          .addComponent(helpLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 544, Short.MAX_VALUE))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(helpLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(cqlLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(scrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(nativeLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(scrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(ogcLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(scrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
        .addContainerGap())
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void cqlTextAreaMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_cqlTextAreaMouseClicked
  {//GEN-HEADEREND:event_cqlTextAreaMouseClicked
    if (evt.getButton() != MouseEvent.BUTTON1)
    {
      try
      {
        CqlReader reader = new CqlReader();
        Expression expression = reader.fromString(cqlTextArea.getText());
        NativePrinter nativePrinter = new NativePrinter();
        nativeTextArea.setText(nativePrinter.toString(expression));
        OgcWriter ogcWriter = new OgcWriter();
        ogcTextArea.setText(ogcWriter.toString(expression));
        cqlTextArea.setBackground(Color.WHITE);
      }
      catch (Exception ex)
      {
        cqlTextArea.setBackground(new Color(255, 200, 200));
      }
    }
  }//GEN-LAST:event_cqlTextAreaMouseClicked

  private void ogcTextAreaMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_ogcTextAreaMouseClicked
  {//GEN-HEADEREND:event_ogcTextAreaMouseClicked
    if (evt.getButton() != MouseEvent.BUTTON1)
    {
      try
      {
        OgcReader reader = new OgcReader();
        Expression expression = reader.fromString(ogcTextArea.getText());
        NativePrinter nativePrinter = new NativePrinter();
        nativeTextArea.setText(nativePrinter.toString(expression));
        CqlWriter cqlWriter = new CqlWriter();
        cqlTextArea.setText(cqlWriter.toString(expression));
        ogcTextArea.setBackground(Color.WHITE);
      }
      catch (Exception ex)
      {
        ogcTextArea.setBackground(new Color(255, 200, 200));
      }
    }
  }//GEN-LAST:event_ogcTextAreaMouseClicked

  /**
   * @param args the command line arguments
   */
  public static void main(String args[])
  {
    /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
     * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
     */
    try
    {
      for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
      {
        if ("Nimbus".equals(info.getName()))
        {
          javax.swing.UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    }
    catch (ClassNotFoundException ex)
    {
      java.util.logging.Logger.getLogger(ExpressionTester.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
    catch (InstantiationException ex)
    {
      java.util.logging.Logger.getLogger(ExpressionTester.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
    catch (IllegalAccessException ex)
    {
      java.util.logging.Logger.getLogger(ExpressionTester.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
    catch (javax.swing.UnsupportedLookAndFeelException ex)
    {
      java.util.logging.Logger.getLogger(ExpressionTester.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
        //</editor-fold>
        //</editor-fold>

    /* Create and display the form */
    java.awt.EventQueue.invokeLater(() ->
    {
      ExpressionTester tester = new ExpressionTester();
      tester.setLocationRelativeTo(null);
      tester.setVisible(true);
    });
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel cqlLabel;
  private javax.swing.JTextArea cqlTextArea;
  private javax.swing.JLabel helpLabel;
  private javax.swing.JList jList1;
  private javax.swing.JScrollPane jScrollPane3;
  private javax.swing.JLabel nativeLabel;
  private javax.swing.JTextArea nativeTextArea;
  private javax.swing.JLabel ogcLabel;
  private javax.swing.JTextArea ogcTextArea;
  private javax.swing.JScrollPane scrollPane1;
  private javax.swing.JScrollPane scrollPane2;
  private javax.swing.JScrollPane scrollPane3;
  // End of variables declaration//GEN-END:variables
}
