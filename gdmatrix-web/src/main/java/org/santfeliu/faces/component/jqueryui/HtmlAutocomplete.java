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
package org.santfeliu.faces.component.jqueryui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectItem;
import javax.faces.component.UISelectItems;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.model.SelectItem;

/**
 * @deprecated Use Primefaces autocomplete component instead.
 * @author blanquepa
 */
@FacesComponent(value = "HtmlAutocomplete")
public class HtmlAutocomplete extends UIInput
{
  
  private Object _submittedValue;
  private String _value;
  private String _style;
  private String _styleClass;
  private String _inputStyle;
  private String _inputStyleClass;
  private String _buttonStyle;
  private String _buttonStyleClass;
  private Boolean _disabled;  
  
  private JQueryUIRenderUtils jQueryUIEncoder = new JQueryUIRenderUtils(this); 
  
  @Override
  public String getFamily()
  {
    return "Autocomplete";
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
  
  public void setButtonStyle(String style)
  {
    this._buttonStyle = style;
  }

  public String getButtonStyle()
  {
    if (_buttonStyle != null) return _buttonStyle;
    ValueExpression ve = getValueExpression("buttonStyle");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setButtonStyleClass(String styleClass)
  {
    this._buttonStyleClass = styleClass;
  }

  public String getButtonStyleClass()
  {
    if (_buttonStyleClass != null) return _buttonStyleClass;
    ValueExpression ve = getValueExpression("buttonStyleClass");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }  
  
  public void setInputStyle(String style)
  {
    this._inputStyle = style;
  }

  public String getInputStyle()
  {
    if (_inputStyle != null) return _inputStyle;
    ValueExpression ve = getValueExpression("inputStyle");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setInputStyleClass(String styleClass)
  {
    this._inputStyleClass = styleClass;
  }

  public String getInputStyleClass()
  {
    if (_inputStyleClass != null) return _inputStyleClass;
    ValueExpression ve = getValueExpression("inputStyleClass");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }    
  
  @Override
  public Object getSubmittedValue()
  {
    return _submittedValue;
  }

  @Override
  public void setSubmittedValue(Object value)
  {
    this._submittedValue = value;
  }  
  
 @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    String clientId = getClientId(context);
    
    jQueryUIEncoder.encodeLibraries(context, writer);
    
    if (!isDisabled())
      encodeJavascript(context, writer);
    
    writer.startElement("select", this);
    writer.writeAttribute("name", clientId + ":value", null);
    writer.writeAttribute("id", clientId + ":combobox", null);
    if (isDisabled())
      writer.writeAttribute("disabled", "true", null);
  }
  
  private void encodeJavascript(FacesContext context, ResponseWriter writer)
    throws IOException
  {
    String clientId = getClientId(context);
    encodeCustomStlyes(writer);
    writer.startElement("script", this);
    encodeJQuery(writer, clientId);
    writer.endElement("script");     
  }
  
  private void encodeCustomStlyes(ResponseWriter writer) throws IOException
  {
    writer.startElement("style", this);
    writer.writeText(".custom-combobox {" +
      "    position: relative;" +
      "    display: inline-block;" +
      "  }" +
      "  .custom-combobox-toggle {" +
      "    position: absolute;" +
      "    top: 0;" +
      "    bottom: 0;" +
      "    margin-left: -1px;" +
      "    padding: 0;" +
      "  }" +
      "  .custom-combobox-input {" +
      "    margin: 0;" +
      "    padding: 5px 10px;" +
      "  }", null);
    writer.endElement("style");    
  }
  
  private void encodeJQuery(ResponseWriter writer, String clientId) throws IOException
  {
    writer.writeText("(function( $ ) {", null);
    encodeWidgetFunction(writer, clientId);
    writer.writeText("})( jQuery );", null);
    writer.writeText("  $(function() {" +
      "    $(escapeClientId('" + clientId + ":combobox')).combobox();" +
      "  });", null);    
  }
  
  private void encodeWidgetFunction(ResponseWriter writer, String clientId) throws IOException
  {
    writer.writeText("$.widget( 'custom.combobox', {", null);
    encodeWidgetFunctions(writer, clientId);
    writer.writeText("});", null);
  }
  
  private void encodeWidgetFunctions(ResponseWriter writer, String clientId) throws IOException
  {
    encodeCreateFunction(writer);
    writer.writeText(",", null);
    encodeCreateAutoCompleteFunction(writer, clientId);
    writer.writeText(",", null);
    encodeCreateShowAllButtonFunction(writer);
    writer.writeText(",", null);
    encodeSourceFunction(writer);
    writer.writeText(",", null);
    encodeDestroyFunction(writer);
  }  
  
  private void encodeCreateFunction(ResponseWriter writer) throws IOException
  {
    String style = getStyle();
    String styleClass = getStyleClass();
    
    StringBuilder sb = new StringBuilder();
    
    sb.append("this.wrapper = $( '<span>' )");
    if (style != null)
      sb.append(".attr( 'style', '").append(style).append("' )");
    sb.append(".addClass( 'custom-combobox ").append(styleClass != null ? styleClass : "").append("' )");
    sb.append(".insertAfter( this.element );");
    sb.append("this.element.hide();");
    sb.append("this._createAutocomplete();");
    sb.append("this._createShowAllButton();");
    
    encodeJSFunction(writer, "_create", "", sb.toString());
  }
  
  private void encodeCreateAutoCompleteFunction(ResponseWriter writer, String clientId) throws IOException
  {
    String inputStyle = getInputStyle();
    String inputStyleClass = getInputStyleClass();   
    
    StringBuilder sb = new StringBuilder();
    
    sb.append("var selected = this.element.children( ':selected' ),");
    sb.append("value = selected.val() ? selected.text() : '';");
    sb.append("this.input = $( '<input>' )");
    sb.append(" .appendTo( this.wrapper )");
    sb.append(" .val( value )");
    sb.append(" .attr( 'title', '' )");
    sb.append(" .attr( 'name', '").append(clientId).append(":autocomplete' )");        
    if (inputStyle != null)
      sb.append(".attr( 'style', '").append(inputStyle).append("' )");
    sb.append(" .addClass( 'custom-combobox-input ").append(inputStyleClass != null ? inputStyleClass : "").append("' )");
    sb.append(" .autocomplete({" );
    sb.append("   delay: 0,");
    sb.append("   minLength: 0,");
    sb.append("   source: $.proxy( this, '_source' )");
    sb.append(" })");
    sb.append(" .tooltip({");
    sb.append("   tooltipClass: 'ui-state-highlight'");
    sb.append(" });");
    sb.append("this._on( this.input, {");
    sb.append("autocompleteselect: function( event, ui ) {");
    sb.append("ui.item.option.selected = true;");
    sb.append("this._trigger( 'select', event, {");
    sb.append("item: ui.item.option");
    sb.append("});");
    sb.append("}");
    sb.append("});");
    
    encodeJSFunction(writer, "_createAutocomplete", "", sb.toString());
  }
  
  private void encodeCreateShowAllButtonFunction(ResponseWriter writer) throws IOException
  {
    String buttonStyle = getButtonStyle();
    String buttonStyleClass = getButtonStyleClass();

    StringBuilder sb = new StringBuilder();    
    sb.append("var input = this.input,");
    sb.append("wasOpen = false;");
    sb.append("$( '<a>' )");
    sb.append(".attr( 'tabIndex', -1 )");
    sb.append(".attr( 'title', 'Show All Items' )");
    sb.append(".appendTo( this.wrapper )");
    sb.append(".button({");
    sb.append("icons: {");
    sb.append("primary: 'ui-icon-triangle-1-s'");
    sb.append("},");
    sb.append("text: false");
    sb.append("})");
    sb.append(".removeClass( 'ui-corner-all' )");
    if (buttonStyle != null)
      sb.append(".attr( 'style', '").append(buttonStyle).append("' )");
    sb.append(".addClass( 'custom-combobox-toggle ").append(buttonStyleClass != null ? buttonStyleClass : "").append("' )");
    sb.append(".mousedown(function() {");
    sb.append("wasOpen = input.autocomplete( 'widget' ).is( ':visible' );");
    sb.append("})");
    sb.append(".click(function() {");
    sb.append("input.focus();");
    sb.append("if ( wasOpen ) {");
    sb.append("return;");
    sb.append("}");
    sb.append("input.autocomplete( 'search', '' );");
    sb.append("});");
    
    encodeJSFunction(writer, "_createShowAllButton", "", sb.toString());    
  }  
  
  private void encodeSourceFunction(ResponseWriter writer) throws IOException
  {
    StringBuilder sb = new StringBuilder();  
    sb.append("var matcher = new RegExp( $.ui.autocomplete.escapeRegex(request.term), 'i' );");
    sb.append("response( this.element.children( 'option' ).map(function() {");
    sb.append("var text = $( this ).text();");
    sb.append("if ( this.value && ( !request.term || matcher.test(text) ) )");
    sb.append("return {");
    sb.append("label: text,");
    sb.append("value: text,");
    sb.append("option: this");
    sb.append("};");
    sb.append("}));");
            
    encodeJSFunction(writer, "_source", "request, response", sb.toString());                
  }

  private void encodeDestroyFunction(ResponseWriter writer) throws IOException
  {
    StringBuilder sb = new StringBuilder();    
    sb.append("this.wrapper.remove();");
    sb.append("this.element.show();");
          
    encodeJSFunction(writer, "_destroy", "", sb.toString());                 
  }  
  
  private void encodeJSFunction(ResponseWriter writer, String fName, String fParams, String fBody) throws IOException
  {
    writer.writeText(fName + ": function( " + fParams + ") {", null);
    writer.writeText(fBody, null);
    writer.writeText("}", null);
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
  }  

  @Override
  public void decode(FacesContext context)
  {
    if (!isRendered()) return;
    if (isDisabled()) return;
    
    Map paramsMap = context.getExternalContext().getRequestParameterMap();
    String clientId = getClientId(context);

    String fvalue = (String)paramsMap.get(jQueryUIEncoder.getFieldId(clientId, "autocomplete"));
    if (fvalue == null) fvalue = "";
    String value = fvalue;
    setSubmittedValue(value);    
  }


  
  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[9];
    values[0] = super.saveState(context);
    values[1] = _value;
    values[2] = _style;
    values[3] = _styleClass;
    values[4] = _disabled;
    values[5] = _buttonStyle;
    values[6] = _buttonStyleClass;
    values[7] = _inputStyle;
    values[8] = _inputStyleClass;
    
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
    _buttonStyle = (String)values[5];
    _buttonStyleClass = (String)values[6];
    _inputStyle = (String)values[7];
    _inputStyleClass = (String)values[8];
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
