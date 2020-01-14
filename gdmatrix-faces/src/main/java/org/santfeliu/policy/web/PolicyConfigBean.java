package org.santfeliu.policy.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.policy.PolicyManagerPort;
import org.matrix.policy.PolicyManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 */
class PolicyConfigBean implements Serializable
{
  static List<SelectItem> actionSelectItems;

  public static PolicyManagerPort getPort() throws Exception
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint =
      wsDirectory.getEndpoint(PolicyManagerService.class);
    return endpoint.getPort(PolicyManagerPort.class,
      UserSessionBean.getCurrentInstance().getUserId(),
      UserSessionBean.getCurrentInstance().getPassword());
  }

  public static List<SelectItem> getActionSelectItems()
  {
    if (actionSelectItems == null)
    {
      List<SelectItem> items = new ArrayList<SelectItem>();
      items.add(new SelectItem("Destroy", "Destruir"));
      items.add(new SelectItem("Transfer", "Tranferir"));
      items.add(new SelectItem("Review", "Revisar"));
      items.add(new SelectItem("RetainPermanently", "Conservació permanent"));
      items.add(new SelectItem("StartWorkflow", "Inicia tramit"));
      items.add(new SelectItem("EmailNotify", "Notificar per correu electronic"));
      items.add(new SelectItem("SMSNotify", "Notificar per SMS"));
      actionSelectItems = items;
    }
    return actionSelectItems;
  }

}
