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
package org.santfeliu.webapp.modules.agenda;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.agenda.AgendaConstants;
import org.matrix.agenda.Event;
import org.matrix.agenda.Attendant;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.PropertyDefinition;
import org.matrix.security.AccessControl;
import org.matrix.security.SecurityConstants;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.security.User;
import org.santfeliu.security.UserCache;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.modules.dic.TypeTypeBean;

/**
 *
 * @author lopezrj-sf
 */
@Named
@ViewScoped
public class EventObjectBean extends ObjectBean
{
  private static final String AUTO_ATTENDANT_TYPE = "_autoAttendantTypeId";

  private Event event = new Event();
  private boolean autoAttendant;
  private String formSelector;

  @Inject
  EventTypeBean eventTypeBean;

  @Inject
  EventFinderBean eventFinderBean;

  @Inject
  TypeTypeBean typeTypeBean;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
  }

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.EVENT_TYPE;
  }

  @Override
  public EventTypeBean getTypeBean()
  {
    return eventTypeBean;
  }

  @Override
  public Event getObject()
  {
    return isNew() ? null : event;
  }

  @Override
  public String getDescription()
  {
    return isNew() ? "" : getDescription(event.getEventId());
  }

  public String getDescription(String eventId)
  {
    return getTypeBean().getDescription(eventId);
  }

  @Override
  public EventFinderBean getFinderBean()
  {
    return eventFinderBean;
  }

  public String getFormSelector()
  {
    return formSelector;
  }

  public void setFormSelector(String formSelector)
  {
    this.formSelector = formSelector;
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

  @Override
  public int getEditModeSelector()
  {
    try
    {
      if (getObjectSetup() == null) loadObjectSetup();
      return getSearchTabs().size();      
    }
    catch (Exception ex)
    {
      return 1;
    }
  }

  public Date getStartDateTime()
  {
    if (event != null && event.getStartDateTime() != null)
      return getDate(event.getStartDateTime());
    else
      return null;
  }

  public Date getEndDateTime()
  {
    if (event != null && event.getEndDateTime() != null)
      return getDate(event.getEndDateTime());
    else
      return null;
  }

  public void setStartDateTime(Date date)
  {
    if (event != null)
    {
      if (date == null)
        date = new Date();
      event.setStartDateTime(TextUtils.formatDate(date, "yyyyMMddHHmmss"));
    }
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

  //TODO Move to superclass
  public String getAdminRole()
  {
    return AgendaConstants.AGENDA_ADMIN_ROLE;
  }

  @Override
  public void loadObject() throws Exception
  {
    formSelector = null;

    if (!NEW_OBJECT_ID.equals(objectId))
    {
      event = AgendaModuleBean.getClient(false).loadEvent(objectId);
    }
    else event = new Event();
  }

  @Override
  public void storeObject() throws Exception
  {
    event = AgendaModuleBean.getClient().storeEvent(event);
    if (isAutoAttendant() && isNew())
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      User user = UserCache.getUser(userSessionBean.getCredentials());
      String personId = user.getPersonId();

      Attendant attendant = new Attendant();
      attendant.setPersonId(personId);
      attendant.setEventId(event.getEventId());

      String autoAttendantType = DictionaryConstants.ATTENDANT_TYPE;
      Type type = TypeCache.getInstance().getType(event.getEventTypeId());
      if (type != null)
      {
        PropertyDefinition autoAttendantPD =
          type.getPropertyDefinition(AUTO_ATTENDANT_TYPE);
        if (autoAttendantPD != null && !autoAttendantPD.getValue().isEmpty())
          autoAttendantType = autoAttendantPD.getValue().get(0);
      }
      attendant.setAttendantTypeId(autoAttendantType);
      AgendaModuleBean.getClient().storeAttendant(attendant);
    }
    setObjectId(event.getEventId());
    eventFinderBean.outdate();
  }

  @Override
  public void removeObject() throws Exception
  {
    AgendaModuleBean.getClient(false).removeEvent(event.getEventId());
    eventFinderBean.outdate();
  }

  @Override
  public Serializable saveState()
  {
    return new Object[] { event, formSelector };
  }

  @Override
  public void restoreState(Serializable state)
  {
    Object[] array = (Object[])state;
    this.event = (Event)array[0];
    this.formSelector = (String)array[1];
  }

  @Override
  public boolean isEditable()
  {
    if (UserSessionBean.getCurrentInstance().isUserInRole(
      AgendaConstants.AGENDA_ADMIN_ROLE))
      return true;

    if (AgendaModuleBean.isRunAsAdmin()) return true;

    if (!super.isEditable()) return false; //tab protection

    if (event == null || event.getEventId() == null ||
      event.getEventTypeId() == null)
      return true;

    Type currentType =
      TypeCache.getInstance().getType(event.getEventTypeId());
    if (currentType == null) return true;

    Set<AccessControl> acls = new HashSet();
    acls.addAll(currentType.getAccessControl());
    acls.addAll(event.getAccessControl());
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

    return false;
  }

  private Date getDate(String dateTime)
  {
    return TextUtils.parseInternalDate(dateTime);
  }

  private boolean isPublicType(String typeId)
  {
    if (!StringUtils.isBlank(typeId))
    {
      Type type = TypeCache.getInstance().getType(typeId);
      if (type != null)
      {
        return type.canPerformAction(DictionaryConstants.READ_ACTION,
            Collections.singleton(SecurityConstants.EVERYONE_ROLE));
      }
    }
    return false;
  }

}
