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
package org.santfeliu.faces.component;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItem;
import javax.faces.component.UISelectItems;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.faces.FacesUtils;

/**
 *
 * @author realor
 */
@FacesComponent(value = "HtmlCommandMenu")
public class HtmlCommandMenu extends UICommand
{
  public static final String COMMAND_MENU_RENDERED = "COMMAND_MENU_SCRIPT";

  private Object _submittedValue;
  private String _value;
  private String _style;
  private String _styleClass;
  private String _title;
  private Boolean _disabled;

  public HtmlCommandMenu()
  {
  }

  @Override
  public String getFamily()
  {
    return "CommandMenu";
  }

  @Override
  public boolean getRendersChildren()
  {
    return true;
  }

  public void setValue(String value)
  {
    this._value = value;
  }

  @Override
  public String getValue()
  {
    if (_value != null) return _value;
    ValueExpression ve = getValueExpression("value");
    return ve != null ? String.valueOf(ve.getValue(getFacesContext().getELContext())) : null;
  }

  public void setDisabled(boolean disabled)
  {
    this._disabled = Boolean.valueOf(disabled);
  }

  public boolean isDisabled()
  {
    if (_disabled != null) return _disabled.booleanValue();
    ValueExpression ve = getValueExpression("disabled");
    Boolean v = ve != null ? (Boolean)ve.getValue(getFacesContext().getELContext()) : null;
    return v != null ? v.booleanValue() : false;
  }

  public void setStyle(String style)
  {
    this._style = style;
  }

  public String getStyle()
  {
    if (_style != null) return _style;
    ValueExpression ve = getValueExpression("style");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
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

  public void setTitle(String title)
  {
    this._title = title;
  }

  public String getTitle()
  {
    if (_title != null) return _title;
    ValueExpression ve = getValueExpression("title");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public Object getSubmittedValue()
  {
    return _submittedValue;
  }

  public void setSubmittedValue(Object value)
  {
    this._submittedValue = value;
  }

  @Override
  public void decode(FacesContext context)
  {
    if (!isRendered()) return;
    if (isDisabled()) return;

    boolean activated = false;
    String clientId = getClientId(context);
    Map parameterMap = context.getExternalContext().getRequestParameterMap();
    String actb = (String)parameterMap.get(clientId + ":actb");
    if (actb != null) // activated by button
    {
      activated = true;
    }
    else // activated by value changed
    {
      String act = (String)parameterMap.get(clientId + ":act");
      activated = "y".equals(act);
    }
    _submittedValue = parameterMap.get(clientId + ":value");
    if (activated)
    {
      queueEvent(new ActionEvent(this));
    }
  }

  @Override
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
    if (_submittedValue != null)
    {
      ValueExpression ve = getValueExpression("value");
      if (ve != null)
      {
        ve.setValue(context.getELContext(), _submittedValue);
        _submittedValue = null;
      }
    }
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    if (!isRendered()) return;

    String clientId = getClientId(context);
    ResponseWriter writer = context.getResponseWriter();
    Map requestMap = context.getExternalContext().getRequestMap();
    if (!requestMap.containsKey(COMMAND_MENU_RENDERED))
    {
      writer.startElement("script", this);
      writer.writeAttribute("type", "text/javascript", null);
      writer.writeText("function fireCommandMenu(formId,clientId){" +
        "showOverlay(); document.forms[formId][clientId + ':act'].value='y';" +
        "document.forms[formId].submit(); return false;}" , null);
      writer.endElement("script");
      requestMap.put(COMMAND_MENU_RENDERED, Boolean.TRUE);
    }

    writer.startElement("input", this);
    writer.writeAttribute("type", "hidden", null);
    writer.writeAttribute("name", clientId + ":act", null);
    writer.writeAttribute("value", "n", null);
    writer.endElement("input");

    writer.startElement("select", this);
    writer.writeAttribute("id", clientId, null);
    writer.writeAttribute("name", clientId + ":value", null);
    String formId = FacesUtils.getParentFormId(this, context);
    if (isDisabled())
    {
      writer.writeAttribute("disabled", "true", null);
    }
    writer.writeAttribute("onchange",
     "return fireCommandMenu('" + formId + "','" + clientId + "');", null);

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
    String title = getTitle();
    if (title != null)
    {
      writer.writeAttribute("title", title, null);
    }
  }

  @Override
  public void encodeChildren(FacesContext context)
    throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();

    List<SelectItem> selectItems = getSelectItems();
    for (SelectItem selectItem : selectItems)
    {
      encodeSelectItem(selectItem, writer);
    }
  }

  @Override
  public void encodeEnd(FacesContext context)
    throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    writer.endElement("select");

    String clientId = getClientId(context);
    writer.startElement("noscript", this);
    writer.startElement("input", this);
    writer.writeAttribute("type", "submit", null);
    writer.writeAttribute("name", clientId + ":actb", null);
    writer.writeAttribute("value", ">", null);
    writer.endElement("input");
    writer.endElement("noscript");
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[6];
    values[0] = super.saveState(context);
    values[1] = _value;
    values[2] = _style;
    values[3] = _styleClass;
    values[4] = _disabled;
    values[5] = _title;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _value = (String)values[1];
    _style = (String)values[2];
    _styleClass = (String)values[3];
    _disabled = (Boolean)values[4];
    _title = (String)values[5];
  }

  private void encodeSelectItem(SelectItem selectItem, ResponseWriter writer)
    throws IOException
  {
    String value = String.valueOf(selectItem.getValue());
    writer.startElement("option", this);
    writer.writeAttribute("value", value, null);
    if (value.equals(getValue()))
    {
      writer.writeAttribute("selected", "selected", null);
    }
    if (selectItem.isDisabled())
      writer.writeAttribute("disabled", "true", null);

    if (StringUtils.isBlank(selectItem.getLabel()))
      writer.writeAttribute("label", " ", null);
    writer.writeText(selectItem.getLabel(), null);
    writer.endElement("option");
  }

  private List<SelectItem> getSelectItems()
  {
    List<SelectItem> selectItems = new ArrayList();
    Iterator iter = getChildren().iterator();
    while (iter.hasNext())
    {
      UIComponent component = (UIComponent)iter.next();
      if (component instanceof UISelectItem)
      {
        UISelectItem uiSelectItem = (UISelectItem)component;
        SelectItem selectItem = (SelectItem)uiSelectItem.getValue();
        if (selectItem == null)
        {
          selectItem = new SelectItem(
            uiSelectItem.getItemValue(),
            uiSelectItem.getItemLabel());
        }
        selectItems.add(selectItem);
      }
      else if (component instanceof UISelectItems)
      {
        UISelectItems siComponent = (UISelectItems)component;
        Object items = siComponent.getValue();
        if (items instanceof SelectItem[])
        {
          for (SelectItem selectItem : (SelectItem[])items)
          {
            selectItems.add(selectItem);
          }
        }
        else if (items instanceof Collection)
        {
          Collection col = (Collection)items;
          for (Object item : col)
          {
            if (item instanceof SelectItem)
            {
              selectItems.add((SelectItem)item);
            }
          }
        }
      }
    }
    return selectItems;
  }
}
