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
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.cases.Demand;
import org.matrix.cases.DemandFilter;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import static org.santfeliu.webapp.modules.cases.CasesModuleBean.getPort;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class CaseDemandsTabBean extends TabBean
{
  private List<Demand> rows;
  private int firstRow;
  private Demand editing;

  @Inject
  CaseObjectBean caseObjectBean;

  @Override
  public ObjectBean getObjectBean()
  {
    return caseObjectBean;
  }

  public List<Demand> getRows()
  {
    return rows;
  }

  public void setRows(List<Demand> rows)
  {
    this.rows = rows;
  }

  public Demand getEditing()
  {
    return editing;
  }

  public void setEditing(Demand demand)
  {
    this.editing = demand;
  }

  public String getDemandTypeDescription(Demand demand)
  {
    String typeId = demand.getDemandTypeId();
    Type type = TypeCache.getInstance().getType(typeId);
    return type.getDescription();
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }

  public String getRootDemandTypeId()
  {
    String typeId = caseObjectBean.getActiveEditTab().getBaseTypeId();
    return typeId == null ? "Demand" : typeId;
  }

  @Override
  public void load()
  {
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        DemandFilter filter = new DemandFilter();
        filter.setCaseId(getObjectId());
        rows = getPort(false).findDemands(filter);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else
    {
      rows = Collections.EMPTY_LIST;
      firstRow = 0;
    }
  }

  @Override
  public void store()
  {
    try
    {
      editing.setCaseId(getObjectId());
      try
      {
        getPort(false).storeDemand(editing);
      }
      catch (Exception ex)
      {
        throw new Exception("INVALID_OPERATION");
      }
      load();
      editing = null;
      growl("STORE_OBJECT");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void cancel()
  {
    editing = null;
  }

  @Override
  public boolean isDialogVisible()
  {
    return (editing != null);
  }

  public void create()
  {
    editing = new Demand();
  }

  public void edit(Demand demand)
  {
    if (demand != null)
    {
      try
      {
        editing = getPort(false).loadDemand(demand.getDemandId());
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else
    {
      create();
    }
  }

  public void remove(Demand demand)
  {
    if (demand != null)
    {
      try
      {
        getPort(false).removeDemand(demand.getDemandId());
        load();
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ editing };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      editing = (Demand)stateArray[0];

      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
}
