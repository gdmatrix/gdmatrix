package org.santfeliu.faces.convert;

import java.io.Serializable;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;

/**
 *
 * @author blanquepa
 */
public class TypeIdConverter implements Converter, Serializable
{
  public TypeIdConverter()
  {
  }
  
  public String getAsString(FacesContext fc, UIComponent uic, Object o)
  {
    String typeId = (String)o;
    Type type = TypeCache.getInstance().getType(typeId);
    if (type != null)
      return type.getDescription();
    else
      return typeId;
  }

  public Object getAsObject(FacesContext fc, UIComponent uic, String string)
  {
    return string;
  }

}
