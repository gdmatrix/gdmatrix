package org.santfeliu.web.obj;

import java.util.List;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author realor
 */
public class ObjectActionsBean extends PageBean
{
  public List<ObjectAction> getObjectActions()
  {
    ObjectBean objectBean = getObjectBean();
    return objectBean.getObjectActions();
  }

  public void executeAction()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    ObjectAction action = (ObjectAction)getValue("#{action}");
    String expression = action.getExpression();
    userSessionBean.executeAction(expression);
  }

  @Override
  public String show()
  {
    return "object_actions";
  }
}
