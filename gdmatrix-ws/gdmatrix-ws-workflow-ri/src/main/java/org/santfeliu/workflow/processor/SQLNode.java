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
package org.santfeliu.workflow.processor;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.santfeliu.dbf.DBConnection;
import org.santfeliu.dbf.DBRepository;
import org.santfeliu.util.LineTokenizer;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.Table;
import org.santfeliu.util.template.Template;
import org.santfeliu.workflow.WorkflowActor;
import org.santfeliu.workflow.WorkflowInstance;


/**
 *
 * @author realor
 */
public class SQLNode extends org.santfeliu.workflow.node.SQLNode
  implements NodeProcessor
{
  private static final DBRepository repository = new DBRepository();
  private static final Logger LOGGER = Logger.getLogger("SQLNode");

  @Override
  public String process(WorkflowInstance instance, WorkflowActor actor)
    throws Exception
  {
    String mdsn;
    if (dsn != null && dsn.trim().length() > 0)
    {
      mdsn = Template.create(dsn).merge(instance);
    }
    else
    {
      mdsn =
        MatrixConfig.getProperty("org.santfeliu.workflow.node.SQLNode.dsn");
    }
    String mstatements = Template.create(statements).merge(instance);
    DBConnection dbConn = repository.getConnection(mdsn);
    try
    {
      LineTokenizer tokenizer = new LineTokenizer(mstatements, ";", true);
      while (tokenizer.hasMoreTokens())
      {
        String statement = tokenizer.nextToken().trim();
        try
        {
          if (statement.toLowerCase().startsWith("select"))
          {
            doSelect(dbConn, instance, statement);
          }
          else
          {
            doUpdate(dbConn, instance, statement);
          }
        }
        catch (Exception ex)
        {
          LOGGER.log(Level.SEVERE, "Query failed: [{0}] with {1}, error: {2}",
            new Object[]{statement, instance, ex.toString()});
          dbConn.rollback();
          throw ex;
        }
      }
      dbConn.commit();
    }
    finally
    {
      dbConn.close();
    }
    return CONTINUE_OUTCOME;
  }

  private void doSelect(DBConnection dbConn,
    WorkflowInstance instance, String statement) throws Exception
  {
    dbConn.setMaxRows(maxRows);
    Table result = dbConn.executeQuery(statement, instance);
    if (maxRows == 1 && result.size() > 0)
    {
      int columnCount = result.getColumnCount();
      for (int col = 0; col < columnCount; col++)
      {
        String columnName = result.getColumnName(col);
        Object value = result.getElementAt(0, col);
        instance.put(columnName, value);
      }
    }
    else
    {
      for (int row = 0; row < result.getRowCount(); row++)
      {
        int columnCount = result.getColumnCount();
        for (int col = 0; col < columnCount; col++)
        {
          String columnName = result.getColumnName(col);
          Object value = result.getElementAt(row, col);
          instance.put(columnName + "_" + row, value);
        }
      }
    }
    LOGGER.log(Level.INFO, "Query executed: [{0}] with {1}, rows selected: {2}",
      new Object[]{statement, instance, result.getRowCount()});
  }

  private void doUpdate(DBConnection dbConn,
    WorkflowInstance instance, String statement) throws Exception
  {
    dbConn.setMaxRows(0);
    int updated = dbConn.executeUpdate(statement, instance);

    LOGGER.log(Level.INFO, "Query executed: [{0}] with {1}, rows updated: {2}",
      new Object[]{statement, instance, updated});
  }

}
