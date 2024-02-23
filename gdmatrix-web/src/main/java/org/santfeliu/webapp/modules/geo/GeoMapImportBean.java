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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.santfeliu.faces.maplibre.model.Layer;
import org.santfeliu.faces.maplibre.model.Source;
import org.santfeliu.faces.maplibre.model.Style;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.geo.io.MapStore;
import org.santfeliu.webapp.modules.geo.io.MapStore.MapFilter;
import org.santfeliu.webapp.modules.geo.io.MapStore.MapGroup;
import org.santfeliu.webapp.modules.geo.io.MapStore.MapView;
import org.santfeliu.webapp.modules.geo.metadata.LegendGroup;
import org.santfeliu.webapp.modules.geo.metadata.Service;
import org.santfeliu.webapp.modules.geo.metadata.ServiceParameters;
import org.santfeliu.webapp.modules.geo.metadata.StyleMetadata;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class GeoMapImportBean extends WebBean implements Serializable
{
  String mapName;
  boolean dialogVisible;
  int dataToImport;
  String importPosition;

  @Inject
  GeoMapBean geoMapBean;

  public String getMapName()
  {
    return mapName;
  }

  public void setMapName(String mapName)
  {
    this.mapName = mapName;
  }

  public boolean isDialogVisible()
  {
    return dialogVisible;
  }

  public void setDialogVisible(boolean dialogVisible)
  {
    geoMapBean.setDialogVisible(true);
    this.dialogVisible = dialogVisible;
  }

  public int getDataToImport()
  {
    return dataToImport;
  }

  public void setDataToImport(int dataToImport)
  {
    this.dataToImport = dataToImport;
  }

  public String getImportPosition()
  {
    return importPosition;
  }

  public void setImportPosition(String importPosition)
  {
    this.importPosition = importPosition;
  }

  public List<MapView> findMapViews(String name)
  {
    MapStore mapStore = geoMapBean.getMapStore();
    List<MapView> mapViews = new ArrayList<>();
    try
    {
      MapFilter mapFilter = new MapFilter();
      mapFilter.setKeywords(name);
      MapStore.MapGroup mapGroup = mapStore.findMaps(mapFilter);
      explodeMapViews(mapGroup, mapViews);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return mapViews;
  }

  public void acceptImport()
  {
    try
    {
      MapStore mapStore = geoMapBean.getMapStore();
      Style style = geoMapBean.getStyle();

      MapStore.MapDocument mapDocument = mapStore.loadMap(mapName);
      Style impStyle = mapDocument.getStyle();

      StyleMetadata styleMetadata = new StyleMetadata(style);
      StyleMetadata impStyleMetadata = new StyleMetadata(impStyle);

      importServices(styleMetadata, impStyleMetadata);

      if (dataToImport >= 2)
      {
        importSources(styleMetadata, impStyleMetadata);

        if (dataToImport >= 3)
        {
          importLayers(styleMetadata, impStyleMetadata);
        }
      }

      geoMapBean.setDialogVisible(false);
      geoMapBean.refresh();

      dialogVisible = false;

      growl("MAP_IMPORT_COMPLETED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
    mapName = null;
  }

  public void cancelImport()
  {
    geoMapBean.setDialogVisible(false);
    dialogVisible = false;
  }

  private void explodeMapViews(MapGroup mapGroup, List<MapView> mapViews)
  {
    mapViews.addAll(mapGroup.getMapViews());

    for (MapGroup subGroup : mapGroup.getMapGroups())
    {
      explodeMapViews(subGroup, mapViews);
    }
  }

  private void importServices(
    StyleMetadata styleMetadata,
    StyleMetadata impStyleMetadata)
  {
    Map<String, Service> serviceMap = styleMetadata.getServiceMap(true);
    Map<String, Service> impServiceMap = impStyleMetadata.getServiceMap(true);

    serviceMap.putAll(impServiceMap);
  }

  private void importSources(
    StyleMetadata styleMetadata,
    StyleMetadata impStyleMetadata)
  {
    Map<String, Source> sources = styleMetadata.getStyle().getSources();
    Map<String, Source> impSources = impStyleMetadata.getStyle().getSources();

    Map<String, ServiceParameters> serviceParametersMap =
      styleMetadata.getServiceParametersMap(true);
    Map<String, ServiceParameters> impServiceParametersMap =
      impStyleMetadata.getServiceParametersMap(true);

    sources.putAll(impSources);
    serviceParametersMap.putAll(impServiceParametersMap);
  }

  private void importLayers(
    StyleMetadata styleMetadata,
    StyleMetadata impStyleMetadata)
  {
    List<Layer> layers = styleMetadata.getStyle().getLayers();
    List<Layer> impLayers = impStyleMetadata.getStyle().getLayers();

    int position = 0;

    for (Layer impLayer : impLayers)
    {
      String impLayerId = impLayer.getId();
      if (indexOfLayer(layers, impLayerId) == -1)
      {
        if ("top".equals(importPosition))
        {
          if (position >= layers.size())
          {
            layers.add(impLayer);
          }
          else
          {
            layers.add(position, impLayer);
          }
          position++;
        }
        else // bottom
        {
          layers.add(impLayer);
        }
      }
    }
  }

  private void importLegend(
    StyleMetadata styleMetadata,
    StyleMetadata impStyleMetadata)
  {
    LegendGroup legendGroup = styleMetadata.getLegend(true);
    LegendGroup impLegendGroup = impStyleMetadata.getLegend(true);

  }

  private int indexOfLayer(List<Layer> layers, String layerId)
  {
    for (int index = 0; index < layers.size(); index++)
    {
      if (layers.get(index).getId().equals(layerId))
      {
        return index;
      }
    }
    return -1;
  }
}
