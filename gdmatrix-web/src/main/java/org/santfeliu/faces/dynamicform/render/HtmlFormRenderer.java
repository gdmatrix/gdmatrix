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

import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.ConverterException;
import javax.faces.event.ActionEvent;
import javax.faces.event.PhaseId;
import javax.faces.render.FacesRenderer;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.Translator;
import org.santfeliu.faces.component.jquery.JQueryRenderUtils;
import org.santfeliu.faces.component.jqueryui.JQueryUIRenderUtils;
import org.santfeliu.faces.dynamicform.DynamicForm;
import org.santfeliu.form.Field;
import org.santfeliu.form.Form;
import org.santfeliu.form.View;
import org.santfeliu.form.type.html.HtmlForm;
import org.santfeliu.form.type.html.HtmlView;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author realor
 */
@FacesRenderer(componentFamily="DynamicForm",
	rendererType="HtmlFormRenderer")
public class HtmlFormRenderer extends FormRenderer
{
  protected static final String DEFAULT_NUMBER_FORMAT = "0.############";
  protected static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";
  protected static final String DEFAULT_DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
  protected static final Set specialAttributes = new HashSet();

  static
  {
    specialAttributes.add("format");
    specialAttributes.add("required");
    specialAttributes.add("disabled");
    specialAttributes.add("offset");
    specialAttributes.add("multivalued");
    specialAttributes.add("sql");
    specialAttributes.add("connection");
    specialAttributes.add("username");
    specialAttributes.add("password");
    specialAttributes.add("dataref");
    specialAttributes.add("renderer");
    specialAttributes.add("translate");
    specialAttributes.add("data-outputorder");
  }

  @Override
  public void decode(FacesContext context, UIComponent component)
  {
    DynamicForm dynamicForm = (DynamicForm)component;
    Form form = dynamicForm.getForm();
    if (form == null) return;

    Map<String, String[]> submittedData = new HashMap<>();
    dynamicForm.setSubmittedValue(submittedData);

    Map parameters = context.getExternalContext().getRequestParameterValuesMap();

    String clientId = component.getClientId(context);
    for (Field field : form.getFields())
    {
      String name = field.getReference();
      String[] stringValues =
        (String[])parameters.get(getFieldId(clientId, name));

      View view = form.getView(name);
      if (view != null && View.BUTTON.equals(view.getViewType()))
      {
        submittedData.put(name, stringValues);
        if (stringValues != null)
        {
          // button pressed
          ActionEvent event = new ActionEvent(component);
          event.setPhaseId(PhaseId.INVOKE_APPLICATION);
          component.queueEvent(event);
        }
      }
      else if (stringValues != null)
      {
        submittedData.put(name, stringValues);
      }
//      else if (Field.BOOLEAN.equals(field.getType()))
//      {
//        // special case: checkboxes don't send fieldValue when not checked.
//        // put 'false' fieldValue in this case
//        if (stringValues == null) submittedData.put(name, new String[]{"false"});
//      }
    }
    // All submitted values are String[], conversion takes place in next phase
    System.out.println("\nSUBMITTED DATA================================== ");
    printMap(submittedData);
  }

  @Override
  public Object getConvertedValue(FacesContext context, UIComponent component,
    Object submittedValue) throws ConverterException
  {
    Map convertedData = new HashMap();
    DynamicForm dynamicForm = (DynamicForm)component;
    Map previousData = (Map)dynamicForm.getValue();

    // preserve previous data
    if (previousData != null) convertedData.putAll(previousData);
    Map<String, String[]> submittedData = (Map<String, String[]>)submittedValue;

    Form form = dynamicForm.getForm();
    for (Field field : form.getFields())
    {
      String name = field.getReference();
      if (!field.isReadOnly())
      {
        String[] stringValues = (String[])submittedData.get(name);
        Object convertedFieldValue = convertFieldValue(dynamicForm, form,
          field, stringValues);
        convertedData.put(name, convertedFieldValue);
      }
    }
    System.out.println("\nCONVERTED DATA==================================");
    printMap(convertedData);
    return convertedData;
  }

  @Override
  public void encodeBegin(FacesContext context, UIComponent component)
    throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    DynamicForm dynamicForm = (DynamicForm)component;

    Form form = dynamicForm.getForm();
    if (form == null)
    {
      // encode nothing
      writer.startElement("p", component);
      writer.writeText("No input form", null);
      writer.endElement("p");
    }
    else if (form instanceof HtmlForm)
    {
      String clientId = component.getClientId(context);
      HtmlForm htmlForm = (HtmlForm)form;
      HtmlView rootView = (HtmlView)form.getRootView();
      encodeHtmlView(rootView, htmlForm, dynamicForm, clientId, writer);
      Map resquestMap = context.getExternalContext().getRequestMap();
      resquestMap.put("formEncoded", true);
    }
    else // other Form technology
    {
      writer.startElement("p", component);
      writer.writeText("Form not supported yet", null);
      writer.endElement("p");
    }
  }

  /***** non public methods *****/

  protected String getFieldId(String clientId, String name)
  {
    return clientId + ":" + name;
  }

  protected Object convertFieldValue(DynamicForm dynamicForm, Form form,
    Field field, String[] stringValues)
  {
    Object convertedFieldValue = null;
    String name = field.getReference();
    HtmlView view = (HtmlView)form.getView(name);
    String label = field.getLabel();
    if (label == null) label = name;

    if (stringValues == null) // special case checkboxes
    {
      if (view != null)
      {
        String type = view.getProperty("type");
        if ("checkbox".equals(type))
        {
          if (field.getMinOccurs() == 1) // value can not be null
          {
            if (field.getMaxOccurs() == 1) // single-valued field
            {
              convertedFieldValue = Boolean.FALSE;
            }
            else // multi-valued field
            {
              ArrayList valueList = new ArrayList();
              valueList.add(Boolean.FALSE);
              convertedFieldValue = valueList;
            }
          }
        }
      }
    }
    else // stringValues not null
    {
      List valueList = new ArrayList();
      for (String stringValue : stringValues)
      {
        if (field.getType().equals(Field.NUMBER))
        {
          if (!StringUtils.isBlank(stringValue))
          {
            try
            {
              valueList.add(Double.valueOf(stringValue));
            }
            catch (NumberFormatException ex)
            {
              dynamicForm.setValid(false);
              FacesUtils.addMessage(DynamicForm.INVALID_VALUE,
                new Object[]{label}, FacesMessage.SEVERITY_ERROR);
            }
          }
        }
        else if (field.getType().equals(Field.BOOLEAN))
        {
          if (!StringUtils.isBlank(stringValue))
            valueList.add("true".equals(stringValue));
        }
        else if (field.getType().equals(Field.TEXT))
        {
          if (!StringUtils.isBlank(stringValue)) valueList.add(stringValue);
        }
        else if (field.getType().equals(Field.DATE))
        {
          if (!StringUtils.isBlank(stringValue))
          {
            String datePattern = DEFAULT_DATE_FORMAT;
            if (view != null)
            {
              String format = view.getProperty("format");
              if (format != null &&
                format.startsWith(HtmlForm.DATE_FORMAT + ":"))
              {
                datePattern = format.substring(HtmlForm.DATE_FORMAT.length() + 1);
              }
            }
            Date date =
              TextUtils.parseUserDate(stringValue, datePattern, false);
            String dateString = TextUtils.formatDate(date, "yyyyMMdd");
            if (dateString == null)
            {
              dynamicForm.setValid(false);
              FacesUtils.addMessage(DynamicForm.INVALID_VALUE,
                new Object[]{label}, FacesMessage.SEVERITY_ERROR);
            }
            else valueList.add(dateString); // internal time repr: yyyyMMdd
          }
        }
        else if (field.getType().equals(Field.DATETIME))
        {
          if (!StringUtils.isBlank(stringValue))
          {
            String dateTimePattern = DEFAULT_DATETIME_FORMAT;
            if (view != null)
            {
              String format = view.getProperty("format");
              if (format != null &&
                format.startsWith(HtmlForm.DATETIME_FORMAT + ":"))
              {
                dateTimePattern =
                  format.substring(HtmlForm.DATETIME_FORMAT.length() + 1);
              }
            }
            Date date =
              TextUtils.parseUserDate(stringValue, dateTimePattern, false);
            String dateTimeString = TextUtils.formatDate(date, "yyyyMMddHHmmss");
            if (dateTimeString == null)
            {
              dynamicForm.setValid(false);
              FacesUtils.addMessage(DynamicForm.INVALID_VALUE,
                new Object[]{label}, FacesMessage.SEVERITY_ERROR);
            }
            else valueList.add(dateTimeString); // internal datetime repr: yyyyMMddHHmmss
          }
        }
        else if (field.getType().equals(Field.TIME))
        {
          if (!StringUtils.isBlank(stringValue))
          {
            String timeString = TextUtils.parseUserTime(stringValue);
            if (timeString == null)
            {
              dynamicForm.setValid(false);
              FacesUtils.addMessage(DynamicForm.INVALID_VALUE,
                new Object[]{label}, FacesMessage.SEVERITY_ERROR);
            }
            else valueList.add(timeString); // internal time repr: HHmmss
          }
        }
      }
      if (!valueList.isEmpty())
      {
        if (field.getMaxOccurs() == 1)
        {
          // single-valued field, return first item
          convertedFieldValue = valueList.get(0);
        }
        else
        {
          // multi-valued field, return all items
          convertedFieldValue = valueList;
        }
      } // else return null
    }
    return convertedFieldValue;
  }

  protected String getValueAsString(DynamicForm component, HtmlView view)
  {
    return getValueAsStringArray(component, view)[0];
  }

  protected String[] getValueAsStringArray(DynamicForm component, HtmlView view)
  {
    String[] stringValues = null;

    String name = view.getReference();
    if (name != null)
    {
      // submitted fieldValue has preference
      Map<String, String[]> submittedData =
        (Map<String, String[]>)component.getSubmittedValue();
      if (submittedData != null)
      {
        stringValues = submittedData.get(name); // valueArray.length > 0
      }
      else
      {
        // take fieldValue from model
        Map modelValues = (Map)component.getValue();
        if (modelValues != null)
        {
          Object values = modelValues.get(name);
          // convert to list
          List valueList = values instanceof List ?
            (List)values : Collections.singletonList(values);
          List<String> stringList = new ArrayList<String>();

          // convert value to String
          for (Object value : valueList)
          {
            if (value != null)
            {
              if (value instanceof Number)
              {
                double number = ((Number)value).doubleValue();
                DecimalFormat df = new DecimalFormat(DEFAULT_NUMBER_FORMAT,
                  new DecimalFormatSymbols(Locale.ENGLISH));
                stringList.add(df.format(number));
              }
              else
              {
                String format = view.getProperty("format");
                if (format != null)
                {
                  if (format.startsWith(HtmlForm.DATETIME_FORMAT))
                  {
                    Date date = TextUtils.parseInternalDate(value.toString());
                    String dateTimePattern =
                      (format.startsWith(HtmlForm.DATETIME_FORMAT + ":")) ?
                      format.substring(HtmlForm.DATETIME_FORMAT.length() + 1) :
                      DEFAULT_DATETIME_FORMAT;
                    stringList.add(TextUtils.formatDate(date, dateTimePattern));
                  }
                  else if (format.startsWith(HtmlForm.DATE_FORMAT))
                  {
                    Date date = TextUtils.parseInternalDate(value.toString());
                    String datePattern =
                      (format.startsWith(HtmlForm.DATE_FORMAT + ":")) ?
                      format.substring(HtmlForm.DATE_FORMAT.length() + 1) :
                      DEFAULT_DATE_FORMAT;
                    stringList.add(TextUtils.formatDate(date, datePattern));
                  }
                  else if (format.equals(HtmlForm.TIME_FORMAT))
                  {
                    stringList.add(
                      TextUtils.formatInternalTime(value.toString()));
                  }
                  else stringList.add(value.toString());
                }
                else
                {
                  stringList.add(value.toString());
                }
              }
            }
          }
          if (!stringList.isEmpty())
          {
            stringValues = stringList.toArray(new String[stringList.size()]);
          }
        }
      }
    }
    return stringValues == null ? new String[]{""} : stringValues;
  }

  protected void encodeHtmlView(HtmlView view, HtmlForm form,
    DynamicForm component, String clientId, ResponseWriter writer)
    throws IOException
  {
    String tag = view.getNativeViewType();
    if (tag == null) tag = "span";
    if (tag.equals("body") || tag.equals("form"))
    {
      encodeChildren(view, form, component, clientId, writer);
    }
    else if (View.TEXT.equals(view.getViewType()))
    {
      encodeText(view, form, component, clientId, writer);
    }
    else if (tag.equals("input"))
    {
      encodeInput(view, form, component, clientId, writer);
    }
    else if (tag.equals("textarea"))
    {
      encodeTextarea(view, form, component, clientId, writer);
    }
    else if (tag.equals("select"))
    {
      encodeSelect(view, form, component, clientId, writer);
    }
    else if (tag.equals("script"))
    {
      encodeScript(view, form, component, clientId, writer);
    }
    else
    {
      encodeGenericView(view, form, component, clientId, writer);
    }
  }

  protected void encodeChildren(HtmlView view, HtmlForm form,
    DynamicForm component, String clientId, ResponseWriter writer)
    throws IOException
  {
    List<View> childViews = view.getChildren();
    for (View childView : childViews)
    {
      encodeHtmlView((HtmlView)childView, form, component, clientId, writer);
    }
  }

  protected void encodeGenericView(HtmlView view, HtmlForm form,
    DynamicForm component, String clientId, ResponseWriter writer)
    throws IOException
  {
    String tag = view.getNativeViewType();
    writer.startElement(tag, component);

    renderViewAttributes(view, writer);
    encodeChildren(view, form, component, clientId, writer);

    writer.endElement(tag);
  }

  protected void encodeScript(HtmlView view, HtmlForm form,
    DynamicForm component, String clientId, ResponseWriter writer)
    throws IOException
  {
    String tag = view.getNativeViewType();
    writer.startElement(tag, component);

    renderViewAttributes(view, writer);
    List<View> childViews = view.getChildren();
    for (View childView : childViews)
    {
      String text = ((HtmlView)childView).getProperty("text");
      if (text != null) writer.writeText(text, null);
    }

    writer.endElement(tag);
  }

  protected void encodeText(HtmlView view, HtmlForm form,
    DynamicForm component, String clientId, ResponseWriter writer)
    throws IOException
  {
    String text = view.getProperty("text");
    Translator translator = component.getTranslator();
    if (text != null) renderHtmlText(text, writer, translator, component);
  }

  protected void encodeInput(HtmlView view, HtmlForm form,
    DynamicForm component, String clientId, ResponseWriter writer)
    throws IOException
  {
    String tag = view.getNativeViewType();
    String type = view.getProperty("type");

    if ("radio".equals(type))
    {
      writer.startElement(tag, component);
      String name = view.getProperty("name");
      String formValue = view.getProperty("value");
      writer.writeAttribute("name", getFieldId(clientId, name), null);
      String value = getValueAsString(component, view);
      if (value == null)
      {
        String checked = view.getProperty("checked");
        if (checked != null && !checked.equalsIgnoreCase("false"))
        {
          writer.writeAttribute("checked", "true", null);
        }
      }
      else if (formValue != null && formValue.equals(value))
        writer.writeAttribute("checked", "true", null);
      renderViewAttributes(view, writer, "name", "checked");
      writer.endElement(tag);
    }
    else if ("checkbox".equals(type))
    {
      writer.startElement(tag, component);
      String name = view.getProperty("name");
      writer.writeAttribute("name", getFieldId(clientId, name), null);
      String value = getValueAsString(component, view);
      if (value == null)
      {
        String checked = view.getProperty("checked");
        if (checked != null && !checked.equalsIgnoreCase("false"))
        {
          writer.writeAttribute("checked", "true", null);
        }
      }
      else if ("true".equals(value.toString()))
        writer.writeAttribute("checked", "true", null);
      writer.writeAttribute("value", "true", null);
      renderViewAttributes(view, writer, "name", "checked", "value");
      writer.endElement(tag);
    }
    else if ("text".equals(type) || "password".equals(type))
    {
      String multiple = view.getProperty("multiple");
      String[] stringValues = null;
      boolean isMultiple = "true".equalsIgnoreCase(multiple);
      if (isMultiple)
        stringValues = getValueAsStringArray(component, view);
      else
        stringValues = new String[]{getValueAsString(component, view)};

      for (int i = 0; i < stringValues.length; i++)
      {
        writer.startElement(tag, component);
        String name = view.getProperty("name");
          String formValue = view.getProperty("value");
          writer.writeAttribute("name", getFieldId(clientId, name), null);

          String value = stringValues[i];
          if (value == null && formValue != null)
            writer.writeAttribute("value", formValue, null);
          else if (value != null)
            writer.writeAttribute("value", value, null);
          renderViewAttributes(view, writer, "name", "value", "maxlength");
          String maxLength = view.getProperty("maxlength");
          if (maxLength != null && !"0".equals(maxLength))
          {
            writer.writeAttribute("maxlength", maxLength, null);
          }
          writer.endElement(tag);

        //encode datepicker component
        String renderer = view.getProperty("renderer");
        if (renderer == null || renderer.equals("datePicker"))
          encodeDatePicker(view, form, component, clientId, writer);
      }
    }
    else if ("submit".equals(type))
    {
      writer.startElement(tag, component);
      String name = view.getProperty("name");
      writer.writeAttribute("name", getFieldId(clientId, name), null);
      renderViewAttributes(view, writer, "name");
      writer.endElement(tag);
    }
    else
    {
      writer.startElement(tag, component);
      renderViewAttributes(view, writer, "name");
      writer.endElement(tag);
    }
  }

  protected void encodeTextarea(HtmlView view, HtmlForm form,
    DynamicForm component, String clientId, ResponseWriter writer)
    throws IOException
  {
    String renderer = view.getProperty("renderer");
    if (renderer != null && renderer.equalsIgnoreCase("htmlEditor"))
    {
      String style = view.getProperty("style");
      writer.startElement("div", component);
      writer.writeAttribute("style", style, null);
    }

    String tag = view.getNativeViewType();
    writer.startElement(tag, component);

    String name = view.getProperty("name");
    writer.writeAttribute("name", getFieldId(clientId, name), null);
    renderViewAttributes(view, writer, "name", "maxlength");
    String maxLength = view.getProperty("maxlength");
    if (maxLength != null && !"0".equals(maxLength))
    {
      writer.writeAttribute("onkeypress",
       "checkMaxLength(this, " + maxLength + ");", null);
    }
    String value = getValueAsString(component, view);
    if (value != null) writer.writeText(value, null);

    writer.endElement(tag);

    if (renderer != null && renderer.equalsIgnoreCase("htmlEditor"))
    {
      encodeHtmlEditor(view, component, clientId, writer);
      writer.endElement("div");
    }
  }

  protected void encodeSelect(HtmlView view, HtmlForm form,
    DynamicForm component, String clientId, ResponseWriter writer)
    throws IOException
  {
    String reference = view.getReference();
    Field field = form.getField(reference);
    if (field == null) return;
    String[] stringValues = getValueAsStringArray(component, view);
    String renderer = view.getProperty("renderer");
    if (field.getMaxOccurs() == 1 || !"checkBoxList".equals(renderer))
    {
      String tag = view.getNativeViewType();
      writer.startElement(tag, component);
      String name = view.getProperty("name");
      writer.writeAttribute("name", getFieldId(clientId, name), null);
      renderViewAttributes(view, writer, "name");
      encodeOptions(view, form, component, clientId, writer, stringValues);
      writer.endElement(tag);
    }
    else // checkBoxList renderer
    {
      writer.startElement("div", component);
      renderViewAttributes(view, writer, "name", "style", "size");
      int size;
      try
      {
        size = Integer.parseInt(view.getProperty("size"));
      }
      catch (NumberFormatException ex)
      {
        size = 5;
      }
      int height = size * 16;
      String style = view.getProperty("style");
      if (style == null) style = "overflow:auto;height:" + height + "px";
      else style += ";overflow:auto;height:" + height + "px";
      style += ";border-width:1px";
      writer.writeAttribute("style", style, null);
      encodeSelectItems(view, form, component, clientId, writer, stringValues);
      writer.endElement("div");
    }
  }

  protected void encodeOptions(HtmlView view, HtmlForm form,
    DynamicForm component, String clientId, ResponseWriter writer,
    String[] stringValues) throws IOException
  {
    Translator translator = component.getTranslator();
    String translate = view.getProperty("translate");
    boolean needsTranslation = Boolean.valueOf(translate);


    List<View> items = view.getChildren();
    for (View item : items)
    {
      if (item.getViewType().equals(View.ITEM))
      {
        writer.startElement("option", component);
        String itemValue = String.valueOf(item.getProperty("value"));
        writer.writeAttribute("value", itemValue, null);
        boolean selected = false;
        int i = 0;
        while (!selected && i < stringValues.length)
        {
          selected = itemValue.equals(stringValues[i++]);
        }
        if (selected)
        {
          writer.writeAttribute("selected", "selected", null);
        }
        String itemText = null;
        if (!item.getChildren().isEmpty())
        {
          View itemLabel = item.getChildren().get(0);
          itemText = String.valueOf(itemLabel.getProperty("text"));
        }
        if (itemText == null) itemText = itemValue;
        if (needsTranslation)
          renderHtmlText(itemText, writer, translator, component);
        else
          writer.writeText(itemText, null);
        writer.endElement("option");
      }
    }
  }

  protected void encodeSelectItems(HtmlView view, HtmlForm form,
    DynamicForm component, String clientId, ResponseWriter writer,
    String[] stringValues) throws IOException
  {
    String name = view.getProperty("name");
    String nameAttr = getFieldId(clientId, name);
    List<View> items = view.getChildren();
    int index = 0;
    for (View item : items)
    {
      if (item.getViewType().equals(View.ITEM))
      {
        writer.startElement("div", component);
        String itemTitle = (String)item.getProperty("title");
        if (itemTitle != null)
        {
          writer.writeAttribute("title", itemTitle, null);
        }
        String id = nameAttr + "_" + index;
        index++;
        writer.startElement("input", component);
        writer.writeAttribute("id", id, null);
        writer.writeAttribute("name", nameAttr, null);
        writer.writeAttribute("type", "checkbox", null);
        String itemValue = String.valueOf(item.getProperty("value"));
        writer.writeAttribute("value", itemValue, null);
        boolean selected = false;
        int i = 0;
        while (!selected && i < stringValues.length)
        {
          selected = itemValue.equals(stringValues[i++]);
        }
        if (selected)
        {
          writer.writeAttribute("checked", "true", null);
        }
        writer.endElement("input");

        String itemText = null;
        if (!item.getChildren().isEmpty())
        {
          View itemLabel = item.getChildren().get(0);
          itemText = String.valueOf(itemLabel.getProperty("text"));
        }
        if (itemText == null) itemText = itemValue;
        writer.startElement("label", component);
        writer.writeAttribute("for", id, null);
        writer.writeText(itemText, null);
        writer.endElement("label");
        writer.endElement("div");
      }
    }
  }

  protected void encodeDatePicker(HtmlView view, HtmlForm form,
    DynamicForm component, String clientId, ResponseWriter writer)
    throws IOException
  {
    String format = view.getProperty("format");
    if (format != null)
    {
      if (format.startsWith(HtmlForm.DATE_FORMAT + ":") ||
          format.startsWith(HtmlForm.DATETIME_FORMAT + ":") ||
          format.equals(HtmlForm.DATE_FORMAT) ||
          format.equals(HtmlForm.DATETIME_FORMAT))
      {
        JQueryUIRenderUtils jqUtils = new JQueryUIRenderUtils(component);
        FacesContext context = FacesContext.getCurrentInstance();
        jqUtils.encodeLibraries(context, writer);
        jqUtils.encodeDatePickerLibraries(context, writer);

        String name = view.getProperty("name");
        String fieldName = getFieldId(clientId, name);

        boolean isDateTimeFormat =
          format.startsWith(HtmlForm.DATETIME_FORMAT + ":")
          || format.equals(HtmlForm.DATETIME_FORMAT);

        String style = view.getProperty("style");

        if (isDateTimeFormat)
        {
          String dateTimePattern = DEFAULT_DATETIME_FORMAT;
          if (!format.equals(HtmlForm.DATETIME_FORMAT))
            dateTimePattern =
              format.substring(HtmlForm.DATETIME_FORMAT.length() + 1);
          String[] pattern = dateTimePattern.split(" ");
          String datePattern = pattern.length == 2 ? pattern[0] : "dd/mm/yy";
          String timePattern = pattern.length == 2 ? pattern[1] : "HH:mm";

          jqUtils.encodeDateTimePicker(writer, fieldName, datePattern,
            timePattern, null, style);
        }
        else
        {
          String datePattern = DEFAULT_DATE_FORMAT;
          if (!format.equals(HtmlForm.DATE_FORMAT))
            datePattern = format.substring(HtmlForm.DATE_FORMAT.length() + 1);

          jqUtils.encodeDatePicker(writer, fieldName, datePattern, null, style);
        }
      }
    }
  }

  protected boolean isJQueryLibraryRequired()
  {
    return true;
  }

  protected void encodeHtmlEditor(HtmlView view, DynamicForm component,
    String clientId, ResponseWriter writer) throws IOException
  {
    //Initial Configuration
    FacesContext context = FacesContext.getCurrentInstance();
    String contextPath = context.getExternalContext().getRequestContextPath();

    //Initial JS link
    writer.startElement("script", component);
    writer.writeAttribute("src",
                          contextPath + "/plugins/ckeditor/ckeditor.js",
                          null);
    writer.endElement("script");

    if (isJQueryLibraryRequired())
    {
      JQueryRenderUtils.encodeLibraries(context, writer, component);
    }

    writer.startElement("script", component);

    Map configMap = new HashMap();
    configMap.put("toolbarCanCollapse", "true");
    configMap.put("language",  FacesUtils.getViewLanguage());

    String style = view.getProperty("style");
    String height = null;
    Pattern pattern = Pattern.compile("height:(.*?)px");
    Matcher matcher = pattern.matcher(style);
    if (matcher.find())
    {
      try
      {
        height = String.valueOf(Integer.valueOf(matcher.group(1)) - 78); //78px is toolbar height
      }
      catch(Exception ex)
      {
        height = "100%";
      }
      if (height != null)
        configMap.put("height", height);

    }

    configMap.put("width", "100%");
    configMap.put("removeButtons", "Scayt,About,A11ychecker,Find,Replace,Anchor,Superscript,Subscript,Outdent,Indent,Blockquote,Styles,Format,SpecialChar,HorizontalRule,PasteFromWord");
    configMap.put("removePlugins", "scayt,elementspath,resize");
    configMap.put("toolbarGroups", "[" +
      "{ name: 'document', groups: [ 'mode', 'document', 'doctools' ] }," +
      "{ name: 'clipboard', groups: [ 'clipboard', 'undo' ] }," +
      "{ name: 'editing', groups: [ 'find', 'selection', 'editing' ] }," +
      "{ name: 'links', groups: [ 'links' ] }," +
      "{ name: 'insert', groups: [ 'insert' ] }," +
      "{ name: 'forms', groups: [ 'forms' ] }," +
      "{ name: 'tools', groups: [ 'tools' ] }," +
      "{ name: 'others', groups: [ 'others' ] }," +
      "{ name: 'basicstyles', groups: [ 'basicstyles', 'cleanup' ] }," +
      "{ name: 'paragraph', groups: [ 'list', 'indent', 'blocks', 'align', 'bidi', 'paragraph' ] }," +
      "{ name: 'styles', groups: [ 'styles' ] }," +
      "{ name: 'colors', groups: [ 'colors' ] }," +
      "{ name: 'about', groups: [ 'about' ] }" +
      "]");
    String config = toString(configMap);
    String name = view.getProperty("name");
    String js = "CKEDITOR.replace( '" + getFieldId(clientId, name) +
      "' , " + config + " );";

    writer.writeText(js, null);
    writer.endElement("script");
  }

  protected void renderViewAttributes(View view, ResponseWriter writer,
    String ... excluded) throws IOException
  {
    for (String propertyName : view.getPropertyNames())
    {
      boolean isExcluded =
        specialAttributes.contains(propertyName.toLowerCase());
      int i = 0;
      while (!isExcluded && i < excluded.length)
      {
        isExcluded = propertyName.equalsIgnoreCase(excluded[i]);
        i++;
      }
      if (!isExcluded)
      {
        Object propertyValue = view.getProperty(propertyName);
        writer.writeAttribute(propertyName, propertyValue, null);
      }
    }
    // special case: disabled attribute
    String disabled = (String)view.getProperty("disabled");
    if (disabled != null && !"false".equals(disabled))
    {
      writer.writeAttribute("disabled", "true", null);
    }
  }

  protected void printMap(Map convertedData)
  {
    for (Object key : convertedData.keySet())
    {
      Object value = convertedData.get(key);
      if (value instanceof String[])
      {
        StringBuilder builder = new StringBuilder();
        for (String s : (String[])value)
        {
          if (builder.length() > 0) builder.append(", ");
          builder.append(s);
        }
        value = "[" + builder.toString() + "]";
      }
      System.out.println(key + "=" + value);
    }
  }

  protected String toString(Map map)
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append("{");
    for (Object key : map.keySet())
    {
      buffer.append(key);
      buffer.append(": ");
      String value = (String)map.get(key);
      if (!value.startsWith("["))
        buffer.append("\"");
      buffer.append(value);
      if (!value.startsWith("["))
        buffer.append("\"");
      buffer.append(",");
    }
    buffer.deleteCharAt(buffer.length()-1);
    buffer.append(", on: { \"change\": (e) => { e.editor.element.$.textContent = e.editor.getData(); } }");
    buffer.append("}");
    return buffer.toString();
  }

  protected void renderHtmlText(String text,
    ResponseWriter writer, Translator translator, DynamicForm component)
    throws IOException
  {
    if (translator != null)
    {
      String userLanguage = FacesUtils.getViewLanguage();
      String translationGroup = component.getTranslationGroup();
      translator.translate(new StringReader(text),
        writer, "text/html", userLanguage, translationGroup);
    }
    else writer.write(text);
  }
}
