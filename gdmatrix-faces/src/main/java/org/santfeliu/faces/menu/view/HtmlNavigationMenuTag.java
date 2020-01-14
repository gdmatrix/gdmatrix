package org.santfeliu.faces.menu.view;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

public class HtmlNavigationMenuTag extends UIComponentTag
{
  private String value;
  private String var;
  private String baseMid;
  private String layout;
  private String orientation;
  private String mode;
  private String style;
  private String styleClass;
  private String maxVisibleMenuItems;
  private String selectedStyleClass;
  private String unselectedStyleClass;

  public HtmlNavigationMenuTag()
  {
  }
  
  public String getComponentType()
  {
    return "NavigationMenu";
  }
  
  public String getRendererType()
  {
    return null;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public String getValue()
  {
    return value;
  }

  public void setVar(String var)
  {
    this.var = var;
  }

  public String getVar()
  {
    return var;
  }

  public void setBaseMid(String baseMid)
  {
    this.baseMid = baseMid;
  }

  public String getBaseMid()
  {
    return baseMid;
  }
  
  public void setLayout(String layout)
  {
    this.layout = layout;
  }
  
  public String getLayout()
  {
    return layout;
  }
  
  public void setOrientation(String orientation)
  {
    this.orientation = orientation;
  }

  public String getOrientation()
  {
    return orientation;
  }

  public void setMode(String mode)
  {
    this.mode = mode;
  }

  public String getMode()
  {
    return mode;
  }
    
  public void setStyle(String style)
  {
    this.style = style;
  }

  public String getStyle()
  {
    return style;
  }

  public void setStyleClass(String styleClass)
  {
    this.styleClass = styleClass;
  }

  public String getStyleClass()
  {
    return styleClass;
  }
  
  public void setSelectedStyleClass(String selectedStyleClass)
  {
    this.selectedStyleClass = selectedStyleClass;
  }

  public String getSelectedStyleClass()
  {
    return selectedStyleClass;
  }

  public void setUnselectedStyleClass(String unselectedStyleClass)
  {
    this.unselectedStyleClass = unselectedStyleClass;
  }

  public String getUnselectedStyleClass()
  {
    return unselectedStyleClass;
  }

  public String getMaxVisibleMenuItems()
  {
    return maxVisibleMenuItems;
  }

  public void setMaxVisibleMenuItems(String maxVisibleMenuItems)
  {
    this.maxVisibleMenuItems = maxVisibleMenuItems;
  }

  @Override
  protected void setProperties(UIComponent component)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Application application = context.getApplication();
    super.setProperties(component);
    HtmlNavigationMenu nav = (HtmlNavigationMenu)component;
    if (var != null)
    {
      nav.getAttributes().put("var", var);
    }
    UIComponentTagUtils.setStringProperty(context, component, "value", value);
    UIComponentTagUtils.setStringProperty(context, component, "baseMid", baseMid);
    UIComponentTagUtils.setStringProperty(context, component, "layout", layout);
    UIComponentTagUtils.setStringProperty(context, component, "orientation", orientation);
    UIComponentTagUtils.setStringProperty(context, component, "mode", mode);
    UIComponentTagUtils.setStringProperty(context, component, "style", style);
    UIComponentTagUtils.setStringProperty(context, component, "styleClass", styleClass);
    UIComponentTagUtils.setStringProperty(context, component, "selectedStyleClass", selectedStyleClass);
    UIComponentTagUtils.setStringProperty(context, component, "unselectedStyleClass", unselectedStyleClass);
    UIComponentTagUtils.setIntegerProperty(context, component, "maxVisibleMenuItems", maxVisibleMenuItems);
  }

  @Override
  public void release()
  {
    super.release();
    value = null;
    var = null;
    baseMid = null;
    layout = null;
    orientation = null;
    mode = null;
    style = null;
    styleClass = null;
    selectedStyleClass = null;
    unselectedStyleClass = null;
    maxVisibleMenuItems = null;
  }
}
