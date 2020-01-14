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
package org.santfeliu.ant.logs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 *
 * @author realor
 */
public class ISAWebLogLoaderTask extends LogFileLoaderTask
{
  BufferedReader reader = null;
  String separator = "\t";

  public String getSeparator()
  {
    return separator;
  }

  public void setSeparator(String separator)
  {
    this.separator = separator;
  }

  @Override
  protected void initReader(File file) throws Exception
  {
    reader = new BufferedReader(
      new InputStreamReader(new FileInputStream(file)));
  }

  @Override
  protected boolean readEntry(Map<String, String> entryMap) throws Exception
  {
    String line = reader.readLine();
    while (line != null && line.startsWith("#"))
    {
      line = reader.readLine(); // skip comments
    }
    if (line == null) return false;

    String columns[] = line.split(separator);
    String ip, userId, dateTime, refUri, uri, bytes, mimeType, host, result;

    if (columns.length > 0) 
    {
      ip = columns[0];
      entryMap.put(IP_FIELD, ip);
      if (columns.length > 1) 
      {
        userId = columns[1];
        entryMap.put(USER_FIELD, getUserId(userId));
        if (columns.length > 5) 
        {
          dateTime = columns[4] + "-" + columns[5];
          entryMap.put(DATETIME_FIELD, dateTime);
          if (columns.length > 8)
          {
            refUri = columns[8];
            entryMap.put("refUri", refUri);
            if (columns.length > 9)
            {
              host = columns[9];
              entryMap.put("host", host);
              if (columns.length > 14)
              {
                bytes = columns[14];
                try
                {
                  Integer.parseInt(bytes);
                  entryMap.put("bytes", bytes);  
                }
                catch (NumberFormatException ex) { }
                if (columns.length > 18)
                {
                  uri = columns[18];
                  entryMap.put(ACTION_FIELD, uri);
                  if (columns.length > 19)
                  {
                    mimeType = columns[19];
                    entryMap.put("mimeType", getMimeType(mimeType));
                    if (columns.length > 28)
                    {
                      result = columns[28];
                      entryMap.put("result", result);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return true;
  }

  @Override
  protected void closeReader()
  {
    try
    {
      reader.close();
    }
    catch (Exception ex)
    {
    }
  }
  
  private String getUserId(String userId)
  {
    if (userId != null && userId.contains("\\"))
    {
      return userId.substring(userId.indexOf("\\") + 1);
    }
    return userId;
  }

  private String getMimeType(String mimeType)
  {
    if (mimeType != null)
    {
      int index = mimeType.indexOf(";");
      if (index > 0)
        mimeType = mimeType.substring(0, index);
    }
    return mimeType;
  }

}
