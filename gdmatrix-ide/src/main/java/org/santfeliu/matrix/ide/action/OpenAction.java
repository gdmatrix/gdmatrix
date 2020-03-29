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
import java.io.File;
import java.io.FileInputStream;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.santfeliu.matrix.ide.DocumentPanel;
import org.santfeliu.matrix.ide.DocumentType;
import org.santfeliu.matrix.ide.Options;

/**
 *
 * @author realor
 */
public class OpenAction extends BaseAction
{  
  public OpenAction()
  {
    this.putValue(Action.SMALL_ICON, 
      loadIcon("/org/santfeliu/matrix/ide/resources/images/open.gif"));    
  }
  
  @Override
  public void actionPerformed(ActionEvent event)
  {
    try
    {
      JFileChooser chooser = new JFileChooser();
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      
      File dir = Options.getLastDirectory();
      chooser.setCurrentDirectory(dir);

      int result = chooser.showOpenDialog(ide);
      if (result == JFileChooser.APPROVE_OPTION)
      {
        File file = chooser.getSelectedFile();
        dir = file.getParentFile();
        Options.setLastDirectory(dir);
        
        String filename = file.getName();
        int index = filename.lastIndexOf(".");
        if (index != -1)
        {
          String name = filename.substring(0, index);
          String extension = filename.substring(index + 1);        
          DocumentType documentType = 
            ide.getMainPanel().getDocumentType(extension);
          if (documentType != null)
          {
            DocumentPanel panel = 
              ide.getMainPanel().createPanel(documentType);
            FileInputStream is = new FileInputStream(file);
            try
            {
              panel.setDisplayName(name);
              panel.setDirectory(file.getParentFile());              
              panel.open(is); // read from file
              panel.setConnectionUrl(null);
              ide.getMainPanel().addPanel(panel); // add panel to framework
              panel.setModified(false);
            }
            finally
            {
              is.close();
            }
          }
          else throw new Exception("Unsupported file type");
        }
        else throw new Exception("Unknow file type");
      }
    }
    catch (Exception ex)
    {
      JOptionPane.showMessageDialog(
        ide.getMainPanel(),
        ex.getMessage(), 
        "Open file", JOptionPane.ERROR_MESSAGE);
    }
  }
}
