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

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.santfeliu.util.iarxiu.util.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 *
 * @author blanquepa
 */
public class DCMetadata extends XmlMetadata
{
  private static final String DOCUMENT_NAMESPACE_URI =
    "http://www.openarchives.org/OAI/2.0/oai_dc/";
  private static final String ELEMENT_NAMESPACE_URI =
    "http://purl.org/dc/elements/1.1/";

  private Element element;
  private QName elementsNS;
  
  DCMetadata() throws ParserConfigurationException
  {
    this.type = MetsConstants.DC_MDTYPE_VALUE;

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    org.w3c.dom.Document doc = dbf.newDocumentBuilder().newDocument();
    Element object = doc.createElement("oai:dc");
    object.setAttribute("xmlns:oai", DOCUMENT_NAMESPACE_URI);
    doc.appendChild(object);
    this.element = object;
    this.elementsNS = new QName(ELEMENT_NAMESPACE_URI, "", "ns");
  }

  @Override
  protected void load(Element element, Mets mets) throws Exception
  {
    element = getRootElement(element);

    QName ns = new QName(element.getNamespaceURI(),
      element.getLocalName(), element.getPrefix());
    if (!ns.equals(this.namespace))
      this.namespace = ns;

    Node node = DOMUtils.getFirstChild(element);
    if (node != null)
    {
      this.elementsNS = new QName(node.getNamespaceURI(),
        node.getLocalName(), node.getPrefix());
    }

    this.element = element;
  }

  @Override
  protected void write(Element element, Mets mets) throws Exception
  {
    Document dom = element.getOwnerDocument();
    Node tempNode = dom.importNode(this.element, true);
    element.appendChild(tempNode);
  }

  @Override
  public void setProperty(String name, String value) throws Exception
  {
    if (element != null)
    {
      String prefix = "";
      if (elementsNS != null)
        prefix = elementsNS.getPrefix() + ":";

      Document doc = element.getOwnerDocument();
      Element prop = doc.createElement(prefix + name);
      System.out.println(elementsNS);
      prop.setAttribute("xmlns:" + elementsNS.getPrefix(),
        elementsNS.getNamespaceURI());
      Text text = doc.createTextNode(value);
      prop.appendChild(text);
      element.appendChild(prop);
    }
  }

  @Override
  public String getPropertyValue(String name) throws Exception
  {
    String value = null;
    NodeList nodeList = null;
    if (!name.contains(":"))
    {
      nodeList =
        element.getElementsByTagNameNS(elementsNS.getNamespaceURI(), name);
    }
    else
    {
      nodeList =
        element.getElementsByTagName(name);
    }

    if (nodeList != null && nodeList.getLength() > 0)
    {
      Node node = nodeList.item(0);
      if (node != null)
        value = node.getTextContent();
    }
    return value;
  }

  public List<String> getPropertyNames()
  {
    return getPropertyNames(this.element);
  }

  private List<String> getPropertyNames(Element element)
  {
    List<String> result = new ArrayList();
    if (element != null)
    {
      NodeList nodeList = element.getChildNodes();
      for (int i = 0; i < nodeList.getLength(); i++)
      {
        Node node = nodeList.item(i);
        if (node instanceof Element)
        {
          element = (Element)node;
          result.add(element.getLocalName());
          result.addAll(getPropertyNames(element));
        }
      }
    }

    return result;
  }

  public Element getElement()
  {
    return this.element;
  }

  public void setElement(Element element)
  {
    this.element = element;
  }
}
