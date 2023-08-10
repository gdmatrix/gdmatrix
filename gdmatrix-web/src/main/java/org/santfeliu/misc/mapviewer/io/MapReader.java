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
package org.santfeliu.misc.mapviewer.io;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Stack;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import org.santfeliu.misc.mapviewer.Bounds;
import org.santfeliu.misc.mapviewer.Map;
import org.santfeliu.misc.mapviewer.Map.Group;
import org.santfeliu.misc.mapviewer.Map.InfoLayer;
import org.santfeliu.misc.mapviewer.Map.Service;
import org.santfeliu.misc.mapviewer.Map.Layer;

/**
 *
 * @author realor
 */
public class MapReader
{
  public MapReader()
  {
  }

  public Map read(InputStream is) throws Exception
  {
    Map map = new Map();
    Stack<String> stack = new Stack<String>();
    Service service = null;
    Group group = null;
    Layer layer = null;
    InfoLayer infoLayer = null;
    String[] property = null;

    XMLInputFactory xmlif = XMLInputFactory.newInstance();
    xmlif.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
    XMLStreamReader reader = xmlif.createXMLStreamReader(is);
    try
    {
      while (reader.hasNext())
      {
        int eventType = reader.next();
        if (XMLStreamReader.START_ELEMENT == eventType)
        {
          String tag = reader.getLocalName();
          stack.push(tag);
          if (tag.equals("service"))
          {
            service = map.createService();
            map.getServices().add(service);
          }
          else if (tag.equals("group"))
          {
            group = map.createGroup();
            map.getGroups().add(group);
          }
          else if (tag.equals("layer"))
          {
            layer = map.createLayer();
            map.getLayers().add(layer);
          }
          else if (tag.equals("info-layer"))
          {
            infoLayer = map.createInfoLayer();
            map.getInfoLayers().add(infoLayer);
          }
          else if (tag.equals("property"))
          {
            property = new String[2];
          }
        }
        else if (XMLStreamReader.END_ELEMENT == eventType)
        {
          String tag = stack.pop();
          if (tag.equals("service")) service = null;
          else if (tag.equals("group")) group = null;
          else if (tag.equals("layer")) layer = null;
          else if (tag.equals("info-layer")) infoLayer = null;
          else if (tag.equals("property"))
          {
            map.getProperties().put(property[0], property[1]);
            property = null;
          }
        }
        else if (XMLStreamReader.CHARACTERS == eventType)
        {
          String tag = stack.peek();
          String text = reader.getText();
          if (tag.equals("name"))
          {
            if (service != null) service.setName(text);
            else if (group != null) group.setName(text);
            else if (layer != null) layer.getNames().add(text);
            else if (infoLayer != null) infoLayer.setName(text);
            else if (property != null) property[0] = text;
            else map.setName(text);
          }
          else if (tag.equals("value"))
          {
            property[1] = text;
          }
          else if (tag.equals("title"))
          {
            map.setTitle(text);
          }
          else if (tag.equals("srs"))
          {
            map.setSrs(text);
          }
          else if (tag.equals("category"))
          {
            map.setCategory(text);
          }
          else if (tag.equals("bounds"))
          {
            map.setBounds(new Bounds(text));
          }
          else if (tag.equals("thumbnail-bounds"))
          {
            map.setThumbnailBounds(new Bounds(text));
          }
          else if (tag.equals("description"))
          {
            if (service != null) service.setDescription(text);
            else map.setDescription(text);
          }
          else if (tag.equals("url"))
          {
            service.setUrl(text);
          }
          else if (tag.equals("service-name"))
          {
            String serviceName = text;
            Service layerService = map.getService(serviceName);
            layer.setService(layerService);
          }
          else if (tag.equals("group-name"))
          {
            String groupName = text;
            Group layerGroup = map.getGroup(groupName);
            layer.setGroup(layerGroup);
          }
          else if (tag.equals("label"))
          {
            if (group != null) group.setLabel(text);
            else if (layer != null) layer.setLabel(text);
          }
          else if (tag.equals("sld"))
          {
            layer.setSld(text);
          }
          else if (tag.equals("style"))
          {
            layer.getStyles().add(text);
          }
          else if (tag.equals("base"))
          {
            layer.setBaseLayer("true".equals(text));
          }
          else if (tag.equals("cql-filter"))
          {
            text = text.replace("&lt;", "<");
            text = text.replace("&gt;", ">");
            text = text.replace("&amp;", "&");
            layer.setCqlFilter(text);
          }
          else if (tag.equals("opacity"))
          {
            try
            {
              layer.setOpacity(Double.parseDouble(text));
            }
            catch (Exception ex)
            {
            }
          }
          else if (tag.equals("buffer"))
          {
            try
            {
              layer.setBuffer(Integer.parseInt(text));
            }
            catch (Exception ex)
            {
            }
          }
          else if (tag.equals("transparent"))
          {
            layer.setTransparentBackground("true".equals(text));
          }
          else if (tag.equals("format"))
          {
            layer.setFormat(text);
          }
          else if (tag.equals("view-role"))
          {
            layer.getViewRoles().add(text);
          }
          else if (tag.equals("edit-role"))
          {
            layer.getEditRoles().add(text);
          }
          else if (tag.equals("visible"))
          {
            layer.setVisible("true".equals(text));
          }
          else if (tag.equals("locatable"))
          {
            layer.setLocatable("true".equals(text));
          }
          else if (tag.equals("snap"))
          {
            layer.setSnap("true".equals(text));
          }
          else if (tag.equals("independent"))
          {
            layer.setIndependent("true".equals(text));
          }
          else if (tag.equals("legend"))
          {
            layer.setOnLegend("true".equals(text));
          }
          else if (tag.equals("legend-graphic"))
          {
            if (!"false".equals(text)) layer.setLegendGraphic(text);
          }
          else if (tag.equals("form-selector"))
          {
            infoLayer.setFormSelector(text);
          }
          else if (tag.equals("hilight") || tag.equals("highlight"))
          {
            infoLayer.setHighlight("true".equals(text));
          }
        }
      }
    }
    finally
    {
      reader.close();
    }
    return map;
  }

  public static void main(String args[])
  {
    try
    {
      InputStream is = MapReader.class.getResourceAsStream("map.xml");
      MapReader reader = new MapReader();
      Map map = reader.read(is);
      System.out.println(map.getDescription());
      System.out.println(map.getProperties());
      map.validate(new OutputStreamWriter(System.out));
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
