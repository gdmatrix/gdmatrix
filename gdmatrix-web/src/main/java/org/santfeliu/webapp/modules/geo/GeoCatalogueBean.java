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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Named;
import org.santfeliu.faces.maplibre.model.Map;
import org.santfeliu.util.template.Template;
import org.santfeliu.util.template.WebTemplate;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.geo.io.MapStore;
import org.santfeliu.webapp.modules.geo.io.MapStore.MapFilter;
import org.santfeliu.webapp.modules.geo.io.MapStore.MapGroup;
import org.santfeliu.webapp.modules.geo.io.MapStore.MapView;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class GeoCatalogueBean extends WebBean implements Serializable
{
  List<MapGroup> mapGroups;
  MapFilter filter = new MapFilter();

  public List<MapGroup> getMapGroups()
  {
    if (mapGroups == null)
    {
      mapGroups = getMapStore().findMaps(filter);
    }
    return mapGroups;
  }

  public String getMapInfo(MapView mapView)
  {
    try
    {
      MapStore.MapDocument mapDocument =
        getMapStore().loadMap(mapView.getMapName());
      Map map = mapDocument.getMap();
      String description = mapDocument.getMap().getDescription();
      description = description.replace(GeoMapBean.DESCRIPTION_BREAK_TAG, "\n");
      Template template = WebTemplate.create(description);
      HashMap<String, Object> variables = new HashMap<>();
      variables.putAll(map.getMetadata());
      variables.put("title", map.getTitle());
      variables.put("creationDate", mapDocument.getCreationDate());
      variables.put("captureDateTime", mapDocument.getCaptureDateTime());
      variables.put("changeDateTime", mapDocument.getChangeDateTime());
      variables.put("captureUserId", mapDocument.getCaptureUserId());
      variables.put("changeUserId", mapDocument.getChangeUserId());
      return template.merge(variables);
    }
    catch (Exception ex)
    {
      error(ex);
      return null;
    }
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
      mapGroups = getMapStore().findMaps(filter);
    }
    catch (Exception ex)
    {
      error(ex);
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
