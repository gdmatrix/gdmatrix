/*
 * GDMatrix
 *  
 * Copyright (C) 2020, Ajuntament de Sant Feliu de Llobregat
 *  
 * This program is licensed and may be used, modified and redistributed under 
 * the terms of the European Public License (EUPL), either version 1.1 or (at 
 * your option) any later version as soon as they are approved by the European 
 * Commission.
 *  
 * Alternatively, you may redistribute and/or modify this program under the 
 * terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation; either  version 3 of the License, or (at your option) 
 * any later version. 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *    
 * See the licenses for the specific language governing permissions, limitations 
 * and more details.
 *    
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along 
 * with this program; if not, you may find them at: 
 *    
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/ 
 * and 
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.santfeliu.faces.selectobject;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;

import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import org.santfeliu.faces.FacesUtils;

/**
 *
 * @author unknown
 */
@FacesComponent(value = "SelectObject")
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
    ValueExpression ve = getValueExpression("itemValue");
    return ve != null ? ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setStore(String value)
  {
    this._store = value;
  }

  public Object getStore()
  {
    if (_store != null) return _store;
    ValueExpression ve = getValueExpression("store");
    return ve != null ? ve.getValue(getFacesContext().getELContext()) : null;
  }
  
  public void setListValues(Vector listValues)
  {
    this._listValues = listValues;
  }

  public Vector getListValues()
  {
    if (_listValues != null) return _listValues;
    ValueExpression ve = getValueExpression("listValues");
    return ve != null ? (Vector)ve.getValue(getFacesContext().getELContext()) : null;
  }
  
  public String getStyle()
  {
    if (_style != null) return _style;
    ValueExpression ve = getValueExpression("style");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
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
    ValueExpression ve = getValueExpression("styleClass");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
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
         
          ValueExpression ve = getValueExpression("store");
          if (ve == null) return;
          try
          {
            ve.setValue(context.getELContext(), value);
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
    ValueExpression ve = getValueExpression("store");
    if (ve == null) return;
    try
    {

      if(getLocalValue().get(clientId) != null)
        ve.setValue(context.getELContext(), getLocalValue().get(clientId));
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
