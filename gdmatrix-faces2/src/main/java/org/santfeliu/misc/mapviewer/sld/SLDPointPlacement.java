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

import org.santfeliu.misc.mapviewer.util.ConversionUtils;

/**
 *
 * @author realor
 */
public class SLDPointPlacement extends SLDNode
{
  public SLDPointPlacement()
  {
  }

  public SLDPointPlacement(String prefix, String name)
  {
    super(prefix, name);
  }

  public String getRotationAsXml()
  {
    return getInnerElements("Rotation");
  }

  public void setRotationAsXml(String rotation)
  {
    setInnerElements("Rotation", rotation);
  }

  public String getRotationAsCql()
  {
    return ConversionUtils.xmlToCql(getInnerElements("Rotation"));
  }

  public void setRotationAsCql(String cql)
  {
    setInnerElements("Rotation", ConversionUtils.cqlToXml(cql));
  }

  // AnchorPoint

  public String getAnchorPointXAsXml()
  {
    SLDNode node = getNode("AnchorPoint", SLDNode.class);
    return node.getInnerElements("AnchorPointX");
  }

  public void setAnchorPointXAsXml(String x)
  {
    SLDNode node = getNode("AnchorPoint", SLDNode.class);
    node.setInnerElements("AnchorPointX", x);
  }

  public String getAnchorPointXAsCql()
  {
    SLDNode node = getNode("AnchorPoint", SLDNode.class);
    return ConversionUtils.xmlToCql(node.getInnerElements("AnchorPointX"));
  }

  public void setAnchorPointXAsCql(String x)
  {
    SLDNode node = getNode("AnchorPoint", SLDNode.class);
    node.setInnerElements("AnchorPointX", ConversionUtils.cqlToXml(x));
  }

  public String getAnchorPointYAsXml()
  {
    SLDNode node = getNode("AnchorPoint", SLDNode.class);
    return node.getInnerElements("AnchorPointY");
  }

  public void setAnchorPointYAsXml(String y)
  {
    SLDNode node = getNode("AnchorPoint", SLDNode.class);
    node.setInnerElements("AnchorPointY", y);
  }

  public String getAnchorPointYAsCql()
  {
    SLDNode node = getNode("AnchorPoint", SLDNode.class);
    return ConversionUtils.xmlToCql(node.getInnerElements("AnchorPointY"));
  }

  public void setAnchorPointYAsCql(String y)
  {
    SLDNode node = getNode("AnchorPoint", SLDNode.class);
    node.setInnerElements("AnchorPointY", ConversionUtils.cqlToXml(y));
  }

  // Displacement
  
  public String getDisplacementXAsXml()
  {
    SLDNode node = getNode("Displacement", SLDNode.class);
    return node.getInnerElements("DisplacementX");
  }

  public void setDisplacementXAsXml(String x)
  {
    SLDNode node = getNode("Displacement", SLDNode.class);
    node.setInnerElements("DisplacementX", x);
  }

  public String getDisplacementXAsCql()
  {
    SLDNode node = getNode("Displacement", SLDNode.class);
    return ConversionUtils.xmlToCql(node.getInnerElements("DisplacementX"));
  }

  public void setDisplacementXAsCql(String x)
  {
    SLDNode node = getNode("Displacement", SLDNode.class);
    node.setInnerElements("DisplacementX", ConversionUtils.cqlToXml(x));
  }

  public String getDisplacementYAsXml()
  {
    SLDNode node = getNode("Displacement", SLDNode.class);
    return node.getInnerElements("DisplacementY");
  }

  public void setDisplacementYAsXml(String y)
  {
    SLDNode node = getNode("Displacement", SLDNode.class);
    node.setInnerElements("DisplacementY", y);
  }

  public String getDisplacementYAsCql()
  {
    SLDNode node = getNode("Displacement", SLDNode.class);
    return ConversionUtils.xmlToCql(node.getInnerElements("DisplacementY"));
  }

  public void setDisplacementYAsCql(String y)
  {
    SLDNode node = getNode("Displacement", SLDNode.class);
    node.setInnerElements("DisplacementY", ConversionUtils.cqlToXml(y));
  }
}
