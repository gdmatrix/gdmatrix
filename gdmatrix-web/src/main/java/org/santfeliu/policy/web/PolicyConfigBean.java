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
package org.santfeliu.policy.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.policy.PolicyManagerPort;
import org.matrix.policy.PolicyManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 */
class PolicyConfigBean implements Serializable
{
  static List<SelectItem> actionSelectItems;

  public static PolicyManagerPort getPort() throws Exception
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint =
      wsDirectory.getEndpoint(PolicyManagerService.class);
    return endpoint.getPort(PolicyManagerPort.class,
      UserSessionBean.getCurrentInstance().getUserId(),
      UserSessionBean.getCurrentInstance().getPassword());
  }

  public static List<SelectItem> getActionSelectItems()
  {
    if (actionSelectItems == null)
    {
      List<SelectItem> items = new ArrayList<SelectItem>();
      items.add(new SelectItem("Destroy", "Destruir"));
      items.add(new SelectItem("Transfer", "Tranferir"));
      items.add(new SelectItem("Review", "Revisar"));
      items.add(new SelectItem("RetainPermanently", "Conservació permanent"));
      items.add(new SelectItem("StartWorkflow", "Inicia tramit"));
      items.add(new SelectItem("EmailNotify", "Notificar per correu electronic"));
      items.add(new SelectItem("SMSNotify", "Notificar per SMS"));
      actionSelectItems = items;
    }
    return actionSelectItems;
  }

}
