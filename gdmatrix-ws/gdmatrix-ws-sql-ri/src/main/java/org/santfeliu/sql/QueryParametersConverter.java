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
package org.santfeliu.sql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.matrix.sql.QueryParameter;
import org.matrix.sql.QueryParameters;

/**
 *
 * @author realor
 */
public class QueryParametersConverter
{
  public QueryParametersConverter()
  {
  }
  
  public static QueryParameters fromMap(Map map)
  {
    QueryParameters result = new QueryParameters();
    Set<Map.Entry> entries = map.entrySet();
    for (Map.Entry entry : entries)
    {
      QueryParameter parameter = new QueryParameter();
      parameter.setName((String)entry.getKey());
      parameter.setValue(entry.getValue());
      result.getParameters().add(parameter);
    }
    
    return result;
  }
  
  public static Map toMap(QueryParameters queryParameters)
  {
    Map result = new HashMap();
    if (queryParameters != null)
    {
      List<QueryParameter> parameters = queryParameters.getParameters();
      for (QueryParameter parameter : parameters)
      {
        result.put(parameter.getName(), parameter.getValue());
      }
    }
    
    return result;
  }
}
