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

import java.util.List;

import org.matrix.dic.DictionaryConstants;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.doc.State;

import org.matrix.util.WSEndpoint;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.util.TextUtils;


/**
 *
 * @author unknown
 */
public class DBDocument
{
  private String docId;
  private int version;
  private String title;
  private String docTypeId;
  private String state;
  private String language;
  private String captureDateTime;
  private String captureUserId;
  private String changeDateTime;
  private String changeUserId;
  private String creationDate;
  private String lockUserId;
  private String contentId;
  private String lastVersion;
  
  private List<DBProperty> properties;

  public DBDocument()
  {
  }

  public void copyFrom(Document document, WSEndpoint endpoint)
    throws Exception
  {
    this.docId = document.getDocId();
    this.version = document.getVersion();
    this.title = TextUtils.replaceSpecialChars(document.getTitle());
    this.docTypeId = 
      endpoint.getExternalEntity("Type").toLocalId(document.getDocTypeId());
    this.state = document.getState().toString();
    this.language = document.getLanguage();
    this.captureDateTime = document.getCaptureDateTime();
    this.captureUserId = document.getCaptureUserId();
    this.changeDateTime = document.getChangeDateTime();
    this.changeUserId = document.getChangeUserId();
    this.creationDate = document.getCreationDate();
    this.lockUserId = document.getLockUserId();
    this.contentId = DocumentUtils.getContentId(document);
  }
  
  public void copyTo(Document document, WSEndpoint endpoint)
    throws Exception
  {
    document.setDocId(docId);
    document.setVersion(version);
    document.setTitle(title);
    String globalTypeId = !DictionaryConstants.DOCUMENT_TYPE.equals(docTypeId) ?
      endpoint.getExternalEntity("Type").toGlobalId(docTypeId) : docTypeId;
    document.setDocTypeId(globalTypeId);
    document.setState(State.fromValue(state));
    document.setLanguage(language);
    document.setCaptureDateTime(captureDateTime);
    document.setCaptureUserId(captureUserId);
    document.setChangeDateTime(changeDateTime);
    document.setChangeUserId(changeUserId);
    document.setCreationDate(creationDate);
    document.setLockUserId(lockUserId);
    if (contentId != null)
    {
      Content content = new Content();
      content.setContentId(contentId);
      document.setContent(content);
    }
    else document.setContent(null);
  }

  //Accessors
  public void setDocId(String documentId)
  {
    this.docId = documentId;
  }

  public String getDocId()
  {
    return docId;
  }

  //Private methods
  public void setDocTypeId(String type)
  {
    this.docTypeId = type;
  }

  public String getDocTypeId()
  {
    return docTypeId;
  }

  public void setState(String state)
  {
    this.state = state;
  }

  public String getState()
  {
    return state;
  }

  public void setLanguage(String language)
  {
    this.language = language;
  }

  public String getLanguage()
  {
    return language;
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

  public String getLockUserId()
  {
    return lockUserId;
  }

  public void setLockUserId(String lockUserId)
  {
    this.lockUserId = lockUserId;
  }

  public String getChangeDateTime()
  {
    return changeDateTime;
  }

  public void setChangeDateTime(String changeDateTime)
  {
    this.changeDateTime = changeDateTime;
  }

  public String getChangeUserId()
  {
    return changeUserId;
  }

  public void setChangeUserId(String changeUserId)
  {
    this.changeUserId = changeUserId;
  }

  public String getCreationDate()
  {
    return creationDate;
  }

  public void setCreationDate(String creationDate)
  {
    this.creationDate = creationDate;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getTitle()
  {
    return title;
  }

  public void setContentId(String contentId)
  {
    this.contentId = contentId;
  }

  public String getContentId()
  {
    return contentId;
  }

  public void setVersion(int version)
  {
    this.version = version;
  }

  public int getVersion()
  {
    return version;
  }

  public String getLastVersion()
  {
    return lastVersion;
  }

  public void setLastVersion(String lastVersion)
  {
    this.lastVersion = lastVersion;
  }

  public void setProperties(List<DBProperty> outputProperties)
  {
    this.properties = outputProperties;
  }

  public List<DBProperty> getProperties()
  {
    return properties;
  }



}
