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
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.agenda.Attendant;
import org.matrix.agenda.AttendantFilter;
import org.matrix.agenda.AttendantView;
import org.matrix.agenda.Event;
import org.matrix.agenda.EventFilter;
import org.matrix.agenda.EventPlace;
import org.matrix.agenda.EventPlaceFilter;
import org.matrix.agenda.EventPlaceView;
import org.matrix.agenda.EventTheme;
import org.matrix.agenda.EventThemeFilter;
import org.matrix.agenda.EventThemeView;
import org.matrix.agenda.EventView;
import org.matrix.dic.Property;
import org.matrix.dic.PropertyDefinition;
import org.matrix.dic.Type;
import org.matrix.dic.TypeFilter;
import org.matrix.kernel.Room;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.util.PojoUtils;
import org.santfeliu.util.TextUtils;
import org.santfeliu.webapp.NavigatorBean;
import org.santfeliu.webapp.NavigatorBean.BaseTypeInfo;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.modules.dic.DicModuleBean;
import org.santfeliu.webapp.modules.kernel.AddressTypeBean;
import org.santfeliu.webapp.modules.kernel.KernelModuleBean;
import org.santfeliu.webapp.modules.kernel.PersonTypeBean;
import org.santfeliu.webapp.modules.kernel.RoomTypeBean;

/**
 *
 * @author lopezrj-sf
 */
@Named
@RequestScoped
public class EventRecurrencesTabBean extends TabBean
{
  public static final String MASTER_EVENTID_PROPERTY = "masterEventId";
  public static final String FIRST_AVAILABLE_DATE = "00010102000000";

  private List<EventView> rows;
  private List<EventView> selectedRows;

  private int firstRow;

  private String deleteDateTime;
  private String deleteMode = "0";

  private int activeTabIndex = 0;
  private String operationMode = "FUTURE";
  private Set<String> allowedAttendantTypeIds = null;

  //Added att
  private String addedAttPersonId;
  private String addedAttTypeId;

  //Removed att
  private String removedAttPersonId;

  //Added theme
  private String addedThemeId;

  //Removed theme
  private String removedThemeId;

  //Changed place
  private String changedPlaceAddressId;
  private String changedPlaceRoomId;
  private String changedPlaceComments;

  private boolean dialogVisible = false;

  @Inject
  EventObjectBean eventObjectBean;

  @Inject
  EventCopyTabBean eventCopyTabBean;

  @Inject
  EventFinderBean eventFinderBean;

  @Inject
  EventPersonsTabBean eventPersonsTabBean;

  @Inject
  EventThemesTabBean eventThemesTabBean;

  @Inject
  EventPlacesTabBean eventPlacesTabBean;

  @Inject
  PersonTypeBean personTypeBean;

  @Inject
  ThemeTypeBean themeTypeBean;

  @Inject
  RoomTypeBean roomTypeBean;

  @Inject
  AddressTypeBean addressTypeBean;
  
  @Inject
  NavigatorBean navigatorBean;

  @PostConstruct
  public void init()
  {
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return eventObjectBean;
  }

  public List<EventView> getRows()
  {
    if (rows == null)
    {
      rows = new ArrayList();
      try
      {
        String masterEventId;
        if (!isMasterEvent()) //Is a recurring event
        {
          masterEventId = getMasterEventId();
        }
        else
          masterEventId = getEvent().getEventId();

        if (!StringUtils.isBlank(masterEventId))
        {
          //Include master event
          EventFilter filter = new EventFilter();
          filter.getEventId().add(masterEventId);
          filter.setStartDateTime(FIRST_AVAILABLE_DATE);
          filter.setReducedAttendantInfo(true);
          filter.setReducedEventPlaceInfo(true);
          List<EventView> masterEvents =
            AgendaModuleBean.getClient().findEventViewsFromCache(filter);
          if (masterEvents != null && !masterEvents.isEmpty())
          {
            rows.add(masterEvents.get(0));
          }

          //Include events with master event property
          filter = new EventFilter();
          Property p = new Property();
          p.setName(MASTER_EVENTID_PROPERTY);
          p.getValue().add(masterEventId);
          filter.getProperty().add(p);
          filter.setStartDateTime(FIRST_AVAILABLE_DATE);
          filter.setReducedAttendantInfo(true);
          filter.setReducedEventPlaceInfo(true);
          List<EventView> relatedEvents =
            AgendaModuleBean.getClient().findEventViewsFromCache(filter);
          if (relatedEvents != null)
          {
            for (EventView relatedEvent : relatedEvents)
            {
              if (!relatedEvent.getEventId().equals(masterEventId))
              {
                rows.add(relatedEvent);
              }
            }
          }
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return rows;
  }

  public void setRows(List<EventView> rows)
  {
    this.rows = rows;
  }

  public List<EventView> getSelectedRows()
  {
    return selectedRows;
  }

  public void setSelectedRows(List<EventView> selectedRows)
  {
    this.selectedRows = selectedRows;
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }

  public String getDeleteMode()
  {
    return deleteMode;
  }

  public void setDeleteMode(String deleteMode)
  {
    this.deleteMode = deleteMode;
  }

  public String getDeleteDateTime()
  {
    return deleteDateTime;
  }

  public void setDeleteDateTime(String deleteDateTime)
  {
    this.deleteDateTime = deleteDateTime;
  }

  public int getActiveTabIndex()
  {
    return activeTabIndex;
  }

  public void setActiveTabIndex(int activeTabIndex)
  {
    this.activeTabIndex = activeTabIndex;
  }

  public String getOperationMode()
  {
    return operationMode;
  }

  public void setOperationMode(String operationMode)
  {
    this.operationMode = operationMode;
  }

  public String getAddedAttPersonId()
  {
    return addedAttPersonId;
  }

  public void setAddedAttPersonId(String addedAttPersonId)
  {
    this.addedAttPersonId = addedAttPersonId;
  }

  public String getAddedAttTypeId()
  {
    return addedAttTypeId;
  }

  public void setAddedAttTypeId(String addedAttTypeId)
  {
    this.addedAttTypeId = addedAttTypeId;
  }

  public String getRemovedAttPersonId()
  {
    return removedAttPersonId;
  }

  public void setRemovedAttPersonId(String removedAttPersonId)
  {
    this.removedAttPersonId = removedAttPersonId;
  }

  public String getAddedThemeId()
  {
    return addedThemeId;
  }

  public void setAddedThemeId(String addedThemeId)
  {
    this.addedThemeId = addedThemeId;
  }

  public String getRemovedThemeId()
  {
    return removedThemeId;
  }

  public void setRemovedThemeId(String removedThemeId)
  {
    this.removedThemeId = removedThemeId;
  }

  public String getChangedPlaceAddressId()
  {
    return changedPlaceAddressId;
  }

  public void setChangedPlaceAddressId(String changedPlaceAddressId)
  {
    this.changedPlaceAddressId = changedPlaceAddressId;
    this.changedPlaceRoomId = null;
  }

  public String getChangedPlaceRoomId()
  {
    return changedPlaceRoomId;
  }

  public void setChangedPlaceRoomId(String changedPlaceRoomId)
  {
    try
    {
      this.changedPlaceRoomId = changedPlaceRoomId;
      if (!StringUtils.isBlank(changedPlaceRoomId))
      {
        Room room =
          KernelModuleBean.getPort(false).loadRoom(changedPlaceRoomId);
        String addressId = room.getAddressId();
        this.changedPlaceAddressId = addressId;
      }
      else
      {
        this.changedPlaceAddressId = null;
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public String getChangedPlaceComments()
  {
    return changedPlaceComments;
  }

  public void setChangedPlaceComments(String changedPlaceComments)
  {
    this.changedPlaceComments = changedPlaceComments;
  }

  public void onClearChangedPlaceAddress()
  {
    setChangedPlaceAddressId(null);
  }

  public void onClearChangedPlaceRoom()
  {
    setChangedPlaceRoomId(null);
  }

  public List<SelectItem> getAllowedAttendantTypes()
  {
    List<SelectItem> result = new ArrayList<>();
    Set<String> typeIds = getAllowedAttendantTypeIds();
    for (String typeId : typeIds)
    {
      Type type = TypeCache.getInstance().getType(typeId);
      if (type != null)
      {
        String value = type.getTypeId();
        String label = type.getDescription();
        SelectItem selectItem = new SelectItem(value, label);
        result.add(selectItem);
      }
    }
    sortSelectItems(result);
    return result;
  }

  public List<SelectItem> getExistingAttendants()
  {
    List<SelectItem> result = new ArrayList<>();
    Set<String> personIds = getExistingAttendantPersonIds();
    for (String personId : personIds)
    {
      String value = personId;
      String label = personTypeBean.getDescription(personId);
      SelectItem selectItem = new SelectItem(value, label);
      result.add(selectItem);
    }
    sortSelectItems(result);
    return result;
  }

  public List<SelectItem> getExistingThemes()
  {
    List<SelectItem> result = new ArrayList<>();
    Set<String> themeIds = getExistingThemeIds();
    for (String themeId : themeIds)
    {
      String value = themeId;
      String label = themeTypeBean.getDescription(themeId);
      SelectItem selectItem = new SelectItem(value, label);
      result.add(selectItem);
    }
    sortSelectItems(result);
    return result;
  }

  @Override
  public boolean isDialogVisible()
  {
    return dialogVisible;
  }

  public void openCopyDialog()
  {
    eventCopyTabBean.reset();
    dialogVisible = true;
  }

  public void closeCopyDialog()
  {
    eventCopyTabBean.reset();
    dialogVisible = false;
  }

  public void addAttendant()
  {
    if (addedAttPersonId == null)
    {
      error("PERSON_MUST_BE_SELECTED");
      return;
    }
    int count = 0;
    List<String> eventIdList = getAddAttendantEventIds(addedAttPersonId);
    for (String eventId : eventIdList)
    {
      try
      {
        Attendant editing = new Attendant();
        editing.setEventId(eventId);
        editing.setPersonId(addedAttPersonId);
        editing.setAttendantTypeId(addedAttTypeId);
        AgendaModuleBean.getClient().storeAttendant(editing);
        count++;
      }
      catch (Exception ex)
      {
      }
    }
    eventPersonsTabBean.clear();
    rows = null;
    growl("ADDED_ATTENDANTS", new Object[]{count});
  }

  public String getAddAttendantMessage()
  {
    if (addedAttPersonId == null)
    {
      return getBundleValue(getAgendaBundlePath(),
        "eventRecurrences_noPersonSelected");
    }
    else
    {
      int changes = getAddAttendantEventIds(addedAttPersonId).size();
      if (changes == 0)
      {
        return getBundleValue(getAgendaBundlePath(),
          "eventRecurrences_noChangesToBeMade");
      }
      else
      {
        return getBundleValue(getAgendaBundlePath(),
          "eventRecurrences_confirmAddAttendant", String.valueOf(changes));
      }
    }
  }

  public boolean isAddAttendantAvailable()
  {
    return (addedAttPersonId != null &&
      !getAddAttendantEventIds(addedAttPersonId).isEmpty());
  }

  public void removeAttendant()
  {
    int count = 0;
    List<String> eventIdList = getRemoveAttendantEventIds(removedAttPersonId);
    for (String eventId : eventIdList)
    {
      try
      {
        if (!StringUtils.isBlank(eventId) &&
          !StringUtils.isBlank(removedAttPersonId))
        {
          AttendantFilter attFilter = new AttendantFilter();
          attFilter.setEventId(eventId);
          attFilter.setPersonId(removedAttPersonId);
          List<Attendant> attList = AgendaModuleBean.getClient().
            findAttendantsFromCache(attFilter);
          for (Attendant att : attList)
          {
            AgendaModuleBean.getClient().removeAttendant(att.getAttendantId());
            count++;
          }
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    eventPersonsTabBean.clear();
    rows = null;
    if (getExistingAttendants().isEmpty())
    {
      activeTabIndex = 0;
    }
    growl("REMOVED_ATTENDANTS", new Object[]{count});
  }

  public String getRemoveAttendantMessage()
  {
    if (removedAttPersonId == null)
    {
      return getBundleValue(getAgendaBundlePath(),
        "eventRecurrences_noPersonSelected");
    }
    else
    {
      int changes = getRemoveAttendantEventIds(removedAttPersonId).size();
      if (changes == 0)
      {
        return getBundleValue(getAgendaBundlePath(),
          "eventRecurrences_noChangesToBeMade");
      }
      else
      {
        return getBundleValue(getAgendaBundlePath(),
          "eventRecurrences_confirmRemoveAttendant", String.valueOf(changes));
      }
    }
  }

  public boolean isRemoveAttendantAvailable()
  {
    return (removedAttPersonId != null &&
      !getRemoveAttendantEventIds(removedAttPersonId).isEmpty());
  }

  public void addTheme()
  {
    if (addedThemeId == null)
    {
      error("THEME_MUST_BE_SELECTED");
      return;
    }
    int count = 0;
    List<String> eventIdList = getAddThemeEventIds(addedThemeId);
    for (String eventId : eventIdList)
    {
      try
      {
        EventTheme editing = new EventTheme();
        editing.setEventId(eventId);
        editing.setThemeId(addedThemeId);
        AgendaModuleBean.getClient().storeEventTheme(editing);
        count++;
      }
      catch (Exception ex)
      {
      }
    }
    eventThemesTabBean.clear();
    rows = null;
    growl("ADDED_THEMES", new Object[]{count});
  }

  public String getAddThemeMessage()
  {
    if (addedThemeId == null)
    {
      return getBundleValue(getAgendaBundlePath(),
        "eventRecurrences_noThemeSelected");
    }
    else
    {
      int changes = getAddThemeEventIds(addedThemeId).size();
      if (changes == 0)
      {
        return getBundleValue(getAgendaBundlePath(),
          "eventRecurrences_noChangesToBeMade");
      }
      else
      {
        return getBundleValue(getAgendaBundlePath(),
          "eventRecurrences_confirmAddTheme", String.valueOf(changes));
      }
    }
  }

  public boolean isAddThemeAvailable()
  {
    return (addedThemeId != null &&
      !getAddThemeEventIds(addedThemeId).isEmpty());
  }
  
  public void removeTheme()
  {
    int count = 0;
    List<String> eventIdList = getRemoveThemeEventIds(removedThemeId);
    for (String eventId : eventIdList)
    {
      try
      {
        if (!StringUtils.isBlank(eventId) &&
          !StringUtils.isBlank(removedThemeId))
        {
          EventThemeFilter eventThemeFilter = new EventThemeFilter();
          eventThemeFilter.setEventId(eventId);
          eventThemeFilter.setThemeId(removedThemeId);
          List<EventThemeView> eventThemeList = AgendaModuleBean.getClient().
            findEventThemeViewsFromCache(eventThemeFilter);
          for (EventThemeView eventTheme : eventThemeList)
          {
            AgendaModuleBean.getClient().removeEventTheme(
              eventTheme.getEventThemeId());
            count++;
          }
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    eventThemesTabBean.clear();
    rows = null;
    if (getExistingThemes().isEmpty())
    {
      activeTabIndex = 0;
    }
    growl("REMOVED_THEMES", new Object[]{count});
  }

  public String getRemoveThemeMessage()
  {
    if (removedThemeId == null)
    {
      return getBundleValue(getAgendaBundlePath(),
        "eventRecurrences_noThemeSelected");
    }
    else
    {
      int changes = getRemoveThemeEventIds(removedThemeId).size();
      if (changes == 0)
      {
        return getBundleValue(getAgendaBundlePath(),
          "eventRecurrences_noChangesToBeMade");
      }
      else
      {
        return getBundleValue(getAgendaBundlePath(),
          "eventRecurrences_confirmRemoveTheme", String.valueOf(changes));
      }
    }
  }

  public boolean isRemoveThemeAvailable()
  {
    return (removedThemeId != null &&
      !getRemoveThemeEventIds(removedThemeId).isEmpty());
  }

  public void changePlace()
  {
    int changedCount = 0;
    int errorCount = 0;
    List<String> eventIdList = getChangePlaceEventIds();
    for (String eventId : eventIdList)
    {
      try
      {
        if (!StringUtils.isBlank(eventId))
        {
          EventPlaceFilter filter = new EventPlaceFilter();
          filter.setEventId(eventId);
          List<EventPlace> oldEventPlaces =
            AgendaModuleBean.getClient().findEventPlacesFromCache(filter);
          //Add new place
          EventPlace eventPlace = new EventPlace();
          eventPlace.setEventId(eventId);
          eventPlace.setAddressId(changedPlaceAddressId);
          eventPlace.setRoomId(changedPlaceRoomId);
          eventPlace.setComments(changedPlaceComments);
          eventPlace.setEventPlaceTypeId(null);
          AgendaModuleBean.getClient().storeEventPlace(eventPlace);
          //Remove old places
          for (EventPlace oldEventPlace : oldEventPlaces)
          {
            AgendaModuleBean.getClient().removeEventPlace(
              oldEventPlace.getEventPlaceId());
          }
          changedCount++;
        }
      }
      catch (Exception ex)
      {
        error(ex);
        errorCount++;
      }
    }
    eventPlacesTabBean.clear();
    rows = null;
    growl("EVENTS_PLACE_MODIFIED", new Object[]{changedCount});
    if (errorCount > 0)
    {
      growl("EVENTS_PLACE_NOT_MODIFIED", new Object[]{errorCount});
    }
  }
  
  public String getChangePlaceMessage()
  {
    if (StringUtils.isBlank(changedPlaceAddressId) &&
      StringUtils.isBlank(changedPlaceRoomId) &&
      StringUtils.isBlank(changedPlaceComments))
    {
      return getBundleValue(getAgendaBundlePath(),
        "eventRecurrences_noValidPlaceSelected");
    }
    else
    {
      int changes = getChangePlaceEventIds().size();
      if (changes == 0)
      {
        return getBundleValue(getAgendaBundlePath(),
          "eventRecurrences_noChangesToBeMade");
      }
      else
      {
        return getBundleValue(getAgendaBundlePath(),
          "eventRecurrences_confirmChangePlace", String.valueOf(changes));
      }
    }
  }

  public boolean isChangePlaceAvailable()
  {
    return (
      (
        !StringUtils.isBlank(changedPlaceAddressId) ||
        !StringUtils.isBlank(changedPlaceRoomId) ||
        !StringUtils.isBlank(changedPlaceComments)
      ) &&
      !getChangePlaceEventIds().isEmpty()
    );
  }

  public Date getStartDateTime()
  {
    EventView row = (EventView)getValue("#{row}");
    if (row != null)
    {
      String startDateTime = row.getStartDateTime();
      try
      {
        return TextUtils.parseInternalDate(startDateTime);
      }
      catch (Exception ex)
      {
      }
    }
    return null;
  }

  public Date getEndDateTime()
  {
    EventView row = (EventView)getValue("#{row}");
    if (row != null)
    {
      String endDateTime = row.getEndDateTime();
      try
      {
        return TextUtils.parseInternalDate(endDateTime);
      }
      catch (Exception ex)
      {
      }
    }
    return null;
  }

  public String getEventPlace()
  {
    String roomDescription = null;
    EventView row = (EventView)getValue("#{row}");
    if (row != null)
    {
      if (!row.getPlaces().isEmpty())
      {
        EventPlaceView eventPlace = row.getPlaces().get(0);
        if (eventPlace.getRoomView() != null)
        {
          roomDescription =
            roomTypeBean.getDescription(
              eventPlace.getRoomView().getRoomId());
        }
        else
        {
          if (eventPlace.getAddressView() != null)
          {
            roomDescription =
              addressTypeBean.getDescription(
                eventPlace.getAddressView().getAddressId());
          }
          else
          {
            roomDescription = eventPlace.getComments();
          }
        }
      }
    }
    return roomDescription;
  }

  public void deleteRecurrences()
  {
    try
    {
      int deleteCount = 0;
      if (deleteMode.equals("2"))
      {
        deleteCount = deleteSelectedRecurrences();
      }
      else if (deleteMode.equals("1"))
      {
        deleteCount = deleteAllRecurrences();
      }
      else if (deleteMode.equals("0"))
      {
        if (deleteDateTime == null)
          error("INVALID_DATE");
        else
          deleteCount = deleteFutureRecurrences(deleteDateTime);
      }
      if (deleteCount > 0)
      {
        eventFinderBean.outdate();
        growl("RECURRENCES_DELETED", new Object[]{deleteCount});
      }
      else
        growl("RECURRENCES_NOT_DELETED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
    load();
  }

  @Override
  public void load()
  {
    try
    {
      if (!isNew())
      {
        rows = null; //reload
        selectedRows = new ArrayList();

        //Start a new recurrences schedule
        newSchedule();

        if (deleteDateTime == null)
          deleteDateTime = getEvent().getStartDateTime();

        resetOperationsPanel();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void newSchedule()
  {
    Event event = getEvent();
    if (eventCopyTabBean == null)
      eventCopyTabBean = new EventCopyTabBean();
    else
      eventCopyTabBean.reset();
    eventCopyTabBean.setEvent(event);
    eventCopyTabBean.setMasterEventId(getMasterEventId());
  }

  public void copyRecurrences()
  {
    try
    {
      if (eventCopyTabBean != null)
        eventCopyTabBean.copy();
      load();
      eventFinderBean.outdate();
      dialogVisible = false;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ activeTabIndex, addedAttPersonId, addedAttTypeId,
      operationMode, changedPlaceAddressId, changedPlaceRoomId,
      changedPlaceComments, removedAttPersonId, addedThemeId, removedThemeId,
      allowedAttendantTypeIds };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      load();
      Object[] stateArray = (Object[])state;
      activeTabIndex = (int)stateArray[0];
      addedAttPersonId = (String)stateArray[1];
      addedAttTypeId = (String)stateArray[2];
      operationMode = (String)stateArray[3];
      changedPlaceAddressId = (String)stateArray[4];
      changedPlaceRoomId = (String)stateArray[5];
      changedPlaceComments = (String)stateArray[6];
      removedAttPersonId = (String)stateArray[7];
      addedThemeId = (String)stateArray[8];
      removedThemeId = (String)stateArray[9];
      allowedAttendantTypeIds = (Set<String>)stateArray[10];
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private void resetOperationsPanel()
  {
    addedAttPersonId = null;
    addedAttTypeId = null;
    removedAttPersonId = null;
    addedThemeId = null;
    removedThemeId = null;
    changedPlaceAddressId = null;
    changedPlaceRoomId = null;
    changedPlaceComments = null;
    activeTabIndex = 0;
    operationMode = "FUTURE";
    allowedAttendantTypeIds = null;
  }

  private Set<String> getAllowedAttendantTypeIds()
  {
    if (allowedAttendantTypeIds == null)
    {
      allowedAttendantTypeIds = new HashSet<>();
      Set<String> baseTypeIds = new HashSet<>();
      for (EditTab editTab : getObjectBean().getEditTabs())
      {
        if ("eventPersonsTabBean".equals(editTab.getBeanName()))
        {
          String tabBaseTypeId = getTabBaseTypeId(editTab);
          if (tabBaseTypeId == null ||
            tabBaseTypeId.equals("Attendant") ||
            tabBaseTypeId.equals("sf:Attendant")) //only root type allowed
          {
            org.santfeliu.dic.Type dicType =
              TypeCache.getInstance().getType("sf:Attendant");
            if (dicType != null)
            {
              allowedAttendantTypeIds.add(dicType.getTypeId());
              return allowedAttendantTypeIds;
            }
          }
          else
          {
            baseTypeIds.add(tabBaseTypeId);
          }
        }
      }
      for (String baseTypeId : baseTypeIds)
      {
        TypeFilter typeFilter = new TypeFilter();
        typeFilter.setTypePath("%/" + baseTypeId + "/%");
        try
        {
          List<Type> typeList =
            DicModuleBean.getPort(true).findTypes(typeFilter);
          for (Type type : typeList)
          {
            if (type.isInstantiable())
            {
              allowedAttendantTypeIds.add(type.getTypeId());
            }
          }
        }
        catch (Exception ex) { }
      }
    }
    return allowedAttendantTypeIds;
  }

  private Set<String> getExistingAttendantPersonIds()
  {
    Set<String> existingAttendantPersonIds = new HashSet<>();
    for (EventView eventView : getRows())
    {
      for (AttendantView att : eventView.getAttendants())
      {
        existingAttendantPersonIds.add(att.getPersonId());
      }
    }
    return existingAttendantPersonIds;
  }

  private Set<String> getExistingThemeIds()
  {
    Set<String> existingThemeIds = new HashSet<>();
    for (EventView eventView : getRows())
    {
      for (EventThemeView eventThemeView : eventView.getThemes())
      {
        existingThemeIds.add(eventThemeView.getThemeId());
      }
    }
    return existingThemeIds;
  }

  private String getTabBaseTypeId(EditTab editTab)
  {
    String tabBaseTypeId = editTab.getBaseTypeId();
    if (tabBaseTypeId == null)
    {
      Event event = eventObjectBean.getObject();
      if (event != null)
      {
        String typeId = event.getEventTypeId();
        if (typeId != null)
        {
          org.santfeliu.dic.Type type = TypeCache.getInstance().getType(typeId);
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

  private void sortSelectItems(List<SelectItem> selectItems)
  {
    Collections.sort(selectItems, new Comparator<SelectItem>()
      {
        @Override
        public int compare(SelectItem o1, SelectItem o2)
        {
          return o1.getLabel().compareTo(o2.getLabel());
        }
      }
    );
  }

  private int deleteFutureRecurrences(String deleteDateTime)
    throws Exception
  {
    int deleteCount = 0;
    if (rows != null && !rows.isEmpty())
    {
      deleteCount = deleteRecurrences(rows, deleteDateTime);
    }
    return deleteCount;
  }

  private int deleteAllRecurrences()
    throws Exception
  {
    int deleteCount = 0;
    if (rows != null && !rows.isEmpty())
    {
      deleteCount = deleteRecurrences(rows);

      //if current event isn't the master then change it to master
      if (!isMasterEvent())
      {
        Event event = getEvent();
        DictionaryUtils.setProperty(event, MASTER_EVENTID_PROPERTY,
          event.getEventId());
        AgendaModuleBean.getClient().storeEvent(event);
      }
    }
    return deleteCount;
  }

  private int deleteSelectedRecurrences() throws Exception
  {
    int deleteCount = 0;
    if (selectedRows != null && !selectedRows.isEmpty())
    {
      deleteCount = deleteRecurrences(selectedRows);
    }
    return deleteCount;
  }

  private int deleteRecurrences(List<EventView> events)
    throws Exception
  {
    return deleteRecurrences(events, null);
  }

  private int deleteRecurrences(List<EventView> events, String sdt)
    throws Exception
  {
    int deleteCount = 0;
    BaseTypeInfo baseTypeInfo = navigatorBean.getBaseTypeInfo();

    for (EventView event : events)
    {
      if (!event.getEventId().equals(getObjectId()))
      {
        boolean delete = (sdt == null);
        if (sdt != null)
        {
          Date eventDate =
            TextUtils.parseInternalDate(event.getStartDateTime());
          Date startDate = TextUtils.parseInternalDate(sdt);
          delete = startDate.before(eventDate);
        }

        if (delete)
        {
          AgendaModuleBean.getClient().removeEvent(event.getEventId());
          deleteCount++;
          if (baseTypeInfo != null) baseTypeInfo.remove(event.getEventId());
        }
      }
    }
    return deleteCount;
  }

  private boolean isMasterEvent()
  {
    Event event = getEvent();
    if (event != null)
    {
      List<String> masterEventValue;
      try
      {
        masterEventValue =
          (List<String>) PojoUtils.getDynamicProperty(event.getProperty(),
            MASTER_EVENTID_PROPERTY);
          if (masterEventValue != null)
            return masterEventValue.get(0).equals(event.getEventId());
      }
      catch (Exception ex)
      {
      }
      return true;
    }
    else
      return false;
  }

  private Event getEvent()
  {
    return eventObjectBean.getEvent();
  }

  private String getMasterEventId()
  {
    Event event = getEvent();
    if (event != null)
    {
      List<String> masterEventValue;
      try
      {
        masterEventValue =
          (List<String>) PojoUtils.getDynamicProperty(event.getProperty(),
            MASTER_EVENTID_PROPERTY);
          if (masterEventValue != null)
            return masterEventValue.get(0);
      }
      catch (Exception ex)
      {
      }

      return event.getEventId();
    }
    return null;
  }

  private List<EventView> getTargetRows()
  {
    List<EventView> result = new ArrayList<>();
    if ("ALL".equals(getOperationMode()))
    {
      result.addAll(getRows());
    }
    else if ("SELECTED".equals(getOperationMode()))
    {
      result.addAll(getSelectedRows());
    }
    else //FUTURE
    {
      DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
      LocalDateTime currentEventDateTime =
        LocalDateTime.parse(getEvent().getStartDateTime(), formatter);
      for (EventView eventView : getRows())
      {
        LocalDateTime eventDateTime =
          LocalDateTime.parse(eventView.getStartDateTime(), formatter);
        if (!eventDateTime.isBefore(currentEventDateTime))
        {
          result.add(eventView);
        }
      }
    }
    return result;
  }

  private List<String> getAddAttendantEventIds(String personId)
  {
    List<String> result = new ArrayList<>();
    List<EventView> targetRows = getTargetRows();
    for (EventView eventView : targetRows)
    {
      if (!isPersonInEvent(eventView, personId))
      {
        result.add(eventView.getEventId());
      }
    }
    return result;
  }

  private List<String> getRemoveAttendantEventIds(String personId)
  {
    List<String> result = new ArrayList<>();
    List<EventView> targetRows = getTargetRows();
    for (EventView eventView : targetRows)
    {
      if (isPersonInEvent(eventView, personId))
      {
        result.add(eventView.getEventId());
      }
    }
    return result;
  }

  private List<String> getAddThemeEventIds(String themeId)
  {
    List<String> result = new ArrayList<>();
    List<EventView> targetRows = getTargetRows();
    for (EventView eventView : targetRows)
    {
      if (!isThemeInEvent(eventView, themeId))
      {
        result.add(eventView.getEventId());
      }
    }
    return result;
  }

  private List<String> getRemoveThemeEventIds(String themeId)
  {
    List<String> result = new ArrayList<>();
    List<EventView> targetRows = getTargetRows();
    for (EventView eventView : targetRows)
    {
      if (isThemeInEvent(eventView, themeId))
      {
        result.add(eventView.getEventId());
      }
    }
    return result;
  }

  private List<String> getChangePlaceEventIds()
  {
    List<String> result = new ArrayList<>();
    List<EventView> targetRows = getTargetRows();
    for (EventView eventView : targetRows)
    {
      if (!isPlaceInEvent(eventView, changedPlaceRoomId, changedPlaceAddressId,
        changedPlaceComments))
      {
        result.add(eventView.getEventId());
      }
    }
    return result;
  }

  private boolean isPersonInEvent(EventView eventView, String personId)
  {
    for (AttendantView att : eventView.getAttendants())
    {
      if (att.getPersonId().equals(personId))
      {
        return true;
      }
    }
    return false;
  }

  private boolean isThemeInEvent(EventView eventView, String themeId)
  {
    for (EventThemeView theme : eventView.getThemes())
    {
      if (theme.getThemeId().equals(themeId))
      {
        return true;
      }
    }
    return false;
  }

  private boolean isPlaceInEvent(EventView eventView, String roomId,
    String addressId, String comments)
  {
    String inputHash;
    if (roomId != null)
    {
      inputHash = "room:" + roomId;
    }
    else if (addressId != null)
    {
      inputHash = "address:" + addressId;
    }
    else 
    {
      inputHash = "comments:" + StringUtils.defaultString(comments);
    }
    for (EventPlaceView place : eventView.getPlaces())
    {
      String placeHash;
      if (place.getRoomView() != null)
      {
        placeHash = "room:" +
          StringUtils.defaultString(place.getRoomView().getRoomId());
      }
      else if (place.getAddressView() != null)
      {
        placeHash = "address:" +
          StringUtils.defaultString(place.getAddressView().getAddressId());
      }
      else
      {
        placeHash = "comments:" +
          StringUtils.defaultString(place.getComments());
      }
      if (inputHash.equals(placeHash))
      {
        return true;
      }
    }
    return false;
  }

  private String getAgendaBundlePath()
  {
    return "org.santfeliu.agenda.web.resources.AgendaBundle";
  }

  private String getBundleValue(String bundlePath, String key)
  {
    return getBundleValue(bundlePath, key, null);
  }
  
  private String getBundleValue(String bundlePath, String key,
    String paramValue)
  {
    try
    {
      ResourceBundle bundle = ResourceBundle.getBundle(bundlePath, getLocale());
      String pattern = bundle.getString(key);
      if (pattern != null)
      {
        return MessageFormat.format(pattern, paramValue);
      }
    }
    catch (Exception ex)
    {
    }
    return null;
  }

}
