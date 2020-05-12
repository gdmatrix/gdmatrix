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
package org.santfeliu.misc.mapviewer.web;

import java.io.Serializable;
import java.util.HashMap;
import org.santfeliu.misc.mapviewer.SLDStore;

/**
 *
 * @author realor
 */
public class SLDCache implements Serializable
{
  private HashMap<String, String> map = new HashMap<String, String>();

  public String getSldUrl(String sldName)
  {
    String url = null;
    try
    {
      url = map.get(sldName);
      if (url == null)
      {
        url = SLDStore.getSldURL(sldName);
        if (url != null) map.put(sldName, url);
      }
    }
    catch (Exception ex)
    {
    }
    return url;
  }

  public void clear()
  {
    map.clear();
  }
}
