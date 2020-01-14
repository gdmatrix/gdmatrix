package org.santfeliu.faces.component;

import java.io.IOException;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import org.apache.myfaces.component.html.ext.HtmlCommandLink;



/**
 *
 * @author blanquepa
 */
public class HtmlAriaCommandLink extends HtmlCommandLink
{
  private String _ariaLabel;
  private Boolean _ariaHidden;
  
  public HtmlAriaCommandLink()
  {
    setRendererType("org.apache.myfaces.Link");
  }

  public void setAriaLabel(String _ariaLabel)
  {
    this._ariaLabel = _ariaLabel;
  }
  
  public String getAriaLabel()
  {
    if (_ariaLabel != null)
      return _ariaLabel;
    ValueBinding vb = getValueBinding("ariaLabel");
    return vb != null? (String) vb.getValue(getFacesContext()): null;
  }

  public void setAriaHidden(Boolean ariaHidden)
  {
    this._ariaHidden = ariaHidden;
  }

  public Boolean getAriaHidden()
  {
    if (_ariaHidden != null) return _ariaHidden;
    ValueBinding vb = getValueBinding("ariaHidden");
    return (vb != null ? (Boolean)vb.getValue(getFacesContext()) : 
      Boolean.FALSE);
  }
  
  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    super.encodeBegin(context);
    ResponseWriter writer = context.getResponseWriter();
    if (getAriaLabel() != null)
    {
      writer.writeAttribute("aria-label", getAriaLabel(), null);
    }
    if (getAriaHidden())
    {
      writer.writeAttribute("aria-hidden", "true", null);
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
    Object values[] = new Object[3];
    values[0] = super.saveState(context);
    values[1] = _ariaLabel;
    values[2] = _ariaHidden;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _ariaLabel = (String)values[1];
    _ariaHidden = (Boolean)values[2];
  }
}
