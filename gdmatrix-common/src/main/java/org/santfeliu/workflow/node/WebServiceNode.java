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

import java.util.logging.Logger;

import org.santfeliu.util.Properties;
import org.santfeliu.workflow.WorkflowNode;

/**
 *
 * @author unknown
 */
public class WebServiceNode extends WorkflowNode
{
  protected static final Logger log = Logger.getLogger("WebServiceNode");

  public static final String ENCODING = "UTF-8";
  protected String endpoint;
  protected String requestMessage = "<?xml version=\"1.0\" ?>\n" + 
    "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" + 
    "<S:Body>\n</S:Body>\n</S:Envelope>";
  protected Properties expressions = new Properties();
  protected Properties requestProperties = new Properties();
  protected String username; // basic authorization
  protected String password;
  protected int connectTimeout = 10; // seconds
  protected int readTimeout = 10; // seconds
  
  public WebServiceNode()
  {
  }

  @Override
  public String getType()
  {
    return "WebService";
  }

  public void setEndpoint(String endpoint)
  {
    this.endpoint = endpoint;
  }

  public String getEndpoint()
  {
    return endpoint;
  }

  public void setRequestMessage(String requestMessage)
  {
    this.requestMessage = requestMessage;
  }

  public String getRequestMessage()
  {
    return requestMessage;
  }

  public void setRequestProperties(Properties requestProperties)
  {
    this.requestProperties = requestProperties;
  }

  public Properties getRequestProperties()
  {
    return requestProperties;
  }

  public void setExpressions(Properties expressions)
  {
    this.expressions = expressions;
  }

  public Properties getExpressions()
  {
    return expressions;
  }

  public String getUsername()
  {
    return username;
  }

  public void setUsername(String username)
  {
    this.username = username;
  }
  
  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public void setConnectTimeout(int connectTimeout)
  {
    this.connectTimeout = connectTimeout;
  }

  public int getConnectTimeout()
  {
    return connectTimeout;
  }

  public void setReadTimeout(int readTimeout)
  {
    this.readTimeout = readTimeout;
  }

  public int getReadTimeout()
  {
    return readTimeout;
  }
  
 
}
