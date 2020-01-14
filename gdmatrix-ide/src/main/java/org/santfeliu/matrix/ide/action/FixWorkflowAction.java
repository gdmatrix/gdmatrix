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
import java.net.URL;
import java.util.List;
import javax.swing.JOptionPane;
import org.santfeliu.matrix.ide.DocumentPanel;
import org.santfeliu.matrix.ide.Options;
import org.santfeliu.matrix.ide.WorkflowPanel;
import org.santfeliu.matrix.ide.WorkflowFixerDialog;
import org.santfeliu.workflow.Workflow;
import org.santfeliu.workflow.util.WorkflowFixer;

/**
 *
 * @author realor
 */
public class FixWorkflowAction extends BaseAction
{
  public FixWorkflowAction()
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

      String fixRulesURL = Options.get("workflowFixRulesURL");
      if (fixRulesURL != null && fixRulesURL.trim().length() > 0)
      {
        try
        {
          URL url = new URL(fixRulesURL);
          WorkflowFixer fixer = WorkflowFixer.getInstance(url);
          List<WorkflowFixer.Issue> issues =
            fixer.check(workflow.getNodes());

          WorkflowFixerDialog dialog = new WorkflowFixerDialog(ide, false);
          dialog.init(workflowPanel, issues);
          dialog.setSize(760, 400);
          dialog.setLocationRelativeTo(ide);
          dialog.setVisible(true);
        }
        catch (Exception ex)
        {
          JOptionPane.showMessageDialog(ide, ex.toString(),
            "Workflow fix", JOptionPane.ERROR_MESSAGE);
        }
      }
      else
      {
        JOptionPane.showMessageDialog(ide, "Fix rules not defined.",
          "Workflow fix", JOptionPane.WARNING_MESSAGE);
      }
    }
  }

  @Override
  public void updateEnabled()
  {
    setEnabled(ide.getMainPanel().getActivePanel() instanceof WorkflowPanel);
  }
}
