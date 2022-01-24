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
package org.santfeliu.signature.xmldsig;

import com.sun.org.apache.xml.internal.security.utils.UnsyncBufferedOutputStream;
import com.sun.org.apache.xml.internal.security.utils.XMLUtils;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.xml.security.algorithms.MessageDigestAlgorithm;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.utils.DigesterOutputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author unknown
 */
public class Data // ElementProxy
{
  private Element dataElem;
  private String BaseURI;

  public Data(Document doc, String BaseURI)
  {
    this.dataElem = doc.createElement(XMLSignedDocument.TAG_DATA);
    this.BaseURI = BaseURI;

    Element contentElem = doc.createElement(XMLSignedDocument.TAG_CONTENT);
    Element propertiesElem = doc.createElement(XMLSignedDocument.TAG_PROPERTIES);
    dataElem.appendChild(contentElem);
    dataElem.appendChild(propertiesElem);
  }

  public Data(Element dataElem, String BaseURI)
    throws Exception
  {
    this.dataElem = dataElem;
    this.BaseURI = BaseURI;
  }

  public Element getElement()
  {
    return dataElem;
  }

  public Element getContentElement()
  {
    return (Element)dataElem.getFirstChild();
  }

  public Element getPropertiesElement()
  {
    return (Element)dataElem.getLastChild();
  }

  public String getBaseURI()
  {
    return BaseURI;
  }

  public void setId(String id)
  {
    dataElem.setAttribute("Id", id);
  }

  public String getId()
  {
    return dataElem.getAttribute("Id");
  }

  public void setType(String type)
  {
    dataElem.setAttribute("Type", type);
  }

  public String getType()
  {
    return dataElem.getAttribute("Type");
  }

  public void setContent(Document doc, byte[] content)
    throws Exception
  {
    if ("text".equals(getType()))
    {
      setText(doc, new String(content));
    }
    else if ("xml".equals(getType()))
    {
      setXML(doc, content);
    }
    else if ("binary".equals(getType()))
    {
      setBinary(doc, content);
    }
    else if ("url".equals(getType()))
    {
      setText(doc, new String(content));
    }
  }

  public byte[] getContent()
  {
    if ("text".equals(getType()))
    {
      return getText().getBytes();
    }
    else if ("xml".equals(getType()))
    {
      return getXML();
    }
    else if ("binary".equals(getType()))
    {
      return getBinary();
    }
    else if ("url".equals(getType()))
    {
      return getText().getBytes();
    }
    return null;
  }

  public void setProperties(Document doc, Map properties)
  {
    if (properties != null)
    {
      Iterator iter = properties.entrySet().iterator();
      while (iter.hasNext())
      {
        Map.Entry entry = (Map.Entry)iter.next();
        String propertyName = String.valueOf(entry.getKey());
        String propertyValue = String.valueOf(entry.getValue());
        Element propElem = doc.createElement(propertyName);
        propElem.appendChild(doc.createTextNode(propertyValue));
        getPropertiesElement().appendChild(propElem);
      }
    }
  }

  // xml data
  public void setXML(Document doc, byte[] content) throws Exception
  {
    String s = new String(content); // convert from platform charset
    content = s.getBytes("UTF-8"); // to UTF-8

    //remove previous content
    Element contentElement = getContentElement();
    if (contentElement.getFirstChild() != null)
    {
      contentElement.removeChild(contentElement.getFirstChild());
    }
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
    Document docData = db.parse(new ByteArrayInputStream(content));
    Node node = doc.importNode(docData.getFirstChild(), true);
    contentElement.appendChild(node);
  }

  public byte[] getXML()
  {
    Element contentElement = getContentElement();
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    // TODO: XMLUtils was changed. Really works ????
    XMLUtils.outputDOM(contentElement.getFirstChild(), os);
    return os.toByteArray();
  }

  // text data
  public void setText(Document doc, String text)
  {
    //remove previous content
    Element contentElement = getContentElement();
    if (contentElement.getFirstChild() != null)
    {
      contentElement.removeChild(contentElement.getFirstChild());
    }
    contentElement.appendChild(doc.createTextNode(text));
  }

  public String getText()
  {
    Element contentElement = getContentElement();
    return contentElement.getFirstChild().getNodeValue();
  }

  // binary data: base64 encoding
  public void setBinary(Document doc, byte[] data)
  {
    //remove previous content
    Element contentElement = getContentElement();
    if (contentElement.getFirstChild() != null)
    {
      contentElement.removeChild(contentElement.getFirstChild());
    }
    String data64 = Base64.getMimeEncoder().encodeToString(data);
    contentElement.appendChild(doc.createTextNode(data64));
  }

  public byte[] getBinary()
  {
    Element contentElement = getContentElement();
    String data64 = contentElement.getFirstChild().getNodeValue();
    return Base64.getMimeDecoder().decode(data64);
  }

  public byte[] digest(String hashAlgorithmURN) throws Exception
  {
    MessageDigestAlgorithm mda = MessageDigestAlgorithm.getInstance(
      getElement().getOwnerDocument(), hashAlgorithmURN);

    mda.reset();
    DigesterOutputStream diOs = new DigesterOutputStream(mda);
    OutputStream os = new UnsyncBufferedOutputStream(diOs);
    XMLSignatureInput output = new XMLSignatureInput(getElement());
    output.updateOutputStream(os);
    os.flush();
    return diOs.getDigestValue();
  }
}
