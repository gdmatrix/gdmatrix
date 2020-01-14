package org.santfeliu.faces.convert;

import java.io.Serializable;
import java.util.Date;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author blanquepa
 */
public class DateTimeConverter implements Converter, Serializable
{
  private String userFormat = "dd/MM/yyyy HH:mm:ss";
  private String internalFormat = "yyyyMMddHHmmss";

  public DateTimeConverter()
  {
  }
  
  public DateTimeConverter(String userFormat)
  {
    this.userFormat = userFormat;
  }

  public DateTimeConverter(String internalFormat, String userFormat)
  {
    this.internalFormat = internalFormat;
    this.userFormat = userFormat;
  }

  public String getUserFormat()
  {
    return userFormat;
  }

  public void setUserFormat(String dateFormat)
  {
    this.userFormat = dateFormat;
  }

  public String getAsString(FacesContext fc, UIComponent uic, Object o)
  {
    String internalDate = (String)o;
    if (uic != null)
    {
      String iFormat = (String)uic.getAttributes().get("internalFormat");
      if (iFormat != null)
        internalFormat = iFormat;
      String uFormat = (String)uic.getAttributes().get("userFormat");
      if (uFormat != null)
        userFormat = uFormat;
    }
    if (internalDate != null)
    {
      Date date = TextUtils.parseUserDate(internalDate, internalFormat);
      return TextUtils.formatDate(date, userFormat, fc.getViewRoot().getLocale());
    }
    else
      return internalDate != null ? internalDate : null;
  }

  public Object getAsObject(FacesContext fc, UIComponent uic, String string)
  {
    if (uic != null)
    {
      String iFormat = (String)uic.getAttributes().get("internalFormat");
      if (iFormat != null)
        internalFormat = iFormat;
      String uFormat = (String)uic.getAttributes().get("userFormat");
      if (uFormat != null)
        userFormat = uFormat;
    }
    Date date = TextUtils.parseUserDate(string, userFormat);
    if (date != null)
    {
      return TextUtils.formatDate(date, internalFormat);
    }
    else
      return string;
  }


}
