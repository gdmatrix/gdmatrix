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
package org.santfeliu.webapp.modules.assistant.openai;

import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class ThreadStore
{
  String dataSourceName = "jdbc/matrix";
  String userId = "anonymous";

  public String getUserId()
  {
    return userId;
  }

  public void setUserId(String userId)
  {
    this.userId = userId;
  }

  public Thread loadThread(String threadId) throws Exception
  {
    Gson gson = new Gson();

    List<String[]> rows = executeSql(
      "SELECT DATA FROM OAI_THREAD WHERE THREADID = ?", threadId);
    if (!rows.isEmpty())
    {
      String[] row = rows.get(0);
      return gson.fromJson(row[0], Thread.class);
    }
    return null;
  }

  public void storeThread(Thread thread) throws Exception
  {
    Gson gson = new Gson();

    executeSql(
      "INSERT INTO OAI_THREAD (THREADID, USERID, DATA) VALUES (?, ?, ?)",
      thread.id, userId, gson.toJson(thread));
  }

  public void removeThread(String threadId) throws Exception
  {
    executeSql("DELETE FROM OAI_THREAD WHERE THREADID = ?", threadId);
  }

  public List<Thread> findThreads() throws Exception
  {
    Gson gson = new Gson();

    List<String[]> rows = executeSql(
      "SELECT DATA FROM OAI_THREAD WHERE USERID = ?", userId);

    List<Thread> threads = new ArrayList<>();
    for (String[] row : rows)
    {
      Thread thread = gson.fromJson(row[0], Thread.class);
      threads.add(thread);
    }

    Collections.sort(threads,
      (a, b) -> (int)(b.getCreatedAt() - a.getCreatedAt()));

    return threads;
  }

  private List<String[]> executeSql(String sql, String... parameters)
    throws Exception
  {
    System.out.println("executeSql: " + sql + " " + Arrays.toString(parameters));
    Context initContext = new InitialContext();
    Context envContext = (Context)initContext.lookup("java:/comp/env");
    DataSource ds = (DataSource)envContext.lookup(dataSourceName);
    try (Connection conn = ds.getConnection())
    {
      conn.setAutoCommit(true);
      try (PreparedStatement statement = conn.prepareStatement(sql))
      {
        int index = 1;
        for (String parameter : parameters)
        {
          statement.setString(index, parameter);
          index++;
        }
        if (statement.execute())
        {
          // is query
          List<String[]> rows = new ArrayList<>();
          try (ResultSet rs = statement.getResultSet())
          {
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next())
            {
              String[] row = new String[columnCount];
              for (int i = 0; i < columnCount; i++)
              {
                row[i] = rs.getString(i + 1);
                rows.add(row);
              }
            }
          }
          return rows;
        }
        else
        {
          return null;
        }
      }
    }
  }
}
