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

import java.io.Serializable;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author realor
 */
public class ServiceParameters implements Serializable
{
  String service;
  String layer;
  String sldName;
  String sldUrl;
  String styles;
  String format;
  Boolean transparent;
  String cqlFilter;
  Integer buffer;

  public String getService()
  {
    return service;
  }

  public void setService(String service)
  {
    if (StringUtils.isBlank(service)) service = null;

    this.service = service;
  }

  public String getLayer()
  {
    return layer;
  }

  public void setLayer(String layer)
  {
    if (StringUtils.isBlank(layer)) layer = null;

    this.layer = layer;
  }

  public String getSldName()
  {
    return sldName;
  }

  public void setSldName(String sldName)
  {
    if (StringUtils.isBlank(sldName)) sldName = null;

    this.sldName = sldName;
  }

  public String getSldUrl()
  {
    return sldUrl;
  }

  public void setSldUrl(String sldUrl)
  {
    if (StringUtils.isBlank(sldUrl)) sldUrl = null;
    this.sldUrl = sldUrl;
  }

  public String getStyles()
  {
    return styles;
  }

  public void setStyles(String styles)
  {
    if (StringUtils.isBlank(styles)) styles = null;

    this.styles = styles;
  }

  public String getFormat()
  {
    return format;
  }

  public void setFormat(String format)
  {
    if (StringUtils.isBlank(format)) format = null;

    this.format = format;
  }

  public Boolean getTransparent()
  {
    return transparent;
  }

  public void setTransparent(Boolean transparent)
  {
    if (Boolean.FALSE.equals(transparent)) transparent = null;

    this.transparent = transparent;
  }

  public String getCqlFilter()
  {
    return cqlFilter;
  }

  public void setCqlFilter(String cqlFilter)
  {
    if (StringUtils.isBlank(cqlFilter)) cqlFilter = null;

    this.cqlFilter = cqlFilter;
  }

  public Integer getBuffer()
  {
    return buffer;
  }

  public void setBuffer(Integer buffer)
  {
    this.buffer = buffer;
  }
}
