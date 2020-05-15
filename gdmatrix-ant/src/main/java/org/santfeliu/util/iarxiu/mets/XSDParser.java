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
package org.santfeliu.util.iarxiu.mets;

import java.io.InputStream;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author blanquepa
 */
public class XSDParser
{
  private static final String ELEMENT_TAG = "element";
  private static final String COMPLEX_TYPE_TAG = "complexType";
  private static final String SEQUENCE_TAG = "sequence";
  private static final String NAME_ATTRIBUTE = "name";
  private static final String TARGET_NAMESPACE_ATTRIBUTE = "targetNamespace";
  private static final String TYPE_ATTRIBUTE = "type";

  public XSDInfo parse(InputStream is) throws Exception
  {
    XSDInfo xsdInfo = new XSDInfo();

    DocumentBuilderFactory dbf =
      DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    Document doc = dbf.newDocumentBuilder().parse(is);

    Element rootElement = doc.getDocumentElement();
    String targetNamespace = rootElement.getAttribute(TARGET_NAMESPACE_ATTRIBUTE);

    Node node = rootElement.getFirstChild();
    if (node != null && node.getNodeType() == Node.TEXT_NODE)
      node = node.getNextSibling();

    if (ELEMENT_TAG.equals(node.getLocalName()))
    {
      if (node instanceof Element)
      {
        Element element = (Element)node;
        String type = element.getAttribute(TYPE_ATTRIBUTE);
        String localName = element.getAttribute(NAME_ATTRIBUTE);
        String prefix =
          (localName.length() > 2 ? localName.substring(0, 3) : localName);
        xsdInfo.setNamespace(new QName(targetNamespace, localName, prefix));

        node = element.getNextSibling();
        if (node != null && node.getNodeType() == Node.TEXT_NODE)
          node = node.getNextSibling();
        while(node != null)
        {
          if (COMPLEX_TYPE_TAG.equals(node.getLocalName()))
          {
            if (node instanceof Element)
            {
              element = (Element)node;
              if (type.equals(element.getAttribute(NAME_ATTRIBUTE)))
              {
                parseComplexType(element, xsdInfo);
              }
            }
          }
          node = node.getNextSibling();
          if (node != null && node.getNodeType() == Node.TEXT_NODE)
            node = node.getNextSibling();
        }
      }
    }

    return xsdInfo;
  }

  private void parseComplexType(Element element, XSDInfo xsdInfo)
  {
    Node node = element.getFirstChild();
    if (node != null && node.getNodeType() == Node.TEXT_NODE)
      node = node.getNextSibling();

    if (SEQUENCE_TAG.equals(node.getLocalName()))
    {
      node = node.getFirstChild();
      if (node != null && node.getNodeType() == Node.TEXT_NODE)
        node = node.getNextSibling();

      while (node != null)
      {
        if (ELEMENT_TAG.equals(node.getLocalName()))
        {
          if (node instanceof Element)
          {
            element = (Element)node;
            String name = element.getAttribute(NAME_ATTRIBUTE);
            xsdInfo.addPropertyName(name);
          }
        }

        node = node.getNextSibling();
        if (node != null && node.getNodeType() == Node.TEXT_NODE)
          node = node.getNextSibling();
      }
    }
  }
}
