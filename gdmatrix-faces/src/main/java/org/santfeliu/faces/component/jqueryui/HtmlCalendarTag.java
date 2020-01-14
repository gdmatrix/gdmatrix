package org.santfeliu.faces.component.jqueryui;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

/**
 *
 * @author blanquepa
 */
public class HtmlCalendarTag extends org.santfeliu.faces.component.HtmlCalendarTag
{
  private String singleInput;  
  private String theme;
  private String dayLabel;
  private String hourLabel;

  public String getSingleInput()
  {
    return singleInput;
  }

  public void setSingleInput(String singleInput)
  {
    this.singleInput = singleInput;
  }

  public String getTheme()
  {
    return theme;
  }

  public void setTheme(String theme)
  {
    this.theme = theme;
  }

  public String getDayLabel()
  {
    return dayLabel;
  }

  public void setDayLabel(String dayLabel)
  {
    this.dayLabel = dayLabel;
  }

  public String getHourLabel()
  {
    return hourLabel;
  }

  public void setHourLabel(String hourLabel)
  {
    this.hourLabel = hourLabel;
  }
  
  @Override
  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      super.setProperties(component);

      UIComponentTagUtils.setBooleanProperty(context, component, 
        "singleInput", singleInput);
      UIComponentTagUtils.setStringProperty(context, component, 
        "theme", theme);      
      UIComponentTagUtils.setStringProperty(context, component, 
        "dayLabel", dayLabel);
      UIComponentTagUtils.setStringProperty(context, component, 
        "hourLabel", hourLabel);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }  
  
  @Override
  public void release()
  {
    super.release();
    singleInput = null;
    theme = null;
    dayLabel = null;
    hourLabel = null;
  }  
}
