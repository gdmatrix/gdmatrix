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
package org.santfeliu.elections.swing;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 *
 * @author realor
 */
public class ElectionsMonitor extends JFrame
{
  private ElectionsPanel electionsPanel = new ElectionsPanel();

  public ElectionsMonitor()
  {
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
    this.setSize(new Dimension(840, 600));
    this.setTitle("Monitor d'eleccions");
    this.getContentPane().add(electionsPanel);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = getSize();
    if (frameSize.height > screenSize.height)
    {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width)
    {
      frameSize.width = screenSize.width;
    }
    setLocation((screenSize.width - frameSize.width) / 2,
                (screenSize.height - frameSize.height) / 2);
  }

  public static void main(String[] args)
  {
    try
    {
      //Default Values
      String propertiesFileURL = "http://www.santfeliu.cat/documents/101303";

      if (args.length > 0)
        propertiesFileURL = args[0];

      URL propertiesFile = new URL(propertiesFileURL);
      Properties properties = new Properties();
      InputStream is = propertiesFile.openStream();
      properties.load(is);
      is.close();

      final Properties configProperties = properties;

      SwingUtilities.invokeLater(new Runnable()
      {
        public void run()
        {
          ElectionsMonitor electionsMonitor = new ElectionsMonitor();
          electionsMonitor.electionsPanel.setConfigProperties(configProperties);
          String title = configProperties.getProperty("appTitle");
          electionsMonitor.setTitle((title != null ? title : "ElectionsMonitor"));
          electionsMonitor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          electionsMonitor.electionsPanel.start();
          electionsMonitor.setVisible(true);
        }
      });
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
