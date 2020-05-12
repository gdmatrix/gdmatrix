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
package org.santfeliu.feed.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.matrix.feed.Entry;
import org.matrix.feed.EntryFilter;
import org.matrix.feed.Feed;
import org.matrix.feed.FeedConstants;
import org.matrix.feed.FeedFilter;
import org.matrix.feed.FeedFolder;
import org.matrix.feed.FeedFolderFilter;
import org.matrix.feed.Folder;
import org.matrix.feed.FolderFilter;
import org.santfeliu.feed.client.FeedManagerClient;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.web.HttpUtils;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author lopezrj
 */
public class FeedRSSServlet extends HttpServlet
{
  private static final String NO_CHANNEL_MESSAGE =
    "No hi ha cap canal associat a aquesta URL";
  private static final String ERROR_MESSAGE =
    "Error al generar contingut RSS";
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    Map<String, String> feedSourceMap = new HashMap<String, String>();    
    try
    {
      Folder folder = getFolder(request);
      Feed feed = null;
      if (folder == null)
      {
        feed = getFeed(request);
      }
      try
      {
        if (folder != null)
        {
          writeRSS(request, response, folder, feedSourceMap);
        }
        else if (feed != null)
        {
          writeRSS(request, response, feed, feedSourceMap);
        }
        else
        {
          throw new Exception();
        }
      }
      catch (Exception ex)
      {
        writeErrorRSS(response, ERROR_MESSAGE);
      }
    }
    catch (Exception ex)
    {
      writeErrorRSS(response, NO_CHANNEL_MESSAGE);
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    doGet(request, response);
  }

  private void writeRSS(HttpServletRequest request, HttpServletResponse 
    response, Folder folder, Map<String, String> feedSourceMap) throws Exception
  {
    String serverURL = getServerURL(request);
    String folderId = folder.getFolderId();
    FeedManagerClient client = getFeedManagerClient(request);
    response.setContentType("text/xml");
    response.setCharacterEncoding("UTF-8");
    PrintWriter writer = response.getWriter();
    writer.println("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
    writer.println("<rss version=\"2.0\">");    
    try
    {
      FeedFolderFilter filter = new FeedFolderFilter();
      filter.getFolderId().addAll(getAllFolderIdList(client, folderId));
      List<FeedFolder> feedFolderList = client.findFeedFoldersFromCache(filter);
      if (!feedFolderList.isEmpty())
      {
        writer.println("<channel>");
        writeChannelDescriptionRSS(writer, serverURL, folder);
        writeChannelImageRSS(writer, serverURL, folder);
        List<String> feedIdList = new ArrayList<String>();
        for (FeedFolder feedFolder : feedFolderList)
        {
          feedIdList.add(feedFolder.getFeedId());
        }
        fillFeedSourceMap(request, feedSourceMap, feedIdList);
        EntryFilter entryFilter = new EntryFilter();
        entryFilter.getFeedId().addAll(feedIdList);
        entryFilter.setExcludeInvisible(true);
        int defaultEntryCount = Integer.valueOf(folder.getDefaultEntryCount());
        entryFilter.setMaxResults(getEntryCount(request, defaultEntryCount));        
        List<Entry> entryList = client.findEntriesFromCache(entryFilter);
        for (Entry entry : entryList)
        {
          writeItemRSS(writer, entry, feedSourceMap);
        }
        writer.print("</channel>");
      }
      writer.print("</rss>");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  private void writeRSS(HttpServletRequest request, HttpServletResponse 
    response, Feed feed, Map<String, String> feedSourceMap) throws Exception
  {
    String serverURL = getServerURL(request);
    FeedManagerClient client = getFeedManagerClient(request);
    response.setContentType("text/xml");
    response.setCharacterEncoding("UTF-8");
    PrintWriter writer = response.getWriter();
    writer.println("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
    writer.println("<rss version=\"2.0\">");    
    try
    {
      writer.println("<channel>");
      writeChannelDescriptionRSS(writer, serverURL, feed);
      writeChannelImageRSS(writer, serverURL, feed);
      List<String> feedIdList = new ArrayList<String>();
      feedIdList.add(feed.getFeedId());
      fillFeedSourceMap(request, feedSourceMap, feedIdList);
      EntryFilter entryFilter = new EntryFilter();
      entryFilter.getFeedId().addAll(feedIdList);
      entryFilter.setExcludeInvisible(true);
      int defaultEntryCount = 10; //TODO Provisional
      entryFilter.setMaxResults(getEntryCount(request, defaultEntryCount));        
      List<Entry> entryList = client.findEntriesFromCache(entryFilter);
      for (Entry entry : entryList)
      {
        writeItemRSS(writer, entry, feedSourceMap);
      }
      writer.print("</channel>");
      writer.print("</rss>");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }  
  
  private List<String> getAllFolderIdList(FeedManagerClient client, String folderId)
  {
    List<String> result = new ArrayList<String>();
    result.add(folderId);
    
    FolderFilter filter = new FolderFilter();
    filter.setParentFolderId(folderId);
    List<Folder> folderList = client.findFoldersFromCache(filter);
    for (Folder folder : folderList)
    {
      result.addAll(getAllFolderIdList(client, folder.getFolderId()));
    }
    
    return result;
  }

  private void writeChannelDescriptionRSS(PrintWriter writer,
    String serverURL, Folder folder)
  {
    String rssTitle = folder.getName();
    String rssDescription = folder.getDescription();
    writeChannelDescription(writer, serverURL, rssTitle, rssDescription);
  }

  private void writeChannelDescriptionRSS(PrintWriter writer,
    String serverURL, Feed feed)
  {
    String rssTitle = feed.getName();
    String rssDescription = feed.getName();
    writeChannelDescription(writer, serverURL, rssTitle, rssDescription);
  }
  
  private void writeChannelDescription(PrintWriter writer, String serverURL, 
    String title, String description)
  {
    writer.println("<title>" + getNonParsedText(title) + "</title>");
    writer.println("<link>" + getNonParsedText(serverURL) + "</link>");
    writer.println("<description>" + getNonParsedText(description, "") +
      "</description>");
  }
  
  private void writeChannelImageRSS(PrintWriter writer, String serverURL,
    Folder folder)
  {
    writeChannelImage(writer, serverURL, folder.getIconDocId(), null);
  }

  private void writeChannelImageRSS(PrintWriter writer, String serverURL,
    Feed feed)
  {
    writeChannelImage(writer, serverURL, null, feed.getIconUrl());
  }
  
  private void writeChannelImage(PrintWriter writer, String serverURL, 
    String iconDocId, String iconUrl)
  {
    if (iconDocId != null || iconUrl != null)
    {
      writer.println("<image>");
      if (iconDocId != null)
      {
        writer.println("<url>" + getNonParsedText(serverURL + "/documents/" + iconDocId) + "</url>");        
      }
      else if (iconUrl != null)
      {
        writer.println("<url>" + getNonParsedText(iconUrl) + "</url>");                
      }
      writer.println("<title>" + getNonParsedText("RSS") + "</title>");
      writer.println("<link>" + getNonParsedText(serverURL) + "</link>");
      writer.println("</image>");
    }
  }  

  private void writeItemRSS(PrintWriter writer, Entry entry, 
    Map<String, String> feedMap)
  {
    writer.println("<item>");
    writer.println("<title>" + getNonParsedText(entry.getTitle(), 
      FeedConstants.DEFAULT_EMPTY_ENTRY_HEADLINE) + "</title>");
    writer.println("<link>" + getNonParsedText(entry.getUrl()) + "</link>");
    writer.println("<description>" + getNonParsedText(entry.getDescription(), "") +
      "</description>");
    writer.println("<pubDate>" +
      getNonParsedText(getEntryDateTime(entry)) +
      "</pubDate>");
    if (feedMap.get(entry.getFeedId()) != null)
    {
      writer.println(feedMap.get(entry.getFeedId())); //<source> tag
    }    
    writer.println("</item>");
  }

  private void writeErrorRSS(HttpServletResponse response, String message)
    throws IOException
  {
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");
    PrintWriter writer = response.getWriter();
    writer.println("<html>");
    writer.println("<head>");
    writer.println("<meta http-equiv=\"Content-Type\" " +
      "content=\"text/html; charset=UTF-8\" />");
    writer.println("<title>RSS</title>");
    writer.println("</head>");
    writer.println("<body>");
    writer.println(message);
    writer.println("</body>");
    writer.println("</html>");
  }

  private String getServerURL(HttpServletRequest request)
  {
    return HttpUtils.getContextURL(request);
  }

  private String getFolderId(HttpServletRequest request) throws Exception
  {
    // TODO: CHECK URL
    String url = request.getRequestURL().toString();
    String servletPath = request.getServletPath();
    String serverURL = url.substring(0, url.indexOf(servletPath));
    String params = url.substring((serverURL + servletPath).length());
    if (params.startsWith("/"))
    {
      int i = params.indexOf(";");
      String folderString = 
        (i > 0 ? params.substring(1, i) : params.substring(1));
      if (folderString.startsWith("_")) return null;
      try
      {
        int folderId = Integer.parseInt(folderString);
        return String.valueOf(folderId);
      }
      catch (NumberFormatException ex) //alias
      {
        String alias = folderString.trim();
        if (!alias.isEmpty())
        {
          FolderFilter filter = new FolderFilter();
          filter.setAlias(alias);
          List<Folder> folderList =
            getFeedManagerClient(request).findFoldersFromCache(filter);
          if (folderList.size() > 0)
          {
            return folderList.get(0).getFolderId();
          }
          else
          {
            throw new Exception("INVALID FOLDER ALIAS");    
          }
        }
        else
        {
          throw new Exception("FOLDER MUST BE SPECIFIED");
        }
      }
    }
    else //no folder specified
    {
      throw new Exception("FOLDER MUST BE SPECIFIED");
    }
  }

  private String getFeedId(HttpServletRequest request) throws Exception
  {
    // TODO: CHECK URL
    String url = request.getRequestURL().toString();
    String servletPath = request.getServletPath();
    String serverURL = url.substring(0, url.indexOf(servletPath));
    String params = url.substring((serverURL + servletPath).length());
    if (params.startsWith("/")) 
    {
      int i = params.indexOf(";");
      String feedString = 
        (i > 0 ? params.substring(1, i) : params.substring(1));
      if (!feedString.startsWith("_")) return null;      
      try
      {
        int feedId = Integer.parseInt(feedString.substring(1));
        return String.valueOf(feedId);
      }
      catch (NumberFormatException ex)
      {
        throw new Exception("FEED MUST BE SPECIFIED");
      }
    }
    else //no feed specified
    {
      throw new Exception("FEED MUST BE SPECIFIED");
    }
  }  
  
  private int getEntryCount(HttpServletRequest request, int defaultEntryCount)
  {
    try
    {
      if (request.getParameter("num") != null)
      {
        return Integer.valueOf(request.getParameter("num"));
      }
    }
    catch (Exception ex)
    {
      //nothing here
    }
    return defaultEntryCount;
  }

  private String getEntryDateTime(Entry entry)
  {
    try
    {
      SimpleDateFormat df1 = new SimpleDateFormat("yyyyMMddHHmmss");
      Date date = df1.parse(entry.getPubDateTime());
      SimpleDateFormat df2 = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z",
        Locale.ENGLISH);
      return df2.format(date);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return "";
  }

  private String getNonParsedText(String text)
  {
    return("<![CDATA[" + text + "]]>");
  }
  
  private String getNonParsedText(String text, String textIfNull)
  {    
      return("<![CDATA[" + (text == null ? textIfNull : text) + "]]>");
  }

  private Folder getFolder(HttpServletRequest request) throws Exception
  {
    FeedManagerClient client = getFeedManagerClient(request);
    String folderId = getFolderId(request);
    return (folderId == null ? null : client.loadFolderFromCache(folderId));
  }

  private Feed getFeed(HttpServletRequest request) throws Exception
  {
    FeedManagerClient client = getFeedManagerClient(request);
    String feedId = getFeedId(request);
    return (feedId == null ? null : client.loadFeedFromCache(feedId));
  }

  private FeedManagerClient getFeedManagerClient(HttpServletRequest request)
  {
    Credentials credentials = SecurityUtils.getCredentials(request, false);
    if (credentials == null)
    {
      credentials = UserSessionBean.getCredentials(request);
    }
    String username = credentials.getUserId();
    String password = credentials.getPassword();
    return new FeedManagerClient(username, password);    
  }

  private void fillFeedSourceMap(HttpServletRequest request, 
    Map<String, String> feedSourceMap, List<String> feedIdList) 
  {
    FeedManagerClient client = getFeedManagerClient(request);
    FeedFilter filter = new FeedFilter();
    filter.getFeedId().addAll(feedIdList);
    List<Feed> feedList = client.findFeedsFromCache(filter);
    for (Feed feed : feedList)
    {
      if ((feed.getUrl() != null || feed.getInternalUrl() != null) && 
        feed.getName() != null)
      {
        StringBuilder sb = new StringBuilder();
        sb.append("<source url=\"");
        if (feed.getUrl() != null)
        {
          sb.append(feed.getUrl());
        }
        else
        {
          sb.append(feed.getInternalUrl());
        }        
        sb.append("\">");
        sb.append(getNonParsedText(feed.getName()));
        sb.append("</source>");      
        feedSourceMap.put(feed.getFeedId(), sb.toString());
      }
    }
  }

}
