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
package org.santfeliu.feed.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author lopezrj
 */
public abstract class JsonFeedReader
{  
  protected FeedUtils.FeedReading feedReading;
  
  public JsonFeedReader(FeedUtils.FeedReading feedReading)
  { 
    this.feedReading = feedReading;
  }

  protected abstract String getJsonCode() throws Exception;
  protected abstract List<JsonEntry> getEntries(String json) throws Exception;
  protected abstract FeedUtils.Row getRow(JsonEntry entry);
  
  public List<FeedUtils.Row> getRowList() throws Exception
  {
    List<FeedUtils.Row> result = new ArrayList();    
    String json = getJsonCode();
    List<JsonEntry> entries = getEntries(json);
    for (int iEntry = 0; 
      iEntry < entries.size() && result.size() < feedReading.getRowCount(); 
      iEntry++)
    {
      JsonEntry entry = entries.get(iEntry);
      result.add(getRow(entry));
    }
    return result;
  }    
  
  protected String readInputStream(InputStream is) throws IOException
  {
    return readInputStream(is, "ISO-8859-1");
  }
  
  protected String readInputStream(InputStream is, String charset) throws IOException
  {
    ByteArrayOutputStream os = null;    
    try 
    {
      byte[] buffer = new byte[4096];
      os = new ByteArrayOutputStream();
      int read;
      while ((read = is.read(buffer)) != -1) 
      {
        os.write(buffer, 0, read);
      }
      return os.toString(charset);
    } 
    finally 
    { 
      try 
      {
        if (os != null) os.close();
      } 
      catch ( IOException e) { } 
    }        
  }
  
  protected class JsonEntry
  {
    
  }
  
}
