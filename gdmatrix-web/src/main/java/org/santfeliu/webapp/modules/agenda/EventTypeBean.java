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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.agenda.Event;
import org.matrix.agenda.EventFilter;
import org.matrix.dic.DictionaryConstants;
import org.santfeliu.webapp.TypeBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.setup.ObjectSetup;
import static org.santfeliu.webapp.modules.agenda.AgendaModuleBean.getClient;
import org.santfeliu.webapp.setup.Column;
import org.santfeliu.webapp.setup.SearchTab;

/**
 *
 * @author lopezrj-sf
 */
@Named
@ApplicationScoped
public class EventTypeBean extends TypeBean<Event, EventFilter>
{
  private static final String BUNDLE_PREFIX = "$$agendaBundle.";

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.EVENT_TYPE;
  }

  @Override
  public String getObjectId(Event event)
  {
    return event.getEventId();
  }

  @Override
  public String describe(Event event)
  {
    return event.getSummary();
  }

  @Override
  public Event loadObject(String objectId)
  {
    try
    {
      return getClient(true).loadEventFromCache(objectId);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  @Override
  public String getTypeId(Event event)
  {
    return event.getEventTypeId();
  }

  @Override
  public ObjectSetup createObjectSetup()
  {
    ObjectSetup objectSetup = new ObjectSetup();
    objectSetup.setViewId("/pages/agenda/event.xhtml");

    List<SearchTab> searchTabs = new ArrayList();
    SearchTab searchTab =
      new SearchTab("Llistat", "/pages/agenda/event_list.xhtml");
    searchTab.getColumns().add(new Column("eventId",
      BUNDLE_PREFIX + "event_id", "col-1"));
    searchTab.getColumns().add(new Column("eventTypeId",
      BUNDLE_PREFIX + "event_type", "col-2"));
    searchTab.getColumns().add(new Column("startDateTime",
      BUNDLE_PREFIX + "event_startDate", "col-2"));
    searchTab.getColumns().add(new Column("endDateTime",
      BUNDLE_PREFIX + "event_endDate", "col-2"));
    searchTab.getColumns().add(new Column("summary",
      BUNDLE_PREFIX + "event_summary", "col-4"));
    searchTabs.add(searchTab);
    objectSetup.setSearchTabs(searchTabs);

    List<EditTab> editTabs = new ArrayList<>();
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_main",
      "/pages/agenda/event_main.xhtml"));
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_persons",
      "/pages/agenda/event_persons.xhtml",
      "eventPersonsTabBean", "persons1",
      "/pages/agenda/event_persons_dialog.xhtml"));
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_places",
      "/pages/agenda/event_places.xhtml",
      "eventPlacesTabBean"));
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_themes",
      "/pages/agenda/event_themes.xhtml",
      "eventThemesTabBean"));
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_documents",
      "/pages/agenda/event_documents.xhtml",
      "eventDocumentsTabBean", "docs1",
      "/pages/agenda/event_documents_dialog.xhtml"));
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_cases",
      "/pages/agenda/event_cases.xhtml",
      "eventCasesTabBean", "cases1",
      "/pages/agenda/event_cases_dialog.xhtml"));
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_recurrences",
      "/pages/agenda/event_recurrences.xhtml",
      "eventRecurrencesTabBean"));
    objectSetup.setEditTabs(editTabs);

    return objectSetup;
  }

  @Override
  public EventFilter queryToFilter(String query, String typeId)
  {
    if (query == null) query = "";

    EventFilter filter = new EventFilter();
    if (checkIntegerValues(query))
    {
      filter.getEventId().addAll(Arrays.asList(query.split(",")));
    }
    else
    {
      if (!StringUtils.isBlank(query)) filter.setContent(query);
    }
    if (typeId != null)
    {
      filter.getEventTypeId().add(typeId);
    }
    return filter;
  }

  @Override
  public String filterToQuery(EventFilter filter)
  {
    String value = "";
    if (!filter.getEventId().isEmpty())
    {
      value = String.join(",", filter.getEventId());
    }
    else if (!StringUtils.isBlank(filter.getContent()))
    {
      value = filter.getContent();
    }
    return value;
  }

  @Override
  public List<Event> find(EventFilter filter)
  {
    try
    {
      return getClient(true).findEventsFromCache(filter);
    }
    catch (Exception ex)
    {
      return Collections.EMPTY_LIST;
    }
  }

  private boolean checkIntegerValues(String s)
  {
    String[] split = s.split(",");
    for (String item : split)
    {
      try
      {
        Integer.valueOf(item);
      }
      catch (NumberFormatException ex)
      {
        return false;
      }
    }
    return true;
  }

}
