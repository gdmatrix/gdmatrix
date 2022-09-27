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
package org.matrix.pf.cms;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.inject.Named;
import org.matrix.pf.web.WebBacking;
import org.santfeliu.web.ApplicationBean;
import org.santfeliu.web.bean.CMSProperty;

/**
 *
 * @author lopezrj-sf
 */
@Named("pf_3colsTemplateConfigBacking")
public class PF3ColsTemplateConfigBacking extends WebBacking
{
  @CMSProperty
  public static final String ICON = "icon";
  @CMSProperty
  public static final String LMENU = "lmenu"; 
  @CMSProperty
  public static final String LANGUAGE = "language"; 
  @CMSProperty
  public static final String LAST_SUCCESS_LOGIN_DT = "last_success_login_dt"; 
  @CMSProperty
  public static final String OBJECT_TYPE_ID = "objectTypeId"; 
  @CMSProperty
  public static final String OBJECT_BACKING = "objectBacking"; 
  @CMSProperty
  public static final String PAGE_SIZE = "pageSize"; 
  
  public PF3ColsTemplateConfigBacking()
  {
  }
  
  public String getIcon()
  {
    return getProperty(ICON, true);
  }
  
  public void setIcon(String icon)
  {
    getConfigHelper().setProperty(ICON, icon);
  }
  
  public String getLMenu()
  {
    return getProperty(LMENU, true);
  }
  
  public void setLMenu(String lmenu)
  {
    getConfigHelper().setProperty(LMENU, lmenu);
  } 

  public String getDefaultLMenu()
  {
    return "false";
  }  
  
  public List<String> getLanguage()
  {
    return getMultivaluedProperty(LANGUAGE, true);
  }
  
  public void setLanguage(List<String> language)
  {
    getConfigHelper().setMultivaluedProperty(LANGUAGE, language);
  }
  
  public List<String> getInheritedLanguage()
  {
    return getMultivaluedProperty(LANGUAGE, false);
  }
  
  public String getLastSuccessLoginDT()
  {
    return getProperty(LAST_SUCCESS_LOGIN_DT, true);
  }
  
  public void setLastSuccessLoginDT(String lastSuccessLoginDT)
  {
    getConfigHelper().setProperty(LAST_SUCCESS_LOGIN_DT, lastSuccessLoginDT);
  } 

  public String getInheritedLastSuccessLoginDT()
  {
    return getProperty(LAST_SUCCESS_LOGIN_DT, false);
  }

  public String getObjectTypeId()
  {
    return getProperty(OBJECT_TYPE_ID, true);
  }
  
  public void setObjectTypeId(String objectTypeId)
  {
    getConfigHelper().setProperty(OBJECT_TYPE_ID, objectTypeId);
  } 
  
  public String getObjectBacking()
  {
    return getProperty(OBJECT_BACKING, true);
  }
  
  public void setObjectBacking(String objectBacking)
  {
    getConfigHelper().setProperty(OBJECT_BACKING, objectBacking);
  } 
  
  public String getPageSize()
  {
    return getProperty(PAGE_SIZE, true);
  }
  
  public void setPageSize(String pageSize)
  {
    getConfigHelper().setProperty(PAGE_SIZE, pageSize);
  } 
  
  public List<String> completeLanguage(String query) 
  {
    List<String> result = new ArrayList();
    String queryLowerCase = query.toLowerCase();
    List<Locale> locales =
      ApplicationBean.getCurrentInstance().getSupportedLocales();        
    for (Locale locale : locales)
    {
      if (locale.toString().contains(queryLowerCase))
      {
        result.add(locale.toString());
      }
    }
    return result;
  }
  
  private CMSConfigHelper getConfigHelper()
  {
    return ((SystemConfigBacking)getBean("systemConfigBacking")).
      getConfigHelper();
  }  
  
}
