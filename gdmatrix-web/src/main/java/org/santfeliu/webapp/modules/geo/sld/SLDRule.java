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
import org.santfeliu.misc.mapviewer.util.ConversionUtils;

/**
 *
 * @author real
 */
public class SLDRule extends SLDNode
{
  public SLDRule()
  {
  }

  public SLDRule(String prefix, String name)
  {
    super(prefix, name);
  }

  public String getTitle()
  {
    return getElementText("Title");
  }

  public void setTitle(String name)
  {
    int index = findNode("Title", 0);
    if (index != -1)
    {
      SLDNode child = getChild(index);
      child.setTextValue(name);
    }
    else
    {
      SLDNode node = new SLDNode("Title");
      node.setTextValue(name);
      insertChild(node, 0);
    }
  }

  public String getMinScaleDenominator()
  {
    return getElementText("MinScaleDenominator");
  }

  public void setMinScaleDenominator(String minScale)
  {
    SLDNode node = getNode("MinScaleDenominator", SLDNode.class);
    node.setTextValue(minScale);
  }

  public String getMaxScaleDenominator()
  {
    return getElementText("MaxScaleDenominator");
  }

  public void setMaxScaleDenominator(String maxScale)
  {
    SLDNode node = getNode("MaxScaleDenominator", SLDNode.class);
    node.setTextValue(maxScale);
  }

  public String getFilterAsXml()
  {
    return getInnerElements("Filter");
  }

  public void setFilterAsXml(String filter)
  {
    setInnerElements("ogc", "Filter", filter); // TODO take right prefix
  }

  public String getFilterAsCql()
  {
    return ConversionUtils.xmlToCql(getInnerElements("Filter"));
  }

  public void setFilterAsCql(String cqlFilter)
  {
    // TODO take right prefix
    setInnerElements("ogc", "Filter", ConversionUtils.cqlToXml(cqlFilter)); 
  }

  public List<SLDSymbolizer> getSymbolizers()
  {
    return findNodes(SLDSymbolizer.class);
  }

  public SLDPointSymbolizer addPointSymbolizer()
  {
    SLDPointSymbolizer symbolizer =
      new SLDPointSymbolizer(null, "PointSymbolizer");
    addChild(symbolizer);
    return symbolizer;
  }

  public SLDLineSymbolizer addLineSymbolizer()
  {
    SLDLineSymbolizer symbolizer =
      new SLDLineSymbolizer(null, "LineSymbolizer");
    addChild(symbolizer);
    return symbolizer;
  }

  public SLDPolygonSymbolizer addPolygonSymbolizer()
  {
    SLDPolygonSymbolizer symbolizer =
      new SLDPolygonSymbolizer(null, "PolygonSymbolizer");
    addChild(symbolizer);
    return symbolizer;
  }

  public SLDTextSymbolizer addTextSymbolizer()
  {
    SLDTextSymbolizer symbolizer =
      new SLDTextSymbolizer(null, "TextSymbolizer");
    addChild(symbolizer);
    return symbolizer;
  }
}
