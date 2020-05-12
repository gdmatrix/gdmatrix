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

import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;
import org.santfeliu.util.TextUtils;

import org.santfeliu.web.obj.PageBean;


/**
 *
 * @author unknown
 */
public class PersonCasesBean extends PageBean
{
  private List<CasePersonView> rows;
    
  public PersonCasesBean()
  {
    load();
  }

  public String show()
  {
    return "person_cases";
  }
  
  private void load()
  {
    try
    {
      if (!isNew())
      {
        CasePersonFilter filter = new CasePersonFilter();
        filter.setPersonId(getObjectId());
        rows = 
          CaseConfigBean.getPort().findCasePersonViews(filter);
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
  
  public void setRows(List<CasePersonView> rows)
  {
    this.rows = rows;
  }

  public List<CasePersonView> getRows()
  {
    return rows;
  }
  
  public String getTypeDescription()
  {
    CasePersonView row = (CasePersonView)getFacesContext().getExternalContext().
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

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }
  
  public String getViewStartDate()
  {
    String date = "";
    CasePersonView row = (CasePersonView)getValue("#{row}");
    if (row != null)
    {
      date = row.getStartDate();
      date = TextUtils.formatDate(
        TextUtils.parseInternalDate(date), "dd/MM/yyyy");
    }
    return date;
  }

  public String getViewEndDate()
  {
    String date = "";
    CasePersonView row = (CasePersonView)getValue("#{row}");
    if (row != null)
    {
      date = row.getEndDate();
      date = TextUtils.formatDate(
        TextUtils.parseInternalDate(date), "dd/MM/yyyy");
    }
    return date;
  }  

}
