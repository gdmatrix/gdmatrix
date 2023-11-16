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

import org.santfeliu.webapp.modules.geo.util.ConversionUtils;

/**
 *
 * @author real
 */
public class SldTextSymbolizer extends SldSymbolizer
{
  public SldTextSymbolizer()
  {
  }

  public SldTextSymbolizer(String prefix, String name)
  {
    super(prefix, name);
  }

  public String getLabelAsXml()
  {
    return getInnerElements("Label");
  }

  public void setLabelAsXml(String elements)
  {
    setInnerElements("Label", elements);
  }

  public String getLabelAsCql()
  {
    return ConversionUtils.xmlToCql(getInnerElements("Label"));
  }

  public void setLabelAsCql(String cql)
  {
    setInnerElements("Label", ConversionUtils.cqlToXml(cql));
  }

  public SldFont getFont()
  {
    return getNode("Font", SldFont.class);
  }

  public SldFill getFill()
  {
    return getNode("Fill", SldFill.class);
  }

  public SldHalo getHalo()
  {
    return getNode("Halo", SldHalo.class);
  }

  public SldPointPlacement getPointPlacement()
  {
    SldNode placement = getNode("LabelPlacement", SldNode.class);
    return placement.getNode("PointPlacement", SldPointPlacement.class);
  }

  public SldLinePlacement getLinePlacement()
  {
    SldNode placement = getNode("LabelPlacement", SldNode.class);
    return placement.getNode("LinePlacement", SldLinePlacement.class);
  }

  @Override
  public String getSymbolizerType()
  {
    return "Text";
  }
}
