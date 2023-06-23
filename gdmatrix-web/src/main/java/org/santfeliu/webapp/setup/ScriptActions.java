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
import java.util.List;

/**
 *
 * @author blanquepa
 */
public class ScriptActions implements Serializable
{
  private String scriptName;
  private List<Action> actions;
  
  public String getScriptName()
  {
    return scriptName;
  }

  public void setScriptName(String scriptName)
  {
    this.scriptName = scriptName;
  }

  public List<Action> getActions()
  {
    return actions;
  }

  public void setActions(List<Action> actions)
  {
    this.actions = actions;
  }
  
  public Action getAction(String name)
  {
    Action action = null;
    if (actions != null && name != null)
    {
      action = actions.stream()
        .filter(a -> name.equals(a.getName()))
        .findFirst()
        .orElse(null);
    }
    return action;
  }
  
  public class Action
  {
    private String label;
    private String name;
    private String icon;
    
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
   
  }
   
}
