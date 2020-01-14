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
package org.matrix.util;

import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author realor
 */
public class WSDirectoryLoader
{
  public static final String DIRECTORY_TAG = "directory";
  public static final String ENDPOINT_TAG = "endpoint";
  public static final String DESCRIPTION_TAG = "description";
  public static final String INTERNAL_ENTITY_TAG = "internal-entity";
  public static final String EXTERNAL_ENTITY_TAG = "external-entity";
  public static final String NAME_ATTRIBUTE = "name";
  public static final String SERVICE_ATTRIBUTE = "service";
  public static final String URL_ATTRIBUTE = "url";
  public static final String WSDL_ATTRIBUTE = "wsdl";
  public static final String PREFIX_ATTRIBUTE = "prefix";
  public static final String ONLY_LOCAL_ATTRIBUTE = "only-local";
  public static final String FROM_ATTRIBUTE = "from";

  public static final Logger logger = Logger.getLogger("WSConfigLoader");

  public void load(InputStream is, WSDirectory directory)
    throws Exception
  {
    DocumentBuilder docBuilder =
      DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document doc = docBuilder.parse(is);
    Node node = doc.getFirstChild();
    String tagName = node.getNodeName();
    if (DIRECTORY_TAG.equals(tagName))
    {
      node = node.getFirstChild();
      while (node != null)
      {
        if (node instanceof Element)
        {
          Element element = (Element)node;
          tagName = element.getNodeName();
          if (ENDPOINT_TAG.equals(tagName))
          {
            loadEndpoint(element, directory);
          }
          else
          {
            // warn
          }
        }
        node = node.getNextSibling();
      }
    }
    else throw new Exception("'" + DIRECTORY_TAG  + "' tag expected");
  }

  private void loadEndpoint(Element element, WSDirectory directory)
    throws Exception
  {
    // read endpoint name
    String name = element.getAttribute(NAME_ATTRIBUTE);
    if (name == null || name.trim().length() == 0)
      throw new Exception("'" + NAME_ATTRIBUTE +
        "' attribute is mandatory in '" + ENDPOINT_TAG + " tag");

    // read service name
    String serviceName = element.getAttribute(SERVICE_ATTRIBUTE);
    if (serviceName == null || serviceName.trim().length() == 0)
      throw new Exception("'" + SERVICE_ATTRIBUTE +
        "' attribute is mandatory in '" + ENDPOINT_TAG + " tag");

    WSEndpoint endpoint = directory.newEndpoint(name, getQName(serviceName));

    // read wsdlLocation
    String wsdl = element.getAttribute(WSDL_ATTRIBUTE);
    if (wsdl == null || wsdl.trim().length() == 0)
      throw new Exception("'" + WSDL_ATTRIBUTE +
        "' attribute is mandatory in '" + ENDPOINT_TAG + " tag");
    endpoint.setWsdlLocation(new URL(wsdl));

    // read url
    String url = element.getAttribute(URL_ATTRIBUTE);
    if (url != null && url.trim().length() > 0)
      endpoint.setUrl(new URL(url));

    String onlyLocal = element.getAttribute(ONLY_LOCAL_ATTRIBUTE);
    if (onlyLocal != null && onlyLocal.length() > 0)
      endpoint.setOnlyLocal(Boolean.parseBoolean(onlyLocal));

    Node node = element.getFirstChild();
    while (node != null)
    {
      if (node instanceof Element)
      {
        Element childElement = (Element)node;
        String tagName = childElement.getNodeName();
        if (DESCRIPTION_TAG.equals(tagName))
        {
          String description = childElement.getTextContent();
          endpoint.setDescription(description);
        }
        else if (INTERNAL_ENTITY_TAG.equals(tagName))
        {
          loadInternalEntity(childElement, endpoint);
        }
        else if (EXTERNAL_ENTITY_TAG.equals(tagName))
        {
          loadExternalEntity(childElement, endpoint);
        }
        else
        {
          // warn
        }
      }
      node = node.getNextSibling();
    }
  }

  private void loadInternalEntity(Element element, WSEndpoint endpoint)
    throws Exception
  {
    String name = element.getAttribute(NAME_ATTRIBUTE);
    if (name == null || name.trim().length() == 0)
      throw new Exception("'" + NAME_ATTRIBUTE +
         "' is mandatory in '" + INTERNAL_ENTITY_TAG + "' tag");

    InternalEntity internalEntity = endpoint.newInternalEntity(name);

    String prefix = element.getAttribute(PREFIX_ATTRIBUTE);
    if (prefix != null && prefix.length() > 0)
      internalEntity.setPrefix(prefix);
  }

  private void loadExternalEntity(Element element, WSEndpoint endpoint)
    throws Exception
  {
    String name = element.getAttribute(NAME_ATTRIBUTE);
    if (name == null || name.trim().length() == 0)
      throw new Exception("'" + NAME_ATTRIBUTE + 
        "' attribute is mandatory in '" + EXTERNAL_ENTITY_TAG + "' tag");

    ExternalEntity externalEntity = endpoint.newExternalEntity(name);

    String from = element.getAttribute(FROM_ATTRIBUTE);
    if (from != null && from.trim().length() > 0)
    {
      externalEntity.setFrom(from);
    }
  }

  private QName getQName(String name)
  {
    String namespaceURI = null;
    String serviceName = null;
    name = name.trim();
    int index = name.indexOf("}");
    if (index != -1)
    {
      namespaceURI = name.substring(1, index);
      serviceName = name.substring(index + 1);
    }
    else
    {
      serviceName = name;
    }
    return new QName(namespaceURI, serviceName);
  }
}
