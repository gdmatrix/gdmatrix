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
package org.santfeliu.webapp.util;

import java.util.Iterator;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import org.mozilla.javascript.Callable;
import org.primefaces.component.tabview.TabView;
import org.santfeliu.form.Form;
import org.santfeliu.form.FormFactory;
import org.santfeliu.util.script.ScriptClient;

/**
 *
 * @author realor
 */
public class ComponentUtils
{

  public static <T extends UIComponent> T findComponent(String expression)
  {
    UIViewRoot viewRoot = FacesContext.getCurrentInstance().getViewRoot();
    try
    {
      return (T)viewRoot.findComponent(expression);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  public static void includeFormComponents(UIComponent parent, String selector,
    String propertyPath, Map context) throws Exception
  {
    includeFormComponents(parent, selector, propertyPath, propertyPath + "s", 
      context, null);
  }

  public static void includeFormComponents(UIComponent parent, String selector,
    String propertyPathUni, String propertyPathMulti, 
    Map context, Map<String, Object> options) throws Exception
  {
    FormFactory formFactory = FormFactory.getInstance();
    formFactory.clearForm(selector);
    Form form = formFactory.getForm(selector, context, false);

    FormImporter formImporter = new FormImporter();
    if (options != null)
    {
      formImporter.getOptions().putAll(options);
    }

    formImporter.importForm(form, parent, propertyPathUni, propertyPathMulti);
  }

  public static void includeScriptComponents(UIComponent parent,
    String scriptName) throws Exception
  {
    ScriptClient client = new ScriptClient();
    client.refreshCache();
    client.executeScript(scriptName);
    Callable callable = (Callable) client.get("includeComponents");
    if (callable != null)
    {
      client.execute(callable, parent, scriptName);
    }
  }

  public static void selectTabWithErrors(String tabViewClientId)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    if (context.isValidationFailed())
    {
      UIViewRoot viewRoot = context.getViewRoot();
      Iterator<String> iter = context.getClientIdsWithMessages();
      if (iter.hasNext())
      {
        String id = iter.next();
        UIComponent component = viewRoot.findComponent(id);

        while (component != null && component != viewRoot)
        {
          if (component instanceof org.primefaces.component.tabview.Tab)
          {
            TabView currentTabView = (TabView)component.getParent();
            int index = currentTabView.getChildren().indexOf(component);
            if (index >= 0)
            {
              currentTabView.setActiveIndex(index);
              if (currentTabView.getClientId().equals(tabViewClientId))
                return;
            }
          }
          component = component.getParent();
        }
      }
    }
  }

}
