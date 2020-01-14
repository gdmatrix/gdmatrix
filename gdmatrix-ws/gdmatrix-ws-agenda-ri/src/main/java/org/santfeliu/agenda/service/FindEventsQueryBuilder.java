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
package org.santfeliu.agenda.service;

import java.util.List;
import javax.persistence.EntityManager;
import org.matrix.agenda.Event;
import org.matrix.agenda.EventFilter;
import org.santfeliu.jpa.QueryBuilder;
import org.santfeliu.security.User;

/**
 *
 * @author blanquepa
 */
public abstract class FindEventsQueryBuilder extends QueryBuilder
{
  protected EventFilter filter;
  protected User user;
  protected List<String> trustors;
  protected boolean counterQuery;

  public static FindEventsQueryBuilder getInstance()
  {
    FindEventsQueryBuilder queryBuilder =
      (FindEventsQueryBuilder)QueryBuilder
        .getInstance(AgendaManager.class, "findEvents");
    if (queryBuilder == null)
      queryBuilder = new DefaultFindEventsQueryBuilder();

    return queryBuilder;
  }

  public boolean isCounterQuery()
  {
    return counterQuery;
  }

  public void setCounterQuery(boolean counterQuery)
  {
    this.counterQuery = counterQuery;
  }

  public EventFilter getFilter()
  {
    return filter;
  }

  public void setFilter(EventFilter filter)
  {
    this.filter = filter;
  }

  public User getUser()
  {
    return user;
  }

  public void setUser(User user)
  {
    this.user = user;
  }

  public List<String> getTrustorors()
  {
    return trustors;
  }

  public void setTrustors(List<String> trustors)
  {
    this.trustors = trustors;
  }

  public abstract List<Event> getEventList(EntityManager entityManager)
    throws Exception;

  public abstract int getEventCount(EntityManager entityManager)
    throws Exception;

  public abstract Event getEvent(EntityManager entityManager)
    throws Exception;
}
