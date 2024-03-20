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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.matrix.cms.CMSManagerPort;
import org.matrix.cms.CMSManagerService;
import org.matrix.cms.Node;
import org.matrix.cms.NodeFilter;
import org.matrix.cms.Property;
import org.matrix.news.New;
import org.matrix.news.NewSection;
import org.matrix.news.NewStoreOptions;
import org.matrix.news.NewsFilter;
import org.matrix.news.NewsManagerPort;
import org.matrix.news.NewsManagerService;
import org.matrix.util.WSEndpoint;
import org.santfeliu.ant.ws.WSTask;
import org.santfeliu.feed.util.FeedUtils;
import org.santfeliu.util.enc.Unicode;

/**
 *
 * @author lopezrj-sf
 */
public class FeedReaderTask2 extends WSTask
{
  private static final String PROP_FN = "feedNode";
  private static final String PROP_FN_FOLDERS = "feedNode.folders";
  private static final String PROP_FN_NAME = "feedNode.name";
  private static final String PROP_FN_TYPE = "feedNode.type";
  private static final String PROP_FN_URL = "feedNode.url";
  private static final String PROP_FN_INTERNALURL = "feedNode.internalUrl";
  private static final String PROP_FN_ENTRYLIFESPAN = "feedNode.entryLifeSpan";
  private static final String PROP_FN_REFRESHINTERVAL = 
    "feedNode.refreshInterval";
  private static final String PROP_FN_LASTREFRESHDT = 
    "feedNode.lastRefreshDateTime";
  private static final String PROP_FN_REFRESHMAXROWS = 
    "feedNode.refreshMaxRows";
  private static final String PROP_FN_ICONURL = 
    "feedNode.iconUrl";
  private static final String PROP_FN_SOURCEID = 
    "feedNode.sourceId";
  private static final int DEFAULT_ENTRYLIFESPAN = 365;
  private static final int DEFAULT_REFRESHMAXROWS = 1000;
  
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
  private String workspaceId;
  private String folderIds;
  private String invalidImagePrefixes;
  private String forceUpdate;

  //logging
  private final StringBuilder logBuffer = new StringBuilder();
  private final StringBuilder errorLogBuffer = new StringBuilder();

  //aux variables
  private String auxMarginDateTime = null;
  private String auxEntryId = null;
  private List<New> auxFeedEntryList = null;  
  private NewsManagerPort newsPort = null;
  private CMSManagerPort cmsPort = null;

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

  public String getWorkspaceId()
  {
    return workspaceId;
  }

  public void setWorkspaceId(String workspaceId)
  {
    this.workspaceId = workspaceId;
  }

  public String getFolderIds()
  {
    return folderIds;
  }

  public void setFolderIds(String folderIds)
  {
    this.folderIds = folderIds;
  }

  public String getInvalidImagePrefixes()
  {
    return invalidImagePrefixes;
  }

  public void setInvalidImagePrefixes(String invalidImagePrefixes)
  {
    this.invalidImagePrefixes = invalidImagePrefixes;
  }

  public String getForceUpdate()
  {
    return forceUpdate;
  }

  public void setForceUpdate(String forceUpdate)
  {
    this.forceUpdate = forceUpdate;
  }

  public String executeExternalTest(NewsManagerPort newsPort, 
    CMSManagerPort cmsPort)
  {
    this.newsPort = newsPort;
    this.cmsPort = cmsPort;
    execute();
    return errorLogBuffer.toString();
  }
  
  @Override
  public void execute()
  {
    validateInput();
    List<String> nodeIdList = getNodeIdList(getFolderIdList());
    for (String nodeId : nodeIdList)
    {
      processFeed(nodeId);
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

  private NewsManagerPort getNewsPort()
  {
    if (newsPort == null)
    {
      WSEndpoint endpoint = getEndpoint(NewsManagerService.class);
      newsPort =
        endpoint.getPort(NewsManagerPort.class, getUsername(), getPassword());
    }
    return newsPort;
  }

  private CMSManagerPort getCMSPort()
  {
    if (cmsPort == null)
    {
      WSEndpoint endpoint = getEndpoint(CMSManagerService.class);
      cmsPort =
        endpoint.getPort(CMSManagerPort.class, getUsername(), getPassword());
    }
    return cmsPort;    
  }
  
  private List<String> getNodeIdList(List<String> folderIdList)
  {
    List<String> result = new ArrayList();
    if (!folderIdList.isEmpty())
    {
      Property p1 = new Property();
      p1.setName(PROP_FN);
      p1.getValue().add("true");
      Property p2 = new Property();
      p2.setName(PROP_FN_FOLDERS);
      p2.getValue().addAll(folderIdList);    
      NodeFilter filter = new NodeFilter();
      filter.getProperty().add(p1);
      filter.getProperty().add(p2);
      filter.getWorkspaceId().add(getWorkspaceId());
      List<Node> nodeList = getCMSPort().findNodes(filter);
      for (Node node : nodeList)
      {
        result.add(node.getNodeId());
      }
    }
    return result;
  }

  private void processFeed(String nodeId)
  {
    long startMs = System.currentTimeMillis();
    String feedName = "";
    try
    {
      Node node = getCMSPort().loadNode(getWorkspaceId(), nodeId);
      feedName = getPropertyValue(node, PROP_FN_NAME);
      log("Processing feed " + nodeId);
      log("Feed: " + feedName);
      log("Type: " + getPropertyValue(node, PROP_FN_TYPE));
      log("URL: " + getPropertyValue(node, PROP_FN_URL));
      log("Internal URL: " + getPropertyValue(node, PROP_FN_INTERNALURL));
      Date now = new Date();
      if (mustUpdateFeed(node, now))
      {
        updateFeed(node, now);
      }
      else
      {
        log("Feed " + nodeId + " (" + feedName + ") not updated");
      }
    }
    catch (Exception ex)
    {
      logError("Error in feed " + nodeId + " (" + feedName + ")");
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

  private void updateFeed(Node node, Date now) throws Exception
  {
    try
    {
      int acceptedCount = 0;
      int updatedCount = 0;
      int existingCount = 0;
      int obsoleteCount = 0;
      //int removedCount = 0;
      int errorCount = 0;

      Integer feedEntryLifeSpan = DEFAULT_ENTRYLIFESPAN;
      String strFeedEntryLifeSpan = 
        getPropertyValue(node, PROP_FN_ENTRYLIFESPAN);
      if (strFeedEntryLifeSpan != null)
      {
        try
        {
          feedEntryLifeSpan = Integer.parseInt(strFeedEntryLifeSpan);
        }
        catch (NumberFormatException ex)
        {
        }
      }
      
      auxMarginDateTime = getMarginDateTime(now, feedEntryLifeSpan);

      System.out.println("Feed " + node.getNodeId() + " -> Margin datetime: " +
        auxMarginDateTime);
/*
      if (auxMarginDateTime != null)
      {
        removedCount = dbClearOldEntries(node.getNodeId(), auxMarginDateTime);
      }
      log("Removed entries: " + removedCount);
*/      
      FeedUtils.FeedReading feedReading = new FeedUtils.FeedReading();
      feedReading.setFeedUrl(getPropertyValue(node, PROP_FN_INTERNALURL));
      feedReading.setSourceUrl(getPropertyValue(node, PROP_FN_URL));
      feedReading.setIncludeSource(true);
      feedReading.setIncludeImages(true);
      feedReading.setInvalidImagePrefixList(getInvalidImagePrefixList());      
      feedReading.setRowCount(getRefreshMaxRows(node));
      List<FeedUtils.Row> rowList = FeedUtils.getRowList(feedReading);
      auxFeedEntryList = getFeedEntries(node.getNodeId());
      for (FeedUtils.Row row : rowList)
      {
        System.out.println(row);        
        int status = validateEntry(row);
        switch (status)
        {
          case ACCEPTED:
            try
            {
              dbInsertEntry(node, row, feedEntryLifeSpan);
              acceptedCount++;
            }
            catch (Exception ex)
            {
              errorCount++;
            }
            break;
          case UPDATED_VISIBLE:
            try
            {
              dbUpdateEntry(node, row, feedEntryLifeSpan, auxEntryId, true);
              updatedCount++;              
            }
            catch (Exception ex)
            {
              errorCount++;
            }            
            break;
          case UPDATED_INVISIBLE:
            try
            {            
              dbUpdateEntry(node, row, feedEntryLifeSpan, auxEntryId, false);
              updatedCount++;              
            }
            catch (Exception ex)
            {
              errorCount++;
            }                        
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
      dbUpdateFeedRefreshDt(node, now);      
    }
    catch (Exception ex)
    {
      throw new Exception("Unrecoverable error: " + ex.toString() + " at " + 
        new Date());
    }
  }

  private String getMarginDateTime(Date now, Integer days)
  {
    if (days == null) return null;
    
    //Integer iDays = Integer.valueOf(days);
    long marginMillis = now.getTime() -
      (Long.valueOf(days) * 24 * 60 * 60 * 1000);
    Date marginDate = new Date(marginMillis);
    return new SimpleDateFormat("yyyyMMddHHmmss").format(marginDate);
  }
  
  private int getRefreshMaxRows(Node node)
  {
    String strRefreshMaxRows = getPropertyValue(node, PROP_FN_REFRESHMAXROWS);
    if (strRefreshMaxRows != null)
    {
      try
      {
        return Integer.valueOf(strRefreshMaxRows);
      }
      catch (NumberFormatException ex)
      {
      }
    }
    return DEFAULT_REFRESHMAXROWS;
  }
/*
  private int dbClearOldEntries(String nodeId, String marginDateTime)
    throws Exception
  {
    NewsFilter filter = new NewsFilter();
    filter.getSectionId().add(nodeId);
    filter.setEndDateTime(marginDateTime);
    filter.setExcludeNotPublished(true);
    filter.setMaxResults(Integer.MAX_VALUE);
    List<New> newsList = getNewsPort().findNews(filter);
    for (New newItem : newsList)
    {
      getNewsPort().removeNew(newItem.getNewId());
    }
    return newsList.size();
  }
*/
  private boolean mustUpdateFeed(Node node, Date now)
  {
    if (Boolean.valueOf(getForceUpdate())) return true;
    
    String lastFeedRefreshDateTime = 
      getPropertyValue(node, PROP_FN_LASTREFRESHDT);
    if (lastFeedRefreshDateTime == null) return true;
    
    String strRefreshInterval = getPropertyValue(node, PROP_FN_REFRESHINTERVAL);
    if (strRefreshInterval != null)
    {
      try
      {
        int refreshInterval = Integer.valueOf(strRefreshInterval);
        long nowTime = now.getTime();
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        long lastRefresh = dbFormat.parse(lastFeedRefreshDateTime).getTime();
        return ((nowTime - lastRefresh) > (refreshInterval * 60 * 1000));
      }
      catch (Exception ex)
      {        
      }
    }
    return true;
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

  // 0: Non existing entry; 
  // 1: Unchanged entry; 
  // 2: Changed visible entry; 
  // 3: Changed invisible entry
  private int dbCheckEntry(FeedUtils.Row row) throws Exception
  {    
    String url = row.getUrl();
    New newItem = getEntryByUrl(url);
    if (newItem != null)
    {
      String entryHash = getHash(row);
      String dbHash = newItem.getHash();
      if (!Boolean.valueOf(getForceUpdate()) && entryHash.equals(dbHash)) 
      {
        //No changes
        return EXISTING;
      }
      else //Entry has changed
      {
        auxEntryId = newItem.getNewId();
        if (!newItem.isDraft()) return UPDATED_VISIBLE;
        else return UPDATED_INVISIBLE;
      }
    }
    return ACCEPTED;
  }

  private String getHash(FeedUtils.Row row) throws Exception
  {
    String s = row.getDate() +
      (row.getHeadLine() == null ? "" : row.getHeadLine()) +
      (row.getSummary() == null ? "" : row.getSummary()) + 
      (row.getImageUrl() == null ? "" : row.getImageUrl() + 
      (row.getUrl() == null ? "" : row.getUrl()));
    MessageDigest m = MessageDigest.getInstance("MD5");
    byte[] data = s.getBytes();
    m.update(data, 0, data.length);
    BigInteger i = new BigInteger(1, m.digest());
    return String.format("%1$032X", i);        
  }

  private void dbInsertEntry(Node node, FeedUtils.Row row, 
    int feedEntryLifeSpan) throws Exception
  {
    New newItem = rowToNew(row, node, feedEntryLifeSpan);
    NewStoreOptions storeOptions = new NewStoreOptions();
    storeOptions.setCleanSummary(false);
    storeOptions.setCleanText(false);
    newItem = getNewsPort().storeNew(newItem, storeOptions);
    //Publish in feed node
    NewSection newSection = new NewSection();
    newSection.setNewId(newItem.getNewId());
    newSection.setSectionId(node.getNodeId());
    String sPrio = newItem.getStartDate().substring(2);
    Integer iPrio = Integer.parseInt(sPrio);
    newSection.setPriority(iPrio);
    getNewsPort().storeNewSection(newSection);    
  }

  private void dbUpdateEntry(Node node, FeedUtils.Row row, 
    int feedEntryLifeSpan, String newId, boolean isVisible) throws Exception
  {
    New newItem = rowToNew(row, node, feedEntryLifeSpan);
    newItem.setNewId(newId);
    newItem.setDraft(!isVisible);    
    NewStoreOptions storeOptions = new NewStoreOptions();
    storeOptions.setCleanSummary(false);
    storeOptions.setCleanText(false);
    getNewsPort().storeNew(newItem, storeOptions);
  }
  
  private New rowToNew(FeedUtils.Row row, Node node, int feedEntryLifeSpan) 
    throws Exception
  {
    New newItem = new New();
    if (row.getHeadLine() != null)
    {
      newItem.setHeadline(row.getHeadLine().length() > 1000 ? 
        row.getHeadLine().substring(0, 1000) :
        row.getHeadLine());
    }
    else
    {
      newItem.setHeadline("Accedir");
    }
    newItem.setCustomUrl(row.getUrl());
    newItem.setCustomUrlTarget("_blank");
    if (row.getSummary() != null)
    {
      newItem.setSummary(row.getSummary().length() > 4000 ? 
        row.getSummary().substring(0, 4000) :
        row.getSummary());
    }
    else
    {
      newItem.setSummary("");
    }
    newItem.setText(row.getSummary());
    newItem.setStartDate(row.getDate().substring(0, 8));
    newItem.setStartTime(row.getDate().substring(8, 14));
    String endDateTime = getEndDateTime(row.getDate(), feedEntryLifeSpan);
    newItem.setEndDate(endDateTime.substring(0, 8));
    newItem.setEndTime(endDateTime.substring(8, 14));    
    newItem.setDraft(false);
    if (row.getImageUrl() != null)
    {
      newItem.setIconUrl(row.getImageUrl());
    }
    else
    {
      newItem.setIconUrl(getPropertyValue(node, PROP_FN_ICONURL));
    }    
    newItem.setSource(getPropertyValue(node, PROP_FN_SOURCEID));    
    newItem.setHash(getHash(row));
    encodeEntry(newItem);    
    return newItem;
  }

  private void dbUpdateFeedRefreshDt(Node node, Date now) throws Exception
  {
    SimpleDateFormat dbFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    setPropertyValue(node, PROP_FN_LASTREFRESHDT, dbFormat.format(now));
    getCMSPort().storeNode(node);
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

  private List<New> getFeedEntries(String nodeId)
  {
    NewsFilter filter = new NewsFilter();
    filter.getSectionId().add(nodeId);
    filter.setExcludeNotPublished(true);
    filter.setMaxResults(Integer.MAX_VALUE);
    return getNewsPort().findNews(filter);
  }

  private New getEntryByUrl(String entryUrl)
  {    
    for (New newItem : auxFeedEntryList)
    {
      if (newItem.getCustomUrl() != null && 
        newItem.getCustomUrl().equals(entryUrl))
      {
        return newItem;
      }
    }
    return null;
  }
  
  private String getPropertyValue(Node node, String propertyName)
  {
    List<Property> propertyList = node.getProperty();
    for (Property property : propertyList)
    {
      if (property.getName().equals(propertyName))
      {
        return property.getValue().get(0);
      }
    }
    return null;
  }  

  private String setPropertyValue(Node node, String propertyName, 
    String propertyValue)
  {
    List<Property> propertyList = node.getProperty();
    for (Property property : propertyList)
    {
      if (property.getName().equals(propertyName))
      {
        property.getValue().clear();
        property.getValue().add(propertyValue);        
      }
    }
    return null;
  }    

  private String getEndDateTime(String startDateTime, int feedEntryLifeSpan)
  {
    try
    {
      SimpleDateFormat dbFormat = new SimpleDateFormat("yyyyMMddHHmmss");
      Date startDate = dbFormat.parse(startDateTime);
      Calendar cal = new GregorianCalendar();
      cal.setTime(startDate);
      cal.add(Calendar.DAY_OF_YEAR, feedEntryLifeSpan);
      Date endDate = cal.getTime();
      return dbFormat.format(endDate);
    }
    catch (ParseException ex)
    {
      return "99991231235959";
    }
  }
  
  private void encodeEntry(New newItem)
  {
    if (newItem.getHeadline() != null)
    {
      String headline = Unicode.encode(newItem.getHeadline(), true);
      newItem.setHeadline(headline.length() > 1000 ? 
        headline.substring(0, 1000) : headline);
    }
    if (newItem.getSummary() != null)
    {
      String summary = Unicode.encode(newItem.getSummary(), true);
      newItem.setSummary(summary.length() > 4000 ? 
        summary.substring(0, 4000) : summary);    
    }
    if (newItem.getText() != null)
    {
      newItem.setText(Unicode.encode(newItem.getText(), true));
    }   
  }
  
  private List<String> getFolderIdList()
  {
    List<String> folderIdList = new ArrayList();
    if (getFolderIds() != null)
    {
      folderIdList.addAll(Arrays.asList(getFolderIds().split(",")));    
    }
    return folderIdList;
  }

  private List<String> getInvalidImagePrefixList()
  {
    List<String> invalidImagePrefixList = new ArrayList();
    if (getInvalidImagePrefixes() != null)
    {
      invalidImagePrefixList.addAll(
        Arrays.asList(getInvalidImagePrefixes().split(",")));    
    }
    return invalidImagePrefixList;
  }  
  
}
