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
package org.santfeliu.faces.dynamicform.render;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.faces.render.FacesRenderer;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.dynamicform.DynamicForm;
import org.santfeliu.form.Field;
import org.santfeliu.form.Form;
import org.santfeliu.form.type.html.HtmlView;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author realor
 */
@FacesRenderer(componentFamily="DynamicForm",
	rendererType="HtmlMultivaluedFormRenderer")
public class HtmlMultivaluedFormRenderer extends HtmlFormRenderer
{
  private char valueSeparator = '|';
  private static final String DEFAULT_NUMBER_FORMAT = "0.############";
  private static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";

  public char getValueSeparator()
  {
    return valueSeparator;
  }

  public void setValueSeparator(char valueSeparator)
  {
    this.valueSeparator = valueSeparator;
  }

  @Override
  public Object getConvertedValue(FacesContext context, UIComponent component,
    Object submittedValue) throws ConverterException
  {
    Map convertedData = new HashMap();
    DynamicForm dynamicForm = (DynamicForm)component;
    Map previousData = (Map)dynamicForm.getValue();

    // preserve previous data
    if (previousData != null)
      convertedData.putAll(previousData);

    Form form = dynamicForm.getForm();
    for (Field field : form.getFields())
    {
      String name = field.getReference();

      if (!field.isReadOnly())
      {
        // convert new fieldValue
        String fieldValue = (String)((Map)submittedValue).get(name);
        String[] values = null;

        if (field.getMaxOccurs() != 1) // multivalued property
        {
          if (fieldValue != null)
          {
            values = fieldValue.split("\\" + valueSeparator);
          }
        }
        else
          values = new String[]{fieldValue};

        List convertedFieldValues = new ArrayList();
        for (int i = 0; i < values.length; i++)
        {
          fieldValue = values[i];
          convertedFieldValues.add(
            convertValue(dynamicForm, field, form, name, fieldValue));
        }

        if (field.getMaxOccurs() != 1) // multivalued property
        {
          if (convertedFieldValues.isEmpty())
            convertedData.put(name, Collections.EMPTY_LIST);
          else
            convertedData.put(name, convertedFieldValues);
        }
        else
          convertedData.put(name, convertedFieldValues.get(0));
      }
    }
    System.out.println("ConvertedData: " + convertedData);
    return convertedData;
  }


  /***** private methods *****/

  @Override
  protected String getValueAsString(DynamicForm dynamicForm, HtmlView view)
  {
    String stringValue = null;

    String name = view.getReference();
    if (name == null) return "";

    // submitted fieldValue has preference
    Map data = (Map)dynamicForm.getSubmittedValue();
    if (data != null)
    {
      Object value = data.get(name);
      if (value instanceof List)
      {
        for (Object v : (List)value)
        {
          stringValue = (stringValue == null ?
            "" : stringValue + valueSeparator) + (String)v;
        }
      }
      else
        stringValue = (String)value;
    }

    // take model fieldValue if not submitted fieldValue found
    if (stringValue == null)
    {
      Map modelValues = (Map)dynamicForm.getValue();
      if (modelValues != null)
      {
        Object value = modelValues.get(name);
        if (value instanceof List)
        {
          List list = (List)value;
          for (Object v : list)
          {
            stringValue = (stringValue == null ?
              "" : stringValue + valueSeparator) + convertToString(view, v);
          }
        }
        else
          stringValue = convertToString(view, value);
      }
    }
    return stringValue;
  }

  private Object convertValue(DynamicForm dynamicForm, Field field, Form form,
    String name, String fieldValue)
  {
    String label = field.getLabel();
    if (label == null) label = name;

    Object convertedFieldValue = null;
    if (fieldValue == null || fieldValue.length() == 0)
    {
      convertedFieldValue = null;
    }
    else if (field.getType().equals(Field.NUMBER))
    {
      try
      {
        convertedFieldValue = new Double(fieldValue);
      }
      catch (NumberFormatException ex)
      {
        dynamicForm.setValid(false);
        FacesUtils.addMessage(DynamicForm.INVALID_VALUE,
          new Object[]{label}, FacesMessage.SEVERITY_ERROR);
      }
    }
    else if (field.getType().equals(Field.BOOLEAN))
    {
      convertedFieldValue = "true".equals(fieldValue);
    }
    else if (field.getType().equals(Field.TEXT))
    {
      convertedFieldValue = fieldValue;
    }
    else if (field.getType().equals(Field.DATE))
    {
      String datePattern = DEFAULT_DATE_FORMAT;
      HtmlView view = (HtmlView)form.getView(name);
      if (view != null)
      {
        String format = view.getProperty("format");
        if (format != null && format.startsWith("date:"))
        {
          datePattern = format.substring(5);
        }
      }
      Date date = TextUtils.parseUserDate(fieldValue, datePattern);
      convertedFieldValue = TextUtils.formatDate(date, "yyyyMMdd");
      if (convertedFieldValue == null)
      {
        dynamicForm.setValid(false);
        FacesUtils.addMessage(DynamicForm.INVALID_VALUE,
          new Object[]{label}, FacesMessage.SEVERITY_ERROR);
      }
    }
    return convertedFieldValue;
  }

  private String convertToString(HtmlView view, Object value)
  {
    String stringValue = null;

    if (value != null)
    {
      if (value instanceof Number)
      {
        double number = ((Number)value).doubleValue();
        DecimalFormat df = new DecimalFormat(DEFAULT_NUMBER_FORMAT,
          new DecimalFormatSymbols(Locale.ENGLISH));
        stringValue = df.format(number);
      }
      else
      {
        String format = view.getProperty("format");
        if (format != null)
        {
          if (format.startsWith("date"))
          {
            Date date = TextUtils.parseInternalDate(value.toString());
            String datePattern = (format.startsWith("date:")) ?
              format.substring(5) : DEFAULT_DATE_FORMAT;
            stringValue = TextUtils.formatDate(date, datePattern);
          }
          else stringValue = value.toString();
        }
        else
        {
          stringValue = value.toString();
        }
      }
    }
    return stringValue;
  }
}
