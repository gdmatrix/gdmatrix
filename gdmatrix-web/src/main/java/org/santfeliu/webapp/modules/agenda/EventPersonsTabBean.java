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
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.agenda.Attendant;
import org.matrix.agenda.AttendantFilter;
import org.matrix.agenda.AttendantView;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.modules.dic.TypeTypeBean;
import org.santfeliu.webapp.modules.kernel.PersonTypeBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author lopezrj-sf
 */
@Named
@ViewScoped
public class EventPersonsTabBean extends TabBean
{
  private final TabInstance EMPTY_TAB_INSTANCE = new TabInstance();

  private Attendant editing;
  Map<String, TabInstance> tabInstances = new HashMap<>();

  public class TabInstance
  {
    String objectId = NEW_OBJECT_ID;
    List<AttendantView> rows;
    int firstRow = 0;
    boolean groupedView = true;
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
    System.out.println("Creating " + this);
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

  public boolean isGroupedView()
  {
    return isGroupedViewEnabled() && getCurrentTabInstance().groupedView;
  }

  public void setGroupedView(boolean groupedView)
  {
    getCurrentTabInstance().groupedView = groupedView;
  }

  public boolean isGroupedViewEnabled()
  {
    return Boolean.parseBoolean(eventObjectBean.getActiveEditTab().
      getProperties().getString("groupedViewEnabled"));
  }

  public String getPersonDescription()
  {
    if (editing != null && !isNew(editing))
    {
      return personTypeBean.getDescription(editing.getPersonId());
    }
    return "";
  }

  public String getAttendantTypeDescription()
  {
    String typeId = null;
    AttendantView row = (AttendantView)getValue("#{row}");
    if (row != null)
    {
      typeId = row.getAttendantTypeId();
      if (typeId != null)
      {
        return typeTypeBean.getDescription(typeId);
      }
    }
    return typeId;
  }

  public List<SelectItem> getAttendedSelectItems()
  {
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.agenda.web.resources.AgendaBundle", getLocale());
    List<SelectItem> result = new ArrayList();
    result.add(new SelectItem("S", bundle.getString("attendants_yes")));
    result.add(new SelectItem("N", bundle.getString("attendants_no")));
    result.add(new SelectItem("J", bundle.getString("attendants_ea")));
    return result;
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

  public void edit(AttendantView row)
  {
    if (row != null)
    {
      try
      {
        editing = AgendaModuleBean.getClient(false).
          loadAttendant(row.getAttendantId());
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
  public void load() throws Exception
  {
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        AttendantFilter filter = new AttendantFilter();
        filter.setEventId(eventObjectBean.getObjectId());
        List<AttendantView> auxList = AgendaModuleBean.getClient(false).
          findAttendantViewsFromCache(filter);
        String typeId = getTabBaseTypeId();
        if (typeId == null)
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
  }

  public void create()
  {
    editing = new Attendant();
    editing.setAttendantTypeId(getCreationTypeId());
  }

  public void switchView()
  {
    getCurrentTabInstance().groupedView = !getCurrentTabInstance().groupedView;
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
        AgendaModuleBean.getClient(false).storeAttendant(editing);
        refreshHiddenTabInstances();
        load();
        editing = null;
        info("STORE_OBJECT");
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
        AgendaModuleBean.getClient(false).removeAttendant(row.getAttendantId());
        refreshHiddenTabInstances();
        load();
        info("REMOVE_OBJECT");
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
  public void clear()
  {
    tabInstances.clear();
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
