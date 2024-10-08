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
package org.santfeliu.webapp.modules.geo.util;

/**
 *
 * @author realor
 */
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.activation.DataHandler;
import org.matrix.dic.Property;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.DocumentManagerPort;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.faces.maplibre.model.Layer;
import org.santfeliu.faces.maplibre.model.Source;
import org.santfeliu.faces.maplibre.model.Style;
import org.santfeliu.misc.mapviewer.Map;
import org.santfeliu.misc.mapviewer.MapDocument;
import org.santfeliu.webapp.modules.geo.io.MapStore;
import org.santfeliu.webapp.modules.geo.metadata.LayerForm;
import org.santfeliu.webapp.modules.geo.metadata.LegendGroup;
import org.santfeliu.webapp.modules.geo.metadata.LegendLayer;
import org.santfeliu.webapp.modules.geo.metadata.PrintReport;
import org.santfeliu.webapp.modules.geo.metadata.Service;
import org.santfeliu.webapp.modules.geo.metadata.ServiceParameters;
import org.santfeliu.webapp.modules.geo.io.SldStore;
import org.santfeliu.webapp.modules.geo.io.SvgStore;
import static org.apache.commons.lang.StringUtils.isBlank;
import org.matrix.doc.Content;
import org.santfeliu.misc.mapviewer.Bounds;
import org.santfeliu.util.MemoryDataSource;

public class MapMigrator
{

  DocumentManagerClient oldPort;
  DocumentManagerClient newPort;
  Collection excludedCategories;
  boolean simulation;

  public MapMigrator(DocumentManagerClient oldPort, DocumentManagerClient newPort)
  {
    this.oldPort = oldPort;
    this.newPort = newPort;
  }

  public DocumentManagerClient getOldPort()
  {
    return oldPort;
  }

  public DocumentManagerClient getNewPort()
  {
    return newPort;
  }

  public Collection getExcludedCategories()
  {
    return excludedCategories;
  }

  public void setExcludedCategories(Collection excludedCategories)
  {
    this.excludedCategories = excludedCategories;
  }

  public boolean isSimulation()
  {
    return simulation;
  }

  public void setSimulation(boolean simulation)
  {
    this.simulation = simulation;
  }

  public void migrateSvgReports() throws Exception
  {
    DocumentFilter filter = new DocumentFilter();
    filter.setDocTypeId(SvgStore.SVG_TYPEID);
    List<Document> docs = oldPort.findDocuments(filter);
    int i = 0;
    for (Document doc : docs)
    {
      doc = oldPort.loadDocument(doc.getDocId(), 0, ContentInfo.ALL);
      String reportName = DictionaryUtils.getPropertyValue(doc.getProperty(), SvgStore.SVG_REPORT_NAME_PROPERTY);
      System.out.println(doc.getDocId() + ", " + reportName + ": " + doc.getTitle());

      doc.setDocId(null);
      doc.setVersion(0);
      doc.setIncremental(false);

      DocumentFilter filter2 = new DocumentFilter();
      filter2.setDocTypeId(SvgStore.SVG_TYPEID);
      Property property = new Property();
      property.setName("report");
      property.getValue().add(reportName);
      filter2.getProperty().add(property);
      List<Document> docs2 = newPort.findDocuments(filter2);
      if (!docs2.isEmpty())
      {
        String oldDocId = ((Document) docs2.get(0)).getDocId();
        doc.setDocId(oldDocId);
        System.out.println("Found: " + oldDocId);
      }
      doc.getContent().setContentId(null);
      if (!simulation)
      {
        System.out.println(i + ": storeReport: " + doc.getDocId());
        newPort.storeDocument(doc);
      }
      Thread.sleep(200L);
      i++;
    }
  }

  public void migrateCategories() throws Exception
  {
    DocumentFilter filter = new DocumentFilter();
    filter.setDocTypeId("XMAPCAT"); // old category
    List<Document> docs = oldPort.findDocuments(filter);
    int i = 0;
    for (Document doc : docs)
    {
      doc = oldPort.loadDocument(doc.getDocId(), 0, ContentInfo.ALL);
      String category = DictionaryUtils.getPropertyValue(doc.getProperty(), MapStore.MAP_CATEGORY_NAME_PROPERTY);
      System.out.println(doc.getDocId() + ", " + category + ": " + doc.getTitle());

      doc.setDocId(null);
      doc.setVersion(0);
      doc.setIncremental(false);
      doc.setDocTypeId(MapStore.MAP_CATEGORY_TYPEID);

      DocumentFilter filter2 = new DocumentFilter();
      filter2.setDocTypeId(MapStore.MAP_CATEGORY_TYPEID);
      Property property = new Property();
      property.setName("category");
      property.getValue().add(category);
      filter2.getProperty().add(property);
      List<Document> docs2 = newPort.findDocuments(filter2);
      if (!docs2.isEmpty())
      {
        String oldDocId = ((Document) docs2.get(0)).getDocId();
        doc.setDocId(oldDocId);
        System.out.println("Found: " + oldDocId);
      }
      doc.getContent().setContentId(null);
      if (!simulation)
      {
        System.out.println(i + ": storeCategory: " + doc.getDocId());
        doc = newPort.storeDocument(doc);
        System.out.println(i + ": stored category: " + doc.getDocId());
      }
      Thread.sleep(200L);
      i++;
    }
  }

  public void migrateLocators() throws Exception
  {
    DocumentFilter filter = new DocumentFilter();
    //filter.setDocTypeId("CODE");
    Property property = new Property();
    property.setName("workflow.js");
    property.getValue().add("%");
    filter.getProperty().add(property);

    List<Document> docs = oldPort.findDocuments(filter);
    int i = 0;
    for (Document doc : docs)
    {
      doc = oldPort.loadDocument(doc.getDocId(), 0, ContentInfo.ALL);
      String locatorName = DictionaryUtils.getPropertyValue(doc.getProperty(), "workflow.js");
      if (!locatorName.endsWith("Locator")) continue;

      System.out.println(doc.getDocId() + ", Locator: " + locatorName + ", " + doc.getTitle());

      String finderName = locatorName.substring(0, locatorName.length() - 7) + "Finder";
      int index = doc.getTitle().indexOf(":");
      String title = index != -1 ? doc.getTitle().substring(index + 1) : doc.getTitle();
      String finderTitle = finderName + ": " + title;
      System.out.println("#" + i + ": Finder: " + finderName + ", " + finderTitle);

      doc.setDocId(null);
      doc.setVersion(0);
      doc.setIncremental(false);

      StringBuilder buffer = new StringBuilder();
      buffer.append("/** ").append(finderName).append(" **/\n\n");
      buffer.append("import { WfsFinder } from \"/resources/gdmatrixfaces/maplibre/finders/WfsFinder.js\";\n\n");

      try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(doc.getContent().getData().getInputStream(), "utf-8")))
      {
        String line = reader.readLine();
        while (line != null)
        {
          buffer.append("//");
          buffer.append(line);
          buffer.append("\n");
          line = reader.readLine();
        }
      }
      String newCode = buffer.toString();
      System.out.println(newCode);

      DocumentFilter filter2 = new DocumentFilter();
      filter2.setDocTypeId("CODE");
      property = new Property();
      property.setName("workflow.js");
      property.getValue().add(finderName);
      filter2.getProperty().add(property);
      List<Document> docs2 = newPort.findDocuments(filter2);
      if (!docs2.isEmpty())
      {
        String oldDocId = ((Document) docs2.get(0)).getDocId();
        doc.setDocId(oldDocId);
        System.out.println("Found: " + oldDocId);
      }

      doc.setTitle(finderTitle);

      MemoryDataSource ds = new MemoryDataSource(newCode.getBytes("UTF-8"),
        "code", "application/javascript");

      Content content = new Content();
      content.setData(new DataHandler(ds));
      content.setContentType("application/javascript");
      doc.setContent(content);

      doc.getProperty().clear();

      property = new Property();
      property.setName("workflow.js");
      property.getValue().add(finderName);
      doc.getProperty().add(property);

      property = new Property();
      property.setName("purpose");
      property.getValue().add("geo");
      doc.getProperty().add(property);

      if (!simulation)
      {
        doc = newPort.storeDocument(doc);
        System.out.println(i + ": storeFinder: " + finderName + " " + doc.getDocId());
      }
      Thread.sleep(200L);
      i++;
    }
  }

  public void migrateSlds() throws Exception
  {
    DocumentFilter filter = new DocumentFilter();
    filter.setDocTypeId("SLD");
    List<Document> docs = oldPort.findDocuments(filter);
    int i = 0;
    for (Document doc : docs)
    {
      doc = oldPort.loadDocument(doc.getDocId(), 0, ContentInfo.ALL);
      doc.setDocId(null);
      doc.setVersion(0);
      doc.setIncremental(false);
      String sldName = DictionaryUtils.getPropertyValue(doc.getProperty(), SldStore.SLD_PROPERTY_NAME);
      System.out.println(doc.getDocId() + ", " + sldName + ": " + doc.getTitle());
      DocumentFilter filter2 = new DocumentFilter();
      filter2.setDocTypeId(SldStore.SLD_TYPEID);
      Property property = new Property();
      property.setName(SldStore.SLD_PROPERTY_NAME);
      property.getValue().add(sldName);
      filter2.getProperty().add(property);
      List<Document> docs2 = newPort.findDocuments(filter2);
      if (!docs2.isEmpty())
      {
        String oldDocId = ((Document) docs2.get(0)).getDocId();
        doc.setDocId(oldDocId);
        System.out.println("Found: " + oldDocId);
      }
      doc.getContent().setContentId(null);
      if (!simulation)
      {
        System.out.println(i + ": storeSLD: " + doc.getDocId());
        newPort.storeDocument(doc);
      }
      Thread.sleep(200L);
      i++;
    }
  }

  public void migrateMaps() throws Exception
  {
    org.santfeliu.misc.mapviewer.MapStore oldMapStore
      = new org.santfeliu.misc.mapviewer.MapStore(oldPort);

    HashMap<String, Integer> scriptReferences = new HashMap<>();

    MapStore styleStore = new MapStore();
    styleStore.setPort((DocumentManagerPort) newPort);
    DocumentFilter filter = new DocumentFilter();
    filter.setDocTypeId("XMAP");
    List<Document> documents = oldPort.findDocuments(filter);
    System.out.println("Map count: " + documents.size());
    int i = 0;
    for (Document document : documents)
    {
      i++;

      document = oldPort.loadDocument(document.getDocId(), 0, ContentInfo.METADATA);
      String mapName = DictionaryUtils.getPropertyValue(document.getProperty(), "mapName");
      String mapCategory = DictionaryUtils.getPropertyValue(document.getProperty(),
        MapStore.MAP_CATEGORY_NAME_PROPERTY);

      if (excludedCategories != null && excludedCategories.contains(mapCategory))
      {
        continue;
      }

      MapDocument mapDoc = oldMapStore.loadMap(mapName);
      Bounds bounds = mapDoc.getBounds();
      double cx = 0.5 * (bounds.getMinX() + bounds.getMaxX());
      double cy = 0.5 * (bounds.getMinY() + bounds.getMaxY());
      double size = (bounds.getMaxX() - bounds.getMinX());

      UTMConverter.LatLng pos = UTMConverter.convertToLatLng(cx, cy, 31, false);
      double zoom = 25 - (int) Math.round(Math.log(size) / Math.log(2));
      System.out.println(pos + " " + zoom);

      String mapTitle = mapDoc.getTitle();
      System.out.println("\n" + i + ":" + mapName + ": " + mapTitle);
      Style style = new Style();
      style.getCenter()[0] = pos.lng;
      style.getCenter()[1] = pos.lat;
      style.setZoom(zoom);
      style.getMetadata().put("maxZoom", 20.0);
      style.getMetadata().put("hash", true);

      // services
      HashMap<String, Service> newServices = new HashMap<>();
      style.getMetadata().put("services", newServices);

      HashMap<String, ServiceParameters> serviceParametersMap = new HashMap<>();
      style.getMetadata().put("serviceParameters", serviceParametersMap);

      List<org.santfeliu.misc.mapviewer.Map.Service> oldServices = mapDoc.getServices();
      for (org.santfeliu.misc.mapviewer.Map.Service service : oldServices)
      {
        System.out.println("  SERVICE " + service.getName() + " " + service.getUrl());
        Service newService = new Service();
        newService.setType("wms");
        String url = service.getUrl();
        if ("http://gis.esantfeliu.org:8080/geoserver/wms".equals(url))
        {
          url = "https://gis.santfeliu.cat/geoserver/wms";
        }
        newService.setUrl(url);
        newService.setDescription(service.getDescription());
        newService.setUseProxy(Boolean.TRUE);
        newServices.put(service.getName(), newService);
      }

      // layerForms
      Set<String> highLightedLayers = new HashSet<>();
      List<Map.InfoLayer> infoLayers = mapDoc.getInfoLayers();
      List<LayerForm> layerForms = new ArrayList<>();
      for (Map.InfoLayer infoLayer : infoLayers)
      {
        String layerName = infoLayer.getName();
        String formSelector = infoLayer.getFormSelector();
        boolean highlight = infoLayer.isHighlight();
        if (highlight)
        {
          highLightedLayers.add(layerName);
        }
        LayerForm layerForm = new LayerForm();
        layerForm.setLayer(layerName);
        layerForm.setFormSelector(formSelector);
        layerForms.add(layerForm);
      }
      style.getMetadata().put("layerForms", layerForms);

      // prepare legend groups
      HashMap<String, LegendGroup> legendGroups = new HashMap<>();
      LegendGroup topLegendGroup = new LegendGroup();
      topLegendGroup.setLabel("Legend");
      LegendGroup baseGroup = new LegendGroup();
      baseGroup.setLabel("Capes base");
      baseGroup.setMode("single");
      topLegendGroup.getChildren().add(baseGroup);
      for (Map.Group group : mapDoc.getGroups())
      {
        LegendGroup legendGroup = new LegendGroup();
        legendGroup.setLabel(group.getLabel());
        legendGroup.setMode("multiple");
        legendGroups.put(group.getName(), legendGroup);
        topLegendGroup.getChildren().add(legendGroup);
      }

      // layers & sources
      List<Map.Layer> oldLayers = mapDoc.getLayers();
      for (Map.Layer oldLayer : oldLayers)
      {
        System.out.println("  LAYER " + oldLayer.getLabel() + ": " + oldLayer.getNames() + ", " + oldLayer.getService().getName());

        String label = oldLayer.getLabel();
        if (isBlank(label))
        {
          label = oldLayer.getNamesString();
        }
        String layerId = getLayerId(label, style.getLayers());
        Layer newLayer = new Layer();
        newLayer.setId(layerId);
        newLayer.setLabel(oldLayer.getLabel());
        newLayer.setType("raster");
        newLayer.setVisible(oldLayer.isVisible());
        newLayer.setLocatable(oldLayer.isLocatable());
        newLayer.setLayers(oldLayer.getNamesString());
        newLayer.setStyles(oldLayer.getStylesString());
        newLayer.setCqlFilter(oldLayer.getCqlFilter());
        style.getLayers().add(newLayer);

        String namesString = oldLayer.getNamesString();
        String[] parts = namesString.split(",");
        for (String part : parts)
        {
          int index = part.indexOf(":");
          if (index != -1)
          {
            part = part.substring(index + 1);
          }
          boolean highlighted = highLightedLayers.contains(part);
          if (highlighted)
          {
            newLayer.getMetadata().put("highlight", highlighted);
            break;
          }
        }

        if (oldLayer.isIndependent())
        {
          String sourceId = layerId;
          Source source = new Source();
          source.setType("raster");
          style.getSources().put(sourceId, source);

          ServiceParameters serviceParameters = new ServiceParameters();
          serviceParameters.setService(oldLayer.getService().getName());
          serviceParameters.setFormat(oldLayer.getFormat());
          serviceParameters.setBuffer(oldLayer.getBuffer());
          serviceParameters.setSldName(oldLayer.getSld());
          serviceParameters.setTransparent(oldLayer.isTransparentBackground());
          serviceParametersMap.put(sourceId, serviceParameters);

          newLayer.setSource(sourceId);
        }
        else
        {
          String sourceId = normalizeId(oldLayer.getService().getName());
          if (!isBlank(oldLayer.getSld()))
          {
            sourceId += "_" + normalizeId(oldLayer.getSld());
          }

          Source source = style.getSources().get(sourceId);
          if (source == null)
          {
            source = new Source();
            source.setType("raster");
            style.getSources().put(sourceId, source);

            ServiceParameters serviceParameters = new ServiceParameters();
            serviceParameters.setService(oldLayer.getService().getName());
            serviceParameters.setFormat(oldLayer.getFormat());
            serviceParameters.setBuffer(oldLayer.getBuffer());
            serviceParameters.setSldName(oldLayer.getSld());
            serviceParameters.setTransparent(oldLayer.isTransparentBackground());
            serviceParametersMap.put(sourceId, serviceParameters);
          }
          newLayer.setSource(sourceId);
        }

        if (oldLayer.isOnLegend())
        {
          LegendLayer legendLayer = new LegendLayer();
          legendLayer.setType("layer");
          String legendGraphic = oldLayer.getLegendGraphic();
          if (!isBlank(legendGraphic))
          {
            System.out.print(legendGraphic + "->");
            legendGraphic = legendGraphic.trim();
            if ("true".equals(legendGraphic))
            {
              legendGraphic = "auto";
            }
            else if ("false".equals(legendGraphic))
            {
              legendGraphic = null;
            }
            else if ("auto".equals(legendGraphic))
            {
              legendGraphic = "auto";
            }
            else if (legendGraphic.startsWith("large:"))
            {
              legendGraphic = "image:" + legendGraphic.substring(6);
            } else
            {
              legendGraphic = "icon:" + legendGraphic;
            }
            System.out.println(legendGraphic);
          }
          legendLayer.setGraphic(legendGraphic);
          legendLayer.setLabel(oldLayer.getLabel());
          legendLayer.setLayerId(layerId);
          if (oldLayer.isBaseLayer())
          {
            baseGroup.getChildren().add(legendLayer);
            continue;
          } else
          {
            Map.Group group = oldLayer.getGroup();
            if (group != null)
            {
              LegendGroup legendGroup = legendGroups.get(group.getName());
              if (legendGroup != null)
              {
                legendGroup.getChildren().add(legendLayer);
                continue;
              }
            }
          }
          topLegendGroup.getChildren().add(legendLayer);
        }
      }
      style.getMetadata().put("legend", topLegendGroup);

      // print reports
      java.util.Map<String, String> properties = mapDoc.getProperties();
      String printReports = properties.get("printReports");
      if (printReports != null)
      {
        List<PrintReport> printReportList = new ArrayList<>();
        String[] pairs = printReports.split(",");
        for (String pair : pairs)
        {
          int index = pair.indexOf(":");
          String reportName = (index == -1) ? pair : pair.substring(0, index);
          String formSelector = (index == -1) ? null : pair.substring(index + 1);
          PrintReport printReport = new PrintReport();
          printReport.setReportName(reportName);
          printReport.setFormSelector(formSelector);
          printReportList.add(printReport);
        }
        style.getMetadata().put("printReports", printReportList);
      }

      // information
      org.santfeliu.webapp.modules.geo.io.MapDocument styleDocument = new org.santfeliu.webapp.modules.geo.io.MapDocument(style);
      Property descriptionProperty = DictionaryUtils.getPropertyByName(document
        .getProperty(), "mapDescription");
      String mapDescription = null;
      if (descriptionProperty != null)
      {
        document.getProperty().remove(descriptionProperty);
        mapDescription = descriptionProperty.getValue().get(0);
      }
      styleDocument.readProperties(document);
      if (mapDescription != null)
      {
        int index = mapDescription.indexOf("-break-");
        String summary = (index == -1) ? mapDescription : mapDescription.substring(0, index);
        String newDesc = (index == -1) ? null : mapDescription.substring(index + 7);
        summary = summary.trim();
        DictionaryUtils.setProperty(styleDocument, "summary", summary);
        if (newDesc != null)
        {
          newDesc = newDesc.trim();
        }
        newDesc = fixHtml(newDesc);
        DictionaryUtils.setProperty(styleDocument, "description", newDesc);
      }
      styleDocument.setAccessControl(document.getAccessControl());

      // Locators
      List<String> scripts =
        (List<String>)styleDocument.getStyle().getMetadata().get("scripts");
      if (scripts == null)
      {
        scripts = new ArrayList<>();
        styleDocument.getStyle().getMetadata().put("scripts", scripts);
      }
      else
      {
        scripts.clear();
      }

      String featureLocators = properties.get("featureLocators");
      System.out.println("FeatureLocators:" + featureLocators);
      if (featureLocators != null)
      {
        String[] locatorNames = featureLocators.split(",");
        for (String locatorName : locatorNames)
        {
          String finderName = locatorName.substring(0, locatorName.length() - 7) + "Finder";
          finderName = finderName.trim();
          finderName = replaceFinder(finderName);

          if (finderName != null && scripts.indexOf(finderName) == -1)
          {
            scripts.add(finderName);
          }
        }
        System.out.println(scripts);
        for (String script : scripts)
        {
          Integer num = scriptReferences.get(script);
          if (num == null) num = 0;
          scriptReferences.put(script, num + 1);
        }
      }

      if (!simulation)
      {
        System.out.println("Saving map " + styleDocument.getName() + "...");
        styleStore.storeMap(styleDocument, false);
      }
      Thread.sleep(1000L);
    }
    System.out.println("script usage:" + scriptReferences);
    System.out.println("script count: " + scriptReferences.size());
  }

  private String replaceFinder(String finderName)
  {
    switch (finderName)
    {
      case "GenericFeatureFinder": return "GenericWfsFinder";
    }
    return finderName;
  }

  private String getLayerId(String label, List<Layer> layers)
  {
    String baseId = normalizeId(label);
    String layerId = baseId;
    int count = 2;
    while (isLayerIdRepeated(layerId, layers))
    {
      layerId = baseId + "_" + count;
      count++;
    }
    return layerId;
  }

  private boolean isLayerIdRepeated(String layerId, List<Layer> layers)
  {
    return layers.stream().anyMatch(l -> l.getId().equals(layerId));
  }

  private String normalizeId(String id)
  {
    id = id.toLowerCase()
      .replace("sf:", "").replace("icc:", "").replace(" ", "_")
      .replace("à", "a")
      .replace("á", "a")
      .replace("è", "e")
      .replace("é", "e")
      .replace("í", "i")
      .replace("ï", "i")
      .replace("ó", "o")
      .replace("ò", "o")
      .replace("ú", "u")
      .replace("ü", "u");

    boolean isSeparator = false;
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < id.length(); i++)
    {
      char ch = id.charAt(i);
      if ((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9'))
      {
        buffer.append(ch);
        isSeparator = false;
      } else if (!isSeparator)
      {
        if (ch == '_' || ch == ',' || ch == ';'
          || ch == '.' || ch == ' ' || ch == '-')
        {
          buffer.append("_");
          isSeparator = true;
        }
      }
    }
    return buffer.toString();
  }

  private String fixHtml(String html)
  {
    if (html != null)
    {
      html = html.trim().replaceAll(" +", " ");
      html = html.trim().replaceAll("</div> <div", "</div>\n<div");
    }
    return html;
  }

  public static void main(String[] args)
  {
    try
    {
      System.out.println("MapMigrator");

      Gson gson = new Gson();
      String configFile = System.getProperty("user.home") + "/MapMigrator.json";
      System.out.println("Reading file: " + configFile);

      java.util.Map config;
      try (FileReader reader = new FileReader(configFile))
      {
        config = gson.fromJson(reader, java.util.Map.class);
      } catch (Exception ex)
      {
        System.out.println("Error reading file: " + ex.getMessage());
        return;
      }

      java.util.Map<String, String> oldStore
        = (java.util.Map<String, String>) config.get("source");
      if (oldStore == null)
      {
        System.out.println("\"source\" property not defined. It's an object with these properties: url, userId, and password.");
        return;
      }

      java.util.Map<String, String> newStore
        = (java.util.Map<String, String>) config.get("target");
      if (newStore == null)
      {
        System.out.println("\"target\" property not defined. It's an object with these properties: url, userId and password.");
        return;
      }

      DocumentManagerClient oldPort = new DocumentManagerClient(
        new URL(oldStore.get("url")),
        oldStore.get("userId"),
        oldStore.get("password"));

      DocumentManagerClient newPort = new DocumentManagerClient(
        new URL(newStore.get("url")),
        newStore.get("userId"),
        newStore.get("password"));

      MapMigrator migrator = new MapMigrator(oldPort, newPort);
      List<String> migrate = (List<String>) config.get("migrate");
      Object ec = config.get("excludedCategories");
      if (ec instanceof Collection)
      {
        migrator.setExcludedCategories((Collection) ec);
      }
      Boolean simulation = (Boolean) config.get("simulation");
      if (simulation != null)
      {
        System.out.println("Simulation: " + simulation);
        migrator.setSimulation(simulation);
      }

      if (migrate == null)
      {
        System.out.println("\"migrate\" property not defined. It's a list of [\"map\", \"cat\", \"sld\", \"svg\", \"loc\"].");
        return;
      }

      for (String type : migrate)
      {
        switch (type)
        {
          case "map":
            migrator.migrateMaps();
            break;
          case "cat":
            migrator.migrateCategories();
            break;
          case "sld":
            migrator.migrateSlds();
            break;
          case "svg":
            migrator.migrateSvgReports();
            break;
          case "loc":
            migrator.migrateLocators();
            break;
        }
      }
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
