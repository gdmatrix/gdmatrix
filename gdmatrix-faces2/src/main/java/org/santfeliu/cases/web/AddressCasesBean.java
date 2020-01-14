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
import org.matrix.cases.CaseAddressFilter;
import org.matrix.cases.CaseAddressView;
import org.santfeliu.web.obj.PageBean;

/**
 *
 * @author blanquepa
 */
public class AddressCasesBean extends PageBean
{
  private List<CaseAddressView> rows;
    
  public AddressCasesBean()
  {
    load();
  }

  public String show()
  {
    return "address_cases";
  }
  
  private void load()
  {
    try
    {
      if (!isNew())
      {
        CaseAddressFilter filter = new CaseAddressFilter();
        filter.setAddressId(getObjectId());
        rows = 
          CaseConfigBean.getPort().findCaseAddressViews(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public String showCase()
  {
    return getControllerBean().showObject("Case",
      (String)getValue("#{row.caseObject.caseId}"));
  }  
  
  public void setRows(List<CaseAddressView> rows)
  {
    this.rows = rows;
  }

  public List<CaseAddressView> getRows()
  {
    return rows;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  public String getTypeDescription()
  {
    CaseAddressView row = (CaseAddressView)getFacesContext().getExternalContext().
      getRequestMap().get("row");

    String type = row.getCaseObject().getCaseTypeId();
    
    CaseConfigBean caseConfigBean = (CaseConfigBean)getBean("caseConfigBean");
    String typeDescription = null;    
    try
    {
      typeDescription = caseConfigBean.getCaseTypeDescription(type);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return typeDescription;
  }
}
