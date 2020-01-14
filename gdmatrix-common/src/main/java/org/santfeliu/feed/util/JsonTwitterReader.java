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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author unknown
 */
public class JsonTwitterReader extends JsonFeedReader
{  
  private static final String TWITTER_BEARER_TOKEN = "AAAAAAAAAAAAAAAAAAAAALtjggAAAAAAYk6fFERJObamVGG8enasbCSnlSo%3DuE0A1X6lsAffQfQ5q3Rt8r3LRjH0K5pG4UbQw0oEhOzwTxkjVT";
  private static final String TWITTER_APP_NAME = "ajsantfeliu feed timeline";
  
  public JsonTwitterReader(FeedUtils.FeedReading feedReading)
  {
    super(feedReading);
  }
      
  @Override
  protected String getJsonCode() throws Exception
  {
    String twitterAccount = null;
    String sourceUrl = feedReading.getSourceUrl();
    int idx = sourceUrl.indexOf("twitter.com/");
    if (idx >= 0)
    {
      twitterAccount = sourceUrl.substring(idx + 12);
    }
    else
    {
      throw new Exception("INVALID_TWITTER_URL");
    }
    HttpsURLConnection connection = null;
    try 
    {
      String endPointUrl = "https://api.twitter.com/1.1/statuses/"
        + "user_timeline.json?count=25&exclude_replies=true&screen_name=" 
        + twitterAccount;
      URL url = new URL(endPointUrl);
      connection = (HttpsURLConnection)url.openConnection();
      connection.setDoOutput(true);
      connection.setDoInput(true); 
      connection.setRequestMethod("GET"); 
      connection.setRequestProperty("Host", "api.twitter.com");
      connection.setRequestProperty("User-Agent", TWITTER_APP_NAME);
      connection.setRequestProperty("Authorization", "Bearer " + TWITTER_BEARER_TOKEN);
      connection.setUseCaches(false);
      return readResponse(connection);
    }
    catch (MalformedURLException e) 
    {
      throw new IOException("Invalid endpoint URL specified.", e);
    }
    finally 
    {
      if (connection != null) 
      {
        connection.disconnect();
      }
    }
  }  
  
  @Override
  protected List<JsonEntry> getEntries(String json) throws Exception
  {
    List<JsonEntry> result = new ArrayList();    
    JSONParser parser = new JSONParser();
    JSONArray dataArray = (JSONArray)parser.parse(json);
    if (dataArray != null)
    {
      for (int i = 0; i < dataArray.size(); i++)
      {
        JsonTwitterEntry entry = new JsonTwitterEntry();
        JSONObject entryObj = (JSONObject)dataArray.get(i);
        entry.setId(String.valueOf(entryObj.get("id")));
        entry.setText((String)entryObj.get("text"));
        entry.setCreatedAt((String)entryObj.get("created_at"));
        //Media url
        JSONObject entitiesObj = (JSONObject)entryObj.get("entities");
        if (entitiesObj != null)
        {
          JSONArray mediaArray = (JSONArray)entitiesObj.get("media");
          if (mediaArray != null)
          {
            for (int j = 0; j < mediaArray.size() && entry.getMediaUrl() == null; j++)
            {
              JSONObject mediaObj = (JSONObject)mediaArray.get(j);
              String type = (String)mediaObj.get("type");
              if ("photo".equals(type))
              {          
                String mediaUrl = (String)mediaObj.get("media_url");
                entry.setMediaUrl(mediaUrl);
              }
            }        
          }
        }
        //Screen name
        JSONObject userObj = (JSONObject)entryObj.get("user");
        if (userObj != null)
        {
          entry.setScreenName((String)userObj.get("screen_name"));
        }      
        result.add(entry);
      }
    }
    return result;
  }

  @Override
  protected FeedUtils.Row getRow(JsonEntry entryParam)
  {    
    JsonTwitterEntry entry = (JsonTwitterEntry)entryParam;
    FeedUtils.Row row = new FeedUtils.Row();
    if (entry.getText() != null)
    {
      row.setHeadLine(FeedUtils.normalizeText(entry.getText()));
    }
    if (entry.getUrl() != null)
    {
      row.setUrl(entry.getUrl());
    }
    else
    {
      row.setUrl(feedReading.getSourceUrl());
    }
    row.setDate(FeedUtils.getDBDateTime(entry.getCreatedAt()));
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
    if (entry.getMediaUrl() != null)
    {
      sbSummary.append("<img src=\"").append(entry.getMediaUrl()).append("\"/>");
    }      
    row.setSummary(sbSummary.toString());

    if (feedReading.isIncludeImages())
    {
      row.setImageUrl(entry.getMediaUrl());
    }
    return row;
  }
  
  private String readResponse(HttpsURLConnection connection) 
  {
    try 
    {
      StringBuilder str = new StringBuilder();
      BufferedReader br = new BufferedReader(
        new InputStreamReader(connection.getInputStream()));
      String line = "";
      while((line = br.readLine()) != null) 
      {
        str.append(line + System.getProperty("line.separator"));
      }
      return str.toString();
    }
    catch (IOException e) { return new String(); }
  }  
  
  private class JsonTwitterEntry extends JsonEntry
  {
    private String id;
    private String text;
    private String mediaUrl;    
    private String createdAt;
    private String screenName;

    public String getId()
    {
      return id;
    }

    public void setId(String id)
    {
      this.id = id;
    }

    public String getText()
    {
      return text;
    }

    public void setText(String text)
    {
      this.text = text;
    }

    public String getMediaUrl()
    {
      return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl)
    {
      this.mediaUrl = mediaUrl;
    }

    public String getCreatedAt()
    {
      return createdAt;
    }

    public void setCreatedAt(String createdAt)
    {
      this.createdAt = createdAt;
    }

    public String getScreenName()
    {
      return screenName;
    }

    public void setScreenName(String screenName)
    {
      this.screenName = screenName;
    }

    public String getUrl()
    {
      return "http://www.twitter.com/" + screenName + "/status/" + id;
    }
    
    @Override
    public String toString() 
    {
      StringBuilder sb = new StringBuilder();
      sb.append("Id: ").append(id).append("\n");
      sb.append("Text: ").append(text).append("\n");
      sb.append("Media url: ").append(mediaUrl).append("\n");
      sb.append("Created at: ").append(createdAt).append("\n");
      sb.append("Url: ").append(getUrl()).append("\n");
      return sb.toString();
    }    
  }
  
}
