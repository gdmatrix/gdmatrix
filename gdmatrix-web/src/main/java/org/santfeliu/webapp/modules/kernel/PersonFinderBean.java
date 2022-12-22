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
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.kernel.PersonFilter;
import org.matrix.kernel.PersonView;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.kernel.web.KernelConfigBean;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.helpers.BigListHelper;
/**
 *
 * @author blanquepa
 */
@Named
@ManualScoped
public class PersonFinderBean extends FinderBean
{
  private String smartFilter;  
  private PersonFilter filter = new PersonFilter();
  private int firstRow;
  private BigListHelper<PersonView> resultListHelper;
  private boolean isSmartFind;  
  
  @Inject
  NavigatorBean navigatorBean;
  
  @Inject
  PersonTypeBean personTypeBean;  
  
  @Inject
  PersonObjectBean personObjectBean;
  
  @PostConstruct
  public void init()
  {
    resultListHelper = new PersonBigListHelper();
  }
  
  public String getSmartFilter()
  {
    return smartFilter;
  }

  public void setSmartFilter(String smartFilter)
  {
    this.smartFilter = smartFilter;
  }  
    
  public PersonFilter getFilter()
  {
    return filter;
  }

  public void setFilter(PersonFilter filter)
  {
    this.filter = filter;
  }
  
  public List<String> getFilterPersonId()
  {
    return this.filter.getPersonId();
  }
  
  public void setFilterPersonId(List<String> personIds)
  {
    this.filter.getPersonId().clear();
    if (personIds != null && !personIds.isEmpty())
      this.filter.getPersonId().addAll(personIds);
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
  public ObjectBean getObjectBean()
  {
    return personObjectBean;
  }

  public BigListHelper getResultListHelper()
  {
    return resultListHelper;
  }
     
  @Override
  public void smartFind()
  {
    isSmartFind = true;
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
    filter = personTypeBean.queryToFilter(smartFilter, baseTypeId);
    doFind(true);
  }
  
  public void smartClear()
  {
    smartFilter = null;
    resultListHelper.clear();
  }

  @Override
  public void find()
  {
    isSmartFind = false;    
    smartFilter = personTypeBean.filterToQuery(filter);    
    doFind(true);
  }

  public String clear()
  {
    filter = new PersonFilter();
    smartFilter = null;
    resultListHelper.clear();
    return null;
  }
  
  @Override
  public Serializable saveState()
  {
    return new Object[]{ isSmartFind, filter, firstRow };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      isSmartFind = (Boolean)stateArray[0];
      filter = (PersonFilter)stateArray[1];

      doFind(false);

      firstRow = (Integer)stateArray[2];
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }    
  
  private void doFind(boolean autoLoad)
  {
    
    resultListHelper.find();
    
    if (autoLoad)
    {  
      if (resultListHelper.getRowCount() == 1)
      {
        PersonView personView = (PersonView) resultListHelper.getRows().get(0);
        navigatorBean.view(personView.getPersonId());
        personObjectBean.setSearchTabIndex(1);
      }
      else
      {
        personObjectBean.setSearchTabIndex(0);
      }
    }    
  }

  private class PersonBigListHelper extends BigListHelper<PersonView>
  {
    @Override
    public int countResults()
    {
      try
      {
        return KernelConfigBean.getPort().countPersons(filter);
      }
      catch (Exception ex)
      {
        error(ex);
      }
      return 0;
    }

    @Override
    public List<PersonView> getResults(int maxResults)
    {
      try
      {
        filter.setFirstResult(firstRow);
        filter.setMaxResults(maxResults);
        return KernelConfigBean.getPort().findPersonViews(filter);
      }
      catch (Exception ex)
      {
        error(ex);
      }
      return null;
    }
  }

}