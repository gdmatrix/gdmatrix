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
package org.santfeliu.webapp.modules.workflow;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.inject.Named;
import org.santfeliu.util.Properties;
import org.santfeliu.webapp.util.ComponentUtils;
import static org.santfeliu.webapp.util.FormImporter.ACTION_METHOD_OPTION;
import static org.santfeliu.webapp.util.FormImporter.ACTION_UPDATE_OPTION;
import org.santfeliu.workflow.form.Form;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class FlexWorkflowBean extends WorkflowBean implements Serializable
{
  private String selector;
  private final Map<String, Object> data = new LinkedHashMap<>();
  private final MultivaluedMap values = new MultivaluedMap();

  @Inject
  WorkflowInstanceBean instanceBean;

  public FlexWorkflowBean()
  {
  }

  public Map<String, Object> getData()
  {
    return data;
  }

  @Override
  public String show(Form form)
  {
    data.clear();
    data.putAll(instanceBean.getVariables());
    Properties parameters = form.getParameters();
    selector = (String)parameters.getProperty("selector");

    return "/pages/workflow/flex_form.xhtml";
  }

  public void loadDynamicComponents(ComponentSystemEvent event)
  {
    UIComponent panel = ComponentUtils.postAddToView(event);
    if (panel != null)
    {
      updateComponents(panel);
    }
  }

  public void doAction(String name, String value)
  {
    data.put(name, value);
    instanceBean.forward();
  }

  @Override
  public Map submit()
  {
    // remove unchanged variables
    Map variables = instanceBean.getVariables();
    for (Object key : variables.keySet())
    {
      String name = key.toString();
      Object oldValue = variables.get(name);
      Object newValue = data.get(name);
      boolean changed = (oldValue == null && newValue != null) ||
        !oldValue.equals(newValue);
      if (!changed) data.remove(name);
    }
    return data;
  }

  private void updateComponents(UIComponent panel)
  {
    try
    {
      Map<String, Object> panelAttributes = panel.getPassThroughAttributes();
      String renderedSelector = (String)panelAttributes.get("form_selector");

      if (!selector.equals(renderedSelector))
      {
        panelAttributes.put("form_selector", selector);

        panel.getChildren().clear();

        Map<String, Object> options = new HashMap<>();
        options.put(ACTION_METHOD_OPTION, "flexWorkflowBean.doAction");
        options.put(ACTION_UPDATE_OPTION, ":mainform:cnt");

        ComponentUtils.includeFormComponents(panel, selector,
           "flexWorkflowBean.value", "flexWorkflowBean.values", data, options);
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      error(ex);
    }
  }

  public Map<String, Object> getValue()
  {
    return data;
  }

  public MultivaluedMap getValues()
  {
    return values;
  }

  public class MultivaluedMap extends AbstractMap<String, Object>
    implements Serializable
  {
    @Override
    public Object put(String key, Object value)
    {
      if (value != null && value.getClass().isArray())
      {
        for (String k : data.keySet())
        {
          if (k.equals(key) || k.startsWith(key + "_"))
            data.put(k, null);
        }

        int length = Array.getLength(value);
        for (int i = 0; i < length; i++)
        {
          String name = key + "_" + i;
          data.put(name, Array.get(value, i));
        }
      }
      return value;
    }

    @Override
    public Object get(Object key)
    {
      String name = String.valueOf(key);
      List<Object> list = data.keySet().stream()
        .filter(k -> k.startsWith(name + "_"))
        .map(k -> data.get(k))
        .collect(Collectors.toList());
      return list;
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet()
    {
      return data.entrySet();
    }

    @Override
    public int size()
    {
      return data.size();
    }
  }

  public static void main(String[] args)
  {
    Map<String,Object> map = new LinkedHashMap();
    map.put("a_0", "1");
    map.put("a_1", "2");
    map.put("b_1", "3");
    List list = map.keySet().stream()
      .filter(k -> k.startsWith("a_"))
      .map(k -> map.get(k))
      .collect(Collectors.toList());
    System.out.println(list);

    map.entrySet().stream()
          .forEach(e -> {if (e.getKey().equals("a_1"))
                      e.setValue(null);});

    System.out.println(map);

  }
}
