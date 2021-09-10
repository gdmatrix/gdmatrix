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
package org.santfeliu.faces.browser.encoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Locale;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.santfeliu.faces.Translator;
import org.santfeliu.faces.browser.HtmlBrowser;
import org.santfeliu.util.net.HttpClient;

/**
 *
 * @author realor
 */
public class TextEncoder extends ContentEncoder
{
  public TextEncoder()
  {
  }

  @Override
  public void encode(HtmlBrowser browser, HttpClient httpClient,
    ResponseWriter writer, Translator translator, String translationGroup)
    throws IOException
  {
    InputStream is = httpClient.getContentInputStream();
    if (is == null) return;
    try
    {
      BufferedReader reader;
      String contentEncoding = httpClient.getContentEncoding();
      if (contentEncoding == null)
      {
        reader = new BufferedReader(new InputStreamReader(is));
      }
      else
      {
        reader = new BufferedReader(new InputStreamReader(is, contentEncoding));
      }
      String line = reader.readLine();
      while (line != null)
      {
        if (translator != null)
        {
          StringWriter sw = new StringWriter();
          translator.translate(new StringReader(line), sw,
            "text/plain", getLanguage(), translationGroup);
          line = sw.toString();
        }
        writer.writeText(line, null);

        writer.startElement("br", browser);
        writer.endElement("br");
        line = reader.readLine();
      }
    }
    finally
    {
      is.close();
    }
  }

  private String getLanguage()
  {
    Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
    return locale.getLanguage();
  }
}
