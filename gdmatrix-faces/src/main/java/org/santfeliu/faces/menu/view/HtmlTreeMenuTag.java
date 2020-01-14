package org.santfeliu.faces.menu.view;


import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

public class HtmlTreeMenuTag extends UIComponentTag
{
  private String value;
  private String expandDepth;
  private String expandSelected;
  private String expandedMenuItems;
  private String var;
  private String baseMid;
  private String mode;
  private String style;
  private String styleClass;
  private String menuStyleClass;
  private String selectedStyleClass;
  private String unselectedStyleClass;
  private String expandImageUrl;
  private String collapseImageUrl;
  private String headingsRender;
  private String headingsBaseLevel;
  private String headingsStyleClass;
  private String enableDropdownButton;

  public HtmlTreeMenuTag()
  {
  }
  
  public String getComponentType()
  {
    return "TreeMenu";
  }
  
  @Override
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

  public String getExpandDepth()
  {
    return expandDepth;
  }

  public void setExpandDepth(String expandDepth)
  {
    this.expandDepth = expandDepth;
  }

  public String getExpandSelected()
  {
    return expandSelected;
  }

  public void setExpandSelected(String expandSelected)
  {
    this.expandSelected = expandSelected;
  }

  public String getHeadingsRender()
  {
    return headingsRender;
  }

  public void setHeadingsRender(String headingsRender)
  {
    this.headingsRender = headingsRender;
  }

  public String getHeadingsBaseLevel()
  {
    return headingsBaseLevel;
  }

  public void setHeadingsBaseLevel(String headingsBaseLevel)
  {
    this.headingsBaseLevel = headingsBaseLevel;
  }

  public String getHeadingsStyleClass()
  {
    return headingsStyleClass;
  }

  public void setHeadingsStyleClass(String headingsStyleClass)
  {
    this.headingsStyleClass = headingsStyleClass;
  }

  public void setExpandedMenuItems(String expandedMenuItems)
  {
    this.expandedMenuItems = expandedMenuItems;
  }

  public String getExpandedMenuItems()
  {
    return expandedMenuItems;
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

  public String getMenuStyleClass()
  {
    return menuStyleClass;
  }

  public void setMenuStyleClass(String menuStyleClass)
  {
    this.menuStyleClass = menuStyleClass;
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

  public String getExpandImageUrl()
  {
    return expandImageUrl;
  }

  public void setExpandImageUrl(String expandImageUrl)
  {
    this.expandImageUrl = expandImageUrl;
  }

  public String getCollapseImageUrl()
  {
    return collapseImageUrl;
  }

  public void setCollapseImageUrl(String collapseImageUrl)
  {
    this.collapseImageUrl = collapseImageUrl;
  }

  public String getEnableDropdownButton()
  {
    return enableDropdownButton;
  }

  public void setEnableDropdownButton(String enableDropdownButton)
  {
    this.enableDropdownButton = enableDropdownButton;
  }

  @Override
  protected void setProperties(UIComponent component)
  {
    super.setProperties(component);

    FacesContext context = FacesContext.getCurrentInstance();
    Application application = context.getApplication();    
    HtmlTreeMenu nav = (HtmlTreeMenu)component;
    if (var != null)
    {
      nav.getAttributes().put("var", var);
    }
    if (expandedMenuItems != null)
    {
      if (isValueReference(expandedMenuItems))
      {
        ValueBinding vb = application.createValueBinding(expandedMenuItems);
        nav.setValueBinding("expandedMenuItems", vb);
      }
    }
    UIComponentTagUtils.setIntegerProperty(context, component, "expandDepth", expandDepth);
    UIComponentTagUtils.setBooleanProperty(context, component, "expandSelected", expandSelected);
    UIComponentTagUtils.setBooleanProperty(context, component, "headingsRender", headingsRender);
    UIComponentTagUtils.setIntegerProperty(context, component, "headingsBaseLevel", headingsBaseLevel);
    UIComponentTagUtils.setStringProperty(context, component, "value", value);
    UIComponentTagUtils.setStringProperty(context, component, "baseMid", baseMid);
    UIComponentTagUtils.setStringProperty(context, component, "mode", mode);
    UIComponentTagUtils.setStringProperty(context, component, "style", style);
    UIComponentTagUtils.setStringProperty(context, component, "styleClass", styleClass);
    UIComponentTagUtils.setStringProperty(context, component, "menuStyleClass", menuStyleClass);
    UIComponentTagUtils.setStringProperty(context, component, "selectedStyleClass", selectedStyleClass);
    UIComponentTagUtils.setStringProperty(context, component, "unselectedStyleClass", unselectedStyleClass);
    UIComponentTagUtils.setStringProperty(context, component, "expandImageUrl", expandImageUrl);
    UIComponentTagUtils.setStringProperty(context, component, "collapseImageUrl", collapseImageUrl);
    UIComponentTagUtils.setStringProperty(context, component, "headingsStyleClass", headingsStyleClass);
    UIComponentTagUtils.setBooleanProperty(context, component, "enableDropdownButton", enableDropdownButton);
  }

  @Override
  public void release()
  {
    super.release();
    value = null;
    expandDepth = null;
    expandSelected = null;
    headingsRender = null;
    headingsBaseLevel = null;
    expandedMenuItems = null;
    var = null;
    baseMid = null;
    mode = null;
    style = null;
    styleClass = null;
    menuStyleClass = null;
    headingsStyleClass = null;
    selectedStyleClass = null;
    unselectedStyleClass = null;
    expandImageUrl = null;
    collapseImageUrl = null;
    enableDropdownButton = null;
  }
}
