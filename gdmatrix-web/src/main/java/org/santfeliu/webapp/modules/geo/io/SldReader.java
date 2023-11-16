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
package org.santfeliu.webapp.modules.geo.io;


import org.santfeliu.misc.mapviewer.io.*;
import java.io.InputStream;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.webapp.modules.geo.sld.SldDebug;
import org.santfeliu.webapp.modules.geo.sld.SldExternalGraphic;
import org.santfeliu.webapp.modules.geo.sld.SldFill;
import org.santfeliu.webapp.modules.geo.sld.SldFont;
import org.santfeliu.webapp.modules.geo.sld.SldGraphic;
import org.santfeliu.webapp.modules.geo.sld.SldHalo;
import org.santfeliu.webapp.modules.geo.sld.SldLinePlacement;
import org.santfeliu.webapp.modules.geo.sld.SldLineSymbolizer;
import org.santfeliu.webapp.modules.geo.sld.SldMark;
import org.santfeliu.webapp.modules.geo.sld.SldNamedLayer;
import org.santfeliu.webapp.modules.geo.sld.SldNode;
import org.santfeliu.webapp.modules.geo.sld.SldPointPlacement;
import org.santfeliu.webapp.modules.geo.sld.SldPointSymbolizer;
import org.santfeliu.webapp.modules.geo.sld.SldPolygonSymbolizer;
import org.santfeliu.webapp.modules.geo.sld.SldRoot;
import org.santfeliu.webapp.modules.geo.sld.SldRule;
import org.santfeliu.webapp.modules.geo.sld.SldStroke;
import org.santfeliu.webapp.modules.geo.sld.SldTextSymbolizer;
import org.santfeliu.webapp.modules.geo.sld.SldUserStyle;

/**
 *
 * @author realor
 */
public class SldReader
{
  public SldRoot read(InputStream is) throws Exception
  {
    XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
    xmlFactory.setProperty(XMLInputFactory.IS_COALESCING, false);
    xmlFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
    xmlFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, true);

    XMLStreamReader xmlReader = xmlFactory.createXMLStreamReader(is);
    SldRoot rootNode = null;
    SldNode currentNode = null;
    try
    {
      while (xmlReader.hasNext())
      {
        xmlReader.next();
        if (xmlReader.isStartElement())
        {
          String name = xmlReader.getLocalName();
          SldNode node = createNode(name, rootNode);
          readAttributes(xmlReader, node);
          if (rootNode == null)
          {
            rootNode = (SldRoot)node;
          }
          else if (currentNode != null)
          {
            currentNode.addChild(node);
          }
          currentNode = node;
        }
        else if (xmlReader.isEndElement())
        {
          if (currentNode != null)
          {
            if (currentNode.getChildCount() == 1)
            {
              SldNode child = currentNode.getChild(0);
              if (child.getName() == null) // textNode
              {
                currentNode.setTextValue(child.getTextValue());
                currentNode.removeChild(0);
              }
            }
            currentNode = currentNode.getParent();
          }
        }
        else if (xmlReader.isCharacters())
        {
          String text = xmlReader.getText();
          text = text.trim().replaceAll("\n", "");
          if (currentNode != null && !blankText(text))
          {
            SldNode textNode = new SldNode();
            textNode.setTextValue(text);
            currentNode.addChild(textNode);
          }
        }
      }
    }
    finally
    {
      xmlReader.close();
    }
    return rootNode;
  }

  private void readAttributes(XMLStreamReader xmlReader, SldNode node)
  {
    int count = xmlReader.getAttributeCount();
    Map<String, String> attributes = node.getAttributes();
    for (int i = 0; i < count; i++)
    {
      String attributePrefix = xmlReader.getAttributePrefix(i);
      String attributeLocalName = xmlReader.getAttributeLocalName(i);
      String attributeValue = xmlReader.getAttributeValue(i);
      String attributeName = StringUtils.isBlank(attributePrefix) ?
        attributeLocalName : attributePrefix + ":" + attributeLocalName;
      attributes.put(StringEscapeUtils.escapeXml(attributeName),
        StringEscapeUtils.escapeXml(attributeValue));
    }
  }

  private boolean blankText(String text)
  {
    boolean blank = true;
    int i = 0;
    while (blank && i < text.length())
    {
      char ch = text.charAt(i);
      if (ch != ' ' && ch != '\n' && ch != '\r' && ch != '\t') blank = false;
      i++;
    }
    return blank;
  }

  private SldNode createNode(String tagName, SldNode rootNode)
  {
    String prefix;
    String name;
    int index = tagName.indexOf(":");
    if (index == -1)
    {
      prefix = null;
      name = tagName;
    }
    else
    {
      prefix = tagName.substring(0, index);
      name = tagName.substring(index + 1);
    }
    SldNode node;
    if ("StyledLayerDescriptor".equals(name))
    {
      node = new SldRoot(prefix, name);
    }
    else if ("NamedLayer".equals(name))
    {
      node = new SldNamedLayer(prefix, name);
    }
    else if ("UserStyle".equals(name))
    {
      node = new SldUserStyle(prefix, name);
    }
    else if ("Rule".equals(name))
    {
      node = new SldRule(prefix, name);
    }
    else if ("PointSymbolizer".equals(name))
    {
      node = new SldPointSymbolizer(prefix, name);
    }
    else if ("LineSymbolizer".equals(name))
    {
      node = new SldLineSymbolizer(prefix, name);
    }
    else if ("PolygonSymbolizer".equals(name))
    {
      node = new SldPolygonSymbolizer(prefix, name);
    }
    else if ("TextSymbolizer".equals(name))
    {
      node = new SldTextSymbolizer(prefix, name);
    }
    else if ("Stroke".equals(name))
    {
      node = new SldStroke(prefix, name);
    }
    else if ("Fill".equals(name))
    {
      node = new SldFill(prefix, name);
    }
    else if ("Halo".equals(name))
    {
      node = new SldHalo(prefix, name);
    }
    else if ("Font".equals(name))
    {
      node = new SldFont(prefix, name);
    }
    else if ("Mark".equals(name))
    {
      node = new SldMark(prefix, name);
    }
    else if ("ExternalGraphic".equals(name))
    {
      node = new SldExternalGraphic(prefix, name);
    }
    else if ("Graphic".equals(name))
    {
      node = new SldGraphic(prefix, name);
    }
    else if ("PointPlacement".equals(name))
    {
      node = new SldPointPlacement(prefix, name);
    }
    else if ("LinePlacement".equals(name))
    {
      node = new SldLinePlacement(prefix, name);
    }
    else
    {
      node = new SldNode(prefix, name);
    }
    return node;
  }

  public static void main(String[] args)
  {
    try
    {
      SldReader reader = new SldReader();
      InputStream is = MapReader.class.getResourceAsStream("sld3.xml");
      SldRoot root = reader.read(is);
      SldDebug debug = new SldDebug();
      debug.printNode(root, System.out);
      System.out.println("\n");
      debug.printSLD(root, System.out);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
