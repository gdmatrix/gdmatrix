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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author realor
 */
public class MapGroup implements Serializable
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
