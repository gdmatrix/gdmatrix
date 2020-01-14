package org.santfeliu.test.web;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
  private List<SelectItem> formSelectItems = new ArrayList<SelectItem>();
  private String selector;
  private Map data;
  private String fieldName;
  private String fieldValue;
  private String dumpFormId;
  
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
      Collections.sort(dataEntries, new Comparator()
      {
        public int compare(Object o1, Object o2)
        {
          Object[] a1 = (Object[])o1;
          Object[] a2 = (Object[])o2;
          String s1 = (String)a1[0];
          String s2 = (String)a2[0];
          return s1.compareTo(s2);
        }
      });
    }
    return dataEntries;
  }

  public List getFormFactoryEntries()
  {
    FormFactory factory = FormFactory.getInstance();
    return factory.getFormEntries();
  }

  public String getFormEntryClass()
  {
    FormFactory.Entry entry = (FormFactory.Entry)getValue("#{row}");
    Form form = entry.getForm();    
    return form.getClass().getName();
  }

  public Form getForm()
  {
    try
    {
      if (selector != null && selector.trim().length() > 0)
      {
        FormFactory factory = FormFactory.getInstance();
        // update form only in render phase
        boolean updated = getFacesContext().getRenderResponse();
        return factory.getForm(selector, getData(), updated);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String getDumpFormId()
  {
    return dumpFormId;
  }

  public String getDumpFormContent()
  {
    StringWriter writer = new StringWriter();
    try
    {
      FormFactory factory = FormFactory.getInstance();
      Iterator<FormFactory.Entry> iter = factory.getFormEntries().iterator();
      Form form = null;
      while (iter.hasNext() && form == null)
      {
        FormFactory.Entry entry = iter.next();
        if (entry.getForm().getId().equals(dumpFormId))
          form = entry.getForm();
      }
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

  /*** actions ***/

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
    dumpFormId = null;
    return null;
  }

  public String clearData()
  {
    data.clear();
    dumpFormId = null;
    return null;
  }

  public String clearForms()
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
    dumpFormId = null;
    return null;
  }

  public String assignFieldValue()
  {
    getData();
    if ("null".equals(fieldValue))
    {
      data.put(fieldName, null);
    }
    else data.put(fieldName, fieldValue);
    dumpFormId = null;
    return null;
  }

  public String dumpForm()
  {
    FormFactory.Entry entry = (FormFactory.Entry)getValue("#{row}");
    dumpFormId = entry.getForm().getId();
    return null;
  }
}
