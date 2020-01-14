package org.santfeliu.faces.clock;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.servlet.http.HttpServletRequest;
import org.santfeliu.web.HttpUtils;

/**
 *
 * @author realor
 */
public class HtmlClock extends UIOutput
{
  private static final String CLOCK_SCRIPT = "clock_script";
  private String _style;
  private String _styleClass;
  private String _format;

  public HtmlClock()
  {
    setRendererType(null);
  }

  @Override
  public String getFamily()
  {
    return "Clock";
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

  public void setFormat(String format)
  {
    this._format = format;
  }

  public String getFormat()
  {
    if (_format != null) return _format;
    ValueBinding vb = getValueBinding("format");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();

    Map requestMap = context.getExternalContext().getRequestMap();
    if (!requestMap.containsKey(CLOCK_SCRIPT))
    {
      HttpServletRequest request =
        (HttpServletRequest)context.getExternalContext().getRequest();
      String contextPath = request.getContextPath();
      writer.startElement("script", this);
      writer.writeAttribute("type", "text/javascript", null);
      writer.writeAttribute("src", 
        contextPath + "/plugins/clock/clock.js", null);
      writer.endElement("script");

      String clockUrl = HttpUtils.getContextURL(request) + "/clock";
      
      writer.startElement("script", this);
      writer.writeAttribute("type", "text/javascript", null);
      writer.writeText("setClockUrl(\"" + clockUrl + "\")", null);
      writer.endElement("script");
      requestMap.put(CLOCK_SCRIPT, "true");
    }

    String clockId = getId();
    String format = getFormat();
    String sformat;
    if ("date".equals(format)) sformat = "dd/MM/yyyy";
    else if ("time".equals(format)) sformat = "HH:mm:ss";
    else sformat = "dd/MM/yyyy HH:mm:ss";
    
    SimpleDateFormat df = new SimpleDateFormat(sformat);
    String time = df.format(new Date());

    writer.startElement("span", this);
    writer.writeAttribute("id", clockId, null);

    String style = getStyle();
    if (style != null)
    writer.writeAttribute("style", style, null);

    String styleClass = getStyleClass();
    if (styleClass != null)
    writer.writeAttribute("class", styleClass, null);

    writer.writeText(time, null);
    writer.endElement("span");
    
    format = (format == null) ? "null" : "\"" + format + "\"";

    writer.startElement("script", this);
    writer.writeAttribute("type", "text/javascript", null);
    writer.writeText("addClock(\"" + clockId + "\", " + format + ")", null);
    writer.endElement("script");
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[4];
    values[0] = super.saveState(context);
    values[1] = _style;
    values[2] = _styleClass;
    values[3] = _format;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _style = (String)values[1];
    _styleClass = (String)values[2];
    _format = (String)values[3];
  }  
}
