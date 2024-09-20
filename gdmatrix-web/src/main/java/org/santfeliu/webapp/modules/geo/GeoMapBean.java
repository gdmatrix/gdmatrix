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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import org.santfeliu.faces.maplibre.model.Style;
import org.santfeliu.webapp.modules.geo.metadata.Service;
import org.santfeliu.webapp.modules.geo.metadata.ServiceParameters;
import org.santfeliu.faces.maplibre.model.Source;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.geo.io.MapStore;
import org.santfeliu.webapp.modules.geo.io.MapDocument;
import org.santfeliu.webapp.modules.geo.io.SldStore;
import org.santfeliu.webapp.modules.geo.io.SvgStore;
import org.santfeliu.webapp.modules.geo.ogc.ServiceCapabilities;
import org.santfeliu.faces.maplibre.encoder.StyleEncoder;
import org.santfeliu.faces.maplibre.encoder.TranslateStyleEncoder;
import static org.santfeliu.webapp.modules.geo.io.MapStore.GEO_ADMIN_ROLE;
import static org.apache.commons.lang.StringUtils.isBlank;
import org.santfeliu.faces.maplibre.model.Layer;
import org.santfeliu.faces.maplibre.model.Light;
import org.santfeliu.faces.maplibre.model.Sky;
import org.santfeliu.faces.maplibre.model.Terrain;
import org.santfeliu.webapp.modules.geo.io.MapAccessLogger;
import org.santfeliu.webapp.modules.geo.io.MapAccessLogger.Access;
import org.santfeliu.webapp.modules.geo.metadata.LegendGroup;
import org.santfeliu.webapp.modules.geo.metadata.LegendItem;
import org.santfeliu.webapp.modules.geo.metadata.LegendLayer;
import org.santfeliu.webapp.modules.geo.metadata.StyleMetadata;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class GeoMapBean extends WebBean implements Serializable
{
  public static final String MAP_NAME_PROPERTY = "map_name";

  private MapDocument mapDocument;
  private String view = "catalogue"; // or "map_viewer", "map_editor", "sld_editor"
  private String mode = "visual"; // or "code"
  private String metadataFormSelector;
  private boolean mapNameChanged;
  private int activeTabIndex;
  private boolean dialogVisible;

  private transient StyleEncoder encoder;

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

  public void refresh()
  {
    GeoMapServicesBean geoMapServicesBean =
      CDI.current().select(GeoMapServicesBean.class).get();
    geoMapServicesBean.refresh();

    GeoMapSourcesBean geoMapSourcesBean =
      CDI.current().select(GeoMapSourcesBean.class).get();
    geoMapSourcesBean.refresh();

    GeoMapLegendBean geoMapLegendBean =
      CDI.current().select(GeoMapLegendBean.class).get();
    geoMapLegendBean.refresh();
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

  /**
   * Returns the style filtered by user roles
   *
   * @return Style
   */
  public Style getUserStyle()
  {
    try
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      Style style = mapDocument.getStyle();
      List<Layer> layers = style.getLayers();
      HashSet<String> removedLayerIds = new HashSet<>();
      for (Layer layer : layers)
      {
        String roleId = layer.getRoleId();
        if (roleId != null && !userSessionBean.isUserInRole(roleId))
        {
          removedLayerIds.add(layer.getId());
        }
      }
      if (removedLayerIds.isEmpty()) return style;

      Style userStyle = new Style();
      userStyle.fromString(style.toString()); // clone style
      layers = userStyle.getLayers();

      for (int i = layers.size() - 1; i >= 0; i--)
      {
        Layer layer = layers.get(i);
        if (removedLayerIds.contains(layer.getId()))
        {
          layers.remove(i);
        }
      }

      StyleMetadata styleMetadata = new StyleMetadata(userStyle);
      LegendGroup legend = styleMetadata.getLegend(false);
      if (legend != null)
      {
        removeLegendLayers(legend, removedLayerIds);
      }
      return userStyle;
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  public StyleEncoder getEncoder()
  {
    if (encoder == null)
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      String language = userSessionBean.getViewLanguage();
      encoder = new TranslateStyleEncoder(language, mapDocument.getName());
    }
    return encoder;
  }

  public MapDocument getMapDocument()
  {
    return mapDocument;
  }

  public boolean isEditorUser()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return userSessionBean.isUserInRole(GEO_ADMIN_ROLE) ||
      userSessionBean.isUserInRole(mapDocument.getWriteRoles());
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

  // terrain

  public void setTerrainEnabled(boolean enabled)
  {
    Style style = getStyle();
    if (enabled)
    {
      if (style.getTerrain() == null)
      {
        style.setTerrain(new Terrain());
      }
    }
    else
    {
      style.setTerrain(null);
    }
  }

  public boolean isTerrainEnabled()
  {
    return getStyle().getTerrain() != null;
  }

  public void onEnableTerrain()
  {
    setTerrainEnabled(isTerrainEnabled());
  }

  // light

  public void setLightEnabled(boolean enabled)
  {
    Style style = getStyle();
    if (enabled)
    {
      if (style.getLight() == null)
      {
        style.setLight(new Light());
      }
    }
    else
    {
      style.setLight(null);
    }
  }

  public boolean isLightEnabled()
  {
    return getStyle().getLight() != null;
  }

  public void onEnableLight()
  {
    setLightEnabled(isLightEnabled());
  }

  public String getLightColor()
  {
    Light light = getStyle().getLight();
    return light == null ? null : light.getColor();

  }

  public void setLightColor(String color)
  {
    Light light = getStyle().getLight();
    if (light != null)
    {
      light.setColor(color);
    }
  }

  // sky

  public void setSkyEnabled(boolean enabled)
  {
    Style style = getStyle();
    if (enabled)
    {
      if (style.getSky() == null)
      {
        style.setSky(new Sky());
      }
    }
    else
    {
      style.setSky(null);
    }
  }

  public boolean isSkyEnabled()
  {
    return getStyle().getSky() != null;
  }

  public void onEnableSky()
  {
    setSkyEnabled(isSkyEnabled());
  }

  public String getSkyColor()
  {
    Sky sky = getStyle().getSky();
    return sky == null ? null : sky.getSkyColor();
  }

  public void setSkyColor(String color)
  {
    Sky sky = getStyle().getSky();
    if (sky != null)
    {
      sky.setSkyColor(color);
    }
  }

  public String getHorizonColor()
  {
    Sky sky = getStyle().getSky();
    return sky == null ? null : sky.getHorizonColor();
  }

  public void setHorizonColor(String color)
  {
    Sky sky = getStyle().getSky();
    if (sky != null)
    {
      sky.setHorizonColor(color);
    }
  }

  public String getFogColor()
  {
    Sky sky = getStyle().getSky();
    return sky == null ? null : sky.getFogColor();
  }

  public void setFogColor(String color)
  {
    Sky sky = getStyle().getSky();
    if (sky != null)
    {
      sky.setFogColor(color);
    }
  }


  // services

  public Map<String, Service> getServiceMap()
  {
    return GeoMapBean.this.getServiceMap(true);
  }

  public Map<String, Service> getServiceMap(boolean create)
  {
    StyleMetadata styleMetadata = new StyleMetadata(getStyle());
    return styleMetadata.getServiceMap(create);
  }

  // service parameters

  public Map<String, ServiceParameters> getServiceParametersMap()
  {
    StyleMetadata styleMetadata = new StyleMetadata(getStyle());
    return styleMetadata.getServiceParametersMap(true);
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
        refresh();
        MapAccessLogger.registerAccess(mapName, getExternalContext());
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
        refresh();
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
    loadFromParameters();

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
      refresh();
    }
    catch (IOException ex)
    {
      error(ex);
    }
  }

  public boolean isCatalogueEnabled()
  {
    return getProperty(MAP_NAME_PROPERTY) == null;
  }

  public List<Access> getStatistics()
  {
    if (mapDocument != null)
    {
      return MapAccessLogger.getStatistics(mapDocument.getName());
    }
    return null;
  }

  public void updateStatistics()
  {
    MapAccessLogger.clearStatistics();
  }

  // non public methods

  void removeLegendLayers(LegendGroup group, HashSet<String> removedLayerIds)
  {
    List<LegendItem> items = group.getChildren();
    for (int i = items.size() - 1; i >= 0; i--)
    {
      LegendItem item = items.get(i);
      if (item instanceof LegendLayer)
      {
        LegendLayer layer = (LegendLayer)item;
        if (removedLayerIds.contains(layer.getLayerId()))
        {
          items.remove(i);
        }
      }
      else if (item instanceof LegendGroup)
      {
        LegendGroup subGroup = (LegendGroup)item;
        removeLegendLayers(subGroup, removedLayerIds);
      }
    }
  }

  void loadFromParameters()
  {
    ExternalContext extContext = getExternalContext();
    Map<String, String> parameters = extContext.getRequestParameterMap();

    String mapName = getProperty(MAP_NAME_PROPERTY);
    if (mapName == null)
    {
      if (!FacesContext.getCurrentInstance().isPostback()) // GET
      {
        mapName = (String)parameters.get(MAP_NAME_PROPERTY);
      }
    }

    if (mapName != null)
    {
      try
      {
        mapDocument = getMapStore().loadMap(mapName);
        if (mapDocument == null)
        {
          error("MAP_NOT_FOUND");
          setView("catalogue");
        }
        else
        {
          refresh();
          MapAccessLogger.registerAccess(mapName, extContext);
          setView("map_viewer");
        }
      }
      catch (Exception ex)
      {
        processError(ex, parameters);
      }
    }
    else
    {
      setView("catalogue");
    }
  }

  void processError(Exception ex, Map<String, String> parameters)
  {
    ExternalContext extContext = getExternalContext();

    String message = ex.getMessage();
    if (message != null && message.contains("ACTION_DENIED"))
    {
      try
      {
        Map<String, List<String>> params = new HashMap<>();
        if (FacesContext.getCurrentInstance().isPostback()) // POST
        {
          List<String> values = new ArrayList<>();
          values.add(UserSessionBean.getCurrentInstance().getSelectedMid());
          params.put("xmid", values);
        }
        else // GET
        {
          for (String name : parameters.keySet())
          {
            String value = parameters.get(name);
            params.put(name, Collections.singletonList(value));
          }
        }
        String url = extContext.encodeRedirectURL("/login.faces", params);
        extContext.redirect(url);
        getFacesContext().responseComplete();
      }
      catch (Exception ioex)
      {
        // ignore
      }
    }
    else
    {
      error(ex);
      setView("catalogue");
    }
  }

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

      Service service = getServiceMap().get(serviceId);
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
      Service service = getServiceMap().get(serviceId);
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
        if (isBlank(sldName))
        {
          serviceParameters.setSldUrl(null);
        }
        else
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
}
