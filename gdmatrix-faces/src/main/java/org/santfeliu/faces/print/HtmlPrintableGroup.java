package org.santfeliu.faces.print;

import java.io.IOException;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.servlet.ServletContext;

public class HtmlPrintableGroup extends UIComponentBase
{
  private String _name;
 
  public HtmlPrintableGroup()
  {
  }
  
  public String getFamily()
  {
    return "PrintableGroup";
  }

  public void setName(String name)
  {
    this._name = name;
  }

  public String getName()
  {
    if (_name != null) return _name;
    ValueBinding vb = getValueBinding("name");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    if (!isRendered()) return;
    ResponseWriter writer = context.getResponseWriter();
    writer.startElement("script", this);
    ServletContext servletContext = 
      (ServletContext)context.getExternalContext().getContext();
    String contextPath = servletContext.getContextPath();
    writer.writeAttribute("src", contextPath + "/plugins/print/print.js", null);
    writer.writeAttribute("type", "text/javascript", null);
    writer.writeText("", null);
    writer.endElement("script");
    
    writer.startElement("div", this);
    writer.writeAttribute("id", getName(), null);
  }

  @Override
  public void encodeEnd(FacesContext context) throws IOException
  {
    if (!isRendered()) return;
    ResponseWriter writer = context.getResponseWriter();
    writer.endElement("div");
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[2];
    values[0] = super.saveState(context);
    values[1] = _name;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _name =(String)values[1];
  }
}
