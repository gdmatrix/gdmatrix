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
package org.matrix.pf.agenda;

import java.util.List;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.matrix.agenda.AgendaConstants;
import org.matrix.agenda.Event;
import org.matrix.agenda.EventFilter;
import org.matrix.pf.cms.CMSContent;
import org.matrix.pf.web.ObjectBacking;
import org.matrix.web.WebUtils;
import org.santfeliu.agenda.web.AgendaConfigBean;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author lopezrj-sf
 */
@CMSContent(typeId = "Event")
@Named("eventBacking")
public class EventBacking extends ObjectBacking<Event>
{   
  public EventBacking()
  {
    super();  
  }
 
  @Override
  public EventSearchBacking getSearchBacking()
  {
    return WebUtils.getBacking("eventSearchBacking");
  }

  @Override
  public String getObjectId(Event event)
  {
    return event.getEventId();
  }
  
  @Override
  public String getTypeId()
  {    
    if (!isNew()) //If not object or search page.
    {
      EventMainBacking mainBacking = WebUtils.getBacking("eventMainBacking"); 
      if (mainBacking != null)
        return mainBacking.getTypeId();
    }    
      
    return super.getTypeId();    
  }  
  
  @Override
  public boolean hasCustomHeader()
  {
    return true;
  }
  
  @Override
  public String getDescription()
  {
    EventMainBacking mainBacking = WebUtils.getBacking("eventMainBacking");
    if (mainBacking != null)
      return getDescription(mainBacking.getEvent().getEventId());
    else
      return super.getDescription();
  }
  
  @Override
  public String getDescription(String objectId)
  {
    try
    {
      if ((objectId != null && objectId.contains(";")) || "".equals(objectId))
        return objectId;
      
      EventFilter filter = new EventFilter();
      filter.getEventId().add(objectId);
      String userId = MatrixConfig.getProperty("adminCredentials.userId");
      String password = MatrixConfig.getProperty("adminCredentials.password");      
      List<Event> events = 
        AgendaConfigBean.getPort(userId, password).findEvents(filter);
      
      if (events != null && !events.isEmpty())
        return getDescription(events.get(0));      
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return objectId;
  }    
  
  @Override
  public String getDescription(Event event)
  {
    if (event == null) return "";    
    return event.getSummary();
  }
  
  @Override
  public List<SelectItem> getFavorites()
  {
    return getFavorites(getRootTypeId());
  }  

  @Override
  public String show()
  {
    return super.show();
  }

  @Override
  public String getAdminRole()
  {
    return AgendaConstants.AGENDA_ADMIN_ROLE;
  }
  
  @Override
  public boolean remove(String objectId)
  {
    try
    {
      return AgendaConfigBean.getPort().removeEvent(objectId);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return false;
  }
     
}
