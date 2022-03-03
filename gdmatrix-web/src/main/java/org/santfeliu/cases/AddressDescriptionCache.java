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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.apache.commons.lang.StringUtils;
import org.matrix.cases.Case;
import org.matrix.cases.CaseAddressFilter;
import org.matrix.cases.CaseAddressView;
import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CaseManagerService;
import org.matrix.kernel.AddressFilter;
import org.matrix.kernel.AddressView;
import org.matrix.kernel.KernelManagerPort;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.jmx.CacheMBean;
import org.santfeliu.jmx.JMXUtils;
import org.santfeliu.kernel.web.KernelConfigBean;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author blanquepa
 */
public class AddressDescriptionCache
{
  private static int MAP_MAX_SIZE = 500;

  private static AddressDescriptionCache cache;

  private Credentials credentials;
  private String caseTypeId;

  private Map<String,String> descriptions;
  private long lastClearMillis;
  private static long CLEAR_MILLIS = 15 * 24 * 60 * 60 * 1000; // 15 days

  public AddressDescriptionCache()
  {
    lastClearMillis = System.currentTimeMillis();
    credentials = getCredentials();
    caseTypeId = getCaseTypeId();
  }

  public synchronized String getAddressDescription(String addressId)
  {
    try
    {
      long nowMillis = System.currentTimeMillis();
      if (mustClearCache(nowMillis)) // clear cache
      {
        clear();
        lastClearMillis = nowMillis;
      }

      if (descriptions == null)
        descriptions = new HashMap();

      if (!StringUtils.isBlank(addressId))
      {
        String description = descriptions.get(addressId);
        if (description == null)
        {
          CaseAddressFilter filter = new CaseAddressFilter();
          filter.setAddressId(addressId);
          List<CaseAddressView> views = 
            getCasePort().findCaseAddressViews(filter);
          for (CaseAddressView view : views)
          {
            Case cas = view.getCaseObject();
            String typeId = cas.getCaseTypeId();
            if (typeId != null)
            {
              Type type = TypeCache.getInstance().getType(typeId);
              if (type.isDerivedFrom(caseTypeId))
              {
                description = cas.getTitle();
                descriptions.put(addressId, description);
                return description;
              }
            }
          }
          
          if (description == null)
          {
            AddressFilter addressFilter = new AddressFilter();
            addressFilter.getAddressIdList().add(addressId);
            KernelManagerPort port = KernelConfigBean.getPortAsAdmin();
            List<AddressView> addressViews =
              port.findAddressViews(addressFilter);
            if (addressViews != null && !addressViews.isEmpty())
              description = addressViews.get(0).getDescription();
          }
          
          if (StringUtils.isBlank(description))
            description = "NOT_FOUND";

          descriptions.put(addressId, description);
        }
        return description;
      }
      else
        return addressId;
    }
    catch (Exception ex)
    {       
      return addressId;
    }    
  }

  public static AddressDescriptionCache getInstance()
  {
    if (cache == null)
    {
      cache = new AddressDescriptionCache();
      JMXUtils.registerMBean("AddressDescriptionCache", cache.getCacheMBean());
    }
    return cache;
  }

  public void clear()
  {
    clear(null);
  }

  public void clear(String caseId)
  {
    if (caseId == null) //full clear
      descriptions.clear();
    else
      descriptions.remove(caseId);
  }

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

  private String getCaseTypeId()
  {
    return MatrixConfig.getProperty("addressDescTypeCaseId");
  }

  private boolean mustClearCache(long nowMillis)
  {
    return nowMillis - lastClearMillis > CLEAR_MILLIS;
  }

  private CaseManagerPort getCasePort()
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

  private AddressCacheMBean getCacheMBean()
  {
    try
    {
      return new AddressCacheMBean();
    }
    catch (NotCompliantMBeanException ex)
    {
      return null;
    }
  }

  public class AddressCacheMBean extends StandardMBean implements CacheMBean
  {
    public AddressCacheMBean() throws NotCompliantMBeanException
    {
      super(CacheMBean.class);
    }

    public String getName()
    {
      return "AddressDescriptionCache";
    }

    public long getMaxSize()
    {
      return MAP_MAX_SIZE;
    }

    public long getSize()
    {
      return descriptions.size();
    }

    public String getDetails()
    {
      return "totalMapSize=" + getSize() + "/" + getMaxSize() ;
    }

    public void clear()
    {
      AddressDescriptionCache.this.clear();
    }

    public void update()
    {
      clear();
    }
  }
}
