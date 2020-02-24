package org.santfeliu.misc.mapviewer.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.UUID;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.Property;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.security.SecurityConstants;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.form.FormFactory;
import org.santfeliu.misc.mapviewer.Map;
import org.santfeliu.misc.mapviewer.Map.Group;
import org.santfeliu.misc.mapviewer.Map.InfoLayer;
import org.santfeliu.misc.mapviewer.MapDocument;
import org.santfeliu.misc.mapviewer.Map.Layer;
import org.santfeliu.misc.mapviewer.Map.Service;
import org.santfeliu.misc.mapviewer.SLDStore;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;
import org.santfeliu.util.enc.HtmlEncoder;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;

/**
 *
 * @author realor
 */
@CMSManagedBean
public class MapViewerBean extends WebBean
{
  @CMSProperty
  public static final String MAP_NAME_PROPERTY = "mapName";
  @CMSProperty
  public static final String MAP_CATALOGUE_MID_PROPERTY = "mapCatalogueMid";
  @CMSProperty
  public static final String PRINT_MID_PROPERTY = "printMid";
  @CMSProperty
  public static final String PRINT_REPORTS_PROPERTY = "printReports";
  @CMSProperty
  public static final String METADATA_FORM_SELECTOR_PROPERTY = 
    "metadataFormSelector";

  public static final String MAP_NAME_PARAMETER = "map_name";
  public static final String LAYER_VISIBILITY_PARAMETER = "layer_visibility";
  public static final String FEATURE_LOCATOR_PARAMETER = "feature_locator";

  public static final String JS_DOCUMENT_TYPE  = "CODE";
  public static final String JS_PROPERTY_NAME  = "workflow.js";

  public static final int SCRIPT_VERSION = 4;
  private transient SLDCache sldCache = new SLDCache();

  public String getScripts()
  {
    StringBuilder buffer = new StringBuilder();
    addBundleFile(buffer);
    addScriptFile("/plugins/mapviewer/OpenLayers.js", buffer);
    addScriptFile("/plugins/mapviewer/Controls.js", buffer);
    addScriptFile("/plugins/mapviewer/MapViewer.js", buffer);
    addScriptFile("/plugins/mapviewer/dxf-parser.js", buffer);
    boolean searchEnabled = addScriptFiles(buffer);
    addScript(searchEnabled, buffer);
    return buffer.toString();
  }

  public String getLegend()
  {
    StringBuilder buffer = new StringBuilder();
    MapBean mapBean = MapBean.getInstance();
    Map map = mapBean.getMap();
    List<Layer> layers = map.getLayers();
    int baseLayerCount = 0;
    for (Layer layer : layers)
    {
      if (layer.isBaseLayer() && layer.isOnLegend()) baseLayerCount++;
    }
    boolean expandGroups = "true".equals(map.getProperties().get(
      MapBean.EXPAND_GROUPS_PROPERTY));
    if (baseLayerCount > 1)
    {
      buffer.append("<fieldset>")
        .append("<legend>" + translate("Base layer" ,"map:" + map.getName()) + "</legend>")
        .append("<ul class=\"baseLayers\">");
      addLayersToLegend(map, null, true, expandGroups, buffer);
      for (Group group : map.getGroups())
      {
        addLayersToLegend(map, group, true, expandGroups, buffer);
      }
      buffer.append("</ul>");
      buffer.append("</fieldset>");
    }
    buffer.append("<fieldset>")
      .append("<legend>" + translate("Overlays" ,"map:" + map.getName()) + "</legend>")
      .append("<ul class=\"baseLayers\">");
    
    buffer.append("<li><ul class=\"overlays\">");
    addLayersToLegend(map, null, false, expandGroups, buffer);
    for (Group group : map.getGroups())
    {
      addLayersToLegend(map, group, false, expandGroups, buffer);
    }
    buffer.append("</ul></li>");
    buffer.append("</ul>");
    buffer.append("</fieldset>");    
    return buffer.toString();
  }

  public String getLayerLegendURL(Layer layer)
  {
    String name = layer.getNames().get(0);
    MapBean mapBean = MapBean.getInstance();
    String baseUrl = mapBean.getBaseUrl();

    String url = baseUrl + "/proxy?url=" + layer.getService().getUrl() +
    "&service=WMS&version=1.0.0&request=GetLegendGraphic&layer=" + name +
    "&format=image/png&width=24&strict=false";
    String sld = layer.getSld();
    if (!StringUtils.isBlank(sld))
    {
      url += "&sld=" + SLDStore.getSldURL(sld);
    }
    List<String> styles = layer.getStyles();
    if (!styles.isEmpty())
    {
      String style = styles.get(0);
      if (!StringUtils.isBlank(style))
      {
        try
        {
          style = URLEncoder.encode(style, "UTF-8");
        }
        catch (UnsupportedEncodingException ex)
        {
        }
        url += "&style=" + style;
      }
    }
    return url;
  }

  public boolean isEditionEnabled()
  {
    MapBean mapBean = MapBean.getInstance();
    MapDocument map = mapBean.getMap();
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return userSessionBean.isUserInRole("DOC_ADMIN") ||
      userSessionBean.isUserInRole("GIS_ADMIN") ||
      userSessionBean.isUserInRole(map.getWriteRoles());
  }

  public boolean isCatalogueVisible()
  {
    return getProperty(MAP_NAME_PROPERTY) == null;
  }

  // actions
  @CMSAction
  public String show()
  {
    return showMap();
  }

  @CMSAction
  public String showMap()
  {
    // look for map name specified by url parameter
    ExternalContext externalContext = getFacesContext().getExternalContext();
    java.util.Map parameters = externalContext.getRequestParameterMap();
    String mapName = (String)parameters.get(MAP_NAME_PARAMETER);
    if (mapName == null)
    {
      // look for map name specified in node
      mapName = getProperty(MAP_NAME_PROPERTY);
    }
    if (mapName != null)
    {
      return showMap(mapName);
    }
    else
    {
      MapBean mapBean = MapBean.getInstance();
      if (!mapBean.getMap().isComplete()) // no renderable map, show catalogue
      {
        return showCatalogue();
      }
      else
      {
        // current map
        return "map_viewer";
      }
    }
  }

  public String showMap(String mapName)
  {
    try
    {
      registerAccess(mapName, getFacesContext().getExternalContext());            
      MapBean mapBean = MapBean.getInstance();
      mapBean.loadMap(mapName);
      MapEditorBean mapEditorBean = (MapEditorBean)getBean("mapEditorBean");
      mapEditorBean.setSelectedTabIndex(0);
      SLDEditorBean sldEditorBean = (SLDEditorBean)getBean("sldEditorBean");
      sldEditorBean.clear();
      return "map_viewer";
    }
    catch (Exception ex)
    {
      error(ex);
      return "map_error";
    } 
  }

  public String showCatalogue()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String mid = userSessionBean.getSelectedMenuItem().getProperty(
      MAP_CATALOGUE_MID_PROPERTY);
    if (mid != null)
    {
      MenuItemCursor menuItem =
        userSessionBean.getMenuModel().getMenuItem(mid);
      if (!menuItem.isNull()) menuItem.select();
    }
    MapCatalogueBean mapCatalogueBean = 
      (MapCatalogueBean)getBean("mapCatalogueBean");
    return mapCatalogueBean.refresh();
  }

  public void refreshForms()
  {
    FormFactory formFactory = FormFactory.getInstance();
    MapDocument map = MapBean.getInstance().getMap();
    for (InfoLayer infoLayer : map.getInfoLayers())
    {
      String formSelector = infoLayer.getFormSelector();
      if (StringUtils.isBlank(formSelector))
      {
        formSelector = "form:" + infoLayer.getName();
      }
      formFactory.clearForm(formSelector);
    }
    String formSelector = 
      getProperty(MapViewerBean.METADATA_FORM_SELECTOR_PROPERTY);
    if (formSelector != null)
    {
      formFactory.clearForm(formSelector);      
    }
  }

  // private methods

  private void addScriptFile(String filename, StringBuilder buffer)
  {
    String contextPath = MatrixConfig.getProperty("contextPath");
    buffer.append("<script src=\"").append(contextPath);
    buffer.append(filename);
    buffer.append("?version=");
    buffer.append(SCRIPT_VERSION);
    buffer.append("\" type=\"text/javascript\">\n</script>\n");
  }

  private boolean addScriptFiles(StringBuilder buffer)
  {
    boolean searchEnabled = false;
    MapBean mapBean = MapBean.getInstance();
    MapDocument map = mapBean.getMap();

    StringBuilder scriptsBuffer = new StringBuilder();
    String value = map.getProperties().get(MapBean.SCRIPT_FILES_PROPERTY);
    if (value != null) scriptsBuffer.append(value);
    value = map.getProperties().get(MapBean.FEATURE_LOCATORS_PROPERTY);
    if (value != null)
    {
      if (scriptsBuffer.length() > 0) scriptsBuffer.append(",");
      scriptsBuffer.append(value);
    }
    if (scriptsBuffer.length() > 0)
    {
      DocumentFilter filter = new DocumentFilter();
      Property property = new Property();

      property.setName(JS_PROPERTY_NAME);
      filter.getProperty().add(property);
      StringTokenizer tokenizer = 
        new StringTokenizer(scriptsBuffer.toString(), ",");
      while (tokenizer.hasMoreTokens())
      {
        String filename = tokenizer.nextToken().trim();
        if (filename.length() > 0)
        {
          property.getValue().add(filename);
        }
      }
      try
      {
        UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
        String userId = userSessionBean.getUserId();
        String password = userSessionBean.getPassword();
        DocumentManagerClient client = 
          new DocumentManagerClient(userId, password);
        filter.setDocTypeId(JS_DOCUMENT_TYPE);
        filter.getOutputProperty().add(JS_PROPERTY_NAME);
        List<Document> documents = client.findDocuments(filter);
        HashMap<String, String> documentMap = new HashMap<String, String>();
        for (Document document : documents)
        {
          String locatorName = DocumentUtils.getPropertyValue(document,
            JS_PROPERTY_NAME);
          String contentId = document.getContent().getContentId();
          documentMap.put(locatorName, contentId);
        }
        for (String locatorName : property.getValue())
        {
          String contentId = documentMap.get(locatorName);
          if (contentId != null)
          {
            String path = "/documents/" + contentId + "/" + locatorName + ".js";
            addScriptFile(path, buffer);
            searchEnabled = true;
          }
        }
      }
      catch (Exception ex)
      {
      }
    }
    return searchEnabled;
  }

  private void addScript(boolean searchEnabled, StringBuilder buffer)
  {
    MapBean mapBean = MapBean.getInstance();
    MapDocument map = mapBean.getMap();

    buffer.append("<script type=\"text/javascript\">\n");

    /* baseUrl */
    buffer.append("var baseUrl = \"").
      append(mapBean.getBaseUrl()).append("\";\n");
    /* srs */
    buffer.append("var srs = \"").append(map.getSrs()).append("\";\n");
    /* maxBounds */
    buffer.append("var maxBounds = new OpenLayers.Bounds(").
      append(map.getBounds().toString()).append(");\n");
    /* mapName */
    buffer.append("var mapName = \"").append(map.getName()).append("\";\n");
    /* mapTitle */
    String title = translate(map.getTitle(), map.getName());
    title = title.replace("\"", "\\\"");
    buffer.append("var mapTitle = \"").append(title).append("\";\n");
    /* language */
    String language = 
      FacesContext.getCurrentInstance().getViewRoot().getLocale().getLanguage();
    buffer.append("var language = \"").append(language).append("\";\n");
    /* searchEnabled */
    buffer.append("var searchEnabled = ").append(searchEnabled).append(";\n");
    /* currentMid */
    String currentMid = getSelectedMenuItem().getMid();
    buffer.append("var currentMid = ").append(currentMid).append(";\n");
    String printMid = getProperty(PRINT_MID_PROPERTY);
    buffer.append("var printMid = ").append(printMid).append(";\n");

    java.util.Map<String, String> properties = map.getProperties();
    /* leftPanelWidth */
    String leftPanelWidth = properties.get(MapBean.LEFT_PANEL_WIDTH_PROPERTY);
    if (leftPanelWidth == null) leftPanelWidth = "290px";
    buffer.append("var leftPanelWidth = \"").
      append(leftPanelWidth).append("\";\n");
    /* rightPanelWidth */
    String rightPanelWidth = properties.get(MapBean.RIGHT_PANEL_WIDTH_PROPERTY);
    if (rightPanelWidth == null) rightPanelWidth = "290px";
    buffer.append("var rightPanelWidth = \"").
      append(rightPanelWidth).append("\";\n");
    /* leftPanelVisible */
    boolean leftPanelVisible =
      "true".equals(properties.get(MapBean.LEFT_PANEL_VISIBLE_PROPERTY));
    buffer.append("var leftPanelVisible = ").
      append(leftPanelVisible).append(";\n");
    /* rightPanelVisible */
    boolean rightPanelVisible =
      "true".equals(properties.get(MapBean.RIGHT_PANEL_VISIBLE_PROPERTY));
    buffer.append("var rightPanelVisible = ").
      append(rightPanelVisible).append(";\n");
    /* searchOnLoad */
    boolean searchOnLoad =
      "true".equals(properties.get(MapBean.SEARCH_ON_LOAD_PROPERTY));
    buffer.append("var searchOnLoad = ").append(searchOnLoad).append(";\n");
    /* printReports */
    String printReports =
      properties.get(MapBean.PRINT_REPORTS_PROPERTY);
    if (printReports == null)
      printReports = getProperty(PRINT_REPORTS_PROPERTY);
    if (printReports != null && !printReports.equals("null"))
      printReports = "\"" + printReports + "\"";
    buffer.append("var printReports = ").
      append(printReports).append(";\n");
    /* isPublic */
    boolean isPublic = 
      map.getReadRoles().contains(SecurityConstants.EVERYONE_ROLE);
    buffer.append("var isPublic = ").append(isPublic).append(";\n");

    /* mapBackground */
    String mapBackground = properties.get(MapBean.MAP_BACKGROUND_PROPERTY);
    if (StringUtils.isBlank(mapBackground))
    {
      mapBackground = "white";
    }
    buffer.append("var mapBackground = '").append(mapBackground).append("';\n");

    /* cssTheme */
    String cssTheme = properties.get(MapBean.CSS_THEME_PROPERTY);
    if (StringUtils.isBlank(cssTheme))
    {
      cssTheme = "default";
    }
    buffer.append("var cssTheme = '").append(cssTheme).append("';\n");
    
    buffer.append("var exportFormats = [");
    String exportFormats =
      properties.get(MapBean.EXPORT_FORMATS_PROPERTY);
    if (!StringUtils.isBlank(exportFormats))
    {
      String[] formats = exportFormats.split(",");
      for (int i = 0; i < formats.length; i++)
      {
        String format = formats[i];
        if (i > 0) buffer.append(", ");
        buffer.append("\"").append(format).append("\"");
      }
    }
    buffer.append("];\n");

    if (!map.getServices().isEmpty())
    {
      for (Service service : map.getServices())
      {
        String url = service.getUrl();
        buffer.append("wmsUrls.push(\"").append(url).append("\");\n");
      }
      for (Layer layer : map.getLayers())
      {
        addLayer(map, layer, buffer);
      }
    }
    addInfoLayers(buffer, map);
    buffer.append("</script>\n");
  }

  private void addBundleFile(StringBuilder buffer)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Locale locale = context.getViewRoot().getLocale();
    String language = locale.getLanguage();
    if (!language.equals("ca") &&
        !language.equals("en") &&
        !language.equals("es"))
    {
      language = "ca"; // defaultLanguage;
    }
    addScriptFile("/plugins/mapviewer/Bundle_" + language + ".js", buffer);
  }

  private void addLayer(MapDocument map, Layer layer, StringBuilder buffer)
  {
    buffer.append("addLayer(");
    buffer.append(parameter(layer.getLabel())).append(", ");
    Service service = layer.getService();
    int serviceIndex = map.getServices().indexOf(service);
    buffer.append(serviceIndex).append(", ");
    buffer.append(parameter(layer.getNamesString())).append(", ");
    buffer.append(layer.isBaseLayer()).append(", ");
    String groupName = layer.getGroup() == null ?
      null : layer.getGroup().getName();
    buffer.append(parameter(groupName)).append(", ");
    String sldUrl = sldCache.getSldUrl(layer.getSld());
    buffer.append(parameter(sldUrl)).append(", ");
    buffer.append(parameter(layer.getStylesString())).append(", ");
    buffer.append(parameter(layer.getFormat())).append(", ");
    buffer.append(parameter(layer.getCqlFilter())).append(", ");
    buffer.append(isLayerViewable(layer) && isLayerVisible(layer)).append(", ");
    buffer.append(layer.isLocatable()).append(", ");
    buffer.append(isLayerEditable(layer)).append(", ");
    buffer.append(layer.isSnap()).append(", ");
    buffer.append(layer.isIndependent()).append(", ");
    buffer.append(layer.getOpacity()).append(", ");
    buffer.append(layer.isTransparentBackground()).append(");\n");
  }

  private void addInfoLayers(StringBuilder buffer, MapDocument map)
  {
    for (InfoLayer infoLayer : map.getInfoLayers())
    {
      buffer.append("addInfoLayer(\"");
      buffer.append(infoLayer.getName());
      buffer.append("\", ");
      buffer.append(parameter(infoLayer.getFormSelector()));
      buffer.append(", ");
      buffer.append(infoLayer.isHighlight());
      buffer.append(");\n");
    }
  }
  
  private void registerAccess(String mapName, ExternalContext context)
  {
    String value = 
      MatrixConfig.getProperty("org.santfeliu.mapviewer.logAccess");
    if (!"true".equals(value)) return;
    try
    {
      Context initContext = new InitialContext();
      Context envContext  = (Context)initContext.lookup("java:/comp/env");
      DataSource ds = (DataSource)envContext.lookup("jdbc/matrix");
      Connection conn = ds.getConnection();
      try
      {
        String id = UUID.randomUUID().toString();
        String userId = UserSessionBean.getCurrentInstance().getUserId();
        String dateTime = TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");
        String ip = ((HttpServletRequest)context.getRequest()).getRemoteAddr();
        PreparedStatement stmt = conn.prepareStatement(
          "insert into log_mapviewer(id, datetime, userid, ip, mapName) " +
          "values (?,?,?,?,?)");
        try
        {
          stmt.setString(1, id);
          stmt.setString(2, dateTime);
          stmt.setString(3, userId);
          stmt.setString(4, ip);
          stmt.setString(5, mapName);
          stmt.executeUpdate();
        }
        finally
        {
          stmt.close();
        }
      }
      finally
      {
        conn.close();
      }      
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      // ignore
    }
  }

  private void addLayersToLegend(Map map, Group group, boolean baseLayers,
    boolean expandGroups, StringBuilder bufferOut)
  {
    StringBuilder buffer = new StringBuilder();
    boolean render = false;
    List<Layer> groupLayers = map.getLayers(group, baseLayers);
    if (!groupLayers.isEmpty())
    {
      buffer.append("<li class=\"group\">");
      if (group == null)
      {
        buffer.append("<ul>");
      }
      else
      {        
        buffer.append("<div>");
        String groupName = group.getName();

        // expand/collapase group button
        String buttonClass = expandGroups ? "collapse" : "expand";
        buffer.append("<a title=\"").append(getSwitchGroupTitle(group, map)). 
          append("\" class=\"").append(buttonClass).
          append("\" value=\"#\" onclick=\"groupClicked('").
          append(groupName).append("',this);\"></a>");

        if (!baseLayers)
        {
          // checkbox input
          buffer.append("<input id=\"check_group_");
          buffer.append(groupName);
          buffer.append("\" type=\"checkbox\" onclick=\"switchGroup(\'").
            append(groupName).append("');\">");
        }
        // label for checkbox
        buffer.append("<label for=\"check_group_");
        buffer.append(groupName);
        buffer.append("\">");
        buffer.append(HtmlEncoder.encode(
          translate(group.getLabel(), "map:" + map.getName())));
        buffer.append("</label>");
        buffer.append("</div>");

        buffer.append("<ul id=\"group_").append(groupName).append("\"");
        if (!expandGroups) buffer.append(" style=\"display:none\"");
        buffer.append(">");
      }
      for (Layer layer : groupLayers)
      {
        if (layer.isOnLegend() && isLayerViewable(layer))
        {
          render = true;
          int layerId = layer.getId();
          buffer.append("<li class=\"item\">");
          if (baseLayers)
          {
            // radio button
            buffer.append("<input id=\"layer_").append(layerId).
              append("\" type=\"radio\" name=\"baselayer\" ").
              append(isLayerVisible(layer) ? "checked" : "").
              append(" onclick=\"setBaseLayer(").append(layerId).append(")\">");
            buffer.append("<label for=\"layer_").append(layerId).append("\">");
            buffer.append(translate(layer.getLabel(), "map:" + map.getName()));
            buffer.append("</label>");
          }
          else
          {
            // checkbox
            buffer.append("<input type=\"checkbox\" ").
              append(" id=\"layer_").append(layerId).append("\" ").
              append(isLayerVisible(layer) ? "checked" : "").
              append(" onclick=\"switchLayer(").append(layerId).append(")\">");
            // legend graphic
            String legendGraphic = layer.getLegendGraphic();
            if (!StringUtils.isBlank(legendGraphic) &&
              !"auto".equals(legendGraphic) &&
              !"true".equals(legendGraphic) &&
              !"false".equals(legendGraphic))
            {
              // legend graphic is a docId
              String contextPath = MatrixConfig.getProperty("contextPath");
              String imageURI = contextPath + "/documents/" + 
                legendGraphic + "?cache=1000000";
              buffer.append("<img src=\"").append(imageURI).
                append("\" alt=\"\" class=\"inlineLayerIcon\">");
            }
            // label
            boolean editable = isLayerEditable(layer);
            buffer.append("<label for=\"layer_");
            buffer.append(layer.getId());
            buffer.append("\" ");
            if (editable)
            {
              buffer.append("id=\"editlayer_");
              buffer.append(layer.getId());
              buffer.append("\" ");
            }
            buffer.append("class=\"");
            buffer.append(editable ? "editable" : "noeditable");
            buffer.append("\">");
            buffer.append(HtmlEncoder.encode(
              translate(layer.getLabel(), "map:" + map.getName())));
            buffer.append("</label>");

            if (editable)
            {
              buffer.append("<span class=\"editButton\" ").
                append("onclick=\"setEditingLayer(").append(layerId).
                append(")\"></span>");
            }
            if ("auto".equals(legendGraphic) || "true".equals(legendGraphic))
            {
              // paint image under layer name
              buffer.append("<img src=\"").append(getLayerLegendURL(layer)).
                append("\" alt=\"\" class=\"layerIcon\">");
            }
          }
          buffer.append("</li>");
        }
      }
      buffer.append("</ul>");
      buffer.append("</li>");
    }
    if (render) bufferOut.append(buffer);
  }

  private boolean isLayerVisible(Layer layer)
  {
    boolean visible;
    ExternalContext externalContext = getExternalContext();
    java.util.Map parameters = externalContext.getRequestParameterMap();
    String visibility = (String)parameters.get(LAYER_VISIBILITY_PARAMETER);
    if (visibility == null)
    {
      // take default visibility
      visible = layer.isVisible();
    }
    else
    {
      int id = layer.getId();
      if (id < visibility.length())
      {
        visible = visibility.charAt(id) == '1';
      }
      else visible = false;
    }
    return visible;
  }

  private boolean isLayerViewable(Layer layer)
  {
    List<String> viewRoles = layer.getViewRoles();
    if (viewRoles.isEmpty()) return true;
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return userSessionBean.isUserInRole(viewRoles);
  }

  private boolean isLayerEditable(Layer layer)
  {
    List<String> editRoles = layer.getEditRoles();
    if (editRoles.isEmpty()) return false;
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return userSessionBean.isUserInRole(editRoles);
  }

  private String parameter(String value)
  {
    if (StringUtils.isBlank(value)) return "null";
    else return "\"" + value + "\"";
  }

  private String translate(String text, String group)
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return userSessionBean.translate(text, group);
  }

  private String getSwitchGroupTitle(Group group, Map map) 
  {
    return HtmlEncoder.encode(
      translate("Fes clic per expandir o contraure grup " + 
      (group.getLabel() != null ? group.getLabel() : group.getName()), 
      "map:" + map.getName()));
  }
}
