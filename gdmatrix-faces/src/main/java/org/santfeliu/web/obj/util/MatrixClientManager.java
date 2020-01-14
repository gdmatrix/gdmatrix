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
