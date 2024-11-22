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
package org.santfeliu.util.sequence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 *
 * @author blanquepa
 */
public class SQLSequenceStore implements SequenceStore
{
  //JNDI connection
  private String dataSource;
  //DriverManager connection
  private String url;
  private String driver;
  private String user;
  private String password;

  public SQLSequenceStore(String dataSource)
  {
    this.dataSource = dataSource;
  }

  public SQLSequenceStore(String driver, String url, String user, String password)
  {
    this.url = url;
    this.driver = driver;
    this.user = user;
    this.password = password;
  }

  public Sequence loadSequence(String name)
    throws Exception
  {
    return loadSequence(name, true);
  }

  public Sequence loadSequence(String name, boolean autoincrement)
    throws Exception
  {
    Sequence sequence = new Sequence(name);
    Connection conn = getConnection();
    try
    {
      conn.setAutoCommit(false);
      int value = autoincrement ? 0 : 1;
      if (autoincrement)
        value = increment(conn, name);
      if (value > 0)
      {
        long result = read(conn, name);
        sequence.setValue(String.valueOf(result));
      }

      conn.commit();
    }
    catch(Exception ex)
    {
      conn.rollback();
    }
    finally
    {
      conn.close();
    }
    return sequence;
  }

  public Sequence changeSequence(String name, String value)
    throws Exception
  {
    Sequence sequence = new Sequence(name, value);
    Connection conn = getConnection();
    try
    {
      conn.setAutoCommit(false);
      if (sequence != null)
        change(conn, sequence);
      conn.commit();
    }
    catch(Exception ex)
    {
      conn.rollback();
    }
    finally
    {
      conn.close();
    }
    return sequence;
  }

  public Sequence createSequence(String name, String initValue) throws Exception
  {
    Sequence sequence = new Sequence(name, initValue);
    Connection conn = getConnection();
    try
    {
      conn.setAutoCommit(false);
      if (sequence != null)
        create(conn, sequence);
      conn.commit();
    }
    catch(Exception ex)
    {
      conn.rollback();
    }
    finally
    {
      conn.close();
    }
    return sequence;
  }

  private Connection getConnection() throws Exception
  {
    if (dataSource != null)
      return getConnection(dataSource);
    else
      return getConnection(driver, url, user, password);
  }

  private Connection getConnection(String dataSource) throws Exception
  {
    javax.naming.Context initContext = new InitialContext();
    javax.naming.Context envContext  =
       (javax.naming.Context)initContext.lookup("java:/comp/env");
    DataSource ds = (DataSource)envContext.lookup(dataSource);
    return ds.getConnection();
  }

  private Connection getConnection(String driver, String url, String user,
          String password) throws Exception
  {
    Connection result = null;
    Class.forName(driver).getConstructor().newInstance();
    result = DriverManager.getConnection(url, user, password);
    return result;
  }

  private long read(Connection conn, String counterName) throws SQLException
  {
    if (counterName != null)
    {
      String sql = "SELECT value from TABLESEQ where counter = ?";
      PreparedStatement stmt = conn.prepareStatement(sql);
      try
      {
        stmt.setString(1, counterName);
        ResultSet rs = stmt.executeQuery();
        if (rs != null)
        {
          rs.next();
          return rs.getLong("value");
        }
      }
      finally
      {
        stmt.close();
      }
    }
    return 0;
  }


  private int increment(Connection conn, String counterName) throws SQLException
  {
    if (counterName != null)
    {
      String sql = "UPDATE TABLESEQ set value = value + 1 WHERE counter = ?";
      PreparedStatement stmt = conn.prepareStatement(sql);
      try
      {
        stmt.setString(1, counterName);
        stmt.execute();
        return stmt.getUpdateCount();
      }
      finally
      {
        stmt.close();
      }
    }
    return 0;
  }

  private int change(Connection conn, Sequence sequence) throws SQLException
  {
    if (sequence != null)
    {
      String sql = "UPDATE TABLESEQ set value = ? WHERE counter = ?";
      PreparedStatement stmt = conn.prepareStatement(sql);
      try
      {
        stmt.setString(1, sequence.getValue());
        stmt.setString(2, sequence.getCounter());
        stmt.execute();
        return stmt.getUpdateCount();
      }
      finally
      {
        stmt.close();
      }
    }
    return 0;
  }

  private int create(Connection conn, Sequence sequence) throws SQLException
  {
    if (sequence != null)
    {
      String sql = "INSERT INTO TABLESEQ (counter, value) VALUES (?, ?)";
      PreparedStatement stmt = conn.prepareStatement(sql);
      try
      {
        stmt.setString(1, sequence.getCounter());
        stmt.setString(2, sequence.getValue());
        stmt.execute();
        return stmt.getUpdateCount();
      }
      finally
      {
        stmt.close();
      }
    }
    return 0;
  }


}
