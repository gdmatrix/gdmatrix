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

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 *
 * @author realor
 */
public class ServletClient
{
  private URL url;
  private String sessionId;

  public ServletClient(URL url, String sessionId)
  {
    this.url = url;
    this.sessionId = sessionId;
  }

  public Map loadDocument(String docId) throws Exception
  {
    return (Map)sendObjects("loadDocument", docId);
  }

  public Map storeDocument(Map document) throws Exception
  {
    return (Map)sendObjects("storeDocument", document);
  }

  public void lockDocument(String docId) throws Exception
  {
    sendObjects("lockDocument", docId);
  }

  public void unlockDocument(String docId) throws Exception
  {
    sendObjects("unlockDocument", docId);
  }

  public Object sendObjects(String operation, Object ... parameters)
    throws Exception
  {
    Object result = null;
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    try
    {
      conn.setRequestMethod("POST");
      conn.setDoInput(true);
      conn.setDoOutput(true);
      conn.setAllowUserInteraction(false);
      conn.setUseCaches(false);
      if (sessionId != null)
      {
        conn.setRequestProperty("Cookie", "JSESSIONID=" + sessionId);
      }

      // request
      ObjectOutputStream out = new ObjectOutputStream(conn.getOutputStream());
      try
      {
        out.writeObject(operation);
        for (int i = 0; i < parameters.length; i++)
        {
          out.writeObject(parameters[i]);
        }
        out.flush();
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
    }
    finally
    {
      conn.disconnect();
    }
    return result;
  }

  public int sendBytes(String contentId, InputStream is, int size)
    throws Exception
  {
    int sent = 0;
    URL putUrl = new URL(url.toString() + "/" + contentId);
    HttpURLConnection conn = (HttpURLConnection)putUrl.openConnection();
    try
    {
      conn.setRequestMethod("PUT");
      conn.setDoInput(true);
      conn.setDoOutput(true);
      conn.setAllowUserInteraction(false);
      conn.setUseCaches(false);
      // request
      OutputStream out = conn.getOutputStream();
      try
      {
        boolean eof = false;
        while (!eof && sent < size)
        {
          int b = is.read();
          if (b == -1)
          {
            eof = true;
          }
          else
          {
            out.write(b);
            sent++;
          }
        }
        out.flush();
      }
      finally
      {
        out.close();
      }

      // read response
      int code = conn.getResponseCode();
      InputStream in  = conn.getInputStream();
      try
      {
        while (in.read() != -1)
        {
        }
        if (code != 200) throw new Exception("HTTP ERROR " + code);
      }
      finally
      {
        in.close();
      }
    }
    finally
    {
      conn.disconnect();
    }
    return sent;
  }
}
