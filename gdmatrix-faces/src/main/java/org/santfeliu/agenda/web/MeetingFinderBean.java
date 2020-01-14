package org.santfeliu.agenda.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import javax.faces.model.SelectItem;
import javax.mail.MessagingException;
import org.apache.commons.lang.StringUtils;
import org.matrix.agenda.AgendaConstants;
import org.matrix.agenda.AgendaManagerPort;
import org.matrix.agenda.Event;
import org.matrix.agenda.EventFilter;
import org.matrix.agenda.EventPlace;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.ContactFilter;
import org.matrix.kernel.ContactView;
import org.matrix.kernel.KernelManagerPort;
import org.matrix.presence.Absence;
import org.matrix.presence.AbsenceFilter;
import org.matrix.presence.PresenceManagerPort;
import org.santfeliu.kernel.web.KernelConfigBean;
import org.santfeliu.web.obj.PageBean;
import org.santfeliu.kernel.web.PersonBean;
import org.santfeliu.kernel.web.RoomBean;
import org.santfeliu.presence.web.PresenceConfigBean;
import org.santfeliu.util.MailSender;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserPreferences;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;
import org.santfeliu.web.obj.ObjectBean;
import org.santfeliu.web.obj.ObjectDescriptionCache;

/**
 *
 * @author blanquepa
 */
@CMSManagedBean
public class MeetingFinderBean extends PageBean implements Serializable
{
  @CMSProperty  
  private static final String DEFAULT_RANGE_START_TIME_PROPERTY = "defaultRangeStartTime"; //HHmm format
  @CMSProperty    
  private static final String DEFAULT_RANGE_END_TIME_PROPERTY = "defaultRangeEndTime"; //HHmm format
  @CMSProperty    
  private static final String DEFAULT_RANGE_START_TIME = "0000";
  @CMSProperty    
  private static final String DEFAULT_RANGE_END_TIME = "2359";
  @CMSProperty    
  private static final String DEFAULT_EVENT_TITLE_PROPERTY= "defaultEventTitle";  
  @CMSProperty
  private static final String MAIL_SENDER = "mailSender";
  @CMSProperty
  private static final String MAIL_DOMAINS = "mailDomains";
  @CMSProperty
  private static final String MAIL_CONTACT_TYPEID = "g5:TCTCON_EMAI";  
          
  private static final long DEFAULT_DURATION = 60; //In minutes
  private static final String DEFAULT_TIME_SELECTOR = "m";
  private static final long DEFAULT_STEP_TIME = 15; //In minutes  
  
  private boolean attPanelCollapsed = false; 
  private boolean periodPanelCollapsed = false;
  private boolean roomPanelCollapsed = true;  
  
  private String mailSender;
  private List<String> mailDomains;
  
  private List<Attendant> attendants = new ArrayList<Attendant>();
  private Attendant selectedAttendant;
  private List<ScheduleSlot> freeSlots;
  private ScheduleSlot editingSlot;
  private HashMap<String, Room> rooms = new HashMap<String, Room>();
  
  private RangeDateTimeData dtData;
  private String rangeStartDateTime;
  private String rangeEndDateTime;
  
  private String duration;
  private String durationSelector;
  private String steps;
  private String timePadding;
  private static final String MINUTES_SELECTOR = "m";
  private static final String HOURS_SELECTOR = "h";
  private Room selectedRoom;
  private int firstRowIndex;
  
  public MeetingFinderBean()
  {
  }

  public boolean isAttPanelCollapsed()
  {
    return attPanelCollapsed;
  }

  public void setAttPanelCollapsed(boolean attPanelCollapsed)
  {
    this.attPanelCollapsed = attPanelCollapsed;
  }

  public boolean isRoomPanelCollapsed()
  {
    return roomPanelCollapsed;
  }

  public void setRoomPanelCollapsed(boolean roomPanelCollapsed)
  {
    this.roomPanelCollapsed = roomPanelCollapsed;
  }

  public boolean isPeriodPanelCollapsed()
  {
    return periodPanelCollapsed;
  }

  public void setPeriodPanelCollapsed(boolean periodPanelCollapsed)
  {
    this.periodPanelCollapsed = periodPanelCollapsed;
  }

  public List<Attendant> getAttendants()
  {
    return attendants;
  }

  public void setAttendants(List<Attendant> attendants)
  {
    this.attendants = attendants;
  }

  public List<ScheduleSlot> getFreeSlots()
  {
    return freeSlots;
  }

  public void setFreeSlots(List<ScheduleSlot> freeSlots)
  {
    this.freeSlots = freeSlots;
  }

  public List<SelectItem> getPersonSelectItems()
  {
    PersonBean personBean = (PersonBean)getBean("personBean");
    return personBean.getSelectItems();
  }  

  public Attendant getSelectedAttendant()
  {
    if (this.selectedAttendant == null)
      this.selectedAttendant = new Attendant();
    return selectedAttendant;
  }
  
  public void setSelectedAttendant(Attendant attendant)
  {
    this.selectedAttendant = attendant;
  }

  public RangeDateTimeData getDateTimeData()
  {
    return dtData;
  }

  public void setDateTimeData(RangeDateTimeData dtData)
  {
    this.dtData = dtData;
  }
  
  public String getRangeEndDateTime()
  {
    return rangeEndDateTime;
  }

  public void setRangeEndDateTime(String rangeEndDateTime)
  {
    if (StringUtils.isBlank(rangeEndDateTime))
    {
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.DATE, 7);
      this.rangeEndDateTime = 
        TextUtils.formatDate(calendar.getTime(),  "yyyyMMdd") + DEFAULT_RANGE_END_TIME + "59";    
    }
    else
      this.rangeEndDateTime = 
        rangeEndDateTime.substring(0, 8) + DEFAULT_RANGE_END_TIME + "59";
  }

  public String getRangeStartDateTime()
  {
    return rangeStartDateTime;
  }

  public void setRangeStartDateTime(String rangeStartDateTime)
  {
    if (StringUtils.isBlank(rangeStartDateTime))
    {
      Calendar calendar = Calendar.getInstance();
      this.rangeStartDateTime = 
        TextUtils.formatDate(calendar.getTime(), "yyyyMMdd") + DEFAULT_RANGE_START_TIME + "00";
    }
    else
      this.rangeStartDateTime = rangeStartDateTime;
  }  
  
  public String getStartHour()
  {
    return dtData.getFormattedStartHour();
  }

  public void setStartHour(String startHour)
  {
    this.dtData.setFormattedStartHour(startHour);
  }

  public String getEndHour()
  {
    return dtData.getFormattedEndHour();
  }

  public void setEndHour(String endHour)
  {
    this.dtData.setFormattedEndHour(endHour);
  }  

  public String getDuration()
  {
    return this.duration;
  }

  public void setDuration(String duration)
  {
    if (StringUtils.isBlank(duration))
      this.duration = String.valueOf(DEFAULT_DURATION);
    else
      this.duration = duration;
  }

  public String getDurationSelector()
  {
    return durationSelector;
  }

  public void setDurationSelector(String durationSelector)
  {
    if (StringUtils.isBlank(duration))
      this.durationSelector = DEFAULT_TIME_SELECTOR;
    else    
      this.durationSelector = durationSelector;
  }

  public String getSteps()
  {
    return steps;
  }

  public void setSteps(String steps)
  {
    if (StringUtils.isBlank(steps))
      this.steps = String.valueOf(DEFAULT_STEP_TIME);
    else       
      this.steps = steps;
  }

  public String getTimePadding()
  {
    return timePadding;
  }

  public void setTimePadding(String timePadding)
  {
    if (StringUtils.isBlank(timePadding))
      this.timePadding = String.valueOf(0);
    else 
    {      
      this.timePadding = timePadding;   
      dtData.setTimePadding(timePadding);
    }
  }

  public boolean isCreatingEvent()
  {
    return this.editingSlot != null;
  }

  public ScheduleSlot getEditingSlot()
  {
    return editingSlot;
  }

  public void setEditingSlot(ScheduleSlot editingSlot)
  {
    this.editingSlot = editingSlot;
  }

  public String getSelectedRoom()
  {
    if (this.selectedRoom != null)
      return selectedRoom.getRoomId();
    else
      return null;
  }

  public void setSelectedRoom(String selectedRoom)
  {
    this.selectedRoom = this.rooms.get(selectedRoom);
  }
  
  public int getFirstRowIndex()
  {
    int size = freeSlots.size();
    if (size == 0)
    {
      firstRowIndex = 0;
    }
    else if (firstRowIndex >= size)
    {
      int pageSize = getPageSize();
      firstRowIndex = pageSize * ((size - 1) / pageSize);
    }
    return firstRowIndex;
  }

  public void setFirstRowIndex(int firstRowIndex)
  {
    this.firstRowIndex = firstRowIndex;
  }  

  public Collection<Room> getRooms() 
  {
    if (rooms == null || rooms.isEmpty())
    {
      UserPreferences userPreferences = 
        UserSessionBean.getCurrentInstance().getUserPreferences();
      RoomBean roomBean = (RoomBean)getBean("roomBean");
      try
      {
        List<String> favRoomIds = 
          userPreferences.getPreferences(roomBean.getObjectTypeId());
        for (String favRoomId : favRoomIds)
        {
          Room roomView = new Room();
          roomView.setRoomId(favRoomId);
          roomView.setDescription(roomBean.getDescription(favRoomId));
          roomView.setSelected(false);
          this.rooms.put(favRoomId, roomView);
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return rooms.values();
  }
  
  @CMSAction
  public String showFirst()
  {
    resetData();
    return show();
  }

  @CMSAction
  public String show()
  {
    selectedAttendant = new Attendant();
    
    String defaultRangeStartTime = getProperty(DEFAULT_RANGE_START_TIME_PROPERTY);
    if (defaultRangeStartTime == null)
      defaultRangeStartTime = DEFAULT_RANGE_START_TIME;
    
    String defaultRangeEndTime = getProperty(DEFAULT_RANGE_END_TIME_PROPERTY);
    if (defaultRangeEndTime == null)
      defaultRangeEndTime = DEFAULT_RANGE_END_TIME;
    
    Calendar calendar = Calendar.getInstance();
    String today = TextUtils.formatDate(calendar.getTime(), "yyyyMMdd") + defaultRangeStartTime + "00";
    calendar.add(Calendar.DATE, 7);
    String weekAgo = TextUtils.formatDate(calendar.getTime(),  "yyyyMMdd") + defaultRangeEndTime + "59";
    rangeStartDateTime = today;
    rangeEndDateTime = weekAgo;

    duration = String.valueOf(DEFAULT_DURATION);
    durationSelector = DEFAULT_TIME_SELECTOR;
    steps = String.valueOf(DEFAULT_STEP_TIME);
    dtData = 
      new RangeDateTimeData(DEFAULT_DURATION, DEFAULT_STEP_TIME, 0);    
    
    return "meeting_finder";
  }
  
  public void resetData()
  {
    selectedAttendant = null;
    dtData = null;
    freeSlots = null;
    attendants.clear();
  }
  
  public String searchPerson()
  {
    return getControllerBean().searchObject("Person",  "#{meetingFinderBean.attendantCallback}");
  }  
  
  public void setAttendantCallback(String personId)
  {
    this.selectedAttendant.setPersonId(personId);
    addAttendant(personId);
  }  
  
  public String addAttendant()
  {
    if (this.freeSlots != null) 
      this.freeSlots.clear();
    String personId = this.selectedAttendant.getPersonId();
    return addAttendant(personId);
  }  
  
  public String addAttendant(String personId)
  {
    if (!StringUtils.isBlank(personId))
    {
      ObjectBean personBean = (PersonBean)getBean("personBean");
      String fullName = 
      ObjectDescriptionCache.getInstance().getDescription(personBean, personId);
      Attendant attendant = new Attendant(personId, fullName);
      if (!this.attendants.contains(attendant))
        this.attendants.add(attendant);
    }
    return null;    
  }
  
  public String removeAttendant()
  {
    if (this.freeSlots != null) 
      this.freeSlots.clear();    
    Attendant row = (Attendant)getRequestMap().get("att");
    attendants.remove(row);
    return null;
  }
  
  public String searchRoom()
  {
    return getControllerBean().searchObject("Room",  "#{meetingFinderBean.roomCallback}");
  }  

  public void setRoomCallback(String roomId)
  {
    this.selectedAttendant.setPersonId(roomId);
    addRoom(roomId);
  }   
  
  public String addRoom(String roomId)
  {
    if (!StringUtils.isBlank(roomId))
    {
      ObjectBean roomBean = (RoomBean)getBean("roomBean");
      String description = 
      ObjectDescriptionCache.getInstance().getDescription(roomBean, roomId);
      Room room = new Room();
      room.setRoomId(roomId);
      room.setDescription(description);
      this.rooms.put(roomId, room);
    }
    return null;    
  }  
  
  public String removeRoom()
  {
    Room row = (Room)getRequestMap().get("room");
    rooms.remove(row.getRoomId());
    return null;
  }  
  
  public String findFreeSlots()
  {
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.web.resources.MessageBundle", getLocale());
    
    try
    {
      firstRowIndex = 0;       
      this.freeSlots = new LinkedList<ScheduleSlot>();
      List<Event> events = new ArrayList();
      AgendaManagerPort port = AgendaConfigBean.getPort();
      List<String> personIds = new ArrayList();              
      for (Attendant att : this.attendants)
      {
        EventFilter filter = new EventFilter();
        filter.setStartDateTime(rangeStartDateTime);
        filter.setEndDateTime(rangeEndDateTime);
        filter.setPersonId(att.getPersonId());
        filter.setDateComparator(AgendaConstants.ACTIVE_DATE_COMPARATOR);
        events.addAll(port.findEvents(filter));
        personIds.add(att.getPersonId());
      }
      
      //Set event slots (from Agenda service)
      SlotsBoard board = new SlotsBoard();
      for (Event event : events)
      {
        long startTime = TextUtils.parseInternalDate(event.getStartDateTime()).getTime() - dtData.getTimePaddingInMillis();
        long endTime = TextUtils.parseInternalDate(event.getEndDateTime()).getTime() + dtData.getTimePaddingInMillis();
        board.addBusySlot(startTime, endTime);
      }
      
      //Set absence slots (from Presence service)
      if (personIds != null && !personIds.isEmpty())
      {
        try
        {
          PresenceManagerPort pport = PresenceConfigBean.getPresencePort();
          AbsenceFilter afilter = new AbsenceFilter();
          afilter.getPersonId().addAll(personIds);
          afilter.setStartDateTime(rangeStartDateTime);
          afilter.setEndDateTime(rangeEndDateTime);
          afilter.setSplitByDay(false);
          List<Absence> absences = pport.findAbsences(afilter);
          for (Absence absence : absences)
          {
            long startTime = TextUtils.parseInternalDate(absence.getStartDateTime()).getTime();
            long endTime = TextUtils.parseInternalDate(absence.getEndDateTime()).getTime();        
            board.addBusySlot(startTime, endTime);
          }
        }
        catch (Exception ex)
        {
          warn(bundle.getString("ABSENCES_OMITTED") + ": " + ex.getMessage());
        }
      }      
      //Set board limits     
      Date dStartDate = TextUtils.parseInternalDate(rangeStartDateTime);
      Date dEndDate = TextUtils.parseInternalDate(rangeEndDateTime);

      Calendar cal = Calendar.getInstance();
      cal.setTime(dStartDate);
      while (cal.getTime().before(dEndDate))
      {
        long t1 = cal.getTimeInMillis();
        long t2 = t1 + dtData.getStartHourInMillis();
        board.addBusySlot(t1, t2);
        t1 = cal.getTimeInMillis() + dtData.getEndHourInMillis() + 1; //Add 1 millisecond to leave space for last slot
        t2 = cal.getTimeInMillis() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS) - 1;
        board.addBusySlot(t1, t2);
        cal.add(Calendar.DATE, 1);
      }
      
      //Compute free slots
      if (!StringUtils.isBlank(duration) && !StringUtils.isBlank(durationSelector))
        dtData.setDuration(duration, durationSelector);
      if (!StringUtils.isBlank(steps))
        dtData.setSteps(steps);

      long lstartTime = dStartDate.getTime();
      long lendTime = dEndDate.getTime();
      
      freeSlots = board.getFreeSlots(lstartTime, lendTime, dtData.getDurationInMillis(),
              dtData.getStepsTimeInMillis());
      
      for (Map.Entry<String, Room> roomEntry : this.rooms.entrySet())
      {
        Room room = roomEntry.getValue();
        if (room.isSelected())
        {
          EventFilter filter = new EventFilter();
          filter.setStartDateTime(rangeStartDateTime);
          filter.setEndDateTime(rangeEndDateTime);
          filter.setDateComparator(AgendaConstants.ACTIVE_DATE_COMPARATOR);
          filter.setRoomId(room.getRoomId());
          room.setSlots(port.findEvents(filter));
        }
      }
      
      //Mark free rooms
      for (ScheduleSlot freeSlot : freeSlots)
      {
        for (Map.Entry<String, Room> roomEntry : this.rooms.entrySet())
        {
          Room room = roomEntry.getValue();
          if (room.isSelected() && room.isRoomFree(freeSlot))
            freeSlot.addAvailableRoom(room);
        }
      }
      
      
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;    
  }
  
  public String createEvent()
  {
    String outcome = null;
    ScheduleSlot slot = (ScheduleSlot)getValue("#{slot}");    
    boolean creatingEvent = (this.editingSlot != null && this.editingSlot.equals(slot));
    if (!creatingEvent)
    {
      int roomCount = slot.getAvailableRooms() == null ? 0 : slot.getAvailableRooms().size();
      if (roomCount > 1)
        this.editingSlot = slot;        
      else 
      {
        Room room = (roomCount == 1 ? slot.getAvailableRooms().get(0) : null);
        outcome = doEventCreation(slot, this.attendants, room);
      }
    }
    else
    {
      outcome = doEventCreation(this.editingSlot, this.attendants, selectedRoom);
      this.editingSlot = null;            
    }
    return outcome;
  }
  
  public String cancelEvent()
  {
    this.editingSlot = null;
    return null;
  }
  
  private String doEventCreation(ScheduleSlot slot, List<Attendant> attendants, Room room)
  {
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.web.resources.MessageBundle", getLocale());

    try
    {
      Event event = new Event();
      
      String defaultTitleName = getProperty(DEFAULT_EVENT_TITLE_PROPERTY);
      if (defaultTitleName == null)
        defaultTitleName = "Default title";      
      event.setSummary(defaultTitleName);
      Date date = new Date(slot.getStartTime());  
      event.setStartDateTime(TextUtils.formatDate(date, "yyyyMMddHHmmss"));
      date = new Date(slot.getEndTime());
      event.setEndDateTime(TextUtils.formatDate(date, "yyyyMMddHHmmss"));
      event.setEventTypeId(DictionaryConstants.EVENT_TYPE);
      event.setOnlyAttendants(true);
      
      AgendaManagerPort port = AgendaConfigBean.getPort();
      event = port.storeEvent(event);
      info(bundle.getString("EVENT_CREATED"));
      
      for (Attendant att : attendants)
      {
        org.matrix.agenda.Attendant attendant = new org.matrix.agenda.Attendant();
        attendant.setPersonId(att.getPersonId());
        attendant.setEventId(event.getEventId());
        attendant.setAttendantTypeId(DictionaryConstants.ATTENDANT_TYPE);
        port.storeAttendant(attendant);      
      }
      info(bundle.getString("ATTENDANTS_ADDED"), new Object[]{attendants.size()});      
      
      if (room != null)
      {
        EventPlace eventPlace = new EventPlace();
        eventPlace.setRoomId(room.getRoomId());
        String[] pk = room.getRoomId().split(";");
        eventPlace.setAddressId(pk[0]);        
        eventPlace.setEventId(event.getEventId());
        eventPlace.setEventPlaceTypeId(DictionaryConstants.EVENT_PLACE_TYPE);
        port.storeEventPlace(eventPlace);
        info(bundle.getString("ROOM_SET"), new Object[]{room.getDescription()});                    
      }
      
      try
      {
        mailSender = getProperty(MAIL_SENDER);
        mailDomains = getSelectedMenuItem().getMultiValuedProperty(MAIL_DOMAINS);      
        if (mailSender != null && mailDomains != null && !mailDomains.isEmpty())
        {
          sendMail(event, freeSlots, attendants, room);
          info(bundle.getString("MAIL_SENT"));
        }
        else
          warn(bundle.getString("MAIL_NOT_SENT") + ": " 
          + bundle.getString("INVALID_MAIL_SETTINGS"));
      }
      catch (MessagingException mex)
      {
        warn(bundle.getString("MAIL_NOT_SENT") + ":" 
          + mex.getMessage());
      }
  
      EventMainBean eventMainBean = (EventMainBean)getBean("eventMainBean");
      eventMainBean.setEvent(event);
     
      return getControllerBean().showObject("Event", event.getEventId());
    }
    catch (Exception ex)
    {
      error(bundle.getString("UNKNOWN_ERROR"), ex.getMessage());
    }
    return null;
  }
  
  private void sendMail(Event event, List<ScheduleSlot> slots, List<Attendant> attendants, Room room)
    throws MessagingException
  {
    List<String> emails = new ArrayList();
    KernelManagerPort port = KernelConfigBean.getPort();
    for (Attendant att : attendants)
    {
      ContactFilter filter = new ContactFilter();
      filter.setPersonId(att.getPersonId());
      filter.setContactTypeId(MAIL_CONTACT_TYPEID);
      List<ContactView> contacts = port.findContactViews(filter);
      if (contacts != null)
      {
        int i = 0;
        boolean matchDomain = false;
        while(!matchDomain && i < contacts.size())
        {
          matchDomain = matchDomain(contacts.get(i).getValue());
          if (matchDomain)
            emails.add(contacts.get(i).getValue());
          i++;
        }
      }
    }

    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.web.resources.MessageBundle", getLocale());
    
    String message = encodeMessage(event, slots, attendants, room, bundle);
    String host = MatrixConfig.getProperty("mail.smtp.host");
    
    MailSender.sendHtmlMail(host, mailSender, emails.toArray(new String[0]), 
      bundle.getString("EMAIL_SUBJECT"), message, true);
  }
  
  private boolean matchDomain(String contact)
  {
    if (mailDomains == null || mailDomains.isEmpty() || contact == null)
      return false;
    boolean match = false;
    for (String domain : mailDomains)
    {
      if (contact.endsWith("@" + domain))
        return true;
    }
    return match;
  }
  
  private String encodeMessage(Event event, List<ScheduleSlot> slots, 
          List<Attendant> attendants, Room room, ResourceBundle bundle)
  {
    String user = UserSessionBean.getCurrentInstance().getDisplayName();
    
    String html =  user + " " +
      bundle.getString("EMAIL_BODY") + "<br>" + 
      bundle.getString("EMAIL_STARTDATE") + ": " + 
        TextUtils.formatInternalDate(event.getStartDateTime(), "dd/MM/yyyy HH:mm") + "<br>" +
      bundle.getString("EMAIL_ENDDATE") + ": " +   
        TextUtils.formatInternalDate(event.getEndDateTime(), "dd/MM/yyyy HH:mm") + "<br>" +
      bundle.getString("EMAIL_ATTENDANTS") + ":" + 
        encodeAttendants(attendants) + "<br>";
      if (room != null) 
        html += bundle.getString("EMAIL_ROOM") + ": " + 
          room.getDescription() + "<br>";
      html += bundle.getString("EMAIL_ALTERNATIVES") + ":" + 
          encodeAlternatives(slots) + "<br>";
  
    return html;
  }

  private String encodeAlternatives(List<ScheduleSlot> slots)
  {
    String result = "<ul>";
    int loops = slots.size() > 10 ? 10 : slots.size();
    for (int i = 0; i < loops; i++)
    {
      ScheduleSlot slot = slots.get(i);
      result += "<li>" + slot.getStartDateTime() + " - " + slot.getEndDateTime() + "</li>";
    }
    result += "</ul>";
    return result;
  }

  private String encodeAttendants(List<Attendant> attendants)
  {
    String result = "<ul>";
    for (Attendant att : attendants)
    {
      String fullName = att.getFullName().substring(0, att.getFullName().indexOf("("));
      result += "<li>" + fullName + "</li>";
    }
    result += "</ul>";
    return result;
  }
  
  public class Attendant implements Serializable
  {
    private String personId;
    private String fullName;
    
    public Attendant()
    {
    }

    public Attendant(String personId, String fullName)
    {
      this.personId = personId;
      this.fullName = fullName;
    }
    
    public String getPersonId()
    {
      return personId;
    }

    public void setPersonId(String personId)
    {
      this.personId = personId;
    }

    public String getFullName()
    {
      return fullName;
    }

    public void setFullName(String fullName)
    {
      this.fullName = fullName;
    }

    @Override
    public int hashCode()
    {
      int hash = 7;
      hash = 29 * hash + (this.personId != null ? this.personId.hashCode() : 0);
      return hash;
    }
    
    @Override
    public boolean equals(Object o)
    {
      if (o instanceof Attendant)
      {
        Attendant att = (Attendant)o;
        if (this.personId == null)
          return (att.personId == null);
        else
          return this.personId.equals(((Attendant) o).getPersonId());
      }
      else
        return false;
    }
  }
  
  public class RangeDateTimeData implements Serializable
  {
    private String startHour; //HHmm
    private String endHour; //HHmm
    private long duration; //in minutes
    private long steps; //in minutes
    private long timePadding; //in minutes

    public RangeDateTimeData(long duration, long steps, long timePadding)
    {
      if (rangeStartDateTime != null && rangeEndDateTime != null)
      {
        this.startHour = rangeStartDateTime.substring(8, 12);
        this.endHour = rangeEndDateTime.substring(8, 12);
      }
      else
      {
        this.startHour = "0000";
        this.endHour = "0030";
      }
      this.duration = duration;
      this.steps = steps;
      this.timePadding = timePadding;
    }
    
    public void setDuration(String duration, String durationSelector)
    {     
      this.duration = timeToLong(duration + durationSelector);
      long time = getTimeBetweenHours(this.startHour, this.endHour);
      if (this.duration > time)
      {
        Date sDate = TextUtils.parseUserDate(this.startHour, "HHmm");
        long t = sDate.getTime() + TimeUnit.MILLISECONDS.convert(this.duration, TimeUnit.MINUTES);
        Date eDate = new Date(t);
        this.endHour = TextUtils.formatDate(eDate, "HHmm");
      }
    }

    public long getDuration()
    {
      if (duration > 0)
        return duration;
      else
        return 1;
    }

    public long getSteps()
    {
      return steps;
    }

    public void setSteps(String steps)
    {
      this.steps = timeToLong(steps + MINUTES_SELECTOR);
    }

    public long getTimePadding()
    {
      return timePadding;
    }

    public void setTimePadding(String timePadding)
    {
      this.timePadding = timeToLong(timePadding + MINUTES_SELECTOR);;
    }

    public long getDurationInMillis()
    {
      return TimeUnit.MILLISECONDS.convert(getDuration(), TimeUnit.MINUTES);
    }
    
    public long getStepsTimeInMillis()
    {
      return TimeUnit.MILLISECONDS.convert(getSteps(), TimeUnit.MINUTES);      
    }
    
    public long getTimePaddingInMillis()
    {
      return TimeUnit.MILLISECONDS.convert(getTimePadding(), TimeUnit.MINUTES);      
    }    
    
    public long getStartHourInMillis()
    {
      if (this.startHour != null)
      {
        String hour = this.startHour.substring(0, 2);
        String minutes = this.startHour.substring(2, 4);
        return 
          (TimeUnit.MILLISECONDS.convert(Long.valueOf(hour), TimeUnit.HOURS) +
           TimeUnit.MILLISECONDS.convert(Long.valueOf(minutes), TimeUnit.MINUTES));
      }
      return 0;
    }
    
    public long getEndHourInMillis()
    {
      if (this.endHour != null)
      {
        String hour = this.endHour.substring(0, 2);
        String minutes = this.endHour.substring(2, 4);
        return 
          (TimeUnit.MILLISECONDS.convert(Long.valueOf(hour), TimeUnit.HOURS) +
           TimeUnit.MILLISECONDS.convert(Long.valueOf(minutes), TimeUnit.MINUTES));
      }
      return 0;
    }    

    public String getFormattedEndHour()
    {
      if (endHour != null)
        return endHour.substring(0, 2) + ":" + endHour.substring(2, 4);
      else
        return endHour;
    }

    public void setFormattedEndHour(String endHour)
    {
      if (endHour != null && endHour.length() == 5 && endHour.contains(":"))
      {
        String[] parts = endHour.split(":");
        this.endHour = parts[0] + parts[1];
      }
      else if (endHour != null && (endHour.length() == 1 || endHour.length() == 2))
      {
        endHour = StringUtils.leftPad(endHour, 2, "0");
        this.endHour = endHour + "00";
      }      
      else
        this.endHour = DEFAULT_RANGE_END_TIME;
    }

    public String getFormattedStartHour()
    {
      if (startHour != null)
        return startHour.substring(0, 2) + ":" + startHour.substring(2, 4);
      else
        return startHour;
    }

    public void setFormattedStartHour(String startHour)
    {
      if (startHour != null && startHour.length() == 5 && startHour.contains(":"))
      {
        String[] parts = startHour.split(":");
        this.startHour = parts[0] + parts[1];
      }
      else if (startHour != null && (startHour.length() == 1 || startHour.length() == 2))
      {
        startHour = StringUtils.leftPad(startHour, 2, "0");
        this.startHour = startHour + "00";
      }
      else
        this.startHour = DEFAULT_RANGE_START_TIME;
    }

    public boolean includeChangeOfDay()
    {
      if (startHour != null && endHour != null)
      {
        return TextUtils.parseUserDate(endHour, "HHmm").
          before(TextUtils.parseUserDate(startHour, "HHmm"));
      }
      else
        return false;
    }

    public boolean isLongerThanOneDay()
    {
      return duration > 1440;
    }

    private long getTimeBetweenHours(String startHour, String endHour)
    {
      String shour = startHour.substring(0, 2);
      String sminutes = startHour.substring(2, 4);
      long start =
        TimeUnit.MINUTES.convert(Long.parseLong(shour), TimeUnit.HOURS);
      start = start + Long.parseLong(sminutes);

      String ehour = endHour.substring(0, 2);
      String eminutes = endHour.substring(2, 4);
      long end =
        TimeUnit.MINUTES.convert(Long.parseLong(ehour), TimeUnit.HOURS);
      end = end + Long.parseLong(eminutes);

      if (end > start)
        return (end - start);
      else 
        return (1440 - start) + end;
    }
    
    private long timeToLong(String duration)
    {
      long result = 0;
      if (!StringUtils.isBlank(duration))
      {
        if (duration.contains(HOURS_SELECTOR))
        {
          String[] parts = duration.split(HOURS_SELECTOR);
          result = result + TimeUnit.MINUTES.convert(Long.parseLong(parts[0]), TimeUnit.HOURS);
          if (parts.length > 1)
            duration = parts[1];
          else
            duration = "0";
        }

        if (duration.contains(MINUTES_SELECTOR))
        {
          String[] parts = duration.split(MINUTES_SELECTOR);
          result = result + Long.parseLong(parts[0]);
        }
        else
          result = result + Long.parseLong(duration);
      }

      return result;
    }     
  }  
 
  public class Room extends SelectItem implements Serializable
  {
    private boolean selected;
    private LinkedList<ScheduleSlot> slots;
    
    public String getRoomId()
    {
      return (String) getValue();
    }

    public void setRoomId(String roomId)
    {
      setValue(roomId);
    }
    
    @Override
    public void setDescription(String description)
    {
      setLabel(description);
      super.setDescription(description);
    }

    public boolean isSelected()
    {
      return selected;
    }

    public void setSelected(boolean selected)
    {
      this.selected = selected;
    }

    public LinkedList<ScheduleSlot> getSlots()
    {
      return slots;
    }

    public void setSlots(List<Event> events)
    {
      this.slots = new LinkedList<ScheduleSlot>();
      for (Event event : events)
      {
        long startTime = TextUtils.parseInternalDate(event.getStartDateTime()).getTime();
        long endTime = TextUtils.parseInternalDate(event.getEndDateTime()).getTime();
        ScheduleSlot slot = new ScheduleSlot(startTime, endTime);
        this.slots.add(slot);
      }
      Collections.sort(slots);
    }
    
    public boolean isRoomFree(ScheduleSlot slot)
    {
      if (this.slots == null)
        return true;
      for (ScheduleSlot roomSlot : this.slots)
      {
        if (slot.isIntersectedBy(roomSlot))
          return false;
        if (roomSlot.getStartTime() > slot.getEndTime())
          break;
      }
      return true;
    }
    
    @Override
    public String toString()
    {
      return getDescription();
    }

  }

}
