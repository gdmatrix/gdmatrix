package org.santfeliu.faces.widget;

import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;
import static javax.faces.webapp.UIComponentTag.isValueReference;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author realor
 */
public class HtmlWidgetContainerTag extends UIComponentTag
{
  private static final String VALUE_SEPARATOR = "--";  
  
  private String columns;
  private String columnClasses;
  private String columnTitles;
  private String columnRenderAsList;
  private String layout;
  private String style;
  private String styleClass;
  private String dynamic;
  private String draggableWidgets;
  private String updateCallback;
  private String title;
  private String headerDocId;
  private String footerDocId;
  private String translator;
  private String translationGroup;  

  @Override
  public String getComponentType()
  {
    return "WidgetContainer";
  }

  @Override
  public String getRendererType()
  {
    return null;
  }

  public String getColumns()
  {
    return columns;
  }

  public void setColumns(String columns)
  {
    this.columns = columns;
  }

  public String getColumnClasses()
  {
    return columnClasses;
  }

  public void setColumnClasses(String columnClasses)
  {
    this.columnClasses = columnClasses;
  }

  public String getColumnTitles()
  {
    return columnTitles;
  }

  public void setColumnTitles(String columnTitles)
  {
    this.columnTitles = columnTitles;
  }

  public String getColumnRenderAsList()
  {
    return columnRenderAsList;
  }

  public void setColumnRenderAsList(String columnRenderAsList)
  {
    this.columnRenderAsList = columnRenderAsList;
  }

  public String getLayout()
  {
    return layout;
  }

  public void setLayout(String layout)
  {
    this.layout = layout;
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

  public String getDynamic()
  {
    return dynamic;
  }

  public void setDynamic(String dynamic)
  {
    this.dynamic = dynamic;
  }

  public String getDraggableWidgets()
  {
    return draggableWidgets;
  }

  public void setDraggableWidgets(String draggableWidgets)
  {
    this.draggableWidgets = draggableWidgets;
  }

  public String getUpdateCallback()
  {
    return updateCallback;
  }

  public void setUpdateCallback(String updateCallback)
  {
    this.updateCallback = updateCallback;
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getHeaderDocId()
  {
    return headerDocId;
  }

  public void setHeaderDocId(String headerDocId)
  {
    this.headerDocId = headerDocId;
  }

  public String getFooterDocId()
  {
    return footerDocId;
  }

  public void setFooterDocId(String footerDocId)
  {
    this.footerDocId = footerDocId;
  }

  public String getTranslator()
  {
    return translator;
  }

  public void setTranslator(String translator)
  {
    this.translator = translator;
  }

  public String getTranslationGroup()
  {
    return translationGroup;
  }

  public void setTranslationGroup(String translationGroup)
  {
    this.translationGroup = translationGroup;
  }  
  
  @Override
  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      super.setProperties(component);
      if (columns != null)
      {
        if (isValueReference(columns))
        {
          ValueBinding vb = context.getApplication().createValueBinding(columns);
          component.setValueBinding("columns", vb);
        }
        else
        {
          List<String> list = TextUtils.stringToList(columns, VALUE_SEPARATOR);
          component.getAttributes().put("columns", list);
        }
      } 
      if (columnClasses != null)
      {
        if (isValueReference(columnClasses))
        {
          ValueBinding vb = context.getApplication().createValueBinding(columnClasses);
          component.setValueBinding("columnClasses", vb);
        }
        else
        {
          List<String> list = TextUtils.stringToList(columnClasses, VALUE_SEPARATOR);
          component.getAttributes().put("columnClasses", list);
        }
      } 
      if (columnTitles != null)
      {
        if (isValueReference(columnTitles))
        {
          ValueBinding vb = context.getApplication().createValueBinding(columnTitles);
          component.setValueBinding("columnTitles", vb);
        }
        else
        {
          List<String> list = TextUtils.stringToList(columnTitles, VALUE_SEPARATOR);
          component.getAttributes().put("columnTitles", list);
        }
      } 
      if (layout != null)
      {
        if (isValueReference(layout))
        {
          ValueBinding vb = context.getApplication().createValueBinding(layout);
          component.setValueBinding("layout", vb);
        }
        else
        {
          List<String> list = TextUtils.stringToList(layout, VALUE_SEPARATOR);
          component.getAttributes().put("layout", list);
        }
      } 
      if (title != null)
      {
        if (isValueReference(title))
        {
          ValueBinding vb = context.getApplication().createValueBinding(title);
          component.setValueBinding("title", vb);
        }
        else
        {
          List<String> list = TextUtils.stringToList(title, VALUE_SEPARATOR);
          component.getAttributes().put("title", list);
        }
      } 
      if (headerDocId != null)
      {
        if (isValueReference(headerDocId))
        {
          ValueBinding vb = context.getApplication().createValueBinding(headerDocId);
          component.setValueBinding("headerDocId", vb);
        }
        else
        {
          List<String> list = TextUtils.stringToList(headerDocId, VALUE_SEPARATOR);
          component.getAttributes().put("headerDocId", list);
        }
      } 
      if (footerDocId != null)
      {
        if (isValueReference(footerDocId))
        {
          ValueBinding vb = context.getApplication().createValueBinding(footerDocId);
          component.setValueBinding("footerDocId", vb);
        }
        else
        {
          List<String> list = TextUtils.stringToList(footerDocId, VALUE_SEPARATOR);
          component.getAttributes().put("footerDocId", list);
        }
      }      
      UIComponentTagUtils.setStringProperty(context, component, "style", style);
      UIComponentTagUtils.setStringProperty(context, component, "styleClass", styleClass);
      UIComponentTagUtils.setBooleanProperty(context, component, "dynamic", dynamic);      
      if (draggableWidgets != null)
      {
        if (isValueReference(draggableWidgets))
        {
          ValueBinding vb = context.getApplication().createValueBinding(draggableWidgets);
          component.setValueBinding("draggableWidgets", vb);
        }
        else
        {
          UIComponentTagUtils.setBooleanProperty(context, component, "draggableWidgets", draggableWidgets);          
        }
      }
      if (columnRenderAsList != null)
      {
        if (isValueReference(columnRenderAsList))
        {
          ValueBinding vb = context.getApplication().createValueBinding(columnRenderAsList);
          component.setValueBinding("columnRenderAsList", vb);
        }
        else
        {
          List<String> list = TextUtils.stringToList(columnRenderAsList, VALUE_SEPARATOR);
          component.getAttributes().put("columnRenderAsList", list);
        }
      }
      
      UIComponentTagUtils.setStringProperty(context, component, "updateCallback", updateCallback);
      if (translator != null)
      {
        if (isValueReference(translator))
        {
          ValueBinding vb = context.getApplication().createValueBinding(translator);
          component.setValueBinding("translator", vb);
        }
      }      
      UIComponentTagUtils.setStringProperty(context, component, "translationGroup", translationGroup);
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
    columns = null;
    columnClasses = null;
    columnTitles = null;
    columnRenderAsList = null;
    layout = null;
    style = null;
    styleClass = null;
    dynamic = null;
    draggableWidgets = null;
    updateCallback = null;
    title = null;
    headerDocId = null;
    footerDocId = null;
    translator = null;
    translationGroup = null;
  }
}
