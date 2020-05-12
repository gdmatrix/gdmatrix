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
package org.santfeliu.cases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.apache.commons.collections.LRUMap;
import org.matrix.cases.CaseCaseFilter;
import org.matrix.cases.CaseCaseView;
import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CaseManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.jmx.CacheMBean;
import org.santfeliu.jmx.JMXUtils;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author lopezrj
 */
public class CaseCaseCache
{
  private static int MAP_MAX_SIZE = 1000;
  private static long CLEAR_MILLIS = 12 * 60 * 60 * 1000; // 12 hours

  private static CaseCaseCache cache;

  private Map directMap;
  private Map reverseMap;

  private Credentials credentials;

  private long lastClearMillis;

  public static synchronized CaseCaseCache getInstance()
  {
    if (cache == null)
    {
      cache = new CaseCaseCache();
      JMXUtils.registerMBean("CaseCaseCache", cache.getCacheMBean());
    }
    return cache;
  }

  protected CaseCaseCache()
  {    
    lastClearMillis = System.currentTimeMillis();
    credentials = getCredentials();
    directMap = Collections.synchronizedMap(new LRUMap(MAP_MAX_SIZE));
    reverseMap = Collections.synchronizedMap(new LRUMap(MAP_MAX_SIZE));
  }
  
  public List<CacheItem> getDirectCaseCases(String caseId)
  {
    return getRelatedCaseCases(caseId, true, null);
  }

  public List<CacheItem> getDirectCaseCases(String caseId, String date)
  {
    return getRelatedCaseCases(caseId, true, date);
  }

  public List<CacheItem> getReverseCaseCases(String caseId)
  {
    return getRelatedCaseCases(caseId, false, null);
  }

  public List<CacheItem> getReverseCaseCases(String caseId, String date)
  {
    return getRelatedCaseCases(caseId, false, date);
  }

  public void clear()
  {
    clear(null);
  }

  public void clear(String caseId)
  {
    if (caseId == null) //full clear
    {
      directMap.clear();
      reverseMap.clear();
    }
    else
    {
      //Direct clear
      List<CacheItem> cacheItemList = (List<CacheItem>)directMap.get(caseId);
      if (cacheItemList != null)
      {
        for (CacheItem cacheItem : cacheItemList)
        {
          reverseMap.remove(cacheItem.getRelCaseId());
        }
      }
      directMap.remove(caseId);

      //Reverse clear
      cacheItemList = (List<CacheItem>)reverseMap.get(caseId);
      if (cacheItemList != null)
      {
        for (CacheItem cacheItem : cacheItemList)
        {
          directMap.remove(cacheItem.getMainCaseId());
        }
      }
      reverseMap.remove(caseId);
    }
  }

  public void reset()
  {
    JMXUtils.unregisterMBean("CaseCaseCache");
    clear();
  }

  // private methods

  private Credentials getCredentials()
  {
    String userId = null;
    String password = null;
    userId = MatrixConfig.getProperty("adminCredentials.userId");
    if (userId != null)
    {
      password = MatrixConfig.getProperty("adminCredentials.password");
    }
    return new Credentials(userId, password);
  }

  private boolean mustClearCaseCases(long nowMillis)
  {
    return nowMillis - lastClearMillis > CLEAR_MILLIS;
  }

  private List<CacheItem> getRelatedCaseCases(String caseId, boolean direct,
    String date)
  {
    Map map;
    if (direct)
      map = directMap;
    else
      map = reverseMap;

    long nowMillis = System.currentTimeMillis();
    if (mustClearCaseCases(nowMillis)) // clear cache
    {
      clear();
      lastClearMillis = nowMillis;
    }

    List<CacheItem> cacheItemList = (List<CacheItem>)map.get(caseId);
    if (cacheItemList == null)
    {
      try
      {
        cacheItemList = new ArrayList<CacheItem>();
        CaseCaseFilter filter = new CaseCaseFilter();
        if (direct)
          filter.setCaseId(caseId);
        else
          filter.setRelCaseId(caseId);
        List<CaseCaseView> caseCaseViewList = getPort().findCaseCaseViews(filter);
        for (CaseCaseView caseCaseView : caseCaseViewList)
        {
          cacheItemList.add(new CacheItem(caseCaseView));
        }
        map.put(caseId, cacheItemList);
      }
      catch (Exception ex)
      {
        // case not found
      }
    }
    List<CacheItem> result = new ArrayList<CacheItem>();
    if (cacheItemList != null)
    {
      for (CacheItem cacheItem : cacheItemList)
      {
        if (includeItem(cacheItem, date))
        {
          result.add(cacheItem);
        }
      }
    }
    else
    {
      result = cacheItemList;
    }
    return result;
  }

  private boolean includeItem(CacheItem item, String date)
  {
    if (date == null || date.isEmpty()) return true; //show full history

    String startDate = "00000000";
    String endDate = "99999999";
    if (item.getCaseCaseStartDate() != null)
      startDate = item.getCaseCaseStartDate();
    if (item.getCaseCaseEndDate() != null)
      endDate = item.getCaseCaseEndDate();
    if (date.compareTo(startDate) < 0) //Future Item
    {
      return false;
    }
    else if (date.compareTo(endDate) > 0) //Old Item
    {
      return false;
    }
    else //Current Item
    {
      return true;
    }
  }

  private CaseManagerPort getPort()
  {
    try
    {
      WSDirectory wsDirectory = WSDirectory.getInstance();
      WSEndpoint endpoint =
        wsDirectory.getEndpoint(CaseManagerService.class);
      return endpoint.getPort(CaseManagerPort.class,
        credentials.getUserId(), credentials.getPassword());
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  private CaseCaseCacheMBean getCacheMBean()
  {
    try
    {
      return new CaseCaseCacheMBean();
    }
    catch (NotCompliantMBeanException ex)
    {
      return null;
    }
  }

  public class CaseCaseCacheMBean extends StandardMBean implements CacheMBean
  {
    public CaseCaseCacheMBean() throws NotCompliantMBeanException
    {
      super(CacheMBean.class);
    }

    public String getName()
    {
      return "CaseCaseCache";
    }

    public long getMaxSize()
    {
      return 2 * MAP_MAX_SIZE;
    }

    public long getSize()
    {
      return directMap.size() + reverseMap.size();
    }

    public String getDetails()
    {
      return "totalMapSize=" + getSize() + "/" + getMaxSize() + "," +
        "directMapSize=" + directMap.size() + "," +
        "reverseMapSize=" + reverseMap.size();
    }

    public void clear()
    {
      CaseCaseCache.this.clear();
    }

    public void update()
    {
      clear();
    }
  }

  public class CacheItem
  {
    private String mainCaseId;
    private String mainCaseTitle;
    private String mainCaseTypeId;

    private String relCaseId;
    private String relCaseTitle;
    private String relCaseTypeId;

    private String caseCaseTypeId;
    private String caseCaseStartDate;
    private String caseCaseEndDate;

    public CacheItem(CaseCaseView caseCaseView)
    {
      this.mainCaseId = caseCaseView.getMainCase().getCaseId();
      this.mainCaseTitle = caseCaseView.getMainCase().getTitle();
      this.mainCaseTypeId = caseCaseView.getMainCase().getCaseTypeId();
      this.relCaseId = caseCaseView.getRelCase().getCaseId();
      this.relCaseTitle = caseCaseView.getRelCase().getTitle();
      this.relCaseTypeId = caseCaseView.getRelCase().getCaseTypeId();
      this.caseCaseTypeId = caseCaseView.getCaseCaseTypeId();
      this.caseCaseStartDate = caseCaseView.getStartDate();
      this.caseCaseEndDate = caseCaseView.getEndDate();
    }

    public String getCaseCaseTypeId()
    {
      return caseCaseTypeId;
    }

    public void setCaseCaseTypeId(String caseCaseTypeId)
    {
      this.caseCaseTypeId = caseCaseTypeId;
    }

    public String getCaseCaseEndDate()
    {
      return caseCaseEndDate;
    }

    public void setCaseCaseEndDate(String caseCaseEndDate)
    {
      this.caseCaseEndDate = caseCaseEndDate;
    }

    public String getCaseCaseStartDate()
    {
      return caseCaseStartDate;
    }

    public void setCaseCaseStartDate(String caseCaseStartDate)
    {
      this.caseCaseStartDate = caseCaseStartDate;
    }

    public String getMainCaseId()
    {
      return mainCaseId;
    }

    public void setMainCaseId(String mainCaseId)
    {
      this.mainCaseId = mainCaseId;
    }

    public String getMainCaseTitle()
    {
      return mainCaseTitle;
    }

    public void setMainCaseTitle(String mainCaseTitle)
    {
      this.mainCaseTitle = mainCaseTitle;
    }

    public String getMainCaseTypeId()
    {
      return mainCaseTypeId;
    }

    public void setMainCaseTypeId(String mainCaseTypeId)
    {
      this.mainCaseTypeId = mainCaseTypeId;
    }

    public String getRelCaseId()
    {
      return relCaseId;
    }

    public void setRelCaseId(String relCaseId)
    {
      this.relCaseId = relCaseId;
    }

    public String getRelCaseTitle()
    {
      return relCaseTitle;
    }

    public void setRelCaseTitle(String relCaseTitle)
    {
      this.relCaseTitle = relCaseTitle;
    }

    public String getRelCaseTypeId()
    {
      return relCaseTypeId;
    }

    public void setRelCaseTypeId(String relCaseTypeId)
    {
      this.relCaseTypeId = relCaseTypeId;
    }
  }

}
