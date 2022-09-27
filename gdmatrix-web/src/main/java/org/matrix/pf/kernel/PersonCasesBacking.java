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
package org.matrix.pf.kernel;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;
import org.matrix.pf.web.PageBacking;
import org.matrix.pf.web.helper.ResultListHelper;
import org.matrix.pf.web.helper.ResultListPage;
import org.matrix.pf.web.helper.TabHelper;
import org.matrix.pf.web.helper.TypedHelper;
import org.matrix.pf.web.helper.TypedTabPage;
import org.matrix.web.WebUtils;
import org.santfeliu.cases.web.CaseConfigBean;

/**
 *
 * @author lopezrj-sf
 */
@Named
public class PersonCasesBacking extends PageBacking 
  implements TypedTabPage, ResultListPage
{  
  private static final String PERSON_BACKING = "personBacking";
  private static final String ROOT_TYPE_ID = "CasePerson";
  private static final String OUTCOME = "pf_person_cases";  
  
  private PersonBacking personBacking;
  
  //Helpers
  private TypedHelper typedHelper;
  private ResultListHelper<CasePersonView> resultListHelper;
  private TabHelper tabHelper;
  
  public PersonCasesBacking()
  {
    //Let to super class constructor.   
  }
  
  @PostConstruct
  public void init()
  {
    personBacking = WebUtils.getBacking(PERSON_BACKING);   
    typedHelper = new TypedHelper(this);
    resultListHelper = new ResultListHelper(this);
    tabHelper = new TabHelper(this);
    populate();
  }

  @Override
  public String getPageObjectId()
  {
    return null;
  }  
  
  @Override
  public String getRootTypeId()
  {
    return ROOT_TYPE_ID;
  }

  @Override
  public PersonBacking getObjectBacking()
  {
    return personBacking;
  }
  
  @Override
  public String getTypeId()
  {
    return personBacking.getPageTypeId();
  }
  
  @Override
  public ResultListHelper<CasePersonView> getResultListHelper()
  {
    return resultListHelper;
  }  
  
  @Override
  public TypedHelper getTypedHelper()
  {
    return typedHelper;
  }  

  public TabHelper getTabHelper()
  {
    return tabHelper;
  }
  
  public List<CasePersonView> getRows()
  {
    return resultListHelper.getRows();
  }
  
  @Override
  public String show(String pageId)
  {
    return show();
  }  
  
  @Override
  public String show()
  {    
    populate();
    return OUTCOME;
  }

  @Override
  public List<CasePersonView> getResults(int firstResult, int maxResults)
  {
    try
    {
      CasePersonFilter filter = new CasePersonFilter();
      filter.setPersonId(personBacking.getObjectId());
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);        
      return CaseConfigBean.getPort().findCasePersonViews(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  @Override
  public String store()
  {
    return null;
  }
 
  @Override
  public void load()
  {
    resultListHelper.search();
  }

  @Override
  public void create()
  {
  }
  
  @Override
  public String cancel()
  {
    return null;
  }  
  
  @Override
  public void reset()
  {
    cancel();
    resultListHelper.reset();
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
    catch (Exception ex)
    {
    }
    return typeDescription;
  }  

}