package org.santfeliu.workflow.form;

import java.util.HashSet;
import java.util.Set;


public class InputTextForm extends Form
{
  public InputTextForm()
  {
  }
  
  public Set getWriteVariables()
  {
    HashSet set = new HashSet();
    set.add((String)parameters.get("var"));
    return set;
  }
}
