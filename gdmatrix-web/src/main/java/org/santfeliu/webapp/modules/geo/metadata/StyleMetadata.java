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

import java.util.ArrayList;
import java.util.HashMap;
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

  Style style;

  public StyleMetadata(Style style)
  {
    this.style = style;
  }

  public Style getStyle()
  {
    return style;
  }

  public Map<String, Service> getServiceMap(boolean create)
  {
    Map<String, Service> serviceMap =
      (Map<String, Service>)style.getMetadata().get(SERVICES);

    if (serviceMap == null)
    {
      if (create)
      {
        serviceMap = new HashMap<>();
        style.getMetadata().put(SERVICES, serviceMap);
      }
      return serviceMap;
    }

    if (!serviceMap.isEmpty())
    {
      for (String serviceId : serviceMap.keySet())
      {
        Object item = serviceMap.get(serviceId);
        if (item instanceof Map) // from gson
        {
          serviceMap.put(serviceId, new Service((Map)item));
        }
        else break;
      }
    }
    return serviceMap;
  }

  public Service getService(String serviceId, boolean create)
  {
    Map<String, Service> serviceMap = getServiceMap(create);

    if (serviceMap == null) return null;

    Service service = serviceMap.get(serviceId);
    if (service == null)
    {
      if (create)
      {
        service = new Service();
        serviceMap.put(serviceId, service);
      }
    }
    return service;
  }

  public Map<String, ServiceParameters> getServiceParametersMap(boolean create)
  {
    Map<String, ServiceParameters> serviceParametersMap =
      (Map<String, ServiceParameters>)style.getMetadata().get(SERVICE_PARAMETERS);

    if (serviceParametersMap == null)
    {
      if (create)
      {
        serviceParametersMap = new HashMap<>();
        style.getMetadata().put(SERVICE_PARAMETERS, serviceParametersMap);
      }
      return serviceParametersMap;
    }

    for (String sourceId : serviceParametersMap.keySet())
    {
      Object item = serviceParametersMap.get(sourceId);
      if (item instanceof Map)
      {
        serviceParametersMap.put(sourceId, new ServiceParameters((Map)item));
      }
      else break;
    }
    return serviceParametersMap;
  }

  public ServiceParameters getServiceParameters(String sourceId, boolean create)
  {
    Map<String, ServiceParameters> serviceParametersMap =
      getServiceParametersMap(create);

    if (serviceParametersMap == null) return null;

    ServiceParameters serviceParameters = serviceParametersMap.get(sourceId);
    if (serviceParameters == null && create)
    {
      serviceParameters = new ServiceParameters();
      serviceParametersMap.put(sourceId, serviceParameters);
    }
    return serviceParameters;
  }

  public List<LayerForm> getLayerForms(boolean create)
  {
    List<LayerForm> list = (List<LayerForm>)style.getMetadata().get(LAYER_FORMS);
    if (list == null)
    {
      if (create)
      {
        list = new ArrayList<>();
        style.getMetadata().put(LAYER_FORMS, list);
      }
    }
    else
    {
      for (int i = 0; i < list.size(); i++)
      {
        Object item = list.get(i);
        if (item instanceof Map) // from gson
        {
          list.set(i, new LayerForm((Map)item));
        }
        else break;
      }
    }
    return list;
  }

  public List<PrintReport> getPrintReports(boolean create)
  {
    List<PrintReport> list = (List<PrintReport>)style.getMetadata().get(PRINT_REPORTS);
    if (list == null)
    {
      if (create)
      {
        list = new ArrayList<>();
        style.getMetadata().put(PRINT_REPORTS, list);
      }
    }
    else
    {
      for (int i = 0; i < list.size(); i++)
      {
        Object item = list.get(i);
        if (item instanceof Map) // from gson
        {
          list.set(i, new PrintReport((Map)item));
        }
        else break;
      }
    }
    return list;
  }

  public LegendGroup getLegend(boolean create)
  {
    LegendGroup legendGroup = null;
    Object legend = style.getMetadata().get(LEGEND);

    if (legend instanceof LegendGroup)
    {
      legendGroup = (LegendGroup)legend;
    }
    else if (legend instanceof Map &&
      "group".equals(((Map)legend).get("type")))
    {
      legendGroup = new LegendGroup((Map)legend);
      style.getMetadata().put(LEGEND, legendGroup);
    }
    else
    {
      if (create)
      {
        legendGroup = new LegendGroup();
        style.getMetadata().put(LEGEND, legendGroup);
      }
    }
    return legendGroup;
  }
}
