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
package org.santfeliu.ant.feed;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.tools.ant.BuildException;
import org.matrix.feed.Entry;
import org.matrix.feed.EntryFilter;
import org.matrix.feed.Feed;
import org.matrix.feed.FeedFolder;
import org.matrix.feed.FeedFolderFilter;
import org.matrix.feed.FeedManagerPort;
import org.matrix.feed.FeedManagerService;
import org.matrix.feed.Folder;
import org.matrix.feed.FolderFilter;
import org.matrix.util.WSEndpoint;
import org.santfeliu.ant.ws.WSTask;
import org.santfeliu.feed.util.FeedUtils;

/**
 *
 * @author unknown
 */
public class FeedReaderTask extends WSTask
{
  public static final int EXISTING = 0;
  public static final int ACCEPTED = 1;
  public static final int ERROR = 2;
  public static final int UPDATED_VISIBLE = 31;
  public static final int UPDATED_INVISIBLE = 32;
  public static final int OBSOLETE = 4;

  public static final String TITLE_FIELD = "title";
  public static final String URL_FIELD = "url";
  public static final String DESCRIPTION_FIELD = "description";
  public static final String PUBDATE_FIELD = "pubdate";

  public static final int MAX_TITLE_LENGTH = 4000;
  
  //Task attributes
  private String logProperty;
  private String errorLogProperty;
  private String folderId;

  //logging
  private StringBuilder logBuffer = new StringBuilder();
  private StringBuilder errorLogBuffer = new StringBuilder();

  //aux variables
  private String auxMarginDateTime = null;
  private String auxEntryId = null;
  private List<Entry> auxFeedEntryList = null; 
  private FeedManagerPort port = null;

  public String getLogProperty()
  {
    return logProperty;
  }

  public void setLogProperty(String logProperty)
  {
    this.logProperty = logProperty;
  }

  public String getErrorLogProperty() 
  {
    return errorLogProperty;
  }

  public void setErrorLogProperty(String errorLogProperty) 
  {
    this.errorLogProperty = errorLogProperty;
  }

  public String getFolderId()
  {
    return folderId;
  }

  public void setFolderId(String folderId)
  {
    this.folderId = folderId;
  }

  public String executeExternalTest(FeedManagerPort port)
  {
    this.port = port;
    execute();
    return errorLogBuffer.toString();
  }
  
  @Override
  public void execute()
  {
    validateInput();
    List<String> folderIdList = new ArrayList<String>();
    if (getFolderId() == null)
    {
      List<Folder> folderList = getPort().findFolders(new FolderFilter());
      for (Folder folder : folderList)
      {
        folderIdList.add(folder.getFolderId());
      }      
    }
    else
    {
      folderIdList.addAll(Arrays.asList(getFolderId().split(",")));
    }
    
    List<String> feedIdList = getFeedIdList(folderIdList);
    for (String feedId : feedIdList)
    {
      processFeed(feedId);
    }

    // write properties
    if (logProperty != null)
    {
      String currentLog = getProject().getProperty(logProperty);
      String log = (currentLog == null ? "" : currentLog) +
        logBuffer.toString();
      getProject().setProperty(logProperty, log);
    }    
    if (errorLogProperty != null)
    {
      String currentErrorLog = getProject().getProperty(errorLogProperty);
      String errorLog = (currentErrorLog == null ? "" : currentErrorLog) +
        errorLogBuffer.toString();
      getProject().setProperty(errorLogProperty, errorLog);
    }    
  }

  private FeedManagerPort getPort()
  {
    if (port == null)
    {
      WSEndpoint endpoint = getEndpoint(FeedManagerService.class);
      port =
        endpoint.getPort(FeedManagerPort.class, getUsername(), getPassword());
    }
    return port;
  }  
  
  private List<String> getFeedIdList(List<String> folderIdList)
  {
    List<String> allFolderIdList = new ArrayList<String>();
    for (String folderId : folderIdList)
    {
      allFolderIdList.addAll(getAllFolderIdList(folderId));
    }
    Set<String> feedIdSet = new HashSet<String>();
    FeedFolderFilter feedFolderFilter = new FeedFolderFilter();
    feedFolderFilter.getFolderId().addAll(allFolderIdList);
    List<FeedFolder> feedFolderList = 
      getPort().findFeedFolders(feedFolderFilter);
    for (FeedFolder feedFolder : feedFolderList)
    {
      feedIdSet.add(feedFolder.getFeedId());
    }
    return new ArrayList<String>(feedIdSet);
  }
  
  private List<String> getAllFolderIdList(String folderId)
  {
    List<String> result = new ArrayList<String>();
    result.add(folderId);
    
    FolderFilter filter = new FolderFilter();
    filter.setParentFolderId(folderId);
    List<Folder> folderList = getPort().findFolders(filter);
    for (Folder folder : folderList)
    {
      result.addAll(getAllFolderIdList(folder.getFolderId()));
    }
    
    return result;
  }  

  private void processFeed(String feedId)
  {
    long startMs = System.currentTimeMillis();
    String feedName = "";    
    try
    {
      Feed feed = getPort().loadFeed(feedId);
      feedName = feed.getName();
      log("Processing feed " + feedId);
      log("Feed: " + feed.getName());
      log("Type: " + feed.getType());
      log("URL: " + feed.getUrl());
      log("Internal URL: " + feed.getInternalUrl());
      Date now = new Date();
      if (mustUpdateFeed(feed, now))
      {
        updateFeed(feed, now);
      }
      else
      {
        log("Feed " + feedId + " (" + feedName + ") not updated");
      }
    }
    catch (Exception ex)
    {
      logError("Error in feed " + feedId + " (" + feedName + ")");
      logError(ex.toString());
    }
    finally
    {
      long endMs = System.currentTimeMillis();
      int secs = (int)((endMs - startMs) / 1000);
      log("Read time: " + (int)(secs / 60) + "m " + (int)(secs % 60) + "s");
      log("");
    }
  }

  private void updateFeed(Feed feed, Date now) throws Exception
  {
    try
    {
      int acceptedCount = 0;
      int updatedCount = 0;
      int existingCount = 0;
      int obsoleteCount = 0;
      int removedCount = 0;
      int errorCount = 0;

      auxMarginDateTime = getMarginDateTime(now, feed.getEntryLifeSpan());

      System.out.println("Feed " + feed.getFeedId() + " -> Margin datetime: " +
        auxMarginDateTime);

      if (auxMarginDateTime != null)
      {
        removedCount = dbClearOldEntries(feed, auxMarginDateTime);
      }
      log("Removed entries: " + removedCount);

      FeedUtils.FeedReading feedReading = new FeedUtils.FeedReading();
      feedReading.setFeedUrl(feed.getInternalUrl());
      feedReading.setSourceUrl(feed.getUrl());
      feedReading.setIncludeSource(false);
      feedReading.setIncludeImages(false);
      feedReading.setRowCount(1000);
      List<FeedUtils.Row> rowList = FeedUtils.getRowList(feedReading);
      auxFeedEntryList = getFeedEntries(feed.getFeedId());      
      for (FeedUtils.Row row : rowList)          
      {
        System.out.println(row);        
        int status = validateEntry(row);
        switch (status)
        {
          case ACCEPTED:
            acceptedCount++;
            dbInsertEntry(feed, row);  
            break;
          case UPDATED_VISIBLE:
            updatedCount++;
            dbUpdateEntry(feed, auxEntryId, row, true);            
            break;
          case UPDATED_INVISIBLE:
            updatedCount++;
            dbUpdateEntry(feed, auxEntryId, row, false);
            break;
          case EXISTING:
            existingCount++;
            break;
          case OBSOLETE:
            obsoleteCount++;
            break;
          case ERROR:
            errorCount++;
            break;
        }          
      }
      log("Created entries: " + acceptedCount);
      log("Updated entries: " + updatedCount);
      log("Existing entries: " + existingCount);
      log("Obsolete entries: " + obsoleteCount);
      log("Error entries: " + errorCount);
      dbUpdateFeedRefreshDt(feed, now);      
    }
    catch (Exception ex)
    {
      throw new Exception("Unrecoverable error: " + ex.toString() + " at " + new Date());
    }
  }

  private String getMarginDateTime(Date now, String days)
  {
    if (days == null) return null;
    
    Integer iDays = Integer.valueOf(days);
    long marginMillis = now.getTime() -
      (Long.valueOf(iDays) * 24 * 60 * 60 * 1000);
    Date marginDate = new Date(marginMillis);
    return new SimpleDateFormat("yyyyMMddHHmmss").format(marginDate);
  }

  private int dbClearOldEntries(Feed feed, String marginDateTime)
    throws Exception
  {
    EntryFilter entryFilter = new EntryFilter();
    entryFilter.getFeedId().add(feed.getFeedId());
    entryFilter.setEndPubDateTime(marginDateTime);
    List<Entry> entryList = getPort().findEntries(entryFilter);
    for (Entry entry : entryList)
    {
      getPort().removeEntry(entry.getEntryId());
    }
    return entryList.size();
  }

  private boolean mustUpdateFeed(Feed feed, Date now) throws Exception
  {
    int refreshInterval = Integer.valueOf(feed.getRefreshInterval());
    long nowTime = now.getTime();
    SimpleDateFormat dbFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    long lastRefresh = dbFormat.parse(feed.getRefreshDateTime()).getTime();
    return ((nowTime - lastRefresh) > (refreshInterval * 60 * 1000));
  }

  protected int validateEntry(FeedUtils.Row row)
  {
    try
    {
      if (row.getDate() == null)
      {
        throw new Exception("Missing Date mandatory field");        
      }
      else if (row.getDate().trim().isEmpty())
      {
        throw new Exception("Null value in Date mandatory field");        
      }
      
      if (row.getUrl() == null)
      {
        throw new Exception("Missing Url mandatory field");        
      }
      else if (row.getUrl().trim().isEmpty())
      {
        throw new Exception("Null value in Url mandatory field");
      }
      
      if (row.getHeadLine() != null && 
        row.getHeadLine().length() > MAX_TITLE_LENGTH)
      {
        row.setHeadLine(row.getHeadLine().substring(0, MAX_TITLE_LENGTH));
      }
      
      if (auxMarginDateTime != null)
      {
        String pubDateTime = row.getDate();
        if (pubDateTime.compareTo(auxMarginDateTime) < 0) return OBSOLETE;
      }
      return dbCheckEntry(row);      
    }
    catch (Exception ex)
    {
      logError("Error: " + ex.getMessage() + ". Entry: " + row);
      return ERROR;
    }
  }

  protected void validateInput()
  {
    if (connVar == null)
      throw new BuildException("Attribute 'connVar' is required");
  }

  // 0: Non existing entry; 1: Unchanged entry; 2: Changed visible entry; 3: Changed invisible entry
  private int dbCheckEntry(FeedUtils.Row row) throws Exception
  {    
    String url = row.getUrl();
    Entry entry = getEntryFromList(auxFeedEntryList, url);
    if (entry != null)
    {
      String entryHash = getHash(row);
      String dbHash = entry.getHash();
      if (entryHash.equals(dbHash)) //No changes
      {
        return EXISTING;
      }
      else //Entry has changed
      {
        auxEntryId = String.valueOf(entry.getEntryId());
        if (entry.isVisible()) return UPDATED_VISIBLE;
        else return UPDATED_INVISIBLE;
      }
    }
    return ACCEPTED;
  }

  private String getHash(FeedUtils.Row row) throws Exception
  {
    String s = row.getDate() +
      row.getHeadLine() +
      (row.getSummary() == null ? "" : row.getSummary());
    MessageDigest m = MessageDigest.getInstance("MD5");
    byte[] data = s.getBytes();
    m.update(data, 0, data.length);
    BigInteger i = new BigInteger(1, m.digest());
    return String.format("%1$032X", i);
  }

  private void dbInsertEntry(Feed feed, FeedUtils.Row row)
    throws Exception
  {
    Entry entry = new Entry();
    entry.setFeedId(feed.getFeedId());
    entry.setTitle(row.getHeadLine());
    entry.setUrl(row.getUrl());
    entry.setDescription(row.getSummary());
    entry.setPubDateTime(row.getDate());
    entry.setHash(getHash(row));
    entry.setVisible(true);
    getPort().storeEntry(entry);
  }

  private void dbUpdateEntry(Feed feed, String entryId,
    FeedUtils.Row row, boolean isVisible) throws Exception
  {
    Entry entry = new Entry();
    entry.setEntryId(entryId);
    entry.setFeedId(feed.getFeedId());
    entry.setTitle(row.getHeadLine());
    entry.setUrl(row.getUrl());
    entry.setDescription(row.getSummary());
    entry.setPubDateTime(row.getDate());
    entry.setHash(getHash(row));
    entry.setVisible(isVisible);
    getPort().storeEntry(entry);
  }

  private void dbUpdateFeedRefreshDt(Feed feed, Date now) throws Exception
  {
    SimpleDateFormat dbFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    feed.setRefreshDateTime(dbFormat.format(now));
    getPort().storeFeed(feed);
  }
  
  @Override
  public void log(String s)
  {
    super.log(s);
    if (logProperty != null)
    {
      logBuffer.append(s);
      logBuffer.append("<br/>");
    }
  }

  private void logError(String s)
  {
    log(s);    
    if (errorLogProperty != null)
    {
      errorLogBuffer.append(s);
      errorLogBuffer.append("<br/>");
    }
  }

  private List<Entry> getFeedEntries(String feedId)
  {
    EntryFilter entryFilter = new EntryFilter();    
    entryFilter.getFeedId().add(feedId);
    return getPort().findEntries(entryFilter);    
  }

  private Entry getEntryFromList(List<Entry> entryList, String entryUrl)
  {    
    for (Entry entry : entryList)
    {
      if (entry.getUrl() != null && entry.getUrl().equals(entryUrl)) 
        return entry;
    }
    return null;
  }

}
