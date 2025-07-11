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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.agenda.Attendant;
import org.matrix.agenda.AttendantFilter;
import org.matrix.agenda.AttendantView;
import org.matrix.agenda.Event;
import org.matrix.dic.PropertyDefinition;
import org.primefaces.PrimeFaces;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.helpers.GroupableRowsHelper;
import org.santfeliu.webapp.modules.dic.TypeTypeBean;
import org.santfeliu.webapp.modules.kernel.PersonTypeBean;
import static org.santfeliu.webapp.setup.Action.POST_TAB_EDIT_ACTION;
import static org.santfeliu.webapp.setup.Action.POST_TAB_LOAD_ACTION;
import static org.santfeliu.webapp.setup.Action.POST_TAB_REMOVE_ACTION;
import static org.santfeliu.webapp.setup.Action.POST_TAB_STORE_ACTION;
import static org.santfeliu.webapp.setup.Action.PRE_TAB_EDIT_ACTION;
import static org.santfeliu.webapp.setup.Action.PRE_TAB_LOAD_ACTION;
import static org.santfeliu.webapp.setup.Action.PRE_TAB_REMOVE_ACTION;
import static org.santfeliu.webapp.setup.Action.PRE_TAB_STORE_ACTION;
import org.santfeliu.webapp.setup.TableProperty;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author lopezrj-sf
 */
@Named
@RequestScoped
public class EventPersonsTabBean extends TabBean
{
  private final TabInstance EMPTY_TAB_INSTANCE = new TabInstance();

  private Attendant editing;
  private Set<String> unavailableAttendants;
  Map<String, TabInstance> tabInstances = new HashMap<>();
  private GroupableRowsHelper groupableRowsHelper;

  public class TabInstance
  {
    String objectId = NEW_OBJECT_ID;
    List<AttendantView> rows;
    int firstRow = 0;
  }

  @Inject
  EventObjectBean eventObjectBean;

  @Inject
  PersonTypeBean personTypeBean;

  @Inject
  TypeTypeBean typeTypeBean;

  @PostConstruct
  public void init()
  {
    groupableRowsHelper = new GroupableRowsHelper()
    {
      @Override
      public ObjectBean getObjectBean()
      {
        return EventPersonsTabBean.this.getObjectBean();
      }

      @Override
      public List<TableProperty> getColumns()
      {
        return Collections.EMPTY_LIST;
      }

      @Override
      public void sortRows()
      {
      }

      @Override
      public String getRowTypeColumnName()
      {
        return "attendantTypeId";
      }

      @Override
      public String getFixedColumnValue(Object row, String columnName)
      {
        AttendantView attendantView = (AttendantView)row;
        if ("attendantId".equals(columnName))
        {
          return attendantView.getAttendantId();
        }
        else if ("attendantPerson".equals(columnName))
        {
          return attendantView.getPersonView().getFullName();
        }
        else if ("attendantTypeId".equals(columnName))
        {
          return typeTypeBean.getDescription(
            attendantView.getAttendantTypeId());
        }
        else if ("attended".equals(columnName))
        {
          return attendantView.getAttended();
        }
        else if ("comments".equals(columnName))
        {
          return attendantView.getComments();
        }
        else
        {
          return null;
        }
      }

    };
  }

  public GroupableRowsHelper getGroupableRowsHelper()
  {
    return groupableRowsHelper;
  }

  public void setGroupableRowsHelper(GroupableRowsHelper groupableRowsHelper)
  {
    this.groupableRowsHelper = groupableRowsHelper;
  }

  public TabInstance getCurrentTabInstance()
  {
    EditTab tab = eventObjectBean.getActiveEditTab();
    if (WebUtils.getBeanName(this).equals(tab.getBeanName()))
    {
      TabInstance tabInstance = tabInstances.get(tab.getSubviewId());
      if (tabInstance == null)
      {
        tabInstance = new TabInstance();
        tabInstances.put(tab.getSubviewId(), tabInstance);
      }
      return tabInstance;
    }
    else return EMPTY_TAB_INSTANCE;
  }

  @Override
  public String getObjectId()
  {
    return getCurrentTabInstance().objectId;
  }

  @Override
  public void setObjectId(String objectId)
  {
    getCurrentTabInstance().objectId = objectId;
  }

  @Override
  public boolean isNew()
  {
    return NEW_OBJECT_ID.equals(getCurrentTabInstance().objectId);
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return eventObjectBean;
  }

  public Attendant getEditing()
  {
    return editing;
  }

  public void setEditing(Attendant editing)
  {
    this.editing = editing;
  }

  public Set<String> getUnavailableAttendants()
  {
    if (unavailableAttendants == null)
    {
      unavailableAttendants = new HashSet();
      try
      {
        List<Attendant> unavailableList = AgendaModuleBean.getClient().
          findAttendantsOccupancyFromCache(eventObjectBean.getObjectId());
        for (Attendant unavailable : unavailableList)
        {
          unavailableAttendants.add(unavailable.getPersonId());
        }
      }
      catch (Exception ex) { }
    }
    return unavailableAttendants;
  }

  public void setUnavailableAttendants(Set<String> unavailableAttendants)
  {
    this.unavailableAttendants = unavailableAttendants;
  }

  public void setAttendantTypeId(String attendantTypeId)
  {
    if (editing != null)
      editing.setAttendantTypeId(attendantTypeId);
  }

  public String getAttendantTypeId()
  {
    return editing == null ? NEW_OBJECT_ID : editing.getAttendantTypeId();
  }

  public List<AttendantView> getRows()
  {
    return getCurrentTabInstance().rows;
  }

  public void setRows(List<AttendantView> rows)
  {
    getCurrentTabInstance().rows = rows;
  }

  public int getFirstRow()
  {
    return getCurrentTabInstance().firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    getCurrentTabInstance().firstRow = firstRow;
  }

  public String getPersonDescription()
  {
    if (editing != null && !isNew(editing))
    {
      return personTypeBean.getDescription(editing.getPersonId());
    }
    return "";
  }

  public String getAttendedLabel()
  {
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.agenda.web.resources.AgendaBundle", getLocale());
    String attended = (String)getValue("#{row.attended}");
    if (attended == null) return "";
    else switch (attended)
    {
      case "S":
        return bundle.getString("attendants_yes");
      case "N":
        return bundle.getString("attendants_no");
      case "J":
        return bundle.getString("attendants_ea");
      default:
        return "";
    }
  }

  public boolean isHidden()
  {
    if (editing != null && editing.isHidden() != null)
      return editing.isHidden();
    else
      return false;
  }

  public void setHidden(boolean hidden)
  {
    editing.setHidden(hidden);
  }

  @Override
  public String getTabBaseTypeId()
  {
    String tabBaseTypeId = super.getTabBaseTypeId();
    if (tabBaseTypeId == null)
    {
      Event event = eventObjectBean.getObject();
      if (event != null)
      {
        String typeId = event.getEventTypeId();
        if (typeId != null)
        {
          Type type = TypeCache.getInstance().getType(typeId);
          if (type != null)
          {
            PropertyDefinition propdef = 
              type.getPropertyDefinition("_attendantTypeId");
            if (propdef != null && !propdef.getValue().isEmpty())
            {
              tabBaseTypeId = propdef.getValue().get(0);
            }
          }
        }  
      }
    }
    return tabBaseTypeId;
  }
  
  public void edit(AttendantView row)
  {
    if (row != null)
    {
      try
      {
        executeTabAction(PRE_TAB_EDIT_ACTION, row);
        editing = AgendaModuleBean.getClient(false).
          loadAttendant(row.getAttendantId());
        executeTabAction(POST_TAB_EDIT_ACTION, editing);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else
    {
      create();
    }
  }
  
  @Override
  public void edit(String rowId)
  {
    List<AttendantView> rows = getRows();
    for (AttendantView row : rows)
    {
      if (row.getAttendantId().equals(rowId))
      {
        edit(row);
        PrimeFaces.current().executeScript("PF('eventPersonsDialog').show()");
        return;
      }
    }
  }  

  @Override
  public void load() throws Exception
  {
    executeTabAction(PRE_TAB_LOAD_ACTION, null);
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        AttendantFilter filter = new AttendantFilter();
        filter.setEventId(eventObjectBean.getObjectId());
        List<AttendantView> auxList = AgendaModuleBean.getClient(false).
          findAttendantViewsFromCache(filter);
        String typeId = getTabBaseTypeId();
        EditTab tab = eventObjectBean.getActiveEditTab();
        if (typeId == null || tab.isShowAllTypes())
        {
          getCurrentTabInstance().rows = auxList;
        }
        else
        {
          List<AttendantView> result = new ArrayList();
          for (AttendantView item : auxList)
          {
            Type attendantType =
              TypeCache.getInstance().getType(item.getAttendantTypeId());
            if (attendantType.isDerivedFrom(typeId))
            {
              result.add(item);
            }
          }
          getCurrentTabInstance().rows = result;
        }
        unavailableAttendants = null;
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else
    {
      TabInstance tabInstance = getCurrentTabInstance();
      tabInstance.objectId = NEW_OBJECT_ID;
      tabInstance.rows = Collections.EMPTY_LIST;
      tabInstance.firstRow = 0;
    }
    executeTabAction(POST_TAB_LOAD_ACTION, null);
  }

  public void create()
  {
    executeTabAction(PRE_TAB_EDIT_ACTION, null);
    editing = new Attendant();
    editing.setAttendantTypeId(getCreationTypeId());
    executeTabAction(POST_TAB_EDIT_ACTION, editing);
  }

  @Override
  public void store()
  {
    try
    {
      if (editing != null)
      {
        String eventId = eventObjectBean.getObjectId();
        editing.setEventId(eventId);
        editing = (Attendant) executeTabAction(PRE_TAB_STORE_ACTION, editing);
        AgendaModuleBean.getClient(false).storeAttendant(editing);
        executeTabAction(POST_TAB_STORE_ACTION, editing);
        refreshHiddenTabInstances();
        load();
        editing = null;
        growl("STORE_OBJECT");
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void remove(AttendantView row)
  {
    if (row != null)
    {
      try
      {
        row = (AttendantView)executeTabAction(PRE_TAB_REMOVE_ACTION, row);
        AgendaModuleBean.getClient(false).removeAttendant(row.getAttendantId());
        executeTabAction(POST_TAB_REMOVE_ACTION, row);
        refreshHiddenTabInstances();
        load();
        growl("REMOVE_OBJECT");
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
  }

  public void cancel()
  {
    editing = null;
  }

  @Override
  public boolean isDialogVisible()
  {
    return (editing != null);
  }

  @Override
  public void clear()
  {
    tabInstances.clear();
  }

  public boolean isAttendantAvailable(AttendantView row)
  {
    return !getUnavailableAttendants().contains(row.getPersonId());
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ editing };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      editing = (Attendant)stateArray[0];
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  @Override
  protected String getCreationTypeId()
  {
    if (getTabBaseTypeId() == null)
      return "Attendant";
    else
      return super.getCreationTypeId();
  }

  private boolean isNew(Attendant attendant)
  {
    return (attendant != null && attendant.getAttendantId() == null);
  }

  private void refreshHiddenTabInstances()
  {
    for (TabInstance tabInstance : tabInstances.values())
    {
      if (tabInstance != getCurrentTabInstance())
      {
        tabInstance.objectId = NEW_OBJECT_ID;
      }
    }
  }

}
