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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;
import org.matrix.dic.DictionaryConstants;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.helpers.GroupableRowsHelper;
import org.santfeliu.webapp.helpers.RowsFilterHelper;
import org.santfeliu.webapp.modules.cases.CaseObjectBean;
import org.santfeliu.webapp.modules.cases.CasesModuleBean;
import org.santfeliu.webapp.modules.dic.TypeTypeBean;
import org.santfeliu.webapp.setup.TableProperty;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.util.DateTimeRowStyleClassGenerator;
import org.santfeliu.webapp.util.RowStyleClassGenerator;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author blanquepa
 */
@Named
@RequestScoped
public class PersonCasesTabBean extends TabBean
{
  private final Map<String, TabInstance> tabInstances = new HashMap();
  private final TabInstance EMPTY_TAB_INSTANCE = new TabInstance();
  private GroupableRowsHelper groupableRowsHelper;

  public class TabInstance
  {
    String objectId = NEW_OBJECT_ID;
    List<CasePersonView> rows;
    int firstRow = 0;
    RowsFilterHelper rowsFilterHelper = RowsFilterHelper.create(null, prev -> 
      new RowsFilterHelper<CasePersonView>(prev)
    {
      @Override
      public ObjectBean getObjectBean() 
      {
        return PersonCasesTabBean.this.getObjectBean();
      }
      
      @Override
      public List<CasePersonView> getRows()
      {
        return rows;
      }

      @Override
      public boolean isGroupedViewEnabled()
      {
        return PersonCasesTabBean.this.getGroupableRowsHelper().
          isGroupedViewEnabled();
      }

      @Override
      public void resetFirstRow()
      {
        firstRow = 0;
      }

      @Override
      public List<TableProperty> getColumns() 
      {
        return Collections.EMPTY_LIST;
      }

      @Override
      public Item getFixedColumnValue(CasePersonView row, 
        String columnName) 
      {
        if ("caseId".equals(columnName))
        {
          return new Item(row.getCaseObject().getCaseId());
        }
        else if ("caseTitle".equals(columnName))
        {
          return new Item(row.getCaseObject().getTitle());
        }
        else if ("caseTypeId".equals(columnName))
        {
          String typeId = row.getCaseObject().getCaseTypeId();
          Item item = RowsFilterHelper.createTypeItem(typeId);
          return (item != null ? item : RowsFilterHelper.createEmptyItem());
        }
        else if ("comments".equals(columnName))
        {
          return new Item(row.getComments());
        }
        else
        {
          return null;
        }
      }

      @Override
      public String getRowTypeId(CasePersonView row)
      {
        return row.getCasePersonTypeId();
      }
    });
    
    RowsFilterHelper rowsFilterHelper2 = 
      RowsFilterHelper.create(rowsFilterHelper, prev -> 
        new RowsFilterHelper<CasePersonView>(prev)
    {
      @Override
      public ObjectBean getObjectBean() 
      {
        return PersonCasesTabBean.this.getObjectBean();
      }
      
      @Override
      public List<CasePersonView> getRows()
      {
        return prev.getFilteredRows();
      }

      @Override
      public boolean isGroupedViewEnabled()
      {
        return PersonCasesTabBean.this.getGroupableRowsHelper().
          isGroupedViewEnabled();
      }

      @Override
      public void resetFirstRow()
      {
        firstRow = 0;
      }

      @Override
      public List<TableProperty> getColumns() 
      {
        return Collections.EMPTY_LIST;
      }

      @Override
      public Item getFixedColumnValue(CasePersonView row, 
        String columnName) 
      {
        if ("caseId".equals(columnName))
        {
          return new Item(row.getCaseObject().getCaseId());
        }
        else if ("caseTitle".equals(columnName))
        {
          return new Item(row.getCaseObject().getTitle());
        }
        else if ("caseTypeId".equals(columnName))
        {
          String typeId = row.getCaseObject().getCaseTypeId();
          Item item = RowsFilterHelper.createTypeItem(typeId);
          return (item != null ? item : RowsFilterHelper.createEmptyItem());
        }
        else if ("comments".equals(columnName))
        {
          return new Item(row.getComments());
        }
        else
        {
          return null;
        }
      }

      @Override
      public String getRowTypeId(CasePersonView row)
      {
        return row.getCasePersonTypeId();
      }
    });    

    public RowsFilterHelper getRowsFilterHelper()
    {
      return rowsFilterHelper;
    }
    
    public RowsFilterHelper getRowsFilterHelper2()
    {
      return rowsFilterHelper2;
    }
    
    public RowsFilterHelper getActiveRowsFilterHelper()
    {
      if (rowsFilterHelper2.isRendered())
      {
        return rowsFilterHelper2;
      }
      else
      {
        return rowsFilterHelper;
      }
    }    
  }

  public Map<String, TabInstance> getTabInstances()
  {
    return tabInstances;
  }

  @Inject
  private PersonObjectBean personObjectBean;

  @Inject
  private CaseObjectBean caseObjectBean;

  @Inject
  TypeTypeBean typeTypeBean;

  @PostConstruct
  public void init()
  {
    groupableRowsHelper = new GroupableRowsHelper()
    {
      @Override
      public ObjectBean getObjectBean()
      {
        return PersonCasesTabBean.this.getObjectBean();
      }

      @Override
      public List<TableProperty> getColumns()
      {
        return Collections.EMPTY_LIST;
      }

      @Override
      public void sortRows()
      {
      }

      @Override
      public String getRowTypeColumnName()
      {
        return null;
      }

      @Override
      public String getFixedColumnValue(Object row, String columnName)
      {
        CasePersonView casePersonView = (CasePersonView)row;
        if ("caseId".equals(columnName))
        {
          return casePersonView.getCaseObject().getCaseId();
        }
        else if ("caseTitle".equals(columnName))
        {
          return casePersonView.getCaseObject().getTitle();
        }
        else if ("caseTypeId".equals(columnName))
        {
          return typeTypeBean.getTypeDescription(
            casePersonView.getCaseObject().getCaseTypeId());
        }
        else if ("comments".equals(columnName))
        {
          return casePersonView.getComments();
        }
        else
        {
          return null;
        }
      }
    };
  }

  public GroupableRowsHelper getGroupableRowsHelper()
  {
    return groupableRowsHelper;
  }

  public void setGroupableRowsHelper(GroupableRowsHelper groupableRowsHelper)
  {
    this.groupableRowsHelper = groupableRowsHelper;
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return personObjectBean;
  }

  public TabInstance getCurrentTabInstance()
  {
    EditTab tab = personObjectBean.getActiveEditTab();
    if (WebUtils.getBeanName(this).equals(tab.getBeanName()))
    {
      TabInstance tabInstance = tabInstances.get(tab.getSubviewId());
      if (tabInstance == null)
      {
        tabInstance = new TabInstance();
        tabInstances.put(tab.getSubviewId(), tabInstance);
      }
      return tabInstance;
    }
    else
      return EMPTY_TAB_INSTANCE;
  }

  @Override
  public String getObjectId()
  {
    return getCurrentTabInstance().objectId;
  }

  @Override
  public void setObjectId(String objectId)
  {
    getCurrentTabInstance().objectId = objectId;
  }

  @Override
  public boolean isNew()
  {
    return NEW_OBJECT_ID.equals(getCurrentTabInstance().objectId);
  }

  public List<CasePersonView> getRows()
  {
    return getCurrentTabInstance().rows;
  }

  public void setRows(List<CasePersonView> rows)
  {
    getCurrentTabInstance().rows = rows;
  }

  public int getFirstRow()
  {
    return getCurrentTabInstance().firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    getCurrentTabInstance().firstRow = firstRow;
  }

  @Override
  public void load()
  {
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        CasePersonFilter filter = new CasePersonFilter();
        filter.setPersonId(personObjectBean.getObjectId());

        String typeId = getTabBaseTypeId();
        EditTab tab = personObjectBean.getActiveEditTab();
        if (tab.isShowAllTypes())
          typeId = DictionaryConstants.CASE_PERSON_TYPE;
        filter.setCasePersonTypeId(typeId);

        List<CasePersonView> auxList =
          CasesModuleBean.getPort(false).findCasePersonViews(filter);
        setRows(auxList);
        getCurrentTabInstance().rowsFilterHelper.reset();
        getCurrentTabInstance().rowsFilterHelper2.reset();
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else
    {
      TabInstance tabInstance = getCurrentTabInstance();
      tabInstance.objectId = NEW_OBJECT_ID;
      tabInstance.rows = Collections.EMPTY_LIST;
      getCurrentTabInstance().rowsFilterHelper.reset();
      getCurrentTabInstance().rowsFilterHelper2.reset();
      tabInstance.firstRow = 0;
    }
  }

  @Override
  public void clear()
  {
    tabInstances.clear();
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{};
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      if (!isNew()) load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public String getRowStyleClass(Object row)
  {
    RowStyleClassGenerator styleClassGenerator = 
      new DateTimeRowStyleClassGenerator("startDate", "endDate", null);
    return styleClassGenerator.getStyleClass(row);
  }  

}
