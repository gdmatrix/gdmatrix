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

import org.matrix.feed.Folder;
import org.santfeliu.web.obj.ObjectBean;

/**
 *
 * @author unknown
 */
public class FolderBean extends ObjectBean
{
  public FolderBean()
  {
  }

  public String getObjectTypeId()
  {
    return "Folder";
  }

  @Override
  public String remove()
  {
    try
    {
      if (!isNew())
      {
        FeedConfigBean.getPort().removeFolder(getObjectId());
        removed();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }    
    return getControllerBean().show();
  }
  
  @Override
  public String getDescription()
  {
    FolderMainBean folderMainBean = (FolderMainBean)getBean("folderMainBean");
    Folder folder = folderMainBean.getFolder();
    return getFolderDescription(folder);
  }   
  
  @Override
  public String getDescription(String oid)
  {
    String description = "";
    try
    {
      Folder folder = FeedConfigBean.getPort().loadFolderFromCache(oid);
      description = getFolderDescription(folder);
    }
    catch (Exception ex)
    {
      error(ex.getMessage());
    }
    return description;
  }
  
  private String getFolderDescription(Folder folder)
  {
    StringBuilder sb = new StringBuilder();  
    if (folder.getName() != null)
    {
      sb.append(folder.getName());
      sb.append(" ");
    }
    sb.append("(");
    sb.append(folder.getFolderId());
    sb.append(")");
    return sb.toString();
  }
  
}
