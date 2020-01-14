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

import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.feed.FeedFolder;
import org.matrix.feed.FeedFolderFilter;
import org.matrix.feed.FeedFolderView;
import org.matrix.feed.Folder;
import org.santfeliu.web.obj.PageBean;
import org.santfeliu.web.obj.PageFinder;

/**
 *
 * @author unknown
 */
public class FolderFeedsBean extends PageBean
{
  private List<FeedFolderView> rows;
  private FeedFolder editingFeed;

  private int firstRowIndex;

  public FolderFeedsBean()
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

  public FeedFolder getEditingFeed()
  {
    return editingFeed;
  }

  public void setEditingFeed(FeedFolder editingFeed)
  {
    this.editingFeed = editingFeed;
  }

  public List<FeedFolderView> getRows()
  {
    return rows;
  }

  public void setRows(List<FeedFolderView> rows)
  {
    this.rows = rows;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  public String show()
  {
    return "folder_feeds";
  }

  public String showFeed()
  {
    return getControllerBean().showObject("Feed",
       (String)getValue("#{row.feedId}"));
  }

  public List<SelectItem> getFeedItems()
  {
    FeedBean feedBean = (FeedBean)getBean("feedBean");
    return feedBean.getSelectItems(editingFeed.getFeedId());
  }

  public String searchFeed()
  {
    return getControllerBean().searchObject("Feed",
      "#{folderFeedsBean.editingFeed.feedId}");
  }

  public String createFeedFolder()
  {
    editingFeed = new FeedFolder();
    editingFeed.setFolderId(getObjectId());
    return null;
  }

  public String editFeedFolder()
  {
    try
    {
      FeedFolderView row =
        (FeedFolderView)getExternalContext().getRequestMap().get("row");
      String feedFolderId = row.getFeedId() + ";" + row.getFolderId();
      editingFeed = FeedConfigBean.getPort().loadFeedFolderFromCache(feedFolderId);
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String storeFeedFolder()
  {
    try
    {
      if (editingFeed.getFeedId() == null ||
        editingFeed.getFeedId().trim().length() == 0)
      {
        throw new Exception("VALUE_IS_MANDATORY");
      }
      else
      {
        try
        {          
          FeedConfigBean.getPort().storeFeedFolder(editingFeed);
        }
        catch (Exception ex)
        {
          throw new Exception("INVALID_OPERATION");
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    finally
    {
      String auxFeedFolderId = editingFeed.getFeedId() + ";" + getObjectId();
      editingFeed = null;
      load();
      firstRowIndex = PageFinder.findFirstRowIndex(rows, getPageSize(),
        "feedFolderId", auxFeedFolderId);
    }
    return null;
  }

  public String cancelFeedFolder()
  {
    editingFeed = null;
    return null;
  }

  public String removeFeedFolder()
  {
    try
    {
      FeedFolderView row =
        (FeedFolderView)getExternalContext().getRequestMap().get("row");
      FeedConfigBean.getPort().removeFeedFolder(row.getFeedFolderId());
      editingFeed = null;
      load();
      if (firstRowIndex >= rows.size() && firstRowIndex > 0)
      {
        firstRowIndex -= getPageSize();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  private void load()
  {
    try
    {
      if (!isNew())
      {
        FeedFolderFilter filter = new FeedFolderFilter();
        filter.getFolderId().add(getFolder().getFolderId());
        rows = FeedConfigBean.getPort().findFeedFolderViewsFromCache(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private Folder getFolder()
  {
    FolderMainBean folderMainBean = (FolderMainBean)getBean("folderMainBean");
    return folderMainBean.getFolder();
  }

}
