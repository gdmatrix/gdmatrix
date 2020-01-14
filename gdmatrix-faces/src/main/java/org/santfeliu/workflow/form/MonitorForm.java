package org.santfeliu.workflow.form;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author realor
 */
public class MonitorForm extends Form
{
  @Override
  public Set getReadVariables()
  {
    HashSet set = new HashSet();

    String messageVarName = (String)getParameters().get("messageVar");
    if (messageVarName != null) set.add(messageVarName);

    String endVarName = (String)getParameters().get("endVar");
    if (endVarName != null) set.add(endVarName);

    return set;
  }

  @Override
  public Set getWriteVariables()
  {
    HashSet set = new HashSet();

    String cancelVarName = (String)getParameters().get("cancelVar");
    if (cancelVarName != null) set.add(cancelVarName);

    return set;
  }
}
