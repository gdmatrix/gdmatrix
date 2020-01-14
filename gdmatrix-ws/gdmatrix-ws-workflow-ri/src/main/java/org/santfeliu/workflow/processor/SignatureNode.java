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
package org.santfeliu.workflow.processor;

import org.matrix.signature.PropertyList;
import org.matrix.signature.SignatureManagerPort;
import org.matrix.signature.SignatureManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;

import org.santfeliu.signature.PropertyListConverter;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.Properties;
import org.santfeliu.util.template.Template;
import org.santfeliu.workflow.WorkflowActor;
import org.santfeliu.workflow.WorkflowInstance;
import org.santfeliu.ws.WSPortFactory;


/**
 *
 * @author unknown
 */
public class SignatureNode extends org.santfeliu.workflow.node.SignatureNode 
  implements NodeProcessor
{
  
  @Override
  public String process(WorkflowInstance instance, WorkflowActor actor)
    throws Exception
  {
    SignatureManagerPort port = getSignatureManagerPort(instance);

    if (operation.equals("createDocument"))
    {
      String type = Template.create(documentType).merge(instance);
      String sigId = port.createDocument(type, getFinalProperties(instance));
      instance.put(documentVariable, sigId);
    }
    else if (operation.equals("setDocumentProperties"))
    {
      String sigId = String.valueOf(instance.get(documentVariable));
      port.setDocumentProperties(sigId, getFinalProperties(instance));
    }
    else if (operation.equals("addData"))
    {
      String sigId = String.valueOf(instance.get(documentVariable));
      String type = Template.create(dataType).merge(instance);
      String text = Template.create(content).merge(instance);
      port.addData(sigId, type, text.getBytes(), 
        getFinalProperties(instance));
    }
    else if (operation.equals("addSystemSignature"))
    {
      String sigId = String.valueOf(instance.get(documentVariable));
      port.addSystemSignature(sigId, keyAlias);
    }
    else if (operation.equals("endDocument"))
    {
      String sigId = String.valueOf(instance.get(documentVariable));
      port.endDocument(sigId, getFinalProperties(instance));
    }
    else if (operation.equals("abortDocument"))
    {
      String sigId = String.valueOf(instance.get(documentVariable));
      port.abortDocument(sigId);
    }
    return CONTINUE_OUTCOME;
  }
  
  private PropertyList getFinalProperties(WorkflowInstance instance)
  {
    Properties finalProps = new Properties();
    Template.merge(properties, finalProps, instance);
    return PropertyListConverter.toPropertyList(finalProps);
  }

  private SignatureManagerPort getSignatureManagerPort(
    WorkflowInstance instance) throws Exception
  {
    String userId =
      MatrixConfig.getProperty("adminCredentials.userId");
    String password =
      MatrixConfig.getProperty("adminCredentials.password");

    String wsdlLocation = null;
    if (serviceURL != null && serviceURL.trim().length() > 0)
    {
      wsdlLocation = Template.create(serviceURL).merge(instance) + "?wsdl";
      return WSPortFactory.getPort(SignatureManagerPort.class,
        wsdlLocation, userId, password);
    }
    else // default wsdlLocation
    {
      WSEndpoint endpoint =
        WSDirectory.getInstance().getEndpoint(SignatureManagerService.class);
      return endpoint.getPort(SignatureManagerPort.class, userId, password);
    }
  }
}
