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
package org.santfeliu.misc.mapviewer;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author realor
 */
public class Map implements Serializable
{
  public static final String MAP_BACKGROUND_PROPERTY = "mapBackground";
  public static final String DESCRIPTION_BREAK_TAG = "-break-";

  private String name;
  private String title;
  private String description;
  private String category;
  private String srs;
  private Bounds bounds = new Bounds();
  private Bounds thumbnailBounds = null;
  private final ServiceList services = new ServiceList();
  private final GroupList groups = new GroupList();
  private final LayerList layers = new LayerList();
  private final List<InfoLayer> infoLayers = new ArrayList<InfoLayer>();
  private final HashMap<String, String> properties = new HashMap<String, String>();

  public Map()
  {
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    if (name != null)
    {
      name = Normalizer.normalize(name, Normalizer.Form.NFD);
      name = name.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
      name = name.toLowerCase();
      name = name.replace(' ', '_');
    }
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

  public boolean isDescriptionFormatted()
  {
    if (description == null) return false;
    return description.contains("<") && description.contains(">");
  }

  public Bounds getBounds()
  {
    return bounds;
  }

  public void setBounds(Bounds bounds)
  {
    this.bounds = bounds;
  }

  public Bounds getThumbnailBounds()
  {
    return thumbnailBounds;
  }

  public void setThumbnailBounds(Bounds thumbnailBounds)
  {
    this.thumbnailBounds = thumbnailBounds;
  }

  public String getSrs()
  {
    return srs;
  }

  public void setSrs(String srs)
  {
    this.srs = srs;
  }

  public void setTo(Map map)
  {
    name = map.name;
    title = map.title;
    description = map.description;
    srs = map.srs;
    bounds = new Bounds(map.bounds);
    if (map.thumbnailBounds != null)
    {
      thumbnailBounds = new Bounds(map.thumbnailBounds);
    }
    services.clear();
    for (Service service : map.services)
    {
      Service newService = createService();
      newService.setTo(service);
      services.add(newService);
    }
    for (Group group : map.getGroups())
    {
      Group newGroup = createGroup();
      newGroup.setTo(group);
      groups.add(newGroup);
    }
    for (Layer layer : map.getLayers())
    {
      Service service = layer.getService();
      int serviceId = map.services.indexOf(service);

      Group group = layer.getGroup();
      int groupId = map.groups.indexOf(group);

      Layer newLayer = createLayer();
      newLayer.setTo(layer);
      newLayer.setService(services.get(serviceId));
      if (groupId != -1)
      {
        newLayer.setGroup(groups.get(groupId));
      }
      layers.add(newLayer);
    }
    infoLayers.clear();
    for (InfoLayer infoLayer : map.getInfoLayers())
    {
      InfoLayer newInfoLayer = createInfoLayer();
      newInfoLayer.setTo(infoLayer);
      infoLayers.add(newInfoLayer);
    }
    properties.clear();
    properties.putAll(map.properties);
  }

  public boolean isComplete()
  {
    return !getServices().isEmpty() && !getLayers().isEmpty();
  }

  public java.util.Map<String, String> getProperties()
  {
    return properties;
  }

  // services

  public Service createService()
  {
    return new Service();
  }

  public List<Service> getServices()
  {
    return services;
  }

  public Service getService(String name)
  {
    boolean found = false;
    Iterator<Service> iter = services.iterator();
    Service service = null;
    while (iter.hasNext() && !found)
    {
      service = iter.next();
      if (service.getName().equals(name)) found = true;
    }
    return found ? service : null;
  }

  // layers

  public Layer createLayer()
  {
    return new Layer();
  }

  public List<Layer> getLayers()
  {
    return layers;
  }

  public List<Layer> getLayers(Service service, Boolean baseLayer)
  {
    ArrayList<Layer> serviceLayers = new ArrayList<Layer>();
    for (Layer layer : layers)
    {
      if (layer.service == service)
      {
        if (baseLayer == null || baseLayer.equals(layer.isBaseLayer()))
        {
          serviceLayers.add(layer);
        }
      }
    }
    return serviceLayers;
  }

  public List<Layer> getLayers(Group group, Boolean baseLayer)
  {
    ArrayList<Layer> groupLayers = new ArrayList<Layer>();
    for (Layer layer : layers)
    {
      if (layer.group == group)
      {
        if (baseLayer == null || baseLayer.equals(layer.isBaseLayer()))
        {
          groupLayers.add(layer);
        }
      }
    }
    return groupLayers;
  }

  public Layer getLayer(String name)
  {
    boolean found = false;
    Iterator<Layer> iter = layers.iterator();
    Layer layer = null;
    while (iter.hasNext() && !found)
    {
      layer = iter.next();
      if (layer.getNames().contains(name)) found = true;
    }
    return found ? layer : null;
  }

  public Group getGroup(String name)
  {
    boolean found = false;
    Iterator<Group> iter = groups.iterator();
    Group group = null;
    while (iter.hasNext() && !found)
    {
      group = iter.next();
      if (group.getName().equals(name)) found = true;
    }
    return found ? group : null;
  }

  public Layer getDefaultBaseLayer()
  {
    Layer defaultBaseLayer = null;
    Iterator<Layer> iter = layers.iterator();
    while (defaultBaseLayer == null && iter.hasNext())
    {
      Layer layer = iter.next();
      if (layer.isBaseLayer()) defaultBaseLayer = layer;
    }
    return defaultBaseLayer;
  }

  // info layers

  public List<InfoLayer> getInfoLayers()
  {
    return infoLayers;
  }

  public InfoLayer getInfoLayer(String name)
  {
    int i = 0;
    boolean found = false;
    while (i < infoLayers.size() && !found)
    {
      if (infoLayers.get(i).getName().equals(name)) found = true;
      else i++;
    }
    return found ? infoLayers.get(i) : null;
  }

  public InfoLayer createInfoLayer()
  {
    return new InfoLayer();
  }

  // groups

  public Group createGroup()
  {
    return new Group();
  }

  public List<Group> getGroups()
  {
    return groups;
  }

  public boolean validate(Writer out) throws IOException
  {
    boolean valid = true;
    for (Service service : services)
    {
      if (service.getName() == null)
      {
        out.write("ERROR: Service with no name!");
        valid = false;
      }
      else if (service.getUrl() == null)
      {
        out.write("ERROR: Service '" + service.getName() + "' with no url!\n");
        valid = false;
      }
    }
    for (Layer layer : layers)
    {
      if (layer.getNames().isEmpty())
      {
        out.write("ERROR: Layer with no name!\n");
        valid = false;
      }
      else if (layer.getService() == null)
      {
        out.write("ERROR: Service is null for layer '" +
          layer.getNamesString() + "'\n");
        valid = false;
      }
      out.flush();
    }
    return valid;
  }

  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append("Map(name: ").append(name).append(",\n");
    builder.append("title: ").append(title).append(",\n");
    builder.append("description: ").append(description).append(",\n");
    builder.append("srs: ").append(srs).append(",\n");
    builder.append("bounds: ").append(bounds).append(",\n");
    if (thumbnailBounds != null)
    {
      builder.append("thumbnail bounds: ").append(thumbnailBounds).append(",\n");
    }
    for (Service service : services)
    {
      builder.append(service).append("\n");
    }
    for (Layer layer : layers)
    {
      builder.append(layer).append("\n");
    }
    builder.append(")\n");
    return builder.toString();
  }

  // list classes

  class ServiceList extends ArrayList<Service>
  {
    @Override
    public boolean add(Service service)
    {
      if (service.getMap() != Map.this)
        throw new RuntimeException("INVALID_SERVICE");
      return super.add(service);
    }

    @Override
    public Service set(int index, Service service)
    {
      if (service.getMap() != Map.this)
        throw new RuntimeException("INVALID_SERVICE");
      return super.set(index, service);
    }

    @Override
    public boolean addAll(Collection<? extends Service> c)
    {
      checkCollection(c);
      return super.addAll(c);
    }

    @Override
    public Service remove(int index)
    {
      Service service = super.remove(index);
      removeLayers(service);
      return service;
    }

    @Override
    public boolean remove(Object o)
    {
      removeLayers(o);
      return super.remove(o);
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex)
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends Service> c)
    {
      checkCollection(c);
      return super.addAll(index, c);
    }

    private void checkCollection(Collection<? extends Service> c)
    {
      for (Service service : c)
      {
        if (service.getMap() != Map.this)
          throw new RuntimeException("INVALID_SERVICE");
      }
    }

    private void removeLayers(Object o)
    {
      if (o instanceof Service)
      {
        Service service = (Service)o;
        ArrayList<Layer> layersToRemove = new ArrayList<Layer>();
        for (Layer layer : layers)
        {
          if (layer.getService() == service)
          {
            layersToRemove.add(layer);
          }
        }
        layers.removeAll(layersToRemove);
      }
    }

    @Override
    public void clear()
    {
      layers.clear();
    }
  }

  class LayerList extends ArrayList<Layer>
  {
    @Override
    public boolean add(Layer layer)
    {
      if (layer.getMap() != Map.this)
        throw new RuntimeException("INVALID_LAYER");
      return super.add(layer);
    }

    @Override
    public Layer set(int index, Layer layer)
    {
      if (layer.getMap() != Map.this)
        throw new RuntimeException("INVALID_LAYER");
      return super.set(index, layer);
    }

    @Override
    public boolean addAll(Collection<? extends Layer> c)
    {
      checkCollection(c);
      return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Layer> c)
    {
      checkCollection(c);
      return super.addAll(index, c);
    }

    private void checkCollection(Collection<? extends Layer> c)
    {
      for (Layer layer : c)
      {
        if (layer.getMap() != Map.this)
          throw new RuntimeException("INVALID_LAYER");
      }
    }
  }

  class GroupList extends ArrayList<Group>
  {
    @Override
    public boolean add(Group group)
    {
      if (group.getMap() != Map.this)
        throw new RuntimeException("INVALID_GROUP");
      return super.add(group);
    }

    @Override
    public Group set(int index, Group group)
    {
      if (group.getMap() != Map.this)
        throw new RuntimeException("INVALID_GROUP");
      return super.set(index, group);
    }

    @Override
    public boolean addAll(Collection<? extends Group> c)
    {
      checkCollection(c);
      return super.addAll(c);
    }

    @Override
    public Group remove(int index)
    {
      Group group = super.remove(index);
      updateLayers(group);
      return group;
    }

    @Override
    public boolean remove(Object o)
    {
      updateLayers(o);
      return super.remove(o);
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex)
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends Group> c)
    {
      checkCollection(c);
      return super.addAll(index, c);
    }

    private void checkCollection(Collection<? extends Group> c)
    {
      for (Group group : c)
      {
        if (group.getMap() != Map.this)
          throw new RuntimeException("INVALID_GROUP");
      }
    }

    private void updateLayers(Object o)
    {
      if (o instanceof Group)
      {
        Group group = (Group)o;
        for (Layer layer : layers)
        {
          if (layer.group == group)
          {
            layer.group = null;
          }
        }
      }
    }
  }

  // Service inner class
  public class Service implements Serializable
  {
    private String name;
    private String description;
    private String url;

    public Map getMap()
    {
      return Map.this;
    }

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public String getDescription()
    {
      return description;
    }

    public void setDescription(String description)
    {
      this.description = description;
    }

    public String getUrl()
    {
      return url;
    }

    public void setUrl(String url)
    {
      this.url = url;
    }

    public List<Layer> getLayers()
    {
      return Map.this.getLayers(this, null);
    }

    public void setTo(Service server)
    {
      name = server.name;
      description = server.description;
      url = server.url;
    }

    @Override
    public String toString()
    {
      StringBuilder builder = new StringBuilder();
      builder.append("Service(name: ").append(name).append(", ");
      builder.append("description: ").append(description).append(", ");
      builder.append("url: ").append(url).append(")");
      return builder.toString();
    }
  }

  // Layer inner class
  public class Layer implements Serializable
  {
    private Service service;
    private List<String> names = new ArrayList<String>();
    private String label;
    private boolean baseLayer;
    private boolean visible;
    private boolean locatable;
    private boolean editable;
    private boolean snap;
    private boolean onLegend;
    private boolean independent;
    private String legendGraphic;
    private double opacity = 1.0;
    private boolean transparentBackground = true;
    private String sld;
    private List<String> styles = new ArrayList<String>();
    private String format;
    private String cqlFilter;
    private List<String> viewRoles = new ArrayList<String>();
    private List<String> editRoles = new ArrayList<String>();
    private Group group;
    private int buffer = 16;

    public Map getMap()
    {
      return Map.this;
    }

    public int getId()
    {
      return layers.indexOf(this);
    }

    public void setService(Service service)
    {
      if (services.indexOf(service) == -1)
        throw new RuntimeException("INVALID_SERVICE");
      this.service = service;
    }

    public Service getService()
    {
      return service;
    }

    public List<String> getNames()
    {
      return names;
    }

    public String getNamesString()
    {
      return merge(names);
    }

    public void setNamesString(String value)
    {
      split(value, names);
    }

    public void setLabel(String label)
    {
      this.label = label;
    }

    public String getLabel()
    {
      return label;
    }

    public boolean isBaseLayer()
    {
      return baseLayer;
    }

    public void setBaseLayer(boolean baseLayer)
    {
      this.baseLayer = baseLayer;
    }

    public boolean isVisible()
    {
      return visible;
    }

    public void setVisible(boolean visible)
    {
      this.visible = visible;
    }

    public boolean isLocatable()
    {
      return locatable;
    }

    public void setLocatable(boolean locatable)
    {
      this.locatable = locatable;
    }

    public double getOpacity()
    {
      return opacity;
    }

    public void setOpacity(double opacity)
    {
      if (opacity < 0) opacity = 1;
      else if (opacity > 1) opacity = 1;
      this.opacity = opacity;
    }

    public boolean isTransparentBackground()
    {
      return transparentBackground;
    }

    public void setTransparentBackground(boolean transparentBackground)
    {
      this.transparentBackground = transparentBackground;
    }

    public boolean isEditable()
    {
      return !editRoles.isEmpty();
    }

    public boolean isSnap()
    {
      return snap;
    }

    public void setSnap(boolean snap)
    {
      this.snap = snap;
    }

    public boolean isOnLegend()
    {
      return onLegend;
    }

    public void setOnLegend(boolean onLegend)
    {
      this.onLegend = onLegend;
    }

    public boolean isIndependent()
    {
      return independent;
    }

    public void setIndependent(boolean independent)
    {
      this.independent = independent;
    }

    public String getLegendGraphic()
    {
      return legendGraphic;
    }

    public void setLegendGraphic(String legendGraphic)
    {
      this.legendGraphic = legendGraphic;
    }

    public void setGroup(Group group)
    {
      if (group != null && groups.indexOf(group) == -1)
        throw new RuntimeException("INVALID_GROUP");
      this.group = group;
    }

    public Group getGroup()
    {
      return group;
    }

    public String getFormat()
    {
      return format;
    }

    public void setFormat(String format)
    {
      this.format = format;
    }

    public String getSld()
    {
      return sld;
    }

    public void setSld(String sld)
    {
      this.sld = sld;
    }

    public List<String> getStyles()
    {
      return styles;
    }

    public String getStylesString()
    {
      return merge(styles);
    }

    public void setStylesString(String value)
    {
      split(value, styles);
    }

    public String getCqlFilter()
    {
      return cqlFilter;
    }

    public void setCqlFilter(String cqlFilter)
    {
      this.cqlFilter = cqlFilter;
    }

    public int getBuffer()
    {
      return buffer;
    }

    public void setBuffer(int buffer)
    {
      this.buffer = buffer;
    }

    public List<String> getViewRoles()
    {
      return viewRoles;
    }

    public String getViewRolesString()
    {
      return merge(viewRoles);
    }

    public void setViewRolesString(String value)
    {
      split(value, viewRoles);
    }

    public List<String> getEditRoles()
    {
      return editRoles;
    }

    public String getEditRolesString()
    {
      return merge(editRoles);
    }

    public void setEditRolesString(String value)
    {
      split(value, editRoles);
    }

    public void setTo(Layer layer)
    {
      service = layer.getMap() == Map.this ? layer.service : null;
      names.clear();
      names.addAll(layer.names);
      label = layer.label;
      baseLayer = layer.baseLayer;
      visible = layer.visible;
      locatable = layer.locatable;
      editable = layer.editable;
      snap = layer.snap;
      independent = layer.independent;
      group = layer.getMap() == Map.this ? layer.group : null;
      onLegend = layer.onLegend;
      legendGraphic = layer.legendGraphic;
      cqlFilter = layer.cqlFilter;
      opacity = layer.opacity;
      transparentBackground = layer.transparentBackground;
      format = layer.format;
      buffer = layer.buffer;
      sld = layer.sld;
      styles.clear();
      styles.addAll(layer.styles);
      viewRoles.clear();
      viewRoles.addAll(layer.viewRoles);
      editRoles.clear();
      editRoles.addAll(layer.editRoles);
    }

    @Override
    public String toString()
    {
      StringBuilder builder = new StringBuilder();
      builder.append("Layer(name: ").append(names).append(", ");
      builder.append("label: ").append(label).append(", ");
      builder.append("baseLayer: ").append(baseLayer).append(", ");
      builder.append("visible: ").append(visible).append(", ");
      builder.append("group: ").append(group).append(", ");
      builder.append("locatable: ").append(locatable).append(", ");
      builder.append("onLegend: ").append(onLegend).append(", ");
      builder.append("format: ").append(format).append(", ");
      builder.append("sld: ").append(sld).append(", ");
      builder.append("style: ").append(styles).append(", ");
      builder.append("viewRoles: ").append(viewRoles).append(")");
      builder.append("editRoles: ").append(editRoles).append(")");
      return builder.toString();
    }
  }

  public class InfoLayer implements Serializable
  {
    private String name;
    private String formSelector;
    private boolean highlight;

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public String getFormSelector()
    {
      return formSelector;
    }

    public void setFormSelector(String formSelector)
    {
      this.formSelector = formSelector;
    }

    public boolean isHighlight()
    {
      return highlight;
    }

    public void setHighlight(boolean highlight)
    {
      this.highlight = highlight;
    }

    public void setTo(InfoLayer layer)
    {
      this.name = layer.name;
      this.formSelector = layer.formSelector;
      this.highlight = layer.highlight;
    }
  }

  public class Group implements Serializable
  {
    private String name;
    private String label;

    public Map getMap()
    {
      return Map.this;
    }

    public String getLabel()
    {
      return label;
    }

    public void setLabel(String label)
    {
      this.label = label;
    }

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public List<Layer> getLayers()
    {
      return Map.this.getLayers(this, null);
    }

    public void setTo(Group group)
    {
      name = group.name;
      label = group.label;
    }

    @Override
    public String toString()
    {
      return name;
    }
  }

  private String merge(List<String> list)
  {
    StringBuilder builder = new StringBuilder();
    for (String elem : list)
    {
      if (builder.length() > 0) builder.append(",");
      builder.append(elem);
    }
    return builder.toString();
  }

  private void split(String value, List<String> list)
  {
    list.clear();
    StringTokenizer tokenizer = new StringTokenizer(value, ",");
    while (tokenizer.hasMoreTokens())
    {
      list.add(tokenizer.nextToken().trim());
    }
  }
}
