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
package org.santfeliu.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.matrix.workflow.Variable;
import org.matrix.workflow.WorkflowConstants;

/**
 *
 * @author unknown
 */
public class VariableListConverter
{
  public static Map<String, Object> toMap(List<Variable> varList)
  {
    if (varList == null) return null;
    HashMap<String, Object> map = new HashMap<String, Object>();
    for (Variable var : varList)
    {
      String name = var.getName();
      String type = var.getType();
      String svalue = var.getValue();
      Object value = null;
      if (svalue == null)
      {
        value = null;
      }
      else if (WorkflowConstants.TEXT_TYPE.equals(type))
      {
        value = svalue;
      }
      else if (WorkflowConstants.NUMBER_TYPE.equals(type))
      {
        value = Double.valueOf(svalue);
      }
      else if (WorkflowConstants.BOOLEAN_TYPE.equals(type))
      {
        value = Boolean.valueOf(svalue);
      }
      else
      {
        value = svalue;
      }
      map.put(name, value);
    }
    return map;
  }

  public static List<Variable> toList(Map map)
  {
    if (map == null) return null;
    List<Variable> varList = new ArrayList<Variable>();
    for (Object e : map.entrySet())
    {
      Map.Entry entry = (Map.Entry)e;
      String name = (String)entry.getKey();
      Object value = entry.getValue();
      Variable variable = new Variable();
      variable.setName(name);
      variable.setValue(value == null ? null : value.toString());
      variable.setType(getType(value));
      varList.add(variable);
    }
    return varList;
  }

  private static String getType(Object value)
  {
    if (value instanceof Number)
    {
      return WorkflowConstants.NUMBER_TYPE;
    }
    else if (value instanceof Boolean)
    {
      return WorkflowConstants.BOOLEAN_TYPE;
    }
    else
    {
      return WorkflowConstants.TEXT_TYPE;
    }
  }
}
