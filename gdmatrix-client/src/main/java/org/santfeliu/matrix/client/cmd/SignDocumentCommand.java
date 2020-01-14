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
package org.santfeliu.matrix.client.cmd;

import java.util.Map;
import javax.swing.SwingUtilities;
import org.santfeliu.matrix.client.Command;
import org.santfeliu.matrix.client.ui.microsigner.MicroSigner;

/**
 *
 * @author realor
 */
public class SignDocumentCommand extends Command
{
  public static final String ERROR_PREFIX = "ERROR:";  
  
  MicroSigner microSigner;
  
  @Override
  protected void doWork() throws Exception
  {
    SwingUtilities.invokeAndWait(new Runnable()
    {
      @Override
      public void run()
      {
        Map properties = getProperties();
        String sigId = (String)properties.get("sigId");
        String url = (String)properties.get("signatureServletUrl");
        System.out.println("SigId: " + sigId);
        System.out.println("SignatureServletUrl: " + url);

        microSigner = new MicroSigner();
        microSigner.setSigId(sigId);
        microSigner.setSignatureServletUrl(url);
        microSigner.showFrame();
      }
    });
    microSigner.waitForTermination();
    
    String result = microSigner.getSignResult();
    if (result.indexOf(ERROR_PREFIX) == -1) // NO ERROR: OK or CANCEL
      properties.put(RESULT, result);    
    else
    {
      String message = result.substring(ERROR_PREFIX.length());      
      properties.put(EXCEPTION, message);
    }
  }
}
