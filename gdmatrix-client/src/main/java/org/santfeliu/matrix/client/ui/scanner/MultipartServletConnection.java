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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.santfeliu.matrix.client.util.MultipartUtility;

/**
 *
 * @author blanquepa
 */
public class MultipartServletConnection extends AbstractServletConnection
{
  private MultipartUtility multipartUtility;
  
  public MultipartServletConnection(String requestURL, String charset, Map properties)
          throws IOException
  {
    multipartUtility = new MultipartUtility(requestURL, charset, "MatrixClient agent");
    this.httpConn = multipartUtility.getHttpConn();
    
    Set keys = properties.keySet();
    for (Object key : keys)
    {
      if (((String)key).startsWith("scan_"))
      {
        multipartUtility.addFormField((String) key, (String) properties.get(key));
      }
    } 
    multipartUtility.addFilePart("file", "file.pdf");    
  } 

  @Override
  public OutputStream getOutputStream()
  {
    return multipartUtility.getOutputStream();
  }

  @Override
  public void flush()
  {
    try
    {
      multipartUtility.finish();
    }
    catch (IOException ex)
    {
      Logger.getLogger(MultipartServletConnection.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
}
