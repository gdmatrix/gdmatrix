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
import org.santfeliu.util.iarxiu.util.DOMUtils;
import javax.xml.namespace.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 *
 * @author blanquepa
 */
public class OtherMetadata extends XmlMetadata
{
  private String urn;
  protected String[] names;
  protected String[] values;

  OtherMetadata()
  {
    this.type = MetsConstants.OTHER_MDTYPE_VALUE;
  }

  protected String getUrn()
  {
    return urn;
  }

  protected void setUrn(String urn)
  {
    this.urn = urn;
  }

  protected void load(Element mdWrapElement, Mets mets) throws Exception
  {
    this.urn = mdWrapElement.getAttribute(MetsConstants.OTHERMDTYPE_ATTRIBUTE);
    XSDInfo xsdInfo = mets.regSchemas.get(urn);
    if (xsdInfo != null)
    {
      setNamespace(xsdInfo.getNamespace());
      setNames(xsdInfo.getPropertyNames());
    }
    else throw new Exception("VOCABULARY_NOT_REGISTERED");

    loadMetadata(getRootElement(mdWrapElement));
  }

  protected void write(Element mdWrapElement, Mets mets) throws Exception
  {
    Document doc = mdWrapElement.getOwnerDocument();
    Element xmlData =
      doc.createElement(setMetsPrefix(MetsConstants.XMLDATA_TAG));
    mdWrapElement.appendChild(xmlData);

    String local = namespace.getLocalPart();
    String prefix = namespace.getPrefix();
    Element element = doc.createElement(prefix + ":" + local);
    element.setAttribute("xmlns:" + prefix, namespace.getNamespaceURI());
    xmlData.appendChild(element);

    if (names != null)
    {
      for (int i = 0; i < names.length; i++)
      {
        String name = names[i];
        String value = values[i];
        if (value != null)
        {
          Element prop = doc.createElement(prefix + ":" + name);
          Text text = doc.createTextNode(value);
          prop.appendChild(text);
          element.appendChild(prop);
        }
      }
    }
  }

  private String setMetsPrefix(String tag)
  {
    return MetsConstants.METS_PREFIX + ":" + tag;
  }

  protected void setNames(String[] names)
  {
    this.names = names;
    this.values = new String[names.length];
  }

  protected void setValues(String[] values)
  {
    this.values = values;
  }

  public void setProperty(String name, String value) throws Exception
  {
    if (values != null)
    {
      int index = getIndex(name);
      if (index == -1)
        throw new Exception("Property '" + name + "' not exists in this vocabulary");
      else
        values[index] = value;
    }
    else
      throw new Exception("VALUES_NOT_INITIALIZED");
  }

  public void removeProperty(String name)
  {
    try
    {
      setProperty(name, null);
    }
    catch (Exception ex)
    {
      //Property not declared
    }
  }

  public String getPropertyValue(String name) throws Exception
  {
    if (values != null)
    {
      return values[getIndex(name)];
    }
    else
      return null;
  }

  public int getPropertyCount()
  {
    if (names != null)
      return names.length;
    else
      return 0;
  }

  public String getPropertyName(int position)
  {
    if (position < getPropertyCount())
    {
      return names[position];
    }
    else
      return null;
  }

  public String getPropertyValue(int position)
  {
    if (position < getPropertyCount())
    {
      return values[position];
    }
    else
      return null;
  }

  public List<String> getPropertyNames()
  {
    List<String> result = new ArrayList();
    for (int i = 0; i < names.length; i++)
    {
      result.add(names[i]);
    }

    return result;
  }

  protected int getIndex(String name)
  {
    if (names != null && name != null)
    {
      for (int i = 0; i < names.length; i++)
      {
        if (name.equals(names[i])) return i;
      }
    }
    return -1;
  }

  private void loadMetadata(Element element)
    throws Exception
  {
    QName ns = new QName(element.getNamespaceURI(),
      element.getLocalName(), element.getPrefix());
    if (!ns.equals(this.namespace))
      this.namespace = ns;

    //vocabulary elements (suposed only one level element)
    Node node = DOMUtils.getFirstChild(element);
    while (node != null)
    {
      String name = node.getLocalName();
      String value = node.getTextContent();
      this.setProperty(name, value);
      node = DOMUtils.getNextSibling(node);
    }
  }
}
