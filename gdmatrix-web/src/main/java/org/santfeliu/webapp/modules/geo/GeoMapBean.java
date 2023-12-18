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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.security.AccessControl;
import org.matrix.security.Role;
import org.matrix.security.RoleFilter;
import org.primefaces.event.ReorderEvent;
import org.santfeliu.faces.maplibre.model.Layer;
import org.santfeliu.faces.maplibre.model.Style;
import org.santfeliu.webapp.modules.geo.metadata.Service;
import org.santfeliu.webapp.modules.geo.metadata.ServiceParameters;
import org.santfeliu.faces.maplibre.model.Source;
import org.santfeliu.security.web.SecurityConfigBean;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.geo.io.MapStore;
import org.santfeliu.webapp.modules.geo.io.MapStore.MapDocument;
import org.santfeliu.webapp.modules.geo.io.SldStore;
import org.santfeliu.webapp.modules.geo.io.SvgStore;
import org.santfeliu.webapp.modules.geo.metadata.LayerForm;
import org.santfeliu.webapp.modules.geo.metadata.PrintReport;
import org.santfeliu.webapp.modules.geo.ogc.ServiceCapabilities;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.model.file.UploadedFile;
import org.santfeliu.util.IOUtils;
import org.santfeliu.webapp.modules.geo.io.MapStore.MapCategory;
import org.santfeliu.webapp.modules.geo.metadata.LegendGroup;
import org.santfeliu.webapp.modules.geo.metadata.LegendItem;
import org.santfeliu.webapp.modules.geo.metadata.LegendLayer;
import static org.matrix.dic.DictionaryConstants.READ_ACTION;
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
  private int activeSourceTabIndex;
  private transient List<String> serviceIds;
  private transient List<String> sourceIds;
  private transient List<String> layerIds;
  private String editingId;
  private Source editingSource;
  private ServiceParameters editingServiceParameters;
  private Service editingService;
  private Layer editingLayer;
  private LegendTreeNode legendTreeRoot;
  private List<TreeNode> legendSelection;
  private List<TreeNode> legendCut;
  private LegendGroup editingLegendGroup;
  private LegendLayer editingLegendLayer;
  private boolean isNewLegendItem;
  private LayerForm editingLayerForm;
  private PrintReport editingPrintReport;
  private String reportToUpload;
  private String roleToAdd;
  private transient ServiceCapabilities serviceCapabilities;
  private transient String capabilitiesServiceId;

  @Inject
  GeoServiceBean geoServiceBean;

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

  public String getEditingId()
  {
    return editingId;
  }

  public void setEditingId(String editingId)
  {
    this.editingId = editingId;
  }

  public void onMapNameChanged(AjaxBehaviorEvent event)
  {
    this.mapNameChanged = true;
  }

  // services ---------------------------------------------

  public Map<String, Service> getServices()
  {
    return getServices(true);
  }

  public Map<String, Service> getServices(boolean neverNull)
  {
    Map<String, Service> services =
      (Map<String, Service>)getStyle().getMetadata().get("services");
    if (services == null && neverNull)
    {
      services = new HashMap<>();
      getStyle().getMetadata().put("services", services);
    }
    return services;
  }

  public ServiceCapabilities getServiceCapabilities()
  {
    return serviceCapabilities;
  }

  public String getCapabilitiesServiceId()
  {
    return capabilitiesServiceId;
  }

  public void loadServiceCapabilities(String serviceId)
  {
    try
    {
      capabilitiesServiceId = serviceId;
      Service service = getServices().get(capabilitiesServiceId);
      serviceCapabilities =
        geoServiceBean.getServiceCapabilities(service.getUrl(), true);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public List<String> getServiceIds()
  {
    if (serviceIds == null)
    {
      serviceIds = new ArrayList<>(getServices().keySet());
      Collections.sort(serviceIds);
    }
    return serviceIds;
  }

  public Service getEditingService()
  {
    return editingService;
  }

  public void addService()
  {
    editingId = null;
    editingService = new Service();
  }

  public void editService(String serviceId)
  {
    editingId = serviceId;
    editingService = cloneObject(getServices().get(serviceId), Service.class);
  }

  public void removeService(String serviceId)
  {
    editingId = null;
    serviceIds = null;
    getServices().remove(serviceId);
  }

  public void acceptService()
  {
    if (!isBlank(editingId))
    {
      getServices().put(editingId, editingService);
    }
    editingId = null;
    editingService = null;
    serviceIds = null;
  }

  public void cancelService()
  {
    editingId = null;
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

  public List<String> getSourceIds()
  {
    if (sourceIds == null)
    {
      sourceIds = new ArrayList<>(getStyle().getSources().keySet());
      Collections.sort(sourceIds);
    }
    return sourceIds;
  }

  public String getSourceInfo(String sourceId)
  {
    Source source = getStyle().getSources().get(sourceId);
    ServiceParameters serviceParameters = getServiceParameters(sourceId);

    if (serviceParameters != null)
    {
      String serviceId = serviceParameters.getService();
      String layers = serviceParameters.getLayers();
      return serviceId + " - " + layers;
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
    ServiceParameters serviceParameters = getServiceParameters(editingId);
    if (serviceParameters == null) return null;

    String serviceId = serviceParameters.getService();
    if (isBlank(serviceId)) return null;

    Service service = getServices().get(serviceId);
    if (service == null) return null;

    return service.getUrl();
  }

  public List<String> completeSourceLayer(String text)
  {
    return completeLayer(editingServiceParameters, text);
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
      getStyle().getSources().put(editingId, editingSource);
      setServiceParameters(editingId, editingServiceParameters);
      String sourceId = editingId;

      editingId = null;
      editingSource = null;
      editingServiceParameters = null;
      sourceIds = null;

      editSld(sourceId, null, null);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void addSource()
  {
    editingId = null;
    editingSource = new Source();
    editingServiceParameters = new ServiceParameters();
  }

  public void editSource(String sourceId)
  {
    editingId = sourceId;
    editingSource =
      cloneObject(getStyle().getSources().get(sourceId), Source.class);
    editingServiceParameters = getServiceParameters(sourceId);
    editingServiceParameters = editingServiceParameters == null ?
      new ServiceParameters() :
      cloneObject(editingServiceParameters, ServiceParameters.class);

    if (!editingSource.getTiles().isEmpty())
    {
      activeSourceTabIndex = 1;
    }
    else if (!isBlank(editingSource.getUrl()))
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

  public void removeSource(String sourceId)
  {
    editingId = null;
    sourceIds = null;
    getStyle().getSources().remove(sourceId);
  }

  public void acceptSource()
  {
    if (!isBlank(editingId))
    {
      getStyle().getSources().put(editingId, editingSource);
      setServiceParameters(editingId, editingServiceParameters);
    }
    editingId = null;
    editingSource = null;
    editingServiceParameters = null;
    sourceIds = null;
  }

  public void cancelSource()
  {
    editingId = null;
    editingSource = null;
  }

  // Layers ----------------------------------------------------

  public List<String> getLayerIds()
  {
    if (layerIds == null)
    {
      layerIds = getStyle().getLayers().stream().map(l -> l.getId()).
        collect(Collectors.toList());
      Collections.sort(layerIds);
    }
    return layerIds;
  }

  public Layer getEditingLayer()
  {
    return editingLayer;
  }

  public String getEditingLayerSldName()
  {
    String sourceId = editingLayer.getSource();
    if (sourceId == null) return null;
    ServiceParameters serviceParameters = getServiceParameters(sourceId);
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


  public void onLayerReorder(ReorderEvent event)
  {
  }

  public List<String> completeLayerLayer(String text)
  {
    String sourceId = editingLayer.getSource();
    ServiceParameters serviceParameters = getServiceParameters(sourceId);
    if (serviceParameters == null) return Collections.EMPTY_LIST;
    return completeLayer(serviceParameters, text);
  }

  public void editLayerSld()
  {
    try
    {
      String sourceId = editingLayer.getSource();
      editSld(sourceId, editingLayer.getLayers(), editingLayer.getStyles());
    }
    catch (Exception ex)
    {
      error(ex);
    }
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
    getStyle().getLayers().remove(layer);
  }

  public void acceptLayer()
  {
    if (!getStyle().getLayers().contains(editingLayer))
    {
      getStyle().getLayers().add(editingLayer);
    }
    editingLayer = null;
  }

  public void cancelLayer()
  {
    editingLayer = null;
  }

  // Legend ----------------------------------------------------

  public TreeNode getLegendTreeRoot()
  {
    if (legendTreeRoot == null)
    {
      legendTreeRoot = new LegendTreeNode("group", null, null);

      LegendGroup legendGroup =
        (LegendGroup)getStyle().getMetadata().get("legend");
      if (legendGroup == null)
      {
        legendGroup = new LegendGroup();
        legendGroup.setLabel("Legend");
        getStyle().getMetadata().put("legend", legendGroup);
      }
      populateLegendItem(legendGroup, legendTreeRoot);
    }
    return legendTreeRoot;
  }

  public List<TreeNode> getLegendSelection()
  {
    return legendSelection;
  }

  public void setLegendSelection(List<TreeNode> legendSelection)
  {
    this.legendSelection = legendSelection;
  }

  public void cutLegendNodes()
  {
    legendCut = legendSelection;
  }

  public boolean isLegendNodeSelected(String type)
  {
    if (legendSelection != null && legendSelection.size() == 1)
    {
      LegendTreeNode node = (LegendTreeNode)legendSelection.get(0);
      return node.getType().equals(type);
    }
    return false;
  }

  public LegendGroup getEditingLegendGroup()
  {
    return editingLegendGroup;
  }

  public LegendLayer getEditingLegendLayer()
  {
    return editingLegendLayer;
  }

  public void addLegendGroup()
  {
    if (legendSelection != null && legendSelection.size() == 1)
    {
      editingLegendGroup = new LegendGroup();
      editingLegendGroup.setMode(LegendGroup.MULTIPLE);
      editingLegendGroup.setLabel("Group");
      isNewLegendItem = true;
    }
  }

  public void addLegendLayer()
  {
    if (legendSelection != null && legendSelection.size() == 1)
    {
      editingLegendLayer = new LegendLayer();
      isNewLegendItem = true;
    }
  }

  public void editLegendGroup()
  {
    editingLegendGroup = (LegendGroup)legendSelection.get(0).getData();
  }

  public void editLegendLayer()
  {
    editingLegendLayer = (LegendLayer)legendSelection.get(0).getData();
  }

  public void acceptLegendGroup()
  {
    if (isNewLegendItem)
    {
      LegendTreeNode groupNode = new LegendTreeNode(editingLegendGroup);
      LegendTreeNode targetNode = (LegendTreeNode)legendSelection.get(0);
      if (targetNode.isGroupNode())
      {
        targetNode.add(groupNode);
      }
      isNewLegendItem = false;
    }
    editingLegendGroup = null;
  }

  public void cancelLegendGroup()
  {
    editingLegendGroup = null;
    isNewLegendItem = false;
  }

  public void acceptLegendLayer()
  {
    if (isBlank(editingLegendLayer.getLabel()))
    {
      Optional<Layer> layer = getStyle().getLayers().stream()
        .filter(l -> l.getId().equals(editingLegendLayer.getLayerId()))
        .findAny();
      if (layer.isPresent())
      {
        editingLegendLayer.setLabel(layer.get().getLabel());
      }
      else
      {
        editingLegendLayer.setLabel(editingLegendLayer.getLayerId());
      }
    }
    if (isNewLegendItem)
    {
      LegendTreeNode layerNode = new LegendTreeNode(editingLegendLayer);
      LegendTreeNode targetNode = (LegendTreeNode)legendSelection.get(0);
      if (targetNode.isGroupNode())
      {
        targetNode.add(layerNode);
      }
      isNewLegendItem = false;
    }
    editingLegendLayer = null;
  }

  public void cancelLegendLayer()
  {
    editingLegendLayer = null;
  }

  public void removeLegendNodes()
  {
    for (TreeNode node : legendSelection)
    {
      TreeNode parentNode = node.getParent();
      if (parentNode != null)
      {
        parentNode.getChildren().remove(node);
      }
    }
  }

  public boolean isTopLegendNode()
  {
    if (legendSelection == null || legendSelection.size() != 1) return false;

    return legendSelection.get(0).getParent() == this.legendTreeRoot;
  }

  public boolean isCutLegendNode(TreeNode node)
  {
    if (legendCut == null || node == null) return false;

    if (legendCut.contains(node)) return true;

    return isCutLegendNode(node.getParent());
  }

  public boolean isLegendPasteEnabled()
  {
    if (legendCut == null) return false;
    if (legendSelection == null) return false;
    if (legendSelection.size() != 1) return false;
    LegendTreeNode node = (LegendTreeNode)legendSelection.get(0);
    return node.isGroupNode();
  }

  public void pasteLegendNodes()
  {
    if (legendCut != null && legendSelection != null)
    {
      if (legendSelection.size() == 1 && !legendCut.isEmpty())
      {
        LegendTreeNode targetNode = (LegendTreeNode)legendSelection.get(0);
        if (targetNode.isGroupNode())
        {
          for (TreeNode node : legendCut)
          {
            LegendTreeNode sourceNode = (LegendTreeNode)node;
            if (targetNode != sourceNode &&
                !targetNode.isDescendant(sourceNode))
            {
              targetNode.add(sourceNode);
            }
          }
        }
      }
      legendCut = null;
    }
  }

  // LayerForm ----------------------------------------------------

  public List<LayerForm> getLayerForms()
  {
    List<LayerForm> layerForms =
      (List<LayerForm>)getStyle().getMetadata().get("layerForms");
    if (layerForms == null)
    {
      layerForms = new ArrayList<>();
      getStyle().getMetadata().put("layerForms", layerForms);
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

  // PrintReports ----------------------------------------------------

  public List<PrintReport> getPrintReports()
  {
    List<PrintReport> printReports =
      (List<PrintReport>)getStyle().getMetadata().get("printReports");
    if (printReports == null)
    {
      printReports = new ArrayList<>();
      getStyle().getMetadata().put("printReports", printReports);
    }
    return printReports;
  }

  public PrintReport getEditingPrintReport()
  {
    return editingPrintReport;
  }

  public void setPrintReportToUpload(String reportName)
  {
    reportToUpload = reportName;
  }

  public void uploadPrintReportFile(FileUploadEvent event)
  {
    UploadedFile reportFileToUpload = event.getFile();
    if (reportToUpload != null)
    {
      File fileToStore = null;
      try
      {
        fileToStore = File.createTempFile("template", ".svg");
        try (InputStream is = reportFileToUpload.getInputStream())
        {
          IOUtils.writeToFile(is, fileToStore);
        }
        getSvgStore().storeSvg(reportToUpload, fileToStore);
      }
      catch (Exception ex)
      {
        error(ex);
      }
      finally
      {
        try
        {
          if (fileToStore != null) fileToStore.delete();
          reportFileToUpload.delete();
        }
        catch (Exception ex2)
        {
        }
      }
    }
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
    return getSvgStore().getReportUrl(reportName);
  }

  public boolean isSvgPrintReport(String reportName)
  {
    return getSvgStore().getReportUrl(reportName).endsWith(".svg");
  }

  public boolean isUploadablePrintReport(String reportName)
  {
    String url = getSvgStore().getReportUrl(reportName);
    return url.endsWith(".svg") || url.equals("#");
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

  // Roles --------------------------------------------------------

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
    if (isBlank(roleToAdd)) return;

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

  // private -------------------------------------------------------

  private void editSld(String sourceId, String layers, String styles)
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

  private List<String> completeLayer(ServiceParameters serviceParameters,
    String text)
  {
    try
    {
      int index = text.lastIndexOf(",");
      if (index != -1) text = text.substring(index + 1);
      text = text.toUpperCase();

      String serviceId = serviceParameters.getService();
      Service service = getServices().get(serviceId);

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

  private void updateSldUrls()
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

    Map legendProperties =
      (Map)getStyle().getMetadata().get("legend");
    if (legendProperties != null &&
        "group".equals(legendProperties.get("type")))
    {
      getStyle().getMetadata().put("legend", new LegendGroup(legendProperties));
    }
    legendTreeRoot = null;
  }

  private LegendTreeNode populateLegendItem(LegendItem legendItem,
    LegendTreeNode parentTreeNode)
  {
    LegendTreeNode node = new LegendTreeNode(legendItem);
    node.setType(legendItem.getType());
    node.setExpanded(true);
    if (parentTreeNode != null)
    {
      parentTreeNode.getChildren().add(node);
    }
    if (legendItem instanceof LegendGroup)
    {
      LegendGroup group = (LegendGroup)legendItem;
      for (LegendItem item : group.getChildren())
      {
        populateLegendItem(item, node);
      }
    }
    return node;
  }

  static public class LegendTreeNode extends DefaultTreeNode<LegendItem>
  {
    public LegendTreeNode()
    {
    }

    public LegendTreeNode(LegendItem data)
    {
      this(data, null);
    }

    public LegendTreeNode(LegendItem data, TreeNode parent)
    {
      this(data.getType(), data, parent);
    }

    public LegendTreeNode(String type, LegendItem data, TreeNode parent)
    {
      super(type, data, parent);
    }

    public boolean isGroupNode()
    {
      return "group".equals(getType());
    }

    public void add(LegendTreeNode legendNode)
    {
      getChildren().add(legendNode);
      LegendGroup legendGroup = (LegendGroup)getData();
      legendGroup.getChildren().add(legendNode.getData());
      setExpanded(true);
    }

    public void add(int index, LegendTreeNode legendNode)
    {
      getChildren().add(index, legendNode);
      LegendGroup legendGroup = (LegendGroup)getData();
      legendGroup.getChildren().add(index, legendNode.getData());
      setExpanded(true);
    }

    public boolean isDescendant(TreeNode ancestor)
    {
      TreeNode node = getParent();
      while (node != null && node != ancestor)
      {
        node = node.getParent();
      }
      return node == ancestor;
    }

    @Override
    public void clearParent()
    {
      LegendTreeNode oldParentNode = (LegendTreeNode)getParent();
      if (oldParentNode != null)
      {
        LegendGroup legendGroup = (LegendGroup)oldParentNode.getData();
        LegendItem legendItem = (LegendItem)getData();
        legendGroup.getChildren().remove(legendItem);
      }
      super.clearParent();
    }

    @Override
    public String toString()
    {
      LegendItem legendItem = getData();
      if (legendItem == null) return null;

      String type = legendItem.getType();
      String label = legendItem.getLabel();
      return type + ": " + label;
    }
  }
}
