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
package org.matrix.pf.web.helper;

import java.io.Serializable;
import java.util.List;
import org.santfeliu.util.BigList;

/**
 *
 * @author blanquepa
 */
public class BigListHelper extends ResultListHelper implements Serializable
{
  public static final int CACHE_SIZE = 15;  

  private BigList rows;
  
  public BigListHelper(BigListPage searchPage)
  {
    super(searchPage);
  }
  
  @Override
  public BigList getRows()
  {
    return rows;
  }
  
  public void refresh()
  {
    initRows(); 
  }  
  
  @Override
  public void search()
  {
    super.search();
  }  
  
//  public void reset()
//  {
//    rows = null;
//    firstRowIndex = 0;
//  }
  
  public int getCacheSize()
  {
    if (getPageSize() != PAGE_SIZE)
      return getPageSize() + 5;
    else
      return CACHE_SIZE;
  } 
  
//  @Override
//  public int getFirstRowIndex()
//  {
//    int size = getRowCount();
//    if (size == 0)
//    {
//      firstRowIndex = 0;
//    }
//    else if (firstRowIndex >= size)
//    {
//      int pageSize = getPageSize();
//      firstRowIndex = pageSize * ((size - 1) / pageSize);
//    }
//    return firstRowIndex;
//  }

//  public void setFirstRowIndex(int firstRowIndex)
//  {
//    this.firstRowIndex = firstRowIndex;
//  }

  @Override
  public int getRowCount()
  {
    return rows == null ? 0 : rows.size();
  }  

  @Override
  protected void initRows()
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
          return ((BigListPage)pageBacking).countResults();
        }

        @Override
        public List getElements(int firstResult, int maxResults)
        {
          return pageBacking.getResults(firstResult, maxResults);
        }
      };
    }
  } 
}
