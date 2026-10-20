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

package org.santfeliu.webapp.data; 

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import org.santfeliu.form.Field;
import org.santfeliu.form.Form;
import org.santfeliu.form.View;
import org.santfeliu.form.type.html.HtmlForm;
import org.santfeliu.form.type.html.HtmlView;

public class FormRenderer
{
  protected static final String DEFAULT_NUMBER_FORMAT = "0.############";
  protected static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";
  protected static final String DEFAULT_DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
  protected static final Set<String> specialAttributes = new HashSet<>();

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

  protected Form form;
  protected Map<String, Object> dataModel;

  public FormRenderer(Form form, Map<String, Object> dataModel)
  {
    this.form = form;
    this.dataModel = dataModel;
  }

  public String encode()
  {
    if (form == null)
    {
      return "<p>No input form</p>";
    }

    StringBuilder html = new StringBuilder();

    if (form instanceof HtmlForm)
    {
      HtmlForm htmlForm = (HtmlForm) form;
      HtmlView rootView = (HtmlView) form.getRootView();
      if (rootView != null)
      {
        encodeHtmlView(rootView, htmlForm, html);
      }
    }
    else
    {
      html.append("<p>Form not supported yet</p>");
    }

    return html.toString();
  }

  protected void encodeHtmlView(HtmlView view, HtmlForm htmlForm, StringBuilder html)
  {
    if (View.COMMENT.equals(view.getViewType())) return;

    String tag = view.getNativeViewType();
    if (tag == null) tag = "span";

    if (tag.equals("body") || tag.equals("form"))
    {
      encodeChildren(view, htmlForm, html);
    }
    else if (View.TEXT.equals(view.getViewType()))
    {
      encodeText(view, html);
    }
    else if (tag.equals("input"))
    {
      encodeInput(view, htmlForm, html);
    }
    else if (tag.equals("textarea"))
    {
      encodeTextarea(view, htmlForm, html);
    }
    else if (tag.equals("select"))
    {
      encodeSelect(view, htmlForm, html);
    }
    else if (tag.equals("script"))
    {
      encodeScript(view, html);
    }
    else
    {
      encodeGenericView(view, htmlForm, html);
    }
  }

  protected void encodeChildren(HtmlView view, HtmlForm htmlForm, StringBuilder html)
  {
    List<View> childViews = view.getChildren();
    for (View childView : childViews)
    {
      encodeHtmlView((HtmlView) childView, htmlForm, html);
    }
  }

  protected void encodeGenericView(HtmlView view, HtmlForm htmlForm, StringBuilder html)
  {
    String tag = view.getNativeViewType();
    html.append("<").append(tag);
    renderViewAttributes(view, html);
    html.append(">");
    
    encodeChildren(view, htmlForm, html);
    
    html.append("</").append(tag).append(">");
  }

  protected void encodeScript(HtmlView view, StringBuilder html)
  {
    String tag = view.getNativeViewType();
    html.append("<").append(tag);
    renderViewAttributes(view, html);
    html.append(">");

    List<View> childViews = view.getChildren();
    for (View childView : childViews)
    {
      String text = ((HtmlView) childView).getProperty("text");
      if (text != null) html.append(text);
    }

    html.append("</").append(tag).append(">");
  }

  protected void encodeText(HtmlView view, StringBuilder html)
  {
    String text = view.getProperty("text");
    if (text != null)
    {
      // Aquí se podría aplicar traducción si fuera necesario
      html.append(text);
    }
  }

  protected void encodeInput(HtmlView view, HtmlForm htmlForm, StringBuilder html)
  {
    String tag = view.getNativeViewType();
    String type = view.getProperty("type");

    if ("radio".equals(type))
    {
      html.append("<").append(tag);
      String name = view.getProperty("name");
      String formValue = view.getProperty("value");
      html.append(" name=\"").append(name).append("\"");
      
      String value = getValueAsString(view);
      if (value == null)
      {
        String checked = view.getProperty("checked");
        if (checked != null && !checked.equalsIgnoreCase("false"))
        {
          html.append(" checked=\"checked\"");
        }
      }
      else if (formValue != null && formValue.equals(value))
      {
        html.append(" checked=\"checked\"");
      }
      renderViewAttributes(view, html, "name", "checked");
      html.append(" />");
    }
    else if ("checkbox".equals(type))
    {
      html.append("<").append(tag);
      String name = view.getProperty("name");
      html.append(" name=\"").append(name).append("\"");
      
      String value = getValueAsString(view);
      if (value == null)
      {
        String checked = view.getProperty("checked");
        if (checked != null && !checked.equalsIgnoreCase("false"))
        {
          html.append(" checked=\"checked\"");
        }
      }
      else if ("true".equals(value))
      {
        html.append(" checked=\"checked\"");
      }
      html.append(" value=\"true\"");
      renderViewAttributes(view, html, "name", "checked", "value");
      html.append(" />");
    }
    else if ("text".equals(type) || "password".equals(type))
    {
      String multiple = view.getProperty("multiple");
      String[] stringValues;
      boolean isMultiple = "true".equalsIgnoreCase(multiple);
      if (isMultiple)
        stringValues = getValueAsStringArray(view);
      else
        stringValues = new String[]{getValueAsString(view)};

      for (String value : stringValues)
      {
        html.append("<").append(tag);
        String name = view.getProperty("name");
        String formValue = view.getProperty("value");
        html.append(" name=\"").append(name).append("\"");

        if (value == null && formValue != null)
          html.append(" value=\"").append(formValue).append("\"");
        else if (value != null)
          html.append(" value=\"").append(value).append("\"");

        renderViewAttributes(view, html, "name", "value", "maxlength");
        String maxLength = view.getProperty("maxlength");
        if (maxLength != null && !"0".equals(maxLength))
        {
          html.append(" maxlength=\"").append(maxLength).append("\"");
        }
        html.append(" />");
      }
    }
    else
    {
      html.append("<").append(tag);
      renderViewAttributes(view, html, "name");
      html.append(" />");
    }
  }

  protected void encodeTextarea(HtmlView view, HtmlForm htmlForm, StringBuilder html)
  {
    String tag = view.getNativeViewType();
    html.append("<").append(tag);

    String name = view.getProperty("name");
    html.append(" name=\"").append(name).append("\"");
    renderViewAttributes(view, html, "name", "maxlength");
    
    String maxLength = view.getProperty("maxlength");
    if (maxLength != null && !"0".equals(maxLength))
    {
      html.append(" onkeypress=\"checkMaxLength(this, ").append(maxLength).append(");\"");
    }
    html.append(">");

    String value = getValueAsString(view);
    if (value != null) html.append(value);

    html.append("</").append(tag).append(">");
  }

  protected void encodeSelect(HtmlView view, HtmlForm htmlForm, StringBuilder html)
  {
    String reference = view.getReference();
    Field field = form.getField(reference);
    if (field == null) return;
    String[] stringValues = getValueAsStringArray(view);

    String tag = view.getNativeViewType();
    html.append("<").append(tag);
    String name = view.getProperty("name");
    html.append(" name=\"").append(name).append("\"");
    renderViewAttributes(view, html, "name");
    
    html.append(">");
    encodeOptions(view, stringValues, html);
    html.append("</").append(tag).append(">");
  }

  protected void encodeOptions(HtmlView view, String[] stringValues, StringBuilder html)
  {
    List<View> items = view.getChildren();
    for (View item : items)
    {
      if (item.getViewType().equals(View.ITEM))
      {
        html.append("<option");
        String itemValue = String.valueOf(item.getProperty("value"));
        html.append(" value=\"").append(itemValue).append("\"");
        
        boolean selected = false;
        int i = 0;
        while (!selected && i < stringValues.length)
        {
          selected = itemValue.equals(stringValues[i++]);
        }
        if (selected)
        {
          html.append(" selected=\"selected\"");
        }
        html.append(">");

        String itemText = null;
        if (!item.getChildren().isEmpty())
        {
          View itemLabel = item.getChildren().get(0);
          itemText = String.valueOf(itemLabel.getProperty("text"));
        }
        if (itemText == null) itemText = itemValue;
        html.append(itemText);
        html.append("</option>");
      }
    }
  }

  protected String getValueAsString(HtmlView view)
  {
    String[] arr = getValueAsStringArray(view);
    return (arr != null && arr.length > 0) ? arr[0] : "";
  }

  protected String[] getValueAsStringArray(HtmlView view)
  {
    String[] stringValues = null;
    String name = view.getReference();
    if (name != null && dataModel != null)
    {
      Object values = dataModel.get(name);
      if (values != null)
      {
        List valueList = values instanceof List ? (List) values : Collections.singletonList(values);
        List<String> stringList = new ArrayList<>();

        for (Object value : valueList)
        {
          if (value != null)
          {
            if (value instanceof Number)
            {
              double number = ((Number) value).doubleValue();
              DecimalFormat df = new DecimalFormat(DEFAULT_NUMBER_FORMAT, new DecimalFormatSymbols(Locale.ENGLISH));
              stringList.add(df.format(number));
            }
            else
            {
              stringList.add(value.toString());
            }
          }
        }
        if (!stringList.isEmpty())
        {
          stringValues = stringList.toArray(new String[0]);
        }
      }
    }
    return stringValues == null ? new String[]{""} : stringValues;
  }

  protected void renderViewAttributes(View view, StringBuilder html, String... excluded)
  {
    for (String propertyName : view.getPropertyNames())
    {
      boolean isExcluded = specialAttributes.contains(propertyName.toLowerCase());
      int i = 0;
      while (!isExcluded && i < excluded.length)
      {
        isExcluded = propertyName.equalsIgnoreCase(excluded[i]);
        i++;
      }
      if (!isExcluded)
      {
        Object propertyValue = view.getProperty(propertyName);
        html.append(" ").append(propertyName).append("=\"").append(propertyValue).append("\"");
      }
    }
    String disabled = (String) view.getProperty("disabled");
    if (disabled != null && !"false".equals(disabled))
    {
      html.append(" disabled=\"true\"");
    }
  }
}
