package org.santfeliu.faces.component;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;
import javax.faces.component.UIOutput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author blanquepa
 */
public class HtmlObject extends UIOutput
{
  private String _httpsDisableOnAgent;
  private String _disabledMessage;

  public HtmlObject()
  {
    setRendererType(null);
  }

  public void setHttpsDisableOnAgent(String _httpsDisableOnAgent)
  {
    this._httpsDisableOnAgent = _httpsDisableOnAgent;
  }
  
  public String getHttpsDisableOnAgent()
  {
    if (_httpsDisableOnAgent != null) return _httpsDisableOnAgent;
    ValueBinding vb = getValueBinding("httpsDisableOnAgent");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  @Override
  public boolean getRendersChildren()
  {
    return true;
  }

  public void setDisabledMessage(String _disabledMessage)
  {
    this._disabledMessage = _disabledMessage;
  }

  public String getDisabledMessage()
  {
    if (_disabledMessage != null) return _disabledMessage;
    ValueBinding vb = getValueBinding("disabledMessage");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  @Override
  public String getFamily()
  {
    return "Object";
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    if (!isRendered()) return;

    ResponseWriter writer = context.getResponseWriter();
    ExternalContext extContext = context.getExternalContext();
    Map headerMap = extContext.getRequestHeaderMap();
    String userAgent = (String)headerMap.get("user-agent");

    HttpServletRequest request =
      (HttpServletRequest)context.getExternalContext().getRequest();

    writer.startElement("div", this);
    if (_httpsDisableOnAgent == null || !request.isSecure() ||
        (request.isSecure() &&
          !Pattern.matches(_httpsDisableOnAgent, userAgent)))
    {
      Object value = getValue();
      if (value != null)
      {
        writer.write(value.toString());
      }
    }
    else
    {
      String message = "WIDGET_DISABLED";
      if (getDisabledMessage() != null)
        message = getDisabledMessage();
      writer.writeText(message, null);
    }
    writer.endElement("div");
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[3];
    values[0] = super.saveState(context);
    values[1] = _httpsDisableOnAgent;
    values[2] = _disabledMessage;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _httpsDisableOnAgent = (String)values[1];
    _disabledMessage = (String)values[2];
  }

  public static void main(String[] args)
  {
    System.out.println(Pattern.matches(
      "Mozilla/(.)+",
      "Mozilla/5.0 (Windows; U; Windows NT 5.1; ca; rv:1.9.2.10) " +
      "Gecko/20100914 Firefox/3.6.10 ( .NET CLR 3.5.30729)")
    );
  }
}
