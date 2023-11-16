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

import org.apache.commons.lang.StringUtils;
import org.santfeliu.webapp.modules.geo.util.ConversionUtils;

/**
 *
 * @author realor
 */
public abstract class SldSymbolizer extends SldNode
{
  public SldSymbolizer()
  {
  }

  public SldSymbolizer(String prefix, String name)
  {
    super(prefix, name);
  }

  public String getGeometryAsXml()
  {
    return getInnerElements("Geometry");
  }

  public void setGeometryAsXml(String geometry)
  {
    setInnerElements("Geometry", geometry);
  }

  public String getGeometryAsCql()
  {
    return ConversionUtils.xmlToCql(getInnerElements("Geometry"));
  }

  public void setGeometryAsCql(String geometry)
  {
    setInnerElements("Geometry", ConversionUtils.cqlToXml(geometry));
  }

  public String getCssParameter(SldNode tagNode, String parameter)
  {
    String value = null;
    boolean found = false;
    int index = 0;
    while (!found && index < tagNode.getChildCount())
    {
      SldNode child = tagNode.getChild(index);
      String paramName = child.getAttributes().get("name");
      if (parameter.equals(paramName))
      {
        found = true;
        value = child.getTextValue();
      }
      else index++;
    }
    return found ? value : null;
  }

  public void setCssParameter(SldNode tagNode, String parameter, String value)
  {
    if (StringUtils.isBlank(value)) value = null;

    boolean found = false;
    int index = 0;
    SldNode child = null;
    while (!found && index < tagNode.getChildCount())
    {
      child = tagNode.getChild(index);
      String paramName = child.getAttributes().get("name");
      if (parameter.equals(paramName)) found = true;
      else index++;
    }
    if (value == null)
    {
      if (found)
      {
        tagNode.removeChild(index);
      }
    }
    else // value != null
    {
      if (found)
      {
        child.setTextValue(value);
      }
      else
      {
        SldNode cssNode = new SldNode(null, "CssParameter");
        cssNode.getAttributes().put("name", parameter);
        cssNode.setTextValue(value);
        tagNode.addChild(cssNode);
      }
    }
  }

  public String getCssParameter(String tag, String parameter)
  {
    int tagIndex = findNode(tag, 0);
    if (tagIndex == -1) return null;

    SldNode tagNode = getChild(tagIndex);
    return getCssParameter(tagNode, parameter);
  }

  public void setCssParameter(String tag, String parameter, String value)
  {
    if (StringUtils.isBlank(value)) value = null;

    SldNode tagNode = null;
    int tagIndex = findNode(tag, 0);
    if (tagIndex == -1)
    {
      tagNode = new SldNode(null, tag);
      addChild(tagNode);
    }
    else tagNode = getChild(tagIndex);
    setCssParameter(tagNode, parameter, value);
  }

  public abstract String getSymbolizerType();
}

