package org.santfeliu.faces.selectobject;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;

import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;


public class SelectObjectTag extends UIComponentTag
{
  // main properties
  private Object itemValue;
  private String store;
  private String size;
  private String listValues;
  private String submitOnchange;
  
  // UICommand attributes
  private String action;
  private String immediate;
  private String actionListener;

  
  // style properties
  private String style;
  private String styleClass;

  public SelectObjectTag()
  {
  }

  public String getComponentType()
  {
    return "SelectObject";
  }

  public String getRendererType()
  {
    return null;
  }
  
  public void setAction(String action)
  {
    this.action = action;
  }

  public String getAction()
  {
    return action;
  }

  public void setSubmitOnchange(String submitOnchange)
  {
    this.submitOnchange = submitOnchange;
  }

  public String getSubmitOnchange()
  {
    return submitOnchange;
  }
  
  public void setImmediate(String immediate)
  {
    this.immediate = immediate;
  }

  public String getImmediate()
  {
    return immediate;
  }

  public void setActionListener(String actionListener)
  {
    this.actionListener = actionListener;
  }

  public String getActionListener()
  {
    return actionListener;
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

  public void setItemValue(Object value)
  {
    this.itemValue = value;
  }

  public Object getItemValue()
  {
    return itemValue;
  }

  public void setStore(String store)
  {
    this.store = store;
  }

  public String getStore()
  {
    return store;
  }
  
  public void setSize(String size)
  {
    this.size = size;
  }

  public String getSize()
  {
    return size;
  }
  
  public void setListValues(String listValues)
  {
    this.listValues = listValues;
  }

  public String getListValues()
  {
    return listValues;
  }
  
   protected void setProperties(UIComponent component)
   {
     try
     {
       FacesContext context = FacesContext.getCurrentInstance();
       super.setProperties(component);
       
       UIComponentTagUtils.setStringProperty(context, component, "size", size);
       UIComponentTagUtils.setStringProperty(context, component, "store", store);
       UIComponentTagUtils.setStringProperty(context, component, "itemValue", String.valueOf(itemValue));
       UIComponentTagUtils.setStringProperty(context, component, "listValues", listValues);
       UIComponentTagUtils.setBooleanProperty(context, component, "submitOnchange", submitOnchange);
       
       UIComponentTagUtils.setActionProperty(context, component, action);
       UIComponentTagUtils.setActionListenerProperty(context, component, actionListener);
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
    style = null;
    styleClass = null;
    store = null;
    itemValue = null;
    listValues = null;
    size = null;
    action = null;
    immediate = null;
    actionListener = null;
    submitOnchange = null;

  }
}
