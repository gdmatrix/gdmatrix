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
package org.santfeliu.matrix.client.ui.microsigner.action;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import org.santfeliu.matrix.client.ui.microsigner.KeyStoreNode;
import org.santfeliu.matrix.client.ui.microsigner.MainPanel;
import org.santfeliu.matrix.client.ui.microsigner.MicroSigner;
import org.santfeliu.matrix.client.ui.microsigner.SecretDialog;

/**
 *
 * @author realor
 */
public class AddPKCS12Action extends AbstractAction
{
  private final MainPanel mainPanel;

  public AddPKCS12Action(MainPanel mainPanel)
  {
    super.putValue(Action.NAME, MicroSigner.getLocalizedText("AddPKCS12"));
    this.mainPanel = mainPanel;
  }
  
  @Override
  public void actionPerformed(ActionEvent event)
  {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle(MicroSigner.getLocalizedText("OpenPKCS12"));
    fileChooser.setFileFilter(new FileFilter()
    {
      public boolean accept(File file)
      {
        if (file.isDirectory()) return true;
        String name = file.getName().toLowerCase();
        return name.endsWith(".p12") || name.endsWith(".pfx");
      }
      
      @Override
      public String getDescription()
      {
        return "PKCS#12 (*.p12, *.pfx)";
      }
    });
    int option = fileChooser.showOpenDialog(mainPanel);
    if (option == JFileChooser.APPROVE_OPTION)
    {
      File file = fileChooser.getSelectedFile();
      SecretDialog dialog = new SecretDialog(mainPanel.getFrame());
      dialog.setText(MicroSigner.getLocalizedText("EnterPKCS12Password"));
      dialog.setLocationRelativeTo(mainPanel);
      dialog.setVisible(true);
      String password = dialog.getSecret();
      try
      {
        KeyStoreNode ksNode = MicroSigner.loadKeyStore(
          "com.sun.net.ssl.internal.ssl.Provider",
          "PKCS12",
          file.getAbsolutePath(),
          password, null);
        mainPanel.addKeyStoreNode(ksNode);
      }
      catch (Throwable ex)
      {
        JOptionPane.showMessageDialog(null, ex.toString(), 
          "ERROR", JOptionPane.ERROR_MESSAGE);
      }
    }
  }
}
