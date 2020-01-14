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
import java.io.File;
import java.io.FileOutputStream;
import javax.activation.DataHandler;
import javax.activation.FileTypeMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import org.matrix.doc.Content;
import org.santfeliu.doc.swing.DocumentBasePanel;
import org.santfeliu.util.MimeTypeMap;

/**
 *
 * @author unknown
 */
public class SaveAsDocumentAction extends AbstractAction
{
  private File saveDir;
  private DocumentBasePanel documentPanel;
  
  public SaveAsDocumentAction(DocumentBasePanel documentPanel)
  {
    this.documentPanel = documentPanel;
    this.putValue(Action.NAME, documentPanel.getLocalizedText("saveAs"));    
  }

  public void actionPerformed(ActionEvent e)
  {
    JFileChooser dialog = new JFileChooser();
    if (saveDir != null)
    {
      dialog.setCurrentDirectory(saveDir);
    }
    int result = dialog.showDialog(documentPanel,
      documentPanel.getLocalizedText("save"));
    if (result == JFileChooser.APPROVE_OPTION)
    {
      File file = dialog.getSelectedFile();
      if (file != null)
      {
        saveDir = file.getParentFile();
        try
        {
          documentPanel.setCursor(
            Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          DataHandler dh = null;
          Content content =
            documentPanel.getClient().loadContent(documentPanel.getDocUUID());

          dh = content.getData();
          String filename = file.getAbsolutePath();
          MimeTypeMap typeMap =
            (MimeTypeMap)FileTypeMap.getDefaultFileTypeMap();
          String extension = typeMap.getExtension(content.getContentType());
          if (extension != null && filename.indexOf("." + extension) < 0) 
            filename += "." + extension;
          FileOutputStream fos = new FileOutputStream(filename);
          dh.writeTo(fos);
          fos.close();
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
          documentPanel.showError(ex);
        }
        finally
        {
          documentPanel.setCursor(
            Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      }
    }  
  }
}
