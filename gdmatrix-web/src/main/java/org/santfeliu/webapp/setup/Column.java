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

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author realor
 */
public class Column implements Serializable, Comparable<Column> 
{
  private String label;
  private String name;
  private String styleClass;
  private String expression;
  
  public Column()
  {
  }
  
  public Column(String name, String label)
  {
    this.name = name;        
    this.label = label;
  }
  
  public Column(String name, String label, String styleClass)
  {
    this(name, label);
    this.styleClass = styleClass;
  }
  
  public Column(String name, String label, String styleClass, String expression)
  {
    this(name, label, styleClass);
    this.expression = expression;
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

  public String getStyleClass()
  {
    return styleClass;
  }

  public void setStyleClass(String styleClass)
  {
    this.styleClass = styleClass;
  }

  public String getExpression()
  {
    return expression;
  }

  public void setExpression(String expression)
  {
    this.expression = expression;
  }

  @Override
  public int compareTo(Column o)
  {
    return getName().compareTo(o.getName()); 
  }

  @Override
  public boolean equals(Object o)
  {
    if (o == null)
      return false;

    if (this.getClass() != o.getClass())
      return false;

    return getName().equals(((Column)o).getName());
  }

  @Override
  public int hashCode()
  {
    int hash = 3;
    hash = 97 * hash + Objects.hashCode(getName());
    return hash;
  }  
}
