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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.santfeliu.faces.maplibre.model.Layer;
import org.santfeliu.faces.maplibre.model.Source;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.geo.io.SldStore;
import org.santfeliu.webapp.modules.geo.metadata.Service;
import org.santfeliu.webapp.modules.geo.metadata.ServiceParameters;
import static org.apache.commons.lang.StringUtils.isBlank;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.maplibre.model.SourceType;
import static org.santfeliu.faces.maplibre.model.SourceType.RASTER_DEM;
import org.santfeliu.faces.maplibre.model.Style;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class GeoMapSourcesBean extends WebBean implements Serializable
{
  private int activeSourceTabIndex;
  private ServiceParameters editingServiceParameters;
  private String editingSourceId;
  private Source editingSource;
  private boolean newSource;

  private transient List<String> sourceIds;
  private transient List<String> terrainSourceIds;
  private transient SelectItem[] sourceTypeSelectItems;

  @Inject
  GeoMapBean geoMapBean;

  public String getEditingSourceId()
  {
    return editingSourceId;
  }

  public void setEditingSourceId(String editingSourceId)
  {
    this.editingSourceId = editingSourceId;
  }

  public boolean isNewSource()
  {
    return newSource;
  }

  public int getActiveSourceTabIndex()
  {
    return activeSourceTabIndex;
  }

  public void setActiveSourceTabIndex(int activeSourceTabIndex)
  {
    this.activeSourceTabIndex = activeSourceTabIndex;
  }

  public SelectItem[] getSourceTypeSelectItems()
  {
    if (sourceTypeSelectItems == null)
    {
      sourceTypeSelectItems =
        FacesUtils.getEnumStringSelectItems(SourceType.class, null);
    }
    return sourceTypeSelectItems;
  }

  public void refresh()
  {
    sourceIds = null;
  }

  public List<String> getSourceIds()
  {
    if (sourceIds == null)
    {
      sourceIds = new ArrayList<>(geoMapBean.getStyle().getSources().keySet());
      Collections.sort(sourceIds);
    }
    return sourceIds;
  }

  public List<String> getTerrainSourceIds()
  {
    if (terrainSourceIds == null)
    {
      terrainSourceIds = new ArrayList<>();
      Map<String, Source> sources = geoMapBean.getStyle().getSources();
      for (String sourceId : sources.keySet())
      {
        Source source = sources.get(sourceId);
        if (RASTER_DEM.equals(source.getType()))
        {
          terrainSourceIds.add(sourceId);
        }
      }
      Collections.sort(terrainSourceIds);
    }
    return terrainSourceIds;
  }

  public String getSourceInfo(String sourceId)
  {
    Source source = geoMapBean.getStyle().getSources().get(sourceId);
    ServiceParameters serviceParameters =
      geoMapBean.getServiceParameters(sourceId);

    if (serviceParameters != null && serviceParameters.getService() != null)
    {
      String serviceId = serviceParameters.getService();
      String layers = serviceParameters.getLayers();
      String sldName = serviceParameters.getSldName();
      StringBuilder buffer = new StringBuilder(serviceId);
      if (!isBlank(layers)) buffer.append(" - ").append(layers);
      return buffer.toString();
    }
    else if (!source.getTiles().isEmpty())
    {
      return source.getTiles().toString();
    }
    else if (!isBlank(source.getUrl()))
    {
      return source.getUrl();
    }
    else
    {
      return String.valueOf(source.getData());
    }
  }

  public String getSourceSldName(String sourceId)
  {
    ServiceParameters serviceParameters =
      geoMapBean.getServiceParameters(sourceId);

    if (serviceParameters != null)
    {
      return serviceParameters.getSldName();
    }
    return null;
  }

  public String getSourceDataUrl()
  {
    return editingSource.getData() instanceof String ?
      (String)editingSource.getData() : null;
  }

  public void setSourceDataUrl(String url)
  {
    if (!isBlank(url))
    {
      editingSource.setData(url);
    }
  }

  public String getSourceServiceUrl()
  {
    ServiceParameters serviceParameters =
      geoMapBean.getServiceParameters(editingSourceId);
    if (serviceParameters == null) return null;

    String serviceId = serviceParameters.getService();
    if (isBlank(serviceId)) return null;

    Service service = geoMapBean.getServiceMap().get(serviceId);
    if (service == null) return null;

    return service.getUrl();
  }

  public List<String> completeSourceLayer(String text)
  {
    return geoMapBean.completeLayer(editingServiceParameters, text);
  }

  public List<String> completeSldName(String text)
  {
    try
    {
      SldStore sldStore = geoMapBean.getSldStore();
      return sldStore.findSld(text);
    }
    catch (Exception ex)
    {
      // ignore;
      return Collections.EMPTY_LIST;
    }
  }

  public Source getEditingSource()
  {
    return editingSource;
  }

  public ServiceParameters getEditingServiceParameters()
  {
    return editingServiceParameters;
  }

  public void addSourceTile()
  {
    editingSource.getTiles().add("");
  }

  public void removeSourceTile(int index)
  {
    editingSource.getTiles().remove(index);
  }

  public void editSourceSld()
  {
    try
    {
      geoMapBean.getStyle().getSources().put(editingSourceId, editingSource);
      geoMapBean.setServiceParameters(editingSourceId, editingServiceParameters);
      String sourceId = editingSourceId;

      editingSourceId = null;
      editingSource = null;
      editingServiceParameters = null;
      sourceIds = null;
      geoMapBean.setDialogVisible(false);

      geoMapBean.editSld(sourceId, null, null);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void addSource()
  {
    editingSourceId = null;
    newSource = true;
    editingSource = new Source();
    editingServiceParameters = new ServiceParameters();
    activeSourceTabIndex = 0;
    geoMapBean.setDialogVisible(true);
  }

  public void editSource(String sourceId)
  {
    editingSourceId = sourceId;
    editingSource = geoMapBean.cloneObject(
        geoMapBean.getStyle().getSources().get(sourceId), Source.class);
    editingServiceParameters = geoMapBean.getServiceParameters(sourceId);
    editingServiceParameters = editingServiceParameters == null ?
      new ServiceParameters() :
      geoMapBean.cloneObject(editingServiceParameters, ServiceParameters.class);
    updateSourcePanel();
  }

  public void copySource(String sourceId)
  {
    editingSourceId = null;
    newSource = true;
    editingSource = geoMapBean.cloneObject(
      geoMapBean.getStyle().getSources().get(sourceId), Source.class);
    editingServiceParameters = geoMapBean.getServiceParameters(sourceId);
    editingServiceParameters = editingServiceParameters == null ?
      new ServiceParameters() :
      geoMapBean.cloneObject(editingServiceParameters, ServiceParameters.class);
    updateSourcePanel();
  }

  public void removeSource(String sourceId)
  {
    Style style = geoMapBean.getStyle();
    editingSourceId = null;
    sourceIds = null;
    List<Layer> layers = new ArrayList<>(style.getLayers());
    for (Layer layer : layers)
    {
      if (sourceId.equals(layer.getSource()))
      {
        GeoMapLayersBean geoMapLayersBean =
          CDI.current().select(GeoMapLayersBean.class).get();

        geoMapLayersBean.removeLayer(layer);
      }
    }
    style.getSources().remove(sourceId);
    geoMapBean.getServiceParametersMap().remove(sourceId);
  }

  public void acceptSource()
  {
    Map<String, Source> sources = geoMapBean.getStyle().getSources();
    if (newSource)
    {
      if (sources.containsKey(editingSourceId))
      {
        error("DUPLICATED_ID");
        return;
      }
      else
      {
        newSource = false;
      }
    }
    sources.put(editingSourceId, editingSource);
    geoMapBean.setServiceParameters(editingSourceId, editingServiceParameters);

    editingSourceId = null;
    editingSource = null;
    editingServiceParameters = null;
    sourceIds = null;
    geoMapBean.setDialogVisible(false);
  }

  public void cancelSource()
  {
    editingSourceId = null;
    editingSource = null;
    newSource = false;
    geoMapBean.setDialogVisible(false);
  }

  public void updateSourcePanel()
  {
    if (editingSource != null)
    {
      if ("image".equals(editingSource.getType()))
      {
        activeSourceTabIndex = 4;
        return;
      }

      if ("video".equals(editingSource.getType()))
      {
        activeSourceTabIndex = 5;
        return;
      }

      if ("vector".equals(editingSource.getType()))
      {
        ServiceParameters serviceParameters = getEditingServiceParameters();
        if (serviceParameters != null)
        {
          serviceParameters.setFormat("application/vnd.mapbox-vector-tile");
        }
        if (editingSource.getUrl() != null)
        {
          activeSourceTabIndex = 2;
          return;
        }
      }

      if ("raster".equals(editingSource.getType()) ||
          "raster-dem".equals(editingSource.getType()))
      {
        ServiceParameters serviceParameters = getEditingServiceParameters();
        if (serviceParameters != null)
        {
          String format = serviceParameters.getFormat() ;
          if (isBlank(format) || !format.startsWith("image/"))
          {
            serviceParameters.setFormat("image/png");
          }
        }
      }

      if ("geojson".equals(editingSource.getType()))
      {
        ServiceParameters serviceParameters = getEditingServiceParameters();
        if (serviceParameters != null)
        {
          serviceParameters.setFormat("application/json");
        }

        if (editingSource.getData() != null)
        {
          activeSourceTabIndex = 3;
          return;
        }
      }

      if (editingSource.getTiles() != null)
      {
        if (!editingSource.getTiles().isEmpty())
        {
          activeSourceTabIndex = 1;
          return;
        }
      }
      activeSourceTabIndex = 0;
    }
  }
}
