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
package org.santfeliu.misc.presence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author realor
 */
public class PresenceDB
{
  private final String dsn;
  private final List<PresenceEntryType> entryTypes;
  private final WorkerProfile workerProfile;
  private static final Logger logger = Logger.getLogger("PresenceDB");
  private static final int MAX_REASON_LENGTH = 100;

  public PresenceDB(String dsn, List<PresenceEntryType> entryTypes,
    WorkerProfile workerProfile)
  {
    this.dsn = dsn;
    this.entryTypes = entryTypes;
    this.workerProfile = workerProfile;
  }

  public void addEntry(PresenceEntry newEntry) throws Exception
  {
    logger.log(Level.INFO, "personId:{0} entryType:{1} dateTime:{2}",
      new Object[]{newEntry.getPersonId(), newEntry.getType(),
      newEntry.getDateTime()});
    Connection conn = getConnection(dsn);
    try
    {
      conn.setAutoCommit(false);
      String personId = newEntry.getPersonId();
      lockEntries(conn, personId);
      PresenceEntry lastEntry = findLastEntry(conn, personId);
      if (lastEntry != null)
      {
        // checks:
        int compare = newEntry.getDateTime().compareTo(lastEntry.getDateTime());
        if (compare <= 0) throw new Exception("DATE_NOT_AFTER_LAST_ENTRY");

        PresenceEntryType entryType = newEntry.getEntryType(entryTypes);
        if (!entryType.isPreviousTypeValid(lastEntry.getType()))
          throw new Exception("PRESENCE_INVALID_STATE");

        if (isEntryDisabled(conn, entryType, newEntry.getDate(), personId))
          throw new Exception("PRESENCE_INVALID_STATE");

        int duration = getDuration(lastEntry, newEntry);
        lastEntry.setDuration(duration);

        // update last entry duration        
        updateEntryDuration(conn, lastEntry);
      }

      // insert new entry
      insertEntry(conn, newEntry);
      applyBonus(conn, newEntry);
      conn.commit();
    }
    catch (Exception ex)
    {
      logger.log(Level.SEVERE, "personId:{0} Error:{1}",
        new Object[]{newEntry.getPersonId(), ex.toString()});
      conn.rollback();
      throw ex;
    }
    finally
    {
      conn.close();
    }
  }

  public void insertEntry(PresenceEntry newEntry) throws Exception
  {
    logger.log(Level.INFO, "personId:{0} entryType:{1} dateTime:{2}",
      new Object[]{newEntry.getPersonId(), newEntry.getType(),
      newEntry.getDateTime()});
    Connection conn = getConnection(dsn);
    try
    {
      conn.setAutoCommit(false);
      String personId = newEntry.getPersonId();
      lockEntries(conn, personId);
      PresenceEntry previousEntry =
        findPreviousEntry(conn, personId, newEntry.getDateTime());
      PresenceEntry nextEntry =
        findNextEntry(conn, personId, newEntry.getDateTime());

      if (previousEntry != null)
      {
        int previousDuration = getDuration(previousEntry, newEntry);
        previousEntry.setDuration(previousDuration);
        updateEntryDuration(conn, previousEntry);
      }

      if (nextEntry != null)
      {
        int newDuration = getDuration(newEntry, nextEntry);
        newEntry.setDuration(newDuration);
      }

      // insert new entry
      insertEntry(conn, newEntry);
      applyBonus(conn, newEntry);
      conn.commit();
    }
    catch (Exception ex)
    {
      logger.log(Level.SEVERE, "personId:{0} Error:{1}",
        new Object[]{newEntry.getPersonId(), ex.toString()});
      conn.rollback();
      throw new Exception("CAN_NOT_SAVE_ENTRY");
    }
    finally
    {
      conn.close();
    }
  }

  public void updateEntry(PresenceEntry entry) throws Exception
  {
    logger.log(Level.INFO, "personId:{0} entryType:{1} dateTime:{2}",
      new Object[]{entry.getPersonId(), entry.getType(), entry.getDateTime()});
    Connection conn = getConnection(dsn);
    try
    {
      conn.setAutoCommit(false);
      updateEntry(conn, entry);
      conn.commit();
    }
    catch (Exception ex)
    {
      logger.log(Level.SEVERE, "personId:{0} Error:{1}",
        new Object[]{entry.getPersonId(), ex.toString()});
      conn.rollback();
      throw ex;
    }
    finally
    {
      conn.close();
    }
  }

  public void removeEntry(PresenceEntry entry) throws Exception
  {
    logger.log(Level.INFO, "personId:{0} entryType:{1} dateTime:{2}",
      new Object[]{entry.getPersonId(), entry.getType(), entry.getDateTime()});
    Connection conn = getConnection(dsn);
    try
    {
      conn.setAutoCommit(false);
      String personId = entry.getPersonId();
      lockEntries(conn, personId);
      PresenceEntry previousEntry =
        findPreviousEntry(conn, personId, entry.getDateTime());
      PresenceEntry nextEntry =
        findNextEntry(conn, personId, entry.getDateTime());

      if (previousEntry != null)
      {
        if (nextEntry == null) // is last entry
        {
          previousEntry.setDuration(0);
        }
        else
        {
          int duration = getDuration(previousEntry, nextEntry);
          previousEntry.setDuration(duration);
        }
        updateEntryDuration(conn, previousEntry);
      }
      removeEntry(conn, entry);
      applyBonus(conn, entry);
      conn.commit();
    }
    catch (Exception ex)
    {
      logger.log(Level.SEVERE, "personId:{0} Error:{1}",
        new Object[]{entry.getPersonId(), ex.toString()});
      conn.rollback();
      throw ex;
    }
    finally
    {
      conn.close();
    }
  }

  public List<PresenceEntry> findEntries(Date currentDate, String personId)
    throws Exception
  {
    logger.log(Level.INFO, "personId:{0}", personId);
    Connection conn = getConnection(dsn);
    try
    {
      return findEntries(conn, currentDate, personId);
    }
    finally
    {
      conn.close();
    }
  }

  public PresenceEntry findLastEntry(String personId) throws Exception
  {
    logger.log(Level.INFO, "enter {0}", personId);
    PresenceEntry entry = null;
    Connection conn = getConnection(dsn);
    try
    {
      entry = findLastEntry(conn, personId);
    }
    finally
    {
      conn.close();
    }
    return entry;
  }

  public int[] getWorkedAndBonusTime(String personId,
    String startDateTime, String endDateTime) throws Exception
  {
    logger.log(Level.INFO, "personId:{0}", personId);
    Connection conn = getConnection(dsn);
    try
    {
      return getWorkedAndBonusTime(conn, personId, startDateTime, endDateTime);
    }
    finally
    {
      conn.close();
    }
  }

  // ************************ private methods ******************************

  private List<PresenceEntry> findEntries(Connection conn, Date currentDate,
    String personId) throws Exception
  {
    List<PresenceEntry> list = new ArrayList<PresenceEntry>();
    PreparedStatement prepStmt = conn.prepareStatement(
      "select * from pcn_presence where personid = ? " +
      "and entrydt between ? and ? order by entrydt");
    try
    {
      String date = TextUtils.formatDate(currentDate, "yyyyMMdd");
      prepStmt.setString(1, personId);
      prepStmt.setString(2, date + "000000");
      prepStmt.setString(3, date + "235959");
      ResultSet rs = prepStmt.executeQuery();
      try
      {
        while (rs.next())
        {
          PresenceEntry entry = readEntry(rs);
          list.add(entry);
        }
      }
      finally
      {
        rs.close();
      }
    }
    finally
    {
      prepStmt.close();
    }
    return list;
  }

  private void insertEntry(Connection conn, PresenceEntry newEntry)
    throws Exception
  {
    PresenceEntryType entryType = newEntry.getEntryType(entryTypes);
    if (entryType == null) throw new Exception("UNKNOWN_ENTRY_TYPE");

    int duration = newEntry.getDuration();
    int workedTime = entryType.getWorkedTime(duration);

    PreparedStatement prepStmt = conn.prepareStatement(
      "insert into pcn_presence (personid, entrydt, entrytype, duration, " +
      "workedTime, bonusTime, manipulated, reason, creationdt, ipaddress)" +
      " values (?,?,?,?,?,0,?,?,?,?)");
    try
    {
      prepStmt.setString(1, newEntry.getPersonId());
      prepStmt.setString(2, newEntry.getDateTime());
      prepStmt.setString(3, newEntry.getType());
      prepStmt.setInt(4, duration);
      prepStmt.setInt(5, workedTime);
      prepStmt.setString(6, newEntry.isManipulated() ? "T" : "F");
      prepStmt.setString(7, normalizeReason(newEntry.getReason()));
      prepStmt.setString(8, newEntry.getCreationDateTime());
      prepStmt.setString(9, newEntry.getIpAddress());
      prepStmt.executeUpdate();
    }
    finally
    {
      prepStmt.close();
    }
  }

  private void updateEntry(Connection conn, PresenceEntry entry)
    throws Exception
  {
    PresenceEntryType entryType = entry.getEntryType(entryTypes);
    if (entryType == null) throw new Exception("UNKNOWN_ENTRY_TYPE");

    String personId = entry.getPersonId();
    String type = entryType.getType();
    String dateTime = entry.getDateTime();
    int duration = entry.getDuration();
    int workedTime = entryType.getWorkedTime(duration);
    int bonusTime = entry.getBonusTime();
    String reason = normalizeReason(entry.getReason());

    logger.log(Level.INFO,
      "personId:{0} dateTime:{1} entryType:{2} duration:{3} " +
      "workedTime:{4} bonusTime:{5} reason:{6}",
      new Object[]{personId, dateTime, type, duration,
        workedTime, bonusTime, reason});

    PreparedStatement prepStmt = conn.prepareStatement(
      "update pcn_presence set entrytype = ?, reason = ?, duration = ?, " + 
      "workedTime = ?, bonusTime = ? where personid = ? and entrydt = ?");
    try
    {
      prepStmt.setString(1, type);
      prepStmt.setString(2, reason);
      prepStmt.setInt(3, duration);
      prepStmt.setInt(4, workedTime);
      prepStmt.setInt(5, bonusTime);
      prepStmt.setString(6, personId);
      prepStmt.setString(7, dateTime);
      prepStmt.executeUpdate();
    }
    finally
    {
      prepStmt.close();
    }
  }

  private void removeEntry(Connection conn, PresenceEntry lastEntry)
    throws Exception
  {
    PreparedStatement prepStmt = conn.prepareStatement(
      "delete pcn_presence where personid = ? and entrydt = ?");
    try
    {
      prepStmt.setString(1, lastEntry.getPersonId());
      prepStmt.setString(2, lastEntry.getDateTime());
      prepStmt.executeUpdate();
    }
    finally
    {
      prepStmt.close();
    }
  }

  private void updateEntryDuration(Connection conn, PresenceEntry entry)
    throws Exception
  {
    PresenceEntryType entryType = entry.getEntryType(entryTypes);
    if (entryType == null) throw new Exception("UNKNOWN_ENTRY_TYPE");

    String personId = entry.getPersonId();
    String dateTime = entry.getDateTime();
    int duration = entry.getDuration();
    int workedTime = entryType.getWorkedTime(duration);

    logger.log(Level.INFO,
      "personId:{0} dateTime:{1} entryType:{2} duration:{3} workedTime:{4}",
      new Object[]{personId, dateTime, entryType.getType(),
        duration, workedTime});

    PreparedStatement prepStmt = conn.prepareStatement(
      "update pcn_presence set duration = ?, workedtime = ? " +
      "where personid = ? and entrydt = ?");
    try
    {
      prepStmt.setInt(1, duration);
      prepStmt.setInt(2, workedTime);
      prepStmt.setString(3, entry.getPersonId());
      prepStmt.setString(4, entry.getDateTime());
      prepStmt.executeUpdate();
    }
    finally
    {
      prepStmt.close();
    }
  }

  private void applyBonus(Connection conn, PresenceEntry entry)
    throws Exception
  {
    if (workerProfile.getBonusTime() == 0) return;

    PresenceEntryType entryType = entry.getEntryType(entryTypes);
    if (entryType == null) return;
    
    if (!entryType.isWork()) return;
    
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(entry.getDate());
    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
    if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) return;

    String date = entry.getDateTime().substring(0, 8);
    String bonusStartDate = workerProfile.getBonusStartDate();
    if (bonusStartDate != null && bonusStartDate.compareTo(date) > 0) return;
    
    String personId = entry.getPersonId();
    PresenceEntry firstWorkEntry = findFirstWorkEntry(conn, personId, date);

    if (firstWorkEntry == null) return;

    if (firstWorkEntry.isBonified()) return;

    resetBonusTime(conn, personId, date);

    firstWorkEntry.setBonusTime(workerProfile.getBonusTime());
    updateEntry(conn, firstWorkEntry);
  }

  private PresenceEntry findLastEntry(Connection conn, String personId)
    throws Exception
  {
    PresenceEntry entry = null;
    PreparedStatement prepStmt = conn.prepareStatement(
      "select * from pcn_presence where personid = ? and entrydt = " +
      "(select max(entrydt) from pcn_presence where personid = ?)");
    try
    {
      prepStmt.setString(1, personId);
      prepStmt.setString(2, personId);
      prepStmt.setMaxRows(1);
      ResultSet rs = prepStmt.executeQuery();
      try
      {
        if (rs.next())
        {
          entry = readEntry(rs);
        }
      }
      finally
      {
        rs.close();
      }
    }
    finally
    {
      prepStmt.close();
    }
    return entry;
  }

  private PresenceEntry findFirstWorkEntry(Connection conn,
    String personId, String date) throws Exception
  {
    logger.log(Level.INFO, "personId:{0} date:{1}",
      new Object[]{personId, date});

    PresenceEntry firstEntry = null;
    StringBuilder typesBuffer = new StringBuilder();
    for (PresenceEntryType entryType : entryTypes)
    {
      if (entryType.isWork())
      {
        typesBuffer.append("'").append(entryType.getType()).append("',");
      }
    }
    typesBuffer.setLength(typesBuffer.length() - 1);

    PreparedStatement prepStmt = conn.prepareStatement(
      "select * from pcn_presence where personid = ? and "  +
      "entrydt like ? and entrytype in (" + typesBuffer + ") " + 
      "order by entrydt asc");
    try
    {
      prepStmt.setString(1, personId);
      prepStmt.setString(2, date + "%");
      prepStmt.setMaxRows(1);
      ResultSet rs = prepStmt.executeQuery();
      try
      {
        if (rs.next())
        {
          firstEntry = readEntry(rs);
        }
      }
      finally
      {
        rs.close();
      }
    }
    finally
    {
      prepStmt.close();
    }
    return firstEntry;
  }

  private PresenceEntry findPreviousEntry(Connection conn, String personId,
    String dateTime) throws Exception
  {
    PresenceEntry entry = null;
    PreparedStatement prepStmt = conn.prepareStatement(
      "select * from pcn_presence where personid = ? and entrydt < ? " +
      "order by entrydt desc");
    try
    {
      prepStmt.setString(1, personId);
      prepStmt.setString(2, dateTime);
      prepStmt.setMaxRows(1);
      ResultSet rs = prepStmt.executeQuery();
      try
      {
        if (rs.next())
        {
          entry = readEntry(rs);
        }
      }
      finally
      {
        rs.close();
      }
    }
    finally
    {
      prepStmt.close();
    }
    return entry;
  }

  private PresenceEntry findNextEntry(Connection conn, String personId,
    String dateTime) throws Exception
  {
    PresenceEntry entry = null;
    PreparedStatement prepStmt = conn.prepareStatement(
      "select * from pcn_presence where personid = ? and entrydt > ? " +
      "order by entrydt asc");
    try
    {
      prepStmt.setString(1, personId);
      prepStmt.setString(2, dateTime);
      prepStmt.setMaxRows(1);
      ResultSet rs = prepStmt.executeQuery();
      try
      {
        if (rs.next())
        {
          entry = readEntry(rs);
        }
      }
      finally
      {
        rs.close();
      }
    }
    finally
    {
      prepStmt.close();
    }
    return entry;
  }

  private boolean isEntryDisabled(Connection conn,
    PresenceEntryType entryType, Date entryDate, String personId)
    throws Exception
  {
    boolean disabled = false;    
    Collection<String> typeNames = entryType.getDisableTypes();
    if (!typeNames.isEmpty())
    {
      StringBuilder typesBuffer = new StringBuilder();
      for (String type : typeNames)
      {
        typesBuffer.append("'").append(type).append("',");
      }
      typesBuffer.setLength(typesBuffer.length() - 1);

      PreparedStatement prepStmt = conn.prepareStatement(
        "select 'disabled' from pcn_presence where personid = ? and "  +
        "entrydt like ? and entrytype in (" + typesBuffer + ")");
      try
      {
        prepStmt.setString(1, personId);    
        String dateString = TextUtils.formatDate(entryDate, "yyyyMMdd");
        prepStmt.setString(2, dateString + "%");
        prepStmt.setMaxRows(1);
        ResultSet rs = prepStmt.executeQuery();
        try
        {
          disabled = rs.next();
        }
        finally
        {
          rs.close();
        }
      }
      finally
      {
        prepStmt.close();
      }
    }
    return disabled;
  }

  private void lockEntries(Connection conn, String personId)
    throws Exception
  {
    int updateCount = 0;
    PreparedStatement prepStmt = conn.prepareStatement(
      "update pcn_user set lockcount = mod(lockcount + 1, 1000000) " +
      "where personid = ?");
    try
    {
      prepStmt.setString(1, personId);
      updateCount = prepStmt.executeUpdate();
    }
    finally
    {
      prepStmt.close();
    }

    if (updateCount == 0)
    {
      prepStmt = conn.prepareStatement(
        "insert into pcn_user (personid, lockcount) values (?, 1)");
      try
      {
        prepStmt.setString(1, personId);
        prepStmt.executeUpdate();
      }
      finally
      {
        prepStmt.close();
      }
    }
  }

  private void resetBonusTime(Connection conn, String personId, String date)
    throws Exception
  {
    logger.log(Level.INFO, "personId:{0} date:{1}",
      new Object[]{personId, date});

    PreparedStatement prepStmt = conn.prepareStatement(
      "update pcn_presence set bonusTime = 0 where personid = ? " +
      "and entrydt like ?");
    try
    {
      prepStmt.setString(1, personId);
      prepStmt.setString(2, date + "%");
      prepStmt.executeUpdate();
    }
    finally
    {
      prepStmt.close();
    }
  }

  private int[] getWorkedAndBonusTime(Connection conn, String personId,
    String startDateTime, String endDateTime) throws Exception
  {
    int times[] = new int[2];
    PreparedStatement prepStmt =
      conn.prepareStatement("select sum(workedtime), sum(bonusTime) " +
      "from pcn_presence where " +
      "personid = ? and ? <= entrydt and entrydt < ? ");
    try
    {
      prepStmt.setString(1, personId);
      prepStmt.setString(2, startDateTime);
      prepStmt.setString(3, endDateTime);

      ResultSet rs = prepStmt.executeQuery();
      try
      {
        if (rs.next())
        {
          times[0] = rs.getInt(1);
          times[1] = rs.getInt(2);
        }
      }
      finally
      {
        rs.close();
      }
    }
    finally
    {
      prepStmt.close();
    }
    return times;
  }

  private PresenceEntry readEntry(ResultSet rs) throws Exception
  {
    PresenceEntry entry = new PresenceEntry();
    entry.setPersonId(rs.getString("personid"));
    entry.setDateTime(rs.getString("entrydt"));
    entry.setType(rs.getString("entrytype"));
    entry.setDuration(rs.getInt("duration"));
    entry.setWorkedTime(rs.getInt("workedtime"));
    entry.setBonusTime(rs.getInt("bonustime"));
    entry.setManipulated("T".equals(rs.getString("manipulated")));
    entry.setReason(rs.getString("reason"));
    entry.setIpAddress(rs.getString("ipaddress"));
    entry.setCreationDateTime(rs.getString("creationdt"));
    return entry;
  }

  private int getDuration(PresenceEntry firstEntry, PresenceEntry nextEntry)
  {
    return (int)
     ((nextEntry.getDate().getTime() - firstEntry.getDate().getTime()) / 1000L);
  }

  private String normalizeReason(String reason)
  {
    if (reason != null)
    {
      reason = TextUtils.replaceSpecialChars(reason);
      reason = TextUtils.replaceTabAndCRWithBlank(reason);
      if (reason.length() > MAX_REASON_LENGTH)
        reason = reason.substring(0, MAX_REASON_LENGTH);
    }
    return reason;
  }

  private Connection getConnection(String dsn) throws Exception
  {
    javax.naming.Context initContext = new InitialContext();
    javax.naming.Context envContext =
      (javax.naming.Context)initContext.lookup("java:/comp/env");
    DataSource dataSource = (DataSource)envContext.lookup(dsn);
    Connection conn = dataSource.getConnection();
    conn.setAutoCommit(true);
    return conn;
  }
}
