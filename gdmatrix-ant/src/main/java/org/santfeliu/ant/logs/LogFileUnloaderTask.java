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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.santfeliu.ant.js.ScriptableTask;

/**
 *
 * @author realor
 */
public class LogFileUnloaderTask extends ScriptableTask
{
  //task attributes
  private String connVar;
  private String logLabel;
  private String logProperty;
  private Integer preserveDays; // days
  private int maxFiles = Integer.MAX_VALUE;
  private int maxWorkTime = Integer.MAX_VALUE; //minutes

  //aux variables
  private String fileTableName = "LOG_FILE";
  private String accessTableName = "LOG_ACCESS";
  private int blockSize = 100;
  private PreparedStatement deleteFileStmt = null;
  private PreparedStatement deleteBlockAccessStmt = null;

  //logging
  private StringBuilder logBuffer = new StringBuilder();

  public String getConnVar()
  {
    return connVar;
  }

  public void setConnVar(String connVar)
  {
    this.connVar = connVar;
  }

  public String getLogLabel()
  {
    return logLabel;
  }

  public void setLogLabel(String logLabel)
  {
    this.logLabel = logLabel;
  }

  public String getLogProperty()
  {
    return logProperty;
  }

  public void setLogProperty(String logProperty)
  {
    this.logProperty = logProperty;
  }

  public Integer getPreserveDays()
  {
    return preserveDays;
  }

  public void setPreserveDays(Integer preserveDays)
  {
    this.preserveDays = preserveDays;
  }

  public int getMaxFiles()
  {
    return maxFiles;
  }

  public void setMaxFiles(int maxFiles)
  {
    this.maxFiles = maxFiles;
  }

  public int getMaxWorkTime()
  {
    return maxWorkTime;
  }

  public void setMaxWorkTime(int maxWorkTime)
  {
    this.maxWorkTime = maxWorkTime;
  }

  public String getFileTableName()
  {
    return fileTableName;
  }

  public void setFileTableName(String fileTableName)
  {
    this.fileTableName = fileTableName;
  }

  @Override
  public void execute()
  {
    validateInput();    
    try
    {
      log("Removing log: " + logLabel);
      logLn();
      int fileCount = 1;
      long workMs = 0;
      createStatements();
      Map<Integer, String> expiredFiles = getExpiredFiles();
      for (int fileId : expiredFiles.keySet())
      {
        if (!fileLimitReached(fileCount, workMs))
        {
          fileCount++;
          long time0 = System.currentTimeMillis();
          log("Removing file " + expiredFiles.get(fileId) + " at " + new Date(time0));
          int index = 0;
          while (dbDeleteBlockAccess(fileId, index + 1, index + blockSize))
          {
            commit();
            index = index + blockSize;
          }
          dbDeleteFile(fileId);
          commit();
          long time1 = System.currentTimeMillis();
          log("Remove operation completed at " + new Date(time1));
          int secs = (int)((time1 - time0) / 1000);
          log("Remove time: " + (int)(secs / 60) + "m " + (int)(secs % 60) + "s");
          workMs = workMs + (time1 - time0);
          logLn();
        }
      }
      log("Removed files: " + (fileCount - 1));
      logLn();
    }
    catch (Exception ex)
    {
      log(ex.toString());
    }
    finally
    {
      closeStatements();
    }
    // write property
    if (logProperty != null)
    {
      getProject().setProperty(logProperty, logBuffer.toString());
    }
  }
  
  @Override
  public void log(String s)
  {
    super.log(s);
    if (logProperty != null)
    {
      logBuffer.append(s);
      logBuffer.append("<br/>");
    }
  }

  public void logLn()
  {
    log("");
  }

  private void validateInput() throws BuildException
  {
    if (connVar == null)
      throw new BuildException("Attribute 'connVar' is required");
    if (logLabel == null)
      throw new BuildException("Attribute 'logLabel' is required");
    if (preserveDays == null)
      throw new BuildException("Attribute 'preserveDays' is required");
    else if (preserveDays < 0)
      throw new BuildException("Invalid value in attribute 'preserveDays'");
  }

  private void commit() throws Exception
  {
    Connection conn = (Connection)getVariable(connVar);
    conn.commit();
  }

  private Connection getConnection()
  {
    return (Connection)getVariable(connVar);
  }

  private boolean dbDeleteBlockAccess(int fileId, int min, int max)
    throws Exception
  {    
    deleteBlockAccessStmt.setInt(1, fileId);
    deleteBlockAccessStmt.setInt(2, min);
    deleteBlockAccessStmt.setInt(3, max);
    return (deleteBlockAccessStmt.executeUpdate() > 0);
  }

  private void dbDeleteFile(int fileId) throws Exception
  {      
    deleteFileStmt.setInt(1, fileId);
    deleteFileStmt.executeUpdate();
  }
  
  private Map<Integer, String> getExpiredFiles() throws Exception
  {
    Map<Integer, String> result = new HashMap<Integer, String>();
    PreparedStatement stmt = null;
    try
    {
      String sql = "select fileid,filename from " + fileTableName + 
        " where label = ?"
        + " and to_date(datetime, 'YYYYMMDDHH24MISS') + ? < sysdate";
      stmt = getConnection().prepareStatement(sql);
      stmt.setString(1, logLabel);
      stmt.setInt(2, preserveDays);
      ResultSet rs = null;
      try
      {
        rs = stmt.executeQuery();
        while (rs.next())
        {
          result.put(rs.getInt(1), rs.getString(2));
        }
      }
      finally
      {
        if (rs != null) rs.close();
      }
    }
    finally
    {
      if (stmt != null) stmt.close();
    }
    return result;
  }
  
  private void createStatements() throws Exception
  {
    Connection conn = getConnection();
    String sql = "delete from " + accessTableName + " where fileid = ?"
      + " and num >= ? and num <= ?";
    deleteBlockAccessStmt = conn.prepareStatement(sql);
    sql = "delete from " + fileTableName + " where fileid = ?";
    deleteFileStmt = conn.prepareStatement(sql);
  }

  private void closeStatements()
  {
    try
    {
      if (deleteBlockAccessStmt != null) deleteBlockAccessStmt.close();
    }
    catch (Exception ex) { }
    try
    {
      if (deleteFileStmt != null) deleteFileStmt.close();
    }
    catch (Exception ex) { }
  }

  private boolean fileLimitReached(int fileCount, long workMs)
  {
    if (fileCount > maxFiles) return true;

    long maxMs = ((long)maxWorkTime) * 60 * 1000;
    if (workMs >= maxMs) return true;

    return false;
  }

}
