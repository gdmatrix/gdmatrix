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
package org.santfeliu.webapp.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.SelectItem;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author blanquepa
 */
@FacesConverter(value = "selectItemConverter")
public class SelectItemConverter implements Converter<SelectItem>
{
  private static final String SEPARATOR = "::";
  private static final String BLANK = "";

  @Override
  public SelectItem getAsObject(FacesContext context, UIComponent component,
    String value)
  {
    String label = value;
    if (value != null && value.contains(SEPARATOR))
    {
      String[] parts = value.split(SEPARATOR);
      if (parts != null && parts.length == 2)
      {
        value = parts[0];
        label = parts[1];
      }
      else
        return new SelectItem(BLANK);
    }

    return new SelectItem(value, label);
  }

  @Override
  public String getAsString(FacesContext context, UIComponent component,
    SelectItem value)
  {
    if (value != null && value.getValue() != null)
    {
      String itemValue = (String) value.getValue();
      if (!StringUtils.isBlank(itemValue))
        return itemValue + SEPARATOR + value.getLabel();
    }

    return BLANK;
  }

}
