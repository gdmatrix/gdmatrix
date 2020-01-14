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
package org.santfeliu.misc.mapviewer.util;

import java.util.HashSet;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.misc.mapviewer.Map;

/**
 *
 * @author realor
 */
public class LayerMerger
{
  private static final int MAX_LENGTH = 100;
  private String serviceUrl;
  private StringBuilder layerNames = new StringBuilder();
  private StringBuilder styleNames = new StringBuilder();
  private StringBuilder cqlFilter = new StringBuilder();
  private String sld;
  private HashSet<String> layerSet = new HashSet<String>();
  private int maxLength = MAX_LENGTH;

  public int getMaxLength()
  {
    return maxLength;
  }

  public void setMaxLength(int maxLength)
  {
    this.maxLength = maxLength;
  }
  
  public boolean merge(Map.Layer layer)
  {
    boolean canMerge =
     (!layer.isBaseLayer() || layerNames.length() == 0) &&
     (serviceUrl == null || serviceUrl.equals(layer.getService().getUrl())) &&
     (sld == null || sld.equals(layer.getSld())) &&
     (layerNames.length() + styleNames.length() + cqlFilter.length() < maxLength);
    
    if (!canMerge) return false;

    if (StringUtils.isBlank(layer.getCqlFilter()))
    {
      canMerge = cqlFilter.length() == 0;
    }
    else // with filter
    {
      canMerge = layerNames.length() == 0 ||
        layerNames.toString().equals(layer.getNamesString());
    }

    if (!canMerge) return false;

    // merge is possible
    
    serviceUrl = layer.getService().getUrl();
    sld = layer.getSld() == null ? "" : layer.getSld();

    String namesString = layer.getNamesString();
    if (!layerSet.contains(namesString))
    {
      // add layer
      if (!layerSet.isEmpty()) layerNames.append(",");
      layerNames.append(namesString);
    
      // add style
      if (!layerSet.isEmpty()) styleNames.append(",");
      styleNames.append(layer.getStylesString());
      
      layerSet.add(namesString);
    }

    if (!StringUtils.isBlank(layer.getCqlFilter()))
    {
      if (cqlFilter.length() > 0) cqlFilter.append(" or ");
      cqlFilter.append("(");
      cqlFilter.append(layer.getCqlFilter());
      cqlFilter.append(")");
    }
    return canMerge;
  }

  public String getServiceUrl()
  {
    return serviceUrl;
  }

  public String getLayerNames()
  {
    return layerNames.toString();
  }

  public String getStyleNames()
  {
    return styleNames.toString();
  }

  public String getCqlFilter()
  {
    return cqlFilter.toString();
  }

  public String getSld()
  {
    return sld;
  }

  public void reset()
  {
    serviceUrl = null;
    sld = null;
    layerNames.setLength(0);
    styleNames.setLength(0);
    cqlFilter.setLength(0);
    layerSet.clear();
  }
}
