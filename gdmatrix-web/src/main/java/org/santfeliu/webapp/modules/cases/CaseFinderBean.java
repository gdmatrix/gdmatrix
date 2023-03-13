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
import org.matrix.cases.Case;
import org.matrix.cases.CaseFilter;
import org.matrix.dic.PropertyDefinition;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.util.BigList;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.setup.Column;
import org.santfeliu.webapp.setup.ObjectSetup;
import org.santfeliu.webapp.setup.ObjectSetupCache;
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
  private transient ObjectSetup objectSetup;
 
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
    try
    {
      if (objectSetup == null)
        loadObjectSetup();
      List<Column> columns = objectSetup.getSearchTabs().get(0).getColumns();
      if (columns == null || columns.isEmpty())
      {
        //Get default objectSetup columns congfiguration        
        columns = 
          caseTypeBean.getObjectSetup().getSearchTabs().get(0).getColumns();
      }
      return columns;
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
    return new Object[]{ finding, getFilterTabSelector(), filter, firstRow, 
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
      filter = (CaseFilter)stateArray[2];
      smartFilter = caseTypeBean.filterToQuery(filter);

      doFind(false);

      firstRow = (Integer)stateArray[3];
      setObjectPosition((Integer)stateArray[4]);
      
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
//        loadObjectSetup();
        
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
              List<Column> columns = getColumns();
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
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public void loadObjectSetup() throws Exception
  {
    String setupName = getProperty("objectSetup");
    if (setupName == null)
    {
      String typeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
      Type type = TypeCache.getInstance().getType(typeId);
      PropertyDefinition propdef = type.getPropertyDefinition("objectSetup");
      if (propdef != null && !propdef.getValue().isEmpty())
      {
        setupName = propdef.getValue().get(0);
      }
    }

    if (setupName != null)
    {
      objectSetup = ObjectSetupCache.getConfig(setupName);
    }
    else
    {
      objectSetup = caseTypeBean.getObjectSetup();
    }
  }  
    
  private List<DataTableRow> toObjectArrayList(List<Case> cases) 
    throws Exception
  {
    List<DataTableRow> convertedRows = new ArrayList();
    for (Case row : cases)
    {
      DataTableRow dataTableRow = 
        new DataTableRow(row.getCaseId(), row.getCaseTypeId());
      dataTableRow.setValues(row, getColumns());
      convertedRows.add(dataTableRow);
    }
    return convertedRows;       
  }
  
}
