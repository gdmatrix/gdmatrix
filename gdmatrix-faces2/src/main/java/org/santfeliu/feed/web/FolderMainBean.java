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
import org.santfeliu.web.obj.PageBean;

/**
 *
 * @author unknown
 */
public class FolderMainBean extends PageBean
{
  private static final String DEFAULT_ENTRY_COUNT = "100";
  
  private Folder folder;

  public FolderMainBean()
  {
    load();
  }

  public Folder getFolder()
  {
    return folder;
  }

  public void setFolder(Folder folder)
  {
    this.folder = folder;
  }
  
  public String show()
  {
    return "folder_main";
  }

  @Override
  public String store()
  {
    try
    {      
      if (folder.getDefaultEntryCount() == null ||
        folder.getDefaultEntryCount().trim().isEmpty())
      {
        folder.setDefaultEntryCount(DEFAULT_ENTRY_COUNT);
      }
      folder = FeedConfigBean.getPort().storeFolder(folder);
      setObjectId(folder.getFolderId());
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return show();
  }
  
  public List<SelectItem> getFolderItems()
  {
    FolderBean folderBean = (FolderBean)getBean("folderBean");
    return folderBean.getSelectItems(folder.getParentFolderId());
  }

  public String searchParentFolder()
  {
    return getControllerBean().searchObject("Folder",
      "#{folderMainBean.folder.parentFolderId}");
  } 
  
  public String showParentFolder()
  {
    return getControllerBean().showObject("Folder", 
      getFolder().getParentFolderId());
  }

  public boolean isRenderShowParentFolder()
  {
    return !isNew() && (getFolder().getParentFolderId() != null) && 
      (!getFolder().getParentFolderId().isEmpty());
  }  
  
  private void load()
  {
    if (isNew())
    {
      folder = new Folder();
    }
    else
    {
      try
      {
        folder = FeedConfigBean.getPort().loadFolderFromCache(getObjectId());
      }
      catch (Exception ex)
      {
        getObjectBean().clearObject();
        error(ex);
        folder = new Folder();
      }
    }
  }

}
