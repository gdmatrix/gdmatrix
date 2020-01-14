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

import org.matrix.cases.InterventionProblem;

/**
 *
 * @author blanquepa
 */
public class DBInterventionProblem extends InterventionProblem
{
  private DBIntervention intervention;
  private DBProblem problem;
  
  public DBInterventionProblem()
  {
  }

  public DBInterventionProblem(InterventionProblem caseDocument)
  {
    copyFrom(caseDocument);
  }

  public void copyTo(InterventionProblem interventionProblem)
  {
    interventionProblem.setIntProbId(intProbId);
    interventionProblem.setIntId(intId);
    interventionProblem.setProbId(probId);
    interventionProblem.setComments(comments);
  }

  public void copyFrom(InterventionProblem interventionProblem)
  {
    intProbId = interventionProblem.getIntProbId();
    intId = interventionProblem.getIntId();
    probId = interventionProblem.getProbId();
    comments = interventionProblem.getComments();
  }

  public DBIntervention getIntervention()
  {
    return intervention;
  }

  public void setIntervention(DBIntervention intervention)
  {
    this.intervention = intervention;
  }

  public DBProblem getProblem()
  {
    return problem;
  }

  public void setProblem(DBProblem problem)
  {
    this.problem = problem;
  }

}
