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
import org.matrix.feed.Feed;
import org.matrix.feed.FeedFolder;
import org.matrix.feed.FeedFolderFilter;
import org.matrix.feed.FeedFolderView;
import org.santfeliu.web.obj.PageBean;
import org.santfeliu.web.obj.PageFinder;

/**
 *
 * @author unknown
 */
public class FeedFoldersBean extends PageBean
{
  private List<FeedFolderView> rows;
  private FeedFolder editingFolder;

  private int firstRowIndex;

  public FeedFoldersBean()
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

  public FeedFolder getEditingFolder()
  {
    return editingFolder;
  }

  public void setEditingFolder(FeedFolder editingFolder)
  {
    this.editingFolder = editingFolder;
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
    return "feed_folders";
  }

  public String showFolder()
  {
    return getControllerBean().showObject("Folder",
       (String)getValue("#{row.folderId}"));
  }

  public List<SelectItem> getFolderItems()
  {
    FolderBean folderBean = (FolderBean)getBean("folderBean");
    return folderBean.getSelectItems(editingFolder.getFolderId());
  }

  public String searchFolder()
  {
    return getControllerBean().searchObject("Folder",
      "#{feedFoldersBean.editingFolder.folderId}");
  }

  public String createFeedFolder()
  {
    editingFolder = new FeedFolder();
    editingFolder.setFeedId(getObjectId());
    return null;
  }

  public String editFeedFolder()
  {
    try
    {
      FeedFolderView row =
        (FeedFolderView)getExternalContext().getRequestMap().get("row");
      String feedFolderId = row.getFeedId() + ";" + row.getFolderId();
      editingFolder = FeedConfigBean.getPort().loadFeedFolderFromCache(feedFolderId);
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
      if (editingFolder.getFolderId() == null ||
        editingFolder.getFolderId().trim().length() == 0)
      {
        throw new Exception("VALUE_IS_MANDATORY");
      }
      else
      {
        try
        {          
          FeedConfigBean.getPort().storeFeedFolder(editingFolder);
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
      String auxFeedFolderId = getObjectId() + ";" + editingFolder.getFeedId();
      editingFolder = null;
      load();
      firstRowIndex = PageFinder.findFirstRowIndex(rows, getPageSize(),
        "feedFolderId", auxFeedFolderId);
    }
    return null;
  }

  public String cancelFeedFolder()
  {
    editingFolder = null;
    return null;
  }

  public String removeFeedFolder()
  {
    try
    {
      FeedFolderView row =
        (FeedFolderView)getExternalContext().getRequestMap().get("row");
      FeedConfigBean.getPort().removeFeedFolder(row.getFeedFolderId());
      editingFolder = null;
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
        filter.getFeedId().add(getFeed().getFeedId());
        rows = FeedConfigBean.getPort().findFeedFolderViewsFromCache(filter);
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
