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
package org.santfeliu.ant.logs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import org.apache.tools.ant.BuildException;

/**
 *
 * @author unknown
 */
public class CSVLogLoaderTask extends LogFileLoaderTask
{
  //task attributes
  private String separator = ";";
  private String quote = "\"";
  private String columnNames;
  private String columnIndexes;
  private String firstRow = "1";
  private String charset = "UTF-8";

  //reader
  private BufferedReader reader = null;

  //aux variables
  private int rowNum = 0;

  public String getSeparator()
  {
    return separator;
  }

  public void setSeparator(String separator)
  {
    this.separator = separator;
  }

  public String getCharset()
  {
    return charset;
  }

  public void setCharset(String charset)
  {
    this.charset = charset;
  }

  public String getQuote()
  {
    return quote;
  }

  public void setQuote(String quote)
  {
    this.quote = quote;
  }

  public String getColumnNames()
  {
    return columnNames;
  }

  public void setColumnNames(String columnNames)
  {
    this.columnNames = columnNames;
  }

  public String getColumnIndexes()
  {
    return columnIndexes;
  }

  public void setColumnIndexes(String columnIndexes)
  {
    this.columnIndexes = columnIndexes;
  }

  public String getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(String firstRow)
  {
    this.firstRow = firstRow;
  }

  protected void initReader(File file) throws Exception
  {
    reader = new BufferedReader(
      new InputStreamReader(new FileInputStream(file), charset));
    rowNum = 0;
  }

  protected boolean readEntry(Map<String, String> entryMap) throws Exception
  {
    String sline = reader.readLine();
    while (sline != null && ++rowNum < Integer.valueOf(firstRow))
    {
      sline = reader.readLine(); //ignore first rows
    }
    if (sline == null) return false;
    int i = 0;
    ArrayList<String> itemList = parseLine(sline, separator.charAt(0), quote.charAt(0));
    for (String columnName : columnNames.split(","))
    {
      int columnIndex = (columnIndexes == null ? i :
        Integer.valueOf(columnIndexes.split(",")[i]) - 1);
      if (columnIndex < itemList.size())
      {
        entryMap.put(columnName, itemList.get(columnIndex));
      }
      i++;
    }
    return true;
  }

  protected void closeReader()
  {
    try
    {
      if (reader != null) reader.close();
    }
    catch (Exception ex) { }
  }

  @Override
  protected void validateInput()
  {
    super.validateInput();
    if (columnNames == null)
      throw new BuildException("Attribute 'columnNames' is required");
    if (columnIndexes != null)
    {
      String[] columnIndexesArray = columnIndexes.split(",");
      int columnCount = columnNames.split(",").length;
      if (columnIndexesArray.length != columnCount)
      {
        throw new BuildException("Invalid number of column indexes");
      }
      else
      {
        for (String columnIndex : columnIndexesArray)
        {
          try
          {
            if (Integer.valueOf(columnIndex) < 1)
            {
              throw new BuildException("Invalid column index");
            }
          }
          catch (NumberFormatException ex)
          {
            throw new BuildException("Invalid column index");
          }
        }
      }
    }
  }

  protected ArrayList parseLine(String line, char separator, char quote)
  {
    ArrayList<String> lines = new ArrayList<String>();
    StringBuffer buffer = new StringBuffer();

    int state = 0;
    int index = 0;
    while (index < line.length())
    {
      char ch = line.charAt(index);
      switch (state)
      {
        case 0: // skip delimiters, new argument
        {
          if (ch == quote)
          {
            state = 2;
            buffer.setLength(0);
          }
          else if (ch == separator)
          {
            lines.add(null);
          }
          else if (ch != quote && ch != separator)
          {
            state = 1;
            buffer.setLength(0);
            buffer.append(ch);
          }
        }; break;

        case 1:
        {
          if (ch == separator)
          {
            state = 0;
            lines.add(buffer.toString());
            buffer.setLength(0);
          }
          else
          {
            buffer.append(ch);
          }
        }; break;

        case 2:
        {
          if (ch == quote)
          {
            state = 3;
          }
          else
          {
            buffer.append(ch);
          }
        }; break;

        case 3:
        {
          if (ch == separator)
          {
            state = 0;
            lines.add(buffer.toString());
          }
          else
          {
            state = 2;
            if (ch != quote) buffer.append(quote);
            buffer.append(ch);
          }
        }; break;
      }
      index++;
    }
    if (buffer.length() > 0) lines.add(buffer.toString());
    return lines;
  }
}
