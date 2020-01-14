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

import java.util.Collection;
import org.matrix.feed.Feed;
import org.matrix.util.WSEndpoint;

/**
 *
 * @author unknown
 */
public class DBFeed extends Feed
{
  private Collection<DBFeedFolder> feedFolders;
  private Collection<DBEntry> entries;

  public DBFeed()
  {
  }

  public DBFeed(Feed feed, WSEndpoint endpoint)
  {
    copyFrom(feed, endpoint);
  }

  public Collection<DBEntry> getEntries()
  {
    return entries;
  }

  public void setEntries(Collection<DBEntry> entries)
  {
    this.entries = entries;
  }

  public Collection<DBFeedFolder> getFeedFolders()
  {
    return feedFolders;
  }

  public void setFeedFolders(Collection<DBFeedFolder> feedFolders)
  {
    this.feedFolders = feedFolders;
  }

  public void copyTo(Feed feed, WSEndpoint endpoint)
  {
    feed.setFeedId(endpoint.toGlobalId(Feed.class, this.getFeedId()));
    feed.setName(this.getName());
    feed.setUrl(this.getUrl());
    feed.setInternalUrl(this.getInternalUrl());
    feed.setType(this.getType());
    feed.setRefreshInterval(this.getRefreshInterval());
    feed.setRefreshDateTime(this.getRefreshDateTime());
    feed.setEntryLifeSpan(this.getEntryLifeSpan());
    feed.setIconUrl(this.getIconUrl());
  }

  public void copyFrom(Feed feed, WSEndpoint endpoint)
  {
    setFeedId(endpoint.toLocalId(Feed.class, feed.getFeedId()));
    setName(feed.getName());
    setUrl(feed.getUrl());
    setInternalUrl(feed.getInternalUrl());
    setType(feed.getType());
    setRefreshInterval(feed.getRefreshInterval());
    setRefreshDateTime(feed.getRefreshDateTime());
    setEntryLifeSpan(feed.getEntryLifeSpan());
    setIconUrl(feed.getIconUrl());
  }

}
