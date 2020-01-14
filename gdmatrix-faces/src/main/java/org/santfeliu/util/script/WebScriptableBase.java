package org.santfeliu.util.script;

import java.util.Map;

import org.mozilla.javascript.Context;

public class WebScriptableBase extends ScriptableBase
{
  public WebScriptableBase(Context context, Map persistentVariables,
    String nonPersistentPrefix)
  {
    super(context, persistentVariables, nonPersistentPrefix);
    WebFunctionFactory.initFunctions(this);
  }
  
  public WebScriptableBase(Context context, Map persistentVariables)
  {
    this(context, persistentVariables, "_");
  }

  public WebScriptableBase(Context context)
  {
    this(context, null);
  }

}

