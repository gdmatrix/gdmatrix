package org.santfeliu.util.script.function;

import java.util.ArrayList;
import java.util.HashMap;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.santfeliu.util.script.ActionsScriptClient;
import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.web.obj.ObjectAction;

/**
 *
 * @author blanquepa
 */
public class ObjectActionsManagerFunction extends BaseFunction {
  public static final String OBJECT_ACTIONS_VAR = "_object_actions_";
  public static final String OBJECT_ACTION_BODIES_VAR = "_object_actions_bodies_";

  private DefineActionFunction defineActionFunction = new DefineActionFunction();
  private GetActionListFunction getActionListFunction = new GetActionListFunction();
  private SetActionCodeFunction setActionCodeFunction = new SetActionCodeFunction();
  private ProcessActionsFunction processActionsFunction = new ProcessActionsFunction();

  @Override
  public Object call(Context cx, Scriptable scope, Scriptable thisObj,
    Object[] args)
  {
    if (thisObj == null) return null;

    thisObj.put("defineAction", thisObj, defineActionFunction);
    thisObj.put("getActionList", thisObj, getActionListFunction);
    thisObj.put("setActionCode", thisObj, setActionCodeFunction);
    thisObj.put("processActions", thisObj, processActionsFunction);
    thisObj.put(OBJECT_ACTIONS_VAR, thisObj, null);
    thisObj.put(OBJECT_ACTION_BODIES_VAR, thisObj, null);
    
    FacesContext facesContext = FacesContext.getCurrentInstance();
    Application application = facesContext.getApplication();
    ControllerBean controllerBean = 
      (ControllerBean) application.getVariableResolver().resolveVariable(facesContext, "controllerBean");
    scope.put("controllerBean", scope, controllerBean); 
    scope.put("pageBean", scope, controllerBean.getPageBean());
    scope.put("objectBean", scope, controllerBean.getObjectBean());
    scope.put("objectId", scope, controllerBean.getObjectBean().getObjectId());
  
    return thisObj;
  }

  class DefineActionFunction extends BaseFunction
  {
    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj,
      Object[] args)
    {
      if (args.length > 0)
      {
        Object definitions = thisObj.get(OBJECT_ACTIONS_VAR , scope);
        if (definitions == null)
          definitions = new ArrayList<ObjectAction>();

        String description = (String)args[0];
        String expression = (String)args[1];
        ObjectAction oa = new ObjectAction();
        oa.setDescription(description);
        oa.setExpression(expression);
        if (oa != null)
          ((ArrayList<ObjectAction>)definitions).add(oa);

        thisObj.put(OBJECT_ACTIONS_VAR, thisObj, definitions);
      }
      return false;
    }
  }

  class SetActionCodeFunction extends BaseFunction
  {
    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj,
      Object[] args)
    {
      if (args.length > 0)
      {
        Object bodies = thisObj.get(OBJECT_ACTION_BODIES_VAR , scope);

        if (bodies == null)
          bodies = new HashMap<String,Object>();

        String actionName = (String)args[0];
        Object code = args[1];
        ((HashMap)bodies).put(actionName, code);

        thisObj.put(OBJECT_ACTION_BODIES_VAR, thisObj, bodies);
      }
      return false;
    }
  }

  class ProcessActionsFunction extends BaseFunction
  {
    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj,
      Object[] args)
    {
      if (args.length >= 0)
      {
        String action = (String)scope.get(ActionsScriptClient.ACTION_PARAM, thisObj);
        HashMap bodies = (HashMap)thisObj.get(OBJECT_ACTION_BODIES_VAR, thisObj);
        Object code = bodies.get(action);
        return code;
      }
      return false;
    }
  }
  
  class GetActionListFunction extends BaseFunction
  {
    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj,
      Object[] args)
    {
      if (args.length == 0)
        return thisObj.get(OBJECT_ACTIONS_VAR, thisObj);

      return false;
    }
  }  
}

