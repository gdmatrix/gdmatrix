package org.santfeliu.faces.heading;

import java.io.IOException;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;


public class HtmlHeading extends UIComponentBase
{
  private Integer _level;
  private String _style;
  private String _styleClass;  

  public HtmlHeading()
  {
  }
  
  public String getFamily()
  {
    return "Heading";
  }

  public void setLevel(Integer level)
  {
    this._level = level;
  }

  public Integer getLevel()
  {
    if (_level != null) return _level;
    ValueBinding vb = getValueBinding("level");
    if (vb == null) return 1;
    
    Integer level = null;
    Object value = vb.getValue(getFacesContext());
    if (value instanceof String)
      level = Integer.valueOf((String)value);
    else  
      level = (Integer)value;
    return level == null ? 1 : level;
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
  public void encodeBegin(FacesContext context) throws IOException
  {
    try
    {
      if (!isRendered()) return;
      ResponseWriter writer = context.getResponseWriter();
      int level = getLevel();

      writer.startElement("h" + level, this);
      String style = getStyle();
      if (style != null)
      {
        writer.writeAttribute("style", style, null);
      }
      String styleClass = getStyleClass();
      if (styleClass != null)
      {
        writer.writeAttribute("class", styleClass, null);
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  @Override
  public void encodeEnd(FacesContext context) throws IOException
  {
    if (!isRendered()) return;
    ResponseWriter writer = context.getResponseWriter();
    int level = getLevel();
    writer.endElement("h" + level);
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[4];
    values[0] = super.saveState(context);
    values[1] = _level;
    values[2] = _style;
    values[3] = _styleClass;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _level = (Integer)values[1];
    _style = (String)values[2];
    _styleClass = (String)values[3];
  }
}
