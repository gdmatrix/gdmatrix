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

/**
 *
 * @author realor
 */
public class Map implements Serializable
{
  String name;
  String title;
  String description;
  String category;
  Camera camera = new Camera();
  java.util.Map<String, Service> services = new HashMap<>();
  java.util.Map<String, Source> sources = new HashMap<>();
  Terrain terrain = new Terrain();
  List<Layer> layers = new ArrayList<>();
  java.util.Map<String, Object> metadata = new HashMap<>();

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getCategory()
  {
    return category;
  }

  public void setCategory(String category)
  {
    this.category = category;
  }

  public Camera getCamera()
  {
    return camera;
  }

  public void setCamera(Camera camera)
  {
    this.camera = camera;
  }

  public Terrain getTerrain()
  {
    return terrain;
  }

  public void setTerrain(Terrain terrain)
  {
    this.terrain = terrain;
  }

  public java.util.Map<String, Service> getServices()
  {
    return services;
  }

  public java.util.Map<String, Source> getSources()
  {
    return sources;
  }

  public List<Layer> getLayers()
  {
    return layers;
  }

  public java.util.Map<String, Object> getMetadata()
  {
    return metadata;
  }

  public void read(Reader reader) throws IOException
  {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(Map.class, (InstanceCreator) (Type type) -> this)
      .create();

    try
    {
      gson.fromJson(reader, Map.class);
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

  @Override
  public String toString()
  {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    return gson.toJson(this);
  }

  public static void main(String[] args)
  {
    try
    {
      Map map = new Map();
      map.read(new File("c:/users/realor/Documents/NetbeansProjects/gdmatrix/gdmatrix-web/src/main/webapp/resources/gdmatrixfaces/maplibre/sample-map.json"));
      System.out.println(map);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
