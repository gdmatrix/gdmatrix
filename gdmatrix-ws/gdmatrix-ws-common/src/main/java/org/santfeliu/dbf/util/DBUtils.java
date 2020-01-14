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
package org.santfeliu.dbf.util;

import java.util.HashMap;

import java.util.Map;

import org.santfeliu.dbf.DBConnection;
import org.santfeliu.dbf.DBException;
import org.santfeliu.dbf.DBKey;
import org.santfeliu.dbf.DBTableMetaData;

/**
 *
 * @author unknown
 */
public class DBUtils
{
  public DBUtils()
  {
  }
  
  public static int getSequenceValue(DBConnection conn, 
                                     String tableName,
                                     DBKey pk, 
                                     String sequenceColumnName)
    throws Exception
  {
    int sequenceValue;
  
    HashMap variables = new HashMap();
    DBTableMetaData metaData = conn.getMetaData(tableName);
    String[] pkColumnNames = metaData.getPrimaryKeyColumnNames();

    String statement = "update " + tableName + 
      " set " + sequenceColumnName + "=" + sequenceColumnName + " + 1 where " + 
      pkColumnNames[0] + "={k0}";
    variables.put("k0", pk.getColumnValue(0));
    for (int i = 1; i < pk.getColumnCount(); i++)
    {
      statement += " and " + pkColumnNames[i] + "={k" + i + "}"; 
      variables.put("k" + i, pk.getColumnValue(i));
    }

    int numUpdated = conn.executeUpdate(statement, variables);
    if (numUpdated == 1) // counter row exists
    {
      Map sequence = conn.selectMap(tableName, pk);
      Object value = sequence.get(sequenceColumnName);
      if (value instanceof Number)
      {
        Number num = (Number)value;
        sequenceValue = num.intValue();
      }
      else if (value instanceof String)
      {
        String s = (String)value;
        sequenceValue = Integer.parseInt(s);
      }
      else
      {
        throw new DBException("Invalid column type");
      }
    }
    else // counter row do not exists, then create it.
    {
      sequenceValue = 0;
      Map sequence = new HashMap();
      for (int i = 0; i < pk.getColumnCount(); i++)
      {
        sequence.put(pkColumnNames[i], pk.getColumnValue(i));
      }
      sequence.put(sequenceColumnName, new Integer(sequenceValue));
      conn.insert(tableName, sequence);
    }
    return sequenceValue;
  }
}
