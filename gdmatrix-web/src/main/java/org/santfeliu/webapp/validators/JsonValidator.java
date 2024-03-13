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
package org.santfeliu.webapp.validators;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.faces.FacesUtils;

/**
 *
 * @author realor
 */

@FacesValidator("JsonValidator")
public class JsonValidator implements Validator<String>
{
  @Override
  public void validate(FacesContext facesContext, UIComponent component,
    String json)
  {
    try
    {
      if (!StringUtils.isBlank(json))
      {
        String className = (String)component.getAttributes().get("jsonClass");
        Class cls = className == null ? Object.class : Class.forName(className);
        Gson gson = new Gson();
        gson.fromJson(json, cls);
      }
    }
    catch (JsonSyntaxException | ClassNotFoundException ex)
    {
      String detail;
      Throwable cause = ex.getCause();
      if (cause != null)
      {
        detail = cause.getLocalizedMessage();
      }
      else
      {
        detail = ex.getLocalizedMessage();
      }
      throw new ValidatorException(FacesUtils.getFacesMessage("JSON_ERROR",
        new Object[]{ detail }, FacesMessage.SEVERITY_ERROR));
    }
  }
}