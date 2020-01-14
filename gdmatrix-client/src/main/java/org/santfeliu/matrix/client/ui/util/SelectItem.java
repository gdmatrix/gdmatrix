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
package org.santfeliu.matrix.client.ui.util;

/**
 *
 * @author blanquepa
 */
public class SelectItem implements Comparable
{
  private String id;
  private String name;
  private boolean favorite = false;

  public SelectItem(String id, String name)
  {
    if (id.startsWith("*"))
    {
      favorite = true;
      id = id.substring(1);
    }
    this.id = id;
    this.name = name;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public boolean isFavorite()
  {
    return favorite;
  }

  public void setFavorite(boolean favorite)
  {
    this.favorite = favorite;
  }

  @Override
  public String toString()
  {
    return name;
  }

  @Override
  public boolean equals(Object o)
  {
    if (o instanceof SelectItem)
      return id.equals(((SelectItem)o).id);
    else
      return false;
  }

  @Override
  public int hashCode()
  {
    int hash = 3;
    hash = 23 * hash + (this.id != null ? this.id.hashCode() : 0);
    hash = 23 * hash + (this.name != null ? this.name.hashCode() : 0);
    return hash;
  }

  @Override
  public int compareTo(Object o)
  {
    int compareFav = new Boolean(favorite).compareTo(((SelectItem)o).isFavorite());
    if (compareFav != 0)
      return -1 * compareFav;
    else 
    {
      if (favorite && "".equals(id))
        return 1;
      else if (favorite && "".equals(((SelectItem)o).getId()))
        return -1;
      else
        return name.compareTo(((SelectItem)o).name);
    }
  }
} 
