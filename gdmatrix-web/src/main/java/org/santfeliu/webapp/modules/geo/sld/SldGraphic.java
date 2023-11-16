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
 * @author realor
 */
public class SldGraphic extends SldNode
{
  public SldGraphic()
  {
  }

  public SldGraphic(String prefix, String name)
  {
    super(prefix, name);
  }

  public SldExternalGraphic getExternalGraphic()
  {
    return getNode("ExternalGraphic", SldExternalGraphic.class);
  }

  public SldMark getMark()
  {
    return getNode("Mark", SldMark.class);
  }

  public String getOpacityAsXml()
  {
    return getInnerElements("Opacity");
  }

  public void setOpacityAsXml(String opacity)
  {
    setInnerElements("Opacity", opacity);
  }

  public String getOpacityAsCql()
  {
    return ConversionUtils.xmlToCql(getInnerElements("Opacity"));
  }

  public void setOpacityAsCql(String opacity)
  {
    setInnerElements("Opacity", ConversionUtils.cqlToXml(opacity));
  }

  public String getSizeAsXml()
  {
    return getInnerElements("Size");
  }

  public void setSizeAsXml(String size)
  {
    setInnerElements("Size", size);
  }

  public String getSizeAsCql()
  {
    return ConversionUtils.xmlToCql(getInnerElements("Size"));
  }

  public void setSizeAsCql(String size)
  {
    setInnerElements("Size", ConversionUtils.cqlToXml(size));
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

  public void setRotationAsCql(String rotation)
  {
    setInnerElements("Rotation", ConversionUtils.cqlToXml(rotation));
  }
}

