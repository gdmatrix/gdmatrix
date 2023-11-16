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
package org.santfeliu.misc.mapviewer.sld;

import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author real
 */
public class SLDRoot extends SLDNode
{
  public SLDRoot(String prefix, String name)
  {
    super(prefix, name);
  }
  
  public List<SLDNamedLayer> getNamedLayers()
  {
    return findNodes(SLDNamedLayer.class);
  }

  public SLDNamedLayer addNamedLayer()
  {
    SLDNamedLayer namedLayer = new SLDNamedLayer(null, "NamedLayer");
    addChild(namedLayer);
    return namedLayer;
  }

  public SLDNamedLayer getNamedLayer(String name)
  {
    if (StringUtils.isBlank(name)) return null;
    boolean found = false;
    SLDNamedLayer namedLayer = null;
    int i = 0;
    while (!found && i < getChildCount())
    {
      SLDNode child = getChild(i);
      if (child instanceof SLDNamedLayer)
      {
        namedLayer = (SLDNamedLayer)child;
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

      SLDNamedLayer namedLayer = getNamedLayer(layerName);
      if (namedLayer == null)
      {
        namedLayer = addNamedLayer();
        namedLayer.setLayerName(layerName);
      }
      
      SLDUserStyle userStyle = namedLayer.getUserStyle(styleName);
      if (userStyle == null)
      {
        userStyle = namedLayer.addUserStyle();
      }
      if (!StringUtils.isBlank(styleName))
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

