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
package org.santfeliu.dbf;


/**
 * Stores table metadata
 * 
 * Single operations on a table row: select, insert, update and delete have
 * built-in DBStatements. These DBStatements only accept parameters with this
 * syntax:
 * <tt>{k0}</tt>: where <tt>k0</tt> is a key in parameters <tt>Map</tt> and
 * its associated value is the first component of the primary key 
 * (component index is zero based).
 * <tt>{o.field}</tt>: where <tt>o</tt> is a key in parameters <tt>Map</tt>
 * and its associated value is another <tt>Map</tt> that contains a key named 
 * <tt>field</tt> associated to the parameter value.
 * 
 */

/**
 *
 * @author realor
 */
public class DBTableMetaData
{
  String tableName;
  String[] columnNames;
  Class[] columnClasses;
  int[] primaryKeyColumnIndices;
  String columnsString;
  
  DBStatement selectStatement;
  DBStatement insertStatement;
  DBStatement updateStatement;
  DBStatement deleteStatement;

  /*
   * primaryKeyColumnIndices cannot be an empty array 
   */ 
  DBTableMetaData(String tableName, 
                  String[] columnNames, 
                  Class[] columnClasses,
                  int[] primaryKeyColumnIndices)
  {
    this.tableName = tableName;
    this.primaryKeyColumnIndices = primaryKeyColumnIndices;
    this.columnNames = columnNames;
    this.columnClasses = columnClasses;
    buildColumnsString();
    buildSelectStatement();
    buildInsertStatement();
    buildUpdateStatement();
    buildDeleteStatement();
  }

  public int getColumnCount()
  {
    return columnNames.length;
  }

  public int getColumnIndex(String columnName)
  {
    boolean found = false;
    int index = 0;
    while (index < columnNames.length && !found)
    {
      if (columnNames[index].equalsIgnoreCase(columnName)) found = true;
      else index++;
    }
    return found ? index : -1;
  }
  
  public String getColumnName(int index)
  {
    return columnNames[index];
  }

  public Class getColumnClass(int index)
  {
    return columnClasses[index];
  }
  
  public Class getColumnClass(String columnName)
  {
    int index = getColumnIndex(columnName);
    return index == -1 ? null : columnClasses[index];
  }
  
  @Override
  public String toString()
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append(tableName);
    buffer.append("={");
    buffer.append(columnNames[0]);
    buffer.append(":");
    buffer.append(columnClasses[0].getName());
    for (int i = 1; i < columnNames.length; i++)
    {
      buffer.append(", ");
      buffer.append(columnNames[i]);
      buffer.append(":");
      buffer.append(columnClasses[i].getName());
    }
    buffer.append("}");
    buffer.append(" PK={");
    buffer.append(columnNames[primaryKeyColumnIndices[0]]);
    for (int i = 1; i < primaryKeyColumnIndices.length; i++)
    {
      buffer.append(", ");
      buffer.append(columnNames[primaryKeyColumnIndices[i]]);
    }
    buffer.append("}");
    return buffer.toString();
  }
  
  public String getTableName()
  {
    return tableName;
  }
  
  public String[] getPrimaryKeyColumnNames()
  {
    String[] names = new String[primaryKeyColumnIndices.length];

    for (int i = 0; i < primaryKeyColumnIndices.length; i++)
    {
      names[i] = columnNames[primaryKeyColumnIndices[i]];
    }
    return names;
  }

  public int[] getPrimaryKeyColumnIndices()
  {
    int[] indices = new int[primaryKeyColumnIndices.length];
    
    for (int i = 0; i < indices.length; i++)
    {
      indices[i] = primaryKeyColumnIndices[i];
    }
    return indices;
  }
  
  public boolean isPrimaryKeyColumn(String columnName)
  {
    int index = getColumnIndex(columnName);
    return isPrimaryKeyColumn(index);
  }

  public boolean isPrimaryKeyColumn(int index)
  {
    boolean found = false;
    int i = 0;
    while (!found && i < primaryKeyColumnIndices.length)
    {
      if (primaryKeyColumnIndices[i] == index) found = true;
      else i++;
    }
    return found;
  }

  private void buildColumnsString()
  {
    StringBuffer buffer = new StringBuffer();
    buffer.append(columnNames[0]);
    for (int i = 1; i < columnNames.length; i++)
    {
      buffer.append(", ");
      buffer.append(columnNames[i]);
    }
    columnsString = buffer.toString();
  }

  DBStatement buildUpdateStatement(String[] colNames)
  {
    StringBuilder buffer = new StringBuilder();
    String[] params = new String[colNames.length + 
                                 primaryKeyColumnIndices.length];
    Class[] paramClasses = new Class[params.length];                               
                                 
    buffer.append("UPDATE ");
    buffer.append(tableName);
    buffer.append(" SET ");
    buffer.append(colNames[0]);
    buffer.append("=?");
    params[0] = "o." + colNames[0];
    paramClasses[0] = getColumnClass(colNames[0]);
    for (int i = 1; i < colNames.length; i++)
    {
      buffer.append(", ");
      buffer.append(colNames[i]);
      buffer.append("=?");
      params[i] = "o." + colNames[i];
      paramClasses[i] = getColumnClass(colNames[i]);
    }
    buffer.append(" WHERE ");
    int index = primaryKeyColumnIndices[0];
    buffer.append(columnNames[index]);
    buffer.append("=?");
    params[colNames.length] = "k0";
    paramClasses[colNames.length] = columnClasses[index];
    for (int i = 1; i < primaryKeyColumnIndices.length; i++)
    {
      buffer.append(" AND ");
      index = primaryKeyColumnIndices[i];
      buffer.append(columnNames[index]);
      buffer.append("=?");
      params[colNames.length + i] = "k" + i;
      paramClasses[colNames.length + i] = columnClasses[index];
    }
    String sql = buffer.toString();
    return new DBStatement(sql, params, paramClasses);
  }

  private void buildSelectStatement()
  {
    StringBuilder buffer = new StringBuilder();
    String[] params = new String[primaryKeyColumnIndices.length];
    Class[] paramClasses = new Class[params.length];

    buffer.append("SELECT ");
    buffer.append(columnsString);
    buffer.append(" FROM ");
    buffer.append(tableName);
    buffer.append(" WHERE ");
    int index = primaryKeyColumnIndices[0];
    buffer.append(columnNames[index]);
    buffer.append("=?");
    params[0] = "k0";
    paramClasses[0] = columnClasses[index];
    for (int i = 1; i < primaryKeyColumnIndices.length; i++)
    {
      buffer.append(" AND ");
      index = primaryKeyColumnIndices[i];
      buffer.append(columnNames[index]);
      buffer.append("=?");    
      params[i] = "k" + i;
      paramClasses[i] = columnClasses[index];
    }
    String sql = buffer.toString();
    selectStatement = new DBStatement(sql, params, paramClasses);
  }

  private void buildInsertStatement()
  {
    StringBuilder buffer = new StringBuilder();
    String[] params = new String[columnNames.length];
    Class[] paramClasses = new Class[params.length];

    buffer.append("INSERT INTO ");
    buffer.append(tableName);
    buffer.append(" (");
    buffer.append(columnsString);
    buffer.append(") VALUES (?");
    params[0] = "o." + columnNames[0];
    paramClasses[0] = columnClasses[0];
    for (int i = 1; i < columnNames.length; i++)
    {
      buffer.append(", ?");
      params[i] = "o." + columnNames[i];
      paramClasses[i] = columnClasses[i];
    }
    buffer.append(")");
    String sql = buffer.toString();
    insertStatement = new DBStatement(sql, params, paramClasses);
  }
  
  private void buildUpdateStatement()
  {
    updateStatement = buildUpdateStatement(columnNames);
  }

  private void buildDeleteStatement()
  {
    StringBuilder buffer = new StringBuilder();
    String[] params = new String[primaryKeyColumnIndices.length];
    Class[] paramClasses = new Class[params.length];

    buffer.append("DELETE ");
    buffer.append(tableName);
    buffer.append(" WHERE ");
    int index = primaryKeyColumnIndices[0];
    buffer.append(columnNames[index]);
    buffer.append("=?");
    params[0] = "k0";
    paramClasses[0] = columnClasses[index];
    for (int i = 1; i < primaryKeyColumnIndices.length; i++)
    {
      buffer.append(" AND ");
      index = primaryKeyColumnIndices[i];
      buffer.append(columnNames[index]);
      buffer.append("=?");
      params[i] = "k" + i;
      paramClasses[i] = columnClasses[index];
    }
    String sql = buffer.toString();
    deleteStatement = new DBStatement(sql, params, paramClasses);
  }
  
  public static void main(String[] args)
  {
    String[] columnNames = {"NOM", "COGNOM", "DNI"};
    Class[] columnClasses = {String.class, String.class, String.class};
    int[] kf = {0, 1};
    DBTableMetaData md = 
      new DBTableMetaData("TEST", columnNames, columnClasses, kf);

    System.out.println(md);    
    System.out.println(md.selectStatement);
    System.out.println(md.insertStatement);
    System.out.println(md.updateStatement);
    System.out.println(md.deleteStatement);    
  }
}