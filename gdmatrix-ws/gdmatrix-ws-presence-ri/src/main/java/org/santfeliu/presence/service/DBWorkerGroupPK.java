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
package org.santfeliu.presence.service;

import java.io.Serializable;

/**
 *
 * @author realor
 */
public class DBWorkerGroupPK implements Serializable
{
  private final String personId;
  private final String relatedPersonId;

  public DBWorkerGroupPK(String personId, String relatedPersonId)
  {
    this.personId = personId;
    this.relatedPersonId = relatedPersonId;
  }

  public DBWorkerGroupPK(String workerGroupId)
  {
    String ids[] = workerGroupId.split(";");
    this.personId = ids[0];
    this.relatedPersonId = ids[1];
  }
  
  public String getPersonId()
  {
    return personId;
  }

  public String getRelatedPersonId()
  {
    return relatedPersonId;
  }

  @Override
  public String toString()
  {
    return personId + ";" + relatedPersonId;
  }
  
  @Override
  public int hashCode()
  {
    return toString().hashCode();
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    return toString().equals(obj.toString());
  }
}
