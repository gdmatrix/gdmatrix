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
package org.matrix.pf.web;

import org.matrix.web.Describable;
import org.santfeliu.web.obj.ObjectDescriptionCache;

/**
 *
 * @author blanquepa
 */
public class ObjectDescriptions
{
  private static final String PF_PREFIX = "pf::";
  
  public static String getDescription(Describable describable, String objectId)
  {
    String description = "";
    if (objectId != null)
    {
      ObjectDescriptionCache cache = ObjectDescriptionCache.getInstance();
      description = cache.getDescription(describable, PF_PREFIX + objectId);
    }
    return description;
  }

  public static void clearDescription(Describable describable, String objectId)
  {
    if (objectId != null)
    {
      ObjectDescriptionCache cache = ObjectDescriptionCache.getInstance();
      cache.clearDescription(describable, PF_PREFIX + objectId);
      cache.clearDescription(describable, objectId);
    }
  }  
}
