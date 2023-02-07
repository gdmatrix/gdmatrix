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
package org.santfeliu.webapp.modules.cases;

import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.cases.Intervention;
import org.matrix.cases.InterventionFilter;
import org.matrix.dic.DictionaryConstants;
import org.santfeliu.webapp.TypeBean;
import org.santfeliu.webapp.modules.dic.TypeTypeBean;

/**
 *
 * @author blanquepa
 */
@Named
@ApplicationScoped
public class InterventionTypeBean 
  extends TypeBean<Intervention, InterventionFilter>
{ 
  @Inject 
  TypeTypeBean typeTypeBean;

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.INTERVENTION_TYPE;
  }

  @Override
  public String getObjectId(Intervention intervention)
  {
    return intervention.getIntId();
  }

  @Override
  public String describe(Intervention intervention)
  {
    return typeTypeBean.getDescription(intervention.getIntTypeId());
  }

  @Override
  public Intervention loadObject(String intId)
  {
    try
    {
      return CasesModuleBean.getPort(true).loadIntervention(intId);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  @Override
  public InterventionFilter queryToFilter(String query, String typeId)
  {
    InterventionFilter filter = new InterventionFilter();
    filter.setIntTypeId(typeId);
    return filter;
  }

  @Override
  public String filterToQuery(InterventionFilter filter)
  {
    return "";
  }

  @Override
  public List find(InterventionFilter filter)
  {
    try
    {
      return CasesModuleBean.getPort(true).findInterventionViews(filter);
      
    }
    catch (Exception ex)
    {
      return Collections.EMPTY_LIST;
    }
  } 
}
