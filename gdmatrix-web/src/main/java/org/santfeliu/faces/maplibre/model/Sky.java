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
package org.santfeliu.faces.maplibre.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 *
 * @author realor
 */
public class Sky implements Serializable
{
  @SerializedName("sky-color")
  String skyColor;

  @SerializedName("fog-color")
  String fogColor;

  @SerializedName("horizon-blend")
  Double horizonBlend;

  @SerializedName("fog-blend")
  Double fogBlend;

  public String getSkyColor()
  {
    return skyColor;
  }

  public void setSkyColor(String skyColor)
  {
    this.skyColor = isBlank(skyColor) ? null : skyColor;
  }

  public String getFogColor()
  {
    return fogColor;
  }

  public void setFogColor(String fogColor)
  {
    this.fogColor = isBlank(fogColor) ? null : fogColor;
  }

  public Double getHorizonBlend()
  {
    return horizonBlend;
  }

  public void setHorizonBlend(Double horizonBlend)
  {
    this.horizonBlend = horizonBlend;
  }

  public Double getFogBlend()
  {
    return fogBlend;
  }

  public void setFogBlend(Double fogBlend)
  {
    this.fogBlend = fogBlend;
  }
}