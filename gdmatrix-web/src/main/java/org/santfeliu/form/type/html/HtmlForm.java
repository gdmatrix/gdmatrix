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
package org.santfeliu.form.type.html;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.santfeliu.form.Field;
import org.santfeliu.form.Form;
import org.santfeliu.form.View;
import org.santfeliu.util.TextUtils;
import org.santfeliu.util.template.WebTemplate;

/**
 *
 * @author realor
 */
public class HtmlForm implements Form, Serializable
{
  String id;
  String title;
  String language;
  String encoding = "UTF-8";
  WebTemplate template;
  Map<String, Object> properties = new HashMap();
  ArrayList<Field> fields = new ArrayList();
  HtmlView rootView;
  Map<String, HtmlView> viewsByRef = new HashMap();
  boolean evaluated;
  boolean contextDependant;
  long expires;
  String lastModified;
  public static final String TEXT_FORMAT = "text";
  public static final String NUMBER_FORMAT = "number";
  public static final String BOOLEAN_FORMAT = "boolean";
  public static final String DATE_FORMAT = "date";
  public static final String TIME_FORMAT = "time";
  public static final String DATETIME_FORMAT = "datetime";
  public static final long EXPIRE_MILLIS = 5 * 60 * 1000;

  public HtmlForm()
  {
    id = UUID.randomUUID().toString();
    expires = System.currentTimeMillis() + EXPIRE_MILLIS;
  }

  @Override
  public String getId()
  {
    return id;
  }

  @Override
  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  @Override
  public String getLanguage()
  {
    return language;
  }

  public void setLanguage(String language)
  {
    this.language = language;
  }

  public long getExpires()
  {
    return expires;
  }

  public void setExpires(long expires)
  {
    this.expires = expires;
  }

  @Override
  public String getLastModified()
  {
    return lastModified;
  }

  @Override
  public void setLastModified(String lastModified)
  {
    this.lastModified = lastModified;
  }

  public void setProperty(String name, Object value)
  {
    properties.put(name, value);
  }

  @Override
  public Object getProperty(String name)
  {
    return properties.get(name);
  }

  @Override
  public Collection<String> getPropertyNames()
  {
    return properties.keySet();
  }

  @Override
  public Collection<Field> getFields()
  {
    return fields;
  }

  public String getEncoding()
  {
    return encoding;
  }

  public void setEncoding(String encoding)
  {
    this.encoding = encoding;
  }

  @Override
  public Field getField(String reference)
  {
    if (reference == null) return null;
    Field field = null;
    Iterator<Field> iter = fields.iterator();
    while (iter.hasNext() && field == null)
    {
      Field currentField = iter.next();
      if (reference.equals(currentField.getReference()))
        field = currentField;
    }
    return field;
  }

  public void addField(Field field)
  {
    fields.add(field);
  }

  public HtmlField addReadOnlyField(String reference)
  {
    return addReadOnlyField(reference, Field.TEXT);
  }

  public HtmlField addReadOnlyField(String reference, String type)
  {
    HtmlField field = null;
    if (reference != null && !reference.startsWith("_"))
    {
      field = (HtmlField)getField(reference);
      if (field == null)
      {
        field = new HtmlField();
        field.setReference(reference);
        field.setLabel(reference); // default label
        field.setType(type); // default type
        field.setReadOnly(true); // default value in this case
        field.setMinOccurs(0); // default value in this case
        field.setMaxOccurs(1); // default value in this case
        fields.add(field);
      }
    }
    return field;
  }

  @Override
  public View getRootView()
  {
    return rootView;
  }

  public void setRootView(HtmlView rootView)
  {
    this.rootView = rootView;
  }

  @Override
  public View getView(String reference)
  {
    return viewsByRef.get(reference);
  }

  @Override
  public boolean validate(String reference, Object value, List errors,
    Locale locale)
  {
    boolean valid = true;
    Field field = getField(reference);
    if (field != null && !field.isReadOnly())
    {
      String label = cleanLabel(field.getLabel());
      System.out.print("Validating " + field);

      // check cardinality
      int cardinality;
      if (value == null) cardinality = 0;
      else if (value instanceof List) cardinality = ((List)value).size();
      else cardinality = 1;

      if (field.getMinOccurs() > 0 && cardinality == 0)
      {
        valid = false;
        String message = "El camp " + label + " és obligatori.";
        System.out.println(" [" + message + "] ");
        errors.add(message);
      }

      if (field.getMinOccurs() > 1 && field.getMinOccurs() > cardinality ||
          field.getMaxOccurs() > 0 && field.getMaxOccurs() < cardinality)
      {
        // multi valued property
        valid = false;
        String message = "El camp " + label +
          " té una cardinalitat incorrecta.";
        System.out.println(" [" + message + "] ");
        errors.add(message);
      }

      // check value type
      if (value != null && cardinality > 0)
      {
        if (value instanceof List) value = ((List)value).get(0);

        View view = getView(reference);
        String format = (view == null) ? null :
          (String)view.getProperty("format");
        String rule;
        if (format != null)
        {
          int index = format.indexOf(':');
          rule = (index == -1) ? null : format.substring(index + 1);
        }
        else rule = null;

        if (Field.TEXT.equals(field.getType()))
        {
          valid = (value instanceof String);
          if (valid && rule != null)
          {
            String text = (String)value;
            valid = text.matches(rule);
          }
        }
        else if (Field.NUMBER.equals(field.getType()))
        {
          valid = (value instanceof Number);
          if (valid && rule != null)
          {
            double number = ((Number)value).doubleValue();
            String tokens[] = rule.split(",");
            try
            {
              double min = Double.parseDouble(tokens[0].trim());
              double max = Double.parseDouble(tokens[1].trim());
              valid = (min <= number && number <= max);
            }
            catch (NumberFormatException ex)
            {
            }
          }
        }
        else if (Field.BOOLEAN.equals(field.getType()))
        {
          valid = (value instanceof Boolean);
        }
        else if (Field.DATE.equals(field.getType()))
        {
          valid = (value instanceof String);
          if (valid)
          {
            valid = TextUtils.parseInternalDate(value.toString()) != null;
          }
        }
        if (!valid)
        {
          String message = "El valor de " + label + " és incorrecte.";
          System.out.println(" [" + message + "] ");
          errors.add(message);
        }
      }
    }
    System.out.println(" : " + valid);
    return valid;
  }

  @Override
  public boolean validate(Map data, List errors, Locale locale)
  {
    boolean valid = true;
    for (Field field : fields)
    {
      String reference = field.getReference();
      Object value = data.get(reference);
      boolean validField = validate(reference, value, errors, locale);
      valid = valid && validField;
    }
    return valid;
  }

  @Override
  public Form evaluate(Map context) throws Exception
  {
    HtmlForm evaluatedForm = new HtmlForm();
    evaluatedForm.title = title;
    evaluatedForm.language = language;
    evaluatedForm.encoding = encoding;
    evaluatedForm.properties = new HashMap(properties);
    evaluatedForm.evaluated = true;
    evaluatedForm.contextDependant = contextDependant;

    if (template == null) // no template, easy way
    {
      // clone View tree
      evaluatedForm.viewsByRef = new HashMap();
      evaluatedForm.rootView =
        cloneViewTree(rootView, evaluatedForm.viewsByRef);

      // clone fields
      for (Field field : fields)
      {
        evaluatedForm.fields.add(((HtmlField)field).clone());
      }
    }
    else // we have template, hard way
    {
      // merge template
      StringWriter writer = new StringWriter();
      Map initialContext = new HashMap(context);
      template.merge(context, writer);

      // parse a new View tree
      HtmlParser parser = new HtmlParser(evaluatedForm);
      parser.parse(new StringReader(writer.toString()));

      // put referenced variables into evaluation context of evaluatedForm
      Set<String> variables = template.getReferencedVariables();
      for (String variable : variables)
      {
        if (!initialContext.containsKey(variable))
        {
          // new variable, set as read only field
          evaluatedForm.addReadOnlyField(variable);
        }
      }
    }
    evaluateDynamicViews(evaluatedForm, context);

    return evaluatedForm;
  }

  @Override
  public boolean isEvaluated()
  {
    return evaluated;
  }

  @Override
  public boolean isContextDependant()
  {
    return contextDependant;
  }

  @Override
  public boolean isOutdated()
  {
    if (expires == 0) return false;
    long now = System.currentTimeMillis();
    return (now > expires);
  }

  @Override
  public void submit(Map data)
  {
  }

  @Override
  public Map read(InputStream is) throws IOException
  {
    StringWriter writer = new StringWriter();
    template =
      WebTemplate.create(new InputStreamReader(is, encoding));
    template.write(writer);

    HtmlParser parser = new HtmlParser(this);
    parser.parse(new StringReader(writer.toString()));

    if (template.getExpressionFragmentCount() == 0)
    {
      template = null;
      contextDependant = false;

      for (HtmlView view : viewsByRef.values())
      {
        if (view instanceof HtmlSelectView)
        {
          HtmlSelectView selectView = (HtmlSelectView)view;

          // put parameters into evaluation context of evaluatedForm
          List<String> parameters = selectView.getParameters(this);
          if (!parameters.isEmpty())
          {
            contextDependant = true;
            break;
          }
        }
      }
    }
    else
    {
      contextDependant = true;
      // Add fields from template variables
      Set<String> variables = template.getReferencedVariables();
      for (String variable : variables)
      {
        addReadOnlyField(variable);
      }
    }
    return null;
  }

  @Override
  public void write(OutputStream os, Map data) throws IOException
  {
    HtmlPrinter printer = new HtmlPrinter(this);
    printer.print(new PrintWriter(os));
  }

  public void linkView(HtmlView view, HtmlField field)
  {
    String reference = field.getReference();
    if (reference != null)
    {
      view.setReference(reference);
      viewsByRef.put(reference, view);
    }
  }

  @Override
  public String toString()
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append(getClass().getName());
    buffer.append("/");
    buffer.append(id);
    if (evaluated)
    {
      buffer.append("/evaluated");
    }
    if (contextDependant)
    {
      buffer.append("/ctx_dep");
    }
    return buffer.toString();
  }

  // private and package methods

  private HtmlView cloneViewTree(HtmlView view,
    Map<String, HtmlView> viewsByRef)
  {
    HtmlView newView = (HtmlView)view.clone();
    String reference = newView.getReference();
    if (reference != null)
    {
      viewsByRef.put(reference, newView);
    }
    for (View child : view.getChildren())
    {
      newView.children.add(cloneViewTree((HtmlView)child, viewsByRef));
    }
    return newView;
  }

  private void evaluateDynamicViews(HtmlForm evaluatedForm, Map context)
  {
    for (HtmlView view : evaluatedForm.viewsByRef.values())
    {
      if (view instanceof HtmlSelectView)
      {
        HtmlSelectView selectView = (HtmlSelectView)view;
        selectView.evaluate(evaluatedForm, context);
      }
    }
  }

  private String cleanLabel(String label)
  {
    String result = label;
    if (result != null)
    {
      result = result.replace("(*)", "");
      result = result.replace("*", "");
      result = result.replace(":", "");
      result = result.trim();
    }
    return result;
  }
}
