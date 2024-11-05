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
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.santfeliu.faces.maplibre.model.Style;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.geo.io.MapDocument;
import org.santfeliu.webapp.modules.geo.io.MapStore;
import org.santfeliu.webapp.modules.geo.metadata.StyleMetadata;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class GeoMapImportBean extends WebBean implements Serializable
{
  String mapName;
  boolean dialogVisible;
  int dataToImport;
  String layersPosition;
  String legendPosition;

  @Inject
  GeoMapBean geoMapBean;

  public String getMapName()
  {
    return mapName;
  }

  public void setMapName(String mapName)
  {
    this.mapName = mapName;
  }

  public boolean isDialogVisible()
  {
    return dialogVisible;
  }

  public void setDialogVisible(boolean dialogVisible)
  {
    geoMapBean.setDialogVisible(true);
    this.dialogVisible = dialogVisible;
  }

  public int getDataToImport()
  {
    return dataToImport;
  }

  public void setDataToImport(int dataToImport)
  {
    this.dataToImport = dataToImport;
  }

  public String getLayersPosition()
  {
    return layersPosition;
  }

  public void setLayersPosition(String layersPosition)
  {
    this.layersPosition = layersPosition;
  }

  public String getLegendPosition()
  {
    return legendPosition;
  }

  public void setLegendPosition(String legendPosition)
  {
    this.legendPosition = legendPosition;
  }

  public void acceptImport()
  {
    try
    {
      MapStore mapStore = geoMapBean.getMapStore();
      Style style = geoMapBean.getStyle();

      MapDocument mapDocument = mapStore.loadMap(mapName);
      Style impStyle = mapDocument.getStyle();

      StyleMetadata styleMetadata = new StyleMetadata(style);
      StyleMetadata impStyleMetadata = new StyleMetadata(impStyle);

      styleMetadata.importStyle(impStyleMetadata, dataToImport,
        layersPosition, legendPosition);

      geoMapBean.setDialogVisible(false);
      geoMapBean.refresh();

      dialogVisible = false;

      growl("MAP_IMPORT_COMPLETED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
    mapName = null;
  }

  public void cancelImport()
  {
    geoMapBean.setDialogVisible(false);
    dialogVisible = false;
  }
}
