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
package org.santfeliu.matrix.ide;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.santfeliu.util.script.ScriptableBase;

/**
 *
 * @author realor
 */
public class JavaScriptRunner extends Thread
{
  public static final Logger LOGGER =
    Logger.getLogger(JavaScriptRunner.class.getSimpleName());

  private final String code;
  private final JTextArea outputTextArea;
  private final HashMap variables = new HashMap();
  private Object result;
  private long startMillis;
  private long executionTime;
  private Consumer resultConsumer;

  public JavaScriptRunner(String code, JTextArea outputTextArea)
  {
    this.code = code;
    this.outputTextArea = outputTextArea;
  }

  public HashMap getVariables()
  {
    return variables;
  }

  @Override
  public void run()
  {
    Context cx = ContextFactory.getGlobal().enterContext();
    OutputHandler handler = new OutputHandler();
    handler.setFilter(record ->
      JavaScriptRunner.this.getId() == record.getLongThreadID());
    handler.setLevel(Level.ALL);
    LOGGER.addHandler(handler);
    LOGGER.setLevel(Level.ALL);
    try
    {
      variables.put("output", new PrintWriter(new OutputWriter()));
      variables.put("logger", LOGGER);

      startMillis = System.currentTimeMillis();
      Scriptable scope = new ScriptableBase(cx, variables);
      result = cx.evaluateString(scope, code, "", 1, null);
    }
    catch (Exception ex)
    {
      result = ex.toString();
    }
    catch (ThreadDeath d)
    {
      result = "Execution interrupted.";
    }
    finally
    {
      LOGGER.removeHandler(handler);

      executionTime = System.currentTimeMillis() - startMillis;

      Context.exit();
      SwingUtilities.invokeLater(() ->
      {
        if (!(result instanceof Undefined))
        {
          outputTextArea.append("\n" + result);
        }
        String time;
        if (executionTime > 3600000)
        {
          time = (executionTime / 3600000.0) + " hours.";
        }
        else if (executionTime > 60000)
        {
          time = (executionTime / 60000.0) + " min.";
        }
        else if (executionTime > 1000)
        {
          time = (executionTime / 1000.0) + " sec.";
        }
        else
        {
          time = executionTime + " ms.";
        }

        outputTextArea.append("\nExecution time: " + time);

        if (resultConsumer != null)
        {
          resultConsumer.accept(result);
        }
      });
    }
  }

  public Consumer getResultConsumer()
  {
    return resultConsumer;
  }

  public void setResultConsumer(Consumer resultConsumer)
  {
    this.resultConsumer = resultConsumer;
  }

  public void end()
  {
    try
    {
      Method method = this.getClass().getMethod("stop", new Class[0]);
      if (method != null)
      {
        method.invoke(this, new Object[0]);
      }
    }
    catch (Exception ex)
    {
      // ignore
    }
  }

  private void outputText(String text)
  {
    SwingUtilities.invokeLater(() ->
    {
      outputTextArea.append(text);
    });

    // give time to EVT to update JTextArea
    try
    {
      Thread.sleep(1);
    }
    catch (InterruptedException ex)
    {
      // ignore
    }
  }

  class OutputWriter extends Writer
  {
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException
    {
      String text = new String(cbuf, off, len);
      outputText(text);
    }

    @Override
    public void flush() throws IOException
    {
    }

    @Override
    public void close() throws IOException
    {
    }
  }

  class OutputHandler extends Handler
  {
    @Override
    public void publish(LogRecord record)
    {
      if (isLoggable(record))
      {
        String level = record.getLevel().getName();
        String formattedMessage;
        if (record.getParameters() == null)
        {
          formattedMessage = record.getMessage();
        }
        else
        {
          formattedMessage =
            MessageFormat.format(record.getMessage(), record.getParameters());
        }
        outputText(level + ": " + formattedMessage + "\n");
      }
    }

    @Override
    public void flush()
    {
    }

    @Override
    public void close() throws SecurityException
    {
    }
  }
}
