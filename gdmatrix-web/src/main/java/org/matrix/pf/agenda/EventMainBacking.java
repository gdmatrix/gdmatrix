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

import java.util.Date;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import org.matrix.agenda.AgendaConstants;
import org.matrix.agenda.Event;
import org.matrix.pf.web.MainPage;
import org.matrix.pf.web.PageBacking;
import org.matrix.pf.web.helper.TabHelper;
import org.matrix.pf.web.helper.TypedHelper;
import org.matrix.pf.web.helper.TypedTabPage;
import org.matrix.web.WebUtils;
import org.santfeliu.agenda.web.AgendaConfigBean;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author lopezrj-sf
 */
@Named("eventMainBacking")
public class EventMainBacking extends PageBacking 
  implements TypedTabPage, MainPage
{
  public static final String SHOW_AUDIT_PROPERTIES = "_showAuditProperties";
  public static final String OUTCOME = "pf_event_main";  
  
  private Event event;
  private TypedHelper typedHelper;
  private TabHelper tabHelper;
  
  private EventBacking eventBacking;
  private boolean autoAttendant;

  public EventMainBacking()
  {
    //Let to super class constructor.  
  }
  
  @PostConstruct
  public void init()
  {
    eventBacking = WebUtils.getBacking("eventBacking"); 
    
    typedHelper = new TypedHelper(this);  
    tabHelper = new TabHelper(this);
  }

  @Override
  public EventBacking getObjectBacking()
  {
    return eventBacking;
  }

  @Override
  public String getRootTypeId()
  {
    return eventBacking.getRootTypeId();
  }

  @Override
  public String getTypeId()
  {
    if (event != null) 
      return event.getEventTypeId();
    else
      return getMenuItemTypeId();
  }

  @Override
  public TypedHelper getTypedHelper()
  {
    return typedHelper;
  } 

  @Override
  public TabHelper getTabHelper()
  {
    return tabHelper;
  }

  public Event getEvent()
  {
    return event;
  }

  public void setEvent(Event event)
  {
    this.event = event;
  }

  public boolean isAutoAttendant()
  {
    return autoAttendant;
  }

  public void setAutoAttendant(boolean autoAttendant)
  {
    this.autoAttendant = autoAttendant;
  }

  public boolean isOnlyAttendants()
  {
    if (event != null && event.isOnlyAttendants() != null)
      return event.isOnlyAttendants();
    else
      return false;
  }

  public void setOnlyAttendants(boolean value)
  {
    event.setOnlyAttendants(value);
  }  
  
  @Override
  public String getPageObjectId()
  {
    return eventBacking.getObjectId();
  }
  
  public Date getStartDateTime()
  {
    if (event != null && event.getStartDateTime() != null)
      return getDate(event.getStartDateTime());
    else
      return null;
  }
  
  public void setStartDateTime(Date date)
  {
    if (date != null && event != null)
    {
      event.setStartDateTime(TextUtils.formatDate(date, "yyyyMMddHHmmss"));
    }
  }  
  
  public Date getEndDateTime()
  {
    if (event != null && event.getEndDateTime() != null)
      return getDate(event.getEndDateTime());
    else
      return null;
  }  
      
  public void setEndDateTime(Date date)
  {
    if (date != null && event != null)
    {
      event.setEndDateTime(TextUtils.formatDate(date, "yyyyMMddHHmmss"));
    }
  }

  public Date getCreationDateTime()
  {
    if (event != null && event.getCreationDateTime() != null)
      return getDate(event.getCreationDateTime());
    else
      return null;
  }
  
  public void setCreationDateTime(Date date)
  {
    if (date != null && event != null)
    {
      event.setCreationDateTime(TextUtils.formatDate(date, "yyyyMMddHHmmss"));
    }
  }  
  
  public Date getChangeDateTime()
  {
    if (event != null && event.getChangeDateTime() != null)
      return getDate(event.getChangeDateTime());
    else
      return null;
  }
  
  public void setChangeDateTime(Date date)
  {
    if (date != null && event != null)
    {
      event.setChangeDateTime(TextUtils.formatDate(date, "yyyyMMddHHmmss"));
    }
  }  
  
  @Override
  public String show(String pageId)
  {
    eventBacking.setObjectId(pageId);
    return show();
  }
  
  @Override
  public String show()
  {
    populate(); 
    return OUTCOME;
  }
  
  @Override
  public void reset()
  {
    create();
  }
  
  @Override
  public void create()
  {
    event = new Event();   
    event.setEventTypeId(getMenuItemTypeId());
  }
  
  @Override
  public void load()
  {
    String eventId = getPageObjectId();
    if (eventId != null)
    {
      try
      {
        event = AgendaConfigBean.getPort().loadEvent(eventId);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
  }  
  
  @Override
  public String store()
  {
    try
    {
      event = AgendaConfigBean.getPort().storeEvent(event);
      eventBacking.setObjectId(event.getEventId());
      info("STORE_OBJECT");
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
    
  @Override
  public String cancel()
  {
    populate();
    info("CANCEL_OBJECT");    
    return null;
  }
  
  //TODO: AuditHelper?
  public boolean isShowAuditProperties()
  {
    try
    {
      if (UserSessionBean.getCurrentInstance().isUserInRole(
        AgendaConstants.AGENDA_ADMIN_ROLE))
        return true;
      
      if (event == null)
        return false;

      org.santfeliu.dic.Type type =
        TypeCache.getInstance().getType(event.getEventTypeId());
      if (type != null)
      {
        String showAuditProperty = getProperty(SHOW_AUDIT_PROPERTIES);
        return showAuditProperty == null || 
          !"false".equalsIgnoreCase(showAuditProperty);
      }
      else 
        return true;
    }
    catch (Exception ex)
    {
      return true;
    }
  }  
      
  private Date getDate(String dateTime)
  {
    return TextUtils.parseInternalDate(dateTime);    
  }  
}
