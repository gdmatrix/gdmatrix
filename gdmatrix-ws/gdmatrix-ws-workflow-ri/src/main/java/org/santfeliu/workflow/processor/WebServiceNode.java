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

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.santfeliu.security.util.BasicAuthorization;
import org.santfeliu.util.XMLPrinter;
import org.santfeliu.util.net.HttpClient;
import org.santfeliu.util.template.Template;
import org.santfeliu.workflow.WorkflowActor;
import org.santfeliu.workflow.WorkflowInstance;
import org.w3c.dom.Document;


/**
 *
 * @author realor
 */
public class WebServiceNode extends org.santfeliu.workflow.node.WebServiceNode 
  implements NodeProcessor
{
  @Override
  public String process(WorkflowInstance instance, WorkflowActor actor)
    throws Exception
  {
    String url = Template.create(endpoint).merge(instance);
    log.log(Level.INFO, "WebServiceNode URL: {0}", url);
    String message = Template.create(requestMessage).merge(instance);

    HttpClient client = new HttpClient();
    client.setURL(url);
    client.setForceHttp(false);
    client.setConnectTimeout(connectTimeout * 1000);
    client.setReadTimeout(readTimeout * 1000);
    client.setRequestProperty("Content-Type", 
      "text/xml;charset=\"" + ENCODING + "\"");

    if (username != null && username.length() > 0 &&
        password != null && password.length() > 0)
    {
      String userId = Template.create(username).merge(instance);
      String pass = Template.create(password).merge(instance);
      BasicAuthorization autho = new BasicAuthorization();
      autho.setUserId(userId);
      autho.setPassword(pass);
      String authoValue = autho.toString();
      client.setRequestProperty("Authorization", authoValue);
    }

    for (Object o : requestProperties.entrySet())
    {
      Map.Entry entry = (Map.Entry)o;
      String property = (String)entry.getKey();
      String value = (String)entry.getValue();
      client.setRequestProperty(property, value);
    }
    log.log(Level.INFO, "WebServiceNode In: \n{0}", message);
    client.doPost(message.getBytes(ENCODING));

    Document document = client.getContentAsXML(true);
    if (document == null)
    {
      String error = client.getHeaderProperty(null);
      if (error == null)
      {
        throw new IOException("Can't read response.");
      }
      else
      {
        throw new IOException(error);
      }
    }
    XPath xPath = XPathFactory.newInstance().newXPath();
    Set set = expressions.entrySet();
    Iterator iter = set.iterator();
    while (iter.hasNext())
    {
      Map.Entry entry = (Map.Entry)iter.next();
      String variable = (String)entry.getKey();
      String xpathExpression = (String)entry.getValue();
      xpathExpression = Template.create(xpathExpression).merge(instance);
      String result = xPath.evaluate(xpathExpression, document);
      instance.put(variable, result);
    }
    XMLPrinter xmlPrinter = new XMLPrinter();
    log.log(Level.INFO, "WebServiceNode Out: \n{0}",
      xmlPrinter.format(document.getDocumentElement()));
    return CONTINUE_OUTCOME;
  }
}
