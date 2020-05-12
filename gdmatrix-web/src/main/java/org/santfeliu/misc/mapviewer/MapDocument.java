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
package org.santfeliu.misc.mapviewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author realor
 */
public class MapDocument extends Map
{
  private String docId;
  private String creationDate;
  private String captureUserId;
  private String captureDateTime;
  private String changeUserId;
  private String changeDateTime;
  private final HashMap metadata = new HashMap();
  private final List<String> readRoles = new ArrayList<String>();
  private final List<String> writeRoles = new ArrayList<String>();
  private String thumbnailDocId;

  public MapDocument()
  {
  }

  public MapDocument(Map map)
  {
    setTo(map);
  }

  public String getDocId()
  {
    return docId;
  }

  public void setDocId(String docId)
  {
    this.docId = docId;
  }

  public String getCreationDate()
  {
    return creationDate;
  }

  public void setCreationDate(String creationDate)
  {
    this.creationDate = creationDate;
  }

  public String getChangeUserId()
  {
    return changeUserId;
  }

  public void setChangeUserId(String changeUserId)
  {
    this.changeUserId = changeUserId;
  }

  public String getChangeDateTime()
  {
    return changeDateTime;
  }

  public void setChangeDateTime(String changeDateTime)
  {
    this.changeDateTime = changeDateTime;
  }

  public String getCaptureUserId()
  {
    return captureUserId;
  }

  public void setCaptureUserId(String captureUserId)
  {
    this.captureUserId = captureUserId;
  }

  public String getCaptureDateTime()
  {
    return captureDateTime;
  }

  public void setCaptureDateTime(String captureDateTime)
  {
    this.captureDateTime = captureDateTime;
  }
  
  public void setMetadata(java.util.Map metadata)
  {
    if (this.metadata != metadata)
    {
      this.metadata.clear();
      this.metadata.putAll(metadata);
    }
  }
  
  public java.util.Map getMetadata() // save as Document.Property
  {
    return metadata;
  }
  
  public List<String> getReadRoles()
  {
    return readRoles;
  }

  public List<String> getWriteRoles()
  {
    return writeRoles;
  }

  public String getThumbnailDocId()
  {
    return thumbnailDocId;
  }

  public void setThumbnailDocId(String thumbnailDocId)
  {
    this.thumbnailDocId = thumbnailDocId;
  }
}
