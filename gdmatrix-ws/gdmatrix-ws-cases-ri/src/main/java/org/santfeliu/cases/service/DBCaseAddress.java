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

import org.matrix.cases.CaseAddress;
import org.santfeliu.jpa.JPAUtils;

import org.santfeliu.kernel.service.DBAddress;
import org.santfeliu.util.audit.Auditable;

/**
 *
 * @author unknown
 */
public class DBCaseAddress extends CaseAddress implements Auditable
{
  private DBCase caseObject;
  private DBAddress address;
  private String addressDesc;
  
  public DBCaseAddress()
  {
  }
  
  public DBCaseAddress(CaseAddress caseAddress)
  {
    JPAUtils.copy(caseAddress, this);
  }
  
  public void copyFrom(CaseAddress caseAddress)
  {
    this.caseAddressId = caseAddress.getCaseAddressId();
    this.caseId = caseAddress.getCaseId();
    this.addressId = caseAddress.getAddressId();
    this.radius = caseAddress.getRadius();
    this.comments = caseAddress.getComments();
    this.startDate = caseAddress.getStartDate();
    this.endDate = caseAddress.getEndDate();
    this.caseAddressTypeId = caseAddress.getCaseAddressTypeId();
  }

  public void setCaseObject(DBCase caseObject)
  {
    this.caseObject = caseObject;
  }

  public DBCase getCaseObject()
  {
    return caseObject;
  }

  public void setAddress(DBAddress address)
  {
    this.address = address;
  }

  public DBAddress getAddress()
  {
    return address;
  }

  public void setAddressDesc(String addressDesc)
  {
    this.addressDesc = addressDesc;
  }

  public String getAddressDesc()
  {
    return addressDesc;
  }
}
