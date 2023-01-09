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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.agenda.AgendaConstants;
import org.matrix.agenda.Event;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Property;
import org.matrix.security.SecurityConstants;
import org.matrix.web.WebUtils;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.web.TypeBean;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.util.TextUtils;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.Tab;
import org.santfeliu.webapp.helpers.PropertyHelper;

/**
 *
 * @author lopezrj-sf
 */
@Named
@ManualScoped
public class EventObjectBean extends ObjectBean
{
  private static final String TYPE_BEAN = "typeBean";

  private Event event = new Event();
  private boolean autoAttendant;
  private PropertyHelper propertyHelper;

  @Inject
  EventTypeBean eventTypeBean;

  @Inject
  EventFinderBean eventFinderBean;

  @PostConstruct
  public void init()
  {
    propertyHelper = new PropertyHelper()
    {
      @Override
      public List<Property> getProperties()
      {
        return event.getProperty();
      }
    };
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

  public PropertyHelper getPropertyHelper()
  {
    return propertyHelper;
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
  public String show()
  {
    return "/pages/agenda/event.xhtml";
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

  //TODO: Move to superclass
  public boolean isEditable()
  {
    return true;
  }

  //TODO Move to superclass
  public String getLanguage()
  {
    return getLocale().getLanguage();
  }

  //TODO Move to superclass
  public String getAdminRole()
  {
    return AgendaConstants.AGENDA_ADMIN_ROLE;
  }

  //TODO Use TypedHelper
  public boolean isPublicType(String typeId)
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

  //TODO Use TypedHelper
  public List<Type> getAllTypes()
  {
    TypeCache tc = TypeCache.getInstance();
    List<Type> types = new ArrayList<>();
    List<SelectItem> items = getAllTypeItems();
    for (SelectItem i : items)
    {
      types.add(tc.getType(String.valueOf(i.getValue())));
    }
    return types;
  }

  @Override
  public void loadObject() throws Exception
  {
    if (!NEW_OBJECT_ID.equals(objectId))
    {
      event = AgendaModuleBean.getClient(false).loadEventFromCache(objectId);
    }
    else event = new Event();
  }

  @Override
  public void loadTabs()
  {
    super.loadTabs();

    if (tabs.isEmpty())
    {
      tabs = new ArrayList<>(); // empty list may be read only
      tabs.add(new Tab("Main", "/pages/agenda/event_main.xhtml"));
      tabs.add(new Tab("Persons", "/pages/agenda/event_persons.xhtml",
        "eventPersonsTabBean"));
      tabs.add(new Tab("Themes", "/pages/agenda/event_themes.xhtml",
        "eventThemesTabBean"));
      tabs.add(new Tab("Places", "/pages/agenda/event_places.xhtml",
        "eventPlacesTabBean"));
    }
  }

  @Override
  public void storeObject() throws Exception
  {
    event = AgendaModuleBean.getClient(false).storeEvent(event);
    setObjectId(event.getEventId());
    eventFinderBean.outdate();
  }

  @Override
  public Serializable saveState()
  {
    return event;
  }

  @Override
  public void restoreState(Serializable state)
  {
    this.event = (Event)state;
  }

  private List<SelectItem> getAllTypeItems()
  {
    return getAllTypeItems(DictionaryConstants.READ_ACTION,
      DictionaryConstants.CREATE_ACTION, DictionaryConstants.WRITE_ACTION);
  }

  private List<SelectItem> getAllTypeItems(String... actions)
  {
    try
    {
      String typeId = getRootTypeId();
      TypeBean typeBean = WebUtils.getBacking(TYPE_BEAN);
      String adminRole = getAdminRole();
      return typeBean.getAllSelectItems(typeId, adminRole, actions, true);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return Collections.emptyList();
  }

  private Date getDate(String dateTime)
  {
    return TextUtils.parseInternalDate(dateTime);
  }

}
