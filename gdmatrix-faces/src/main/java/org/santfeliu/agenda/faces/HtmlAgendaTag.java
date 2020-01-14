package org.santfeliu.agenda.faces;

import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author blanquepa
 */
public class HtmlAgendaTag extends UIComponentTag
{
  private String themes;
  private String rows;
  private String translator;
  private String translationGroup;
  private String style;
  private String styleClass;
  private String dateStyle;
  private String dateStyleClass;
  private String nameStyle;
  private String nameStyleClass;
  private String var;
  private String url;
  private String commentsStyle;
  private String commentsStyleClass;
  private String dateFormat;
  private String roomId;
  private String eventTypes;
  private String target;
  private String personId;
  private String caseId;

  public String getDateStyle()
  {
    return dateStyle;
  }

  public void setDateStyle(String dateStyle)
  {
    this.dateStyle = dateStyle;
  }

  public String getDateStyleClass()
  {
    return dateStyleClass;
  }

  public void setDateStyleClass(String dateStyleClass)
  {
    this.dateStyleClass = dateStyleClass;
  }

  public String getNameStyle()
  {
    return nameStyle;
  }

  public void setNameStyle(String nameStyle)
  {
    this.nameStyle = nameStyle;
  }

  public String getNameStyleClass()
  {
    return nameStyleClass;
  }

  public void setNameStyleClass(String nameStyleClass)
  {
    this.nameStyleClass = nameStyleClass;
  }

  public String getRows()
  {
    return rows;
  }

  public void setRows(String rows)
  {
    this.rows = rows;
  }

  public String getStyle()
  {
    return style;
  }

  public void setStyle(String style)
  {
    this.style = style;
  }

  public String getStyleClass()
  {
    return styleClass;
  }

  public void setStyleClass(String styleClass)
  {
    this.styleClass = styleClass;
  }

  public String getCommentsStyle()
  {
    return commentsStyle;
  }

  public void setCommentsStyle(String commentsStyle)
  {
    this.commentsStyle = commentsStyle;
  }

  public String getCommentsStyleClass()
  {
    return commentsStyleClass;
  }

  public void setCommentsStyleClass(String commentsStyleClass)
  {
    this.commentsStyleClass = commentsStyleClass;
  }

  public String getThemes()
  {
    return themes;
  }

  public void setThemes(String themes)
  {
    this.themes = themes;
  }

  public String getTranslationGroup()
  {
    return translationGroup;
  }

  public void setTranslationGroup(String translationGroup)
  {
    this.translationGroup = translationGroup;
  }

  public String getTranslator()
  {
    return translator;
  }

  public void setTranslator(String translator)
  {
    this.translator = translator;
  }

  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public String getVar()
  {
    return var;
  }

  public void setVar(String var)
  {
    this.var = var;
  }

  public String getDateFormat()
  {
    return dateFormat;
  }

  public void setDateFormat(String dateFormat)
  {
    this.dateFormat = dateFormat;
  }

  public String getRoomId()
  {
    return roomId;
  }

  public void setRoomId(String roomId)
  {
    this.roomId = roomId;
  }

  public String getEventTypes()
  {
    return eventTypes;
  }

  public void setEventTypes(String eventTypes)
  {
    this.eventTypes = eventTypes;
  }

  public String getTarget()
  {
    return target;
  }

  public void setTarget(String target)
  {
    this.target = target;
  }

  public String getPersonId()
  {
    return personId;
  }

  public void setPersonId(String personId)
  {
    this.personId = personId;
  }

  public String getCaseId() {
    return caseId;
  }

  public void setCaseId(String caseId) {
    this.caseId = caseId;
  }

  @Override
  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      super.setProperties(component);
      if (rows != null)
      {
        if (isValueReference(rows))
        {
          ValueBinding vb = context.getApplication().createValueBinding(rows);
          component.setValueBinding("rows", vb);
        }
        else
        {
          UIComponentTagUtils.setIntegerProperty(context, component, "rows", rows);
        }
      }
      if (themes != null)
      {
        if (isValueReference(themes))
        {
          ValueBinding vb = context.getApplication().createValueBinding(themes);
          component.setValueBinding("themes", vb);
        }
        else
        {
          List<String> list = TextUtils.stringToList(themes, ",");
          component.getAttributes().put("themes", list);
        }
      }
      if (translator != null)
      {
        if (isValueReference(translator))
        {
          ValueBinding vb = context.getApplication().createValueBinding(translator);
          component.setValueBinding("translator", vb);
        }
      }
      UIComponentTagUtils.setStringProperty(
        context, component, "translationGroup", translationGroup);
      UIComponentTagUtils.setStringProperty(
        context, component, "style", style);
      UIComponentTagUtils.setStringProperty(
        context, component, "styleClass", styleClass);
      UIComponentTagUtils.setStringProperty(
        context, component, "dateStyle", dateStyle);
      UIComponentTagUtils.setStringProperty(
        context, component, "dateStyleClass", dateStyleClass);
      UIComponentTagUtils.setStringProperty(
        context, component, "nameStyle", nameStyle);
      UIComponentTagUtils.setStringProperty(
        context, component, "nameStyleClass", nameStyleClass);
      UIComponentTagUtils.setStringProperty(
        context, component, "commentsStyle", commentsStyle);
      UIComponentTagUtils.setStringProperty(
        context, component, "commentsStyleClass", commentsStyleClass);
      UIComponentTagUtils.setStringProperty(
        context, component, "var", var);
      if (url != null)
      {
        if (isValueReference(url))
        {
          ValueBinding vb = context.getApplication().createValueBinding(url);
          component.setValueBinding("url", vb);
        }
        else
          UIComponentTagUtils.setStringProperty(
            context, component, "url", url);
      }

      UIComponentTagUtils.setStringProperty(
        context, component, "dateFormat", dateFormat);
      if (roomId != null)
      {
        if (isValueReference(roomId))
        {
          ValueBinding vb = context.getApplication().createValueBinding(roomId);
          component.setValueBinding("roomId", vb);
        }
        else
          UIComponentTagUtils.setStringProperty(
            context, component, "roomId", roomId);
      }
      if (eventTypes != null)
      {
        if (isValueReference(eventTypes))
        {
          ValueBinding vb = context.getApplication().createValueBinding(eventTypes);
          component.setValueBinding("eventTypes", vb);
        }
        else
        {
          List<String> list = TextUtils.stringToList(eventTypes, ",");
          component.getAttributes().put("eventTypes", list);
        }
      }
      UIComponentTagUtils.setStringProperty(
        context, component, "target", target);
      if (personId != null)
      {
        if (isValueReference(personId))
        {
          ValueBinding vb = context.getApplication().createValueBinding(personId);
          component.setValueBinding("personId", vb);
        }
        else
          UIComponentTagUtils.setStringProperty(
            context, component, "personId", personId);
      }
      if (caseId != null)
      {
        if (isValueReference(caseId))
        {
          ValueBinding vb = context.getApplication().createValueBinding(caseId);
          component.setValueBinding("caseId", vb);
        }
        else
          UIComponentTagUtils.setStringProperty(
            context, component, "caseId", caseId);
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  @Override
  public void release()
  {
    super.release();
    rows = null;
    themes = null;
    translator = null;
    translationGroup = null;
  }

  @Override
  public String getComponentType()
  {
    return "Agenda";
  }

  @Override
  public String getRendererType()
  {
    return null;
  }
}
