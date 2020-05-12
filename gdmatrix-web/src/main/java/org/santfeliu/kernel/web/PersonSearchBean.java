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
package org.santfeliu.kernel.web;

import java.util.List;
import org.matrix.kernel.PersonFilter;
import org.santfeliu.web.obj.BasicSearchBean;

/**
 *
 * @author unknown
 */
public class PersonSearchBean extends BasicSearchBean
{
  private PersonFilter filter;
  private String personId;

  public PersonSearchBean()
  {
    filter = new PersonFilter();
  }

  public void setFilter(PersonFilter filter)
  {
    this.filter = filter;
  }

  public PersonFilter getFilter()
  {
    return filter;
  }

  public void setPersonId(String personId)
  {
    this.personId = personId;
  }

  public String getPersonId()
  {
    return personId;
  }

  @Override
  public int countResults()
  {
    try
    {
      filter.getPersonId().clear();
      if (personId != null && personId.trim().length() > 0)
      {
        filter.getPersonId().add(personId);
      }
      return KernelConfigBean.getPort().countPersons(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return 0;
  }

  @Override
  public List getResults(int firstResult, int maxResults)
  {
    try
    {
      filter.getPersonId().clear();
      if (personId != null && personId.trim().length() > 0)
      {
        filter.getPersonId().add(personId);
      }
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

  public String showPerson()
  {
    return getControllerBean().showObject("Person",
      (String)getValue("#{row.personId}"));
  }

  public String selectPerson()
  {
    return getControllerBean().select((String)getValue("#{row.personId}"));
  }

  public String show()
  {
    return "person_search";
  }
  
  public String clearFilter()
  {
    this.personId = null;
    filter.getPersonId().clear();
    filter.setFullName(null);
    filter.setName(null);
    filter.setFirstSurname(null);
    filter.setSecondSurname(null);
    filter.setNif(null);
    filter.setPassport(null);
    filter.setFirstResult(0);
    filter.setMaxResults(0);    
    
    //Clear results
    reset();

    return show();    
  }
}

