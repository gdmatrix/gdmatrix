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
package org.santfeliu.web;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

/**
 *
 * @author blanquepa
 */
public class IconMap implements Map
{
  private String iconsPath;
  private String[] extensions = new String[]{"png", "gif", "jpg"};

  public IconMap(String iconsPath)
  {
    this.iconsPath = iconsPath;
  }

  public String[] getExtensions()
  {
    return extensions;
  }

  public void setExtensions(String[] extensions)
  {
    this.extensions = extensions;
  }

  public int size()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean isEmpty()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean containsKey(Object key)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean containsValue(Object value)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Object get(Object key)
  {
    String result = null;
    String iconPath = iconsPath + key;
    ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
    if (extensions == null)
      extensions = new String[]{"png", "gif", "jpg"};

    for (int i = 0; i < extensions.length; i++)
    {
      try
      {
        URL iconUrl = ctx.getResource(iconPath + "." + extensions[i]);
        if (iconUrl != null)
          return iconPath + "." + extensions[i];
      }
      catch (MalformedURLException ex)
      {
      }
    }

    return result;
  }

  public Object put(Object key, Object value)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Object remove(Object key)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void putAll(Map m)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void clear()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Set keySet()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Collection values()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Set entrySet()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}
