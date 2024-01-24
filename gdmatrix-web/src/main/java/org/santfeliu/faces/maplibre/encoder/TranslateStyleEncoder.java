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
package org.santfeliu.faces.maplibre.encoder;

import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Set;
import org.santfeliu.translation.StringTranslator;
import org.santfeliu.translation.TranslatorFactory;

/**
 *
 * @author realor
 */
public class TranslateStyleEncoder extends StyleEncoder
{
  protected StringTranslator stringTranslator;
  protected String language;
  protected String group;
  protected Set<String> properties;

  public TranslateStyleEncoder(String language, String group)
  {
    this(TranslatorFactory.getDefaultStringTranslator(), language, group, null);
  }

  public TranslateStyleEncoder(String language, String group,
    Set<String> properties)
  {
    this(TranslatorFactory.getDefaultStringTranslator(), language, group,
      properties);
  }

  public TranslateStyleEncoder(StringTranslator stringTranslator,
    String language, String group, Set<String> properties)
  {
    this.stringTranslator = stringTranslator;
    this.language = language;
    this.group = group;
    this.properties = properties == null ?
      Collections.singleton("label") : properties;
  }

  @Override
  protected JsonWriter createJsonWriter(Writer writer)
  {
    return new JsonWriter(writer)
    {
      boolean translate;

      @Override
      public JsonWriter name(String name) throws IOException
      {
        this.translate = properties.contains(name);
        return super.name(name);
      }

      @Override
      public JsonWriter value(String value) throws IOException
      {
        if (translate)
        {
          value = stringTranslator.translate(value, language, group);
          translate = false;
        }
        return super.value(value);
      }
    };
  }
}
