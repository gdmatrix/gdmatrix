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
import java.io.FileOutputStream;

import javax.swing.Action;
import org.santfeliu.matrix.ide.DocumentPanel;


/**
 *
 * @author unknown
 */
public class SaveAction extends BaseAction
{
  public SaveAction()
  {
    this.putValue(Action.SMALL_ICON, 
      loadIcon("/org/santfeliu/matrix/ide/resources/icon/save.gif"));
  }
  
  @Override
  public void actionPerformed(ActionEvent event)
  {
    try
    {
      DocumentPanel panel = getIDE().getMainPanel().getActivePanel();
      String name = panel.getDisplayName();
      File dir = panel.getDirectory();
      if (panel != null && name != null && dir != null)
      {
        String extension = panel.getDocumentType().getExtension();
        File file = new File(dir, name + "." + extension);
        FileOutputStream fos = new FileOutputStream(file);
        try
        {
          panel.setDisplayName(name);
          panel.save(fos);
          panel.setConnectionUrl(null);
          panel.setDirectory(file.getParentFile());
          panel.setModified(false);
          getIDE().getMainPanel().updateActions();
        }
        finally
        {
          fos.close();
        }
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  @Override
  public void updateEnabled()
  {
    DocumentPanel panel = getIDE().getMainPanel().getActivePanel();
    setEnabled(panel != null && panel.getDisplayName() != null &&
      panel.getDirectory() != null && panel.isModified());
  }  
}
