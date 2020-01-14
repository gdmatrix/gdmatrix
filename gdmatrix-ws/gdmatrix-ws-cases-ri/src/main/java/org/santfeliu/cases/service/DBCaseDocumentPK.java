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
package org.santfeliu.cases.service;


/**
 *
 * @author unknown
 */
public class DBCaseDocumentPK
  
{
  private String caseId;
  private String docId;
  
  public DBCaseDocumentPK()
  {
  }

  public DBCaseDocumentPK(String caseDocumentId)
  {
    if (caseDocumentId != null)
    {
      String ids[] = caseDocumentId.split(CaseManager.PK_SEPARATOR);
      this.caseId = ids[0];
      this.docId = ids[1];
    }
  }

  public DBCaseDocumentPK(String caseId, String docId)
  {
    this.caseId = caseId;
    this.docId = docId;
  }

  public void setCaseId(String caseId)
  {
    this.caseId = caseId;
  }

  public String getCaseId()
  {
    return caseId;
  }

  public void setDocId(String docId)
  {
    this.docId = docId;
  }

  public String getDocId()
  {
    return docId;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    final DBCaseDocumentPK other = (DBCaseDocumentPK) obj;
    if ((this.caseId == null) ? (other.caseId != null) : !this.caseId.equals(other.caseId))
    {
      return false;
    }
    if ((this.docId == null) ? (other.docId != null) : !this.docId.equals(other.docId))
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 3;
    hash = 73 * hash + (this.caseId != null ? this.caseId.hashCode() : 0);
    hash = 73 * hash + (this.docId != null ? this.docId.hashCode() : 0);
    return hash;
  }
}
