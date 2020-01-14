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
package org.santfeliu.translation.string;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.santfeliu.translation.StringTranslator;
import org.santfeliu.translation.TranslatorFactory;
import org.santfeliu.translation.util.TranslationUtils;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author realor
 */
public class GoogleStringTranslator implements StringTranslator
{
  private static String sourceLanguage;
  private static String serviceURL =
    "https://www.googleapis.com/language/translate/v2";
  private static String apiKey;

  protected static final Logger logger =
    Logger.getLogger("GoogleStringTranslator");

  static
  {
    try
    {
      sourceLanguage = TranslatorFactory.getDefaultLanguage();

      String value = MatrixConfig.getClassProperty(
        GoogleStringTranslator.class, "serviceURL");
      if (value != null) serviceURL = value;

      value = MatrixConfig.getClassProperty(
        GoogleStringTranslator.class, "apiKey");
      if (value != null) apiKey = value;
      else
      {
        value = System.getenv("GoogleAPIKey");
        if (value != null) apiKey = value;
        else logger.log(Level.WARNING, "Google API key undefined");
      }
    }
    catch (Exception ex)
    {
      logger.log(Level.SEVERE, "init failed", ex);
    }
  }

  public String translate(String text, String destLanguage, String group)
  {
    String translation = null;
    try
    {
      translation = fastTranslate(destLanguage, text);
      if (translation == null && apiKey != null)
      {
        StringBuilder urlBuffer = new StringBuilder();
        urlBuffer.append(serviceURL);
        urlBuffer.append("?key=").append(apiKey);
        urlBuffer.append("&source=").append(sourceLanguage);
        urlBuffer.append("&target=").append(destLanguage);
        text = prepareText(text);
        urlBuffer.append("&q=").append(URLEncoder.encode(text, "UTF-8"));

        URL url = new URL(urlBuffer.toString());
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        InputStream is;
        try
        {
          is = conn.getInputStream();
          translation = readTranslation(is);
          translation = processTranslation(translation, destLanguage);
        }
        catch (IOException ex)
        {
          is = conn.getErrorStream();
          String errorMessage = readErrorMessage(is);
          translation = text; // echo text
          logger.log(Level.SEVERE, errorMessage);
        }
      }
    }
    catch (Exception ex)
    {
      translation = text; // echo text
      logger.log(Level.SEVERE, ex.toString());
    }
    return translation;
  }

  private String readTranslation(InputStream is) throws Exception
  {
    String translation = null;
    Reader reader = new InputStreamReader(is, "UTF-8");
    try
    {
      JSONParser parser = new JSONParser();
      JSONObject object = (JSONObject)parser.parse(reader);
      JSONObject data = (JSONObject)object.get("data");
      JSONArray translationsArray = (JSONArray)data.get("translations");
      if (translationsArray.size() == 1)
      {
        JSONObject translationObj = (JSONObject)translationsArray.get(0);
        translation = (String)translationObj.get("translatedText");
      }
    }
    finally
    {
      reader.close();
    }
    return translation;
  }

  private String readErrorMessage(InputStream is) throws Exception
  {
    String errorMessage = null;
    Reader reader = new InputStreamReader(is, "UTF-8");
    try
    {
      JSONParser parser = new JSONParser();
      JSONObject object = (JSONObject)parser.parse(reader);
      JSONObject error = (JSONObject)object.get("error");
      String code = String.valueOf(error.get("code"));
      String message = (String)error.get("message");
      errorMessage = "Error " + code + ": " + message;
    }
    finally
    {
      reader.close();
    }
    return errorMessage;
  }

  private String fastTranslate(String destLanguage, String text)
  {
    if (destLanguage.equals(sourceLanguage)) return text;
    return TranslationUtils.directTranslate(destLanguage, text);
  }

  private String prepareText(String text)
  {
    // remove help texts inside marks
    text = text.replaceAll("\\:\\:\\:.*?\\*\\)", "*)");
    // remove empty marks
    text = text.replaceAll("\\(\\*\\*\\)", "");
    // replace curling by normal apostrophe
    text = text.replace('\u2019', '\'');
    return text;
  }

  private String processTranslation(String translation, String destLanguage)
  {
    // replace html symbol &#39; and &quot;
    translation = translation.replaceAll("&#39;", "'");
    translation = translation.replaceAll("&quot;", "'");
    // replace html symbols > and <
    translation = translation.replaceAll("&gt;", ">");
    translation = translation.replaceAll("&lt;", "<");
    // replace chinese by normal parenthesis
    if ("zh".equals(destLanguage))
    {
      translation = translation.replace('\uff08', '(');
      translation = translation.replace('\uff09', ')');
    }
    // remove whitespace between ( and *
    translation = translation.replaceAll("\\( \\*", "(*");
    // remove whitespace between * and )
    translation = translation.replaceAll("\\* \\)", "*)");
    return translation;
  }

  public static void main(String[] args)
  {
    GoogleStringTranslator googleTranslator = new GoogleStringTranslator();    
    String translation = googleTranslator.translate(
      "l'Alcalde no va dir res.", "en", "grp");
    System.out.println(translation);
  }
}
