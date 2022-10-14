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
package org.santfeliu.faces.convert;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

/**
 *
 * @author realor
 */
public class EnumConverter implements Converter, Serializable
{
  public EnumConverter()
  {
  }

  @Override
  public Object getAsObject(FacesContext context,
    UIComponent component, String value) throws ConverterException
  {
    if (value == null || value.trim().length() == 0)
    {
      return null;
    }
    String enumClassName = (String)component.getAttributes().get("enum");
    if (enumClassName == null)
      throw new ConverterException("enum class not defined");
    Class<Enum> enumClass = null;
    try
    {
      enumClass = (Class<Enum>)Class.forName(enumClassName);
    }
    catch (Exception e)
    {
      throw new ConverterException("invalid enum class");
    }
    Enum[] constants = enumClass.getEnumConstants();
    boolean found = false;
    int i = 0;
    while (!found && i < constants.length)
    {
      if (constants[i].toString().equals(value)) found = true;
      else i++;
    }
    if (!found) throw new ConverterException("invalid value");
    return constants[i];
  }

  @Override
  public String getAsString(FacesContext context,
    UIComponent component, Object value) throws ConverterException
  {
    if (value == null) return "";
    return String.valueOf(value);
  }
}

