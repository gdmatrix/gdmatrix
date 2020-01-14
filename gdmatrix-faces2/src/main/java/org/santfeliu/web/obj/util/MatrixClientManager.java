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
package org.santfeliu.web.obj.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author blanquepa
 */
public class MatrixClientManager implements Serializable
{
  private static final String EXCEPTION = "exception";
  private static final String RESULT = "result";
  
  private CommandMap commands;
  
  public MatrixClientManager()
  {
    commands = new CommandMap();
  }

  public CommandMap getCommands() 
  {
    return commands;
  }

  public void setCommands(CommandMap commands) 
  {
    this.commands = commands;
  }
  
  public Object parseResult(String command) throws Exception
  {
    Map commandResult = commands.get(command).getResult();
    if (commandResult != null)
    {
      String exception = (String)commandResult.get(EXCEPTION);
      if (exception != null)
        throw new Exception(exception);
      return commandResult.get(RESULT);
    }
    else
      return null;
  }  
  
  public Map getResults(String command)
  {
    return commands.get(command).getResult();
  }
  
  public Map getParameters(String command)
  {
    return commands.get(command).getParameters();
  }
  
  public void resetCommand(String command)
  {
    commands.remove(command);
  }
  
  public void addParameter(String command, String name, Object value)
  {
    Command cmnd = commands.get(command);
    if (cmnd != null)
      cmnd.getParameters().put(name, value);
  }
  
  public class Command implements Serializable
  {
    Map parameters;
    Map result;
    
    public Command()
    {
      parameters = new HashMap();
      result = new HashMap();
    }

    public Map getParameters()
    {
      return parameters;
    }

    public Map getResult()
    {
      return result;
    }

    public void setResult(Map result)
    {
      this.result = result;
    }
  }  
  
  public class CommandMap extends HashMap<String,Command>
  {
    public Command get(String key) 
    {
      Object value = super.get(key);
      if (value == null)
      {
        Command command = new Command();
        put(key, command);
        return command;
      }
      else
        return (Command)value;
    }
  }
}
