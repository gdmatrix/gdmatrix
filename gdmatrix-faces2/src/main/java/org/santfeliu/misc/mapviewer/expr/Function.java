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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author realor
 */
public class Function extends Expression
{  
  public static final String GREATER_THAN = "GREATER_THAN";
  public static final String LESS_THAN = "LESS_THAN";
  public static final String EQUAL_TO = "EQUAL_TO";
  public static final String NOT_EQUAL_TO = "NOT_EQUAL_TO";
  public static final String GREATER_EQUAL_THAN = "GREATER_EQUAL_THAN";
  public static final String LESS_EQUAL_THAN = "LESS_EQUAL_THAN";
  public static final String AND = "AND";
  public static final String OR = "OR";
  public static final String NOT = "NOT";
  public static final String LIST = "LIST";
  public static final String ADD = "ADD";
  public static final String SUB = "SUB";
  public static final String MUL = "MUL";
  public static final String DIV = "DIV";
  public static final String NEGATE = "NEGATE";
  public static final String IN = "IN";
  public static final String NOT_IN = "NOT_IN";
  public static final String LIKE = "LIKE";
  public static final String NOT_LIKE = "NOT_LIKE";
  public static final String ILIKE = "ILIKE";
  public static final String NOT_ILIKE = "NOT_ILIKE";
  public static final String BETWEEN = "BETWEEN";
  public static final String NOT_BETWEEN = "NOT_BETWEEN";
  public static final String IS_NULL = "IS_NULL";
  public static final String IS_NOT_NULL = "IS_NOT_NULL";
  
  private static final HashMap<String, Expression> composedFunctions = 
    new HashMap<String, Expression>();
  
  public static Function expandFunction(Function function)
  {
    String functionName = function.getName();
    if (functionName.equals(NOT_LIKE))
      return new Function(NOT, new Function(LIKE, function.getArguments()));
    
    if (functionName.equals(IS_NOT_NULL)) 
      return new Function(NOT, new Function(IS_NULL, function.getArguments()));
 
    if (functionName.equals(NOT_BETWEEN))
      return new Function(NOT, new Function(BETWEEN, function.getArguments()));

    if (functionName.equals(NEGATE))
      return new Function(MUL, new Literal(Literal.NUMBER, "-1"), 
         function.getArguments().get(0));  
    
    if (functionName.equals(ILIKE))
      return new Function(LIKE, 
        new Function("strToUpperCase", function.getArguments().get(0)),
        new Function("strToUpperCase", function.getArguments().get(1)));

    if (functionName.equals(NOT_ILIKE))
      return new Function(NOT, new Function(LIKE, 
        new Function("strToUpperCase", function.getArguments().get(0)),
        new Function("strToUpperCase", function.getArguments().get(1))));
    
    return function;
  };
  
  
  private String name;
  private List<Expression> arguments = new ArrayList<Expression>();

  public Function()
  {    
  }
  
  public Function(String name)
  {
    this.name = name;
  }

  public Function(String name, List<Expression> arguments)
  {
    this.name = name;
    this.arguments.addAll(arguments);
  }  
  
  public Function(String name, Expression ... arguments)
  {
    this.name = name;
    this.arguments.addAll(Arrays.asList(arguments));
  }
  
  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }  
  
  public List<Expression> getArguments()
  {
    return arguments;
  }
}
