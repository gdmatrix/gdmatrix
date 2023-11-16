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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.activation.DataHandler;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.DocumentManagerPort;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.faces.maplibre.model.Map;
import org.santfeliu.faces.maplibre.model.Source;
import org.santfeliu.webapp.modules.doc.DocModuleBean;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class MapStore
{
  public static final String MAP_TYPEID = "GMAP";
  public static final String MAP_NAME_PROPERTY = "mapName";

  private String userId = "anonymous";
  private String password;

  public void setCredentials(String userId, String password)
  {
    this.userId = userId;
    this.password = password;
  }

  public List<MapItem> findMaps()
  {
    List<MapItem> mapItems = new ArrayList<>();
    DocumentFilter filter = new DocumentFilter();
    filter.setDocTypeId(MAP_TYPEID);
    filter.getOutputProperty().add(MAP_NAME_PROPERTY);
    List<Document> documents = getPort().findDocuments(filter);
    for (Document document : documents)
    {
      String title = document.getTitle();
      String mapName = DictionaryUtils.getPropertyValue(document.getProperty(), "mapName");
      MapItem mapItem = new MapItem();
      mapItem.setTitle(title);
      mapItem.setMapName(mapName);
      mapItems.add(mapItem);
    }
    return mapItems;
  }

  public Map loadMap(String mapName) throws Exception
  {
    String docId = getMapDocId(mapName);
    if (docId != null)
    {
      Document document = getPort().loadDocument(docId, 0, ContentInfo.ALL);
      InputStream is = document.getContent().getData().getInputStream();
      Map map = new Map();
      map.read(new InputStreamReader(is, "UTF-8"));
      return map;
    }
    return null;
  }

  public boolean storeMap(Map map, boolean isNewMap)
    throws Exception
  {
    String mapName = map.getName();
    String docId = getMapDocId(mapName);

    DocumentManagerPort port = getPort();
    Document document;
    if (docId == null)
    {
      document = new Document();
      document.setDocTypeId(MAP_TYPEID);
      Property property = new Property();
      property.setName(MAP_NAME_PROPERTY);
      property.getValue().add(mapName);
      document.getProperty().add(property);
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
    document.setTitle(map.getTitle());
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

    DocumentManagerPort port = getPort();
    port.removeDocument(docId, 0);
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

  private DocumentManagerPort getPort()
  {
    return DocModuleBean.getPort(userId, password);
  }

  public static class MapItem implements Serializable
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
  }
}
