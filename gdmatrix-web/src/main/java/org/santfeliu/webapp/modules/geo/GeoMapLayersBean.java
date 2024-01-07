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
package org.santfeliu.webapp.modules.geo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.ReorderEvent;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.maplibre.model.Layer;
import org.santfeliu.faces.maplibre.model.LayerType;
import org.santfeliu.faces.maplibre.model.Source;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.geo.metadata.ServiceParameters;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class GeoMapLayersBean extends WebBean implements Serializable
{
  private int activeLayerTabIndex;
  private Layer editingLayer;
  private boolean newLayer;

  private transient List<String> layerIds;
  private transient SelectItem[] layerTypeSelectItems;

  @Inject
  GeoMapBean geoMapBean;

  public int getActiveLayerTabIndex()
  {
    return activeLayerTabIndex;
  }

  public void setActiveLayerTabIndex(int activeLayerTabIndex)
  {
    this.activeLayerTabIndex = activeLayerTabIndex;
  }

  public List<String> getLayerIds()
  {
    if (layerIds == null)
    {
      layerIds = geoMapBean.getStyle().getLayers().stream().map(l -> l.getId()).
        collect(Collectors.toList());
      Collections.sort(layerIds);
    }
    return layerIds;
  }

  public SelectItem[] getLayerTypeSelectItems()
  {
    if (layerTypeSelectItems == null)
    {
      layerTypeSelectItems =
        FacesUtils.getEnumStringSelectItems(LayerType.class, null);
    }
    return layerTypeSelectItems;
  }

  public Layer getEditingLayer()
  {
    return editingLayer;
  }

  public boolean isNewLayer()
  {
    return newLayer;
  }

  public String getEditingLayerSldName()
  {
    String sourceId = editingLayer.getSource();
    if (sourceId == null) return null;
    ServiceParameters serviceParameters =
      geoMapBean.getServiceParameters(sourceId);
    if (serviceParameters == null) return null;

    return serviceParameters.getSldName();
  }

  public void toggleLayerVisibility(Layer layer)
  {
    layer.setVisible(!layer.isVisible());
  }

  public boolean isLayerVisible(Layer layer)
  {
    return layer.isVisible();
  }

  public void toggleLayerLocatability(Layer layer)
  {
    layer.setLocatable(!layer.isLocatable());
  }

  public boolean isLayerLocatable(Layer layer)
  {
    return layer.isLocatable();
  }

  public void toggleLayerHighlight(Layer layer)
  {
    layer.setHighlightEnabled(!layer.isHighlightEnabled());
  }

  public boolean isLayerHighlightEnabled(Layer layer)
  {
    return layer.isHighlightEnabled();
  }

  public String getJsonPaint()
  {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    return gson.toJson(editingLayer.getPaint());
  }

  public void setJsonPaint(String json)
  {
    Gson gson = new Gson();
    editingLayer.setPaint(gson.fromJson(json, Map.class));
  }

  public String getJsonLayout()
  {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    return gson.toJson(editingLayer.getLayout());
  }

  public void setJsonLayout(String json)
  {
    Gson gson = new Gson();
    editingLayer.setLayout(gson.fromJson(json, Map.class));
  }

  public String getJsonFilter()
  {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    return gson.toJson(editingLayer.getFilter());
  }

  public void setJsonFilter(String json)
  {
    Gson gson = new Gson();
    editingLayer.setFilter(gson.fromJson(json, Object.class));
  }

  public void onLayerReorder(ReorderEvent event)
  {
  }

  public List<String> completeLayerLayer(String text)
  {
    String sourceId = editingLayer.getSource();
    ServiceParameters serviceParameters =
      geoMapBean.getServiceParameters(sourceId);
    if (serviceParameters == null) return Collections.EMPTY_LIST;
    return geoMapBean.completeLayer(serviceParameters, text);
  }

  public void editLayerSld()
  {
    try
    {
      String sourceId = editingLayer.getSource();
      geoMapBean.editSld(sourceId, editingLayer.getLayers(),
        editingLayer.getStyles());
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void updateLayerPanel()
  {
    if (editingLayer != null)
    {
      String sourceId = editingLayer.getSource();
      Source source = geoMapBean.getStyle().getSources().get(sourceId);
      if (source != null)
      {
        String sourceType = source.getType();
        if ("raster".equals(sourceType))
        {
          editingLayer.setType("raster");
        }
        else if ("vector".equals(sourceType) || "geojson".equals(sourceType))
        {
          String layerType = editingLayer.getType();
          if ("raster".equals(layerType))
          {
            editingLayer.setType(null);
          }
        }
      }
      activeLayerTabIndex = 0;
    }
  }

  public void addLayer()
  {
    editingLayer = new Layer();
    newLayer = true;
    geoMapBean.setDialogVisible(true);
  }

  public void editLayer(Layer layer)
  {
    editingLayer = geoMapBean.cloneObject(layer, Layer.class);
    updateLayerPanel();
    geoMapBean.setDialogVisible(true);
  }

  public void copyLayer(Layer layer)
  {
    editingLayer = geoMapBean.cloneObject(layer, Layer.class);
    editingLayer.setId(null);
    newLayer = true;
    updateLayerPanel();
    geoMapBean.setDialogVisible(true);
  }

  public void removeLayer(Layer layer)
  {
    GeoMapLegendBean geoMapLegendBean =
      CDI.current().select(GeoMapLegendBean.class).get();

    geoMapLegendBean.removeLegendLayerNode(layer.getId());

    geoMapBean.getStyle().getLayers().remove(layer);
  }

  public void acceptLayer()
  {
    List<Layer> layers = geoMapBean.getStyle().getLayers();
    if (newLayer)
    {
      String layerId = editingLayer.getId();
      if (layers.stream().anyMatch(l -> l.getId().equals(layerId)))
      {
        error("DUPLICATED_ID");
        return;
      }
      else
      {
        newLayer = false;
        layers.add(editingLayer);
      }
    }
    else
    {
      for (int i = 0; i < layers.size(); i++)
      {
        if (layers.get(i).getId().equals(editingLayer.getId()))
        {
          layers.set(i, editingLayer);
          break;
        }
      }
    }
    editingLayer = null;
    geoMapBean.setDialogVisible(false);
  }

  public void cancelLayer()
  {
    editingLayer = null;
    newLayer = false;
    geoMapBean.setDialogVisible(false);
  }
}
