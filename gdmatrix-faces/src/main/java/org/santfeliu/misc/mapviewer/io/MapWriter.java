package org.santfeliu.misc.mapviewer.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.misc.mapviewer.Bounds;
import org.santfeliu.misc.mapviewer.Map;
import org.santfeliu.misc.mapviewer.Map.Group;
import org.santfeliu.misc.mapviewer.Map.InfoLayer;
import org.santfeliu.misc.mapviewer.Map.Layer;
import org.santfeliu.misc.mapviewer.Map.Service;
import org.santfeliu.util.enc.XMLEncoder;

/**
 *
 * @author realor
 */
public class MapWriter
{
  public void MapWriter()
  {
  }

  public void write(Map map, OutputStream os) throws Exception
  {
    BufferedWriter writer = new BufferedWriter(
      new OutputStreamWriter(os, "UTF-8"));
    write(map, writer);
  }

  public void write(Map map, Writer writer) throws Exception
  {
    try
    {
      writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      writer.write("<map version=\"1.0\">\n");
      writeField(writer, "name", map.getName(), 1);
      writeField(writer, "title", map.getTitle(), 1);
      writeCDATAField(writer, "description", map.getDescription(), 1);
      writeField(writer, "category", map.getCategory(), 1);
      writeField(writer, "srs", map.getSrs(), 1);

      Bounds bounds = map.getBounds();
      writeField(writer, "bounds",
        bounds.getMinX() + "," +
        bounds.getMinY() + "," +
        bounds.getMaxX() + "," +
        bounds.getMaxY(), 1);

      Bounds thumbnailBounds = map.getThumbnailBounds();
      if (thumbnailBounds != null)
      {
        writeField(writer, "thumbnail-bounds",
          thumbnailBounds.getMinX() + "," +
          thumbnailBounds.getMinY() + "," +
          thumbnailBounds.getMaxX() + "," +
          thumbnailBounds.getMaxY(), 1);
      }

      for (Service service : map.getServices())
      {
        writeElement(writer, "<service>", 1);
        writeField(writer, "name", service.getName(), 2);
        writeField(writer, "description", service.getDescription(), 2);
        writeField(writer, "url", service.getUrl(), 2);
        writeElement(writer, "</service>", 1);
      }

      List<Group> groups = map.getGroups();
      for (int groupId = 0; groupId < groups.size(); groupId++)
      {
        Group group = groups.get(groupId);
        writeElement(writer, "<group id=\"" + groupId + "\">", 1);
        writeField(writer, "name", group.getName(), 2);
        writeField(writer, "label", group.getLabel(), 2);
        writeElement(writer, "</group>",  1);
      }

      List<Layer> layers = map.getLayers();
      for (int layerId = 0; layerId < layers.size(); layerId++)
      {
        Layer layer = layers.get(layerId);
        writeElement(writer, "<layer id=\"" + layerId + "\">", 1);
        writeField(writer, "service-name", layer.getService().getName(), 2);
        for (String name : layer.getNames())
        {
          writeField(writer, "name", name, 2);
        }
        if (!StringUtils.isBlank(layer.getLabel()))
        {
          writeField(writer, "label", layer.getLabel(), 2);
        }
        writeField(writer, "base", String.valueOf(layer.isBaseLayer()), 2);
        writeField(writer, "visible", String.valueOf(layer.isVisible()), 2);
        writeField(writer, "locatable", String.valueOf(layer.isLocatable()), 2);
        writeField(writer, "snap", String.valueOf(layer.isSnap()), 2);
        writeField(writer, "independent", String.valueOf(layer.isIndependent()), 2);
        if (layer.getGroup() != null)
        {
          writeField(writer, "group-name",
            String.valueOf(layer.getGroup().getName()), 2);
        }
        writeField(writer, "legend", String.valueOf(layer.isOnLegend()), 2);
        String legendGraphic = layer.getLegendGraphic();
        if (!StringUtils.isBlank(legendGraphic))
        {
          writeField(writer, "legend-graphic", legendGraphic, 2);
        }
        writeField(writer, "opacity", String.valueOf(layer.getOpacity()), 2);
        writeField(writer, "transparent", 
          String.valueOf(layer.isTransparentBackground()), 2);
        writeField(writer, "format", layer.getFormat(), 2);
        String cqlFilter = layer.getCqlFilter();
        if (!StringUtils.isBlank(cqlFilter))
        {
          writeField(writer, "cql-filter", cqlFilter, 2);
        }
        if (!StringUtils.isBlank(layer.getSld()))
        {
          writeField(writer, "sld", layer.getSld(), 2);
        }
        for (String style : layer.getStyles())
        {
          if (!StringUtils.isBlank(style))
          {
            writeField(writer, "style", style, 2);
          }
        }
        for (String viewRole : layer.getViewRoles())
        {
          if (!StringUtils.isBlank(viewRole))
          {
            writeField(writer, "view-role", viewRole, 2);
          }
        }
        for (String editRole : layer.getEditRoles())
        {
          if (!StringUtils.isBlank(editRole))
          {
            writeField(writer, "edit-role", editRole, 2);
          }
        }
        writeElement(writer, "</layer>", 1);
      }

      for (InfoLayer layer : map.getInfoLayers())
      {
        writeElement(writer, "<info-layer>", 1);
        writeField(writer, "name", layer.getName(), 2);
        writeField(writer, "form-selector", layer.getFormSelector(), 2);
        writeField(writer, "highlight", String.valueOf(layer.isHighlight()), 2);
        writeElement(writer, "</info-layer>", 1);
      }

      java.util.Map<String, String> properties = map.getProperties();
      Set<Entry<String, String>> entrySet = properties.entrySet();
      for (Entry<String, String> entry : entrySet)
      {
        String name = entry.getKey();
        String value = entry.getValue();
        writeElement(writer, "<property>", 1);
        writeField(writer, "name", name, 2);
        if (value != null)
        {
          writeField(writer, "value", XMLEncoder.encode(value), 2);
        }
        writeElement(writer, "</property>", 1);
      }
      writer.write("</map>\n");
    }
    finally
    {
      writer.close();
    }
  }

  private void writeElement(Writer writer, String elem, int level)
    throws IOException
  {
    for (int i = 0; i < level * 2; i++)
    {
      writer.write(" ");
    }
    writer.write(elem);
    writer.write("\n");
  }

  private void writeField(Writer writer, String tag, String value,
    int level) throws IOException
  {
    if (value != null)
    {
      for (int i = 0; i < level * 2; i++)
      {
        writer.write(" ");
      }
      writer.write("<");
      writer.write(tag);
      writer.write(">");
      writer.write(XMLEncoder.encode(value));
      writer.write("</");
      writer.write(tag);
      writer.write(">\n");
    }
  }

  private void writeCDATAField(Writer writer, String tag, String value,
    int level) throws IOException
  {
    if (value != null)
    {
      for (int i = 0; i < level * 2; i++)
      {
        writer.write(" ");
      }
      writer.write("<");
      writer.write(tag);
      writer.write("><![CDATA[");
      writer.write(value);
      writer.write("]]></");
      writer.write(tag);
      writer.write(">\n");
    }
  }

  public static void main(String args[])
  {
    try
    {
      InputStream is = MapReader.class.getResourceAsStream("sample.xml");
      MapReader reader = new MapReader();
      Map map = reader.read(is);
      System.out.println(map);
      MapWriter writer = new MapWriter();
      writer.write(map, System.out);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

}
