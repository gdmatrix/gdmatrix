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

import java.text.SimpleDateFormat;
import java.util.List;
import org.matrix.feed.Entry;
import org.matrix.feed.EntryFilter;
import org.matrix.feed.Feed;
import org.santfeliu.web.obj.PageBean;

/**
 *
 * @author unknown
 */
public class FeedEntriesBean extends PageBean
{
  private List<Entry> rows;

  private int firstRowIndex;

  public FeedEntriesBean()
  {
    load();
  }

  public int getFirstRowIndex()
  {
    return firstRowIndex;
  }

  public void setFirstRowIndex(int firstRowIndex)
  {
    this.firstRowIndex = firstRowIndex;
  }

  public List<Entry> getRows()
  {
    return rows;
  }

  public void setRows(List<Entry> rows)
  {
    this.rows = rows;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  public String show()
  {
    return "feed_entries";
  }

  public String switchEntryVisibility()  
  {
    try
    {
      Entry row = (Entry)getExternalContext().getRequestMap().get("row");
      row.setVisible(!row.isVisible());
      FeedConfigBean.getPort().storeEntry(row);
      load();      
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String getRowDate() throws Exception
  {
    try
    {
      Entry row = (Entry)getExternalContext().getRequestMap().get("row");
      SimpleDateFormat sysFormat = new SimpleDateFormat("yyyyMMddHHmmss");
      SimpleDateFormat humanFormat =
        new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
      return humanFormat.format(sysFormat.parse(row.getPubDateTime()));
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "";
  }

  private void load()
  {
    try
    {
      if (!isNew())
      {
        EntryFilter filter = new EntryFilter();
        filter.getFeedId().add(getFeed().getFeedId());
        rows = FeedConfigBean.getPort().findEntriesFromCache(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private Feed getFeed()
  {
    FeedMainBean feedMainBean = (FeedMainBean)getBean("feedMainBean");
    return feedMainBean.getFeed();
  }

}
