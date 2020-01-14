package org.santfeliu.faces.convert;

import java.io.Serializable;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 *
 * @author realor
 */
public class IntervalConverter implements Converter, Serializable
{
  public Object getAsObject(FacesContext fc, UIComponent uic, String value)
  {
    if (value == null) return null;
    int totalSeconds = 0;
    value = value.trim();
    boolean negative;
    if (value.startsWith("-"))
    {
      value = value.substring(1);
      negative = true;
    }
    else
    {
      negative = false;
    }
    
    String[] parts = value.split(" ");
    for (String part : parts)
    {
      try
      {
        if (part.endsWith("h"))
        {
          part = part.substring(0, part.length() - 1);
          totalSeconds += Integer.parseInt(part) * 3600;
        }
        else if (part.endsWith("m"))
        {
          part = part.substring(0, part.length() - 1);
          totalSeconds += Integer.parseInt(part) * 60;
        }
        else if (part.endsWith("s"))
        {
          part = part.substring(0, part.length() - 1);
          totalSeconds += Integer.parseInt(part);
        }
      }
      catch (NumberFormatException ex)
      {
        // Ignore
      }
    }
    return negative ? -totalSeconds : totalSeconds;
  }

  public String getAsString(FacesContext fc, UIComponent uic, Object value)
  {
    if (value == null) return null;
    
    int totalSeconds = ((Number)value).intValue();
    if (totalSeconds == 0) return "0";
    boolean negative;
    if (totalSeconds < 0) 
    {
      totalSeconds = -totalSeconds;
      negative = true;
    }
    else 
    {
      negative = false;
    }
    int hours = totalSeconds / 3600;
    int minutes = (totalSeconds % 3600) / 60;
    int seconds = (totalSeconds % 3600) % 60;
    
    StringBuilder buffer = new StringBuilder();
    boolean timeSet = false;
    if (negative)
    {
      buffer.append("-");
    }
    else
    {
      String positive = (String)uic.getAttributes().get("positiveSignum");
      if ("true".equals(positive))
      {
        buffer.append("+");
      }
    }
    if (hours > 0)
    {
      buffer.append(hours);
      buffer.append("h");
      timeSet = true;
    }
    if (minutes > 0)
    {
      if (timeSet) buffer.append(" ");
      else timeSet = true;
      buffer.append(minutes);
      buffer.append("m");
    }
    if (seconds > 0)
    {
      if (timeSet) buffer.append(" ");
      buffer.append(seconds);
      buffer.append("s");      
    }
    return buffer.toString();
  }
}
