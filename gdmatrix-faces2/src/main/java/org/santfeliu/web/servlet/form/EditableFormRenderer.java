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
package org.santfeliu.web.servlet.form;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.santfeliu.form.Form;
import org.santfeliu.form.View;
import org.santfeliu.form.type.html.HtmlForm;
import org.santfeliu.util.HTMLCharTranslator;

/**
 *
 * @author realor
 */
public class EditableFormRenderer implements FormRenderer
{
  static final boolean removeGroupIds = true;
  static final HashSet<String> specialAttributes = new HashSet();
  static
  {
    //specialAttributes.add("format");
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
  }

  public void renderForm(Form form, Map data, Writer writer)
    throws Exception
  {
    if (form instanceof HtmlForm)
    {
      View view = form.getRootView();
      writeFormView(view, (HtmlForm)form, data, writer);
    }
  }

  protected void writeFormView(View view, HtmlForm form,
    Map data, Writer writer) throws IOException
  {
    String tag = view.getNativeViewType();
    if (tag == null) tag = "span";
    if (tag.equals("body") || tag.equals("form"))
    {
      writeChildren(view, form, data, writer);
    }
    else if (View.LABEL.equals(view.getViewType()))
    {
      writeLabel(view, form, data, writer);
    }
    else if (tag.equals("input"))
    {
      writeInput(view, form, data, writer);
    }
    else if (tag.equals("textarea"))
    {
      writeTextarea(view, form, data, writer);
    }
    else if (tag.equals("select"))
    {
      writeSelect(view, form, data, writer);
    }
    else
    {
      writeGenericView(view, form, data, writer);
    }
  }

  protected void writeChildren(View view, HtmlForm form, Map data, Writer writer)
    throws IOException
  {
    List<View> children = view.getChildren();
    for (View child : children)
    {
      writeFormView(child, form, data, writer);
    }
  }

  protected void writeLabel(View view, HtmlForm form, Map data, Writer writer)
    throws IOException
  {
    String text = (String)view.getProperty("text");
    text = HTMLCharTranslator.toHTMLText(text);
    writer.write(text);
  }

  protected void writeInput(View view, HtmlForm form, Map data, Writer writer)
    throws IOException
  {
    String entity = (String)data.get("entity");
    writer.write("<" + view.getNativeViewType());
    writeAttributes(view, writer, "name", "value");
    String reference = view.getReference();

    String name = (entity == null) ? reference : entity + "." + reference;
    writer.write(" name=\"" + name + "\"");

    Object value = data.get(reference);
    if (value != null)
    {
      String svalue = value.toString();
      writer.write(" value=\"" + svalue + "\"");
    }
    writer.write(">");
  }

  protected void writeTextarea(View view, HtmlForm form, Map data, Writer writer)
    throws IOException
  {
    String entity = (String)data.get("entity");
    writer.write("<" + view.getNativeViewType());
    writeAttributes(view, writer, "name");
    String reference = view.getReference();

    String name = (entity == null) ? reference : entity + "." + reference;
    writer.write(" name=\"" + name + "\">");

    Object value = data.get(reference);
    if (value != null)
    {
      String svalue = value.toString();
      writer.write(svalue);
    }
    writer.write("</" + view.getNativeViewType() + ">");
  }

  protected void writeSelect(View view, HtmlForm form, Map data, Writer writer)
    throws IOException
  {
    String entity = (String)data.get("entity");
    writer.write("<" + view.getNativeViewType());
    writeAttributes(view, writer, "name");
    String reference = view.getReference();

    String name = (entity == null) ? reference : entity + "." + reference;
    writer.write(" name=\"" + name + "\">");

    writeSelectOptions(view, form, data, writer);
    writer.write("</" + view.getNativeViewType() + ">");
  }

  protected void writeSelectOptions(View view, Form form, Map data, Writer writer)
    throws IOException
  {
    String reference = view.getReference();
    String value = (String)data.get(reference);
    List<View> items = view.getChildren();
    for (View item : items)
    {
      if (item.getViewType().equals(View.ITEM))
      {
        writer.write("<option ");
        String itemValue = String.valueOf(item.getProperty("value"));
        writer.write("value=\"" + itemValue + "\" ");
        boolean selected = itemValue.equals(value);
        if (selected)
        {
          writer.write("selected=\"selected\"");
        }
        String itemText = null;
        if (!item.getChildren().isEmpty())
        {
          View itemLabel = item.getChildren().get(0);
          itemText = String.valueOf(itemLabel.getProperty("text"));
        }
        if (itemText == null) itemText = itemValue;
        writer.write(">");
        writer.write(HTMLCharTranslator.toHTMLText(itemText));
        writer.write("</option>");
      }
    }
  }

  protected void writeGenericView(View view, HtmlForm form, Map data,
    Writer writer) throws IOException
  {
    String tag = view.getNativeViewType();
    writer.write("<" + tag);
    if (removeGroupIds && "div".equalsIgnoreCase(tag))
    {
      //  avoid duplicate ids when merging forms
      writeAttributes(view, writer, "id");
    }
    else
    {
      writeAttributes(view, writer);
    }
    writer.write(">");
    writeChildren(view, form, data, writer);
    writer.write("</" + tag + ">");
  }

  protected void writeAttributes(View view, Writer writer,
    String ...excluded) throws IOException
  {
    Collection<String> propertyNames = view.getPropertyNames();
    for (String propertyName : propertyNames)
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
        String value = String.valueOf(view.getProperty(propertyName));
        writer.write(" " + propertyName + "=\"" + value + "\"");
      }
    }
  }
}
