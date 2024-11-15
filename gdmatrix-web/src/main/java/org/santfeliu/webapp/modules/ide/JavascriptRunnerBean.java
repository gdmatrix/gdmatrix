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
package org.santfeliu.webapp.modules.ide;

import com.google.gson.Gson;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import jdk.nashorn.internal.objects.NativeJSON;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.WrappedException;
import org.santfeliu.util.script.ScriptableBase;
import org.santfeliu.web.servlet.stream.StreamQueue;

/**
 *
 * @author realor
 */
@Named
@SessionScoped
public class JavascriptRunnerBean implements Serializable
{
  transient Map<String, Runner> runners = new HashMap<>();
  static final int END_TAG = 0;

  public Runner getRunner(String consoleId)
  {
    return runners.get(consoleId);
  }

  public void run(String consoleId, String source) throws Exception
  {
    Runner runner = runners.get(consoleId);
    if (runner != null) throw new Exception("Already running");

    runner = new Runner(consoleId, source);
    runners.put(consoleId, runner);

    runner.start();
  }

  public class Runner extends Thread
  {
    String consoleId;
    String source;

    public Runner(String consoleId, String source)
    {
      this.consoleId = consoleId;
      this.source = source;
      this.setName("JavascriptRunner-" + consoleId);
    }

    @Override
    public void run()
    {
      StreamQueue queue = StreamQueue.getInstance(consoleId, true);
      Console console = new Console(queue);

      Context ctx = Context.enter();
      Object result;
      try
      {
        ScriptableBase scope = new ScriptableBase(ctx);
        scope.put("console", scope, console);
        Object value = ctx.evaluateString(scope, source, "source", 0, null);
        result = unwrap(value);
        queue.push(new Message("info", result));
      }
      catch (WrappedException wex)
      {
        result = wex.getWrappedException();
        queue.push(new Message("error", result));
      }
      catch (ThreadDeath d)
      {
        queue.push(new Message("error", "Thread death"));
      }
      catch (Throwable ex)
      {
        result = ex.toString();
        queue.push(new Message("error", result));
      }
      finally
      {
        queue.push(END_TAG);
        runners.remove(consoleId);
        Context.exit();
      }
    }

    Object unwrap(Object result)
    {
      if (result instanceof NativeJavaObject)
      {
        NativeJavaObject nat = (NativeJavaObject)result;
        result = nat.unwrap();
      }
      if (result instanceof Undefined)
        result = null;

      return result;
    }

    void terminate()
    {
      final Thread threadToKill = this;

      Thread killer = new Thread(() ->
      {
        int count = 0;
        while (threadToKill.isAlive() && count < 3)
        {
          threadToKill.interrupt();
          try
          {
            Thread.sleep(1000);
          }
          catch (Exception ex)
          {
          }
          count++;
        }
        if (threadToKill.isAlive())
        {
          try
          {
            Thread.class.getMethod("stop").invoke(threadToKill);
          }
          catch (Exception ex)
          {
          }
        }
      }, "Killer-" + threadToKill.getName());
      killer.start();
    }

    public String getSource()
    {
      return source;
    }

    public String getConsoleId()
    {
      return consoleId;
    }
  }

  public class Console
  {
    StreamQueue queue;

    public Console(StreamQueue queue)
    {
      this.queue = queue;
    }

    public void info(Object ...values)
    {
      _log("info", values);
    }

    public void warn(Object ...values)
    {
      _log("warn", values);
    }

    public void error(Object ...values)
    {
      _log("error", values);
    }

    public void log(Object ...values)
    {
      _log("log", values);
    }

    public void _log(String level, Object ...values)
    {
      queue.push(new Message(level, values));

      try
      {
        Thread.sleep(100);
      }
      catch (Exception ex)
      {
        throw new RuntimeException("Execution interrupted");
      }
    }
  }

  public class Message
  {
    String level;
    Object[] values;

    public Message(String level, Object ...values)
    {
      this.level = level;
      this.setValues(values);
    }

    public String getLevel()
    {
      return level;
    }

    public Object[] getValues()
    {
      return values;
    }

    private void setValues(Object[] values)
    {
      this.values = new Object[values.length];
      for (int i = 0; i < values.length; i++)
      {
        Object value = values[i];
        if (value instanceof String ||
          value instanceof Number ||
          value instanceof Boolean ||
          value == null)
        {
          this.values[i] = value;
        }
        else if (value instanceof NativeObject || value instanceof NativeArray)
        {
          Gson gson = new Gson();
          this.values[i] = gson.toJson(value);
        }
        else if (value instanceof NativeJavaObject)
        {
          Object javaObject = ((NativeJavaObject)value).unwrap();
          this.values[i] = javaObject.toString();
        }
        else
        {
          this.values[i] = String.valueOf(value);
        }
      }
    }
  }

  @PreDestroy
  public void onDestroy()
  {
    for (Runner runner : runners.values())
    {
      System.out.println("Interrupting " + runner);
      runner.terminate();
    }
    runners.clear();
  }
}
