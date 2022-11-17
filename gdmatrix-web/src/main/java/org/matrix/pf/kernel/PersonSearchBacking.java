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
import org.apache.commons.lang.StringUtils;
import org.matrix.kernel.PersonFilter;
import org.matrix.kernel.PersonView;
import org.matrix.pf.web.SearchBacking;
import org.matrix.web.WebUtils;
import org.santfeliu.kernel.web.KernelConfigBean;
/**
 *
 * @author blanquepa
 */
@Named("personSearchBacking")
public class PersonSearchBacking extends SearchBacking
{
  public static final String OUTCOME = "pf_person_search";  
  
  private PersonFilter filter;
  
  private PersonBacking personBacking;
  
  public PersonSearchBacking()
  {
    super();
  }
  
  @PostConstruct
  public void init()
  {
    personBacking = WebUtils.getBacking("personBacking");
    filter = new PersonFilter();
    smartValue = null;
  }

  public PersonFilter getFilter()
  {
    return filter;
  }

  public void setFilter(PersonFilter filter)
  {
    this.filter = filter;
  }
  
  public List<String> getPersonId()
  {
    return this.filter.getPersonId();
  }
  
  public void setPersonId(List<String> personIds)
  {
    this.filter.getPersonId().clear();
    if (personIds != null && !personIds.isEmpty())
      this.filter.getPersonId().addAll(personIds);
  }

  @Override
  public PersonBacking getObjectBacking()
  {
    return personBacking;
  }
     
  @Override
  public String search()
  {
    smartValue = convert(filter);
    return super.search();
  }
  
  @Override
  public String smartSearch()
  {
    filter = convert(smartValue);  
    return super.search();
  }

  @Override
  public String clear()
  {
    filter = new PersonFilter();
    smartValue = null;
    bigListHelper.reset();
    return null;
  }
  
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
  public List<PersonView> getResults(int firstResult, int maxResults)
  {
    try
    {
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      return KernelConfigBean.getPort().findPersonViews(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  } 
  
  @Override
  public String getObjectId(Object row)
  {
    PersonView personView = (PersonView) row;
    return personView.getPersonId();
  }

  @Override
  public String getDescription(Object row)
  {
    PersonView personView = (PersonView) row;
    return personView.getFullName();
  }
    
  private PersonFilter convert(String smartValue)
  {
    filter = new PersonFilter();
    if (smartValue != null)
    {
      if (smartValue.matches("\\d+"))
        filter.getPersonId().add(smartValue);
      else if (smartValue.matches("(\\d+\\D+|\\D+\\d+)"))
        filter.setNif(smartValue);
      else
        filter.setFullName(smartValue);
    }  
    return filter;
  }
    
  private String convert(PersonFilter filter)
  {
    String value = null;
    if (!filter.getPersonId().isEmpty())
      value = filter.getPersonId().get(0);
    else if (!StringUtils.isBlank(filter.getNif()))
      value = filter.getNif();
    else if (!StringUtils.isBlank(filter.getFullName()))
      value = filter.getFullName();

    return value;
  }

  @Override
  public String getTypeId()
  {
    return personBacking.getRootTypeId();
  }

  @Override
  public String getOutcome()
  {
    return OUTCOME;
  }
    
}