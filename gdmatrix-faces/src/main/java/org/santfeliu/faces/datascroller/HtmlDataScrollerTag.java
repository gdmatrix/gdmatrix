package org.santfeliu.faces.datascroller;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

/**
 *
 * @author real
 */
public class HtmlDataScrollerTag extends UIComponentTag
{
  private String _for;
  private String pageIndexVar;
  private String pageCountVar;
  private String rowsCountVar;
  private String firstRowIndexVar;
  private String lastRowIndexVar;
  private String style;
  private String styleClass;

  public String getFor()
  {
    return _for;
  }

  public void setFor(String _for)
  {
    this._for = _for;
  }

  public String getFirstRowIndexVar()
  {
    return firstRowIndexVar;
  }

  public void setFirstRowIndexVar(String firstRowIndexVar)
  {
    this.firstRowIndexVar = firstRowIndexVar;
  }

  public String getLastRowIndexVar()
  {
    return lastRowIndexVar;
  }

  public void setLastRowIndexVar(String lastRowIndexVar)
  {
    this.lastRowIndexVar = lastRowIndexVar;
  }

  public String getPageCountVar()
  {
    return pageCountVar;
  }

  public void setPageCountVar(String pageCountVar)
  {
    this.pageCountVar = pageCountVar;
  }

  public String getPageIndexVar()
  {
    return pageIndexVar;
  }

  public void setPageIndexVar(String pageIndexVar)
  {
    this.pageIndexVar = pageIndexVar;
  }

  public String getRowsCountVar()
  {
    return rowsCountVar;
  }

  public void setRowsCountVar(String rowsCountVar)
  {
    this.rowsCountVar = rowsCountVar;
  }

  public String getStyle()
  {
    return style;
  }

  public void setStyle(String style)
  {
    this.style = style;
  }

  public String getStyleClass()
  {
    return styleClass;
  }

  public void setStyleClass(String styleClass)
  {
    this.styleClass = styleClass;
  }

  @Override
  public String getComponentType()
  {
    return "DataScroller";
  }

  @Override
  public String getRendererType()
  {
    return null;
  }

  @Override
  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      super.setProperties(component);
      UIComponentTagUtils.setStringProperty(context, component, "for", _for);
      UIComponentTagUtils.setStringProperty(context, component, "pageIndexVar", pageIndexVar);
      UIComponentTagUtils.setStringProperty(context, component, "pageCountVar", pageCountVar);
      UIComponentTagUtils.setStringProperty(context, component, "rowsCountVar", rowsCountVar);
      UIComponentTagUtils.setStringProperty(context, component, "firstRowIndexVar", firstRowIndexVar);
      UIComponentTagUtils.setStringProperty(context, component, "lastRowIndexVar", lastRowIndexVar);
      UIComponentTagUtils.setStringProperty(context, component, "style", style);
      UIComponentTagUtils.setStringProperty(context, component, "styleClass", styleClass);
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
    _for = null;
    pageIndexVar = null;
    pageCountVar = null;
    rowsCountVar = null;
    firstRowIndexVar = null;
    lastRowIndexVar = null;
    style = null;
    styleClass = null;
  }
}
