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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 * @author blanquepa
 */
  public class LogFormatter extends Formatter
  {
    private String format =
      "%1$td-%1$tb-%1$tY %1$tk:%1$tM:%1$tS.%1$tL %4$s %2$s: %5$s%6$s%n";
    private final Date dat = new Date();
    
    public LogFormatter()
    {
      this(null);
    }
    
    public LogFormatter(String format)
    {
      if (format != null)
        this.format = format;
    }
    
    @Override
    public String format(LogRecord record)
    {
      dat.setTime(record.getMillis());
      String source;
      if (record.getSourceClassName() != null) 
      {
        source = record.getSourceClassName();
        if (record.getSourceMethodName() != null) 
        {
          source += " " + record.getSourceMethodName();
        }
      } 
      else 
      {
        source = record.getLoggerName();
      }
      String message = formatMessage(record);
      String throwable = "";
      if (record.getThrown() != null) 
      {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println();
        record.getThrown().printStackTrace(pw);
        pw.close();
        throwable = sw.toString();
      }
      return String.format(format, dat, source, record.getLoggerName(),
        record.getLevel(), message, throwable);
    }
      
  }
