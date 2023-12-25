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
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author realor
 */
public class Source implements Serializable
{
  String type;
  String attribution;
  double[] bounds = new double[]{-180, -85.051129, 180, 85.051129};
  Double maxzoom;
  Double minzoom;
  String scheme;
  List<String> tiles = new ArrayList<>();
  String url; // tile url or image url
  List<String> urls; // videos
  List<double[]> coordinates;
  @SerializedName("volatile")
  Boolean _volatile;

  Object promoteId;

  Integer tileSize;
  String encoding;
  Double baseShift;
  Double redFactor;
  Double greenFactor;
  Double blueFactor;

  Integer buffer;
  Object data;
  Object filter;
  Double tolerance;

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public List<String> getTiles()
  {
    return tiles;
  }

  public void setTiles(List<String> tiles)
  {
    this.tiles = tiles;
  }

  public String getAttribution()
  {
    return attribution;
  }

  public void setAttribution(String attribution)
  {
    this.attribution = attribution;
  }

  public double[] getBounds()
  {
    return bounds;
  }

  public void setBounds(double[] bounds)
  {
    this.bounds = bounds;
  }

  public Double getMaxzoom()
  {
    return maxzoom;
  }

  public void setMaxzoom(Double maxzoom)
  {
    this.maxzoom = maxzoom;
  }

  public Double getMinzoom()
  {
    return minzoom;
  }

  public void setMinzoom(Double minzoom)
  {
    this.minzoom = minzoom;
  }

  public String getScheme()
  {
    return scheme;
  }

  public void setScheme(String scheme)
  {
    if (StringUtils.isBlank(scheme)) scheme = null;
    this.scheme = scheme;
  }

  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    if (StringUtils.isBlank(url)) url = null;
    this.url = url;
  }

  public List<String> getUrls()
  {
    return urls;
  }

  public void setUrls(List<String> urls)
  {
    this.urls = urls;
  }

  public List<double[]> getCoordinates()
  {
    return coordinates;
  }

  public void setCoordinates(List<double[]> coordinates)
  {
    this.coordinates = coordinates;
  }

  public Boolean getVolatile()
  {
    return _volatile;
  }

  public void setVolatile(Boolean _volatile)
  {
    this._volatile = _volatile;
  }

  public Object getPromoteId()
  {
    return promoteId;
  }

  public void setPromoteId(Object promoteId)
  {
    this.promoteId = promoteId;
  }

  public Integer getTileSize()
  {
    return tileSize;
  }

  public void setTileSize(Integer tileSize)
  {
    this.tileSize = tileSize;
  }

  public String getEncoding()
  {
    return encoding;
  }

  public void setEncoding(String encoding)
  {
    this.encoding = encoding;
  }

  public Double getBaseShift()
  {
    return baseShift;
  }

  public void setBaseShift(Double baseShift)
  {
    this.baseShift = baseShift;
  }

  public Double getRedFactor()
  {
    return redFactor;
  }

  public void setRedFactor(Double redFactor)
  {
    this.redFactor = redFactor;
  }

  public Double getGreenFactor()
  {
    return greenFactor;
  }

  public void setGreenFactor(Double greenFactor)
  {
    this.greenFactor = greenFactor;
  }

  public Double getBlueFactor()
  {
    return blueFactor;
  }

  public void setBlueFactor(Double blueFactor)
  {
    this.blueFactor = blueFactor;
  }

  public Integer getBuffer()
  {
    return buffer;
  }

  public void setBuffer(Integer buffer)
  {
    this.buffer = buffer;
  }

  public Object getData()
  {
    return data;
  }

  public void setData(Object data)
  {
    this.data = data;
  }

  public Object getFilter()
  {
    return filter;
  }

  public void setFilter(Object filter)
  {
    this.filter = filter;
  }

  public Double getTolerance()
  {
    return tolerance;
  }

  public void setTolerance(Double tolerance)
  {
    this.tolerance = tolerance;
  }
}
