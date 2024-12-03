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

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.map.LRUMap;
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
  private final List<FormBuilder> builders = new ArrayList<>();
  // Form cache
  private final LRUMap formCache;

  public FormFactory()
  {
    // TODO: externally setup
    builders.add(new ReferenceFormBuilder());
    builders.add(new PathFormBuilder());
    builders.add(new URLFormBuilder());
    builders.add(new TypeFormBuilder());
    builders.add(new DocumentFormBuilder());
    formCache = new LRUMap(100);
  }

  public static FormFactory getInstance()
  {
    return factory;
  }

  public List<FormDescriptor> findForms(String selector)
  {
    List<FormDescriptor> formDescriptors = new ArrayList();
    for (FormBuilder builder : builders)
    {
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
    String contextHash = hash(context);

    // when updated == true, returned form is updated when
    // method form.isOutdated() returns true
    // look for evaluated form for this context in cache
    Form form = restoreForm(selector, contextHash, updated);

    if (form == null)
    {
      // restore non evaluated
      form = restoreForm(selector, "", updated);
    }

    if (form == null) // form not in cache or is outdated
    {
      // call builders to create form
      Iterator<FormBuilder> iter = builders.iterator();
      while (iter.hasNext() && form == null)
      {
        FormBuilder builder = iter.next();
        form = builder.getForm(selector); // non evaluated form
      }
      if (form != null)
      {
        saveForm(selector, "", form); // save new form (non evaluated)
      }
    }

    if (form != null && !form.isEvaluated() && context != null)
    {
      form = form.evaluate(context);
      System.out.println("Evaluate form " + selector + " for " + context + " = " + form);
      saveForm(selector, contextHash, form); // save evaluated form
    }
    return form;
  }

  public synchronized Map<String, Form> getCachedForms()
  {
    return Collections.unmodifiableMap(new HashMap(formCache));
  }

  public void clearForms()
  {
    formCache.clear();
  }

  public void clearForm(String selector)
  {
    removeForm(selector);
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

  public List<FormBuilder> getFormBuilders()
  {
    return Collections.unmodifiableList(builders);
  }

  private String hash(Map context)
  {
    if (context == null || context.isEmpty()) return "";

    try
    {
      StringBuilder buffer = new StringBuilder();
      Iterator<String> iter = context.keySet().iterator();
      while (iter.hasNext())
      {
        String property = iter.next();
        if (!property.startsWith("_"))
        {
          Object value = context.get(property);
          buffer.append(property);
          buffer.append("=");
          buffer.append(value);
        }
      }
      String text = buffer.toString();
      MessageDigest digester = MessageDigest.getInstance("SHA-256");
      byte[] bytes = digester.digest(text.getBytes("UTF-8"));
      StringBuilder hexString = new StringBuilder(2 * bytes.length);
      for (int i = 0; i < bytes.length; i++)
      {
        String hex = Integer.toHexString(0xff & bytes[i]);
        if(hex.length() == 1)
        {
          hexString.append('0');
        }
        hexString.append(hex);
      }
      return hexString.toString();
    }
    catch (Exception ex)
    {
      return "";
    }
  }

  // Form cache entries

  private synchronized void saveForm(String selector,
    String contextHash, Form form)
  {
    String key = selector + "/" + contextHash;
    formCache.put(key, form);
  }

  private synchronized Form restoreForm(String selector,
    String contextHash, boolean updated)
  {
    String key = selector + "/" + contextHash;
    Form form = (Form)formCache.get(key);
    if (form != null && updated && form.isOutdated())
    {
      formCache.remove(key);
      form = null;
    }
    return form;
  }

  private synchronized void removeForm(String selector)
  {
    Iterator iter = formCache.keySet().iterator();
    while (iter.hasNext())
    {
      String key = (String)iter.next();
      if (key.startsWith(selector + "/"))
      {
        iter.remove();
      }
    }
  }
}
