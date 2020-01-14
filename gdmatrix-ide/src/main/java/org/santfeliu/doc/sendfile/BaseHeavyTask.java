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
package org.santfeliu.doc.sendfile;

import java.net.URL;
import java.text.DecimalFormat;
import javax.swing.JOptionPane;
import org.santfeliu.swing.HeavyTask;

/**
 *
 * @author realor
 */
public abstract class BaseHeavyTask extends HeavyTask
{
  protected SendFileApplet applet;
  protected ServletClient client;
  private String errorMessage;
  
  public BaseHeavyTask(SendFileApplet applet)
  {
    this.applet = applet;
    privileged = true;
    try
    {
      URL url = new URL(applet.getServletURL());
      System.out.println("Connecting to " + url);
      client = new ServletClient(url, applet.getSessionId());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  // Utility methods

  public void showError(String message)
  {
    errorMessage = message;
    try
    {
      invokeAndWait("showError");
    }
    catch (Exception exc)
    {
    }
  }

  public void showError(Exception ex)
  {
    ex.printStackTrace();
    if (ex.getMessage() != null)
    {
      showError(applet.getLocalizedMessage(ex.getMessage()));
    }
    else
    {
      showError(ex.toString());
    }
  }

  public void showError()
  {
    JOptionPane.showMessageDialog(null, errorMessage,
      "ERROR", JOptionPane.ERROR_MESSAGE);
  }

  public String getSizeString(long bytes)
  {
    String s;    
    DecimalFormat df = new DecimalFormat("###,###,###.##");
    if (bytes < 1024)
    {
      s = String.valueOf(bytes) + " bytes";
    }
    else if (bytes < 1024 * 1024) // < 1 Mb
    {
      double num = (double)bytes;
      s = df.format(num / 1024.0) + " Kb";
    }
    else
    {
      double num = (double)bytes;
      s = df.format(num / (1024.0 * 1024.0)) + " Mb";
    }
    return s;
  }
}
