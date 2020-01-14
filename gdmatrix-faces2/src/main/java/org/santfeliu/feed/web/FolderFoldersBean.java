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
import org.matrix.feed.Folder;
import org.matrix.feed.FolderFilter;
import org.santfeliu.web.obj.PageBean;

/**
 *
 * @author unknown
 */
public class FolderFoldersBean extends PageBean
{
  private List<Folder> rows;  
  private String editingFolderId;

  public FolderFoldersBean()
  {
    load();
  }

  public String getEditingFolderId() 
  {
    return editingFolderId;
  }

  public void setEditingFolderId(String editingFolderId) 
  {
    this.editingFolderId = editingFolderId;
  }

  public List<Folder> getRows() 
  {
    return rows;
  }

  public void setRows(List<Folder> rows) 
  {
    this.rows = rows;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }
  
  public String show()
  {
    return "folder_folders";
  }

  public String showChildFolder()
  {
    return getControllerBean().showObject("Folder",
      (String)getValue("#{row.folderId}"));
  }

  public String storeChildFolder()
  {
    try
    {
      if (editingFolderId == null || editingFolderId.trim().length() == 0)
      {
        throw new Exception("VALUE_IS_MANDATORY");
      }
      else
      {
        Folder editingFolder = 
          FeedConfigBean.getPort().loadFolderFromCache(editingFolderId);
        editingFolder.setParentFolderId(getFolder().getFolderId());
        FeedConfigBean.getPort().storeFolder(editingFolder);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    finally
    {
      editingFolderId = null;
      load();
    }
    return null;
  }
  
  public String removeChildFolder()
  {
    try
    {
      Folder row = (Folder)getExternalContext().getRequestMap().get("row");
      row.setParentFolderId(null);
      FeedConfigBean.getPort().storeFolder(row);
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String searchFolder()
  {
    return getControllerBean().searchObject("Folder",
      "#{folderFoldersBean.editingFolderId}");
  }

  public List<SelectItem> getFolderItems()
  {
    FolderBean folderBean = (FolderBean)getBean("folderBean");
    return folderBean.getSelectItems(editingFolderId);
  }

  private void load()
  {
    try
    {
      if (!isNew())
      {
        FolderFilter filter = new FolderFilter();
        filter.setParentFolderId(getFolder().getFolderId());
        rows = FeedConfigBean.getPort().findFoldersFromCache(filter);
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
