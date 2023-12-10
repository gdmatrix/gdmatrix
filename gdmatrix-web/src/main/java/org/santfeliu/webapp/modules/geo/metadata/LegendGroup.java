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
import java.util.List;

/**
 *
 * @author realor
 */
public class LegendGroup extends LegendItem
{
  public static final String SINGLE = "single";
  public static final String MULTIPLE = "multiple";
  public static final String BLOCK = "block";

  String mode = MULTIPLE;
  List<LegendItem> children;
  boolean expanded = true;

  public LegendGroup()
  {
    this.type = "group";
  }

  public LegendGroup(java.util.Map properties)
  {
    super(properties);
    this.mode = (String)properties.get("mode");
    if (this.mode == null) mode = MULTIPLE;
    children = new ArrayList<>();
    List list = (List)properties.get("children");
    if (list != null)
    {
      for (int i = 0; i < list.size(); i++)
      {
        java.util.Map itemProperties = (java.util.Map)list.get(i);
        LegendItem item = LegendItem.create(itemProperties);
        if (item != null)
        {
          children.add(item);
        }
      }
    }
  }

  public String getMode()
  {
    return mode;
  }

  public void setMode(String mode)
  {
    this.mode = mode;
  }

  public List<LegendItem> getChildren()
  {
    if (children == null) children = new ArrayList<>();
    return children;
  }

  public void setChildren(List<LegendItem> children)
  {
    this.children = children;
  }

  public boolean isExpanded()
  {
    return expanded;
  }

  public void setExpanded(boolean expanded)
  {
    this.expanded = expanded;
  }
}
