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
package org.santfeliu.matrix.ide.action;

import java.awt.event.ActionEvent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;
import org.santfeliu.matrix.ide.DocumentPanel;
import org.santfeliu.matrix.ide.WorkflowGridDialog;
import org.santfeliu.matrix.ide.WorkflowPanel;
import org.santfeliu.workflow.Workflow;

/**
 *
 * @author real
 */
public class WorkflowGridAction extends BaseAction
{
  public WorkflowGridAction()
  {
  }

  @Override
  public void actionPerformed(ActionEvent event)
  {
    DocumentPanel panel = ide.getMainPanel().getActivePanel();
    if (panel instanceof WorkflowPanel)
    {
      WorkflowPanel workflowPanel = (WorkflowPanel)panel;
      Workflow workflow = workflowPanel.getWorkflow();

      WorkflowGridDialog dialog = new WorkflowGridDialog(ide);
      dialog.setGridSize(workflow.getGridSize());
      dialog.setNodeWidth(workflow.getNodeWidth());
      dialog.setNodeHeight(workflow.getNodeHeight());
      dialog.setGridVisible(workflow.isGridVisible());
      dialog.setGridEnabled(workflow.isGridEnabled());
      dialog.setLocationRelativeTo(workflowPanel);
      dialog.pack();
      if (dialog.showDialog())
      {
        addUndoableEdit(workflowPanel,
          dialog.getGridSize(),
          dialog.getNodeWidth(),
          dialog.getNodeHeight(),
          dialog.isGridVisible(),
          dialog.isGridEnabled());

        workflow.setGridSize(dialog.getGridSize());
        workflow.setNodeWidth(dialog.getNodeWidth());
        workflow.setNodeHeight(dialog.getNodeHeight());
        workflow.setGridVisible(dialog.isGridVisible());
        workflow.setGridEnabled(dialog.isGridEnabled());
        if (workflow == ide.getMainPanel().getEditObject())
        {
          ide.getMainPanel().updateEditObject(); // update
        }
        workflowPanel.updateGrid();
        workflowPanel.setModified(true);
      }
    }
  }

  @Override
  public void updateEnabled()
  {
    setEnabled(ide.getMainPanel().getActivePanel() instanceof WorkflowPanel);
  }

  protected void addUndoableEdit(final WorkflowPanel panel,
    final int gridSize, final int nodeWidth, final int nodeHeight,
    final boolean gridVisible, final boolean gridEnabled)
  {
    UndoableEdit edit = new AbstractUndoableEdit()
    {
      WorkflowPanel workflowPanel = panel;
      int oldGridSize = workflowPanel.getWorkflow().getGridSize();
      int oldNodeWidth = workflowPanel.getWorkflow().getNodeWidth();
      int oldNodeHeight = workflowPanel.getWorkflow().getNodeHeight();
      boolean oldGridVisible = workflowPanel.getWorkflow().isGridVisible();
      boolean oldGridEnabled = workflowPanel.getWorkflow().isGridEnabled();

      int newGridSize = gridSize;
      int newNodeWidth = nodeWidth;
      int newNodeHeight = nodeHeight;
      boolean newGridVisible = gridVisible;
      boolean newGridEnabled = gridEnabled;

      @Override
      public void undo()
      {
        super.undo();
        Workflow workflow = workflowPanel.getWorkflow();
        workflow.setGridSize(oldGridSize);
        workflow.setNodeWidth(oldNodeWidth);
        workflow.setNodeHeight(oldNodeHeight);
        workflow.setGridVisible(oldGridVisible);
        workflow.setGridEnabled(oldGridEnabled);
        workflowPanel.updateGrid();
      }

      @Override
      public void redo()
      {
        super.redo();
        Workflow workflow = workflowPanel.getWorkflow();
        workflow.setGridSize(newGridSize);
        workflow.setNodeWidth(newNodeWidth);
        workflow.setNodeHeight(newNodeHeight);
        workflow.setGridVisible(newGridVisible);
        workflow.setGridEnabled(newGridEnabled);
        workflowPanel.updateGrid();
      }
    };
    panel.getUndoManager().addEdit(edit);
  }
}
