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

import java.util.Vector;

/**
 * Encapsulates a SQL statement with parameters
 * 
 * Parameters in the statement are specified within braces.
 * 
 * Example:
 * <code>select name, '56{d}' from persona 
 * where nif = {o.nom} and sex = {o.sex} and city like {city}</code>
 *
 * Parameter {d} is ignored because it is inside a literal.
 *
 */
/**
 *
 * @author unknown
 */
public class DBStatement 
{ 
  String sql;
  String[] params;
  Class[] paramClasses;

  DBStatement(String statement)
    throws DBException
  {
    parse(statement);
  }

  DBStatement(String sql, String[] params, Class[] paramClasses)
  {
    this.sql = sql;
    this.params = params;
    this.paramClasses = paramClasses;
    //System.out.println(this);
  }

  public String getSql()
  {
    return sql;
  }

  public String[] getParameters()
  {
    return params;
  }
  
  public Class[] getParameterClasses()
  {
    return paramClasses;
  }
  
  public String toString()
  {
    StringBuffer buffer = new StringBuffer(sql);
    if (params.length > 0)
    {
      buffer.append(" (");
      buffer.append(params[0]);
      Class cls = paramClasses[0];
      if (cls != null)
      {
        buffer.append("<");
        buffer.append(cls.getName());        
        buffer.append(">");
      }
      for (int i = 1; i < params.length; i++)
      {
        buffer.append(", ");
        buffer.append(params[i]);
        cls = paramClasses[i];
        if (cls != null)
        {
          buffer.append("<");
          buffer.append(cls.getName());        
          buffer.append(">");
        }
      }
      buffer.append(")");
    }
    return buffer.toString();
  }
  
  private void parse(String statement)
    throws DBException
  {
    Vector vparams = new Vector();
    StringBuffer bufferSql = new StringBuffer();
    StringBuffer bufferParam = new StringBuffer();
    int index = 0;
    int state = 0;
    while (index < statement.length())
    {
      char ch = statement.charAt(index++);
      switch (state)
      {
        case 0:
          if (ch == '{')
          { 
            bufferParam.setLength(0);
            state = 1;
          }
          else
          {
            bufferSql.append(ch);
            if (ch == '\'') state = 2;
            else if (ch == '\"') state = 3;
          }
          break;
        case 1:
          if (ch == '}')
          { 
            bufferSql.append("?");
            vparams.add(bufferParam.toString());
            state = 0;
          }
          else bufferParam.append(ch);
          break;
        case 2:
          if (ch == '\'')
          {
            state = 0;
          }
          bufferSql.append(ch);
          break;
        case 3:
          if (ch == '\"')
          {
            state = 0;
          }
          bufferSql.append(ch);
          break;
      }
    }
    if (state != 0) throw new DBException("PARSE_ERROR");
    sql = bufferSql.toString();
    params = new String[vparams.size()];
    paramClasses = new Class[vparams.size()];
    vparams.toArray(params);
  }

  public static void main(String[] args)
  {
    try
    {
      DBStatement dbStmt = new DBStatement(
        "select '{d}' \"{o}\" from persona where a = {dds.nom} and s = {ddd}");
      System.out.println(dbStmt);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}