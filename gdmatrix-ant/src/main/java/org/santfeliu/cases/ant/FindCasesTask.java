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
package org.santfeliu.cases.ant;

import java.util.List;
import org.apache.tools.ant.taskdefs.Sequential;
import org.matrix.cases.Case;
import org.matrix.cases.CaseFilter;
import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CaseManagerService;
import org.matrix.util.WSEndpoint;
import org.santfeliu.ant.db.Statement;
import org.santfeliu.ant.ws.WSTask;

/**
 *
 * @author lopezrj
 */
public class FindCasesTask extends WSTask
{
  //Input
  private String caseIdVar;
  //Output
  private String caseVar;
  //Nested elements
  private Statement filter;
  private Sequential forEachCase;

  public String getCaseIdVar()
  {
    return caseIdVar;
  }

  public void setCaseIdVar(String caseIdVar)
  {
    this.caseIdVar = caseIdVar;
  }

  public String getCaseVar()
  {
    return caseVar;
  }

  public void setCaseVar(String caseVar)
  {
    this.caseVar = caseVar;
  }

  public void addFilter(Statement filter)
  {
    this.filter = filter;
  }

  public void addForEachCase(Sequential forEachCase)
  {
    this.forEachCase = forEachCase;
  }

  @Override
  public void execute()
  {    
    WSEndpoint endpoint = getEndpoint(CaseManagerService.class);
    CaseManagerPort port = 
      endpoint.getPort(CaseManagerPort.class, getUsername(), getPassword());
    CaseFilter caseFilter = new CaseFilter();
    if (caseIdVar != null)
    {
      Object caseId = getVariable(caseIdVar);
      if (caseId != null) caseFilter.getCaseId().add(String.valueOf(caseId));
    }
    if (filter != null)
    {
      String parsedSQL = getProject().replaceProperties(filter.getSql());
      caseFilter.setSearchExpression(parsedSQL);
    }
    caseFilter.setFirstResult(0);
    caseFilter.setMaxResults(0);
    List<Case> cases = port.findCases(caseFilter);
    for (Case _case : cases)
    {
      _case = port.loadCase(_case.getCaseId());
      setVariable(caseVar, _case);
      forEachCase.perform();
    }
  }

}
