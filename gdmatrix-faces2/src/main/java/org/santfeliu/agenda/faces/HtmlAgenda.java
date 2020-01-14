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
package org.santfeliu.agenda.faces;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.matrix.agenda.Event;
import org.matrix.agenda.EventFilter;
import org.matrix.agenda.OrderByProperty;
import org.matrix.cases.CaseEventFilter;
import org.matrix.cases.CaseEventView;
import org.matrix.cases.CaseManagerPort;
import org.matrix.dic.Property;
import org.santfeliu.agenda.client.AgendaManagerClient;
import org.santfeliu.agenda.web.AgendaConfigBean;
import org.santfeliu.cases.web.CaseConfigBean;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.Translator;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 */
@FacesComponent(value = "HtmlAgenda")
public class HtmlAgenda extends UIComponentBase
{
  private List _themes;
  private Integer _rows;
  private Translator _translator;
  private String _translationGroup;
  private String _style;
  private String _styleClass;
  private String _dateStyle;
  private String _dateStyleClass;
  private String _nameStyle;
  private String _nameStyleClass;
  private String _var;
  private String _url;
  private String _commentsStyle;
  private String _commentsStyleClass;
  private String _dateFormat;
  //new filter attributes
  private String _personId;
  private String _roomId;
  private List _eventTypes;
  private String _propertyName;
  private List _propertyValues;
  private List _orderBy;
  private String _target;
  private String _caseId;


  public HtmlAgenda()
  {
    setRendererType(null);
  }

  @Override
  public String getFamily()
  {
    return "Agenda";
  }

  public List getThemes()
  {
    if (_themes != null) return _themes;
    ValueExpression ve = getValueExpression("themes");
    return ve != null ? (List)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setThemes(List _themes)
  {
    this._themes = _themes;
  }

  public int getRows()
  {
    if (_rows != null) return _rows.intValue();
    ValueExpression ve = getValueExpression("rows");
    Integer v = ve != null ? (Integer)ve.getValue(getFacesContext().getELContext()) : null;
    return v != null ? v.intValue() : 0;
  }

  public void setRows(int _rows)
  {
    this._rows = _rows;
  }

  public Translator getTranslator()
  {
    if (_translator != null)
      return _translator;
    ValueExpression ve = getValueExpression("translator");
    return ve != null? (Translator) ve.getValue(getFacesContext().getELContext()): null;
  }

  public void setTranslator(Translator _translator)
  {
    this._translator = _translator;
  }

  public void setTranslationGroup(String translationGroup)
  {
    this._translationGroup = translationGroup;
  }

  public String getTranslationGroup()
  {
    if (_translationGroup != null)
      return _translationGroup;
    ValueExpression ve = getValueExpression("translationGroup");
    return ve != null? (String) ve.getValue(getFacesContext().getELContext()): null;
  }

  public String getStyle()
  {
    if (_style != null) return _style;
    ValueExpression ve = getValueExpression("style");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;

  }

  public void setStyle(String _style)
  {
    this._style = _style;
  }

  public String getStyleClass()
  {
    if (_styleClass != null) return _styleClass;
    ValueExpression ve = getValueExpression("styleClass");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setStyleClass(String _styleClass)
  {
    this._styleClass = _styleClass;
  }

  public String getDateStyle()
  {
    if (_dateStyle != null) return _dateStyle;
    ValueExpression ve = getValueExpression("dateStyle");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setDateStyle(String _dateStyle)
  {
    this._dateStyle = _dateStyle;
  }

  public String getDateStyleClass()
  {
    if (_dateStyleClass != null) return _dateStyleClass;
    ValueExpression ve = getValueExpression("dateStyleClass");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;

  }

  public void setDateStyleClass(String _dateStyleClass)
  {
    this._dateStyleClass = _dateStyleClass;
  }

  public String getNameStyle()
  {
     if (_nameStyle != null) return _nameStyle;
    ValueExpression ve = getValueExpression("nameStyle");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setNameStyle(String _nameStyle)
  {
    this._nameStyle = _nameStyle;
  }

  public String getNameStyleClass()
  {
    if (_nameStyleClass != null) return _nameStyleClass;
    ValueExpression ve = getValueExpression("nameStyleClass");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;

  }

  public void setNameStyleClass(String _nameStyleClass)
  {
    this._nameStyleClass = _nameStyleClass;
  }

  public void setCommentsStyle(String _commentsStyle)
  {
    this._commentsStyle = _commentsStyle;
  }

  public String getCommentsStyle()
  {
    if (_commentsStyle != null) return _commentsStyle;
    ValueExpression ve = getValueExpression("commentsStyle");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public String getCommentsStyleClass()
  {
    if (_commentsStyleClass != null) return _commentsStyleClass;
    ValueExpression ve = getValueExpression("commentsStyleClass");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setCommentsStyleClass(String _commentsStyleClass)
  {
    this._commentsStyleClass = _commentsStyleClass;
  }

  public String getUrl()
  {
    if (_url != null) return _url;
    ValueExpression ve = getValueExpression("url");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setUrl(String _url)
  {
    this._url = _url;
  }

  public String getVar()
  {
    return _var;
  }

  public void setVar(String _var)
  {
    this._var = _var;
  }

  public String getDateFormat()
  {
    if (_dateFormat != null) return _dateFormat;
    ValueExpression ve = getValueExpression("dateFormat");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setDateFormat(String _dateFormat)
  {
    this._dateFormat = _dateFormat;
  }

  public List getEventTypes()
  {
    if (_eventTypes != null) return _eventTypes;
    ValueExpression ve = getValueExpression("eventTypes");
    return ve != null ? (List)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setEventTypes(List _eventTypes)
  {
    this._eventTypes = _eventTypes;
  }

  public String getPersonId()
  {
    if (_personId != null) return _personId;
    ValueExpression ve = getValueExpression("personId");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setPersonId(String _personId)
  {
    this._personId = _personId;
  }

  public String getRoomId()
  {
    if (_roomId != null) return _roomId;
    ValueExpression ve = getValueExpression("roomId");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setRoomId(String _roomId)
  {
    this._roomId = _roomId;
  }
  
  public String getCaseId()
  {
    if (_caseId != null) return _caseId;
    ValueExpression ve = getValueExpression("caseId");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setCaseId(String _caseId)
  {
    this._caseId = _caseId;
  }  

  public String getPropertyName()
  {
    if (_propertyName != null) return _propertyName;
    ValueExpression ve = getValueExpression("propertyName");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setPropertyName(String _propertyName)
  {
    this._propertyName = _propertyName;
  }

  public List getPropertyValues()
  {
    if (_propertyValues != null) return _propertyValues;
    ValueExpression ve = getValueExpression("propertyValues");
    return ve != null ? (List)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public List getOrderBy()
  {
    if (_orderBy != null) return _orderBy;
    ValueExpression ve = getValueExpression("orderBy");
    return ve != null ? (List)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setOrderBy(List _orderBy)
  {
    this._orderBy = _orderBy;
  }

  public String getTarget()
  {
    return _target;
  }

  public void setTarget(String _target)
  {
    this._target = _target;
  }

  public void setPropertyValues(List _propertyValues)
  {
    this._propertyValues = _propertyValues;
  }

  @Override
  public void processValidators(FacesContext context)
  {
    if (context == null) throw new NullPointerException("context");
    if (!isRendered()) return;
    super.processValidators(context);
  }

  @Override
  public void processUpdates(FacesContext context)
  {
    if (context == null) throw new NullPointerException("context");
    if (!isRendered()) return;
    super.processUpdates(context);
    try
    {
      updateModel(context);
    }
    catch (RuntimeException e)
    {
      context.renderResponse();
      throw e;
    }
  }

  public void updateModel(FacesContext context)
  {
    if (_themes != null)
    {
      ValueExpression ve = getValueExpression("themes");
      if (ve != null)
      {
        if (!ve.isReadOnly(context.getELContext()))
        {
          ve.setValue(context.getELContext(), _themes);
          _themes = null;
        }
      }
    }
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    if (!isRendered()) return;
    String clientId = getClientId(context);
    ResponseWriter writer = context.getResponseWriter();
    try
    {
      List<Event> events = new ArrayList();
      
      if (getCaseId() != null)
      {
        //If caseId exists call Case port
        CaseEventFilter caseEventFilter = new CaseEventFilter();
        caseEventFilter.setCaseId(getCaseId());
        List<CaseEventView> views = getCasePort().findCaseEventViews(caseEventFilter);
        for (CaseEventView view : views)
        {
          events.add(view.getEvent());
        }
      }
      else
      {
        EventFilter filter = new EventFilter();
        filter.setMaxResults(getRows());

        String startDateTime = 
          TextUtils.formatDate(new Date(), "yyyyMMdd") + "000000";
        filter.setStartDateTime(startDateTime);

        //themes
        filter.getThemeId().addAll(getThemes());

        //types
        if (getEventTypes() != null && !getEventTypes().isEmpty())
          filter.getEventTypeId().addAll(getEventTypes());

        //person
        if (getPersonId() != null)
          filter.setPersonId(getPersonId());

        //room
        if (getRoomId() != null)
          filter.setRoomId(getRoomId());

        //properties
        String propertyName = getPropertyName();
        if (propertyName != null)
        {
          Property property = new Property();
          property.setName(propertyName);
          property.getValue().addAll(getPropertyValues());
          filter.getProperty().add(property);
        }

        //order by
        if (getOrderBy() != null && !getOrderBy().isEmpty())
        {
          filter.getOrderBy().clear();
          Iterator it = getOrderBy().iterator();
          while (it.hasNext())
          {
            String orderByElement = (String) it.next();
            String[] parts = orderByElement.split(":");
            OrderByProperty orderByProperty = new OrderByProperty();
            orderByProperty.setName(parts[0]);
            if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1]))
            {
              orderByProperty.setDescending(true);
            }
            filter.getOrderBy().add(orderByProperty);
          }
        }
        events = getPort().findEventsFromCache(filter);
      }
      
      Translator translator = getTranslator();
      if (events != null && events.size() > 0)
        encodeEvents(events, writer, translator, clientId);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      FacesUtils.addMessage(this, "CAN_NOT_SHOW_EVENTS", null,
        FacesMessage.SEVERITY_ERROR);
    }
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[21];
    values[0] = super.saveState(context);
    values[1] = _themes;
    values[2] = _rows;
    values[3] = _translator;
    values[4] = _translationGroup;
    values[5] = _style;
    values[6] = _styleClass;
    values[7] = _dateStyle;
    values[8] = _dateStyleClass;
    values[9] = _nameStyle;
    values[10] = _nameStyleClass;
    values[11] = _commentsStyle;
    values[12] = _commentsStyleClass;
    values[13] = _var;
    values[14] = _url;
    values[15] = _dateFormat;
    values[16] = _roomId;
    values[17] = _eventTypes;
    values[18] = _target;
    values[19] = _personId;
    values[20] = _caseId;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _themes = (List)values[1];
    _rows = (Integer)values[2];
    _translator = (Translator)values[3];
    _translationGroup = (String)values[4];
    _style = (String)values[5];
    _styleClass = (String)values[6];
    _dateStyle = (String)values[7];
    _dateStyleClass = (String)values[8];
    _nameStyle = (String)values[9];
    _nameStyleClass = (String)values[10];
    _commentsStyle = (String)values[11];
    _commentsStyleClass = (String)values[12];
    _var = (String)values[13];
    _url = (String)values[14];
    _dateFormat = (String)values[15];
    _roomId = (String)values[16];
    _eventTypes = (List)values[17];
    _target = (String)values[18];
    _personId = (String)values[19];
    _caseId = (String)values[20];
  }

//Private
  private void encodeEvents(List<Event> events, ResponseWriter writer,
    Translator translator, String clientId)
    throws IOException
  {
    writer.startElement("ul", this);
    String style = getStyle();
    if (style != null)
      writer.writeAttribute("style", style, null);
    String styleClass = getStyleClass();
    if (styleClass != null)
      writer.writeAttribute("class", styleClass, null);

    int count = 0;
    for (Event event : events)
    {
      writer.startElement("li", this);
      String rowClass = (count % 2 == 0) ? "event1" : "event2";
      writer.writeAttribute("class", rowClass, null);
      encodeEvent(event, writer, translator, clientId);
      writer.endElement("li");
      count++;
    }
    writer.endElement("ul");
  }

  private void encodeEvent(Event event, ResponseWriter writer,
    Translator translator, String clientId) throws IOException
  {
    if (_var != null)
    {
      Map requestMap =
        getFacesContext().getExternalContext().getRequestMap();
      requestMap.put(getVar(), event);
    }

    //Date division
    writer.startElement("div", this);
    String dateStyle = getDateStyle();
    if (dateStyle != null)
      writer.writeAttribute("style", dateStyle, null);
    String dateStyleClass = getDateStyleClass();
    if (dateStyleClass != null)
      writer.writeAttribute("class", dateStyleClass, null);

    String dateFormat = getDateFormat() != null ? getDateFormat() : "dd/MM/yyyy";

    String systemStartDateTime = event.getStartDateTime();
    String userStartDate = TextUtils.formatDate(
      TextUtils.parseInternalDate(systemStartDateTime), dateFormat);
    writer.writeText(userStartDate, null);
    
    String systemEndDateTime = event.getEndDateTime();

    String startDay =
      systemStartDateTime != null ? systemStartDateTime.substring(0, 8) : null;
    String endDay =
      systemEndDateTime != null ? systemEndDateTime.substring(0, 8) : null;

    if (endDay != null && !endDay.equals(startDay))
    {
      String userEndDate = TextUtils.formatDate(
        TextUtils.parseInternalDate(systemEndDateTime), dateFormat);
      writer.writeText("-" + userEndDate, null);
    }
    writer.endElement("div");

    //Name division
    writer.startElement("div", this);
    String nameStyle = getNameStyle();
    if (nameStyle != null)
      writer.writeAttribute("style", nameStyle, null);
    String nameStyleClass = getNameStyleClass();
    if (nameStyleClass != null)
      writer.writeAttribute("class", nameStyleClass, null);

    String name = event.getSummary();
    if (name == null && event.getEventTypeId() != null)
    {
      Type type = TypeCache.getInstance().getType(event.getEventTypeId());
      if (type != null)
        name = type.getDescription();
      else
        name = event.getEventTypeId();
    }
    String url = getUrl();
    if (url != null)
    {
      writer.startElement("a", this);
      writer.writeAttribute("href", getUrl(), null);
      if (getTarget() != null)
        writer.writeAttribute("target", getTarget(), null);
      writer.writeAttribute("name", clientId + ":value", null);
      writer.writeAttribute("aria-label", translate(name, translator, event.getEventId()) + " " + userStartDate, null);
      renderPlainText(name, writer, translator, event.getEventId());
      writer.endElement("a");
    }
    else
      renderPlainText(name, writer, translator, event.getEventId());
    writer.endElement("div");

    //Comments division
    String comments = event.getDescription();
    if (comments != null)
    {
      writer.startElement("div", this);
      String commentsStyle = getCommentsStyle();
      if (commentsStyle != null)
        writer.writeAttribute("style", commentsStyle, null);
      String commentsStyleClass = getCommentsStyleClass();
      if (commentsStyleClass != null)
        writer.writeAttribute("class", commentsStyleClass, null);
      renderPlainText(comments, writer, translator, event.getEventId());
      writer.endElement("div");
    }
  }
  
  private String translate(String text, Translator translator, String trGroup)  
    throws IOException
  {
    String textToRender = null;
    if (translator != null)
    {
      String userLanguage = FacesUtils.getViewLanguage();
      String translationGroup = getTranslationGroup() + ":" + trGroup;
      StringWriter sw = new StringWriter();
      translator.translate(new StringReader(text), sw, "text/plain",
        userLanguage, translationGroup);
      textToRender = sw.toString();
    } 
    else
      textToRender = text;
    
    return textToRender;
  }

  private void renderPlainText(String text,
    ResponseWriter writer, Translator translator, String trGroup) throws IOException
  {
    String textToRender = null;
    if (translator != null)
      textToRender = translate(text, translator, trGroup);
    else 
      textToRender = text;

    String lines[] = textToRender.split("\n");
    if (lines.length > 0)
    {
      writer.writeText(lines[0], JSFAttr.VALUE_ATTR);
      for (int i = 1; i < lines.length; i++)
      {
        writer.startElement("br", this);
        writer.endElement("br");
        writer.writeText(lines[i], JSFAttr.VALUE_ATTR);
      }
    }
  }

  private AgendaManagerClient getPort() throws Exception
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();    
    String userId = userSessionBean.getUsername();
    String password = userSessionBean.getPassword();
    return AgendaConfigBean.getPort(userId, password);
  }
  
  private CaseManagerPort getCasePort() throws Exception
  {
    return CaseConfigBean.getPort();
  }

  protected boolean isValueReference(String value)
  {
    if (value != null)
      return value.contains("#{");

    else return false;
  }
}

