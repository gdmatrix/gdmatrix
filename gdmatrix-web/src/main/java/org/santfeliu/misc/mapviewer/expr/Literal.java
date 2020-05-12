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
package org.santfeliu.misc.mapviewer.expr;

/**
 *
 * @author realor
 */
public class Literal extends Expression
{
  public static final int NULL = 0;
  public static final int STRING = 1;
  public static final int NUMBER = 2;
  public static final int BOOLEAN = 3;
  public static final int DATE = 4;
  
  public static final String TRUE = "TRUE";
  public static final String FALSE = "FALSE";
  
  private String value;
  private int type;

  public Literal()
  {    
  }
  
  public Literal(int type, String value)
  {
    this.type = type;
    this.value = value;
  }
  
  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public int getType()
  {
    return type;
  }

  public void setType(int type)
  {
    this.type = type;
  }
  
  public void detectType()
  {
    // TODO: detect date literals
    if (value.equals(TRUE) || value.equals(FALSE)) type = BOOLEAN;
    else
    {
      try
      {
        Double.parseDouble(value);
        type = NUMBER;
      }
      catch (NumberFormatException ex)
      {
        type = STRING;
      }
    }
  }
}
