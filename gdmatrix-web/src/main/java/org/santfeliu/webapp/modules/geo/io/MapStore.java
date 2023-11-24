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
import java.util.HashMap;
import java.util.List;
import javax.activation.DataHandler;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.DocumentManagerPort;
import org.matrix.security.AccessControl;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.faces.maplibre.model.Map;
import org.santfeliu.webapp.modules.doc.DocModuleBean;
import static org.matrix.dic.DictionaryConstants.DELETE_ACTION;
import static org.matrix.dic.DictionaryConstants.READ_ACTION;
import static org.matrix.dic.DictionaryConstants.WRITE_ACTION;
import org.matrix.doc.OrderByProperty;

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
  public static final String MAP_DESCRIPTION_PROPERTY = "description";
  public static final String MAP_CATEGORY_PROPERTY = "category";
  public static final String MAP_CATEGORY_POSITION_PROPERTY = "position";

  public static HashMap<String, MapCategory> categoryCache = new HashMap<>();

  private DocumentManagerPort documentManagerPort;

  public void setCredentials(String userId, String password)
  {
    documentManagerPort = DocModuleBean.getPort(userId, password);
  }

  public List<MapGroup> findMaps(MapFilter mapFilter)
  {
    HashMap<String, MapGroup> mapGroupMap = new HashMap<>();

    DocumentFilter filter = new DocumentFilter();
    filter.setDocTypeId(MAP_TYPEID);

    if (!StringUtils.isBlank(mapFilter.getTitle()))
    {
      filter.setTitle("%" + mapFilter.getTitle() + "%");
    }

    if (!StringUtils.isBlank(mapFilter.getCategory()))
    {
      Property property = new Property();
      property.setName(MAP_CATEGORY_PROPERTY);
      property.getValue().add(mapFilter.getCategory());
      filter.getProperty().add(property);
    }

    filter.getOutputProperty().add(MAP_NAME_PROPERTY);
    filter.getOutputProperty().add(MAP_CATEGORY_PROPERTY);
    filter.getOutputProperty().add(MAP_DESCRIPTION_PROPERTY);
    OrderByProperty orderProperty = new OrderByProperty();
    orderProperty.setName(MAP_CATEGORY_PROPERTY);
    filter.getOrderByProperty().add(orderProperty);
    List<Document> documents = getPort().findDocuments(filter);
    for (Document document : documents)
    {
      String mapTitle = document.getTitle();
      String mapName = DictionaryUtils.getPropertyValue(
        document.getProperty(), MAP_NAME_PROPERTY);
      String mapDescription = DictionaryUtils.getPropertyValue(
        document.getProperty(), MAP_DESCRIPTION_PROPERTY);
      String category = DictionaryUtils.getPropertyValue(
        document.getProperty(), MAP_CATEGORY_PROPERTY);

      MapGroup mapGroup = mapGroupMap.get(category);
      if (mapGroup == null)
      {
        mapGroup = new MapGroup();
        mapGroup.setCategory(loadCategory(category));
        mapGroupMap.put(category, mapGroup);
      }

      MapView mapView = new MapView();
      mapView.setTitle(mapTitle);
      mapView.setMapName(mapName);
      mapView.setDescription(mapDescription);
      mapGroup.getMapViews().add(mapView);
    }
    List<MapGroup> mapGroups = new ArrayList(mapGroupMap.values());
    // sort MapViewGroups;

    return mapGroups;
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
    Map map = mapDocument.getMap();
    String mapName = map.getName();
    String mapDescription = map.getDescription();
    String docId = getMapDocId(mapName);

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
    document.setTitle(map.getTitle());
    document.getProperty().clear();
    for (Property property : mapDocument.getProperties())
    {
      String propertyName = property.getName();
      if (!propertyName.equals(MAP_NAME_PROPERTY) &&
        !propertyName.equals(MAP_DESCRIPTION_PROPERTY))
      {
        document.getProperty().add(property);
      }
    }
    Property property = new Property();
    property.setName(MAP_NAME_PROPERTY);
    property.getValue().add(mapName);
    document.getProperty().add(property);

    if (!StringUtils.isBlank(mapDescription))
    {
      property = new Property();
      property.setName(MAP_DESCRIPTION_PROPERTY);
      property.getValue().add(mapDescription);
      document.getProperty().add(property);
    }

    document.getAccessControl().clear();
    document.getAccessControl().addAll(mapDocument.getAccessControl());

    Content content = new Content();
    byte[] bytes = map.toString().getBytes("UTF-8");
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

  public MapCategory loadCategory(String category)
  {
    MapCategory mapCategory = categoryCache.get(category);
    if (mapCategory == null)
    {
      mapCategory = new MapCategory();
      mapCategory.setCategory(category);
      mapCategory.setTitle(category);
      mapCategory.setIcon("pi pi-circle");

      DocumentFilter filter = new DocumentFilter();
      filter.setDocTypeId(MAP_CATEGORY_TYPEID);
      Property property = new Property();
      property.setName(MAP_CATEGORY_PROPERTY);
      property.getValue().add(category);
      filter.getProperty().add(property);
      List<Document> documents = getPort().findDocuments(filter);
      if (!documents.isEmpty())
      {
        Document document = documents.get(0);
        mapCategory.setTitle(document.getTitle());
        String position = DictionaryUtils.getPropertyValue(
          document.getProperty(), MAP_CATEGORY_POSITION_PROPERTY);
        mapCategory.setPosition(position);
      }
      categoryCache.put(category, mapCategory);
    }
    return mapCategory;
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
    Map map;
    final List<Property> properties = new ArrayList<>();
    final List<AccessControl> accessControl = new ArrayList<>();
    String creationDate;
    String captureUserId;
    String captureDateTime;
    String changeUserId;
    String changeDateTime;

    public MapDocument()
    {
      map = new Map();
      map.setName("new_map");
      map.setTitle("New map");
    }

    public MapDocument(Map map)
    {
      this.map = map;
    }

    public MapDocument(Document document) throws IOException
    {
      InputStream is = document.getContent().getData().getInputStream();
      this.map = new Map();
      this.map.read(new InputStreamReader(is, "UTF-8"));
      this.properties.addAll(document.getProperty());
      this.setAccessControl(document.getAccessControl());
      this.updateAuditory(document);
    }

    public Map getMap()
    {
      return map;
    }

    public List<Property> getProperties()
    {
      return properties;
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

    public final void updateAuditory(Document document)
    {
      this.creationDate = document.getCreationDate();
      this.changeUserId = document.getChangeUserId();
      this.changeDateTime = document.getChangeDateTime();
      this.captureUserId = document.getCaptureUserId();
      this.captureDateTime = document.getCaptureDateTime();
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
  }

  public static class MapCategory implements Serializable
  {
    String category;
    String title;
    String icon;
    String description;
    String position;

    public String getCategory()
    {
      return category;
    }

    public void setCategory(String category)
    {
      this.category = category;
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
  }

  public static class MapGroup implements Serializable
  {
    MapCategory category;
    List<MapView> mapViews = new ArrayList<>();

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
      return mapViews;
    }

    public void setMapViews(List<MapView> mapViews)
    {
      this.mapViews = mapViews;
    }
  }

  public static class MapView implements Serializable
  {
    String mapName;
    String title;
    String description;

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

    public String getDescription()
    {
      return description;
    }

    public void setDescription(String description)
    {
      this.description = description;
    }
  }

  public static class MapFilter implements Serializable
  {
    String title;
    String category;

    public String getTitle()
    {
      return title;
    }

    public void setTitle(String title)
    {
      this.title = title;
    }

    public String getCategory()
    {
      return category;
    }

    public void setCategory(String category)
    {
      this.category = category;
    }
  }
}
