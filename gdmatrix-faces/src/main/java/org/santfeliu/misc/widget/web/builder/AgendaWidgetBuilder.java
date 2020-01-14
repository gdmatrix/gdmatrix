package org.santfeliu.misc.widget.web.builder;

import org.santfeliu.misc.widget.web.WidgetDefinition;
import java.util.List;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;
import org.santfeliu.agenda.faces.HtmlAgenda;
import org.santfeliu.security.UserCache;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 */
public class AgendaWidgetBuilder extends WidgetBuilder
{
  public AgendaWidgetBuilder()
  {
  }

  @Override
  public UIComponent getComponent(WidgetDefinition widgetDef,
    FacesContext context)
  {
    HtmlAgenda component = new HtmlAgenda();
    
    component.getAttributes().put("nodeId", widgetDef.getMid());    
    
    Map properties = widgetDef.getProperties();

    if (properties != null)
    {
      //Theme
      String theme = (String)properties.get("theme");
      if (isValueReference((String)properties.get("theme")))
        UIComponentTagUtils.setValueBinding(context, component, "themes", theme);
      else
      {
        List<String> themes = widgetDef.getMultivaluedProperty("theme");
        component.setThemes(themes);
      }

      //Types
      String eventTypeId = (String)properties.get("eventTypeId");
      if (isValueReference(eventTypeId))
        UIComponentTagUtils.setValueBinding(context, component, "eventTypes", eventTypeId);
      else
      {
        List<String> eventTypeIdList = widgetDef.getMultivaluedProperty("eventTypeId");
        component.setEventTypes(eventTypeIdList);
      }

      //Person
      String attendantMode = (String)properties.get("attendantMode");
      if (attendantMode != null && "true".equalsIgnoreCase(attendantMode))
      {
        Credentials credentials =
          UserSessionBean.getCurrentInstance().getCredentials();
        if (credentials != null)
        {
          component.setPersonId(UserCache.getUser(credentials).getPersonId());
        }
      }

      //Room
      String roomId = (String)properties.get("roomId");
      if (roomId != null)
        component.setRoomId(roomId);

      //Properties
      String propertyName = (String)properties.get("propertyName");
      if (propertyName != null)
      {
        component.setPropertyName(propertyName);
        List values =
          widgetDef.getMultivaluedProperty("propertyValue");
        component.setPropertyValues(values);
      }

      setTranslationProperties(component, properties, "event", context);

      //rows
      String rowsValue = (String)properties.get("rows");
      if (rowsValue != null)
        component.setRows(Integer.valueOf(rowsValue).intValue());

      //style
      component.setStyle((String)properties.get("agendaStyle"));
      String eventStyleClass = (String)properties.get("agendaStyleClass");
      if (eventStyleClass == null)
        eventStyleClass = "agenda";
      component.setStyleClass(eventStyleClass);

      component.setDateStyle((String)properties.get("dateStyle"));
      String dateStyleClass = (String)properties.get("dateStyleClass");
      if (dateStyleClass == null)
        dateStyleClass = "eventDate";
      component.setDateStyleClass(dateStyleClass);

      component.setNameStyle((String)properties.get("nameStyle"));
      String nameStyleClass = (String)properties.get("nameStyleClass");
      if (nameStyleClass == null)
        nameStyleClass = "eventName";
      component.setNameStyleClass(nameStyleClass);

      component.setCommentsStyle((String)properties.get("commentsStyle"));
      String commentsStyleClass = (String)properties.get("commentsStyleClass");
      if (commentsStyleClass == null)
        commentsStyleClass = "eventComments";
      component.setCommentsStyleClass(commentsStyleClass);

      component.setVar((String)properties.get("var"));
      String urlValue = (String)properties.get("url");
      UIComponentTagUtils.setStringProperty(
        context, component, "url", urlValue);

      String dateFormat = (String)properties.get("dateFormat");
      if (dateFormat == null)
        dateFormat = "dd/MM/yyyy";
      component.setDateFormat(dateFormat);

    }
    return component;
  }
}
