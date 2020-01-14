package org.santfeliu.faces.menu.view;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

public class HtmlNavigationPathTag extends UIComponentTag
{
  private String value;
  private String var;
  private String baseMid;
  private String mode;
  private String style;
  private String styleClass;
  private String maxDepth;  
  private String renderMode;

  public HtmlNavigationPathTag()
  {
  }
  
  public String getComponentType()
  {
    return "NavigationPath";
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

  public String getMaxDepth()
  {
    return maxDepth;
  }

  public void setMaxDepth(String maxDepth)
  {
    this.maxDepth = maxDepth;
  }

  public String getRenderMode()
  {
    return renderMode;
  }

  public void setRenderMode(String renderMode)
  {
    this.renderMode = renderMode;
  }
  
  @Override
  protected void setProperties(UIComponent component)
  {
    super.setProperties(component);

    FacesContext context = FacesContext.getCurrentInstance();
    HtmlNavigationPath nav = (HtmlNavigationPath)component;
    if (var != null)
    {
      nav.getAttributes().put("var", var);
    }
    UIComponentTagUtils.setStringProperty(context, component, "value", value);
    UIComponentTagUtils.setStringProperty(context, component, "baseMid", baseMid);
    UIComponentTagUtils.setStringProperty(context, component, "mode", mode);
    UIComponentTagUtils.setStringProperty(context, component, "style", style);
    UIComponentTagUtils.setStringProperty(context, component, "styleClass", styleClass);
    UIComponentTagUtils.setIntegerProperty(context, component, "maxDepth", maxDepth);
    UIComponentTagUtils.setStringProperty(context, component, "renderMode", renderMode);    
  }

  @Override
  public void release()
  {
    super.release();
    value = null;
    var = null;
    baseMid = null;
    mode = null;
    style = null;
    styleClass = null;
    maxDepth = null;
    renderMode = null;
  }
}
