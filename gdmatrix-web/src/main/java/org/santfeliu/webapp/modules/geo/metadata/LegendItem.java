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

import java.io.Serializable;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author realor
 */
public abstract class LegendItem implements Serializable
{
  String type;
  String label;
  String icon;

  public LegendItem()
  {
  }

  public LegendItem(java.util.Map properties)
  {
    this.type = (String)properties.get("type");
    this.label = (String)properties.get("label");
    this.icon = (String)properties.get("icon");
    if (StringUtils.isBlank(icon)) icon = null;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public String getIcon()
  {
    return icon;
  }

  public void setIcon(String icon)
  {
    if (StringUtils.isBlank(icon)) icon = null;
    this.icon = icon;
  }

  public static LegendItem create(java.util.Map properties)
  {
    Object itemType = properties.get("type");
    if ("layer".equals(itemType))
    {
      return new LegendLayer(properties);
    }
    else if ("group".equals(itemType))
    {
      return new LegendGroup(properties);
    }
    return null;
  }
}
