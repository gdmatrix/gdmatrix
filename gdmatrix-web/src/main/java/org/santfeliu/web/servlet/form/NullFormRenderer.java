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
package org.santfeliu.web.servlet.form;

import java.io.Writer;
import java.util.Map;
import org.santfeliu.form.Form;
import java.io.IOException;
import org.santfeliu.util.HTMLCharTranslator;

/**
 *
 * @author realor
 */
public class NullFormRenderer implements FormRenderer
{
  public static final String ENTITY_FIELD_NAME = "entity";
  public static final String FORMSEED_FIELD_NAME = "formseed";

  @Override
  public void renderForm(Form form, Map data, Writer writer) throws IOException
  {
    // Form is null
    String entity = (String)data.get(ENTITY_FIELD_NAME);
    if (entity == null) entity = "ENTITY";
    writer.write("<div>");
    writer.write(entity + ":");
    writer.write("</div>");
    writer.write("<ul>");
    for (Object key : data.keySet())
    {
      String name = String.valueOf(key);
      if (!name.equals(ENTITY_FIELD_NAME) &&
          !name.equals(FORMSEED_FIELD_NAME))
      {
        writer.write("<li>");
        name = HTMLCharTranslator.toHTMLText(name);
        String value = String.valueOf(data.get(key));
        value = HTMLCharTranslator.toHTMLText(value);
        writer.write(name + ": " + value);
        writer.write("</li>");
      }
    }
    writer.write("</ul>");
  }
}
