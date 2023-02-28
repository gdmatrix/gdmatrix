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
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.agenda.Attendant;
import org.matrix.agenda.AttendantFilter;
import org.matrix.agenda.AttendantView;
import org.matrix.dic.DictionaryConstants;
import org.primefaces.PrimeFaces;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.modules.kernel.PersonObjectBean;
import org.santfeliu.webapp.setup.EditTab;

/**
 *
 * @author lopezrj-sf
 */
@Named
@ViewScoped
public class EventPersonsTabBean extends TabBean
{
  private Attendant editing;
  Map<String, TabInstance> tabInstances = new HashMap<>();

  public class TabInstance
  {
    String objectId = NEW_OBJECT_ID;
    String typeId = getTabBaseTypeId();
    List<AttendantView> rows;
    int firstRow = 0;
    boolean groupedView = isGroupedViewEnabled();

    private void loadTabRows()
    {
      AttendantFilter filter = new AttendantFilter();
      filter.setEventId(eventObjectBean.getObjectId());
      List<AttendantView> auxList = AgendaModuleBean.getClient(false).
        findAttendantViewsFromCache(filter);
      if (typeId == null)
      {
        rows = auxList;
      }
      else
      {
        List<AttendantView> result = new ArrayList();
        for (AttendantView item : auxList)
        {
          if (typeId.equals(item.getAttendantTypeId()))
          {
            result.add(item);
          }
        }
        rows = result;
      }
    }
  }

  @Inject
  EventObjectBean eventObjectBean;

  @Inject
  PersonObjectBean personObjectBean;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
  }

  public TabInstance getCurrentTabInstance()
  {
    EditTab tab = eventObjectBean.getActiveEditTab();
    TabInstance tabInstance = tabInstances.get(tab.getSubviewId());
    if (tabInstance == null)
    {
      tabInstance = new TabInstance();
      tabInstances.put(tab.getSubviewId(), tabInstance);
    }
    return tabInstance;
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

  //TODO Move to superclass
  public String getRootTypeId()
  {
    return DictionaryConstants.ATTENDANT_TYPE;
  }

  public Attendant getEditing()
  {
    return editing;
  }

  public void setEditing(Attendant editing)
  {
    this.editing = editing;
  }

  public List<AttendantView> getRows()
  {
    return getCurrentTabInstance().rows;
  }

  public void setRows(List<AttendantView> rows)
  {
    this.getCurrentTabInstance().rows = rows;
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
    return getCurrentTabInstance().groupedView;
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

  public String getPageObjectDescription()
  {
    if (editing != null && !isNew(editing))
    {
      return personObjectBean.getDescription(editing.getPersonId());
    }
    return null;
  }

  //TODO Use ObjectDescriptor
  public String getAttendantTypeDescription()
  {
    String typeId = null;
    AttendantView row = (AttendantView)getValue("#{row}");
    if (row != null)
    {
      typeId = row.getAttendantTypeId();
      if (typeId != null)
      {
        Type type = TypeCache.getInstance().getType(typeId);
        if (type != null) return type.getDescription();
      }
    }
    return typeId;
  }

  public String getAttendedLabel()
  {
    String attended = (String)getValue("#{row.attended}");
    if (attended == null) return "";
    else switch (attended)
    {
      case "S":
        return "SI";
      case "N":
        return "NO";
      case "J":
        return "FJ";
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

  public void setPersonId(String personId)
  {
    editing.setPersonId(personId);
    showDialog();
  }

  public String getPersonId()
  {
    return editing.getPersonId();
  }

  public String edit(AttendantView row)
  {
    String attendantId = null;
    if (row != null)
      attendantId = row.getAttendantId();

    return editPerson(attendantId);
  }

  @Override
  public void load()
  {
    load(false);
  }

  public void create()
  {
    editing = new Attendant();
  }

  public void switchView()
  {
    getCurrentTabInstance().groupedView = !getCurrentTabInstance().groupedView;
  }

  @Override
  public void store()
  {
    storePerson();
    load(true);
    editing = null;
    info("STORE_OBJECT");
  }

  public void remove(AttendantView row)
  {
    removePerson(row);
    load(true);
  }

  public String cancel()
  {
    editing = null;
    return null;
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

  private void load(boolean updateSameTypeTabs)
  {
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        if (updateSameTypeTabs)
        {
          for (TabInstance tabInstance : tabInstances.values())
          {
            tabInstance.loadTabRows();
          }
        }
        else
        {
          getCurrentTabInstance().loadTabRows();
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

  private boolean isNew(Attendant attendant)
  {
    return (attendant != null && attendant.getAttendantId() == null);
  }

  private String editPerson(String attendantId)
  {
    try
    {
      if (attendantId != null && !isEditing(attendantId))
      {
        editing =
          AgendaModuleBean.getClient(false).loadAttendantFromCache(attendantId);
      }
      else if (attendantId == null)
      {
        editing = new Attendant();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  private void storePerson()
  {
    try
    {
      if (editing != null)
      {
        //Person must be selected
        if (editing.getPersonId() == null || editing.getPersonId().isEmpty())
        {
          throw new Exception("PERSON_MUST_BE_SELECTED");
        }

        String eventId = eventObjectBean.getObjectId();
        editing.setEventId(eventId);
        AgendaModuleBean.getClient(false).storeAttendant(editing);
        editing = null;
        info("STORE_OBJECT");
        hideDialog();
      }
    }
    catch (Exception ex)
    {
      error(ex);
      showDialog();
    }
  }

  private String removePerson(AttendantView row)
  {
    try
    {
      if (row == null)
        throw new Exception("PERSON_MUST_BE_SELECTED");

      String rowAttendantId = row.getAttendantId();

      if (editing != null &&
        rowAttendantId.equals(editing.getAttendantId()))
      {
        editing = null;
      }

      AgendaModuleBean.getClient(false).removeAttendant(rowAttendantId);

      info("REMOVE_OBJECT");
      return null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  private void showDialog()
  {
    PrimeFaces current = PrimeFaces.current();
    current.executeScript("PF('eventPersonsDialog').show();");
  }

  private void hideDialog()
  {
    PrimeFaces current = PrimeFaces.current();
    current.executeScript("PF('eventPersonsDialog').hide();");
  }

  private boolean isEditing(String pageObjectId)
  {
    if (editing == null)
      return false;

    String attendantId = editing.getAttendantId();
    return attendantId != null
      && attendantId.equals(pageObjectId);
  }

  private String getTabBaseTypeId()
  {
    EditTab editTab = eventObjectBean.getActiveEditTab();
    return editTab.getProperties().getString("typeId");
  }

}
