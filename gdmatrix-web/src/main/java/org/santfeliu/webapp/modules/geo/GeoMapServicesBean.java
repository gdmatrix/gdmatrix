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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.inject.Named;
import org.santfeliu.faces.maplibre.model.Style;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.geo.metadata.Service;
import org.santfeliu.webapp.modules.geo.metadata.ServiceParameters;
import org.santfeliu.webapp.modules.geo.ogc.ServiceCapabilities;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class GeoMapServicesBean extends WebBean implements Serializable
{
  private Service editingService;
  private String editingServiceId;
  private boolean newService;

  private transient List<String> serviceIds;
  private transient ServiceCapabilities serviceCapabilities;
  private transient String capabilitiesServiceId;

  @Inject
  GeoMapBean geoMapBean;

  public String getEditingServiceId()
  {
    return editingServiceId;
  }

  public void setEditingServiceId(String serviceId)
  {
    editingServiceId = serviceId;
  }

  public boolean isNewService()
  {
    return newService;
  }

  public ServiceCapabilities getServiceCapabilities()
  {
    return serviceCapabilities;
  }

  public String getCapabilitiesServiceId()
  {
    return capabilitiesServiceId;
  }

  public void loadServiceCapabilities(String serviceId)
  {
    try
    {
      capabilitiesServiceId = serviceId;
      Service service = geoMapBean.getServiceMap().get(capabilitiesServiceId);
      GeoServiceBean geoServiceBean =
        CDI.current().select(GeoServiceBean.class).get();

      serviceCapabilities =
        geoServiceBean.getServiceCapabilities(service.getUrl(), true);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void refresh()
  {
    serviceIds = null;
  }

  public List<String> getServiceIds()
  {
    if (serviceIds == null)
    {
      serviceIds = new ArrayList<>(geoMapBean.getServiceMap().keySet());
      Collections.sort(serviceIds);
    }
    return serviceIds;
  }

  public Service getEditingService()
  {
    return editingService;
  }

  public void addService()
  {
    editingServiceId = null;
    newService = true;
    editingService = new Service();
    geoMapBean.setDialogVisible(true);
  }

  public void editService(String serviceId)
  {
    editingServiceId = serviceId;
    editingService = geoMapBean.cloneObject(
      geoMapBean.getServiceMap().get(serviceId), Service.class);
    geoMapBean.setDialogVisible(true);
  }

  public void copyService(String serviceId)
  {
    editingServiceId = null;
    newService = true;
    editingService = geoMapBean.cloneObject(
      geoMapBean.getServiceMap().get(serviceId), Service.class);
    geoMapBean.setDialogVisible(true);
  }

  public void removeService(String serviceId)
  {
    editingServiceId = null;
    serviceIds = null;
    Style style = geoMapBean.getStyle();
    // remove cascade sources
    List<String> sourceIdList = new ArrayList<>(style.getSources().keySet());
    for (String sourceId : sourceIdList)
    {
      ServiceParameters serviceParameters =
        geoMapBean.getServiceParameters(sourceId);
      if (serviceParameters != null)
      {
        if (serviceId.equals(serviceParameters.getService()))
        {
          GeoMapSourcesBean geoMapSourcesBean =
            CDI.current().select(GeoMapSourcesBean.class).get();

          geoMapSourcesBean.removeSource(sourceId);
        }
      }
    }
    geoMapBean.getServiceMap().remove(serviceId);
  }

  public void acceptService()
  {
    if (newService)
    {
      if (geoMapBean.getServiceMap().containsKey(editingServiceId))
      {
        error("DUPLICATED_ID");
        return;
      }
      else
      {
        newService = false;
      }
    }
    geoMapBean.getServiceMap().put(editingServiceId, editingService);

    editingServiceId = null;
    editingService = null;
    serviceIds = null;
    geoMapBean.setDialogVisible(false);
  }

  public void cancelService()
  {
    editingServiceId = null;
    editingService = null;
    newService = false;
    geoMapBean.setDialogVisible(false);
  }
}
