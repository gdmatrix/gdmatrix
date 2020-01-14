package org.santfeliu.faces.component;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

/**
 *
 * @author realor
 */
public class HtmlSaveScroll extends UIComponentBase
{
  private Integer _value = null; // scroll value
  private Boolean _resetIfError;

  public void setValue(Integer value)
  {
    this._value = value;
  }

  public Integer getValue()
  {
    if (_value != null) return _value;
    ValueBinding vb = getValueBinding("value");
    if (vb == null) return 0;
    Integer value = (Integer)vb.getValue(getFacesContext());
    return value == null ? 0 : value;
  }

  public void setResetIfError(Boolean resetIfError)
  {
    this._resetIfError = resetIfError;
  }

  public Boolean getResetIfError()
  {
    if (_resetIfError != null) return _resetIfError;
    ValueBinding vb = getValueBinding("resetIfError");
    if (vb == null) return false;
    Boolean value = (Boolean)vb.getValue(getFacesContext());
    return value == null ? false : value;
  }  
  
  @Override
  public String getFamily()
  {
    return "SaveScroll";
  }

  @Override
  public void decode(FacesContext context)
  {
    Map parameters = context.getExternalContext().getRequestParameterMap();
    String textValue = (String)parameters.get("__SAVESCROLL");
    if (textValue != null)
    {
      _value = Integer.parseInt(textValue);
      ValueBinding vb = getValueBinding("value");
      if (vb != null)
      {
        // save _value in backed bean and reset _value
        vb.setValue(context, _value);
        _value = null;
      }
    }
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    if (!isRendered()) return;
    ResponseWriter writer = context.getResponseWriter();
    
    Integer scrollValue = getValue();
    if (getResetIfError() && isFacesError(context))
    {
      scrollValue = 0;
    }
    
    writer.startElement("input", this);
    writer.writeAttribute("id", "__SAVESCROLL", null);
    writer.writeAttribute("type", "hidden", null);
    writer.writeAttribute("name", "__SAVESCROLL", null);
    writer.writeAttribute("value", scrollValue, null);
    writer.endElement("input");

    String contextPath = context.getExternalContext().getRequestContextPath();
    writer.startElement("script", this);
    writer.writeAttribute("type", "text/javascript", null);
    writer.writeAttribute("src", 
      contextPath + "/plugins/savescroll/savescroll.js", null);
    writer.endElement("script");
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[5];
    values[0] = super.saveState(context);
    values[1] = _value;
    values[2] = _resetIfError;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _value = (Integer)values[1];
    _resetIfError = (Boolean)values[2];
  }
  
  private boolean isFacesError(FacesContext context)
  {
    Iterator itMessages = context.getMessages();
    while (itMessages.hasNext())
    {
      FacesMessage facesMessage = (FacesMessage)itMessages.next();
      if (facesMessage.getSeverity() == FacesMessage.SEVERITY_ERROR || 
        facesMessage.getSeverity() == FacesMessage.SEVERITY_FATAL)
      {
        return true;
      }
    }
    return false;
  }
}
