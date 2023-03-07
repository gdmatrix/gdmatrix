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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
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
import org.santfeliu.webapp.setup.Column;
import org.santfeliu.webapp.setup.SearchTab;
import org.santfeliu.webapp.util.DataTableRow;

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
  private List<DataTableRow> rows;
  private int firstRow;
  private boolean finding;
  private boolean outdated;
  private List<Column> columns;
 
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
    return rows == null ? NEW_OBJECT_ID : rows.get(position).getRowId();
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

  public List getRows()
  {
    return rows;
  }

  public void setRows(List rows)
  {
    this.rows = rows;
  }

  public List<Column> getColumns()
  {    
    return columns;
  }

  public void setColumns(List<Column> columns)
  {
    this.columns = columns;
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
    return new Object[]{ finding, getFilterTabSelector(), 
      columns, filter, firstRow, getObjectPosition() };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      finding = (Boolean)stateArray[0];
      setFilterTabSelector((Integer)stateArray[1]);
      columns = ((List<Column>)stateArray[2]);
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
              for (Column column : columns)
              {
                filter.getOutputProperty().add(column.getName());
              }
              List<Case> cases = 
                CasesModuleBean.getPort(false).findCases(filter);
              
              return toObjectArrayList(cases);     
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
            navigatorBean.view(rows.get(0).getRowId());
            caseObjectBean.setSearchTabSelector(
              caseObjectBean.getEditModeSelector());
          }
          else
          {
            caseObjectBean.setSearchTabSelector(0);
          }
        }
        
        List<SearchTab> searchTabs = caseObjectBean.getSearchTabs();
        if (!searchTabs.isEmpty())
        {
          SearchTab searchTab = searchTabs.get(0);
          columns = searchTab != null ? searchTab.getColumns() : 
            getDefaultColumns();  
        }
        
        if (columns == null)
          columns = getDefaultColumns();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  

  private List<Column> getDefaultColumns()
  {
    List<Column> defaultColumns = new ArrayList();
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.cases.web.resources.CaseBundle", getLocale());

    Column col1 = new Column();
    col1.setName("caseId");
    col1.setLabel(bundle.getString("case_caseId"));
    defaultColumns.add(col1);

    Column col2 = new Column();
    col2.setName("caseTypeId");
    col2.setLabel(bundle.getString("case_type"));
    defaultColumns.add(col2);

    Column col3 = new Column();
    col3.setName("title");
    col3.setLabel(bundle.getString("case_title"));
    defaultColumns.add(col3);

    return defaultColumns;         
  }  
  
  private List<DataTableRow> toObjectArrayList(List<Case> cases) 
    throws Exception
  {
    List<DataTableRow> convertedRows = new ArrayList();
    for (Case row : cases)
    {
      DataTableRow dataTableRow = 
        new DataTableRow(row.getCaseId(), row.getCaseTypeId());
      dataTableRow.setValues(row, columns);
      convertedRows.add(dataTableRow);
    }
    return convertedRows;       
  }
  
}
