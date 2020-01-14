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

import org.matrix.kernel.PersonAddress;



/**
 *
 * @author unknown
 */
public class DBPersonAddress extends DBEntityBase
{
  private String personId;
  private String addressId;
  private int persnd;
  private String stdapladd;
  private String stdaplmod;
  private String baixasw;
  private String valdata;
  
  private DBPerson person;
  private DBAddress address;

  public DBPersonAddress()
  {
  }

  public void setPersonId(String personId)
  {
    this.personId = personId;
  }

  public String getPersonId()
  {
    return personId;
  }

  public void setAddressId(String addressId)
  {
    this.addressId = addressId;
  }

  public String getAddressId()
  {
    return addressId;
  }

  public void setPersnd(int persnd)
  {
    this.persnd = persnd;
  }

  public int getPersnd()
  {
    return persnd;
  }

  public void setStdapladd(String stdapladd)
  {
    this.stdapladd = stdapladd;
  }

  public String getStdapladd()
  {
    return stdapladd;
  }

  public void setStdaplmod(String stdaplmod)
  {
    this.stdaplmod = stdaplmod;
  }

  public String getStdaplmod()
  {
    return stdaplmod;
  }

  public void setBaixasw(String baixasw)
  {
    this.baixasw = baixasw;
  }

  public String getBaixasw()
  {
    return baixasw;
  }

  public void setValdata(String valdata)
  {
    this.valdata = valdata;
  }

  public String getValdata()
  {
    return valdata;
  }

  /* relationships */
  public void setAddress(DBAddress address)
  {
    this.address = address;
  }

  public DBAddress getAddress()
  {
    return address;
  }

  public void setPerson(DBPerson person)
  {
    this.person = person;
  }

  public DBPerson getPerson()
  {
    return person;
  }

  public void copyTo(PersonAddress personAddress)
  {
    personAddress.setPersonAddressId(personId + 
      KernelManager.PK_SEPARATOR + addressId);
    personAddress.setPersonId(personId);
    personAddress.setAddressId(addressId);
  }
  
  public void copyFrom(PersonAddress personAddress)
  {
    personId = personAddress.getPersonId();
    addressId = personAddress.getAddressId();
  }
}
