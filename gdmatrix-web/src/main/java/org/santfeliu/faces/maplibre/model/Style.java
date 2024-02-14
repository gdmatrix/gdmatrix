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
package org.santfeliu.faces.maplibre.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * Maplibre style (https://maplibre.org/maplibre-style-spec/root/)
 *
 * @author realor
 */
public class Style implements Serializable
{
  String name;
  int version = 8;
  double[] center = new double[]{2.045, 41.384};
  double zoom = 10;
  double bearing;
  double pitch;
  List sprite;
  String glyphs =  "https://demotiles.maplibre.org/font/{fontstack}/{range}.pbf";
  Map<String, Source> sources = new HashMap<>();
  Terrain terrain;
  Sky sky;
  Light light;
  List<Layer> layers = new ArrayList<>();
  Map<String, Object> metadata = new HashMap<>();

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public int getVersion()
  {
    return version;
  }

  public void setVersion(int version)
  {
    this.version = version;
  }

  public double[] getCenter()
  {
    return center;
  }

  public void setCenter(double[] center)
  {
    this.center = center;
  }

  public double getZoom()
  {
    return zoom;
  }

  public void setZoom(double zoom)
  {
    this.zoom = zoom;
  }

  public double getBearing()
  {
    return bearing;
  }

  public void setBearing(double bearing)
  {
    this.bearing = bearing;
  }

  public double getPitch()
  {
    return pitch;
  }

  public void setPitch(double pitch)
  {
    this.pitch = pitch;
  }

  public List getSprite()
  {
    return sprite;
  }

  public void setSprite(List sprite)
  {
    this.sprite = sprite;
  }

  public String getGlyphs()
  {
    return glyphs;
  }

  public void setGlyphs(String glyphs)
  {
    this.glyphs = glyphs;
  }

  public Terrain getTerrain()
  {
    return terrain;
  }

  public void setTerrain(Terrain terrain)
  {
    this.terrain = terrain;
  }

  public Sky getSky()
  {
    return sky;
  }

  public void setSky(Sky sky)
  {
    this.sky = sky;
  }

  public Light getLight()
  {
    return light;
  }

  public void setLight(Light light)
  {
    this.light = light;
  }

  public Map<String, Source> getSources()
  {
    if (sources == null) sources = new HashMap<>();
    return sources;
  }

  public List<Layer> getLayers()
  {
    if (layers == null) layers = new ArrayList<>();
    return layers;
  }

  public Map<String, Object> getMetadata()
  {
    if (metadata == null) metadata = new HashMap<>();
    return metadata;
  }

  public void read(Reader reader) throws IOException
  {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(Style.class, (InstanceCreator) (Type type) -> this)
      .create();

    try
    {
      gson.fromJson(reader, Style.class);
    }
    finally
    {
      reader.close();
    }
  }

  public void read(File file) throws IOException
  {
    read(new FileReader(file));
  }

  public void write(Writer writer) throws IOException
  {
    Gson gson = new Gson();
    try
    {
      gson.toJson(this, writer);
    }
    finally
    {
      writer.close();
    }
  }

  public void write(File file) throws IOException
  {
    write(new FileWriter(file));
  }

  public void fromString(String json) throws IOException
  {
    read(new StringReader(json));
  }

  public void cleanUp()
  {
    if (terrain != null && isBlank(terrain.source))
    {
      terrain = null;
    }
    for (Source source : getSources().values())
    {
      if ("geojson".equals(source.getType()))
      {
        source.setTiles(null);
        source.setBounds(null);
      }
    }
  }

  @Override
  public String toString()
  {
    Gson gson = new GsonBuilder()
      .setPrettyPrinting()
      .disableHtmlEscaping().create();
    return gson.toJson(this);
  }
}
