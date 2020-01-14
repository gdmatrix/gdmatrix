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
package org.santfeliu.matrix.client.ui.microsigner;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author unknown
 */
public class SignatureServletClient
{
  private String endPoint;

  public SignatureServletClient()
  {
  }

  public void setTargetEndPointAddress(String endPoint)
  {
    this.endPoint = endPoint;
  }

  public String getTargetEndPointAddress()
  {
    return endPoint;
  }

  public byte[] addSignature(String sigId, byte[] certData)
    throws Exception
  {
    return (byte[])invoke("addSignature", 
      new Object[]{sigId, certData});
  }

  public String endSignature(String sigId, byte[] signatureData)
    throws Exception
  {
    return (String)invoke("endSignature", 
      new Object[]{sigId, signatureData});
  }

  public String abortSignature(String sigId)
    throws Exception
  {
    return (String)invoke("abortSignature", new Object[]{sigId});
  }

  private Object invoke(String operation, Object[] parameters)
    throws Exception
  {   
    Object result = null;    
    URL url = new URL(endPoint);
    URLConnection conn = url.openConnection();
    conn.setDoInput(true);
    conn.setDoOutput(true);
    // request
    ObjectOutputStream out = new ObjectOutputStream(conn.getOutputStream());
    try
    {
      out.writeObject(operation);
      for (int i = 0; i < parameters.length; i++)
      {
        out.writeObject(parameters[i]);
      }
    }
    finally
    {
      out.close();
    }
    // response
    ObjectInputStream in  = new ObjectInputStream(conn.getInputStream());
    try
    {
      result = in.readObject();
      if (result instanceof Exception) throw (Exception)result;
    }
    finally
    {
      in.close();
    }
    return result;
  }
}
