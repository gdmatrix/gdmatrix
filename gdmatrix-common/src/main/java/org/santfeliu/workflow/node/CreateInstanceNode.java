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
package org.santfeliu.workflow.node;


import org.santfeliu.util.Properties;
import org.santfeliu.workflow.WorkflowNode;


/**
 *
 * @author unknown
 */
public class CreateInstanceNode extends WorkflowNode
{
  protected String workflowName;
  protected boolean waitResult;
  protected Properties parameters = new Properties();
  
  public CreateInstanceNode()
  {
  }

  @Override
  public String getType()
  {
    return "CreateInstance";
  }

  public void setWorkflowName(String workflowName)
  {
    this.workflowName = workflowName;
  }

  public String getWorkflowName()
  {
    return workflowName;
  }

  public void setWaitResult(boolean waitResult)
  {
    this.waitResult = waitResult;
  }

  public boolean isWaitResult()
  {
    return waitResult;
  }

  public void setParameters(Properties parameters)
  {
    this.parameters = parameters;
  }

  public Properties getParameters()
  {
    return parameters;
  }
  
  @Override
  public Object clone() throws CloneNotSupportedException
  {
    CreateInstanceNode newNode = (CreateInstanceNode)super.clone();
    newNode.parameters = (Properties)this.parameters.clone();
    return newNode;
  }
  

}
