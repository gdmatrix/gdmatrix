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
  String label;
  String type;
  String source;
  boolean locatable;
  boolean visible;
  boolean legend;
  String form;
  Map<String, Object> paint = new HashMap<>();
  Map<String, Object> layout = new HashMap<>();

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
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
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

  public boolean isLocatable()
  {
    return locatable;
  }

  public void setLocatable(boolean locatable)
  {
    this.locatable = locatable;
  }

  public boolean isVisible()
  {
    return visible;
  }

  public void setVisible(boolean visible)
  {
    this.visible = visible;
  }

  public boolean isLegend()
  {
    return legend;
  }

  public void setLegend(boolean legend)
  {
    this.legend = legend;
  }

  public String getForm()
  {
    return form;
  }

  public void setForm(String form)
  {
    this.form = form;
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
}
