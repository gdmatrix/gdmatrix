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
package org.santfeliu.dic.proxy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Type;
import org.matrix.dic.TypeFilter;
import org.matrix.util.InternalEntity;
import org.matrix.util.WSEndpoint;

/**
 *
 * @author realor
 */
public class TypeConverter
{
  public static Type toGlobal(Type type, WSEndpoint endpoint)
  {
    Type globalType = endpoint.toGlobal(Type.class, type);
    globalType.setTypePath(toGlobalTypePath(type.getTypePath(), endpoint));
    return globalType;
  }

  public static Type toLocal(Type type, WSEndpoint endpoint)
  {
    Type localType = endpoint.toLocal(Type.class, type);
    localType.setTypePath(toLocalTypePath(type.getTypePath(), endpoint));
    return localType;
  }

  public static TypeFilter toLocal(TypeFilter filter, WSEndpoint endpoint)
  {
    TypeFilter localFilter = endpoint.toLocal(TypeFilter.class, filter);
    localFilter.setTypePath(toLocalTypePath(filter.getTypePath(), endpoint));
    return localFilter;
  }

  public static String toLocalTypePath(String typePath, WSEndpoint endpoint)
  {
    return convertTypePath(typePath, endpoint, 0);
  }

  public static String toGlobalTypePath(String typePath, WSEndpoint endpoint)
  {
    return convertTypePath(typePath, endpoint, 1);
  }

  public static String convertTypePath(String typePath, WSEndpoint endpoint,
    int mode)
  {
    if (typePath == null) return null;

    InternalEntity entity = endpoint.getInternalEntity(
      DictionaryConstants.TYPE_TYPE);

    Pattern pattern = Pattern.compile(
      DictionaryConstants.TYPE_PATH_SEPARATOR + ".*?" +
      DictionaryConstants.TYPE_PATH_SEPARATOR);
    Matcher matcher = pattern.matcher(typePath);

    StringBuilder builder = new StringBuilder();
    int index = 0;
    while (matcher.find(index))
    {
      builder.append(typePath.substring(index, matcher.start()));
      String id = typePath.substring(matcher.start() + 1, matcher.end() - 1);
      builder.append(DictionaryConstants.TYPE_PATH_SEPARATOR);
      if (id.equals(""))
      {
        builder.append("%");
      }
      else if (id.contains("%"))
      {
        builder.append(id);
      }
      else
      {
        if (mode == 0) builder.append(entity.toLocalId(id));
        else builder.append(entity.toGlobalId(id));
      }
      index = matcher.end() - 1;
    }
    builder.append(typePath.substring(index));
    return builder.toString();
  }
}
