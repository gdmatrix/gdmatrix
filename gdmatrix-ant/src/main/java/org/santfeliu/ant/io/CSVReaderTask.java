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
package org.santfeliu.ant.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;
import org.santfeliu.ant.js.AntScriptable;
import org.santfeliu.ant.js.ScriptableTask;

/**
 *
 * @author Administrador
 */
public class CSVReaderTask extends ScriptableTask
  implements TaskContainer
{
  private File file;
  private String columns;
  private String encoding;
  private String separator = ",";
  private String quote = "\"";
  private boolean skipHeader = true;
  private String skipValues;
  private ArrayList<Task> tasks = new ArrayList();

  public File getFile()
  {
    return file;
  }

  public void setFile(File file)
  {
    this.file = file;
  }

  public String getColumns()
  {
    return columns;
  }

  public void setColumns(String columns)
  {
    this.columns = columns;
  }

  public String getEncoding()
  {
    return encoding;
  }

  public void setEncoding(String encoding)
  {
    this.encoding = encoding;
  }

  public String getSeparator()
  {
    return separator;
  }

  public void setSeparator(String separator)
  {
    this.separator = separator;
  }

  public String getQuote()
  {
    return quote;
  }

  public void setQuote(String quote)
  {
    this.quote = quote;
  }

  public boolean isSkipHeader()
  {
    return skipHeader;
  }

  public void setSkipHeader(boolean skipHeader)
  {
    this.skipHeader = skipHeader;
  }

  public String getSkipValues()
  {
    return skipValues;
  }

  public void setSkipValues(String skipValues)
  {
    this.skipValues = skipValues;
  }
  
  public void addTask(Task task)
  {
    tasks.add(task);
  }
  
  @Override
  public void execute() throws BuildException
  {
    if (file == null) 
      throw new BuildException("Attribute 'file' is required");
    if (columns == null) 
      throw new BuildException("Attribute 'columns' is required");
    if (!file.exists()) 
      throw new BuildException("File " + file + " not found");
    try
    {
      String[] columnNames = columns.split(",");
      for (int i = 0; i < columnNames.length; i++)
      {
        String columnName = columnNames[i].replace('\n', ' ');
        columnName = columnName.trim();
      }
      readFile(columnNames);
    }
    catch (Exception ex)
    {
      throw new BuildException(ex);
    }
  }

  private void readFile(String[] columnNames) throws Exception
  {
    AntScriptable scriptable = getScriptable();
    InputStreamReader isr;
    if (encoding == null)
    {
      // use default encoding
      isr = new InputStreamReader(new FileInputStream(file));
    }
    else
    {
      // use specified encoding
      isr = new InputStreamReader(new FileInputStream(file), encoding);
    }
    String[] skipValuesArray = null;
    if (skipValues != null)
    {
      skipValuesArray = skipValues.split(separator);
    }
    BufferedReader reader = new BufferedReader(isr);
    try
    {
      String line = reader.readLine();
      if (skipHeader) line = reader.readLine();
      while (line != null)
      {
        String[] values = parseLine(line);
        if (!skip(values, skipValuesArray))
        {
          for (int i = 0; i < columnNames.length; i++)
          {
            String value = (i < values.length) ? decodeValue(values[i]) : null;
            scriptable.put(columnNames[i], scriptable, value);
          }
          for (Task task : tasks) task.perform();
        }
        line = reader.readLine();
      }
    }
    finally
    {
      reader.close();
    }
  }

  private boolean skip(String[] values, String[] skipValuesArray)
  {
    if (skipValuesArray != null)
    {
      for (int i = 0; i < values.length; i++)
      {
        if (skipValuesArray.length > i)
        {
          String value1 = values[i];
          String value2 = skipValuesArray[i];

          if (value1 != null)
          {
            if (value2.length() > 0 && !value2.equals("*"))
            {
              if (value2.startsWith("!"))
              {
                if (!value1.equals(value2.substring(1))) return true;
              }
              else
              {
                if (value1.equals(value2)) return true;
              }
            }
          }
        }
      }
    }
    return false;
  }
  
  private String[] parseLine(String line)
  {
    ArrayList<String> values = new ArrayList<String>();
    char ch;
    int i = 0;
    boolean inQuote = false;
    StringBuilder buffer = new StringBuilder();
    while (i < line.length())
    {
      ch = line.charAt(i);
      if (ch == separator.charAt(0) && !inQuote)
      {
        values.add(buffer.toString());
        buffer.setLength(0);
      }
      else if (ch == quote.charAt(0))
      {
        inQuote = !inQuote;
      }
      else
      {
        buffer.append(ch);
      }
      i++;
    }
    values.add(buffer.toString());
    return values.toArray(new String[values.size()]);
  }

  private String decodeValue(String value) throws Exception
  {
    if (value.startsWith("X'") && value.endsWith("'"))
    {
      value = value.substring(2, value.length() - 1);
      byte[] data = new byte[value.length() / 2];
      for (int i = 0; i < data.length; i++)
      {
        String hexabyte = value.substring(i * 2, (i + 1) * 2);
        data[i] = (byte)Integer.parseInt(hexabyte, 16);
      }
      value = new String(data, "UTF-8");
    }
    return value;
  }
}
