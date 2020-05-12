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
package org.santfeliu.misc.mapviewer.web;

import java.util.HashMap;
import java.util.List;
import javax.faces.application.Application;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.misc.mapviewer.Bounds;
import org.santfeliu.misc.mapviewer.Map;
import org.santfeliu.misc.mapviewer.MapCategory;
import org.santfeliu.misc.mapviewer.MapDocument;
import org.santfeliu.misc.mapviewer.MapStore;
import org.santfeliu.misc.mapviewer.MapView;
import org.santfeliu.util.template.Template;
import org.santfeliu.util.template.WebTemplate;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author realor
 */
public class MapBean extends FacesBean
{
  /* Map properties */
  public static final String LEFT_PANEL_WIDTH_PROPERTY = "leftPanelWidth";
  public static final String RIGHT_PANEL_WIDTH_PROPERTY = "rightPanelWidth";
  public static final String LEFT_PANEL_VISIBLE_PROPERTY = "leftPanelVisible";
  public static final String RIGHT_PANEL_VISIBLE_PROPERTY = "rightPanelVisible";
  public static final String EXPAND_GROUPS_PROPERTY = "expandGroups";
  public static final String SCRIPT_FILES_PROPERTY = "scripts";
  public static final String MAP_BACKGROUND_PROPERTY = Map.MAP_BACKGROUND_PROPERTY;
  public static final String FEATURE_LOCATORS_PROPERTY = "featureLocators";
  public static final String EXPORT_FORMATS_PROPERTY = "exportFormats";
  public static final String SEARCH_ON_LOAD_PROPERTY = "searchOnLoad";
  public static final String PRINT_REPORTS_PROPERTY = "printReports";
  public static final String CSS_THEME_PROPERTY = "cssTheme";
  
  private int thumbnailWidth = 300;
  private int thumbnailHeight = 240;

  private MapDocument map = new MapDocument();
  
  public static MapBean getInstance()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    if (context == null) // not in FacesContext
    {
      return null;
    }
    else
    {
      Application application = context.getApplication();
      return (MapBean)application.getVariableResolver().
        resolveVariable(context, "mapBean");
    }
  }

  public MapDocument getMap()
  {
    return map;
  }

  public void setMap(MapDocument map)
  {
    this.map = map;
  }

  public int getThumbnailWidth()
  {
    return thumbnailWidth;
  }

  public void setThumbnailWidth(int thumbnailWidth)
  {
    this.thumbnailWidth = thumbnailWidth;
  }

  public int getThumbnailHeight()
  {
    return thumbnailHeight;
  }

  public void setThumbnailHeight(int thumbnailHeight)
  {
    this.thumbnailHeight = thumbnailHeight;
  }

  public String getBaseUrl()
  {
    return getContextURL();
  }

  public String getDescription()
  {
    String description = map.getDescription();
    if (description == null) return null;
    description = description.replace(Map.DESCRIPTION_BREAK_TAG, "\n");
    Template template = WebTemplate.create(description);
    HashMap<String, String> variables = new HashMap<String, String>();
    variables.putAll(map.getMetadata());
    variables.put("docId", map.getDocId());
    variables.put("title", map.getTitle());
    variables.put("creationDate", map.getCreationDate());
    variables.put("captureDateTime", map.getCaptureDateTime());
    variables.put("changeDateTime", map.getChangeDateTime());
    variables.put("captureUserId", map.getCaptureUserId());
    variables.put("changeUserId", map.getChangeUserId());
    return template.merge(variables);
  }
  
  // actions
  public void newMap()
  {
    map = new MapDocument();
  }

  public void loadMap(String mapName) throws Exception
  {
    MapStore mapStore = getMapStore();
    map = mapStore.loadMap(mapName);
  }

  public void reloadMap() throws Exception
  {
    MapStore mapStore = getMapStore();
    map = mapStore.loadMap(map.getName());
  }

  public void storeMap(boolean updateThumbnail) throws Exception
  {
    checkMap();
    MapStore mapStore = getMapStore();
    if (updateThumbnail)
    {
      Bounds thumbnailBounds = map.getThumbnailBounds();
      if (thumbnailBounds == null)
      {
        thumbnailBounds = getCurrentBounds();
        map.setThumbnailBounds(thumbnailBounds);
      }
      mapStore.saveThumbnail(map, thumbnailBounds, getBaseUrl(),
        thumbnailWidth, thumbnailHeight);
    }
    mapStore.storeMap(map);
  }

  public void deleteMap() throws Exception
  {
    MapStore mapStore = getMapStore();
    mapStore.deleteMap(map);
  }

  public int countMapViews(String propertyName, String searchText, 
    String category) throws Exception
  {
    MapStore mapStore = getMapStore();
    return mapStore.countMapViews(propertyName, searchText, category);
  }

  public List<MapView> findMapViews(String propertyName, String searchText, 
    String category, int firstResult, int maxResults) throws Exception
  {
    MapStore mapStore = getMapStore();
    return mapStore.findMapViews(propertyName, searchText, category, 
      firstResult, maxResults);
  }
  
  public List<MapCategory> findMapCategories() throws Exception
  {
    MapStore mapStore = getMapStore();
    return mapStore.findMapCategories();
  }
    
  private void checkMap() throws Exception
  {
    if (StringUtils.isBlank(map.getName()))
      throw new Exception("MAP_NAME_IS_MANDATORY");
    if (!map.isComplete()) throw new Exception("MAP_IS_NOT_COMPLETE");
  }

  private MapStore getMapStore()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MapStore mapStore = new MapStore(userSessionBean.getCredentials());
    return mapStore;
  }

  private Bounds getCurrentBounds()
  {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    ExternalContext externalContext = facesContext.getExternalContext();
    java.util.Map cookieMap = externalContext.getRequestCookieMap();
    try
    {
      Cookie cookie = (Cookie)cookieMap.get("map_extent");
      if (cookie != null)
      {
        String value = cookie.getValue();
        value = value.replace("%2C", ",");
        return new Bounds(value);
      }
    }
    catch (Exception ex)
    {
    }
    return null;
  }
}
