package org.santfeliu.policy.web;

import org.matrix.policy.PolicyManagerPort;
import org.matrix.policy.Policy;
import org.santfeliu.web.obj.ObjectBean;

/**
 *
 * @author realor
 */
public class PolicyBean extends ObjectBean
{
  @Override
  public String getObjectTypeId()
  {
    return "Policy";
  }

  @Override
  public String getDescription()
  {
    String description = getObjectId();
    PolicyMainBean policyMainBean =
      (PolicyMainBean)getBean("policyMainBean");
    String title = policyMainBean.getPolicy().getTitle();
    if (title != null) description += ": " + title;
    return description;
  }

  @Override
  public String getDescription(String objectId)
  {
    String description = objectId;
    try
    {
      PolicyManagerPort port = PolicyConfigBean.getPort();
      Policy policy = port.loadPolicy(objectId);
      description += ": " + policy.getTitle();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return description;
  }

  @Override
  public String remove()
  {
    try
    {
      if (!isNew())
      {
        PolicyManagerPort port = PolicyConfigBean.getPort();
        port.removePolicy(getObjectId());
        removed();
      }
      // store
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "policy_main";
  }
}
