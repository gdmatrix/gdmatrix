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
public class ExternalEntity extends Entity
{
  private String from;

  ExternalEntity(WSEndpoint endpoint, String name)
  {
    super(endpoint, name);
  }

  public String getFrom()
  {
    return from;
  }

  public void setFrom(String from)
  {
    this.from = from;
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
      if (index == -1) // globalId has no prefix
      {
        localId = globalId;
      }
      else // globalId has prefix
      {
        if (from == null)
        {
          localId = globalId;
        }
        else
        {
          WSEndpoint fromEndpoint = endpoint.getDirectory().getEndpoint(from);
          InternalEntity entity = fromEndpoint.getInternalEntity(name);
          String prefix = entity.getPrefix();
          if (prefix != null)
          {
            // check prefix
            String idPrefix = globalId.substring(0, index);
            if (!idPrefix.equals(prefix))
              throw new InvalidIdentifierException();

            // remove prefix
            localId = globalId.substring(index + 1);
          }
          else localId = globalId;
        }
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
      if (index != -1) // locaId has prefix
      {
        String tempId = localId.substring(index + 1);
        if (DictionaryConstants.rootTypeIds.contains(tempId))
        {
          // it's root type
          globalId = tempId;
        }
        else
        {
          // preserve prefix
          globalId = localId;
        }
      }
      else // localId has no prefix
      {
        if (DictionaryConstants.rootTypeIds.contains(localId))
        {
          globalId = localId; // it's root type
        }
        else if (from == null)
        {
          globalId = localId; // accepts references from any endpoint
        }
        else
        {
          WSEndpoint fromEndpoint = endpoint.getDirectory().getEndpoint(from);
          InternalEntity entity = fromEndpoint.getInternalEntity(name);
          String prefix = entity.getPrefix();
          
          globalId = (prefix == null)?
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
    buffer.append("ExternalEntity(");
    buffer.append(name);
    if (from != null) buffer.append(",").append(from);
    buffer.append(")");
    return buffer.toString();
  }
}
