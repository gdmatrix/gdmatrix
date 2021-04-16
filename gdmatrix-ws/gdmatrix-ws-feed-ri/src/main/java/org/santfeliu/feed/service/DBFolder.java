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
import org.matrix.feed.Folder;
import org.matrix.util.WSEndpoint;

/**
 *
 * @author unknown
 */
public class DBFolder extends Folder
{
  private Collection<DBFeedFolder> feedFolders;  
  
  private DBFolder parentFolder;  

  public DBFolder()
  {
  }

  public DBFolder(Folder folder, WSEndpoint endpoint)
  {
    copyFrom(folder, endpoint);
  }

  public Collection<DBFeedFolder> getFeedFolders()
  {
    return feedFolders;
  }

  public void setFeedFolders(Collection<DBFeedFolder> feedFolders)
  {
    this.feedFolders = feedFolders;
  }

  public DBFolder getParentFolder() 
  {
    return parentFolder;
  }

  public void setParentFolder(DBFolder parentFolder) 
  {
    this.parentFolder = parentFolder;
  }

  public void copyTo(Folder folder, WSEndpoint endpoint)
  {
    folder.setFolderId(endpoint.toGlobalId(Folder.class, this.getFolderId()));
    folder.setName(this.getName());
    folder.setDescription(this.getDescription());
    folder.setIconDocId(this.getIconDocId());
    folder.setAlias(this.getAlias());
    folder.setParentFolderId(this.getParentFolderId());
    folder.setDefaultEntryCount(this.getDefaultEntryCount());
  }

  public void copyFrom(Folder folder, WSEndpoint endpoint)
  {
    setFolderId(endpoint.toLocalId(Folder.class, folder.getFolderId()));
    setName(folder.getName());
    setDescription(folder.getDescription());
    setIconDocId(folder.getIconDocId());
    setAlias(folder.getAlias());
    setParentFolderId(folder.getParentFolderId());
    setDefaultEntryCount(folder.getDefaultEntryCount());
  }

}
