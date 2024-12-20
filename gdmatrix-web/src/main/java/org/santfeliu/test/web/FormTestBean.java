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
package org.santfeliu.test.web;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.faces.component.html.HtmlInputText;
import javax.faces.model.SelectItem;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.form.Form;
import org.santfeliu.form.FormDescriptor;
import org.santfeliu.form.FormDumper;
import org.santfeliu.form.FormFactory;
import org.santfeliu.util.enc.HtmlEncoder;

/**
 *
 * @author realor
 */
public class FormTestBean extends FacesBean implements Serializable
{
  private transient HtmlInputText selectorInputText = new HtmlInputText();
  private final List<SelectItem> formSelectItems = new ArrayList<>();
  private String selector;
  private Map data;
  private String fieldName;
  private String fieldValue;
  private String dumpFormSelector;
  private transient Map<String, Form> cachedForms;

  public FormTestBean()
  {
  }

  public HtmlInputText getSelectorInputText()
  {
    return selectorInputText;
  }

  public void setSelectorInputText(HtmlInputText selectorInputText)
  {
    this.selectorInputText = selectorInputText;
  }

  public List<SelectItem> getFormSelectItems()
  {
    return formSelectItems;
  }

  public int getFormCount()
  {
    return formSelectItems.size() - 1;
  }

  public String getSelector()
  {
    return selector;
  }

  public void setSelector(String selector)
  {
    this.selector = selector;
  }

  public void setData(Map data)
  {
    this.data = data;
  }

  public Map getData()
  {
    if (data == null)
    {
      data = new HashMap();
    }
    return data;
  }

  public String getFieldName()
  {
    return fieldName;
  }

  public void setFieldName(String fieldName)
  {
    this.fieldName = fieldName;
  }

  public String getFieldValue()
  {
    return fieldValue;
  }

  public void setFieldValue(String fieldValue)
  {
    this.fieldValue = fieldValue;
  }

  public List getFormFields()
  {
    Form form = getForm();
    if (form == null) return Collections.EMPTY_LIST;

    List list = new ArrayList();
    list.addAll(form.getFields());
    return list;
  }

  public List getDataEntries()
  {
    List dataEntries = new ArrayList();
    Map map = getData();
    if (map != null)
    {
      for (Object o : map.entrySet())
      {
        Map.Entry entry = (Map.Entry)o;
        String name = (String)entry.getKey();
        Object value = entry.getValue();
        String className = null;
        if (value == null) className = null;
        else if (value instanceof List)
        {
          className = "List";
          List valueList = (List)value;
          if (!valueList.isEmpty())
          {
            className += "<" + valueList.get(0).getClass().getName() + ">";
          }
        }
        else className = value.getClass().getName();
        dataEntries.add(new Object[]{name, value, className});
      }
      Collections.sort(dataEntries, (Object o1, Object o2) ->
      {
        Object[] a1 = (Object[])o1;
        Object[] a2 = (Object[])o2;
        String s1 = (String)a1[0];
        String s2 = (String)a2[0];
        return s1.compareTo(s2);
      });
    }
    return dataEntries;
  }

  public Map<String, Form> getCachedForms()
  {
    if (cachedForms == null)
    {
      FormFactory factory = FormFactory.getInstance();
      cachedForms = factory.getCachedForms();
    }
    return cachedForms;
  }

  public Set<String> getCachedFormSelectors()
  {
    return getCachedForms().keySet();
  }

  public Form getForm()
  {
    try
    {
      if (selector != null && selector.trim().length() > 0)
      {
        FormFactory factory = FormFactory.getInstance();
        return factory.getForm(selector, getData());
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String findForms()
  {
    try
    {
      String selectorBase = (String)selectorInputText.getValue();
      FormFactory formFactory = FormFactory.getInstance();
      List<FormDescriptor> descriptors = formFactory.findForms(selectorBase);
      formSelectItems.clear();
      formSelectItems.add(new SelectItem("", " "));
      for (FormDescriptor descriptor : descriptors)
      {
        SelectItem selectItem = new SelectItem();
        selectItem.setValue(descriptor.getSelector());
        selectItem.setDescription(descriptor.getTitle());
        selectItem.setLabel(descriptor.getTitle());
        formSelectItems.add(selectItem);
      }
      selector = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String submit()
  {
    dumpFormSelector = null;
    return null;
  }

  public String clearData()
  {
    data.clear();
    dumpFormSelector = null;
    return null;
  }

  public void clearForms()
  {
    try
    {
      FormFactory factory = FormFactory.getInstance();
      factory.clearForms();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    dumpFormSelector = null;
  }

  public void updateForms()
  {
    cachedForms = null;
    dumpFormSelector = null;
  }

  public String assignFieldValue()
  {
    getData();
    if ("null".equals(fieldValue))
    {
      data.put(fieldName, null);
    }
    else data.put(fieldName, fieldValue);
    dumpFormSelector = null;
    return null;
  }

  public void setDumpFormSelector(String selector)
  {
    dumpFormSelector = selector;
  }

  public String getDumpFormSelector()
  {
    return dumpFormSelector;
  }

  public String getDumpFormContent()
  {
    StringWriter writer = new StringWriter();
    try
    {
      FormFactory factory = FormFactory.getInstance();
      Form form = factory.getForm(dumpFormSelector, null);
      if (form != null)
      {
        FormDumper dumper = new FormDumper();
        dumper.dump(form, writer);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    String content = writer.toString();
    content = HtmlEncoder.encode(content);
    content = content.replaceAll(" ", "&nbsp;");
    content = content.replaceAll("\n", "</br>");
    return content;
  }
}
