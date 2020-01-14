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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author unknown
 */
public class JsonFacebookReader extends JsonFeedReader
{
  public JsonFacebookReader(FeedUtils.FeedReading feedReading)
  {
    super(feedReading);
  }
    
  @Override
  protected String getJsonCode() throws Exception
  {    
    InputStream is = null;    
    try
    {
      is = FeedUtils.getInputStreamFromUrl(feedReading.getFeedUrl());
      return readInputStream(is);      
    }    
    finally
    {
      try
      {
        if (is != null) is.close();
      }
      catch (Exception ex3) { }
    }    
  }  
  
  @Override
  protected List<JsonEntry> getEntries(String json) throws Exception
  {
    List<JsonEntry> result = new ArrayList();    
    JSONParser parser = new JSONParser();
    JSONObject object = (JSONObject)parser.parse(json);    
    JSONArray dataArray = (JSONArray)object.get("data");       
    for (int i = 0; i < dataArray.size(); i++)
    {
      JsonFacebookEntry entry = new JsonFacebookEntry();
      JSONObject entryObj = (JSONObject)dataArray.get(i);
      entry.setId((String)entryObj.get("id"));
      String link = (String)entryObj.get("link");
      if (link == null)
      {
        link = (String)entryObj.get("permalink_url");
      }
      entry.setLink(link);
      entry.setMessage((String)entryObj.get("message"));
      entry.setPicture((String)entryObj.get("picture"));
      String dateTime = (String)entryObj.get("updated_time");
      if (dateTime == null)
      {
        dateTime = (String)entryObj.get("created_time");
      }
      entry.setUpdatedTime(dateTime);
      result.add(entry);
    }
    return result;
  }

  @Override
  protected FeedUtils.Row getRow(JsonEntry entryParam)
  {    
    JsonFacebookEntry entry = (JsonFacebookEntry)entryParam;
    FeedUtils.Row row = new FeedUtils.Row();
    if (entry.getMessage() != null)
    {
      row.setHeadLine(FeedUtils.normalizeText(entry.getMessage()));
    }
    if (entry.getLink() != null)
    {
      row.setUrl(entry.getLink());
    }
    else
    {
      row.setUrl(feedReading.getSourceUrl());
    }
    row.setDate(FeedUtils.getDBDateTime(entry.getUpdatedTime()));
    if (feedReading.isIncludeSource())
    {
      row.setSourceUrl("");
      row.setSourceTitle("");
    }
    StringBuilder sbSummary = new StringBuilder();
    if (row.getHeadLine() != null)
    {
      sbSummary.append(row.getHeadLine()).append("<br>");
    }
    if (entry.getPicture() != null)
    {
      sbSummary.append("<img src=\"").append(entry.getPicture()).append("\"/>");
    }      
    row.setSummary(sbSummary.toString());

    if (feedReading.isIncludeImages())
    {
      row.setImageUrl(entry.getPicture());
    }
    return row;
  }
  
  private class JsonFacebookEntry extends JsonEntry
  {
    private String id;
    private String message;
    private String picture;
    private String link;
    private String updatedTime;

    public String getId()
    {
      return id;
    }

    public void setId(String id)
    {
      this.id = id;
    }

    public String getMessage()
    {
      return message;
    }

    public void setMessage(String message)
    {
      this.message = message;
    }

    public String getPicture()
    {
      return picture;
    }

    public void setPicture(String picture)
    {
      this.picture = picture;
    }

    public String getLink()
    {
      return link;
    }

    public void setLink(String link)
    {
      this.link = link;
    }

    public String getUpdatedTime()
    {
      return updatedTime;
    }

    public void setUpdatedTime(String updatedTime)
    {
      this.updatedTime = updatedTime;
    }

    @Override
    public String toString() 
    {
      StringBuilder sb = new StringBuilder();
      sb.append("Id: ").append(id).append("\n");
      sb.append("Message: ").append(message).append("\n");
      sb.append("Picture: ").append(picture).append("\n");
      sb.append("Link: ").append(link).append("\n");
      sb.append("Updated Time: ").append(updatedTime).append("\n");
      return sb.toString();
    }    
  }
  
}
