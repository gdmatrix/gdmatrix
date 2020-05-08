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
package org.santfeliu.agenda.web;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import org.apache.commons.lang.StringUtils;
import org.matrix.agenda.AgendaConstants;
import org.matrix.agenda.Attendant;
import org.matrix.agenda.Event;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.PropertyDefinition;
import org.matrix.security.AccessControl;
import org.matrix.security.SecurityConstants;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.doc.util.HtmlFixer;
import org.santfeliu.security.User;
import org.santfeliu.security.UserCache;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.DynamicTypifiedPageBean;
import org.santfeliu.ws.WSExceptionFactory;

/**
 *
 * @author blanquepa
 */
public class EventMainBean extends DynamicTypifiedPageBean
{
  public static final String AUTO_ATTENDANT_TYPE = "_autoAttendantTypeId";
  
  private Event event;
  private boolean modified;
  private boolean panelCollapsed = false;
  private boolean autoAttendant = true;

  public EventMainBean()
  {
    super(DictionaryConstants.EVENT_TYPE, AgendaConstants.AGENDA_ADMIN_ROLE);
    allTypesVisible = false;
  }

  //Accessors
  public Event getEvent()
  {
    return event;
  }

  public void setEvent(Event event)
  {
    this.event = event;
  }

  public boolean isPanelCollapsed()
  {
    return panelCollapsed;
  }

  public void setPanelCollapsed(boolean panelCollapsed)
  {
    this.panelCollapsed = panelCollapsed;
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
      return event.isOnlyAttendants().booleanValue();
    else
      return false;
  }

  public void setOnlyAttendants(boolean value)
  {
    event.setOnlyAttendants(value);
  }

  @Override
  public boolean isModified()
  {
    return true;
  }

  public void setModified(boolean modified)
  {
    this.modified = modified;
  }

  public boolean isEditable() throws Exception
  {
    if (event == null || event.getEventId() == null)
      return true;

    if (UserSessionBean.getCurrentInstance().isUserInRole(
      AgendaConstants.AGENDA_ADMIN_ROLE))
      return true;

    if (AgendaConfigBean.isRunAsAdmin()) return true;

    Type currentType = getCurrentType();
    if (currentType == null)
      return true;

    Set<AccessControl> acls = new HashSet();
    acls.addAll(currentType.getAccessControl());
    acls.addAll(event.getAccessControl());

    if (acls != null)
    {
      for (AccessControl acl : acls)
      {
        String action = acl.getAction();
        if (DictionaryConstants.WRITE_ACTION.equals(action))
        {
          String roleId = acl.getRoleId();
          if (UserSessionBean.getCurrentInstance().isUserInRole(roleId))
            return true;
        }
      }
    }

    return false;
  }

  public Date getCreationDateTime()
  {
    if (event != null && event.getCreationDateTime() != null)
      return TextUtils.parseInternalDate(event.getCreationDateTime());
    else
      return null;
  }

  public Date getChangeDateTime()
  {
    if (event != null && event.getChangeDateTime() != null)
      return TextUtils.parseInternalDate(event.getChangeDateTime());
    else
      return null;
  }

  //Actions
  public String show()
  {
    return "event_main";
  }

  public String showType()
  {
    return getControllerBean().showObject("Type", getCurrentTypeId());
  }

  public boolean isRenderShowTypeButton()
  {
    return getCurrentTypeId() != null && getCurrentTypeId().trim().length() > 0;
  }

  public boolean isPublicType()
  {
    Type type = getCurrentType();
    if (type != null)
      return type.canPerformAction("Read",
        Collections.singleton(SecurityConstants.EVERYONE_ROLE));
    else
      return false;
  }

  public String searchType()
  {
    return searchType("#{eventMainBean.currentTypeId}");
  }

  public String allDay()
  {
    String startDateTime = event.getStartDateTime();
    Date date = TextUtils.parseInternalDate(startDateTime);
    if (date != null)
    {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      calendar.set(Calendar.HOUR_OF_DAY, 0);
      calendar.set(Calendar.MINUTE, 0);
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);
      Date time1 = calendar.getTime();
      calendar.add(Calendar.HOUR_OF_DAY, 24);
      calendar.add(Calendar.SECOND, -1);
      Date time2 = calendar.getTime();
      event.setStartDateTime(TextUtils.formatDate(time1, "yyyyMMddHHmmss"));
      event.setEndDateTime(TextUtils.formatDate(time2, "yyyyMMddHHmmss"));
    }
    return null;
  }
  
  @Override
  public String store()
  {
    try
    {
      event.setEventTypeId(getCurrentTypeId());

      if (event.getStartDateTime() == null || event.getEndDateTime() == null)
      {
        Calendar cal = Calendar.getInstance();
        if (event.getStartDateTime() != null)
        {
          cal.setTime(TextUtils.parseInternalDate(event.getStartDateTime()));
        }
        else
        {
          cal.set(Calendar.HOUR_OF_DAY, 0);
          cal.set(Calendar.MINUTE, 0);
          cal.set(Calendar.SECOND, 0);
          event.setStartDateTime(TextUtils.formatDate(cal.getTime(), "yyyyMMddHHmmss"));
        }
          
        if (event.getEndDateTime() == null)
        {
          int duration = getEventTypeDuration(event.getEventTypeId());
          cal.add(Calendar.MINUTE, duration);
          event.setEndDateTime(TextUtils.formatDate(cal.getTime(), "yyyyMMddHHmmss")) ;
        }
      }

      event.getProperty().clear();
      List properties = getFormDataAsProperties();
      if (properties != null)
        event.getProperty().addAll(properties);
      
      //Correcció dels atributs aria-label als enllaços
      String scriptName = MatrixConfig.getProperty("htmlFixer.script");
      String userId = MatrixConfig.getProperty("adminCredentials.userId");
      String password = MatrixConfig.getProperty("adminCredentials.password");
      HtmlFixer htmlFixer = new HtmlFixer(scriptName, userId, password);
      event.setDetail(htmlFixer.fixCode(event.getDetail()));

      event = AgendaConfigBean.getPort().storeEvent(event);

      setFormDataFromProperties(event.getProperty());

      if (autoAttendant && isNew())
      {
        UserSessionBean userSessionBean =
          UserSessionBean.getCurrentInstance();
        User user = UserCache.getUser(userSessionBean.getCredentials());
        String personId = user.getPersonId();
        
        Attendant attendant = new Attendant();
        attendant.setPersonId(personId);
        attendant.setEventId(event.getEventId());

        String autoAttendantType = DictionaryConstants.ATTENDANT_TYPE;
        Type type = TypeCache.getInstance().getType(getCurrentTypeId());
        if (type != null)
        {
          PropertyDefinition autoAttendantPD = 
            type.getPropertyDefinition(AUTO_ATTENDANT_TYPE);
          if (autoAttendantPD != null && !autoAttendantPD.getValue().isEmpty())
            autoAttendantType = autoAttendantPD.getValue().get(0);
        }
        attendant.setAttendantTypeId(autoAttendantType);        
        attendant = AgendaConfigBean.getPort().storeAttendant(attendant);
      }

      Date startDate = TextUtils.parseInternalDate(event.getStartDateTime());
      Date endDate = TextUtils.parseInternalDate(event.getEndDateTime());
      if (endDate.getTime() - startDate.getTime() > 86400000)
      {
        warn("LONG_DURATION_EVENT");
      }

      setObjectId(event.getEventId());
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return show();
  }

  //Events
  public void valueChanged(ValueChangeEvent event)
  {
    modified = true;
  }

  protected void load()
  {
    if (isNew())
    {
      event = new Event();
    }
    else
    {
      try
      {
        event = AgendaConfigBean.getPort().loadEventFromCache(getObjectId());
        setCurrentTypeId(event.getEventTypeId());
      }
      catch (Exception ex)
      {
        getObjectBean().clearObject();
        event = new Event();
        setCurrentTypeId(null);
        error(ex);
      }
      setFormDataFromProperties(event.getProperty());
    }
  }

  @Override
  public List<SelectItem> getAllTypeItems()
  {
    List<SelectItem> typeItems = getAllTypeItems(
      DictionaryConstants.CREATE_ACTION,
      DictionaryConstants.WRITE_ACTION);
    
    for (SelectItem typeItem : typeItems)
    {
      String typeId = (String)typeItem.getValue();
      if (!StringUtils.isBlank(typeId))
      {
        Type type = TypeCache.getInstance().getType(typeId);
        if (type.canPerformAction(DictionaryConstants.READ_ACTION, 
          Collections.singleton(SecurityConstants.EVERYONE_ROLE)))
        {
          typeItem.setLabel(typeItem.getLabel() + " " + ((char)0x24CC) + " ");
        }
      }
    }

    return typeItems;
  }

  private int getEventTypeDuration(String eventTypeId)
  {
    // return event type duration in minutes
    try
    {
      TypeCache typeCache = TypeCache.getInstance();
      Type eventType = typeCache.getType(eventTypeId);
      PropertyDefinition propDef = eventType.getPropertyDefinition("duration");
      if (propDef != null)
      {
        String value = propDef.getValue().get(0);
        return Integer.parseInt(value);
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return 30; // default duration
  }  
}
