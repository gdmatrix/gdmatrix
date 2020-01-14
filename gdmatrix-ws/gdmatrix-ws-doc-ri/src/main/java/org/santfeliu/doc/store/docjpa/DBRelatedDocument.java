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
package org.santfeliu.doc.store.docjpa;

import org.matrix.doc.RelatedDocument;
import org.matrix.doc.RelationType;


/**
 *
 * @author unknown
 */
public class DBRelatedDocument
{
  private String docId;
  private String name;
  private String relationType;
  private String relDocId;
  private String captureDateTime;
  private String captureUserId;
  private String changeDateTime;
  private String changeUserId;
  
  public DBRelatedDocument()
  {
  }
  
  public DBRelatedDocument(String docId, 
    RelatedDocument relatedDocument)
  {
     copyFrom(docId, relatedDocument);
  }
  
  public void copyFrom(String docId, 
    RelatedDocument relatedDocument)
  {
    this.docId = docId;
    this.name = relatedDocument.getName();
    this.relationType = relatedDocument.getRelationType().toString();
    this.captureDateTime = relatedDocument.getCaptureDateTime();
    this.captureUserId = relatedDocument.getCaptureUserId();
    this.changeDateTime = relatedDocument.getChangeDateTime();
    this.changeUserId = relatedDocument.getChangeUserId();
    this.relDocId = relatedDocument.getDocId();
  }
  
  public void copyTo(RelatedDocument relatedDocument)
    throws Exception
  {
    relatedDocument.setRelationType(RelationType.fromValue(this.relationType));
    relatedDocument.setName(this.name);
    relatedDocument.setCaptureDateTime(this.captureDateTime);
    relatedDocument.setCaptureUserId(this.captureUserId);
    relatedDocument.setChangeDateTime(this.changeDateTime);
    relatedDocument.setChangeUserId(this.changeUserId);
    relatedDocument.setDocId(this.relDocId);
  }

  public void setDocId(String docId)
  {
    this.docId = docId;
  }

  public String getDocId()
  {
    return docId;
  }

  public void setChangeUserId(String changeUserId)
  {
    this.changeUserId = changeUserId;
  }

  public String getChangeUserId()
  {
    return changeUserId;
  }

  public String getCaptureDateTime()
  {
    return captureDateTime;
  }

  public void setCaptureDateTime(String captureDateTime)
  {
    this.captureDateTime = captureDateTime;
  }

  public String getCaptureUserId()
  {
    return captureUserId;
  }

  public void setCaptureUserId(String captureUserId)
  {
    this.captureUserId = captureUserId;
  }

  public String getChangeDateTime()
  {
    return changeDateTime;
  }

  public void setChangeDateTime(String changeDateTime)
  {
    this.changeDateTime = changeDateTime;
  }

  public void setRelationType(String relationType)
  {
    this.relationType = relationType;
  }

  public String getRelationType()
  {
    return relationType;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  public void setRelDocId(String relDocId)
  {
    this.relDocId = relDocId;
  }

  public String getRelDocId()
  {
    return relDocId;
  }
}
