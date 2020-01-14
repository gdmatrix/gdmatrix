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

import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.Contact;
import org.santfeliu.dic.util.InternalValueConverter;


/**
 *
 * @author unknown
 */
public class DBTeleco extends DBEntityBase
{
  private String personId;
  private int contactNumber;
  private String contactTypeId;
  private String value;
  private String comments;
  private String valdata;

  private InternalValueConverter typeIdConverter =
    new InternalValueConverter(DictionaryConstants.CONTACT_TYPE);

  public DBTeleco()
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

  public void setContactNumber(int contactNumber)
  {
    this.contactNumber = contactNumber;
  }

  public int getContactNumber()
  {
    return contactNumber;
  }

  public void setContactTypeId(String contactTypeId)
  {
    // Warning: contactTypeId.length() must be less or equal to 4. 
    // But sometimes contactTypeId.length() > 4, so it must be truncated.
    if (contactTypeId.length() > 4)
    {
      contactTypeId = contactTypeId.substring(0, 4);
    }
    this.contactTypeId = contactTypeId;
  }

  public String getContactTypeId()
  {
    return contactTypeId;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public String getValue()
  {
    return value;
  }

  public void setComments(String comments)
  {
    this.comments = comments;
  }

  public String getComments()
  {
    return comments;
  }

  public void setValdata(String valdata)
  {
    this.valdata = valdata;
  }

  public String getValdata()
  {
    return valdata;
  }
  
  public void copyFrom(Contact contact)
  {
    this.personId = contact.getPersonId();
    this.contactTypeId =
      typeIdConverter.fromTypeId(contact.getContactTypeId());
    this.value = contact.getValue();
    this.comments = contact.getComments();
  }
  
  public void copyTo(Contact contact)
  {
    contact.setPersonId(personId);
    contact.setContactTypeId(typeIdConverter.getTypeId(contactTypeId));
    contact.setValue(value);
    contact.setComments(comments);
  }
}
