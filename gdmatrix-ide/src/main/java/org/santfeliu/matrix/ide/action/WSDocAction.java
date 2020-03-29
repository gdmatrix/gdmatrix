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

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.net.URL;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.santfeliu.matrix.ide.ConnectionParameters;

/**
 *
 * @author realor
 */
public class WSDocAction extends BaseAction
{
  public WSDocAction()
  {
    this.putValue(Action.SMALL_ICON,
      loadIcon("/org/santfeliu/matrix/ide/resources/images/wsdoc.gif"));
  }

  @Override
  public void actionPerformed(ActionEvent event)
  {
    if (Desktop.isDesktopSupported())
    {
      ConnectionParameters params =
        ide.getMainPanel().getConnectionPanel().getSelectedConnection();
      if (params != null)
      {
        try
        {
          String urlString = params.getURL();
          if (urlString != null)
          {
            URL urlBase = new URL(urlString);
            URL url = new URL(urlBase, "wsdoc");
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(url.toURI());
          }
        }
        catch (Exception ex)
        {
        }
      }
      else
      {
        JOptionPane.showMessageDialog(ide, "No connections defined!",
          "WS documentation", JOptionPane.WARNING_MESSAGE);
      }
    }
  }
}
