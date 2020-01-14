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

import org.matrix.cases.CasePerson;
import org.santfeliu.dic.service.DBType;

import org.santfeliu.kernel.service.DBPerson;
import org.santfeliu.util.audit.Auditable;

/**
 *
 * @author unknown
 */
public class DBCasePerson extends CasePerson implements Auditable
{
  private DBCase caseObject;
  private DBPerson person;
  private String personDesc;
  private String contactId1;
  private String contactId2;
  private String contactId3;
  private String representantContactId1;
  private String representantContactId2;
  private String representantContactId3;
  private DBType casePersonType;  

  public DBCasePerson()
  {
  }
  
  public DBCasePerson(CasePerson casePerson)
  {
    copyFrom(casePerson);
  }

  public void copyFrom(CasePerson casePerson)
  {
    this.casePersonId = casePerson.getCasePersonId();
    this.caseId = casePerson.getCaseId();
    this.personId = casePerson.getPersonId();
    this.startDate = casePerson.getStartDate();
    this.endDate = casePerson.getEndDate();
    this.comments = casePerson.getComments();

    this.casePersonTypeId = casePerson.getCasePersonTypeId();
    this.addressId = casePerson.getAddressId();
    this.contactId1 = casePerson.getContactId().size() >= 1 ?
      casePerson.getContactId().get(0) : null;
    this.contactId2 = casePerson.getContactId().size() >= 2 ?
      casePerson.getContactId().get(1) : null;
    this.contactId3 = casePerson.getContactId().size() >= 3 ?
      casePerson.getContactId().get(2) : null;

    this.representantPersonId = casePerson.getRepresentantPersonId();
    this.representantAddressId = casePerson.getRepresentantAddressId();
    this.representantContactId1 =
      casePerson.getRepresentantContactId().size() >= 1 ?
      casePerson.getRepresentantContactId().get(0) : null;
    this.representantContactId2 =
      casePerson.getRepresentantContactId().size() >= 2 ?
      casePerson.getRepresentantContactId().get(1) : null;
    this.representantContactId3 =
      casePerson.getRepresentantContactId().size() >= 3 ?
      casePerson.getRepresentantContactId().get(2) : null;
  }

  public void copyTo(CasePerson casePerson)
  {
    casePerson.setCasePersonId(casePersonId);
    casePerson.setCaseId(caseId);
    casePerson.setPersonId(personId);
    casePerson.setStartDate(startDate);
    casePerson.setEndDate(endDate);
    casePerson.setComments(comments);
    casePerson.setCasePersonTypeId(casePersonTypeId);
    casePerson.setAddressId(addressId);
    casePerson.getContactId().clear();
    if (contactId1 != null) casePerson.getContactId().add(contactId1);
    if (contactId2 != null) casePerson.getContactId().add(contactId2);
    if (contactId3 != null) casePerson.getContactId().add(contactId3);

    casePerson.setRepresentantPersonId(representantPersonId);
    casePerson.setRepresentantAddressId(representantAddressId);
    casePerson.getRepresentantContactId().clear();
    if (representantContactId1 != null)
      casePerson.getRepresentantContactId().add(representantContactId1);
    if (representantContactId2 != null)
      casePerson.getRepresentantContactId().add(representantContactId2);
    if (representantContactId3 != null)
      casePerson.getRepresentantContactId().add(representantContactId3);
  }

  public void setCaseObject(DBCase caseObject)
  {
    this.caseObject = caseObject;
  }

  public DBCase getCaseObject()
  {
    return caseObject;
  }

  public void setPerson(DBPerson person)
  {
    this.person = person;
  }

  public DBPerson getPerson()
  {
    return person;
  }

  public void setPersonDesc(String personDesc)
  {
    this.personDesc = personDesc;
  }

  public String getPersonDesc()
  {
    return personDesc;
  }

  public String getContactId1()
  {
    return contactId1;
  }

  public void setContactId1(String contactId1)
  {
    this.contactId1 = contactId1;
  }

  public String getContactId2()
  {
    return contactId2;
  }

  public void setContactId2(String contactId2)
  {
    this.contactId2 = contactId2;
  }

  public String getContactId3()
  {
    return contactId3;
  }

  public void setContactId3(String contactId3)
  {
    this.contactId3 = contactId3;
  }

  public String getRepresentantContactId1()
  {
    return representantContactId1;
  }

  public void setRepresentantContactId1(String representantContactId1)
  {
    this.representantContactId1 = representantContactId1;
  }

  public String getRepresentantContactId2()
  {
    return representantContactId2;
  }

  public void setRepresentantContactId2(String representantContactId2)
  {
    this.representantContactId2 = representantContactId2;
  }

  public String getRepresentantContactId3()
  {
    return representantContactId3;
  }

  public void setRepresentantContactId3(String representantContactId3)
  {
    this.representantContactId3 = representantContactId3;
  }

  public DBType getCasePersonType()
  {
    return casePersonType;
  }

  public void setCasePersonType(DBType casePersonType)
  {
    this.casePersonType = casePersonType;
  }
}
