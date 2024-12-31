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
package org.santfeliu.util.script.function;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.santfeliu.util.script.ScriptClient;

/**
 *
 * @author realor
 */

/*
 * Usage: response = httpFetch(String url, {
 *   method: method (string: "GET", "POST", ...),
 *   headers: { ... }
 *   body: body (string)
 *   charset : (string: "UTF-8", "windows-1252", ...)
 *   version: (string: "1.1", "2")
 * })
 */

public class HttpFetchFunction extends BaseFunction
{
  @Override
  public Object call(Context cx, Scriptable scope, Scriptable thisObj,
    Object[] args)
  {
    try
    {
      String url = Context.toString(args[0]);
      NativeObject headers = null;
      String method = "GET";
      String body = null;
      String charsetName = null;
      String versionName = null;

      if (args.length >= 2)
      {
        body = args[1].getClass().toString();

        if (args[1] instanceof NativeObject)
        {
          NativeObject options = (NativeObject)args[1];
          method = Context.toString(options.get("method", scope));
          if (options.has("headers", scope))
          {
            headers = (NativeObject)options.get("headers", scope);
          }
          if (options.has("body", scope))
          {
            body = Context.toString(options.get("body", scope));
          }
          if (options.has("charset", scope))
          {
            charsetName = Context.toString(options.get("charset", scope));
          }
          if (options.has("version", scope))
          {
            versionName = Context.toString(options.get("version", scope));
          }
        }
      }
      HttpRequest.Builder builder = HttpRequest.newBuilder()
        .uri(URI.create(url));

      Charset charset;
      if (charsetName == null)
      {
        charset = Charset.defaultCharset();
      }
      else
      {
        charset = Charset.forName(charsetName);
      }

      if ("GET".equals(method))
      {
        builder.GET();
      }
      else if ("POST".equals(method))
      {
        builder.POST(BodyPublishers.ofString(body, charset));
      }
      else if ("PUT".equals(method))
      {
        builder.PUT(BodyPublishers.ofString(body, charset));
      }
      else if ("DELETE".equals(method))
      {
        builder.DELETE();
      }

      if (headers != null)
      {
        for (Object id : headers.getIds())
        {
          Object value = headers.get(id);
          builder.header(id.toString(), Context.toString(value));
        }
      }
      Version version;
      if ("2".equals(versionName))
      {
        version = HttpClient.Version.HTTP_2;
      }
      else
      {
        version = HttpClient.Version.HTTP_1_1;
      }

      HttpRequest request = builder.build();
      HttpResponse<String> response = HttpClient.newBuilder()
        .version(version)
        .build()
        .send(request, BodyHandlers.ofString());
      return response;
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  public static void main(String[] args)
  {
    ScriptClient scriptClient = new ScriptClient();

    HttpResponse<String> response = (HttpResponse<String>)scriptClient.execute(
      "httpFetch('http://www.santfeliu.cat/robots.txt',{method:'POST',body:'test',headers:{aaa:'77'}})");
    System.out.println(response);
    System.out.println(response.body());
  }
}
