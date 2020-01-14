package org.santfeliu.faces.convert;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

public class EnumConverter implements Converter, Serializable
{
  public EnumConverter()
  {
  }

  public Object getAsObject(FacesContext context, 
    UIComponent component, String value) throws ConverterException
  {
    if (value == null || value.trim().length() == 0)
    {
      return null;
    }
    String enumClassName = (String)component.getAttributes().get("enum");
    if (enumClassName == null)
      throw new ConverterException("enum class not defined");
    Class<Enum> enumClass = null;
    try
    {
      enumClass = (Class<Enum>)Class.forName(enumClassName);
    }
    catch (Exception e)
    {
      throw new ConverterException("invalid enum class");
    }
    Enum[] constants = enumClass.getEnumConstants();
    boolean found = false;
    int i = 0;
    while (!found && i < constants.length)
    {
      System.out.println(constants[i].toString());
      if (constants[i].toString().equals(value)) found = true;
      else i++;
    }
    if (!found) throw new ConverterException("invalid value");
    return constants[i];
  }

  public String getAsString(FacesContext context, 
    UIComponent component, Object value) throws ConverterException
  {
    if (value == null) return "";
    return String.valueOf(value);
  }
}

