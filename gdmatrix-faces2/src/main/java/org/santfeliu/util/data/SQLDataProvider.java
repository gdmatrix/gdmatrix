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
package org.santfeliu.util.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.matrix.sql.QueryParameter;
import org.matrix.sql.QueryParameters;
import org.matrix.sql.QueryRow;
import org.matrix.sql.QueryTable;
import org.matrix.sql.SQLManagerPort;
import org.matrix.sql.SQLManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.util.Table;

/**
 *
 * @author realor
 */
public class SQLDataProvider implements DataProvider
{
  private String query;
  private String connection;
  private String username;
  private String password;
  private List<String> parameters;

  public void init(String reference)
  {
    // parse reference: <connection>:<username>:<password>:<query>
    int index = reference.indexOf(":");
    if (index != -1)
    {
      setConnection(reference.substring(0, index));
      reference = reference.substring(index + 1);
      index = reference.indexOf(":");
      if (index != -1)
      {
        setUsername(reference.substring(0, index));
        reference = reference.substring(index + 1);
        index = reference.indexOf(":");
        if (index != -1)
        {
          setPassword(reference.substring(0, index));
          setQuery(reference.substring(index + 1));
        }
      }
    }
  }

  public String getConnection()
  {
    return connection;
  }

  public void setConnection(String connection)
  {
    this.connection = connection;
  }

  public String getQuery()
  {
    return query;
  }

  public void setQuery(String query)
  {
    this.query = query;
    parameters = null;
  }

  public String getUsername()
  {
    return username;
  }

  public void setUsername(String username)
  {
    if (StringUtils.isBlank(username)) username = null;
    this.username = username;
  }

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    if (StringUtils.isBlank(password)) username = null;
    this.password = password;
  }

  public Table getData(Map context)
  {
    QueryParameters queryParameters = new QueryParameters();
    for (String parameter : getParameters())
    {
      Object value = context.get(parameter);
      if (value instanceof List) // multivalued properties
      {
        List list = (List)value;
        value = (list.size() > 0) ? list.get(0) : null;
      }
      QueryParameter param = new QueryParameter();
      param.setName(parameter);
      param.setValue(value);
      queryParameters.getParameters().add(param);
    }

    // get port to SQL service
    WSDirectory dir = WSDirectory.getInstance();
    WSEndpoint endpoint = dir.getEndpoint(SQLManagerService.class);
    SQLManagerPort port = endpoint.getPort(SQLManagerPort.class);

    QueryTable queryTable = port.executeAliasQuery(query, queryParameters,
     connection, username, password);
    List<String> columns = queryTable.getColumnName();

    Table table = new Table(columns.toArray(new String[columns.size()]));
    for (int i = 0; i < queryTable.getQueryRow().size(); i++)
    {
      QueryRow row = queryTable.getQueryRow().get(i);
      table.addRow(row.getValues().toArray());
    }
    return table;
  }

  public List<String> getParameters()
  {
    if (parameters == null)
    {
      parameters = new ArrayList();
      Pattern pattern = Pattern.compile("\\{.*?\\}");
      Matcher matcher = pattern.matcher(query);

      while (matcher.find())
      {
        int start = matcher.start();
        int end = matcher.end();
        String parameter = query.substring(start + 1, end - 1).trim();
        if (parameters.indexOf(parameter) == -1)
        {
          parameters.add(parameter);
        }
      }
    }
    return parameters;
  }
}
