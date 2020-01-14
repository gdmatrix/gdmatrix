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
package org.santfeliu.ant.logs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *
 * @author realor
 */
public class Matrix2WebLogLoaderTask extends LogFileLoaderTask
{
  BufferedReader reader = null;
  String separator = ";";
  String extractParameters;

  private static final String OTHERS = "Others";
  private static final String OS[] =
  {
    "Windows",
    "Macintosh",
    "Linux",
    "Android",
    "Symbian"
  };
  private  final String br[] =
  {
    "MSIE",
    "Firefox",
    "Chrome",
    "Safari",
    "Opera"
  };

  public String getSeparator()
  {
    return separator;
  }

  public void setSeparator(String separator)
  {
    this.separator = separator;
  }

  public String getExtractParameters()
  {
    return extractParameters;
  }

  public void setExtractParameters(String extractParameters)
  {
    this.extractParameters = extractParameters;
  }

  @Override
  protected void initReader(File file) throws Exception
  {
    reader = new BufferedReader(
      new InputStreamReader(new FileInputStream(file)));
  }

  @Override
  protected boolean readEntry(Map<String, String> entryMap) throws Exception
  {
    String line = reader.readLine();
    if (line == null) return false;

    String columns[] = line.split(separator);
    String dateTime, userId, action, path, ip, userAgent, browserLanguage,
      language, sessionId, method, parameters, browserType;

    if (columns.length > 0) 
    {
      dateTime = columns[0];
      entryMap.put(DATETIME_FIELD, dateTime);
      if (columns.length > 1) 
      {
        userId = columns[1];
        entryMap.put(USER_FIELD, userId);
        if (columns.length > 2)
        {
          action = columns[2];
          entryMap.put(ACTION_FIELD, action);
          if (columns.length > 3)
          {
            path = columns[3];
            entryMap.put("path", path);
            if (columns.length > 4)
            {
              ip = columns[4];
              entryMap.put(IP_FIELD, ip);
              if (columns.length > 6)
              {
                userAgent = columns[6];
                processUserAgent(userAgent, entryMap);
                entryMap.put("userAgent", userAgent);
                if (columns.length > 7)
                {
                  browserLanguage = columns[7];
                  entryMap.put("browserLanguage", browserLanguage);
                  if (columns.length > 8)
                  {
                    language = columns[8];
                    entryMap.put("language", language);
                    if (columns.length > 9)
                    {
                      sessionId = columns[9];
                      entryMap.put("sessionId", sessionId);
                      if (columns.length > 10)
                      {
                        method = columns[10];
                        entryMap.put("method", method);
                        if (columns.length > 11)
                        {
                          parameters = columns[11];
                          processParameters(parameters, entryMap);
                          entryMap.put("parameters", parameters);
                          if (columns.length > 12)
                          {
                            browserType = columns[12];
                            entryMap.put("browserType", browserType);
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return true;
  }

  @Override
  protected void closeReader()
  {
    try
    {
      reader.close();
    }
    catch (Exception ex)
    {
    }
  }

  private void processUserAgent(String userAgent, Map<String, String> entryMap)
  {
    String browser = OTHERS;
    String os = OTHERS;
    boolean robot = false;

    boolean find = false;
    int i = 0;
    while (i < OS.length && !find)
    {
      if (userAgent.contains(OS[i]))
      {
        os = OS[i];
        find = true;
      }
      i++;
    }
    robot = !find;

    String version = "";
    find = false;
    i = 0;
    while (i < br.length && !find)
    {
      if (userAgent.contains(br[i]))
      {
        find = true;
        browser = br[i];
        int index = userAgent.indexOf(br[i]);
        int length = br[i].length();
        if (index < 0 || index + length + 5 >= userAgent.length())
        {
          version = "";
        }
        else
        {
          version = userAgent.substring(index + length, index + length + 5);
          version = version.replace("/", " ");
          version = version.replace("\\", " ");
          if (version.endsWith(".") || version.endsWith("b"))
          {
            version = version.substring(0, version.length() - 1);
          }
        }
      }
      i++;
    }
    String browserVersion = browser + " " + version;
    entryMap.put("browser", browser);
    entryMap.put("os", os);
    entryMap.put("robot", robot ? "true" : "false");
    entryMap.put("browserVersion", browserVersion.trim());
  }

  private void processParameters(String parameters,
    Map<String, String> entryMap)
  {
    if (extractParameters != null && parameters != null)
    {
      String[] extractParametersArray = extractParameters.split(",");
      for (String parameter : extractParametersArray)
      {
        boolean isPrefix = false;
        if (parameter.endsWith("*"))
        {
          isPrefix = true;
          parameter = parameter.substring(0, parameter.length() - 1);
        }
        String value = extractParameter(parameter, parameters, isPrefix);
        if (value != null)
        {
          entryMap.put(parameter, value);
        }
      }
    }
  }

  private String extractParameter(String parameter, String parameters,
    boolean isPrefix)
  {    
    String auxParameters = parameters;
    if (auxParameters.startsWith("?"))
    {
      auxParameters = auxParameters.substring(1);
    }
    StringTokenizer tokenizer = new StringTokenizer(auxParameters, "&");
    while (tokenizer.hasMoreTokens())
    {
      String token = tokenizer.nextToken();      
      int i = token.indexOf("=");
      if (i >= 0)
      {
        String name = token.substring(0, i);
        if ((!isPrefix && name.equals(parameter)) ||
          (isPrefix && name.startsWith(parameter)))
        {
          String value = token.substring(i + 1);
          if (value != null && value.trim().length() > 0)
          {
            return value.trim();
          }
        }
      }
    }
    return null;
  }
  
}
