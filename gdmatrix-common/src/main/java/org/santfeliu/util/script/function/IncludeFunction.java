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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.activation.DataHandler;
import org.matrix.doc.Document;
import org.matrix.translation.TranslationConstants;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.net.HttpClient;
import org.santfeliu.util.script.ScriptableBase;
import org.santfeliu.util.template.Template;

/**
 *
 * @author realor
 */

/*
 * Usage: include(String reference [, String language])
 *
 * returns: a String that contains the data of document referenced by
     reference and language.
 *
 *   reference format can be:
 *     form:<name>
 *     html:<name>
 *     js:<name>
 *     URL
 *
 * If document content type is text/*, templates are applied recursively.
 *
 * Examples:
 *
 *   ${include("js:math")}
 *   ${include("form:info", "en")}
 *   ${include("html:demo", userLang)}
 */

public class IncludeFunction extends BaseFunction
{
  private static final String FORM_PREFIX = "form:";
  private static final String FORM_PROPERTY = "workflow.form";

  private static final String HTML_PREFIX = "html:";
  private static final String HTML_PROPERTY = "workflow.html";

  private static final String JS_PREFIX = "js:";
  private static final String JS_PROPERTY = "workflow.js";

  public IncludeFunction()
  {
  }

  @Override
  public Object call(Context cx, Scriptable scope, Scriptable thisObj,
    Object[] args)
  {
    String result = "";
    try
    {
      if (args.length > 0)
      {
        Content content = null;
        String language = null;
        String reference = Context.toString(args[0]);
        if (args.length > 1 && args[1] != null)
        {
          language = Context.toString(args[1]);
        }
        if (reference.startsWith(FORM_PREFIX))
        {
          // It's assummed that form is an HTML MULTI_LANGUAGE, UTF-8 encoded.
          String formName = reference.substring(FORM_PREFIX.length());
          content = getContentByName(formName, FORM_PROPERTY, language);
        }
        else if (reference.startsWith(HTML_PREFIX))
        {
          // It's assummed that form is an HTML MULTI_LANGUAGE, UTF-8 encoded.
          String htmlName = reference.substring(HTML_PREFIX.length());
          content = getContentByName(htmlName, HTML_PROPERTY, language);
        }
        else if (reference.startsWith(JS_PREFIX))
        {
          // It's assummed that form is an HTML MULTI_LANGUAGE, UTF-8 encoded.
          String jsName = reference.substring(JS_PREFIX.length());
          content = getContentByName(jsName, JS_PROPERTY, language);
        }
        else // reference is an URL
        {
          content = getContentByUrl(reference, language);
        }

        if (content != null && content.mimeType != null &&
            content.mimeType.startsWith("text/"))
        {
          // apply templates if context is text
          if (scope instanceof ScriptableBase)
          {
            ScriptableBase scriptable = (ScriptableBase)scope;
            Template template = Template.create(content.data);
            result = template.merge(scriptable.getPersistentVariables());
          }
          else
          {
            result = content.data;
          }
        }
        else
        {
          result = content.data;
        }
      }
    }
    catch (Exception ex)
    {
    }
    return result;
  }

  private Content getContentByName(String formName,
    String propertyName, String userLanguage) throws Exception
  {
    Content content = null;
    if (userLanguage == null)
      userLanguage = TranslationConstants.UNIVERSAL_LANGUAGE;
    DocumentManagerClient client = getDocumentManagerClient();

    Document document =
      client.loadDocumentByName(null, propertyName, formName, userLanguage, 0);
      //TODO: Comprobar que funciona pasar un null como docTypeId

    if (document != null)
    {
      content = new Content();
      DataHandler dh = DocumentUtils.getContentData(document);
      content.data = createString(dh.getDataSource().getInputStream());
      String language = document.getLanguage();
      if (!TranslationConstants.UNIVERSAL_LANGUAGE.equals(language))
        content.language = language;
      content.mimeType = DocumentUtils.getContentType(document);
    }
    return content;
  }

  private Content getContentByUrl(String url, String userLanguage)
    throws Exception
  {
    Content content = null;
    HttpClient httpClient = new HttpClient();
    httpClient.setURL(url);
    httpClient.setForceHttp(true);
    httpClient.setMaxContentLength(524288);
    httpClient.setConnectTimeout(1 * 60 * 1000); // 1 minute
    httpClient.setReadTimeout(1 * 60 * 1000); // 1 minute
    httpClient.setRequestProperty("User-Agent", HttpClient.USER_AGENT_IE6);
    httpClient.setRequestProperty("Accept-Charset", "utf-8");
    if (userLanguage != null)
    {
      httpClient.setRequestProperty("Accept-Language", userLanguage);
    }
    httpClient.connect();
    String data = httpClient.getContentAsString();
    if (data != null)
    {
      content = new Content();
      content.data = data;
      content.language = httpClient.getContentLanguage();
      content.mimeType = httpClient.getContentType();
    }
    return content;
  }

  private String createString(InputStream is) throws IOException
  {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int count = is.read(buffer);
    while (count != -1)
    {
      os.write(buffer, 0, count);
      count = is.read(buffer);
    }
    is.close();
    return new String(os.toByteArray(), "utf-8");
  }

  private DocumentManagerClient getDocumentManagerClient()
    throws Exception
  {
    String userId =
      MatrixConfig.getProperty("adminCredentials.userId");
    String password =
      MatrixConfig.getProperty("adminCredentials.password");

    return new DocumentManagerClient(userId, password);
  }

  class Content
  {
    String data;
    String language;
    String mimeType;
  }
}
