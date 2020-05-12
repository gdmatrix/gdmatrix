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
import org.matrix.feed.Folder;
import org.matrix.feed.FolderFilter;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.obj.BasicSearchBean;

/**
 *
 * @author unknown
 */
@CMSManagedBean
public class FolderSearchBean extends BasicSearchBean
{
  private String folderIdInput;
  private FolderFilter filter;
  
  public FolderSearchBean()
  {
    filter = new FolderFilter();
  }

  public String getFolderIdInput()
  {
    return folderIdInput;
  }

  public void setFolderIdInput(String folderIdInput)
  {
    this.folderIdInput = folderIdInput;
  }

  public FolderFilter getFilter()
  {
    return filter;
  }

  public void setFilter(FolderFilter filter)
  {
    this.filter = filter;
  }

  public int countResults()
  {
    try
    {
      setFilterFolderId();
      return FeedConfigBean.getPort().countFoldersFromCache(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return 0;
  }

  public List getResults(int firstResult, int maxResults)
  {
    try
    {
      setFilterFolderId();
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      return FeedConfigBean.getPort().findFoldersFromCache(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  @CMSAction  
  public String show()
  {
    return "folder_search";
  }

  public String selectFolder()
  {
    Folder row = (Folder)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    String folderId = row.getFolderId();
    return getControllerBean().select(folderId);
  }

  public String showFolder()
  {
    return getControllerBean().showObject("Folder",
      (String)getValue("#{row.folderId}"));
  }

  private void setFilterFolderId()
  {
    filter.getFolderId().clear();
    if (folderIdInput != null)
    {
      for (String folderId : folderIdInput.split(";"))
      {
        if (!folderId.isEmpty()) filter.getFolderId().add(folderId);
      }
    }
  }
  
}
