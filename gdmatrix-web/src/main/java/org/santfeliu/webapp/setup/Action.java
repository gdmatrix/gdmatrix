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
package org.santfeliu.webapp.setup;

import java.util.Objects;

/**
 *
 * @author blanquepa
 */
public class Action
{
  private static final String URL_PREFIX = "url:";
    
  private String label;
  private String name;
  private Object[] parameters;
  private String icon;

  public Action(String label, String name)
  {
    this.label = label;
    this.name = name;
  }

  public Action(String label, String name, String icon)
  {
    this(label, name);
    this.icon = icon;
  }
  
  public Action(String label, String name, String[] parameters, String icon)
  {
    this(label, name, icon);
    this.parameters = parameters;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getIcon()
  {
    return icon;
  }

  public void setIcon(String icon)
  {
    this.icon = icon;
  }

  public Object[] getParameters()
  {
    return parameters;
  }

  public void setParameters(Object[] parameters)
  {
    this.parameters = parameters;
  }
  
  public boolean isUrlAction()
  {
    return this.name != null && this.name.startsWith(URL_PREFIX);
  }
  
  public String getUrl()
  {
    if (this.name != null && this.name.startsWith(URL_PREFIX))
      return this.name.substring(URL_PREFIX.length());    
    else
      return this.name;
  }
  
  public String getUrlTarget()
  {   
    String url = getUrl();
    return url != null && url.matches("https?://.*") ? "_blank" : "_self";
  }  

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 73 * hash + Objects.hashCode(this.name);
    return hash;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final Action other = (Action) obj;
    return Objects.equals(this.name, other.name);
  }

}
