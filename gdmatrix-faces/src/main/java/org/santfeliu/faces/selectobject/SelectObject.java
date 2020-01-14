package org.santfeliu.faces.selectobject;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import org.santfeliu.faces.FacesUtils;

public class SelectObject extends UICommand
{
  private String _size;
  private String _store;
  private String _itemValue;
  private Vector _listValues;

  // local value is a Map because could be more than one instance of the component.
  //(ex. if the component is placed inside a datatable).
  private Map _localValue = new HashMap();

  private boolean _submitOnchange;

  private String _style;
  private String _styleClass;
  
  public SelectObject()
  {
  }

  public void setSize(String size)
  {
    this._size = size;
  }

  public String getSize()
  {
    return _size;
  }

  public void setItemValue(Object value)
  {
    this._itemValue = String.valueOf(value);
  }

  public Object getItemValue()
  {
    if (_itemValue != null) return _itemValue;
    ValueBinding vb = getValueBinding("itemValue");
    return vb != null ? vb.getValue(getFacesContext()) : null;
  }

  public void setStore(String value)
  {
    this._store = value;
  }

  public Object getStore()
  {
    if (_store != null) return _store;
    ValueBinding vb = getValueBinding("store");
    return vb != null ? vb.getValue(getFacesContext()) : null;
  }
  
  public void setListValues(Vector listValues)
  {
    this._listValues = listValues;
  }

  public Vector getListValues()
  {
    if (_listValues != null) return _listValues;
    ValueBinding vb = getValueBinding("listValues");
    return vb != null ? (Vector)vb.getValue(getFacesContext()) : null;
  }
  
  public String getStyle()
  {
    if (_style != null) return _style;
    ValueBinding vb = getValueBinding("style");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }
  
  public void setStyle(String style)
  {
    this._style = style;
  }

  public void setStyleClass(String styleClass)
  {
    this._styleClass = styleClass;
  }
  
  public String getStyleClass()
  {
    if (_styleClass != null) return _styleClass;
    ValueBinding vb = getValueBinding("styleClass");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }
  
  
  public void decode(FacesContext context)
  {
    try
    {
      if (!isRendered()) return;
      String clientId = getClientId(context);
      Map parameterMap = context.getExternalContext().getRequestParameterMap();
      String value = (String)parameterMap.get(clientId + ":obj");
      if(value != null)
      {
        Object itemValue = getItemValue();
        if (itemValue != null && !String.valueOf(itemValue).equals(value) 
                                                          && isSubmitOnchange())
        { 
        // send an action event when the newValue and the itemValue are diferents.
        // One assumes that the itemValue is the first element of the list, and if it change
        // an action event will be send.
         
          ValueBinding vb = getValueBinding("store");
          if (vb == null) return;
          try
          {
            vb.setValue(context, value);
          }
          catch (RuntimeException e)
          {
          }
          queueEvent(new ActionEvent(this));  

        }
        _localValue.put(clientId, value);
      }
      else
      {
        if(getItemValue() != null)
        {
          // no changed --> get old value;  
          _localValue.put(clientId, String.valueOf(getItemValue()));
        }            
      }
      
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    
  }
  
  public void encodeBegin(FacesContext context) throws IOException
  {
    try
    {
      if (!isRendered()) return;
      String clientId = getClientId(context);
      ResponseWriter writer = context.getResponseWriter();
      
      
      writer.startElement("select", this);
      writer.writeAttribute("name", clientId + ":obj", null);
      String formId = FacesUtils.getParentFormId(this, context);
      if(isSubmitOnchange())
      {
        writer.writeAttribute("onchange",
               "document.forms['" + formId + "'].submit(); return false;" , null);  
      }
      
      String style = getStyle();
      if (style != null)
      {
        writer.writeAttribute("style", style, null);
      }
      String styleClass = getStyleClass();
      if (styleClass != null)
      {
        writer.writeAttribute("class", styleClass, null);
      }
      String size = getSize();
      if(size != null)
      {
        writer.writeAttribute("size", size, null);
      }
      
      //_print mainValue:
      boolean exist = false;
      Object mv = getItemValue();

      //list of values;
      Vector values = getListValues();
      
      if(values != null)
      {
        for (int i = 0; i < values.size(); i++)
        {
          SelectItem si = (SelectItem)values.get(i);
          String v = String.valueOf(si.getValue());
          String label = si.getLabel();
          writer.startElement("option", this);
          writer.writeAttribute("value", v, null);
          if (mv == null && i == 0)
          {
            writer.writeAttribute("selected", "selected", null);  
          }
          if(mv != null && String.valueOf(mv).equals(v))
          {
            exist = true;
            writer.writeAttribute("selected", "selected", null);  
          }
          if(label != null)
          {
            v = label;
          }
          writer.writeText(v, null);
          writer.endElement("option");            
        }
      }
      
      if (mv != null && !exist)
      { 
        writer.startElement("option", this);
        writer.writeAttribute("value", String.valueOf(mv), null);
        writer.writeAttribute("selected", "selected", null);
        writer.writeText(mv, null);
        writer.endElement("option");
      }
      else if(mv == null)
      {
        writer.startElement("option", this);
        writer.writeAttribute("value", "", null);
        writer.writeAttribute("selected", "selected", null);
        writer.writeText("", null);
        writer.endElement("option");
      }
      
      writer.endElement("select");
    }
      
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public void encodeEnd(FacesContext context) throws IOException
  {    
  }

  public void processUpdates(FacesContext context)
  {
    if (context == null) throw new NullPointerException("context");
    if (!isRendered()) return;
    super.processUpdates(context);
    try
    {
      updateModel(context);
    }
    catch (RuntimeException e)
    {
      context.renderResponse();
      throw e;
    }
  }


  public void updateModel(FacesContext context)
  {
    String clientId = getClientId(context);
    ValueBinding vb = getValueBinding("store");
    if (vb == null) return;
    try
    {

      if(getLocalValue().get(clientId) != null)
        vb.setValue(context, getLocalValue().get(clientId));
    }
    catch (RuntimeException e)
    {
    }
  }
  
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[9];
    values[0] = super.saveState(context);
    values[1] = _size;
    values[2] = _store;
    values[3] = _itemValue;
    values[4] = _listValues;
    values[5] = _localValue; 
    values[6] = _style;
    values[7] = _styleClass;
    values[8] = _submitOnchange;
    return values;
  }

  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
     _size = (String)values[1];
    _store = (String) values[2];
    _itemValue = (String)values[3];
    _listValues = (Vector)values[4];
    _localValue = (Map)values[5];
    _style = (String)values[6];
    _styleClass = (String)values[7];
    _submitOnchange = Boolean.valueOf(String.valueOf(values[8]));

  }

  public void setSubmitOnchange(boolean submitOnchange)
  {
    this._submitOnchange = submitOnchange;
  }

  public boolean isSubmitOnchange()
  {
    return _submitOnchange;
  }

  public void setLocalValue(Map localValue)
  {
    this._localValue = localValue;
  }

  public Map getLocalValue()
  {
    return _localValue;
  }
}
