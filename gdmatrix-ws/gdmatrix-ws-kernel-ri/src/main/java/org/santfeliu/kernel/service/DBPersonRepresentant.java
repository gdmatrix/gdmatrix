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
import org.matrix.kernel.PersonRepresentant;
import org.santfeliu.dic.util.InternalValueConverter;

/**
 *
 * @author realor
 */
public class DBPersonRepresentant extends DBEntityBase
{
  private String personId;
  private String representantId;
  private String representationTypeId;
  private String comments;
  private String valdata;
  private DBPerson person;
  private DBPerson representant;

  private static final InternalValueConverter typeIdConverter =
    new InternalValueConverter(DictionaryConstants.PERSON_REPRESENTANT_TYPE);


  public DBPersonRepresentant()
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

  public void setRepresentantId(String representantId)
  {
    this.representantId = representantId;
  }

  public String getRepresentantId()
  {
    return representantId;
  }

  public void setRepresentationTypeId(String representationTypeId)
  {
    this.representationTypeId = representationTypeId;
  }

  public String getRepresentationTypeId()
  {
    return representationTypeId;
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

  /* relationships */
  public void setPerson(DBPerson person)
  {
    this.person = person;
  }

  public DBPerson getPerson()
  {
    return person;
  }

  public void setRepresentant(DBPerson representant)
  {
    this.representant = representant;
  }

  public DBPerson getRepresentant()
  {
    return representant;
  }

  public void copyTo(PersonRepresentant personRepresentant)
  {
    personRepresentant.setPersonRepresentantId(
      personId + KernelManager.PK_SEPARATOR + representantId);
    personRepresentant.setPersonId(personId);
    personRepresentant.setRepresentantId(representantId);
    personRepresentant.setRepresentationTypeId(
      typeIdConverter.getTypeId(representationTypeId));
    personRepresentant.setComments(comments);
  }
  
  public void copyFrom(PersonRepresentant personRepresentant)
  {
    this.personId = personRepresentant.getPersonId();
    this.representantId = personRepresentant.getRepresentantId();
    this.representationTypeId =
      typeIdConverter.fromTypeId(personRepresentant.getRepresentationTypeId());
    this.comments = personRepresentant.getComments();
  }
}
