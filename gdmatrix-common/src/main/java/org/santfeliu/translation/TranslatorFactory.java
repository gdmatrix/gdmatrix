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
package org.santfeliu.translation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.santfeliu.translation.stream.HtmlTranslator;
import org.santfeliu.translation.stream.PlainTextTranslator;
import org.santfeliu.translation.string.DefaultStringTranslator;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author realor
 */
public class TranslatorFactory
{
  private static final Map<String, StringTranslator> stringTranslatorMap =
    new HashMap<String, StringTranslator>();

  private static final Map<String, StreamTranslator> streamTranslatorMap =
    new HashMap<String, StreamTranslator>();

  private static String defaultLanguage;
  private static Set<String> supportedLanguages;

  static
  {
    // register StreamTranslators
    putStreamTranslatorForContentType("text/plain", new PlainTextTranslator());
    putStreamTranslatorForContentType("text/html", new HtmlTranslator());
  }

  public static String getDefaultLanguage()
  {
    if (defaultLanguage == null)
    {
      defaultLanguage =
        MatrixConfig.getProperty("org.santfeliu.translation.defaultLanguage");
      if (defaultLanguage == null) defaultLanguage = "ca";
    }
    return defaultLanguage;
  }

  /* Set of languages supported by translation module. This list can be
    different to the list of JSF supported locales */
  public static Set<String> getSupportedLanguages()
  {
    if (supportedLanguages == null)
    {
      Set set = new HashSet();
      String str = MatrixConfig.getProperty(
        "org.santfeliu.translation.supportedLanguages");
      if (str != null)
      {
        String[] languages = str.split(",");
        for (String language : languages)
        {
          set.add(language);
        }
      }
      set.add(getDefaultLanguage());
      supportedLanguages = set;
    }
    return supportedLanguages;
  }

  public static boolean isSupportedLanguage(String language)
  {
    return getDefaultLanguage().equals(language) ||
      getSupportedLanguages().contains(language);
  }

  public static StringTranslator getDefaultStringTranslator()
  {
    String className = MatrixConfig.getProperty(
      "org.santfeliu.translation.stringTranslator");
    if (className == null)
      className = DefaultStringTranslator.class.getName();
    return getStringTranslator(className);
  }

  public static StringTranslator getStringTranslator(String className)
  {
    StringTranslator translator = stringTranslatorMap.get(className);
    if (translator == null)
    {
      try
      {
        Class cls = Class.forName(className);
        translator = (StringTranslator)cls.getConstructor().newInstance();
        stringTranslatorMap.put(className, translator);
      }
      catch (Exception ex)
      {
        throw new RuntimeException(ex);
      }
    }
    return translator;
  }

  public static StreamTranslator getStreamTranslatorForContentType(
     String contentType)
  {
    return streamTranslatorMap.get(contentType);
  }

  public static void putStreamTranslatorForContentType(String contentType,
    StreamTranslator translator)
  {
    streamTranslatorMap.put(contentType, translator);
  }
}
