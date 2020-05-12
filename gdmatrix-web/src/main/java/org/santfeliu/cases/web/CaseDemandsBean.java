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
package org.santfeliu.cases.web;

import java.util.List;
import org.matrix.cases.Case;

import org.matrix.cases.Demand;
import org.matrix.cases.CaseManagerPort;

import org.matrix.cases.DemandFilter;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.PropertyDefinition;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.web.obj.TypifiedPageBean;


/**
 *
 * @author unknown
 */
public class CaseDemandsBean extends TypifiedPageBean
{
  public static final String ROOT_TYPE_ID_PROPERTY = "_demandRootTypeId";

  private List<Demand> rows;
  private Demand editingDemand;
  
  public CaseDemandsBean()
  {
    super(DictionaryConstants.DEMAND_TYPE, "CASE_ADMIN");

    CaseMainBean caseMainBean = (CaseMainBean)getBean("caseMainBean");
    Case cas = caseMainBean.getCase();
    Type caseType = TypeCache.getInstance().getType(cas.getCaseTypeId());
    if (caseType != null)
    {
      PropertyDefinition pd =
        caseType.getPropertyDefinition(ROOT_TYPE_ID_PROPERTY);
      if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
        setRootTypeId(pd.getValue().get(0));
    }

    load();    
  }
  
  public String show()
  {
    return "case_demands";
  }
  
  public String store()
  {
    if (editingDemand != null)
    {
      storeDemand();
    }
    else
    {
      load();
    }
    return show();
  }

  public String createDemand()
  {
    editingDemand = new Demand();
    return null;
  }
  
  public String editDemand()
  {
    try
    {
      Demand row = (Demand)getExternalContext().
        getRequestMap().get("row");   
      String demandId = row.getDemandId();
      if (demandId != null)
        editingDemand =
          CaseConfigBean.getPort().loadDemand(demandId);
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public String storeDemand()
  {
    try
    {
      String caseId = getObjectId();
      editingDemand.setCaseId(caseId);
      
      CaseManagerPort port = CaseConfigBean.getPort();
      port.storeDemand(editingDemand);
      editingDemand = null;
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }    
  
  public String removeDemand()
  {
    try
    {
      Demand row = (Demand)getRequestMap().get("row");
      CaseManagerPort port = CaseConfigBean.getPort();
      port.removeDemand(row.getDemandId());
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public String cancelDemand()
  {
    editingDemand = null;
    return null;
  }  
  
  private void load()
  {
    try
    {
      if (!isNew())
      {
        DemandFilter filter = new DemandFilter();
        filter.setCaseId(getObjectId());
        rows = CaseConfigBean.getPort().findDemands(filter);
        
        setRowsTypeLabels();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void setRows(List<Demand> rows)
  {
    this.rows = rows;
  }

  public List<Demand> getRows()
  {
    return rows;
  }

  public void setEditingDemand(Demand editingDemand)
  {
    this.editingDemand = editingDemand;
  }

  public Demand getEditingDemand()
  {
    return editingDemand;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  public String showEditType()
  {
    return getControllerBean().showObject("Type", 
      getEditingDemand().getDemandTypeId());
  }

  public boolean isRenderShowEditTypeButton()
  {
    return getEditingDemand().getDemandTypeId() != null &&
      getEditingDemand().getDemandTypeId().trim().length() > 0;
  }

  public String showRowType()
  {
    return getControllerBean().showObject("Type", getRowTypeId());
  }

  private String getRowTypeId()
  {
    try
    {
      Demand row = (Demand)getExternalContext().getRequestMap().get("row");
      String demandId = row.getDemandId();
      if (demandId != null)
      {
        return CaseConfigBean.getPort().loadDemand(demandId).getDemandTypeId();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  private void setRowsTypeLabels()
  {
    for (Demand demand : rows)
    {
      if (demand.getDemandId() != null)
      {
        String typeId = demand.getDemandTypeId();
        TypeCache typeCache = TypeCache.getInstance();
        try
        {
          Type type = typeCache.getType(typeId);
          if (type != null)
            demand.setDemandTypeId(type.formatTypePath(
              false, true, false, getRootTypeId()));
        }
        catch (Exception ex)
        {
          warn(ex.getMessage());
        }
      }
    }
  }  
}
