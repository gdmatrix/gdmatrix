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
package org.santfeliu.webapp.modules.geo.pdfgen;

import java.text.SimpleDateFormat;
import java.util.Map;
import org.santfeliu.faces.maplibre.model.Style;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.util.PojoUtils;
import org.santfeliu.webapp.modules.geo.io.MapStore;
import org.santfeliu.webapp.modules.geo.io.MapStore.MapDocument;

/**
 *
 * @author realor
 */
public class MapContext
{
  public static final String MAP = "map";
  public static final String MAP_NAME = "map_name";
  public static final String BBOX = "bbox";
  public static final String BOUNDS = "bounds";
  public static final String CREDENTIALS = "credentials";
  public static final String LAYER_VISIBILITY = "layer_visibility";
  public static final String SCALE = "scale";
  public static final String SCALE_LABEL = "scale_label";
  public static final String DATE_TIME = "date_time";
  public static final String SRS = "srs";
  public static final String LAST_X = "last_x";
  public static final String LAST_Y = "last_y";
  public static final String HILIGHT_SERVICE = "hilight_service";
  public static final String HILIGHT_LAYER = "hilight_layer";
  public static final String HILIGHT_GEOMETRY = "hilight_geometry";
  public static final String HILIGHT_STYLE = "hilight_style";

  public static final String DEFAULT_SRS = "EPSG:25831";

  public static void init(Map context) throws Exception
  {
    MapDocument map = (MapDocument)context.get(MAP);
    if (map == null)
    {
      // map
      String mapName = (String)context.get(MAP_NAME);
      if (mapName == null) throw new Exception("map_name is not defined!");
      Credentials credentials = (Credentials)context.get(CREDENTIALS);
      if (credentials == null) credentials = new Credentials();
      MapStore mapStore = new MapStore();
      mapStore.setCredentials(credentials.getUserId(), credentials.getPassword());
      map = mapStore.loadMap(mapName);
      context.put(MAP, map);

      Style style = map.getStyle();

      // bounds
      String bbox = (String)context.get(BBOX);
      Bounds bounds;
      if (bbox != null)
      {
        bounds = new Bounds(bbox);
      }
      else
      {
        double[] center = style.getCenter();
        double zoom = style.getZoom();
        bounds = new Bounds(center[0], center[1], zoom);
      }
      context.put(BOUNDS, bounds);

      if (context.get(SRS) == null)
      {
        context.put(SRS, DEFAULT_SRS);
      }

      // dateTime
      SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
      String dateTime = df.format(new java.util.Date());
      context.put(DATE_TIME, dateTime);

      // scale
      String scale = (String)context.get(SCALE);
      double scaleValue = 0;
      try
      {
        if (scale != null)
        {
          scaleValue = Double.parseDouble(scale);
          if (scaleValue > 0)
          {
            String scaleLabel = "1:" + scale;
            context.put(SCALE_LABEL, scaleLabel);
          }
        }
      }
      catch (NumberFormatException ex)
      {
      }
      context.put(SCALE, scaleValue);
    }
  }

  public static Object getProperty(Map context, String name)
  {
    MapDocument map = (MapDocument)context.get(MAP);
    Object value = context.get(name);
    if (value == null)
    {
      value = map.getStyle().getMetadata().get(name);
    }
    if (value == null)
    {
      value = PojoUtils.getStaticProperty(map, name);
    }
    return value;
  }
}
