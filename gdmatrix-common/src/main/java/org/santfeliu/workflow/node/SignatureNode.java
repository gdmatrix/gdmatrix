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
public class SignatureNode extends WorkflowNode
{
  protected String serviceURL = "http://localhost/services/signature";
  protected String documentVariable;
  protected String operation;
  protected String documentType = "xmldsig"; // default documentType
  protected String dataType;
  protected String content;
  protected Properties properties = new Properties();
  protected String keyAlias;

  public SignatureNode()
  {
  }
  
  @Override
  public String getType()
  {
    return "Signature";
  }
  
  public void setServiceURL(String serviceURL)
  {
    this.serviceURL = serviceURL;
  }

  public String getServiceURL()
  {
    return serviceURL;
  }

  public void setDocumentVariable(String documentVariable)
  {
    this.documentVariable = documentVariable;
  }

  public String getDocumentVariable()
  {
    return documentVariable;
  }

  public void setOperation(String operation)
  {
    this.operation = operation;
  }

  public String getOperation()
  {
    return operation;
  }

  public void setDocumentType(String documentType)
  {
    this.documentType = documentType;
  }

  public String getDocumentType()
  {
    return documentType;
  }

  public void setDataType(String dataType)
  {
    this.dataType = dataType;
  }

  public String getDataType()
  {
    return dataType;
  }

  public void setContent(String content)
  {
    this.content = content;
  }

  public String getContent()
  {
    return content;
  }

  public void setProperties(Properties properties)
  {
    this.properties = properties;
  }

  public Properties getProperties()
  {
    return properties;
  }

  public void setKeyAlias(String keyAlias)
  {
    this.keyAlias = keyAlias;
  }

  public String getKeyAlias()
  {
    return keyAlias;
  }

  @Override
  public Object clone() throws CloneNotSupportedException
  {
    SignatureNode newNode = (SignatureNode)super.clone();
    newNode.properties = (Properties)this.properties.clone();
    return newNode;
  }
  

}
