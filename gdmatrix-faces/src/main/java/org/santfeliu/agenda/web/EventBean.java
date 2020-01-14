package org.santfeliu.agenda.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import org.matrix.agenda.Event;
import org.matrix.dic.PropertyDefinition;
import org.santfeliu.agenda.client.AgendaManagerClient;
import org.santfeliu.agenda.web.EventCopyBean.EventRow;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.ObjectPropertiesConverter;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.obj.ObjectAction;

import org.santfeliu.web.obj.ObjectBean;

public class EventBean extends ObjectBean
{
  private static final String RENDER_ATTENDANTS_PROPERTY = "_showAttendants";
  private static final String RENDER_DOCUMENTS_PROPERTY = "_showDocuments";
  private static final String RENDER_PLACES_PROPERTY = "_showPlaces";
  private static final String RENDER_THEMES_PROPERTY = "_showThemes";
  private static final String RENDER_CASES_PROPERTY = "_showCases";

  private static final String CASES_LABEL_PROPERTY = "_casesLabel";
  
  public static final String SEARCH_MID = "eventSearchMid";

  public EventBean()
  {
  }

  public String getObjectTypeId()
  {
    return "Event";
  }
  
  @Override
  public String getActualTypeId()
  {
    EventMainBean eventMainBean = (EventMainBean)getBean("eventMainBean");
    if (isNew())
      return eventMainBean.getCurrentTypeId();
    else if (eventMainBean.getEvent() != null)
      return eventMainBean.getEvent().getEventTypeId();
    else
      return getObjectTypeId();
  }
  
  @Override
  public String cancel()  
  {
    AgendaManagerClient.getCache().clear();
    return super.cancel();
  }
  
  @Override
  public String remove()
  {
    try
    {
      if (!isNew())
      {
        preRemove();
        AgendaConfigBean.getPort().removeEvent(getObjectId());
        postRemove();
        removed();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    
    return getControllerBean().show();
  }

  public String duplicate()
  {
    if (!isNew())
    {
      try
      {
        EventMainBean eventMainBean = (EventMainBean)getBean("eventMainBean");
        Event event = eventMainBean.getEvent();
        Map formData = eventMainBean.getData();
        ObjectPropertiesConverter converter = new ObjectPropertiesConverter(event);
        converter.fromPropertiesMap(formData);
        EventCopyBean eventCopyBean = new EventCopyBean();
        eventCopyBean.setEvent(event);
        EventRow copy = eventCopyBean.duplicate(0, 0);
        eventCopyBean.copyRecurrences(false);
        if (!getFacesContext().getMessages().hasNext())
          info("DUPLICATE_END");
        return getControllerBean().show(getSelectedMenuItem().getMid(), copy.getEvent().getEventId());
      }
      catch (Exception ex)
      {
        error(ex);
        return null;
      }
    }
    else
      return null;
  }

  public String reserveRoomBeforeEvent()
  {
    if (!isNew())
    {
      try
      {
        EventMainBean eventMainBean = (EventMainBean)getBean("eventMainBean");
        Event event = eventMainBean.getEvent();
        event.setEventTypeId("sf:ReservaDeSalaEvent");
        EventCopyBean eventCopyBean = new EventCopyBean();
        eventCopyBean.setEvent(event);
        eventCopyBean.setCopyAttendants(false);
        EventRow copy = eventCopyBean.duplicate(-60, 60);
        if (!copy.isRoomAvailable())
          error("ROOM_UNAVAILABLE");
        else
        {
          eventCopyBean.copyRecurrences(false);
          info("COPY_END");
          return getControllerBean().show(getSelectedMenuItem().getMid(),
            copy.getEvent().getEventId());
        }
      }
      catch (Exception ex)
      {
        error(ex);
        return null;
      }
    }

    return null;
  }

  public String reserveRoomAfterEvent()
  {
    if (!isNew())
    {
      try
      {
        EventMainBean eventMainBean = (EventMainBean)getBean("eventMainBean");
        Event event = eventMainBean.getEvent();
        event.setEventTypeId("sf:ReservaDeSalaEvent");
        EventCopyBean eventCopyBean = new EventCopyBean();
        eventCopyBean.setEvent(event);
        eventCopyBean.setCopyAttendants(false);
        long time = EventCopyBean.getTimeBetweenDates(event.getStartDateTime(),
          event.getEndDateTime());
        EventRow copy = eventCopyBean.duplicate(time, 60);
        if (!copy.isRoomAvailable())
          error("ROOM_UNAVAILABLE");
        else
        {
          eventCopyBean.copyRecurrences(false);
          info("COPY_END");
          return getControllerBean().show(getSelectedMenuItem().getMid(),
            copy.getEvent().getEventId());
        }
      }
      catch (Exception ex)
      {
        error(ex);
        return null;
      }
    }

    return null;
  }

  public String copy()
  {
    if (!isNew())
    {
      EventRecurrencesBean eventRecurrencesBean =
        (EventRecurrencesBean)getBean("eventRecurrencesBean");
      eventRecurrencesBean.setCurrentCollapsed(true);
      eventRecurrencesBean.setNewCollapsed(false);
      setBean("eventRecurrencesBean", eventRecurrencesBean);

      MenuItemCursor cursor =
        getControllerBean().getPageMenuItem(getSelectedMenuItem(), "eventRecurrencesBean");
      return getControllerBean().show(cursor.getMid(), objectId);
    }
    else
      return null;
  }
  
  @Override
  public String getDescription()
  {
    EventMainBean eventMainBean = (EventMainBean)getBean("eventMainBean");
    Event event = eventMainBean.getEvent();
    return getEventDescription(event);
  }   
  
  @Override
  public String getDescription(String oid)
  {
    String description = "";
    try
    {
      Event event = AgendaConfigBean.getPort().loadEventFromCache(oid);
      description = getEventDescription(event);
    }
    catch (Exception ex)
    {
      error(ex.getMessage());
    }
    return description;
  }
  
  private String getEventDescription(Event event)
  {
    StringBuilder buffer = new StringBuilder();
    if (event.getSummary() != null)
    {
      buffer.append(event.getSummary());
      buffer.append(" : ");
    }
    AgendaConfigBean agendaConfigBean = (AgendaConfigBean)getBean("agendaConfigBean");
    buffer.append(agendaConfigBean.getEventTypeDescription(event.getEventTypeId()));
    buffer.append(" (");
    buffer.append(event.getEventId());
    buffer.append(")");

    return buffer.toString();
  }

  @Override
  public boolean isEditable()
  {
    EventMainBean eventMainBean = (EventMainBean)getBean("eventMainBean");
    try
    {
      return eventMainBean.isEditable();
    }
    catch (Exception ex)
    {
      return false;
    }
  }

  public boolean isAttendantsTabRendered()
  {
    return isTabRendered(RENDER_ATTENDANTS_PROPERTY);
  }

  public boolean isDocumentsTabRendered()
  {
    return isTabRendered(RENDER_DOCUMENTS_PROPERTY);
  }

  public boolean isPlacesTabRendered()
  {
    return isTabRendered(RENDER_PLACES_PROPERTY);
  }

  public boolean isThemesTabRendered()
  {
    return isTabRendered(RENDER_THEMES_PROPERTY);
  }
  
  public boolean isCasesTabRendered()
  {
    return isTabRendered(RENDER_CASES_PROPERTY);
  }

  public String getCasesTabLabel()
  {
    return getTabLabel(CASES_LABEL_PROPERTY);
  }
  
  @Override
  public List<ObjectAction> getObjectActions()
  {
    ArrayList<ObjectAction> actions = new ArrayList<ObjectAction>();
    if (isNew()) return Collections.EMPTY_LIST;

    EventMainBean eventMainBean = (EventMainBean)getBean("eventMainBean");
    Event event = eventMainBean.getEvent();

    if (event == null) return Collections.EMPTY_LIST;

    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.agenda.web.resources.AgendaBundle", getLocale());

    Type type = TypeCache.getInstance().getType(event.getEventTypeId());
    if (type != null)
    {
      List<String> actionNames = type.getActions();
      for (String actionName : actionNames)
      {
        ObjectAction action = new ObjectAction();
        boolean editable = isEditable();
        if ("Duplicate".equals(actionName) && editable)
        {
          action.setDescription(bundle.getString(actionName));
          action.setExpression("#{eventBean.duplicate}");
          actions.add(action);
        }
        else if ("Copy".equals(actionName) && editable)
        {
          action.setDescription(bundle.getString(actionName));
          action.setExpression("#{eventBean.copy}");
          actions.add(action);
        }
        else if ("ReserveRoom".equals(actionName) && editable)
        {
          ObjectAction after = new ObjectAction();
          after.setDescription(bundle.getString("ReserveRoomAfter"));
          after.setExpression("#{eventBean.reserveRoomAfterEvent}");
          actions.add(after);

          ObjectAction before = new ObjectAction();
          before.setDescription(bundle.getString("ReserveRoomBefore"));
          before.setExpression("#{eventBean.reserveRoomBeforeEvent}");
          actions.add(before);
        }
      }
      actions.addAll(super.getObjectActions());
    }
    return actions;
  }
  
  private boolean isTabRendered(String renderPropertyName)
  {
    if (isNew()) return false;

    EventMainBean eventMainBean = (EventMainBean)getBean("eventMainBean");

    org.santfeliu.dic.Type type = eventMainBean.getCurrentType();
    if (type == null)
      return true;

    PropertyDefinition pd = type.getPropertyDefinition(renderPropertyName);
    if (pd == null) return true;
    if (pd.getValue().isEmpty()) return true;
    return "true".equals(pd.getValue().get(0));
  }

}
