package org.santfeliu.util.script.function;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 *
 * @author blanquepa
 */
public class GetBeanFunction extends BaseFunction
{
  @Override
  public Object call(Context cx, Scriptable scope, Scriptable thisObj,
    Object[] args)
  {
    if (args.length == 1)
    {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      Application application = facesContext.getApplication();
      return application.getVariableResolver().resolveVariable(facesContext, (String)args[0]);
    }

    return null;
  }  
}
