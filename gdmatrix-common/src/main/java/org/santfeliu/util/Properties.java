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
package org.santfeliu.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;


/**
 *
 * @author unknown
 */
public class Properties extends TreeMap
  implements Serializable
{
  public Properties()
  {
  }

  public void setProperty(String name, Object value)
  {
    super.put(name, value);
  }

  public Object getProperty(String name)
  {
    return super.get(name);
  }

  public Object removeProperty(String name)
  {
    return super.remove(name);
  }

  public boolean containsProperty(String name)
  {
    return super.containsKey(name);
  }

  public void save(Writer os) throws IOException
  {
    PrintWriter writer = new PrintWriter(os);
    try
    {
      Iterator iter = entrySet().iterator();
      while (iter.hasNext())
      {
        Map.Entry entry = (Map.Entry)iter.next();
        String name = entry.getKey().toString();
        Object value = entry.getValue();
        String svalue;
        if (value != null)
        {
          if (value instanceof String)
          {
            // TODO: find a better way to quote strings
            svalue = value.toString();
            int index = svalue.indexOf('"');
            if (index == -1) svalue = "\"" + svalue + "\"";
            else
            {
              index = svalue.indexOf('\'');
              if (index == -1) svalue = "'" + svalue + "'";
              else svalue = "\"" + svalue.replaceAll("\"", "''") + "\"";
            }
          }
          else svalue = value.toString();
        }
        else
        {
          svalue = "null";
        }
        writer.write(name + " = " + svalue + ";\n");
      }
    }
    finally
    {
      writer.close();
    }
  }

  public void load(Reader reader) throws IOException
  {
    StringBuffer nameBuffer = new StringBuffer();
    StringBuffer valueBuffer = new StringBuffer();
    boolean forceString = false;
    int ch = reader.read();
    int state = 0;
    boolean end = false;
    while (!end)
    {
      switch (state)
      {
        case 0: // look for '='
          if (ch == ' ' || ch == '\t' || ch == '\n' || ch == -1)
          {
            // skip
          }
          else if (ch == '=')
          {
            state = 1;
          }
          else
          {
            nameBuffer.append((char)ch);
          }
          break;

        case 1: // look for ';'
          if (ch == ' ' || ch == '\t' || ch == '\n')
          {
            // skip
          }
          else if (ch == ';' || ch == -1)
          {
            String name = nameBuffer.toString();
            String svalue = valueBuffer.toString();
            if (name.length() > 0 && svalue.length() > 0)
            {
              Object value;
              if (forceString)
              {
                value = svalue;
              }
              else
              {
                if (svalue.equals("null"))
                {
                  value = null;
                }
                else if (svalue.equals("true") || svalue.equals("false"))
                {
                  value = Boolean.valueOf(svalue);
                }
                else
                {
                  try
                  {
                    double number = Double.parseDouble(svalue);
                    value = number;
                  }
                  catch (NumberFormatException ex)
                  {
                    value = svalue;
                  }
                }
              }
              put(name, value);
            }
            nameBuffer.setLength(0);
            valueBuffer.setLength(0);
            forceString = false;
            state = 0;
          }
          else if (ch == '\'')
          {
            state = 2;
            forceString = true;
          }
          else if (ch == '"')
          {
            state = 3;
            forceString = true;
          }
          else
          {
            valueBuffer.append((char)ch);
          }
          break;

        case 2: // look for ending '\''
          if (ch == '\'' || ch == -1)
          {
            state = 1;
          }
          else
          {
            valueBuffer.append((char)ch);
          }
          break;

        case 3: // look for ending '"'
          if (ch == '"' || ch == -1)
          {
            state = 1;
          }
          else
          {
            valueBuffer.append((char)ch);
          }
          break;
      }
      if (ch == -1) end = true;
      else ch = reader.read();
    }
  }

  public boolean loadFromString(String s)
  {
    if (s == null) return false;
    try
    {
      load(new StringReader(s));
      return true;
    }
    catch (Exception ex)
    {
      return false;
    }
  }

  public String saveToString()
  {
    try
    {
      StringWriter writer = new StringWriter();
      save(writer);
      return writer.toString();
    }
    catch (Exception ex)
    {
      return "";
    }
  }

  public static void main(String args[])
  {
    try
    {
      Properties properties = new Properties();
      properties.loadFromString("ao=5i55;b='hola que 9 8';t=\" demo\";c=true; f=null");
      System.out.println(properties.saveToString());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
