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
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Named;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.geo.io.MapStore;
import org.santfeliu.webapp.modules.geo.io.MapStore.MapFilter;
import org.santfeliu.webapp.modules.geo.io.MapStore.MapGroup;

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
