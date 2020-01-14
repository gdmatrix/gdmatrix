package org.santfeliu.faces.menu.view;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import org.apache.commons.lang.StringUtils;

public class HtmlNavigationPath extends UIComponentBase
{
  public static String ACTIVE = "ACTIVE";
  public static String PASSIVE = "PASSIVE";

  private Object _value;
  private String _var;
  private String _baseMid;  
  private String _mode = ACTIVE;
  private String _style;
  private String _styleClass;
  private Integer _maxDepth; 
  private String _renderMode;

  public String getFamily()
  {
    return "NavigationPath";
  }

  public HtmlNavigationPath()
  {
  }

  public void setValue(Object value)
  {
    if (value instanceof String)
    {
      _value = value;
    }
  }

  public Object getValue()
  {
    if (_value != null) return _value;
    ValueBinding vb = getValueBinding("value");
    return vb != null ? (Object)vb.getValue(getFacesContext()) : null;
  }

  public void setVar(String var)
  {
    _var = var;
  }

  public String getVar()
  {
    return _var;
  }

  public void setBaseMid(String baseMid)
  {
    _baseMid = baseMid;
  }

  public String getBaseMid()
  {
    if (_baseMid != null) return _baseMid;
    ValueBinding vb = getValueBinding("baseMid");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setMode(String mode)
  {
    _mode = mode;
  }

  public String getMode()
  {
    if (_mode != null) return _mode;
    ValueBinding vb = getValueBinding("mode");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setStyle(String style)
  {
    _style = style;
  }

  public String getStyle()
  {
    if (_style != null) return _style;
    ValueBinding vb = getValueBinding("style");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setStyleClass(String styleClass)
  {
    _styleClass = styleClass;
  }

  public String getStyleClass()
  {
    if (_styleClass != null) return _styleClass;
    ValueBinding vb = getValueBinding("styleClass");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setMaxDepth(Integer maxDepth)
  {
    this._maxDepth = maxDepth;
  }
  
  public Integer getMaxDepth()
  {
    if (_maxDepth != null) return _maxDepth.intValue();
    ValueBinding vb = getValueBinding("maxDepth");
    if (vb != null)    
    {
      try
      {
        String sNumber = (String)vb.getValue(getFacesContext());      
        return Integer.parseInt(sNumber);
      }
      catch (NumberFormatException ex) { }
    }
    return null;    
  }  
  
  public void setRenderMode(String renderMode)
  {
    _renderMode = renderMode;
  }

  public String getRenderMode()
  {
    if (_renderMode != null) return _renderMode;
    ValueBinding vb = getValueBinding("renderMode");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }  
  
  @Override
  public boolean getRendersChildren()
  {
    return true;
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[9];
    values[0] = super.saveState(context);
    values[1] = _value; 
    values[2] = _var; 
    values[3] = _baseMid; 
    values[4] = _mode;
    values[5] = _style;
    values[6] = _styleClass;
    values[7] = _maxDepth;  
    values[8] = _renderMode;
    return values;
  }
  
  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _value = values[1];
    _var = (String)values[2];
    _baseMid = (String)values[3];
    _mode = (String)values[4];
    _style = (String)values[5];
    _styleClass = (String)values[6];
    _maxDepth = (Integer)values[7];
    _renderMode = (String)values[8];
  }
  
  @Override
  public String getRendererType()
  {
    String renderMode = getRenderMode();
    if (renderMode == null)
      return "SpanNavigationPath";
    else
      return StringUtils.capitalize(renderMode) + "NavigationPath";
  }
}
