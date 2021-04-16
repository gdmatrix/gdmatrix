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
package org.santfeliu.feed.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import org.matrix.feed.Entry;
import org.matrix.feed.EntryFilter;
import org.matrix.feed.Feed;
import org.matrix.feed.FeedFilter;
import org.matrix.feed.FeedFolder;
import org.matrix.feed.FeedFolderFilter;
import org.matrix.feed.FeedFolderView;
import org.matrix.feed.FeedManagerPort;
import org.matrix.feed.Folder;
import org.matrix.feed.FolderFilter;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.util.TextUtils;
import org.santfeliu.util.enc.Unicode;
import org.santfeliu.ws.WSUtils;
import org.santfeliu.ws.annotations.Initializer;
import org.santfeliu.ws.annotations.MultiInstance;

/**
 *
 * @author lopezrj
 */
@WebService(endpointInterface = "org.matrix.feed.FeedManagerPort")
@HandlerChain(file="handlers.xml")
@MultiInstance
public class FeedManager implements FeedManagerPort
{
  private static final Logger LOGGER = Logger.getLogger("Feed");

  @Resource
  WebServiceContext wsContext;

  @PersistenceContext(unitName="feed_ri")
  public EntityManager entityManager;

  @Initializer
  public void initialize(String endpointName)
  {    
  }
  
  @Override
  public Folder loadFolder(String folderId)
  {
    LOGGER.log(Level.INFO, "loadFolder {0}", new Object[]{folderId});
    if (folderId == null)
      throw new WebServiceException("feed:FOLDERID_IS_MANDATORY");

    WSEndpoint endpoint = getWSEndpoint();
    DBFolder dbFolder = entityManager.find(DBFolder.class,
      endpoint.toLocalId(Folder.class, folderId));
    if (dbFolder == null)
      throw new WebServiceException("feed:FOLDER_NOT_FOUND");
    Folder folder = new Folder();
    dbFolder.copyTo(folder, endpoint);
    return folder;
  }

  @Override
  public Folder storeFolder(Folder folder)
  {
    String folderId = folder.getFolderId();
    LOGGER.log(Level.INFO, "storeFolder {0}", new Object[]{folderId});
    WSEndpoint endpoint = getWSEndpoint();
    if (isFolderCycleDetected(folder.getFolderId(), folder.getParentFolderId()))
      throw new WebServiceException("feed:FOLDER_CYCLE_DETECTED");
    if (folderId == null) //insert
    {
      DBFolder dbFolder = new DBFolder(folder, endpoint);
      entityManager.persist(dbFolder);
      dbFolder.copyTo(folder, endpoint);
    }
    else //update
    {
      String localFolderId = endpoint.toLocalId(Folder.class, folderId);
      DBFolder dbFolder = entityManager.find(DBFolder.class, localFolderId);
      if (dbFolder == null)
        throw new WebServiceException("feed:FOLDER_NOT_FOUND");
      dbFolder.copyFrom(folder, endpoint);
      entityManager.merge(dbFolder);
    }
    return folder;
  }

  @Override
  public boolean removeFolder(String folderId)
  {
    LOGGER.log(Level.INFO, "removeFolder {0}", new Object[]{folderId});
    if (folderId == null)
      throw new WebServiceException("feed:FOLDERID_IS_MANDATORY");

    WSEndpoint endpoint = getWSEndpoint();
    DBFolder dbFolder = null;
    String localFolderId = endpoint.toLocalId(Folder.class, folderId);
    try
    {
      dbFolder = entityManager.getReference(DBFolder.class, localFolderId);
    }
    catch (EntityNotFoundException ex)
    {
      return false;
    }
    
    Query query = entityManager.createNamedQuery("removeFeedFolders");
    query.setParameter("folderId", localFolderId);
    query.setParameter("feedId", null);
    query.executeUpdate();
    entityManager.remove(dbFolder);
    return true;
  }

  @Override
  public int countFolders(FolderFilter filter)
  {
    LOGGER.log(Level.INFO, "countFolders");
    WSEndpoint endpoint = getWSEndpoint();
    Query query = entityManager.createNamedQuery("countFolders");
    applyFolderFilter(query, filter, endpoint);
    Number number = (Number)query.getSingleResult();
    return number.intValue();
  }

  @Override
  public List<Folder> findFolders(FolderFilter filter)
  {
    LOGGER.log(Level.INFO, "findFolders");
    WSEndpoint endpoint = getWSEndpoint();
    List<Folder> folderList = new ArrayList<Folder>();
    Query query = entityManager.createNamedQuery("findFolders");
    applyFolderFilter(query, filter, endpoint);
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
    List<DBFolder> dbFolderList = query.getResultList();
    for (DBFolder dbFolder : dbFolderList)
    {
      Folder folder = new Folder();
      dbFolder.copyTo(folder, endpoint);
      folderList.add(folder);
    }
    return folderList;
  }

  @Override
  public Feed loadFeed(String feedId)
  {
    LOGGER.log(Level.INFO, "loadFeed {0}", new Object[]{feedId});
    if (feedId == null)
      throw new WebServiceException("feed:FEEDID_IS_MANDATORY");

    WSEndpoint endpoint = getWSEndpoint();
    DBFeed dbFeed = entityManager.find(DBFeed.class,
      endpoint.toLocalId(Feed.class, feedId));
    if (dbFeed == null) throw new WebServiceException("feed:FEED_NOT_FOUND");
    Feed feed = new Feed();
    dbFeed.copyTo(feed, endpoint);
    return feed;
  }

  @Override
  public Feed storeFeed(Feed feed)
  {
    String feedId = feed.getFeedId();
    LOGGER.log(Level.INFO, "storeFeed {0}", new Object[]{feedId});
    WSEndpoint endpoint = getWSEndpoint();
    if (feedId == null) //insert
    {
      String now = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
      feed.setRefreshDateTime(now);
      DBFeed dbFeed = new DBFeed(feed, endpoint);
      entityManager.persist(dbFeed);
      dbFeed.copyTo(feed, endpoint);
    }
    else //update
    {
      String localFeedId = endpoint.toLocalId(Feed.class, feedId);
      DBFeed dbFeed = entityManager.find(DBFeed.class, localFeedId);
      if (dbFeed == null)
        throw new WebServiceException("feed:FEED_NOT_FOUND");
      dbFeed.copyFrom(feed, endpoint);
      entityManager.merge(dbFeed);
    }
    return feed;
  }

  @Override
  public boolean removeFeed(String feedId)
  {
    LOGGER.log(Level.INFO, "removeFeed {0}", new Object[]{feedId});
    if (feedId == null)
      throw new WebServiceException("feed:FEEDID_IS_MANDATORY");

    WSEndpoint endpoint = getWSEndpoint();
    DBFeed dbFeed = null;
    String localFeedId = endpoint.toLocalId(Feed.class, feedId);
    try
    {
      dbFeed = entityManager.getReference(DBFeed.class, localFeedId);
    }
    catch (EntityNotFoundException ex)
    {
      return false;
    }
    Query query = entityManager.createNamedQuery("removeFeedFolders");
    query.setParameter("folderId", null);
    query.setParameter("feedId", localFeedId);
    query.executeUpdate();
    query = entityManager.createNamedQuery("removeFeedsEntries");
    query.setParameter("feedId", "," + localFeedId + ",");
    query.executeUpdate();
    entityManager.remove(dbFeed);
    return true;
  }

  @Override
  public int countFeeds(FeedFilter filter)
  {
    LOGGER.log(Level.INFO, "countFeeds");
    WSEndpoint endpoint = getWSEndpoint();
    Query query = entityManager.createNamedQuery("countFeeds");
    applyFeedFilter(query, filter, endpoint);
    Number number = (Number)query.getSingleResult();
    return number.intValue();
  }

  @Override
  public List<Feed> findFeeds(FeedFilter filter)
  {
    LOGGER.log(Level.INFO, "findFeeds");
    WSEndpoint endpoint = getWSEndpoint();
    List<Feed> feedList = new ArrayList<Feed>();
    Query query = entityManager.createNamedQuery("findFeeds");
    applyFeedFilter(query, filter, endpoint);
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
    List<DBFeed> dbFeedList = query.getResultList();
    for (DBFeed dbFeed : dbFeedList)
    {
      Feed feed = new Feed();
      dbFeed.copyTo(feed, endpoint);
      feedList.add(feed);
    }
    return feedList;
  }

  @Override
  public int countFeedFolders(FeedFolderFilter filter)
  {
    LOGGER.log(Level.INFO, "countFeedFolders");
    WSEndpoint endpoint = getWSEndpoint();
    Query query = entityManager.createNamedQuery("countFeedFolders");
    applyFeedFolderFilter(query, filter, endpoint);
    Number number = (Number)query.getSingleResult();
    return number.intValue();
  }

  @Override
  public List<FeedFolder> findFeedFolders(FeedFolderFilter filter)
  {
    LOGGER.log(Level.INFO, "findFeedFolders");
    WSEndpoint endpoint = getWSEndpoint();
    List<FeedFolder> feedFolderList = new ArrayList<FeedFolder>();
    Query query = entityManager.createNamedQuery("findFeedFolders");
    applyFeedFolderFilter(query, filter, endpoint);
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
    List<DBFeedFolder> dbFeedFolderList = query.getResultList();
    for (DBFeedFolder dbFeedFolder : dbFeedFolderList)
    {
      FeedFolder feedFolder = new FeedFolder();
      dbFeedFolder.copyTo(feedFolder, endpoint);
      feedFolderList.add(feedFolder);
    }
    return feedFolderList;
  }

  @Override
  public List<FeedFolderView> findFeedFolderViews(FeedFolderFilter filter)
  {
    LOGGER.log(Level.INFO, "findFeedFolderViews");
    WSEndpoint endpoint = getWSEndpoint();
    List<FeedFolderView> feedFolderViewList = new ArrayList<FeedFolderView>();
    Query query = entityManager.createNamedQuery("findFeedFolderViews");
    applyFeedFolderFilter(query, filter, endpoint);
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
    List<Object[]> rowList = query.getResultList();
    if (rowList != null)
    {
      for (Object[] row : rowList)
      {
        FeedFolderView ffv = new FeedFolderView();
        ffv.setFeedId(String.valueOf(row[0]));
        ffv.setFeedName((String)row[1]);
        ffv.setFeedUrl((String)row[2]);
        ffv.setFeedType((String)row[3]);
        ffv.setFolderId(String.valueOf(row[4]));
        ffv.setFolderName((String)row[5]);
        ffv.setFeedFolderId(ffv.getFeedId() + ";" + ffv.getFolderId());
        feedFolderViewList.add(ffv);
      }
    }
    return feedFolderViewList;
  }

  @Override
  public FeedFolder loadFeedFolder(String feedFolderId)
  {
    LOGGER.log(Level.INFO, "loadFeedFolder {0}", new Object[]{feedFolderId});
    if (feedFolderId == null)
      throw new WebServiceException("feed:FEEDFOLDERID_IS_MANDATORY");

    WSEndpoint endpoint = getWSEndpoint();
    DBFeedFolder dbFeedFolder = entityManager.find(DBFeedFolder.class,
      new DBFeedFolderPK(feedFolderId));
    if (dbFeedFolder == null)
      throw new WebServiceException("feed:FEEDFOLDER_NOT_FOUND");
    FeedFolder feedFolder = new FeedFolder();
    dbFeedFolder.copyTo(feedFolder, endpoint);
    return feedFolder;
  }

  @Override
  public FeedFolder storeFeedFolder(FeedFolder feedFolder)
  {
    String feedFolderId = feedFolder.getFeedFolderId();
    LOGGER.log(Level.INFO, "storeFeedFolder {0}", new Object[]{feedFolderId});
    WSEndpoint endpoint = getWSEndpoint();
    if (feedFolderId == null) //insert
    {
      DBFeedFolder dbFeedFolder = new DBFeedFolder(feedFolder, endpoint);
      entityManager.persist(dbFeedFolder);
      dbFeedFolder.copyTo(feedFolder, endpoint);
    }
    else //update
    {
      String localFeedFolderId =
        endpoint.toLocalId(FeedFolder.class, feedFolderId);
      DBFeedFolder dbFeedFolder =
        entityManager.find(DBFeedFolder.class, localFeedFolderId);
      if (dbFeedFolder == null)
        throw new WebServiceException("feed:FEEDFOLDER_NOT_FOUND");
      dbFeedFolder.copyFrom(feedFolder, endpoint);
      entityManager.merge(dbFeedFolder);
    }
    return feedFolder;
  }

  @Override
  public boolean removeFeedFolder(String feedFolderId)
  {
    LOGGER.log(Level.INFO, "removeFeedFolder {0}", new Object[]{feedFolderId});
    if (feedFolderId == null)
      throw new WebServiceException("feed:FEEDFOLDERID_IS_MANDATORY");

    WSEndpoint endpoint = getWSEndpoint();
    String localFeedFolderId =
      endpoint.toLocalId(FeedFolder.class, feedFolderId);

    DBFeedFolder dbFeedFolder = null;
    try
    {
      DBFeedFolderPK pk = new DBFeedFolderPK(localFeedFolderId);
      dbFeedFolder =
        entityManager.getReference(DBFeedFolder.class, pk);
    }
    catch (EntityNotFoundException ex)
    {
      return false;
    }
    entityManager.remove(dbFeedFolder);
    return true;
  }

  @Override
  public Entry loadEntry(String entryId)
  {
    LOGGER.log(Level.INFO, "loadEntry {0}", new Object[]{entryId});
    if (entryId == null)
      throw new WebServiceException("feed:ENTRYID_IS_MANDATORY");

    WSEndpoint endpoint = getWSEndpoint();
    DBEntry dbEntry = entityManager.find(DBEntry.class,
      endpoint.toLocalId(Entry.class, entryId));
    if (dbEntry == null)
      throw new WebServiceException("feed:ENTRY_NOT_FOUND");
    Entry entry = new Entry();
    dbEntry.copyTo(entry, endpoint);
    decodeEntry(entry);
    return entry;
  }

  @Override
  public Entry storeEntry(Entry entry)
  {
    String entryId = entry.getEntryId();
    LOGGER.log(Level.INFO, "storeEntry {0}", new Object[]{entryId});
    WSEndpoint endpoint = getWSEndpoint();
    encodeEntry(entry);
    if (entryId == null) //insert
    {
      DBEntry dbEntry = new DBEntry(entry, endpoint);
      entityManager.persist(dbEntry);
      dbEntry.copyTo(entry, endpoint);
    }
    else //update
    {
      String localEntryId = endpoint.toLocalId(Entry.class, entryId);
      DBEntry dbEntry = entityManager.find(DBEntry.class, localEntryId);
      if (dbEntry == null)
        throw new WebServiceException("feed:ENTRY_NOT_FOUND");
      dbEntry.copyFrom(entry, endpoint);
      entityManager.merge(dbEntry);
    }
    return entry;
  }

  @Override
  public boolean removeEntry(String entryId)
  {
    LOGGER.log(Level.INFO, "removeEntry {0}", new Object[]{entryId});
    if (entryId == null)
      throw new WebServiceException("feed:ENTRYID_IS_MANDATORY");

    WSEndpoint endpoint = getWSEndpoint();
    DBEntry dbEntry = null;
    String localEntryId = endpoint.toLocalId(Entry.class, entryId);
    try
    {
      dbEntry = entityManager.getReference(DBEntry.class, localEntryId);
    }
    catch (EntityNotFoundException ex)
    {
      return false;
    }
    entityManager.remove(dbEntry);
    return true;
  }

  @Override
  public int countEntries(EntryFilter filter)
  {
    LOGGER.log(Level.INFO, "countEntries");
    WSEndpoint endpoint = getWSEndpoint();
    Query query = entityManager.createNamedQuery("countEntries");
    applyEntryFilter(query, filter, endpoint);
    Number number = (Number)query.getSingleResult();
    return number.intValue();
  }

  @Override
  public List<Entry> findEntries(EntryFilter filter)
  {
    LOGGER.log(Level.INFO, "findEntries");
    WSEndpoint endpoint = getWSEndpoint();
    List<Entry> entryList = new ArrayList<Entry>();
    Query query = entityManager.createNamedQuery("findEntries");
    applyEntryFilter(query, filter, endpoint);
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
    List<DBEntry> dbEntryList = query.getResultList();
    for (DBEntry dbEntry : dbEntryList)
    {
      Entry entry = new Entry();
      dbEntry.copyTo(entry, endpoint);
      decodeEntry(entry);
      entryList.add(entry);
    }
    return entryList;
  }

  //PRIVATE METHODS: All id fields are local

  private void encodeEntry(Entry entry)
  {
    entry.setTitle(Unicode.encode(entry.getTitle()));
    entry.setDescription(Unicode.encode(entry.getDescription()));    
  }
  
  private void decodeEntry(Entry entry)
  {
    entry.setTitle(Unicode.decode(entry.getTitle()));
    entry.setDescription(Unicode.decode(entry.getDescription()));        
  }
  
  private void applyFolderFilter(Query query, FolderFilter filter,
    WSEndpoint endpoint)
  {
    List<String> localFolderIdList = endpoint.toLocalIds(Folder.class,
      filter.getFolderId());
    query.setParameter("folderId", listToString(localFolderIdList));
    query.setParameter("name", conditionalUpperCase(filter.getName(), false));
    query.setParameter("alias", filter.getAlias());
    query.setParameter("parentFolderId", filter.getParentFolderId());
  }

  private void applyFeedFilter(Query query, FeedFilter filter,
    WSEndpoint endpoint)
  {
    List<String> localFeedIdList = endpoint.toLocalIds(Feed.class,
      filter.getFeedId());
    query.setParameter("feedId", listToString(localFeedIdList));
    query.setParameter("name", conditionalUpperCase(filter.getName(), false));
    query.setParameter("url", conditionalUpperCase(filter.getUrl(), false));
    query.setParameter("type", filter.getType());
  }

  private void applyFeedFolderFilter(Query query, FeedFolderFilter filter,
    WSEndpoint endpoint)
  {
    List<String> localFolderIdList = endpoint.toLocalIds(Folder.class,
      filter.getFolderId());
    List<String> localFeedIdList = endpoint.toLocalIds(Feed.class,
      filter.getFeedId());
    query.setParameter("folderId", listToString(localFolderIdList));
    query.setParameter("feedId", listToString(localFeedIdList));
  }

  private void applyEntryFilter(Query query, EntryFilter filter,
    WSEndpoint endpoint)
  {
    List<String> localEntryIdList = endpoint.toLocalIds(Entry.class,
      filter.getEntryId());
    List<String> localFeedIdList = endpoint.toLocalIds(Feed.class,
      filter.getFeedId());
    query.setParameter("entryId", listToString(localEntryIdList));
    query.setParameter("feedId", listToString(localFeedIdList));
    query.setParameter("title", conditionalUpperCase(filter.getTitle(), false));
    query.setParameter("url", conditionalUpperCase(filter.getUrl(), false));    
    query.setParameter("startDateTime", filter.getStartPubDateTime());
    query.setParameter("endDateTime", filter.getEndPubDateTime());
    query.setParameter("excludeInvisible", 
      filter.isExcludeInvisible() ? "Y" : "N");
  }
  
  private boolean isFolderCycleDetected(String folderId, String newParentFolderId)
  {
    if (newParentFolderId == null || newParentFolderId.isEmpty()) return false;
    if (newParentFolderId.equals(folderId))
    {
      return true;
    }
    else
    {      
      DBFolder dbNewParentFolder = entityManager.find(DBFolder.class, newParentFolderId);
      if (dbNewParentFolder == null)
        throw new WebServiceException("feed:PARENT_FOLDER_NOT_FOUND");
      return isFolderCycleDetected(folderId, dbNewParentFolder.getParentFolderId());
    }
  }  

  private String conditionalUpperCase(String value, boolean caseSensitive)
  {
    if (value == null || value.length() == 0)
    {
      return null;
    }
    else if (caseSensitive)
    {
      return "%" + value + "%";
    }
    else
    {
      return "%" + value.toUpperCase() + "%";
    }
  }

  private String listToString(List<String> list)
  {
    String result = TextUtils.collectionToString(list, ",");
    if (result != null) result = "," + result + ",";
    return result;
  }

  private WSEndpoint getWSEndpoint()
  {
    String endpointName = WSUtils.getServletAdapter(wsContext).getName();
    return WSDirectory.getInstance().getEndpoint(endpointName);
  }

}
