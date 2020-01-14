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
package org.santfeliu.feed.client;

import java.net.URL;
import java.util.List;
import org.matrix.feed.Entry;
import org.matrix.feed.EntryFilter;
import org.matrix.feed.Feed;
import org.matrix.feed.FeedFilter;
import org.matrix.feed.FeedFolder;
import org.matrix.feed.FeedFolderFilter;
import org.matrix.feed.FeedFolderView;
import org.matrix.feed.FeedManagerPort;
import org.matrix.feed.FeedManagerService;
import org.matrix.feed.Folder;
import org.matrix.feed.FolderFilter;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.ws.WSCallCache;

/**
 *
 * @author lopezrj
 */
public class FeedManagerClient implements FeedManagerPort
{
  FeedManagerPort port;
  private static WSCallCache cache;

  public FeedManagerClient()
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    init(wsDirectory, null, null);
  }

  public FeedManagerClient(URL wsDirectoryURL)
  {
    WSDirectory wsDirectory = WSDirectory.getInstance(wsDirectoryURL);
    init(wsDirectory, null, null);
  }

  public FeedManagerClient(String userId, String password)
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    init(wsDirectory, userId, password);
  }

  public FeedManagerClient(URL wsDirectoryURL, String userId, String password)
  {
    WSDirectory wsDirectory = WSDirectory.getInstance(wsDirectoryURL);
    init(wsDirectory, userId, password);
  }

  public FeedManagerClient(FeedManagerPort port)
  {
    this.port = port;
  }

  public static WSCallCache getCache()
  {
    if (cache == null)
    {
      cache = new WSCallCache("feed");
    }
    return cache;
  }  

  public FeedManagerPort getPort()
  {
    return port;
  }
  
  // NON-CACHED METHODS
  
  @Override
  public int countFolders(FolderFilter filter)
  {
    return port.countFolders(filter);
  }

  @Override
  public List<Folder> findFolders(FolderFilter filter)
  {
    return port.findFolders(filter);
  }

  @Override
  public Folder loadFolder(String folderId)
  {
    return port.loadFolder(folderId);
  }
  
  @Override
  public Folder storeFolder(Folder folder)
  {
    Folder f = port.storeFolder(folder);
    getCache().clear();
    return f;
  }

  @Override
  public boolean removeFolder(String folderId)
  {    
    boolean r = port.removeFolder(folderId);
    getCache().clear();
    return r;
  }
  
  @Override
  public int countFeeds(FeedFilter filter)
  {
    return port.countFeeds(filter);
  }

  @Override
  public List<Feed> findFeeds(FeedFilter filter)
  {
    return port.findFeeds(filter);
  }

  @Override
  public Feed loadFeed(String feedId)
  {
    return port.loadFeed(feedId);
  }

  @Override
  public Feed storeFeed(Feed feed)
  {    
    Feed f = port.storeFeed(feed);
    getCache().clear();
    return f;
  }

  @Override
  public boolean removeFeed(String feedId)
  {    
    boolean r = port.removeFeed(feedId);
    getCache().clear();
    return r;
  }  
  
  @Override
  public int countFeedFolders(FeedFolderFilter filter)
  {
    return port.countFeedFolders(filter);
  }

  @Override
  public List<FeedFolder> findFeedFolders(FeedFolderFilter filter)
  {
    return port.findFeedFolders(filter);
  }

  @Override
  public FeedFolder loadFeedFolder(String feedFolderId)
  {
    return port.loadFeedFolder(feedFolderId);
  }

  @Override
  public FeedFolder storeFeedFolder(FeedFolder feedFolder)
  {    
    FeedFolder ff = port.storeFeedFolder(feedFolder);
    getCache().clear();
    return ff;
  }

  @Override
  public boolean removeFeedFolder(String feedFolderId)
  {   
    boolean r = port.removeFeedFolder(feedFolderId);
    getCache().clear();
    return r;
  }  
  
  @Override
  public int countEntries(EntryFilter filter)
  {
    return port.countEntries(filter);
  }
  
  @Override
  public List<Entry> findEntries(EntryFilter filter)
  {
    return port.findEntries(filter);
  }

  @Override
  public Entry loadEntry(String entryId)
  {
    return port.loadEntry(entryId);
  }

  @Override
  public Entry storeEntry(Entry entry)
  {    
    Entry e = port.storeEntry(entry);
    getCache().clear();
    return e;
  }

  @Override
  public boolean removeEntry(String entryId)
  {   
    boolean r = port.removeEntry(entryId);
    getCache().clear();
    return r;
  }  
  
  @Override
  public List<FeedFolderView> findFeedFolderViews(FeedFolderFilter filter)
  {
    return port.findFeedFolderViews(filter);
  }
  
// CACHED METHODS  
    
  public int countFoldersFromCache(FolderFilter filter)
  {
    return (Integer)getCache().getCallResult(port, "countFolders", 
      new Object[]{filter});
  }

  public List<Folder> findFoldersFromCache(FolderFilter filter)
  {
    return (List<Folder>)getCache().getCallResult(port, "findFolders", 
      new Object[]{filter});
  }

  public Folder loadFolderFromCache(String folderId)
  {
    return (Folder)getCache().getCallResult(port, "loadFolder", 
      new Object[]{folderId});
  }

  public int countFeedsFromCache(FeedFilter filter)
  {
    return (Integer)getCache().getCallResult(port, "countFeeds", 
      new Object[]{filter});    
  }

  public List<Feed> findFeedsFromCache(FeedFilter filter)
  {
    return (List<Feed>)getCache().getCallResult(port, "findFeeds", 
      new Object[]{filter});    
  }

  public Feed loadFeedFromCache(String feedId)
  {
    return (Feed)getCache().getCallResult(port, "loadFeed", 
      new Object[]{feedId});    
  }

  public int countFeedFoldersFromCache(FeedFolderFilter filter)
  {
    return (Integer)getCache().getCallResult(port, "countFeedFolders", 
      new Object[]{filter});    
  }

  public List<FeedFolder> findFeedFoldersFromCache(FeedFolderFilter filter)
  {
    return (List<FeedFolder>)getCache().getCallResult(port, "findFeedFolders", 
      new Object[]{filter});    
  }

  public FeedFolder loadFeedFolderFromCache(String feedFolderId)
  {
    return (FeedFolder)getCache().getCallResult(port, "loadFeedFolder", 
      new Object[]{feedFolderId});        
  }

  public int countEntriesFromCache(EntryFilter filter)
  {
    return (Integer)getCache().getCallResult(port, "countEntries", 
      new Object[]{filter}, new Object[]{getModifiedFilter(filter)});
  }
  
  public List<Entry> findEntriesFromCache(EntryFilter filter)
  {
    return (List<Entry>)getCache().getCallResult(port, "findEntries", 
      new Object[]{filter}, new Object[]{getModifiedFilter(filter)});    
  }

  public Entry loadEntryFromCache(String entryId)
  {
    return (Entry)getCache().getCallResult(port, "loadEntry", 
      new Object[]{entryId});        
  }

  public List<FeedFolderView> findFeedFolderViewsFromCache(FeedFolderFilter 
    filter)
  {
    return (List<FeedFolderView>)getCache().getCallResult(port, 
      "findFeedFolderViews", new Object[]{filter});            
  }
  
  private void init(WSDirectory wsDirectory, String userId, String password)
  {
    WSEndpoint endpoint = wsDirectory.getEndpoint(FeedManagerService.class);
    port = endpoint.getPort(FeedManagerPort.class, userId, password);
  }  
  
  private EntryFilter getModifiedFilter(EntryFilter filter)
  {
    EntryFilter auxFilter = new EntryFilter();
    auxFilter.getEntryId().addAll(filter.getEntryId());
    auxFilter.getFeedId().addAll(filter.getFeedId());
    auxFilter.setTitle(filter.getTitle());
    auxFilter.setUrl(filter.getUrl());
    auxFilter.setStartPubDateTime(filter.getStartPubDateTime() == null ? null : 
      filter.getStartPubDateTime().substring(0, 12));
    auxFilter.setEndPubDateTime(filter.getEndPubDateTime() == null ? null : 
      filter.getEndPubDateTime().substring(0, 12));
    auxFilter.setExcludeInvisible(filter.isExcludeInvisible());
    auxFilter.setFirstResult(filter.getFirstResult());
    auxFilter.setMaxResults(filter.getMaxResults());
    return auxFilter;
  }  

}
