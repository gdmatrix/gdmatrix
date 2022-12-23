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
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.cases.Case;
import org.matrix.cases.CaseFilter;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;

/**
 *
 * @author realor
 */
@Named
@ManualScoped
public class CaseFinderBean extends FinderBean
{
  private String smartFilter;
  private CaseFilter filter = new CaseFilter();
  private List<Case> rows;
  private int firstRow;
  private boolean isSmartFind;

  @Inject
  NavigatorBean navigatorBean;

  @Inject
  CaseTypeBean caseTypeBean;

  @Inject
  CaseObjectBean caseObjectBean;

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

  @Override
  public void smartFind()
  {
    isSmartFind = true;
    doFind(true);
  }

  public void smartClear()
  {
    smartFilter = null;
    rows = null;
  }

  @Override
  public void find()
  {
    isSmartFind = false;
    doFind(true);
  }

  public void clear()
  {
    filter = new CaseFilter();
    rows = null;
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ isSmartFind, filter, firstRow, getObjectPosition() };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      isSmartFind = (Boolean)stateArray[0];
      filter = (CaseFilter)stateArray[1];

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
      firstRow = 0;
      String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();

      if (isSmartFind)
      {
        filter = caseTypeBean.queryToFilter(smartFilter, baseTypeId);
        setTabIndex(0);
      }
      else
      {
        smartFilter = caseTypeBean.filterToQuery(filter);
        filter.setCaseTypeId(baseTypeId);
        setTabIndex(1);
      }

      filter.setMaxResults(40);
      rows = CasesModuleBean.getPort(false).findCases(filter);

      if (autoLoad)
      {
        if (rows.size() == 1)
        {
          navigatorBean.view(rows.get(0).getCaseId());
          caseObjectBean.setSearchTabIndex(1);
        }
        else
        {
          caseObjectBean.setSearchTabIndex(0);
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

}
