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
package org.santfeliu.webapp.modules.geo.expr;

import java.util.HashMap;

/**
 *
 * @author realor
 */
public class OgcExpression
{
  final static HashMap<String, String> nativeToOgc = new HashMap<>();
  final static HashMap<String, String> ogcToNative = new HashMap<>();

  static
  {
    register("Add", Function.ADD);
    register("Sub", Function.SUB);
    register("Mul", Function.MUL);
    register("Div", Function.DIV);
    register("And", Function.AND);
    register("Or", Function.OR);
    register("Not", Function.NOT);
    register("PropertyIsEqualTo", Function.EQUAL_TO);
    register("PropertyIsNotEqualTo", Function.NOT_EQUAL_TO);
    register("PropertyIsLessThan", Function.LESS_THAN);
    register("PropertyIsLessThanOrEqualTo", Function.LESS_EQUAL_THAN);
    register("PropertyIsGreaterThan", Function.GREATER_THAN);
    register("PropertyIsGreaterThanOrEqualTo", Function.GREATER_EQUAL_THAN);
    register("PropertyIsLike", Function.LIKE);
    register("PropertyIsNull", Function.IS_NULL);
    register("PropertyIsBetween", Function.BETWEEN);
  }

  static void register(String ogcFunction, String nativeFunction)
  {
    nativeToOgc.put(nativeFunction, ogcFunction);
    ogcToNative.put(ogcFunction, nativeFunction);
  }

  public static String getNativeFunction(String ogcFunction)
  {
    return ogcToNative.get(ogcFunction);
  }

  public static String getOgcFunction(String nativeFunction)
  {
    return nativeToOgc.get(nativeFunction);
  }
}
