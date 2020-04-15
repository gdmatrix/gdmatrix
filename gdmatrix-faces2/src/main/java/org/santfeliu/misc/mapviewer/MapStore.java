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
package org.santfeliu.misc.mapviewer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Property;
import org.matrix.dic.PropertyDefinition;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentConstants;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.OrderByProperty;
import org.matrix.security.AccessControl;
import org.matrix.security.SecurityConstants;
import org.matrix.translation.TranslationConstants;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.misc.mapviewer.io.MapReader;
import org.santfeliu.misc.mapviewer.io.MapWriter;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.util.MemoryDataSource;
import org.santfeliu.misc.mapviewer.Map.Service;
import org.santfeliu.misc.mapviewer.Map.Layer;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.template.Template;

/**
 *
 * @author realor
 */
public class MapStore
{
  public static final String MAP_TYPEID = "XMAP";
  public static final String MAP_THUMBNAIL_TYPEID = "IMAGE";
  public static final String MAP_NAME_PROPERTY = "mapName";
  public static final String MAP_DESCRIPTION_PROPERTY = "mapDescription";
  public static final String MAP_THUMBNAIL_PROPERTY = "thumbnailDocId";

  public static final String CATEGORY_TYPEID = "XMAPCAT";
  public static final String CATEGORY_NAME_PROPERTY = "category";
  public static final String CATEGORY_DESCRIPTION_PROPERTY = "description";  
  public static final String CATEGORY_POSITION_PROPERTY = "position";
  
  public static final int CREATE_OR_UPDATE = 0;
  public static final int CREATE = 1;
  public static final int UPDATE = 2;

  public static final String THUMBNAIL_FORMAT = "jpeg";
  private final DocumentManagerClient docClient;

  public MapStore(Credentials credentials)
  {
    docClient = new DocumentManagerClient(
      credentials.getUserId(), credentials.getPassword());
  }

  public MapStore(String userId, String password)
  {
    docClient = new DocumentManagerClient(userId, password);
  }

  public MapStore(DocumentManagerClient docClient)
  {
    this.docClient = docClient;
  }

  public Document getMapDocumentByName(String mapName)
  {
    DocumentFilter filter = new DocumentFilter();
    filter.setDocTypeId(MAP_TYPEID);
    Property property = new Property();
    property.setName(MAP_NAME_PROPERTY);
    property.getValue().add(mapName);
    filter.getProperty().add(property);
    filter.setMaxResults(1);
    List<Document> list = docClient.findDocuments(filter);
    return list.isEmpty() ? null : list.get(0);
  }

  public Document getMapDocumentByDocId(String docId)
  {
    try
    {
      return docClient.loadDocument(docId, 0, ContentInfo.ALL);
    }
    catch (Exception ex)
    {
      return null;
    }
  }
  
  public MapDocument loadMap(String mapName) throws Exception
  {
    MapDocument map;
    Document document = getMapDocumentByName(mapName);
    if (document != null)
    {
      document = docClient.loadDocument(document.getDocId(), 0, ContentInfo.ALL);
      Content content = document.getContent();
      InputStream is = content.getData().getInputStream();
      MapReader mapReader = new MapReader();
      map = new MapDocument(mapReader.read(is));
      map.setName(mapName);
      map.setDocId(document.getDocId());
      for (Property property : document.getProperty())
      {        
        map.getMetadata().put(property.getName(), property.getValue().get(0));
      }
      map.setCreationDate(document.getCreationDate());
      map.setCaptureUserId(document.getCaptureUserId());
      map.setCaptureDateTime(document.getCaptureDateTime());
      map.setChangeUserId(document.getChangeUserId());
      map.setChangeDateTime(document.getChangeDateTime());
      Property property =
        DictionaryUtils.getProperty(document, MAP_THUMBNAIL_PROPERTY);
      if (property != null)
      {
        map.setThumbnailDocId(property.getValue().get(0));
      }
      property =
        DictionaryUtils.getProperty(document, CATEGORY_NAME_PROPERTY);
      if (property != null)
      {
        map.setCategory(property.getValue().get(0));
      }
      map.getReadRoles().clear();
      map.getWriteRoles().clear();
      for (AccessControl acl : document.getAccessControl())
      {
        String action = acl.getAction();
        if (action.equals(DictionaryConstants.READ_ACTION))
        {
          map.getReadRoles().add(acl.getRoleId());
        }
        else if (action.equals(DictionaryConstants.WRITE_ACTION))
        {
          map.getWriteRoles().add(acl.getRoleId());
        }
      }
      return map;
    }
    else throw new Exception("MAP_NOT_FOUND");
  }

  public void storeMap(MapDocument map) throws Exception
  {
    storeMap(map, map.getDocId() == null ? CREATE : UPDATE);
  }

  public void storeMap(MapDocument map, int mode) throws Exception
  {
    String mapName = map.getName();
    Document document;
    if (mode == CREATE)
    {
      document = getMapDocumentByName(mapName);
      if (document == null)
      {
        document = new Document();
      }
      else // map already exists
      {
        throw new Exception("MAP_ALREADY_EXISTS");
      }
    }
    else // UPDATE
    {
      String docId = map.getDocId();
      if (docId == null)
      {
        throw new Exception("MAP_CAN_NOT_BE_UPDATED");
      }
      else
      {
        String oldMapName = null;
        document = getMapDocumentByDocId(docId);
        if (document == null) // document was removed!, create it again
        {
          document = new Document();
        }
        else
        {
          Property nameProperty = 
            DictionaryUtils.getProperty(document, MAP_NAME_PROPERTY);
          if (nameProperty != null)
          {
            oldMapName = nameProperty.getValue().get(0);
          }
        }
        if (!mapName.equals(oldMapName)) // name was changed!
        {
          if (getMapDocumentByName(mapName) != null)
          {
            throw new Exception("MAP_ALREADY_EXISTS");
          }
        }
      }
    }

    document.setTitle(map.getTitle());
    document.setDocTypeId(MAP_TYPEID);
    document.setCreationDate(map.getCreationDate());
    for (Object prop : map.getMetadata().keySet())
    {
      String propName = String.valueOf(prop);
      Object propValue = map.getMetadata().get(propName);
      DictionaryUtils.setProperty(document, propName, propValue);
    }
    DictionaryUtils.setProperty(document, MAP_NAME_PROPERTY, mapName);
    DictionaryUtils.setProperty(document, MAP_DESCRIPTION_PROPERTY,
      map.getDescription());
    if (!StringUtils.isBlank(map.getCategory()))
    {
      DictionaryUtils.setProperty(document, CATEGORY_NAME_PROPERTY, 
        map.getCategory());
    }
    if (!StringUtils.isBlank(map.getThumbnailDocId()))
    {
      DictionaryUtils.setProperty(document, MAP_THUMBNAIL_PROPERTY,
        map.getThumbnailDocId());
    }

    MapWriter writer = new MapWriter();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    writer.write(map, bos);
    Content content = new Content();
    DataSource ds = new MemoryDataSource(bos.toByteArray(), "map", "text/xml");
    content.setData(new DataHandler(ds));
    document.setContent(content);
    // set roles from document.getAccessControl()
    document.getAccessControl().clear();
    for (String readRole : map.getReadRoles())
    {
      AccessControl acl = new AccessControl();
      acl.setAction(DictionaryConstants.READ_ACTION);
      acl.setRoleId(readRole);
      document.getAccessControl().add(acl);
    }
    for (String writeRole : map.getWriteRoles())
    {
      AccessControl acl = new AccessControl();
      acl.setAction(DictionaryConstants.WRITE_ACTION);
      acl.setRoleId(writeRole);
      document.getAccessControl().add(acl);
    }
    document = docClient.storeDocument(document);
    map.setDocId(document.getDocId());
    map.setCreationDate(document.getCreationDate());
    map.setChangeUserId(document.getChangeUserId());
    map.setChangeDateTime(document.getChangeDateTime());
  }

  public void saveThumbnail(MapDocument map, Bounds bounds,
    String baseUrl, int width, int height) throws Exception
  {
    List<URL> urls = getThumbnailUrls(map, bounds, baseUrl,
      2 * width, 2 * height);
    BufferedImage doubleImage = new BufferedImage(2 * width, 2 * height,
      BufferedImage.TYPE_INT_ARGB);
    Graphics g = doubleImage.getGraphics();
    String scolor = map.getProperties().get(Map.MAP_BACKGROUND_PROPERTY);
    Color color = scolor == null ? Color.WHITE : parseColor(scolor);
    g.setColor(color);
    g.fillRect(0, 0, 2 * width, 2 * height);

    for (URL url : urls)
    {
      BufferedImage layerImage = ImageIO.read(url);
      g.drawImage(layerImage, 0, 0, null);
    }
    g.dispose();

    BufferedImage image =
      new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    g = image.getGraphics();
    Image tmpImage =
      doubleImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    g.drawImage(tmpImage, 0, 0, width, height, null);
    g.dispose();

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    if (THUMBNAIL_FORMAT.equals("jpeg"))
    {
      ImageOutputStream ios = ImageIO.createImageOutputStream(os);
      ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
      ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
      jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
      jpgWriteParam.setCompressionQuality(0.9f);

      jpgWriter.setOutput(ios);
      IIOImage outputImage = new IIOImage(image, null, null);
      jpgWriter.write(null, outputImage, jpgWriteParam);
      jpgWriter.dispose();
    }
    else
    {
      ImageIO.write(image, THUMBNAIL_FORMAT, os);
    }
    Document document = new Document();
    document.setDocId(map.getThumbnailDocId());
    String contentType = "image/" + THUMBNAIL_FORMAT;
    DataHandler dh = new DataHandler(new MemoryDataSource(os.toByteArray(), 
      "thumbnail", contentType));

    Content content = new Content();
    content.setData(dh);
    content.setContentType(contentType);
    document.setContent(content);
    document.setTitle(map.getTitle());
    document.setDocTypeId(MAP_THUMBNAIL_TYPEID);
    document.setLanguage(TranslationConstants.UNIVERSAL_LANGUAGE);

    AccessControl readAcl = new AccessControl();
    readAcl.setRoleId(SecurityConstants.EVERYONE_ROLE);
    readAcl.setAction(DictionaryConstants.READ_ACTION);
    document.getAccessControl().add(readAcl);

    AccessControl writeAcl = new AccessControl();
    writeAcl.setRoleId(SecurityConstants.EVERYONE_ROLE);
    writeAcl.setAction(DictionaryConstants.WRITE_ACTION);
    document.getAccessControl().add(writeAcl);

    AccessControl deleteAcl = new AccessControl();
    deleteAcl.setRoleId(SecurityConstants.EVERYONE_ROLE);
    deleteAcl.setAction(DictionaryConstants.DELETE_ACTION);
    document.getAccessControl().add(deleteAcl);

    document.setIncremental(true);
    document = docClient.storeDocument(document);
    map.setThumbnailDocId(document.getDocId());
  }

  public void deleteMap(MapDocument map) throws Exception
  {
    String thumbnailDocId = map.getThumbnailDocId();
    if (thumbnailDocId != null)
    {
      docClient.removeDocument(thumbnailDocId, 0);
    }
    String docId = map.getDocId();
    docClient.removeDocument(docId, 0);
  }

  public int countMapViews(String propertyName, String searchText, 
    String category) throws Exception
  {
    DocumentFilter filter = createMapFilter(propertyName, searchText, category);
    return docClient.countDocuments(filter);
  }

  public List<MapView> findMapViews(String propertyName, String searchText, 
    String category, int firstResult, int maxResults) throws Exception
  {
    List<MapView> mapViews = new ArrayList<MapView>();
    DocumentFilter filter = createMapFilter(propertyName, searchText, category);
    filter.setFirstResult(firstResult);
    filter.setMaxResults(maxResults);
    TypeCache typeCache = TypeCache.getInstance();
    Type mapType = typeCache.getType(MapStore.MAP_TYPEID);
    List<PropertyDefinition> propDefs = mapType.getPropertyDefinition();
    for (PropertyDefinition propDef : propDefs)
    {
      if (!propDef.isHidden())
      {
        filter.getOutputProperty().add(propDef.getName());
      }
    }
    OrderByProperty orderBy;
    orderBy = new OrderByProperty();
    orderBy.setName(DocumentConstants.TITLE);
    orderBy.setDescending(false);
    filter.getOrderByProperty().add(orderBy);

    orderBy = new OrderByProperty();
    orderBy.setName(DocumentConstants.CREATION_DATE);
    orderBy.setDescending(true);
    filter.getOrderByProperty().add(orderBy);

    List<Document> documents = docClient.findDocuments(filter);
    if (!documents.isEmpty())
    {
      DocumentFilter thumbsFilter = new DocumentFilter();
      HashMap<String, MapView> mapViewCache = new HashMap<String, MapView>();
      for (Document document : documents)
      {
        HashMap<String, String> variables = new HashMap<String, String>();
        for (Property property : document.getProperty())
        {
          variables.put(property.getName(), property.getValue().get(0));
        }
        variables.put(DocumentConstants.DOCID, document.getDocId());
        variables.put(DocumentConstants.TITLE, document.getTitle());
        variables.put(DocumentConstants.CREATION_DATE, document.getCreationDate());
        variables.put("captureDateTime", document.getCaptureDateTime());
        variables.put("changeDateTime", document.getChangeDateTime());
        variables.put("captureUserId", document.getCaptureUserId());
        variables.put("changeUserId", document.getChangeUserId());
        String thumbnailDocId = variables.get(MAP_THUMBNAIL_PROPERTY);
        String description = variables.get(MAP_DESCRIPTION_PROPERTY);
        if (description != null)
        {
          int index = description.indexOf(Map.DESCRIPTION_BREAK_TAG);
          if (index != -1)
          {
            description = description.substring(0, index);
          }
          else
          {
            index = description.indexOf("\n");
            if (index != -1)
            {
              description = description.substring(0, index);
            }
          }
          Template template = Template.create(description);
          description = template.merge(variables);
        }
        MapView mapView = new MapView();
        mapView.setName(variables.get(MAP_NAME_PROPERTY));
        mapView.setTitle(document.getTitle());
        mapView.setCreationDate(document.getCreationDate());
        mapView.setDescription(description);
        mapView.setThumbnailDocId(thumbnailDocId);
        mapViews.add(mapView);
        mapViewCache.put(thumbnailDocId, mapView);
        thumbsFilter.getDocId().add(thumbnailDocId);
      }
      thumbsFilter.setIncludeContentMetadata(false);
      List<Document> thumbs = docClient.findDocuments(thumbsFilter);
      for (Document thumb : thumbs)
      {
        String docId = thumb.getDocId();
        MapView mapView = mapViewCache.get(docId);
        if (mapView != null)
        {
          mapView.setThumbnailContentId(thumb.getContent().getContentId());
        }
      }
    }
    return mapViews;
  }

  public List<URL> getThumbnailUrls(MapDocument map, Bounds bounds,
    String baseUrl, int width, int height) throws Exception
  {
    if (map.getLayers().isEmpty()) return Collections.EMPTY_LIST;
    List<URL> urls = new ArrayList<URL>();
    if (bounds == null) bounds = map.getBounds();

    String port = MatrixConfig.getProperty("org.santfeliu.web.defaultPort");
    String contextPath = MatrixConfig.getProperty("contextPath");
    
    boolean baseAdded = false;
    for (Layer layer : map.getLayers())
    {
      if (layer.isVisible() && (!baseAdded || !layer.isBaseLayer()))
      {
        Service service = layer.getService();
        String layersString =
          URLEncoder.encode(layer.getNamesString(), "UTF-8");
        String url = "http://localhost:" + port + contextPath + 
          "/proxy?url=" + service.getUrl() +
         "&SERVICE=WMS&VERSION=1.1.0&REQUEST=GetMap&LAYERS=" + layersString +
         "&BBOX=" + bounds.getAdjusted(width, height) +
         "&WIDTH=" + width +
         "&HEIGHT=" + height + "&srs="+ map.getSrs() +
         "&TRANSPARENT=true" +
         "&FORMAT=image/png";
        String stylesString = layer.getStylesString();
        if (!StringUtils.isBlank(stylesString))
        {
          stylesString = URLEncoder.encode(stylesString, "UTF-8");
          url += "&STYLES=" + stylesString;
        }
        else
        {
          url += "&STYLES=";
        }
        String sld = layer.getSld();
        if (!StringUtils.isBlank(sld))
        {
          String sldUrl = SLDStore.getSldURL(sld);
          url += "&SLD=" + sldUrl;
        }
        String cqlFilter = layer.getCqlFilter();
        if (!StringUtils.isBlank(cqlFilter))
        {
          cqlFilter = URLEncoder.encode(cqlFilter, "UTF-8");
          url += "&CQL_FILTER=" + cqlFilter;
        }
        urls.add(new URL(url));
        if (layer.isBaseLayer()) baseAdded = true;
      }
    }
    return urls;
  }
  
  public List<MapCategory> findMapCategories()
  {
    List<MapCategory> mapCategories = new ArrayList<MapCategory>();
    DocumentFilter filter = new DocumentFilter();
    filter.setDocTypeId(CATEGORY_TYPEID);
    filter.setFirstResult(0);
    filter.setMaxResults(0);
    filter.getOutputProperty().add(CATEGORY_NAME_PROPERTY);
    filter.getOutputProperty().add(CATEGORY_DESCRIPTION_PROPERTY);

    OrderByProperty orderBy;
    orderBy = new OrderByProperty();
    orderBy.setName(CATEGORY_POSITION_PROPERTY);
    orderBy.setDescending(false);
    filter.getOrderByProperty().add(orderBy);

    orderBy = new OrderByProperty();
    orderBy.setName("title");
    orderBy.setDescending(false);
    filter.getOrderByProperty().add(orderBy);
    
    List<Document> documents = docClient.findDocuments(filter);
    for (Document document : documents)
    {
      String name = null;
      String description = null;
      Property property;
      property = DictionaryUtils.getProperty(document, CATEGORY_NAME_PROPERTY);
      if (property != null) name = property.getValue().get(0);
      property = DictionaryUtils.getProperty(document, CATEGORY_DESCRIPTION_PROPERTY);
      if (property != null) description = property.getValue().get(0);

      MapCategory mapCategory = new MapCategory();
      mapCategory.setName(name);
      mapCategory.setTitle(document.getTitle());
      mapCategory.setCreationDate(document.getCreationDate());
      mapCategory.setDescription(description);
      mapCategory.setThumbnailDocId(document.getDocId());
      mapCategory.setThumbnailContentId(document.getContent().getContentId());
      mapCategories.add(mapCategory);
    }
    return mapCategories;
  }
  
  private DocumentFilter createMapFilter(String propertyName, String searchText, 
    String category)
  {
    DocumentFilter filter = new DocumentFilter();
    filter.setDocTypeId(MAP_TYPEID);
    if (!StringUtils.isBlank(searchText))
    {
      String pattern = "%" + searchText + "%";
      if (propertyName == null || propertyName.equals(DocumentConstants.TITLE))
      {
        filter.setTitle(pattern);
      }
      else
      {
        Property property = new Property();
        property.setName(propertyName);
        property.getValue().add(pattern);
        filter.getProperty().add(property);
      }
    }
    else if (!StringUtils.isBlank(category))
    {
      Property property = new Property();
      property.setName(CATEGORY_NAME_PROPERTY);
      property.getValue().add(category);
      filter.getProperty().add(property);
    }
    return filter;
  }
  
  private Color parseColor(String scolor)
  {
    Color color = null;
    try
    {
      if (scolor.startsWith("#"))
      {
        color = Color.decode(scolor);
      }
      else
      {
        color = colorTable.get(scolor.toLowerCase());
      }
    }
    catch (Exception ex)
    {
    }
    return color == null ? Color.WHITE : color;
  }

  static final HashMap<String, Color> colorTable = new HashMap();
  static
  {
    colorTable.put("white", Color.WHITE);
    colorTable.put("black", Color.BLACK);
    colorTable.put("red", Color.RED);
    colorTable.put("green", Color.GREEN);
    colorTable.put("blue", Color.BLUE);
    colorTable.put("gray", Color.GRAY);
    colorTable.put("yellow", Color.YELLOW);
    colorTable.put("orange", Color.ORANGE);
    colorTable.put("magenta", Color.MAGENTA);
    colorTable.put("cyan", Color.CYAN);
  }
}
