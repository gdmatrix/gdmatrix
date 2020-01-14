package org.santfeliu.faces.convert;

import java.io.Serializable;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import org.santfeliu.doc.util.DocumentUtils;

/**
 *
 * @author blanquepa
 */
public class FileSizeConverter implements Converter, Serializable
{
  public FileSizeConverter()
  {
  }
  
  public String getAsString(FacesContext fc, UIComponent uic, Object o)
  {
    if (o != null)
    {
      Long bytes = Long.valueOf(String.valueOf(o));
      return DocumentUtils.getSizeString(bytes);
    }
    else
      return DocumentUtils.getSizeString(0);
  }

  public Object getAsObject(FacesContext fc, UIComponent uic, String string)
  {
    return string;
  }
}
