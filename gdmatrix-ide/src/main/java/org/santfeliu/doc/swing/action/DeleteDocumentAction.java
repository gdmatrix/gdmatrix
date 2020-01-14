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
package org.santfeliu.doc.swing.action;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.matrix.doc.DocumentConstants;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.doc.State;
import org.santfeliu.doc.swing.DeleteChooserDialog;
import org.santfeliu.doc.swing.DocumentSimplePanel;
import org.santfeliu.swing.Utilities;

/**
 *
 * @author unknown
 */
public class DeleteDocumentAction extends AbstractAction
{
  private DocumentSimplePanel documentPanel;
  
  public DeleteDocumentAction(DocumentSimplePanel documentPanel)
  {
    this.documentPanel = documentPanel;
    this.putValue(Action.NAME, documentPanel.getLocalizedText("delete"));
  }

  public void actionPerformed(ActionEvent e)
  {
    try
    {
      String docId = documentPanel.getDocId();
      //String language = documentPanel.getDocLanguage();
      DeleteChooserDialog dialog = openDeleteChooser();
      
      documentPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      String selectedOption = dialog.getSelectedOption();
      if (DeleteChooserDialog.LOGICAL.equals(selectedOption))
      {
        Document document = new Document();
        document.setDocId(docId);
        document.setIncremental(true);
        document.setState(State.DELETED);
        if (documentPanel.isDocCreateNewVersion())
        {
          document.setVersion(-1);
        }
        else
        {
          document.setVersion(Integer.parseInt(documentPanel.getDocVersion()));
        }
        Content content = new Content();
        content.setContentId(documentPanel.getDocUUID());
        document.setContent(content);
        document = documentPanel.getClient().storeDocument(document);
        documentPanel.loadDocument(docId, document.getVersion());
      }
      else if (DeleteChooserDialog.CURRENT_VERSION.equals(selectedOption))
      {
        documentPanel.getClient().removeDocument(docId, 
          Integer.parseInt(documentPanel.getDocVersion())); //Current version
        documentPanel.loadDocument(docId, DocumentConstants.LAST_VERSION);
      }
      else if (DeleteChooserDialog.PURGE.equals(selectedOption))
      {
        documentPanel.getClient().removeDocument(docId, -3); //Purge
        documentPanel.loadDocument(docId, DocumentConstants.LAST_VERSION);
      }
      else if (DeleteChooserDialog.ALL_VERSIONS.equals(selectedOption))
      {
        documentPanel.getClient().removeDocument(docId, -2); //All versions
        documentPanel.loadDocument(docId, DocumentConstants.LAST_VERSION);
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      documentPanel.showError(ex);      
    }
    finally
    {
      documentPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
  }
  
  public boolean isEnabled()
  {
//    return ((!documentPanel.isDocumentLocked() || documentPanel.isDocumentLockedByUser()) && 
//      documentPanel.isDocumentActive() && documentPanel.existsDocument());
    return ((!documentPanel.isDocumentLocked() || 
      documentPanel.isDocumentLockedByUser()) && 
      documentPanel.existsDocument());
  }
  
  private DeleteChooserDialog openDeleteChooser()
  {
    DeleteChooserDialog deleteChooser = new DeleteChooserDialog();
    Utilities.centerWindow(documentPanel, deleteChooser);
    deleteChooser.setVisible(true);
    return deleteChooser;
  }

}
