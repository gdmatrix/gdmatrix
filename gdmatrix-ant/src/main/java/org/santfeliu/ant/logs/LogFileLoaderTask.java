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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.FileResource;
import org.santfeliu.ant.js.EvalTask;
import org.santfeliu.ant.js.ScriptableTask;
import org.santfeliu.util.enc.Unicode;
import org.santfeliu.util.script.MapScriptable;

public abstract class LogFileLoaderTask extends ScriptableTask
{
  public static final int IGNORED = 0;
  public static final int ACCEPTED = 1;
  public static final int ERROR = 2;
  public static final int WARNING = 3;
  public static final String LOG_SUFFIX = ".load";

  public static final String DATETIME_FIELD = "dateTime";
  public static final String USER_FIELD = "user";
  public static final String ACTION_FIELD = "action";
  public static final String IP_FIELD = "ip";

  //task attributes
  private String fileSequenceName = "LOG_FILE_SEQUENCE";
  private String fileTableName = "LOG_FILE";
  private String accessTableName = "LOG_ACCESS";
  private String propertyTableName = "LOG_PROPERTY";
  private String connVar;
  private String logProperty;
  private String datePattern;
  private String dateTimeZone = "UTC";
  private String unicodeFields;
  private String logLabel;
  private int batchSize = 100;
  private int maxFiles = Integer.MAX_VALUE;
  private int maxWorkTime = Integer.MAX_VALUE;

  //aux variables
  private int fileCount = 0;
  private int errorFileCount = 0;
  private String adaptedDatePattern = null;

  //logging
  private StringBuilder logBuffer = new StringBuilder();
  private PrintStream logStream;

  //nested elements
  private FileSet fileSet;
  private EvalTask includeEval;
  private EvalTask updateEval;
  private List<ExcludePattern> excludePatterns =
    new ArrayList<ExcludePattern>();

  //db variables
  private PreparedStatement fileSequenceStmt = null;
  private PreparedStatement selectFileStmt = null;
  private PreparedStatement insertFileStmt = null;
  private PreparedStatement updateFileStmt = null;
  private PreparedStatement updateFileStatsStmt = null;
  private PreparedStatement deleteFileStmt = null;
  private PreparedStatement insertAccessStmt = null;  
  private PreparedStatement deleteBlockAccessStmt = null;
  private PreparedStatement deleteWasteAccessStmt = null;
  private PreparedStatement insertPropertyStmt = null;

  //db fields length
  private static int DB_FILENAME_MAXSIZE = 1000;
  private static int DB_LABEL_MAXSIZE = 200;
  private static int DB_USER_MAXSIZE = 100;
  private static int DB_ACTION_MAXSIZE = 1000;
  private static int DB_IP_MAXSIZE = 20;
  private static int DB_PROPNAME_MAXSIZE = 100;
  private static int DB_PROPVALUE_MAXSIZE = 4000;

  public String getConnVar()
  {
    return connVar;
  }

  public void setConnVar(String connVar)
  {
    this.connVar = connVar;
  }

  public String getLogProperty()
  {
    return logProperty;
  }

  public void setLogProperty(String logProperty)
  {
    this.logProperty = logProperty;
  }

  public String getLogLabel()
  {
    return logLabel;
  }

  public void setLogLabel(String logLabel)
  {
    this.logLabel = logLabel;
  }

  public String getFileSequenceName()
  {
    return fileSequenceName;
  }

  public void setFileSequenceName(String fileSequenceName)
  {
    this.fileSequenceName = fileSequenceName;
  }

  public String getFileTableName()
  {
    return fileTableName;
  }

  public void setFileTableName(String fileTableName)
  {
    this.fileTableName = fileTableName;
  }

  public String getAccessTableName()
  {
    return accessTableName;
  }

  public void setAccessTableName(String accessTableName)
  {
    this.accessTableName = accessTableName;
  }

  public String getPropertyTableName()
  {
    return propertyTableName;
  }

  public void setPropertyTableName(String propertyTableName)
  {
    this.propertyTableName = propertyTableName;
  }

  public String getDatePattern()
  {
    return datePattern;
  }

  public void setDatePattern(String datePattern)
  {
    this.datePattern = datePattern;
  }

  public String getDateTimeZone()
  {
    return dateTimeZone;
  }

  public void setDateTimeZone(String dateTimeZone)
  {
    this.dateTimeZone = dateTimeZone;
  }

  public String getUnicodeFields()
  {
    return unicodeFields;
  }

  public void setUnicodeFields(String unicodeFields)
  {
    this.unicodeFields = unicodeFields;
  }

  public int getBatchSize()
  {
    return batchSize;
  }

  public void setBatchSize(int batchSize)
  {
    this.batchSize = batchSize;
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

  public EvalTask getIncludeEval()
  {
    return includeEval;
  }

  public EvalTask getUpdateEval()
  {
    return updateEval;
  }

  public void add(FileSet fileSet)
  {
    this.fileSet = fileSet;
  }

  public void addIncludeEval(EvalTask includeEval)
  {
    this.includeEval = includeEval;
  }

  public void addUpdateEval(EvalTask updateEval)
  {
    this.updateEval = updateEval;
  }

  public ExcludePattern createExcludePattern()
  {
    ExcludePattern excludePattern = new ExcludePattern();
    excludePatterns.add(excludePattern);
    return excludePattern;
  }

  @Override
  public void execute()
  {
    validateInput();
    try
    {
      log("Loading log: " + logLabel);
      logLn();
      adaptedDatePattern = datePattern;
      if (datePattern != null && dateTimeZone != null)
      {
        adaptedDatePattern = datePattern + " z";
      }
      long workMs = 0;
      long startMs = System.currentTimeMillis();
      createStatements();
      Iterator iter = fileSet.iterator();
      while (iter.hasNext())
      {
        File file = ((FileResource)iter.next()).getFile();
        if (!isFileLoaded(file) && !isFileOpen(file) && !isLogFile(file) &&
          !fileLimitReached(fileCount, workMs))
        {
          fileCount++;
          loadFile(file);
          long endMs = System.currentTimeMillis();
          workMs = workMs + (endMs - startMs);
          startMs = System.currentTimeMillis();
          logLn();
        }
      }
      log("Loaded files: " + (fileCount - errorFileCount));
      logLn();
      log("Error files: " + errorFileCount);
      logLn();
    }
    catch (Exception ex)
    {
      logError(ex.toString());
    }
    finally
    {
      closeStatements();
    }
    // write property
    if (logProperty != null)
    {
      String currentLog = getProject().getProperty(logProperty);
      String log = (currentLog == null ? "" : currentLog) +
        logBuffer.toString();
      getProject().setProperty(logProperty, log);
    }
  }

  @Override
  public void log(String s)
  {
    log(s, true, true);
  }

  public void logError(String s)
  {
    log(s, false, true);
  }

  public void logLn()
  {
    log("");
  }

  //Protected methods

  protected abstract void initReader(File file) throws Exception;
  protected abstract boolean readEntry(Map<String, String> entryMap)
    throws Exception;
  protected abstract void closeReader();

  protected void loadFile(File file)
  {
    try
    {
      boolean reloadMode = false;
      removeLogFile(file);
      openLogStream(file); // open log file
      long time0 = System.currentTimeMillis();
      log("Start loading file " + file + " at " + new Date(time0));
      int fileId = dbSelectFileId(file);
      if (fileId > 0) //reload existing file
      {
        reloadMode = true;
        dbUpdateFile(fileId);
      }
      else //create new file
      {
        fileId = dbSelectNewFileId();
        dbInsertFile(fileId, file);        
      }
      commit();
      int acceptedCount = 0;
      int ignoredCount = 0;
      int errorCount = 0;
      int warningCount = 0;
      int batchCount = 0;

      Map<String, String> entryMap = new HashMap();
      initReader(file);
      try
      {
        while (readEntry(entryMap))
        {
          int status = processEntry(entryMap);
          switch (status)
          {
            case IGNORED:
              ignoredCount++;
              break;
            case WARNING:
              warningCount++;
            case ACCEPTED:
              acceptedCount++;
              batchCount++;
              dbInsertAccess(fileId, acceptedCount, entryMap);
              dbInsertProperties(fileId, acceptedCount, entryMap);
              if (batchCount == batchSize)
              {
                if (reloadMode)
                {
                  dbDeleteBlockAccess(fileId, acceptedCount - batchCount + 1,
                    acceptedCount);
                }
                insertAccessStmt.executeBatch();
                insertPropertyStmt.executeBatch();
                commit();
                batchCount = 0;
              }
              break;
            case ERROR:
              errorCount++;
              break;
          }
          entryMap.clear();
        }
        if (batchCount > 0)
        {
          if (reloadMode)
          {
            dbDeleteBlockAccess(fileId, acceptedCount - batchCount + 1,
              acceptedCount);
          }
          insertAccessStmt.executeBatch();
          insertPropertyStmt.executeBatch();
          commit();
        }
        if (reloadMode)
        {
          dbDeleteWasteAccess(fileId, acceptedCount);
          commit();
        }
        long time1 = System.currentTimeMillis();
        int secs = (int)((time1 - time0) / 1000);
        dbUpdateFileStats(fileId, acceptedCount, ignoredCount, warningCount,
          errorCount, secs);
        commit();
        log("Inserted entries: " + acceptedCount +
          " (with " + warningCount + " warnings)");
        log("Ignored entries: " + ignoredCount);
        log("Error entries: " + errorCount);
        log("Load completed at " + new Date(time1));
        log("Load time: " + (int)(secs / 60) + "m " + (int)(secs % 60) + "s");
      }
      finally
      {
        log("Load details: <a href=\"file:///" + file.getPath() + LOG_SUFFIX +
          "\">" + file.getPath() + LOG_SUFFIX + "</a>", true, false);
        closeReader(); // only called if initReader was previously called
      }
    }
    catch (Exception ex)
    {
      log("Unrecoverable error: " + ex.toString() + " at " + new Date());
      errorFileCount++;
    }
    finally
    {
      closeLogStream();
    }
  }

  protected int processEntry(Map<String, String> entryMap)
  {
    setVariable("entry", new MapScriptable(getScriptable(), entryMap));
    int status = convertEntry(entryMap);
    if (status == ACCEPTED)
    {
      status = validateEntry(entryMap);
    }
    else if (status == WARNING)
    {
      int status2 = validateEntry(entryMap);
      if (status2 != ACCEPTED) status = status2;
    }
    return status;
  }

  protected int convertEntry(Map<String, String> entryMap)
  {
    try
    {
      //update
      if (updateEval != null) updateEval.execute();
      //date conversion
      if (adaptedDatePattern != null && entryMap.containsKey(DATETIME_FIELD))
      {
        entryMap.put(DATETIME_FIELD,
          getDBDateTime(entryMap.get(DATETIME_FIELD)));
      }
      //unicode conversion
      if (unicodeFields != null)
      {
        Set keySet = entryMap.keySet();
        for (String unicodeField : unicodeFields.split(","))
        {
          if (keySet.contains(unicodeField.trim()))
          {
            String newValue = Unicode.decode(entryMap.get(unicodeField));
            entryMap.put(unicodeField, newValue);
          }
        }
      }
      return ACCEPTED;
    }
    catch (Exception ex)
    {
      logError("Error: " + ex.getMessage() + ". Data: " + entryMap);
      return ERROR;
    }
  }

  protected int validateEntry(Map<String, String> entryMap)
  {
    try
    {
      for (ExcludePattern excludePattern : excludePatterns)
      {
        if (excludePattern.eval(entryMap)) return IGNORED;
      }
      List<String> fieldList = new ArrayList<String>(entryMap.keySet());
      for (String mandatoryField : getMandatoryFieldNames())
      {
        if (!fieldList.contains(mandatoryField))
        {
          throw new Exception("Missing '" + mandatoryField +
            "' mandatory field");
        }
      }
      for (String field : fieldList)
      {
        String value = (String)entryMap.get(field);
        if (value == null || value.trim().isEmpty())
        {
          if (getMandatoryFieldNames().contains(field))
          {
            throw new Exception("Null value in '" + field +
              "' mandatory field");
          }
          else entryMap.remove(field);
        }
      }
      if (includeEval == null || includeEval.eval()) //filtering
      {
        return ACCEPTED;
      }
      return IGNORED;
    }
    catch (Exception ex)
    {
      logError("Error: " + ex.getMessage() + ". Data: " + entryMap);
      return ERROR;
    }
  }

  protected void validateInput()
  {
    if (connVar == null)
      throw new BuildException("Attribute 'connVar' is required");
    if (fileSet == null)
      throw new BuildException("Attribute 'fileSet' is required");
  }

  //Private methods

  private String getDBDateTime(String date) throws Exception
  {
    String adaptedDate = (dateTimeZone == null ? date :
      date + " " + dateTimeZone);
    SimpleDateFormat logFormat = new SimpleDateFormat(adaptedDatePattern);
    SimpleDateFormat dbFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    return dbFormat.format(logFormat.parse(adaptedDate));
  }

  private void openLogStream(File file) throws IOException
  {
    String path = file.getPath();
    File logFile = new File(path + LOG_SUFFIX);
    logStream = new PrintStream(new FileOutputStream(logFile), true);
    logStream.flush();
  }

  private void closeLogStream()
  {
    if (logStream != null)
    {
      logStream.close();
    }
  }

  private void removeLogFile(File file)
  {
    String path = file.getPath();
    File logFile = new File(path + LOG_SUFFIX);
    if (logFile.exists()) logFile.delete();
  }

  private boolean isFileLoaded(File file)
  {
    File fileLog = new File(file.getPath() + LOG_SUFFIX);
    return fileLog.exists();
  }

  private boolean isFileOpen(File file)
  {
    try
    {
      File testFile = new File(file.getPath() + ".test");
      if (file.renameTo(testFile)) //success -> not locked
      {
        testFile.renameTo(file);
        return false;
      }
      return true;
    }
    catch (SecurityException ex)
    {
      return true;
    }
  }

  private boolean isLogFile(File file)
  {
    return file.getName().endsWith(LOG_SUFFIX);
  }
  
  private int dbSelectNewFileId() throws Exception
  {
    int fileId = 0;
    ResultSet rs = fileSequenceStmt.executeQuery();
    try
    {
      if (rs.next()) fileId = rs.getInt(1);
    }
    finally
    {
      rs.close();
    }
    return fileId;
  }

  private int dbSelectFileId(File file) throws Exception
  {
    int fileId = 0;
    selectFileStmt.setString(1, truncate(file.getPath(), DB_FILENAME_MAXSIZE));    
    ResultSet rs = selectFileStmt.executeQuery();
    try
    {
      if (rs.next()) fileId = rs.getInt(1);
    }
    finally
    {
      rs.close();
    }
    return fileId;
  }

  private void dbInsertFile(int fileId, File file) throws Exception
  {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    insertFileStmt.setInt(1, fileId);
    insertFileStmt.setString(2, truncate(file.getPath(), DB_FILENAME_MAXSIZE));
    insertFileStmt.setString(3, truncate(logLabel, DB_LABEL_MAXSIZE));
    insertFileStmt.setString(4, df.format(new Date())); // datetime
    insertFileStmt.executeUpdate();
  }

  private void dbUpdateFile(int fileId) throws Exception
  {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    updateFileStmt.setString(1, truncate(logLabel, DB_LABEL_MAXSIZE));
    updateFileStmt.setString(2, df.format(new Date())); // datetime
    updateFileStmt.setInt(3, fileId);
    updateFileStmt.executeUpdate();
  }

  private void dbUpdateFileStats(int fileId, int acceptedCount,
    int ignoredCount, int warningCount, int errorCount, int secs)
    throws Exception
  {
    updateFileStatsStmt.setInt(1, acceptedCount);
    updateFileStatsStmt.setInt(2, ignoredCount);
    updateFileStatsStmt.setInt(3, warningCount);
    updateFileStatsStmt.setInt(4, errorCount);
    updateFileStatsStmt.setInt(5, secs);
    updateFileStatsStmt.setInt(6, fileId);
    updateFileStatsStmt.executeUpdate();
  }

  private void dbInsertAccess(int fileId, int num, Map<String, String> entryMap)
    throws Exception
  {
    insertAccessStmt.setInt(1, fileId);
    insertAccessStmt.setInt(2, num);
    insertAccessStmt.setString(3, entryMap.get(DATETIME_FIELD));
    insertAccessStmt.setString(4, truncate(entryMap.get(USER_FIELD),
      DB_USER_MAXSIZE));
    insertAccessStmt.setString(5, truncate(entryMap.get(ACTION_FIELD),
      DB_ACTION_MAXSIZE));
    insertAccessStmt.setString(6, truncate(entryMap.get(IP_FIELD),
      DB_IP_MAXSIZE));
    insertAccessStmt.addBatch();
  }

  private void dbDeleteBlockAccess(int fileId, int min, int max)
    throws Exception
  {
    deleteBlockAccessStmt.setInt(1, fileId);
    deleteBlockAccessStmt.setInt(2, min);
    deleteBlockAccessStmt.setInt(3, max);
    deleteBlockAccessStmt.execute();
  }

  private void dbDeleteWasteAccess(int fileId, int num) throws Exception
  {
    deleteWasteAccessStmt.setInt(1, fileId);
    deleteWasteAccessStmt.setInt(2, num);
    deleteWasteAccessStmt.execute();
  }

  private void dbInsertProperties(int fileId, int num,
    Map<String, String> entryMap) throws Exception
  {    
    Set<String> mainFieldNames = getMainFieldNames();
    for (String fieldName : entryMap.keySet())
    {
      if (!mainFieldNames.contains(fieldName))
      {
        insertPropertyStmt.setInt(1, fileId);
        insertPropertyStmt.setInt(2, num);
        insertPropertyStmt.setString(3, truncate(fieldName,
          DB_PROPNAME_MAXSIZE));
        insertPropertyStmt.setString(4, truncate(entryMap.get(fieldName),
          DB_PROPVALUE_MAXSIZE));
        insertPropertyStmt.addBatch();
      }
    }
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

  private void createStatements() throws Exception
  {
    String sql;
    Connection conn = getConnection();

    sql = "select " + fileSequenceName + ".nextval from dual";
    fileSequenceStmt = conn.prepareStatement(sql);

    sql = "select fileid from " + fileTableName + " where filename = ?";
    selectFileStmt = conn.prepareStatement(sql);
    
    sql = "insert into " + fileTableName + 
      "(fileid, filename, label, datetime) values (?, ?, ?, ?)";
    insertFileStmt = conn.prepareStatement(sql);

    sql = "update " + fileTableName +
      " set label = ?,datetime = ? where fileid = ?";
    updateFileStmt = conn.prepareStatement(sql);

    sql = "update " + fileTableName +
      " set accepted = ?,ignored = ?,warnings = ?,errors = ?,duration = ?" +
      " where fileid = ?";
    updateFileStatsStmt = conn.prepareStatement(sql);

    sql = "delete from " + fileTableName + " where filename = ?";
    deleteFileStmt = conn.prepareStatement(sql);
    
    sql = "insert into " + accessTableName +
      "(fileid, num, datetime, userid, act, ip)" +
      " values (?, ?, ?, ?, ?, ?)";
    insertAccessStmt = conn.prepareStatement(sql);

    sql = "delete from " + accessTableName + " where fileid = ? "
      + "and num >= ? and num <= ?";
    deleteBlockAccessStmt = conn.prepareStatement(sql);

    sql = "delete from " + accessTableName + " where fileid = ? and num > ?";
    deleteWasteAccessStmt = conn.prepareStatement(sql);

    sql = "insert into " + propertyTableName +
      "(fileid, num, propname, propvalue) values (?, ?, ?, ?)";
    insertPropertyStmt = conn.prepareStatement(sql);

  }

  private void closeStatements()
  {
    try
    {
      if (fileSequenceStmt != null) fileSequenceStmt.close();
    }
    catch (Exception ex)
    {
    }

    try
    {
      if (selectFileStmt != null) selectFileStmt.close();
    }
    catch (Exception ex)
    {
    }

    try
    {
      if (insertFileStmt != null) insertFileStmt.close();
    }
    catch (Exception ex)
    {
    }

    try
    {
      if (updateFileStmt != null) updateFileStmt.close();
    }
    catch (Exception ex)
    {
    }

    try
    {
      if (updateFileStatsStmt != null) updateFileStatsStmt.close();
    }
    catch (Exception ex)
    {
    }

    try
    {
      if (deleteFileStmt != null) deleteFileStmt.close();
    }
    catch (Exception ex)
    {
    }

    try
    {
      if (insertAccessStmt != null) insertAccessStmt.close();
    }
    catch (Exception ex)
    {
    }
    
    try
    {
      if (deleteBlockAccessStmt != null) deleteBlockAccessStmt.close();
    }
    catch (Exception ex)
    {
    }

    try
    {
      if (deleteWasteAccessStmt != null) deleteWasteAccessStmt.close();
    }
    catch (Exception ex)
    {
    }

    try
    {
      if (insertPropertyStmt != null) insertPropertyStmt.close();
    }
    catch (Exception ex)
    {
    }
  }

  private String truncate(String s, int length)
  {    
    if (s == null || length > s.length()) return s;
    return s.substring(0, length);
  }

  private boolean fileLimitReached(int fileCount, long workMs)
  {
    if (fileCount + 1 > maxFiles) return true;

    long maxMs = ((long)maxWorkTime) * 60 * 1000;
    if (workMs >= maxMs) return true;

    return false;
  }

  private void log(String s, boolean toProperty, boolean toStream)
  {
    super.log(s);
    if (toProperty)
    {
      if (logProperty != null)
      {
        logBuffer.append(s);
        logBuffer.append("<br/>");
      }
    }
    if (toStream)
    {
      if (logStream != null)
      {
        logStream.println(s);
      }
    }
  }

  //static methods

  private static Set<String> getMainFieldNames()
  {
    Set<String> result = new HashSet<String>();
    String[] fieldArray = {DATETIME_FIELD, USER_FIELD, ACTION_FIELD, IP_FIELD};
    result.addAll(Arrays.asList(fieldArray));
    return result;
  }

  private static Set<String> getMandatoryFieldNames()
  {
    Set<String> result = new HashSet<String>();
    String[] fieldArray = {DATETIME_FIELD, USER_FIELD, ACTION_FIELD};
    result.addAll(Arrays.asList(fieldArray));
    return result;
  }

  //Inner classes

  /**
 *
 * @author unknown
 */
public class ExcludePattern
  {
    //attribute
    private String field;
    //inner text
    private String expression;
    //aux pattern
    private Pattern pattern;

    public ExcludePattern()
    {

    }

    public String getField()
    {
      return field;
    }

    public void setField(String field)
    {
      this.field = field;
    }

    public void addText(String text)
    {
      this.expression = text;
    }

    public boolean eval(Map<String, String> entryMap)
    {
      String s = entryMap.get(field);
      if (s == null) return false;
      Matcher m = getPattern().matcher(s);
      return m.matches();
    }

    private Pattern getPattern()
    {
      if (pattern == null)
      {
        pattern = Pattern.compile(expression);
      }
      return pattern;
    }
  }

}
