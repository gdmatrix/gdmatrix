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
package org.santfeliu.webapp.data;

import java.util.List;
import org.matrix.agenda.EventFilter;
import org.matrix.agenda.EventView;
import org.matrix.agenda.OrderByProperty;
import org.matrix.dic.Property;
import org.santfeliu.agenda.client.AgendaManagerClient;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.webapp.modules.agenda.AgendaModuleBean;

/**
 *
 * @author realor
 */
public class EventQuery extends DataQuery<EventView>
{
  EventFilter filter;

  public EventQuery(Credentials credentials)
  {
    super(credentials);
    filter = new EventFilter();
  }

  public EventQuery content(String content)
  {
    filter.setContent(content);
    return this;
  }

  public EventQuery startDateTime(String startDateTime)
  {
    filter.setStartDateTime(startDateTime);
    return this;
  }

  public EventQuery endDateTime(String endDateTime)
  {
    filter.setEndDateTime(endDateTime);
    return this;
  }

  @Override
  public DataQuery<EventView> eq(String field, Object value)
  {
    Property property = new Property();
    property.setName(field);
    property.getValue().add(String.valueOf(value));
    filter.getProperty().add(property);
    return this;
  }
  
  public EventQuery theme(String themeId)
  {
    filter.getThemeId().add(themeId);
    return this;
  }  
  
  @Override
  public EventQuery orderBy(String field, boolean descending)
  {
    var orderBy = new OrderByProperty();
    orderBy.setName(field);
    orderBy.setDescending(descending);
    filter.getOrderBy().add(orderBy);
    return this;
  }

  @Override
  public EventQuery firstResult(Number firstResult)
  {
    filter.setFirstResult(firstResult.intValue());
    return this;
  }
  
  @Override
  public EventQuery maxResults(Number maxResults)
  {
    filter.setMaxResults(maxResults.intValue());
    return this;
  }
  
  @Override
  public List<EventView> execute()
  {          
    AgendaManagerClient client = 
      AgendaModuleBean.getClient(credentials.getUserId(), credentials.getPassword());
    return client.findEventViews(filter);
  }  
}
