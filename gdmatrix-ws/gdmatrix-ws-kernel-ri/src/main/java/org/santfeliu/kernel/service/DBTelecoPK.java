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
package org.santfeliu.kernel.service;

import java.io.Serializable;

/**
 *
 * @author unknown
 */
public class DBTelecoPK implements Serializable
{
  private String personId;
  private int contactNumber;

  public DBTelecoPK()
  {
  }

  public DBTelecoPK(String contactId)
  {
    String ids[] = contactId.split(KernelManager.PK_SEPARATOR);
    personId = ids[1];
    contactNumber = Integer.parseInt(ids[2]);
  }

  public void setPersonId(String personId)
  {
    this.personId = personId;
  }

  public String getPersonId()
  {
    return personId;
  }

  public void setContactNumber(int contactNumber)
  {
    this.contactNumber = contactNumber;
  }

  public int getContactNumber()
  {
    return contactNumber;
  }
  
  public boolean equals(Object o)
  {
    DBTelecoPK pk = (DBTelecoPK)o;
    return pk.getPersonId().equals(personId) && 
      pk.contactNumber == contactNumber;
  }
  
  public int hashCode()
  {
    return (personId + KernelManager.PK_SEPARATOR + contactNumber).hashCode();
  }
}
