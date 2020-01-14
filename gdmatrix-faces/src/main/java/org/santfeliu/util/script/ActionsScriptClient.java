package org.santfeliu.util.script;

import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author blanquepa
 */
public class ActionsScriptClient extends ScriptClient
{
  public static final String ACTION_PARAM = "action";
  
  public ActionsScriptClient()
  {
    super();
    this.userId = MatrixConfig.getProperty("adminCredentials.userId");
    this.password = MatrixConfig.getProperty("adminCredentials.password");
  }
  
  public ActionsScriptClient(String userId, String password)
  {
    super(userId, password);
  }  
  
  /* Put objects into scope */
  @Override
  public void put(String key, Object object)
  {
    if (scope == null)
      scope = new WebScriptableBase(context);

    scope.put(key, scope, object);
  } 

  @Override
  public Object executeScript(String action)
    throws Exception
  {
    Object result = null;
    if (action != null)
    {
      action = action.substring(action.indexOf(":") + 1); //supress prefix
      if (action.contains("."))
      {
        String scriptName = action.substring(0, action.indexOf("."));
        action = action.substring(action.indexOf(".") + 1);  
        if (action.contains("?"))
        {
          String[] parts = action.split("\\?");
          action = parts[0];
          String[] params = parts[1].split("&");
          for (String param : params)
          {
            String[] pparts = param.split("=");
            String name = pparts[0];
            String value = pparts[1];
            put(name, value);
          }
        }
        put(ACTION_PARAM, action);
        result = executeScript(scriptName, scope);            
      }
      else
        result = executeScript(action, scope);
    }
    return result;
  }  
}
