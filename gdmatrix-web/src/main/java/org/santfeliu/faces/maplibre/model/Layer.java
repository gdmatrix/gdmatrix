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
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author realor
 */
public class Layer implements Serializable
{
  String id;
  String type;
  String source;
  Map<String, Object> paint = new HashMap<>();
  Map<String, Object> layout = new HashMap<>();
  Map<String, Object> metadata = new HashMap<>();

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getLabel()
  {
    return (String)metadata.get("label");
  }

  public void setLabel(String label)
  {
    metadata.put("label", label);
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getSource()
  {
    return source;
  }

  public void setSource(String source)
  {
    this.source = source;
  }

  public String getLayers()
  {
    return (String)metadata.get("layers");
  }

  public void setLayers(String layers)
  {
    metadata.put("layers", layers);
  }

  public String getCqlFilter()
  {
    return (String)metadata.get("cqlFilter");
  }

  public void setCqlFilter(String cqlFilter)
  {
    metadata.put("cqlFilter", cqlFilter);
  }

  public String getStyles()
  {
    return (String)metadata.get("styles");
  }

  public void setStyles(String styles)
  {
    metadata.put("styles", styles);
  }

  public boolean isVisible()
  {
    Object value = metadata.get("visible");
    return (value instanceof Boolean) ? (Boolean)value : true;
  }

  public void setVisible(boolean visible)
  {
    metadata.put("visible", visible);
  }

  public boolean isLocatable()
  {
    Object value = metadata.get("locatable");
    return (value instanceof Boolean) ? (Boolean)value : false;
  }

  public void setLocatable(boolean locatable)
  {
    metadata.put("locatable", locatable);
  }

  public boolean isHighlightEnabled()
  {
    Object value = metadata.get("highlight");
    return value instanceof Boolean ? (Boolean)value : false;
  }

  public void setHighlightEnabled(boolean enabled)
  {
    metadata.put("highlight", enabled);
  }

  public Map<String, Object> getPaint()
  {
    return paint;
  }

  public void setPaint(Map<String, Object> paint)
  {
    this.paint = paint;
  }

  public Map<String, Object> getLayout()
  {
    return layout;
  }

  public void setLayout(Map<String, Object> layout)
  {
    this.layout = layout;
  }

  public Map<String, Object> getMetadata()
  {
    return metadata;
  }

  public void setMetadata(Map<String, Object> metadata)
  {
    this.metadata = metadata;
  }
}
