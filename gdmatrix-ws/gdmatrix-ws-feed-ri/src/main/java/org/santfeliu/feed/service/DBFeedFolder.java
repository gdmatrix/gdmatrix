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

import org.matrix.feed.FeedFolder;
import org.matrix.util.WSEndpoint;

/**
 *
 * @author unknown
 */
public class DBFeedFolder extends FeedFolder
{
  private DBFeed feed;
  private DBFolder folder;

  public DBFeedFolder()
  {
  }

  public DBFeedFolder(FeedFolder feedFolder, WSEndpoint endpoint)
  {
    copyFrom(feedFolder, endpoint);
  }

  public DBFeed getFeed()
  {
    return feed;
  }

  public void setFeed(DBFeed feed)
  {
    this.feed = feed;
  }

  public DBFolder getFolder()
  {
    return folder;
  }

  public void setFolder(DBFolder folder)
  {
    this.folder = folder;
  }

  public void copyTo(FeedFolder feedFolder, WSEndpoint endpoint)
  {
    feedFolder.setFeedFolderId(this.getFeedId() + ";" + this.getFolderId());
    feedFolder.setFeedId(this.getFeedId());
    feedFolder.setFolderId(this.getFolderId());
  }
  
  public void copyFrom(FeedFolder feedFolder, WSEndpoint endpoint)
  {
    setFeedFolderId(feedFolder.getFeedFolderId());
    setFeedId(feedFolder.getFeedId());
    setFolderId(feedFolder.getFolderId());
  }

}
