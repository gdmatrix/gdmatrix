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
package org.santfeliu.misc.systeminfo.web;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;

/**
 *
 * @author realor
 */
@CMSManagedBean
public class SystemInfoBean extends FacesBean
{
  public static RuntimeMXBean RUNTIME_MX_BEAN = getRuntimeMXBean();

  public String getUpTime()
  {
    long ms = RUNTIME_MX_BEAN.getUptime();
    int secs = (int)(ms / 1000);
    int d = (int)(secs / (24 * 60 * 60));
    secs = secs - d * (24 * 60 * 60);
    int h = (int)(secs / (60 * 60));
    secs = secs - h * (60 * 60);
    int m = (int)(secs / 60);
    secs = secs - m * 60;
    int s = secs;
    return (d > 0 ? d + "d " : "") +
      (h > 0 ? h + "h " : "") +
      (m > 0 ? m + "m " : "") +
      s + "s";
  }

  public String getStartDateTime()
  {
    Date date = new Date(RUNTIME_MX_BEAN.getStartTime());
    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    return format.format(date);
  }

  public List<String> getSystemProperties()
  {
    List<String> result = new ArrayList<String>();
    Map<String, String> properties = RUNTIME_MX_BEAN.getSystemProperties();
    for (String sKey : properties.keySet())
    {
      String value = properties.get(sKey);
      result.add(sKey + " = "+ (value != null ? value : "null"));
    }
    return result;
  }

  public List<String> getLibraryPath()
  {
    String s = RUNTIME_MX_BEAN.getLibraryPath();
    List<String> result = TextUtils.stringToList(s, ";");
    if (result == null) result = new ArrayList<String>();
    return result;
  }

  public List<String> getBootClassPath()
  {
    List<String> result = new ArrayList<String>();
    if (RUNTIME_MX_BEAN.isBootClassPathSupported())
    {
      String s = RUNTIME_MX_BEAN.getBootClassPath();
      result = TextUtils.stringToList(s, ";");
    }
    return result;
  }

  public List<String> getClassPath()
  {
    String s = RUNTIME_MX_BEAN.getClassPath();
    List<String> result = TextUtils.stringToList(s, ";");
    if (result == null) result = new ArrayList<String>();
    return result;
  }

  public RuntimeMXBean getRuntimeBean()
  {
    return RUNTIME_MX_BEAN;
  }

  @CMSAction
  public String show()
  {
    return "system_info";
  }

  private static RuntimeMXBean getRuntimeMXBean()
  {
    return ManagementFactory.getRuntimeMXBean();
  }
}
