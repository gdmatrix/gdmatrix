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
package org.santfeliu.webapp.helpers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.util.BigList;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.bean.CMSProperty;
import org.santfeliu.web.obj.util.ParametersManager;
import org.santfeliu.web.obj.util.RequestParameters;

/**
 *
 * @author lopezrj-sf
 */
public abstract class BasicSearchHelper
{
  @CMSProperty
  public static final String SEARCH_TITLE_PROPERTY = "oc.searchTitle";
  @CMSProperty
  public static final String PAGE_SIZE_PROPERTY = "pageSize";

  private static final int PAGE_SIZE = 10;
  private static final int CACHE_SIZE = 15;

  private int firstRowIndex;
  private BigList rows; // when rows == null, result table is not shown

  public BigList getRows()
  {
    return rows;
  }

  // force data refresh, page is unchanged
  public String refresh()
  {
    initRows();
    return null;
  }

  // force data refresh, move to first page
  public String search()
  {
    firstRowIndex = 0; // reset index
    initRows(); // force rows population
    return null;
  }

  // new search, clear data, result table is not shown
  public String reset()
  {
    rows = null;
    firstRowIndex = 0;
    return null;
  }

  public abstract int countResults();

  public abstract List getResults(int firstResult, int maxResults);

  public String getTitle(MenuItemCursor cursor)
  {
    String title = cursor.getProperty(SEARCH_TITLE_PROPERTY);
    if (title == null)
    {
      title = cursor.getLabel();
    }
    return title;
  }

  public int getFirstRowIndex()
  {
    int size = getRowCount();
    if (size == 0)
    {
      firstRowIndex = 0;
    }
    else if (firstRowIndex >= size)
    {
      int pageSize = getPageSize();
      firstRowIndex = pageSize * ((size - 1) / pageSize);
    }
    return firstRowIndex;
  }

  public void setFirstRowIndex(int firstRowIndex)
  {
    this.firstRowIndex = firstRowIndex;
  }

  public int getRowCount()
  {
    return rows == null ? 0 : rows.size();
  }

  public int getCacheSize()
  {
    if (getPageSize() != PAGE_SIZE)
      return getPageSize() + 5;
    else
      return CACHE_SIZE;
  }

  public int getPageSize()
  {
    String pageSize = getSelectedMenuItem().getProperty(PAGE_SIZE_PROPERTY);
    if (pageSize != null)
      return Integer.valueOf(pageSize);
    else
      return PAGE_SIZE;
  }

  public String executeParametersManagers(ParametersManager[] managers)
  {
    String outcome = null;
    if (managers != null)
    {
      RequestParameters reqParameters = getRequestParameters();
      for (ParametersManager manager : Arrays.asList(managers))
      {
        outcome = manager.execute(reqParameters);
        if (outcome != null)
          return outcome;
      }
    }
    return outcome;
  }

  // force new seach
  private void initRows()
  {
    if (rows != null && (getPageSize() == rows.getBlockSize()))
    {
      // clear rows cache, reuse rows
      rows.clearCache();
    }
    else
    {
      // create new BigList
      rows = new BigList(getCacheSize(), getPageSize())
      {
        @Override
        public int getElementCount()
        {
          return countResults();
        }

        @Override
        public List getElements(int firstResult, int maxResults)
        {
          return getResults(firstResult, maxResults);
        }
      };
    }
  }

  private RequestParameters getRequestParameters()
  {
    RequestParameters parameters = new RequestParameters();

    Map requestMap = getExternalContext().getRequestParameterMap();
    HttpServletRequest request =
      (HttpServletRequest)getExternalContext().getRequest();
    String qs = request.getQueryString();

    for (Object key : requestMap.keySet())
    {
      String skey = String.valueOf(key);
      String svalue = String.valueOf(requestMap.get(key));
      parameters.add(skey, svalue, qs);
    }

    return parameters;
  }

  private ExternalContext getExternalContext()
  {
    return FacesContext.getCurrentInstance().getExternalContext();
  }

  private MenuItemCursor getSelectedMenuItem()
  {
    return UserSessionBean.getCurrentInstance().getMenuModel().
      getSelectedMenuItem();
  }

}
