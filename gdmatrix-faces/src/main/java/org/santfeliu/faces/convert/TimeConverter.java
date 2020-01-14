package org.santfeliu.faces.convert;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author realor
 */
public class TimeConverter implements Converter
{
  public Object getAsObject(FacesContext context, UIComponent component, 
    String value)
  {
    return TextUtils.parseUserTime(value);
  }

  public String getAsString(FacesContext context, UIComponent component, 
    Object value)
  {
    return TextUtils.formatInternalTime((String)value);
  }
}
