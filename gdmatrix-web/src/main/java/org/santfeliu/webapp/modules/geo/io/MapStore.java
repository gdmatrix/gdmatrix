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
package org.santfeliu.webapp.modules.geo.io;

import com.sun.xml.ws.util.ByteArrayDataSource;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.DocumentManagerPort;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.faces.maplibre.model.Style;
import org.santfeliu.webapp.modules.doc.DocModuleBean;
import static org.apache.commons.lang.StringUtils.isBlank;
import org.santfeliu.util.FileDataSource;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.keywords.KeywordsManager;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class MapStore
{
  static final Logger LOGGER = Logger.getLogger("MapStore");

  public static final String MAP_TYPEID = "GMAP";
  public static final String MAP_CATEGORY_TYPEID = "GMAPCAT";
  public static final String MAP_NAME_PROPERTY = "mapName";
  public static final String BASE_MAP_NAME_PROPERTY = "baseMapName";
  public static final String MAP_SUMMARY_PROPERTY = "summary";
  public static final String MAP_DESCRIPTION_PROPERTY = "description";
  public static final String MAP_KEYWORDS_PROPERTY = "keywords";
  public static final String MAP_SNAPSHOT_DOCID_PROPERTY = "snapshotDocId";
  public static final String MAP_CATEGORY_NAME_PROPERTY = "category";
  public static final String MAP_CATEGORY_POSITION_PROPERTY = "position";
  public static final String MAP_CATEGORY_PARENT_PROPERTY = "parentCategory";
  public static final String MAP_CATEGORY_DESCRIPTION_PROPERTY = "description";
  public static final String GEO_ADMIN_ROLE = "GIS_ADMIN";

  private static List<MapCategory> categoryList;
  private static Map<String, MapCategory> categoryCache =
    Collections.synchronizedMap(new HashMap<>());
  private static long lastPurgeMillis;
  private static final long CATEGORY_CACHE_REFRESH_TIME = 300000; // 5 minutes

  private DocumentManagerPort documentManagerPort;

  public void setCredentials(String userId, String password)
  {
    documentManagerPort = DocModuleBean.getPort(userId, password);
  }

  public MapGroup findMaps(MapFilter mapFilter)
  {
    LOGGER.log(Level.INFO, "Finding maps with name {0} and keywords {1}",
      new Object[]{mapFilter.getMapName(), mapFilter.getKeywords()});

    HashMap<String, MapGroup> mapGroupMap = new HashMap<>();
    MapGroup rootGroup = new MapGroup();
    mapGroupMap.put(null, rootGroup);

    DocumentFilter filter = new DocumentFilter();
    filter.setDocTypeId(MAP_TYPEID);

    if (!isBlank(mapFilter.getTitle()))
    {
      filter.setTitle("%" + mapFilter.getTitle() + "%");
    }

    if (!isBlank(mapFilter.getMapName()))
    {
      Property property = new Property();
      property.setName(MAP_NAME_PROPERTY);
      property.getValue().add("%" + mapFilter.getMapName() + "%");
      filter.getProperty().add(property);
    }

    if (!isBlank(mapFilter.getCategoryName()))
    {
      Property property = new Property();
      property.setName(MAP_CATEGORY_NAME_PROPERTY);
      property.getValue().add(mapFilter.getCategoryName());
      filter.getProperty().add(property);
    }

    if (!isBlank(mapFilter.getKeywords()))
    {
      String keywords = mapFilter.getKeywords();

      Property property = new Property();
      property.setName(MAP_NAME_PROPERTY);
      property.getValue().add("%");
      filter.getProperty().add(property);

      KeywordsManager keywordsManager = new KeywordsManager(keywords);
      property = keywordsManager.getDisjointKeywords();
      filter.getProperty().add(property);
    }

    filter.getOutputProperty().add(MAP_NAME_PROPERTY);
    filter.getOutputProperty().add(MAP_CATEGORY_NAME_PROPERTY);
    filter.getOutputProperty().add(MAP_SNAPSHOT_DOCID_PROPERTY);

    List<Document> documents = getPort().findDocuments(filter);
    for (Document document : documents)
    {
      String mapTitle = document.getTitle();
      String mapName = DictionaryUtils.getPropertyValue(
        document.getProperty(), MAP_NAME_PROPERTY);
      String categoryName = DictionaryUtils.getPropertyValue(
        document.getProperty(), MAP_CATEGORY_NAME_PROPERTY);
      String snapshotDocId = DictionaryUtils.getPropertyValue(
        document.getProperty(), MAP_SNAPSHOT_DOCID_PROPERTY);

      MapGroup mapGroup = getMapGroup(categoryName, mapGroupMap);

      MapView mapView = new MapView();
      mapView.setTitle(mapTitle);
      mapView.setMapName(mapName);
      mapView.setSnapshotDocId(snapshotDocId);
      mapGroup.getMapViews().add(mapView);
    }
    rootGroup.complete();

    return rootGroup;
  }

  public MapDocument loadMap(String mapName) throws Exception
  {
    LOGGER.log(Level.INFO, "Loading map {0}", mapName);
    String docId = getMapDocId(mapName);
    if (docId != null)
    {
      Document document = getPort().loadDocument(docId, 0, ContentInfo.ALL);
      return new MapDocument(document);
    }
    return null;
  }

  public boolean storeMap(MapDocument mapDocument, boolean isNewMap)
    throws Exception
  {
    LOGGER.log(Level.INFO, "Storing map {0}", mapDocument.getName());

    Style style = mapDocument.getStyle();
    String mapName = mapDocument.getName();
    String docId = getMapDocId(mapName);
    style.setName(mapName);
    style.cleanUp();

    DocumentManagerPort port = getPort();
    Document document;
    if (docId == null)
    {
      document = new Document();
      document.setDocTypeId(MAP_TYPEID);
    }
    else
    {
      if (isNewMap)
      {
        // previous map found, do not save it
        return false;
      }
      else
      {
        document = port.loadDocument(docId, 0, ContentInfo.METADATA);
      }
    }
    // set document properties
    document.setTitle(mapDocument.getTitle());
    document.setCreationDate(mapDocument.getCreationDate());

    document.getProperty().clear();
    document.getProperty().addAll(mapDocument.getProperty());

    Property property = new Property();
    property.setName(MAP_NAME_PROPERTY);
    property.getValue().add(mapName);
    document.getProperty().add(property);

    String baseMapName = mapDocument.getBaseMapName();
    if (!isBlank(baseMapName))
    {
      property = new Property();
      property.setName(BASE_MAP_NAME_PROPERTY);
      property.getValue().add(baseMapName);
      document.getProperty().add(property);
    }

    String mapSummary = mapDocument.getSummary();
    if (!isBlank(mapSummary))
    {
      property = new Property();
      property.setName(MAP_SUMMARY_PROPERTY);
      property.getValue().add(mapSummary);
      document.getProperty().add(property);
    }

    String mapDescription = mapDocument.getDescription();
    if (!isBlank(mapDescription))
    {
      property = new Property();
      property.setName(MAP_DESCRIPTION_PROPERTY);
      property.getValue().add(mapDescription);
      document.getProperty().add(property);
    }

    String mapCategoryName = mapDocument.getCategoryName();
    if (!isBlank(mapCategoryName))
    {
      property = new Property();
      property.setName(MAP_CATEGORY_NAME_PROPERTY);
      property.getValue().add(mapCategoryName);
      document.getProperty().add(property);
    }

    String mapKeywords = mapDocument.getKeywords();
    if (isBlank(mapKeywords))
    {
      mapKeywords = mapDocument.getTitle();
    }
    else
    {
      mapKeywords = mapDocument.getTitle() + " " + mapKeywords;
    }
    property = new Property();
    property.setName(MAP_KEYWORDS_PROPERTY);
    property.getValue().add(KeywordsManager.toKeywordsText(mapKeywords));
    document.getProperty().add(property);

    String snapshotDocId = mapDocument.getSnapshotDocId();
    if (!isBlank(snapshotDocId))
    {
      property = new Property();
      property.setName(MAP_SNAPSHOT_DOCID_PROPERTY);
      property.getValue().add(snapshotDocId);
      document.getProperty().add(property);
    }

    document.getAccessControl().clear();
    document.getAccessControl().addAll(mapDocument.getAccessControl());

    Content content = new Content();
    content.setContentType("application/json");
    byte[] bytes = style.toString().getBytes("UTF-8");
    ByteArrayDataSource ds = new ByteArrayDataSource(bytes, "application/json");
    content.setData(new DataHandler(ds));
    document.setContent(content);

    document = port.storeDocument(document);

    mapDocument.readProperties(document);

    return true;
  }

  public void removeMap(String mapName)
  {
    LOGGER.log(Level.INFO, "Removing map {0}", mapName);

    String docId = getMapDocId(mapName);

    getPort().removeDocument(docId, 0);
  }

  public String getMapSummary(String mapName) throws Exception
  {
    MapDocument mapDocument = loadMap(mapName);
    return mapDocument.getMergedSummary();
  }

  public String getMapSummaryAndDescription(String mapName) throws Exception
  {
    MapDocument mapDocument = loadMap(mapName);
    return mapDocument.getMergedSummaryAndDescription();
  }

  public String storeMapSnapshot(String mapName, File file)
  {
    String docId = getMapDocId(mapName);
    if (docId == null) throw new RuntimeException("MAP_NOT_FOUND");

    DocumentManagerPort port = getPort();
    Document mapDocument = port.loadDocument(docId, 0, ContentInfo.METADATA);

    String snapshotDocId = DictionaryUtils.getPropertyValue(
      mapDocument.getProperty(), MAP_SNAPSHOT_DOCID_PROPERTY);

    Document snapshotDocument = new Document();
    snapshotDocument.setDocId(snapshotDocId);
    snapshotDocument.setTitle("Snapshot of " + mapName);
    snapshotDocument.setDocTypeId("IMAGE");
    Content content = new Content();
    content.setContentType("image/png");
    content.setData(new DataHandler(new FileDataSource(file, "image/png")));
    snapshotDocument.setContent(content);
    snapshotDocument = port.storeDocument(snapshotDocument);
    snapshotDocId = snapshotDocument.getDocId();

    // add snapshotDocId property to mapDocument
    DictionaryUtils.setPropertyValue(mapDocument.getProperty(),
      MAP_SNAPSHOT_DOCID_PROPERTY, snapshotDocId, false);
    port.storeDocument(mapDocument);

    return snapshotDocId;
  }

  // Categories

  public void purgeCategoryCache()
  {
    lastPurgeMillis = 0;
  }

  public java.util.Map<String, MapCategory> getCategoryCache()
  {
    long now = System.currentTimeMillis();
    if (now - lastPurgeMillis > CATEGORY_CACHE_REFRESH_TIME)
    {
      lastPurgeMillis = now;
      HashMap<String, MapCategory> cache = new HashMap<>();

      DocumentFilter filter = new DocumentFilter();
      filter.setDocTypeId(MAP_CATEGORY_TYPEID);
      filter.getOutputProperty().add(MAP_CATEGORY_NAME_PROPERTY);
      filter.getOutputProperty().add(MAP_CATEGORY_PARENT_PROPERTY);
      filter.getOutputProperty().add(MAP_CATEGORY_DESCRIPTION_PROPERTY);
      filter.getOutputProperty().add(MAP_CATEGORY_POSITION_PROPERTY);
      List<Document> documents = getPortAsAdmin().findDocuments(filter);
      for (Document document : documents)
      {
        MapCategory category = new MapCategory();

        category.setDocId(document.getDocId());
        category.setTitle(document.getTitle());

        String categoryName = DictionaryUtils.getPropertyValue(
          document.getProperty(), MAP_CATEGORY_NAME_PROPERTY);
        category.setName(categoryName);

        String parentCategoryName = DictionaryUtils.getPropertyValue(
          document.getProperty(), MAP_CATEGORY_PARENT_PROPERTY);
        category.setParentCategoryName(parentCategoryName);

        String description = DictionaryUtils.getPropertyValue(
          document.getProperty(), MAP_CATEGORY_DESCRIPTION_PROPERTY);
        category.setDescription(description);

        String position = DictionaryUtils.getPropertyValue(
          document.getProperty(), MAP_CATEGORY_POSITION_PROPERTY);
        if (position == null) position = "00";
        category.setPosition(position);

        Content content = document.getContent();
        if (content != null)
        {
          category.setContentId(content.getContentId());
        }
        cache.put(categoryName, category);
      }

      List<MapCategory> rootCategories = new ArrayList<>();
      HashMap<MapCategory, List<MapCategory>> subCategoriesMap = new HashMap<>();

      // find and fix loops
      for (MapCategory category : cache.values())
      {
        String parentCategoryName = category.getParentCategoryName();
        MapCategory parentCategory = cache.get(parentCategoryName);

        MapCategory ancestorCategory = parentCategory;
        while (ancestorCategory != null && ancestorCategory != category)
        {
          parentCategoryName = ancestorCategory.getParentCategoryName();
          ancestorCategory = cache.get(parentCategoryName);
        }
        if (ancestorCategory == category) // loop detected
        {
          category.setParentCategoryName(null); // fix loop
          parentCategory = null;
        }

        if (parentCategory == null)
        {
          rootCategories.add(category);
        }
        else
        {
          List<MapCategory> subCategories = subCategoriesMap.get(parentCategory);
          if (subCategories == null)
          {
            subCategories = new ArrayList<>();
            subCategoriesMap.put(parentCategory, subCategories);
          }
          subCategories.add(category);
        }
      }

      Collections.sort(rootCategories,
        (a, b) -> a.getPosition().compareTo(b.getPosition()));

      List<MapCategory> sortedCategories = new ArrayList<>();
      for (MapCategory rootCategory : rootCategories)
      {
        addCategories(rootCategory, subCategoriesMap, sortedCategories, 0);
      }

      categoryCache = cache;
      categoryList = sortedCategories;
    }
    // returned cache is immutable and has no loops
    return categoryCache;
  }

  private void addCategories(MapCategory category,
    HashMap<MapCategory, List<MapCategory>> subCategoriesMap,
    List<MapCategory> sortedCategories, int level)
  {
    sortedCategories.add(category);
    category.setLevel(level);

    List<MapCategory> subCategories = subCategoriesMap.get(category);
    if (subCategories != null)
    {
      Collections.sort(subCategories,
        (a, b) -> a.getPosition().compareTo(b.getPosition()));

      for (MapCategory subCategory : subCategories)
      {
        addCategories(subCategory, subCategoriesMap, sortedCategories, level + 1);
      }
    }
  }

  public MapCategory getCategory(String categoryName)
  {
    return getCategoryCache().get(categoryName);
  }

  public List<MapCategory> getCategoryList()
  {
    getCategoryCache();
    return categoryList;
  }

  public void storeCategory(MapCategory category, File imageFile)
    throws Exception
  {
    DocumentManagerPort port = getPortAsAdmin();
    Document document;
    String docId = category.getDocId();
    if (docId != null)
    {
      document = port.loadDocument(docId, 0, ContentInfo.METADATA);
    }
    else
    {
      if (getCategory(category.getName()) != null)
      {
        throw new Exception("There is already a category with that name");
      }
      document = new Document();
    }
    document.setTitle(category.getTitle());
    document.setDocTypeId(MAP_CATEGORY_TYPEID);

    String parentCategoryName = category.getParentCategoryName();
    if (isBlank(parentCategoryName))
    {
      parentCategoryName = null;
    }
    else
    {
      // detect loops
      String ancestorCategoryName = parentCategoryName;
      do
      {
        if (ancestorCategoryName.equals(category.getName()))
          throw new Exception("Loop detected in category hierarchy");

        MapCategory ancestorCategory = getCategory(ancestorCategoryName);
        ancestorCategoryName = ancestorCategory.getParentCategoryName();
      } while (ancestorCategoryName != null);
    }

    DictionaryUtils.setProperty(document, MAP_CATEGORY_NAME_PROPERTY,
      category.getName());
    DictionaryUtils.setProperty(document, MAP_CATEGORY_PARENT_PROPERTY,
      parentCategoryName);
    DictionaryUtils.setProperty(document, MAP_CATEGORY_DESCRIPTION_PROPERTY,
      category.getDescription());
    DictionaryUtils.setProperty(document, MAP_CATEGORY_POSITION_PROPERTY,
      category.getPosition());

    if (imageFile != null)
    {
      Content content = new Content();
      content.setContentType("image/svg+xml");
      content.setData(new DataHandler(new FileDataSource(imageFile)));
      document.setContent(content);
    }

    getPortAsAdmin().storeDocument(document);

    purgeCategoryCache();
  }

  public void setPort(DocumentManagerPort port)
  {
    this.documentManagerPort = port;
  }

  public DocumentManagerPort getPort()
  {
    if (documentManagerPort == null)
    {
      documentManagerPort = DocModuleBean.getPort("anonymous", null);
    }
    return documentManagerPort;
  }

  public DocumentManagerPort getPortAsAdmin()
  {
    String adminUserId = MatrixConfig.getProperty("adminCredentials.userId");
    String adminPassword = MatrixConfig.getProperty("adminCredentials.password");
    if (adminUserId != null && adminPassword != null)
    {
      return DocModuleBean.getPort(adminUserId, adminPassword);
    }
    else
    {
      return getPort();
    }
  }

  private MapGroup getMapGroup(String categoryName,
    HashMap<String, MapGroup> mapGroupMap)
  {
    MapGroup mapGroup = mapGroupMap.get(categoryName);
    if (mapGroup == null)
    {
      mapGroup = new MapGroup();
      MapCategory category = getCategory(categoryName);
      mapGroup.setCategory(category);
      mapGroupMap.put(categoryName, mapGroup);

      String parentCategoryName = category.getParentCategoryName();
      MapGroup parentMapGroup = getMapGroup(parentCategoryName, mapGroupMap);
      parentMapGroup.getMapGroups().add(mapGroup);
    }
    return mapGroup;
  }

  private String getMapDocId(String mapName)
  {
    DocumentFilter filter = new DocumentFilter();
    filter.setDocTypeId(MAP_TYPEID);
    Property property = new Property();
    property.setName(MAP_NAME_PROPERTY);
    property.getValue().add(mapName);
    filter.getProperty().add(property);
    List<Document> documents = getPortAsAdmin().findDocuments(filter);
    return documents.isEmpty() ? null : documents.get(0).getDocId();
  }
}
