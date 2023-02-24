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
package org.santfeliu.webapp.modules.cases;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.cases.Case;
import org.matrix.cases.CaseFilter;
import org.santfeliu.util.BigList;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.helpers.ColumnsHelper;
import org.santfeliu.webapp.setup.SearchTab;

/**
 *
 * @author realor
 */
@Named
@ViewScoped
public class CaseFinderBean extends FinderBean
{
  private String smartFilter;
  private CaseFilter filter = new CaseFilter();
  private List<Case> rows;
  private int firstRow;
  private boolean finding;
  private boolean outdated;
  private ColumnsHelper columnsHelper;
 
  @Inject
  NavigatorBean navigatorBean;

  @Inject
  CaseTypeBean caseTypeBean;

  @Inject
  CaseObjectBean caseObjectBean;
  
  @PostConstruct
  public void init()
  {
    columnsHelper = new ColumnsHelper<Case>(caseTypeBean) 
    {
      @Override
      public List<Case> getRows()
      {
        return rows != null ? rows : Collections.emptyList();
      }
      
      @Override
      public Case getRowData(Case row)
      {
        try
        {
          return CasesModuleBean.getPort(false).loadCase(row.getCaseId());
        }
        catch (Exception ex)
        {
          return null;
        }
      }
    };
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return caseObjectBean;
  }

  public String getSmartFilter()
  {
    return smartFilter;
  }

  public void setSmartFilter(String smartFilter)
  {
    this.smartFilter = smartFilter;
  }

  public CaseFilter getFilter()
  {
    return filter;
  }

  public void setFilter(CaseFilter filter)
  {
    this.filter = filter;
  }

  @Override
  public String getObjectId(int position)
  {
    return rows == null ? NEW_OBJECT_ID : rows.get(position).getCaseId();
  }

  @Override
  public int getObjectCount()
  {
    return rows == null ? 0 : rows.size();
  }

  public List<String> getCaseIdList()
  {
    return filter.getCaseId();
  }

  public void setCaseIdList(List<String> caseIdList)
  {
    filter.getCaseId().clear();
    if (caseIdList != null)
    {
      filter.getCaseId().addAll(caseIdList);
    }
  }

  public List<Case> getRows()
  {
    return rows;
  }

  public void setRows(List<Case> rows)
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

  public ColumnsHelper getColumnsHelper()
  {
    return columnsHelper;
  }

  public void setColumnsHelper(ColumnsHelper columnsHelper)
  {
    this.columnsHelper = columnsHelper;
  }

  @Override
  public void smartFind()
  {
    finding = true;
    setFilterTabSelector(0);
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
    filter = caseTypeBean.queryToFilter(smartFilter, baseTypeId);
    doFind(true);
    firstRow = 0;
  }

  @Override
  public void find()
  {
    finding = true;
    setFilterTabSelector(1);
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
    filter.setCaseTypeId(baseTypeId);
    smartFilter = caseTypeBean.filterToQuery(filter);
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
    filter = new CaseFilter();
    smartFilter = null;
    rows = null;
    finding = false;
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ finding, getFilterTabSelector(), columnsHelper, filter, firstRow,
      getObjectPosition() };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      finding = (Boolean)stateArray[0];
      setFilterTabSelector((Integer)stateArray[1]);
      columnsHelper = (ColumnsHelper)stateArray[2];
      filter = (CaseFilter)stateArray[3];
      smartFilter = caseTypeBean.filterToQuery(filter);

      doFind(false);

      firstRow = (Integer)stateArray[4];
      setObjectPosition((Integer)stateArray[5]);
      
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
      if (!finding)
      {
        rows = Collections.EMPTY_LIST;
      }
      else
      {        
        rows = new BigList(20, 10)
        {
          @Override
          public int getElementCount()
          {
            try
            {
              return CasesModuleBean.getPort(false).countCases(filter);
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
              return CasesModuleBean.getPort(false).findCases(filter);
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
            navigatorBean.view(rows.get(0).getCaseId());
            caseObjectBean.setSearchTabSelector(
              caseObjectBean.getEditModeSelector());
          }
          else
          {
            caseObjectBean.setSearchTabSelector(0);
          }
        }
        
        SearchTab searchTab = caseObjectBean.getActiveSearchTab();
        if (searchTab != null)
        {
          columnsHelper.setColumns(searchTab.getColumns());
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

}
