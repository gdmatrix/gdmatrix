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
package org.santfeliu.doc.store.cntfs;

import java.io.File;

import java.util.List;
import java.util.Set;

import org.matrix.doc.Content;

import org.matrix.doc.ContentInfo;
import org.santfeliu.doc.store.ContentStoreConnection;


/**
 *
 * @author unknown
 */
public class FileSystemContentStoreConnection implements ContentStoreConnection
{
  public FileSystemContentStoreConnection()
  {
  }

  public Content storeContent(Content content, File file)
  {
    return null;
  }

  public Content loadContent(String contentId, ContentInfo contentInfo)
  {
    return null;
  }

  public boolean removeContent(String contentId)
  {
    return false;
  }

  public Content copyContent(Content content, String currentContentId) throws Exception
  {
    return null;
  }
  
  public List<Content> findContents(Set<String> contentsId)
  {
    return null;
  }
  
  public File markupContent(String contentId, String searchExpression)
  {
    return null;
  }

  public void rollback()
  {
  }

  public void commit()
  {
  }

  public void close()
  {
  }


}
