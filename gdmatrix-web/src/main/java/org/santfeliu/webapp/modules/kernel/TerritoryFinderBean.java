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
package org.santfeliu.webapp.modules.kernel;

import java.io.Serializable;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.helpers.ResultListHelper;

/**
 *
 * @author blanquepa
 * @param <U> filter
 * @param <V> view.
 */
public abstract class TerritoryFinderBean<U, V extends Serializable> 
  extends FinderBean
{
  protected String smartFilter;
  protected U filter;
  protected boolean isSmartFind;
  
  protected ResultListHelper<V> resultListHelper;

  public String getSmartFilter()
  {
    return smartFilter;
  }

  public void setSmartFilter(String smartFilter)
  {
    this.smartFilter = smartFilter;
  }

  public U getFilter()
  {
    return filter;
  }

  public void setFilter(U filter)
  {
    this.filter = filter;
  }

  public ResultListHelper getResultListHelper()
  {
    return resultListHelper;
  }

  public void setResultListHelper(ResultListHelper resultListHelper)
  {
    this.resultListHelper = resultListHelper;
  }

  public boolean isIsSmartFind()
  {
    return isSmartFind;
  }

  public void setIsSmartFind(boolean isSmartFind)
  {
    this.isSmartFind = isSmartFind;
  }

  @Override
  public void smartFind()
  {
    isSmartFind = true;
    doFind(true);
  }

  public void smartClear()
  {
    smartFilter = null;
    resultListHelper.getRows().clear();
  }

  @Override
  public void find()
  {
    isSmartFind = false;
    doFind(true);
  }
  
  protected abstract void doFind(boolean autoLoad);
    
  @Override
  public Serializable saveState()
  {
    return new Object[]{ isSmartFind, smartFilter, filter, 
      resultListHelper.getFirstRowIndex() };
  }

  @Override
  public void restoreState(Serializable state)
  {
    Object[] stateArray = (Object[])state;
    isSmartFind = (Boolean)stateArray[0];
    smartFilter = (String)stateArray[1];
    filter = (U)stateArray[2];
    resultListHelper.setFirstRowIndex((Integer)stateArray[3]);

    doFind(false);
  }  
}
