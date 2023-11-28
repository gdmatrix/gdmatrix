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
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import static org.matrix.dic.DictionaryConstants.READ_ACTION;
import org.matrix.dic.Property;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.DocumentManagerPort;
import org.matrix.security.AccessControl;
import org.matrix.security.Role;
import org.matrix.security.RoleFilter;
import org.primefaces.event.ReorderEvent;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.faces.maplibre.model.Layer;
import org.santfeliu.faces.maplibre.model.Map;
import org.santfeliu.faces.maplibre.model.Service;
import org.santfeliu.faces.maplibre.model.ServiceParameters;
import org.santfeliu.faces.maplibre.model.Source;
import org.santfeliu.security.web.SecurityConfigBean;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.doc.DocModuleBean;
import org.santfeliu.webapp.modules.geo.io.MapStore;
import org.santfeliu.webapp.modules.geo.io.MapStore.MapDocument;
import org.santfeliu.webapp.modules.geo.io.SldStore;
import org.santfeliu.webapp.modules.geo.io.SvgStore;
import org.santfeliu.webapp.modules.geo.metadata.LayerForm;
import org.santfeliu.webapp.modules.geo.metadata.PrintReport;
import org.santfeliu.webapp.modules.geo.ogc.ServiceCapabilities;

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
  private int activeSourceTabIndex;
  private transient List<String> serviceNames;
  private transient List<String> sourceNames;
  private String editingName;
  private Source editingSource;
  private Service editingService;
  private Layer editingLayer;
  private LayerForm editingLayerForm;
  private PrintReport editingPrintReport;
  private String roleToAdd;
  private transient ServiceCapabilities serviceCapabilities;
  private transient String capabilitiesServiceName;

  @Inject
  GeoServiceBean geoServiceBean;

  @PostConstruct
  public void init()
  {
    mapDocument = new MapDocument();
  }

  public void setViewAndMode(String view, String mode)
  {
    this.setView(view);
    this.setMode(mode);
  }

  public String getView()
  {
    return view;
  }

  public void setView(String view)
  {
    if ("map_viewer".equals(view)) updateSldUrls();
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

  public Map getMap()
  {
    return mapDocument.getMap();
  }

  public MapDocument getMapDocument()
  {
    return mapDocument;
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

  public String getEditingName()
  {
    return editingName;
  }

  public void setEditingName(String editingName)
  {
    this.editingName = editingName;
  }

  public String getRoleToAdd()
  {
    return roleToAdd;
  }

  public void setRoleToAdd(String roleToAdd)
  {
    this.roleToAdd = roleToAdd;
  }

  public void updateAccessControl(AccessControl ac, String action)
  {
    ac.setAction(action);
  }

  public void removeAccessControl(AccessControl ac)
  {
    mapDocument.getAccessControl().remove(ac);
  }

  public void addAccessControl()
  {
    if (StringUtils.isBlank(roleToAdd)) return;

    long count = mapDocument.getAccessControl().stream()
      .filter(ac -> ac.getRoleId().equals(roleToAdd)).count();

    if (count == 0)
    {
      AccessControl ac = new AccessControl();
      ac.setRoleId(roleToAdd);
      ac.setAction(READ_ACTION);
      mapDocument.getAccessControl().add(ac);
    }
  }

  public List<String> findRoles(String text)
  {
    List<String> roleIds = new ArrayList<>();
    try
    {
      RoleFilter filter = new RoleFilter();
      filter.setName(text);
      List<Role> roles = SecurityConfigBean.getPort(true).findRoles(filter);
      for (Role role : roles)
      {
        roleIds.add(role.getRoleId());
      }
      Collections.sort(roleIds);
    }
    catch (Exception ex)
    {
      // ignore
    }
    return roleIds;
  }

  public void onMapNameChanged(AjaxBehaviorEvent event)
  {
    this.mapNameChanged = true;
  }

  public ServiceCapabilities getServiceCapabilities()
  {
    return serviceCapabilities;
  }

  public String getCapabilitiesServiceName()
  {
    return capabilitiesServiceName;
  }

  public void loadServiceCapabilities(String serviceName)
  {
    try
    {
      capabilitiesServiceName = serviceName;
      Service service = getMap().getServices().get(capabilitiesServiceName);
      serviceCapabilities =
        geoServiceBean.getServiceCapabilities(service.getUrl(), true);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  // services ---------------------------------------------

  public List<String> getServiceNames()
  {
    if (serviceNames == null)
    {
      serviceNames = new ArrayList<>(getMap().getServices().keySet());
      Collections.sort(serviceNames);
    }
    return serviceNames;
  }

  public Service getEditingService()
  {
    return editingService;
  }

  public void addService()
  {
    editingName = null;
    editingService = new Service();
  }

  public void editService(String serviceName)
  {
    editingName = serviceName;
    editingService = cloneObject(getMap().getServices().get(serviceName), Service.class);
  }

  public void removeService(String serviceName)
  {
    editingName = null;
    serviceNames = null;
    getMap().getServices().remove(serviceName);
  }

  public void acceptService()
  {
    if (!StringUtils.isBlank(editingName))
    {
      getMap().getServices().put(editingName, editingService);
    }
    editingName = null;
    editingService = null;
    serviceNames = null;
  }

  public void cancelService()
  {
    editingName = null;
    editingService = null;
  }


  // sources ---------------------------------------------

  public int getActiveSourceTabIndex()
  {
    return activeSourceTabIndex;
  }

  public void setActiveSourceTabIndex(int activeSourceTabIndex)
  {
    this.activeSourceTabIndex = activeSourceTabIndex;
  }

  public List<String> getSourceNames()
  {
    if (sourceNames == null)
    {
      sourceNames = new ArrayList<>(getMap().getSources().keySet());
      Collections.sort(sourceNames);
    }
    return sourceNames;
  }

  public String getSourceData(String sourceName)
  {
    Source source = getMap().getSources().get(sourceName);
    ServiceParameters serviceParameters = source.getServiceParameters();
    String service = serviceParameters.getService();

    if (!StringUtils.isBlank(service))
    {
      String layer = serviceParameters.getLayer();
      return service + " - " + layer;
    }
    else if (!source.getTiles().isEmpty())
    {
      return source.getTiles().toString();
    }
    else if (!StringUtils.isBlank(source.getUrl()))
    {
      return source.getUrl();
    }
    else
    {
      return String.valueOf(source.getData());
    }
  }

  public String getSourceDataUrl()
  {
    return editingSource.getData() instanceof String ?
      (String)editingSource.getData() : null;
  }

  public void setSourceDataUrl(String url)
  {
    if (!StringUtils.isBlank(url))
    {
      editingSource.setData(url);
    }
  }

  public String getSourceServiceUrl()
  {
    String serviceName = editingSource.getServiceParameters().getService();
    if (StringUtils.isBlank(serviceName)) return null;

    Service service = getMap().getServices().get(serviceName);
    if (service == null) return null;

    return service.getUrl();
  }

  public List<String> completeLayer(String text)
  {
    try
    {
      int index = text.lastIndexOf(";");
      if (index != -1) text = text.substring(index + 1);
      text = text.toUpperCase();
      String serviceName = editingSource.getServiceParameters().getService();
      Service service = getMap().getServices().get(serviceName);
      String serviceUrl = service.getUrl();
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

  public List<String> completeSldName(String text)
  {
    try
    {
      SldStore sldStore = getSldStore();
      return sldStore.findSld(text);
    }
    catch (Exception ex)
    {
      // ignore;
      return Collections.EMPTY_LIST;
    }
  }

  public void editSld()
  {
    try
    {
      ServiceParameters serviceParameters = editingSource.getServiceParameters();
      String sldName = serviceParameters.getSldName();
      String layer = serviceParameters.getLayer();
      Service service = getMap().getServices().get(serviceParameters.getService());
      String serviceUrl = service == null ? null : service.getUrl();

      if (!StringUtils.isBlank(sldName) && !StringUtils.isBlank(layer))
      {
        List<String> layers = Arrays.asList(layer.split(";"));
        String styleNames = serviceParameters.getStyles();
        List<String> styles = StringUtils.isBlank(styleNames) ?
          Collections.EMPTY_LIST : Arrays.asList(styleNames.split(";"));

        acceptSource();

        GeoSldBean geoSldBean = CDI.current().select(GeoSldBean.class).get();
        geoSldBean.editSld(sldName, layers, styles, serviceUrl);

        this.view = "sld_editor";
        this.mode = "visual";
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public Source getEditingSource()
  {
    return editingSource;
  }

  public void addSourceTile()
  {
    editingSource.getTiles().add("");
  }

  public void removeSourceTile(int index)
  {
    editingSource.getTiles().remove(index);
  }

  public void addSource()
  {
    editingName = null;
    editingSource = new Source();
  }

  public void editSource(String sourceName)
  {
    editingName = sourceName;
    editingSource = cloneObject(getMap().getSources().get(sourceName), Source.class);

    if (!editingSource.getTiles().isEmpty())
    {
      activeSourceTabIndex = 1;
    }
    else if (!StringUtils.isBlank(editingSource.getUrl()))
    {
      activeSourceTabIndex = 2;
    }
    else if (editingSource.getData() != null)
    {
      activeSourceTabIndex = 3;
    }
    else
    {
      activeSourceTabIndex = 0;
    }
  }

  public void removeSource(String sourceName)
  {
    editingName = null;
    sourceNames = null;
    getMap().getSources().remove(sourceName);
  }

  public void acceptSource()
  {
    if (!StringUtils.isBlank(editingName))
    {
      getMap().getSources().put(editingName, editingSource);
    }
    editingName = null;
    editingSource = null;
    sourceNames = null;
  }

  public void cancelSource()
  {
    editingName = null;
    editingSource = null;
  }

  // Layers ----------------------------------------------------

  public Layer getEditingLayer()
  {
    return editingLayer;
  }

  public void setLayerVisible(Layer layer, boolean visible)
  {
    layer.setVisible(visible);
  }

  public void setLayerLocatable(Layer layer, boolean locatable)
  {
    layer.setLocatable(locatable);
  }

  public String getJsonPaint()
  {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    return gson.toJson(editingLayer.getPaint());
  }

  public void setJsonPaint(String json)
  {
    Gson gson = new Gson();
    editingLayer.setPaint(gson.fromJson(json, java.util.Map.class));
  }

  public String getJsonLayout()
  {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    return gson.toJson(editingLayer.getLayout());
  }

  public void setJsonLayout(String json)
  {
    Gson gson = new Gson();
    editingLayer.setLayout(gson.fromJson(json, java.util.Map.class));
  }

  public void addLayer()
  {
    editingLayer = new Layer();
  }

  public void editLayer(Layer layer)
  {
    editingLayer = layer;
  }

  public void removeLayer(Layer layer)
  {
    getMap().getLayers().remove(layer);
  }

  public void acceptLayer()
  {
    if (!getMap().getLayers().contains(editingLayer))
    {
      getMap().getLayers().add(editingLayer);
    }
    editingLayer = null;
  }

  public void cancelLayer()
  {
    editingLayer = null;
  }

  public void onLayerReorder(ReorderEvent event)
  {
  }

  // LayerForm ----------------------------------------------------

  public List<LayerForm> getLayerForms()
  {
    List<LayerForm> layerForms =
      (List<LayerForm>)getMap().getMetadata().get("layerForms");
    if (layerForms == null)
    {
      layerForms = new ArrayList<>();
      getMap().getMetadata().put("layerForms", layerForms);
    }
    return layerForms;
  }

  public LayerForm getEditingLayerForm()
  {
    return editingLayerForm;
  }

  public void addLayerForm()
  {
    editingLayerForm = new LayerForm();
  }

  public void editLayerForm(LayerForm form)
  {
    editingLayerForm = form;
  }

  public void removeLayerForm(LayerForm form)
  {
    getLayerForms().remove(form);
  }

  public void acceptLayerForm()
  {
    if (!getLayerForms().contains(editingLayerForm))
    {
      getLayerForms().add(editingLayerForm);
    }
    editingLayerForm = null;
  }

  public void cancelLayerForm()
  {
    editingLayerForm = null;
  }

  // PrintReports

  public List<PrintReport> getPrintReports()
  {
    List<PrintReport> printReports =
      (List<PrintReport>)getMap().getMetadata().get("printReports");
    if (printReports == null)
    {
      printReports = new ArrayList<>();
      getMap().getMetadata().put("printReports", printReports);
    }
    return printReports;
  }

  public PrintReport getEditingPrintReport()
  {
    return editingPrintReport;
  }

  public void addPrintReport()
  {
    editingPrintReport = new PrintReport();
  }

  public void editPrintReport(PrintReport printReport)
  {
    editingPrintReport = printReport;
  }

  public void removePrintReport(PrintReport printReport)
  {
    getPrintReports().remove(printReport);
  }

  public void acceptPrintReport()
  {
    if (!getPrintReports().contains(editingPrintReport))
    {
      getPrintReports().add(editingPrintReport);
    }
    editingPrintReport = null;
  }

  public void cancelPrintReport()
  {
    editingPrintReport = null;
  }

  public String getPrintReportUrl(String reportName)
  {
    return getSvgStore().getSvgUrl(reportName);
  }

  public List<String> completeReportName(String text)
  {
    try
    {
      SvgStore svgStore = getSvgStore();
      return svgStore.findSvg(text);
    }
    catch (Exception ex)
    {
      // ignore;
      return Collections.EMPTY_LIST;
    }
  }

  // Map -------------------------------------------------------

  public void newMap()
  {
    mapDocument = new MapDocument();
    this.activeTabIndex = 0;
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
        this.setView(view);
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
      MapDocument mapReloaded = getMapStore().loadMap(getMap().getName());
      if (mapReloaded == null)
      {
        error("MAP_NOT_FOUND");
      }
      else
      {
        mapDocument = mapReloaded;
        convertMetadata();
        growl("MAP_RELOADED");
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
        mapNameChanged = false;
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void removeMap()
  {
    String mapName = getMap().getName();

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
    return getMap().toString();
  }

  public void setJsonMap(String json)
  {
    try
    {
      getMap().fromString(json);
      convertMetadata();
    }
    catch (IOException ex)
    {
      error(ex);
    }
  }

  // private -------------------------------------------------------

  private void updateSldUrls()
  {
    SldStore sldStore = getSldStore();
    for (Source source : getMap().getSources().values())
    {
      ServiceParameters serviceParameters = source.getServiceParameters();
      String sldName = serviceParameters.getSldName();
      if (!StringUtils.isBlank(sldName))
      {
        String sldUrl = sldStore.getSldUrl(sldName);
        System.out.println(">>>>SLD " + sldName + "=" + sldUrl);
        serviceParameters.setSldUrl(sldUrl);
      }
    }
  }

  private <T> T cloneObject(T object, Class<T> type)
  {
    Gson gson = new Gson();
    String json = gson.toJson(object);
    return gson.fromJson(json, type);
  }

  private MapStore getMapStore()
  {
    MapStore mapStore = CDI.current().select(MapStore.class).get();
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    mapStore.setCredentials(userSessionBean.getUserId(), userSessionBean.getPassword());
    return mapStore;
  }

  private SldStore getSldStore()
  {
    SldStore sldStore = CDI.current().select(SldStore.class).get();
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    sldStore.setCredentials(userSessionBean.getUserId(), userSessionBean.getPassword());
    return sldStore;
  }

  private SvgStore getSvgStore()
  {
    SvgStore svgStore = CDI.current().select(SvgStore.class).get();
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    svgStore.setCredentials(userSessionBean.getUserId(), userSessionBean.getPassword());
    return svgStore;
  }

  private void convertMetadata()
  {
    List list = (List)getMap().getMetadata().get("layerForms");
    if (list != null)
    {
      for (int i = 0; i < list.size(); i++)
      {
        list.set(i, new LayerForm((java.util.Map)list.get(i)));
      }
    }

    list = (List)getMap().getMetadata().get("printReports");
    if (list != null)
    {
      for (int i = 0; i < list.size(); i++)
      {
        list.set(i, new PrintReport((java.util.Map)list.get(i)));
      }
    }
  }
}
