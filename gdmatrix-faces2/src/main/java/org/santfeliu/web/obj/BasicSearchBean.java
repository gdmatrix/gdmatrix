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
package org.santfeliu.web.obj;

import java.util.List;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.util.BigList;
import org.santfeliu.web.bean.CMSProperty;
import org.santfeliu.web.obj.util.SetObjectManager;
import org.santfeliu.web.obj.util.JumpManager;
import org.santfeliu.web.obj.util.RequestParameters;

/**
 *
 * @author realor
 */
public abstract class BasicSearchBean extends PageBean
{
  @CMSProperty
  public static final String SEARCH_TITLE_PROPERTY = "oc.searchTitle";
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

  @Override
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
  
  protected String executeParametersManagers(
    JumpManager jumpManager,
    SetObjectManager setObjectManager)
  {
    RequestParameters reqParameters = getRequestParameters();
    String outcome = jumpManager.execute(reqParameters); 
    if (outcome != null)
      return outcome;

    outcome = setObjectManager.execute(reqParameters);
    if (outcome != null)
      return outcome;

    return outcome;
  } 

  public boolean checkJumpSuitability(String objectId)
  {
    return true;
  }
  
  public String getNotSuitableMessage()
  {
    return "INVALID_OBJECT";
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
}
