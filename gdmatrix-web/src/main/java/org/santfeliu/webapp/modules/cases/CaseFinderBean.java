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
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.cases.Case;
import org.matrix.cases.CaseFilter;
import org.santfeliu.classif.ClassCache;
import org.santfeliu.util.BigList;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.setup.Column;
import org.santfeliu.webapp.util.DataTableRow;
import org.santfeliu.webapp.util.DateTimeRowStyleClassGenerator;
import org.santfeliu.webapp.util.RowStyleClassGenerator;

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
  private boolean outdated;
  private String formSelector; 
 
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
  
  public String getClassId()
  {
    if (filter.getClassId().isEmpty()) return null;
    return filter.getClassId().get(0);
  }

  public void setClassId(String classId)
  {
    filter.getClassId().clear();

    if (!StringUtils.isBlank(classId))
    {
      filter.getClassId().add(classId);
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

  public String getFormSelector()
  {
    return formSelector;
  }

  public void setFormSelector(String formSelector)
  {
    this.formSelector = formSelector;
  }
  
  public List<Column> getColumns()
  {
    try
    {
      if (objectSetup == null)
        loadObjectSetup();
      
      List<Column> columns = objectSetup.getSearchTabs().get(0).getColumns();
      
      return columns != null ? columns : Collections.emptyList();
    }
    catch (Exception ex)
    {
      return Collections.emptyList();
    }
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
    setFinding(true);
    setFilterTabSelector(0);
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
    filter = caseTypeBean.queryToFilter(smartFilter, baseTypeId);
    doFind(true);
    resetWildcards(filter);
    firstRow = 0;
  }

  @Override
  public void find()
  {
    setFinding(true);
    setFilterTabSelector(1);
    if (StringUtils.isBlank(filter.getCaseTypeId()))
    {
      String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
      filter.setCaseTypeId(baseTypeId);
    }
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
    setFinding(false);
    formSelector = null;
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ isFinding(), getFilterTabSelector(), filter, firstRow, 
      getObjectPosition(), formSelector, rows, outdated };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      setFinding((Boolean)stateArray[0]);
      setFilterTabSelector((Integer)stateArray[1]);
      filter = (CaseFilter)stateArray[2];
      smartFilter = caseTypeBean.filterToQuery(filter);
      firstRow = (Integer)stateArray[3];
      setObjectPosition((Integer)stateArray[4]);      
      formSelector = (String)stateArray[5];
      rows = (List<DataTableRow>)stateArray[6];
      outdated = (Boolean)stateArray[7];
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
      if (!isFinding())
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
              String title = filter.getTitle();
              filter.setTitle(setWildcards(title));
              String classId = CaseFinderBean.this.getClassId();
              if (classId != null)
              {
                List<String> classIds =
                  ClassCache.getInstance().getTerminalClassIds(classId);
                filter.getClassId().clear();
                filter.getClassId().addAll(classIds);                
              }
              int count = CasesModuleBean.getPort(false).countCases(filter);
              filter.setTitle(title);
              CaseFinderBean.this.setClassId(classId);
              return count;              
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
              String title = filter.getTitle();
              filter.setTitle(setWildcards(title));
              String classId = CaseFinderBean.this.getClassId();
              if (classId != null)
              {
                List<String> classIds =
                  ClassCache.getInstance().getTerminalClassIds(classId);
                filter.getClassId().clear();
                filter.getClassId().addAll(classIds);                
              }   

              setOrderBy(filter);
              
              List<Column> columns = getColumns();
              for (Column column : columns)
              {
                filter.getOutputProperty().add(column.getName());
              }
              List<Case> cases = 
                CasesModuleBean.getPort(false).findCases(filter);
              filter.setTitle(title);
              CaseFinderBean.this.setClassId(classId);
              return toDataTableRows(cases);     
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
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
    
  private List<DataTableRow> toDataTableRows(List<Case> cases) 
    throws Exception
  {
    List<DataTableRow> convertedRows = new ArrayList();
    for (Case row : cases)
    {
      DataTableRow dataTableRow = 
        new DataTableRow(row.getCaseId(), row.getCaseTypeId());
      dataTableRow.setValues(this, row, getColumns());
      dataTableRow.setStyleClass(getRowStyleClass(row));
      convertedRows.add(dataTableRow);
    }
    
    return convertedRows;       
  }
    
  private String setWildcards(String text)
  {
    if (text != null && !text.startsWith("\"") && !text.endsWith("\""))
      text = "%" + text.replaceAll("^%|%$", "") + "%" ;
    else if (text != null && text.startsWith("\"") && text.endsWith("\""))
      text = text.replaceAll("^\"|\"$", "");
    return text;
  } 
  
  private void resetWildcards(CaseFilter filter)
  {
    String title = filter.getTitle();
    if (title != null && !title.startsWith("\"") && !title.endsWith("\""))
      title = title.replaceAll("^%+|%+$", "");
    filter.setTitle(title);
  }
  
  private void setOrderBy(CaseFilter filter) throws Exception
  {
    if (objectSetup == null)
      loadObjectSetup();
                  
    int tabSelector = caseObjectBean.getSearchTabSelector();
    tabSelector = 
      tabSelector < objectSetup.getSearchTabs().size() ? tabSelector : 0;    
    List<String> orderBy = 
      objectSetup.getSearchTabs().get(tabSelector).getOrderBy(); 
    
    if (orderBy != null && !orderBy.isEmpty())
    {
      StringBuilder buffer = new StringBuilder(" ORDER BY ");
      boolean firstColumn = true;
      for (String column : orderBy)
      {
        if (!firstColumn)
          buffer.append(", ");
        else
          firstColumn = false;

        String[] parts = column.split(":");
        buffer.append(parts[0]).append(" ");
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1]))
          buffer.append(" desc ");
      }

      filter.setSearchExpression(buffer.toString());                    
    }    
  }
  
  private RowStyleClassGenerator getRowStyleClassGenerator()
  {
    return new DateTimeRowStyleClassGenerator("startDate,startTime", 
      "endDate,endTime", null);
  }
  
  private String getRowStyleClass(Object row)
  {
    RowStyleClassGenerator styleClassGenerator = 
      getRowStyleClassGenerator();
    return styleClassGenerator.getStyleClass(row);    
  }  
  
}
