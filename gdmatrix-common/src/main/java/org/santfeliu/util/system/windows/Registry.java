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
package org.santfeliu.util.system.windows;

import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author unknown
 */
public class Registry
{
  private static final String REGQUERY_UTIL = "reg query ";

  public Registry()
  {
  }

  public static List<String[]> getKeyValues(String key) throws Exception
  {
    Process process = Runtime.getRuntime().exec(
      REGQUERY_UTIL + "\"" + key + "\"");
    StreamReader reader = new StreamReader(process.getInputStream());

    reader.start();
    process.waitFor();
    reader.join();

    return reader.getResult();
  }

  public static Object getKeyAttribute(String key, String attribute)
    throws Exception
  {
    List<String[]> result = getKeyValues(key);
    boolean found = false;
    int i = 0;
    String[] line = null;
    while (!found && i < result.size())
    {
      line = result.get(i);
      if (line.length >= 4)
      {
        String property = line[1];
        if (attribute.length() == 0 && 
          property.startsWith("<") && 
          property.endsWith(">")) // default value
        {
          found = true;
        }
        else if (attribute.equals(property))
        {
          found = true;
        }
      }
      i++;
    }
    if (found)
    {
      String type = line[2];
      String value = line[3];
      if (type.equals("REG_SZ")) return value;
      if (type.equals("REG_EXPAND_SZ"))
      {
        return expandString(value);
      }
      // else convert
    }
    return null;
  }

  public static void printKeyValues(String key) throws Exception
  {
    List<String[]> list = getKeyValues(key);
    for (String[] line : list)
    {
      for (String elem : line)
      {
        System.out.println(elem);
      }
      System.out.println("----");
    }
  }

  private static String expandString(String value)
  {
    StringBuffer buffer = new StringBuffer();
    StringBuffer envBuffer = new StringBuffer();
    int state = 0;
    for (int i = 0; i < value.length(); i++)
    {
      char ch = value.charAt(i);
      switch (state)
      {
        case 0:
          if (ch == '%')
          {
            envBuffer.setLength(0);
            state = 1;
          }
          else buffer.append(ch);
          break;

        case 1:
          if (ch == '%')
          {
            String env = envBuffer.toString();
            buffer.append(System.getenv(env));
            state = 0;
          }
          else envBuffer.append(ch);
          break;
      }
    }
    return buffer.toString();
  }

  static class StreamReader extends Thread
  {
    private InputStream is;
    private List<String[]> result = new ArrayList<String[]>();
    
    StreamReader(InputStream is)
    {
      this.is = is;
    }

    public void run()
    {
      try
      {
        StringBuffer buffer = new StringBuffer();
        int ch;
        String key = "";
        while ((ch = is.read()) != -1)
        {
          if (ch == '\r');
          else if (ch == '\n')
          {
            if (buffer.length() > 0)
            {
              String line = buffer.toString();
              if (line.startsWith("!")) // header
              {
              }
              else if (!line.startsWith(" ")) // node
              {
                key = line;
                result.add(new String[]{key});
              }
              else // attribute
              {
                String t = key + "\t" + line.trim();
                result.add(t.split("\t"));
              }
            }
            buffer.setLength(0);
          }
          else
          {
            buffer.append((char)ch);
          }
        }
      }
      catch (IOException e)
      {
      }
    }

    public List<String[]> getResult()
    {
      return result;
    }
  }
}
