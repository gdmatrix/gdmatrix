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
import javax.swing.Action;
import org.matrix.doc.Document;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.node.FormNode;
import org.santfeliu.matrix.ide.DocumentPanel;
import org.santfeliu.matrix.ide.MainPanel;
import org.santfeliu.matrix.ide.WorkflowPanel;
import org.santfeliu.workflow.swing.graph.WorkflowVertex;
import org.santfeliu.matrix.ide.DocumentType;

/**
 *
 * @author unknown
 */
public class OpenDesignAction extends BaseAction
{
  public OpenDesignAction()
  {
    this.putValue(Action.SMALL_ICON, 
                  loadIcon("/org/santfeliu/matrix/ide/resources/icon/form.gif"));
  }

  @Override
  public void actionPerformed(ActionEvent event)
  {
    DocumentPanel panel = ide.getMainPanel().getActivePanel();
    if (panel instanceof WorkflowPanel)
    {
      WorkflowPanel wfPanel = (WorkflowPanel) panel;
      Object[] cells = wfPanel.getGraph().getSelectionCells();
      for (Object cell: cells)
      {
        if (cell instanceof WorkflowVertex)
        {
          WorkflowVertex vertex = (WorkflowVertex) cell;
          WorkflowNode node = vertex.getNode();
          if (node instanceof FormNode)
          {
            FormNode formNode = (FormNode) node;
            String formType =formNode.getFormType();
            if ("custom".equals(formType))
            {
              String type = (String)formNode.getParameters().get("type");
              String ref = (String)formNode.getParameters().get("ref");
              if ("form".equals(type))
              {
                openDocument(type, ref);
              }
              else if ("html".equals(type))
              {
                openDocument(type, ref);
              }
            }
            else if ("dynamic".equals(formType))
            {
              String selector = 
                (String)formNode.getParameters().get("selector");
              if (selector != null)
              {
                // WARN: assuming selector prefixes are immutable.
                // TODO: Use FormFactory instead.
                if (selector.startsWith("html:"))
                {
                  String ref = selector.substring(5);
                  openDocument("html", ref);
                }
                else if (selector.startsWith("form:"))
                {
                  String ref = selector.substring(5);
                  openDocument("form", ref);
                }
              }
            }
          }
        }
      }
    }
  }

  private void openDocument(String type, String ref)
  {
    MainPanel mainPanel = ide.getMainPanel();
    DocumentType documentType = mainPanel.getDocumentType(type);
    try
    {
      DocumentManagerClient client = mainPanel.getDocumentManagerClient();
      String docTypeId = documentType.getDocTypeId();
      Document document = client.loadDocumentByName(docTypeId, 
        documentType.getPropertyName(), ref, null, 0);

      if (document != null)
      {
        try
        {
          String docId = document.getDocId();
          String language = document.getLanguage();
          Integer version = document.getVersion();
          mainPanel.openDocumentFromDM(documentType, docId, language, version);
        }
        catch (Exception ex)
        {
        }
      }
    }
    catch (Exception ex)
    {
    }
  }

  @Override
  public void updateEnabled()
  {
    setEnabled(ide.getMainPanel().getActivePanel() instanceof WorkflowPanel);
  }
}
