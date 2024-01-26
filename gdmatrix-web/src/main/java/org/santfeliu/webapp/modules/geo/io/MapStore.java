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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.activation.DataHandler;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.DocumentManagerPort;
import org.matrix.security.AccessControl;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.faces.maplibre.model.Style;
import org.santfeliu.webapp.modules.doc.DocModuleBean;
import org.santfeliu.util.template.WebTemplate;
import static org.matrix.dic.DictionaryConstants.DELETE_ACTION;
import static org.matrix.dic.DictionaryConstants.READ_ACTION;
import static org.matrix.dic.DictionaryConstants.WRITE_ACTION;
import static org.apache.commons.lang.StringUtils.isBlank;
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
  public static final String MAP_TYPEID = "GMAP";
  public static final String MAP_CATEGORY_TYPEID = "XMAPCAT";
  public static final String MAP_NAME_PROPERTY = "mapName";
  public static final String MAP_SUMMARY_PROPERTY = "summary";
  public static final String MAP_DESCRIPTION_PROPERTY = "description";
  public static final String MAP_KEYWORDS_PROPERTY = "keywords";
  public static final String MAP_CATEGORY_NAME_PROPERTY = "category";
  public static final String MAP_CATEGORY_POSITION_PROPERTY = "position";
  public static final String MAP_CATEGORY_PARENT_PROPERTY = "parentCategory";
  public static final String GEO_ADMIN_ROLE = "GIS_ADMIN";

  public static List<MapCategory> categoryList;
  public static HashMap<String, MapCategory> categoryCache = new HashMap<>();
  public static long lastPurgeMillis;

  private DocumentManagerPort documentManagerPort;

  public void setCredentials(String userId, String password)
  {
    documentManagerPort = DocModuleBean.getPort(userId, password);
  }

  public MapGroup findMaps(MapFilter mapFilter)
  {
    HashMap<String, MapGroup> mapGroupMap = new HashMap<>();
    MapGroup rootGroup = new MapGroup();
    mapGroupMap.put(null, rootGroup);

    DocumentFilter filter = new DocumentFilter();
    filter.setDocTypeId(MAP_TYPEID);

    if (!isBlank(mapFilter.getTitle()))
    {
      filter.setTitle("%" + mapFilter.getTitle() + "%");
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
    List<Document> documents = getPort().findDocuments(filter);
    for (Document document : documents)
    {
      String mapTitle = document.getTitle();
      String mapName = DictionaryUtils.getPropertyValue(
        document.getProperty(), MAP_NAME_PROPERTY);
      String categoryName = DictionaryUtils.getPropertyValue(
        document.getProperty(), MAP_CATEGORY_NAME_PROPERTY);

      MapGroup mapGroup = getMapGroup(categoryName, mapGroupMap);

      MapView mapView = new MapView();
      mapView.setTitle(mapTitle);
      mapView.setMapName(mapName);
      mapGroup.getMapViews().add(mapView);
    }
    rootGroup.complete();

    return rootGroup;
  }

  public MapDocument loadMap(String mapName) throws Exception
  {
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
    for (Property property : mapDocument.getProperty())
    {
      String propertyName = property.getName();
      if (!propertyName.equals(MAP_NAME_PROPERTY) &&
          !propertyName.equals(MAP_SUMMARY_PROPERTY) &&
          !propertyName.equals(MAP_DESCRIPTION_PROPERTY) &&
          !propertyName.equals(MAP_KEYWORDS_PROPERTY) &&
          !propertyName.equals(MAP_CATEGORY_NAME_PROPERTY))
      {
        document.getProperty().add(property);
      }
    }
    Property property = new Property();
    property.setName(MAP_NAME_PROPERTY);
    property.getValue().add(mapName);
    document.getProperty().add(property);

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
    if (!isBlank(mapKeywords))
    {
      property = new Property();
      property.setName(MAP_KEYWORDS_PROPERTY);
      property.getValue().add(mapKeywords);
      document.getProperty().add(property);
    }

    document.getAccessControl().clear();
    document.getAccessControl().addAll(mapDocument.getAccessControl());

    Content content = new Content();
    byte[] bytes = style.toString().getBytes("UTF-8");
    ByteArrayDataSource ds = new ByteArrayDataSource(bytes, "application/json");
    content.setData(new DataHandler(ds));
    document.setContent(content);

    port.storeDocument(document);

    return true;
  }

  public void removeMap(String mapName)
  {
    String docId = getMapDocId(mapName);

    getPort().removeDocument(docId, 0);
  }

  public String getMapSummary(String mapName) throws Exception
  {
    MapStore.MapDocument mapDocument = loadMap(mapName);
    return mapDocument.getMergedSummary();
  }

  public String getMapSummaryAndDescription(String mapName) throws Exception
  {
    MapStore.MapDocument mapDocument = loadMap(mapName);
    return mapDocument.getMergedSummaryAndDescription();
  }

  // Categories

  public void purgeCategoryCache()
  {
    lastPurgeMillis = 0;
  }

  public java.util.Map<String, MapCategory> getCategoryCache()
  {
    long now = System.currentTimeMillis();
    if (now - lastPurgeMillis > 300000) // 5 minutes
    {
      lastPurgeMillis = now;
      HashMap<String, MapCategory> cache = new HashMap<>();

      DocumentFilter filter = new DocumentFilter();
      filter.setDocTypeId(MAP_CATEGORY_TYPEID);
      filter.getOutputProperty().add(MAP_CATEGORY_NAME_PROPERTY);
      filter.getOutputProperty().add(MAP_CATEGORY_PARENT_PROPERTY);
      filter.getOutputProperty().add(MAP_CATEGORY_POSITION_PROPERTY);
      List<Document> documents = getPortAsAdmin().findDocuments(filter);
      for (Document document : documents)
      {
        MapCategory category = new MapCategory();

        category.setTitle(document.getTitle());

        String categoryName = DictionaryUtils.getPropertyValue(
          document.getProperty(), MAP_CATEGORY_NAME_PROPERTY);
        category.setName(categoryName);

        String parentCategoryName = DictionaryUtils.getPropertyValue(
          document.getProperty(), MAP_CATEGORY_PARENT_PROPERTY);
        category.setParentCategoryName(parentCategoryName);

        String position = DictionaryUtils.getPropertyValue(
          document.getProperty(), MAP_CATEGORY_POSITION_PROPERTY);
        if (position == null) position = "00";
        category.setPosition(position);

        Content content = document.getContent();
        if (content != null && content.getContentId() != null)
        {
          category.setIcon("image:" + content.getContentId());
        }
        else
        {
          category.setIcon("pi pi-circle");
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
    return documentManagerPort = DocModuleBean.getPort(adminUserId, adminPassword);
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
    List<Document> documents = getPort().findDocuments(filter);
    return documents.isEmpty() ? null : documents.get(0).getDocId();
  }

  public static class MapDocument implements Serializable
  {
    String name;
    String title;
    String summary;
    String description;
    String keywords;
    String categoryName;
    String creationDate;
    String captureUserId;
    String captureDateTime;
    String changeUserId;
    String changeDateTime;
    Style style; // MapLibre style
    final List<Property> property = new ArrayList<>();
    final List<AccessControl> accessControl = new ArrayList<>();

    public MapDocument()
    {
      style = new Style();
      name = "new_map";
      title = "New map";
    }

    public MapDocument(Style map)
    {
      this.style = map;
    }

    public MapDocument(Document document) throws IOException
    {
      InputStream is = document.getContent().getData().getInputStream();
      this.style = new Style();
      this.style.read(new InputStreamReader(is, "UTF-8"));
      this.readProperties(document);
      this.setAccessControl(document.getAccessControl());
    }

    public Style getStyle()
    {
      return style;
    }

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public String getTitle()
    {
      return title;
    }

    public void setTitle(String title)
    {
      this.title = title;
    }

    public String getSummary()
    {
      return summary;
    }

    public void setSummary(String summary)
    {
      this.summary = summary;
    }

    public String getDescription()
    {
      return description;
    }

    public void setDescription(String description)
    {
      this.description = description;
    }

    public String getMergedSummary()
    {
      String sum = summary == null ? "" : summary;
      return mergeMapTemplate(sum);
    }

    public String getMergedSummaryAndDescription()
    {
      String sum = summary == null ? "" : summary;
      String desc = description == null ? "" : description;
      return mergeMapTemplate(sum + desc);
    }

    public String getKeywords()
    {
      return keywords;
    }

    public void setKeywords(String keywords)
    {
      this.keywords = keywords;
    }

    public String getCategoryName()
    {
      return categoryName;
    }

    public void setCategoryName(String categoryName)
    {
      if (isBlank(categoryName)) categoryName = null;
      this.categoryName = categoryName;
    }

    public String getCreationDate()
    {
      return creationDate;
    }

    public void setCreationDate(String creationDate)
    {
      this.creationDate = creationDate;
    }

    public String getCaptureUserId()
    {
      return captureUserId;
    }

    public void setCaptureUserId(String captureUserId)
    {
      this.captureUserId = captureUserId;
    }

    public String getCaptureDateTime()
    {
      return captureDateTime;
    }

    public void setCaptureDateTime(String captureDateTime)
    {
      this.captureDateTime = captureDateTime;
    }

    public String getChangeUserId()
    {
      return changeUserId;
    }

    public void setChangeUserId(String changeUserId)
    {
      this.changeUserId = changeUserId;
    }

    public String getChangeDateTime()
    {
      return changeDateTime;
    }

    public void setChangeDateTime(String changeDateTime)
    {
      this.changeDateTime = changeDateTime;
    }

    public List<Property> getProperty()
    {
      return property;
    }

    public List<AccessControl> getFullAccessControl()
    {
      List<AccessControl> fullAcl = new ArrayList<>();
      for (AccessControl ac : accessControl)
      {
        String roleId = ac.getRoleId();
        String action = ac.getAction();
        fullAcl.add(ac);
        if (WRITE_ACTION.equals(action) ||
            DELETE_ACTION.equals(action))
        {
          AccessControl nac = new AccessControl();
          nac.setRoleId(roleId);
          nac.setAction(READ_ACTION);
          fullAcl.add(ac);
        }
        else if (DELETE_ACTION.equals(action))
        {
          AccessControl nac = new AccessControl();
          nac.setRoleId(roleId);
          nac.setAction(WRITE_ACTION);
          fullAcl.add(ac);
        }
      }
      return fullAcl;
    }

    public List<AccessControl> getAccessControl()
    {
      return accessControl;
    }

    public final void setAccessControl(List<AccessControl> acl)
    {
      HashMap<String, AccessControl> roles = new HashMap<>();

      for (AccessControl ac : acl)
      {
        String roleId = ac.getRoleId();
        String action = ac.getAction();
        AccessControl prevAc = roles.get(roleId);

        if (prevAc == null ||
            (READ_ACTION.equals(prevAc.getAction()) &&
             (WRITE_ACTION.equals(action) || DELETE_ACTION.equals(action))) ||
            (WRITE_ACTION.equals(prevAc.getAction()) && DELETE_ACTION.equals(action)))
        {
          roles.put(roleId, ac);
        }
      }
      this.accessControl.clear();
      this.accessControl.addAll(roles.values());
    }

    public List<String> getWriteRoles()
    {
      List<String> roles = new ArrayList<>();
      for (AccessControl ac : accessControl)
      {
        if (WRITE_ACTION.equals(ac.getAction()) ||
            DELETE_ACTION.equals(ac.getAction()))
        {
          roles.add(ac.getRoleId());
        }
      }
      return roles;
    }

    public String mergeMapTemplate(String templateSource)
    {
      WebTemplate template = WebTemplate.create(templateSource);
      HashMap<String, Object> variables = new HashMap<>();
      for (Property prop : getProperty())
      {
        variables.put(prop.getName(), prop.getValue().get(0));
      }
      variables.put("title", getTitle());
      variables.put("creationDate", getCreationDate());
      variables.put("captureDateTime", getCaptureDateTime());
      variables.put("changeDateTime", getChangeDateTime());
      variables.put("captureUserId", getCaptureUserId());
      variables.put("changeUserId", getChangeUserId());
      return template.merge(variables);
    }

    public final void readProperties(Document document)
    {
      this.title = document.getTitle();
      this.creationDate = document.getCreationDate();
      this.changeUserId = document.getChangeUserId();
      this.changeDateTime = document.getChangeDateTime();
      this.captureUserId = document.getCaptureUserId();
      this.captureDateTime = document.getCaptureDateTime();

      for (Property documentProperty : document.getProperty())
      {
        String propertyName = documentProperty.getName();
        String value = documentProperty.getValue().get(0);
        switch (propertyName)
        {
          case MAP_NAME_PROPERTY:
            name = value;
            break;
          case MAP_SUMMARY_PROPERTY:
            summary = value;
            break;
          case MAP_DESCRIPTION_PROPERTY:
            description = value;
            break;
          case MAP_KEYWORDS_PROPERTY:
            keywords = value;
            break;
          case MAP_CATEGORY_NAME_PROPERTY:
            categoryName = value;
            break;
          default:
            getProperty().add(documentProperty);
        }
      }
    }
  }

  public static class MapCategory implements Serializable
  {
    String name;
    String title;
    String icon;
    String description;
    String position;
    String parentCategoryName;
    int level;

    public String getName()
    {
      return name;
    }

    public void setName(String categoryName)
    {
      this.name = categoryName;
    }

    public String getParentCategoryName()
    {
      return parentCategoryName;
    }

    public void setParentCategoryName(String parentCategoryName)
    {
      this.parentCategoryName = parentCategoryName;
    }

    public String getTitle()
    {
      return title;
    }

    public void setTitle(String title)
    {
      this.title = title;
    }

    public String getIcon()
    {
      return icon;
    }

    public void setIcon(String icon)
    {
      this.icon = icon;
    }

    public String getDescription()
    {
      return description;
    }

    public void setDescription(String description)
    {
      this.description = description;
    }

    public String getPosition()
    {
      return position;
    }

    public void setPosition(String position)
    {
      this.position = position;
    }

    public boolean isRootCategoty()
    {
      return parentCategoryName == null;
    }

    public int getLevel()
    {
      return level;
    }

    public void setLevel(int level)
    {
      this.level = level;
    }

    @Override
    public String toString()
    {
      return "MapCategory{" + name + ", " + parentCategoryName + "}";
    }
  }

  public static class MapGroup implements Serializable
  {
    MapCategory category;
    List<MapView> mapViews = new ArrayList<>();
    List<MapGroup> mapGroups = new ArrayList<>();
    int mapCount;

    public MapCategory getCategory()
    {
      return category;
    }

    public void setCategory(MapCategory category)
    {
      this.category = category;
    }

    public List<MapView> getMapViews()
    {
      if (mapViews == null) mapViews = new ArrayList<>();
      return mapViews;
    }

    public void setMapViews(List<MapView> mapViews)
    {
      this.mapViews = mapViews;
    }

    public List<MapGroup> getMapGroups()
    {
      if (mapGroups == null) mapGroups = new ArrayList<>();
      return mapGroups;
    }

    public void setMapGroups(List<MapGroup> mapGroups)
    {
      this.mapGroups = mapGroups;
    }

    public int getMapCount()
    {
      return mapCount;
    }

    public void complete()
    {
      Collections.sort(mapViews, (a, b) ->
        a.title.compareTo(b.title));

      Collections.sort(mapGroups, (a, b) ->
        a.category.position.compareTo(b.category.position));

      mapCount = mapViews.size();
      for (MapGroup group : mapGroups)
      {
        group.complete();
        mapCount += group.getMapCount();
      }
    }

    @Override
    public String toString()
    {
      return "MapGroup{" + category + ", " +
        getMapGroups() + ", " + getMapViews() + "}";
    }
  }

  public static class MapView implements Serializable
  {
    String mapName;
    String title;

    public String getMapName()
    {
      return mapName;
    }

    public void setMapName(String mapName)
    {
      this.mapName = mapName;
    }

    public String getTitle()
    {
      return title;
    }

    public void setTitle(String title)
    {
      this.title = title;
    }

    @Override
    public String toString()
    {
      return "MapView{" + mapName + "}";
    }
  }

  public static class MapFilter implements Serializable
  {
    String title;
    String categoryName;
    String keywords;

    public String getTitle()
    {
      return title;
    }

    public void setTitle(String title)
    {
      this.title = title;
    }

    public String getCategoryName()
    {
      return categoryName;
    }

    public void setCategoryName(String categoryName)
    {
      this.categoryName = categoryName;
    }

    public String getKeywords()
    {
      return keywords;
    }

    public void setKeywords(String keywords)
    {
      this.keywords = keywords;
    }
  }
}
