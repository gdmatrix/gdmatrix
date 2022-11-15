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
import java.util.List;
import javax.swing.Action;
import javax.swing.JFileChooser;
import org.santfeliu.matrix.ide.DocumentPanel;
import org.santfeliu.matrix.ide.MainPanel;
import org.santfeliu.matrix.ide.MatrixIDE;
import org.santfeliu.matrix.ide.PackagePanel;


/**
 *
 * @author realor
 */
public class SaveInPackageAction extends BaseAction
{
  public SaveInPackageAction()
  {
    this.putValue(Action.SMALL_ICON,
      loadIcon("/org/santfeliu/matrix/ide/resources/images/saveinpkg.gif"));
  }

  @Override
  public void actionPerformed(ActionEvent event)
  {
    try
    {
      DocumentPanel panel = getIDE().getMainPanel().getActivePanel();

      List<DocumentPanel> panels = getPackagePanels();

      PackagePanel pkgPanel = (PackagePanel)panels.get(0);

      File dir = pkgPanel.getUnpackDir();

      JFileChooser chooser = new JFileChooser();
      chooser.setDialogTitle("Save in package " + pkgPanel.getDisplayName());
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      chooser.setCurrentDirectory(dir);
      String displayName = panel.getDisplayName();
      if (!"new".equals(displayName))
      {
        chooser.setSelectedFile(new File(dir, displayName));
      }

      int result = chooser.showSaveDialog(ide);
      if (result == JFileChooser.APPROVE_OPTION)
      {
        File file = chooser.getSelectedFile();

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
    catch (Exception ex)
    {
      MatrixIDE.log(ex);
    }
  }

  @Override
  public void updateEnabled()
  {
    DocumentPanel panel = getIDE().getMainPanel().getActivePanel();
    setEnabled(panel != null && !(panel instanceof PackagePanel)
      && !getPackagePanels().isEmpty());
  }

  public List<DocumentPanel> getPackagePanels()
  {
    MainPanel mainPanel = getIDE().getMainPanel();

    return mainPanel.getPanels(panel -> panel instanceof PackagePanel);
  }
}
