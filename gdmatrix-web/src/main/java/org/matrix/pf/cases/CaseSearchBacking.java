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
import org.matrix.pf.web.SearchBacking;
import org.matrix.web.WebUtils;
import org.santfeliu.cases.web.CaseConfigBean;
import org.santfeliu.web.bean.CMSProperty;
/**
 *
 * @author blanquepa
 */
@Named("caseSearchBacking")
public class CaseSearchBacking extends SearchBacking
{
  @CMSProperty
  public static final String LOAD_METADATA_PROPERTY = "loadMetadata";
  
  public static final String OUTCOME = "pf_case_search";
  
  private CaseFilter filter;
  private String caseIdFilter;
  
  public CaseSearchBacking()
  {
    //Let to super class constructor.    
  }
  
  @PostConstruct
  public void init()
  {
    objectBacking = WebUtils.getInstance(CaseBacking.class);
    filter = new CaseFilter();
    String typeId = getMenuItemTypeId();
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
    String typeId = getMenuItemTypeId();
    if (typeId != null)
      filter.setCaseTypeId(typeId);
    super.search();
    return OUTCOME;
  }
  
  @Override
  public String smartSearch()
  {
    filter = smartValueToFilter(); 
    String typeId = getMenuItemTypeId();
    if (typeId != null)
      filter.setCaseTypeId(typeId);    
    super.search();
    return OUTCOME;
  }

  @Override
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
    List<Case> results = null;
    try
    {
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      results = CaseConfigBean.getPort().findCases(filter);
      loadMetadata(results);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return results;
  }  
  
  private CaseFilter smartValueToFilter()
  {
    CaseFilter result = new CaseFilter();
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
          result.setTitle("%" + smartValue + "%");
      }
    }  
    return result;
  }
    
  private String filterToSmartValue()
  {
    String result = null;
    if (!StringUtils.isBlank(caseIdFilter))
      result = caseIdFilter;
    else if (!StringUtils.isBlank(filter.getTitle()))
    {
      result = filter.getTitle();
      filter.setTitle("%" + filter.getTitle() + "%");
    }

    return result;
  }

  @Override
  public String getFilterTypeId()
  {
    return filter != null ? filter.getCaseTypeId() : null;
  }

  @Override
  public void refresh()
  {
    init();
  }

  @Override
  public String getOutcome()
  {
    return OUTCOME;
  }
  
  //TODO: Move to SearchBean
  private void loadMetadata(List<Case> results) throws Exception
  {
    boolean loadMetadata = false;
    loadMetadata = (getProperty(LOAD_METADATA_PROPERTY) != null
      && getProperty(LOAD_METADATA_PROPERTY).equalsIgnoreCase("true"));
    if (loadMetadata)
    {
      for (int i = 0; i < results.size(); i++)
      {
        Case cas = CaseConfigBean.getPort().loadCase(results.get(i).getCaseId());
        results.set(i, cas);
      }
    }
  }
  
//  public String getViewStartDate()
//  {
//    String date = "";
//    CasePersonView row = (CasePersonView)getValue("#{row}");
//    if (row != null)
//    {
//      date = row.getStartDate();
//      date = TextUtils.formatDate(
//        TextUtils.parseInternalDate(date), "dd/MM/yyyy");
//    }
//    return date;
//  }
//
//  public String getViewEndDate()
//  {
//    String date = "";
//    CasePersonView row = (CasePersonView)getValue("#{row}");
//    if (row != null)
//    {
//      date = row.getEndDate();
//      date = TextUtils.formatDate(
//        TextUtils.parseInternalDate(date), "dd/MM/yyyy");
//    }
//    return date;
//  }   
  
}
