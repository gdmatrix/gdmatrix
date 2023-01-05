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
package org.santfeliu.webapp.modules.agenda;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.agenda.Theme;
import org.matrix.agenda.ThemeFilter;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.util.BigList;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;

/**
 *
 * @author lopezrj-sf
 */
@Named
@ManualScoped
public class ThemeFinderBean extends FinderBean
{
  private String smartFilter;
  private ThemeFilter filter = new ThemeFilter();
  private List<Theme> rows;
  private int firstRow;
  private int findMode;
  private boolean outdated;  
  
  @Inject
  NavigatorBean navigatorBean;

  @Inject
  ThemeObjectBean themeObjectBean;

  @Inject
  ThemeTypeBean themeTypeBean;

  @Override
  public ObjectBean getObjectBean()
  {
    return themeObjectBean;
  }

  public String getSmartFilter()
  {
    return smartFilter;
  }

  public void setSmartFilter(String smartFilter)
  {
    this.smartFilter = smartFilter;
  }

  public ThemeFilter getFilter()
  {
    return filter;
  }

  public void setFilter(ThemeFilter filter)
  {
    this.filter = filter;
  }

  public List<Theme> getRows()
  {
    return rows;
  }

  public void setRows(List<Theme> rows)
  {
    this.rows = rows;
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }
  
  @Override
  public String getObjectId(int position)
  {
    return rows == null ? NEW_OBJECT_ID : rows.get(position).getThemeId();
  }

  @Override
  public int getObjectCount()
  {
    return rows == null ? 0 : rows.size();
  }
  
  @Override
  public void smartFind()
  {
    findMode = 1;
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();    
    filter = themeTypeBean.queryToFilter(smartFilter, baseTypeId);            
    doFind(true);
    firstRow = 0;    
  }

  @Override
  public void find()
  {
    findMode = 2;
    smartFilter = themeTypeBean.filterToQuery(filter);    
    doFind(true);
    firstRow = 0;    
  }

  public void outdate()
  {
    this.outdated = true;
  }  
  
  public void update()
  {
    if (outdated)
    {
      doFind(false);
    }
  }  
  
  public void clear()
  {
    filter = new ThemeFilter();
    smartFilter = null;    
    rows = null;
    findMode = 0;    
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ findMode, filter, firstRow, getObjectPosition() };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      findMode = (Integer)stateArray[0];
      filter = (ThemeFilter)stateArray[1];

      doFind(false);

      firstRow = (Integer)stateArray[2];
      setObjectPosition((Integer)stateArray[3]);      
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private void doFind(boolean autoLoad)
  {
    try
    {
      if (findMode == 0)
      {
        rows = Collections.EMPTY_LIST;
      }
      else
      {
        if (findMode == 1)
        {
          setTabIndex(0);        
        }
        else
        {
          setTabIndex(1);
        }
        rows = new BigList(20, 10)
        {
          @Override
          public int getElementCount()
          {
            try
            {
              return AgendaModuleBean.getClient(false).
                countThemesFromCache(filter);
            }
            catch (Exception ex)
            {
              error(ex);
              return 0;
            }
          }

          @Override
          public List getElements(int firstResult, int maxResults)
          {
            try
            {
              filter.setFirstResult(firstResult);
              filter.setMaxResults(maxResults);
              return AgendaModuleBean.getClient(false).
                findThemesFromCache(filter);
            }
            catch (Exception ex)
            {
              error(ex);
              return null;
            }
          }
        };
        
        outdated = false;        
        
        if (autoLoad)
        {
          if (rows.size() == 1)
          {
            navigatorBean.view(rows.get(0).getThemeId());
            themeObjectBean.setSearchTabIndex(themeObjectBean.
              getEditionTabIndex());
          }
          else
          {
            themeObjectBean.setSearchTabIndex(0);
          }
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

}
