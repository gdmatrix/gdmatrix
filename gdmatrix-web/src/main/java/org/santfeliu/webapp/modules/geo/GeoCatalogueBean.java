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

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.santfeliu.util.IOUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.geo.io.MapCategory;
import org.santfeliu.webapp.modules.geo.io.MapStore;
import org.santfeliu.webapp.modules.geo.io.MapDocument;
import org.santfeliu.webapp.modules.geo.io.MapFilter;
import org.santfeliu.webapp.modules.geo.io.MapGroup;
import org.santfeliu.webapp.modules.geo.io.MapView;
import static org.santfeliu.webapp.modules.geo.io.MapStore.GEO_ADMIN_ROLE;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class GeoCatalogueBean extends WebBean implements Serializable
{
  MapGroup mapGroup;
  MapFilter filter = new MapFilter();
  MapView currentMapView;
  MapCategory currentMapCategory;
  Set<String> expandedCategories = new HashSet<>();

  transient MapDocument currentMapDocument;

  public MapGroup getMapGroup()
  {
    if (mapGroup == null)
    {
      mapGroup = getMapStore().findMaps(filter);
    }
    return mapGroup;
  }

  public MapView getCurrentMapView()
  {
    return currentMapView;
  }

  public void setCurrentMapView(MapView mapView)
  {
    currentMapView = mapView;
  }

  public MapCategory getCurrentMapCategory()
  {
    return currentMapCategory;
  }

  public void setCurrentMapCategory(MapCategory mapCategory)
  {
    currentMapCategory = new MapCategory(mapCategory);
  }

  public void createMap()
  {
    GeoMapBean geoMapBean = CDI.current().select(GeoMapBean.class).get();
    geoMapBean.newMap();
    geoMapBean.setView("map_editor");
  }

  public void createMapCategory()
  {
    currentMapCategory = new MapCategory();
  }

  public void expandMapCategory(String categoryName)
  {
    expandedCategories.add(categoryName);
  }

  public void collapseMapCategory(String categoryName)
  {
    expandedCategories.remove(categoryName);
  }

  public boolean isMapCategoryExpanded(String categoryName)
  {
    return expandedCategories.contains(categoryName);
  }

  public Set<String> getExpandedCategories()
  {
    return expandedCategories;
  }

  public void setExpandedCategories(Set<String> expandedCategories)
  {
    this.expandedCategories = expandedCategories;
  }

  public void showMap(MapView mapView)
  {
    this.currentMapView = mapView;
    GeoMapBean geoMapBean = CDI.current().select(GeoMapBean.class).get();
    geoMapBean.loadMap(mapView.getMapName(), "map_viewer");
  }

  public String getCurrentMapSummary()
  {
    try
    {
      MapDocument mapDocument = getCurrentMapDocument();
      return mapDocument.getMergedSummary();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public boolean isCurrentMapEditable()
  {
    try
    {
      MapDocument mapDocument = getCurrentMapDocument();
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      return  userSessionBean.isUserInRole(GEO_ADMIN_ROLE) ||
        userSessionBean.isUserInRole(mapDocument.getWriteRoles());
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return false;
  }

  public MapFilter getFilter()
  {
    return filter;
  }

  public void setFilter(MapFilter filter)
  {
    this.filter = filter;
  }

  public void findMaps()
  {
    try
    {
      mapGroup = getMapStore().findMaps(filter);
      currentMapCategory = null;
      expandedCategories.clear();

      if (!StringUtils.isBlank(filter.getKeywords()))
      {
        expandMapGroup(mapGroup);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void storeMapCategory()
  {
    try
    {
      getMapStore().storeCategory(currentMapCategory, null);
      GeoCategoryBean geoCategoryBean =
        CDI.current().select(GeoCategoryBean.class).get();
      geoCategoryBean.updateCategories();
      mapGroup = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void uploadCategoryImage(FileUploadEvent event)
  {
    if (currentMapCategory != null)
    {
      UploadedFile imageToUpload = event.getFile();
      File imageFile = null;
      try
      {
        imageFile = File.createTempFile("image", ".svg");
        try (InputStream is = imageToUpload.getInputStream())
        {
          IOUtils.writeToFile(is, imageFile);
        }
        getMapStore().storeCategory(currentMapCategory, imageFile);
        mapGroup = null;
      }
      catch (Exception ex)
      {
        error(ex);
      }
      finally
      {
        currentMapCategory = null;
        try
        {
          if (imageFile != null) imageFile.delete();
          imageToUpload.delete();
        }
        catch (Exception ex2)
        {
        }
      }
    }
  }

  public void cancelMapCategory()
  {
    currentMapCategory = null;
  }

  public boolean isAdminUser()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return userSessionBean.isUserInRole(GEO_ADMIN_ROLE);
  }

  private MapDocument getCurrentMapDocument() throws Exception
  {
    if (currentMapDocument == null && currentMapView != null)
    {
      currentMapDocument = getMapStore().loadMap(currentMapView.getMapName());
    }
    return currentMapDocument;
  }

  private void expandMapGroup(MapGroup mapGroup)
  {
    MapCategory mapCategory = mapGroup.getCategory();
    if (mapCategory != null)
    {
      expandedCategories.add(mapCategory.getName());
    }
    for (MapGroup subMapGroup : mapGroup.getMapGroups())
    {
      expandMapGroup(subMapGroup);
    }
  }

  private MapStore getMapStore()
  {
    MapStore mapStore = CDI.current().select(MapStore.class).get();
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    mapStore.setCredentials(userSessionBean.getUserId(), userSessionBean.getPassword());
    return mapStore;
  }
}
