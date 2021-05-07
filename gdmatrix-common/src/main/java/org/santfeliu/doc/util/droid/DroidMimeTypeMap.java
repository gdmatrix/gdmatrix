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
package org.santfeliu.doc.util.droid;

import javax.activation.FileTypeMap;
import org.santfeliu.util.MimeTypeMap;

/**
 *
 * @author blanquepa
 */
public class DroidMimeTypeMap extends MimeTypeMap
{
  private final Droid droid;
   
  public DroidMimeTypeMap(Droid droid)
  {
    super();
    this.droid = droid;       
  }
        
  @Override
  public String getExtension(String mimeType)
  {
    String extension = super.getExtension(mimeType);
    if (extension == null && droid != null)
    {
      extension = droid.getExtension(mimeType);
      if (extension != null)
        putExtension(mimeType, extension);
    }      
    
    return extension;    
  }
   
  public static MimeTypeMap getMimeTypeMap(Droid droid)
  {
    MimeTypeMap mimeMap;
    
    FileTypeMap map = FileTypeMap.getDefaultFileTypeMap();
    if (map instanceof DroidMimeTypeMap)
    {
      mimeMap = (DroidMimeTypeMap)map;
    }
    else
    {
      if (droid != null)
      {
        mimeMap = new DroidMimeTypeMap(droid);
        FileTypeMap.setDefaultFileTypeMap(mimeMap);
      }
      else
        mimeMap = new MimeTypeMap();
    }
    return mimeMap;     
  }
   
}
