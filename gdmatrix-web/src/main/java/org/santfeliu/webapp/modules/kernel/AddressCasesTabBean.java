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
import org.matrix.cases.CaseAddressFilter;
import org.matrix.cases.CaseAddressView;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.helpers.GroupableRowsHelper;
import org.santfeliu.webapp.helpers.RowsFilterHelper;
import org.santfeliu.webapp.modules.cases.CaseObjectBean;
import org.santfeliu.webapp.modules.cases.CasesModuleBean;
import org.santfeliu.webapp.modules.dic.TypeTypeBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.setup.TableProperty;
import org.santfeliu.webapp.util.DateTimeRowStyleClassGenerator;
import org.santfeliu.webapp.util.RowStyleClassGenerator;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author blanquepa
 */
@Named
@RequestScoped
public class AddressCasesTabBean extends TabBean
{
  private final Map<String, TabInstance> tabInstances = new HashMap();  
  private final TabInstance EMPTY_TAB_INSTANCE = new TabInstance();
  private GroupableRowsHelper groupableRowsHelper;
    
  public class TabInstance
  {
    String objectId = NEW_OBJECT_ID;
    List<CaseAddressView> rows;
    int firstRow = 0;
    RowsFilterHelper rowsFilterHelper =
      new RowsFilterHelper<CaseAddressView>()
    {
      @Override
      public ObjectBean getObjectBean() 
      {
        return AddressCasesTabBean.this.getObjectBean();
      }
      
      @Override
      public List<CaseAddressView> getRows()
      {
        return rows;
      }

      @Override
      public boolean isGroupedViewEnabled()
      {
        return AddressCasesTabBean.this.getGroupableRowsHelper().
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
      public String getFixedColumnValue(CaseAddressView row, 
        String columnName) 
      {
        if ("caseId".equals(columnName))
        {
          return row.getCaseObject().getCaseId();
        }
        else if ("caseTitle".equals(columnName))
        {
          return row.getCaseObject().getTitle();
        }
        else if ("caseTypeId".equals(columnName))
        {
          return typeTypeBean.getTypeDescription(
            row.getCaseObject().getCaseTypeId());
        }
        else if ("comments".equals(columnName))
        {
          return row.getComments();
        }
        else
        {
          return null;
        }
      }

      @Override
      public String getRowTypeId(CaseAddressView row) 
      {
        return row.getCaseAddressTypeId();
      }
    };

    public RowsFilterHelper getRowsFilterHelper()
    {
      return rowsFilterHelper;
    }
  }  
  
  @Inject
  private AddressObjectBean addressObjectBean;

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
        return AddressCasesTabBean.this.getObjectBean();
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
        CaseAddressView caseAddressView = (CaseAddressView)row;
        if ("caseId".equals(columnName))
        {
          return caseAddressView.getCaseObject().getCaseId();
        }
        else if ("caseTitle".equals(columnName))
        {
          return caseAddressView.getCaseObject().getTitle();
        }
        else if ("caseTypeId".equals(columnName))
        {
          return typeTypeBean.getTypeDescription(
            caseAddressView.getCaseObject().getCaseTypeId());
        }
        else if ("comments".equals(columnName))
        {
          return caseAddressView.getComments();
        }
        else
        {
          return null;
        }
      }
    };
  }
  
  public Map<String, TabInstance> getTabInstances()
  {
    return tabInstances;
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
    return addressObjectBean;
  }

  public TabInstance getCurrentTabInstance()
  {
    EditTab tab = addressObjectBean.getActiveEditTab();
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
  
  public List<CaseAddressView> getRows()
  {
    return getCurrentTabInstance().rows;
  }

  public void setRows(List<CaseAddressView> rows)
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
    System.out.println("load addressCases:" + getObjectId());
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        CaseAddressFilter filter = new CaseAddressFilter();
        filter.setAddressId(addressObjectBean.getObjectId());        
        List<CaseAddressView> auxList = 
          CasesModuleBean.getPort(false).findCaseAddressViews(filter);
        setRows(auxList);
        getCurrentTabInstance().rowsFilterHelper.load();
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
      getCurrentTabInstance().rowsFilterHelper.load();
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
