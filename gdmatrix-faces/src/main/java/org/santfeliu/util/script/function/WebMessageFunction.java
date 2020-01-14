package org.santfeliu.util.script.function;

import javax.faces.application.FacesMessage;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.santfeliu.faces.FacesUtils;

/**
 *
 * @author blanquepa
 */
public class WebMessageFunction extends BaseFunction
{
  @Override
  public Object call(Context cx, Scriptable scope, Scriptable thisObj,
    Object[] args)
  {
    if (args.length > 0 && args.length <= 2)
    {
      String message = null;
      String severity = "info";
      message = (String)args[0];
      if (args.length == 2)
        severity = (String)args[1];

      if ("info".equals(severity))
        FacesUtils.addMessage(message, null, FacesMessage.SEVERITY_INFO);
      else if ("warn".equals(severity))
        FacesUtils.addMessage(message, null, FacesMessage.SEVERITY_WARN);
      else if ("error".equals(severity))
        FacesUtils.addMessage(message, null, FacesMessage.SEVERITY_ERROR);
      else if ("fatal".equals(severity))
        FacesUtils.addMessage(message, null, FacesMessage.SEVERITY_FATAL);

      return true;
    }
    return false;
  }
}
