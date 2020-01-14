package org.santfeliu.workflow.form;

import java.util.HashSet;
import java.util.Set;

public class SelectMenuForm extends Form
{
  public SelectMenuForm()
  {
  }

  public Set getWriteVariables()
  {
    HashSet set = new HashSet();
    set.add((String)parameters.get("var"));
    return set;
  }
}
