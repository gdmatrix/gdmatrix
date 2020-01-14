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
package org.santfeliu.matrix.client.ui.scanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.santfeliu.matrix.client.Command;
import org.santfeliu.matrix.client.cmd.doc.ScanDocumentCommand;

/**
 *
 * @author blanquepa
 */
public abstract class AbstractServletConnection implements ServletConnection
{
  protected HttpURLConnection httpConn;

  @Override
  public abstract OutputStream getOutputStream();

  @Override
  public abstract void flush();

  @Override
  public void parseResponse(ScanDocumentCommand command) throws IOException
  {
    ResourceBundle bundle = java.util.ResourceBundle.getBundle(
      "org/santfeliu/matrix/client/ui/scanner/resources/ScanFrame");    
    
    int responseCode = httpConn.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_OK)
    {
      // reads server's response
      BufferedReader reader = new BufferedReader(new InputStreamReader(
        httpConn.getInputStream()));
      String response = reader.readLine();
      Logger.getLogger(getClass().getName()).info("Server's response: " + response);
      String docId = httpConn.getHeaderField("docId");          
      command.getProperties().put(Command.RESULT, docId);
    }
    else
    {
      //TODO send error stream
      BufferedReader reader = new BufferedReader(new InputStreamReader(
        httpConn.getErrorStream()));
      String strResponse = reader.readLine();

      if (strResponse != null && strResponse.startsWith("<html>"))
      {
        //HTTP Status XXX - ERROR
        try
        {
          Pattern pattern = Pattern.compile("<h1>(.+?)</h1>");
          Matcher matcher = pattern.matcher(strResponse);        
          matcher.find();
          String error = matcher.group(1);
          error = error.substring(error.lastIndexOf("-") + 1).trim();
          command.getProperties().put(Command.EXCEPTION, bundle.getString("Error." + error));
        }
        catch (Exception ex)
        {
          command.getProperties().put(Command.EXCEPTION,
            httpConn.getResponseCode() + " " + httpConn.getResponseMessage()); 
        }
      }          
      else
        command.getProperties().put(Command.EXCEPTION, strResponse);
    }
  }
  
}
