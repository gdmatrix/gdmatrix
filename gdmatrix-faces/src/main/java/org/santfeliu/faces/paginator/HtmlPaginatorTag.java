package org.santfeliu.faces.paginator;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;

import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;


public class HtmlPaginatorTag extends UIComponentTag
{
  private String value;
  private String immediate;
  private String pageCount;
  private String visiblePages;
  private String style;
  private String styleClass;
  private String valueChangeListener;

  public HtmlPaginatorTag()
  {
  }

  public String getComponentType()
  {
    return "Paginator";
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

  public void setValueChangeListener(String valueChangeListener)
  {
    this.valueChangeListener = valueChangeListener;
  }

  public String getValueChangeListener()
  {
    return valueChangeListener;
  }

  public void setImmediate(String immediate)
  {
    this.immediate = immediate;
  }

  public String getImmediate()
  {
    return immediate;
  }

  public void setPageCount(String pageCount)
  {
    this.pageCount = pageCount;
  }

  public String getPageCount()
  {
    return pageCount;
  }

  public void setVisiblePages(String visiblePages)
  {
    this.visiblePages = visiblePages;
  }

  public String getVisiblePages()
  {
    return visiblePages;
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

  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      Application application = context.getApplication();
      super.setProperties(component);
      
      HtmlPaginator paginator = (HtmlPaginator)component;

      UIComponentTagUtils.setValueProperty(context, component, value);
      if (pageCount != null)
      {
        if (isValueReference(pageCount))
        {
          ValueBinding vb = application.createValueBinding(pageCount);
          component.setValueBinding("pageCount", vb);
        }
        else paginator.setPageCount(Integer.parseInt(pageCount));
      }
      if (visiblePages != null)
      {
        if (isValueReference(visiblePages))
        {
          ValueBinding vb = application.createValueBinding(visiblePages);
          component.setValueBinding("visiblePages", vb);
        }
        else paginator.setVisiblePages(Integer.parseInt(visiblePages));
      }
      UIComponentTagUtils.setValueChangedListenerProperty(context, component, valueChangeListener);
      UIComponentTagUtils.setBooleanProperty(context, component, "immediate", immediate);
      UIComponentTagUtils.setStringProperty(context, component, "style", style);
      UIComponentTagUtils.setStringProperty(context, component, "styleClass", styleClass);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public void release()
  {
    super.release();
    value = null;
    immediate = null;
    pageCount = null;
    visiblePages = null;
    style = null;
    styleClass = null;
    valueChangeListener = null;
  }
}
