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

import javax.swing.SpinnerNumberModel;

/**
 *
 * @author realor
 */
public class WorkflowGridDialog extends javax.swing.JDialog
{
  boolean accept = false;

  /** Creates new form WorkflowGridDialog */
  public WorkflowGridDialog(java.awt.Frame parent)
  {
    super(parent, true);
    initComponents();
  }

  public int getGridSize()
  {
    return ((Number)gridSizeSpinner.getValue()).intValue();
  }

  public void setGridSize(int gridSize)
  {
    gridSizeSpinner.setValue(gridSize);
  }

  public int getNodeWidth()
  {
    return ((Number)nodeWidthSpinner.getValue()).intValue();
  }

  public void setNodeWidth(int nodeWidth)
  {
    nodeWidthSpinner.setValue(nodeWidth);
  }

  public int getNodeHeight()
  {
    return ((Number)nodeHeightSpinner.getValue()).intValue();
  }

  public void setNodeHeight(int nodeHeight)
  {
    nodeHeightSpinner.setValue(nodeHeight);
  }

  public boolean isGridVisible()
  {
    return gridVisibleCheckBox.isSelected();
  }

  public void setGridVisible(boolean visible)
  {
    gridVisibleCheckBox.setSelected(visible);
  }

  public boolean isGridEnabled()
  {
    return gridEnabledCheckBox.isSelected();
  }

  public void setGridEnabled(boolean enabled)
  {
    gridEnabledCheckBox.setSelected(enabled);
  }

  public boolean showDialog()
  {
    setVisible(true);
    return accept;
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {
    java.awt.GridBagConstraints gridBagConstraints;

    centerPanel = new javax.swing.JPanel();
    optionsPanel = new javax.swing.JPanel();
    gridSizeLabel = new javax.swing.JLabel();
    gridSizeSpinner = new javax.swing.JSpinner();
    gridVisibleLabel = new javax.swing.JLabel();
    gridVisibleCheckBox = new javax.swing.JCheckBox();
    gridEnabledLabel = new javax.swing.JLabel();
    gridEnabledCheckBox = new javax.swing.JCheckBox();
    nodeWidthLabel = new javax.swing.JLabel();
    nodeWidthSpinner = new javax.swing.JSpinner();
    nodeHeightLabel = new javax.swing.JLabel();
    nodeHeightSpinner = new javax.swing.JSpinner();
    pixelsLabel1 = new javax.swing.JLabel();
    pixelsLabel2 = new javax.swing.JLabel();
    pixelsLabel3 = new javax.swing.JLabel();
    southPanel = new javax.swing.JPanel();
    acceptButton = new javax.swing.JButton();
    cancelButton = new javax.swing.JButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setTitle("Grid options");

    centerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
    centerPanel.setLayout(new java.awt.BorderLayout());

    optionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
    optionsPanel.setLayout(new java.awt.GridBagLayout());

    gridSizeLabel.setText("Grid size:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    optionsPanel.add(gridSizeLabel, gridBagConstraints);

    gridSizeSpinner.setModel(new SpinnerNumberModel(8, 4, 50, 1)
    );
    gridSizeSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(gridSizeSpinner, "####"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    optionsPanel.add(gridSizeSpinner, gridBagConstraints);

    gridVisibleLabel.setText("Grid visible:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    optionsPanel.add(gridVisibleLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    optionsPanel.add(gridVisibleCheckBox, gridBagConstraints);

    gridEnabledLabel.setText("Grid enabled:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    optionsPanel.add(gridEnabledLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    optionsPanel.add(gridEnabledCheckBox, gridBagConstraints);

    nodeWidthLabel.setText("Node width:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    optionsPanel.add(nodeWidthLabel, gridBagConstraints);

    nodeWidthSpinner.setModel(new SpinnerNumberModel(100, 10, 500, 1));
    nodeWidthSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(nodeWidthSpinner, "####"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    optionsPanel.add(nodeWidthSpinner, gridBagConstraints);

    nodeHeightLabel.setText("Node height:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    optionsPanel.add(nodeHeightLabel, gridBagConstraints);

    nodeHeightSpinner.setModel(new SpinnerNumberModel(50, 10, 500, 1));
    nodeHeightSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(nodeHeightSpinner, "####"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    optionsPanel.add(nodeHeightSpinner, gridBagConstraints);

    pixelsLabel1.setText("pixels");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    optionsPanel.add(pixelsLabel1, gridBagConstraints);

    pixelsLabel2.setText("pixels");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    optionsPanel.add(pixelsLabel2, gridBagConstraints);

    pixelsLabel3.setText("pixels");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    optionsPanel.add(pixelsLabel3, gridBagConstraints);

    centerPanel.add(optionsPanel, java.awt.BorderLayout.CENTER);

    getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

    acceptButton.setText("Accept");
    acceptButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        acceptButtonActionPerformed(evt);
      }
    });
    southPanel.add(acceptButton);

    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        cancelButtonActionPerformed(evt);
      }
    });
    southPanel.add(cancelButton);

    getContentPane().add(southPanel, java.awt.BorderLayout.SOUTH);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void acceptButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_acceptButtonActionPerformed
  {//GEN-HEADEREND:event_acceptButtonActionPerformed
    accept = true;
    setVisible(false);
    dispose();
  }//GEN-LAST:event_acceptButtonActionPerformed

  private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
  {//GEN-HEADEREND:event_cancelButtonActionPerformed
    accept = false;
    setVisible(false);
    dispose();
  }//GEN-LAST:event_cancelButtonActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton acceptButton;
  private javax.swing.JButton cancelButton;
  private javax.swing.JPanel centerPanel;
  private javax.swing.JCheckBox gridEnabledCheckBox;
  private javax.swing.JLabel gridEnabledLabel;
  private javax.swing.JLabel gridSizeLabel;
  private javax.swing.JSpinner gridSizeSpinner;
  private javax.swing.JCheckBox gridVisibleCheckBox;
  private javax.swing.JLabel gridVisibleLabel;
  private javax.swing.JLabel nodeHeightLabel;
  private javax.swing.JSpinner nodeHeightSpinner;
  private javax.swing.JLabel nodeWidthLabel;
  private javax.swing.JSpinner nodeWidthSpinner;
  private javax.swing.JPanel optionsPanel;
  private javax.swing.JLabel pixelsLabel1;
  private javax.swing.JLabel pixelsLabel2;
  private javax.swing.JLabel pixelsLabel3;
  private javax.swing.JPanel southPanel;
  // End of variables declaration//GEN-END:variables
}
