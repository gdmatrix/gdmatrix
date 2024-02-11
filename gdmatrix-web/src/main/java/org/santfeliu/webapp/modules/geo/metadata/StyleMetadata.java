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
package org.santfeliu.webapp.modules.geo.metadata;

import java.util.List;
import java.util.Map;
import org.santfeliu.faces.maplibre.model.Style;

/**
 *
 * @author realor
 */
public class StyleMetadata
{
  public static final String SERVICES = "services";
  public static final String SERVICE_PARAMETERS = "serviceParameters";
  public static final String LAYER_FORMS = "layerForms";
  public static final String PRINT_REPORTS = "printReports";
  public static final String LEGEND = "legend";
  public static final String PROFILE = "profile";
  public static final String SCRIPTS = "scripts";

  public static void convert(Style style)
  {
    Map<String, Object> services =
      (Map<String, Object>)style.getMetadata().get(SERVICES);
    if (services != null)
    {
      for (String serviceId : services.keySet())
      {
        services.put(serviceId, new Service((Map)services.get(serviceId)));
      }
    }

    Map<String, Object> params =
      (Map<String, Object>)style.getMetadata().get(SERVICE_PARAMETERS);
    if (params != null)
    {
      for (String sourceId : params.keySet())
      {
        params.put(sourceId, new ServiceParameters((Map)params.get(sourceId)));
      }
    }

    List list = (List)style.getMetadata().get(LAYER_FORMS);
    if (list != null)
    {
      for (int i = 0; i < list.size(); i++)
      {
        list.set(i, new LayerForm((Map)list.get(i)));
      }
    }

    list = (List)style.getMetadata().get(PRINT_REPORTS);
    if (list != null)
    {
      for (int i = 0; i < list.size(); i++)
      {
        list.set(i, new PrintReport((Map)list.get(i)));
      }
    }

    Map legendProperties = (Map)style.getMetadata().get(LEGEND);
    if (legendProperties != null &&
        "group".equals(legendProperties.get("type")))
    {
      style.getMetadata().put("legend", new LegendGroup(legendProperties));
    }
  }
}
