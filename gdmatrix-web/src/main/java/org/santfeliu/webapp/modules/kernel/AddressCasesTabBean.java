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
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.cases.CaseAddressFilter;
import org.matrix.cases.CaseAddressView;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.helpers.GroupableRowsHelper;
import org.santfeliu.webapp.helpers.TypeSelectHelper;
import org.santfeliu.webapp.modules.cases.CaseObjectBean;
import org.santfeliu.webapp.modules.cases.CasesModuleBean;
import org.santfeliu.webapp.modules.dic.TypeTypeBean;
import org.santfeliu.webapp.setup.TableProperty;

/**
 *
 * @author blanquepa
 */
@Named
@ViewScoped
public class AddressCasesTabBean extends TabBean
{
  @Inject
  private AddressObjectBean addressObjectBean;

  @Inject
  private CaseObjectBean caseObjectBean;

  @Inject
  TypeTypeBean typeTypeBean;
  
  private List<CaseAddressView> rows;
  private int firstRow;
  GroupableRowsHelper groupableRowsHelper;
  private TypeSelectHelper typeSelectHelper;
  

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
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
        return "caseAddressTypeId";
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
          return typeTypeBean.getDescription(
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
    typeSelectHelper = new TypeSelectHelper<CaseAddressView>()
    {
      @Override
      public List<CaseAddressView> getRows()
      {
        return rows;
      }

      @Override
      public boolean isGroupedViewEnabled()
      {
        return getGroupableRowsHelper().isGroupedViewEnabled();
      }

      @Override
      public String getBaseTypeId()
      {
        return getTabBaseTypeId();
      }

      @Override
      public void resetFirstRow()
      {
        firstRow = 0;
      }

      @Override
      public String getRowTypeId(CaseAddressView row)
      {
        return row.getCaseAddressTypeId();
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

  public TypeSelectHelper getTypeSelectHelper()
  {
    return typeSelectHelper;
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return addressObjectBean;
  }

  public List<CaseAddressView> getRows()
  {
    return rows;
  }

  public void setRows(List<CaseAddressView> rows)
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
  public void load()
  {
    System.out.println("load addressCases:" + getObjectId());
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {    
      try
      {
        CaseAddressFilter filter = new CaseAddressFilter();
        filter.setAddressId(addressObjectBean.getObjectId());
        rows = CasesModuleBean.getPort(false).findCaseAddressViews(filter);
        getTypeSelectHelper().load();
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else 
    {
      rows = Collections.EMPTY_LIST;
      getTypeSelectHelper().load();      
    }
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

}
