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
package org.santfeliu.edu.web;

import java.util.List;

import org.matrix.edu.InscriptionFilter;


import org.santfeliu.web.obj.BasicSearchBean;


/**
 *
 * @author unknown
 */
public class InscriptionSearchBean extends BasicSearchBean
{
  private InscriptionFilter filter = new InscriptionFilter();

  public InscriptionSearchBean()
  {
  }

  public int countResults()
  {
    try
    {
      return EducationConfigBean.getPort().countInscriptions(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return 0;
  }

  public List getResults(int firstResult, int maxResults)
  {
    try
    {
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      return EducationConfigBean.getPort().findInscriptionViews(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String show()
  {
    return "inscription_search";
  }
  
  public String selectInscription()
  {
    return null;
  }

  public String showInscription()
  {
    return getControllerBean().showObject("Inscription",
      (String)getValue("#{row.inscriptionId}"));
  }

  public void setFilter(InscriptionFilter filter)
  {
    this.filter = filter;
  }

  public InscriptionFilter getFilter()
  {
    return filter;
  }
}
