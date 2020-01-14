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
import org.matrix.cases.CaseCase;
import org.matrix.dic.Property;
import org.santfeliu.dic.service.DBType;
import org.santfeliu.jpa.JPAUtils;

/**
 *
 * @author blanquepa
 */
public class DBCaseCase extends CaseCase
{
  private DBCase mainCase;
  private DBType caseCaseType;    

  public DBCaseCase()
  {
  }
  
  public DBCaseCase(CaseCase caseCase)
  {    
    copyFrom(caseCase);
  }

  public void copyFrom(CaseCase caseCase)
  {
    JPAUtils.copy(caseCase, this);
    setProperty(caseCase.getProperty());
  }  
  
  public DBCase getMainCase()
  {
    return mainCase;
  }

  public void setMainCase(DBCase mainCase)
  {
    this.mainCase = mainCase;
  }

  public DBType getCaseCaseType()
  {
    return caseCaseType;
  }

  public void setCaseCaseType(DBType caseCaseType)
  {
    this.caseCaseType = caseCaseType;
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
