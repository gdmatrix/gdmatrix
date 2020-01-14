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
package org.santfeliu.util.script.function;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 *
 * @author realor
 */

/*
 * Usage: webCounter(name)
 *
 * returns: ''
 *
 * Example:
 *
 *   ${webCounter(name)} => 
 *
 */
public class WebCounterFunction extends BaseFunction
{
  @Override
  public Object call(Context cx, Scriptable scope, Scriptable thisObj,
    Object[] args)
  {
    if (args.length > 0)
    {
      String name = String.valueOf(args[0]);
      SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHH");
      String dt = df.format(new Date());
      String day = dt.substring(0, 8);
      String hour = dt.substring(8);

      try
      {
        javax.naming.Context initContext = new InitialContext();
        javax.naming.Context envContext  =
           (javax.naming.Context)initContext.lookup("java:/comp/env");
        DataSource ds = (DataSource)envContext.lookup("jdbc/matrix");
        Connection conn = ds.getConnection();
        try
        {
          PreparedStatement prepStmt = conn.prepareStatement(
            "UPDATE WEB_COUNTER SET COUNTER = COUNTER + 1 " +
            "WHERE NAME = ? AND DAY = ? AND HOUR = ?");
          try
          {
            prepStmt.setString(1, name);
            prepStmt.setString(2, day);
            prepStmt.setString(3, hour);
            int updated = prepStmt.executeUpdate();
            if (updated == 0)
            {
              PreparedStatement prepStmt2 = conn.prepareStatement(
                "INSERT INTO WEB_COUNTER (NAME, DAY, HOUR, COUNTER) VALUES " +
                "(?, ?, ?, 1)");
              try
              {
                prepStmt2.setString(1, name);
                prepStmt2.setString(2, day);
                prepStmt2.setString(3, hour);
                prepStmt2.executeUpdate();
              }
              finally
              {
                prepStmt2.close();
              }
            }
          }
          finally
          {
            prepStmt.close();
          }          
        }
        finally
        {
          conn.close();
        }
      }
      catch (Exception ex)
      {
      }
    }
    return "";
  }
}
