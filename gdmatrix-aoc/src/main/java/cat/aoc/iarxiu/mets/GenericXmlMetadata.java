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
package cat.aoc.iarxiu.mets;

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 *
 * @author blanquepa
 */
public class GenericXmlMetadata extends XmlMetadata
{
  protected Element element;

  @Override
  protected void load(Element element, Mets mets) throws Exception
  {
    element = getRootElement(element);

    QName ns = new QName(element.getNamespaceURI(),
      element.getLocalName(), element.getPrefix());
    if (!ns.equals(this.namespace))
      this.namespace = ns;

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
      Node node = getProperty(name);
      if (node != null)
      {
        node.setTextContent(value);
      }
      else
      {
        String prefix = "";
        if (namespace != null)
          prefix = namespace.getPrefix() + ":";

        Document doc = element.getOwnerDocument();
        Element prop = doc.createElement(prefix + name);
        Text text = doc.createTextNode(value);
        prop.appendChild(text);
        element.appendChild(prop);
      }
    }
  }

  @Override
  public String getPropertyValue(String name) throws Exception
  {
    String value = null;

    Node node = getProperty(name);
    if (node != null)
      value = node.getTextContent();

    return value;
  }

  @Override
  public List<String> getPropertyNames()
  {
    return getPropertyNames(this.element);
  }

  public Element getElement()
  {
    return this.element;
  }

  public void setElement(Element element)
  {
    this.element = element;
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

  private Node getProperty(String name)
  {
    Node node = null;
    NodeList nodeList = null;
    if (!name.contains(":"))
    {
      nodeList =
        element.getElementsByTagNameNS(namespace.getNamespaceURI(), name);
    }
    else
    {
      nodeList =
        element.getElementsByTagName(name);
    }

    if (nodeList != null && nodeList.getLength() > 0)
      node = nodeList.item(0);

    return node;
  }
}
