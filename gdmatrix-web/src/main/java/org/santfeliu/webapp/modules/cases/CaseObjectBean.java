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

import java.util.ArrayList;
import java.util.List;
import javax.inject.Named;
import org.matrix.cases.Case;
import org.matrix.cases.CaseFilter;
import org.santfeliu.cases.web.CaseConfigBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.Tab;

/**
 *
 * @author realor
 */
@Named
public class CaseObjectBean extends ObjectBean
{
  private List<Tab> tabs;
  private String smartFilter;
  private CaseFilter filter = new CaseFilter();
  private List<Case> rows;
  private int firstRow;

  public CaseObjectBean()
  {
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
  public String show()
  {
    return "/pages/cases/case.xhtml";
  }

  public String show(String objectId)
  {
    System.out.println("ObjectId: " + objectId);
    setObjectId(objectId);
    return show();
  }

  @Override
  public List<Tab> getTabs()
  {
    if (tabs == null)
    {
      tabs = new ArrayList<>();
      tabs.add(new Tab("Main", "/pages/cases/case_main.xhtml", "caseMainTabBean"));
      tabs.add(new Tab("Persons", "/pages/cases/case_persons.xhtml", "casePersonsTabBean"));
    }
    return tabs;
  }

  public void smartSearch()
  {
    try
    {
      firstRow = 0;
      CaseFilter basicFilter = new CaseFilter();
      basicFilter.setTitle(smartFilter);
      basicFilter.setMaxResults(40);
      rows = CaseConfigBean.getPort().findCases(basicFilter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void search()
  {
    try
    {
      firstRow = 0;
      filter.setMaxResults(40);
      rows = CaseConfigBean.getPort().findCases(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
}
