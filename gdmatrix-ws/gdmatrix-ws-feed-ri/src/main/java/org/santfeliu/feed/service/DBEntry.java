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

import org.matrix.feed.Entry;
import org.matrix.feed.Feed;
import org.matrix.util.WSEndpoint;

/**
 *
 * @author unknown
 */
public class DBEntry extends Entry
{
  private byte[] baDescription;
  private String strVisible;
  
  private DBFeed entryFeed;

  public DBEntry()
  {
  }

  public DBEntry(Entry entry, WSEndpoint endpoint)
  {
    copyFrom(entry, endpoint);
  }

  public byte[] getBaDescription()
  {
    return baDescription;
  }

  public void setBaDescription(byte[] baDescription)
  {
    this.baDescription = baDescription;
  }

  public String getStrVisible()
  {
    return strVisible;
  }

  public void setStrVisible(String strVisible)
  {
    this.strVisible = strVisible;
  }

  public DBFeed getEntryFeed()
  {
    return entryFeed;
  }

  public void setEntryFeed(DBFeed entryFeed)
  {
    this.entryFeed = entryFeed;
  }

  public void copyTo(Entry entry, WSEndpoint endpoint)
  {
    entry.setEntryId(endpoint.toGlobalId(Entry.class, this.getEntryId()));
    entry.setTitle(this.getTitle());
    entry.setUrl(this.getUrl());
    if ((baDescription != null) && (baDescription.length > 0))
    {
      entry.setDescription(new String(baDescription));
    }
    else
    {
      entry.setDescription(this.getDescription());
    }
    entry.setPubDateTime(this.getPubDateTime());
    entry.setFeedId(endpoint.toGlobalId(Feed.class, this.getFeedId()));
    entry.setHash(this.getHash());
    entry.setVisible(this.getStrVisible() == null || 
      "Y".equals(this.getStrVisible()));
  }

  public void copyFrom(Entry entry, WSEndpoint endpoint)
  {
    setEntryId(endpoint.toLocalId(Entry.class, entry.getEntryId()));
    setTitle(entry.getTitle());
    setUrl(entry.getUrl());
    if (entry.getDescription() != null)
      baDescription = entry.getDescription().getBytes();
    setDescription(entry.getDescription());
    setPubDateTime(entry.getPubDateTime());
    setFeedId(endpoint.toLocalId(Feed.class, entry.getFeedId()));
    setHash(entry.getHash());
    setVisible(entry.isVisible());
    setStrVisible(entry.isVisible() ? "Y" : "N");    
  }

}
