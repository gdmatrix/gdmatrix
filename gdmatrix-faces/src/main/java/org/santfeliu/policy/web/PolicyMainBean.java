package org.santfeliu.policy.web;

import java.util.Date;
import java.util.List;
import org.matrix.dic.DictionaryConstants;
import org.matrix.policy.Policy;
import org.matrix.policy.PolicyManagerPort;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.obj.DynamicTypifiedPageBean;
import org.santfeliu.ws.WSExceptionFactory;

/**
 *
 * @author realor
 */
public class PolicyMainBean extends DynamicTypifiedPageBean
{
  private Policy policy;

  public PolicyMainBean()
  {
    super(DictionaryConstants.POLICY_TYPE, "POLICY_ADMIN");
  }

  public Policy getPolicy()
  {
    return policy;
  }

  public void setPolicy(Policy policy)
  {
    this.policy = policy;
  }

  public Date getCreationDateTime()
  {
    return TextUtils.parseInternalDate(policy.getCreationDateTime());
  }

  public Date getChangeDateTime()
  {
    return TextUtils.parseInternalDate(policy.getChangeDateTime());
  }

  @Override
  public String show()
  {
    return "policy_main";
  }

  @Override
  public String store()
  {
    try
    {
      // apply setters
      policy.setPolicyTypeId(getCurrentTypeId());
      policy.getProperty().clear();
      List properties = getFormDataAsProperties();
      if (properties != null)
        policy.getProperty().addAll(properties);

      PolicyManagerPort port = PolicyConfigBean.getPort();
      policy = port.storePolicy(policy);
      setObjectId(policy.getPolicyId());

      setFormDataFromProperties(policy.getProperty());
    }
    catch (Exception ex)
    {
      error(ex);
      List<String> details = WSExceptionFactory.getDetails(ex);
      if (details.size() > 0) error(details);
    }
    return "policy_main";
  }

  protected void load()
  {
    if (isNew())
    {
      policy = new Policy();
    }
    else
    {
      try
      {
        PolicyManagerPort port = PolicyConfigBean.getPort();
        policy = port.loadPolicy(getObjectId());
      }
      catch (Exception ex)
      {
        getObjectBean().clearObject();
        error(ex);
        policy = new Policy();
      }
      setCurrentTypeId(policy.getPolicyTypeId());
      setFormDataFromProperties(policy.getProperty());
    }
  }

  public String searchType()
  {
    return searchType("#{policyMainBean.currentTypeId}");
  }

  public String showType()
  {
    return getControllerBean().showObject("Type", getCurrentTypeId());
  }
  
  public boolean isRenderShowTypeButton()
  {
    return getCurrentTypeId() != null && getCurrentTypeId().trim().length() > 0;
  }

  public String getScripts()
  {
    StringBuilder buffer = new StringBuilder();
    addScriptFile("/plugins/codemirror/codemirror.js", buffer);
    addScriptFile("/plugins/codemirror/javascript.js", buffer);
    addScriptFile("/plugins/codemirror/runmode.js", buffer);
    addScriptFile("/plugins/codemirror/matchbrackets.js", buffer);
    buffer.append("<script type=\"text/javascript\">var editor1 = " +
      "document.getElementById(\"activationDateExpression\");var cm1 = " +
      "CodeMirror.fromTextArea(editor1, " + 
      "{lineNumbers:false, matchBrackets: true, lineWrapping: true});</script>");
    buffer.append("<script type=\"text/javascript\">var editor2 = " +
      "document.getElementById(\"activationCondition\");var cm2 = " +
      "CodeMirror.fromTextArea(editor2, " + 
      "{lineNumbers:false, matchBrackets: true, lineWrapping: true});</script>");
    return buffer.toString();
  }

  private void addScriptFile(String path, StringBuilder buffer)
  {
    String contextPath = getContextPath();
    buffer.append("<script src=\"").append(contextPath);
    buffer.append(path);
    buffer.append("\" type=\"text/javascript\">\n</script>\n");
  }
}
