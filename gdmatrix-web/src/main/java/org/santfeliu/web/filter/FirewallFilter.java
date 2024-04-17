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
package org.santfeliu.web.filter;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.santfeliu.jmx.JMXUtils;

/**
 *
 * @author realor
 */
public class FirewallFilter implements Filter
{
  static final Logger LOGGER =
    Logger.getLogger(FirewallFilter.class.getSimpleName());
  static final String PATTERN_PREFIX = "pattern.";
  static final String MBEAN_NAME = "Firewall";
  static final int LAST_BLOCKED_REQUEST_COUNT = 10;

  final List<Check> checks = new ArrayList<>();
  int totalRequestCount = 0;
  int maliciousRequestCount = 0;
  Map<String, Integer> maliciousRequestCountByType = new HashMap<>();
  LinkedList<String> lastBlockedRequests = new LinkedList<>();
  CompositeType compositeType;
  FirewallMBean mbean;
  SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  @Override
  public void init(FilterConfig config) throws ServletException
  {
    LOGGER.log(Level.INFO, "Init firewall filter");

    // load config
    List<String> itemNames = new ArrayList<>();
    List<String> itemDescriptions = new ArrayList<>();
    List<OpenType> itemTypes = new ArrayList<>();
    Enumeration<String> names = config.getInitParameterNames();
    while (names.hasMoreElements())
    {
      String name = names.nextElement();
      if (name.startsWith(PATTERN_PREFIX))
      {
        String type = name.substring(PATTERN_PREFIX.length());
        String regex = config.getInitParameter(name);
        LOGGER.log(Level.INFO, "Creating pattern \"{0}\" for \"{1}\" type",
          new Object[]{regex, type});
        Check check = new Check(type, regex);
        checks.add(check);
        itemNames.add(type);
        itemDescriptions.add(type + " type request count");
        itemTypes.add(SimpleType.INTEGER);
        maliciousRequestCountByType.put(type, 0);
      }
    }
    try
    {
      // open type creation
      compositeType = new CompositeType("MaliciousRequestCountByType",
        "Malicious request count by type",
        itemNames.toArray(new String[0]),
        itemDescriptions.toArray(new String[0]),
        itemTypes.toArray(new OpenType[0]));

      // register bean
      mbean = new FirewallMBean();
      JMXUtils.registerMBean(MBEAN_NAME, mbean);
    }
    catch (NotCompliantMBeanException | OpenDataException ex)
    {
      LOGGER.log(Level.SEVERE, "MBean creation error", ex);
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
    FilterChain chain) throws IOException, ServletException
  {
    HttpServletRequest httpRequest = (HttpServletRequest)request;
    HttpServletResponse httpResponse = (HttpServletResponse)response;

    totalRequestCount++;

    if ("GET".equals(httpRequest.getMethod()))
    {
      String path = httpRequest.getServletPath();
      String query = httpRequest.getQueryString();
      if (query != null) path += "?" + query;
      String decodedPath = URLDecoder.decode(path, "UTF-8");
      if (isMaliciousPath(decodedPath))
      {
        LOGGER.log(Level.SEVERE, "Malicious request detected: {0}", path);
        httpResponse.sendError(400, "Malicious request detected");
      }
      else
      {
        chain.doFilter(request, response);
      }
    }
    else
    {
      chain.doFilter(request, response);
    }
  }

  @Override
  public void destroy()
  {
    if (mbean != null)
    {
      JMXUtils.unregisterMBean(MBEAN_NAME);
    }
    reset();
  }

  private void reset()
  {
    totalRequestCount = 0;
    maliciousRequestCount = 0;
    synchronized (this)
    {
      for (String type : maliciousRequestCountByType.keySet())
      {
        maliciousRequestCountByType.put(type, 0);
      }
      lastBlockedRequests.clear();
    }
  }

  private boolean isMaliciousPath(String path)
  {
    for (Check check : checks)
    {
      if (check.isMaliciousPath(path))
      {
        String type = check.getType();
        synchronized (this)
        {
          maliciousRequestCount++;
          Integer count = maliciousRequestCountByType.get(type);
          if (count == null) count = 0;
          count++;
          maliciousRequestCountByType.put(type, count);
          String dateString = dateFormat.format(new Date());
          lastBlockedRequests.add(dateString + ": " + path);
          if (lastBlockedRequests.size() > LAST_BLOCKED_REQUEST_COUNT)
          {
            lastBlockedRequests.removeFirst();
          }
        }
        return true;
      }
    }
    return false;
  }

  static class Check
  {
    final String type;
    final Pattern pattern;

    Check(String type, String regex)
    {
      this.type = type;
      this.pattern = Pattern.compile(regex);
    }

    boolean isMaliciousPath(String path)
    {
      return pattern.matcher(path).matches();
    }

    public String getType()
    {
      return type;
    }
  }

  public static interface FirewallStatistics
  {
    int getTotalRequestCount();
    int getMaliciousRequestCount();
    CompositeDataSupport getMaliciousRequestCountByType()
      throws OpenDataException;
    String[] getLastBlockedRequests();
    void resetCounters();
  }

  public class FirewallMBean extends StandardMBean implements FirewallStatistics
  {
    public <T extends Object> FirewallMBean()
      throws NotCompliantMBeanException
    {
      super(FirewallStatistics.class);
    }

    @Override
    public int getTotalRequestCount()
    {
      return totalRequestCount;
    }

    @Override
    public int getMaliciousRequestCount()
    {
      return maliciousRequestCount;
    }

    @Override
    public CompositeDataSupport getMaliciousRequestCountByType()
      throws OpenDataException
    {
      return new CompositeDataSupport(compositeType, maliciousRequestCountByType);
    }

    @Override
    public String[] getLastBlockedRequests()
    {
      int count = lastBlockedRequests.size();
      return lastBlockedRequests.toArray(new String[count]);
    }

    @Override
    public void resetCounters()
    {
      reset();
    }
  }
}
