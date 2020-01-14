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
package org.santfeliu.ant.ws;

import javax.xml.ws.Service;
import org.apache.tools.ant.BuildException;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.ant.js.ScriptableTask;

/**
 *
 * @author blanquepa
 */
public class WSTask extends ScriptableTask
{
  protected String connVar;
  protected String username;
  protected String password;

  public String getConnVar()
  {
    return connVar;
  }

  public void setConnVar(String connVar)
  {
    this.connVar = connVar;
  }

  public void setUsername(String username)
  {
    this.username = username;
  }

  public String getUsername()
  {
    if (username == null)
      return (String)getVariable(connVar + "_username");
    else
      return username;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public String getPassword()
  {
    if (password == null)
      return (String)getVariable(connVar + "_password");
    else
      return password;
  }

  protected WSEndpoint getEndpoint(Class<? extends Service> serviceClass)
  {
    WSDirectory wsDir = (WSDirectory)getVariable(connVar);
    if (wsDir == null) throw new BuildException("connVar undefined");
    return wsDir.getEndpoint(serviceClass);
  }
}
