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

import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.cases.Case;
import org.matrix.cases.CaseFilter;
import org.matrix.dic.Property;
import static org.matrix.pf.cases.CaseConfigBacking.LOAD_METADATA_PROPERTY;
import org.matrix.pf.script.ScriptFormHelper;
import org.matrix.pf.script.ScriptFormPage;
import org.matrix.pf.web.SearchBacking;
import org.matrix.pf.web.helper.TypedHelper;
import org.matrix.pf.web.helper.TypedPage;
import org.matrix.web.WebUtils;
import org.santfeliu.cases.web.CaseConfigBean;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author blanquepa
 */
@Named("caseSearchBacking")
public class CaseSearchBacking extends SearchBacking 
  implements TypedPage, ScriptFormPage
{

  public static final String OUTCOME = "pf_case_search";
  
  private CaseBacking caseBacking;
  
  private CaseFilter filter;
  private TypedHelper typedHelper;
  private ScriptFormHelper scriptHelper;
  
  public CaseSearchBacking()
  {   
  }
  
  @PostConstruct
  public void init()
  {
    caseBacking = WebUtils.getBacking("caseBacking");
    filter = new CaseFilter();
    String typeId = getMenuItemTypeId();
    if (typeId != null)
      filter.setCaseTypeId(typeId);    
    smartValue = null;
    typedHelper = new TypedHelper(this);
    scriptHelper = new ScriptFormHelper(this);
  }
  
  public CaseFilter getFilter()
  {
    return filter;
  }

  public void setFilter(CaseFilter filter)
  {
    this.filter = filter;
  }

  public ScriptFormHelper getScriptHelper()
  {
    return scriptHelper;
  }

  public void setScriptHelper(ScriptFormHelper scriptHelper)
  {
    this.scriptHelper = scriptHelper;
  }

  public List<String> getCaseId()
  {
    return filter.getCaseId();
  }

  public void setCaseId(List<String> caseIds)
  {
    filter.getCaseId().clear();
    if (caseIds != null && !caseIds.isEmpty())
      this.filter.getCaseId().addAll(caseIds);
  }
  
  public Date getFromDate()
  {
    if (filter != null && filter.getFromDate() != null)
      return TextUtils.parseInternalDate(filter.getFromDate());
    else
      return null;
  }
  
  public void setFromDate(Date date)
  {
    if (date != null && filter != null)
      filter.setFromDate(TextUtils.formatDate(date, "yyyyMMdd"));
  }  
  
  public Date getToDate()
  {
    if (filter != null && filter.getToDate() != null)
      return TextUtils.parseInternalDate(filter.getToDate());
    else
      return null;
  }   
  
  public void setToDate(Date date)
  {
    if (date != null && filter != null)
      filter.setToDate(TextUtils.formatDate(date, "yyyyMMdd"));
  }   

  @Override
  public CaseBacking getObjectBacking()
  {
    return caseBacking;
  }
  
  @Override
  public String show()
  {
    String outcome = null;
    try
    {
      outcome = super.show();
      scriptHelper.show();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    
    return outcome;
  }
     
  @Override
  public String search()
  {
    smartValue = convert(filter);
    if (StringUtils.isBlank(filter.getCaseTypeId()))
    {
      String typeId = getMenuItemTypeId();
      if (typeId != null)
        filter.setCaseTypeId(typeId);
    }
    scriptHelper.mergeProperties();
    return super.search();
  }
  
  @Override
  public String smartSearch()
  {     
    filter = convert(smartValue); 
    String typeId = getMenuItemTypeId();
    if (typeId != null)
      filter.setCaseTypeId(typeId);    
    return super.search();
  }

  @Override
  public String clear()
  {
    filter = new CaseFilter();
    String typeId = getMenuItemTypeId();
    if (typeId != null)
      filter.setCaseTypeId(typeId);        
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
 
  private CaseFilter convert(String smartValue)
  {
    filter = new CaseFilter();
    if (smartValue != null)
    {
      try
      {
        Integer.valueOf(smartValue);
        filter.getCaseId().add(smartValue);
      }
      catch (NumberFormatException ex)
      {
        if (!StringUtils.isBlank(smartValue))
          filter.setTitle("%" + smartValue + "%");
      }
    }  
    return filter;
  }
    
  private String convert(CaseFilter filter)
  {
    String value = null;
    if (!filter.getCaseId().isEmpty())
      value = filter.getCaseId().get(0);
    else if (!StringUtils.isBlank(filter.getTitle()))
    {
      value = filter.getTitle();
      filter.setTitle("%" + filter.getTitle() + "%");
    }

    return value;
  }

  @Override
  public String getTypeId()
  {
    return filter != null ? filter.getCaseTypeId() : getMenuItemTypeId();
  }

  @Override
  public String getOutcome()
  {
    return OUTCOME;
  }
  
  @Override
  public String getConfigTypeId()
  {
    return getMenuItemTypeId();
  }
  
  @Override
  public String getRootTypeId()
  {
    return caseBacking.getRootTypeId();
  }

  @Override
  public TypedHelper getTypedHelper()
  {
    return typedHelper;
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

  @Override
  public List<Property> getProperties()
  {
    return filter.getProperty();
  }

  @Override
  public String save() throws Exception
  { //TODO: Search specific ScriptHelper?
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void load() throws Exception
  {
    
  }
  
  
}
