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

import java.io.IOException;
import java.net.MalformedURLException;
import javax.swing.SwingUtilities;
import org.santfeliu.matrix.client.Command;
import org.santfeliu.matrix.client.ui.scanner.ApplicationServletConnection;
import org.santfeliu.matrix.client.ui.scanner.MultipartServletConnection;
import org.santfeliu.matrix.client.ui.scanner.ServletConnection;
import org.santfeliu.matrix.client.ui.scanner.ScanFrame;

/**
 *
 * @author blanquepa
 */
public class ScanDocumentCommand extends Command
{
  private boolean acquired = false;
  
  private String servletUrl;
  
  protected void init() throws MalformedURLException
  {
    servletUrl = (String)properties.get("scanServletUrl");
  }
  
  @Override
  protected void doWork() throws Exception 
  {
    init();

    final ScanDocumentCommand command = this;

    try
    {
      SwingUtilities.invokeLater(new Runnable()
      {
        @Override
        public void run()
        {
          ScanFrame frame = new ScanFrame();
          frame.setCommand(command);
          frame.showFrame();
        }
      });
      
      while (!acquired)
      {
        Thread.sleep(1000);
      }
      System.out.println("Command scan terminated");
    }
    finally
    {
      acquired = false;
    }
  }
  
  public void setAcquired(boolean acquired)
  {
    this.acquired = acquired;
  }

  public String getServletUrl() 
  {
    return servletUrl;
  }
  
  public ServletConnection getConnection() throws IOException 
  {
    String multiformConnectionDisabled = 
      (String)properties.get("multipartConnectionDisabled");
    if (multiformConnectionDisabled != null && multiformConnectionDisabled.equalsIgnoreCase("true"))
      return new ApplicationServletConnection(servletUrl, properties);
    else
      return new MultipartServletConnection(getServletUrl(), "UTF-8", properties);
  }
}
