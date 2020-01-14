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
package org.santfeliu.workflow.web;

import java.io.Serializable;

import java.util.Map;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;

import org.matrix.workflow.WorkflowManagerPort;
import org.matrix.workflow.WorkflowManagerService;

import org.santfeliu.faces.FacesBean;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.workflow.form.Form;


public abstract class FormBean extends FacesBean implements Serializable
{
  // actions
  public abstract String show(Form form);
  public abstract Map submit();

  protected WorkflowManagerPort getWorkflowManagerPort()
    throws Exception
  {
    WSDirectory dir = WSDirectory.getInstance();
    WSEndpoint endpoint = dir.getEndpoint(WorkflowManagerService.class);

    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String userId = userSessionBean.getUsername();
    String password = userSessionBean.getPassword();

    return endpoint.getPort(WorkflowManagerPort.class, userId, password);
  }  
}
