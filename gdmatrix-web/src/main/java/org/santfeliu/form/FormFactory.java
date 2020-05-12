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
package org.santfeliu.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.santfeliu.form.builder.DocumentFormBuilder;
import org.santfeliu.form.builder.PathFormBuilder;
import org.santfeliu.form.builder.ReferenceFormBuilder;
import org.santfeliu.form.builder.TypeFormBuilder;
import org.santfeliu.form.builder.URLFormBuilder;

/**
 * selector format: <type>:<parameters>
 *
 * @author realor
 */

public class FormFactory
{
  static FormFactory factory = new FormFactory();

  // builders list
  private List<FormBuilder> builders = new ArrayList<FormBuilder>();
  // Form cache
  private Entry first;
  private int numEntries = 0;
  private int maxEntries = 20;

  public FormFactory()
  {
    // TODO: externally setup
    builders.add(new ReferenceFormBuilder());
    builders.add(new PathFormBuilder());
    builders.add(new URLFormBuilder());
    builders.add(new TypeFormBuilder());
    builders.add(new DocumentFormBuilder());
  }

  public static FormFactory getInstance()
  {
    return factory;
  }
  
  public List<FormDescriptor> findForms(String selector)
  {
    List<FormDescriptor> formDescriptors = new ArrayList();
    Iterator<FormBuilder> iter = builders.iterator();
    while (iter.hasNext())
    {
      FormBuilder builder = iter.next();
      formDescriptors.addAll(builder.findForms(selector));
    }
    return formDescriptors;
  }

  public Form getForm(String selector, Map context) throws Exception
  {
    return getForm(selector, context, false);
  }

  public Form getForm(String selector, Map context, boolean updated)
    throws Exception
  {
    // when updated == true, returned form is updated when
    // method form.isOutdated() returns true
    Form form = null;

    // look for evaluated form for this context in cache
    form = restoreForm(selector, context, updated);

    if (form == null && context != null)
    {
      // no evaluated form was found in cache
      // look for non evaluated form in cache
      form = restoreForm(selector, null, updated);
    }

    if (form == null) // form not in cache or is outdated
    {
      // call builders to create form
      Iterator<FormBuilder> iter = builders.iterator();
      while (iter.hasNext() && form == null)
      {
        FormBuilder builder = iter.next();
        form = builder.getForm(selector);
      }
      if (form != null)
      {
        saveForm(selector, form); // save new form (non evaluated)
      }
    }

    // evaluate form for this context
    if (form != null && form.getContext() == null && context != null)
    {
      // evaluate form from non evaluated form
      form = form.evaluate(context);
      saveForm(selector, form); // save evaluated form
    }
    return form;
  }

  public void clearForms()
  {
    this.first = null;
    this.numEntries = 0;
  }

  public void clearForm(String selector)
  {
    removeForm(selector);
  }

  public void clearForm(String selector, Map context)
  {
    removeForm(selector, context);
  }

  public int getMaxEntries()
  {
    return maxEntries;
  }

  public void setMaxEntries(int maxEntries)
  {
    this.maxEntries = maxEntries;
    if (numEntries > maxEntries) clearForms();
  }

  // builders
  public void addFormBuilder(FormBuilder builder)
  {
    builders.add(builder);
  }

  public void removeFormBuilder(FormBuilder builder)
  {
    builders.remove(builder);
  }

  public List<Entry> getFormEntries()
  {
    ArrayList<Entry> entries = new ArrayList();
    Entry current = first;
    while (current != null)
    {
      entries.add(current);
      current = current.next;
    }
    return entries;
  }

  public List<FormBuilder> getFormBuilders()
  {
    return Collections.unmodifiableList(builders);
  }

  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append("FormFactory(numEntries: ");
    builder.append(numEntries);
    builder.append(", ");
    Entry current = first;
    while (current != null)    
    {
      builder.append("(");
      builder.append(current);
      builder.append(")");
      current = current.next;
      if (current != null) builder.append(", ");
    }
    builder.append(")");
    return builder.toString();
  }

  // Form cache entries

  private synchronized void saveForm(String selector, Form form)
  {
    Map context = form.getContext();
    Entry entry = getEntry(selector, context, true);
    entry.form = form;
  }

  private synchronized Form restoreForm(String selector, Map context,
    boolean updated)
  {
    Entry entry = getEntry(selector, context, false);
    Form form = (entry == null) ? null : entry.form;
    if (form != null && updated && form.isOutdated()) form = null;
    return form;
  }

  private synchronized void removeForm(String selector)
  {
    Entry current = first;
    Entry previous = null;
    while (current != null)
    {
      if (current.selector.equals(selector))
      {
        // found, remove it
        if (previous == null)
        {
          first = current.next;
        }
        else
        {
          previous.next = current.next;
        }
        current = current.next;
        numEntries--;
      }
      else
      {
        // skip
        previous = current;
        current = current.next;
      }
    }
  }

  private synchronized void removeForm(String selector, Map context)
  {
    Entry entry = getEntry(selector, context, false);
    if (entry != null)
    {
      first = entry.next;
      entry.next = null;
      numEntries--;
    }
  }

  // It returns the entry for this selector + context and put on first position
  private Entry getEntry(String selector, Map context, boolean create)
  {
    Entry current = first;
    Entry previous = null;

    boolean found = false;
    while (current != null && !found)
    {
      if (entryMatch(current, selector, context))
      {
        // move first
        if (first != current) 
        {
          // first and previous are always != null in this point
          previous.next = current.next;
          current.next = first;
          first = current;
        }
        found = true;
      }
      else
      {
        if (current.next != null) previous = current;
        current = current.next;
      }
    }
    if (!found && create)
    {
      current = new Entry();
      current.selector = selector;
      current.next = first;
      first = current;
      if (numEntries < maxEntries)
      {
        numEntries++; // add new entry
      }
      else if (previous != null)
      {
        previous.next = null; // remove last
      }
    }
    return current;
  }

  private boolean entryMatch(Entry entry, String selector, Map context)
  {
    if (entry.selector.equals(selector))
    {
      Map evaluationContext = entry.form.getContext();
      if (context == null && evaluationContext == null)
      {
        return true;
      }
      else if (context != null && evaluationContext != null)
      {
        boolean match = true;
        Iterator<Map.Entry> iter = evaluationContext.entrySet().iterator();
        while (iter.hasNext() && match)
        {
          Map.Entry e = iter.next();
          String name = String.valueOf(e.getKey());
          Object value1 = e.getValue();
          Object value2 = context.get(name);
          match = (value1 == null && value2 == null) || 
                  (value1 != null && value1.equals(value2));
        }
        return match;
      }
    }
    return false;
  }

  public class Entry
  {
    String selector;
    Form form;
    Entry next;

    public String getSelector()
    {
      return selector;
    }

    public Form getForm()
    {
      return form;
    }

    @Override
    public String toString()
    {
      return (form == null) ?
        selector : "\"" + selector + "\", " + form + ", " + form.getContext();
    }
  }

  public static void main(String[] args)
  {
    try
    {
      FormFactory formFactory = FormFactory.getInstance();

      Map context = new HashMap();
      context.put("a", 23);
      context.put("b", 12);
      context.put("title", "TEST");

      Form form1 = formFactory.getForm("path:c:/aaa.html", context);
      System.out.println(form1);
      Form form1e = form1.evaluate(context);
      System.out.println("Context: " + form1e.getContext());
      System.out.println(formFactory + "\n");
/*
      Form form2 = formFactory.getForm("path:c:/sample2.html", context);
      System.out.println(form2);
      System.out.println(formFactory + "\n");

      Form form3 = formFactory.getForm("path:c:/sample3.html", context);
      System.out.println(form3);
      System.out.println(formFactory + "\n");

      Form form4 = formFactory.getForm("path:c:/sample4.html", context);
      System.out.println(form4);
      System.out.println(formFactory + "\n");

      Form form5 = formFactory.getForm("path:c:/sample5.html", context);
      System.out.println(form5);
      System.out.println(formFactory + "\n");

      Form form6 = formFactory.getForm("path:c:/sample4.html", context);
      System.out.println(form6);
      System.out.println(formFactory + "\n");

      Form form7 = formFactory.getForm("path:c:/sample5.html", context);
      System.out.println(form7);
      System.out.println(formFactory + "\n");

 */
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
