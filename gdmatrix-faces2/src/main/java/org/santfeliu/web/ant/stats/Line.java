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
package org.santfeliu.web.ant.stats;

import org.santfeliu.util.enc.Unicode;

public final class Line
{
  public static final String OTHERS = "Others";

  private String date;
  private String userId;
  private String nodeId;
  private String path;
  private String ip;
  private String browser;
  private String browserVersion;
  private String os;
  private String language;
  private String sessionId;
  private String method;
  private String parameters;
  private boolean robot;
  static final String OS[] =
  {
    "Windows",
    "Macintosh",
    "Linux",
    "Android",
    "Symbian"
  };
  static final String br[] =
  {
    "MSIE",
    "Firefox",
    "Chrome",
    "Safari",
    "Opera"
  };

  public Line()
  {
  }

  public Line(String line)
  {
    parse(line);
  }

  public String getUserId()
  {
    return userId;
  }

  public void setUserId(String userId)
  {
    this.userId = userId;
  }

  public String getBrowser()
  {
    return browser;
  }

  public void setBrowser(String browser)
  {
    this.browser = browser;
  }

  public String getBrowserVersion()
  {
    return browserVersion;
  }

  public void setBrowserVersion(String browserVersion)
  {
    this.browserVersion = browserVersion;
  }

  public String getDate()
  {
    return date;
  }

  public void setDate(String date)
  {
    this.date = date;
  }

  public String getIp()
  {
    return ip;
  }

  public void setIp(String ip)
  {
    this.ip = ip;
  }

  public String getLanguage()
  {
    return language;
  }

  public void setLanguage(String language)
  {
    this.language = language;
  }

  public String getMethod()
  {
    return method;
  }

  public void setMethod(String method)
  {
    this.method = method;
  }

  public String getNodeId()
  {
    return nodeId;
  }

  public void setNodeId(String nodeId)
  {
    this.nodeId = nodeId;
  }

  public String getOs()
  {
    return os;
  }

  public void setOs(String os)
  {
    this.os = os;
  }

  public String getParameters()
  {
    return parameters;
  }

  public void setParameters(String parameters)
  {
    this.parameters = parameters;
  }

  public String getPath()
  {
    return path;
  }

  public void setPath(String path)
  {
    this.path = path;
  }

  public String getSessionId()
  {
    return sessionId;
  }

  public void setSessionId(String sessionId)
  {
    this.sessionId = sessionId;
  }

  public boolean isRobot()
  {
    return robot;
  }

  public void setRobot(boolean robot)
  {
    this.robot = robot;
  }

  public void parse(String sline)
  {
    String[] values = sline.split(";");
    date = values[0];
    userId = values[1];
    nodeId = values[2];
    path = Unicode.decode(values[3]);
    ip = values[4];
    processAgent(values[6]);
    language = values[8];
    sessionId = values[9];
    if (values.length > 10)
    {
      method = values[10];
      parameters = Unicode.decode(values[11]);
    }
  }

  private void processAgent(String agent)
  {
    browser = OTHERS;
    os = OTHERS;
    robot = false;
    boolean find = false;

    int i = 0;
    while (i < OS.length && !find)
    {
      if (agent.contains(OS[i]))
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
      if (agent.contains(br[i]))
      {
        browser = br[i];
        int index = agent.indexOf(br[i]);
        int longitud = br[i].length();
        if (index < 0 || index + longitud + 5 >= agent.length())
        {
          version = "";
        }
        else
        {
          version = agent.substring(index + longitud, index + longitud + 5);
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
    browserVersion = browser + " " + version;
  }
}
