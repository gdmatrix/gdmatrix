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

import java.util.ArrayList;
import java.util.List;
import org.matrix.cases.Case;
import org.matrix.dic.Property;

import org.santfeliu.dic.service.DBType;
import org.santfeliu.util.audit.Auditable;


/**
 *
 * @author unknown
 */
public class DBCase extends Case implements Auditable
{
  private DBType caseType;

  public DBCase()
  {
  }
  
  public DBCase(Case caseObject)
  {
    copyFrom(caseObject);
  }
  
  public final void copyFrom(Case caseObject)
  {
    this.caseId = caseObject.getCaseId();
    this.startDate = caseObject.getStartDate();
    this.startTime = caseObject.getStartTime();
    this.endDate = caseObject.getEndDate();
    this.endTime = caseObject.getEndTime();
    this.caseTypeId = caseObject.getCaseTypeId();
    this.state = caseObject.getState();
    this.description = caseObject.getDescription();
    this.source = caseObject.getSource();
    this.title = caseObject.getTitle();
    this.comments = caseObject.getComments();

    //collections
    setClassId(caseObject.getClassId());
//    setProperty(caseObject.getProperty());
    this.property = caseObject.getProperty();
    this.accessControl = caseObject.getAccessControl();
  }

  public DBType getCaseType()
  {
    return caseType;
  }

  public void setCaseType(DBType caseType)
  {
    this.caseType = caseType;
  }

  private void setClassId(List<String> classId)
  {
    this.classId = classId;
  }

  private void setProperty(List<Property> property)
  {
    if (property != null && !property.isEmpty())
    {
      this.property = new ArrayList<Property>();
      for (Property p : property)
      {
        Property paux = new Property();
        paux.setName(p.getName());
        if (p.getValue() != null && !p.getValue().isEmpty())
          paux.getValue().add(p.getValue().get(0));
        else
          paux.getValue().addAll(p.getValue());
        this.property.add(paux);
      }
    }
    else
      this.property = property;
  }


  
}
