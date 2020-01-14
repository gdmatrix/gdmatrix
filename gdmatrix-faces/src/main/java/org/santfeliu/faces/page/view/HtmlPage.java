package org.santfeliu.faces.page.view;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;

import javax.faces.el.ValueBinding;


public class HtmlPage extends UIComponentBase
{
  private String _template; // templateName
  private Boolean _match = Boolean.TRUE; // match user template

  public HtmlPage()
  {
  }
  
  public String getFamily()
  {
    return "Page";
  }
  
  public void setTemplate(String template)
  {
    _template = template;
  }
  
  public String getTemplate()
  {
    if (_template != null) return _template;
    ValueBinding vb = getValueBinding("template");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;  
  }

  public void setMatch(boolean match)
  {
    _match = Boolean.valueOf(match);  
  }
  
  public boolean isMatch()
  {
    return _match.booleanValue();
  }
  
  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[3];
    values[0] = super.saveState(context);
    values[1] = _template;
    values[2] = _match;
    return values;
  }
  
  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _template = (String)values[1];
    _match = (Boolean)values[2];
  }
  
  @Override
  public String toString()
  {
    return super.toString() + ": " + _template + " " +
      (_match.booleanValue() ? "[match]" : "[no match]");
  }
}
