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
package org.santfeliu.news.service;

import org.matrix.dic.Type;
import org.matrix.news.NewDocument;
import org.matrix.util.WSEndpoint;

/**
 *
 * @author unknown
 */
public class DBNewDocument extends NewDocument
{
  private DBNew ndNew; //Relationship
  
  public DBNewDocument()
  {
  }
  
  public DBNewDocument(NewDocument newDocument, WSEndpoint endpoint)
  {
    copyFrom(newDocument, endpoint);
  }

  //Relationships
  public void setNdNew(DBNew ndNew)
  {
    this.ndNew = ndNew;
  }

  public DBNew getNdNew()
  {
    return ndNew;
  }
  
  public void copyTo(NewDocument newDocument, WSEndpoint endpoint)
  {
    newDocument.setNewDocumentId(this.getNewId() + 
      NewsManager.PK_SEPARATOR + this.getDocumentId());
    newDocument.setNewId(this.getNewId());
    newDocument.setDocumentId(this.getDocumentId());
    //newDocument.setDocRole(this.getDocRole());
    newDocument.setNewDocTypeId(
      endpoint.toGlobalId(Type.class, this.getNewDocTypeId()));
    newDocument.setMimeType(this.getMimeType());
    newDocument.setTitle(this.getTitle());
    newDocument.setContentId(this.getContentId());
//    newDocument.setLanguage(this.getLanguage());
  }
  
  public void copyFrom(NewDocument newDocument, WSEndpoint endpoint)
  {
    this.setNewDocumentId(newDocument.getNewDocumentId());
    this.setNewId(newDocument.getNewId());
    this.setDocumentId(newDocument.getDocumentId());
    //this.setDocRole(newDocument.getDocRole());
    this.setNewDocTypeId(endpoint.toLocalId(Type.class, newDocument.getNewDocTypeId()));
    this.setMimeType(newDocument.getMimeType());
    this.setTitle(newDocument.getTitle());
    this.setContentId(newDocument.getContentId());
//    this.setLanguage(newDocument.getLanguage());
  }
}
