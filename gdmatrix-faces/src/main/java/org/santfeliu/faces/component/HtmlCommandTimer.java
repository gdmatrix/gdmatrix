package org.santfeliu.faces.component;

import java.io.IOException;
import java.util.Map;
import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;

/**
 *
 * @author realor
 */
public class HtmlCommandTimer extends UICommand
{
  private String _time;
  private String _enabled;

  public HtmlCommandTimer()
  {
    setRendererType(null);
  }

  public String getTime()
  {
    if (_time != null) return _time;
    ValueBinding vb = getValueBinding("time");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setTime(String time)
  {
    this._time = time;
  }

  public String getEnabled()
  {
    if (_enabled != null) return _enabled;
    ValueBinding vb = getValueBinding("enabled");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setEnabled(String enabled)
  {
    this._enabled = enabled;
  }

  @Override
  public boolean getRendersChildren()
  {
    return true;
  }

  @Override
  public void decode(FacesContext context)
  {
    Map parameters = context.getExternalContext().getRequestParameterMap();
    String value = (String)parameters.get("refresh");
    if ("yes".equals(value))
    {
      queueEvent(new ActionEvent(this));
    }
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    if (!isRendered()) return;
    ResponseWriter writer = context.getResponseWriter();
    writer.startElement("input", this);
    writer.writeAttribute("type", "hidden", null);
    writer.writeAttribute("name", "refresh", null);
    writer.writeAttribute("value", "", null);
    writer.endElement("input");

    writer.startElement("script", this);
    writer.writeAttribute("type", "text/javascript", null);
    writer.write("function refreshPage(){");
    String enabled = getEnabled();
    if (enabled != null)
      writer.write("if (" + enabled + "){");
    else writer.write("if (true){");
    writer.write("document.forms[0]['refresh'].value='yes';"
      + "document.forms[0].submit();} else {");
    writer.write("setTimeout('refreshPage()', " + getTime() + ");}}");
    writer.write("setTimeout('refreshPage()', " + getTime() + ");");
    writer.endElement("script");
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[3];
    values[0] = super.saveState(context);
    values[1] = _time;
    values[2] = _enabled;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[]) state;
    super.restoreState(context, values[0]);
    _time = (String)values[1];
    _enabled = (String)values[2];
  }
}
