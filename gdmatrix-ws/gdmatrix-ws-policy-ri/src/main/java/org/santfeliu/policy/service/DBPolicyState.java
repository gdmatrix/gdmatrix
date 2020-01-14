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
package org.santfeliu.policy.service;

import org.matrix.policy.PolicyState;

/**
 *
 * @author unknown
 */
public class DBPolicyState
{
  public static String toDB(PolicyState state)
  {
    if (state == null) return "P";
    String stateValue = null;
    switch (state)
    {
      case PENDENT: stateValue = "P"; break;
      case APPROVED: stateValue = "A"; break;
      case EXECUTED: stateValue = "E"; break;
      case CANCELLED: stateValue = "C"; break;
      case CONFLICT: stateValue = "F"; break;
      case EXECUTING: stateValue = "X"; break;
      case FAILED: stateValue = "L"; break;
    }
    return stateValue;
  }

  public static PolicyState fromDB(String stateValue)
  {
    PolicyState state = null;
    if ("P".equals(stateValue))
    {
      state = PolicyState.PENDENT;
    }
    else if ("A".equals(stateValue))
    {
      state = PolicyState.APPROVED;
    }
    else if ("E".equals(stateValue))
    {
      state = PolicyState.EXECUTED;
    }
    else if ("C".equals(stateValue))
    {
      state = PolicyState.CANCELLED;
    }
    else if ("F".equals(stateValue))
    {
      state = PolicyState.CONFLICT;
    }
    else if ("X".equals(stateValue))
    {
      state = PolicyState.EXECUTING;
    }
    else if ("L".equals(stateValue))
    {
      state = PolicyState.FAILED;
    }
    else
    {
      state = PolicyState.PENDENT;
    }
    return state;
  }
}

