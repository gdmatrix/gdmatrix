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
package org.santfeliu.cases.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.santfeliu.jpa.QueryBuilder;

/**
 *
 * @author blanquepa
 */
class JPAFindInterventionPropertiesQueryBuilder extends QueryBuilder
{
  List<String> interventionIds;

  @Override
  public Query getQuery(EntityManager em) throws Exception
  {
    StringBuilder buffer = new StringBuilder();

    appendMainStatement(buffer);

    System.out.println(buffer);
    
    Query query = em.createQuery(buffer.toString());
    setParameters(query);

    return query;
  }

  private void appendMainStatement(StringBuilder buffer)
  {
    buffer.append("SELECT ip FROM InterventionProperty ip ");
    appendOperator(buffer, "AND");
    appendInOperator(buffer, "ip.id", ":", "ids", interventionIds);
    buffer.append("ORDER BY ip.id,ip.name,ip.index ");
  }

  public List<String> getInterventionIds()
  {
    return interventionIds;
  }

  public void setInterventionIds(Collection<String> interventionIds)
  {
    this.interventionIds = new ArrayList<String>();
    this.interventionIds.addAll(interventionIds);
  }
  
}
