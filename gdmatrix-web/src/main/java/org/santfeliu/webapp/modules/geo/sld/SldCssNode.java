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

import java.util.regex.Pattern;
import static org.apache.commons.lang.StringUtils.isBlank;
import org.santfeliu.webapp.modules.geo.util.ConversionUtils;

/**
 *
 * @author realor
 */
public class SldCssNode extends SldNode
{
  public static final Pattern NUMBER_PATTERN = Pattern.compile("[\\-]?[0-9]*(['.'][0-9]*)?");
  public static final Pattern STRING_PATTERN = Pattern.compile("\\'[^\\']*\\'");

  public SldCssNode()
  {
  }

  public SldCssNode(String prefix, String name)
  {
    super(prefix, name);
  }

  public String getCssParameter(String parameter)
  {
    SldNode child = getCssParameterNode(parameter);
    if (child == null) return null;

    String value = child.getTextValue();
    if (!isBlank(value)) return value;

    String cql = ConversionUtils.xmlToCql(child.getInnerElements());
    if (NUMBER_PATTERN.matcher(cql).matches())
    {
      return cql;
    }
    else if (STRING_PATTERN.matcher(cql).matches())
    {
      return cql.substring(1, cql.length() - 1);
    }
    return null;
  }

  public void setCssParameter(String parameter, String value)
  {
    if (isBlank(value)) value = null;

    SldNode child = getCssParameterNode(parameter);
    if (value == null)
    {
      if (child != null)
      {
        removeChild(child);
      }
    }
    else // value != null
    {
      if (child == null)
      {
        child = new SldNode(null, "CssParameter");
        child.getAttributes().put("name", parameter);
        addChild(child);
      }
      child.setTextValue(value);
    }
  }

  public String getCssParameterAsCql(String parameter)
  {
    SldNode child = getCssParameterNode(parameter);
    if (child == null) return null;

    String value = child.getTextValue();
    if (!isBlank(value))
    {
      if (NUMBER_PATTERN.matcher(value).matches())
      {
        return value;
      }
      else
      {
        return "'" + value + "'";
      }
    }
    return ConversionUtils.xmlToCql(child.getInnerElements());
  }

  public void setCssParameterAsCql(String parameter, String cql)
  {
    if (isBlank(cql)) cql = null;

    SldNode child = getCssParameterNode(parameter);
    if (cql == null)
    {
      if (child != null)
      {
        removeChild(child);
      }
    }
    else // value != null
    {
      if (child == null)
      {
        child = new SldNode(null, "CssParameter");
        child.getAttributes().put("name", parameter);
        addChild(child);
      }

      if (NUMBER_PATTERN.matcher(cql).matches())
      {
        child.setTextValue(cql);
      }
      else if (STRING_PATTERN.matcher(cql).matches())
      {
        child.setTextValue(cql.substring(1, cql.length() - 1));
      }
      else
      {
        child.setInnerElements(ConversionUtils.cqlToXml(cql));
      }
    }
  }

  private SldNode getCssParameterNode(String parameter)
  {
    boolean found = false;
    int index = 0;
    SldNode child = null;
    while (!found && index < getChildCount())
    {
      child = getChild(index);
      String paramName = child.getAttributes().get("name");
      if (parameter.equals(paramName)) found = true;
      else index++;
    }
    return found ? child : null;
  }

  public static void main(String[] args)
  {
    System.out.println(NUMBER_PATTERN.matcher("-3434.897").matches());
    System.out.println(STRING_PATTERN.matcher("'fsdf'").matches());

  }
}
