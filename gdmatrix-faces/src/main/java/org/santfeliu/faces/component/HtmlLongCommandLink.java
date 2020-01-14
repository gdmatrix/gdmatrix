package org.santfeliu.faces.component;

import org.santfeliu.faces.HtmlRenderUtils;
import java.io.IOException;
import java.util.Map;
import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;
import org.santfeliu.faces.FacesUtils;

public class HtmlLongCommandLink extends UICommand
{  
  private String _value;
  private String _style;
  private String _styleClass;

  public HtmlLongCommandLink()
  {
  }

  @Override
  public String getFamily()
  {
    return "LongCommandLink";
  }

  public void setValue(String value)
  {
    this._value = value;
  }

  @Override
  public String getValue()
  {
    if (_value != null) return _value;
    ValueBinding vb = getValueBinding("value");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setStyle(String style)
  {
    this._style = style;
  }

  public String getStyle()
  {
    if (_style != null) return _style;
    ValueBinding vb = getValueBinding("style");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setStyleClass(String styleClass)
  {
    this._styleClass = styleClass;
  }

  public String getStyleClass()
  {
    if (_styleClass != null) return _styleClass;
    ValueBinding vb = getValueBinding("styleClass");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }
  
  @Override
  public void decode(FacesContext context)
  {
    try
    {
      if (!isRendered()) return;
      String clientId = getClientId(context);
      Map parameterMap = context.getExternalContext().getRequestParameterMap();
      String formId = FacesUtils.getParentFormId(this, context);
      
      Object value = parameterMap.get(
        formId + ":" + HtmlRenderUtils.HIDDEN_LINK_ID);
      if (clientId.equals(value))
      {
        queueEvent(new ActionEvent(this));
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    if (!isRendered()) return;

    HtmlRenderUtils.renderHiddenLink(this, context);

    String clientId = getClientId(context);
    ResponseWriter writer = context.getResponseWriter();
    String formId = FacesUtils.getParentFormId(this, context);
    
    HtmlRenderUtils.renderOverlay(writer);

    writer.startElement("a", this);
    writer.writeAttribute("href", "#", null);
    if (formId != null)
    {
      writer.writeAttribute("onclick", 
        "showOverlay(); document.forms['" + formId + 
        "']['" + formId + ":" + HtmlRenderUtils.HIDDEN_LINK_ID + "'].value='" + 
        clientId + "'; document.forms['" + formId + 
        "'].submit(); return false;", null);
    }
    if (getStyle() != null)
    {
      writer.writeAttribute("style", getStyle(), null);
    }
    if (getStyleClass() != null)
    {
      writer.writeAttribute("class", getStyleClass(), null);
    }
    String value = getValue();
    if (value != null)
    {
      writer.writeText(value, null);
    }
  }

  @Override
  public void encodeEnd(FacesContext context) throws IOException
  {
    if (!isRendered()) return;    
    ResponseWriter writer = context.getResponseWriter();
    writer.endElement("a");
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[4];
    values[0] = super.saveState(context);
    values[1] = _value;
    values[2] = _style;
    values[3] = _styleClass;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _value = (String)values[1];
    _style = (String)values[2];
    _styleClass = (String)values[3];
  }
}
