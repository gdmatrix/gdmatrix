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
package org.santfeliu.util.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Properties;
import org.santfeliu.util.enc.Unicode;

/**
 *
 * @author realor
 */
public class CSVLogger
{
  private static final HashMap<String, CSVLogger> loggers =
    new HashMap<String, CSVLogger>();

  private PrintWriter writer;
  private String logDate;
  
  // setup parameters
  private File logDir;
  private String prefix = "LOG";
  private String suffix = ".csv";
  private String dateFormat = "yyyyMMdd";
  private String separator = ",";
  private String quote = "\\u002c";
  private boolean trimValues = true;

  public CSVLogger()
  {
  }

  public CSVLogger(File setupFile)
  {
    try
    {
      Properties properties = new Properties();
      properties.load(new FileInputStream(setupFile));
      logDir = new File(properties.getProperty("logDir"), "/");
      prefix = properties.getProperty("prefix", "LOG");
      suffix = properties.getProperty("suffix", ".csv");
      dateFormat = properties.getProperty("dateFormat", "yyyyMMdd");
      separator = properties.getProperty("separator", ",");
      quote = properties.getProperty("quote", "\\u002c");
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  public synchronized static CSVLogger getInstance(String setupFileName)
  {
    CSVLogger logger = loggers.get(setupFileName);
    if (logger == null)
    {
      File setupFile = new File(setupFileName);
      if (setupFile.exists())
      {
        logger = new CSVLogger(setupFile);
        loggers.put(setupFileName, logger);
      }
    }
    return logger;
  }

  public synchronized static void closeAll()
  {
    for (CSVLogger logger : loggers.values())
    {
      logger.close();
    }
  }

  public File getLogDir()
  {
    return logDir;
  }

  public void setLogDir(File logDir)
  {
    this.logDir = logDir;
  }

  public String getPrefix()
  {
    return prefix;
  }

  public void setPrefix(String prefix)
  {
    this.prefix = prefix;
  }

  public String getSuffix()
  {
    return suffix;
  }

  public void setSuffix(String suffix)
  {
    this.suffix = suffix;
  }

  public String getDateFormat()
  {
    return dateFormat;
  }

  public void setDateFormat(String dateFormat)
  {
    this.dateFormat = dateFormat;
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

  public synchronized void log(String ... values)
  {
    try
    {
      java.util.Date date = new java.util.Date();
      SimpleDateFormat df = new SimpleDateFormat(dateFormat);
      String dt = df.format(date);
      // if day change detected, create new log file
      if (!dt.equals(logDate))
      {
        logDate = dt;
        if (writer != null)
        {
          writer.flush();
          writer.close();
        }
        logDir.mkdirs();
        File logFile = new File(logDir, prefix + logDate + suffix);
        writer = new PrintWriter(new FileOutputStream(logFile, true), true);
      }
      // write line to file
      if (values.length > 0)
      {
        writer.print(quoteValue(values[0]));
        for (int i = 1; i < values.length; i++)
        {
          writer.print(separator);
          writer.print(quoteValue(values[i]));
        }
        writer.println(); // CR, flush buffer
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public synchronized void flush()
  {
    if (writer != null)
    {
      writer.flush();
    }
  }

  public synchronized void close()
  {
    if (writer != null)
    {
      writer.close();
    }
  }

  protected String quoteValue(String value)
  {
    if (value == null) value = "";
    value = Unicode.encode(value);
    value = value.replaceAll(separator, quote);
    if (trimValues)
    {
      value = value.trim();
    }
    return value;
  }

  public static void main(String args[])
  {
    CSVLogger logger = CSVLogger.getInstance("c:/log_svc.properties");
    for (int i = 0; i < 10; i++)
    {
      logger.log("hhhh", "KK;KK", "OO\nOOO", "342345", "99343");
    }
    logger.flush();
  }
}
