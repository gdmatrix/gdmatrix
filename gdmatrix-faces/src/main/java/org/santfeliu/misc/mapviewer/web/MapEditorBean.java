package org.santfeliu.misc.mapviewer.web;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import javax.faces.model.SelectItem;
import org.apache.commons.lang.StringUtils;
import org.matrix.security.User;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.form.Form;
import org.santfeliu.form.FormFactory;
import org.santfeliu.misc.mapviewer.Bounds;
import org.santfeliu.misc.mapviewer.Map;
import org.santfeliu.misc.mapviewer.Map.Group;
import org.santfeliu.misc.mapviewer.Map.InfoLayer;
import org.santfeliu.misc.mapviewer.MapDocument;
import org.santfeliu.misc.mapviewer.Map.Layer;
import org.santfeliu.misc.mapviewer.Map.Service;
import org.santfeliu.misc.mapviewer.MapCategory;
import org.santfeliu.misc.mapviewer.ServiceCache;
import org.santfeliu.misc.mapviewer.ServiceCapabilities;
import org.santfeliu.security.UserCache;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSManagedBean;

/**
 *
 * @author realor
 */
@CMSManagedBean
public class MapEditorBean extends WebBean implements Savable
{
  private Service previousService;
  private Group previousGroup;
  private Layer editingLayer;
  private InfoLayer previousInfoLayer;
  private transient List<String> propertyNames;
  private String editingPropertyName;
  private String editingPropertyValue;
  private int serviceIndex = -1;
  private int groupIndex = -1;
  private int layerIndex = -1;
  private int infoLayerIndex = -1;
  private int propertyIndex = -1;
  private String readRole;
  private String writeRole;
  private int selectedTabIndex = 0;
  private transient ServiceCapabilities capabilities;
  private transient SLDCache sldCache = new SLDCache();
  private transient List<MapCategory> mapCategories;

  public void setMapBounds(String text) throws Exception
  {
    try
    {
      if (!StringUtils.isBlank(text))
      {
        MapDocument map = MapBean.getInstance().getMap();
        Bounds bounds = new Bounds(text);
        map.setBounds(bounds);
      }
    }
    catch (Exception ex)
    {
      error("MAP_INVALID_BOUNDS");
    }
  }

  public String getMapBounds()
  {
    MapBean mapBean = MapBean.getInstance();
    return mapBean.getMap().getBounds().toString();
  }

  public void setThumbnailBounds(String text) throws Exception
  {
    try
    {
      MapDocument map = MapBean.getInstance().getMap();
      if (!StringUtils.isBlank(text))
      {
        Bounds bounds = new Bounds(text);
        map.setThumbnailBounds(bounds);
      }
      else
      {
        map.setThumbnailBounds(null);
      }
    }
    catch (Exception ex)
    {
      error("MAP_INVALID_BOUNDS");
    }
  }

  public String getThumbnailBounds()
  {
    MapBean mapBean = MapBean.getInstance();
    Bounds bounds = mapBean.getMap().getThumbnailBounds();
    return bounds == null ? "" : bounds.toString();
  }
  
  public boolean isMetadataFormRendered()
  {
    String formSelector = 
      getProperty(MapViewerBean.METADATA_FORM_SELECTOR_PROPERTY);
    return formSelector != null;
  }

  public Form getMetadataForm()
  {
    try
    {
      String formSelector = 
        getProperty(MapViewerBean.METADATA_FORM_SELECTOR_PROPERTY);
      if (formSelector != null)
      {
        MapBean mapBean = MapBean.getInstance();
        FormFactory formFactory = FormFactory.getInstance();
        // update form only in render phase
        boolean updated = getFacesContext().getRenderResponse();
        return formFactory.getForm(formSelector, 
          mapBean.getMap().getMetadata(), updated);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public List<SelectItem> getCategorySelectItems()
  {
    List<SelectItem> selectItems = new ArrayList<SelectItem>();
    SelectItem item = new SelectItem("", " ");
    selectItems.add(item);
    try
    {
      if (mapCategories == null)
      {
        mapCategories = MapBean.getInstance().findMapCategories();
      }
      for (MapCategory mapCategory : mapCategories)
      {
        item = new SelectItem(mapCategory.getName(), mapCategory.getTitle());
        selectItems.add(item);
      }
    }
    catch (Exception ex)
    {      
    }
    return selectItems;
  }
  
  public String getCaptureUserDisplayName()
  {
    MapBean mapBean = MapBean.getInstance();
    String userId = mapBean.getMap().getCaptureUserId();
    return getUserDisplayName(userId);
  }

  public int getYearsFromCapture()
  {
    MapBean mapBean = MapBean.getInstance();
    String captureDateTime = mapBean.getMap().getCaptureDateTime();
    return (int)(getEllapsedTime(captureDateTime) / (365.25*24*3600*1000));
  }
  
  public String getChangeUserDisplayName()
  {
    MapBean mapBean = MapBean.getInstance();
    String userId = mapBean.getMap().getChangeUserId();
    return getUserDisplayName(userId);
  }

  public int getDaysFromLastChange()
  {
    MapBean mapBean = MapBean.getInstance();
    String changeDateTime = mapBean.getMap().getChangeDateTime();
    return (int)(getEllapsedTime(changeDateTime) / (24*3600*1000));
  }  

  private long getEllapsedTime(String dateTime)
  {
    Date date = TextUtils.parseInternalDate(dateTime);
    Date now = new Date();
    return now.getTime() - date.getTime();    
  }
  
  private String getUserDisplayName(String userId)
  {
    try
    {
      String password = MatrixConfig.getProperty(
        "org.santfeliu.security.service.SecurityManager.masterPassword");
      User user = UserCache.getUser(userId, password);    
      return user.getDisplayName();
    }
    catch (Exception ex)
    {
      return null;
    }
  }
  
  public Layer getEditingLayer()
  {
    return editingLayer;
  }

  public int getServiceIndex()
  {
    return serviceIndex;
  }

  public int getLayerIndex()
  {
    return layerIndex;
  }

  public int getSelectedTabIndex()
  {
    return selectedTabIndex;
  }

  public void setSelectedTabIndex(int selectedTabIndex)
  {
    this.selectedTabIndex = selectedTabIndex;
  }

  public ServiceCapabilities getCapabilities()
  {
    return capabilities;
  }

  public List<SelectItem> getServiceSelectItems()
  {
    MapDocument map = MapBean.getInstance().getMap();
    List<SelectItem> items = new ArrayList<SelectItem>();
    for (Service service : map.getServices())
    {
      SelectItem item = new SelectItem();
      String name = service.getName();
      if (!StringUtils.isBlank(name))
      {
        String description = service.getDescription();
        if (description == null) description = name;
        item.setValue(name);
        item.setLabel(description);
        items.add(item);
      }
    }
    return items;
  }

  public List<SelectItem> getGroupSelectItems()
  {
    MapDocument map = MapBean.getInstance().getMap();
    List<SelectItem> items = new ArrayList<SelectItem>();
    SelectItem item = new SelectItem();
    item.setValue("");
    item.setLabel("");
    items.add(item);
    for (Group group : map.getGroups())
    {
      if (!StringUtils.isBlank(group.getName()))
      {
        item = new SelectItem();
        String name = group.getName();
        String label = group.getLabel();
        if (label == null) label = name;
        item.setValue(name);
        item.setLabel(label);
        items.add(item);
      }
    }
    return items;
  }

  public int getServiceCount()
  {
    MapDocument map = MapBean.getInstance().getMap();
    return map.getServices().size();
  }

  public int getLayerCount()
  {
    MapDocument map = MapBean.getInstance().getMap();
    return map.getLayers().size();
  }

  public void setLayerService(String serviceName)
  {
    MapDocument map = MapBean.getInstance().getMap();
    Service service = map.getService(serviceName);
    editingLayer.setService(service);
  }

  public String getLayerService()
  {
    Service service = editingLayer.getService();
    return service == null ? null : service.getName();
  }

  public void setLayerGroup(String groupName)
  {
    MapDocument map = MapBean.getInstance().getMap();
    if (!StringUtils.isBlank(groupName))
    {
      Group group = map.getGroup(groupName);
      editingLayer.setGroup(group);
    }
    else
    {
      editingLayer.setGroup(null);
    }
  }

  public String getLayerGroup()
  {
    Group group = editingLayer.getGroup();
    return group == null ? null : group.getName();
  }

  public void setLayerEditRoles(String editRoleString)
  {
    List<String> editRoles = editingLayer.getEditRoles();
    editRoles.clear();
    StringTokenizer tokenizer = new StringTokenizer(editRoleString, ",");
    while (tokenizer.hasMoreTokens())
    {
      String editRole = tokenizer.nextToken().trim();
      if (!editRoles.contains(editRole)) editRoles.add(editRole);
    }
  }

  public String getLayerEditRoles()
  {
    List<String> editRoles = editingLayer.getEditRoles();
    StringBuilder buffer = new StringBuilder();
    for (String editRole : editRoles)
    {
      if (buffer.length() > 0) buffer.append(", ");
      buffer.append(editRole);
    }
    return buffer.toString();
  }

  public boolean isFilteredLayer()
  {
    Layer layer = (Layer)getValue("#{layer}");
    return !StringUtils.isBlank(layer.getCqlFilter());
  }

  public String getReadRole()
  {
    return readRole;
  }

  public void setReadRole(String readRole)
  {
    this.readRole = readRole;
  }

  public String getWriteRole()
  {
    return writeRole;
  }

  public void setWriteRole(String writeRole)
  {
    this.writeRole = writeRole;
  }

  public String getScripts()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    Credentials credentials = userSessionBean.getCredentials();
    StringBuilder buffer = new StringBuilder();
    addScriptFile("OpenLayers.js", buffer);
    addScriptFile("Controls.js", buffer);
    addScriptFile("autocomplete.js", buffer);
    buffer.append("<script type=\"text/javascript\">");
    MapBean mapBean = MapBean.getInstance();
    String baseUrl = mapBean.getBaseUrl();
    buffer.append("var baseUrl = '").append(baseUrl).append("';\n");
    buffer.append("var serviceUrl = null;\n");
    buffer.append("var serviceArray = [];\n");
    buffer.append("var serviceList = {};\n");
    MapDocument map = MapBean.getInstance().getMap();
    List<Service> services = map.getServices();
    for (Service service : services)
    {
      String serviceUrl = service.getUrl();
      buffer.append("serviceArray.push('");
      buffer.append(service.getUrl());
      buffer.append("');\n");
      buffer.append("serviceList[\"");
      buffer.append(service.getName());
      buffer.append("\"]=[");
      try
      {
        ServiceCapabilities serviceCapabilities =
          ServiceCache.getServiceCapabilities(serviceUrl, false, credentials);
        List<ServiceCapabilities.Layer> layers = serviceCapabilities.getLayers();
        int i = 0;
        for (ServiceCapabilities.Layer layer : layers)
        {
          if (i > 0) buffer.append(", ");
          buffer.append("\"");
          buffer.append(layer.getName());
          buffer.append("\"");
          i++;
        }
      }
      catch (Exception ex)
      {
      }
      buffer.append("];\n");
    }
    buffer.append("</script>\n");
    return buffer.toString();
  }

  public String getLayerTestUrl()
  {
    try
    {
      Layer layer = (Layer)getValue("#{layer}");
      Map map = layer.getMap();
      String srs = map.getSrs();
      Service service = layer.getService();
      StringBuilder buffer = new StringBuilder();
      buffer.append("/proxy?url=");
      buffer.append(service.getUrl());
      buffer.append("&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap");
      buffer.append("&LAYERS=");
      buffer.append(layer.getNamesString());
      String styles = layer.getStylesString();
      if (!StringUtils.isBlank(styles))
      {
        buffer.append("&STYLES=");
        buffer.append(URLEncoder.encode(styles, "UTF-8"));
      }
      String cqlFilter = layer.getCqlFilter();
      if (!StringUtils.isBlank(cqlFilter))
      {
        buffer.append("&CQL_FILTER=");
        buffer.append(URLEncoder.encode(cqlFilter, "UTF-8"));
      }
      String sld = layer.getSld();
      if (!StringUtils.isBlank(sld))
      {
        if (sldCache == null) sldCache = new SLDCache();
        String sldUrl = sldCache.getSldUrl(sld);
        buffer.append("&SLD=");
        buffer.append(URLEncoder.encode(sldUrl, "UTF-8"));
      }
      buffer.append("&SRS=");
      buffer.append(URLEncoder.encode(srs, "UTF-8"));
      Bounds bounds = map.getBounds();
      bounds = bounds.getAdjusted(256, 256);
      buffer.append("&BBOX=");
      buffer.append(bounds.getMinX());
      buffer.append(",");
      buffer.append(bounds.getMinY());
      buffer.append(",");
      buffer.append(bounds.getMaxX());
      buffer.append(",");
      buffer.append(bounds.getMaxY());
      buffer.append("&WIDTH=256");
      buffer.append("&HEIGHT=256");
      buffer.append("&FORMAT=image/png");

      return buffer.toString();
    }
    catch (Exception ex)
    {
      return "#";
    }
  }

  // Service actions
  public void addService()
  {
    MapDocument map = MapBean.getInstance().getMap();
    previousService = null;
    serviceIndex = map.getServices().size();
    map.getServices().add(map.createService());
    selectedTabIndex = 1;
  }

  public void editService()
  {
    cancelService();
    MapDocument map = MapBean.getInstance().getMap();
    Service service = (Service)getValue("#{service}");
    serviceIndex = (Integer)getValue("#{index}");
    previousService = map.createService();
    previousService.setTo(service);
    selectedTabIndex = 1;
  }

  public void removeService()
  {
    cancelService();
    int index = (Integer)getValue("#{index}");
    MapDocument map = MapBean.getInstance().getMap();
    map.getServices().remove(index);
    selectedTabIndex = 1;
  }

  public void showCapabilities()
  {
    try
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      Credentials credentials = userSessionBean.getCredentials();
      Service service = (Service)getValue("#{service}");
      String serviceUrl = service.getUrl();
      capabilities = ServiceCache.getServiceCapabilities(serviceUrl, true,
        credentials);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void saveService()
  {
    MapDocument map = MapBean.getInstance().getMap();
    Service service = map.getServices().get(serviceIndex);
    if (StringUtils.isBlank(service.getName()))
    {
      error("SERVICE_NAME_IS_MANDATORY");
    }
    else
    {
      endServiceEdition();
    }
  }

  public void cancelService()
  {
    if (serviceIndex != -1)
    {
      MapDocument map = MapBean.getInstance().getMap();
      if (previousService == null)
      {
        map.getServices().remove(serviceIndex);
      }
      else
      {
        Service service = map.getServices().get(serviceIndex);
        service.setTo(previousService);
      }
    }
    endServiceEdition();
  }

  // Layer actions
  public void toggleBaseLayer()
  {
    Layer layer = (Layer)getValue("#{layer}");
    layer.setBaseLayer(!layer.isBaseLayer());
    if (layer.isBaseLayer())
    {
      prepareBaseLayer(layer);
    }
  }

  public void toggleVisible()
  {
    Layer layer = (Layer)getValue("#{layer}");
    layer.setVisible(!layer.isVisible());
  }

  public void toggleLocatable()
  {
    Layer layer = (Layer)getValue("#{layer}");
    layer.setLocatable(!layer.isLocatable());
  }

  public void toggleSnap()
  {
    Layer layer = (Layer)getValue("#{layer}");
    layer.setSnap(!layer.isSnap());
  }

  public void toggleIndependent()
  {
    Layer layer = (Layer)getValue("#{layer}");
    layer.setIndependent(!layer.isIndependent());
  }

  public void toggleOnLegend()
  {
    Layer layer = (Layer)getValue("#{layer}");
    layer.setOnLegend(!layer.isOnLegend());
  }

  public void addLayer()
  {
    MapDocument map = MapBean.getInstance().getMap();
    editingLayer = map.createLayer();
    editingLayer.setBaseLayer(false);
    editingLayer.setVisible(true);
    editingLayer.setLocatable(true);
    editingLayer.setOnLegend(true);
    editingLayer.setFormat("image/png");
    layerIndex = -1;
    selectedTabIndex = 3;
  }

  public void editLayer()
  {
    cancelService();
    cancelGroup();
    MapDocument map = MapBean.getInstance().getMap();
    Layer layer = (Layer)getValue("#{layer}");
    layerIndex = (Integer)getValue("#{index}");
    editingLayer = map.createLayer();
    editingLayer.setTo(layer);
    selectedTabIndex = 3;
  }

  public void moveLayerUp()
  {
    int index = (Integer)getValue("#{index}");
    if (index > 0)
    {
      MapDocument map = MapBean.getInstance().getMap();
      List<Layer> layers = map.getLayers();
      Layer layer1 = layers.get(index);
      Layer layer2 = layers.get(index - 1);
      layers.set(index, layer2);
      layers.set(index - 1, layer1);
    }
    cancelLayer();
    selectedTabIndex = 3;
  }

  public void moveLayerDown()
  {
    MapDocument map = MapBean.getInstance().getMap();
    List<Layer> layers = map.getLayers();
    int index = (Integer)getValue("#{index}");
    if (index < layers.size() - 1)
    {
      Layer layer1 = layers.get(index);
      Layer layer2 = layers.get(index + 1);
      layers.set(index, layer2);
      layers.set(index + 1, layer1);
    }
    cancelLayer();
    selectedTabIndex = 3;
  }

  public void saveLayer()
  {
    if (editingLayer.getNames().isEmpty())
    {
      error("LAYER_NAME_IS_MANDATORY");
    }
    else
    {
      if (editingLayer.isBaseLayer())
      {
        prepareBaseLayer(editingLayer);
      }
      MapDocument map = MapBean.getInstance().getMap();
      if (layerIndex == -1)
      {
        map.getLayers().add(editingLayer);
      }
      else
      {
        map.getLayers().get(layerIndex).setTo(editingLayer);
      }
      endLayerEdition();
    }
    selectedTabIndex = 3;
  }

  public void cancelLayer()
  {
    endLayerEdition();
  }

  public void removeLayer()
  {
    int index = (Integer)getValue("#{index}");
    if (index >= 0)
    {
      MapDocument map = MapBean.getInstance().getMap();
      map.getLayers().remove(index);
    }
    selectedTabIndex = 3;
  }

  public String getLayerNames()
  {
    Layer layer = (Layer)getValue("#{layer}");
    String names = layer.getNamesString();
    if (!StringUtils.isBlank(names))
    {
      names = names.replaceAll(",", ", ");
    }
    return names;
  }

  public String getLayerStyles()
  {
    Layer layer = (Layer)getValue("#{layer}");
    String styles = layer.getStylesString();
    if (!StringUtils.isBlank(styles))
    {
      styles = styles.replaceAll(",", ", ");
    }
    return styles;
  }


// groups actions

  public int getGroupCount()
  {
    MapDocument map = MapBean.getInstance().getMap();
    return map.getGroups().size();
  }

  public int getGroupIndex()
  {
    return groupIndex;
  }

  public void addGroup()
  {
    MapDocument map = MapBean.getInstance().getMap();
    previousGroup = null;
    groupIndex = map.getGroups().size();
    map.getGroups().add(map.createGroup());
    selectedTabIndex = 2;
  }

  public void editGroup()
  {
    cancelGroup();
    MapDocument map = MapBean.getInstance().getMap();
    previousGroup = map.createGroup();
    groupIndex = (Integer)getValue("#{index}");
    previousGroup.setTo(map.getGroups().get(groupIndex));
    selectedTabIndex = 2;
  }

  public void removeGroup()
  {
    cancelGroup();
    int index = (Integer)getValue("#{index}");
    MapDocument map = MapBean.getInstance().getMap();
    map.getGroups().remove(index);
    selectedTabIndex = 2;
  }

  public void saveGroup()
  {
    MapDocument map = MapBean.getInstance().getMap();
    Group group = map.getGroups().get(groupIndex);
    if (StringUtils.isBlank(group.getName()))
    {
      error("GROUP_NAME_IS_MANDATORY");
    }
    else
    {
      endGroupEdition();
    }
    selectedTabIndex = 2;
  }

  public void cancelGroup()
  {
    if (groupIndex != -1)
    {
      MapDocument map = MapBean.getInstance().getMap();
      if (previousGroup == null)
      {
        map.getGroups().remove(groupIndex);
      }
      else
      {
        Group group = map.getGroups().get(groupIndex);
        group.setTo(previousGroup);
      }
    }
    endGroupEdition();
  }

  public void moveGroupUp()
  {
    int index = (Integer)getValue("#{index}");
    if (index > 0)
    {
      MapDocument map = MapBean.getInstance().getMap();
      List<Group> groups = map.getGroups();
      Group group1 = groups.get(index);
      Group group2 = groups.get(index - 1);
      groups.set(index, group2);
      groups.set(index - 1, group1);
    }
    selectedTabIndex = 2;
  }

  public void moveGroupDown()
  {
    MapDocument map = MapBean.getInstance().getMap();
    List<Group> groups = map.getGroups();
    int index = (Integer)getValue("#{index}");
    if (index < groups.size() - 1)
    {
      Group group1 = groups.get(index);
      Group group2 = groups.get(index + 1);
      groups.set(index, group2);
      groups.set(index + 1, group1);
    }
    selectedTabIndex = 2;
  }

  // info layers actions

  public int getInfoLayerCount()
  {
    MapDocument map = MapBean.getInstance().getMap();
    return map.getInfoLayers().size();
  }

  public int getInfoLayerIndex()
  {
    return infoLayerIndex;
  }

  public InfoLayer getEditingInfoLayer()
  {
    return previousInfoLayer;
  }

  public void addAllInfoLayers()
  {
    MapDocument map = MapBean.getInstance().getMap();
    for (Layer layer : map.getLayers())
    {
      if (layer.isLocatable())
      {
        List<String> layerNames = layer.getNames();
        for (String name : layerNames)
        {
          int index = name.indexOf(":");
          if (index != -1) name = name.substring(index + 1);
          if (map.getInfoLayer(name) == null)
          {
            InfoLayer infoLayer = map.createInfoLayer();
            infoLayer.setName(name);
            infoLayer.setHighlight(true);
            map.getInfoLayers().add(infoLayer);
          }
        }
      }
    }
  }

  public void addInfoLayer()
  {
    MapDocument map = MapBean.getInstance().getMap();
    previousInfoLayer = null;
    infoLayerIndex = map.getInfoLayers().size();
    map.getInfoLayers().add(map.createInfoLayer());
  }

  public void editInfoLayer()
  {
    cancelInfoLayer();
    MapDocument map = MapBean.getInstance().getMap();
    previousInfoLayer = map.createInfoLayer();
    infoLayerIndex = (Integer)getValue("#{index}");
    previousInfoLayer.setTo(map.getInfoLayers().get(infoLayerIndex));
  }

  public void removeInfoLayer()
  {
    cancelInfoLayer();
    MapDocument map = MapBean.getInstance().getMap();
    int index = (Integer)getValue("#{index}");
    map.getInfoLayers().remove(index);
    selectedTabIndex = 4;
  }

  public void saveInfoLayer()
  {
    MapDocument map = MapBean.getInstance().getMap();
    InfoLayer infoLayer = map.getInfoLayers().get(infoLayerIndex);
    if (StringUtils.isBlank(infoLayer.getName()))
    {
      error("INFO_LAYER_NAME_IS_MANDATORY");
    }
    else
    {
      endInfoLayerEdition();
    }
    selectedTabIndex = 4;
  }

  public void cancelInfoLayer()
  {
    if (infoLayerIndex != -1)
    {
      MapDocument map = MapBean.getInstance().getMap();
      if (previousInfoLayer == null)
      {
        map.getInfoLayers().remove(infoLayerIndex);
      }
      else
      {
        InfoLayer infoLayer = map.getInfoLayers().get(infoLayerIndex);
        infoLayer.setTo(previousInfoLayer);
      }
    }
    endInfoLayerEdition();
    selectedTabIndex = 4;
  }

  public void moveInfoLayerUp()
  {
    int index = (Integer)getValue("#{index}");
    if (index > 0)
    {
      MapDocument map = MapBean.getInstance().getMap();
      List<InfoLayer> infoLayers = map.getInfoLayers();
      InfoLayer infoLayer1 = infoLayers.get(index);
      InfoLayer infoLayer2 = infoLayers.get(index - 1);
      infoLayers.set(index, infoLayer2);
      infoLayers.set(index - 1, infoLayer1);
    }
    selectedTabIndex = 4;
  }

  public void moveInfoLayerDown()
  {
    MapDocument map = MapBean.getInstance().getMap();
    List<InfoLayer> infoLayers = map.getInfoLayers();
    int index = (Integer)getValue("#{index}");
    if (index < infoLayers.size() - 1)
    {
      InfoLayer infoLayer1 = infoLayers.get(index);
      InfoLayer infoLayer2 = infoLayers.get(index + 1);
      infoLayers.set(index, infoLayer2);
      infoLayers.set(index + 1, infoLayer1);
    }
    selectedTabIndex = 4;
  }

  // Properties actions

  public int getPropertyIndex()
  {
    return propertyIndex;
  }

  public List<String> getPropertyNames()
  {
    if (propertyNames == null)
    {
      MapDocument map = MapBean.getInstance().getMap();
      Set<String> names = map.getProperties().keySet();
      propertyNames = new ArrayList<String>(names);
      Collections.sort(propertyNames);
      if ("".equals(editingPropertyName))
      {
        propertyNames.add("");
        propertyIndex = propertyNames.size() - 1;
      }
    }
    return propertyNames;
  }

  public String getPropertyValue()
  {
    MapDocument map = MapBean.getInstance().getMap();
    String propertyName = (String)getValue("#{propertyName}");
    return map.getProperties().get(propertyName);
  }

  public String getEditingPropertyName()
  {
    return editingPropertyName;
  }

  public void setEditingPropertyName(String editingPropertyName)
  {
    this.editingPropertyName = editingPropertyName;
  }

  public String getEditingPropertyValue()
  {
    return editingPropertyValue;
  }

  public void setEditingPropertyValue(String editingPropertyValue)
  {
    this.editingPropertyValue = editingPropertyValue;
  }

  public void editProperty()
  {
    cancelProperty();
    MapDocument map = MapBean.getInstance().getMap();
    editingPropertyName = (String)getValue("#{propertyName}");
    editingPropertyValue = map.getProperties().get(editingPropertyName);
    propertyIndex = (Integer)getValue("#{index}");
    selectedTabIndex = 5;
  }

  public void removeProperty()
  {
    cancelProperty();
    MapDocument map = MapBean.getInstance().getMap();
    String propertyName = (String)getValue("#{propertyName}");
    map.getProperties().remove(propertyName);
    selectedTabIndex = 5;
  }

  public void saveProperty()
  {
    MapDocument map = MapBean.getInstance().getMap();
    if (!StringUtils.isBlank(editingPropertyName))
      map.getProperties().put(editingPropertyName, editingPropertyValue);
    endPropertyEdition();
    selectedTabIndex = 5;
  }

  public void cancelProperty()
  {
    endPropertyEdition();
    selectedTabIndex = 5;
  }

  public void addProperty()
  {
    editingPropertyName = "";
    propertyNames = null;
    selectedTabIndex = 5;
  }

  public void addDefaultProperties()
  {
    endPropertyEdition();
    MapDocument map = MapBean.getInstance().getMap();
    java.util.Map<String, String> properties = map.getProperties();
    if (!properties.containsKey(MapBean.LEFT_PANEL_VISIBLE_PROPERTY))
    {
      properties.put(MapBean.LEFT_PANEL_VISIBLE_PROPERTY, "false");
    }
    if (!properties.containsKey(MapBean.RIGHT_PANEL_VISIBLE_PROPERTY))
    {
      properties.put(MapBean.RIGHT_PANEL_VISIBLE_PROPERTY, "true");
    }
    if (!properties.containsKey(MapBean.LEFT_PANEL_WIDTH_PROPERTY))
    {
      properties.put(MapBean.LEFT_PANEL_WIDTH_PROPERTY, "290px");
    }
    if (!properties.containsKey(MapBean.RIGHT_PANEL_WIDTH_PROPERTY))
    {
      properties.put(MapBean.RIGHT_PANEL_WIDTH_PROPERTY, "290px");
    }
    if (!properties.containsKey(MapBean.EXPAND_GROUPS_PROPERTY))
    {
      properties.put(MapBean.EXPAND_GROUPS_PROPERTY, "false");
    }
    if (!properties.containsKey(MapBean.SCRIPT_FILES_PROPERTY))
    {
      properties.put(MapBean.SCRIPT_FILES_PROPERTY, "script_name");
    }
    if (!properties.containsKey(MapBean.FEATURE_LOCATORS_PROPERTY))
    {
      properties.put(MapBean.FEATURE_LOCATORS_PROPERTY, "locator_name");
    }
    if (!properties.containsKey(MapBean.EXPORT_FORMATS_PROPERTY))
    {
      properties.put(MapBean.EXPORT_FORMATS_PROPERTY, "shape-zip,csv,GML3");
    }
    if (!properties.containsKey(MapBean.SEARCH_ON_LOAD_PROPERTY))
    {
      properties.put(MapBean.SEARCH_ON_LOAD_PROPERTY, "false");
    }
    if (!properties.containsKey(MapBean.PRINT_REPORTS_PROPERTY))
    {
      properties.put(MapBean.PRINT_REPORTS_PROPERTY, "sample_a4,sample_a3");
    }
    if (!properties.containsKey(MapBean.MAP_BACKGROUND_PROPERTY))
    {
      properties.put(MapBean.MAP_BACKGROUND_PROPERTY, "white");
    }
    if (!properties.containsKey(MapBean.CSS_THEME_PROPERTY))
    {
      properties.put(MapBean.CSS_THEME_PROPERTY, "default");
    }
    selectedTabIndex = 5;
  }


  // Roles actions
  public void addReadRole()
  {
    if (!StringUtils.isBlank(readRole))
    {
      MapDocument map = MapBean.getInstance().getMap();
      List<String> readRoles = map.getReadRoles();
      if (!readRoles.contains(readRole))
      {
        readRoles.add(readRole);
      }
    }
    readRole = null;
    selectedTabIndex = 6;
  }

  public void removeReadRole()
  {
    String roleId = (String)getValue("#{role}");
    MapDocument map = MapBean.getInstance().getMap();
    map.getReadRoles().remove(roleId);
    selectedTabIndex = 6;
  }

  public void addWriteRole()
  {
    if (!StringUtils.isBlank(writeRole))
    {
      MapDocument map = MapBean.getInstance().getMap();
      List<String> readRoles = map.getReadRoles();
      if (!readRoles.contains(writeRole))
      {
        readRoles.add(writeRole);
      }
      List<String> writeRoles = map.getWriteRoles();
      if (!writeRoles.contains(writeRole))
      {
        writeRoles.add(writeRole);
      }
    }
    writeRole = null;
    selectedTabIndex = 6;
  }

  public void removeWriteRole()
  {
    String roleId = (String)getValue("#{role}");
    MapDocument map = MapBean.getInstance().getMap();
    map.getWriteRoles().remove(roleId);
    selectedTabIndex = 6;
  }

  public int getRoleCount()
  {
    MapDocument map = MapBean.getInstance().getMap();
    return map.getReadRoles().size() + map.getWriteRoles().size();
  }

  public int getPropertyCount()
  {
    MapDocument map = MapBean.getInstance().getMap();
    return map.getProperties().size();
  }
  
  // Map actions
  public String editMap()
  {
    endEdition();
    return "map_editor";
  }

  public String showMap()
  {
    MapViewerBean mapViewerBean = (MapViewerBean)getValue("#{mapViewerBean}");
    return mapViewerBean.show();
  }

  public void newMap()
  {
    endEdition();
    selectedTabIndex = 0;
    MapBean mapBean = MapBean.getInstance();
    mapBean.newMap();
  }

  public void copyMap()
  {
    endEdition();
    MapBean mapBean = MapBean.getInstance();
    MapDocument map = mapBean.getMap();
    map.setDocId(null);
    map.setName(null);
    map.setThumbnailDocId(null);
    map.setChangeUserId(null);
    map.setChangeDateTime(null);
    info("MAP_COPIED");
  }

  public String reloadMap()
  {
    try
    {
      cancelEdition();
      MapBean mapBean = MapBean.getInstance();
      mapBean.reloadMap();
      propertyNames = null;
      info("MAP_RELOADED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "map_editor";
  }

  public void saveMap()
  {
    try
    {
      cancelEdition();
      MapBean mapBean = MapBean.getInstance();
      mapBean.storeMap(true);
      info("MAP_SAVED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void fastSaveMap()
  {
    try
    {
      cancelEdition();
      MapBean mapBean = MapBean.getInstance();
      mapBean.storeMap(false);
      info("MAP_SAVED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void deleteMap()
  {
    try
    {
      MapBean mapBean = MapBean.getInstance();
      mapBean.deleteMap();
      info("MAP_DELETED");
      endEdition();
      newMap();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  // sld

  public String editSLD()
  {
    Map.Layer layer = (Map.Layer)getValue("#{layer}");
    String sld = layer.getSld();
    SLDEditorBean sldEditorBean = (SLDEditorBean)getBean("sldEditorBean");
    sldEditorBean.setServiceUrl(layer.getService().getUrl());
    selectedTabIndex = 3;
    return sldEditorBean.editSLD(sld, layer.getNames(), layer.getStyles());
  }

  // private methods

  private void addScriptFile(String filename, StringBuilder buffer)
  {
    String contextPath = MatrixConfig.getProperty("contextPath");
    buffer.append("<script src=\"").append(contextPath);
    buffer.append("/plugins/mapviewer/");
    buffer.append(filename);
    buffer.append("\" type=\"text/javascript\">\n</script>\n");
  }

  private void cancelEdition()
  {
    cancelService();
    cancelGroup();
    cancelLayer();
    cancelInfoLayer();
    cancelProperty();
  }

  private void endEdition()
  {
    endServiceEdition();
    endGroupEdition();
    endLayerEdition();
    endInfoLayerEdition();
    endPropertyEdition();
  }

  private void endServiceEdition()
  {
    previousService = null;
    serviceIndex = -1;
  }

  private void endGroupEdition()
  {
    groupIndex = -1;
    previousGroup = null;
  }

  private void endLayerEdition()
  {
    editingLayer = null;
    layerIndex = -1;
  }

  private void endInfoLayerEdition()
  {
    infoLayerIndex = -1;
    previousInfoLayer = null;
  }

  private void endPropertyEdition()
  {
    editingPropertyName = null;
    editingPropertyValue = null;
    propertyNames = null;
    propertyIndex = -1;
  }

  private void prepareBaseLayer(Layer layer)
  {
    layer.setOnLegend(true);
    layer.setIndependent(true);
    layer.getEditRoles().clear();
    layer.setSnap(false);
  }
}
