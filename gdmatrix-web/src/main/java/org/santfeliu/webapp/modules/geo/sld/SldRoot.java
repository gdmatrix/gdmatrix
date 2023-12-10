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
package org.santfeliu.webapp.modules.geo.sld;

import java.util.List;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 *
 * @author realor
 */
public class SldRoot extends SldNode
{
  public SldRoot(String prefix, String name)
  {
    super(prefix, name);
  }

  public List<SldNamedLayer> getNamedLayers()
  {
    return findNodes(SldNamedLayer.class);
  }

  public SldNamedLayer addNamedLayer()
  {
    SldNamedLayer namedLayer = new SldNamedLayer(null, "NamedLayer");
    addChild(namedLayer);
    return namedLayer;
  }

  public SldNamedLayer getNamedLayer(String name)
  {
    if (isBlank(name)) return null;
    boolean found = false;
    SldNamedLayer namedLayer = null;
    int i = 0;
    while (!found && i < getChildCount())
    {
      SldNode child = getChild(i);
      if (child instanceof SldNamedLayer)
      {
        namedLayer = (SldNamedLayer)child;
        if (name.equals(namedLayer.getLayerName())) found = true;
      }
      i++;
    }
    return found ? namedLayer : null;
  }

  public void addNamedLayers(List<String> layers, List<String> styles)
  {
    for (int i = 0; i < layers.size(); i++)
    {
      String layerName = layers.get(i);
      String styleName = null;
      if (i < styles.size()) styleName = styles.get(i);

      SldNamedLayer namedLayer = getNamedLayer(layerName);
      if (namedLayer == null)
      {
        namedLayer = addNamedLayer();
        namedLayer.setLayerName(layerName);
      }

      SldUserStyle userStyle = namedLayer.getUserStyle(styleName);
      if (userStyle == null)
      {
        userStyle = namedLayer.addUserStyle();
      }
      if (!isBlank(styleName))
      {
        userStyle.setStyleName(styleName);
        userStyle.setDefaultStyle(false);
      }
      else
      {
        userStyle.setDefaultStyle(true);
      }
    }
  }
}

