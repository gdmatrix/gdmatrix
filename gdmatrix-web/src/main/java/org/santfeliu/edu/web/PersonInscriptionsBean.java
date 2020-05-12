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
import org.matrix.edu.InscriptionView;

import org.santfeliu.web.obj.PageBean;

/**
 *
 * @author unknown
 */
public class PersonInscriptionsBean extends PageBean
{
  List<InscriptionView> inscriptionViews;

  public PersonInscriptionsBean()
  {
    load();
  }

  public void setInscriptionViews(List<InscriptionView> inscriptionViews)
  {
    this.inscriptionViews = inscriptionViews;
  }

  public List<InscriptionView> getInscriptionViews()
  {
    return inscriptionViews;
  }

  public String show()
  {
    return "person_inscriptions";
  }
  
  public String showInscription()
  {
    return getControllerBean().showObject("Inscription",
      (String)getValue("#{row.inscriptionId}"));
  }

  public int getRowCount()
  {
    return (getInscriptionViews() == null ? 0 : getInscriptionViews().size());
  }
  
  private void load()
  {
    try
    {
      if (!isNew())
      {
        InscriptionFilter filter = new InscriptionFilter();
        filter.setPersonId(getObjectId());
        this.inscriptionViews =
          EducationConfigBean.getPort().findInscriptionViews(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
}
