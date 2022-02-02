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
package org.matrix.pf.cases;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.cases.Case;
import org.matrix.cases.CaseFilter;
import org.matrix.cases.CasePersonView;
import org.matrix.pf.web.SearchBacking;
import org.matrix.web.WebUtils;
import org.santfeliu.cases.web.CaseConfigBean;
import org.santfeliu.util.TextUtils;
/**
 *
 * @author blanquepa
 */
@Named("caseSearchBacking")
public class CaseSearchBacking extends SearchBacking
{
  private CaseFilter filter;
  private String caseIdFilter;
  
  public CaseSearchBacking()
  {
  }
  
  @PostConstruct
  public void init()
  {
    objectBacking = WebUtils.getInstance(CaseBacking.class);
    filter = new CaseFilter();
    String typeId = getObjectTypeId();
    if (typeId != null)
      filter.setCaseTypeId(typeId);    
    smartValue = null;
  }

  public CaseFilter getFilter()
  {
    return filter;
  }

  public void setFilter(CaseFilter filter)
  {
    this.filter = filter;
  }

  public String getCaseIdFilter()
  {
    return caseIdFilter;
  }

  public void setCaseIdFilter(String caseIdFilter)
  {
    this.caseIdFilter = caseIdFilter;
  }   
   
  @Override
  public String search()
  {
    smartValue = filterToSmartValue();
    String typeId = getObjectTypeId();
    if (typeId != null)
      filter.setCaseTypeId(typeId);
    super.search();
    return "pf_case_search";
  }
  
  public String smartSearch()
  {
    filter = smartValueToFilter(); 
    String typeId = getObjectTypeId();
    if (typeId != null)
      filter.setCaseTypeId(typeId);    
    super.search();
    return "pf_case_search";
  }

  public String clear()
  {
    filter = new CaseFilter();
    caseIdFilter = null;
    smartValue = null;
    return null;
  }
  
  @Override
  public int countResults()
  {
    try
    {
      return CaseConfigBean.getPort().countCases(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return 0;
  }

  @Override
  public List<Case> getResults(int firstResult, int maxResults)
  {
    try
    {
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      return CaseConfigBean.getPort().findCases(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }  
  
  private CaseFilter smartValueToFilter()
  {
    CaseFilter filter = new CaseFilter();
    if (smartValue != null)
    {
      try
      {
        Integer.valueOf(smartValue);
        caseIdFilter = smartValue;
      }
      catch (NumberFormatException ex)
      {
        if (!StringUtils.isBlank(smartValue))
          filter.setTitle("%" + smartValue + "%");
      }
    }  
    return filter;
  }
    
  private String filterToSmartValue()
  {
    String smartValue = null;
    if (!StringUtils.isBlank(caseIdFilter))
      smartValue = caseIdFilter;
    else if (!StringUtils.isBlank(filter.getTitle()))
    {
      smartValue = filter.getTitle();
      filter.setTitle("%" + filter.getTitle() + "%");
    }

    return smartValue;
  }
  
  @Override
  public String show()
  {
    return "pf_case_search";
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
