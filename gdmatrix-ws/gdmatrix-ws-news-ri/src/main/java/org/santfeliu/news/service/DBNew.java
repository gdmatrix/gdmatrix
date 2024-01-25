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

import java.util.Collection;
import org.matrix.news.New;

/**
 *
 * @author unknown
 */
public class DBNew extends New
{
  private byte[] baText;
  private String strDraft;
  
  //Relationships
  private Collection<DBNewSection> newSections;
  private Collection<DBNewDocument> newDocuments;
  
  public DBNew()
  {
  }
  
  public DBNew(New newObject)
  {
    copyFrom(newObject);
  }
  
  public void setBaText(byte[] baText)
  {
    this.baText = baText;
  }

  public byte[] getBaText()
  {
    return baText;
  }

  public void setStrDraft(String strDraft)
  {
    this.strDraft = strDraft;
  }

  public String getStrDraft()
  {
    return strDraft;
  }
  
  //Relationships  
  public void setNewSections(Collection<DBNewSection> newSections)
  {
    this.newSections = newSections;
  }

  public Collection<DBNewSection> getNewSections()
  {
    return newSections;
  }

  public void setNewDocuments(Collection<DBNewDocument> newDocuments)
  {
    this.newDocuments = newDocuments;
  }

  public Collection<DBNewDocument> getNewDocuments()
  {
    return newDocuments;
  }
  
  public void copyTo(New newObject)
  {
    newObject.setNewId(this.getNewId());  
    newObject.setHeadline(this.getHeadline());
    newObject.setSummary(this.getSummary());
    newObject.setKeywords(this.getKeywords());
    newObject.setCustomUrl(this.getCustomUrl());
    newObject.setCustomUrlTarget(this.getCustomUrlTarget());
    newObject.setHash(this.getHash());
    newObject.setIconUrl(this.getIconUrl());
    if ((baText != null) && (baText.length > 0))
    {
      newObject.setText(new String(baText));  
    }
    else
    {
      newObject.setText(this.getText());
    }
    newObject.setRegisterDate(this.getRegisterDate());
    newObject.setRegisterTime(this.getRegisterTime());
    newObject.setStartDate(this.getStartDate());
    newObject.setStartTime(this.getStartTime());
    newObject.setEndDate(this.getEndDate());
    newObject.setEndTime(this.getEndTime());
    newObject.setSource(this.getSource());
    newObject.setUserId(this.getUserId());
    if (this.getStrDraft() != null)
    {
      newObject.setDraft(
        this.getStrDraft().equalsIgnoreCase("Y") ? true : false);      
    }
    newObject.setTotalReadingCount(this.getTotalReadingCount());
  }
  
  public void copyFrom(New newObject)
  {
    this.setNewId(newObject.getNewId());  
    this.setHeadline(newObject.getHeadline());
    this.setSummary(newObject.getSummary());
    this.setKeywords(newObject.getKeywords());
    this.setCustomUrl(newObject.getCustomUrl());
    this.setCustomUrlTarget(newObject.getCustomUrlTarget());
    this.setHash(newObject.getHash());
    this.setIconUrl(newObject.getIconUrl());
    if (newObject.getText() != null) 
      this.baText = newObject.getText().getBytes();    
    this.setText(newObject.getText());
    this.setRegisterDate(newObject.getRegisterDate());
    this.setRegisterTime(newObject.getRegisterTime());
    this.setStartDate(newObject.getStartDate());
    this.setStartTime(newObject.getStartTime());
    this.setEndDate(newObject.getEndDate());
    this.setEndTime(newObject.getEndTime());
    this.setSource(newObject.getSource());
    this.setUserId(newObject.getUserId());
    this.setDraft(newObject.isDraft());
    this.setStrDraft(newObject.isDraft() ? "Y" : "N");
    this.setTotalReadingCount(newObject.getTotalReadingCount());
  }
}
