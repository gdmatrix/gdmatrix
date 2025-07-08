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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.cases.Case;
import org.matrix.cases.CaseFilter;
import org.matrix.dic.PropertyDefinition;
import org.santfeliu.classif.ClassCache;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.util.BigList;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.exporters.CSVDataTableRowsExporter;
import org.santfeliu.webapp.helpers.TablePropertyHelper;
import org.santfeliu.webapp.setup.TableProperty;
import org.santfeliu.webapp.util.DataTableRow;
import org.santfeliu.webapp.util.DateTimeRowStyleClassGenerator;
import org.santfeliu.webapp.util.RowStyleClassGenerator;
import org.santfeliu.webapp.DataTableRowExportable;
import org.santfeliu.webapp.helpers.RowsExportHelper;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class CaseFinderBean extends FinderBean implements DataTableRowExportable
{  
  //Dictionary properties
  private static final String PERSON_SEARCH_ENABLED = "_personSearchEnabled";
  
  private String smartFilter;
  private CaseFilter filter = new CaseFilter();
  private List<DataTableRow> rows;
  private int firstRow;
  private boolean outdated;
  private String formSelector;
  private String sortBy;
  
  @Inject
  NavigatorBean navigatorBean;

  @Inject
  CaseTypeBean caseTypeBean;

  @Inject
  CaseObjectBean caseObjectBean;

  @PostConstruct
  public void init()
  {
    CSVDataTableRowsExporter.register();
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
    
  @Override
  public CaseFilter getFilter()
  {
    if (filter != null && StringUtils.isBlank(filter.getCaseTypeId()))
    {
      String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
      filter.setCaseTypeId(baseTypeId);
    }
    
    return filter != null ? (CaseFilter) getSessionProperties(filter) : filter;
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

  @Override
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

  public String getSortBy() 
  {
    return sortBy;
  }

  public void setSortBy(String sortBy) 
  {
    this.sortBy = sortBy;
  }

  @Override
  public List<TableProperty> getTableProperties()
  {
    try
    {
      List<TableProperty> tableProperties =
        getObjectSetup().getSearchTabs().get(0).getTableProperties();

      return tableProperties != null ?
        tableProperties : Collections.emptyList();
    }
    catch (Exception ex)
    {
      return Collections.emptyList();
    }
  }

  @Override
  public List<TableProperty> getColumns()
  {
    return TablePropertyHelper.getColumnTableProperties(getTableProperties());
  }

  @Override
  public List<? extends DataTableRow> getExportableRows()
  {
    if (rows.size() <= getPageSize())
    {
      return rows;
    }
    else
    {
      return ((BigList)rows).getElements(0, Integer.MAX_VALUE);
    }
  }
  
  @Override
  public int getRowExportLimit()
  {
    return RowsExportHelper.getActiveSearchTabRowExportLimit(caseObjectBean);
  }
  
  @Override
  public boolean isExportable()
  {
    return RowsExportHelper.isActiveSearchTabExportable(caseObjectBean);
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }

  public void sortByColumn(String columnName)
  {
    if (sortBy == null) //first sort
    {
      sortBy = columnName + ":asc";  
    }
    else
    {
      String currentColumnName = sortBy.split(":")[0];
      if (currentColumnName.equals(columnName)) //direction switch
      {
        if (sortBy.endsWith(":desc"))
        {
          sortBy = columnName + ":asc";
        }
        else
        {
          sortBy = columnName + ":desc";
        }
      }
      else
      {
        sortBy = columnName + ":asc";
      }
    }
    find();
  }
  
  public String getSortIcon(String columnName)
  {
    if (!getOrderByColumns().contains(columnName) || !isFinding())
    {
      return null; //no sorting enabled
    }
    else if (sortBy == null) //sorting enabled, but no sort column selected
    {
      return "pi pi-sort-alt";
    }    
    else //sorting enabled, and sort column selected
    {
      String currentColumnName = sortBy.split(":")[0];
      if (currentColumnName.equals(columnName)) //sorted by this column
      {
        if (sortBy.endsWith(":desc")) //desc
        {
          return "pi pi-sort-amount-down";
        }
        else //asc
        {
          return "pi pi-sort-amount-up";
        }
      }
      else //not sorted by this column
      {
        return "pi pi-sort-alt";
      }
    }
  }
  
  @Override
  public void smartFind()
  {
    setFinding(true);
    setFilterTabSelector(0);
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
    filter = caseTypeBean.queryToFilter(smartFilter, baseTypeId);
    clearSessionProperties();     
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
    setSessionProperties(filter);
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

  @Override
  public void clear()
  {
    super.clear();
    filter = new CaseFilter();
    smartFilter = null;
    rows = null;
    setFinding(false);
    formSelector = null;
  }

  public boolean isRenderPersonId()
  {
    try
    {
      String typeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
      Type type = TypeCache.getInstance().getType(typeId);
      PropertyDefinition pd = type.getPropertyDefinition(PERSON_SEARCH_ENABLED);
      if (pd != null && !pd.getValue().isEmpty() &&
        pd.getValue().get(0).equalsIgnoreCase("true"))
      {
        return isRender("personId");
      }
    }
    catch (Exception ex)
    {
    }
    return false; //default value
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ isFinding(), getFilterTabSelector(), filter, firstRow,
      getObjectPosition(), formSelector, rows, outdated, getPageSize(), 
      sortBy };
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
      setPageSize((Integer)stateArray[8]);
      sortBy = (String)stateArray[9];
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
        rows = new BigList(2 * getPageSize() + 1, getPageSize())
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

              String searchExpression = filter.getSearchExpression();
              if (StringUtils.isBlank(searchExpression))
                setOrderBy(filter);

              List<TableProperty> tableProperties = getTableProperties();
              for (TableProperty tableProperty : tableProperties)
              {
                filter.getOutputProperty().add(tableProperty.getName());
              }
              List<Case> cases =
                CasesModuleBean.getPort(false).findCases(filter);
              filter.setTitle(title);
              filter.setSearchExpression(searchExpression);

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
      dataTableRow.setValues(this, row, getTableProperties());
      dataTableRow.setStyleClass(getRowStyleClass(row));
      convertedRows.add(dataTableRow);
    }

    return convertedRows;
  }

  private List<String> getOrderByColumns()
  {
    try
    {
      int tabSelector = caseObjectBean.getSearchTabSelector();
      tabSelector =
        tabSelector < getObjectSetup().getSearchTabs().size() ? tabSelector : 0;
      List<String> orderByColumns = 
        getObjectSetup().getSearchTabs().get(tabSelector).getOrderByColumns();
      if (orderByColumns == null || orderByColumns.isEmpty())
      {
        //default value
        orderByColumns = Arrays.asList("caseId", "title", "caseTypeId", 
          "startDate", "endDate");
      }
      if (!isFinding())
      {
        sortBy = null;
      }
      return new ArrayList(orderByColumns);      
    }
    catch (Exception ex)
    {
      return Collections.EMPTY_LIST;
    }
  }  
  
  private String setWildcards(String text)
  {
    if (text != null)
      text = text.trim();
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
    List<String> orderBy;
    if (sortBy == null)
    {
      int tabSelector = caseObjectBean.getSearchTabSelector();
      tabSelector =
        tabSelector < getObjectSetup().getSearchTabs().size() ? tabSelector : 0;
      orderBy = getObjectSetup().getSearchTabs().get(tabSelector).getOrderBy();
    }
    else
    {
      orderBy = Arrays.asList(sortBy);
    }

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
