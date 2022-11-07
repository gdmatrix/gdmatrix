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
import javax.swing.JFileChooser;
import org.santfeliu.matrix.ide.DocumentPanel;
import org.santfeliu.matrix.ide.MainPanel;
import org.santfeliu.matrix.ide.MatrixIDE;
import org.santfeliu.matrix.ide.Options;

/**
 *
 * @author realor
 */
public class SaveAsAction extends BaseAction
{
  public SaveAsAction()
  {
    this.putValue(Action.SMALL_ICON,
      loadIcon("/org/santfeliu/matrix/ide/resources/images/saveas.gif"));
  }

  @Override
  public void actionPerformed(ActionEvent event)
  {
    try
    {
      DocumentPanel panel = getIDE().getMainPanel().getActivePanel();
      if (panel != null)
      {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        File dir = panel.getDirectory();
        if (dir == null)
        {
          dir = Options.getLastDirectory();
        }
        if (dir != null) chooser.setCurrentDirectory(dir);

        int result = chooser.showSaveDialog(ide);
        if (result == JFileChooser.APPROVE_OPTION)
        {
          File file = chooser.getSelectedFile();
          dir = file.getParentFile();
          Options.setLastDirectory(dir);

          String filename = file.getName();
          String name;
          String extension = panel.getDocumentType().getExtension();
          int index = filename.indexOf(".");
          if (index == -1) // no extension especified
          {
            name = filename;
          }
          else // extension especified, ignore it
          {
            name = filename.substring(0, index);
          }
          file = new File(file.getParentFile(), name + "." + extension);

          try (FileOutputStream fos = new FileOutputStream(file))
          {
            panel.setDisplayName(name);
            panel.save(fos);
            panel.setConnectionUrl(null);
            panel.setDirectory(file.getParentFile());
            panel.setModified(false);
            MainPanel mainPanel = getIDE().getMainPanel();
            mainPanel.updateActions();
            mainPanel.showStatus(panel);
          }
        }
      }
    }
    catch (Exception ex)
    {
      MatrixIDE.log(ex);
    }
  }

  @Override
  public void updateEnabled()
  {
    setEnabled(ide.getMainPanel().getActivePanel() != null);
  }
}
