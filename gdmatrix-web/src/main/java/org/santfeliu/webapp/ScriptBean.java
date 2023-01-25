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
package org.santfeliu.webapp;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.FacesEvent;
import javax.inject.Named;
import org.mozilla.javascript.Callable;
import org.santfeliu.util.script.ScriptClient;

/**
 * Utility bean to call methods defined in scripts.
 *
 * Script data in saved in request scope.
 *
 * Examples:
 *
 * #{scriptBean.ssmCat.name} => calls getName() & setName(value)
 * #{scriptBean.accounting.removeAll.invoke} => calls removeAll()
 * #{scriptBean.ssmCat.remove.invoke(2)} => calls remove(index)
 * #{scriptBean.accounting.onClose.process} => calls onClose(event)
 *
 * @author realor
 */

@ApplicationScoped
@Named
public class ScriptBean extends AbstractMap<String, Object>
  implements Serializable
{
  @Override
  public Set<Entry<String, Object>> entrySet()
  {
    return Collections.EMPTY_SET;
  }

  @Override
  public ScriptWrapper get(Object scriptName)
  {
    if (!(scriptName instanceof String)) return null;

    Map<String, Object> requestMap =
      FacesContext.getCurrentInstance().getExternalContext().getRequestMap();

    Object object = requestMap.get(scriptName);
    ScriptWrapper wrapper;
    if (object instanceof ScriptWrapper)
    {
      wrapper = (ScriptWrapper)object;
    }
    else
    {
      wrapper = new ScriptWrapper(scriptName.toString());
      requestMap.put(scriptName.toString(), wrapper);
    }
    return wrapper;
  }

  public class ScriptWrapper extends AbstractMap<String, Object>
    implements Serializable
  {
    private final ScriptClient client = new ScriptClient();

    public ScriptWrapper(String scriptName)
    {
      try
      {
        client.executeScript(scriptName);
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }

    @Override
    public Set<Entry<String, Object>> entrySet()
    {
      return Collections.EMPTY_SET;
    }

    @Override
    public Object get(Object member)
    {
      Object result = null;
      try
      {
        if (!(member instanceof String)) return null;

        String smember = (String)member;
        result = client.get(smember);

        if (result instanceof Callable)
        {
          result = new MethodWrapper((Callable)result);
        }
        else if (result == null)
        {
          String getterName =
            "get" + smember.substring(0, 1).toUpperCase() + smember.substring(1);

          Object getter = client.get(getterName);
          if (getter instanceof Callable)
          {
            Callable callable = (Callable)getter;
            result = client.execute(callable, new Object[0]);
          }
        }
        else
        {
          result = ScriptClient.unwrap(result);
        }
      }
      catch (Exception ex)
      {
        throw new RuntimeException(ex);
      }
      return result;
    }

    @Override
    public Object put(String member, Object value)
    {
      try
      {
        if (!(member instanceof String)) return null;

        String smember = (String)member;

        String setterName =
          "set" + smember.substring(0, 1).toUpperCase() + smember.substring(1);

        Object setter = client.get(setterName);
        if (setter instanceof Callable)
        {
          Callable callable = (Callable)setter;
          client.execute(callable, new Object[]{value});
        }
      }
      catch (Exception ex)
      {
        throw new RuntimeException(ex);
      }
      return null;
    }

    public class MethodWrapper
    {
      private final Callable callable;

      public MethodWrapper(Callable callable)
      {
        this.callable = callable;
      }

      public String invoke()
      {
        return invokeArgs(new Object[0]);
      }

      public String invoke(Object arg)
      {
        return invokeArgs(new Object[]{arg});
      }

      public String invoke(Object arg0, Object arg1)
      {
        return invokeArgs(new Object[]{arg0, arg1});
      }

      public String invoke(Object arg0, Object arg1, Object arg2)
      {
        return invokeArgs(new Object[]{arg0, arg1, arg2});
      }

      public String invokeArgs(Object[] args)
      {
        Object result = client.execute(callable, args);
        return result instanceof String ? (String) result : null;
      }

      public void process(FacesEvent event)
      {
        invokeArgs(new Object[]{ event});
      }
    }
  }

}
