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
package org.santfeliu.matrix.client.cmd.doc;

import java.net.MalformedURLException;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.matrix.doc.ContentInfo;
import static org.santfeliu.matrix.client.Command.RESULT;
import org.santfeliu.matrix.client.ui.MessageWindow;

/**
 *
 * @author blanquepa
 */
public class UpdateDocumentCommand extends SendDocumentCommand
{
  private String docId;
  
  @Override
  protected void init() throws MalformedURLException
  {
    super.init();
    docId = (String)properties.get("docId");
  }
  
  @Override
  public void doWork() throws Exception
  {
    init();
    
    document = docClient.loadDocument(docId, 0, ContentInfo.METADATA);
    
    boolean doSave = askDocumentProperties();

    if (doSave)
    {
      if (file != null && !validateFileSize(file))
          throw new Exception("INVALID_FILE_SIZE");
      MessageWindow messageWindow = new MessageWindow();
      messageWindow.showWindow("Desant document...");
      try
      {
        Logger.getLogger(getClass().getName()).info("Saving document");
        saveDocument();
        Logger.getLogger(getClass().getName()).info("Done " + document.getContent().getContentId());
        properties.put(RESULT, document.getDocId());
      }
      catch (Exception ex)
      {
        messageWindow.hideWindow();
        JOptionPane.showMessageDialog(null, "S'ha produït un error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }        
      finally
      {
        messageWindow.hideWindow();
      }
      Logger.getLogger(getClass().getName()).info("Document sent.");      
    }
    
    release();
  }
}
