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
package org.santfeliu.edu.web;

import org.matrix.edu.Inscription;

import org.matrix.kernel.Person;

import org.santfeliu.kernel.web.KernelConfigBean;
import org.santfeliu.web.obj.ObjectBean;

/**
 *
 * @author unknown
 */
public class InscriptionBean extends ObjectBean
{
  public InscriptionBean()
  {
  }

  public String getObjectTypeId()
  {
    return "Inscription";
  }
  
  public String remove()
  {
    try
    {
      if (!isNew())
      {
        EducationConfigBean.getPort().removeInscription(objectId);
        removed();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return getControllerBean().show();
  }

  public String getDescription()
  {
    InscriptionMainBean mainBean = 
      (InscriptionMainBean)getBean("inscriptionMainBean");
    return getDescription(mainBean.getInscription());
  }

  public String getDescription(String objectId)
  {
    String description = objectId;
    try
    {
      Inscription inscription = 
        EducationConfigBean.getPort().loadInscription(objectId);
      description = getDescription(inscription);
    }
    catch (Exception ex)
    {
    }
    return description;
  }
  
  private String getDescription(Inscription inscription)
  {
    if (inscription == null) return "";
    StringBuffer buffer = new StringBuffer();
    try
    {
      String code = inscription.getCode();
      String personId = inscription.getPersonId();
      Person person = KernelConfigBean.getPort().loadPerson(personId);
      buffer.append(code + ": " + person.getName() + " " +
        person.getFirstSurname() + " " +
        person.getSecondSurname() + " - " + inscription.getCourseId());
      buffer.append(" (");
      buffer.append(inscription.getInscriptionId());
      buffer.append(")");
    }
    catch (Exception ex)
    {
    }
    return buffer.toString();
  }
}
