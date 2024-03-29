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
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.santfeliu.faces.maplibre.model.Style;
import org.santfeliu.webapp.modules.geo.metadata.Service;
import org.santfeliu.webapp.modules.geo.metadata.ServiceParameters;
import org.santfeliu.faces.maplibre.model.Source;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.geo.io.MapStore;
import org.santfeliu.webapp.modules.geo.io.MapStore.MapDocument;
import org.santfeliu.webapp.modules.geo.io.SldStore;
import org.santfeliu.webapp.modules.geo.io.SvgStore;
import org.santfeliu.webapp.modules.geo.metadata.LayerForm;
import org.santfeliu.webapp.modules.geo.metadata.PrintReport;
import org.santfeliu.webapp.modules.geo.ogc.ServiceCapabilities;
import org.santfeliu.webapp.modules.geo.io.MapStore.MapCategory;
import org.santfeliu.webapp.modules.geo.metadata.LegendGroup;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class GeoMapBean extends WebBean implements Serializable
{
  private MapDocument mapDocument;
  private String view = "catalogue"; // or "map_viewer", "map_editor", "sld_editor"
  private String mode = "visual"; // or "code"
  private String metadataFormSelector;
  private boolean mapNameChanged;
  private int activeTabIndex;
  private boolean dialogVisible;

  @PostConstruct
  public void init()
  {
    mapDocument = new MapDocument();
  }

  public void setViewAndMode(String view, String mode)
  {
    setView(view);
    setMode(mode);
  }

  public String getView()
  {
    return view;
  }

  public void setView(String view)
  {
    if ("map_viewer".equals(view))
    {
      getStyle().cleanUp();
      updateSldUrls();
    }
    this.view = view;
  }

  public String getMode()
  {
    return mode;
  }

  public void setMode(String mode)
  {
    this.mode = mode;
  }

  public String getContent()
  {
    String content = "/pages/geo/" + view;

    if (view.endsWith("editor")) content += "_" + mode;

    return content + ".xhtml";
  }

  public Style getStyle()
  {
    return mapDocument.getStyle();
  }

  public MapDocument getMapDocument()
  {
    return mapDocument;
  }

  public MapCategory getCategory(String categoryName)
  {
    return getMapStore().getCategory(categoryName);
  }

  public List<SelectItem> getCategorySelectItems()
  {
    List<SelectItem> selectItems = new ArrayList<>();

    for (MapCategory category : getMapStore().getCategoryList())
    {
      SelectItem selectItem =
        new SelectItem(category.getName(), category.getTitle());
      selectItems.add(selectItem);
    }
    return selectItems;
  }

  public String getMetadataFormSelector()
  {
    return metadataFormSelector;
  }

  public void setMetadataFormSelector(String metadataFormSelector)
  {
    this.metadataFormSelector = metadataFormSelector;
  }

  public int getActiveTabIndex()
  {
    return activeTabIndex;
  }

  public void setActiveTabIndex(int activeTabIndex)
  {
    this.activeTabIndex = activeTabIndex;
  }

  public boolean isDialogVisible()
  {
    return dialogVisible;
  }

  public void setDialogVisible(boolean visible)
  {
    this.dialogVisible = visible;
  }

  public void onMapNameChanged(AjaxBehaviorEvent event)
  {
    this.mapNameChanged = true;
  }

  // services

  public Map<String, Service> getServices()
  {
    return getServices(true);
  }

  public Map<String, Service> getServices(boolean neverNull)
  {
    Map<String, Object> metadata = getStyle().getMetadata();

    Map<String, Service> services =
      (Map<String, Service>)metadata.get("services");
    if (services == null && neverNull)
    {
      services = new HashMap<>();
      metadata.put("services", services);
    }
    return services;
  }

  // service parameters

  public Map<String, ServiceParameters> getServiceParametersMap()
  {
    Map<String, Object> metadata = getStyle().getMetadata();

    Map<String, ServiceParameters> map =
      (Map<String, ServiceParameters>)metadata.get("serviceParameters");
    if (map == null)
    {
      map = new HashMap<>();
      metadata.put("serviceParameters", map);
    }
    return map;
  }

  public ServiceParameters getServiceParameters(String sourceId)
  {
    Map<String, ServiceParameters> map = getServiceParametersMap();
    return map.get(sourceId);
  }

  public void setServiceParameters(String sourceIds,
    ServiceParameters serviceParameters)
  {
    Map<String, ServiceParameters> map = getServiceParametersMap();
    map.put(sourceIds, serviceParameters);
  }

  // MapOptions

  public void setMaxZoom(Double zoom)
  {
    getStyle().getMetadata().put("maxZoom", zoom);
  }

  public Double getMaxZoom()
  {
    Object value = getStyle().getMetadata().get("maxZoom");
    return value instanceof Number ? ((Number)value).doubleValue() : null;
  }

  public void setMinZoom(Double zoom)
  {
    getStyle().getMetadata().put("minZoom", zoom);
  }

  public Double getMinZoom()
  {
    Object value = getStyle().getMetadata().get("minZoom");
    return value instanceof Number ? ((Number)value).doubleValue() : null;
  }

  public void setMaxPitch(Double pitch)
  {
    getStyle().getMetadata().put("maxPitch", pitch);
  }

  public Double getMaxPitch()
  {
    Object value = getStyle().getMetadata().get("maxPitch");
    return value instanceof Number ? ((Number)value).doubleValue() : null;
  }

  public void setMinPitch(Double pitch)
  {
    getStyle().getMetadata().put("minPitch", pitch);
  }

  public Double getMinPitch()
  {
    Object value = getStyle().getMetadata().get("minPitch");
    return value instanceof Number ? ((Number)value).doubleValue() : null;
  }

  public void setHash(boolean hash)
  {
    getStyle().getMetadata().put("hash", hash);
  }

  public boolean isHash()
  {
    Object value = getStyle().getMetadata().get("hash");
    return value instanceof Boolean ? (Boolean)value : false;
  }

  // Map -------------------------------------------------------

  public void newMap()
  {
    mapDocument = new MapDocument();
    activeTabIndex = 0;
  }

  public void loadMap(String mapName)
  {
    loadMap(mapName, null);
  }

  public void loadMap(String mapName, String view)
  {
    try
    {
      mapDocument = getMapStore().loadMap(mapName);
      if (mapDocument == null)
      {
        newMap();
        error("MAP_NOT_FOUND");
      }
      else if (view != null)
      {
        convertMetadata();
        setView(view);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void reloadMap()
  {
    try
    {
      MapDocument mapReloaded = getMapStore().loadMap(getMapDocument().getName());
      if (mapReloaded == null)
      {
        error("MAP_NOT_FOUND");
      }
      else
      {
        mapDocument = mapReloaded;
        convertMetadata();
        growl("MAP_RELOADED");
        mapNameChanged = false;
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void saveMap()
  {
    try
    {
      if (getMapStore().storeMap(mapDocument, mapNameChanged))
      {
        growl("MAP_SAVED");
      }
      else
      {
        warn("A map with that name already exists. Save again to replace it.");
      }
      mapNameChanged = false;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void removeMap()
  {
    String mapName = getMapDocument().getName();

    getMapStore().removeMap(mapName);

    growl("MAP_REMOVED");
    mapDocument = new MapDocument();
  }

  public String show()
  {
    String template = UserSessionBean.getCurrentInstance().getTemplate();
    return "/templates/" + template + "/template.xhtml";
  }

  public String getJsonMap()
  {
    return getStyle().toString();
  }

  public void setJsonMap(String json)
  {
    try
    {
      getStyle().fromString(json);
      convertMetadata();
    }
    catch (IOException ex)
    {
      error(ex);
    }
  }

  // non public methods

  void editSld(String sourceId, String layers, String styles)
  {
    try
    {
      ServiceParameters serviceParameters = getServiceParameters(sourceId);
      if (serviceParameters == null) return;

      String serviceId = serviceParameters.getService();
      if (serviceId == null) return;

      String sldName = serviceParameters.getSldName();
      System.out.println("sldName:" + sldName);

      Service service = getServices().get(serviceId);
      String serviceUrl = service == null ? null : service.getUrl();

      List<String> layerList = new ArrayList<>();
      List<String> styleList = new ArrayList<>();

      if (!isBlank(serviceParameters.getLayers()))
      {
        layerList.addAll(Arrays.asList(serviceParameters.getLayers().split(",")));
      }
      if (!isBlank(layers))
      {
        layerList.addAll(Arrays.asList(layers.split(",")));
      }
      if (!isBlank(serviceParameters.getStyles()))
      {
        styleList.addAll(Arrays.asList(serviceParameters.getStyles().split(",")));
      }
      if (!isBlank(styles))
      {
        styleList.addAll(Arrays.asList(styles.split(",")));
      }

      System.out.println("service + sld: " + serviceUrl + " " + sldName);
      if (serviceUrl != null && !isBlank(sldName))
      {
        GeoSldBean geoSldBean = CDI.current().select(GeoSldBean.class).get();
        geoSldBean.editSld(sldName, layerList, styleList, serviceUrl);

        this.view = "sld_editor";
        this.mode = "visual";
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  List<String> completeLayer(ServiceParameters serviceParameters, String text)
  {
    try
    {
      int index = text.lastIndexOf(",");
      if (index != -1) text = text.substring(index + 1);
      text = text.toUpperCase();

      String serviceId = serviceParameters.getService();
      Service service = getServices().get(serviceId);
      String serviceUrl = service.getUrl();

      GeoServiceBean geoServiceBean =
        CDI.current().select(GeoServiceBean.class).get();

      ServiceCapabilities capabilities =
        geoServiceBean.getServiceCapabilities(serviceUrl, false);

      List<ServiceCapabilities.Layer> layers = capabilities.getLayers();
      List<String> layerNames = new ArrayList<>();
      for (ServiceCapabilities.Layer layer : layers)
      {
        if (layer.getName().toUpperCase().contains(text))
        {
          layerNames.add(layer.getName());
        }
      }
      return layerNames;
    }
    catch (Exception ex)
    {
      return Collections.EMPTY_LIST;
    }
  }

  void updateSldUrls()
  {
    SldStore sldStore = getSldStore();
    Map<String, Source> sourceMap = getStyle().getSources();
    for (String sourceId : sourceMap.keySet())
    {
      ServiceParameters serviceParameters = getServiceParameters(sourceId);
      if (serviceParameters != null)
      {
        String sldName = serviceParameters.getSldName();
        if (!isBlank(sldName))
        {
          String sldUrl = sldStore.getSldUrl(sldName);
          serviceParameters.setSldUrl(sldUrl);
        }
      }
    }
  }

  <T> T cloneObject(T object, Class<T> type)
  {
    Gson gson = new Gson();
    String json = gson.toJson(object);
    return gson.fromJson(json, type);
  }

  MapStore getMapStore()
  {
    MapStore mapStore = CDI.current().select(MapStore.class).get();
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    mapStore.setCredentials(userSessionBean.getUserId(), userSessionBean.getPassword());
    return mapStore;
  }

  SldStore getSldStore()
  {
    SldStore sldStore = CDI.current().select(SldStore.class).get();
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    sldStore.setCredentials(userSessionBean.getUserId(), userSessionBean.getPassword());
    return sldStore;
  }

  SvgStore getSvgStore()
  {
    SvgStore svgStore = CDI.current().select(SvgStore.class).get();
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    svgStore.setCredentials(userSessionBean.getUserId(), userSessionBean.getPassword());
    return svgStore;
  }

  private void convertMetadata()
  {
    Map<String, Object> services =
      (Map<String, Object>)getStyle().getMetadata().get("services");
    if (services != null)
    {
      for (String serviceId : services.keySet())
      {
        services.put(serviceId, new Service((Map)services.get(serviceId)));
      }
    }

    Map<String, Object> params =
      (Map<String, Object>)getStyle().getMetadata().get("serviceParameters");
    if (params != null)
    {
      for (String sourceId : params.keySet())
      {
        params.put(sourceId, new ServiceParameters((Map)params.get(sourceId)));
      }
    }

    List list = (List)getStyle().getMetadata().get("layerForms");
    if (list != null)
    {
      for (int i = 0; i < list.size(); i++)
      {
        list.set(i, new LayerForm((Map)list.get(i)));
      }
    }

    list = (List)getStyle().getMetadata().get("printReports");
    if (list != null)
    {
      for (int i = 0; i < list.size(); i++)
      {
        list.set(i, new PrintReport((Map)list.get(i)));
      }
    }

    Map legendProperties = (Map)getStyle().getMetadata().get("legend");
    if (legendProperties != null &&
        "group".equals(legendProperties.get("type")))
    {
      getStyle().getMetadata().put("legend", new LegendGroup(legendProperties));
    }
    GeoMapLegendBean geoMapLegendBean =
      CDI.current().select(GeoMapLegendBean.class).get();

    geoMapLegendBean.updateLegendTreeRoot();
  }
}
