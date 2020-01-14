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
package org.matrix.util;

import org.matrix.dic.DictionaryConstants;

/**
 *
 * @author realor
 */
public class InternalEntity extends Entity
{
  private String prefix;

  InternalEntity(WSEndpoint endpoint, String name)
  {
    super(endpoint, name);
  }

  public String getPrefix()
  {
    return prefix;
  }

  public void setPrefix(String prefix)
  {
    this.prefix = prefix;
  }

  @Override
  public String toLocalId(String globalId)
  {
    String localId;
    if (globalId == null)
    {
      localId = null;
    }
    else
    {
      int index = globalId.indexOf(WSDirectory.PREFIX_SEPARATOR);
      if (index == -1)
      {
        // globalId has no prefix
        localId = globalId;
      }
      else
      {
        if (prefix != null)
        {
          // check prefix
          String prf = globalId.substring(0, index);
          if (!prf.equals(prefix))
            throw new InvalidIdentifierException();
        }
        localId = globalId.substring(index + 1);
      }
    }
    return localId;
  }

  @Override
  public String toGlobalId(String localId)
  {
    String globalId;

    if (localId == null)
    {
      globalId = null;
    }
    else
    {
      int index = localId.indexOf(WSDirectory.PREFIX_SEPARATOR);
      if (index != -1) // localId has prefix
      {
        String tempId = localId.substring(index + 1);
        if (DictionaryConstants.rootTypeIds.contains(tempId))
        {
          // it's root type, remove prefix
          globalId = tempId;
        }
        else
        {
          // it's not a root type, preserve prefix
          globalId = localId;
        }
      }
      else // localId has no prefix
      {
        if (DictionaryConstants.rootTypeIds.contains(localId))
        {
          globalId = localId; // it's root type
        }
        else
        {
          globalId = (prefix == null) ?
            localId : prefix + WSDirectory.PREFIX_SEPARATOR + localId;
        }
      }
    }
    return globalId;
  }

  @Override
  public String toString()
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append("InternalEntity(");
    buffer.append(name);
    if (prefix != null)
    {
      buffer.append(",");
      buffer.append(prefix);
    }
    buffer.append(")");
    return buffer.toString();
  }
}
