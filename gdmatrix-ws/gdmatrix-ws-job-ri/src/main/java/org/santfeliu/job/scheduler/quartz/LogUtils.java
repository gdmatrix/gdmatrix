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
package org.santfeliu.job.scheduler.quartz;

import java.io.File;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.matrix.job.LogType;
import org.santfeliu.job.service.LogFormatter;

/**
 *
 * @author blanquepa
 */
public class LogUtils
{
  /**
   * Create new logger (or get if exists) with loggerId name and set verbosity 
   * level. Created without and specific handlder.
   * 
   * @param loggerId
   * @param logVerbosity
   * @return
   * @throws IOException 
   */
  public static Logger createNewLogger(String loggerId, String logVerbosity) 
    throws IOException
  {
    Logger logger = Logger.getLogger(loggerId);          

    if (logVerbosity != null)
    {
      if ("SEVERE".equalsIgnoreCase(logVerbosity))
        logger.setLevel(Level.SEVERE);
      else if ("WARNING".equalsIgnoreCase(logVerbosity))
        logger.setLevel(Level.WARNING);
      else if ("INFO".equalsIgnoreCase(logVerbosity))
        logger.setLevel(Level.INFO);
      else if ("FINE".equalsIgnoreCase(logVerbosity))
        logger.setLevel(Level.FINE);
      else if ("FINER".equalsIgnoreCase(logVerbosity))
       logger.setLevel(Level.FINER);
      else if ("FINEST".equalsIgnoreCase(logVerbosity))
        logger.setLevel(Level.FINEST);    
      else if ("ALL".equalsIgnoreCase(logVerbosity))
        logger.setLevel(Level.ALL);           
    }
    else
      logger.setLevel(Level.INFO);
    
    return logger;
  }
  
  /**
   * Set JobFileHandler to Logger identified with loggerId
   * @param loggerId
   * @param logFile
   * @param logFormat
   * @param logType
   * @throws IOException 
   */
  public static void setHandler(String loggerId, File logFile,
    String logFormat, LogType logType) throws IOException
  {
    Handler handler = getHandler(loggerId);
    if (handler != null)
      removeHandler(loggerId, handler);
    
    boolean appendMode = LogType.CONTINUOUS.equals(logType);
    if (logFile == null)
      logFile = File.createTempFile("Job", ".log");    
    
      Logger logger = Logger.getLogger(loggerId);
    JobFileHandler fh = 
      new JobFileHandler(logFile.getAbsolutePath(), appendMode);
    LogFormatter formatter = new LogFormatter(logFormat);  
    fh.setFormatter(formatter);        
    logger.addHandler(fh);
  }  
    
  public static JobFileHandler getHandler(String loggerId)
  {
    Logger logger = Logger.getLogger(loggerId);    
    if (logger != null)
    {
      int last = logger.getHandlers().length - 1;
      if (last >= 0)
      {
        Handler handler = logger.getHandlers()[last];
        if (handler instanceof JobFileHandler)
          return (JobFileHandler) handler;
      }
    }
    return null;
  }
  
  public static void removeHandler(String loggerId)
  {
    removeHandler(loggerId, getHandler(loggerId));
  }
  
  public static void removeHandler(String loggerId, Handler handler)
  {
    Logger logger = Logger.getLogger(loggerId);      
    if (logger != null)
    {
      if (handler instanceof JobFileHandler)
      {
        try
        {
          handler.close();
        }
        finally
        {
          logger.removeHandler(handler);
          File f = new File(((JobFileHandler)handler).getLogFilePath());
          f.delete();
        }
      }
    }
  }
  
  public static File getLogFile(String loggerId)
  {
    JobFileHandler handler = LogUtils.getHandler(loggerId);
    if (handler != null)
    {
      File logFile = new File(handler.getLogFilePath());
      return logFile;
    }
    return null;
  } 
  
  public static void log(String loggerId, String message, Level level)
  {
    Logger logger = Logger.getLogger(loggerId);
    logger.log(level, message);    
  }
    
}
